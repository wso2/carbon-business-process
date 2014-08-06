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
import org.wso2.carbon.humantask.core.engine.PeopleQueryEvaluator;
import org.wso2.carbon.humantask.core.engine.runtime.api.HumanTaskIllegalArgumentException;
import org.wso2.carbon.humantask.core.engine.runtime.api.HumanTaskIllegalStateException;
import org.wso2.carbon.humantask.core.engine.runtime.api.HumanTaskRuntimeException;

import java.util.ArrayList;
import java.util.List;

/**
 * task nomination operation.
 */
public class Nominate extends AbstractHumanTaskCommand {
    private static Log log = LogFactory.getLog(Nominate.class);

    private List<OrganizationalEntityDAO> nominees = new ArrayList<OrganizationalEntityDAO>();

    public Nominate(String callerId, Long taskId, List<OrganizationalEntityDAO> nominees) {
        super(callerId, taskId);
        if (nominees == null || nominees.size() < 1) {
            throw new HumanTaskRuntimeException("At least 1 nominee should be provided.");
        }

        this.nominees = nominees;
    }

    /**
     * Checks the Pre-conditions before executing the task operation.
     */
    @Override
    protected void checkPreConditions() {
        PeopleQueryEvaluator pqe = getEngine().getPeopleQueryEvaluator();
        pqe.checkOrgEntitiesExist(nominees);
        // Check for Excluded Owners.
        GenericHumanRoleDAO excludedOwnerRoles = getTask().getGenericHumanRole(GenericHumanRoleDAO.GenericHumanRoleType.EXCLUDED_OWNERS);
        if (excludedOwnerRoles != null) {

            List<OrganizationalEntityDAO> excludedOrgEntities = excludedOwnerRoles.getOrgEntities();
            //Case 1 : Matching Excluded OrgEntity List.
            for (OrganizationalEntityDAO nominee : nominees) {
                for (OrganizationalEntityDAO excludedOrgEntity : excludedOrgEntities) {
                    if (nominee.getOrgEntityType() == excludedOrgEntity.getOrgEntityType()
                            && nominee.getName().equals(excludedOrgEntity.getName())) {
                        String errMsg = String.format("The task nomination failed. One nominee is in the excluded Owner List for task " + getTask().getId() + ".");
                        log.error(errMsg);
                        throw new HumanTaskIllegalArgumentException(errMsg);
                    }
                }
            }

            //Case 2: Checking OrgEntity Users in Excluded OrgEntity Groups.
            for (OrganizationalEntityDAO nominee : nominees) {
                if (nominee.getOrgEntityType() == OrganizationalEntityDAO.OrganizationalEntityType.USER) {
                    List<String> roleNameListForUser = getEngine().getPeopleQueryEvaluator().getRoleNameListForUser(nominee.getName());
                    for (OrganizationalEntityDAO excludedOrgEntity : excludedOrgEntities) {
                        if (excludedOrgEntity.getOrgEntityType() == OrganizationalEntityDAO.OrganizationalEntityType.GROUP
                                && roleNameListForUser.contains(excludedOrgEntity.getName())) {
                            String errMsg = String.format("The task nomination failed. One nominee is in an excluded Owner Group for task " + getTask().getId() + ".");
                            log.error(errMsg);
                            throw new HumanTaskIllegalArgumentException(errMsg);
                        }
                    }
                }
            }
        }
    }

    /**
     * Perform the authorization checks before executing the task operation.
     */
    @Override
    protected void authorise() {
        List<GenericHumanRoleDAO.GenericHumanRoleType> allowedRoles =
                new ArrayList<GenericHumanRoleDAO.GenericHumanRoleType>();
        allowedRoles.add(GenericHumanRoleDAO.GenericHumanRoleType.BUSINESS_ADMINISTRATORS);
        authoriseRoles(allowedRoles);
    }

    /**
     * Perform the state checks before executing the task operation.
     */
    @Override
    protected void checkState() {
        checkPreState(TaskStatus.CREATED);
    }

    /**
     * Checks the post-conditions after executing the task operation.
     */
    @Override
    protected void checkPostConditions() {
        TaskDAO task = getTask();
        if (!(TaskStatus.RESERVED.equals(task.getStatus()) || TaskStatus.READY.equals(task.getStatus()))) {
            String errMsg = String.format("The task nomination failed. Task status is not in Reserved or Ready.");
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

    /**
     * The method to execute the business logic for the specific command.
     */
    @Override
    public void execute() {
        authorise();
        TaskDAO task = getTask();
        checkPreConditions();
        checkState();
        task.nominate(nominees);
        processTaskEvent();
        checkPostConditions();
    }
}
