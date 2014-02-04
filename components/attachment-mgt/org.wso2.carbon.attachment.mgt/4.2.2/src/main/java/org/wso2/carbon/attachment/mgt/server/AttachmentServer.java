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
import org.wso2.carbon.attachment.mgt.configuration.*;
import org.wso2.carbon.attachment.mgt.configuration.AttachmentMgtConfigurationConstants;
import org.wso2.carbon.attachment.mgt.core.dao.DAOManagerImpl;
import org.wso2.carbon.attachment.mgt.core.datasource.impl.BasicDataSourceManager;
import org.wso2.carbon.attachment.mgt.core.exceptions.AttachmentMgtException;
import org.wso2.carbon.utils.CarbonUtils;

import java.io.File;

/**
 * This class act as the context for resources used by AttachmentServer.
 * The resources (like dataSource) are initialized here and populated to other child parties.
 */
public class AttachmentServer extends AbstractAttachmentServer {
    /**
     * Class Logger
     */
    private static final Log log = LogFactory.getLog(AttachmentServer.class);

    /**
     * Maintains the configurations related to Attachment-Mgt component
     */
    private AttachmentServerConfiguration serverConfig;

    private static AttachmentServer instance = new AttachmentServer();

    private AttachmentServer() {}

    public static AttachmentServer getInstance() {
        return instance;
    }

    /**
     * Main public method of this class. org.wso2.carbon.attachment.mgt.server.internal
     * .AttachmentServiceComponent invokes it when the bundle resolved.
     */
    public void init() {
        loadAttachmentServerConfig();
        initDataSourceManager();
        initDAOManager();
    }

    @Override
    public void shutdown() {
        shutdownDataSourceManager();
        shutdownDAOManager();
        unloadAttachmentServerConfig();
    }

    /**
     * Initialize the Data-Access(Persistent API) Specific configurations
     */
    public void initDAOManager() {
        this.daoManager = new DAOManagerImpl();
        this.daoManager.init(serverConfig);
    }

    /**
     * Starting and initialization of Data source
     */
    public void initDataSourceManager() {
        if (serverConfig == null) {
            log.error("Attachment Server configurations are not loaded properly.");
        }

        // Basic configuration Initialization of the Data-source
        dataSourceManager = new BasicDataSourceManager(serverConfig);

        try {
            dataSourceManager.start();
        } catch (AttachmentMgtException e) {
            log.error("Data-Source initialization failed. Reason:" + e.getLocalizedMessage(), e);
        }
    }



    /**
     * Initializes the configurations related to Attachment-Mgt component
     */
    private void loadAttachmentServerConfig() {
        if (log.isDebugEnabled()) {
            log.debug("Loading Attachment Mgt Server Configuration...");
        }

        if (isAttachmentMgtConfigurationFileAvailable()) {
            File attMgtConfigFile = new File(calculateAttachmentMgtServerConfigurationFilePath());
            serverConfig = new AttachmentServerConfiguration(attMgtConfigFile);
        } else {
            log.info("Humantask configuration file: " + AttachmentMgtConfigurationConstants
                    .ATTACHMENT_MANAGEMENT_CONFIG_FILE + " not found. Loading default configurations.");
            serverConfig = new AttachmentServerConfiguration();
        }
    }

    /**
     * De-referencing the server configuration
     */
    private void unloadAttachmentServerConfig() {
        serverConfig = null;
        if (log.isDebugEnabled()) {
            log.debug("Unloaded Attachment Mgt Server Configuration.");
        }
    }

    /**
     * Determine whether the server configuration file is available or not
     *
     * @return true if the server configuration file exists
     */
    private boolean isAttachmentMgtConfigurationFileAvailable() {
        File attMgtConfigFile = new File(calculateAttachmentMgtServerConfigurationFilePath());
        return attMgtConfigFile.exists();
    }

    private String calculateAttachmentMgtServerConfigurationFilePath() {
        return CarbonUtils.getCarbonConfigDirPath() + File.separator + AttachmentMgtConfigurationConstants
                .ATTACHMENT_MANAGEMENT_CONFIG_FILE;
    }
}