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

package org.wso2.carbon.humantask.core.dao;


import javax.sql.DataSource;
import javax.transaction.TransactionManager;
import java.util.Map;

/**
 * JDBC specific DAO factory properties.
 */
public interface HumanTaskDAOConnectionFactoryJDBC extends HumanTaskDAOConnectionFactory {

    /**
     *  Set the datasource
     * @param dataSource : The datasource.
     */
    void setDataSource(DataSource dataSource);

    /**
     * The transaction manager.
     * @param tnxManager : The transaction manager to set.
     */
    void setTransactionManager(TransactionManager tnxManager);

    /**
     * TODO
     * @param propertiesMap
     */
    void setDAOConnectionFactoryProperties(Map<String, Object> propertiesMap);
}
