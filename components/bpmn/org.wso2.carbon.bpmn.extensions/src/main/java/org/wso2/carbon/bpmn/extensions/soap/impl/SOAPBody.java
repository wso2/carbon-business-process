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
 * Creates the SOAP body of the SOAP Envelope.
 */
public class SOAPBody {


    private Element soapBodyElement;


    protected SOAPBody() {
    }

    protected SOAPBody(Element soapBodyElement) {
        this.soapBodyElement = soapBodyElement;
    }

    /**
     * Gets the SOAP Body element.
     *
     * @return SOAP Body element
     */
    public Element getSoapBodyElement() {

        return soapBodyElement;
    }

    /**
     * Sets the SOAP Body element.
     *
     * @param soapBodyElement
     */
    public void setSoapBodyElement(Element soapBodyElement) {
        this.soapBodyElement = soapBodyElement;
    }

    /**
     * Gets the payload of the SOAP Body.
     *
     * @return the payload
     */
    public Node getPayload() {
        return soapBodyElement.getFirstChild();
    }

    /**
     * Sets the payload to SOAP Body.
     *
     * @param payload
     */
    public void setPayload(Node payload) {

        soapBodyElement.appendChild(payload);
    }

    /**
     * Deletes the payload from the SOAP Body.
     */
    public void deletePayload() {

        soapBodyElement.removeChild(soapBodyElement.getFirstChild());


    }

    /**
     * Returns the SOAP Body as a string.
     *
     * @return SOAP Body as a string
     */
    public String serialize() throws SOAPException {
        String str = null;
        Transformer serializer = null;
        try {
            serializer = TransformerFactory.newInstance().newTransformer();
            StringWriter stw = new StringWriter();
            serializer.transform(new DOMSource(soapBodyElement), new StreamResult(stw));
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
