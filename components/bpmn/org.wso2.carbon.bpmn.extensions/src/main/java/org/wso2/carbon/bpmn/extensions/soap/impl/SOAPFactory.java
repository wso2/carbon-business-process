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

/**
 * Factory class used to create SOAP Models.
 */
public class SOAPFactory {

    private SOAPModel soapModel;

    /**
     * Gets a new instance of the SOAP Model used to create a SOAP Envelope.
     *
     * @param soapVersion soap version to be used i.e. soap11 or soap12
     * @return new instance of the SOAP Model
     * @throws SOAPException
     */
    public SOAPModel getSoapModel(String soapVersion) throws SOAPException {
        soapModel = new SOAPModel(soapVersion);
        return soapModel;
    }

    /**
     * Gets a new instance of the SOAP Model used to create a SOAP Envelope.
     *
     * @return new instance of the SOAP Model
     * @throws SOAPException
     */
    public SOAPModel getSoapModel() throws SOAPException {
        return getSoapModel(Constants.SOAP11_VERSION);
    }

    /**
     * Sets a SOAP Model to a the factory.
     *
     * @param soapModel
     */
    public void setSoapModel(SOAPModel soapModel) {
        this.soapModel = soapModel;
    }
}
