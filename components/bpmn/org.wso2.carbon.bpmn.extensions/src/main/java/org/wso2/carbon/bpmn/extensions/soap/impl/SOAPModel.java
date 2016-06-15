/**
 * Copyright (c) 2015 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.bpmn.extensions.soap.impl;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.wso2.carbon.bpmn.extensions.soap.constants.Constants;
import org.wso2.carbon.bpmn.extensions.soap.constants.SOAP11Constants;
import org.wso2.carbon.bpmn.extensions.soap.constants.SOAP12Constants;
import org.wso2.carbon.bpmn.extensions.soap.util.DOMUtil;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;


/**
 * Model used to build the SOAP envelope which contains all the methods.
 */
public class SOAPModel {

    private SOAPEnvelope envelope;
    private DocumentBuilder docBuilder;
    private Document doc;
    private String soapVersion;

    protected SOAPModel(String soapVersion) throws SOAPException {
        this.soapVersion = soapVersion;
        envelope = new SOAPEnvelope(this);
        docBuilder = createDocumentBuilder();
        doc = docBuilder.newDocument();
    }

    /**
     * Gets the SOAP version of the message.
     *
     * @return soap version
     */
    public String getSoapVersion() {
        return soapVersion;
    }

    /**
     * Creates the Document Builder object.
     *
     * @return documentbuilder
     * @throws SOAPException
     */
    private DocumentBuilder createDocumentBuilder() throws SOAPException {
        DocumentBuilderFactory docFactory = DOMUtil.getSecuredDocumentBuilder();
        DocumentBuilder docBuilder = null;
        try {
            docFactory.setNamespaceAware(true);
            docBuilder = docFactory.newDocumentBuilder();
        } catch (ParserConfigurationException pce) {
            throw new SOAPException("Configuration Error", pce);
        }


        return docBuilder;
    }

    /**
     * Creates the SOAP Envelope.
     *
     * @return SOAP Envelope
     * @throws SOAPException
     */
    public SOAPEnvelope createSOAPEnvelope() throws SOAPException {

        String namespaceURI = null;
        String encodingNSURI = null;
        if (soapVersion.equals(Constants.SOAP11_VERSION)) {
            namespaceURI = SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI;
            encodingNSURI = SOAP11Constants.SOAP_ENCODING_NAMESPACE_URI;
        } else {
            namespaceURI = SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI;
            encodingNSURI = SOAP12Constants.SOAP_ENCODING_NAMESPACE_URI;
        }
        Element rootElement = doc.createElementNS(namespaceURI,
                Constants.SOAP_ENVELOPE);
        rootElement.setPrefix(Constants.SOAP_NAMESPACE_PREFIX);

        Attr encodingStyleAttr = doc.createAttributeNS(namespaceURI, Constants.ENCODING_STYLE);
        encodingStyleAttr.setValue(encodingNSURI);
        encodingStyleAttr.setPrefix(Constants.SOAP_NAMESPACE_PREFIX);
        rootElement.setAttributeNode(encodingStyleAttr);

        envelope.setSoapEnvelopeElement(rootElement);


        return envelope;
    }

    /**
     * Generates the entire SOAP Request/Envelope.
     *
     * @return SOAP Request/Envelope
     * @throws SOAPException
     */
    public SOAPEnvelope generateSOAPEnvelope() throws SOAPException {
        envelope = getSoapEnvelope();

        if (envelope != null) {
            if (envelope.getSoapHeader() == null) {
                createSOAPHeader();
            }
            if (envelope.getSoapBody() == null) {
                createSOAPBody();
            }
        } else {
            createSOAPEnvelope();
            createSOAPHeader();
            createSOAPBody();

        }

        return envelope;
    }

    /**
     * Creates the node object when attaching elements to the SOAP Body and Header.
     *
     * @param payload
     * @return node
     * @throws SOAPException
     */
    public Node createNode(String payload) throws SOAPException {
        Node fragmentNode = null;
        try {
            fragmentNode = docBuilder.parse(new InputSource(new StringReader(payload)))
                    .getDocumentElement();
        } catch (SAXException e) {
            throw new SOAPException("Error with the XML Parser", e);
        } catch (IOException e) {
            throw new SOAPException("An I/O operation has been failed or interrupted", e);
        }
        fragmentNode = doc.importNode(fragmentNode, true);
        return fragmentNode;

    }

    /**
     * Creates the SOAP Body without elements.
     *
     * @return SOAP Body
     * @throws SOAPException
     */
    public SOAPBody createSOAPBody() throws SOAPException {

        String namespaceURI = null;
        SOAPBody soapBody = null;
        if (soapVersion.equals(Constants.SOAP11_VERSION)) {
            namespaceURI = SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI;
        } else {
            namespaceURI = SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI;
        }
        //Creates the main SOAP Body Element
        if (envelope != null) {
            if (envelope.getSoapBody() == null) {

                Element bodyElement = doc.createElementNS(namespaceURI, Constants.BODY);
                bodyElement.setPrefix(Constants.SOAP_NAMESPACE_PREFIX);

                soapBody = new SOAPBody(bodyElement);
                envelope.setSoapBody(soapBody);
            } else {
                soapBody = envelope.getSoapBody();
                envelope.setSoapBody(soapBody);
            }
        } else {
            throw new SOAPException("No SOAP Envelope created!!!");
        }

        return soapBody;

    }

    /**
     * Creates the SOAP Body by attaching the payload.
     *
     * @param payload element/request payload to be attached
     * @return SOAP Body
     * @throws SOAPException
     */
    public SOAPBody createSOAPBody(Node payload) throws SOAPException {

        String namespaceURI = null;
        SOAPBody soapBody = null;
        if (soapVersion.equals(Constants.SOAP11_VERSION)) {
            namespaceURI = SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI;
        } else {
            namespaceURI = SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI;
        }
        //Creates the main SOAP Body Element
        if (envelope != null) {
            if (envelope.getSoapBody() == null) {

                Element bodyElement = doc.createElementNS(namespaceURI, Constants.BODY);
                bodyElement.setPrefix(Constants.SOAP_NAMESPACE_PREFIX);

                //soapBody.setSoapBodyElement(bodyElement);
                soapBody = new SOAPBody(bodyElement);
                soapBody.setPayload(payload);

                bodyElement.appendChild(payload);
                envelope.setSoapBody(soapBody);
            } else {
                soapBody = envelope.getSoapBody();
                soapBody.setPayload(payload);

                envelope.setSoapBody(soapBody);
            }
        } else {
            throw new SOAPException("No SOAP Envelope created!!!");
        }
        return soapBody;

    }

    /**
     * Gets the SOAP Envelope.
     *
     * @return SOAP Envelope
     */
    public SOAPEnvelope getSoapEnvelope() {
        return envelope;
    }


    /**
     * Creates the SOAP Header without elements.
     *
     * @return SOAP Header
     * @throws SOAPException
     */
    public SOAPHeader createSOAPHeader() throws SOAPException {

        String namespaceURI = null;
        SOAPHeader soapHeader = null;
        if (soapVersion.equals(Constants.SOAP11_VERSION)) {
            namespaceURI = SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI;
        } else {
            namespaceURI = SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI;
        }
        //Creates the main SOAP Body Element
        if (envelope != null) {
            if (envelope.getSoapHeader() == null) {

                Element headerElement = doc.createElementNS(namespaceURI, Constants.HEADER);
                headerElement.setPrefix(Constants.SOAP_NAMESPACE_PREFIX);

                soapHeader = new SOAPHeader(headerElement);
                envelope.setSoapHeader(soapHeader);
            } else {
                soapHeader = envelope.getSoapHeader();
                envelope.setSoapHeader(soapHeader);
            }
        } else {
            throw new SOAPException("No SOAP Envelope created!!!");
        }

        return soapHeader;

    }

    /**
     * Creates the SOAP Header by attaching the header.
     *
     * @param header
     * @return SOAP Header
     * @throws SOAPException
     */
    public SOAPHeader createSOAPHeader(Node header) throws SOAPException {

        String namespaceURI = null;
        SOAPHeader soapHeader = null;
        if (soapVersion.equals(Constants.SOAP11_VERSION)) {
            namespaceURI = SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI;
        } else {
            namespaceURI = SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI;
        }
        //Creates the main SOAP Body Element
        if (envelope != null) {
            if (envelope.getSoapHeader() == null) {

                Element headerElement = doc.createElementNS(namespaceURI, Constants.HEADER);
                headerElement.setPrefix(Constants.SOAP_NAMESPACE_PREFIX);

                soapHeader = new SOAPHeader(headerElement);
                soapHeader.setHeader(header);

                headerElement.appendChild(header);
                envelope.setSoapHeader(soapHeader);
            } else {
                soapHeader = envelope.getSoapHeader();
                soapHeader.setHeader(header);

                envelope.setSoapHeader(soapHeader);
            }
        } else {
            throw new SOAPException("No SOAP Envelope created!!!");
        }
        return soapHeader;

    }

    /**
     * Creates the SOAP Header by attaching the header blocks.
     *
     * @param headers
     * @return SOAP Header
     * @throws SOAPException
     */
    public SOAPHeader createSOAPHeader(NodeListImpl headers) throws SOAPException {

        String namespaceURI = null;
        SOAPHeader soapHeader = null;
        if (soapVersion.equals(Constants.SOAP11_VERSION)) {
            namespaceURI = SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI;
        } else {
            namespaceURI = SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI;
        }
        //Creates the main SOAP Body Element
        if (envelope != null) {
            if (envelope.getSoapHeader() == null) {

                Element headerElement = doc.createElementNS(namespaceURI, Constants.HEADER);
                headerElement.setPrefix(Constants.SOAP_NAMESPACE_PREFIX);
                soapHeader = new SOAPHeader(headerElement);
                soapHeader.setHeaders(headers);
                for (int i = 0; i < headers.getLength(); i++) {
                    Node n = headers.item(i);
                    headerElement.appendChild(n);
                }

                envelope.setSoapHeader(soapHeader);
            } else {
                soapHeader = envelope.getSoapHeader();
                soapHeader.setHeaders(headers);

                envelope.setSoapHeader(soapHeader);
            }
        } else {
            throw new SOAPException("No SOAP Envelope created!!!");
        }
        return soapHeader;

    }

    /**
     * Creating the SOAP Envelope when a string is given.
     *
     * @param messageBody
     * @return SOAP Envelope
     * @throws SOAPException
     * @throws java.io.IOException
     * @throws org.xml.sax.SAXException
     */
    public SOAPEnvelope createSOAPEnvelope(String messageBody) throws SOAPException, IOException, SAXException {
        doc = docBuilder.parse(new InputSource(new StringReader(messageBody)));

        Element rootElement = doc.getDocumentElement();
        envelope.setSoapEnvelopeElement(rootElement);

        if (rootElement.getNamespaceURI().equals(SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI) &&
                !(getSoapVersion().equals(Constants.SOAP12_VERSION))) {
            throw new SOAPException("Namespace URI of envelope is of SOAP12 but soap version given is of SOAP11." +
                    " SOAP version mismatch");
        } else if (rootElement.getNamespaceURI().equals(SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI) &&
                !(getSoapVersion().equals(Constants.SOAP11_VERSION))) {
            throw new SOAPException("Namespace URI of envelope is of SOAP11 but soap version given is of SOAP12." +
                    " SOAP version mismatch");
        }

        org.w3c.dom.NodeList children = rootElement.getChildNodes();
        Node current = null;
        int count = children.getLength();
        for (int i = 0; i < count; i++) {
            current = children.item(i);
            Element element = (Element) current;

            if (element.getLocalName().equalsIgnoreCase("Header")) {
                SOAPHeader soapHeader = new SOAPHeader(element);
                envelope.setSoapHeader(soapHeader);
            } else if (element.getLocalName().equalsIgnoreCase("Body")) {
                SOAPBody soapBody = new SOAPBody(element);
                envelope.setSoapBody(soapBody);
            }

        }

        return envelope;
    }


}
