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

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.impl.el.FixedValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.bpmn.extensions.soap.SOAPCallBackResponse;
import org.xml.sax.SAXException;

import java.io.IOException;

/**
 * Handles the response message received.
 */
public class SOAPCallBackResponseImpl implements SOAPCallBackResponse {
    private static final Logger log = LoggerFactory.getLogger(SOAPCallBackResponseImpl.class);
    private DelegateExecution execution;
    private FixedValue outputVariable;

    public SOAPCallBackResponseImpl(DelegateExecution execution, FixedValue outputVariable) {
        this.execution = execution;
        this.outputVariable = outputVariable;
    }

    @Override
    public void handleResponseReceived(CarbonSOAPMessage carbonSOAPMessage) {

        try {
            log.info(carbonSOAPMessage.getSOAPEnvelope().serialize());
            String response = carbonSOAPMessage.getSOAPEnvelope().serialize();
            if (outputVariable != null) {
                String outVarName = outputVariable.getValue(execution).toString();
               // execution.setVariable(outVarName, response);

            } else {
                String outputNotFoundErrorMsg = "Output variable is not provided. " +
                        "outputVariable must be provided to save " +
                        "the response.";
                throw new SOAPException(outputNotFoundErrorMsg);
            }
        } catch (SOAPException e) {
          //  throw new BpmnError("SOAP Exception when processing the response message");
            log.error("SOAP Exception when processing the response message");
        } catch (SAXException e) {
         //   throw new BpmnError("SAX Exception");
            log.error("SAX Exception");
        } catch (IOException e) {
           // throw new BpmnError("I/O Exception");
            log.error("I/O Exception");
        }
    }
}
