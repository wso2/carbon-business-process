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

import org.wso2.carbon.humantask.core.dao.*;
import org.wso2.carbon.humantask.core.engine.runtime.api.HumanTaskIllegalAccessException;
import org.wso2.carbon.humantask.core.engine.util.OperationAuthorizationUtil;
import org.wso2.carbon.humantask.core.internal.HumanTaskServerHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * The release operation logic.
 */
public class Release extends AbstractHumanTaskCommand {

    public Release(String callerId, Long taskId) {
        super(callerId, taskId);
    }

    /**
     * Checks the Pre-conditions before executing the task operation.
     */
    @Override
    protected void checkPreConditions() {
        TaskDAO task = getTask();
        OrganizationalEntityDAO caller = getOperationInvoker();
        checkForValidTask();
        //if the task is in progress status we need to stop it first before releasing it!
        if (TaskStatus.IN_PROGRESS.equals(task.getStatus())) {

            List<GenericHumanRoleDAO.GenericHumanRoleType> allowedRoles = new ArrayList
                    <GenericHumanRoleDAO.GenericHumanRoleType>();
            allowedRoles.add(GenericHumanRoleDAO.GenericHumanRoleType.ACTUAL_OWNER);
            if (HumanTaskServerHolder.getInstance().getHtServer().getServerConfig().isTaskOperationsForBusinessAdministratorEnabled()) {
                allowedRoles.add(GenericHumanRoleDAO.GenericHumanRoleType.BUSINESS_ADMINISTRATORS);
            }

            if (!OperationAuthorizationUtil.authoriseUser(task, caller, allowedRoles,
                                                          getEngine().getPeopleQueryEvaluator())) {
                throw new HumanTaskIllegalAccessException(String.format("The user[%s] cannot perform [%s]" +
                                                                  " operation as he is not in task roles[%s]",
                                                                  caller.getName(), Release.class, allowedRoles));
            }

            task.stop();
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
        checkPreState(TaskStatus.RESERVED);
    }

    /**
     * Checks the post-conditions after executing the task operation.
     */
    @Override
    protected void checkPostConditions() {
        checkPostState(TaskStatus.READY);
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
        task.release();
        processTaskEvent();
        checkPostConditions();
    }
}
