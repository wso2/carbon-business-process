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

package org.wso2.carbon.attachment.mgt.server;

import org.wso2.carbon.attachment.mgt.core.dao.DAOManager;
import org.wso2.carbon.attachment.mgt.core.datasource.AbstractDataSourceManager;
import org.wso2.carbon.attachment.mgt.core.datasource.impl.BasicDataSourceManager;
import org.wso2.carbon.attachment.mgt.core.scheduler.Scheduler;

import javax.transaction.TransactionManager;

public interface Server {
    public void init();
    public void shutdown();
    public DAOManager getDaoManager();
    void initDataSourceManager();
    void shutdownDataSourceManager();
    public AbstractDataSourceManager getDataSourceManager();
    void initDAOManager();
    void shutdownDAOManager();
}