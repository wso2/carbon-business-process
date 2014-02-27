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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.attachment.mgt.core.dao.DAOManager;
import org.wso2.carbon.attachment.mgt.core.datasource.AbstractDataSourceManager;
import org.wso2.carbon.attachment.mgt.core.exceptions.AttachmentMgtException;

public abstract class AbstractAttachmentServer implements Server {
    /**
     * Class Logger
     */
    private static Log log = LogFactory.getLog(AbstractAttachmentServer.class);

    /**
     * Maintains the references related to Data-Access (Persistent API) related configuration
     */
    protected DAOManager daoManager;

    /**
     * Maintains the references related to data source and it's configuration
     */
    protected AbstractDataSourceManager dataSourceManager;

    /**
     * Returns the DAOManager used by the Attachment-Mgt component
     *
     * @return the dao-manager reference used by the Attachment-Mgt component
     */
    public DAOManager getDaoManager() {
        if (daoManager != null) {
            return daoManager;
        } else {
            log.error("daoManager is not initialized yet.");
            throw new NullPointerException("daoManager is not initialized yet.");
        }
    }

    /**
     * Returns the dataSource used by the Attachment-Mgt component
     *
     * @return the dataSource used by the Attachment-Mgt component
     */
    public AbstractDataSourceManager getDataSourceManager() {
        if (dataSourceManager != null) {
            return dataSourceManager;
        } else {
            log.error("dataSourceManager is not initialized yet.");
            throw new NullPointerException("dataSourceManager is not initialized yet.");
        }
    }

    /**
     * Shut down the data-source
     */
    public void shutdownDataSourceManager() {
        try {
            dataSourceManager.shutdown();
            dataSourceManager = null;

            if (log.isDebugEnabled()) {
                log.debug("Data-Source Manager was shut-down.");
            }

        } catch (AttachmentMgtException e) {
            log.error("Data-Source shutdown failed. Reason:" + e.getLocalizedMessage(), e);
        }
    }

    /**
     * Shut down the DAO manager
     */
    public void shutdownDAOManager() {
        this.daoManager.shutdown();

        this.daoManager = null;

        if (log.isDebugEnabled()) {
            log.debug("DAO Manager was shut-down.");
        }
    }
}