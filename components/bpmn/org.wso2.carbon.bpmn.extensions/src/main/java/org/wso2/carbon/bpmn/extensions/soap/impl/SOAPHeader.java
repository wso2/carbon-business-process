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

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;


/**
 * Creates the SOAP Header of the SOAP Envelope.
 */
public class SOAPHeader {


    private Element soapHeaderElement;


    protected SOAPHeader() {
    }

    protected SOAPHeader(Element soapHeaderElement) {
        this.soapHeaderElement = soapHeaderElement;
    }

    /**
     * Gets the SOAP Header element.
     *
     * @return SOAP Header element
     */
    public Element getSoapHeaderElement() {

        return soapHeaderElement;
    }

    /**
     * Sets the SOAP Header element.
     *
     * @param soapHeaderElement
     */
    public void setSoapHeaderElement(Element soapHeaderElement) {
        this.soapHeaderElement = soapHeaderElement;
    }

    /**
     * Gets the header at the specified index.
     *
     * @param index
     * @return header at the specified index
     */
    public Node getHeader(int index) {
        return soapHeaderElement.getFirstChild();
    }

    /**
     * Sets the header to the SOAP Header.
     *
     * @param header
     */
    public void setHeader(Node header) {

        soapHeaderElement.appendChild(header);
    }

    /**
     * Get all the headers i.e. all the header blocks from the SOAP Header.
     *
     * @return nodeList with all the headers
     */
    public NodeListImpl getAllHeaders() {
        org.w3c.dom.NodeList list = soapHeaderElement.getChildNodes();
        NodeListImpl nodes = new NodeListImpl();
        for (int i = 0; i < list.getLength(); i++) {
            Node n = list.item(i);
            nodes.addNode(n);
        }
        return nodes;
    }

    /**
     * Sets the headers to the SOAP Header.
     *
     * @param headers
     */
    public void setHeaders(NodeListImpl headers) {
        for (int i = 0; i < headers.getLength(); i++) {
            Node n = headers.item(i);
            setHeader(n);
        }
    }

    /**
     * Deletes the specified header.
     *
     * @param node
     */
    public void deleteHeaders(Node node) {
        soapHeaderElement.removeChild(node);
    }

    /**
     * Deletes the header at the specified index.
     *
     * @param index
     */
    public void deleteHeaders(int index) {
        soapHeaderElement.removeChild(soapHeaderElement.getChildNodes().item(index));
    }

    /**
     * Returns the SOAP Header as a string.
     *
     * @return SOAP Header as a string
     */
    public String serialize() throws SOAPException {
        String str = null;
        Transformer serializer = null;
        try {
            serializer = TransformerFactory.newInstance().newTransformer();
            StringWriter stw = new StringWriter();
            serializer.transform(new DOMSource(soapHeaderElement), new StreamResult(stw));
            str = stw.toString();

        } catch (TransformerConfigurationException e) {
            throw new SOAPException("Configuration error when converting the element to string");
        } catch (TransformerException e) {
            throw new SOAPException("Exceptional condition that occured during the transformation process" +
                    " when converting the element to string");
        }


        return str;
    }


}
