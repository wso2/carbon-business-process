/*
 *
 *   Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 * /
 */
package org.wso2.carbon.attachment.mgt.core.dao.impl.jpa.hibernate;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.attachment.mgt.api.attachment.Attachment;
import org.wso2.carbon.attachment.mgt.core.AttachmentImpl;
import org.wso2.carbon.attachment.mgt.core.dao.AttachmentDAO;
import org.wso2.carbon.attachment.mgt.core.dao.AttachmentMgtDAOTransformerFactory;
import org.wso2.carbon.attachment.mgt.core.dao.impl.jpa.openjpa.entity.AttachmentDAOImpl;
import org.wso2.carbon.attachment.mgt.core.exceptions.AttachmentMgtException;

import java.net.URISyntaxException;

public class AttachmentMgtDAOTransformerFactoryImpl implements AttachmentMgtDAOTransformerFactory {
    private static Log log = LogFactory.getLog(AttachmentMgtDAOTransformerFactoryImpl.class);
    @Override
    public AttachmentDAO convertAttachment(Attachment attachment) throws AttachmentMgtException {
        AttachmentDAO attachmentDAO = new AttachmentDAOImpl();
        attachmentDAO.setName(attachment.getName());
        attachmentDAO.setCreatedBy(attachment.getCreatedBy());
        attachmentDAO.setContent(attachment.getContent());
        attachmentDAO.setContentType(attachment.getContentType());

        return attachmentDAO;
    }

    @Override
    public Attachment convertAttachment(AttachmentDAO attachment) throws AttachmentMgtException {
        Attachment attachmentDTO = null;
        try {
            attachmentDTO = new AttachmentImpl(attachment);
        } catch (URISyntaxException e) {
            log.error(e.getLocalizedMessage(), e);
            throw new AttachmentMgtException("Object conversion failed due to reason : " + e.getLocalizedMessage(), e);
        }

        return attachmentDTO;
    }
}
