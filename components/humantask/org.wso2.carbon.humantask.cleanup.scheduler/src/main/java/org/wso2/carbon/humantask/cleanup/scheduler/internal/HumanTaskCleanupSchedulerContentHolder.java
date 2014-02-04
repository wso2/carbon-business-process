/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.carbon.humantask.cleanup.scheduler.internal;

import org.wso2.carbon.humantask.core.HumanTaskServer;
import org.wso2.carbon.ntask.core.service.TaskService;

/**
 * Data holder for the HumanTaskServiceComponent
 */
public final class HumanTaskCleanupSchedulerContentHolder {
    private static HumanTaskCleanupSchedulerContentHolder instance;

    private HumanTaskServer humantaskServer;

    private TaskService taskService;

    private HumanTaskCleanupSchedulerContentHolder() {
    }

    public static HumanTaskCleanupSchedulerContentHolder getInstance() {
        if (instance == null) {
            instance = new HumanTaskCleanupSchedulerContentHolder();
        }
        return instance;
    }

    public HumanTaskServer getHumanTaskServer() {
        return humantaskServer;
    }

    public void setHumanTaskServer(
            org.wso2.carbon.humantask.core.HumanTaskEngineService humantaskEngineService) {
        if (humantaskEngineService == null) {
            this.humantaskServer = null;
        } else {
            this.humantaskServer = humantaskEngineService.getHumanTaskServer();
        }
    }

    public TaskService getTaskService() {
        return taskService;
    }

    public void setTaskService(TaskService taskService) {
        this.taskService = taskService;
    }
}
