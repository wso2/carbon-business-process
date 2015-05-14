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

package org.wso2.carbon.humantask.core.store;

import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.deployment.DeploymentEngine;
import org.apache.axis2.deployment.util.Utils;
import org.apache.axis2.description.*;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ws.commons.schema.resolver.DefaultURIResolver;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.bpel.common.ServiceConfigurationUtil;
import org.wso2.carbon.bpel.common.config.EndpointConfiguration;
import org.wso2.carbon.humantask.TNotification;
import org.wso2.carbon.humantask.TTask;
import org.wso2.carbon.humantask.core.HumanTaskConstants;
import org.wso2.carbon.humantask.core.dao.DeploymentUnitDAO;
import org.wso2.carbon.humantask.core.dao.HumanTaskDAOConnection;
import org.wso2.carbon.humantask.core.dao.TaskDAO;
import org.wso2.carbon.humantask.core.dao.TaskPackageStatus;
import org.wso2.carbon.humantask.core.deployment.*;
import org.wso2.carbon.humantask.core.engine.HumanTaskEngine;
import org.wso2.carbon.humantask.core.engine.runtime.api.HumanTaskRuntimeException;
import org.wso2.carbon.humantask.core.integration.AxisHumanTaskMessageReceiver;
import org.wso2.carbon.humantask.core.integration.CallBackServiceImpl;
import org.wso2.carbon.humantask.core.integration.HumanTaskWSDLLocator;
import org.wso2.carbon.humantask.core.internal.HumanTaskServiceComponent;
import org.wso2.carbon.humantask.core.utils.HumanTaskStoreUtils;
import org.wso2.carbon.registry.core.config.RegistryContext;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.utils.FileManipulator;
import org.wso2.carbon.utils.ServerConstants;

import javax.cache.*;
import javax.persistence.EntityManager;
import javax.transaction.NotSupportedException;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;
import javax.wsdl.Definition;
import javax.wsdl.OperationType;
import javax.xml.namespace.QName;
import java.io.File;
import java.net.URI;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Manages human task deployments for a tenant. There will be HumanTaskStore per tenant. Handles the deployment
 * of human tasks, and management of deployed tasks.
 */
public class HumanTaskStore {
    private static final Log log = LogFactory.getLog(HumanTaskStore.class);

    private int tenantId;

    private ConfigurationContext configContext;

    private List<HumanTaskBaseConfiguration> taskConfigurations =
            new ArrayList<HumanTaskBaseConfiguration>();

    // HumanTaskConfiguration QName with HumanTaskBaseConfiguration
    // TaskConfiguration QName is constructed by appending the task version to local name
    private Map<QName, HumanTaskBaseConfiguration> taskBaseConfigurationHashMap =
            new HashMap<QName, HumanTaskBaseConfiguration>();


    // Store the current task task version string ( TaskPackageName +"-" version ) against taskpackagename
    private Map<String, String> loadedPackages = new HashMap<String, String>();

    // List of TaskConfiguration QNames vs versioned taskPackageName
    private Map<String, List<QName>> taskConfigurationsInTaskPackage = new HashMap<String, List<QName>>();

    private Map<QName, QName> activeTaskConfigurationQNameMap = new HashMap<QName, QName>();


    // This is the human task deployment repository

    private File humanTaskDeploymentRepo;

    private HumanTaskEngine engine;

    public HumanTaskStore(int tenantId, ConfigurationContext configContext) {
        this.tenantId = tenantId;
        this.configContext = configContext;
        this.engine = HumanTaskServiceComponent.getHumanTaskServer().getTaskEngine();

        if (HumanTaskServiceComponent.getHumanTaskServer().getServerConfig().isCachingEnabled()) {
            initializeCaches();
        }
    }

    /**
     * This will simply deploy the new task and will not perform removal of existing tasks, which should be done prior to
     * deploying this task
     * @param humanTaskDU
     * @return List of task configuration Qnames deployed
     * @throws HumanTaskDeploymentException
     */
    public List<QName> deploy(HumanTaskDeploymentUnit humanTaskDU) throws HumanTaskDeploymentException {
        List<QName> taskConfigsInPackage = new ArrayList<QName>();
        TTask[] tasks = humanTaskDU.getTasks();
        List<HumanTaskBaseConfiguration> configurations = new ArrayList<HumanTaskBaseConfiguration>();
        if (tasks != null) {

            for (TTask task : tasks) {
                QName taskQName = new QName(humanTaskDU.getNamespace(), task.getName());

                if (log.isDebugEnabled()) {
                    log.debug(" Adding task " + task.getName() + "to task configuration");
                }

                TaskConfiguration taskConf =
                        new TaskConfiguration(task,
                                humanTaskDU.getTaskServiceInfo(taskQName),
                                humanTaskDU.getHumanInteractionsDefinition(),
                                humanTaskDU.getWSDLs(),
                                humanTaskDU.getNamespace(),
                                humanTaskDU.getName(),
                                getTenantAxisConfig(),
                                humanTaskDU.getPackageName(),
                                humanTaskDU.getVersion(),
                                humanTaskDU.getHumanTaskDefinitionFile());
                taskConf.setPackageStatus(humanTaskDU.getTaskPackageStatus());
                configurations.add(taskConf);
//                taskConfigurations.add(taskConf);
//
//                // Aggregate task configurations in the hash map
//                taskBaseConfigurationHashMap.put(taskConf.getName(), taskConf);
//                taskConfigsInPackage.add(taskConf.getName());

                if (!taskConf.isErroneous()) {
                    createCallBackService(taskConf);
                    if(taskConf.getPackageStatus() == TaskPackageStatus.ACTIVE) {
                        deploy(taskConf);
//                        activeTaskConfigurationQNameMap.put(taskQName, taskConf.getName());
                    }
                }
            }
        }





        TNotification[] notifications = humanTaskDU.getNotifications();
        if (notifications != null) {
            for (TNotification notification : notifications) {
                QName notificationQName = new QName(humanTaskDU.getNamespace(), notification.getName());
                NotificationConfiguration notificationConf =
                        new NotificationConfiguration(notification,
                                humanTaskDU.getNotificationServiceInfo(notificationQName),
                                humanTaskDU.getHumanInteractionsDefinition(),
                                humanTaskDU.getWSDLs(),
                                humanTaskDU.getNamespace(),
                                humanTaskDU.getName(),
                                getTenantAxisConfig(),
                                humanTaskDU.getPackageName(),
                                humanTaskDU.getVersion(),
                                humanTaskDU.getHumanTaskDefinitionFile());

                notificationConf.setPackageStatus(humanTaskDU.getTaskPackageStatus());
                configurations.add(notificationConf);
//                taskConfigurations.add(notificationConf);
//                taskBaseConfigurationHashMap.put(notificationConf.getName(), notificationConf);
//                taskConfigsInPackage.add(notificationConf.getName());

                if (!notificationConf.isErroneous()) {
                    // Deploy the axis2 service only for the active version of the task/notification
                    if(notificationConf.getPackageStatus() == TaskPackageStatus.ACTIVE) {
                        deploy(notificationConf);
//                        activeTaskConfigurationQNameMap.put(notificationQName, notificationConf.getName());
                    }
                }
            }
        }
        // Add task configuration to runtime only after axis2 service deployment is complete.This avoids the error
        // condition if a service name is deployed with same name outside of this task package
        for(HumanTaskBaseConfiguration configuration:configurations){
            taskConfigurations.add(configuration);
            taskBaseConfigurationHashMap.put(configuration.getName(), configuration);
            taskConfigsInPackage.add(configuration.getName());
            if(configuration.getPackageStatus() == TaskPackageStatus.ACTIVE) {
                activeTaskConfigurationQNameMap.put(configuration.getDefinitionName(), configuration.getName());
            }
        }

        for (TNotification inlineNotification : humanTaskDU.getInlineNotifications()) {
            QName notificationQName = new QName(humanTaskDU.getNamespace(), inlineNotification.getName());
            NotificationConfiguration notificationConf =
                    new NotificationConfiguration(inlineNotification,
                            humanTaskDU.getNotificationServiceInfo(notificationQName),
                            humanTaskDU.getHumanInteractionsDefinition(),
                            humanTaskDU.getWSDLs(),
                            humanTaskDU.getNamespace(),
                            humanTaskDU.getName(),
                            getTenantAxisConfig(),
                            humanTaskDU.getPackageName(),
                            humanTaskDU.getVersion(),
                            humanTaskDU.getHumanTaskDefinitionFile());
            notificationConf.setPackageStatus(humanTaskDU.getTaskPackageStatus());
            taskConfigurations.add(notificationConf);
            taskConfigsInPackage.add(notificationConf.getName());
            taskBaseConfigurationHashMap.put(notificationConf.getName(), notificationConf);
            if(notificationConf.getPackageStatus() == TaskPackageStatus.ACTIVE){
                activeTaskConfigurationQNameMap.put(notificationQName, notificationConf.getName());
            }

        }


        taskConfigurationsInTaskPackage.put(humanTaskDU.getName(), taskConfigsInPackage);
        return taskConfigsInPackage;
    }

    /**
     * Performance a test deployment of the task in order to avoid deployment issues due to invalid task packages
     * @param humanTaskDU
     * @return
     * @throws HumanTaskDeploymentException
     */
    public void validateTaskConfig(HumanTaskDeploymentUnit humanTaskDU) throws HumanTaskDeploymentException, AxisFault {
        boolean validateTask = HumanTaskServiceComponent.getHumanTaskServer().getServerConfig().getEnableTaskValidationBeforeDeployment();
        if(validateTask){
            TTask[] tasks = humanTaskDU.getTasks();
            if (tasks != null) {
                for (TTask task : tasks) {
                    QName taskQName = new QName(humanTaskDU.getNamespace(), task.getName());
                    TaskConfiguration taskConf =
                            new TaskConfiguration(task,
                                    humanTaskDU.getTaskServiceInfo(taskQName),
                                    humanTaskDU.getHumanInteractionsDefinition(),
                                    humanTaskDU.getWSDLs(),
                                    humanTaskDU.getNamespace(),
                                    humanTaskDU.getName(),
                                    getTenantAxisConfig(),
                                    humanTaskDU.getPackageName(),
                                    humanTaskDU.getVersion(),
                                    humanTaskDU.getHumanTaskDefinitionFile());
                    if(taskConf.isErroneous()){
                        throw new HumanTaskDeploymentException(taskConf.getDeploymentError());
                    }
                }
            }

            TNotification[] notifications = humanTaskDU.getNotifications();
            if (notifications != null) {
                for (TNotification notification : notifications) {
                    QName notificationQName = new QName(humanTaskDU.getNamespace(), notification.getName());
                    NotificationConfiguration notificationConf =
                            new NotificationConfiguration(notification,
                                    humanTaskDU.getNotificationServiceInfo(notificationQName),
                                    humanTaskDU.getHumanInteractionsDefinition(),
                                    humanTaskDU.getWSDLs(),
                                    humanTaskDU.getNamespace(),
                                    humanTaskDU.getName(),
                                    getTenantAxisConfig(),
                                    humanTaskDU.getPackageName(),
                                    humanTaskDU.getVersion(),
                                    humanTaskDU.getHumanTaskDefinitionFile());
                    if (notificationConf.isErroneous()) {
                        throw new HumanTaskDeploymentException(notificationConf.getDeploymentError());
                    }
                }
            }

            for (TNotification inlineNotification : humanTaskDU.getInlineNotifications()) {
                QName notificationQName = new QName(humanTaskDU.getNamespace(), inlineNotification.getName());
                NotificationConfiguration notificationConf =
                        new NotificationConfiguration(inlineNotification,
                                humanTaskDU.getNotificationServiceInfo(notificationQName),
                                humanTaskDU.getHumanInteractionsDefinition(),
                                humanTaskDU.getWSDLs(),
                                humanTaskDU.getNamespace(),
                                humanTaskDU.getName(),
                                getTenantAxisConfig(),
                                humanTaskDU.getPackageName(),
                                humanTaskDU.getVersion(),
                                humanTaskDU.getHumanTaskDefinitionFile());
                notificationConf.setPackageStatus(humanTaskDU.getTaskPackageStatus());
                if(notificationConf.isErroneous()){
                    throw new HumanTaskDeploymentException(notificationConf.getDeploymentError());
                }
            }
        }
        return;
    }

    /**
     * Handles the deployment steps for the master node and salve node in the cluster
     * @param humanTaskFile
     * @throws Exception
     */
    public void deploy(File humanTaskFile) throws Exception {

        // Currently using the registry read/write mount property to determine whether this node is a master node
        // or a salve node.
        // Handle this properly with hazelcast leader for cluster scenario TODO
        boolean isMasterServer = !isServerReadOnly();
        // Versions of this ht package is already deployed
        boolean isExistingPackage = false;
        // Exactly matching ht package already exists
        boolean isPackageReload = false;

        DeploymentUnitDAO currentlyActiveTaskPackage = null;

        String md5sum = HumanTaskStoreUtils.getMD5Checksum(humanTaskFile);
        String packageName = FilenameUtils.removeExtension(humanTaskFile.getName());

        List<DeploymentUnitDAO> existingDeploymentUnitsForPackage = getExistingDeploymentUnitsForPackage(packageName.trim());
        if (existingDeploymentUnitsForPackage != null && existingDeploymentUnitsForPackage.size() > 0) {
            isExistingPackage = true;
            for (DeploymentUnitDAO dao : existingDeploymentUnitsForPackage) {
                if ((dao.getStatus() == (TaskPackageStatus.ACTIVE))) {
                    // extract the currently active task package
                    currentlyActiveTaskPackage = dao;
                    if(dao.getChecksum().equals(md5sum)){
                        // Check whether the md5sum matches the active task package.
                        isPackageReload = true;
                    }
                }
            }
        }

        // We will only allow writes to db only for the master node to avoid duplicate version creation
        if (isExistingPackage && isPackageReload) {
            // Reload the existing versions of the human task package . No need of creating a new version of the package
            // This could be due to server restart, deployment of the same package or master node has already deployed the
            // new version of the package

            // First check if the currently active task package is already loaded
            String activePackageName = loadedPackages.get(currentlyActiveTaskPackage.getPackageName());

            if (activePackageName!= null && activePackageName.equals(currentlyActiveTaskPackage.getName())) {
                if(log.isDebugEnabled()) {
                    log.debug("This task package and its previous versions are already loaded");
                }
                // This task package and its previous versions are already loaded , hence return
                return;
            }

            // Load the existing versions of the package
            reloadExistingTaskVersions(existingDeploymentUnitsForPackage, humanTaskFile, md5sum, isMasterServer);
            return;
        }

        // New version of the package is being deployed on top of the existing version
        if (isExistingPackage && !isPackageReload) {
            if (isMasterServer) {
                // Retire the existing version of the package and deploy the new version
                // This could be two scenarios. Server restart with new version and deploying on existing version.
                String activePackageName = loadedPackages.get(currentlyActiveTaskPackage.getPackageName());
                if(activePackageName == null) {
                    // This is a server restart, we need to load existing versions
                    reloadExistingTaskVersions(existingDeploymentUnitsForPackage, humanTaskFile, md5sum, isMasterServer);
                }
                long newVersion = getNextVersion();
                HumanTaskDeploymentUnit newDeploymentUnit = createNewDeploymentUnit(humanTaskFile, tenantId, newVersion, md5sum);
                validateTaskConfig(newDeploymentUnit);

                retireTaskPackageConfigurations(currentlyActiveTaskPackage.getName());
                currentlyActiveTaskPackage.setStatus(TaskPackageStatus.RETIRED);
                updateDeploymentUnitDao(currentlyActiveTaskPackage);
                // Retiring of currently active package is complete.

                // Create and deploy new version
                deployNewTaskVersion(newDeploymentUnit, newVersion);
                // Successfully deployed the packages.
                return;
            } else {
                // Cannot allow creation of a new version from slave nodes, deploy the new version on the master node
                // first to avoid duplicate version creation
                // Write log, issue warning and return
                log.warn("Cannot deploy new version of the task in slave node. Hence deploy the task archive in master" +
                        "node fist");
                return;

            }

        }

        // First version of a new package is being deployed

        if (!isMasterServer) {
            // Issue warning, write warn message and return as we cannot allow deployment of new versions on slave nodes
            // before deployment of the ht package in the master node
            log.warn("Cannot deploy a new version on the package on the salve node first, " +
                    "Deploy the package on the master node first");
            return;
        }


        // Create new version of deployment unit
        // Process the human task configurations
        // Store deployment unit information to the db
        // Deploy axis2 services

        long newVersion = getNextVersion();
        HumanTaskDeploymentUnit newDeploymentUnit = createNewDeploymentUnit(humanTaskFile, tenantId, newVersion, md5sum);
        validateTaskConfig(newDeploymentUnit);
        deployNewTaskVersion(newDeploymentUnit, newVersion);

        return;
    }

    /**
     * Creates a new deployment unit from the given information
     * @param humanTaskFile
     * @param tenantId
     * @param version
     * @param md5sum
     * @return
     * @throws HumanTaskDeploymentException
     */
    public HumanTaskDeploymentUnit createNewDeploymentUnit(File humanTaskFile,
                                                           int tenantId,
                                                           long version,
                                                           String md5sum) throws HumanTaskDeploymentException {
        ArchiveBasedHumanTaskDeploymentUnitBuilder builder =
                new ArchiveBasedHumanTaskDeploymentUnitBuilder(humanTaskFile, tenantId, version, md5sum);
        HumanTaskDeploymentUnit newHumanTaskDeploymentUnit = builder.createNewHumanTaskDeploymentUnit();
        return newHumanTaskDeploymentUnit;
    }

    /**
     * Deploy the new task version using the given deployment unit
     * This method will do the deployment of axis2 services, updating the task configuration lists, and updating the db
     * @param deploymentUnit
     * @param version
     * @throws Exception
     */
    public void deployNewTaskVersion(HumanTaskDeploymentUnit deploymentUnit ,long version) throws Exception {
        if(log.isDebugEnabled()) {
            log.debug("Deploying new package version " + deploymentUnit.getName());
        }
        deploy(deploymentUnit);
        setNextVersion(version);
        createDeploymentUnitDAO(deploymentUnit);
        loadedPackages.put(deploymentUnit.getPackageName(), deploymentUnit.getName());
    }

    /**
     * Retire the task configurations in a given task package
     * @param versionedPackageName
     */
    public void retireTaskPackageConfigurations(String versionedPackageName) {
        if (versionedPackageName == null)
            return;
        if(log.isDebugEnabled()) {
            log.debug("Retiring task package configuration for package" + versionedPackageName);
        }
        List<QName> qNames = taskConfigurationsInTaskPackage.get(versionedPackageName);
        if (qNames != null) {
            for (QName taskQName : qNames) {
                HumanTaskBaseConfiguration humanTaskBaseConfiguration = taskBaseConfigurationHashMap.get(taskQName);
                removeAxisServiceForTaskConfiguration(humanTaskBaseConfiguration);
                humanTaskBaseConfiguration.setPackageStatus(TaskPackageStatus.RETIRED);
            }
        }
    }

    /**
     * Reload existing task versions for a given deployment unit
     * @param existingDeploymentUnitsForPackage
     * @param archiveFile
     * @param md5sum
     * @throws HumanTaskDeploymentException
     */
    public void reloadExistingTaskVersions(List<DeploymentUnitDAO> existingDeploymentUnitsForPackage,
                                           File archiveFile, String md5sum, boolean isMasterServer) throws HumanTaskDeploymentException {
        // deployment units list should not be null, having a safety check anyway
        if (existingDeploymentUnitsForPackage == null) {
            return;
        }
        if(log.isDebugEnabled()) {
            log.debug("Reloading existing task versions");
        }
        for (DeploymentUnitDAO dao : existingDeploymentUnitsForPackage) {

            if(!isMasterServer){
                // We need to avoid deployment of already loaded packages
                String versionedName = dao.getName();
                List<QName> qNames = taskConfigurationsInTaskPackage.get(versionedName);
                if(qNames!= null && qNames.size() > 0){
                    // This dao is already loaded
                    if(log.isDebugEnabled()){
                        log.debug("This is already loaded package, skipping " + versionedName);
                    }
                    continue;
                }

            }

            String taskDirectoryPath = humanTaskDeploymentRepo.getAbsolutePath() + File.separator +
                    tenantId + File.separator + dao.getName();

            File taskDirectory = new File(taskDirectoryPath);
            ArchiveBasedHumanTaskDeploymentUnitBuilder deploymentUnitBuilder = null;
            if(log.isDebugEnabled()){
                log.debug("Loading task : "+ dao.getName());
            }
            try {
                if (taskDirectory.exists()) {
                    // This is an existing task configuration

                    deploymentUnitBuilder =
                            new ArchiveBasedHumanTaskDeploymentUnitBuilder(taskDirectory, tenantId, dao.getVersion(),
                                    dao.getPackageName(), dao.getChecksum());
                } else if(dao.getStatus() == TaskPackageStatus.ACTIVE){
                    // This node is a salve node and task is being reloaded or new version has been deployed on master
                    deploymentUnitBuilder = new ArchiveBasedHumanTaskDeploymentUnitBuilder(archiveFile,
                            tenantId, dao.getVersion(), md5sum);
                } else {
                    String errMsg = "Error loading task. Cannot find the task directory for retired task " +
                            dao.getName();
                    log.error(errMsg);
                    throw new HumanTaskDeploymentException(errMsg);
                }

                // Check whether this is a new version deployment on a slave node
                if(!isMasterServer && dao.getStatus() == TaskPackageStatus.ACTIVE){
                    String currentDeployedVersion = loadedPackages.get(dao.getPackageName());
                    if(currentDeployedVersion != null && currentDeployedVersion.equals(dao.getName()) == false) {
                        // This is a new version on the salve node  , retire the existing version
                         retireTaskPackageConfigurations(currentDeployedVersion);
                    }
                }


                HumanTaskDeploymentUnit taskDeploymentUnit =
                        deploymentUnitBuilder.createNewHumanTaskDeploymentUnit();
                taskDeploymentUnit.setTaskPackageStatus(dao.getStatus());
                deploy(taskDeploymentUnit);
                if(dao.getStatus() == TaskPackageStatus.ACTIVE) {
                    // Add the active package to the loaded packages
                    loadedPackages.put(dao.getPackageName(), dao.getName());
                }

            } catch (HumanTaskDeploymentException e) {
                String errMsg = "Error loading the task configuration";
                log.error(errMsg + e);
                throw e;
            }
        }
    }

    private void createCallBackService(TaskConfiguration taskConf)
            throws HumanTaskDeploymentException {
        EndpointConfiguration endpointConfig =
                taskConf.getEndpointConfiguration(taskConf.getCallbackServiceName().getLocalPart(),
                        taskConf.getCallbackPortName());
        CallBackServiceImpl callbackService = new CallBackServiceImpl(tenantId,
                taskConf.getCallbackServiceName(), taskConf.getCallbackPortName(),
                taskConf.getName(), taskConf.getResponseWSDL(), taskConf.getResponseOperation(),
                endpointConfig);
        taskConf.setCallBackService(callbackService);
    }

    private void deploy(HumanTaskBaseConfiguration taskConfig) throws HumanTaskDeploymentException {

        if (taskConfig != null) {
            /**
             * Creating AxisService for HI
             */
            if (log.isDebugEnabled()) {
                log.debug("Deploying task " + taskConfig.getName());
            }

            AxisService axisService;
            Definition wsdlDef = taskConfig.getWSDL();

            if (taskConfig instanceof TaskConfiguration) {
                //to get the task id as response
                wsdlDef.getPortType(taskConfig.getPortType()).getOperation(
                        taskConfig.getOperation(),
                        null, null).setStyle(OperationType.REQUEST_RESPONSE);
            } else {
                //ONE_WAY no feed back for NOTIFICATIONS
                wsdlDef.getPortType(taskConfig.getPortType()).getOperation(
                        taskConfig.getOperation(),
                        null, null).setStyle(OperationType.ONE_WAY);
            }

            WSDL11ToAxisServiceBuilder serviceBuilder = createAxisServiceBuilder(taskConfig, wsdlDef);

            try {
                axisService = createAxisService(serviceBuilder, taskConfig);
                ServiceConfigurationUtil.configureService(axisService,
                        taskConfig.getEndpointConfiguration(
                                taskConfig.getServiceName().getLocalPart(),
                                taskConfig.getPortName()),
                        getConfigContext());

                ArrayList<AxisService> serviceList = new ArrayList<AxisService>();
                serviceList.add(axisService);
                DeploymentEngine.addServiceGroup(createServiceGroupForService(axisService), serviceList,
                        null, null, getTenantAxisConfig());

                if (log.isDebugEnabled()) {
                    log.debug(" Published axis2 service " + axisService.getName() + " for task " + taskConfig.getName());
                }

            } catch (AxisFault axisFault) {

                String errMsg = "Error populating the service";
                log.error(errMsg);
                throw new HumanTaskDeploymentException(errMsg, axisFault);
            }

        }
    }

    //Creates the AxisServiceBuilder object.
    private WSDL11ToAxisServiceBuilder createAxisServiceBuilder(
            HumanTaskBaseConfiguration taskConfig, Definition wsdlDef) {
        WSDL11ToAxisServiceBuilder serviceBuilder =
                new WSDL11ToAxisServiceBuilder(wsdlDef,
                        taskConfig.getServiceName(), taskConfig.getPortName());
        String wsdlBaseURI = wsdlDef.getDocumentBaseURI();
        serviceBuilder.setBaseUri(wsdlBaseURI);
        /*we don't need custom resolvers since registry takes care of it*/
        serviceBuilder.setCustomResolver(new DefaultURIResolver());
        URI wsdlBase = null;
        try {
            wsdlBase = new URI(convertToVaildURI(wsdlBaseURI));
        } catch (Exception e) {
            String error = "Error occurred while creating AxisServiceBuilder.";
            log.error(error);
        }
        serviceBuilder.setCustomWSDLResolver(new HumanTaskWSDLLocator(wsdlBase));
        serviceBuilder.setServerSide(true);
        return serviceBuilder;
    }

    //Creates the AxisService object from the provided ServiceBuilder object.
    private AxisService createAxisService(WSDL11ToAxisServiceBuilder serviceBuilder, HumanTaskBaseConfiguration config)
            throws AxisFault {
        AxisService axisService;
        axisService = serviceBuilder.populateService();
        axisService.setParent(getTenantAxisConfig());
        axisService.setWsdlFound(true);
        axisService.setCustomWsdl(true);
        //axisService.setFileName(new URL(taskConfig.getWsdlDefLocation()));
        axisService.setClassLoader(getTenantAxisConfig().getServiceClassLoader());
        Utils.setEndpointsToAllUsedBindings(axisService);
        axisService.addParameter(new Parameter("modifyUserWSDLPortAddress", "true"));

        /* Setting service type to use in service management*/
        axisService.addParameter(ServerConstants.SERVICE_TYPE, "humantask");

        /* Fix for losing of security configuration  when updating human-task package*/
        axisService.addParameter(new Parameter(CarbonConstants.PRESERVE_SERVICE_HISTORY_PARAM,
                "true"));

        Iterator operations = axisService.getOperations();

        AxisHumanTaskMessageReceiver msgReceiver = new AxisHumanTaskMessageReceiver();

        msgReceiver.setHumanTaskEngine(HumanTaskServiceComponent.getHumanTaskServer().
                getTaskEngine());
        // Setting the task configuration to the message receiver. Hence no need to search for task configuration, when
        // the actual task invocation happens, we will already have the task configuration attached to the message receiver
        // itself
        msgReceiver.setTaskBaseConfiguration(config);
        while (operations.hasNext()) {
            AxisOperation operation = (AxisOperation) operations.next();
            // Setting Message Receiver even if operation has a message receiver specified.
            // This is to fix the issue when build service configuration using services.xml(Always RPCMessageReceiver
            // is set to operations).
            operation.setMessageReceiver(msgReceiver);
            getTenantAxisConfig().getPhasesInfo().setOperationPhases(operation);
        }
        List<String> transports = new ArrayList<String>();
        transports.add(Constants.TRANSPORT_HTTPS);
        transports.add(Constants.TRANSPORT_HTTP);
        transports.add(Constants.TRANSPORT_LOCAL);
        axisService.setExposedTransports(transports);
        if (HumanTaskServiceComponent.getHumanTaskServer().getServerConfig().isHtCoordinationEnabled()
                && HumanTaskServiceComponent.getHumanTaskServer().getServerConfig().isTaskRegistrationEnabled()
                && config.getConfigurationType() == HumanTaskBaseConfiguration.ConfigurationType.TASK) {
            // Only Engage coordination module in-case of Tasks. Coordination module is not required for notifications

            axisService.engageModule(getConfigContext().getAxisConfiguration().getModule("htcoordination"));
        }
        return axisService;
    }

    private AxisServiceGroup createServiceGroupForService(AxisService svc) throws AxisFault {
        AxisServiceGroup svcGroup = new AxisServiceGroup();
        svcGroup.setServiceGroupName(svc.getName());
        svcGroup.addService(svc);
        svcGroup.addParameter(new Parameter(CarbonConstants.PRESERVE_SERVICE_HISTORY_PARAM, "true"));

        return svcGroup;
    }

    public HumanTaskBaseConfiguration getTaskConfiguration(QName portType, String operation) {
        for (HumanTaskBaseConfiguration taskConf : taskConfigurations) {
            if (taskConf.getPortType().equals(portType) && taskConf.getOperation().equals(operation)) {
                return taskConf;
            }
        }
        return null;
    }

    /**
     * @return : The tenant id of this task store.
     */
    public int getTenantId() {
        return tenantId;
    }

    /**
     * @return : The tenant axis configuration of this task store.
     */
    public AxisConfiguration getTenantAxisConfig() {
        return configContext.getAxisConfiguration();
    }

    /**
     * @return : The deployement repository location.
     */
    public File getHumanTaskDeploymentRepo() {
        return humanTaskDeploymentRepo;
    }

    /**
     * @param humanTaskDeploymentRepo : The deployment repository location to set.
     */
    public void setHumanTaskDeploymentRepo(File humanTaskDeploymentRepo) {
        this.humanTaskDeploymentRepo = humanTaskDeploymentRepo;
    }

    /**
     * @return : The list of task configurations in the store.
     */
    public List<HumanTaskBaseConfiguration> getTaskConfigurations() {
        return this.taskConfigurations;
    }

    /**
     * Gets the simple task definition information for a given package name.
     *
     * @param packageName : The package name.
     * @return : The matching package information list.
     */
    public List<SimpleTaskDefinitionInfo> getTaskConfigurationInfoListForPackage(
            String packageName) {
        List<SimpleTaskDefinitionInfo> matchingTaskDefinitions = new ArrayList<SimpleTaskDefinitionInfo>();
        for (HumanTaskBaseConfiguration taskBaseConfiguration : this.taskConfigurations) {
            if (taskBaseConfiguration.getPackageName().equals(packageName)) {
                matchingTaskDefinitions.add(DeploymentUtil.getSimpleTaskDefinitionInfo(taskBaseConfiguration));
            }
        }
        return matchingTaskDefinitions;
    }

    /**
     * Gets the list of SimpleTaskDefinitionInfo objects for the all the HumanTaskBaseConfiguration objects in this store.
     *
     * @return :
     */
    public List<SimpleTaskDefinitionInfo> getTaskConfigurationInfoList() {
        return DeploymentUtil.getTaskConfigurationsInfoList(this.getTaskConfigurations());
    }

    /**
     * Un Deploys a given HumanTask package.
     * If the config is hard undeployment,delete all instance information from the db and delete the task configs
     * If the config is soft undeployment, leave all task instances and undeploy the service and the configuration only
     *
     *
     * @param packageName : The package name to be unDeployed.
     */
    public void unDeploy(String packageName) {

        if(log.isDebugEnabled()){
            log.debug("Un deploying task package : "+ packageName);
        }
        try {
            removeMatchingPackage(packageName);
        } catch (Exception e) {
            log.error("Error in deploying task package", e);
        }
    }

    /**
     * Gets the HumanTaskBaseConfiguration object for the given task QName.
     *
     * @param taskName : The task name of which the task configuration is to be retrieved.
     * @return : The matching HumanTaskBaseConfiguration object.
     */
    public HumanTaskBaseConfiguration getTaskConfiguration(QName taskName) {
        return taskBaseConfigurationHashMap.get(taskName);
    }

    public HumanTaskBaseConfiguration getActiveTaskConfiguration(QName taskQname){
        QName qName = activeTaskConfigurationQNameMap.get(taskQname);
        if(qName != null){
            return taskBaseConfigurationHashMap.get(qName);
        }
        return null;
    }

    private void removeMatchingPackageVersion(String versionedPackageName, TaskPackageStatus status){
        List<QName> taskConfigurationQNameList = taskConfigurationsInTaskPackage.get(versionedPackageName);
        if(taskConfigurationQNameList != null){
            for(QName name:taskConfigurationQNameList){
                HumanTaskBaseConfiguration humanTaskBaseConfiguration = taskBaseConfigurationHashMap.get(name);
                if(humanTaskBaseConfiguration != null){
                    taskConfigurations.remove(humanTaskBaseConfiguration);
                    if(status == TaskPackageStatus.ACTIVE){
                        removeAxisServiceForTaskConfiguration(humanTaskBaseConfiguration);
                    }
                }
                activeTaskConfigurationQNameMap.remove(humanTaskBaseConfiguration.getDefinitionName());

                taskBaseConfigurationHashMap.remove(name);
            }
        }
        taskConfigurationsInTaskPackage.remove(versionedPackageName);
        // Now delete the file from repository
        deleteHumanTaskPackageFromRepo(versionedPackageName);
    }

    private boolean removeMatchingPackage(final String packageName) throws Exception {

        if(!isServerReadOnly()){
            // This is the master server
            Object o = engine.getScheduler().execTransaction(new Callable<Object>() {
                public Object call() throws Exception {

                    HumanTaskDAOConnection connection = engine.getDaoConnectionFactory().getConnection();
                    List<DeploymentUnitDAO> deploymentUnitsForPackageName = connection.getDeploymentUnitsForPackageName(tenantId, packageName);
                    for(DeploymentUnitDAO deploymentUnitDAO:deploymentUnitsForPackageName){
                        removeMatchingPackageVersion(deploymentUnitDAO.getName(), deploymentUnitDAO.getStatus());
                    }
                    List<TaskDAO> matchingTaskInstances = connection.getMatchingTaskInstances(packageName, tenantId);
                    for(TaskDAO taskDAO:matchingTaskInstances){
                        taskDAO.deleteInstance();
                    }
                    connection.deleteDeploymentUnits(packageName, tenantId);
                    return null;
                }
            });
            loadedPackages.remove(packageName);
        } else {
            // Slave nodes
            List<HumanTaskBaseConfiguration> matchingTaskConfigurations = new ArrayList<HumanTaskBaseConfiguration>();
            for(HumanTaskBaseConfiguration configuration:taskConfigurations){
                if(configuration.getPackageName().equals(packageName)){
                    matchingTaskConfigurations.add(configuration);
                }
            }

            for(HumanTaskBaseConfiguration configuration:matchingTaskConfigurations){
                String taskPackageName = configuration.getPackageName();
                long version = configuration.getVersion();
                String versionedPackageName = taskPackageName + "-" + version;
                removeMatchingPackageVersion(versionedPackageName, configuration.getPackageStatus());
            }
            loadedPackages.remove(packageName);
        }
        return true;
    }



    private boolean removeMatchingPackageAfterTaskObsoletion(String packageName) {
        final HumanTaskEngine taskEngine = HumanTaskServiceComponent.getHumanTaskServer().getTaskEngine();
        boolean matchingPackagesFound = false;
        final int tId = this.tenantId;
        List<HumanTaskBaseConfiguration> matchingTaskConfigurations =
                new ArrayList<HumanTaskBaseConfiguration>();
        for (final HumanTaskBaseConfiguration configuration : this.getTaskConfigurations()) {
            if (configuration.getPackageName().equals(packageName)) {
                matchingTaskConfigurations.add(configuration);
                try {
                    taskEngine.getScheduler().execTransaction(new Callable<Object>() {
                        public Object call() throws Exception {
                            taskEngine.getDaoConnectionFactory().getConnection().obsoleteTasks(
                                    configuration.getName().toString(), tId);
                            return null;
                        }
                    });
                } catch (Exception e) {
                    String errMsg = "Error occurred while making tasks obsolete";
                    log.error(errMsg);
                    throw new HumanTaskRuntimeException(errMsg, e);
                }
                // we don't want the associated service once the task configuration is removed!
                removeAxisServiceForTaskConfiguration(configuration);
                matchingPackagesFound = true;
            }
        }

        // remove the task configurations with the matching package name.
        for (HumanTaskBaseConfiguration removableConfiguration : matchingTaskConfigurations) {
            this.getTaskConfigurations().remove(removableConfiguration);
        }

        return matchingPackagesFound;
    }

    /**
     * Remove the service associated with the task configuration.
     *
     * @param removableConfiguration : The task configuration.
     */
    public void removeAxisServiceForTaskConfiguration(
            HumanTaskBaseConfiguration removableConfiguration) {
        try {
            //If there are matching axis services we remove them.
            if (removableConfiguration.getServiceName() != null &&
                    StringUtils.isNotEmpty(removableConfiguration.getServiceName().getLocalPart())) {
                String axisServiceName = removableConfiguration.getServiceName().getLocalPart();
                AxisService axisService = getTenantAxisConfig().getService(axisServiceName);
                if (axisService != null) {
                    axisService.releaseSchemaList();
                    getTenantAxisConfig().stopService(axisServiceName);
                    getTenantAxisConfig().removeServiceGroup(axisServiceName);
                    if (log.isDebugEnabled()) {
                        log.debug("Un deployed axis2 service " + axisServiceName);
                    }
                } else {
                    log.warn("Could not find matching AxisService in " +
                            "Tenant AxisConfiguration for service name :" + axisServiceName);
                }
            } else {
                log.warn(String.format("Could not find a associated service name for " +
                        "[%s] configuration [%s]",
                        removableConfiguration.getConfigurationType(),
                        removableConfiguration.getName().toString()));
            }
        } catch (AxisFault axisFault) {
            String error = "Error occurred while removing the axis service " +
                    removableConfiguration.getServiceName();

            log.error(error);
            throw new HumanTaskRuntimeException(error, axisFault);
        }
    }

    /**
     * deletes the human task package archive from the repository.
     *
     * @param packageName : The package name to be deleted.
     */
    public void deleteHumanTaskArchive(String packageName) {
        File humanTaskArchive = getHumanTaskArchiveLocation(packageName);
        log.info("UnDeploying HumanTask package " + packageName + ". Deleting HumanTask archive " +
                humanTaskArchive.getName() + "....");
        if (humanTaskArchive.exists()) {
            if (!humanTaskArchive.delete()) {
                //For windows
                humanTaskArchive.deleteOnExit();
            }
        } else {
            log.warn("HumanTask archive [" + humanTaskArchive.getAbsolutePath() +
                    "] not found. This can happen if you delete " +
                    "the HumanTask archive from the file system.");
        }
    }

    /**
     * Return the human task archive file for the given package name.
     *
     * @param packageName : The human task archive package name.
     * @return : The matching human task archive file.
     */
    public File getHumanTaskArchiveLocation(String packageName) {
        String repoPath = getTenantAxisConfig().getRepository().getPath();

        int length = repoPath.length();
        String lastChar = repoPath.substring(length - 1);

        if (!File.separator.equals(lastChar)) {
            repoPath += File.separator;
        }

        String htArchiveLocation = repoPath + HumanTaskConstants.HUMANTASK_REPO_DIRECTORY + File.separator +
                packageName + "." + HumanTaskConstants.HUMANTASK_PACKAGE_EXTENSION;
        return new File(htArchiveLocation);
    }

    public ConfigurationContext getConfigContext() {
        return configContext;
    }

    public Cache getCache(String cacheName) {
        CacheManager cacheManager = Caching.getCacheManagerFactory().getCacheManager(HumanTaskConstants.HT_CACHE_MANAGER);
        if (cacheManager != null) {
            return cacheManager.getCache(cacheName);
        }
        return null;
    }

    private void initializeCaches() {
         Caching.getCacheManagerFactory().getCacheManager(HumanTaskConstants.HT_CACHE_MANAGER);
        // Currently there is no way to obtain the same cache again since when all the objects in the cache are removed, the
        // cache is also removed.Hence using the default cache
//        final int cacheExpiryDuration = HumanTaskServiceComponent.getHumanTaskServer().getServerConfig().getCacheExpiryDuration();

//        CacheManager humantaskCacheManager = Caching.getCacheManagerFactory().getCacheManager(HumanTaskConstants.HT_CACHE_MANAGER);


//        humantaskCacheManager.<String, Boolean>createCacheBuilder(HumanTaskConstants.HT_CACHE_ROLE_NAME_LIST)
//                        .setExpiry(CacheConfiguration.ExpiryType.MODIFIED, new CacheConfiguration.Duration(TimeUnit.SECONDS, cacheExpiryDuration))
//                        .setStoreByValue(false)
//                        .build();
//
//        humantaskCacheManager.<String, Boolean>createCacheBuilder(HumanTaskConstants.HT_CACHE_USER_NAME_LIST)
//                .setExpiry(CacheConfiguration.ExpiryType.MODIFIED, new CacheConfiguration.Duration(TimeUnit.SECONDS, cacheExpiryDuration))
//                .setStoreByValue(false)
//                .build();
//
//        humantaskCacheManager.<String, List<String>>createCacheBuilder(HumanTaskConstants.HT_CACHE_ROLE_NAME_LIST_FOR_USER)
//                .setExpiry(CacheConfiguration.ExpiryType.MODIFIED, new CacheConfiguration.Duration(TimeUnit.SECONDS, cacheExpiryDuration))
//                .setStoreByValue(false)
//                .build();
//
//        humantaskCacheManager.<String, List<String>>createCacheBuilder(HumanTaskConstants.HT_CACHE_USER_NAME_LIST_FOR_ROLE)
//                .setExpiry(CacheConfiguration.ExpiryType.MODIFIED, new CacheConfiguration.Duration(TimeUnit.SECONDS, cacheExpiryDuration))
//                .setStoreByValue(false)
//                .build();
    }

    public void unloadCaches(){
        if(HumanTaskServiceComponent.getHumanTaskServer().getServerConfig().isCachingEnabled()){
            CacheManagerFactory cacheManagerFactory = Caching.getCacheManagerFactory();
            if(cacheManagerFactory != null){
                if(log.isDebugEnabled()) {
                    log.debug("Closing the cache manager factory for the tenant");
                }
                cacheManagerFactory.close();
            }
        }
    }

    //removes the package from the human task package repository.
    private void deleteHumanTaskPackageFromRepo(String packageName) {

        String humanTaskPackageLocation = this.humanTaskDeploymentRepo.getAbsolutePath() +
                File.separator + tenantId + File.separator +
                packageName;
        File humanTaskPackageDirectory = new File(humanTaskPackageLocation);
        if(log.isDebugEnabled()) {
            log.debug("Deleting human task package from directory " + humanTaskPackageDirectory);
        }
        log.info("UnDeploying HumanTask package. " + "Deleting " + packageName + " HumanTask package");

        if (humanTaskPackageDirectory.exists()) {
            FileManipulator.deleteDir(humanTaskPackageDirectory);
        } else {
            log.warn("HumanTask package " + humanTaskPackageDirectory.getAbsolutePath() +
                    " not found. This can happen if you delete " +
                    "the HumanTask package from the file system.");
        }
    }

    /**
     * Update the task configurations with the given package status.
     *
     * @param packageName : package Name.
     * @param status      : The new status
     */
    public void updateTaskStatusForPackage(String packageName, TaskPackageStatus status) {
        for (HumanTaskBaseConfiguration taskBaseConfiguration : this.taskConfigurations) {
            if (taskBaseConfiguration.getPackageName().equals(packageName)) {
                taskBaseConfiguration.setPackageStatus(status);
            }
        }
    }

    private String convertToVaildURI(String filePath) {
        File tmpFile = new File(filePath);
        if (tmpFile.exists()) {
            return tmpFile.toURI().toString();
        }
        // if not a file path
        return filePath;
    }

    public long getNextVersion() throws Exception {
        long version = HumanTaskServiceComponent.getHumanTaskServer().getTaskEngine().getScheduler().
                execTransaction(new Callable<Long>() {
                    public Long call() throws Exception {
                        HumanTaskEngine engine = HumanTaskServiceComponent.getHumanTaskServer().getTaskEngine();
                        HumanTaskDAOConnection daoConn = engine.getDaoConnectionFactory().getConnection();
                        return daoConn.getNextVersion();

                    }

                });
        return version;
    }

    public void setNextVersion(final Long version) throws Exception {
        HumanTaskServiceComponent.getHumanTaskServer().getTaskEngine().getScheduler().
                execTransaction(new Callable<Long>() {
                    public Long call () throws Exception {
                        HumanTaskEngine engine = HumanTaskServiceComponent.getHumanTaskServer().getTaskEngine();
                        HumanTaskDAOConnection daoConn = engine.getDaoConnectionFactory().getConnection();
                        daoConn.setNextVersion(version.longValue());
                        return version;
                    }
                });
    }

    private List<DeploymentUnitDAO> getExistingDeploymentUnitsForPackage(final String packageName) throws Exception {
        List<DeploymentUnitDAO> deploymentUnitsForPackageName = engine.getScheduler().
                execTransaction(new Callable<List<DeploymentUnitDAO>>() {
                    public List<DeploymentUnitDAO> call() throws Exception {
                        HumanTaskDAOConnection daoConnection = engine.getDaoConnectionFactory().getConnection();
                        List<DeploymentUnitDAO> deploymentUnitsForPackageName = null;
                        if (daoConnection != null) {
                            deploymentUnitsForPackageName = daoConnection.
                                    getDeploymentUnitsForPackageName(tenantId, packageName);

                        }
                        return deploymentUnitsForPackageName;
                    }
                });
        return deploymentUnitsForPackageName;
    }


    private boolean isServerReadOnly() {
        try {
            RegistryContext registryContext = HumanTaskServiceComponent.getRegistryService().getConfigSystemRegistry().getRegistryContext();
            if (registryContext.isReadOnly()) {
                return true;
            }
        } catch (RegistryException e) {
            log.error("Error while reading registry status");

        }
        return false;
    }

    public DeploymentUnitDAO createDeploymentUnitDAO(final HumanTaskDeploymentUnit deploymentUnit) throws Exception {
        return HumanTaskServiceComponent.getHumanTaskServer().getTaskEngine().getScheduler().
                execTransaction(new Callable<DeploymentUnitDAO>() {
                    public DeploymentUnitDAO call() throws Exception {
                        HumanTaskEngine engine = HumanTaskServiceComponent.getHumanTaskServer().getTaskEngine();
                        HumanTaskDAOConnection daoConn = engine.getDaoConnectionFactory().getConnection();
                        return daoConn.createDeploymentUnitDAO(deploymentUnit, tenantId);
                    }
                });
    }

    public void updateDeploymentUnitDao(final DeploymentUnitDAO deploymentUnit) throws Exception {
        HumanTaskServiceComponent.getHumanTaskServer().getTaskEngine().getScheduler().
                execTransaction(new Callable<DeploymentUnitDAO>() {
                    public DeploymentUnitDAO call() throws Exception {
                        EntityManager entityManager = engine.getDaoConnectionFactory().getConnection().getEntityManager();
                        entityManager.merge(deploymentUnit);
                        return deploymentUnit;
                    }
                });
    }
}
