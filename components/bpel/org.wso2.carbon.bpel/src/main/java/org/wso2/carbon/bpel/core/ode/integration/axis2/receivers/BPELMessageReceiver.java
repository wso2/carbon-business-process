/*
 * Copyright (c) 2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.bpel.core.ode.integration.axis2.receivers;

import org.apache.axiom.attachments.Attachments;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.engine.AxisEngine;
import org.apache.axis2.receivers.AbstractMessageReceiver;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.attachment.mgt.skeleton.AttachmentMgtException;
import org.wso2.carbon.attachment.mgt.skeleton.types.TAttachment;
import org.wso2.carbon.bpel.core.BPELConstants;
import org.wso2.carbon.bpel.core.internal.BPELServerHolder;
import org.wso2.carbon.bpel.core.ode.integration.BPELMessageContext;
import org.wso2.carbon.bpel.core.ode.integration.BPELProcessProxy;
import org.wso2.carbon.bpel.core.ode.integration.utils.BPELMessageContextFactory;

import java.util.ArrayList;
import java.util.List;
import javax.activation.DataHandler;

import static org.wso2.carbon.bpel.core.ode.integration.utils.BPELMessageContextFactory.hasResponse;

/**
 * Axis Engine will handover the incoming message to a service exposed by
 * BPEL process to this. This will handover the message to the BPELProcessProxy after doing
 * some pre processing.
 * <p/>
 * The fact that "BPELMessageReceiver extends AbstractMessageReceiver" explains most of the
 * basic things.
 */
public class BPELMessageReceiver extends AbstractMessageReceiver {
    private static final String REQ_RES_TRACE = "org.wso2.carbon.bpel.ReqResTraceLog";
    private static Log log = LogFactory.getLog(BPELMessageReceiver.class);
    private static Log messageTraceLog = LogFactory.getLog(BPELConstants.MESSAGE_TRACE);
    private static Log reqResTraceLog = LogFactory.getLog(REQ_RES_TRACE);

    private BPELProcessProxy processProxy;
    private long requestTime;
    private String status = "FAIL";

    /**
     * Upload each attachment from attachment-map and returns a list of attachment ids.
     *
     * @param attachmentsMap
     * @return list of attachment ids
     */
    private List<String> persistAttachments(Attachments attachmentsMap) {
        List<String> attachmentIdList = new ArrayList<String>();
        //for each attachment upload it
        for (String id : attachmentsMap.getAllContentIDs()) {
            DataHandler attachmentContent = attachmentsMap.getDataHandler(id);

            try {
                String attachmentID = BPELServerHolder.getInstance().getAttachmentService()
                        .getAttachmentService().add(createAttachmentDTO(attachmentContent));
                attachmentIdList.add(attachmentID);
                log.info("Attachment added. ID : " + attachmentID);
            } catch (AttachmentMgtException e) {
                log.error(e.getLocalizedMessage(), e);
            }
        }
        return attachmentIdList;
    }

    private TAttachment createAttachmentDTO(DataHandler attachmentHandler) {
        TAttachment attachment = new TAttachment();

        String attachmentName = attachmentHandler.getName();
        attachment.setName(attachmentName);

        log.warn("Couldn't determine the name of BPEL client. So the owner of the attachment:" + attachmentName + " " +
                "will be the default bpel client" + org.wso2.carbon.bpel.core.BPELConstants.DAFAULT_BPEL_CLIENT);
        attachment.setCreatedBy(org.wso2.carbon.bpel.core.BPELConstants.DAFAULT_BPEL_CLIENT);

        attachment.setContentType(attachmentHandler.getContentType());
        //As well there are some other parameters to be set.
        attachment.setContent(attachmentHandler);

        return attachment;
    }

    protected final void invokeBusinessLogic(final MessageContext inMessageContext)
            throws AxisFault {

        if (messageTraceLog.isDebugEnabled()) {
            messageTraceLog.debug("Message received: " +
                    inMessageContext.getAxisService().getName() + "." +
                    inMessageContext.getAxisOperation().getName());
            if (messageTraceLog.isTraceEnabled()) {
                messageTraceLog.trace("Request message: " +
                        inMessageContext.getEnvelope());
            }
        }

        SOAPFactory soapFactory = getSOAPFactory(inMessageContext);
        final BPELMessageContext bpelMessageContext = BPELMessageContextFactory.createBPELMessageContext(
                inMessageContext,
                processProxy,
                soapFactory);

        //Initializing the attachments in the BPEL Message Context
        List<String> attachmentIDs = persistAttachments(inMessageContext.getAttachmentMap());
        if (attachmentIDs != null && !attachmentIDs.isEmpty()) {
            bpelMessageContext.setAttachmentIDList(attachmentIDs);
        }

        if (hasResponse(inMessageContext.getAxisOperation())) {
            handleInOutOperation(bpelMessageContext);
            if (messageTraceLog.isDebugEnabled()) {
                messageTraceLog.debug("Reply Sent: " +
                        inMessageContext.getAxisService().getName() + "." +
                        inMessageContext.getAxisOperation().getName());
                if (messageTraceLog.isTraceEnabled()) {
                    messageTraceLog.trace("Response message: " +
                            bpelMessageContext.getOutMessageContext().getEnvelope());
                }
            }
        } else {
            handleInOnlyOperation(bpelMessageContext);
        }
    }

    public final void setProcessProxy(final BPELProcessProxy processProxy) {
        this.processProxy = processProxy;
    }

    private void handleInOutOperation(BPELMessageContext bpelMessageContext) throws AxisFault {
        traceRequestLine(bpelMessageContext.getInMessageContext());
        if (log.isDebugEnabled()) {
            log.debug("Received request message for "
                    + bpelMessageContext.getInMessageContext().getAxisService().getName() + "."
                    + bpelMessageContext.getInMessageContext().getAxisOperation().getName());
        }
        try {
            processProxy.onAxisServiceInvoke(bpelMessageContext);
        } catch (AxisFault e) {
            status = "FAIL";
            traceStatus(bpelMessageContext.getInMessageContext().getMessageID(), "response");
            throw e;
        }
        status = bpelMessageContext.getOutMessageContext().isFault() ? "FAIL" : "SUCCESS";
        if (log.isDebugEnabled()) {
            log.debug("Reply for "
                    + bpelMessageContext.getInMessageContext().getAxisService().getName() + "."
                    + bpelMessageContext.getInMessageContext().getAxisOperation().getName());
            log.debug("\tReply message "
                    + bpelMessageContext.getOutMessageContext().getEnvelope());
        }
        try {
            AxisEngine.send(bpelMessageContext.getOutMessageContext());
        } catch (AxisFault e) {
            status = "FAIL";
            traceStatus(bpelMessageContext.getInMessageContext().getMessageID(), "response");
            throw e;
        }
        status = "SUCCESS";
        traceStatus(bpelMessageContext.getInMessageContext().getMessageID(), "response");
    }

    private void handleInOnlyOperation(BPELMessageContext bpelMessageContext) throws AxisFault {
        traceRequestLine(bpelMessageContext.getInMessageContext());
        if (log.isDebugEnabled()) {
            log.debug("Received one-way message for "
                    + bpelMessageContext.getInMessageContext().getAxisService().getName() + "."
                    + bpelMessageContext.getInMessageContext().getAxisOperation().getName());
        }
        try {
            processProxy.onAxisServiceInvoke(bpelMessageContext);
        } catch (AxisFault e) {
            status = "FAIL";
            traceStatus(bpelMessageContext.getInMessageContext().getMessageID(), "response");
            throw e;
        }
        status = "SUCCESS";
        traceStatus(bpelMessageContext.getInMessageContext().getMessageID(), "response");
    }

    private void traceStatus(String messageId, String direction) {
        if (reqResTraceLog.isTraceEnabled()) {
            reqResTraceLog.trace("UUID=" + messageId +
                    ",Direction:" + direction +
                    ",Status=" + status +
                    ",timeSpentForResponse=" + (System.currentTimeMillis() - requestTime));
        }
    }

    private void traceRequestLine(final MessageContext inMessageContext) {
        if (reqResTraceLog.isTraceEnabled()) {
            requestTime = System.currentTimeMillis();
            AxisService axisService = inMessageContext.getAxisService();
            String epName = axisService.getEndpointName();
            epName = axisService.getEndpoint(epName).getEndpointURL();
            reqResTraceLog.trace("To:" + epName +
                    ",MessageID:" + inMessageContext.getMessageID() +
                    ",Direction:request" +
                    ",requestTime:" + requestTime);
        }
    }

}
