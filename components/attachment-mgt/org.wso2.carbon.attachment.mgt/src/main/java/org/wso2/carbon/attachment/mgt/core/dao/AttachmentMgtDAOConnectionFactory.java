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


import javax.sql.DataSource;
import javax.transaction.TransactionManager;
import java.util.Map;

/**
 * Attachment-Mgt DAO Connection Factory Interface. This would be used to obtain
 * AttachmentMgtDAOFactory to query the DataSource where attachments are maintained.
 */
public interface AttachmentMgtDAOConnectionFactory {
    /**
     * @return AttachmentMgtDAOConnection : The dao connection which acts as the interface to
     *         the data source where the attachment are maintained..
     */
    public AttachmentMgtDAOConnection getDAOConnection();

    /**
     * Initialization logic.
     */
    public void init();

    /**
     * Shutdown logic.
     */
    public void shutdown();

    /**
     * Passes a set of properties which are required in initializing
     *
     * @param propertiesMap
     */
    public void setDAOConnectionFactoryProperties(Map<String, Object> propertiesMap); //TODO:
    // THis is more JPA Specific. So better to move to JPABasedAttachmentMgtDAOConnectionFactory.
    // But there's a problem on using reflection and cast it to the concrete impl then.

    /**
     * Set the dataSource related to DAO Connection
     */
    public void setDataSource(DataSource dataSource); //TODO: Can there be a DAO without
    // dataSource ???

/**
     * The transaction manager.
     *
     * @param tnxManager : The transaction manager to set.
     */
    public void setTransactionManager(TransactionManager tnxManager); //TODO: Can there be a DAO
    // without transaction manager

}
