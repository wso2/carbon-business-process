/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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


package org.wso2.carbon.humantask.core.dao.jpa.openjpa.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.humantask.core.dao.*;
import org.wso2.carbon.humantask.core.dao.jpa.openjpa.model.Task;
import org.wso2.carbon.humantask.core.engine.runtime.api.HumanTaskRuntimeException;
import org.wso2.carbon.humantask.core.store.LeanTaskConfiguration;

import java.util.Date;


public class LeanTaskBuilderImpl {

    private static Log log = LogFactory.getLog(LeanTaskBuilderImpl.class);

    private LeanTaskCreationContext creationContext;
    private MessageDAO inputMessage;

    /**
     *
     * @param creationContext
     * @return
     */
    public LeanTaskBuilderImpl addTaskCreationContext(LeanTaskCreationContext creationContext) {
        this.creationContext = creationContext;
        return this;
    }

    /**
     *
     * @param inputMessage
     * @return
     */
    public LeanTaskBuilderImpl addInputMessage(MessageDAO inputMessage) {
        this.inputMessage = inputMessage;
        return this;
    }

    public TaskDAO build() {
        validateParams();
        TaskDAO task = null;
        LeanTaskConfiguration taskConfiguration = creationContext.getTaskConfiguration();
        int tenantId = creationContext.getTenantId();

        if (creationContext.getTaskConfiguration().isLeanTask()) {
            task = new Task(taskConfiguration.getName(), TaskType.LEAN_TASK, tenantId);
        }

        task.setInputMessage(this.inputMessage);
        task.setSkipable(false);
        task.setEscalated(false);
        task.setStatus(TaskStatus.CREATED);
        task.setActivationTime(new Date());
        task.setTaskVersion(taskConfiguration.getVersion());
        task.setTaskPackageName(taskConfiguration.getPackageName());
        task.setDefinitionName(taskConfiguration.getDefinitionName());

        return task;
    }
    //validate parameters
    private void validateParams() {
        if (inputMessage == null) {
            throw new HumanTaskRuntimeException("the input message cannot be null");
        }

        if (creationContext == null) {
            throw new HumanTaskRuntimeException("the task creation context cannot be null");
        }
    }
}
