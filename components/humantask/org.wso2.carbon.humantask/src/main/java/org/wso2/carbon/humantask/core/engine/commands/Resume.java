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
import org.wso2.carbon.humantask.core.dao.*;
import org.wso2.carbon.humantask.core.engine.runtime.api.HumanTaskIllegalStateException;
import org.wso2.carbon.humantask.core.engine.runtime.api.HumanTaskRuntimeException;

import java.util.ArrayList;
import java.util.List;

/**
 * Resume operation.
 */
public class Resume extends AbstractHumanTaskCommand {
    private static final Log log = LogFactory.getLog(Start.class);

    public Resume(String callerId, Long taskId) {
        super(callerId, taskId);
    }

    /**
     * Checks the Pre-conditions before executing the task operation.
     */
    @Override
    protected void checkPreConditions() {
        checkForValidTask();
    }

    /**
     * Perform the authorization checks before executing the task operation.
     */
    @Override
    protected void authorise() {
        List<GenericHumanRoleDAO.GenericHumanRoleType> allowedRoles =
                new ArrayList<GenericHumanRoleDAO.GenericHumanRoleType>();
        allowedRoles.add(GenericHumanRoleDAO.GenericHumanRoleType.ACTUAL_OWNER);
        allowedRoles.add(GenericHumanRoleDAO.GenericHumanRoleType.BUSINESS_ADMINISTRATORS);
        allowedRoles.add(GenericHumanRoleDAO.GenericHumanRoleType.STAKEHOLDERS);
        allowedRoles.add(GenericHumanRoleDAO.GenericHumanRoleType.POTENTIAL_OWNERS);

        authoriseRoles(allowedRoles);
    }

    /**
     * Perform the state checks before executing the task operation.
     */
    @Override
    protected void checkState() {
        TaskDAO task = getTask();
        OrganizationalEntityDAO caller = getOperationInvoker();
        if (!TaskStatus.SUSPENDED.equals(task.getStatus())) {
            String errMsg = String.format("User[%s] cannot [%s] task[%d] as the task is in state[%s]. " +
                    "Only tasks in [%s] state can be resumed!",
                    caller.getName(), Resume.class, task.getId(),
                    task.getStatus(), TaskStatus.SUSPENDED);
            log.error(errMsg);
            throw new HumanTaskIllegalStateException(errMsg);
        }


        boolean isInResumableState = false;
        if (TaskStatus.IN_PROGRESS.equals(task.getStatusBeforeSuspension()) ||
                TaskStatus.READY.equals(task.getStatusBeforeSuspension()) ||
                TaskStatus.RESERVED.equals(task.getStatusBeforeSuspension())) {
            isInResumableState = true;
        }
        if (!isInResumableState) {
            String errMsg = String.format("User[%s] cannot perform [%s] operation on task[%d] as the task is in state[%s]. " +
                    "[%s] operation can be performed only on tasks in states[%s,%s,%s]",
                    caller.getName(), Suspend.class, task.getId(),
                    task.getStatus(), Suspend.class, TaskStatus.RESERVED,
                    TaskStatus.READY, TaskStatus.IN_PROGRESS);
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
        boolean isInSuspendableState = false;
        if (TaskStatus.IN_PROGRESS.equals(task.getStatus()) ||
                TaskStatus.READY.equals(task.getStatus()) ||
                TaskStatus.RESERVED.equals(task.getStatus())) {
            isInSuspendableState = true;
        }
        if (!isInSuspendableState) {
            String errMsg = String.format("The task[id:%d] did not resume successfully as " +
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
        task.resume();
        processTaskEvent();
        checkPostConditions();
    }
}
