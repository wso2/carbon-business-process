/*
 * Copyright (c) 2013 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.bpel.b4p.coordination.dao.jpa.hibernate;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bpel.b4p.coordination.dao.HTCoordinationDAOConnection;
import org.wso2.carbon.bpel.b4p.coordination.dao.HTProtocolHandlerDAO;
import org.wso2.carbon.bpel.b4p.coordination.dao.TaskProtocolHandler;
import org.wso2.carbon.bpel.b4p.coordination.dao.jpa.openjpa.entity.HTProtocolHandler;

import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.Query;

/**
 *
 */
@Deprecated
public class HTCoordinationDAOConnectionImpl implements HTCoordinationDAOConnection {

    private static final Log log = LogFactory.getLog(HTCoordinationDAOConnectionImpl.class);

    /**
     * The entity manager handling object persistence
     */
    private EntityManager entityManager;

    /**
     * @param entityManager : The entity manager handling object persistence.
     */
    public HTCoordinationDAOConnectionImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    /**
     * @see org.wso2.carbon.bpel.b4p.coordination.dao.HTCoordinationDAOConnection#createCoordinatedTask(String, String)
     */
    @Override
    public HTProtocolHandlerDAO createCoordinatedTask(String messageID, String htProtocolHandlerURL) {
        HTProtocolHandlerDAO protocolHandlerDAO = new HTProtocolHandler();
        protocolHandlerDAO.setMessageID(messageID);
        protocolHandlerDAO.setProcessInstanceID(null);
        protocolHandlerDAO.setTaskID(null);
        protocolHandlerDAO.setHumanTaskProtocolHandlerURL(htProtocolHandlerURL);

        entityManager.persist(protocolHandlerDAO);
        return protocolHandlerDAO;

    }

    /**
     * @see org.wso2.carbon.bpel.b4p.coordination.dao.HTCoordinationDAOConnection#createCoordinatedTask(String,
     * String, String, String)
     */
    @Override
    public HTProtocolHandlerDAO createCoordinatedTask(String messageID, String htProtocolHandlerURL, String
            processInstanceID, String taskID) {
        HTProtocolHandlerDAO protocolHandlerDAO = new HTProtocolHandler();
        protocolHandlerDAO.setMessageID(messageID);
        protocolHandlerDAO.setProcessInstanceID(processInstanceID);
        protocolHandlerDAO.setTaskID(taskID);
        protocolHandlerDAO.setHumanTaskProtocolHandlerURL(htProtocolHandlerURL);

        entityManager.persist(protocolHandlerDAO);
        return protocolHandlerDAO;

    }

    /**
     * @see org.wso2.carbon.bpel.b4p.coordination.dao.HTCoordinationDAOConnection#updateProtocolHandler(String,
     * String, String)
     */
    @Override
    public void updateProtocolHandler(String messageID, String processInstanceID, String taskID) {
        entityManager.createQuery("UPDATE org.wso2.carbon.bpel.b4p.coordination.dao.jpa.openjpa.entity" +
                ".HTProtocolHandler t SET t.taskID = :taskID, t.processInstanceID = :processInstanceID WHERE t" +
                ".messageID = :messageID").
                setParameter("messageID", messageID).
                setParameter("processInstanceID", processInstanceID).
                setParameter("taskID", taskID).executeUpdate();
    }

    /**
     * @see org.wso2.carbon.bpel.b4p.coordination.dao.HTCoordinationDAOConnection#getProtocolHandlers(String)
     */
    @Override
    public List<HTProtocolHandlerDAO> getProtocolHandlers(String processInstanceID) {
        Query query = entityManager.createQuery("SELECT t FROM org.wso2.carbon.bpel.b4p.coordination.dao.jpa.openjpa" +
                ".entity.HTProtocolHandler t WHERE t.processInstanceID = :processInstanceID");
        query.setParameter("processInstanceID", processInstanceID);
        return (List<HTProtocolHandlerDAO>) query.getResultList();
    }

    /**
     * @see org.wso2.carbon.bpel.b4p.coordination.dao.HTCoordinationDAOConnection#getProtocolHandlerURLsWithTasks
     * (String)
     */
    @Override
    public List<TaskProtocolHandler> getProtocolHandlerURLsWithTasks(String processInstanceID) {
        Query query = entityManager.createQuery("SELECT NEW org.wso2.carbon.bpel.b4p.coordination.dao" +
                ".TaskProtocolHandler(t.protocolHandlerURL, t.taskID) FROM org.wso2.carbon.bpel.b4p.coordination.dao" +
                ".jpa.openjpa.entity.HTProtocolHandler t WHERE t.processInstanceID = :processInstanceID ORDER BY t" +
                ".protocolHandlerURL ASC");
        query.setParameter("processInstanceID", processInstanceID);
        return (List<TaskProtocolHandler>) query.getResultList();
    }

    /**
     * @see org.wso2.carbon.bpel.b4p.coordination.dao.HTCoordinationDAOConnection#deleteCoordinationData(String)
     */
    @Override
    public boolean deleteCoordinationData(String processInstanceID) {
        Query q = entityManager.createQuery("DELETE FROM org.wso2.carbon.bpel.b4p.coordination.dao.jpa.openjpa.entity" +
                ".HTProtocolHandler t WHERE t.processInstanceID = :processInstanceID");
        q.setParameter("processInstanceID", processInstanceID);
        return q.executeUpdate() == 1;
    }

    /**
     * @see org.wso2.carbon.bpel.b4p.coordination.dao.HTCoordinationDAOConnection#deleteTaskData(String)
     */
    @Override
    public boolean deleteTaskData(String taskID) {
        Query q = entityManager.createQuery("DELETE FROM org.wso2.carbon.bpel.b4p.coordination.dao.jpa.openjpa.entity" +
                ".HTProtocolHandler t WHERE t.taskID = :taskID");
        q.setParameter("taskID", taskID);
        return q.executeUpdate() == 1;
    }

    @Override
    public EntityManager getEntityManager() {
        return entityManager;
    }
}
