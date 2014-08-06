/*
 * Copyright (c) 2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.bpel.core.ode.integration;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFault;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.TwoChannelAxisOperation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.epr.EndpointFactory;
import org.apache.ode.bpel.epr.MutableEndpoint;
import org.apache.ode.bpel.epr.WSAEndpoint;
import org.apache.ode.bpel.iapi.*;
import org.apache.ode.il.OMUtils;
import org.apache.ode.utils.DOMUtils;
import org.apache.ode.utils.GUID;
import org.apache.ode.utils.Namespaces;
import org.apache.ode.utils.Properties;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.wso2.carbon.bpel.core.ode.integration.axis2.WSDLAwareMessage;
import org.wso2.carbon.bpel.core.ode.integration.utils.SOAPUtils;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import javax.transaction.TransactionManager;
import javax.wsdl.Definition;
import javax.wsdl.Port;
import javax.wsdl.Service;
import javax.wsdl.extensions.UnknownExtensibilityElement;
import javax.wsdl.extensions.http.HTTPAddress;
import javax.wsdl.extensions.soap.SOAPAddress;
import javax.wsdl.extensions.soap12.SOAP12Address;
import javax.xml.namespace.QName;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * This will act as a proxy between BPEL Process deployed in ODE Engine and Axis Service
 * which expose the BPEL Process to the external world.
 * Once the request comes to the service published by BPEL Process, request will handover
 * to the BPELProcessProxy via BPELMessageReceiver.
 */
public class BPELProcessProxy {
    private static final Log log = LogFactory.getLog(BPELProcessProxy.class);

    private BpelServer odeBpelServer;

    private ProcessConf processConfiguration;

    private TransactionManager transactionManager;

    // WSDL Definition of the BPEL Process
    private Definition wsdlDefinition;

    // QName of the service published by the process
    private QName serviceName;

    // Name of the port from WSDL service definition
    private String port;

    private WSAEndpoint serviceReference;

    private AxisService axisService;

    public BPELProcessProxy(final ProcessConf processConf,
                            final BPELServerImpl bpelServer,
                            final QName serviceName,
                            final String port) {
        this.processConfiguration = processConf;
        this.odeBpelServer = bpelServer.getODEBPELServer();
        this.serviceName = serviceName;
        this.port = port;

        this.transactionManager = bpelServer.getTransactionManager();
        this.wsdlDefinition = processConfiguration.getDefinitionForService(serviceName);
        this.serviceReference = EndpointFactory.convertToWSA(createServiceRef(
                genEPRfromWSDL(this.wsdlDefinition, serviceName, port)));
    }

    public final ProcessConf getProcessConfiguration() {
        return processConfiguration;
    }

    public final Definition getWsdlDefinition() {
        return wsdlDefinition;
    }

    public final QName getServiceName() {
        return serviceName;
    }

    public final String getPort() {
        return port;
    }

    public final AxisService getAxisService() {
        return axisService;
    }

    public final void setAxisService(final AxisService axisService) {
        this.axisService = axisService;
    }

    public final WSAEndpoint getServiceReference() {
        return serviceReference;
    }

    public final void onAxisServiceInvoke(final BPELMessageContext bpelMessageContext)
            throws AxisFault {

        boolean success = true;
        MyRoleMessageExchange odeMessageExchange = null;
        Future responseFuture = null;
        // Used to handle exception loosing in when exception occurred
        // inside finally block.
        Exception cachedException = null;

        try {
            transactionManager.begin();
            if (log.isDebugEnabled()) {
                log.debug("Starting Transaction.");
            }
            odeBpelServer.acquireTransactionLocks();

            odeMessageExchange = createMessageExchange(bpelMessageContext.getInMessageContext());

            if (odeMessageExchange.getOperation() != null) {
                responseFuture = invokeBPELProcessThroughODEMessageExchange(
                        odeMessageExchange,
                        bpelMessageContext);
                success = commitODEMessageExchange(odeMessageExchange);
            } else {
                success = false;
            }
        } catch (Exception e) {
            cachedException = e;
            success = false;
            handleExceptionAtODEInvocation(e);
        } finally {
            if (!success) {
                releaseODEMessageExchangeAndRollbackTransaction(
                        odeMessageExchange,
                        cachedException,
                        success);
            }
        }

        if (odeMessageExchange.getOperation().getOutput() != null) {
            waitForTheResponse(responseFuture, odeMessageExchange);

            if (bpelMessageContext.getOutMessageContext() != null) {
                if (log.isDebugEnabled()) {
                    log.debug("Handling response for MEX " + odeMessageExchange);
                }

                setOutMessageContextSOAPEnvelope(bpelMessageContext);

                boolean commit = false;

                beginTransactionForTheResponsePath();

                try {
                    // Refreshing the message exchange
                    odeMessageExchange = (MyRoleMessageExchange) odeBpelServer.getEngine()
                            .getMessageExchange(odeMessageExchange.getMessageExchangeId());
                    onResponse(bpelMessageContext,
                            odeMessageExchange,
                            bpelMessageContext.getOutMessageContext());

                    // Everything went well. So we can commit the transaction now.
                    commit = true;
                } catch (AxisFault af) {
                    cachedException = af;
                    log.warn("MEX produced a fault " + odeMessageExchange, af);
                    commit = true;
                    throw af;
                } catch (Exception e) {
                    cachedException = e;
                    log.error("Error processing response for MEX " + odeMessageExchange, e);
                    throw new BPELFault("An exception occurred when invoking ODE.", e);
                } finally {
                    try {
                        odeMessageExchange.release(commit);
                    } finally {
                        if (commit) {
                            commitTransactionForTheResponsePath(cachedException);
                        } else {
                            rollbackTransactionForTheResponsePath(cachedException);
                        }
                    }
                }
            }
            if (!success) {
                throw new BPELFault("Message was either un-routable or timed out!");
            }

        }

    }

    private void beginTransactionForTheResponsePath() throws BPELFault {
        try {
            if (log.isDebugEnabled()) {
                log.debug("Starting transaction.");
            }
            transactionManager.begin();
        } catch (Exception ex) {
            String errorMessage = "Failed to start transaction!";
            log.error(errorMessage, ex);
            throw new BPELFault(errorMessage, ex);
        }
    }

    private void commitTransactionForTheResponsePath(final Exception cachedException)
            throws BPELFault {
        try {
            if (log.isDebugEnabled()) {
                log.debug("Comitting transaction.");
            }
            transactionManager.commit();
        } catch (Exception e) {
            String errorMessage = "Commiting Response Path Transaction Failed.";
            e.initCause(cachedException);
            log.error(errorMessage, e);
            throw new BPELFault(errorMessage, e);
        }
    }

    private void rollbackTransactionForTheResponsePath(final Exception cachedException)
            throws BPELFault {
        try {
            transactionManager.rollback();
        } catch (Exception e) {
            e.initCause(cachedException);
            throw new BPELFault("Rollback failed!", e);
        }
    }

    private void setOutMessageContextSOAPEnvelope(final BPELMessageContext bpelMessageContext)
            throws AxisFault {
        SOAPEnvelope envelope = bpelMessageContext.getSoapFactoryForCurrentMessageFlow()
                .getDefaultEnvelope();
        bpelMessageContext.getOutMessageContext().setEnvelope(envelope);
    }

    private MyRoleMessageExchange createMessageExchange(final MessageContext inMessageContext) {
        Integer tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        MyRoleMessageExchange messageExchange;
        String messageId = new GUID().toString();
        messageExchange = odeBpelServer.getEngine().createMessageExchange("" + messageId,
                serviceName,
                inMessageContext.getAxisOperation().getName().getLocalPart(), null,
                tenantId.toString());

        if (log.isDebugEnabled()) {
            log.debug("ODE routed to portType " + messageExchange.getPortType()
                    + " operation " + messageExchange.getOperation()
                    + " from service " + serviceName);
        }

        messageExchange.setProperty("isTwoWay", Boolean.toString(
                inMessageContext.getAxisOperation() instanceof TwoChannelAxisOperation));
        return messageExchange;
    }

    private Message createInputMessageToODE(
            final BPELMessageContext bpelMessageContext,
            final MyRoleMessageExchange messageExchange) throws AxisFault {
        // Preparing message to send to ODE
        Message odeRequest = messageExchange.createMessage(
                messageExchange.getOperation().getInput().getMessage().getQName());
        fillODEMessage(odeRequest, bpelMessageContext.getRequestMessage());

        return odeRequest;
    }

    private Future invokeBPELProcessThroughODEMessageExchange(
            final MyRoleMessageExchange odeMessageExchange,
            final BPELMessageContext bpelMessageContext) throws AxisFault {
        Message request = createInputMessageToODE(bpelMessageContext, odeMessageExchange);

        if (log.isDebugEnabled()) {
            log.debug("Invoking ODE using MEX " + odeMessageExchange);
            log.debug("Message content:  " + DOMUtils.domToString(request.getMessage()));
        }

        return odeMessageExchange.invoke(request, bpelMessageContext.getAttachmentIDList());
    }

    private boolean commitODEMessageExchange(final MyRoleMessageExchange odeMessageExchange) {
        try {
            if (log.isDebugEnabled()) {
                log.debug("Committing ODE MEX " + odeMessageExchange);
                log.debug("Committing transaction.");
            }

            transactionManager.commit();
        } catch (Exception e) {
            log.error("Commit failed", e);
            return false;
        }

        return true;
    }

    private void handleExceptionAtODEInvocation(final Exception e) throws BPELFault {
        String errorMessage = "Exception occurred while invoking ODE";
        log.error(errorMessage, e);
        String message = e.getMessage();
        if (message == null) {
            message = errorMessage;
        }
        throw new BPELFault(message, e);
    }

    public final void releaseODEMessageExchangeAndRollbackTransaction(
            final MyRoleMessageExchange odeMessageExchange,
            final Exception cachedException,
            final boolean isProcessInvokeSuccess) throws BPELFault {
        try {
            if (odeMessageExchange != null) {
                odeMessageExchange.release(isProcessInvokeSuccess);
            }
        } finally {
            try {
                transactionManager.rollback();
            } catch (Exception e) {
                e.initCause(cachedException);
                throw new BPELFault("Rollback failed", e);
            }
        }
    }

    private void waitForTheResponse(
            final Future responseFuture,
            final MyRoleMessageExchange odeMessageExchange) throws BPELFault {
        try {
            responseFuture.get(getTimeout(), TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            String errorMsg = "Timeout or execution error when waiting for response to MEX "
                    + odeMessageExchange + " " + e.toString();
            log.error(errorMsg, e);
            throw new BPELFault(errorMsg, e);
        }
    }

    /**
     * Fill the ODE's message object with the WSDL message parts processed at the message receiver.
     *
     * @param odeRequest      Request message pass in to BPEL engine
     * @param inComingMessage incoming request from message  receiver
     */
    private void fillODEMessage(final Message odeRequest, final WSDLAwareMessage inComingMessage) {
        Map<String, OMElement> bodyParts = inComingMessage.getBodyParts();
        Map<String, OMElement> headerParts = inComingMessage.getHeaderParts();
        for (Map.Entry<String, OMElement> bodyPart : bodyParts.entrySet()) {
            if (inComingMessage.isRPC()) {
                /* In RPC Style messages parent element's name is equal to the part name.*/
                odeRequest.setPart(bodyPart.getKey(), OMUtils.toDOM(bodyPart.getValue()));
            } else {
                /* in document style there isn't any relationship between part name and element
                * names. therefore we wrap document style message parts with a element which
                * has part name as it's local name.
                */
                Document doc = DOMUtils.newDocument();
                Element destPart = doc.createElementNS(null, bodyPart.getKey());
                destPart.appendChild(doc.importNode(OMUtils.toDOM(bodyPart.getValue()), true));
                odeRequest.setPart(bodyPart.getKey(), destPart);
            }
        }

        for (Map.Entry<String, OMElement> headerPart : headerParts.entrySet()) {
            odeRequest.setHeaderPart(headerPart.getKey(), OMUtils.toDOM(headerPart.getValue()));
        }
    }

    private void onResponse(final BPELMessageContext bpelMessageContext,
                            final MyRoleMessageExchange mex,
                            final MessageContext msgContext)
            throws AxisFault {
        switch (mex.getStatus()) {
            case FAULT:
                if (log.isDebugEnabled()) {
                    log.debug("Fault response message: " + mex.getFault());
                }
                SOAPFault fault = SOAPUtils.createSoapFault(bpelMessageContext, mex);
                msgContext.getEnvelope().getBody().addFault(fault);

                if (log.isDebugEnabled()) {
                    log.debug("Returning fault: " + msgContext.getEnvelope().toString());
                }
                break;
            case ASYNC:
            case RESPONSE:
                SOAPUtils.createSOAPResponse(bpelMessageContext, mex);
                if (log.isDebugEnabled()) {
                    log.debug("Response message " + msgContext.getEnvelope());
                }
                //writeHeader(msgContext, mex);
                break;
            case FAILURE:
                throw new BPELFault("Message exchange failure");
            default:
                throw new BPELFault("Received ODE message exchange in unexpected state: "
                        + mex.getStatus());
        }
    }

    /**
     * do not store the value so it can be dynamically updated.
     *
     * @return Message Exchange Timeout
     */
    private long getTimeout() {
        String timeout = processConfiguration.getEndpointProperties(serviceReference)
                .get(Properties.PROP_MEX_TIMEOUT);
        if (timeout != null) {
            try {
                return Long.parseLong(timeout);
            } catch (NumberFormatException e) {
                log.warn("Mal-formatted Property: [" + Properties.PROP_MEX_TIMEOUT + "="
                        + timeout + "] Default value (" + Properties.DEFAULT_MEX_TIMEOUT
                        + ") will be used");
            }
        }
        // Default value is 120000 milliseconds and if specified in bps.xml configuration file, that value will be
        // returned.
        return BPELServerImpl.getInstance().getBpelServerConfiguration().getMexTimeOut();
    }

    /**
     * Get the EPR of this service from the WSDL.
     *
     * @param wsdlDef     WSDL Definition
     * @param serviceName service name
     * @param portName    port name
     * @return XML representation of the EPR
     */
    public static Element genEPRfromWSDL(
            final Definition wsdlDef,
            final QName serviceName,
            final String portName) {

        Service serviceDef = wsdlDef.getService(serviceName);
        if (serviceDef != null) {
            Port portDef = serviceDef.getPort(portName);
            if (portDef != null) {
                Document doc = DOMUtils.newDocument();
                Element service = doc.createElementNS(Namespaces.WSDL_11, "service");
                service.setAttribute("name", serviceDef.getQName().getLocalPart());
                service.setAttribute("targetNamespace", serviceDef.getQName().getNamespaceURI());
                Element port = doc.createElementNS(Namespaces.WSDL_11, "port");
                service.appendChild(port);
                port.setAttribute("name", portDef.getName());
                port.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:bindns",
                        portDef.getBinding().getQName().getNamespaceURI());
                port.setAttribute("bindns:binding", portDef.getName());
                for (Object extElmt : portDef.getExtensibilityElements()) {
                    if (extElmt instanceof SOAPAddress) {
                        Element soapAddr = doc.createElementNS(Namespaces.SOAP_NS, "address");
                        port.appendChild(soapAddr);
                        soapAddr.setAttribute("location", ((SOAPAddress) extElmt).getLocationURI());
                    } else if (extElmt instanceof HTTPAddress) {
                        Element httpAddr = doc.createElementNS(Namespaces.HTTP_NS, "address");
                        port.appendChild(httpAddr);
                        httpAddr.setAttribute("location", ((HTTPAddress) extElmt).getLocationURI());
                    } else if (extElmt instanceof SOAP12Address) {
                        Element soap12Addr = doc.createElementNS(Namespaces.SOAP12_NS, "address");
                        port.appendChild(soap12Addr);
                        soap12Addr.setAttribute("location", ((SOAP12Address) extElmt).getLocationURI());
                    } else {
                        port.appendChild(
                                doc.importNode(((UnknownExtensibilityElement) extElmt).getElement(),
                                        true));
                    }
                }
                return service;
            }
        }
        return null;
    }

    /**
     * Create-and-copy a service-ref element.
     *
     * @param elmt Service Reference element
     * @return wrapped element
     */
    public static MutableEndpoint createServiceRef(final Element elmt) {
        Document doc = DOMUtils.newDocument();
        QName elQName = new QName(elmt.getNamespaceURI(), elmt.getLocalName());
        // If we get a service-ref, just copy it, otherwise make a service-ref
        // wrapper
        if (!EndpointReference.SERVICE_REF_QNAME.equals(elQName)) {
            Element serviceref = doc.createElementNS(
                    EndpointReference.SERVICE_REF_QNAME.getNamespaceURI(),
                    EndpointReference.SERVICE_REF_QNAME.getLocalPart());
            serviceref.appendChild(doc.importNode(elmt, true));
            doc.appendChild(serviceref);
        } else {
            doc.appendChild(doc.importNode(elmt, true));
        }

        return EndpointFactory.createEndpoint(doc.getDocumentElement());
    }

}
