/*
 * Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.bpel.b4p.coordination.db;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bpel.b4p.coordination.configuration.CoordinationConfiguration;
import org.wso2.carbon.bpel.b4p.coordination.dao.HTCoordinationDAOConnectionFactoryJDBC;
import org.wso2.carbon.bpel.b4p.coordination.dao.Constants;
import org.wso2.carbon.bpel.b4p.internal.B4PContentHolder;
import org.wso2.carbon.bpel.core.ode.integration.config.BPELServerConfiguration;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import javax.transaction.TransactionManager;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Internal representation of the b4p coordination database.
 */
public class DatabaseUtil {

    private static final Log log = LogFactory.getLog(DatabaseUtil.class);

    private TransactionManager tnxManager;

    /* A flag indicating whether the database is already initiated */
    private boolean started;

    private DataSource dataSource;

    private BPELServerConfiguration bpelServerConfiguration;

    public DatabaseUtil() {
        bpelServerConfiguration = B4PContentHolder.getInstance().getBpelServer().getBpelServerConfiguration();
    }

    /**
     * @return The BPEL DataSource..
     */
    public synchronized DataSource getDataSource() {
        if (this.dataSource == null) {
            throw new RuntimeException("BPEL Database is not properly initialised!");
        }
        return this.dataSource;
    }

    public synchronized void start() throws DatabaseConfigurationException {

        if (started) {
            return;
        }
        this.dataSource = null;
        initDataSource();
        setUpB4PDataBase();
        started = true;
    }

    /**
     * Shut down logic for the Database related resources.
     */
    public synchronized void shutdown() {
        if (!started) {
            return;
        }
        this.dataSource = null;
        this.started = false;
    }

    private void setUpB4PDataBase() throws DatabaseConfigurationException {
        if (System.getProperty("setup") != null) {
            B4PDatabaseCreator b4pDBCreator;
            try {
                b4pDBCreator = new B4PDatabaseCreator(getDataSource());
            } catch (Exception ex) {
                String errorMsg = "Error while creating B4PDatabaseCreator";
                log.error(errorMsg, ex);
                throw new DatabaseConfigurationException(errorMsg, ex);
            }

            //Checking whether table are already created.
            if (!b4pDBCreator.isDatabaseStructureCreated("SELECT * FROM HT_COORDINATION_DATA")) {
                try {
                    b4pDBCreator.createRegistryDatabase();
                } catch (Exception e) {
                    String errMsg = "Error while B4P component database";
                    log.error(errMsg, e);
                    throw new DatabaseConfigurationException(errMsg, e);
                }
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("B4P Component database already exists. Using the old database.");
                }
            }
        }
    }

    private void initDataSource() throws DatabaseConfigurationException {
        try {
            this.dataSource = (DataSource) lookupInJndi(bpelServerConfiguration.getDataSourceName());

            if (log.isDebugEnabled()) {
                log.debug("B4P Component using external DataSource " + bpelServerConfiguration.getDataSourceName());
            }

        } catch (Exception e) {
            String errorMsg = "Failed to resolved external DataSource at " + bpelServerConfiguration.getDataSourceName();
            log.error(errorMsg, e);
            throw new DatabaseConfigurationException(errorMsg, e);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T lookupInJndi(String objName) throws Exception {
        ClassLoader old = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
        try {
            InitialContext ctx = null;
            try {
                if (bpelServerConfiguration.getDataSourceJNDIRepoInitialContextFactory() != null &&
                        bpelServerConfiguration.getDataSourceJNDIRepoProviderURL() != null) {
                    Properties jndiProps = new Properties();

                    jndiProps.setProperty(Context.INITIAL_CONTEXT_FACTORY,
                            bpelServerConfiguration.getDataSourceJNDIRepoInitialContextFactory());
                    jndiProps.setProperty(Context.PROVIDER_URL,
                            bpelServerConfiguration.getDataSourceJNDIRepoProviderURL());
                    ctx = new InitialContext(jndiProps);
                } else {
                    ctx = new InitialContext();
                }
                return (T) ctx.lookup(objName);
            } finally {
                if (ctx != null) {
                    try {
                        ctx.close();
                    } catch (Exception ex1) {
                        log.error("Error closing JNDI connection.", ex1);
                    }
                }
            }
        } finally {
            Thread.currentThread().setContextClassLoader(old);
        }
    }

    /**
     * @param tnxManager : The transaction manager to set.
     */
    public void setTransactionManager(TransactionManager tnxManager) {
        this.tnxManager = tnxManager;
    }

    public TransactionManager getTnxManager() {
        return tnxManager;
    }

    public HTCoordinationDAOConnectionFactoryJDBC createDAOConnectionFactory() throws DatabaseConfigurationException {
        String daoConnectionFactoryClassName = CoordinationConfiguration.getInstance().getDaoConnectionFactoryClass();

        if (log.isDebugEnabled()) {
            log.debug("Using DAO connection factory class for B4P module:" + daoConnectionFactoryClassName);
        }

        HTCoordinationDAOConnectionFactoryJDBC htCoordinationDAOConnectionFactoryJDBC;
        try {
            htCoordinationDAOConnectionFactoryJDBC = (HTCoordinationDAOConnectionFactoryJDBC)
                    Class.forName(daoConnectionFactoryClassName).newInstance();
        } catch (Exception ex) {
            String errMsg = "B4P module DAO Connection Factory instantiation failed!";
            log.error(errMsg);
            throw new DatabaseConfigurationException(errMsg, ex);
        }

        htCoordinationDAOConnectionFactoryJDBC.setDataSource(getDataSource());
        htCoordinationDAOConnectionFactoryJDBC.setTransactionManager(getTnxManager());
        htCoordinationDAOConnectionFactoryJDBC.setDAOConnectionFactoryProperties(getGenericDAOFactoryProperties());
        htCoordinationDAOConnectionFactoryJDBC.init();

        return htCoordinationDAOConnectionFactoryJDBC;
    }

    private Map<String, Object> getGenericDAOFactoryProperties() {
        Map<String, Object> daoFactoryProperties = new HashMap<String, Object>();
        daoFactoryProperties.put(Constants.PROP_ENABLE_DDL_GENERATION,
                CoordinationConfiguration.getInstance().isGenerateDdl());
        daoFactoryProperties.put(Constants.PROP_ENABLE_SQL_TRACING, CoordinationConfiguration.getInstance().isShowSQL());
        return daoFactoryProperties;
    }
}

