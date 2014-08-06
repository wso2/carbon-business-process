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
import org.wso2.carbon.humantask.core.engine.runtime.api.HumanTaskIllegalAccessException;
import org.wso2.carbon.humantask.core.engine.runtime.api.HumanTaskIllegalStateException;
import org.wso2.carbon.humantask.core.engine.util.OperationAuthorizationUtil;
import org.wso2.carbon.humantask.core.internal.HumanTaskServerHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * The start command
 */
public class Start extends AbstractHumanTaskCommand {
    private static final Log log = LogFactory.getLog(Start.class);

    public Start(String callerId, Long taskId) {
        super(callerId, taskId);
    }

    /**
     * If the task is in the READY state, we need to perform a claim operation before the task is started.
     */
    @Override
    protected void checkPreConditions() {
        TaskDAO task = getTask();
        OrganizationalEntityDAO caller = getOperationInvoker();
        checkForValidTask();

        if (TaskStatus.READY.equals(task.getStatus())) {
            List<GenericHumanRoleDAO.GenericHumanRoleType> allowedRoles = new ArrayList
                    <GenericHumanRoleDAO.GenericHumanRoleType>();
            allowedRoles.add(GenericHumanRoleDAO.GenericHumanRoleType.POTENTIAL_OWNERS);
            if (HumanTaskServerHolder.getInstance().getHtServer().getServerConfig().isTaskOperationsForBusinessAdministratorEnabled()) {
                allowedRoles.add(GenericHumanRoleDAO.GenericHumanRoleType.BUSINESS_ADMINISTRATORS);
            }
            if (!OperationAuthorizationUtil.authoriseUser(task, caller, allowedRoles,
                    getEngine().getPeopleQueryEvaluator())) {
                throw new HumanTaskIllegalAccessException(String.format("The user[%s] cannot perform [%s]" +
                        " operation as he is not in task roles[%s]",
                        caller.getName(), Claim.class, allowedRoles));
            }

            task.claim(caller);
            reloadTask();
        }
    }

    /**
     * Perform the authorization checks before executing the task operation.
     */
    @Override
    protected void authorise() {
        List<GenericHumanRoleDAO.GenericHumanRoleType> allowedRoles =
                new ArrayList<GenericHumanRoleDAO.GenericHumanRoleType>();
        allowedRoles.add(GenericHumanRoleDAO.GenericHumanRoleType.ACTUAL_OWNER);
        if (HumanTaskServerHolder.getInstance().getHtServer().getServerConfig().isTaskOperationsForBusinessAdministratorEnabled()) {
            allowedRoles.add(GenericHumanRoleDAO.GenericHumanRoleType.BUSINESS_ADMINISTRATORS);
        }
        authoriseRoles(allowedRoles);
    }

    /**
     * Perform the state checks before executing the task operation.
     */
    @Override
    protected void checkState() {
        TaskDAO task = getTask();
        if (!TaskStatus.RESERVED.equals(task.getStatus())) {
            String errMsg = String.format("User[%s] cannot perform [%s] operation on task[%d] as the task is in state[%s]. " +
                    "[%s] operation can be performed only on tasks in [%s] state",
                    getOperationInvoker().getName(), Start.class, task.getId(),
                    task.getStatus(), Start.class, TaskStatus.RESERVED);
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
        if (!TaskStatus.IN_PROGRESS.equals(task.getStatus())) {
            String errMsg = String.format("The task[id:%d] did not start successfully as " +
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
        checkPreConditions();
        authorise();
        TaskDAO task = getTask();
        checkState();
        task.start();
        processTaskEvent();
        checkPostConditions();
    }
}
