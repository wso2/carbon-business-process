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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.bpmn.extensions.soap.SOAPCallBackResponse;

/**
 * Handles the response message received.
 */
public class SOAPCallBackResponseImpl implements SOAPCallBackResponse {
    private static final Logger log = LoggerFactory.getLogger(SOAPCallBackResponseImpl.class);
    private boolean success = false;
    private String responseMessage;

    public SOAPCallBackResponseImpl() {

    }

    /**
     * Gets the response.
     * @return reponse as a string i.e. serialized
     */
    public String getResponseMessage() {
        return responseMessage;
    }

    /**
     * Sets the response.
     * @param responseMessage
     */
    public void setResponseMessage(String responseMessage) {
        this.responseMessage = responseMessage;
    }

    /**
     * Checks whether the response is fully received.
     * @return true if received else false
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * Sets true or false depending on the status of the response received.
     * @param success
     */
    public void setSucess(boolean success) {
        this.success = success;
    }

    @Override
    public void handleResponseReceived(CarbonSOAPMessage carbonSOAPMessage) {

        try {
            responseMessage = carbonSOAPMessage.getSOAPMessage().serialize();
            setResponseMessage(responseMessage);
            setSucess(true);

        } catch (Throwable e) {
            log.error("SOAP Exception when processing the response message", e);
        }
    }
}
