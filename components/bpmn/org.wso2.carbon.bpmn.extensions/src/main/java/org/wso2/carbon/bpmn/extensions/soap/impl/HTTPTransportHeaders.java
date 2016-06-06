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
import org.wso2.carbon.bpmn.extensions.soap.constants.SOAP11Constants;
import org.wso2.carbon.bpmn.extensions.soap.constants.SOAP12Constants;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Defines the transport binding rules specific to each soap version.
 */
public class HTTPTransportHeaders {
    private Map<String, String> headers = new ConcurrentHashMap<String, String>();

    public HTTPTransportHeaders(SOAPModel soapModel) throws SOAPException {

        String soapVersion = soapModel.getSoapVersion();
        if (soapVersion.equals(Constants.SOAP11_VERSION)) {
            headers.put("Content-Type", SOAP11Constants.SOAP11_CONTENT_TYPE);
            headers.put("SOAPAction", "\"\""); //Mandatory
        } else {
            headers.put("Content-Type", SOAP12Constants.SOAP12_CONTENT_TYPE);
            headers.put("SOAPAction", "\"\""); //Optional
        }

    }

    /**
     * Get the HTTP transport headers.
     *
     * @return List of headers
     */
    public Map<String, String> getHeaders() {
        return headers;
    }

    /**
     * Set the HTTP transport headers.
     *
     * @param headers List of Headers
     */
    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    /**
     * Add a new HTTP transport header field.
     * NOTE : Either the Content-Length or Transfer-Encoding should be specified when setting the headers.
     * Add the Host and the Connection fields when adding the headers
     *
     * @param key   Name of the HTTP transport header field
     * @param value value of the HTTP transport header field
     */
    public void addHeader(String key, String value) {
        headers.put(key, value);
    }

    /**
     * Add the value for the SOAPAction field.
     *
     * @param soapAction
     */
    public void addSOAPAction(String soapAction) {
        headers.put("SOAPAction", soapAction);
    }
}
