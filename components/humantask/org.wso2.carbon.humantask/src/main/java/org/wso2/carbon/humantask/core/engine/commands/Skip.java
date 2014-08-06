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

import org.wso2.carbon.humantask.core.dao.EventDAO;
import org.wso2.carbon.humantask.core.dao.GenericHumanRoleDAO;
import org.wso2.carbon.humantask.core.dao.TaskDAO;
import org.wso2.carbon.humantask.core.dao.TaskStatus;
import org.wso2.carbon.humantask.core.engine.runtime.api.HumanTaskRuntimeException;
import org.wso2.carbon.humantask.core.internal.HumanTaskServiceComponent;
import org.wso2.carbon.humantask.core.store.TaskConfiguration;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.List;

/**
 * Skip operation.
 */
public class Skip extends AbstractHumanTaskCommand {
    public Skip(String callerId, Long taskId) {
        super(callerId, taskId);
    }

    /**
     * Checks the Pre-conditions before executing the task operation.
     */
    @Override
    protected void checkPreConditions() {
        TaskDAO task = getTask();
        checkForValidTask();
        if (!task.isSkipable()) {
            throw new HumanTaskRuntimeException(
                    String.format("The task[id:%d] is not a skippable task.", task.getId()));
        }
    }

    /**
     * Perform the authorization checks before executing the task operation.
     */
    @Override
    protected void authorise() {
        List<GenericHumanRoleDAO.GenericHumanRoleType> allowedRoles =
                new ArrayList<GenericHumanRoleDAO.GenericHumanRoleType>();
        allowedRoles.add(GenericHumanRoleDAO.GenericHumanRoleType.TASK_INITIATOR);
        allowedRoles.add(GenericHumanRoleDAO.GenericHumanRoleType.BUSINESS_ADMINISTRATORS);
        allowedRoles.add(GenericHumanRoleDAO.GenericHumanRoleType.STAKEHOLDERS);

        authoriseRoles(allowedRoles);
    }

    /**
     * Perform the state checks before executing the task operation.
     */
    @Override
    protected void checkState() {
        List<TaskStatus> allowedStates = new ArrayList<TaskStatus>();
        allowedStates.add(TaskStatus.CREATED);
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
        checkPostState(TaskStatus.OBSOLETE);
    }

    @Override
    protected EventDAO createTaskEvent() {
        EventDAO taskEvent = super.createTaskEvent();
        taskEvent.setDetails(" ");
        return taskEvent;
    }

    public void execute() {
        authorise();
        TaskDAO task = getTask();
        checkPreConditions();
        checkState();
        task.skip();
        sendSkipProtocolMessage(task);
        processTaskEvent();
        checkPostConditions();
    }

    private void sendSkipProtocolMessage(TaskDAO task) {
        // Sending Skip Protocol Message if Coordination is enabled
        if (HumanTaskServiceComponent.getHumanTaskServer().getServerConfig().isHtCoordinationEnabled()) {

            TaskConfiguration taskConf = (TaskConfiguration) HumanTaskServiceComponent.
                    getHumanTaskServer().getTaskStoreManager().getHumanTaskStore(task.getTenantId()).
                    getTaskConfiguration(QName.valueOf(task.getName()));
            try {
                taskConf.getCallBackService().invokeSkip(task.getId());
            } catch (Exception e) {
                throw new HumanTaskRuntimeException("Error occurred while sending skip protocol message to  callback service", e);
            }
        }
    }

}
