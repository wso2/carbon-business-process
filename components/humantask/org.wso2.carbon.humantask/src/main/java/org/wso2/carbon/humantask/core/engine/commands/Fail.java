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

import org.w3c.dom.Element;
import org.wso2.carbon.humantask.core.dao.EventDAO;
import org.wso2.carbon.humantask.core.dao.GenericHumanRoleDAO;
import org.wso2.carbon.humantask.core.dao.TaskDAO;
import org.wso2.carbon.humantask.core.dao.TaskStatus;
import org.wso2.carbon.humantask.core.internal.HumanTaskServerHolder;

import java.util.ArrayList;
import java.util.List;


/**
 *
 */
public class Fail extends AbstractHumanTaskCommand {
    private Element faultElement;

    private String faultName;


    public Fail(String callerId, Long taskId, String faultName, Element faultData) {
        super(callerId, taskId);
        this.faultElement = faultData;
        this.faultName = faultName;
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
        List<GenericHumanRoleDAO.GenericHumanRoleType> allowedRoles = new
                ArrayList<GenericHumanRoleDAO.GenericHumanRoleType>();
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
        checkPreState(TaskStatus.IN_PROGRESS);
    }

    /**
     * Checks the post-conditions after executing the task operation.
     */
    @Override
    protected void checkPostConditions() {
        checkPostState(TaskStatus.FAILED);
    }

    @Override
    protected EventDAO createTaskEvent() {
        EventDAO taskEvent = super.createTaskEvent();
        taskEvent.setDetails("");
        return taskEvent;
    }

    /**
     * The method to execute the business logic for the specific command.
     */
    @Override
    public void execute() {
        authorise();
        TaskDAO task = getTask();
        checkPreConditions();
        checkState();
        task.fail(faultName, faultElement);
        processTaskEvent();
        checkPostConditions();
    }
}
