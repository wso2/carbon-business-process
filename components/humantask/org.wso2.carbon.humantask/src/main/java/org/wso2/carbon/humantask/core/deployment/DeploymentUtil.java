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

import org.wso2.carbon.humantask.core.dao.TaskType;
import org.wso2.carbon.humantask.core.store.HumanTaskBaseConfiguration;
import org.wso2.carbon.humantask.core.store.NotificationConfiguration;
import org.wso2.carbon.humantask.core.store.TaskConfiguration;

import java.util.ArrayList;
import java.util.List;

/**
 * The utility class for converting SimpleTaskDefinitionInfo to HumanTaskBaseConfiguration object back and forth.
 */
public final class DeploymentUtil {
    private DeploymentUtil() {
    }

    /**
     * Converts the given list of HumanTaskBaseConfiguration objects to SimpleTaskDefinitionInfo.
     *
     * @param taskBaseConfigs :
     * @return :
     */
    public static List<SimpleTaskDefinitionInfo> getTaskConfigurationsInfoList(
            List<HumanTaskBaseConfiguration> taskBaseConfigs) {
        List<SimpleTaskDefinitionInfo> simpleTaskInfoList = new ArrayList<SimpleTaskDefinitionInfo>();

        for (HumanTaskBaseConfiguration taskConfig : taskBaseConfigs) {
            simpleTaskInfoList.add(getSimpleTaskDefinitionInfo(taskConfig));
        }
        return simpleTaskInfoList;
    }

    /**
     * Gets the SimpleTaskDefinitionInfo object for a given HumanTaskBaseConfiguration object.
     *
     * @param baseConfiguration : The object to be converted.
     * @return :
     */
    public static SimpleTaskDefinitionInfo getSimpleTaskDefinitionInfo(
            HumanTaskBaseConfiguration baseConfiguration) {

        SimpleTaskDefinitionInfo taskInfo = new SimpleTaskDefinitionInfo();
        taskInfo.setPackageName(baseConfiguration.getPackageName());
        taskInfo.setTaskName(baseConfiguration.getName().toString());
        taskInfo.setHumanTaskDefinitionFile(baseConfiguration.getHumanTaskDefinitionFile());
        if (baseConfiguration instanceof NotificationConfiguration) {
            taskInfo.setTaskType(TaskType.NOTIFICATION);
        } else if (baseConfiguration instanceof TaskConfiguration) {
            taskInfo.setTaskType(TaskType.TASK);
        }
        taskInfo.setPackageStatus(baseConfiguration.getPackageStatus());
        taskInfo.setErroneous( baseConfiguration.isErroneous());
        taskInfo.setDeploymentError(baseConfiguration.getDeploymentError());
        return taskInfo;
    }
}
