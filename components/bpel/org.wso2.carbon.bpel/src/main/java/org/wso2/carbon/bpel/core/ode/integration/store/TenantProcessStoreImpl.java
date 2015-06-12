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

import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.compiler.api.CompilationException;
import org.apache.ode.bpel.dd.DeployDocument;
import org.apache.ode.bpel.dd.TBAMServerProfiles;
import org.apache.ode.bpel.dd.TDeployment;
import org.apache.ode.bpel.dd.TProvide;
import org.apache.ode.bpel.engine.BpelServerImpl;
import org.apache.ode.bpel.iapi.ContextException;
import org.apache.ode.bpel.iapi.ProcessConf;
import org.apache.ode.bpel.iapi.ProcessState;
import org.apache.ode.store.DeploymentUnitDAO;
import org.apache.ode.store.DeploymentUnitDir;
import org.apache.ode.store.ProcessConfDAO;
import org.wso2.carbon.application.deployer.AppDeployerUtils;
import org.wso2.carbon.bpel.common.config.EndpointConfiguration;
import org.wso2.carbon.bpel.core.BPELConstants;
import org.wso2.carbon.bpel.core.internal.BPELServiceComponent;
import org.wso2.carbon.bpel.core.ode.integration.BPELServerImpl;
import org.wso2.carbon.bpel.core.ode.integration.config.bam.BAMServerProfile;
import org.wso2.carbon.bpel.core.ode.integration.config.bam.BAMServerProfileBuilder;
import org.wso2.carbon.bpel.core.ode.integration.store.clustering.BPELProcessStateChangedCommand;
import org.wso2.carbon.bpel.core.ode.integration.store.repository.BPELPackageInfo;
import org.wso2.carbon.bpel.core.ode.integration.store.repository.BPELPackageRepository;
import org.wso2.carbon.bpel.core.ode.integration.store.repository.BPELPackageRepositoryUtils;
import org.wso2.carbon.bpel.skeleton.ode.integration.mgt.services.ProcessManagementException;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.config.RegistryContext;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.utils.RegistryClientUtils;
import org.wso2.carbon.unifiedendpoint.core.UnifiedEndpointConstants;
import org.wso2.carbon.utils.FileManipulator;

import javax.xml.namespace.QName;
import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Tenant specific process store.
 * Contains all the information about tenant's BPEL packages and processes.
 */
public class TenantProcessStoreImpl implements TenantProcessStore {
    private static final Log log = LogFactory.getLog(TenantProcessStoreImpl.class);
    private static final Log deploymentLog = LogFactory.getLog(BPELConstants.LOGGER_DEPLOYMENT);

    // ID of the tenant who owns this process store
    private Integer tenantId;

    // Tenant's Configuration context
    private ConfigurationContext tenantConfigContext;

    // Tenant's configuration registry
    private Registry tenantConfigRegistry;

    // Parent process store
    private ProcessStoreImpl parentProcessStore;

    // BPEL Deployment units available for this tenant
    private final Map<String, DeploymentUnitDir> deploymentUnits =
            new ConcurrentHashMap<String, DeploymentUnitDir>();

    // BPEL Processes in this tenant
    private final Map<QName, ProcessConfigurationImpl> processConfigMap =
            new ConcurrentHashMap<QName, ProcessConfigurationImpl>();

    private final Map<String, List<QName>> processesInDeploymentUnit =
            new ConcurrentHashMap<String, List<QName>>();

    private BPELPackageRepository repository;

    // Tenant's BPEL deployment unit repository
    private File bpelDURepo;

    // Axis2 bpel deployment repository where we put BPEL archive artifacts
    private File bpelArchiveRepo;

    // Holds the BAM server profiles
    private final Map<String, BAMServerProfile> bamProfiles =
            new ConcurrentHashMap<String, BAMServerProfile>();

    private final Map<String, Object> dataPublisherMap =
            new ConcurrentHashMap<String, Object>();


    public TenantProcessStoreImpl(ConfigurationContext configContext, ProcessStoreImpl parent)
            throws RegistryException {
        tenantId  = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        tenantConfigContext = configContext;

        tenantConfigRegistry =
                BPELServiceComponent.getRegistryService().getConfigSystemRegistry(tenantId);
        parentProcessStore = parent;
    }

    public void init() throws Exception {

        bpelDURepo = new File(parentProcessStore.getLocalDeploymentUnitRepo(), tenantId.toString());
        if (!bpelDURepo.exists() && !bpelDURepo.mkdirs()) {
            log.warn("Cannot create tenant " + tenantId + " BPEL deployment unit repository.");
        }
        repository = new BPELPackageRepository(tenantConfigRegistry, bpelDURepo, bpelArchiveRepo);
        repository.init();
    }

    public void handleNewBPELPackageDeploymentNotification(String bpelPackageName) {
        // Copy archive to file system
        // Write the deployment logic to handle that.

        try {
            repository.restoreBPELArchive(repository.getBPELPackageInfo(
                    BPELConstants.REG_PATH_OF_BPEL_PACKAGES +
                            bpelPackageName));
        } catch (Exception e) {
            log.error("Error occurred while deploying: " + bpelPackageName, e);
        }
    }

    public void handleBPELPackageUndeploymentNotification(String bpelPackageName,
                                                          List<String> versionsOfPackage) {
        updateLocalInstanceWithUndeployment(bpelPackageName, versionsOfPackage);
    }

    public void handleBPELProcessStateChangedNotification(QName pid, ProcessState processState) {
        if (log.isDebugEnabled()) {
            log.debug("Changing state of the process " + pid + " to " +
                    processState);
        }
        if (!isProcessExist(pid)) {
            String errMsg = "Process " + pid + " not found. Process state change failed.";
            log.error(errMsg);
            return;
        }

        if (processState == null) {
            String errMessage = "Process State cannot be null. Process state change failed";
            log.error(errMessage);
            return;
        }

        parentProcessStore.updateLocalInstanceWithStateChange(pid, processState);
    }

    /**
     * Deploy processes in given BPEL archive in ODE.
     * <p/>
     * Deployment flow:
     * - Get the current version from ODE(This version number is global to ODE.)
     * - Create the deployment context(@see BPELDeploymentContext)
     * - Check for existing BPEL archives with the same name(use when updating BPEL packages)
     * - If this is a new deployment or update, extract the archive and deploy processes
     * - If this is a reload(at startup) handle the reload.
     * <p/>
     * Process Versioning:
     * Process version is a single, sequentially incremented number. All deployed packages share
     * the same sequence. All processes in a bundle share the same version number and it's
     * the number of their bundle.
     *
     * @param bpelArchive BPEL package directory
     * @throws RegistryException
     */
    public void deploy(File bpelArchive) throws Exception {

        log.info("Deploying BPEL archive: " + bpelArchive.getName());

        long versionForThisDeployment = parentProcessStore.getCurrentVersion();

        BPELDeploymentContext deploymentContext =
                new BPELDeploymentContext(
                        tenantId,
                        parentProcessStore.getLocalDeploymentUnitRepo().getAbsolutePath(),
                        bpelArchive,
                        versionForThisDeployment);
        boolean isExistingPackage = repository.isExistingBPELPackage(deploymentContext);
        boolean isLoadOnly = repository.isBPELPackageReload(deploymentContext);

        deploymentContext.setExistingPackage(isExistingPackage);

        if (deploymentLog.isDebugEnabled()) {
            if (isExistingPackage) {
                deploymentLog.debug("Package: " + deploymentContext.getBpelPackageName() +
                        " is already available");
            } else {
                deploymentLog.debug("Package: " + deploymentContext.getBpelPackageName() +
                        " is a new deployment");
            }

            if (isLoadOnly) {
                deploymentLog.debug("Package: " + deploymentContext.getBpelPackageName() +
                        " has already deployed. Therefore the package is reloaded");
            } else {
                deploymentLog.debug("Package: " + deploymentContext.getBpelPackageName() +
                        " is found as a new deployment");
            }

        }

        if (isExistingPackage && isLoadOnly) {
            reloadExistingVersionsOfBPELPackage(deploymentContext);
            // attach this bpel archive with cApp
/*            attachWithCapp(deploymentContext.getArchiveName(),
                    deploymentContext.getBpelPackageName(), tenantId);*/
            return; // Once we finish reloading exit from the normal flow.
            // Else this is a update of existing BPEL package
        }

        if (isConfigRegistryReadOnly()) {
            log.warn("This node seems to be a slave, since the configuration registry is in read-only mode, " +
                    "hence processes cannot be directly deployed in this node. Please deploy the process in Master node first.");
            return;
        }

        try {
            Utils.extractBPELArchive(deploymentContext);
        } catch (Exception exception) {
            String errMsg = "Error extracting BPEL archive " + deploymentContext.getBpelArchive()
                    + ".";
            deploymentContext.setDeploymentFailureCause(errMsg);
            deploymentContext.setStackTrace(exception);
            deploymentContext.setFailed(true);
            handleDeploymentError(deploymentContext);
            throw exception;
        }

        if (!validateBPELPackage(deploymentContext, isExistingPackage)) {
            deploymentContext.setFailed(true);
            handleDeploymentError(deploymentContext);
            return; // Exist from the normal flow on BPEL package validation error.
        }

        deployBPELPackageInODE(deploymentContext);

        if (isExistingPackage) {
            repository.handleBPELPackageUpdate(deploymentContext);
        } else {
            repository.handleNewBPELPackageAddition(deploymentContext);
        }

        // attach this bpel archive with cApp
/*        attachWithCapp(deploymentContext.getArchiveName(),
                deploymentContext.getBpelPackageName(), tenantId);*/

        // We should use the deployment synchronizer instead of the below code
//        parentProcessStore.sendProcessDeploymentNotificationsToCluster(
//                new NewBPELPackageDeployedCommand(deploymentContext.getBpelPackageName(), tenantId),
//                configurationContext);

    }

    private boolean isConfigRegistryReadOnly() {
        RegistryContext context = tenantConfigRegistry.getRegistryContext();
        try {
            if (context != null) {
                return context.isReadOnly();
            }
        } catch (Exception e) {
            log.error("An error occurred while obtaining registry instance", e);
        }
        return false;
    }

    /**
     * Undeploying BPEL package.
     *
     * @param bpelPackageName Name of the BPEL package which going to be undeployed
     */
    public void undeploy(String bpelPackageName)
            throws RegistryException, BPELUIException {

        if (log.isDebugEnabled()) {
            log.debug("Un-deploying BPEL package " + bpelPackageName + " ....");
        }

        if (!repository.isExistingBPELPackage(bpelPackageName)) {
            // This can be a situation where we un-deploy the archive through management console,
            // so that, the archive is deleted from the repo. As a result this method get invoked.
            // to handle this case we just log the message but does not throw an exception.
            final String warningMsg = "Cannot find BPEL package with name " + bpelPackageName +
                    " in the repository. If the bpel package is un-deployed through the management" +
                    " console or if this node is a member of a cluster, please ignore this warning.";

            if(isConfigRegistryReadOnly()) {
                // This is for the deployment synchronizer scenarios where package un-deployment on a worker node
                // has to remove the deployed bpel package from the memory and remove associated services
                handleUndeployOnSlaveNode(bpelPackageName);

            } else {
                log.warn(warningMsg);
            }
            return;
        }

        if (repository.isExistingBPELPackage(bpelPackageName) && isConfigRegistryReadOnly()) {
            log.warn("This node seems to be a slave, since the configuration registry is in read-only mode, hence processes cannot be directly undeployed from this node. Please undeploy the process in Master node first.");
            return;
        }

        List<String> versionsOfThePackage;

        try {
            versionsOfThePackage = repository.getAllVersionsForPackage(bpelPackageName);
        } catch (RegistryException re) {
            String errMessage = "Cannot get all versions of the package " + bpelPackageName
                    + " from registry.";
            log.error(errMessage);
            throw re;
        }

        //check the instance count to be deleted
        long instanceCount = getInstanceCountForPackage(versionsOfThePackage);
        if (instanceCount > BPELServerImpl.getInstance().getBpelServerConfiguration().getBpelInstanceDeletionLimit()) {
            throw new BPELUIException("Instance deletion limit reached.");
        }

        for (String nameWithVersion : versionsOfThePackage) {
            parentProcessStore.deleteDeploymentUnitDataFromDB(nameWithVersion);
            Utils.deleteInstances(getProcessesInPackage(nameWithVersion));

            //location for extracted BPEL package
            String bpelPackageLocation = parentProcessStore.getLocalDeploymentUnitRepo().getAbsolutePath() + File.separator + tenantId + File.separator +
                    nameWithVersion;
            File bpelPackage = new File(bpelPackageLocation);
            //removing extracted bpel package at repository/bpel/0/
            deleteBpelPackageFromRepo(bpelPackage);

            for (QName pid : getProcessesInPackage(nameWithVersion)) {
                ProcessConfigurationImpl processConf =
                        (ProcessConfigurationImpl) getProcessConfiguration(pid);
                // This property is read when we removing the axis service for this process.
                // So that we can decide whether we should persist service QOS configs
                processConf.setUndeploying(true);
            }
        }

        try {
            repository.handleBPELPackageUndeploy(bpelPackageName);
        } catch (RegistryException re) {
            String errMessage = "Cannot update the BPEL package repository for undeployment of" +
                    "package " + bpelPackageName + ".";
            log.error(errMessage);
            throw re;
        }

        updateLocalInstanceWithUndeployment(bpelPackageName, versionsOfThePackage);

        // We should use the deployment synchronizer, instead of the code below.
//        parentProcessStore.sendProcessDeploymentNotificationsToCluster(
//                new BPELPackageUndeployedCommand(versionsOfThePackage, bpelPackageName, tenantId),
//                configurationContext);
    }

    private int getInstanceCountForPackage(List<String> versionsOfThePackage) {
        int count = 0;
        for (String versionName : versionsOfThePackage ) {
            count += Utils.getInstanceCountForProcess(getProcessesInPackage(versionName));
        }
        return count;
    }

    /**
     *  Undeployment scenario in a worker node( Slave ) in the clustered setup
     *  When the BPELDeployer get called for undeploying the bpel package, following has already taken place.
     *  The package information stored in the registry as well as the zip archive is deleted
     *  Process, Instance information have been removed from the ODE database
     *  However, on the slave node, the bpel process and the web services associated with the bpel process
     *  is still in memory. We need to unload the bpel process and the associated web services
     * @param bpelPackageName bpel package name
     * @return
     *
     */
    private int handleUndeployOnSlaveNode( String bpelPackageName) {
        List<String> packageList = findMatchingProcessByPackageName(bpelPackageName);
        if(packageList.size() < 1) {
            log.debug("Handling un-deploy operation on salve (worker) node : package list is empty");
            return -1;
        }

        for(String packageName : packageList) {
            //location for extracted BPEL package
            String bpelPackageLocation = parentProcessStore.getLocalDeploymentUnitRepo().getAbsolutePath() + File.separator + tenantId + File.separator +
                    packageName;
            File bpelPackage = new File(bpelPackageLocation);
            //removing extracted bpel package at repository/bpel/tenantID/
            deleteBpelPackageFromRepo(bpelPackage);

            for (QName pid : getProcessesInPackage(packageName)) {
                ProcessConfigurationImpl processConf =
                        (ProcessConfigurationImpl) getProcessConfiguration(pid);
                // This property is read when we removing the axis service for this process.
                // So that we can decide whether we should persist service QOS configs
                processConf.setUndeploying(true);
            }
        }

        Collection<QName> undeployedProcesses = new ArrayList<QName>();

        for (String nameWithVersion : packageList) {
            undeploySpecificVersionOfBPELPackage(nameWithVersion, undeployedProcesses);
        }

        BPELServerImpl instance = BPELServerImpl.getInstance();
        BpelServerImpl odeBpelServer = instance.getODEBPELServer();

        for (QName pid : undeployedProcesses) {
            odeBpelServer.unregister(pid);
            ProcessConf pConf = parentProcessStore.getProcessConfiguration(pid);
            if(pConf != null) {
                if (log.isDebugEnabled()) {
                    log.debug("Cancelling all cron scheduled jobs for process " + pid);
                }
                odeBpelServer.getContexts().cronScheduler.cancelProcessCronJobs(
                        pid, true);
            }
            log.info("Process " + pid + " un-deployed.");
        }

        parentProcessStore.updateProcessAndDUMapsForSalve(tenantId, bpelPackageName, undeployedProcesses);
        return 0;
    }




    private List<String> findMatchingProcessByPackageName(String packageName) {
        List<String> stringList = new ArrayList<String>();
        Set<String> strings = processesInDeploymentUnit.keySet();
        String regexPattern = packageName + "-(\\d*)";
        Pattern pattern = Pattern.compile(regexPattern);
        Iterator<String> iterator = strings.iterator();
        while (iterator.hasNext()) {
            String next = iterator.next();
            Matcher matcher= pattern.matcher(next);
            if(matcher.matches()) {
                stringList.add(next);
            }
        }
        return stringList;
    }

    private void deleteBpelPackageFromRepo(File bpelPackage) {
        log.info("Undeploying BPEL package. " + "Deleting " + bpelPackage + " BPEL package");

        if (bpelPackage.exists()) {
            FileManipulator.deleteDir(bpelPackage);
        } else {
            log.warn("BPEL package " + bpelPackage.getAbsolutePath() +
                    " not found. This can happen if you delete " +
                    "the BPEL package from the file system.");
        }
    }

    /**
     * Update the local instance of the BPS server regarding the undeployment of the bpel package.
     *
     * @param bpelPackageName      Name of the BPEL package
     * @param versionsOfThePackage List of deployed versions of the package
     */
    public void updateLocalInstanceWithUndeployment(String bpelPackageName,
                                                    List<String> versionsOfThePackage) {
        // Delete the package from the file repository
        deleteBpelArchive(bpelPackageName);
        Collection<QName> undeployedProcesses = new ArrayList<QName>();

        for (String nameWithVersion : versionsOfThePackage) {
            undeploySpecificVersionOfBPELPackage(nameWithVersion, undeployedProcesses);
        }

        parentProcessStore.updateMapsAndFireStateChangeEventsForUndeployedProcesses(tenantId,
                bpelPackageName, undeployedProcesses);
    }

    /**
     * Delete BPEL Archive from the BPEL repository of the file system.
     *
     * @param bpelPackageName Name of the BPEL package
     */
    private void deleteBpelArchive(String bpelPackageName) {
        String bpelArchiveLocation = tenantConfigContext.getAxisConfiguration().getRepository().
                getPath() + File.separator + BPELConstants.BPEL_REPO_DIRECTORY + File.separator +
                bpelPackageName + "." + BPELConstants.BPEL_PACKAGE_EXTENSION;
        log.info("Undeploying BPEL package " + bpelPackageName + ". Deleting BPEL archive " +
                bpelArchiveLocation + "....");
        File bpelArchive = new File(bpelArchiveLocation);
        if (bpelArchive.exists()) {
            if (!bpelArchive.delete()) {
                //For windows
                bpelArchive.deleteOnExit();
            }
        } else {
            log.warn("BPEL archive " + bpelArchive.getAbsolutePath() +
                    " not found. This can happen if you delete " +
                    "the BPEL archive from the file system.");
        }
    }

    private void undeploySpecificVersionOfBPELPackage(final String packageName,
                                                      final Collection<QName> undeployedProcesses) {
        DeploymentUnitDir du = deploymentUnits.remove(packageName);
        processesInDeploymentUnit.remove(packageName);
        if (du != null) {
            long version = du.getVersion();
            for (QName name : du.getProcessNames()) {
                QName pid = Utils.toPid(name, version);
                undeployedProcesses.add(pid);
            }
        }
    }

    public void handleTenantUnload() {
    }

    public void hydrate() {
    }

    public ProcessConf getProcessConfiguration(QName pid) {
        return processConfigMap.get(pid);
    }

    public void setState(QName pid, ProcessState processState)
            throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("Changing state of the process " + pid + " to " + processState);
        }
        if (!isProcessExist(pid)) {
            String errMsg = "Process " + pid + " not found.";
            log.error(errMsg);
            // TODO : Introduce hierarchical exceptions to ODE integration layer.
            throw new Exception(errMsg);
        }

        if (processState == null) {
            String errMessage = "Process State cannot be null.";
            log.error(errMessage);
            throw new Exception(errMessage);
        }

        parentProcessStore.setState(pid, processState);

        parentProcessStore.sendProcessDeploymentNotificationsToCluster(
                new BPELProcessStateChangedCommand(pid, processState, tenantId));
    }

    private Boolean isProcessExist(QName pid) {
        return processConfigMap.containsKey(pid);
    }

    public BPELPackageRepository getBPELPackageRepository() {
        return repository;
    }

    public Map<QName, ProcessConfigurationImpl> getProcessConfigMap() {
        return processConfigMap;
    }

    public ProcessConf removeProcessConfiguration(QName pid) {
        return processConfigMap.remove(pid);
    }

    public List<QName> getProcessesInPackage(String packageName) {
        List<QName> processes = processesInDeploymentUnit.get(packageName);
        if (processes == null) {
            processes = Collections.EMPTY_LIST;
        }
        return processes;
    }

    public Boolean containsProcess(QName pid) {
        return processConfigMap.containsKey(pid);
    }

    public void setBpelArchiveRepo(File bpelArchiveRepo) {
        this.bpelArchiveRepo = bpelArchiveRepo;
    }

    /**
     * Log and store the information to registry on BPEL deployment error.
     *
     * @param deploymentContext information about current deployment
     * @throws RegistryException on error accessing registry for persisting information.
     */
    private void handleDeploymentError(BPELDeploymentContext deploymentContext)
            throws Exception {
        if (deploymentContext.getStackTrace() != null) {
            log.error(deploymentContext.getDeploymentFailureCause(),
                    deploymentContext.getStackTrace());
        } else {
            log.error(deploymentContext.getDeploymentFailureCause());
        }
        // Stop writing the error condition to the registry.
        //repository.handleBPELPackageDeploymentError(deploymentContext);
    }

    /**
     * Reload old versions of BPEL package. This is used to handle restart of BPEL server.
     * At restart based on the last modified time of the BPEL archives we'll reload all the versions
     * of that BPEL archive.
     *
     * @param deploymentContext information about current deployment
     * @throws RegistryException on error loading resources from registry.
     * @throws org.wso2.carbon.bpel.skeleton.ode.integration.mgt.services.ProcessManagementException
     *
     */
    private void reloadExistingVersionsOfBPELPackage(BPELDeploymentContext deploymentContext)
            throws RegistryException, ProcessManagementException {
        BPELPackageInfo bpelPackage = repository.getBPELPackageInfo(deploymentContext);
        for (String packageName : bpelPackage.getAvailableVersions()) {
            if (!deploymentUnits.containsKey(packageName)) {
                loadExistingBPELPackage(packageName);
            }
        }
    }

    /**
     * Deploy BPEL package in ODE and add process configuration objects to necessary maps in process
     * store.
     *
     * @param deploymentContext information about current deployment
     * @throws Exception in case of duplicate deployment unit or if error occurred during deploying package in ODE
     */
    private void deployBPELPackageInODE(BPELDeploymentContext deploymentContext) throws Exception {
        File bpelPackage = deploymentContext.getBPELPackageContent();
        log.info("Starting deployment of processes from directory "
                + bpelPackage.getAbsolutePath());

        final Date deployDate = new Date();
        // Create the DU and compile/scan it before doing any other work.
        final DeploymentUnitDir du = new DeploymentUnitDir(bpelPackage);
        // Before coming to this stage, we create the bpel package directory with the static version
        // so we don't need to get the version from database. We can directly use static version
        // calculated from bpel package directory name.
        du.setVersion(du.getStaticVersion());

        try {
            du.compile();
        } catch (CompilationException ce) {
            String logMessage = "Deployment failed due to compilation issues. " + ce.getMessage();
            log.error(logMessage, ce);
            deploymentContext.setFailed(true);
            deploymentContext.setDeploymentFailureCause(logMessage);
            deploymentContext.setStackTrace(ce);
            handleDeploymentError(deploymentContext);
            throw new BPELDeploymentException(logMessage, ce);
        }

        du.scan();
        DeployDocument dd = du.getDeploymentDescriptor();
        List<ProcessConfigurationImpl> processConfs = new ArrayList<ProcessConfigurationImpl>();
        List<QName> processIds = new ArrayList<QName>();

        if (deploymentUnits.containsKey(du.getName())) {
            String logMessage = "Aborting deployment. Duplicate Deployment unit "
                    + du.getName() + ".";
            log.error(logMessage);
            deploymentContext.setFailed(true);
            deploymentContext.setDeploymentFailureCause(logMessage);
            handleDeploymentError(deploymentContext);
            throw new BPELDeploymentException(logMessage);
        }

        // Validate BPEL package partially before retiring old versions.
        validateBPELPackage(du);

        if (deploymentContext.isExistingPackage()) {
            reloadExistingVersionsOfBPELPackage(deploymentContext);
        }

        // Before updating a BPEL package we need to retire processes in old version
        retirePreviousPackageVersions(du);

        for (TDeployment.Process processDD : dd.getDeploy().getProcessList()) {
            QName processId = Utils.toPid(processDD.getName(), du.getVersion());

            ProcessConfigurationImpl processConf = new ProcessConfigurationImpl(
                    tenantId,
                    processDD,
                    du,
                    deployDate,
                    parentProcessStore.getEndpointReferenceContext(),
                    tenantConfigContext);
            processConf.setAbsolutePathForBpelArchive(deploymentContext.getBpelArchive().getAbsolutePath());
            processIds.add(processId);
            processConfs.add(processConf);

            readBAMServerProfiles(processDD, du);
        }

        deploymentUnits.put(du.getName(), du);
        processesInDeploymentUnit.put(du.getName(), processIds);
        for (ProcessConfigurationImpl processConf : processConfs) {
            processConfigMap.put(processConf.getProcessId(), processConf);
            deploymentContext.addProcessId(processConf.getProcessId());
        }


        try {
            parentProcessStore.onBPELPackageDeployment(
                    tenantId,
                    du.getName(),
                    BPELPackageRepositoryUtils.getResourcePathForBPELPackageContent(deploymentContext),
                    processConfs);
        } catch (ContextException ce) {
            deploymentContext.setDeploymentFailureCause("BPEL Package deployment failed at " +
                    "ODE layer. Possible cause: " + ce.getMessage());
            deploymentContext.setStackTrace(ce);
            deploymentContext.setFailed(true);
            handleDeploymentError(deploymentContext);
            throw ce;
        }
    }

    /**
     * Check whether processes in this package are already available in the process store or check
     * whether processes are correctly compiled.
     *
     * @param du BPEL deployment unit
     * @throws BPELDeploymentException if there's a error in BPEL package
     */
    private void validateBPELPackage(DeploymentUnitDir du)
            throws BPELDeploymentException {
        DeployDocument dd = du.getDeploymentDescriptor();
        for (TDeployment.Process processDD : dd.getDeploy().getProcessList()) {
            QName processId = Utils.toPid(processDD.getName(), du.getVersion());
            if (processConfigMap.containsKey(processId)) {
                String logMessage = "Aborting deployment. Duplicate process ID " + processId + ".";
                log.error(logMessage);
                throw new BPELDeploymentException(logMessage);
            }

            QName processType = Utils.getProcessType(processDD);

            DeploymentUnitDir.CBPInfo cbpInfo = du.getCBPInfo(processType);
            if (cbpInfo == null) {
                //removeDeploymentArtifacts(deploymentContext, du);
                String logMessage = "Aborting deployment. Cannot find Process definition for type "
                        + processType + ".";
                log.error(logMessage);
                throw new BPELDeploymentException(logMessage);
            }
        }
    }

    private void handleDeploymentErrorsAtODELayer(BPELDeploymentContext deploymentContext,
                                                  String duName) {
        deploymentUnits.remove(duName);
        processesInDeploymentUnit.remove(duName);
        for (QName pid : deploymentContext.getProcessIdsForCurrentDeployment()) {
            processConfigMap.remove(pid);
        }

    }

    private void loadExistingBPELPackage(String bpelPackageName) throws RegistryException, ProcessManagementException, BPELDeploymentException {
        DeploymentUnitDAO duDAO = parentProcessStore.getDeploymentUnitDAO(bpelPackageName);
        if (duDAO == null) {
            String errMsg = "Cannot find DeploymentUnitDAO instance for package "
                    + bpelPackageName + ".";
            log.error(errMsg);
            throw new BPELDeploymentException(errMsg);
        }
        File bpelPackage = findBPELPackageInFileSystem(duDAO);
        if (bpelPackage == null || !bpelPackage.exists()) {
            throw new BPELDeploymentException("Deployed directory " +
                    bpelPackage + " no longer there!");
        }

        DeploymentUnitDir du = new DeploymentUnitDir(bpelPackage);
        du.setVersion(du.getStaticVersion());
        du.scan();

        List<ProcessConfigurationImpl> loaded = new ArrayList<ProcessConfigurationImpl>();
        List<QName> processIds = new ArrayList<QName>();

        for (ProcessConfDAO pConfDAO : duDAO.getProcesses()) {
            TDeployment.Process processDD = du.getProcessDeployInfo(pConfDAO.getType());
            if (processDD == null) {
                log.warn("Cannot load " + pConfDAO.getPID() + "; cannot find descriptor.");
                continue;
            }

            // TODO: update the props based on the values in the DB.

            ProcessConfigurationImpl pConf = new ProcessConfigurationImpl(
                    tenantId,
                    processDD,
                    du,
                    duDAO.getDeployDate(),
                    parentProcessStore.getEndpointReferenceContext(),
                    tenantConfigContext);
            pConf.setAbsolutePathForBpelArchive(bpelPackage.getAbsolutePath());
            pConf.setState(pConfDAO.getState());
            processIds.add(pConfDAO.getPID());
            // if the deployment descriptor is updated at runtime, first load the updated data in
            // registry and use them with the specific process
            repository.readPropertiesOfUpdatedDeploymentInfo(pConf, bpelPackageName);
            readBAMServerProfiles(processDD, du);

            processConfigMap.put(pConf.getProcessId(), pConf);
            loaded.add(pConf);
        }

        deploymentUnits.put(du.getName(), du);
        processesInDeploymentUnit.put(du.getName(), processIds);
        parentProcessStore.onBPELPackageReload(tenantId, du.getName(), loaded);
    }

    private void readBAMServerProfiles(TDeployment.Process processDD, DeploymentUnitDir du){
        TBAMServerProfiles bamServerProfiles = processDD.getBamServerProfiles();
        if (bamServerProfiles != null) {
            for (TBAMServerProfiles.Profile bamServerProfile :
                    bamServerProfiles.getProfileList()) {
                String location = bamServerProfile.getLocation();

                if (location.startsWith(UnifiedEndpointConstants.VIRTUAL_FILE)) {
                    if (!EndpointConfiguration.isAbsolutePath(
                            location.substring(UnifiedEndpointConstants.VIRTUAL_FILE.length()))) {
                        location = EndpointConfiguration.getAbsolutePath(
                                du.getDeployDir().getAbsolutePath(),
                                location.substring(UnifiedEndpointConstants.VIRTUAL_FILE.length()));
                    }
                } else if((!location.startsWith(UnifiedEndpointConstants.VIRTUAL_CONF_REG) &&
                        !location.startsWith(UnifiedEndpointConstants.VIRTUAL_GOV_REG) &&
                        !location.startsWith(UnifiedEndpointConstants.VIRTUAL_REG))) {
                    if(EndpointConfiguration.isAbsolutePath(location)){
                        location = UnifiedEndpointConstants.VIRTUAL_FILE + location;
                    }else {
                        location = EndpointConfiguration.getAbsolutePath(du.getDeployDir().getAbsolutePath(), location);
                        location = UnifiedEndpointConstants.VIRTUAL_FILE + location;
                    }
                }
                BAMServerProfileBuilder builder = new BAMServerProfileBuilder(location, tenantId);
                BAMServerProfile profile = builder.build();
                addBAMServerProfile(profile.getName(), profile);
            }
        }
    }

    private File findBPELPackageInFileSystem(DeploymentUnitDAO dudao) {
        String duName = dudao.getName();
        // Done: Fix the logic to handle registry
        log.info("Looking for BPEL package in file system for deployment unit " + duName);

        File bpelDUDirectory = new File(bpelDURepo, duName);
        if (bpelDUDirectory.exists()) {
            return bpelDUDirectory;
        } else {
            String registryCollectionPath = dudao.getDeploymentUnitDir();
            try {
                if (tenantConfigRegistry.resourceExists(registryCollectionPath)) {
                    if (!bpelDUDirectory.exists() && !bpelDUDirectory.mkdirs()) {
                        String errMsg = "Error creating BPEL deployment unit repository for " +
                                "tenant " + tenantId;
                        log.error(errMsg);
                        log.error("Failed to load BPEL deployment unit " + duName +
                                " due to above error.");
                        throw new BPELDeploymentException(errMsg);
                    }

                    boolean deployedOnCarbon310 = false;
                    //Check whether the registry repo is of type carbon 3.1.0
                    if (tenantConfigRegistry.resourceExists(registryCollectionPath +
                            RegistryConstants.PATH_SEPARATOR + duName)) {
                        registryCollectionPath += RegistryConstants.PATH_SEPARATOR + duName;
                        deployedOnCarbon310 = true;
                        if (log.isDebugEnabled()) {
                            log.debug("Found a carbon 3.1.0 compatible deployment unit at " +
                                    registryCollectionPath);
                        }
                    }

                    RegistryClientUtils.exportFromRegistry(bpelDUDirectory, registryCollectionPath,
                            tenantConfigRegistry);

                    if (deployedOnCarbon310) {
                        if (log.isDebugEnabled()) {
                            log.debug("Recompiling the carbon 3.1.0 compatible deployment unit at "
                                    + bpelDUDirectory);
                        }
                        //Re-compiling to get rid of binary compatibility issues.
                        DeploymentUnitDir du = new DeploymentUnitDir(bpelDUDirectory);
                        for (File file : du.allFiles()) {
                            if (file.getAbsolutePath().endsWith(".cbp") && !file.delete()) {
                                log.warn("Unable to delete " + file);
                            }
                        }
                        du.compile();
                    }

                    return bpelDUDirectory;
                } else {
                    String errMsg = "Expected resource: " + registryCollectionPath +
                            " not found in the registry";
                    log.error(errMsg);
                    throw new BPELDeploymentException(errMsg);
                }
            } catch (RegistryException re) {
                String errMsg = "Error while exporting deployment unit: " + duName +
                        " to file system from the registry.";
                log.error(errMsg, re);
                throw new BPELDeploymentException(errMsg, re);
            }
        }
    }


    /**
     * Retire all the other versions of the same DU:
     * first take the DU name and insert version regexp,
     * than try to match the this string against names of already deployed DUs.
     * For instance if we are deploying DU "AbsenceRequest-2/AbsenceRequest.ode" and
     * there's already version 2 than regexp
     * "AbsenceRequest([-\\.](\d)+)?/AbsenceRequest.ode" will be matched against
     * "AbsenceRequest-2/AbsenceRequest.ode" and setRetirePackage() will be called accordingly.
     *
     * @param du DeploymentUnitDir object containing in-memory representation of BPEL package. 
     */
    private void retirePreviousPackageVersions(DeploymentUnitDir du) {
        //retire all the other versions of the same DU
        String[] nameParts = du.getName().split("/");
        /* Replace the version number (if any) with regexp to match any version number */
        nameParts[0] = nameParts[0].replaceAll("([-\\Q.\\E](\\d)+)?\\z", "");
        nameParts[0] += "([-\\Q.\\E](\\d)+)?";
        StringBuilder duNameRegExp = new StringBuilder(du.getName().length() * 2);
        for (int i = 0, n = nameParts.length; i < n; i++) {
            if (i > 0) {
                duNameRegExp.append("/");
            }
            duNameRegExp.append(nameParts[i]);
        }

        Pattern duNamePattern = Pattern.compile(duNameRegExp.toString());
        for (String deployedDUname : deploymentUnits.keySet()) {
            Matcher matcher = duNamePattern.matcher(deployedDUname);
            if (matcher.matches()) {
                parentProcessStore.setRetiredPackage(deployedDUname, true);
            }
        }
    }

    private boolean validateBPELPackage(BPELDeploymentContext bpelDeploymentContext,
                                        boolean isExistingPackage) {
        DeploymentUnitDir du;
        try {
            du = new DeploymentUnitDir(bpelDeploymentContext.getBPELPackageContent());
        } catch (IllegalArgumentException e) {
            bpelDeploymentContext.setDeploymentFailureCause(e.getMessage());
            bpelDeploymentContext.setStackTrace(e);
            return false;
        }

        if (!isExistingPackage) {
            DeployDocument deployDocument = du.getDeploymentDescriptor();
            List<TDeployment.Process> processList = deployDocument.getDeploy().getProcessList();
            for (TDeployment.Process process : processList) {
                List<TProvide> provideList = process.getProvideList();
                for (TProvide provide : provideList) {
                    if (getDeployedServices().containsKey(provide.getService().getName())) {
                        String errMsg = "Service: " + provide.getService().getName() + " already " +
                                "used by another process. Try again with a different " +
                                "service name";
                        bpelDeploymentContext.setDeploymentFailureCause(errMsg);
                        return false;
                    }
                }
            }
        }
        return true;

    }


    /**
     * Current bpel package can be coming from a cApp. If that is the case, we have to attach
     * this process with its owner cApp.
     *
     * @param bpelArchiveName - file name of the BPEL package
     * @param bpelPackageName - package name extracted out of the archive name
     * @param tenantId        - current tenant id
     */
    private void attachWithCapp(String bpelArchiveName, String bpelPackageName, int tenantId) {
        // attach with cApp
        AppDeployerUtils.attachArtifactToOwnerApp(bpelArchiveName,
                BPELConstants.BPEL_TYPE,
                bpelPackageName, tenantId);
    }

    public Map<QName, Object> getDeployedServices() {
        return parentProcessStore.getServicesPublishedByTenant(tenantId);
    }

    public void addBAMServerProfile(String name, BAMServerProfile profile) {
        bamProfiles.put(name, profile);
    }

    public BAMServerProfile getBAMServerProfile(String name) {
        return bamProfiles.get(name);
    }

    public synchronized void addDataPublisher(String processName, Object publisher) {
        dataPublisherMap.put(processName, publisher);
    }

    public Object getDataPublisher(String processName) {
        return dataPublisherMap.get(processName);
    }

    public Map getDataPublisherMap(){
        return dataPublisherMap;
    }

    public DeploymentUnitDir getDeploymentUnitDir(QName pid) {
        for (String du : processesInDeploymentUnit.keySet()) {
            if (processesInDeploymentUnit.get(du) != null) {
                if (processesInDeploymentUnit.get(du).contains(pid)) {
                    return deploymentUnits.get(du);
                }
            }
        }
        return null;
    }
}
