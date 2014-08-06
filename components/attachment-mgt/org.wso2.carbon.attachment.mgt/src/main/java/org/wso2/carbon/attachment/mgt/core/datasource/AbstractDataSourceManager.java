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

import javax.sql.DataSource;
import javax.transaction.TransactionManager;

/**
 * Manage entities like data-source, transaction-manager referenced by data-source manager
 */
public abstract class AbstractDataSourceManager implements DataSourceManager {
    /**
     * Data-Source reference
     */
    protected DataSource dataSource;

    /** {@inheritDoc} */
    @Override
    public DataSource getDataSource() {
        if (this.dataSource != null) {
            return this.dataSource;
        } else {
            throw new NullPointerException("DataSource is not initialized.");
        }
    }
}
