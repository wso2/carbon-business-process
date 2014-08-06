/*
 * Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.apache.axis2.util.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.wso2.carbon.humantask.core.dao.*;
import org.wso2.carbon.humantask.core.engine.runtime.api.HumanTaskIllegalArgumentException;
import org.wso2.carbon.humantask.core.engine.runtime.api.HumanTaskRuntimeException;
import org.wso2.carbon.humantask.core.internal.HumanTaskServiceComponent;
import org.wso2.carbon.humantask.core.store.TaskConfiguration;
import org.wso2.carbon.humantask.core.utils.DOMUtils;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.List;

/**
 * Task completion logic.
 */
public class Complete extends AbstractHumanTaskCommand {
    private Element taskOutput;

    public Complete(String callerId, Long taskId, Element output) {
        super(callerId, taskId);
        if (output == null) {
            throw new HumanTaskIllegalArgumentException("The task output cannot be null.");
        }

        this.taskOutput = output;
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
        List<GenericHumanRoleDAO.GenericHumanRoleType> allowedRoles =
                new ArrayList<GenericHumanRoleDAO.GenericHumanRoleType>();
        allowedRoles.add(GenericHumanRoleDAO.GenericHumanRoleType.ACTUAL_OWNER);
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
        checkPostState(TaskStatus.COMPLETED);
    }

    /**
     * The method to execute the business logic for the specific command.
     */
    public void execute() {
        authorise();
        TaskDAO task = getTask();
        checkPreConditions();
        checkState();
        task.complete(createMessage());
        TaskConfiguration taskConf = (TaskConfiguration) HumanTaskServiceComponent.
                getHumanTaskServer().getTaskStoreManager().getHumanTaskStore(task.getTenantId()).
                getTaskConfiguration(QName.valueOf(task.getName()));
        try {
            taskConf.getCallBackService().invoke(XMLUtils.toOM(taskOutput), task.getId());
        } catch (Exception e) {
            throw new HumanTaskRuntimeException("Error occurred while invoking callback service", e);
        }
        processTaskEvent();
        checkPostConditions();
    }

    @Override
    protected EventDAO createTaskEvent() {
        EventDAO taskEvent = super.createTaskEvent();
        taskEvent.setDetails("");
        return taskEvent;
    }

    private MessageDAO createMessage() {
        MessageDAO output = getEngine().getDaoConnectionFactory().getConnection().createMessage();
        output.setMessageType(MessageDAO.MessageType.OUTPUT);
        output.setTask(getTask());
        Document doc = DOMUtils.newDocument();
        Element message = doc.createElement("message");
        doc.appendChild(message);

        Node importedNode = doc.importNode(taskOutput, true);
        message.appendChild(importedNode);

        output.setHeader(message);
        output.setData(message);
        return output;
    }
}
