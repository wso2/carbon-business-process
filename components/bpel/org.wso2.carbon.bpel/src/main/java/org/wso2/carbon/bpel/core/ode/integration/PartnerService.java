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

import org.apache.axiom.soap.SOAPFault;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.httpclient.HttpConnectionManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.epr.EndpointFactory;
import org.apache.ode.bpel.epr.MutableEndpoint;
import org.apache.ode.bpel.epr.WSAEndpoint;
import org.apache.ode.bpel.epr.WSDL11Endpoint;
import org.apache.ode.bpel.iapi.EndpointReference;
import org.apache.ode.bpel.iapi.Message;
import org.apache.ode.bpel.iapi.MessageExchange;
import org.apache.ode.bpel.iapi.PartnerRoleChannel;
import org.apache.ode.bpel.iapi.PartnerRoleMessageExchange;
import org.apache.ode.bpel.iapi.ProcessConf;
import org.apache.ode.il.OMUtils;
import org.apache.ode.utils.DOMUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.wso2.carbon.bpel.common.config.EndpointConfiguration;
import org.wso2.carbon.bpel.core.BPELConstants;
import org.wso2.carbon.bpel.core.ode.integration.axis2.WSDLAwareSOAPProcessor;
import org.wso2.carbon.bpel.core.ode.integration.store.ProcessConfigurationImpl;
import org.wso2.carbon.bpel.core.ode.integration.utils.AxisServiceUtils;
import org.wso2.carbon.bpel.core.ode.integration.utils.BPELMessageContextFactory;
import org.wso2.carbon.bpel.core.ode.integration.utils.Messages;
import org.wso2.carbon.bpel.core.ode.integration.utils.SOAPUtils;
import org.wso2.carbon.unifiedendpoint.core.UnifiedEndpoint;
import org.wso2.carbon.unifiedendpoint.core.UnifiedEndpointConstants;

import javax.wsdl.Binding;
import javax.wsdl.Definition;
import javax.wsdl.Fault;
import javax.wsdl.Operation;
import javax.wsdl.Port;
import javax.wsdl.Service;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.http.HTTPBinding;
import javax.xml.namespace.QName;

/**
 * Implements the BPEL Process Partner Endpoint. This will handle all the communication between
 * partner services and BPEL process. This implements PartnerRoleChanel interface in-addition
 * to providing methods used to invoke external partner endpoints.
 */
public class PartnerService implements PartnerRoleChannel {
    private static final Log log = LogFactory.getLog(PartnerService.class);
    private static Log messageTraceLog = LogFactory.getLog(BPELConstants.MESSAGE_TRACE);

    // WSDL Definition for partner service
    private Definition wsdlDefinition;

    // Service QName of the partner service
    private QName serviceName;

    // Port name in the service definition of the WSDL
    private String portName;

    // Client side configuration context to use with external service invocations
    private ConfigurationContext clientConfigCtx;

    // Process configuration
    private ProcessConf processConfiguration;

    private WSAEndpoint endpointReference;

    private String endpointUrl;

    // WSDL Binding to use for this service invocation
    private Binding binding;

    // Unified endpoint for this partner endpoint
    private UnifiedEndpoint uep;

    public PartnerService(Definition wsdlDefinition,
                          QName serviceName,
                          String portName,
                          ConfigurationContext clientConfigCtx,
                          ProcessConf pconf,
                          HttpConnectionManager connManager) throws AxisFault {
        this.wsdlDefinition = wsdlDefinition;
        this.serviceName = serviceName;
        this.portName = portName;
        this.clientConfigCtx = clientConfigCtx;
        this.processConfiguration = pconf;

        inferBindingInformation();

        this.clientConfigCtx.setProperty(HTTPConstants.MULTITHREAD_HTTP_CONNECTION_MANAGER,
                connManager);
        this.clientConfigCtx.setProperty(HTTPConstants.REUSE_HTTP_CLIENT, "false");

        Element eprEle = BPELProcessProxy.genEPRfromWSDL(this.wsdlDefinition, this.serviceName,
                this.portName);
        if (eprEle == null) {
            throw new IllegalArgumentException("Service Port definition not found for service:"
                    + this.serviceName + " and port:" + this.portName);
        }
        this.endpointReference = EndpointFactory.convertToWSA(
                BPELProcessProxy.createServiceRef(eprEle));
        endpointUrl = endpointReference.getUrl();

        initUEP();

        if (log.isDebugEnabled()) {
            String msg = "Process ID => " + this.processConfiguration.getProcessId() +
                    " Deployer => " + this.processConfiguration.getDeployer();
            log.debug(msg);
        }


    }

    private void initUEP() throws AxisFault {
        EndpointConfiguration endpointConf =
                ((ProcessConfigurationImpl) processConfiguration).getEndpointConfiguration(
                        new WSDL11Endpoint(this.serviceName, portName));
        if (endpointConf == null) {
            uep = new UnifiedEndpoint();
            uep.setUepId(this.serviceName.getLocalPart());
            uep.setAddressingEnabled(true);
            uep.setAddressingVersion(UnifiedEndpointConstants.ADDRESSING_VERSION_FINAL);
        } else {
            uep = endpointConf.getUnifiedEndpoint();
        }
    }

    public Definition getWsdlDefinition() {
        return wsdlDefinition;
    }

    public Binding getBinding() {
        return binding;
    }

    public void invoke(final PartnerRoleMessageExchange partnerRoleMessageExchange) {
        boolean isTwoWay = (partnerRoleMessageExchange.getMessageExchangePattern() ==
                MessageExchange.MessageExchangePattern.REQUEST_RESPONSE);
        try {
            // Override options are passed to the axis MessageContext so we can
            // retrieve them in our session out changeHandler
            //
            // Below logic is required only if tenant information from the thread local context is required here.
            // However,
            // it does not seem required, hence commenting out.
//            String deployer = processConfiguration.getDeployer();
//
//            if(log.isDebugEnabled()) {
//                String msg = "Process Name => " + processConfiguration.getProcessId() +
//                        " Deployer =>" + processConfiguration.getDeployer();
//                log.debug(msg);
//            }
//
//            PrivilegedCarbonContext.startTenantFlow();
//            // Assuming that deployer should not be null
//            String domain = BPELServerHolder.getInstance().getRealmService().getTenantManager().getDomain(Integer
// .parseInt(deployer));
//            if(domain != null) {
//                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(domain);
//                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(Integer.parseInt(deployer));
//
//            } else {
//                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(MultitenantConstants
// .SUPER_TENANT_DOMAIN_NAME);
//                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(Integer.parseInt(deployer));
//            }


            final MessageContext mctx = new MessageContext();


            BPELMessageContext partnerInvocationContext =
                    BPELMessageContextFactory.createBPELMessageContext(mctx, this);
            ExtensibilityElement bindingType =
                    WSDLAwareSOAPProcessor.getBindingExtension(binding);

            try {
                if (bindingType instanceof HTTPBinding) {
                    /**
                     * In the current HTTP binding support implementation we only support GET, DELETE with
                     * x-form-url-encoded and POST, PUT with application/xml content types. And as the return(out put)
                     * we only support mime cotent text/xml with part name. Example HTTP Binding definition:
                     * <wsdl:binding name="RESTServiceHttpBinding" type="ns:RESTServicePortType">
                     *     <http:binding verb="POST" />
                     *     <wsdl:operation name="modifyMobileNumber">
                     *         <http:operation location="modifyMobileNumber" />
                     *         <wsdl:input>
                     *             <mime:content type="application/x-www-form-urlencoded"/>
                     *         </wsdl:input>
                     *         <wsdl:output>
                     *             <mime:content type="text/xml" part="parameters" />
                     *         </wsdl:output>
                     *     </wsdl:operation>
                     * </wsdl:binding>
                     *
                     * *** We ignore mime:content inside input element. We infer the content type based on HTTP verb.
                     *
                     *
                     */
                    //=========================================================
                    if (uep.getAddress() == null) {
                        uep.setAddress(endpointUrl);
                    }

                    partnerInvocationContext.setUep(uep);
                    partnerInvocationContext.setTwoWay(isTwoWay);
                    partnerInvocationContext.setService(serviceName);
                    partnerInvocationContext.setPort(portName);
                    partnerInvocationContext.
                            setCaller(partnerRoleMessageExchange.getCaller().getLocalPart());
                    partnerInvocationContext.
                            setOperationName(partnerRoleMessageExchange.getOperationName());

                    SOAPUtils.createSOAPRequest(partnerInvocationContext, partnerRoleMessageExchange);


                    String mexEndpointUrl =
                            ((MutableEndpoint) partnerRoleMessageExchange.getEndpointReference())
                                    .getUrl();
                    if (!endpointUrl.equals(mexEndpointUrl)) {
                        uep.setAddress(mexEndpointUrl);
                    }

                    if (messageTraceLog.isDebugEnabled()) {
                        messageTraceLog.debug("Invoking service: MEXId: " +
                                partnerRoleMessageExchange.getMessageExchangeId() +
                                " :: " + serviceName + "." +
                                partnerRoleMessageExchange.getOperationName());
                        if (messageTraceLog.isTraceEnabled()) {
                            messageTraceLog.trace("Request message: MEXId: " +
                                    partnerRoleMessageExchange.getMessageExchangeId() +
                                    " :: " +
                                    partnerInvocationContext.getInMessageContext().
                                            getEnvelope());
                        }
                    }

                    HTTPBindingHandler httpBindingHandler =
                            new HTTPBindingHandler(clientConfigCtx, serviceName, portName,
                                    wsdlDefinition
                            );
                    HTTPBindingHandler.HTTPBindingResponse response =
                            httpBindingHandler.invoke(partnerRoleMessageExchange, partnerInvocationContext);

                    if (isTwoWay) {
                        MessageContext responseMessageContext = response.getReponseMessageContext();
                        partnerInvocationContext.setOutMessageContext(responseMessageContext);
                        MessageContext fltMessageContext = response.getFaultMessageContext();

                        if (messageTraceLog.isTraceEnabled()) {
                            messageTraceLog.trace("Response message: MEXId: " +
                                    partnerRoleMessageExchange.getMessageExchangeId() +
                                    " :: " + responseMessageContext.getEnvelope());
                        }

                        if (fltMessageContext != null) {
                            replyHTTP(partnerInvocationContext, partnerRoleMessageExchange, true);
                        } else {
                            replyHTTP(partnerInvocationContext, partnerRoleMessageExchange,
                                    response.isFault());
                        }

                    } else {  /* one-way case */
                        partnerRoleMessageExchange.replyOneWayOk();
                    }
                } else {


                    /* make the given options the parent so it becomes the defaults of the
                    * MessageContext. That allows the user to override specific options on a given
                    * message context and not affect the overall options.
                    */

                    if (uep.getAddress() == null) {
                        uep.setAddress(endpointUrl);
                    }

                    partnerInvocationContext.setUep(uep);
                    partnerInvocationContext.setTwoWay(isTwoWay);
                    partnerInvocationContext.setService(serviceName);
                    partnerInvocationContext.setPort(portName);
                    partnerInvocationContext.
                            setCaller(partnerRoleMessageExchange.getCaller().getLocalPart());
                    partnerInvocationContext.
                            setOperationName(partnerRoleMessageExchange.getOperationName());

                    SOAPUtils.createSOAPRequest(partnerInvocationContext, partnerRoleMessageExchange);


                    String mexEndpointUrl =
                            ((MutableEndpoint) partnerRoleMessageExchange.getEndpointReference())
                                    .getUrl();
                    if (!endpointUrl.equals(mexEndpointUrl)) {
                        uep.setAddress(mexEndpointUrl);
                    }

                    if (messageTraceLog.isDebugEnabled()) {
                        messageTraceLog.debug("Invoking service: MEXId: " +
                                partnerRoleMessageExchange.getMessageExchangeId() +
                                " :: " + serviceName + "." +
                                partnerRoleMessageExchange.getOperationName());
                        if (messageTraceLog.isTraceEnabled()) {
                            messageTraceLog.trace("Request message: MEXId: " +
                                    partnerRoleMessageExchange.getMessageExchangeId() +
                                    " :: " +
                                    partnerInvocationContext.getInMessageContext().
                                            getEnvelope());
                        }
                    }

                    AxisServiceUtils.invokeService(partnerInvocationContext, clientConfigCtx);

                    if (messageTraceLog.isDebugEnabled()) {
                        messageTraceLog.debug("Service invocation completed: MEXId: " +
                                partnerRoleMessageExchange.getMessageExchangeId() +
                                " :: " + serviceName + "." +
                                partnerRoleMessageExchange.getOperationName());
                    }

                    if (isTwoWay) {
                        final Operation operation = partnerRoleMessageExchange.getOperation();
                        MessageContext response = partnerInvocationContext.getOutMessageContext();
                        MessageContext flt = partnerInvocationContext.getFaultMessageContext();
                        if (messageTraceLog.isTraceEnabled()) {
                            messageTraceLog.trace("Response message: MEXId: " +
                                    partnerRoleMessageExchange.getMessageExchangeId() +
                                    " :: " + response.getEnvelope());
                        }

                        if (flt != null) {
                            reply(partnerInvocationContext, partnerRoleMessageExchange, operation,
                                    flt, true);
                        } else {
                            reply(partnerInvocationContext, partnerRoleMessageExchange, operation,
                                    response, response.isFault());
                        }

                    } else {  /* one-way case */
                        partnerRoleMessageExchange.replyOneWayOk();
                    }

                }
            } finally {
                // make sure the HTTP connection is released to the pool!
                TransportOutDescription out = mctx.getTransportOut();
                if (out != null && out.getSender() != null) {
                    out.getSender().cleanup(mctx);
                }
            }

//            PrivilegedCarbonContext.endTenantFlow();

        } catch (Exception e) {
            String errmsg = Messages.msgErrorSendingMessageToAxisForODEMex(
                    partnerRoleMessageExchange.toString());
            log.error(errmsg, e);
            replyWithFailure(partnerRoleMessageExchange,
                    MessageExchange.FailureType.COMMUNICATION_ERROR, errmsg);
        }


    }

    private void inferBindingInformation() {
        Service serviceDef = wsdlDefinition.getService(serviceName);
        if (serviceDef == null) {
            throw new NullPointerException(Messages.msgServiceDefinitionNotFound(
                    serviceName.getLocalPart()));
        }
        Port port = serviceDef.getPort(portName);
        if (port == null) {
            throw new NullPointerException(Messages.msgServicePortNotFound(
                    serviceName.getLocalPart(), portName));
        }

        binding = port.getBinding();
        if (binding == null) {
            throw new NullPointerException(Messages.msgBindingNotFound(
                    serviceName.getLocalPart(), portName));
        }

    }

    private void replyWithFailure(final PartnerRoleMessageExchange odeMex,
                                  final MessageExchange.FailureType error, final String errmsg) {
        try {
            odeMex.replyWithFailure(error, errmsg, null);
        } catch (Exception e) {
            String emsg = "Error executing replyWithFailure; reply will be lost.";
            log.error(emsg, e);
        }
    }

    private void reply(final BPELMessageContext partnerInvocationContext,
                       final PartnerRoleMessageExchange odeMex, final Operation operation,
                       final MessageContext reply, final boolean isFault) {
        try {
            if (log.isDebugEnabled()) {
                log.debug("Received response for MEX " + odeMex);
            }
            if (isFault) {
                Document odeMsg = DOMUtils.newDocument();
                Element odeMsgEl = odeMsg.createElementNS(null, "message");
                odeMsg.appendChild(odeMsgEl);
                Fault fault = SOAPUtils.parseSoapFault(odeMsgEl, reply.getEnvelope(), operation);

                if (fault != null) {
                    if (log.isWarnEnabled()) {
                        log.warn("Fault response: faultName=" + fault.getName() + " faultType="
                                + fault.getMessage().getQName() + "\n"
                                + DOMUtils.domToString(odeMsgEl));
                    }

                    QName faultType = fault.getMessage().getQName();
                    QName faultName = new QName(wsdlDefinition.getTargetNamespace(),
                            fault.getName());
                    Message response = odeMex.createMessage(faultType);
                    response.setMessage(odeMsgEl);

                    odeMex.replyWithFault(faultName, response);
                } else {
                    SOAPFault soapFault = reply.getEnvelope().getBody().getFault();
                    QName faultType = new QName(wsdlDefinition.getTargetNamespace(),
                            "UnknownFault");
                    Message response = odeMex.createMessage(faultType);
                    Element actAsPart = odeMsgEl.getOwnerDocument().createElementNS(null,
                            soapFault.getLocalName());
                    odeMsgEl.appendChild(actAsPart);

                    if (soapFault.getCode() != null) {
                        actAsPart.appendChild(odeMsgEl.getOwnerDocument().importNode(
                                OMUtils.toDOM(soapFault.getCode()), true));
                    }

                    if (soapFault.getReason() != null) {
                        actAsPart.appendChild(odeMsgEl.getOwnerDocument().importNode(
                                OMUtils.toDOM(soapFault.getReason()), true));
                    }

                    if (log.isWarnEnabled()) {
                        log.warn("Fault response: " + DOMUtils.domToString(odeMsgEl));
                    }
                    response.setMessage(odeMsgEl);
                    odeMex.replyWithFault(faultType, response);
                }
            } else {
                Message response =
                        SOAPUtils.parseSOAPResponseFromPartner(partnerInvocationContext, odeMex);
                if (log.isDebugEnabled()) {
                    log.debug("Response:\n" + (response.getMessage() != null ?
                            DOMUtils.domToString(response.getMessage()) : "empty"));
                }
                odeMex.reply(response);
            }
        } catch (BPELFault bpelFault) {
            handleError(odeMex, bpelFault);
        } catch (AxisFault axisFault) {
            handleError(odeMex, axisFault);
        }
    }

    private void handleError(PartnerRoleMessageExchange odeMex, Exception ex) {
        String errmsg = "Unable to process response: " + ex.getMessage();
        log.error(errmsg, ex);
        odeMex.replyWithFailure(MessageExchange.FailureType.OTHER, errmsg, null);
    }

    private void replyHTTP(final BPELMessageContext partnerInvocationContext,
                           final PartnerRoleMessageExchange odeMex, final boolean isFault) {
        try {
            if (log.isDebugEnabled()) {
                log.debug("Received response for MEX " + odeMex);
            }
            if (isFault) {
                Document odeMsg = DOMUtils.newDocument();
                Element odeMsgEl = odeMsg.createElementNS(null, "message");
                odeMsg.appendChild(odeMsgEl);

                QName faultType = new QName("http://wso2.org/bps/fault", "HTTPBindingFault");
                QName faultName = new QName("http://wso2.org/bps/fault", "RESTPartnerServiceError");

                Element fault = odeMsg.createElementNS(null, "fault");
                fault.setTextContent("Error returned from REST Partner");
                odeMsgEl.appendChild(fault);

                Message response = odeMex.createMessage(faultType);
                response.setMessage(odeMsgEl);

                odeMex.replyWithFault(faultName, response);
            } else {
                Message response =
                        SOAPUtils.parseResponseFromRESTService(partnerInvocationContext, odeMex);
                if (log.isDebugEnabled()) {
                    log.debug("Response:\n" + (response.getMessage() != null ?
                            DOMUtils.domToString(response.getMessage()) : "empty"));
                }
                odeMex.reply(response);
            }
        } catch (Exception ex) {
            String errmsg = "Unable to process response: " + ex.getMessage();
            log.error(errmsg, ex);
            odeMex.replyWithFailure(MessageExchange.FailureType.OTHER, errmsg, null);
        }

    }

    public EndpointReference getInitialEndpointReference() {
        return endpointReference;
    }

    public void close() {
        //Don't we need to implement this method?? pls comment
    }
}
