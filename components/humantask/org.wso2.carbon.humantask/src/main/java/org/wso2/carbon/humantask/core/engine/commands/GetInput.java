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

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Element;
import org.wso2.carbon.humantask.core.dao.GenericHumanRoleDAO;
import org.wso2.carbon.humantask.core.dao.TaskDAO;
import org.wso2.carbon.humantask.core.engine.util.CommonTaskUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Get input operation.
 */
public class GetInput extends AbstractHumanTaskCommand {

    private Element inputElement;

    private String partName;

    public GetInput(String callerId, Long taskId, String partName) {
        super(callerId, taskId);
        this.partName = partName;
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
        allowedRoles.add(GenericHumanRoleDAO.GenericHumanRoleType.POTENTIAL_OWNERS);
        allowedRoles.add(GenericHumanRoleDAO.GenericHumanRoleType.BUSINESS_ADMINISTRATORS);
        allowedRoles.add(GenericHumanRoleDAO.GenericHumanRoleType.STAKEHOLDERS);
        allowedRoles.add(GenericHumanRoleDAO.GenericHumanRoleType.TASK_INITIATOR);
        allowedRoles.add(GenericHumanRoleDAO.GenericHumanRoleType.NOTIFICATION_RECIPIENTS);

        authoriseRoles(allowedRoles);
    }

    /**
     * Perform the state checks before executing the task operation.
     */
    @Override
    protected void checkState() {

    }

    /**
     * Checks the post-conditions after executing the task operation.
     */
    @Override
    protected void checkPostConditions() {

    }

    @Override
    public void execute() {
        authorise();
        TaskDAO task = getTask();
        checkPreConditions();
        checkState();
        if (StringUtils.isNotEmpty(partName)) {
            inputElement = CommonTaskUtil.getMessagePart(task.getInputMessage(), partName);

        } else {
            inputElement = (Element) task.getInputMessage().getBodyData().getFirstChild().getFirstChild();
        }
        checkPostConditions();
    }

    public Element getInputElement() {
        return inputElement;
    }
}
