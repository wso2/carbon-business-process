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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.humantask.cleanup.scheduler.util.TaskCleanupSchedulerUtil;
import org.wso2.carbon.humantask.core.HumanTaskEngineService;
import org.wso2.carbon.humantask.core.HumanTaskServer;
import org.wso2.carbon.ntask.core.service.TaskService;

@Component(
        name = "org.wso2.carbon.humantask.HumanTaskCleanupSchedulerServiceComponent",
        immediate = true)
public class HumanTaskCleanupSchedulerServiceComponent {

    private static Log log = LogFactory.getLog(HumanTaskCleanupSchedulerServiceComponent.class);

    @Activate
    protected void activate(ComponentContext ctxt) {

        try {
            log.info("Starting HumanTaskCleanupSchedulerServiceComponent");
            TaskCleanupSchedulerUtil.initTaskCleanupJob();
            log.debug("Started HumanTaskCleanupSchedulerServiceComponent");
        } catch (Throwable t) {
            log.error("Failed to activate the HumanTaskCleanupSchedulerServiceComponent", t);
        }
    }

    @Reference(
            name = "humantask.engine",
            service = org.wso2.carbon.humantask.core.HumanTaskEngineService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetHumanTaskServer")
    protected void setHumanTaskServer(HumanTaskEngineService humantaskEngineService) {

        if (log.isDebugEnabled()) {
            log.debug("HumanTaskEngineService bound to the HumanTask Cleanup component");
        }
        HumanTaskCleanupSchedulerContentHolder.getInstance().setHumanTaskServer(humantaskEngineService);
    }

    protected void unsetHumanTaskServer(HumanTaskEngineService humantaskEngineService) {

        if (log.isDebugEnabled()) {
            log.debug("HumanTaskServerService unbound from the HumanTask cleanup component");
        }
        HumanTaskCleanupSchedulerContentHolder.getInstance().setHumanTaskServer(null);
    }

    public static HumanTaskServer getHumanTaskServer() {

        return HumanTaskCleanupSchedulerContentHolder.getInstance().getHumanTaskServer();
    }

    @Reference(
            name = "ntask.component",
            service = org.wso2.carbon.ntask.core.service.TaskService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetTaskService")
    protected void setTaskService(TaskService taskService) {

        if (log.isDebugEnabled()) {
            log.debug("Setting the Task Service for Humantask Cleanup component");
        }
        HumanTaskCleanupSchedulerContentHolder.getInstance().setTaskService(taskService);
    }

    protected void unsetTaskService(TaskService taskService) {

        if (log.isDebugEnabled()) {
            log.debug("Unsetting the Task Service for Humantask Cleanup component");
        }
        HumanTaskCleanupSchedulerContentHolder.getInstance().setTaskService(null);
    }

    public static TaskService getTaskService() {

        return HumanTaskCleanupSchedulerContentHolder.getInstance().getTaskService();
    }
}
