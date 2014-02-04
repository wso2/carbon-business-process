/*
 * Copyright (c) 2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.humantask.core.integration;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAPFault;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.engine.AxisEngine;
import org.apache.axis2.receivers.AbstractMessageReceiver;
import org.apache.axis2.util.MessageContextBuilder;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bpel.common.WSDLAwareSOAPProcessor;
import org.wso2.carbon.humantask.core.HumanTaskConstants;
import org.wso2.carbon.humantask.core.engine.HumanTaskEngine;
import org.wso2.carbon.humantask.core.integration.utils.SOAPUtils;
import org.wso2.carbon.humantask.core.store.HumanTaskBaseConfiguration;

/**
 * Message receiver for the humantasks exposed as services.
 */
public class AxisHumanTaskMessageReceiver extends AbstractMessageReceiver {

    private static Log log = LogFactory.getLog(AxisHumanTaskMessageReceiver.class);
    private static Log messageTraceLog = LogFactory.getLog(HumanTaskConstants.MESSAGE_TRACE);

    /**
     * The human task engine
     */
    private HumanTaskEngine humanTaskEngine;

    /**
     * Attach BaseConfiguration at the time of service deployment to the message receiver.
     */
    private HumanTaskBaseConfiguration taskBaseConfiguration;

    @Override
    protected void invokeBusinessLogic(MessageContext messageContext) throws AxisFault {

        if (messageTraceLog.isDebugEnabled()) {
            messageTraceLog.debug("Message received: " +
                    messageContext.getAxisService().getName() + "." +
                    messageContext.getAxisOperation().getName());
            if (messageTraceLog.isTraceEnabled()) {
                messageTraceLog.trace("Request message: " +
                        messageContext.getEnvelope());
            }
        }

        WSDLAwareSOAPProcessor soapProcessor = new WSDLAwareSOAPProcessor(messageContext);
        String taskId;

        if (hasResponse(messageContext.getAxisOperation())) {
            //Task
            MessageContext outMessageContext = MessageContextBuilder.createOutMessageContext(messageContext);
            outMessageContext.getOperationContext().addMessageContext(outMessageContext);
            SOAPEnvelope envelope = getSOAPFactory(messageContext).getDefaultEnvelope();

            try {
                taskId = humanTaskEngine.invoke(soapProcessor.parseRequest(), taskBaseConfiguration);

                if (taskId != null) {
                    if (log.isDebugEnabled()) {
                        log.debug("Task: " + taskId + "successfully created");
                    }
                    envelope.getBody().addChild(getFeedbackPayLoad(taskId));
                } else {
                    String reason = "Error occurred while initiating human task. The task ID is not found";
                    handleFault(soapProcessor.getSoapFactory(), envelope, reason);
                }
            } catch (Exception e) {
                handleFault(soapProcessor.getSoapFactory(), envelope, e.getMessage());
                log.error("Task creation failed.", e);
            }
            outMessageContext.setEnvelope(envelope);
            if (messageTraceLog.isDebugEnabled()) {
                messageTraceLog.debug("Replied TaskID: " +
                        messageContext.getAxisService().getName() + "." +
                        messageContext.getAxisOperation().getName());
                if (messageTraceLog.isTraceEnabled()) {
                    messageTraceLog.trace("Replied TaskID message: " +
                            outMessageContext.getEnvelope());
                }
            }
            AxisEngine.send(outMessageContext);
        } else {
            //Notification
            if (log.isDebugEnabled()) {
                log.debug("Notification request received.");
            }
            try {
                taskId = humanTaskEngine.invoke(soapProcessor.parseRequest(), taskBaseConfiguration);

                if (log.isDebugEnabled()) {
                    log.debug("Notification: " + taskId + "successfully created");
                }
            } catch (Exception e) {
                log.error("Notification creation failed.", e);
            }
        }
    }

    private void handleFault(SOAPFactory soapFactory, SOAPEnvelope envelope, String reason) {
        SOAPFault fault = SOAPUtils.createSOAPFault(soapFactory, reason);
        envelope.getBody().addFault(fault);
    }

    /**
     * @param humanTaskEngine : The human task engine reference.
     */
    public void setHumanTaskEngine(HumanTaskEngine humanTaskEngine) {
        this.humanTaskEngine = humanTaskEngine;
    }

    public void setTaskBaseConfiguration(HumanTaskBaseConfiguration taskBaseConfiguration) {
        this.taskBaseConfiguration = taskBaseConfiguration;
    }

    // checks whether the provided AxisOperation has a response.
    private boolean hasResponse(AxisOperation op) {
        switch (op.getAxisSpecificMEPConstant()) {
            case WSDLConstants.MEP_CONSTANT_IN_OUT:
                return true;
            case WSDLConstants.MEP_CONSTANT_OUT_ONLY:
                return true;
            case WSDLConstants.MEP_CONSTANT_OUT_OPTIONAL_IN:
                return true;
            case WSDLConstants.MEP_CONSTANT_ROBUST_OUT_ONLY:
                return true;
            default:
                return false;
        }
    }

    // constructs the feed back payload with the created task id.
    private OMElement getFeedbackPayLoad(String taskID) {
        OMFactory fbOMFactory = OMAbstractFactory.getOMFactory();
        OMElement payLoadEle = fbOMFactory.createOMElement("part", null);
        OMElement hiFeedbackEle = fbOMFactory.createOMElement(HumanTaskConstants.B4P_CORRELATION_HEADER,
                fbOMFactory.createOMNamespace(HumanTaskConstants.B4P_NAMESPACE, null),
                payLoadEle);
        OMElement taskIDEle =
                fbOMFactory.createOMElement(HumanTaskConstants.B4P_CORRELATION_HEADER_ATTRIBUTE,
                        fbOMFactory.createOMNamespace(HumanTaskConstants.B4P_NAMESPACE, null),
                        hiFeedbackEle);
        taskIDEle.setText(taskID);
        return payLoadEle;
    }
}
