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

package org.wso2.carbon.attachment.mgt.core.datasource.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.attachment.mgt.configuration.AttachmentServerConfiguration;
import org.wso2.carbon.attachment.mgt.core.datasource.AbstractDataSourceManager;
import org.wso2.carbon.attachment.mgt.core.exceptions.AttachmentMgtException;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.util.Properties;

/**
 * Concrete implementation fora a basic data-source {@link org.apache.commons.dbcp.BasicDataSource}}
 */
public class BasicDataSourceManager extends AbstractDataSourceManager {
    /**
     * Logger class
     */
    private static Log log = LogFactory.getLog(BasicDataSourceManager.class);

    private BasicDataSourceConfiguration dataSourceConfig;

    //private DataSource dataSource;

    /* A flag indicating whether the database is already initiated */
    private boolean started;

    /**
     * Here the init() method also called. So the client doesn't have to explicitly init() the
     * data-source.
     *
     * @param serverConfig
     */
    public BasicDataSourceManager(AttachmentServerConfiguration serverConfig) {
        init(serverConfig);
    }

    /**
     * Initialize the manager and the cooperated resources like DataSource
     */
    public void init(AttachmentServerConfiguration serverConfig) {
        loadDataSourceConfiguration(serverConfig.getDataSourceName(), serverConfig.getDataSourceJNDIRepoInitialContextFactory(),
                serverConfig.getDataSourceJNDIRepoProviderURL());
    }

    @Override
    public void start() {
        startDataSource();
    }

    @Override
    public void shutdown() throws AttachmentMgtException {
        dataSource = null;
        dataSourceConfig = null;
    }

    /**
     * Initialize the in memory DataSource Configuration.
     *
     * @param dataSourceName
     * @param dataSourceJNDIRepoInitialContextFactory
     *
     * @param dataSourceJNDIRepoProviderURL
     */
    private void loadDataSourceConfiguration(String dataSourceName,
                                             String dataSourceJNDIRepoInitialContextFactory,
                                             String dataSourceJNDIRepoProviderURL) {
        dataSourceConfig = new BasicDataSourceConfiguration(dataSourceName,
                dataSourceJNDIRepoInitialContextFactory,
                dataSourceJNDIRepoProviderURL);
    }

    /**
     * Initialize and Connects to the data source based on the BasicDataSourceConfiguration
     */
    private synchronized void startDataSource() {
        if (started) {
            return;
        }
        this.dataSource = null;
        initDataSource();
    }

    /**
     * Initialize the data Source
     */
    private void initDataSource() {
        initExternalDb();
    }

    private void initExternalDb() {
        try {
            this.dataSource = lookupInJNDI(dataSourceConfig.getDataSourceName());
        } catch (NamingException e) {
            log.error("Could not initialize the DataSource:" + dataSourceConfig.getDataSourceName(), e);
        }
    }

    private DataSource lookupInJNDI(String remoteObjectName) throws NamingException {
        InitialContext ctx = null;
        try {
            if (dataSourceConfig.getDataSourceJNDIRepoInitialContextFactory() != null &&
                    dataSourceConfig.getDataSourceJNDIRepoProviderURL() != null) {
                Properties jndiProps = new Properties();

                jndiProps.setProperty(Context.INITIAL_CONTEXT_FACTORY,
                        dataSourceConfig.getDataSourceJNDIRepoInitialContextFactory());
                jndiProps.setProperty(Context.PROVIDER_URL,
                        dataSourceConfig.getDataSourceJNDIRepoProviderURL());

                ctx = new InitialContext(jndiProps);
            } else {
                ctx = new InitialContext();
            }
            return (DataSource) ctx.lookup(remoteObjectName);
        } finally {
            if (ctx != null) {
                try {
                    ctx.close();
                } catch (Exception ex1) {
                    log.error("Error closing JNDI connection.", ex1);
                }
            }

        }
    }

}
