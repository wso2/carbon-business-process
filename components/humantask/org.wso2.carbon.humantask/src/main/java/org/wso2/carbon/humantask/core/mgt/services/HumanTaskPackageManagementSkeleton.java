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

package org.wso2.carbon.humantask.core.mgt.services;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.humantask.core.HumanTaskServer;
import org.wso2.carbon.humantask.core.dao.TaskPackageStatus;
import org.wso2.carbon.humantask.core.deployment.SimpleTaskDefinitionInfo;
import org.wso2.carbon.humantask.core.internal.HumanTaskServiceComponent;
import org.wso2.carbon.humantask.core.store.HumanTaskBaseConfiguration;
import org.wso2.carbon.humantask.core.store.HumanTaskStore;
import org.wso2.carbon.humantask.core.store.NotificationConfiguration;
import org.wso2.carbon.humantask.core.store.TaskConfiguration;
import org.wso2.carbon.humantask.skeleton.mgt.services.HumanTaskPackageManagementSkeletonInterface;
import org.wso2.carbon.humantask.skeleton.mgt.services.PackageManagementException;
import org.wso2.carbon.humantask.skeleton.mgt.services.types.DeployedPackagesPaginated;
import org.wso2.carbon.humantask.skeleton.mgt.services.types.DeployedTaskDefinitionsPaginated;
import org.wso2.carbon.humantask.skeleton.mgt.services.types.HumanTaskDefinition;
import org.wso2.carbon.humantask.skeleton.mgt.services.types.HumanTaskPackageDownloadData;
import org.wso2.carbon.humantask.skeleton.mgt.services.types.TaskDefinitionInfo;
import org.wso2.carbon.humantask.skeleton.mgt.services.types.TaskDefinition_type0;
import org.wso2.carbon.humantask.skeleton.mgt.services.types.TaskInfoType;
import org.wso2.carbon.humantask.skeleton.mgt.services.types.TaskStatusType;
import org.wso2.carbon.humantask.skeleton.mgt.services.types.TaskType;
import org.wso2.carbon.humantask.skeleton.mgt.services.types.Task_type0;
import org.wso2.carbon.humantask.skeleton.mgt.services.types.UndeployStatus_type0;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

/**
 * The human task package management service skeleton.
 */
public class HumanTaskPackageManagementSkeleton extends AbstractAdmin
        implements HumanTaskPackageManagementSkeletonInterface {

    private static Log log = LogFactory.getLog(HumanTaskPackageManagementSkeleton.class);

    /**
     * @param page : The page number.
     * @return :
     */
    @Override
    public DeployedPackagesPaginated listDeployedPackagesPaginated(int page) {
        return null;
    }


    /**
     * Lists the tasks in the given package name.
     *
     * @param packageName : The name of the package to list task definitions.
     * @return : The Task_type0 array containing the task definition information.
     */
    @Override
    public Task_type0[] listTasksInPackage(String packageName) throws PackageManagementException {

        if (StringUtils.isEmpty(packageName)) {
            throw new IllegalArgumentException("The provided package name is empty!");
        }

        try {
            List<SimpleTaskDefinitionInfo> taskDefsInPackage =
                    getTenantTaskStore().getTaskConfigurationInfoListForPackage(packageName);
            Task_type0[] taskDefArray = new Task_type0[taskDefsInPackage.size()];
            int i = 0;
            for (SimpleTaskDefinitionInfo taskDefinitionInfo : taskDefsInPackage) {
                taskDefArray[i] = createTaskTypeObject(taskDefinitionInfo);
                i++;
            }

            return taskDefArray;
        } catch (Exception ex) {
            String errMsg = "listTasksInPackage operation failed";
            log.error(errMsg, ex);
            throw new PackageManagementException(errMsg, ex);
        }
    }

    @Override
    public DeployedTaskDefinitionsPaginated listDeployedTaskDefinitionsPaginated(int page)
            throws PackageManagementException {

        int tPage = page;
        try {
            DeployedTaskDefinitionsPaginated paginatedTaskDefs = new DeployedTaskDefinitionsPaginated();

            if (tPage < 0 || tPage == Integer.MAX_VALUE) {
                tPage = 0;
            }

            int itemsPerPage = 10;
            int startIndexForCurrentPage = tPage * itemsPerPage;
            int endIndexForCurrentPage = (tPage + 1) * itemsPerPage;

            List<SimpleTaskDefinitionInfo> taskConfigs = getTenantTaskStore().getTaskConfigurationInfoList();

            int taskDefListSize = taskConfigs.size();
            int pages = (int) Math.ceil((double) taskDefListSize / itemsPerPage);
            paginatedTaskDefs.setPages(pages);

            SimpleTaskDefinitionInfo[] taskDefinitionInfoArray =
                    taskConfigs.toArray(new SimpleTaskDefinitionInfo[taskDefListSize]);

            for (int i = startIndexForCurrentPage;
                 (i < endIndexForCurrentPage && i < taskDefListSize); i++) {
                paginatedTaskDefs.addTaskDefinition(createTaskDefObject(taskDefinitionInfoArray[i]));
            }

            return paginatedTaskDefs;
        } catch (Exception ex) {
            String errMsg = "listDeployedTaskDefinitionsPaginated operation failed";
            log.error(errMsg, ex);
            throw new PackageManagementException(errMsg, ex);
        }
    }

    @Override
    public TaskInfoType getTaskInfo(QName taskId) throws PackageManagementException {
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();

        TaskInfoType taskInfo = null;
        HumanTaskBaseConfiguration taskConf = HumanTaskServiceComponent.
                getHumanTaskServer().getTaskStoreManager().getHumanTaskStore(tenantId).
                getTaskConfiguration(taskId);
        if (taskConf != null) {
            taskInfo = new TaskInfoType();
            taskInfo.setTaskId(taskConf.getName());
            taskInfo.setPackageName(taskConf.getPackageName());

            if (TaskPackageStatus.ACTIVE.equals(taskConf.getPackageStatus())) {
                taskInfo.setStatus(TaskStatusType.ACTIVE);
            } else if (TaskPackageStatus.RETIRED.equals(taskConf.getPackageStatus())) {
                taskInfo.setStatus(TaskStatusType.INACTIVE);
            } else if (TaskPackageStatus.UNDEPLOYING.equals(taskConf.getPackageStatus())) {
                taskInfo.setStatus(TaskStatusType.UNDEPLOYING);
            }

            taskInfo.setDeploymentError(taskConf.getDeploymentError());
            taskInfo.setErroneous(taskConf.isErroneous());
            if (taskConf instanceof TaskConfiguration) {
                taskInfo.setTaskType(TaskType.TASK);
            } else if (taskConf instanceof NotificationConfiguration) {
                taskInfo.setTaskType(TaskType.NOTIFICATION);
            }

            taskInfo.setDefinitionInfo(fillTaskDefinitionInfo(taskConf));
        }

        return taskInfo;
    }

    private TaskDefinitionInfo fillTaskDefinitionInfo(HumanTaskBaseConfiguration taskConf)
            throws PackageManagementException {
        TaskDefinitionInfo taskDefInfo = new TaskDefinitionInfo();
        taskDefInfo.setTaskName(taskConf.getName());

        HumanTaskDefinition taskDefinition = new HumanTaskDefinition();
        taskDefinition.setExtraElement(createTaskDefOMElement(taskConf.getHumanTaskDefinitionFile()));
        taskDefInfo.setDefinition(taskDefinition);
        return taskDefInfo;
    }

    private OMElement createTaskDefOMElement(File humanTaskDefFile)
            throws PackageManagementException {

        XMLStreamReader reader;
        FileInputStream fis = null;
        OMElement humanTaskDefinition;
        try {
            fis = new FileInputStream(humanTaskDefFile);
            XMLInputFactory xif = XMLInputFactory.newInstance();
            reader = xif.createXMLStreamReader(fis);
            StAXOMBuilder builder = new StAXOMBuilder(reader);
            humanTaskDefinition = builder.getDocumentElement();
            humanTaskDefinition.build();
        } catch (XMLStreamException e) {
            String errMsg = "XML stream reader exception: " + humanTaskDefFile.getAbsolutePath();
            log.error(errMsg, e);
            throw new PackageManagementException(errMsg, e);
        } catch (FileNotFoundException e) {
            String errMsg = "HT File reading exception: " + humanTaskDefFile.getAbsolutePath();
            log.error(errMsg, e);
            throw new PackageManagementException(errMsg, e);
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    log.warn("Cannot close file input stream.", e);
                }
            }
        }

        return humanTaskDefinition;
    }

    private TaskDefinition_type0 createTaskDefObject(
            SimpleTaskDefinitionInfo taskConfiguration) {
        TaskDefinition_type0 taskDef = new TaskDefinition_type0();

        taskDef.setPackageName(taskConfiguration.getPackageName());
        taskDef.setTaskName(taskConfiguration.getTaskName());


        if (TaskPackageStatus.ACTIVE.equals(taskConfiguration.getPackageStatus())) {
            taskDef.setState(TaskStatusType.ACTIVE);
        } else if (TaskPackageStatus.RETIRED.equals(taskConfiguration.getPackageStatus())) {
            taskDef.setState(TaskStatusType.INACTIVE);
        } else if (TaskPackageStatus.UNDEPLOYING.equals(taskConfiguration.getPackageStatus())) {
            taskDef.setState(TaskStatusType.UNDEPLOYING);
        }

        taskDef.setDeploymentError(taskConfiguration.getDeploymentError());
        taskDef.setErroneous(taskConfiguration.isErroneous());

        if (org.wso2.carbon.humantask.core.dao.TaskType.TASK.equals(
                taskConfiguration.getTaskType())) {
            taskDef.setType(TaskType.TASK);
        } else if (org.wso2.carbon.humantask.core.dao.TaskType.NOTIFICATION.equals(
                taskConfiguration.getTaskType())) {
            taskDef.setType(TaskType.NOTIFICATION);
        }
        return taskDef;
    }

    private Task_type0 createTaskTypeObject(SimpleTaskDefinitionInfo taskConfiguration) {

        Task_type0 task = new Task_type0();
        task.setName(taskConfiguration.getTaskName());
        if (org.wso2.carbon.humantask.core.dao.TaskType.TASK.equals(taskConfiguration.getTaskType())) {
            task.setType(TaskType.TASK);
        } else if (org.wso2.carbon.humantask.core.dao.TaskType.NOTIFICATION.equals(
                taskConfiguration.getTaskType())) {
            task.setType(TaskType.NOTIFICATION);
        }

        task.setErroneous(taskConfiguration.isErroneous());
        task.setDeploymentError(taskConfiguration.getDeploymentError());

        return task;
    }


    public HumanTaskPackageDownloadData downloadHumanTaskPackage(String packageName)
            throws PackageManagementException {

        File humanTaskArchive = getTenantTaskStore().getHumanTaskArchiveLocation(packageName);

        DataHandler handler;
        if (humanTaskArchive != null) {
            FileDataSource dataSource = new FileDataSource(humanTaskArchive);
            handler = new DataHandler(dataSource);

            HumanTaskPackageDownloadData data = new HumanTaskPackageDownloadData();
            data.setPackageName(humanTaskArchive.getName());
            data.setPackageFileData(handler);
            return data;
        } else {
            return null;
        }
    }

    public UndeployStatus_type0 undeployHumanTaskPackage(String packageName) {

        try {
            // We will only delete the zip file. The HumanTask Deployer's un-deploy method will
            // be executed. The un-deployment logic is written there.

            getTenantTaskStore().deleteHumanTaskArchive(packageName);
            getTenantTaskStore().updateTaskStatusForPackage(packageName, TaskPackageStatus.UNDEPLOYING);
        } catch (Exception ex) {
            log.error("Undeploy HumanTaskPackage operation failed", ex);
            return UndeployStatus_type0.FAILED;
        }
        return UndeployStatus_type0.SUCCESS;
    }

    // Returns the task store for the tenant.
    private HumanTaskStore getTenantTaskStore() {
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();

        HumanTaskServer server = HumanTaskServiceComponent.getHumanTaskServer();
        return server.getTaskStoreManager().getHumanTaskStore(tenantId);
    }
}
