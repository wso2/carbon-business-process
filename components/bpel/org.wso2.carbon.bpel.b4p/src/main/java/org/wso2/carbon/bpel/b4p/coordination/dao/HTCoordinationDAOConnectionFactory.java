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

package org.wso2.carbon.bpel.b4p.coordination.dao;

import javax.sql.DataSource;

/**
 * HT Coordination DAO Connection Factory Interface. This would be used to obtain HTCoordinationDAOConnection to
 * query the Database.
 */
public interface HTCoordinationDAOConnectionFactory {

    /**
     * @return HTCoordinationDAOConnection : The dao connection which acts as the interface to the database.
     */
    HTCoordinationDAOConnection getConnection();

    /**
     * Initialization logic.
     */
    void init();

    /**
     * @return : The DataSource
     */
    DataSource getDataSource();

    /**
     * Shutdown logic.
     */
    void shutdown();
}
