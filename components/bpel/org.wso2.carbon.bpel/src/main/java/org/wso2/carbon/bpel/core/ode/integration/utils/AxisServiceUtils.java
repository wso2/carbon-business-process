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

package org.wso2.carbon.bpel.core.ode.integration.utils;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.OperationClient;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.context.ServiceGroupContext;
import org.apache.axis2.deployment.util.Utils;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.AxisServiceGroup;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.WSDL11ToAxisServiceBuilder;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.commons.httpclient.Header;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.protocol.HTTP;
import org.apache.ode.bpel.iapi.ProcessConf;
import org.apache.ode.utils.Namespaces;
import org.apache.ode.utils.stl.CollectionsX;
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.w3c.dom.Element;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.bpel.core.BPELConstants;
import org.wso2.carbon.bpel.core.internal.BPELServiceComponent;
import org.wso2.carbon.bpel.core.ode.integration.BPELMessageContext;
import org.wso2.carbon.bpel.core.ode.integration.BPELProcessProxy;
import org.wso2.carbon.bpel.core.ode.integration.axis2.Axis2UriResolver;
import org.wso2.carbon.bpel.core.ode.integration.axis2.Axis2WSDLLocator;
import org.wso2.carbon.bpel.core.ode.integration.axis2.receivers.BPELMessageReceiver;
import org.wso2.carbon.bpel.core.ode.integration.config.BPELServerConfiguration;
import org.wso2.carbon.utils.ServerConstants;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.wsdl.BindingOperation;
import javax.wsdl.Definition;
import javax.wsdl.Input;
import javax.wsdl.Port;
import javax.wsdl.Service;
import javax.wsdl.extensions.http.HTTPAddress;
import javax.wsdl.extensions.soap.SOAPAddress;
import javax.wsdl.extensions.soap.SOAPOperation;
import javax.wsdl.extensions.soap12.SOAP12Address;
import javax.wsdl.extensions.soap12.SOAP12Operation;
import javax.xml.namespace.QName;

/**
 * This class contains utility functions used by ODE-Carbon Integration Layer to create,
 * and configure AxisServices.
 */
public final class AxisServiceUtils {
    private static final Log log = LogFactory.getLog(AxisServiceUtils.class);

    private AxisServiceUtils() {
    }

    /**
     * Build the underlying Axis Service from Service QName and Port Name of interest using given WSDL
     * for BPEL document.
     * In the current implementation we are extracting service name from the soap:address' location property.
     * But specified port may not contain soap:adress instead it may contains http:address. We need to handle that
     * situation.
     *
     * @param axisConfiguration AxisConfiguration to which we should publish the service
     * @param processProxy      BPELProcessProxy
     * @return Axis Service build using WSDL, Service and Port
     * @throws org.apache.axis2.AxisFault on error
     */
    public static AxisService createAxisService(AxisConfiguration axisConfiguration,
                                                BPELProcessProxy processProxy) throws AxisFault {
        QName serviceName = processProxy.getServiceName();
        String portName = processProxy.getPort();
        Definition wsdlDefinition = processProxy.getWsdlDefinition();
        ProcessConf processConfiguration = processProxy.getProcessConfiguration();

        if (log.isDebugEnabled()) {
            log.debug("Creating AxisService: Service=" + serviceName + " port=" + portName +
                    " WSDL=" + wsdlDefinition.getDocumentBaseURI() + " BPEL=" +
                    processConfiguration.getBpelDocument());
        }

        WSDL11ToAxisServiceBuilder serviceBuilder = createAxisServiceBuilder(processProxy);

        /** Need to figure out a way to handle service name extractoin. According to my perspective extracting
         * the service name from the EPR is not a good decision. But we need to handle JMS case properly.
         * I am keeping JMS handling untouched until we figureout best solution. */
        /* String axisServiceName = extractServiceName(processConf, wsdlServiceName, portName);*/
        AxisService axisService = populateAxisService(processProxy, axisConfiguration, serviceBuilder);

        Iterator operations = axisService.getOperations();
        BPELMessageReceiver messageRec = new BPELMessageReceiver();

        /** Set the corresponding BPELService to message receivers */
        messageRec.setProcessProxy(processProxy);

        while (operations.hasNext()) {
            AxisOperation operation = (AxisOperation) operations.next();
            // Setting WSDLAwareMessage Receiver even if operation has a message receiver specified.
            // This is to fix the issue when build service configuration using services.xml(Always RPCMessageReceiver
            // is set to operations).
            operation.setMessageReceiver(messageRec);
            axisConfiguration.getPhasesInfo().setOperationPhases(operation);
        }

        /**
         * TODO: JMS Destination handling.
         */
        return axisService;
    }

    private static AxisService populateAxisService(BPELProcessProxy processProxy,
                                                   AxisConfiguration axisConfiguration,
                                                   WSDL11ToAxisServiceBuilder serviceBuilder)
            throws AxisFault {
        ProcessConf pConf = processProxy.getProcessConfiguration();
        AxisService axisService = serviceBuilder.populateService();
        axisService.setParent(axisConfiguration);
        axisService.setWsdlFound(true);
        axisService.setCustomWsdl(true);
        axisService.setClassLoader(axisConfiguration.getServiceClassLoader());
        URL wsdlUrl = null;
        for (File file : pConf.getFiles()) {
            if (file.getAbsolutePath().
                    indexOf(processProxy.getWsdlDefinition().getDocumentBaseURI()) > 0) {
                try {
                    wsdlUrl = file.toURI().toURL();
                } catch (MalformedURLException e) {
                    String errorMessage = "Cannot convert File URI to URL.";
                    handleException(pConf.getProcessId(), errorMessage, e);
                }
            }
        }
        if (wsdlUrl != null) {
            axisService.setFileName(wsdlUrl);
        }

        Utils.setEndpointsToAllUsedBindings(axisService);

        axisService.addParameter(new Parameter("useOriginalwsdl", "true"));
        axisService.addParameter(new Parameter("modifyUserWSDLPortAddress", "true"));
        axisService.addParameter(new Parameter("setEndpointsToAllUsedBindings", "true"));

        /* Setting service type to use in service management*/
        axisService.addParameter(ServerConstants.SERVICE_TYPE, "bpel");

        /* Process ID as a service parameter to use with process try-it*/
        axisService.addParameter(BPELConstants.PROCESS_ID, pConf.getProcessId());

        /* Fix for losing of security configuration  when updating BPEL package*/
        axisService.addParameter(new Parameter(CarbonConstants.PRESERVE_SERVICE_HISTORY_PARAM,
                "true"));
//        axisService.addParameter(
//                new Parameter(BPELConstants.MESSAGE_RECEIVER_INVOKE_ON_SEPARATE_THREAD, "true"));

        return axisService;
    }

    private static WSDL11ToAxisServiceBuilder createAxisServiceBuilder(BPELProcessProxy processProxy)
            throws AxisFault {
        Definition wsdlDef = processProxy.getWsdlDefinition();
        QName serviceName = processProxy.getServiceName();
        String portName = processProxy.getPort();
        ProcessConf pConf = processProxy.getProcessConfiguration();
        QName pid = pConf.getProcessId();
        InputStream wsdlInStream = null;

        URI wsdlBaseURI = pConf.getBaseURI()
                .resolve(wsdlDef.getDocumentBaseURI());
        try {
            wsdlInStream = wsdlBaseURI.toURL().openStream();
        } catch (MalformedURLException e) {
            String errMsg = "Malformed WSDL base URI.";
            handleException(pid, errMsg, e);
        } catch (IOException e) {
            String errMsg = "Error opening stream.";
            handleException(pid, errMsg, e);
        }
        WSDL11ToAxisServiceBuilder serviceBuilder = new WSDL11ToAxisPatchedBuilder(wsdlInStream,
                serviceName,
                portName);
        serviceBuilder.setBaseUri(wsdlBaseURI.toString());
        serviceBuilder.setCustomResolver(new Axis2UriResolver());
        try {
            serviceBuilder.setCustomWSDLResolver(new Axis2WSDLLocator(wsdlBaseURI));
        } catch (URISyntaxException e) {
            String errorMessage = "URI syntax invalid.";
            handleException(pid, errorMessage, e);
        }
        serviceBuilder.setServerSide(true);

        return serviceBuilder;
    }

    private static void handleException(QName pid, String errorMessage, Exception e) throws AxisFault {
        String tErrorMessage = "Error creating axis service for process " + pid + ".Cause: " +
                errorMessage;
        log.error(tErrorMessage, e);
        throw new AxisFault(tErrorMessage, e);
    }

//    public static void engageModules(AxisDescription description, String... modules)
//            throws AxisFault {
//        for (String m : modules) {
//            if (description.getAxisConfiguration().getModule(m) != null) {
//                if (!description.getAxisConfiguration().isEngaged(m) && !description.isEngaged(m)) {
//                    description.engageModule(description.getAxisConfiguration().getModule(m));
//                }
//            } else {
//                if (log.isDebugEnabled()) {
//                    log.debug("Module " + m + " is not available.");
//                }
//            }
//        }
//    }

    /**
     * Axis2 monkey patching to force the usage of the read(element,baseUri) method
     * of XmlSchema as the normal read is broken.
     */
    public static class WSDL11ToAxisPatchedBuilder extends WSDL11ToAxisServiceBuilder {
        public WSDL11ToAxisPatchedBuilder(InputStream in, QName serviceName, String portName) {
            super(in, serviceName, portName);
        }

//        public WSDL11ToAxisPatchedBuilder(Definition def, QName serviceName, String portName) {
//            super(def, serviceName, portName);
//        }
//
//        public WSDL11ToAxisPatchedBuilder(Definition def, QName serviceName, String portName,
//                                          boolean isAllPorts) {
//            super(def, serviceName, portName, isAllPorts);
//        }
//
//        public WSDL11ToAxisPatchedBuilder(InputStream in, AxisService service) {
//            super(in, service);
//        }
//
//        public WSDL11ToAxisPatchedBuilder(InputStream in) {
//            super(in);
//        }

        protected XmlSchema getXMLSchema(Element element, String baseUri) {
            XmlSchemaCollection schemaCollection = new XmlSchemaCollection();
            if (baseUri != null) {
                schemaCollection.setBaseUri(baseUri);
            }
            return schemaCollection.read(element, baseUri);
        }
    }

    public static void invokeService(BPELMessageContext partnerInvocationContext,
                                     ConfigurationContext configContext)
            throws AxisFault {
        MessageContext mctx = partnerInvocationContext.getInMessageContext();
        OperationClient opClient = getOperationClient(partnerInvocationContext, configContext);
        mctx.getOptions().setParent(opClient.getOptions());

        /*
        Else we assume that the epr is not changed by the process.
        In this case there's a limitation we cannot invoke the epr in the wsdl
        (by assingning that epr by partnerlink assign) if there is a endpoint
        configuration available for that particular service
        */

        addCustomHeadersToMessageContext(mctx);

        opClient.addMessageContext(mctx);
        Options operationOptions = opClient.getOptions();

        if (partnerInvocationContext.getUep().isAddressingEnabled()) {
            //Currently we set the action manually, but this should be handled by
            // addressing module it-self?
            String action = getAction(partnerInvocationContext);
            if (log.isDebugEnabled()) {
                log.debug("Soap action: " + action);
            }
            operationOptions.setAction(action);
            //TODO set replyto as well
            //operationOptions.setReplyTo(mctx.getReplyTo());
        }

        if (partnerInvocationContext.getUep().getAddress() == null) {
            partnerInvocationContext.getUep().setAddress(
                    getEPRfromWSDL(partnerInvocationContext.getBpelServiceWSDLDefinition(),
                            partnerInvocationContext.getService(),
                            partnerInvocationContext.getPort()));
        }

        operationOptions.setTo(partnerInvocationContext.getUep());

        opClient.execute(true);

        if (partnerInvocationContext.isTwoWay()) {
            partnerInvocationContext.setOutMessageContext(opClient.getMessageContext(
                    WSDLConstants.MESSAGE_LABEL_IN_VALUE));
            partnerInvocationContext.setFaultMessageContext(opClient.getMessageContext(
                    WSDLConstants.MESSAGE_LABEL_FAULT_VALUE));
        }
    }

    /**
     * Extracts the action to be used for the given operation.  It first checks to see
     * if a value is specified using WS-Addressing in the portType, it then falls back onto
     * getting it from the SOAP Binding.
     *
     * @param partnerMessageContext BPELMessageContext
     * @return The action value for the specified operation
     */
    public static String getAction(BPELMessageContext partnerMessageContext) {
        String action = getWSAInputAction(partnerMessageContext);
        if (action == null || "".equals(action)) {
            action = getSoapAction(partnerMessageContext);
        }
        return action;
    }

    /**
     * Attempts to extract the WS-Addressing "Action" attribute value from the operation definition.
     * When WS-Addressing is being used by a service provider, the "Action" is specified in the
     * portType->operation instead of the SOAP binding->operation.
     *
     * @param partnerMessageContext BPELMessageContext
     * @return the SOAPAction value if one is specified, otherwise empty string
     */
    public static String getWSAInputAction(BPELMessageContext partnerMessageContext) {
        BindingOperation bop = partnerMessageContext.getWsdlBindingForCurrentMessageFlow()
                .getBindingOperation(partnerMessageContext.getOperationName(), null, null);
        if (bop == null) {
            return "";
        }

        Input input = bop.getOperation().getInput();
        if (input != null) {
            Object action = input.getExtensionAttribute(new QName(Namespaces.WS_ADDRESSING_NS,
                    "Action"));
            if (action instanceof String) {
                return ((String) action);
            }

            action = input.getExtensionAttribute(new QName(BPELConstants.WS_ADDRESSING_NS2,
                    "Action"));
            if (action instanceof String) {
                return ((String) action);
            }

            action = input.getExtensionAttribute(new QName(BPELConstants.WS_ADDRESSING_NS3,
                    "Action"));
            if (action instanceof String) {
                return ((String) action);
            }

            action = input.getExtensionAttribute(new QName(BPELConstants.WS_ADDRESSING_NS4,
                    "Action"));
            if (action instanceof String) {
                return ((String) action);
            }
        }
        return "";
    }

    /**
     * Attempts to extract the SOAP Action is defined in the WSDL document.
     *
     * @param partnerMessageContext BPELMessageContext
     * @return the SOAPAction value if one is specified, otherwise empty string
     */
    public static String getSoapAction(BPELMessageContext partnerMessageContext) {
        BindingOperation bop = partnerMessageContext.getWsdlBindingForCurrentMessageFlow().
                getBindingOperation(partnerMessageContext.getOperationName(), null, null);
        if (bop == null) {
            return "";
        }

        if (partnerMessageContext.isSoap12()) {
            for (SOAP12Operation soapOp : CollectionsX.filter(bop.getExtensibilityElements(),
                    SOAP12Operation.class)) {
                return soapOp.getSoapActionURI();
            }
        } else {
            for (SOAPOperation soapOp : CollectionsX.filter(bop.getExtensibilityElements(),
                    SOAPOperation.class)) {
                return soapOp.getSoapActionURI();
            }
        }

        return "";
    }

    public static OperationClient getOperationClient(BPELMessageContext partnerMessageContext,
                                                     ConfigurationContext clientConfigCtx)
            throws AxisFault {
        AxisService anonymousService =
                AnonymousServiceFactory.getAnonymousService(partnerMessageContext.getService(),
                        partnerMessageContext.getPort(),
                        clientConfigCtx.getAxisConfiguration(), partnerMessageContext.getCaller());
        anonymousService.engageModule(clientConfigCtx.getAxisConfiguration().getModule("UEPModule"));
        anonymousService.getParent().addParameter(
                BPELConstants.HIDDEN_SERVICE_PARAM, "true");
        ServiceGroupContext sgc = new ServiceGroupContext(
                clientConfigCtx, (AxisServiceGroup) anonymousService.getParent());
        ServiceContext serviceCtx = sgc.getServiceContext(anonymousService);

        // get a reference to the DYNAMIC operation of the Anonymous Axis2 service
        AxisOperation axisAnonymousOperation = anonymousService.getOperation(
                partnerMessageContext.isTwoWay() ? ServiceClient.ANON_OUT_IN_OP :
                        ServiceClient.ANON_OUT_ONLY_OP);

        Options clientOptions = cloneOptions(partnerMessageContext.getInMessageContext().getOptions());
        clientOptions.setExceptionToBeThrownOnSOAPFault(false);
        /* This value doesn't overrideend point config. */
        clientOptions.setTimeOutInMilliSeconds(60000);

        return axisAnonymousOperation.createClient(serviceCtx, clientOptions);
    }

    /**
     * Clones the given {@link org.apache.axis2.client.Options} object. This is not a deep copy
     * because this will be called for each and every message going out from synapse. The parent
     * of the cloning options object is kept as a reference.
     *
     * @param options clonning object
     * @return clonned Options object
     */
    public static Options cloneOptions(Options options) {

        // create new options object and set the parent
        Options clonedOptions = new Options(options.getParent());

        // copy general options
        clonedOptions.setCallTransportCleanup(options.isCallTransportCleanup());
        clonedOptions.setExceptionToBeThrownOnSOAPFault(options.isExceptionToBeThrownOnSOAPFault());
        clonedOptions.setManageSession(options.isManageSession());
        clonedOptions.setSoapVersionURI(options.getSoapVersionURI());
        clonedOptions.setTimeOutInMilliSeconds(options.getTimeOutInMilliSeconds());
        clonedOptions.setUseSeparateListener(options.isUseSeparateListener());

        // copy transport related options
        clonedOptions.setListener(options.getListener());
        clonedOptions.setTransportIn(options.getTransportIn());
        clonedOptions.setTransportInProtocol(options.getTransportInProtocol());
        clonedOptions.setTransportOut(clonedOptions.getTransportOut());

        // copy username and password options
        clonedOptions.setUserName(options.getUserName());
        clonedOptions.setPassword(options.getPassword());

        // cloen the property set of the current options object
        for (Object o : options.getProperties().keySet()) {
            String key = (String) o;
            clonedOptions.setProperty(key, options.getProperty(key));
        }

        return clonedOptions;
    }

    /**
     * Get the EPR of this service from the WSDL.
     *
     * @param wsdlDef     WSDL Definition
     * @param serviceName service name
     * @param portName    port name
     * @return XML representation of the EPR
     */
    public static String getEPRfromWSDL(
            final Definition wsdlDef,
            final QName serviceName,
            final String portName) {
        Service serviceDef = wsdlDef.getService(serviceName);
        if (serviceDef != null) {
            Port portDef = serviceDef.getPort(portName);
            if (portDef != null) {
                for (Object extElmt : portDef.getExtensibilityElements()) {
                    if (extElmt instanceof SOAPAddress) {
                        return ((SOAPAddress) extElmt).getLocationURI();
                    } else if (extElmt instanceof HTTPAddress) {
                        return ((HTTPAddress) extElmt).getLocationURI();
                    } else if (extElmt instanceof SOAP12Address) {
                        return ((SOAP12Address) extElmt).getLocationURI();
                    }
                }
            }
        }
        return null;
    }

    public static void addCustomHeadersToMessageContext(MessageContext mctx) {

        List<Header> headers = null;

        BPELServerConfiguration bpelServerConfiguration = BPELServiceComponent.getBPELServer()
                .getBpelServerConfiguration();

        if (!bpelServerConfiguration.isKeepAlive()) {

            headers = new ArrayList();
            headers.add(new Header(HTTP.CONN_DIRECTIVE, HTTP.CONN_CLOSE));
        }


        //Add more custom header values in the future
        if ((headers != null) && (headers.size() > 0)) {
            mctx.setProperty(HTTPConstants.HTTP_HEADERS, headers);
        }
    }
}
