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

package org.wso2.carbon.humantask.cleanup.scheduler.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.humantask.cleanup.scheduler.internal.HumanTaskCleanupSchedulerServiceComponent;
import org.wso2.carbon.humantask.cleanup.scheduler.ntask.RemovableTaskCleanupJob;
import org.wso2.carbon.humantask.core.HumanTaskConstants;
import org.wso2.carbon.humantask.core.configuration.HumanTaskServerConfiguration;
import org.wso2.carbon.humantask.core.engine.HumanTaskServerException;

import org.wso2.carbon.ntask.common.TaskException;
import org.wso2.carbon.ntask.core.TaskInfo;
import org.wso2.carbon.ntask.core.TaskManager;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Utility methods related to task clean up scheduler.
 */
public final class TaskCleanupSchedulerUtil {

    private static Log log = LogFactory.getLog(TaskCleanupSchedulerUtil.class);

    private TaskCleanupSchedulerUtil() {
    }

    /**
     * Initialises the task clean up task.
     *
     * @throws HumanTaskServerException :
     */
    public static void initTaskCleanupJob() throws HumanTaskServerException {

        HumanTaskServerConfiguration serverConfig = HumanTaskCleanupSchedulerServiceComponent.getHumanTaskServer().getServerConfig();

        if (serverConfig.isTaskCleanupEnabled()) {
            try {
                log.info("Initialising the task cleanup service.....");
                HumanTaskCleanupSchedulerServiceComponent.getTaskService().registerTaskType(HumanTaskConstants.HUMANTASK_TASK_TYPE);
                TaskManager taskManager = HumanTaskCleanupSchedulerServiceComponent.getTaskService().
                getTaskManager(HumanTaskConstants.HUMANTASK_TASK_TYPE);

                TaskInfo.TriggerInfo triggerInfo = new TaskInfo.TriggerInfo();
                triggerInfo.setCronExpression(serverConfig.getTaskCleanupCronExpression());
                triggerInfo.setDisallowConcurrentExecution(true);
                Map<String, String> propertyMap = new LinkedHashMap<String, String>();
                TaskInfo taskInfo = new TaskInfo(HumanTaskConstants.HUMANTASK_CLEANUP_JOB,
                        RemovableTaskCleanupJob.class.getName(), propertyMap, triggerInfo);
                taskManager.registerTask(taskInfo);
                taskManager.rescheduleTask(HumanTaskConstants.HUMANTASK_CLEANUP_JOB);
            } catch (TaskException ex) {
                String errMsg = "Error occurred while registering task type : " + HumanTaskConstants.HUMANTASK_TASK_TYPE;
                throw new HumanTaskServerException(errMsg, ex);
            }
        }
    }
}
