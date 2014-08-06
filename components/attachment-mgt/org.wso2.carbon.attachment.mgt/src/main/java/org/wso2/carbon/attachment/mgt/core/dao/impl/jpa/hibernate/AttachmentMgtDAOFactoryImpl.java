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
import org.wso2.carbon.attachment.mgt.core.dao.AttachmentDAO;
import org.wso2.carbon.attachment.mgt.core.dao.AttachmentMgtDAOFactory;
import org.wso2.carbon.attachment.mgt.core.dao.impl.jpa.openjpa.entity.AttachmentDAOImpl;
import org.wso2.carbon.attachment.mgt.core.exceptions.AttachmentMgtException;
import org.wso2.carbon.attachment.mgt.core.scheduler.Scheduler;
import org.wso2.carbon.attachment.mgt.util.URLGeneratorUtil;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.List;
import java.util.concurrent.Callable;

public class AttachmentMgtDAOFactoryImpl implements AttachmentMgtDAOFactory {
    /**
     * Class Logger
     */
    private static Log log = LogFactory.getLog(AttachmentMgtDAOFactoryImpl.class);

    private EntityManager entityManager;

    /**
     * Job executor to manage transactional level implementation
     */
    JobExecutor jobExecutor;

    public AttachmentMgtDAOFactoryImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
        jobExecutor = new JobExecutor();
    }

    @Override
    public AttachmentDAO addAttachment(final Attachment attachment) throws AttachmentMgtException {
        try {
            AttachmentDAO resultantDAO = jobExecutor.execTransaction(new Callable<AttachmentDAO>() {
                @Override
                public AttachmentDAO call() throws Exception {
                    AttachmentDAO attachmentDAO = new AttachmentDAOImpl();
                    attachmentDAO.setName(attachment.getName());
                    attachmentDAO.setCreatedBy(attachment.getCreatedBy());
                    attachmentDAO.setContentType(attachment.getContentType());
                    attachmentDAO.setUrl(URLGeneratorUtil.generateURL());
                    attachmentDAO.setContent(attachment.getContent());

                    entityManager.persist(attachmentDAO);
                    if (entityManager.contains(attachmentDAO)) {
                        return attachmentDAO;
                    } else {
                        String errorMsg = "Attachment couldn't persist in the Data Store";
                        throw new AttachmentMgtException(errorMsg);
                    }

                }
            });
            return resultantDAO;
        } catch (Exception e) {
            String errorMsg = "org.wso2.carbon.attachment.mgt.core.dao.impl.hibernate.AttachmentMgtDAOFactoryImpl.addAttachment operation failed. " +
                    "Reason: " + e.getLocalizedMessage();
            log.error(errorMsg, e);
            throw new AttachmentMgtException(errorMsg, e);
        }
    }

    @Override
    public AttachmentDAO getAttachmentInfo(final String id) throws AttachmentMgtException {
        try {
            AttachmentDAO resultantDAO = jobExecutor.execTransaction(new Callable<AttachmentDAO>() {
                @Override
                public AttachmentDAO call() throws Exception {
                    AttachmentDAO attachmentDAO = null;
                    attachmentDAO = entityManager.find(AttachmentDAOImpl.class, Long.parseLong(id));
                    return attachmentDAO;
                }
            });

            if (resultantDAO != null) {
                return resultantDAO;
            } else {
                throw new AttachmentMgtException("Attachment not found for id : " + id);
            }
        } catch (Exception e) {
            String errorMsg = "org.wso2.carbon.attachment.mgt.core.dao.impl.hibernate.AttachmentMgtDAOFactoryImpl.getAttachmentInfo operation failed. " +
                    "Reason: " + e.getLocalizedMessage();
            log.error(errorMsg, e);
            throw new AttachmentMgtException(errorMsg, e);
        }
    }

    @Override
    public boolean removeAttachment(final String id) throws AttachmentMgtException {
        try {
            boolean isRemoved = false;
            isRemoved = jobExecutor.execTransaction(new Callable<Boolean>() {
                @Override
                public Boolean call() throws Exception {
                    Query query = entityManager.createQuery("DELETE FROM org.wso2.carbon.attachment.mgt.core.dao.impl" +
                            ".jpa.openjpa.entity.AttachmentDAOImpl x WHERE x.id = " +
                            ":attachmentID");
                    query.setParameter("attachmentID", Long.parseLong(id));
                    int noOfRowsUpdated = query.executeUpdate();
                    //entityManager.remove(getAttachmentInfo(id));
                    if (noOfRowsUpdated == 1) {
                        return true;
                    } else {
                        return false;
                    }
                }
            });
            return isRemoved;
        } catch (Exception e) {
            String errorMsg = "org.wso2.carbon.attachment.mgt.core.dao.impl.hibernate.AttachmentMgtDAOFactoryImpl.removeAttachment operation failed. " +
                    "Reason: " + e.getLocalizedMessage();
            log.error(errorMsg, e);
            throw new AttachmentMgtException(errorMsg, e);
        }
    }

    @Override
    public AttachmentDAO getAttachmentInfoFromURL(final String attachmentURI) throws AttachmentMgtException {
        try {
            AttachmentDAO resultantDAO = jobExecutor.execTransaction(new Callable<AttachmentDAO>() {
                @Override
                public AttachmentDAO call() throws Exception {
                    Query query = entityManager.createQuery("SELECT x FROM org.wso2.carbon.attachment.mgt.core.dao.impl.jpa.openjpa.entity.AttachmentDAOImpl AS x WHERE x.url = :attachmentURI");
                    query.setParameter("attachmentURI", attachmentURI);

                    List<AttachmentDAO> daoList = query.getResultList();

                    if (daoList.isEmpty()) {
                        throw new AttachmentMgtException("Attachment not found for the uri:" + attachmentURI);
                    } else if (daoList.size() != 1) {
                        String errorMsg = "There exist more than one attachment for the attachment URI:" + attachmentURI + ". org" +
                                ".wso2.carbon.attachment.mgt.util.URLGeneratorUtil.generateURL method has generated " +
                                "similar uris for different attachments. This has caused a major inconsistency for " +
                                "attachment management.";
                        log.fatal(errorMsg);
                        throw new AttachmentMgtException(errorMsg);
                    } else {
                        return daoList.get(0);
                    }
                }
            });
            return resultantDAO;
        } catch (Exception e) {
            String errorMsg = "org.wso2.carbon.attachment.mgt.core.dao.impl.hibernate.AttachmentMgtDAOFactoryImpl.getAttachmentInfoFromURL operation failed. " +
                    "Reason: " + e.getLocalizedMessage();
            log.error(errorMsg, e);
            throw new AttachmentMgtException(errorMsg, e);
        }
    }

    class JobExecutor implements Scheduler {
        @Override
        public <T> T execTransaction(Callable<T> transaction) throws Exception {
            // run in new transaction
            Exception ex = null;

            try {
                if (log.isDebugEnabled()) {
                    log.debug("Beginning a new transaction");
                }
                entityManager.getTransaction().begin();
            } catch (Exception e) {
                String errMsg = "Internal Error, could not begin transaction. Reason : " + e.getLocalizedMessage();
                throw new AttachmentMgtException(errMsg, e);
            }

            try {
                ex = null;
                return transaction.call();
            } catch (Exception e) {
                ex = e;
            } finally {
                if (ex == null) {
                    if (log.isDebugEnabled()) {
                        log.debug("Committing on " + entityManager.getTransaction() + "...");
                    }
                    try {
                        entityManager.getTransaction().commit();
                    } catch (Exception e2) {
                        ex = e2;
                    }
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug("Rollback on " + entityManager.getTransaction() + "...");
                    }
                    entityManager.getTransaction().rollback();
                }

            }

            throw ex;
        }
    }
}
