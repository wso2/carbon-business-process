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

package org.wso2.carbon.humantask.core.deployment;

import org.wso2.carbon.humantask.core.dao.TaskPackageStatus;
import org.wso2.carbon.humantask.core.dao.TaskType;

import java.io.File;
import java.util.Date;

/**
 * A  POJO representing the basic information regarding a human task definition.
 * This will be used especially to display information about the task definition.
 */
public class SimpleTaskDefinitionInfo {

    /**
     * The name of the package *
     */
    private String packageName;

    /**
     * The deployment date of the package *
     */
    private Date deployedDate;

    /**
     * The name of the task definition
     * NOTE: This will return the task's QName as a string.
     */
    private String taskName;

    /**
     * The name of the package *
     */
    private TaskType taskType;

    /**
     * The human task definition file.
     */
    private File humanTaskDefinitionFile;

    /**
     * The status of the package containing this task definition
     */
    private TaskPackageStatus packageStatus;

    /**
     * boolean flag to indicate the error status of the task def.
     */
    private boolean erroneous;

    /**
     * Deployment errors if there are any.
     */
    private String deploymentError;


    /**
     * @return : The package name.
     */
    public String getPackageName() {
        return packageName;
    }

    /**
     * @param packageName : The package name to set.
     */
    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    /**
     * @return : The deployment date.
     */
    public Date getDeployedDate() {
        if (deployedDate != null) {
            return (Date) deployedDate.clone();
        }
        return null;
    }

    /**
     * @param deployedDate : The deployment date to set.
     */
    public void setDeployedDate(Date deployedDate) {
        if (deployedDate != null) {
            this.deployedDate = (Date) deployedDate.clone();
        } else {
            this.deployedDate = null;
        }
    }

    /**
     * @return : The task name.
     *         NOTE: This will return the task's QName as a string.
     */
    public String getTaskName() {
        return taskName;
    }

    /**
     * @param taskName : The task name to set.
     */
    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    /**
     * @return : The task type.
     */
    public TaskType getTaskType() {
        return taskType;
    }

    /**
     * @param taskType : The task type to set.
     */
    public void setTaskType(TaskType taskType) {
        this.taskType = taskType;
    }

    /**
     * @return : The human task definition file.
     */
    public File getHumanTaskDefinitionFile() {
        return humanTaskDefinitionFile;
    }

    /**
     * @param humanTaskDefinitionFile : The human task definition file to set.
     */
    public void setHumanTaskDefinitionFile(File humanTaskDefinitionFile) {
        this.humanTaskDefinitionFile = humanTaskDefinitionFile;
    }

    /**
     * @return : The status of the package containing this definition.
     */
    public TaskPackageStatus getPackageStatus() {
        return packageStatus;
    }

    /**
     *
     * @param packageStatus : The status of the package.to set.
     */
    public void setPackageStatus(TaskPackageStatus packageStatus) {
        this.packageStatus = packageStatus;
    }

    /**
     * @return : true if the task configuration failed to deploy.
     */
    public boolean isErroneous() {
        return erroneous;
    }

    /**
     * @param erroneous : the error status
     */
    public void setErroneous(boolean erroneous) {
        this.erroneous = erroneous;
    }

    /**
     * @return : The deployment error if there's any.
     */
    public String getDeploymentError() {
        return deploymentError;
    }

    /**
     * @param deploymentError : The deployment error to set
     */
    public void setDeploymentError(String deploymentError) {
        this.deploymentError = deploymentError;
    }
}
