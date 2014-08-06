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

package org.wso2.carbon.attachment.mgt.test;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.attachment.mgt.configuration.AttachmentMgtConfigurationConstants;
import org.wso2.carbon.attachment.mgt.configuration.AttachmentServerConfiguration;
import org.wso2.carbon.attachment.mgt.core.dao.DAOManagerImpl;
import org.wso2.carbon.attachment.mgt.core.datasource.impl.JDBCManager;
import org.wso2.carbon.attachment.mgt.core.exceptions.AttachmentMgtException;
import org.wso2.carbon.attachment.mgt.server.AbstractAttachmentServer;
import org.wso2.carbon.utils.ServerConstants;

import java.io.File;

/**
 * Mock Server implementation to be used for test-cases
 */
public class MockAttachmentServer extends AbstractAttachmentServer {
    /**
     * Class Logger
     */
    private static Log log = LogFactory.getLog(MockAttachmentServer.class);

    /**
     * Maintains the configurations related to Attachment-Mgt component
     */
    private AttachmentServerConfiguration serverConfig;

    private static final String CONFIG_FILE_PATH = "src" + File.separator + "test" + File
            .separator + "resources" + File.separator + AttachmentMgtConfigurationConstants.ATTACHMENT_MANAGEMENT_CONFIG_FILE;

    private static final String DATABASE_CONFIG_FILE_PATH = "src" + File.separator + "test" + File
            .separator + "resources" + File.separator + "dbConfig.xml";



    private static final String CARBON_CONFIG_DIR_PATH = "src" + File.separator + "test" + File
            .separator + "resources";

    @Override
    public void init() {
        System.setProperty(ServerConstants.CARBON_CONFIG_DIR_PATH, CARBON_CONFIG_DIR_PATH);

        loadAttachmentServerConfig();
        initDataSourceManager();
        initDAOManager();
    }

    @Override
    public void shutdown() {
        //shutdownDAOManager();
        //TODO log.warn("Here, ideally the DAO manager should be shut down (shutdownDAOManager). But it fails test cases due" +
        //         " to some reason which need to be fixed properly.");
        //shutdownDataSourceManager();
        //TODO  log.warn("Here, ideally the data-source manager should be shut down (shutdownDataSourceManager). But it fails" +
        //         " test cases due to some reason which need to be fixed properly.");
        unloadAttachmentServerConfig();
    }

    private void loadAttachmentServerConfig() {
        File attMgtConfigFile = new File(CONFIG_FILE_PATH);
        serverConfig = new AttachmentServerConfiguration(attMgtConfigFile);
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

    public void initDAOManager() {
        this.daoManager = new DAOManagerImpl();
        daoManager.init(serverConfig);
    }

    public void initDataSourceManager() {
        dataSourceManager = new JDBCManager();
        try {
            ((JDBCManager) dataSourceManager).initFromFileConfig(DATABASE_CONFIG_FILE_PATH);
            //This class cast can be probably avoided if the init() method is correctly
            // implemented.
        } catch (AttachmentMgtException e) {
            log.error(e.getLocalizedMessage(), e);
        }
    }
}