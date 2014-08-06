/**
 *  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.bpel.core.ode.integration.utils;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.soap.*;
import org.apache.axis2.AxisFault;
import org.apache.ode.bpel.iapi.MessageExchange;
import org.apache.ode.bpel.iapi.MyRoleMessageExchange;
import org.apache.ode.bpel.iapi.PartnerRoleMessageExchange;
import org.apache.ode.il.OMUtils;
import org.apache.ode.utils.DOMUtils;
import org.apache.ode.utils.NSContext;
import org.apache.ode.utils.Namespaces;
import org.apache.ode.utils.stl.CollectionsX;
import org.apache.ode.utils.wsdl.WsdlUtils;
import org.w3c.dom.*;
import org.wso2.carbon.bpel.core.ode.integration.BPELFault;
import org.wso2.carbon.bpel.core.ode.integration.BPELMessageContext;

import javax.wsdl.*;
import javax.wsdl.extensions.ElementExtensible;
import javax.wsdl.extensions.mime.MIMEContent;
import javax.wsdl.extensions.soap12.SOAP12Body;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import java.util.*;

/**
 * Utility class which contains methods for creating response SOAP messages and
 * SOAP faults.
 */
public final class SOAPUtils {

    // Utility classes should not have a public or default constructor.

    private SOAPUtils() {
    }

    /**
     * Create SOAP Response message from response returned byBPELServer ODE engine.
     *
     * @param bpelMessageContext DTO contains details on current messageflow.
     * @param odeMessageExchange ODE MyRoleMessageExchange contains data on the current process
     *                           invocation
     * @throws AxisFault in case of a error while creating SOAP response.
     */
    public static void createSOAPResponse(
            final BPELMessageContext bpelMessageContext,
            final MyRoleMessageExchange odeMessageExchange) throws AxisFault {
        checkForNullValuesInResponse(bpelMessageContext, odeMessageExchange);

        BindingOperation bindingOp = getBindingOperation(bpelMessageContext,
                odeMessageExchange.getOperationName());
        BindingOutput bindingOutput = getBindingOutPut(bindingOp);

        SOAPEnvelope soapEnv = bpelMessageContext.getOutMessageContext().getEnvelope();
        if (soapEnv == null) {
            soapEnv = bpelMessageContext.getSoapFactoryForCurrentMessageFlow().getDefaultEnvelope();
            bpelMessageContext.getOutMessageContext().setEnvelope(soapEnv);
        }

        populateSOAPHeaders(
                odeMessageExchange.getResponse(),
                soapEnv,
                bpelMessageContext.getSoapFactoryForCurrentMessageFlow(),
                getSOAPHeaders(bindingOutput),
                odeMessageExchange.getOperation());

        populateSOAPBody(
                soapEnv,
                bindingOutput,
                bpelMessageContext.isSoap12(),
                bpelMessageContext.getSoapFactoryForCurrentMessageFlow(),
                odeMessageExchange.getOperation(),
                odeMessageExchange.getOperation().getOutput().getMessage(),
                odeMessageExchange.getResponse(),
                bpelMessageContext.isRPCStyleOperation(),
                false);
    }

    /**
     * Create SOAP Request message from request submitted by ODE engine.
     *
     * @param bpelMessageContext        DTO containing details about current message flow
     * @param odePartnerMessageExchange ODE PartnerRoleMessageExchange containing information about
     *                                  current external service invocation.
     * @throws AxisFault If an error occurred while creating the SOAP request
     */
    public static void createSOAPRequest(
            final BPELMessageContext bpelMessageContext,
            final PartnerRoleMessageExchange odePartnerMessageExchange) throws AxisFault {
        checkForNullValuesInRequest(bpelMessageContext, odePartnerMessageExchange);

        BindingOperation bindingOp = getBindingOperation(bpelMessageContext,
                odePartnerMessageExchange.getOperationName());
        BindingInput bindingInput = getBindingInput(bindingOp);
        SOAPEnvelope soapEnvelope = bpelMessageContext.getInMessageContext().getEnvelope();
        if (soapEnvelope == null) {
            soapEnvelope =
                    bpelMessageContext.getSoapFactoryForCurrentMessageFlow().getDefaultEnvelope();
            bpelMessageContext.getInMessageContext().setEnvelope(soapEnvelope);
        }

        populateSOAPHeaders(
                odePartnerMessageExchange.getRequest(),
                soapEnvelope,
                bpelMessageContext.getSoapFactoryForCurrentMessageFlow(),
                getSOAPHeaders(bindingInput),
                odePartnerMessageExchange.getOperation());

        populateSOAPBody(
                soapEnvelope,
                bindingInput,
                bpelMessageContext.isSoap12(),
                bpelMessageContext.getSoapFactoryForCurrentMessageFlow(),
                odePartnerMessageExchange.getOperation(),
                odePartnerMessageExchange.getOperation().getInput().getMessage(),
                odePartnerMessageExchange.getRequest(),
                bpelMessageContext.isRPCStyleOperation(),
                true);
    }

    private static BindingInput getBindingInput(BindingOperation bindingOp) {
        BindingInput bindingInput = bindingOp.getBindingInput();
        if (bindingInput == null) {
            throw new NullPointerException("BindingInput is null.");
        }

        return bindingInput;
    }

    @SuppressWarnings("unchecked")
    private static void populateSOAPBody(
            SOAPEnvelope soapEnvelope,
            ElementExtensible bindingInput,
            boolean soap12,
            SOAPFactory soapFactory,
            Operation operation,
            Message messageDefinition,
            org.apache.ode.bpel.iapi.Message messageFromODE,
            boolean isRPC,
            boolean isRequest) throws BPELFault {
        if (soap12) {
            SOAP12Body soapBodyDefinition = getSOAP12Body(bindingInput);
            if (soapBodyDefinition != null) {
                SOAPBody soapBody;
                if (soapEnvelope.getBody() != null) {
                    soapBody = soapEnvelope.getBody();
                } else {
                    soapBody = soapFactory.createSOAPBody(soapEnvelope);
                }


                OMElement partHolder;
                if (isRPC) {
                    String rpcWrapperElementName;
                    if (isRequest) {
                        rpcWrapperElementName = operation.getName();
                    } else {
                        rpcWrapperElementName = operation.getName() + "Response";
                    }
                    partHolder = createRPCWrapperElement(
                            soapBody,
                            soapFactory,
                            new QName(soapBodyDefinition.getNamespaceURI(), rpcWrapperElementName));
                } else {
                    partHolder = soapBody;
                }

                List<Part> parts = messageDefinition.getOrderedParts(soapBodyDefinition.getParts());
                for (Part p : parts) {
                    Element partContent = DOMUtils.findChildByName(
                            messageFromODE.getMessage(),
                            new QName(null, p.getName()));
                    if (partContent == null) {
                        throw new BPELFault("Missing required part in ODE Message: " +
                                new QName(null, p.getName()));
                    }

                    OMElement omPartContent = OMUtils.toOM(partContent, soapFactory);
                    if (isRPC) {
                        partHolder.addChild(omPartContent);
                    } else {
                        for (Iterator<OMNode> i = omPartContent.getChildren(); i.hasNext(); ) {
                            partHolder.addChild(i.next());
                        }
                    }

                }
            }
        } else {
            javax.wsdl.extensions.soap.SOAPBody soapBodyDefinition = getSOAP11Body(bindingInput);
            if (soapBodyDefinition != null) {
                SOAPBody soapBody;
                if (soapEnvelope.getBody() != null) {
                    soapBody = soapEnvelope.getBody();
                } else {
                    soapBody = soapFactory.createSOAPBody(soapEnvelope);
                }


                OMElement partHolder;
                if (isRPC) {
                    String rpcWrapperElementName;
                    if (isRequest) {
                        rpcWrapperElementName = operation.getName();
                    } else {
                        rpcWrapperElementName = operation.getName() + "Response";
                    }
                    partHolder = createRPCWrapperElement(
                            soapBody,
                            soapFactory,
                            new QName(soapBodyDefinition.getNamespaceURI(), rpcWrapperElementName));
                } else {
                    partHolder = soapBody;
                }

                List<Part> parts = messageDefinition.getOrderedParts(soapBodyDefinition.getParts());
                for (Part p : parts) {
                    Element partContent = DOMUtils.findChildByName(
                            messageFromODE.getMessage(),
                            new QName(null, p.getName()));
                    if (partContent == null) {
                        throw new BPELFault("Missing required part in ODE Message: " +
                                new QName(null, p.getName()));
                    }

                    OMElement omPartContent = OMUtils.toOM(partContent, soapFactory);
                    if (isRPC) {
                        partHolder.addChild(omPartContent);
                    } else {
                        for (Iterator<OMNode> i = omPartContent.getChildren(); i.hasNext(); ) {
                            partHolder.addChild(i.next());
                        }
                    }

                }
            }
        }
    }

    private static OMElement createRPCWrapperElement(
            SOAPBody soapBody,
            SOAPFactory soapFactory,
            QName wrapperElementName) {
        return soapFactory.createOMElement(wrapperElementName, soapBody);
    }

    private static void populateSOAPHeaders(
            org.apache.ode.bpel.iapi.Message messageFromOde,
            SOAPEnvelope soapEnvelope,
            SOAPFactory soapFactory,
            List<javax.wsdl.extensions.soap.SOAPHeader> soapHaderDefinitions,
            Operation operation
    ) throws BPELFault {
        if (messageFromOde.getHeaderParts().size() > 0
                || soapHaderDefinitions.size() > 0) {
            for (javax.wsdl.extensions.soap.SOAPHeader soapHeaderDefinition : soapHaderDefinitions) {
                handleSOAPHeaderElementsInBindingOperation(
                        soapEnvelope,
                        soapFactory,
                        messageFromOde,
                        operation,
                        soapHeaderDefinition);
            }

            org.apache.axiom.soap.SOAPHeader soapHeader = soapEnvelope.getHeader();
            if (soapHeader == null) {
                soapHeader = soapFactory.createSOAPHeader(soapEnvelope);
            }

            for (Node headerNode : messageFromOde.getHeaderParts().values()) {
                if (headerNode.getNodeType() == Node.ELEMENT_NODE) {
                    addSOAPHeaderBock(soapHeader, headerNode, soapFactory);
                } else {
                    throw new BPELFault("SOAP Header Must be an Element");
                }
            }
        }
    }

    private static void addSOAPHeaderBock(
            final org.apache.axiom.soap.SOAPHeader soapHeader,
            final Node headerNode,
            final SOAPFactory soapFactory) {
        if (soapHeader.getFirstChildWithName(new QName(headerNode.getNamespaceURI(),
                headerNode.getLocalName())) == null) {
            OMNamespace ns = null;
            if (headerNode.getNamespaceURI() != null) {
                if (headerNode.getPrefix() != null) {
                    ns = soapFactory.createOMNamespace(headerNode.getNamespaceURI(), headerNode.getPrefix());
                } else {
                    ns = soapFactory.createOMNamespace(headerNode.getNamespaceURI(), "");
                }
            }
            SOAPHeaderBlock hb = soapHeader.addHeaderBlock(headerNode.getLocalName(),
                    ns);
            NSContext nscontext = DOMUtils.getMyNSContext((Element) headerNode);
            injectNamespaces(hb, nscontext.toMap());

            NamedNodeMap attrs = headerNode.getAttributes();
            for (int i = 0; i < attrs.getLength(); ++i) {
                Attr attr = (Attr) attrs.item(i);

                if (attr.getLocalName().equals("xmlns")
                        || (attr.getNamespaceURI() != null && attr.getNamespaceURI().equals(DOMUtils.NS_URI_XMLNS))) {
                    continue;
                }
                OMNamespace attrOmNs = null;
                String attrNs = attr.getNamespaceURI();
                String attrPrefix = attr.getPrefix();

                if (attrNs != null) {
                    attrOmNs = hb.findNamespace(attrNs, null);
                }
                if (attrOmNs == null && attrPrefix != null) {
                    attrOmNs = hb.findNamespace(null, attrPrefix);
                }
                hb.addAttribute(attr.getLocalName(), attr.getValue(), attrOmNs);
            }
            NodeList childNodes = headerNode.getChildNodes();
            for (int i = 0; i < childNodes.getLength(); i++) {
                Node node = childNodes.item(i);
                switch (childNodes.item(i).getNodeType()) {
                    case Node.CDATA_SECTION_NODE:
                        hb.addChild(soapFactory.createOMText(node.getTextContent(), XMLStreamConstants.CDATA));
                        break;
                    case Node.TEXT_NODE:
                        hb.addChild(soapFactory.createOMText(node.getTextContent(), XMLStreamConstants.CHARACTERS));
                        break;
                    case Node.ELEMENT_NODE:
                        OMUtils.toOM((Element) node, soapFactory, hb);
                        break;
                }
            }
        }
    }

    private static void injectNamespaces(OMElement omElement, Map<String, String> nscontext) {
        for (String prefix : nscontext.keySet()) {
            String uri = nscontext.get(prefix);
            if (prefix.equals("")) {
                omElement.declareDefaultNamespace(uri);
            } else {
                omElement.declareNamespace(uri, prefix);
            }
        }
    }

    public static BindingOperation getBindingOperation(
            final BPELMessageContext bpelMessageContext, final String operationName) {
        BindingOperation bOp = bpelMessageContext.getWsdlBindingForCurrentMessageFlow()
                .getBindingOperation(operationName, null, null);
        if (bOp == null) {
            throw new NullPointerException("BindingOperation not found for operation "
                    + operationName + ".");
        }

        return bOp;
    }

    private static BindingOutput getBindingOutPut(final BindingOperation bOp) {
        BindingOutput bOutput = bOp.getBindingOutput();
        if (bOutput == null) {
            throw new NullPointerException("BindingOutput cannot be null for operation "
                    + bOp.getName() + ".");
        }

        return bOutput;
    }

    private static void checkForNullValuesInResponse(
            final BPELMessageContext bpelMessageContext,
            final MessageExchange odeMessageExchange) {
        if (odeMessageExchange.getOperation() == null) {
            throw new NullPointerException("Null operation");
        }
        if (odeMessageExchange.getResponse() == null) {
            throw new NullPointerException("Null message.");
        }
        if (bpelMessageContext.getOutMessageContext() == null) {
            throw new NullPointerException("Null msgCtx");
        }
    }

    public static void checkForNullValuesInRequest(
            final BPELMessageContext bpelMessageContext,
            final MessageExchange odeMessageExchange) {
        if (odeMessageExchange.getOperation() == null) {
            throw new NullPointerException("Null operation");
        }
        if (odeMessageExchange.getRequest() == null) {
            throw new NullPointerException("Null message.");
        }
        if (bpelMessageContext.getInMessageContext() == null) {
            throw new NullPointerException("Null msgCtx");
        }
    }

    @SuppressWarnings("unchecked")
    private static void handleSOAPHeaderElementsInBindingOperation(
            SOAPEnvelope soapEnvelope,
            SOAPFactory soapFactory,
            org.apache.ode.bpel.iapi.Message messageFromOde,
            Operation wsdlOperation,
            final javax.wsdl.extensions.soap.SOAPHeader soapHeaderElementDefinition)
            throws BPELFault {
        Map<String, Node> headerParts = messageFromOde.getHeaderParts();

        Message responseMessageDefinition = wsdlOperation.getOutput() != null ?
                wsdlOperation.getOutput().getMessage() : null;

        // If there isn't a message attribute in header element definition or if the
        // message attribute value is equal to the response message QName, then this
        // header element is part of the actual payload.
        // Refer SOAP Binding specification at http://www.w3.org/TR/wsdl#_soap-b.
        final boolean isHeaderElementAPartOfPayload =
                soapHeaderElementDefinition.getMessage() == null
                        || ((wsdlOperation.getStyle() !=  OperationType.ONE_WAY) && soapHeaderElementDefinition.getMessage().equals(
                        responseMessageDefinition.getQName()));

        if (soapHeaderElementDefinition.getPart() == null) {
            // Part information not found. Ignoring this header definition.
            return;
        }

        if (isHeaderElementAPartOfPayload
                && (responseMessageDefinition != null && responseMessageDefinition.getPart(soapHeaderElementDefinition.getPart())
                == null)) {
            // If SOAP Header element is part of the actual payload and
            // if we couldn't find part in the response message definition equals to
            // name of the SOAP header element definition part attribute value,
            // we should throw a exception.
            throw new BPELFault("SOAP Header Element Definition refer unknown part.");
        }

        Element partElement = null;
        if (headerParts.size() > 0 && isHeaderElementAPartOfPayload) {
            try {
                partElement = (Element) headerParts.get(soapHeaderElementDefinition.getPart());
            } catch (ClassCastException e) {
                throw new BPELFault("SOAP Header must be a DOM Element.", e);
            }
        }

        // We only complain about missing header data if that data is a part of the actual
        // message payload. This is because, some headers will provided by SOAP engine.
        if (partElement == null && isHeaderElementAPartOfPayload) {
            if (messageFromOde.getPart(soapHeaderElementDefinition.getPart())
                    != null) {
                partElement = messageFromOde.getPart(
                        soapHeaderElementDefinition.getPart());
            } else {
                throw new BPELFault("Missing Required part in response message.");
            }
        }

        // Handle headers defined as abstract message parts. In this scenario, the header is not part of the payload,
        // and can be found and extracted from the odeMessage object
        if(partElement == null && messageFromOde.getParts().size() > 0 && !isHeaderElementAPartOfPayload) {
            try{
            partElement = (Element)messageFromOde.getPart(soapHeaderElementDefinition.getPart());
            }catch (ClassCastException e) {
               throw new BPELFault("Soap header must be an element" + messageFromOde.getPart(soapHeaderElementDefinition.getPart()));

            }
        }
        // If header is not part of the payload and if header element is null,
        // just ignore this case.
        if (partElement == null) {
            return;
        }

        org.apache.axiom.soap.SOAPHeader soapHeader = soapEnvelope.getHeader();
        if (soapHeader == null) {
            soapHeader = soapFactory
                    .createSOAPHeader(soapEnvelope);
        }

        OMElement omPart = OMUtils.toOM(partElement, soapFactory);
        for (Iterator<OMNode> i = omPart.getChildren(); i.hasNext(); ) {
            soapHeader.addChild(i.next());
        }

    }

    /**
     * Crete SOAP Fault from fault information returned from ODE.
     *
     * @param bpelMessageContext DTO containing information on current messageflow.
     * @param odeMessageContext  ODE MyRoleMessageExchange containing information on current process
     *                           invocation.
     * @return SOAPFault instance
     * @throws AxisFault in case of a error while creating SOAP Fault.
     */
    public static SOAPFault createSoapFault(final BPELMessageContext bpelMessageContext,
                                            final MessageExchange odeMessageContext)
            throws AxisFault {
        SOAPFactory soapFactory = bpelMessageContext.getSoapFactoryForCurrentMessageFlow();

        OMElement detail = buildSoapDetail(bpelMessageContext, odeMessageContext);

        SOAPFault fault = soapFactory.createSOAPFault();
        SOAPFaultCode code = soapFactory.createSOAPFaultCode(fault);
        code.setText(new QName(Namespaces.SOAP_ENV_NS, "Server"));
        SOAPFaultReason reason = soapFactory.createSOAPFaultReason(fault);
        reason.setText(odeMessageContext.getFault());
        SOAPFaultDetail soapDetail = soapFactory.createSOAPFaultDetail(fault);
        if (detail != null) {
            soapDetail.addDetailEntry(detail);
        }
        return fault;
    }

    private static OMElement buildSoapDetail(
            final BPELMessageContext bpelMessageContext,
            final MessageExchange odeMessageContext) throws AxisFault {
        Element message = odeMessageContext.getResponse().getMessage();
        QName faultName = odeMessageContext.getFault();
        Operation operation = odeMessageContext.getOperation();
        SOAPFactory soapFactory = bpelMessageContext.getSoapFactoryForCurrentMessageFlow();

        if (faultName.getNamespaceURI() == null) {
            return toFaultDetail(message, soapFactory);
        }
        Fault f = operation.getFault(faultName.getLocalPart());
        if (f == null) {
            return toFaultDetail(message, soapFactory);
        }

        // For faults, there will be exactly one part.
        Part p = (Part) f.getMessage().getParts().values().iterator().next();
        if (p == null) {
            return toFaultDetail(message, soapFactory);
        }
        Element partEl = DOMUtils.findChildByName(message, new QName(null, p.getName()));
        if (partEl == null) {
            return toFaultDetail(message, soapFactory);
        }
        Element detail = DOMUtils.findChildByName(partEl, p.getElementName());
        if (detail == null) {
            return toFaultDetail(message, soapFactory);
        }

        return OMUtils.toOM(detail, soapFactory);
    }

    private static OMElement toFaultDetail(final Element message, final SOAPFactory soapFactory) {
        if (message == null) {
            return null;
        }
        Element firstPart = DOMUtils.getFirstChildElement(message);
        if (firstPart == null) {
            return null;
        }
        Element detail = DOMUtils.getFirstChildElement(firstPart);
        if (detail == null) {
            return OMUtils.toOM(firstPart, soapFactory);
        }
        return OMUtils.toOM(detail, soapFactory);
    }

    public static org.apache.ode.bpel.iapi.Message parseSOAPResponseFromPartner(
            BPELMessageContext partnerInvocationContext,
            MessageExchange partnerRoleMessageExchange) throws BPELFault {
        org.apache.ode.bpel.iapi.Message messageToODE = partnerRoleMessageExchange.createMessage(
                partnerRoleMessageExchange.getOperation().getOutput().getMessage().getQName());

        BindingOperation bindingOp = getBindingOperation(partnerInvocationContext,
                partnerRoleMessageExchange.getOperationName());
        BindingOutput bindingOutPut = getBindingOutPut(bindingOp);
        SOAPEnvelope responseFromPartnerService =
                partnerInvocationContext.getOutMessageContext().getEnvelope();
        if (partnerInvocationContext.isSoap12()) {
            javax.wsdl.extensions.soap12.SOAP12Body soapBodyDefinition = getSOAP12Body(bindingOutPut);

            if (soapBodyDefinition != null) {
                if (responseFromPartnerService.getBody() != null) {
                    extractSOAPBodyParts(partnerRoleMessageExchange,
                            messageToODE,
                            responseFromPartnerService.getBody(),
                            soapBodyDefinition.getParts(),
                            soapBodyDefinition.getNamespaceURI(),
                            partnerInvocationContext.isRPCStyleOperation());
                } else {
                    throw new BPELFault("SOAP Body cannot be null for WSDL operation which "
                            + "requires SOAP Body.");
                }
            }
        } else {
            javax.wsdl.extensions.soap.SOAPBody soapBodyDefinition = getSOAP11Body(bindingOutPut);

            if (soapBodyDefinition != null) {
                if (responseFromPartnerService.getBody() != null) {
                    extractSOAPBodyParts(partnerRoleMessageExchange,
                            messageToODE,
                            responseFromPartnerService.getBody(),
                            soapBodyDefinition.getParts(),
                            soapBodyDefinition.getNamespaceURI(),
                            partnerInvocationContext.isRPCStyleOperation());
                } else {
                    throw new BPELFault("SOAP Body cannot be null for WSDL operation which "
                            + "requires SOAP Body.");
                }
            }
        }

        if (getSOAPHeaders(bindingOutPut) != null &&
                responseFromPartnerService.getHeader() != null) {
            extractSoapHeaderParts(messageToODE,
                    partnerInvocationContext.getBpelServiceWSDLDefinition(),
                    responseFromPartnerService.getHeader(),
                    getSOAPHeaders(bindingOutPut),
                    partnerRoleMessageExchange.getOperation().getOutput().getMessage());
        }

        return messageToODE;

    }

    public static org.apache.ode.bpel.iapi.Message parseResponseFromRESTService(BPELMessageContext partnerInvocationContext,
                                                                                PartnerRoleMessageExchange odePartnerMex) {
        org.apache.ode.bpel.iapi.Message messageToODE = odePartnerMex.createMessage(
                odePartnerMex.getOperation().getOutput().getMessage().getQName());
        BindingOperation bindingOp = getBindingOperation(partnerInvocationContext,
                odePartnerMex.getOperationName());
        BindingOutput bindingOutPut = getBindingOutPut(bindingOp);
        javax.wsdl.extensions.mime.MIMEContent mimeContent = getFirstExtensibilityElement(bindingOutPut, MIMEContent.class);
        if (mimeContent != null) {
            SOAPEnvelope soapEnv = partnerInvocationContext.getOutMessageContext().getEnvelope();

            Iterator childElementsItr = soapEnv.getBody().getChildElements();
            while (childElementsItr.hasNext()) {
                OMNode child = (OMNode) childElementsItr.next();
                if (child.getType() == OMNode.ELEMENT_NODE) {
                    Document doc = DOMUtils.newDocument();
                    Element domPart = doc.createElementNS(null, mimeContent.getPart());
                    domPart.appendChild(doc.importNode(OMUtils.toDOM((OMElement) child), true));
                    messageToODE.setPart(mimeContent.getPart(), domPart);

                    return messageToODE;
                }
            }
        }
        throw new IllegalArgumentException("WSO2 BPS only support HTTP binding with mime output.");
    }

    private static void extractSOAPBodyParts(
            MessageExchange partnerRoleMessageExchange,
            org.apache.ode.bpel.iapi.Message messageToODE,
            SOAPBody omSOAPBody,
            List parts,
            String namespace,
            boolean isRPC) throws BPELFault {
        List<Part> messageBodyParts = partnerRoleMessageExchange.getOperation()
                .getOutput().getMessage().getOrderedParts(parts);
        if (isRPC) {
            String rpcWrapperElementName = partnerRoleMessageExchange.getOperationName()
                    + "Response";
            OMElement rpcWrapperElement = omSOAPBody.getFirstChildWithName(
                    new QName(namespace, rpcWrapperElementName));
            if (rpcWrapperElement == null) {
                throw new BPELFault("Message body doesn't contain expected part wrapper: " +
                        new QName(namespace, rpcWrapperElementName));
            }
            // In RPC the body element is the operation name, wrapping parts. Order doesn't
            // really matter as far as we're concerned. All we need to do is copy the soap:body
            // children, since doc-lit rpc looks the same in ode and soap.
            for (Part partDef : messageBodyParts) {
                OMElement omPartElement = rpcWrapperElement.getFirstChildWithName(
                        new QName(null, partDef.getName()));
                if (omPartElement == null) {
                    throw new BPELFault("SOAP body doesn't contain required part: " +
                            new QName(null, partDef.getName()));
                }
                messageToODE.setPart(omPartElement.getLocalName(), OMUtils.toDOM(omPartElement));
            }
        } else {
            // In doc-literal style, we expect the elements in the body to correspond (in order)
            // to the parts defined in the binding. All the parts should be element-typed,
            // otherwise it is a mess.
            Iterator<OMElement> omParts = omSOAPBody.getChildElements();
            for (Part partDef : messageBodyParts) {
                if (!omParts.hasNext()) {
                    throw new BPELFault("SOAP body doesn't contain required part.");
                }

                OMElement omPart = omParts.next();
                if (partDef.getElementName() == null) {
                    throw new BPELFault("Binding defines non element document list parts.");
                }

                if (!omPart.getQName().equals(partDef.getElementName())) {
                    throw new BPELFault("Unexpected element in SOAP body: " + omPart.getQName());
                }
                Document doc = DOMUtils.newDocument();
                Element domPart = doc.createElementNS(null, partDef.getName());
                domPart.appendChild(doc.importNode(OMUtils.toDOM(omPart), true));
                messageToODE.setPart(partDef.getName(), domPart);
            }
        }
    }

    private static void extractSoapHeaderParts(org.apache.ode.bpel.iapi.Message message,
                                               Definition wsdl,
                                               org.apache.axiom.soap.SOAPHeader soapHeader,
                                               List<javax.wsdl.extensions.soap.SOAPHeader> headerDefs,
                                               Message msg) throws BPELFault {
        // Checking that the definitions we have are at least there
        for (javax.wsdl.extensions.soap.SOAPHeader headerDef : headerDefs) {
            handleSoapHeaderPartDef(message, wsdl, soapHeader, headerDef, msg);
        }

        // Extracting whatever header elements we find in the message, binding and abstract parts
        // aren't reliable enough given what people do out there.
        Iterator headersIter = soapHeader.getChildElements();
        while (headersIter.hasNext()) {
            OMElement header = (OMElement) headersIter.next();
            String partName = findHeaderPartName(headerDefs, wsdl, header.getQName());
            message.setHeaderPart(partName, OMUtils.toDOM(header));
        }
    }

    private static void handleSoapHeaderPartDef(org.apache.ode.bpel.iapi.Message odeMessage,
                                                Definition wsdl,
                                                SOAPHeader header,
                                                javax.wsdl.extensions.soap.SOAPHeader headerdef,
                                                Message msgType) throws BPELFault {
        // Is this header part of the "payload" messsage?
        boolean payloadMessageHeader = headerdef.getMessage() == null
                || headerdef.getMessage().equals(msgType.getQName());
        boolean requiredHeader = payloadMessageHeader
                || (headerdef.getRequired() != null && headerdef.getRequired());

        if (requiredHeader && header == null) {
            throw new BPELFault("SOAP Header missing required element.");
        }
        if (header == null) {
            return;
        }

        Message hdrMsg = wsdl.getMessage(headerdef.getMessage());
        if (hdrMsg == null) {
            return;
        }
        Part p = hdrMsg.getPart(headerdef.getPart());
        if (p == null || p.getElementName() == null) {
            return;
        }
        OMElement headerEl = header.getFirstChildWithName(p.getElementName());
        if (requiredHeader && headerEl == null) {
            throw new BPELFault("SOAP Header missing required element: " + p.getElementName());
        }

        if (headerEl == null) {
            return;
        }

        odeMessage.setHeaderPart(p.getName(), OMUtils.toDOM(headerEl));
    }

    private static String findHeaderPartName(List<javax.wsdl.extensions.soap.SOAPHeader> headerDefs,
                                             Definition wsdl,
                                             QName elmtName) {
        for (javax.wsdl.extensions.soap.SOAPHeader headerDef : headerDefs) {
            Message hdrMsg = wsdl.getMessage(headerDef.getMessage());
            for (Object o : hdrMsg.getParts().values()) {
                Part p = (Part) o;
                if (p.getElementName().equals(elmtName)) {
                    return p.getName();
                }
            }
        }
        return elmtName.getLocalPart();
    }

    public static Fault parseSoapFault(Element odeMsgEl, SOAPEnvelope envelope, Operation operation)
            throws AxisFault {
        SOAPFault flt = envelope.getBody().getFault();
        SOAPFaultDetail detail = flt.getDetail();
        Fault fdef = inferFault(operation, flt);
        if (fdef == null) {
            return null;
        }

        Part pdef = (Part) fdef.getMessage().getParts().values().iterator().next();
        Element partel = odeMsgEl.getOwnerDocument().createElementNS(null, pdef.getName());
        odeMsgEl.appendChild(partel);

        if (detail.getFirstChildWithName(pdef.getElementName()) != null) {
            partel.appendChild(odeMsgEl.getOwnerDocument().importNode(
                    OMUtils.toDOM(detail.getFirstChildWithName(pdef.getElementName())), true));
        } else {
            partel.appendChild(odeMsgEl.getOwnerDocument().importNode(OMUtils.toDOM(detail), true));
        }

        return fdef;
    }

    private static Fault inferFault(Operation operation, SOAPFault flt) {
        if (flt.getDetail() == null) {
            return null;
        }

        if (flt.getDetail().getFirstElement() == null) {
            return null;
        }

        // The detail is a dummy <detail> node containing the interesting fault element
        QName elName = flt.getDetail().getFirstElement().getQName();
        return WsdlUtils.inferFault(operation, elName);
    }


    public static javax.wsdl.extensions.soap.SOAPBody getSOAP11Body(final ElementExtensible ee) {
        return getFirstExtensibilityElement(ee, javax.wsdl.extensions.soap.SOAPBody.class);
    }

    public static javax.wsdl.extensions.soap12.SOAP12Body getSOAP12Body(final ElementExtensible ee) {
        return getFirstExtensibilityElement(ee, javax.wsdl.extensions.soap12.SOAP12Body.class);
    }

    public static <T> T getFirstExtensibilityElement(final ElementExtensible parent,
                                                     final Class<T> cls) {
        Collection<T> ee = CollectionsX.filter(parent.getExtensibilityElements(), cls);

        return ee.isEmpty() ? null : ee.iterator().next();

    }

    @SuppressWarnings("unchecked")
    public static List<javax.wsdl.extensions.soap.SOAPHeader> getSOAPHeaders(
            final ElementExtensible eee) {
        return CollectionsX.filter(new ArrayList<javax.wsdl.extensions.soap.SOAPHeader>(),
                (Collection<Object>) eee.getExtensibilityElements(),
                javax.wsdl.extensions.soap.SOAPHeader.class);
    }
}
