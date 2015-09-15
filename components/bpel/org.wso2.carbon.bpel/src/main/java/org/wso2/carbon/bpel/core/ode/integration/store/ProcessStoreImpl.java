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

package org.wso2.carbon.bpel.core.ode.integration.store;

import org.apache.axis2.AxisFault;
import org.apache.axis2.clustering.state.StateClusteringCommand;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.iapi.*;
import org.apache.ode.store.ConfStoreConnection;
import org.apache.ode.store.ConfStoreConnectionFactory;
import org.apache.ode.store.DeploymentUnitDAO;
import org.apache.ode.store.ProcessConfDAO;
import org.apache.ode.store.jpa.DbConfStoreConnectionFactory;
import org.apache.ode.utils.DOMUtils;
import org.apache.ode.utils.GUID;
import org.hsqldb.jdbc.jdbcDataSource;
import org.w3c.dom.Node;
import org.wso2.carbon.bpel.cluster.notifier.BPELClusterNotifier;
import org.wso2.carbon.bpel.core.ode.integration.ODEConfigurationProperties;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import javax.sql.DataSource;
import javax.xml.namespace.QName;
import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.concurrent.*;

/**
 * This class implements multi-tenancy supported BPEL Process Store .Multi-tenancy features are achieved
 * via TenantProcessStores and this process store implementation is composed out of tenant process stores.
 */
public class ProcessStoreImpl implements ProcessStore, MultiTenantProcessStore {
    private static final Log log = LogFactory.getLog(ProcessStoreImpl.class);

    static final Comparator<ProcessConfigurationImpl> BY_DEPLOYEDDATE =
            new Comparator<ProcessConfigurationImpl>() {
                public int compare(ProcessConfigurationImpl o1,
                                   ProcessConfigurationImpl o2) {
                    return o1.getDeployDate().compareTo(o2.getDeployDate());
                }
            };

    public static enum TenatProcessStoreState {
        // Resides in Memory
        ACTIVE,
        // Unloaded from memory
        INACTIVE
    }

    private Map<Integer, TenantProcessStore> tenantProcessStores =
            new ConcurrentHashMap<Integer, TenantProcessStore>();

    // Keeps the state of the tenant process store. Used to handle loading and unloading of tenants.
    // When tenant get unloaded, we are removing tenant process store from memory. When tenant loads again new
    // tenant process store will get created.
    // But we need to handle the list of processes and deployment units in this carefully to reflect these unloads.
    private Map<Integer, TenatProcessStoreState> tenantProcessStoreState =
            new ConcurrentHashMap<Integer, TenatProcessStoreState>();


    private CopyOnWriteArrayList<ProcessStoreListener> processStoreListeners =
            new CopyOnWriteArrayList<ProcessStoreListener>();

    private ConfStoreConnectionFactory connectionFactory;

    // Only keeps the names of the BPEL packages. Doesn't keep actual DeploymentUnit in
    // parent process store.
    private CopyOnWriteArrayList<String> deploymentUnits = new CopyOnWriteArrayList<String>();

    // Only keeps QNames of the processes. We keep process configuration in tenant process store.
    private CopyOnWriteArrayList<QName> processes = new CopyOnWriteArrayList<QName>();

    private Map<QName, Integer> processToTenantMap = new ConcurrentHashMap<QName, Integer>();

    private Map<String, Integer> deploymentUnitToTenantMap = new ConcurrentHashMap<String, Integer>();

    private Map<String, ArrayList<QName>> deploymentUnitToProcessesMap =
            new ConcurrentHashMap<String, ArrayList<QName>>();

    private Map<QName, String> processToDeploymentUnitMap =
            new ConcurrentHashMap<QName, String>();

    private Map<Integer, Map<QName, Object>> servicesPublishedByTenants = new ConcurrentHashMap<Integer, Map<QName, Object>>();

    private File bpelDURepo;

    private EndpointReferenceContext eprContext;

    //In-memory DataSource, or <code>null</code> if we are using a real DS.
    // We need this to shutdown the DB.
    private DataSource inMemDs;

    /**
     * Executor used to process DB transactions. Allows us to isolate the TX context, and to ensure
     * that only one TX gets executed a time. We don't really care to parallelize these operations
     * because: i) HSQL does not isolate transactions and we don't want to get confused ii) we're
     * already serializing all the operations with a read/write lock. iii) we don't care about
     * performance, these are infrequent operations.
     */
    private ExecutorService executor = Executors.newSingleThreadExecutor(new SimpleThreadFactory());


    public ProcessStoreImpl(EndpointReferenceContext eprContext,
                            DataSource ds,
                            ODEConfigurationProperties configurationProps) {
        this.eprContext = eprContext;
        if (ds != null) {
            connectionFactory = new DbConfStoreConnectionFactory(ds, false,
                    configurationProps.getTxFactoryClass());
        } else {
            // If the datasource is not provided, then we create a HSQL-based in-memory
            // database. Makes testing a bit simpler.
            String guid = new GUID().toString();
            DataSource hsqlds = createInternalDS(guid);
            connectionFactory = new DbConfStoreConnectionFactory(hsqlds, true,
                    configurationProps.getTxFactoryClass());
            inMemDs = hsqlds;
        }
    }

    public static DataSource createInternalDS(String guid) {
        jdbcDataSource hsqlds = new jdbcDataSource();
        hsqlds.setDatabase("jdbc:hsqldb:mem:" + guid);
        hsqlds.setUser("sa");
        hsqlds.setPassword("");
        return hsqlds;
    }

    public void shutdown() {
        if (inMemDs != null) {
            shutdownInternalDB(inMemDs);
            inMemDs = null;
        }

        if (executor != null) {
            executor.shutdown();
            executor = null;
        }
    }

    public static void shutdownInternalDB(DataSource ds) {
        Connection conn = null;
        try {
            conn = ds.getConnection();
            Statement stmt = conn.createStatement();
            try {
                stmt.execute("SHUTDOWN;");
            } finally {
                if (stmt != null) {
                    stmt.close();
                }
            }
        } catch (SQLException e) {
            log.error("Error shutting down data base.", e);
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException se) {
                log.warn("Unable to close the SQL connection.", se);
            }
        }
    }


    /////////////////////////////////////////////////////////////////////////
    //                                                                     //
    // This process store implementation is aware about the multi-tenancy. //
    // And this process store doesn't support normal deployment method     //
    // Supported by ODE's process store. All the package deployment        //
    // operations except store the configuration in persistence storage    //
    // is handled by TenantProcessStores.                                  //
    //                                                                     //
    /////////////////////////////////////////////////////////////////////////

    public Collection<QName> deploy(File file) {
        throw new UnsupportedOperationException("Operation deploy(File file) is not supported " +
                "multi-tenant aware process store.");
    }

    public Collection<QName> undeploy(File file) {
        throw new UnsupportedOperationException("Operation undeploy(File file) is not supported " +
                "multi-tenant aware process store.");
    }

    public void sendProcessDeploymentNotificationsToCluster(StateClusteringCommand command)
            throws AxisFault {
        BPELClusterNotifier.sendClusterNotification(command, this);
    }

    private void updateProcessAndDUMaps(Integer tenantId,
                                        String duName,
                                        Collection<QName> pids,
                                        boolean isDeploy) {
        if (isDeploy) {
            deploymentUnits.add(duName);
            for (QName pid : pids) {
                processes.add(pid);
            }
            deploymentUnitToTenantMap.put(duName, tenantId);
            populateProcessToTenatMap(tenantId, pids);
            populateDeploymentUnitToProcessMap(duName, pids);
        } else {
            for (QName pid : pids) {
                ProcessConfigurationImpl processConf =
                        (ProcessConfigurationImpl) getProcessConfiguration(pid);
                if (processConf != null && processConf.isUndeploying()) {
                    processToTenantMap.remove(pid);
                }
                removeProcessConfiguration(pid, tenantId);
            } // Moving to bindingcontextimpl to fix a issue when getting tenant id to deactivate service

            deploymentUnits.remove(duName);
            for (QName pid : pids) {
                processes.remove(pid);
            }
            deploymentUnitToTenantMap.remove(duName);
            deploymentUnitToProcessesMap.remove(duName);
        }
    }

    public void updateProcessAndDUMapsForSalve(Integer tenantId,
                                        String duName,
                                        Collection<QName> pids) {
        for (QName pid : pids) {
            ProcessConfigurationImpl processConf =
                    (ProcessConfigurationImpl) getProcessConfiguration(pid);
            if (processConf != null && processConf.isUndeploying()) {
                processToTenantMap.remove(pid);
            }
            removeProcessConfiguration(pid, tenantId);
        } // Moving to bindingcontextimpl to fix a issue when getting tenant id to deactivate service

        deploymentUnits.remove(duName);
        for (QName pid : pids) {
            processes.remove(pid);
        }
        deploymentUnitToTenantMap.remove(duName);
        deploymentUnitToProcessesMap.remove(duName);
    }

    public void removeFromProcessToTenantMap(QName pid) {
        processToTenantMap.remove(pid);
    }

    private void populateDeploymentUnitToProcessMap(String duName, Collection<QName> pids) {
        ArrayList<QName> processIds = new ArrayList<QName>();
        for (QName pid : pids) { //TODO processIds.addAll(pids);
            processIds.add(pid);
        }

        deploymentUnitToProcessesMap.put(duName, processIds);
    }

    private void populateProcessToTenatMap(Integer tenantId, Collection<QName> pids) {
        for (QName pid : pids) {
            processToTenantMap.put(pid, tenantId);
        }
    }

    public DeploymentUnitDAO getDeploymentUnitDAO(final String bpelPackageName) {
        DeploymentUnitDAO duDAO = exec(new Callable<DeploymentUnitDAO>() {
            @Override
            public DeploymentUnitDAO call(ConfStoreConnection conn) {
                return conn.getDeploymentUnit(bpelPackageName);
            }
        });

        if (duDAO == null) {
            // Null scenario should handle at tenant process store.
            log.warn("Cannot find deployment unit information on DB for deployment unit "
                    + bpelPackageName);
        }

        return duDAO;
    }

    public void onBPELPackageReload(Integer tenantId, String duName,
                                    List<ProcessConfigurationImpl> pConfs) {
        CopyOnWriteArrayList<QName> pids = new CopyOnWriteArrayList<QName>();
        for (ProcessConf pConf : pConfs) {
            pids.add(pConf.getProcessId());
        }
        updateProcessAndDUMaps(tenantId, duName, pids, true);

        Collections.sort(pConfs, BY_DEPLOYEDDATE);
        for (ProcessConfigurationImpl processConfiguration : pConfs) {
            try {
                if(log.isDebugEnabled()) {
                    log.debug("Firing state change event --" + processConfiguration.getState()  +
                              "--  for process conf " + processConfiguration.getPackage() +
                              "located at " + processConfiguration.getAbsolutePathForBpelArchive());
                }
                fireStateChange(processConfiguration.getProcessId(),
                        processConfiguration.getState(),
                        duName);
            } catch (Exception e) {
                log.error("Error while firing state change event for process "
                        + processConfiguration.getProcessId() + " in deployment unit "
                        + duName + ".");
            }
        }
    }

    public void onBPELPackageDeployment(Integer tenantId,
                                        final String duName,
                                        final String duLocation,
                                        final List<ProcessConfigurationImpl> processConfs) {
        boolean status = exec(new Callable<Boolean>() {
            @Override
            public Boolean call(ConfStoreConnection conn) {
                DeploymentUnitDAO duDao = conn.getDeploymentUnit(duName);
                if (duDao != null) {
                    /*
                    This is for clustering scenario. update/deployment
                     */
                    return true;
                }

                duDao = conn.createDeploymentUnit(duName);
                duDao.setDeploymentUnitDir(duLocation);

                for (ProcessConf pConf : processConfs) {
                    try {
                        ProcessConfDAO processConfDao = duDao.createProcess(pConf.getProcessId(),
                                pConf.getType(),
                                pConf.getVersion());
                        processConfDao.setState(pConf.getState());
                        for (Map.Entry<QName, Node> prop : pConf.getProcessProperties().entrySet()) {
                            processConfDao.setProperty(prop.getKey(),
                                    DOMUtils.domToString(prop.getValue()));
                        }
                        conn.setVersion(pConf.getVersion());
                    } catch (Exception e) {
                        String errmsg = "Error persisting deployment record for "
                                + pConf.getProcessId()
                                + "; process will not be available after restart!";
                        log.error(errmsg, e);
                        return false;
                    }
                }
                return true;
            }
        });

        if (status) {
            CopyOnWriteArrayList<QName> pids = new CopyOnWriteArrayList<QName>();
            for (ProcessConf pConf : processConfs) {
                pids.add(pConf.getProcessId());
            }
            updateProcessAndDUMaps(tenantId, duName, pids, true);

            for (ProcessConfigurationImpl processConf : processConfs) {
                fireEvent(new ProcessStoreEvent(ProcessStoreEvent.Type.DEPLOYED,
                        processConf.getProcessId(), duName));
                fireStateChange(processConf.getProcessId(), processConf.getState(), duName);
            }

        }
    }

    public void deleteDeploymentUnitDataFromDB(final String duName) {
        try {
            exec(new Callable<Boolean>() {
                public Boolean call(ConfStoreConnection conn) {
                    DeploymentUnitDAO duDao = conn.getDeploymentUnit(duName);
                    if (duDao != null) {
                        duDao.delete();
                    }
                    return true;
                }
            });

        } catch (Exception ex) {
            log.error("Error synchronizing with data store; " + duName
                    + " may be reappear after restart!");
        }
    }

    public void updateMapsAndFireStateChangeEventsForUndeployedProcesses(Integer tenantId,
                                                                         String duName,
                                                                         Collection<QName> pids) {
        for (QName pid : pids) {
            fireEvent(new ProcessStoreEvent(ProcessStoreEvent.Type.UNDEPLOYED, pid, duName));
            log.info("Process " + pid + " undeployed.");
        }
        updateProcessAndDUMaps(tenantId, duName, pids, false);
    }

    public Collection<String> getPackages() {
        return deploymentUnits;
    }

    public List<QName> listProcesses(String packageName) {
        return deploymentUnitToProcessesMap.get(packageName);
    }

    public List<QName> getProcesses() {
        return processes;
    }

    public ProcessConf getProcessConfiguration(QName pid) {
        Integer tenantId = processToTenantMap.get(pid);
        if (tenantId != null) {
            TenantProcessStore tenantProcessStore = tenantProcessStores.get(tenantId);
            if (tenantProcessStore != null) {
                return tenantProcessStore.getProcessConfiguration(pid);
            }
        }

        return null;
    }

    private void removeProcessConfiguration(QName pid, int tenantId) {
        TenantProcessStore tenantProcessStore = tenantProcessStores.get(tenantId);
        if (tenantProcessStore != null) {
            tenantProcessStore.removeProcessConfiguration(pid);
        }
    }

    public void registerListener(ProcessStoreListener processStoreListener) {
        if (log.isDebugEnabled()) {
            log.debug("Registering process store listner " + processStoreListener);
        }
        processStoreListeners.add(processStoreListener);
    }

    public void unregisterListener(ProcessStoreListener processStoreListener) {
        if (log.isDebugEnabled()) {
            log.debug("Unregistering process store listener " + processStoreListener);
        }
        processStoreListeners.remove(processStoreListener);
    }

    public void setProperty(final QName pid, final QName propName, final String value) {
        if (log.isDebugEnabled()) {
            log.debug("Setting property " + propName + " on process " + propName);
        }

        if (processes.indexOf(pid) == -1) {
            String errMsg = "Process " + pid + " not found.";
            log.error(errMsg);
            throw new ContextException(errMsg);
        }

        final String duName = getDeploymentUnitForProcess(pid);
        if (duName == null) {
            // This cannot happen if every thing in process store is in sync
            String errMsg = "Deployment unit for process " + pid + " not found.";
            log.error(errMsg);
            throw new ContextException(errMsg);
        }
        exec(new Callable<Object>() {
            public Object call(ConfStoreConnection conn) {
                DeploymentUnitDAO duDao = conn.getDeploymentUnit(duName);
                if (duDao == null) {
                    return null;
                }

                ProcessConfDAO pConfDao = duDao.getProcess(pid);
                if (pConfDao == null) {
                    return null;
                }
                pConfDao.setProperty(propName, value);
                return null;
            }
        });

        fireEvent(new ProcessStoreEvent(ProcessStoreEvent.Type.PROPERTY_CHANGED, pid, duName));
    }

    private String getDeploymentUnitForProcess(QName pid) {
        //////////////////////////////////////////////////
        // This implementation has poor performance. But this
        // method does not get called frequently. So ignore
        // the performance impact.
        //////////////////////////////////////////////////
        if ((processToDeploymentUnitMap.get(pid)) != null) {
            return processToDeploymentUnitMap.get(pid);
        }

        for (Map.Entry<String, ArrayList<QName>> entry : deploymentUnitToProcessesMap.entrySet()) {
            if (entry.getValue().contains(pid)) {
                processToDeploymentUnitMap.put(pid, entry.getKey());
                return entry.getKey();
            }
        }

        return null;
    }

    public void setProperty(final QName pid, final QName propName, final Node value) {
        setProperty(pid, propName, DOMUtils.domToStringLevel2(value));
    }

    public void setState(final QName pid, final ProcessState processState) {
        validateMethodParameters(pid, processState);
        final String duName = getDeploymentUnitForProcess(pid);
        validateDeploymentUnitForTheProcess(duName, pid);

        ProcessState old = exec(new Callable<ProcessState>() {
            public ProcessState call(ConfStoreConnection conn) {
                DeploymentUnitDAO duDao = conn.getDeploymentUnit(duName);
                if (duDao == null) {
                    String errMsg = "Deployment unit " + duName + " not found.";
                    log.error(errMsg);
                    throw new ContextException(errMsg);
                }

                ProcessConfDAO pConfDao = duDao.getProcess(pid);
                if (pConfDao == null) {
                    String errMsg = "Process " + pid + " not found in deployment unit " + duName +
                            ".";
                    log.error(errMsg);
                    throw new ContextException(errMsg);
                }

                ProcessState old = pConfDao.getState();
                pConfDao.setState(processState);
                return old;
            }
        });

        ProcessConfigurationImpl pConf = (ProcessConfigurationImpl) getProcessConfiguration(pid);
        pConf.setState(processState);

        if (old != null && !old.equals(processState)) {
            fireStateChange(pid, processState, duName);
        }
    }

    private void validateDeploymentUnitForTheProcess(String duName, QName pid) {
        if (duName == null) {
            // This cannot happen if every thing in process store is in sync
            String errMsg = "Deployment unit for process " + pid + " not found.";
            log.error(errMsg);
            throw new ContextException(errMsg);
        }
    }

    private void validateMethodParameters(QName pid, ProcessState processState) {
        if (log.isDebugEnabled()) {
            log.debug("Changing process state for " + pid + " to " + processState);
        }

        if (processState == null) {
            String errMessage = "Process State cannot be null.";
            log.error(errMessage);
            throw new ContextException(errMessage);
        }

        if (processes.indexOf(pid) == -1) {
            String errMsg = "Process " + pid + " not found.";
            log.error(errMsg);
            throw new ContextException(errMsg);
        }
    }

    public void updateLocalInstanceWithStateChange(QName pid, ProcessState processState) {
        validateMethodParameters(pid, processState);
        final String duName = getDeploymentUnitForProcess(pid);
        validateDeploymentUnitForTheProcess(duName, pid);
        ProcessConfigurationImpl pConf = (ProcessConfigurationImpl) getProcessConfiguration(pid);
        pConf.setState(processState);

        fireStateChange(pid, processState, duName);
    }

    public void setRetiredPackage(String packageName, boolean retired) {
        ArrayList<QName> processList = deploymentUnitToProcessesMap.get(packageName);
        if (processList == null) {
            throw new ContextException("Couldn't find the package " + packageName);
        }
        for (QName pid : processList) {
            setState(pid, retired ? ProcessState.RETIRED : ProcessState.ACTIVE);
        }
    }


    public long getCurrentVersion() {
        return exec(new Callable<Long>() {
            public Long call(ConfStoreConnection conn) {
                return conn.getNextVersion();
            }
        });
    }

    public void refreshSchedules(String packageName) {
        List<QName> pids = listProcesses(packageName);
        if (pids != null) {
            for (QName pid : pids) {
                fireEvent(new ProcessStoreEvent(ProcessStoreEvent.Type.SCHEDULE_SETTINGS_CHANGED,
                        pid, packageName));
            }
        }
    }

    public TenantProcessStore createProcessStoreForTenant(
            ConfigurationContext tenantConfigurationContext) {

        Integer tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();

        try {
            TenantProcessStore processStore = new TenantProcessStoreImpl(tenantConfigurationContext,
                    this);
            tenantProcessStores.put(tenantId, processStore);
            tenantProcessStoreState.put(tenantId, TenatProcessStoreState.ACTIVE);
            if (log.isDebugEnabled()) {
                log.debug("TenantProcessStore created for tenant " + tenantId + ".");
            }

            return processStore;
        } catch (RegistryException re) {
            log.error("Error getting configuration registry for the tenant " +
                    tenantId + ".");
            return null;
        }
    }

    public void unloadTenantProcessStore(Integer tenantId) {
        tenantProcessStoreState.put(tenantId, TenatProcessStoreState.INACTIVE);
        tenantProcessStores.remove(tenantId);
        removeServicesPublishedByTenat(tenantId);
    }

//    public void hydrateTenantProcessStore(String tenantId) {
//
//    }

    public void setLocalBPELDeploymentUnitRepo(File bpelDURepo) {
        this.bpelDURepo = bpelDURepo;
    }

    public File getLocalDeploymentUnitRepo() {
        return this.bpelDURepo;
    }

    public TenantProcessStore getTenantsProcessStore(Integer tenantId) {
        return tenantProcessStores.get(tenantId);
    }

    public Map<QName, Object> getServicesPublishedByTenant(Integer tenantId) {
        if (servicesPublishedByTenants.get(tenantId) == null) {
            servicesPublishedByTenants.put(tenantId, new ConcurrentHashMap<QName, Object>());
        }

        return servicesPublishedByTenants.get(tenantId);
    }

    public void removeServicesPublishedByTenat(Integer tenantId) {
        servicesPublishedByTenants.remove(tenantId);
    }

//    public void addServicePublishByTenant(Integer tenantId, QName serviceName) {
//        Map<QName, Object> services = servicesPublishedByTenants.get(tenantId);
//        if (services == null) {
//            services = new ConcurrentHashMap<QName, Object>();
//            services.put(serviceName, new Object());
//            servicesPublishedByTenants.put(tenantId, services);
//        } else {
//            services.put(serviceName, new Object());
//        }
//
//
//    }

    /**
     * Get tenant id of the given process
     *
     * @param pid QName of the process
     * @return Tenant id of the process
     */
    public Integer getTenantId(QName pid) {
        return processToTenantMap.get(pid);
    }

    public EndpointReferenceContext getEndpointReferenceContext() {
        return eprContext;
    }

    protected void fireStateChange(QName processId, ProcessState state, String duname) {
        switch (state) {
            case ACTIVE:
                fireEvent(new ProcessStoreEvent(ProcessStoreEvent.Type.ACTIVATED, processId,
                        duname));
                break;
            case DISABLED:
                fireEvent(new ProcessStoreEvent(ProcessStoreEvent.Type.DISABLED, processId,
                        duname));
                break;
            case RETIRED:
                fireEvent(new ProcessStoreEvent(ProcessStoreEvent.Type.RETIRED, processId,
                        duname));
                break;
        }

    }

    protected void fireEvent(ProcessStoreEvent pse) {
        if (log.isDebugEnabled()) {
            log.debug("firing event: " + pse);
        }
        for (ProcessStoreListener psl : processStoreListeners) {
            psl.onProcessStoreEvent(pse);
        }
    }

    abstract class Callable<V> implements java.util.concurrent.Callable<V> {
        public V call() {
            boolean success = false;
            // in JTA, transaction is bigger than the session
            connectionFactory.beginTransaction();
            ConfStoreConnection conn = getConnection();
            try {
                V r = call(conn);
                connectionFactory.commitTransaction();
                success = true;
                return r;
            } finally {
                if (!success) {
                    try {
                        connectionFactory.rollbackTransaction();
                    } catch (Exception ex) {
                        log.error("DbError", ex);
                    }
                }
            }
            // session is closed automatically when committed or rolled back under JTA
        }

        abstract V call(ConfStoreConnection conn);
    }

    private ConfStoreConnection getConnection() {
        return connectionFactory.getConnection();
    }

    /**
     * Execute database transactions in an isolated context.
     *
     * @param <T>      return type
     * @param callable transaction
     * @return T
     */
    synchronized <T> T exec(Callable<T> callable) {
        // We want to submit db jobs to an executor to isolate
        // them from the current thread,
        Future<T> future = executor.submit(callable);
        try {
            return future.get();
        } catch (Exception e) {
            throw new ContextException("DbError", e);
        }
    }

    private static class SimpleThreadFactory implements ThreadFactory {
        private int threadNumber = 0;

        public Thread newThread(Runnable r) {
            threadNumber += 1;
            Thread t = new Thread(r, "ProcessStoreImpl-" + threadNumber);
            t.setDaemon(true);
            return t;
        }
    }


}
