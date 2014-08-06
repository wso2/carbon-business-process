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
import org.wso2.carbon.humantask.core.engine.runtime.api.HumanTaskIllegalArgumentException;
import org.wso2.carbon.humantask.core.engine.runtime.api.HumanTaskRuntimeException;

import java.util.ArrayList;
import java.util.List;

/**
 * Delegate operation business logic.
 */
public class Delegate extends AbstractHumanTaskCommand {

    private static final Log log = LogFactory.getLog(Delegate.class);

    private OrganizationalEntityDAO delegatee;

    public Delegate(String callerId, Long taskId, OrganizationalEntityDAO delegatee) {
        super(callerId, taskId);
        this.delegatee = delegatee;
    }

    /**
     * Checks the Pre-conditions before executing the task operation.
     */
    @Override
    protected void checkPreConditions() {

        checkForValidTask();
        OrganizationalEntityDAO caller = getOperationInvoker();
        TaskDAO task = getTask();
        //if the delegatee is not an existing user
        if (!getEngine().getPeopleQueryEvaluator().isExistingUser(delegatee.getName())) {
            String errMsg = String.format("The user[%s] cannot delegate task[id:%d] to the given" +
                    " delegatee[name:%s] as he/she does not exist in the user store",
                    caller.getName(), task.getId(), delegatee.getName());
            log.error(errMsg);
            throw new HumanTaskIllegalArgumentException(errMsg);
        }
        if (isExcludedOwner(delegatee.getName())) {
            String errMsg = String.format("The user[%s] cannot delegate task[id:%d] to the given" +
                    " delegatee[name:%s] as he/she is an exclude owner for this task.",
                    caller.getName(), task.getId(), delegatee.getName());
            log.error(errMsg);
            throw new HumanTaskIllegalArgumentException(errMsg);
        }
        //if the task is in reserved or in-progress we have to release it first.
        if (TaskStatus.RESERVED.equals(task.getStatus()) || TaskStatus.IN_PROGRESS.equals(task.getStatus())) {

            //task releasing can be done only by bus admins and the actual owner.
            List<GenericHumanRoleDAO.GenericHumanRoleType> allowedRoles = new
                    ArrayList<GenericHumanRoleDAO.GenericHumanRoleType>();
            allowedRoles.add(GenericHumanRoleDAO.GenericHumanRoleType.BUSINESS_ADMINISTRATORS);
            allowedRoles.add(GenericHumanRoleDAO.GenericHumanRoleType.ACTUAL_OWNER);
            try {
                authoriseRoles(allowedRoles);
            } catch (Exception ex) {
                String err = String.format("The task[id:%d] can be only delegated after it's released. " +
                        "But for the task to be released you need to be a business " +
                        "administrator or the actual owner of the task. " +
                        "Given user[%s] is not in those roles!",
                        task.getId(), caller.getName());
                log.error(err);
                throw new HumanTaskIllegalAccessException(err, ex);
            }
            task.release();
        }

        GenericHumanRoleDAO potentialOwnersRole = task.getGenericHumanRole(
                GenericHumanRoleDAO.GenericHumanRoleType.POTENTIAL_OWNERS);

        if (getEngine().getPeopleQueryEvaluator().isOrgEntityInRole(delegatee,
                potentialOwnersRole)) {
            task.persistToPotentialOwners(delegatee);
        }
    }

    /**
     * Perform the authorization checks before executing the task operation.
     */
    @Override
    protected void authorise() {
        List<GenericHumanRoleDAO.GenericHumanRoleType> allowedRoles = new
                ArrayList<GenericHumanRoleDAO.GenericHumanRoleType>();
        allowedRoles.add(GenericHumanRoleDAO.GenericHumanRoleType.POTENTIAL_OWNERS);
        allowedRoles.add(GenericHumanRoleDAO.GenericHumanRoleType.STAKEHOLDERS);
        allowedRoles.add(GenericHumanRoleDAO.GenericHumanRoleType.ACTUAL_OWNER);
        allowedRoles.add(GenericHumanRoleDAO.GenericHumanRoleType.BUSINESS_ADMINISTRATORS);

        authoriseRoles(allowedRoles);
    }

    /**
     * Perform the state checks before executing the task operation.
     */
    @Override
    protected void checkState() {
        List<TaskStatus> allowedStates = new ArrayList<TaskStatus>();
        allowedStates.add(TaskStatus.READY);
        allowedStates.add(TaskStatus.RESERVED);
        allowedStates.add(TaskStatus.IN_PROGRESS);
        checkPreStates(allowedStates);
    }

    /**
     * Checks the post-conditions after executing the task operation.
     */
    @Override
    protected void checkPostConditions() {
        checkPostState(TaskStatus.RESERVED);
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
        task.delegate(delegatee);
        processTaskEvent();
        checkPostConditions();
    }
}
