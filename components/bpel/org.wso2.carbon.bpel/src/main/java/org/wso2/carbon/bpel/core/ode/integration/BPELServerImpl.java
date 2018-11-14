/*
 * Copyright (c) 2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.bpel.core.ode.integration;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.Member;
import com.hazelcast.core.MemberAttributeEvent;
import com.hazelcast.core.MembershipEvent;
import com.hazelcast.core.MembershipListener;
import org.apache.commons.httpclient.HttpConnectionManager;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.commons.httpclient.util.IdleConnectionTimeoutThread;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.common.evt.DebugBpelEventListener;
import org.apache.ode.bpel.dao.BpelDAOConnectionFactory;
import org.apache.ode.bpel.engine.BpelServerImpl;
import org.apache.ode.bpel.engine.CountLRUDehydrationPolicy;
import org.apache.ode.bpel.engine.cron.CronScheduler;
import org.apache.ode.bpel.extension.ExtensionBundleRuntime;
import org.apache.ode.bpel.extension.ExtensionCorrelationFilter;
import org.apache.ode.bpel.iapi.BpelEngineException;
import org.apache.ode.bpel.iapi.BpelEventListener;
import org.apache.ode.bpel.iapi.EndpointReferenceContext;
import org.apache.ode.bpel.iapi.ProcessConf;
import org.apache.ode.bpel.iapi.ProcessStoreEvent;
import org.apache.ode.bpel.iapi.ProcessStoreListener;
import org.apache.ode.bpel.iapi.Scheduler;
import org.apache.ode.bpel.intercept.MessageExchangeInterceptor;
import org.apache.ode.bpel.memdao.BpelDAOConnectionFactoryImpl;
import org.apache.ode.il.dbutil.Database;
import org.apache.ode.scheduler.simple.JdbcDelegate;
import org.apache.ode.scheduler.simple.ODECluster;
import org.apache.ode.scheduler.simple.SimpleScheduler;
import org.wso2.carbon.bpel.core.BPELConstants;
import org.wso2.carbon.bpel.core.internal.BPELServerHolder;
import org.wso2.carbon.bpel.core.internal.BPELServiceComponent;
import org.wso2.carbon.bpel.core.ode.integration.config.BPELServerConfiguration;
import org.wso2.carbon.bpel.core.ode.integration.jmx.Instance;
import org.wso2.carbon.bpel.core.ode.integration.jmx.InstanceStatusMonitor;
import org.wso2.carbon.bpel.core.ode.integration.jmx.Processes;
import org.wso2.carbon.bpel.core.ode.integration.store.BPELDeploymentContext;
import org.wso2.carbon.bpel.core.ode.integration.store.MultiTenantProcessStore;
import org.wso2.carbon.bpel.core.ode.integration.store.ProcessConfigurationImpl;
import org.wso2.carbon.bpel.core.ode.integration.store.ProcessStoreImpl;
import org.wso2.carbon.bpel.core.ode.integration.store.TenantProcessStoreImpl;
import org.wso2.carbon.bpel.core.ode.integration.utils.BPELDatabaseCreator;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.config.RegistryContext;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.MBeanRegistrar;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.NotCompliantMBeanException;
import javax.sql.DataSource;
import javax.transaction.TransactionManager;

/**
 * BPELServer implementation. All the ODE BPEL Engine initialization is handled here.
 */
public final class BPELServerImpl implements BPELServer, Observer {
    private static Log log = LogFactory.getLog(BPELServerImpl.class);

    /* ODE BPEL Server instance*/
    private BpelServerImpl odeBpelServer;

    private ProcessStoreImpl processStore;

    private TransactionManager transactionManager;

    /* For the moment it's look like we don't want multi-threaded http connection manager*/
    private MultiThreadedHttpConnectionManager httpConnectionManager;

    /* BPEL DAO Connection Factory*/
    private BpelDAOConnectionFactory daoConnectionFactory;

    /* ODE Database manager */
    private Database db;

    /* ODE Scheduler */
    private Scheduler scheduler;

    /* ODE Configuration properties */
    private ODEConfigurationProperties odeConfigurationProperties;

    private ExecutorService executorService;

    private CronScheduler cronScheduler;

    private IdleConnectionTimeoutThread idleConnectionTimeoutThread;

    /* BPEL Server Configuration */
    private BPELServerConfiguration bpelServerConfiguration;

    private boolean isManager= false;

    private static BPELServerImpl ourInstance = new BPELServerImpl();

    public static BPELServerImpl getInstance() {
        return ourInstance;
    }

    private BPELServerImpl() {
    }

    /**
     * Initialize the ODE BPEL engine.
     *
     * @throws Exception if failed to start the BPEL engine.
     */
    public void init() throws Exception {
        bpelServerConfiguration = new BPELServerConfiguration();
        odeConfigurationProperties = new ODEConfigurationProperties(bpelServerConfiguration);

        if (log.isDebugEnabled()) {
            log.debug("Initializing transaction manager");
        }
        initTransactionManager();

        if (log.isDebugEnabled()) {
            log.debug("Creating data source");
        }
        initDataSource();

        if (log.isDebugEnabled()) {
            log.debug("Starting DAO");
        }
        initDAO();

        BPELEndpointReferenceContextImpl eprContext =
                new BPELEndpointReferenceContextImpl();

        if (log.isDebugEnabled()) {
            log.debug("Initializing BPEL process store");
        }
        initProcessStore(eprContext);

        if (log.isDebugEnabled()) {
            log.debug("Initializing BPEL server");
        }
        initBPELServer(eprContext);

        isManager = isManagerNode();
        if (isManager)
            log.info("This node is a manager node");

        if (log.isDebugEnabled()) {
            log.debug("Initializing multithreaded connection manager");
        }
        initHttpConnectionManager();

        /* Register event listeners configured in ode-axis2.properties file*/
        registerEventListeners();

        /* Register message exchange interceptors configured in ode-axis.properties file*/
        registerMexInterceptors();

        registerExtensionActivityBundles();
        registerExtensionCorrelationFilters();

        //registerExtensionActivityBundles();

        //registerExternalVariableModules();

        try {
            odeBpelServer.start();
        } catch (Exception e) {
            shutdown();
            String errMsg = "BPEL Server failed to start.";
            log.error(errMsg, e);
            throw new Exception(errMsg, e);
        }

        if (bpelServerConfiguration.getUseDistributedLock() && isAxis2ClusteringEnabled()) {
            BPELServerHolder.getInstance().addObserver(this);
            if (log.isDebugEnabled()) {
                log.debug("Clustering Enabled, Registering Observer for HazelCast service");
            }
        }
        registerMBeans();
    }

    /**
     * Shutdown ODE BPEL Server, schedulers, process store, database connections and
     * http connection pools.
     *
     * @throws Exception if error occurred while shutting down BPEL Server.
     */
    public void shutdown() throws Exception {
        if (scheduler != null) {
            try {
                if (log.isDebugEnabled()) {
                    log.debug("Shutting down quartz scheduler.");
                }
                scheduler.shutdown();
            } catch (Exception e) {
                log.warn("Scheduler couldn't be shut down.", e);
            } finally {
                scheduler = null;
            }
        }

        if (odeBpelServer != null) {
            try {
                if (log.isDebugEnabled()) {
                    log.debug("Shutting down BPEL server.");
                }
                odeBpelServer.shutdown();
            } catch (Exception e) {
                log.warn("Error stopping services.", e);
            } finally {
                odeBpelServer = null;
            }
        }

        if (cronScheduler != null) {
            try {
                if (log.isDebugEnabled()) {
                    log.debug("Shutting down cron scheduler.");
                }
                cronScheduler.shutdown();
            } catch (Exception e) {
                log.warn("Cron scheduler couldn't be shutdown.", e);
            } finally {
                cronScheduler = null;
            }

        }

        if (processStore != null) {
            try {
                if (log.isDebugEnabled()) {
                    log.debug("Shutting down process store.");
                }
                processStore.shutdown();
            } catch (Exception e) {
                log.warn("Process store could not be shutdown.", e);
            } finally {
                processStore = null;
            }
        }

        if (daoConnectionFactory != null) {
            try {
                daoConnectionFactory.shutdown();
            } catch (Exception e) {
                log.warn("DAO shutdown failed.", e);
            } finally {
                daoConnectionFactory = null;
            }
        }

        if (db != null) {
            try {
                db.shutdown();
            } catch (Exception e) {
                log.warn("DB shutdown failed.", e);
            } finally {
                db = null;
            }
        }

        if (transactionManager != null) {
            transactionManager = null;
        }

        if (httpConnectionManager != null) {
            try {
                if (log.isDebugEnabled()) {
                    log.debug("Shutting down HTTP Connection Manager.");
                }
                httpConnectionManager.shutdown();
            } catch (Exception e) {
                log.warn("HTTP Connection Manager shutdown failed.");
            }
        }

        if (idleConnectionTimeoutThread != null) {
            try {
                if (log.isDebugEnabled()) {
                    log.debug("Shutting down Idle Connection Timeout Thread.");
                }
                idleConnectionTimeoutThread.shutdown();
            } catch (Exception e) {
                log.warn("Idle connection timeout thread shutdown failed.");
            }
        }

        executorService.shutdown();

        log.info("BPEL Server shutdown completed.");
    }


    /**
     * Register BPEL Event listener.
     *
     * @param eventListenerClass Fully qualified class name of BpelEventListener implementation.
     */
    public void registerEventListener(final String eventListenerClass) {
        try {
            odeBpelServer.registerBpelEventListener(
                    (BpelEventListener) Class.forName(eventListenerClass).newInstance());
            log.info("Registered custom BPEL event listener: " + eventListenerClass);
        } catch (Exception e) {
            log.warn("Couldn't register the event listener " + eventListenerClass
                    + ", the class couldn't loaded properly: ", e);
        }
    }

    /**
     * Register ODE message echange interceptor.
     *
     * @param mexInterceptorClass Fully qualified class name of  ODe MexInterceptor implementation
     */
    public void registerMessageExchangeInterceptor(final String mexInterceptorClass) {

    }

    /**
     * Get the multi-tenant process store instance of BPEL Server.
     *
     * @return MultiTenant Process store instance
     */
    public MultiTenantProcessStore getMultiTenantProcessStore() {
        return processStore;
    }

    /**
     * Get the multi threaded http connection manager to use with external service invocations.
     *
     * @return HttpConnectionManager instace(multi-threaded implementation).
     */
    public HttpConnectionManager getHttpConnectionManager() {
        return httpConnectionManager;
    }

    public BpelServerImpl getODEBPELServer() {
        return odeBpelServer;
    }

    public TransactionManager getTransactionManager() {
        return transactionManager;
    }

    public ODEConfigurationProperties getOdeConfigurationProperties() {
        return odeConfigurationProperties;
    }

    /**
     * Initialize the transaction manager.
     *
     * @throws BPELEngineException If error occured while initializing transaction manager
     */
    private void initTransactionManager() throws BPELEngineException {
        String txFactoryName = bpelServerConfiguration.getTransactionFactoryClass();

        if (log.isDebugEnabled()) {
            log.debug("Initializing transaction manager using " + txFactoryName);
        }

        try {
            Class txFactoryClass = this.getClass().getClassLoader().loadClass(txFactoryName);
            Object txFactory = txFactoryClass.newInstance();
            int transactionTimeout = bpelServerConfiguration.getTransactionManagerTimeout();
            if (transactionTimeout > -1) {
                transactionManager = (TransactionManager) txFactoryClass.
                        getMethod("getTransactionManager", int.class).invoke(txFactory, transactionTimeout);
            } else {
                transactionManager = (TransactionManager) txFactoryClass.
                        getMethod("getTransactionManager", (Class[]) null).invoke(txFactory);
            }


            // Didn't use Debug Transaction manager which used in ODE.
            // TODO: Look for the place we use this axis parameter.
            //axisConfiguration.addParameter("ode.transaction.manager", transactionManager);
        } catch (Exception e) {
            log.fatal("Couldn't initialize a transaction manager with factory: "
                    + txFactoryName, e);
            throw new BPELEngineException("Couldn't initialize a transaction manager with factory: "
                    + txFactoryName, e);
        }
    }

    /**
     * Initialize the data source.
     *
     * @throws BPELEngineException If error occured while initializing datasource
     */
    private void initDataSource() throws BPELEngineException {
        db = new Database(odeConfigurationProperties);
        db.setTransactionManager(transactionManager);

        if (System.getProperty("setup") != null) {
            BPELDatabaseCreator bpelDBCreator;
            try {
                bpelDBCreator = new BPELDatabaseCreator(
                        db.<DataSource>lookupInJndi(odeConfigurationProperties.getDbDataSource()));
            } catch (Exception e) {
                String errMsg = "Error creating BPELDatabaseCreator";
                log.error(errMsg, e);
                throw new BPELEngineException(errMsg, e);
            }
            if (!bpelDBCreator.isDatabaseStructureCreated("SELECT * FROM ODE_SCHEMA_VERSION")) {
                try {
                    //TODO rename following method
                    bpelDBCreator.createRegistryDatabase();
                } catch (Exception e) {
                    String errMsg = "Error creating BPEL database";
                    log.error(errMsg, e);
                    throw new BPELEngineException(errMsg, e);
                }
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("BPEL database already exists. Using the old database.");
                }
            }
        }

        // In carbon, embedded H2 database for ODE is located at CARBON_HOME/repository/database
        String dbRoot = CarbonUtils.getCarbonHome() + File.separator + "repository" + File.separator
                + "database";
        File dbRootDir = new File(dbRoot);

        if (dbRootDir.exists() && dbRootDir.isDirectory()) {
            db.setWorkRoot(dbRootDir);
        } else {
            db.setWorkRoot(null);
        }

        try {
            db.start();
        } catch (Exception e) {
            String errMsg =
                    "Error starting database connections, check the database configuration!";
            log.error(errMsg, e);
            throw new BPELEngineException(errMsg, e);
        }
    }

    /**
     * Initialize ODE DAO connection factory.
     *
     * @throws BPELEngineException if DAO connection factory creation fails
     */
    private void initDAO() throws BPELEngineException {
        log.info("Using DAO Connection Factory class: " +
                odeConfigurationProperties.getDAOConnectionFactory());
        try {
            daoConnectionFactory = db.createDaoCF();
        } catch (Exception e) {
            String errMsg = "Error instantiating DAO Connection Factory class " +
                    odeConfigurationProperties.getDAOConnectionFactory();
            log.error(errMsg, e);
            throw new BPELEngineException(errMsg, e);
        }
    }

    /**
     * Initialize process store/
     *
     * @param eprContext Endpoint reference context
     * @throws Exception if process store initialization failed
     */
    private void initProcessStore(EndpointReferenceContext eprContext) throws Exception {
        processStore = new ProcessStoreImpl(eprContext,
                db.getDataSource(),
                odeConfigurationProperties);
        processStore.setLocalBPELDeploymentUnitRepo(new File(CarbonUtils.getCarbonHome() +
                File.separator + "repository" + File.separator + "bpel"));
        processStore.registerListener(new ProcessStoreListenerImpl());
    }

    /**
     * Init ODE BpelServer.
     *
     * @param eprContext endpoint reference context.
     */
    private void initBPELServer(EndpointReferenceContext eprContext) {
        initExecutorService(createThreadFactory());

        odeBpelServer = new BpelServerImpl();

        setupJobScheduler();
        setupCronScheduler();

        odeBpelServer.setDaoConnectionFactory(daoConnectionFactory);
        odeBpelServer.setInMemDaoConnectionFactory(
                new BpelDAOConnectionFactoryImpl(scheduler, odeConfigurationProperties.getInMemMexTtl()));
        odeBpelServer.setEndpointReferenceContext(eprContext);
        odeBpelServer.setMessageExchangeContext(new BPELMessageExchangeContextImpl());
        odeBpelServer.setBindingContext(new BPELBindingContextImpl(this));
        odeBpelServer.setScheduler(scheduler);

        // TODO: Analyze a way of integrating with lazy loading
        activateDehydration();

        odeBpelServer.setMigrationTransactionTimeout(
                odeConfigurationProperties.getMigrationTransactionTimeout());
        odeBpelServer.setConfigProperties(
                odeConfigurationProperties.getProperties());
        odeBpelServer.init();

        odeBpelServer.setInstanceThrottledMaximumCount(
                odeConfigurationProperties.getInstanceThrottledMaximumCount());
        odeBpelServer.setProcessThrottledMaximumCount(
                odeConfigurationProperties.getProcessThrottledMaximumCount());
        odeBpelServer.setProcessThrottledMaximumSize(
                odeConfigurationProperties.getProcessThrottledMaximumSize());
        odeBpelServer.setHydrationLazy(odeConfigurationProperties.isHydrationLazy());
        odeBpelServer.setHydrationLazyMinimumSize(
                odeConfigurationProperties.getHydrationLazyMinimumSize());
    }

    /**
     * Activate process dehydration.
     */
    private void activateDehydration() {
        if (bpelServerConfiguration.isProcessDehydrationEnabled()) {
            CountLRUDehydrationPolicy dehy = new CountLRUDehydrationPolicy();
            if (bpelServerConfiguration.getProcessDehydrationMaxAge() > 0) {
                dehy.setProcessMaxAge(bpelServerConfiguration.getProcessDehydrationMaxAge());
                if (log.isDebugEnabled()) {
                    log.debug("Process Max Age: "
                            + bpelServerConfiguration.getProcessDehydrationMaxAge());
                }
            }
            if (bpelServerConfiguration.getProcessDehydraionMaxCount() > 0) {
                dehy.setProcessMaxCount(bpelServerConfiguration.getProcessDehydraionMaxCount());
                if (log.isDebugEnabled()) {
                    log.debug("Process Max Count: "
                            + bpelServerConfiguration.getProcessDehydraionMaxCount());
                }
            }
            odeBpelServer.setDehydrationPolicy(dehy);
            log.info("Process Dehydration is activated...");
        }
    }

    /**
     * Setting up cron scheduler
     */
    private void setupCronScheduler() {
        cronScheduler = new CronScheduler();
        cronScheduler.setScheduledTaskExec(executorService);
        cronScheduler.setContexts(odeBpelServer.getContexts());
        odeBpelServer.setCronScheduler(cronScheduler);

        cronScheduler.scheduleSystemCronJobs(bpelServerConfiguration.getSystemCleanupCronJobs());
    }

    private void setupJobScheduler() {
        scheduler = createScheduler();
        scheduler.setJobProcessor(odeBpelServer);

        BpelServerImpl.PolledRunnableProcessor polledRunnableProcessor =
                new BpelServerImpl.PolledRunnableProcessor();
        polledRunnableProcessor.setPolledRunnableExecutorService(executorService);
        polledRunnableProcessor.setContexts(odeBpelServer.getContexts());
        scheduler.setPolledRunnableProcesser(polledRunnableProcessor);

    }

    private Scheduler createScheduler() {
        SimpleScheduler simpleScheduler = new SimpleScheduler(bpelServerConfiguration.getNodeId(),
                new JdbcDelegate(db.getDataSource()),
                odeConfigurationProperties.getProperties());
        simpleScheduler.setExecutorService(executorService);
        simpleScheduler.setTransactionManager(transactionManager);
        return simpleScheduler;
    }

    private ThreadFactory createThreadFactory() {
        return new ThreadFactory() {
            private int threadNumber = 0;

            public Thread newThread(Runnable r) {
                threadNumber += 1;
                Thread t = new Thread(r, "BPELServer-" + threadNumber);
                t.setDaemon(true);
                return t;
            }
        };
    }

    private void initExecutorService(ThreadFactory threadFactory) {
        if (odeConfigurationProperties.getThreadPoolMaxSize() == 0) {
            executorService = Executors.newCachedThreadPool(threadFactory);
        } else {
            executorService = Executors.newFixedThreadPool(
                    odeConfigurationProperties.getThreadPoolMaxSize(),
                    threadFactory);
        }
    }

    private void initHttpConnectionManager() throws Exception {
        httpConnectionManager = new MultiThreadedHttpConnectionManager();
        int maxConnectionsPerHost = bpelServerConfiguration.getMaxConnectionsPerHost();
        int maxTotalConnections = bpelServerConfiguration.getMaxTotalConnections();
        if (log.isDebugEnabled()) {
            log.debug(HttpConnectionManagerParams.MAX_HOST_CONNECTIONS + "=" + maxConnectionsPerHost);
            log.debug(HttpConnectionManagerParams.MAX_TOTAL_CONNECTIONS + "=" + maxTotalConnections);
        }
        if (maxConnectionsPerHost < 1 || maxTotalConnections < 1) {
            String errmsg = HttpConnectionManagerParams.MAX_HOST_CONNECTIONS + " and " +
                    HttpConnectionManagerParams.MAX_TOTAL_CONNECTIONS
                    + " must be positive integers!";
            log.error(errmsg);
            throw new Exception(errmsg);
        }
        httpConnectionManager.getParams().setDefaultMaxConnectionsPerHost(maxConnectionsPerHost);
        httpConnectionManager.getParams().setMaxTotalConnections(maxTotalConnections);

        // TODO: Modify this and move configuration to bps.xml
        // Register the connection manager to a idle check thread
        idleConnectionTimeoutThread = new IdleConnectionTimeoutThread();
        idleConnectionTimeoutThread.setName("Http_Idle_Connection_Timeout_Thread");
        long idleConnectionTimeout = Long.parseLong(
                odeConfigurationProperties
                        .getProperty("http.idle.connection.timeout", "30000"));
        long idleConnectionCheckInterval = Long.parseLong(
                odeConfigurationProperties
                        .getProperty("http.idle.connection.check.interval", "30000"));

        if (log.isDebugEnabled()) {
            log.debug("http.idle.connection.timeout=" + idleConnectionTimeout);
            log.debug("http.idle.connection.check.interval=" + idleConnectionCheckInterval);
        }
        idleConnectionTimeoutThread.setConnectionTimeout(idleConnectionTimeout);
        idleConnectionTimeoutThread.setTimeoutInterval(idleConnectionCheckInterval);

        idleConnectionTimeoutThread.addConnectionManager(httpConnectionManager);
        idleConnectionTimeoutThread.start();
    }

    private void registerEventListeners() {
        /* let's always register the debugging listener */
        odeBpelServer.registerBpelEventListener(new DebugBpelEventListener());


        List<String> eventListeners = bpelServerConfiguration.getEventListeners();
        if (!eventListeners.isEmpty()) {
            for (String listenerCN : eventListeners) {
                try {
                    odeBpelServer.registerBpelEventListener(
                            (BpelEventListener) Class.forName(listenerCN).newInstance());
                    log.info("Registered custom BPEL event listener: " + listenerCN);
                } catch (Exception e) {
                    log.warn("Couldn't register the event listener " + listenerCN
                            + ", the class couldn't loaded properly: ", e);
                }
            }
        }
    }

    private void registerMexInterceptors() {
        List<String> mexInterceptors = bpelServerConfiguration.getMexInterceptors();
        if (!mexInterceptors.isEmpty()) {
            for (String interceptorCN : mexInterceptors) {
                try {
                    odeBpelServer.registerMessageExchangeInterceptor(
                            (MessageExchangeInterceptor) Class.forName(interceptorCN)
                                    .newInstance());
                    log.info("Registered message exchange interceptor: " + interceptorCN);
                } catch (Exception e) {
                    log.warn("Couldn't register the message exchange interceptor " + interceptorCN
                            + ", the class couldn't be " + "loaded properly.", e);
                }
            }
        }
    }

    private void registerExtensionActivityBundles() {

        try {
            log.info("Registering E4X Extension...");
            odeBpelServer.registerExtensionBundle((ExtensionBundleRuntime) Class.
                    forName("org.apache.ode.extension.e4x.JSExtensionBundle").newInstance());
        } catch (Exception e) {
            log.error("Couldn't register e4x extension bundles runtime.", e);
        }

        try {
            log.info("Registering B4P Extension...");
            odeBpelServer.registerExtensionBundle((ExtensionBundleRuntime) Class.
                    forName("org.wso2.carbon.bpel.b4p.extension.BPEL4PeopleExtensionBundle").newInstance());
        } catch (Exception e) {
            log.error("Couldn't register B4P extension bundles runtime.", e);
        }

        //TODO register B4P extension, once it is available

        List<String> extensionBundleRuntimes = bpelServerConfiguration.getExtensionBundleRuntimes();
//        List<String> extensionBundleValidators = bpelServerConfiguration.getExtensionBundleValidators();
        if (extensionBundleRuntimes != null) {
            for (String extension : extensionBundleRuntimes) {
                try {
                    // instantiate bundle
                    ExtensionBundleRuntime bundleRT =
                            (ExtensionBundleRuntime) Class.forName(extension).newInstance();
                    // register extension bundle (BPEL server)
                    odeBpelServer.registerExtensionBundle(bundleRT);
                } catch (Exception e) {
                    log.warn("Couldn't register the extension bundle runtime " + extension +
                            ", the class couldn't be " + "loaded properly.");
                }
            }
        }
        //TODO register validators
/*
        if (extensionBundleValidators != null) {
            Map<QName, ExtensionValidator> validators = new HashMap<QName, ExtensionValidator>();
            for (String validator : extensionBundleValidators) {
                try {
                    // instantiate bundle
                    ExtensionBundleValidation bundleVal =
                            (ExtensionBundleValidation) Class.forName(validator).newInstance();
                    //add validators
                    validators.putAll(bundleVal.getExtensionValidators());
                } catch (Exception e) {
                    log.warn("Couldn't register the extension bundle validator " + validator +
                            ", the class couldn't be " + "loaded properly.");
                }
            }
            // register extension bundle (BPEL store)
            store.setExtensionValidators(validators);
        }
*/
    }

    private void registerExtensionCorrelationFilters() {

        try {
            log.info("Registering B4P Filter...");
            odeBpelServer.registerExtensionCorrelationFilter((ExtensionCorrelationFilter) Class.
                    forName("org.wso2.carbon.bpel.b4p.extension.BPEL4PeopleCorrelationFilter").newInstance());
        } catch (Exception e) {
            log.error("Couldn't register B4P extension filter.", e);
        }

        List<String> extensionFilters = bpelServerConfiguration.getExtensionCorrelationFilters();
        if (extensionFilters != null) {
            // TODO replace StringTokenizer by regex
            for (String filter : extensionFilters) {
                try {
                    // instantiate bundle
                    ExtensionCorrelationFilter filterRT =
                            (ExtensionCorrelationFilter) Class.forName(filter).newInstance();
                    // register correlation filter (BPEL server)
                    odeBpelServer.registerExtensionCorrelationFilter(filterRT);
                } catch (Exception e) {
                    log.warn("Couldn't register the extension correlation filter " + filter + ", the class couldn't " +
                            "be " +
                            "loaded properly.");
                }
            }
        }
    }


    private class ProcessStoreListenerImpl implements ProcessStoreListener {
        public void onProcessStoreEvent(ProcessStoreEvent processStoreEvent) {
            if (log.isDebugEnabled()) {
                log.debug("Process store event: " + processStoreEvent);
            }
            ProcessConf pConf = processStore.getProcessConfiguration(processStoreEvent.pid);
            switch (processStoreEvent.type) {
                case DEPLOYED:
                    if (pConf != null) {
                        /*
                        * If and only if an old process exists with the same pid,
                        * the old process is cleaned up. The following line is IMPORTANT and
                        * used for the case when the deployment and store do not have the
                        * process while the process itself exists in the BPEL_PROCESS table.
                        * Notice that the new process is actually created on the 'ACTIVATED'
                        * event.
                        */
                        odeBpelServer.cleanupProcess(pConf);
                    }
                    break;
                case ACTIVATED:
                    // bounce the process
                    odeBpelServer.unregister(processStoreEvent.pid);
                    if (pConf != null) {
                        //odeBpelServer.register(pConf);
                        try {
                            odeBpelServer.register(pConf);
                        } catch (BpelEngineException ex) {
                            String failureCause = "Process registration failed for:" +
                                    pConf.getProcessId() + ". " + ex.getMessage();
                            //create DeploymentContext in order to persist the error
                            int tenantID = processStore.getTenantId(pConf.getProcessId());
                            String bpelRepoRoot = processStore.getLocalDeploymentUnitRepo().getAbsolutePath();
                            ProcessConfigurationImpl pConfImpl = (ProcessConfigurationImpl) pConf;
                            File bpelArchive = new File(pConfImpl.getAbsolutePathForBpelArchive());

                            BPELDeploymentContext deploymentContext =
                                    new BPELDeploymentContext(tenantID,
                                            bpelRepoRoot, bpelArchive, pConf.getVersion());
                            deploymentContext.setDeploymentFailureCause(failureCause);
                            deploymentContext.setStackTrace(ex);
                            deploymentContext.setFailed(true);

                            TenantProcessStoreImpl store =
                                    (TenantProcessStoreImpl) processStore.getTenantsProcessStore(tenantID);
                            try {
                                store.getBPELPackageRepository().handleBPELPackageDeploymentError(deploymentContext);
                            } catch (Exception e) {
                                log.error("Unable to persist the failure cause. Failure: " + failureCause, e);
                            }

                            throw ex;
                        }
                    } else {
                        if (log.isDebugEnabled()) {
                            log.debug("slightly odd:received event " +
                                    processStoreEvent + " for process not in store!");
                        }
                    }
                    break;
                case RETIRED:
                    // are there are instances of this process running?
                    boolean hasInstances = odeBpelServer.hasActiveInstances(
                            processStoreEvent.pid);
                    // Remove the process
                    odeBpelServer.unregister(processStoreEvent.pid);
                    // bounce the process if necessary
                    if (hasInstances) {
                        if (pConf != null) {
                            odeBpelServer.register(pConf);
                        } else {
                            if (log.isDebugEnabled()) {
                                log.debug("slightly odd:received event " +
                                        processStoreEvent + " for process not in store!");
                            }
                        }
                    } else {
                        // we may have potentially created a lot of garbage, so,
                        // let's hope the garbage collector is configured properly.
                        if (pConf != null) {
                            odeBpelServer.cleanupProcess(pConf);
                        }
                    }
                    break;
                case DISABLED:
                case UNDEPLOYED:
                    odeBpelServer.unregister(processStoreEvent.pid);
                    if (pConf != null) {
                        odeBpelServer.cleanupProcess(pConf);
                    }
                    break;
                default:
                    if (log.isDebugEnabled()) {
                        log.debug("Ignoring store event: " + processStoreEvent);
                    }
            }

            if (pConf != null) {
                if (processStoreEvent.type == ProcessStoreEvent.Type.UNDEPLOYED) {
                    if (log.isDebugEnabled()) {
                        log.debug("Cancelling all cron scheduled jobs on store event: "
                                + processStoreEvent);
                    }
                    odeBpelServer.getContexts().cronScheduler.cancelProcessCronJobs(
                            processStoreEvent.pid, true);
                }

                // Except for undeploy event, we need to re-schedule process dependent jobs
                if (log.isDebugEnabled()) {
                    log.debug("(Re)scheduling cron scheduled jobs on store event: "
                            + processStoreEvent);
                }
                if (processStoreEvent.type != ProcessStoreEvent.Type.UNDEPLOYED) {
                    odeBpelServer.getContexts().cronScheduler.scheduleProcessCronJobs(
                            processStoreEvent.pid, pConf);

                }
            }
        }
    }

    /**
     * Function to check whether this nos node is manager node by checking registry read/write ability
     *
     * @return true if this is manager node, false otherwise
     */
    private boolean isManagerNode() {
        try {
            //get config registry of super tenant
            Registry configRegistry = BPELServiceComponent.getRegistryService().getConfigSystemRegistry(-1234);
            RegistryContext registryContext = configRegistry.getRegistryContext();
            if (registryContext != null) {
                return !registryContext.isReadOnly();
            }
        } catch (RegistryException e) {
            log.error("Error occurred while retrieving config registry", e);
        }
        return false;
    }

    public BPELServerConfiguration getBpelServerConfiguration() {
        return bpelServerConfiguration;
    }

    static class BPELEngineException extends Exception {
        public BPELEngineException() {
            super();
        }

        public BPELEngineException(String message) {
            super(message);
        }

        public BPELEngineException(String message, Throwable cause) {
            super(message, cause);
        }

        public BPELEngineException(Throwable cause) {
            super(cause);
        }
    }


    public void registerMBeans() throws Exception, MBeanRegistrationException, InstanceAlreadyExistsException,
            NotCompliantMBeanException {
        log.info("Registering MBeans");
        Processes processMBean = new Processes();
        Instance instanceMBean = new Instance();
        InstanceStatusMonitor statusMonitorMBean = InstanceStatusMonitor.getInstanceStatusMonitor();
//        ObjectName instanceStatusObjectName= new ObjectName("org.wso2.carbon.bpel.core.ode.integration
// .jmx:type=InstanceStatusMonitor");
//        ObjectName processObjectName= new ObjectName("org.wso2.carbon.bpel.core.ode.integration.jmx:type=Process");
//        ObjectName instanceObjectName= new ObjectName("org.wso2.carbon.bpel.core.ode.integration.jmx:type=Instance");
        MBeanRegistrar.registerMBean(processMBean, "org.wso2.carbon.bpel.core.ode.integration.jmx:type=Process");
        MBeanRegistrar.registerMBean(instanceMBean, "org.wso2.carbon.bpel.core.ode.integration.jmx:type=Instance");
        MBeanRegistrar.registerMBean(statusMonitorMBean, "org.wso2.carbon.bpel.core.ode.integration" +
                ".jmx:type=InstanceStatusMonitor");


    }

    public Scheduler getScheduler() {
        return scheduler;
    }

    private boolean isAxis2ClusteringEnabled() {
//        return BPELServerHolder.getInstance().getConfigCtxService().
//                getServerConfigContext().getAxisConfiguration().getClusteringAgent() != null;
        return true;
    }

    public void update(Observable o, Object arg) {
        HazelcastInstance hazelcastInstance = BPELServiceComponent.getHazelcastInstance();
        if (hazelcastInstance != null) {
            String name = hazelcastInstance.getName();
            // Set hazelcast instance name as system property
            System.setProperty("WSO2_HZ_INSTANCE_NAME", name);
            if (bpelServerConfiguration.getUseInstanceStateCache()) {
                // set use instance state cache property
                System.setProperty("WSO2_USE_STATE_CACHE", "true");
            }
            odeBpelServer.setHazelcastInstance(hazelcastInstance);
            if (log.isInfoEnabled()) {
                log.info("Configured HazelCast instance for BPS cluster");
            }
            // Registering this node in BPS cluster BPS-675.
            hazelcastInstance.getCluster().addMembershipListener(new MemberShipListener());
            Member localMember = hazelcastInstance.getCluster().getLocalMember();
            String localMemberID = getHazelCastNodeID(localMember);

            log.info("Registering HZ localMember ID " + localMemberID
                    + " as ODE Node ID " + bpelServerConfiguration.getNodeId());

            hazelcastInstance.getMap(BPELConstants.BPS_CLUSTER_NODE_MAP)
                    .put(localMemberID, bpelServerConfiguration.getNodeId());
        }
        ((SimpleScheduler) scheduler).setCluster(new ODEClusterImpl());
        //scheduler.start();
    }

    /**
     * Provides HazelCast node id
     * Added to fix BPS-675
     *
     * @param member
     * @return
     */
    protected static String getHazelCastNodeID(Member member) {
        String hostName = member.getSocketAddress().getHostName();
        int port = member.getSocketAddress().getPort();
        return hostName + ":" + port;
    }

    /**
     * ODEClusterImpl class is added to fix BPS-675
     */
    class ODEClusterImpl implements ODECluster {

        @Override
        public boolean isClusterEnabled() {
            return bpelServerConfiguration.getUseDistributedLock() && isAxis2ClusteringEnabled();
        }

        /**
         * Check whether current node is the leader or not.
         *
         * @return boolean
         */
        @Override
        public boolean isLeader() {
            HazelcastInstance hazelcastInstance = BPELServiceComponent.getHazelcastInstance();
            Member leader = hazelcastInstance.getCluster().getMembers().iterator().next();
            if (leader.localMember()) {
                log.debug("ODEClusterImpl#isLeader: true");
                return true;
            }
            log.debug("#ODEClusterImpl#isLeader: false");
            return false;
        }


        @Override
        public void removeMember(String memberID) {
            // Only manager node is allowed to remove node from the cluster forcefully
            if (memberID != null && isManager()) {
                log.info("Forcefully removing member from BPS Cluster: " + memberID);

                HazelcastInstance hazelcastInstance = BPELServiceComponent.getHazelcastInstance();
                for (Object key : hazelcastInstance.getMap(BPELConstants.BPS_CLUSTER_NODE_MAP).keySet()) {
                    if (memberID.equals(hazelcastInstance.getMap(BPELConstants.BPS_CLUSTER_NODE_MAP).get(key))) {
                        hazelcastInstance.getMap(BPELConstants.BPS_CLUSTER_NODE_MAP).remove(key);
                        log.info("Member "+ memberID + "[" + key + "] removed from WSO2_BPS_NODE_ID_MAP");
                        break;
                    }
                }
            }
        }

        @Override
        public boolean isManager() {
            if (log.isDebugEnabled()) {
                log.debug("ODEClusterImpl#isManager:" + isManager);
            }
            return isManager;
        }

        @Override
        public String getLeader() {
            HazelcastInstance hazelcastInstance = BPELServiceComponent.getHazelcastInstance();
            Member leader = hazelcastInstance.getCluster().getMembers().iterator().next();
            String leaderNodeId =
                    (String) hazelcastInstance.getMap(BPELConstants.BPS_CLUSTER_NODE_MAP).get(getHazelCastNodeID(leader));
            if (log.isDebugEnabled()) {
                log.debug("ODEClusterImpl#getLeader: Hazelcast cluster leader member : " + leader +
                        " , NodeId : " + leaderNodeId);
            }
            return leaderNodeId;
        }

        /**
         * returns Current BPS Nodes in the cluster.
         *
         * @return ODE Node list
         */
        @Override
        public List<String> getKnownNodes() {
            List<String> nodeList = new ArrayList<String>();
            HazelcastInstance hazelcastInstance = BPELServiceComponent.getHazelcastInstance();
            for (Object s : hazelcastInstance.getMap(BPELConstants.BPS_CLUSTER_NODE_MAP).keySet()) {
                nodeList.add((String) hazelcastInstance.getMap(BPELConstants.BPS_CLUSTER_NODE_MAP).get(s));
            }
            if (log.isDebugEnabled()) {
                log.debug("ODEClusterImpl#getKnownNodes: Known nodeList: " + nodeList);
            }
            return nodeList;
        }

    }

    /**
     * MemberShipListener class is added to fix BPS-675
     */
    static class MemberShipListener implements MembershipListener {

        @Override
        public void memberAdded(MembershipEvent membershipEvent) {
            // Noting to do here.
            if (log.isDebugEnabled()) {
                log.debug("New member added event triggered: " + membershipEvent);
            }
            log.info("ODEClusterImpl#memberAdded: Member added to BPS Cluster: " + membershipEvent.getMember());
        }

        @Override
        public void memberRemoved(MembershipEvent membershipEvent) {
            if (log.isDebugEnabled()) {
                log.debug("Member removed event triggered: " + membershipEvent);
            }

            log.info("ODEClusterImpl#memberRemoved: Member removed from BPS Cluster: " + membershipEvent.getMember());
            HazelcastInstance hazelcastInstance = BPELServiceComponent.getHazelcastInstance();
            Member leader = hazelcastInstance.getCluster().getMembers().iterator().next();

            if (log.isDebugEnabled()) {
                StringBuilder nodeListBuilder = new StringBuilder();
                nodeListBuilder.append("[");
                for (Member member : hazelcastInstance.getCluster().getMembers()) {
                    nodeListBuilder.append(",").append(getHazelCastNodeID(member));
                }
                nodeListBuilder.append("]");
                log.debug("BPS Cluster members: " + nodeListBuilder.toString());
                log.debug("Leader node of the BPS cluster: " + getHazelCastNodeID(leader));
            }

            // Allow Leader to update distributed map.
            if (leader.localMember()) {
                String leftMemberID = getHazelCastNodeID(membershipEvent.getMember());
                hazelcastInstance.getMap(BPELConstants.BPS_CLUSTER_NODE_MAP).remove(leftMemberID);

                if (log.isDebugEnabled()) {
                    log.debug("Removed the member: " + leftMemberID + " from the distributed map (WSO2_BPS_NODE_ID_MAP)");
                }
                log.info("Member " + membershipEvent.getMember() + "[" + leftMemberID + "] removed from WSO2_BPS_NODE_ID_MAP");
            }
        }

        @Override
        public void memberAttributeChanged(MemberAttributeEvent memberAttributeEvent) {
            // Noting to do here.
            if (log.isDebugEnabled()) {
                log.debug("ODEClusterImpl#memberAttributeChanged Member attribute change from BPS Cluster: " +
                        memberAttributeEvent.getMember());
            }
        }
    }

}
