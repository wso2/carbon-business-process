/*
 * Copyright (c) 2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.bpel.core.ode.integration.axis2;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.impl.llom.soap12.SOAP12Factory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisBinding;
import org.apache.axis2.description.AxisEndpoint;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.WSDL2Constants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.utils.stl.CollectionsX;

import javax.wsdl.*;
import javax.wsdl.extensions.ElementExtensible;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.http.HTTPBinding;
import javax.wsdl.extensions.soap.SOAPBinding;
import javax.wsdl.extensions.soap.SOAPBody;
import javax.wsdl.extensions.soap.SOAPHeader;
import javax.wsdl.extensions.soap12.SOAP12Binding;
import javax.wsdl.extensions.soap12.SOAP12Body;
import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * This class implements the WSDL aware SOAP processor. This process incoming SOAP message according to
 * the WSDL bindings, binding operations and WSDL messages.
 */
public class WSDLAwareSOAPProcessor {
    private static Log log = LogFactory.getLog(WSDLAwareSOAPProcessor.class);

    private Definition wsdlDef;
    private boolean isRPC;
    private SOAPFactory soapFactory;
    private QName serviceName;
    private String portName;
    private Service wsdlServiceDef;
    private Binding wsdlBinding;
    private MessageContext inMessageCtx;

    private static final String WSDL_4_J_DEFINITION = "wsdl4jDefinition";
    private static final String WSDL_BINDING_STYLE_RPC = "rpc";

    private boolean soap12 = false;

    public WSDLAwareSOAPProcessor(MessageContext inMsgCtx) throws AxisFault {
        QName bindingQName;
        AxisService axisService;

        inMessageCtx = inMsgCtx;
        axisService = inMsgCtx.getAxisService();
        serviceName = new QName(axisService.getTargetNamespace(), axisService.getName());

        wsdlDef = (Definition) axisService.getParameter(WSDL_4_J_DEFINITION).getValue();
        if (wsdlDef == null) {
            throw new AxisFault("No WSDL Definition was found for service " +
                    serviceName.getLocalPart() + ".");
        }

        wsdlServiceDef = wsdlDef.getService(serviceName);
        if (wsdlServiceDef == null) {
            throw new AxisFault("WSDL Service Definition not found for service " +
                    serviceName.getLocalPart());
        }

        /**
         * This will get the current end point which Axis2 picks the incoming request.
         */
        AxisEndpoint axisEndpoint = (AxisEndpoint) inMsgCtx.
                getProperty(WSDL2Constants.ENDPOINT_LOCAL_NAME);
        if (axisEndpoint == null) {
            String defaultEndpointName = axisService.getEndpointName();
            axisEndpoint = axisService.getEndpoints().get(defaultEndpointName);
            if (axisEndpoint == null) {
                throw new AxisFault("AxisEndpoint cannot be null.");
            }
        }

        portName = axisEndpoint.getName();
        AxisBinding axisBinding = axisEndpoint.getBinding();
        bindingQName = axisBinding.getName();

        /** In this implementation, we assume that AxisBinding's QName is equal to WSDL bindings QName. */
        wsdlBinding = wsdlDef.getBinding(bindingQName);
        if (wsdlBinding == null) {
            throw new AxisFault("WSDL Binding null for incoming message.");
        }

        ExtensibilityElement bindingType = getBindingExtension(wsdlBinding);

        if (!(bindingType instanceof SOAPBinding || bindingType instanceof SOAP12Binding ||
                bindingType instanceof HTTPBinding)) {
            throw new AxisFault("WSDL Binding not supported.");
        }

        isRPC = isRPC(bindingType);

        soapFactory = (SOAPFactory) inMsgCtx.getEnvelope().getOMFactory();
        if (soapFactory == null) {
            if (bindingType instanceof SOAPBinding) {
                soapFactory = OMAbstractFactory.getSOAP11Factory();
            } else {
                soapFactory = OMAbstractFactory.getSOAP12Factory();
            }
        }

        if (soapFactory instanceof SOAP12Factory) {
            soap12 = true;
        }
    }

    public static boolean isRPC(ExtensibilityElement bindingType) {
        if (bindingType instanceof SOAPBinding) {
            return ((SOAPBinding) bindingType).getStyle() != null &&
                    ((SOAPBinding) bindingType).getStyle().equals(WSDL_BINDING_STYLE_RPC);
        } else if (bindingType instanceof SOAP12Binding) {
            return ((SOAP12Binding) bindingType).getStyle() != null &&
                    ((SOAP12Binding) bindingType).getStyle().equals(WSDL_BINDING_STYLE_RPC);
        }
        /**
         * We are using Document literal style binding inside Axis2 when we got a request
         * via HTTPbinding.
         */

        return false;
    }

    public WSDLAwareMessage parseRequest() throws AxisFault {
        /**
         * I assume that local part of the Axis Operation's name is always equal to
         * the operation name in the WSDL.
         */
        BindingOperation bindingOp = wsdlBinding.getBindingOperation(
                wsdlBinding.getPortType().getOperation(
                        inMessageCtx.getAxisOperation().getName().getLocalPart(),
                        null,
                        null).getName(),
                null,
                null);
        if (bindingOp == null) {
            throw new AxisFault("WSDL binding operation not found for service: " +
                    serviceName.getLocalPart() + " port: " + portName);
        }

        BindingInput bindingInput = bindingOp.getBindingInput();
        if (bindingInput == null) {
            throw new AxisFault("BindingInput not found for service: " +
                    serviceName.getLocalPart() + " port: " + portName);
        }

        return processMessageParts(bindingInput);
    }

    private WSDLAwareMessage processMessageParts(BindingInput bindingInput) throws AxisFault {
        WSDLAwareMessage message = new WSDLAwareMessage();
        message.setBinding(wsdlBinding);
        List parts;
        String namespace;
        if (soap12) {
            SOAP12Body soapBodyDef = getFirstExtensibilityElement(bindingInput, SOAP12Body.class);
            if (soapBodyDef == null) {
                String errMessage = "SOAPBody null for binding input.";
                log.error(errMessage);
                throw new AxisFault(errMessage);
            }
            parts = soapBodyDef.getParts();
            namespace = soapBodyDef.getNamespaceURI();
        } else {
            SOAPBody soapBodyDef = getFirstExtensibilityElement(bindingInput, SOAPBody.class);
            if (soapBodyDef == null) {
                String errMessage = "SOAPBody null for binding input.";
                log.error(errMessage);
                throw new AxisFault(errMessage);
            }
            parts = soapBodyDef.getParts();
            namespace = soapBodyDef.getNamespaceURI();
        }

        QName axisOperationName = inMessageCtx.getAxisOperation().getName();

        /**
         * Local part of the axis Operation's name equals to WSDL Operation name.
         */
        Operation op = wsdlBinding.getPortType().getOperation(axisOperationName.getLocalPart(),
                null, null);
        String rpcWrapper = op.getName();
        List bodyParts = op.getInput().getMessage().getOrderedParts(parts);

        if (isRPC) {
            message.setRPC(true);
            QName rpWrapperQName = new QName(namespace, rpcWrapper);
            OMElement partWrapper = inMessageCtx.getEnvelope().getBody().
                    getFirstChildWithName(rpWrapperQName);
            if (partWrapper == null) {
                String errMsg = "SOAP Body doesn't contain expected part wrapper.";
                log.error(errMsg);
                throw new AxisFault(errMsg);
            }
            /* In RPC the body element is the operation name, wrapping parts. Order doesn't really
             * matter as far as we're concerned. All we need to do is copy the soap:body children,
             * since doc-lit rpc looks the same in ode and soap.*/
            for (Object partDef : bodyParts) {
                OMElement srcPart = partWrapper.getFirstChildWithName(
                        new QName(null, ((Part) partDef).getName()));
                if (srcPart == null) {
                    throw new AxisFault("SOAP Body doesn't contain required part " +
                            ((Part) partDef).getName() + ".");
                }

                message.addBodyPart(srcPart.getLocalName(), srcPart);
            }
        } else {
            /**
             * In doc-literal style, we expect the elements in the body to correspond (in order) to
             * the parts defined in the binding. All the parts should be element-typed, otherwise
             * it is a mess.
             */
            message.setRPC(false);
            Iterator srcParts = inMessageCtx.getEnvelope().getBody().getChildElements();
            for (Object partDef : bodyParts) {
                if (!srcParts.hasNext()) {
                    throw new AxisFault("SOAP Body does not contain required part" +
                            ((Part) partDef).getName() + ".");
                }

                OMElement srcPart = (OMElement) srcParts.next();
                if (((Part) partDef).getElementName() == null) {
                    throw new AxisFault("Binding defines non-element document literal part(s)");
                }
                if (!srcPart.getQName().equals(((Part) partDef).getElementName())) {
                    throw new AxisFault("Unexpected element in SOAP body.");
                }
                message.addBodyPart(((Part) partDef).getName(), srcPart);
            }
        }

        processSoapHeaderParts(message, bindingInput, op);
        return message;
    }

    private void processSoapHeaderParts(WSDLAwareMessage message, BindingInput bindingInput,
                                        Operation op) throws AxisFault {
        /* TODO: Analyze the header handling implementation */
        List<SOAPHeader> headerDefs = getSOAPHeaders(bindingInput);
        org.apache.axiom.soap.SOAPHeader soapHeader = inMessageCtx.getEnvelope().getHeader();

        for (SOAPHeader headerDef : headerDefs) {
            handleSoapHeaderPartDef(message, headerDef, op.getInput().getMessage(), soapHeader);
        }
        if (soapHeader != null) {
            Iterator headersIter = soapHeader.getChildElements();
            while (headersIter.hasNext()) {
                OMElement header = (OMElement) headersIter.next();
                String partName = findHeaderPartName(headerDefs, header.getQName());
                //The following commented fix, avoids adding any of the headers. So that reverting
                // back to old fix
//                if (partName != null) {
                // Fix for JIRA https://wso2.org/jira/browse/CARBON-5499
                message.addHeaderPart(partName, header);
//                }
            }
        }

    }

    private void handleSoapHeaderPartDef(WSDLAwareMessage message, SOAPHeader headerDef,
                                         Message msgType,
                                         org.apache.axiom.soap.SOAPHeader soapHeader)
            throws AxisFault {
        boolean payloadMessageHeader = headerDef.getMessage() == null ||
                headerDef.getMessage().equals(msgType.getQName());
        boolean requiredHeader = payloadMessageHeader || (headerDef.getRequired() != null &&
                headerDef.getRequired());


        if (requiredHeader && soapHeader == null) {
            throw new AxisFault("Missing required SOAP header element.");
        }

        if (soapHeader == null) {
            return;
        }

        Message headerMsg = wsdlDef.getMessage(headerDef.getMessage());
        if (headerMsg == null) {
            return;
        }

        Part p = headerMsg.getPart(headerDef.getPart());
        if (p == null || p.getElementName() == null) {
            return;
        }

        OMElement headerEl = soapHeader.getFirstChildWithName(p.getElementName());
        if (requiredHeader && headerEl == null) {
            throw new AxisFault("Missing required SOAP header element.");
        }

        if (headerEl == null) {
            return;
        }

        message.addHeaderPart(p.getName(), headerEl);

    }

    private String findHeaderPartName(List<SOAPHeader> headerDefs, QName elementName) {
        for (SOAPHeader headerDef : headerDefs) {
            Message hdrMsg = wsdlDef.getMessage(headerDef.getMessage());
            for (Object o : hdrMsg.getParts().values()) {
                Part p = (Part) o;
                if (p.getElementName().equals(elementName)) {
                    return p.getName();
                }
            }
        }
        //The following commented fix, avoids adding any of the headers. So that reverting back to old fix
        return elementName.getLocalPart();
//        // Fix to avoid unwanted headers getting copied to input Message to ODE.
//        // JIRA - https://wso2.org/jira/browse/CARBON-5499
//        return null;
    }

    @SuppressWarnings("unchecked")
    public static List<SOAPHeader> getSOAPHeaders(ElementExtensible eee) {
        return CollectionsX.filter(new ArrayList<SOAPHeader>(),
                (Collection<Object>) eee.getExtensibilityElements(),
                SOAPHeader.class);
    }

    public static <T> T getFirstExtensibilityElement(ElementExtensible parent, Class<T> cls) {
        Collection<T> ee = CollectionsX.filter(parent.getExtensibilityElements(), cls);

        return ee.isEmpty() ? null : ee.iterator().next();

    }

    /**
     * Look up the ExtensibilityElement defining the binding for the given Port or
     * throw an {@link IllegalArgumentException} if multiple bindings found.
     *
     * @param binding WSDL binding
     * @return an instance of {@link SOAPBinding} or {@link HTTPBinding} or null
     * @throws IllegalArgumentException if multiple bindings found.
     */
    public static ExtensibilityElement getBindingExtension(Binding binding) {
        Collection bindings = new ArrayList();
        CollectionsX.filter(bindings, binding.getExtensibilityElements(), HTTPBinding.class);
        CollectionsX.filter(bindings, binding.getExtensibilityElements(), SOAPBinding.class);
        CollectionsX.filter(bindings, binding.getExtensibilityElements(), SOAP12Binding.class);
        if (bindings.size() == 0) {
            return null;
        } else if (bindings.size() > 1) {
            // exception if multiple bindings found
            throw new IllegalArgumentException("Multiple bindings: " + binding.getQName());
        } else {
            // retrieve the single element
            return (ExtensibilityElement) bindings.iterator().next();
        }
    }
}
