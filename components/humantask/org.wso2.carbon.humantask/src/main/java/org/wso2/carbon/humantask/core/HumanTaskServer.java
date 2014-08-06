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

package org.wso2.carbon.humantask.core;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.humantask.core.api.event.HumanTaskEventListener;
import org.wso2.carbon.humantask.core.api.scheduler.Scheduler;
import org.wso2.carbon.humantask.core.configuration.HumanTaskServerConfiguration;
import org.wso2.carbon.humantask.core.dao.HumanTaskDAOConnectionFactory;
import org.wso2.carbon.humantask.core.db.Database;
import org.wso2.carbon.humantask.core.engine.HumanTaskEngine;
import org.wso2.carbon.humantask.core.engine.HumanTaskServerException;
import org.wso2.carbon.humantask.core.engine.PeopleQueryEvaluator;
import org.wso2.carbon.humantask.core.engine.event.processor.EventProcessor;
import org.wso2.carbon.humantask.core.scheduler.JobProcessorImpl;
import org.wso2.carbon.humantask.core.scheduler.SimpleScheduler;
import org.wso2.carbon.humantask.core.store.HumanTaskStoreManager;
import org.wso2.carbon.humantask.core.utils.GUID;
import org.wso2.carbon.utils.CarbonUtils;

import javax.transaction.TransactionManager;
import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * Human Task Server which is responsible for initializing data sources, task store, schedulers and
 * other resources used by Task Engine.
 */
public class HumanTaskServer {

    private static final Log log = LogFactory.getLog(HumanTaskServer.class);

    /**
     * The human task server configurations
     */
    private HumanTaskServerConfiguration serverConfig;

    /**
     * The task engine
     */
    private HumanTaskEngine taskEngine;

    /**
     * The human task database representation
     */
    private Database database;

    /**
     * The task store manager
     */
    private HumanTaskStoreManager taskStoreManager;

    /**
     * The transaction manager
     */
    private TransactionManager tnxManager;

    /**
     * The dao connection factory
     */
    private HumanTaskDAOConnectionFactory daoConnectionFactory;

    /**
     * Human task scheduler
     */
    private Scheduler scheduler;

    private EventProcessor eventProcessor;

    /**
     * The initialisation logic for the human task server.
     *
     * @throws HumanTaskServerException : If the server initialisation fails.
     */
    public void init() throws HumanTaskServerException {
        loadHumanTaskServerConfiguration();
        initTransactionManager();
        initDataSource();
        initDAO();
        initEventProcessor();
        initHumanTaskEngine();
        initPeopleQueryEvaluator();
        initHumanTaskStore();
        initScheduler();
    }

    /**
     * Scheduler initialisation.
     */
    private void initScheduler() {
        ThreadFactory threadFactory = new ThreadFactory() {
            private int threadNumber = 0;

            public Thread newThread(Runnable r) {
                threadNumber += 1;
                Thread t = new Thread(r, "HumanTaskServer-" + threadNumber);
                t.setDaemon(true);
                return t;
            }
        };

        ExecutorService executorService = Executors.
                newFixedThreadPool(serverConfig.getThreadPoolMaxSize(), threadFactory);

        SimpleScheduler simpleScheduler = new SimpleScheduler(new GUID().toString());
        simpleScheduler.setExecutorService(executorService);
        simpleScheduler.setTransactionManager(tnxManager);
        taskEngine.setScheduler(simpleScheduler);
        simpleScheduler.setJobProcessor(new JobProcessorImpl());
        // Start the scheduler within the HumanTaskSchedulerInitializer to ensure that all the tasks are deployed
        // when the scheduler actually starts.
        // simpleScheduler.start();

        scheduler = simpleScheduler;
    }

    /**
     *
     *
     * @throws HumanTaskServerException :
     */
    private void initPeopleQueryEvaluator() throws HumanTaskServerException {
        try {
            PeopleQueryEvaluator peopleQueryEvaluator = (PeopleQueryEvaluator)
                    Class.forName(serverConfig.getPeopleQueryEvaluatorClass()).newInstance();
            taskEngine.setPeopleQueryEvaluator(peopleQueryEvaluator);
        } catch (Exception ex) {
            String errMsg = "Error instantiating the PeopleQueryEvaluator Class :" +
                            serverConfig.getPeopleQueryEvaluatorClass();
            log.error(errMsg);
            throw new HumanTaskServerException(errMsg, ex);
        }
    }

    // Initialises the human task engine.
    private void initHumanTaskEngine() {
        HumanTaskEngine humanTaskEngine = new HumanTaskEngine();
        humanTaskEngine.setDaoConnectionFactory(this.daoConnectionFactory);
        humanTaskEngine.setEventProcessor(this.eventProcessor);
        this.taskEngine = humanTaskEngine;
    }

    // Initialises the data source with the provided configuration parameters.
    private void initDataSource() throws HumanTaskServerException {
        database = new Database(serverConfig);
        //TODO - need to handle the external transaction managers.
        database.setTransactionManager(tnxManager);
        try {
            database.start();
        } catch (Exception e) {
            String errMsg = "Humantask Database Initialization failed.";
            log.error(errMsg);
            throw new HumanTaskServerException(errMsg, e);
        }
    }

    /**
     * Initialize the data access layer.
     *
     * @throws HumanTaskServerException : If the dao layer initializing fails..
     */
    private void initDAO() throws HumanTaskServerException {
        try {
            this.daoConnectionFactory = database.createDAOConnectionFactory();
        } catch (Exception e) {
            String errMsg = "Error instantiating the DAO Connection Factory Class :" +
                            serverConfig.getDaoConnectionFactoryClass();
            throw new HumanTaskServerException(errMsg, e);
        }
    }

    //

    /**
     *  Event processor initialisation logic.
     *  As of now we have a set of event listeners which we register at the server startup.
     *
     * @throws HumanTaskServerException : If the event listener object instantiation fails.
     */
    private void initEventProcessor() throws HumanTaskServerException {
        EventProcessor eventProcessor = new EventProcessor();
        for (String eventListenerClassName : serverConfig.getEventListenerClassNames()) {
            try {
                Class eventListenerClass = this.getClass().getClassLoader().loadClass(eventListenerClassName);
                HumanTaskEventListener eventListener = (HumanTaskEventListener) eventListenerClass.newInstance();
                eventProcessor.addEventListener(eventListener);
            } catch (Exception e) {
                log.fatal("Couldn't initialize the event listener for class: "
                          + eventListenerClassName, e);
                throw new HumanTaskServerException("Couldn't initialize a event listener: "
                                                   + eventListenerClassName, e);
            }
        }

        this.eventProcessor = eventProcessor;
    }

    /**
     * Initialize human task stores and reload existing tasks if necessary.
     */
    private void initHumanTaskStore() {
        taskStoreManager = new HumanTaskStoreManager();
    }

    /**
     * Read the human task configuration file and load it to memory. If configuration file is not there default
     * configuration will be created.
     */
    private void loadHumanTaskServerConfiguration() {
        if (log.isDebugEnabled()) {
            log.debug("Loading Human Task Server Configuration...");
        }

        if (isHumanTaskConfigurationFileAvailable()) {
            File htServerConfigFile = new File(calculateHumanTaskServerConfigurationFilePath());
            serverConfig = new HumanTaskServerConfiguration(htServerConfigFile);
        } else {
            log.info("Humantask configuration file: " + HumanTaskConstants.HUMANTASK_CONFIG_FILE +
                     " not found. Loading default configurations.");
            serverConfig = new HumanTaskServerConfiguration();
        }
    }

    /**
     * @return : true is the configuration file is in the file system false otherwise.
     */
    private boolean isHumanTaskConfigurationFileAvailable() {
        File humanTaskConfigurationFile = new File(calculateHumanTaskServerConfigurationFilePath());
        return humanTaskConfigurationFile.exists();
    }

    /**
     * Note: Need to figure out how to merge this with bps.xml
     *
     * @return Human task server configuration path.
     */
    private String calculateHumanTaskServerConfigurationFilePath() {
        return CarbonUtils.getCarbonConfigDirPath() + File.separator +
               HumanTaskConstants.HUMANTASK_CONFIG_FILE;
    }

    /**
     * @return : The human task store manager.
     */
    public HumanTaskStoreManager getTaskStoreManager() {
        return taskStoreManager;
    }

    /**
     * @return : The taskEngine.
     */
    public HumanTaskEngine getTaskEngine() {
        return taskEngine;
    }

    /**
     * @return : The human task database.
     */
    public Database getDatabase() {
        return this.database;
    }

    // initialize the external transaction manager.
    private void initTransactionManager() throws HumanTaskServerException {
        String transactionFactoryName = serverConfig.getTransactionFactoryClass();
        if (log.isDebugEnabled()) {
            log.debug("Initializing transaction manager using " + transactionFactoryName);
        }

        try {
            Class txFactoryClass = this.getClass().getClassLoader().loadClass(transactionFactoryName);
            Object txFactory = txFactoryClass.newInstance();
            tnxManager = (TransactionManager) txFactoryClass.
                    getMethod("getTransactionManager", (Class[]) null).invoke(txFactory);

            // Didn't use Debug Transaction manager which used in ODE.
            // TODO: Look for the place we use this axis parameter.
            //axisConfiguration.addParameter("ode.transaction.manager", transactionManager);
        } catch (Exception e) {
            log.fatal("Couldn't initialize a transaction manager with factory: "
                      + transactionFactoryName, e);
            throw new HumanTaskServerException("Couldn't initialize a transaction manager with factory: "
                                               + transactionFactoryName, e);
        }
    }

    /**
     * @return : The server configuration information.
     */
    public HumanTaskServerConfiguration getServerConfig() {
        return serverConfig;
    }

    /**
     * @return : The DAO Connection Factory.
     */
    public HumanTaskDAOConnectionFactory getDaoConnectionFactory() {
        return daoConnectionFactory;
    }

    /**
     * The shutdown logic for the human task server.
     */
    public void shutdown() {
        //TODO add shutdown hook

        if (scheduler != null) {
            try {
                if (log.isDebugEnabled()) {
                    log.debug("shutting down scheduler.");
                }
                scheduler.shutdown();
                scheduler = null;
            } catch (Exception ex) {
                log.error("Scheduler couldn't be shutdown.", ex);
            }
        }
    }
}
