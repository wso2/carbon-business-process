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

package org.wso2.carbon.attachment.mgt.core.datasource;

import org.wso2.carbon.attachment.mgt.configuration.AttachmentServerConfiguration;
import org.wso2.carbon.attachment.mgt.core.exceptions.AttachmentMgtException;

import javax.sql.DataSource;
import javax.transaction.TransactionManager;

/**
 * Interface for Data-Source Manager. So this will manager init, start and shutdown of the data-source
 */
public interface DataSourceManager {
    /**
     * Initialization of the data-source
     * @throws AttachmentMgtException if initialization fails
     */
    public void init(AttachmentServerConfiguration serverConfig) throws AttachmentMgtException;

    /**
     * Starting of the data-source
     * @throws AttachmentMgtException if start fails
     */
    public void start() throws AttachmentMgtException;

    /**
     * Shutting-down of the data-source
     * @throws AttachmentMgtException if shutdown fails
     */
    public void shutdown() throws AttachmentMgtException;

    /**
     * Returns the data-source reference which is managed by the DataSourceManager
     * @return
     */
    public DataSource getDataSource();
}
