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

import java.io.StringWriter;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;


/**
 * Creates the SOAP Envelope i.e. the SOAP request payload.
 */
public class SOAPEnvelope {


    private SOAPBody soapBody;
    private Element soapEnvelopeElement;
    private SOAPHeader soapHeader;
    private SOAPModel soapModel;


    protected SOAPEnvelope(SOAPModel soapModel) throws SOAPException {
        this.soapModel = soapModel;
        soapEnvelopeElement = null;
    }

    public SOAPModel getSoapModel() {
        return soapModel;
    }

    /**
     * Gets the SOAP Envelope element.
     *
     * @return SOAP Envelope element
     */
    public Element getSoapEnvelopeElement() {
        return soapEnvelopeElement;
    }

    /**
     * Sets the SOAP Envelope element.
     *
     * @param soapEnvelopeElement
     */
    public void setSoapEnvelopeElement(Element soapEnvelopeElement) throws SOAPException {
        this.soapEnvelopeElement = soapEnvelopeElement;
    }

    /**
     * Gets the SOAP Body element from the SOAP Envelope.
     *
     * @return SOAP Body
     */
    public SOAPBody getSoapBody() {

        return soapBody;
    }

    /**
     * Sets the SOAP Body element to the SOAP Envelope.
     *
     * @param soapBody
     */
    public void setSoapBody(SOAPBody soapBody) throws SOAPException {
        if (this.soapEnvelopeElement != null) {
            soapEnvelopeElement.appendChild(soapBody.getSoapBodyElement());
            this.soapBody = soapBody;
        } else {
            throw new SOAPException("Soap Envelope Element is null");
        }

    }

    /**
     * Gets the SOAP Header element from the SOAP Envelope.
     *
     * @return SOAP Header
     */
    public SOAPHeader getSoapHeader() {

        return soapHeader;
    }

    /**
     * Sets the SOAP Header element to the SOAP Envelope.
     *
     * @param soapHeader
     */
    public void setSoapHeader(SOAPHeader soapHeader) {

        if (soapBody != null || this.soapHeader == null) {
            soapEnvelopeElement.insertBefore(soapHeader.getSoapHeaderElement(), soapEnvelopeElement.getFirstChild());
        } else {
            soapEnvelopeElement.appendChild(soapHeader.getSoapHeaderElement());
        }
        this.soapHeader = soapHeader;
    }

    /**
     * Returns the SOAP Envelope as a string.
     *
     * @return SOAP Envelope as a string
     */
    public String serialize() throws SOAPException {
        String str = null;
        Transformer serializer = null;
        try {
            serializer = TransformerFactory.newInstance().newTransformer();
            StringWriter stw = new StringWriter();
            serializer.transform(new DOMSource(soapEnvelopeElement), new StreamResult(stw));
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

