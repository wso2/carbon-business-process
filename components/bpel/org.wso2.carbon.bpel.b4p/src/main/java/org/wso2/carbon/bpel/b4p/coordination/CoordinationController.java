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

package org.wso2.carbon.bpel.b4p.coordination;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bpel.b4p.coordination.configuration.CoordinationConfiguration;
import org.wso2.carbon.bpel.b4p.coordination.dao.HTCoordinationDAOConnectionFactory;
import org.wso2.carbon.bpel.b4p.coordination.db.DatabaseConfigurationException;
import org.wso2.carbon.bpel.b4p.coordination.db.DatabaseUtil;
import org.wso2.carbon.bpel.b4p.coordination.event.listeners.EventListener;
import org.wso2.carbon.bpel.b4p.internal.B4PContentHolder;
import org.wso2.carbon.bpel.core.ode.integration.BPELServer;
import org.wso2.carbon.bpel.core.ode.integration.BPELServerImpl;


import javax.transaction.TransactionManager;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class CoordinationController {

    private static final Log log = LogFactory.getLog(CoordinationController.class);

    /**
     * The transaction manager
     */
    private TransactionManager tnxManager;

    private DatabaseUtil dbUtil;

    private HTCoordinationDAOConnectionFactory daoConnectionFactory;
    private CoordinationConfiguration configuration;
    private BPELServer bpelServer;

    private ExecutorService executorService;

    public CoordinationController() {
        configuration = CoordinationConfiguration.getInstance();
        bpelServer = B4PContentHolder.getInstance().getBpelServer();
    }

    /**
     * Initialization logic of B4P coordination service.
     *
     * @throws B4PCoordinationException : if coordination service initialization fails.
     */
    public void init() throws B4PCoordinationException {
        if (configuration.isHumantaskCoordinationEnabled()) {
            log.info("Initialising B4P Coordination service");
            initExecutorService(createThreadFactory());
            initTransactionManager();
            initDataSource();
            initDao();
            registerTerminationListener();

        }
    }


    private void initExecutorService(ThreadFactory threadFactory) {
        executorService = Executors.newCachedThreadPool(threadFactory);
    }


    private void initTransactionManager() {
        if (log.isDebugEnabled()) {
            log.debug("Using BPEL server transaction manager");
        }
        tnxManager = ((BPELServerImpl) bpelServer).getTransactionManager();
    }

    /**
     * Initialize Database util class.
     *
     * @throws B4PCoordinationException
     */
    private void initDataSource() throws B4PCoordinationException {
        if (log.isDebugEnabled()) {
            log.debug("Initialising B4P Coordination database");
        }
        dbUtil = new DatabaseUtil();
        dbUtil.setTransactionManager(tnxManager);
        try {
            dbUtil.start();
        } catch (DatabaseConfigurationException e) {
            String errMsg = "B4P Coordination Database Initialization failed.";
            log.error(errMsg);
            throw new B4PCoordinationException(errMsg, e);
        }
    }

    private void initDao() throws B4PCoordinationException {
        if (log.isDebugEnabled()) {
            log.debug("Initialising B4P Coordination DAO Connection Factory");
        }
        try {
            this.daoConnectionFactory = this.dbUtil.createDAOConnectionFactory();
        } catch (DatabaseConfigurationException e) {
            String errMsg = "Error occurred during instantiating the DAO Connection Factory Class for B4P Coordination";
            log.error(errMsg, e);
            throw new B4PCoordinationException(errMsg, e);
        }
    }

    /**
     * registering b4p EventListener to ODE engine
     */
    private void registerTerminationListener() {
        if (log.isDebugEnabled()) {
            log.debug("Registering TerminationEvent listener");
        }
        B4PContentHolder.getInstance().getBpelServer().registerEventListener(EventListener.class.getName());
    }

    public HTCoordinationDAOConnectionFactory getDaoConnectionFactory() {
        return daoConnectionFactory;
    }

    private ThreadFactory createThreadFactory() {
        return new ThreadFactory() {
            private int threadNumber = 0;

            public Thread newThread(Runnable r) {
                threadNumber += 1;
                Thread t = new Thread(r, "B4PCoordination-" + threadNumber);
                t.setDaemon(true);
                return t;
            }
        };
    }

    public void runTask(CoordinationTask task) {
        this.executorService.submit(task);
    }
}
