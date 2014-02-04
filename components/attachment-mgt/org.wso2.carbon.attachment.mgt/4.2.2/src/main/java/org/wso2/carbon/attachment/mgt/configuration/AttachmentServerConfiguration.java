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

package org.wso2.carbon.attachment.mgt.configuration;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xmlbeans.XmlException;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.attachment.mgt.server.config.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * In-memory configuration manager for AttachmentServer
 */
public class AttachmentServerConfiguration {
    /**
     * Logger class
     */
    private static Log log = LogFactory.getLog(AttachmentServerConfiguration.class);

    private AttachmentManagementConfigDocument attachmentManagementConfigDocument;

    private String dataSourceName;

    private String dataSourceJNDIRepoInitialContextFactory;

    private String dataSourceJNDIRepoProviderURL;

    private String daoConnectionFactoryClass;

    private String daoTransformerFactoryClass = "org.wso2.carbon.attachment.mgt.core.dao.impl.jpa.openjpa.AttachmentMgtDAOTransformerFactoryImpl";

    private String transactionFactoryClass = "org.apache.ode.il.EmbeddedGeronimoFactory";

    /**
     * Referred when constructing the JPA specific DAO-Manager
     */
    private boolean generateDdl =false;

    /**
     * Referred when constructing the JPA specific DAO-Manager
     */
    private boolean showSql = false;

    public AttachmentServerConfiguration() {
        this.dataSourceName = "bpsds";
        this.dataSourceJNDIRepoInitialContextFactory = null;
        this.dataSourceJNDIRepoProviderURL = null;
        this.daoConnectionFactoryClass = "org.wso2.carbon.attachment.mgt.core.dao.impl.jpa" +
                ".openjpa.AttachmentMgtDAOConnectionFactoryImpl";
        this.daoTransformerFactoryClass = "org.wso2.carbon.attachment.mgt.core.dao.impl.jpa.openjpa.AttachmentMgtDAOTransformerFactoryImpl";
        this.transactionFactoryClass = null;
        this.generateDdl = true;
        this.showSql = false;
    }

    public AttachmentServerConfiguration(File htServerConfig) {
        attachmentManagementConfigDocument = readConfigurationFromFile(htServerConfig);

        if (attachmentManagementConfigDocument == null) {
            return;
        }
        initConfigurationFromFile();
    }


    private void initConfigurationFromFile() {
        TAttachmentManagementConfig tAttachmentManagementConfig = attachmentManagementConfigDocument.getAttachmentManagementConfig();
        if (tAttachmentManagementConfig == null) {
            return;
        }

        if(tAttachmentManagementConfig.getPersistenceConfig()!=null)
        {
            initPersistenceConfig(tAttachmentManagementConfig.getPersistenceConfig());
        }

        if(tAttachmentManagementConfig.getTransactionManagerConfig()!=null)
        {
            initTransactionManagerConfig(tAttachmentManagementConfig.getTransactionManagerConfig());
        }

        if(tAttachmentManagementConfig.getTransformerFactoryConfig()!=null)
        {
            initTransformerFactoryConfig(tAttachmentManagementConfig.getTransformerFactoryConfig());
        }
    }

    private void initPersistenceConfig(TPersistenceConfig tPersistenceConfig) {
        if (tPersistenceConfig.getDataSource() != null) {
            this.dataSourceName = tPersistenceConfig.getDataSource().trim();
        }
        if (tPersistenceConfig.getJNDIInitialContextFactory() != null) {
            this.dataSourceJNDIRepoInitialContextFactory =
                    tPersistenceConfig.getJNDIInitialContextFactory().trim();
        }
        if (tPersistenceConfig.getJNDIProviderUrl() != null) {
            this.dataSourceJNDIRepoProviderURL = tPersistenceConfig.getJNDIProviderUrl().trim();
            int portOffset = getCarbonPortOffset();

            // We need to adjust the port value according to the offset defined in the carbon configuration.
            String portValueString = dataSourceJNDIRepoProviderURL.substring(
                    dataSourceJNDIRepoProviderURL.lastIndexOf(':') + 1,
                    dataSourceJNDIRepoProviderURL.length());

            String urlWithoutPort = dataSourceJNDIRepoProviderURL
                    .substring(0, dataSourceJNDIRepoProviderURL.lastIndexOf(':') + 1);


            int actualPortValue = Integer.parseInt(portValueString);
            int correctedPortValue = actualPortValue + portOffset;

            this.dataSourceJNDIRepoProviderURL = urlWithoutPort.concat(Integer.toString(correctedPortValue));

        }

        if (tPersistenceConfig.getDAOConnectionFactoryClass() != null) {
            this.daoConnectionFactoryClass = tPersistenceConfig.getDAOConnectionFactoryClass().trim();
        }

        this.generateDdl = tPersistenceConfig.getGenerateDdl();
        this.showSql = tPersistenceConfig.getShowSql();
    }

    private void initTransactionManagerConfig(TTransactionManagerConfig tTransactionManagerConfig) {
        if (tTransactionManagerConfig.getTransactionManagerClass() != null) {
            this.transactionFactoryClass = tTransactionManagerConfig.getTransactionManagerClass().
                    trim();
        } else {
            log.debug("TransactionManagerClass not provided with HumanTask configuration." +
                    "Using default TransactionManagerClass :" + transactionFactoryClass);
        }
    }

    private void initTransformerFactoryConfig(TTransformerFactoryConfig tTransformerFactoryConfig) {
        if (tTransformerFactoryConfig.getTransformerFactoryClass() != null) {
            this.daoTransformerFactoryClass = tTransformerFactoryConfig.getTransformerFactoryClass().trim();
        } else {
            log.debug("TransformerFactoryClass not provided with HumanTask configuration." +
                    "Using default TransformerFactoryClass :" + daoTransformerFactoryClass);
        }
    }
    private AttachmentManagementConfigDocument readConfigurationFromFile(File htServerConfiguration) {
        try {
            return AttachmentManagementConfigDocument.Factory.parse(new FileInputStream(htServerConfiguration));
        } catch (XmlException e) {
            log.error("Error parsing human task server configuration.", e);
        } catch (FileNotFoundException e) {
            log.info("Cannot find the human task server configuration in specified location "
                    + htServerConfiguration.getPath() + " . Loads the default configuration.");
        } catch (IOException e) {
            log.error("Error reading human task server configuration file" + htServerConfiguration.getPath() + " .");
        }

        return null;
    }

    public String getDataSourceName() {
        return dataSourceName;
    }

    public String getDataSourceJNDIRepoInitialContextFactory() {
        return dataSourceJNDIRepoInitialContextFactory;
    }

    public String getDataSourceJNDIRepoProviderURL() {
        return dataSourceJNDIRepoProviderURL;
    }

    public String getDaoConnectionFactoryClass() {
        return daoConnectionFactoryClass;
    }

    public String getDaoTransformerFactoryClass() {
        return daoTransformerFactoryClass;
    }

    public String getTransactionFactoryClass() {
        return transactionFactoryClass;
    }

    public boolean isGenerateDdl() {
        return generateDdl;
    }

    public boolean isShowSql() {
        return showSql;
    }

    private int getCarbonPortOffset() {

        String offset = CarbonUtils.getServerConfiguration().getFirstProperty(
                AttachmentMgtConfigurationConstants.CARBON_CONFIG_PORT_OFFSET_NODE);

        try {
            return ((offset != null) ? Integer.parseInt(offset.trim()) :
                    0);
        } catch (NumberFormatException e) {
            log.warn("Error occurred while reading port offset. Invalid port offset: " +
                    offset + " Setting the port offset to 0",
                    e);
            return 0;
        }
    }
}
