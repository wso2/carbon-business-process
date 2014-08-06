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

package org.wso2.carbon.attachment.mgt.core.dao;

import org.wso2.carbon.attachment.mgt.api.attachment.Attachment;
import org.wso2.carbon.attachment.mgt.core.exceptions.AttachmentMgtException;

/**
 * Interface for DAO operations related to Attachment Management
 */
public interface AttachmentMgtDAOFactory {

    /**
     * Add an attachment to the data-source
     * @return DAO related to crated Attachment
     * @throws AttachmentMgtException if attachment couldn't saved
     */
    public AttachmentDAO addAttachment(Attachment attachment) throws AttachmentMgtException;

    /**
     * Returns the attachment DAO for a given attachment-id
     * @return attachment DAO related to the attachment id
     * @throws AttachmentMgtException
     */
    public AttachmentDAO getAttachmentInfo (String id) throws AttachmentMgtException;

    /**
     * Remove the attachment with the given id
     * @param id attachment id
     * @return whether the attachment removed or not
     * @throws AttachmentMgtException
     */
    public boolean removeAttachment (String id) throws AttachmentMgtException;

    /**
     * Returns the attachment DAO for a given attachment-url
     *
     * @param attachmentURI a unique property maintain per each attachment
     * @return DAO related to the attachment url
     */
    public AttachmentDAO getAttachmentInfoFromURL (String attachmentURI) throws AttachmentMgtException;
}
