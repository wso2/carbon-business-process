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


import org.wso2.carbon.bpmn.extensions.soap.SOAPCallBackResponse;
import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.CarbonMessage;

/**
 * Callback class used in request path to execute in response path.
 * When response arrives callback methods should execute.
 */
public class CallbackSOAPMessage implements CarbonCallback {

    private CarbonSOAPMessage response;
    private SOAPCallBackResponse soapCallBackResponse;

    public CallbackSOAPMessage() {
    }

    public CallbackSOAPMessage(SOAPCallBackResponse soapCallBackResponse) {
        this.soapCallBackResponse = soapCallBackResponse;
    }

    /**
     * Calls in response path to do work for response.
     *
     * @param cMsg CarbonMessage to be processed.
     */
    @Override
    public void done(CarbonMessage cMsg) {

        response = new CarbonSOAPMessage(cMsg);

        //Handle the received response message
        soapCallBackResponse.handleResponseReceived(response);

    }

    /**
     * Gets the response from the response path.
     *
     * @return CarbonSOAPMessage
     */
    public CarbonSOAPMessage getResponse() {

        return response;
    }

}
