/*
 * Copyright (c) 2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.wso2.carbon.humantask.core.engine.runtime.api.HumanTaskIllegalOperationException;
import org.wso2.carbon.humantask.core.internal.HumanTaskServerHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * Task Claim Operation.
 */
public class Claim extends AbstractHumanTaskCommand {

    private static final Log log = LogFactory.getLog(Claim.class);

    public Claim(String callerId, Long taskId) {
        super(callerId, taskId);
    }

    @Override
    protected void checkPreConditions() {
        checkForValidTask();
        TaskDAO task = getTask();
        for (GenericHumanRoleDAO humanRole : task.getHumanRoles()) {
            if (GenericHumanRoleDAO.GenericHumanRoleType.ACTUAL_OWNER.equals(humanRole.getType())
                && humanRole.getOrgEntities().size() > 0) {
                throw new HumanTaskIllegalOperationException(String.format("The task[%d] already has an actual" +
                                                                  " owner[%s]", task.getId(),
                                                                  humanRole.getOrgEntities()));
            }
        }
    }

    @Override
    protected void authorise() {
        List<GenericHumanRoleDAO.GenericHumanRoleType> allowedRoles = new ArrayList<GenericHumanRoleDAO.GenericHumanRoleType>();
        allowedRoles.add(GenericHumanRoleDAO.GenericHumanRoleType.POTENTIAL_OWNERS);
        if (HumanTaskServerHolder.getInstance().getHtServer().getServerConfig().isTaskOperationsForBusinessAdministratorEnabled()) {
            allowedRoles.add(GenericHumanRoleDAO.GenericHumanRoleType.BUSINESS_ADMINISTRATORS);
        }
        authoriseRoles(allowedRoles);
    }

    @Override
    protected void checkState() {
        checkPreState(TaskStatus.READY);
    }

    @Override
    protected void checkPostConditions() {

    }

    @Override
    protected EventDAO createTaskEvent() {
        EventDAO taskEvent = super.createTaskEvent();
        taskEvent.setDetails("");
        return taskEvent;
    }

    @Override
    public void execute() {
        OrganizationalEntityDAO caller = getOperationInvoker();
        TaskDAO task = getTask();
        if (log.isDebugEnabled()) {
            log.debug(String.format("User[%s] claiming task[%d]", caller.getName(), task
                    .getId()));
        }

        authorise();
        checkPreConditions();
        checkState();
        task.claim(caller);
        processTaskEvent();
        checkPostConditions();
    }
}
