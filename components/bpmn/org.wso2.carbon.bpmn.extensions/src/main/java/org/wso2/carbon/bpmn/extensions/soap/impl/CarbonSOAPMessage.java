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


import org.wso2.carbon.bpmn.extensions.soap.constants.Constants;
import org.wso2.carbon.bpmn.extensions.soap.constants.SOAP12Constants;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.DefaultCarbonMessage;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

/**
 *  SOAP message implementation from the Carbon Message.
 */
public class CarbonSOAPMessage extends DefaultCarbonMessage {
    private SOAPEnvelope soapEnvelope;

    public CarbonSOAPMessage(CarbonMessage cMsg) {
        List<ByteBuffer> fullMessageBody = cMsg.getFullMessageBody();
        for (ByteBuffer buffer : fullMessageBody) {
            this.addMessageBody(buffer);
        }
        Map headers = cMsg.getHeaders();
        setHeaders(headers);

    }

    public CarbonSOAPMessage() {
    }

    /**
     * Get the SOAP Envelope.
     *
     * @return SOAP Envelope
     */
    public SOAPEnvelope getSOAPMessage() throws SOAPException, IOException, SAXException {

        if (soapEnvelope != null) {
            return soapEnvelope;
        } else {
            ByteBuffer byteBuffer = getMessageBody();
            byte[] data = new byte[byteBuffer.remaining()];
            byteBuffer.get(data);
            String token = new String(data, Charset.defaultCharset());
            Map headers = getHeaders();
            String contentType = (String.valueOf(headers.get("Content-Type"))).split(";")[0].trim();
            String soapVersion = null;
            if (contentType.equals(SOAP12Constants.SOAP12_CONTENT_TYPE)) {
                soapVersion = Constants.SOAP12_VERSION;
            } else {
                soapVersion = Constants.SOAP11_VERSION;
            }
            SOAPModel soapModel = new SOAPModel(soapVersion);
            soapModel.createSOAPEnvelope(token);
            soapEnvelope = soapModel.getSoapEnvelope();
            return soapEnvelope;
        }

    }

    /**
     * Set the SOAP Envelope to the message body.
     *
     * @param soapEnvelope
     */
    public void setSOAPMessage(SOAPEnvelope soapEnvelope) throws SOAPException {
        String stringMessageBody = soapEnvelope.serialize();
        addMessageBody(ByteBuffer.wrap(stringMessageBody.getBytes(Charset.defaultCharset())));
        setEndOfMsgAdded(true);
    }

    /**
     * Set the Header properties.
     *
     * @param httpTransportHeaders
     */
    public void setHeaderProperties(HTTPTransportHeaders httpTransportHeaders) {
        setHeaders(httpTransportHeaders.getHeaders());
    }
}
