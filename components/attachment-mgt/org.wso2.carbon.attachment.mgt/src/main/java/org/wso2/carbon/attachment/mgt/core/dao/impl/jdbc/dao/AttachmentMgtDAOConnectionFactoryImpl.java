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

package org.wso2.carbon.attachment.mgt.core.dao.impl.jdbc.dao;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.attachment.mgt.core.dao.AttachmentMgtDAOConnection;
import org.wso2.carbon.attachment.mgt.core.dao.AttachmentMgtDAOConnectionFactory;

import javax.sql.DataSource;
import javax.transaction.TransactionManager;
import java.util.Map;

/**
 * JDBC specific implementation of {@link org.wso2.carbon.attachment.mgt.core.dao.AttachmentMgtDAOConnectionFactory}
 */
public class AttachmentMgtDAOConnectionFactoryImpl implements AttachmentMgtDAOConnectionFactory {
    /**
     * Class Logger
     */
    private static Log log = LogFactory.getLog(AttachmentMgtDAOConnectionFactoryImpl.class);

    protected AttachmentMgtDAOConnection daoConnection;

    private DataSource dataSource;

    @Override
    public AttachmentMgtDAOConnection getDAOConnection() {
        if (daoConnection != null) {
            return daoConnection;
        } else {
            RuntimeException e = new NullPointerException("daoConnection is not initialized yet.");
            log.error("daoConnection is not initialized yet.", e);

            throw e;

        }
    }

    @Override
    public void init() {
        log.warn("Still not impled.");
        daoConnection = new AttachmentMgtDAOConnectionImpl();
    }

    @Override
    public void shutdown() {
        log.warn("Still not impled.");
        daoConnection = null;
    }

    @Override
    public void setDAOConnectionFactoryProperties(Map<String, Object> propertiesMap) {
        log.warn("Still not maintained a property map.");
    }

    @Override
    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void setTransactionManager(TransactionManager tnxManager) {
        log.warn("In the default jdbc implementation we are not using a transaction manager. " +
                 "Please check whether a transaction manager is compulsory or not.");
    }

}
