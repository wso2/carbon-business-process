/*
 * Copyright (c) 2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.soap.SOAP11Constants;
import org.apache.axiom.soap.SOAP12Constants;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.OperationClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.WSDL2Constants;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.ode.bpel.epr.MutableEndpoint;
import org.apache.ode.bpel.iapi.PartnerRoleMessageExchange;
import org.apache.ode.il.OMUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.wso2.carbon.bpel.core.ode.integration.axis2.WSDLAwareSOAPProcessor;
import org.wso2.carbon.bpel.core.ode.integration.utils.AxisServiceUtils;
import org.wso2.carbon.bpel.core.ode.integration.utils.Messages;

import java.util.List;
import javax.wsdl.Binding;
import javax.wsdl.BindingOperation;
import javax.wsdl.Definition;
import javax.wsdl.Port;
import javax.wsdl.Service;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.http.HTTPAddress;
import javax.wsdl.extensions.http.HTTPBinding;
import javax.wsdl.extensions.http.HTTPOperation;
import javax.xml.namespace.QName;

/**
 * HTTP BindingHandler.
 */
public class HTTPBindingHandler {
    private ConfigurationContext configurationContext;
    private QName serviceName;
    private String portName;
    private Binding httpBinding;
    private Definition wsdl;

    public HTTPBindingHandler(ConfigurationContext configurationContext,
                              QName serviceName,
                              String portName,
                              Definition wsdl) throws AxisFault {
        this.configurationContext = configurationContext;
        this.serviceName = serviceName;
        this.portName = portName;
        this.wsdl = wsdl;

        inferBinding();
    }

    public HTTPBindingResponse invoke(final PartnerRoleMessageExchange partnerRoleMessageExchange,
                                      final BPELMessageContext bpelMessageContext) throws AxisFault {

        MessageContext messageContext;

        OperationClient operationClient = AxisServiceUtils.getOperationClient(bpelMessageContext,
                configurationContext);
        operationClient.getOptions().setAction("\"\"");
        operationClient.getOptions().setExceptionToBeThrownOnSOAPFault(true);


        addPropertyToOperationClient(operationClient,
                WSDL2Constants.ATTR_WHTTP_QUERY_PARAMETER_SEPARATOR, "&");
        addPropertyToOperationClient(operationClient, Constants.Configuration.ENABLE_REST, true);
        addPropertyToOperationClient(operationClient,
                Constants.Configuration.HTTP_METHOD, getVerb().trim());
        addPropertyToOperationClient(operationClient,
                WSDL2Constants.ATTR_WHTTP_LOCATION,
                getHTTPLocation(partnerRoleMessageExchange.getOperationName()));
        addPropertyToOperationClient(operationClient,
                Constants.Configuration.CONTENT_TYPE, inferContentType(getVerb()));
        addPropertyToOperationClient(operationClient,
                Constants.Configuration.MESSAGE_TYPE, inferContentType(getVerb()));

        SOAPEnvelope soapEnvelope =
                getFactory(operationClient.getOptions().getSoapVersionURI()).getDefaultEnvelope();
        messageContext = new MessageContext();

        populateSOAPBody(soapEnvelope, partnerRoleMessageExchange);
        messageContext.setEnvelope(soapEnvelope);

        operationClient.addMessageContext(messageContext);

        String mexEndpointUrl =
                ((MutableEndpoint) partnerRoleMessageExchange.getEndpointReference())
                        .getUrl();

        if (!mexEndpointUrl.equals(getServiceLocation())) {
            operationClient.getOptions().setTo(new EndpointReference(mexEndpointUrl));
        }
        operationClient.getOptions().setTo(bpelMessageContext.getUep());

        operationClient.execute(true);

        MessageContext responseMessageContext =
                operationClient.getMessageContext(WSDLConstants.MESSAGE_LABEL_IN_VALUE);
        MessageContext faultMessageContext =
                operationClient.getMessageContext(WSDLConstants.MESSAGE_LABEL_FAULT_VALUE);

        return new HTTPBindingResponse(responseMessageContext, faultMessageContext);
    }


    private void populateSOAPBody(SOAPEnvelope soapEnvelope,
                                  PartnerRoleMessageExchange partnerRoleMessageExchange) {
        org.apache.ode.bpel.iapi.Message messageToSend = partnerRoleMessageExchange.getRequest();
        if (messageToSend.getParts().size() == 1) {
            soapEnvelope.getBody().addChild(OMUtils.toOM(
                    getPartContent(messageToSend.getPart(messageToSend.getParts().get(0))),
                    soapEnvelope.getOMFactory()));
        } else {
            throw new IllegalArgumentException("HTTP Binding doesn't support multiple message part as the input.");
        }
    }

    private Element getPartContent(Element part) {
        if (part.getFirstChild().getNodeType() == Node.ELEMENT_NODE) {
            return (Element) part.getFirstChild();
        }

        throw new IllegalArgumentException("Cannot find message content in part element of ODE request to " +
                "the external service.");
    }

    private static SOAPFactory getFactory(String soapVersionURI) {

        if (SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI.equals(soapVersionURI)) {
            return OMAbstractFactory.getSOAP11Factory();
        } else if (SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI.equals(soapVersionURI)) {
            return OMAbstractFactory.getSOAP12Factory();
        } else {
            throw new RuntimeException(org.apache.axis2.i18n.Messages
                    .getMessage("unknownsoapversion"));
        }
    }

    private String getVerb() {
        ExtensibilityElement extElement = WSDLAwareSOAPProcessor.getBindingExtension(httpBinding);
        if (extElement instanceof HTTPBinding) {
            return ((HTTPBinding) extElement).getVerb();
        }

        throw new IllegalArgumentException("Current binding is not a HTTP Binding.");
    }

    private String inferContentType(String httpVerb) {
        if (httpVerb.trim().equals("GET") || httpVerb.trim().equals("DELETE")) {
            return HTTPConstants.MEDIA_TYPE_X_WWW_FORM;
        }

        return HTTPConstants.MEDIA_TYPE_APPLICATION_XML;
    }

    private String getHTTPLocation(String operationName) {
        for (Object bindingOperation : httpBinding.getBindingOperations()) {
            if (((BindingOperation) bindingOperation).getName().equals(operationName)) {
                List<Object> extElements = ((BindingOperation) bindingOperation).getExtensibilityElements();
                if (extElements.size() == 0) {
                    throw new RuntimeException();
                } else if (extElements.size() > 1) {
                    throw new RuntimeException();
                } else {
                    return ((HTTPOperation) extElements.get(0)).getLocationURI();
                }
            }
        }

        throw new NullPointerException("HTTP Operation's location attribute is null.");
    }

    private void addPropertyToOperationClient(OperationClient operationClient,
                                              String propertyKey,
                                              Object propertyValue) {
        operationClient.getOptions().setProperty(propertyKey, propertyValue);
    }

    protected void addPropertyToOperationClient(OperationClient operationClient,
                                                String propertyKey,
                                                boolean value) {
        addPropertyToOperationClient(operationClient, propertyKey, Boolean.valueOf(value));
    }

//    protected void addPropertyToOperationClient(OperationClient operationClient,
//                                                String propertyKey,
//                                                int value) {
//        addPropertyToOperationClient(operationClient, propertyKey, Integer.valueOf(value));
//    }

    private String getServiceLocation() {
        for (Object extElement : getPortDefinition().getExtensibilityElements()) {
            if (extElement instanceof HTTPAddress) {
                return ((HTTPAddress) extElement).getLocationURI();
            }
        }

        throw new NullPointerException("Service Location is null. Cannot find HTTP Address from WSDL definition");
    }

    private Port getPortDefinition() {
        Service serviceDef = wsdl.getService(serviceName);
        if (serviceDef == null) {
            throw new NullPointerException(Messages.msgServiceDefinitionNotFound(
                    serviceName.getLocalPart()));
        }
        return serviceDef.getPort(portName);
    }

    private void inferBinding() {
        Service serviceDef = wsdl.getService(serviceName);
        if (serviceDef == null) {
            throw new NullPointerException(Messages.msgServiceDefinitionNotFound(
                    serviceName.getLocalPart()));
        }
        Port port = serviceDef.getPort(portName);

        if (port == null) {
            throw new NullPointerException(Messages.msgServicePortNotFound(
                    serviceName.getLocalPart(), portName));
        }

        httpBinding = port.getBinding();
        if (httpBinding == null) {
            throw new NullPointerException(Messages.msgBindingNotFound(
                    serviceName.getLocalPart(), portName));
        }
    }

//    private void populateAxisService() {
//        service = AnonymousServiceFactory.getAnonymousService(serviceName,
//                portName,
//                configurationContext.getAxisConfiguration(), caller);
//
//        String targetNamesapce = getTargetNamesapce();
//
//        List<BindingOperation> bindingOperations = httpBinding.getBindingOperations();
//        for (BindingOperation bindingOperation : bindingOperations) {
//            String operationName = bindingOperation.getName();
//            AxisOperation operation = createAxisOperation(operationName, targetNamesapce, isOutOnly
// (bindingOperation));
//            operations.put(new QName(targetNamesapce, operationName), operation);
//            service.addOperation(operation);
//        }
//    }

//    private boolean isOutOnly(BindingOperation bindingOperation) {
//        if (bindingOperation.getBindingInput() != null && bindingOperation.getBindingOutput() == null) {
//            return true;
//        } else if (bindingOperation.getBindingOutput() != null && bindingOperation.getBindingInput() == null) {
//            throw new IllegalArgumentException("Outonly services are not supported.");
//        }
//
//        return false;
//
//    }

//    private String getTargetNamesapce() {
//        return httpBinding.getQName().getNamespaceURI();
//    }
//
//    private AxisOperation createAxisOperation(String name, String nsUrl, boolean outOnly) {
//        AxisOperation operation;
//
//        if (outOnly) {
//            operation = new OutOnlyAxisOperation();
//        } else {
//            operation = new OutInAxisOperation();
//        }
//
//        operation.setName(new QName(nsUrl, name));
//        return operation;
//    }

    /**
     * HTTPBindingResponse.
     */
    public static class HTTPBindingResponse {
        private MessageContext responseMessageContext;
        private MessageContext faultMessageContext;

        public HTTPBindingResponse(MessageContext responseMessageContext,
                                   MessageContext faultMessageContext) {
            this.responseMessageContext = responseMessageContext;
            this.faultMessageContext = faultMessageContext;
        }

        public boolean isFault() {
            return faultMessageContext != null;
        }

        public MessageContext getReponseMessageContext() {
            return responseMessageContext;
        }

        public MessageContext getFaultMessageContext() {
            return faultMessageContext;
        }
    }
}
