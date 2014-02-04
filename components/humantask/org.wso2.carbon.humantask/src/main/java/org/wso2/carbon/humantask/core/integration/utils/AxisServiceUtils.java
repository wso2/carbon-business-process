/*
 * Copyright (c) 2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.humantask.core.integration.utils;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.OperationClient;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.context.ServiceGroupContext;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.AxisServiceGroup;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bpel.common.CollectionsX;

import javax.wsdl.*;
import javax.wsdl.extensions.http.HTTPAddress;
import javax.wsdl.extensions.soap.SOAPAddress;
import javax.wsdl.extensions.soap.SOAPOperation;
import javax.wsdl.extensions.soap12.SOAP12Address;
import javax.wsdl.extensions.soap12.SOAP12Operation;
import javax.xml.namespace.QName;

/**
 * This class contains utility functions used by HumanTask Axis integration Layer to create,
 * and configure AxisServices.
 */
public final class AxisServiceUtils {
    private static Log log = LogFactory.getLog(AxisServiceUtils.class);
    public static final String WS_ADDRESSING_NS = "http://www.w3.org/2005/08/addressing";
    public static final String WS_ADDRESSING_NS2 = "http://www.w3.org/2006/05/addressing/wsdl";
    public static final String WS_ADDRESSING_NS3 = "http://www.w3.org/2006/02/addressing/wsdl";
    public static final String WS_ADDRESSING_NS4 = "http://schemas.xmlsoap.org/ws/2004/08/addressing";

    private AxisServiceUtils() {
    }

    public static void invokeService(ServiceInvocationContext partnerInvocationContext,
                                     ConfigurationContext configContext)
            throws AxisFault {
        //TODO check for one-way
        MessageContext mctx = partnerInvocationContext.getInMessageContext();
        OperationClient opClient = getOperationClient(partnerInvocationContext, configContext);
        mctx.getOptions().setParent(opClient.getOptions());

        /*
        Else we assume that the epr is not changed by the process.
        In this case there's a limitation we cannot invoke the epr in the wsdl
        (by assigning that epr by partnerlink assign) if there is a endpoint
        configuration available for that particular service
        */
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
        operationOptions.setTo(partnerInvocationContext.getUep());

        opClient.execute(true);
    }

    private static OperationClient getOperationClient(ServiceInvocationContext partnerMessageContext,
                                                      ConfigurationContext clientConfigCtx)
            throws AxisFault {
        AxisService anonymousService =
                AnonymousServiceFactory.getAnonymousService(partnerMessageContext.getService(),
                        partnerMessageContext.getPort(),
                        clientConfigCtx.getAxisConfiguration(), partnerMessageContext.getCaller());
        anonymousService.engageModule(clientConfigCtx.getAxisConfiguration().getModule("UEPModule"));
        anonymousService.getParent().addParameter(
                "hiddenService", "true");
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
     * Extracts the action to be used for the given operation.  It first checks to see
     * if a value is specified using WS-Addressing in the portType, it then falls back onto
     * getting it from the SOAP Binding.
     *
     * @param partnerMessageContext BPELMessageContext
     * @return The action value for the specified operation
     */
    public static String getAction(ServiceInvocationContext partnerMessageContext) {
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
    public static String getWSAInputAction(ServiceInvocationContext partnerMessageContext) {
        BindingOperation bop = partnerMessageContext.getWsdlBindingForCurrentMessageFlow()
                .getBindingOperation(partnerMessageContext.getOperationName(), null, null);
        if (bop == null) {
            return "";
        }

        Input input = bop.getOperation().getInput();
        if (input != null) {
            Object action = input.getExtensionAttribute(new QName(WS_ADDRESSING_NS,
                    "Action"));
            if (action instanceof String) {
                return ((String) action);
            }

            action = input.getExtensionAttribute(new QName(WS_ADDRESSING_NS2,
                    "Action"));
            if (action instanceof String) {
                return ((String) action);
            }

            action = input.getExtensionAttribute(new QName(WS_ADDRESSING_NS3,
                    "Action"));
            if (action instanceof String) {
                return ((String) action);
            }

            action = input.getExtensionAttribute(new QName(WS_ADDRESSING_NS4,
                    "Action"));
            if (action instanceof QName) {
                return ((QName) action).getLocalPart();
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
    public static String getSoapAction(ServiceInvocationContext partnerMessageContext) {
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
}
