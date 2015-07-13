/*
 * Copyright (c) 2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.attachment.mgt.client.dto2api;

import org.apache.axis2.databinding.types.URI;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.attachment.mgt.api.attachment.Attachment;
import org.wso2.carbon.attachment.mgt.core.AttachmentImpl;
import org.wso2.carbon.attachment.mgt.core.exceptions.AttachmentMgtException;
import org.wso2.carbon.attachment.mgt.skeleton.types.TAttachment;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.util.ByteArrayDataSource;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;

/**
 * This class manages conversions between org.wso2.carbon.attachment.mgt.skeleton.types and org
 * .wso2.carbon.attachment.mgt.core.dao
 */
public class TransformerUtil {
    /**
     * Logger class
     */
    private static Log log = LogFactory.getLog(TransformerUtil.class);

    /**
     * Transform (DTO) {@link TAttachment} to {@link Attachment}
     *
     * @param attachment
     * @return
     * @throws AttachmentMgtException
     */
    public static Attachment convertAttachment(TAttachment attachment) throws AttachmentMgtException {
        Attachment attachmentDTO = null;
        attachmentDTO = new AttachmentImpl(attachment.getName(), attachment.getCreatedBy(), attachment.getContentType(),
                attachment.getContent(), attachment.getCreatedTime().getTime());
        return attachmentDTO;
    }

    /**
     * Transform {@link Attachment} to (DTO) {@link TAttachment}
     *
     * @param attachment
     * @return
     */
    public static TAttachment convertAttachment(Attachment attachment) throws AttachmentMgtException {
        TAttachment attachmentDTO = new TAttachment();
        attachmentDTO.setId(attachment.getId());
        attachmentDTO.setName(attachment.getName());
        attachmentDTO.setCreatedBy(attachment.getCreatedBy());

        Calendar cal = Calendar.getInstance();
        cal.setTime(attachment.getCreatedTime());
        attachmentDTO.setCreatedTime(cal);

        attachmentDTO.setContentType(attachment.getContentType());

        attachmentDTO.setContent(attachment.getContent());

        try {
            URI attachmentURI = new URI(attachment.getURL().toString());
            attachmentDTO.setUrl(attachmentURI);
        } catch (URI.MalformedURIException e) {
            log.error(e.getLocalizedMessage(), e);
            throw new AttachmentMgtException("Conversion of Attachment to TAttachment (DTO) failed due to reason : " +
                                             e.getLocalizedMessage(), e);
        }

        return attachmentDTO;
    }


}