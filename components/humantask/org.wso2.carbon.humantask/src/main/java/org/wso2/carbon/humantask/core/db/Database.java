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

package org.wso2.carbon.humantask.core.db;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.humantask.core.configuration.HumanTaskServerConfiguration;
import org.wso2.carbon.humantask.core.dao.Constants;
import org.wso2.carbon.humantask.core.dao.HumanTaskDAOConnectionFactoryJDBC;
import org.wso2.carbon.humantask.core.engine.HumanTaskServerException;
import org.wso2.carbon.humantask.core.utils.HumanTaskDatabaseCreator;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import javax.transaction.TransactionManager;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Internal representation of the human task database.
 */
public class Database {

    private static final Log log = LogFactory.getLog(Database.class);

    private HumanTaskServerConfiguration serverConfiguration;

    /* A flag indicating whether the database is already initiated */
    private boolean started;

    private TransactionManager tnxManager;

    private DataSource dataSource;

    /**
     * @return The Human Task DataSource..
     */
    public synchronized DataSource getDataSource() {
        if (this.dataSource == null) {
            throw new RuntimeException("Human Task Database is not properly initialised!");
        }
        return this.dataSource;
    }

    public Database(HumanTaskServerConfiguration htConfig) {
        if (htConfig == null) {
            throw new IllegalArgumentException("Must provide the human task server configuration!");
        }
        this.serverConfiguration = htConfig;
    }

    /**
     * Database initialization logic.
     */
    public synchronized void start() throws DatabaseConfigurationException, HumanTaskServerException {

        if (started) {
            return;
        }
        this.dataSource = null;
        initDataSource();
        setupHumanTaskDatabase();
    }

    private void setupHumanTaskDatabase() throws HumanTaskServerException {
                if (System.getProperty("setup") != null) {
            HumanTaskDatabaseCreator humantaskDBCreator;
            try {
                humantaskDBCreator = new HumanTaskDatabaseCreator(getDataSource());
            } catch (Exception e) {
                String errMsg = "Error creating HumanTaskDatabaseCreator";
                log.error(errMsg, e);
                throw new HumanTaskServerException(errMsg, e);
            }
            if (!humantaskDBCreator.isDatabaseStructureCreated("SELECT * FROM HT_JOB")) {
                try {
                    //TODO rename following method
                    humantaskDBCreator.createRegistryDatabase();
                } catch (Exception e) {
                    String errMsg = "Error creating HumanTask database";
                    log.error(errMsg, e);
                    throw new HumanTaskServerException(errMsg, e);
                }
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("HumanTask database already exists. Using the old database.");
                }
            }
        }
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

    private void initDataSource() throws DatabaseConfigurationException {
        //Note : We can improve this to consider internal/external databases.
        initExternalDb();
    }

    private void initExternalDb() throws DatabaseConfigurationException {
        try {
            this.dataSource = (DataSource) lookupInJndi(serverConfiguration.getDataSourceName());

            if(log.isDebugEnabled()) {
                log.debug("HumanTask Server using external DataSource " +
                          serverConfiguration.getDataSourceName());
            }

        } catch (Exception e) {
            String errorMsg = "Failed to resolved external DataSource at " +
                              serverConfiguration.getDataSourceName();
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
                if (serverConfiguration.getDataSourceJNDIRepoInitialContextFactory() != null &&
                        serverConfiguration.getDataSourceJNDIRepoProviderURL() != null) {
                    Properties jndiProps = new Properties();

                    jndiProps.setProperty(Context.INITIAL_CONTEXT_FACTORY,
                            serverConfiguration.getDataSourceJNDIRepoInitialContextFactory());
                    jndiProps.setProperty(Context.PROVIDER_URL,
                            serverConfiguration.getDataSourceJNDIRepoProviderURL());

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
     *
     * @param tnxManager : The transaction manager to set.
     */
    public void setTransactionManager(TransactionManager tnxManager) {
        this.tnxManager = tnxManager;
    }

    /**
     * Creates the DAO connection factory.
     * @return : the connection factory.
     * @throws DatabaseConfigurationException : If the provided config factory cannot be instantiated.
     */
    public HumanTaskDAOConnectionFactoryJDBC createDAOConnectionFactory()
            throws DatabaseConfigurationException {
        String connectionFactoryClassName = serverConfiguration.getDaoConnectionFactoryClass();

        if(log.isDebugEnabled()) {
            log.debug("Using DAO connection factory class: " + connectionFactoryClassName);
        }

        HumanTaskDAOConnectionFactoryJDBC humanTaskDAOConnectionFactoryJDBC;

        try{
            humanTaskDAOConnectionFactoryJDBC = (HumanTaskDAOConnectionFactoryJDBC)
                    Class.forName(connectionFactoryClassName).newInstance();
        }  catch (Exception ex) {
            String errMsg = "Human Task DAO Connection Factory instantiation failed!";
            log.error(errMsg);
            throw new DatabaseConfigurationException(errMsg, ex);
        }

        humanTaskDAOConnectionFactoryJDBC.setDataSource(getDataSource());
        humanTaskDAOConnectionFactoryJDBC.setTransactionManager(getTnxManager());
        humanTaskDAOConnectionFactoryJDBC.setDAOConnectionFactoryProperties(
                getGenericDAOFactoryProperties());
        humanTaskDAOConnectionFactoryJDBC.init();


        return humanTaskDAOConnectionFactoryJDBC;
    }

    /**
     * Gets the generic properties for DAO Connection Factory.
     * @return
     */
    private Map<String, Object> getGenericDAOFactoryProperties(){
        Map<String, Object> daoFactoryProperties = new HashMap<String, Object>();
        daoFactoryProperties.put(Constants.DATA_SOURCE_PROP, getDataSource());
        daoFactoryProperties.put(Constants.PROP_ENABLE_DDL_GENERATION,
                serverConfiguration.isGenerateDdl());
        daoFactoryProperties.put(Constants.PROP_ENABLE_SQL_TRACING, serverConfiguration.isShowSql());
        daoFactoryProperties.put(Constants.DAO_FACTORY_CLASS_PROP,
                serverConfiguration.getDaoConnectionFactoryClass());

        return daoFactoryProperties;
    }

    public TransactionManager getTnxManager() {
        return tnxManager;
    }
}
