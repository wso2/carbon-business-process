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

package org.wso2.carbon.humantask.core.deployment;

import org.wso2.carbon.humantask.*;
import org.wso2.carbon.humantask.core.dao.DeploymentUnitDAO;
import org.wso2.carbon.humantask.core.dao.TaskPackageStatus;
import org.wso2.carbon.humantask.core.dao.TaskStatus;
import org.wso2.carbon.humantask.core.deployment.config.HTDeploymentConfigDocument;
import org.wso2.carbon.humantask.core.deployment.config.THTDeploymentConfig;
import org.wso2.carbon.humantask.core.store.HumanTaskArtifactContentType;
import org.wso2.carbon.humantask.skeleton.mgt.services.types.TaskStatusType;

import javax.wsdl.Definition;
import javax.xml.namespace.QName;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Contains all the tasks and notifications coming from a single human task artifact. In addition
 * to task definitions this also contains wsdls, xml schemas and human task artifact meta-data.
 */
public class HumanTaskDeploymentUnit {

    private HumanInteractionsDocument humanInteraction;

    private HTDeploymentConfigDocument deploymentConfiguration;

    private List<Definition> wsdls;

    // Versioned artifact name
    private String name;
    // Un-versioned artifact
    private String packageName;

    private long version;

    private String md5sum;

    private String targetNamespace;

    private File humanTaskDefinitionFile;

    private TaskPackageStatus taskPackageStatus;

    public void setHumanInteractionsDefinition(HumanInteractionsDocument hiDefinition) {
        this.humanInteraction = hiDefinition;
        this.targetNamespace = hiDefinition.getHumanInteractions().getTargetNamespace();
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setWSDLs(List<Definition> wsdls) {
        this.wsdls = wsdls;
    }

    public void setDeploymentConfiguration(HTDeploymentConfigDocument configDocument) {
        this.deploymentConfiguration = configDocument;
    }

    public String getName() {
        return name;
    }

    public HumanInteractionsDocument getHumanInteractionsDefinition() {
        return humanInteraction;
    }

    public List<Definition> getWSDLs() {
        return wsdls;
    }

    public String getNamespace() {
        return targetNamespace;
    }

    public HTDeploymentConfigDocument getDeploymentConfiguration() {
        return deploymentConfiguration;
    }

    public TTask[] getTasks() {
        if (humanInteraction.getHumanInteractions().isSetTasks() &&
                humanInteraction.getHumanInteractions().getTasks().sizeOfTaskArray() > 0) {
            return humanInteraction.getHumanInteractions().getTasks().getTaskArray();
        }
        return null;
    }

    public THTDeploymentConfig.Task getTaskServiceInfo(QName taskName) {
        for (THTDeploymentConfig.Task task :
                deploymentConfiguration.getHTDeploymentConfig().getTaskArray()) {
            if (task.getName().equals(taskName)) {
                return task;
            }
        }
        return null;
    }

    public THTDeploymentConfig.Notification getNotificationServiceInfo(QName notificationName) {
        for (THTDeploymentConfig.Notification notification :
                deploymentConfiguration.getHTDeploymentConfig().getNotificationArray()) {
            if (notification.getName().equals(notificationName)) {
                return notification;
            }
        }
        return null;
    }

    public TNotification[] getNotifications() {
        if (humanInteraction.getHumanInteractions().isSetNotifications() &&
                humanInteraction.getHumanInteractions().getNotifications().sizeOfNotificationArray() > 0) {
            return humanInteraction.getHumanInteractions().getNotifications().getNotificationArray();
        }
        return null;
    }


    public File getHumanTaskDefinitionFile() {
        return humanTaskDefinitionFile;
    }

    public void setHumanTaskDefinitionFile(File humanTaskDefinitionFile) {
        this.humanTaskDefinitionFile = humanTaskDefinitionFile;
    }

    public List<TNotification> getInlineNotifications() {
        List<TNotification> notifications = new ArrayList<TNotification>();
        TTask[] tasks = getTasks();
        if (tasks == null) {
            return notifications;
        }
        for (TTask task : tasks) {
            if (task.isSetDeadlines()) {
                TDeadlines deadlines = task.getDeadlines();
                TDeadline[] deadlineArray = deadlines.getStartDeadlineArray();
                if (deadlineArray != null) {
                    for (TDeadline deadline : deadlineArray) {
                        for (TEscalation escalation : deadline.getEscalationArray()) {
                            if (escalation.isSetNotification()) {
                                notifications.add(escalation.getNotification());
                            }
                        }
                    }
                }
                deadlineArray = deadlines.getCompletionDeadlineArray();
                if (deadlineArray != null) {
                    for (TDeadline deadline : deadlineArray) {
                        for (TEscalation escalation : deadline.getEscalationArray()) {
                            if (escalation.isSetNotification()) {
                                notifications.add(escalation.getNotification());
                            }
                        }
                    }
                }
            }
        }
        return notifications;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public String getMd5sum() {
        return this.md5sum;
    }
    public void setMd5sum(String md5sum) {
        this.md5sum = md5sum;
    }

    public TaskPackageStatus getTaskPackageStatus() {
        return taskPackageStatus;
    }

    public void setTaskPackageStatus(TaskPackageStatus taskPackageStatus) {
        this.taskPackageStatus = taskPackageStatus;
    }

}
