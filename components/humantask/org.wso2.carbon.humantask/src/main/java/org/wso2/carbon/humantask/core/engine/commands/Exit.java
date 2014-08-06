/*
 * Copyright (c) 2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.humantask.core.engine.commands;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.humantask.core.dao.EventDAO;
import org.wso2.carbon.humantask.core.dao.TaskDAO;
import org.wso2.carbon.humantask.core.dao.TaskStatus;
import org.wso2.carbon.humantask.core.engine.runtime.api.HumanTaskIllegalStateException;
import org.wso2.carbon.humantask.core.engine.runtime.api.HumanTaskRuntimeException;

/**
 * Exit operation logic.
 */
public class Exit extends AbstractHumanTaskCommand {

    private static final Log log = LogFactory.getLog(Claim.class);

    public Exit(String callerId, Long taskId) {
        super(callerId, taskId);
    }

    /**
     * Checks the Pre-conditions before executing the task operation.
     */
    @Override
    protected void checkPreConditions() {

    }

    /**
     * Perform the authorization checks before executing the task operation.
     */
    @Override
    protected void authorise() {

    }

    /**
     * Perform the state checks before executing the task operation.
     */
    @Override
    protected void checkState() {
        TaskDAO task = getTask();
        boolean isInFinalState = false;
        if (TaskStatus.EXITED.equals(task.getStatus()) || TaskStatus.ERROR.equals(task.getStatus())
                || TaskStatus.FAILED.equals(task.getStatus()) || TaskStatus.OBSOLETE.equals(task.getStatus()) ||
                TaskStatus.COMPLETED.equals(task.getStatus())) {
            isInFinalState = true;
        }
        if (isInFinalState) {
            String errMsg = String.format("User[%s] cannot perform [%s] operation on task[%d] as the task is in state[%s]. " +
                    "[%s] operation can be performed only on tasks not in states[%s,%s,%s,%s,%s]",
                    getOperationInvoker().getName(), Exit.class, task.getId(),
                    task.getStatus(), Exit.class, TaskStatus.EXITED,
                    TaskStatus.ERROR, TaskStatus.FAILED, TaskStatus.OBSOLETE,
                    TaskStatus.COMPLETED);
            log.error(errMsg);
            throw new HumanTaskIllegalStateException(errMsg);
        }
    }

    /**
     * Checks the post-conditions after executing the task operation.
     */
    @Override
    protected void checkPostConditions() {
        TaskDAO task = getTask();
        if (!TaskStatus.EXITED.equals(task.getStatus())) {
            String errMsg = String.format("The task[id:%d] did not exit successfully as " +
                    "it's state is still in [%s]", task.getId(), task.getStatus());
            log.error(errMsg);
            throw new HumanTaskIllegalStateException(errMsg);
        }
    }

    @Override
    protected EventDAO createTaskEvent() {
        EventDAO taskEvent = super.createTaskEvent();
        taskEvent.setDetails("");
        return taskEvent;
    }

    @Override
    public void execute() {
        authorise();
        TaskDAO task = getTask();
        checkPreConditions();
        checkState();
        task.exit();
        processTaskEvent();
        checkPostConditions();
    }
}
