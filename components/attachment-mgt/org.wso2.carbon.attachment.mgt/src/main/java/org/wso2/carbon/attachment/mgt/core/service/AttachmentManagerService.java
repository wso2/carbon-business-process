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

package org.wso2.carbon.attachment.mgt.core.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.attachment.mgt.api.attachment.Attachment;
import org.wso2.carbon.attachment.mgt.client.dto2api.TransformerUtil;
import org.wso2.carbon.attachment.mgt.core.dao.AttachmentDAO;
import org.wso2.carbon.attachment.mgt.core.dao.AttachmentMgtDAOConnectionFactory;
import org.wso2.carbon.attachment.mgt.core.dao.AttachmentMgtDAOTransformerFactory;
import org.wso2.carbon.attachment.mgt.server.internal.AttachmentServerHolder;
import org.wso2.carbon.attachment.mgt.server.internal.AttachmentServiceComponent;
import org.wso2.carbon.attachment.mgt.skeleton.AttachmentMgtException;
import org.wso2.carbon.attachment.mgt.skeleton.AttachmentMgtServiceSkeletonInterface;
import org.wso2.carbon.attachment.mgt.skeleton.types.TAttachment;

import java.util.concurrent.Callable;

/**
 * Service skeleton implementation {@link AttachmentMgtServiceSkeletonInterface}
 */
public class AttachmentManagerService implements AttachmentMgtServiceSkeletonInterface {
    /**
     * Logger class
     */
    private static Log log = LogFactory.getLog(AttachmentManagerService.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public String add(final TAttachment attachment) throws AttachmentMgtException {

        if(log.isDebugEnabled() && attachment != null) {
            log.debug("Saving the attachment with id " + attachment.getId());
        }
        try {
            Attachment att = TransformerUtil.convertAttachment(attachment);

            // 1. get Att-Mgt DAO Conn factory
            // 2. get Att-Mgt DAO Connection
            // 3. addAttachment
            //getDaoConnectionFactory().getDAOConnection().addAttachment(att);
            AttachmentDAO daoImpl = getDaoConnectionFactory().getDAOConnection().getAttachmentMgtDAOFactory()
                    .addAttachment(att);

            return daoImpl.getID().toString();

        } catch (org.wso2.carbon.attachment.mgt.core.exceptions.AttachmentMgtException ex) {
            String errMsg = "org.wso2.carbon.attachment.mgt.core.service.AttachmentManagerService.add " +
                            "operation failed. Reason:" + ex.getLocalizedMessage();
            log.error(errMsg, ex);
            throw new AttachmentMgtException(errMsg, ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TAttachment getAttachmentInfo(final String id) throws AttachmentMgtException {
        try {
            AttachmentDAO attachmentDAO = getDaoConnectionFactory().getDAOConnection()
                    .getAttachmentMgtDAOFactory()
                    .getAttachmentInfo(id);

            Attachment attachment = getDaoTransformFactory().convertAttachment(attachmentDAO);

            return TransformerUtil.convertAttachment(attachment);
        } catch (org.wso2.carbon.attachment.mgt.core.exceptions.AttachmentMgtException e) {
            String errorMsg = "org.wso2.carbon.attachment.mgt.core.service" +
                              ".AttachmentManagerService.getAttachmentInfo operation failed. " +
                              "Reason:" + e.getLocalizedMessage();
            log.error(errorMsg, e);
            throw new AttachmentMgtException(errorMsg, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TAttachment getAttachmentInfoFromURL(final String attachmentURL) throws AttachmentMgtException {
        try {
            //Extracting the attachment uri
            String attachmentUniqueID = attachmentURL.substring(attachmentURL.lastIndexOf("/") + 1);
            AttachmentDAO attachmentDAO = getDaoConnectionFactory().getDAOConnection().getAttachmentMgtDAOFactory()
                    .getAttachmentInfoFromURL(attachmentUniqueID);

            Attachment attachment = getDaoTransformFactory().convertAttachment(attachmentDAO);

            return TransformerUtil.convertAttachment(attachment);
        } catch (org.wso2.carbon.attachment.mgt.core.exceptions.AttachmentMgtException e) {
            String errorMsg = "org.wso2.carbon.attachment.mgt.core.service.AttachmentManagerService.getAttachmentInfoFromURL operation failed. " +
                              "Reason:" + e.getLocalizedMessage();
            log.error(errorMsg, e);
            throw new AttachmentMgtException(errorMsg, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean remove(final String id) throws AttachmentMgtException {
        try {
            return getDaoConnectionFactory().getDAOConnection().getAttachmentMgtDAOFactory().removeAttachment(id);
        } catch (org.wso2.carbon.attachment.mgt.core.exceptions.AttachmentMgtException e) {
            String errorMsg = "org.wso2.carbon.attachment.mgt.core.service" +
                              ".AttachmentManagerService.remove operation failed. Reason:" + e
                    .getLocalizedMessage();
            log.error(errorMsg, e);
            throw new AttachmentMgtException(errorMsg, e);
        }
    }

    /**
     * @return : The {@link AttachmentMgtDAOConnectionFactory}
     */
    private AttachmentMgtDAOConnectionFactory getDaoConnectionFactory() throws org.wso2.carbon.attachment.mgt.core.exceptions.AttachmentMgtException {
        try {
            return AttachmentServerHolder.getInstance().getAttachmentServer().getDaoManager()
                    .getDAOConnectionFactory();
        } catch (org.wso2.carbon.attachment.mgt.core.exceptions.AttachmentMgtException e) {
            throw e;
        }
    }

    private AttachmentMgtDAOTransformerFactory getDaoTransformFactory() throws org.wso2.carbon.attachment.mgt.core.exceptions.AttachmentMgtException {
        try {
            return AttachmentServerHolder.getInstance().getAttachmentServer().getDaoManager()
                    .getDAOTransformerFactory();
        } catch (org.wso2.carbon.attachment.mgt.core.exceptions.AttachmentMgtException e) {
            throw e;
        }
    }
}