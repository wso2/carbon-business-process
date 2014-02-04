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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.humantask.core.api.event.TaskEventInfo;
import org.wso2.carbon.humantask.core.dao.EventDAO;
import org.wso2.carbon.humantask.core.dao.GenericHumanRoleDAO;
import org.wso2.carbon.humantask.core.dao.OrganizationalEntityDAO;
import org.wso2.carbon.humantask.core.dao.TaskDAO;
import org.wso2.carbon.humantask.core.dao.TaskEventType;
import org.wso2.carbon.humantask.core.dao.TaskStatus;
import org.wso2.carbon.humantask.core.dao.TaskType;
import org.wso2.carbon.humantask.core.engine.HumanTaskCommand;
import org.wso2.carbon.humantask.core.engine.HumanTaskEngine;
import org.wso2.carbon.humantask.core.engine.runtime.api.HumanTaskIllegalAccessException;
import org.wso2.carbon.humantask.core.engine.runtime.api.HumanTaskIllegalArgumentException;
import org.wso2.carbon.humantask.core.engine.runtime.api.HumanTaskIllegalOperationException;
import org.wso2.carbon.humantask.core.engine.runtime.api.HumanTaskIllegalStateException;
import org.wso2.carbon.humantask.core.engine.runtime.api.HumanTaskRuntimeException;
import org.wso2.carbon.humantask.core.engine.util.CommonTaskUtil;
import org.wso2.carbon.humantask.core.engine.util.OperationAuthorizationUtil;
import org.wso2.carbon.humantask.core.internal.HumanTaskServiceComponent;

import java.util.List;

/**
 * Abstract class for HumanTaskCommand. Contains the common operations and properties
 */
public abstract class AbstractHumanTaskCommand implements HumanTaskCommand {
    private static final Log log = LogFactory.getLog(Claim.class);

    /**
     * The human task engine
     */
    private HumanTaskEngine engine;

    /**
     * The task related to this operation
     */
    private TaskDAO task;

    /**
     * The caller of the operation.
     */
    private OrganizationalEntityDAO operationInvoker;

    /**
     * The task event related to this command.
     */
    private EventDAO event;

    protected HumanTaskEngine getEngine() {
        return engine;
    }

    protected TaskDAO getTask() {
        return task;
    }

    protected OrganizationalEntityDAO getOperationInvoker() {
        return operationInvoker;
    }

    protected EventDAO getEvent() {
        return event;
    }

    /**
     * @param invokerUserName : The user name of the operation invoker.
     * @param taskId          : The task id.
     */
    public AbstractHumanTaskCommand(String invokerUserName, Long taskId) {
        engine = HumanTaskServiceComponent.getHumanTaskServer().getTaskEngine();
        validateCaller(invokerUserName);
        this.operationInvoker = engine.getDaoConnectionFactory().getConnection().
                createNewOrgEntityObject(invokerUserName, OrganizationalEntityDAO.OrganizationalEntityType.USER);
        this.task = engine.getDaoConnectionFactory().getConnection().getTask(taskId);
        validateTenant(invokerUserName);
        if(this.task != null) {
            this.event = engine.getDaoConnectionFactory().getConnection().createNewEventObject(task);
        }

    }

    //checks the method caller is an actual user existing in the user store.
    private void validateCaller(String callerId) {
        if (StringUtils.isEmpty(callerId) || !engine.getPeopleQueryEvaluator().isExistingUser(callerId)) {
            String errMsg = "The caller[name:" + callerId + "] is not a valid user in the user store.";
            log.error(errMsg);
            throw new HumanTaskIllegalArgumentException(errMsg);
        }
    }

    private void validateTenant(String callerId) {
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        if (this.task != null) {
            if (tenantId == task.getTenantId()) {
                return;
            } else {
                log.error(callerId + " can't perform other tenant's task");
                throw new HumanTaskIllegalAccessException("Access Denied. You are not authorized to perform this task");
            }
        }
    }

    /**
     * Checks whether the this task is a valid task of TASK.
     */
    public void checkForValidTask() {
        if (task == null) {
            throw new HumanTaskRuntimeException("The task is not loaded properly");
        }

        if (!TaskType.TASK.equals(task.getType())) {
            String errMsg = String.format("The task[%d] is a notification, hence cannot perform [%s].",
                                          task.getId(), this.getClass().getSimpleName());
            log.error(errMsg);
            throw new HumanTaskIllegalArgumentException(errMsg);
        }
    }

    /**
     * Checks whether the this task is a valid task of TASK.
     */
    public void checkForValidNotification() {
        if (task == null) {
            throw new HumanTaskRuntimeException("The task is not loaded properly");
        }

        if (!TaskType.NOTIFICATION.equals(task.getType())) {

            String errMsg = String.format("The task[%d] is a task, hence cannot perform [%s].",
                                          task.getId(), this.getClass().getSimpleName());
            log.error(errMsg);
            throw new HumanTaskIllegalOperationException(errMsg);
        }
    }

    /**
     * Checks the post state of a task after a command execution
     *
     * @param expectedStatus : The expected post state.
     */
    protected void checkPostState(TaskStatus expectedStatus) {
        if (!expectedStatus.equals(task.getStatus())) {
            String errMsg = String.format("Operation [%s] was not successfully performed on task[id: %d]" +
                                          " as it's state is still in[%s]", this.getClass().getSimpleName(),
                                          task.getId(), task.getStatus());
            log.error(errMsg);
            throw new HumanTaskIllegalStateException(errMsg);
        }
    }

    /**
     * A common method shared across all the commands to check the expected state of the
     * task before the task operation is performed.
     *
     * @param expectedStatus : The expected pre state.
     */
    protected void checkPreState(TaskStatus expectedStatus) {
        if (!expectedStatus.equals(task.getStatus())) {
            String errMsg = String.format("User[%s] cannot [%s] task[id:%d] as the task is in state[%s]. " +
                                          "[%s] operation can be performed on tasks in state[%s]!",
                                          operationInvoker.getName(), this.getClass().getSimpleName(), task.getId(),
                                          task.getStatus(), this.getClass().getSimpleName(),
                                          expectedStatus);
            log.error(errMsg);
            throw new HumanTaskIllegalStateException(errMsg);
        }
    }

    /**
     * A common method shared across all the commands to check the expected state of the
     * task before the task operation is performed.
     *
     * @param expectedStates : The expected pre states.
     */
    protected void checkPreStates(List<TaskStatus> expectedStates) {
        if (!expectedStates.contains(task.getStatus())) {
            String errMsg = String.format("User[%s] cannot [%s] task[id:%d] as the task is in state[%s]. " +
                                          "[%s] operation can be performed on tasks in states[%s]!",
                                          operationInvoker.getName(), this.getClass().getSimpleName(), task.getId(),
                                          task.getStatus(), this.getClass().getSimpleName(),
                                          expectedStates);
            log.error(errMsg);
            throw new HumanTaskIllegalStateException(errMsg);
        }
    }

    protected void authoriseRoles(List<GenericHumanRoleDAO.GenericHumanRoleType> allowedRoles) {
        if (!OperationAuthorizationUtil.authoriseUser(this.task, operationInvoker, allowedRoles,
                                                      engine.getPeopleQueryEvaluator())) {
            String errorMsg = String.format("The user[%s] cannot perform [%s]" +
                    " operation as either he is in EXCLUDED_OWNERS role or he is not in task roles [%s]",
                    operationInvoker.getName(), this.getClass().getSimpleName(),
                    allowedRoles);
            log.error(errorMsg);
            throw new HumanTaskIllegalAccessException("Access Denied. You are not authorized to perform this task");
        }
    }

    protected void reloadTask() {
        this.task = engine.getDaoConnectionFactory().getConnection().getTask(task.getId());
    }

    /**
     * Checks the Pre-conditions before executing the task operation.
     */
    protected abstract void checkPreConditions();

    /**
     * Perform the authorization checks before executing the task operation.
     */
    protected abstract void authorise();

    /**
     * Perform the state checks before executing the task operation.
     */
    protected abstract void checkState();

    /**
     * Checks the post-conditions after executing the task operation.
     */
    protected abstract void checkPostConditions();

    /**
     * Creates the task event object corresponding the the command being executed.
     *
     * @return : The task event object.
     */
    protected EventDAO createTaskEvent() {
        event.setTask(task);
        //As the getClass method gives the run time class name we can directly set the operation type.
        event.setType(TaskEventType.valueOf(this.getClass().getSimpleName().toUpperCase()));
        event.setUser(operationInvoker.getName());
        event.setNewState(task.getStatus());
        return event;
    }

    /**
     * @return : Returns the empty event object.
     *         Note the calling child object should fill all the fields properly.
     */
    protected EventDAO getTaskEvent() {
        return event;
    }

    /**
     *
     */
    protected void processTaskEvent() {
        EventDAO eventDAO = createTaskEvent();
        getTask().persistEvent(eventDAO);
        TaskEventInfo taskEventInfo = CommonTaskUtil.populateTaskEventInfo(eventDAO, task);
        getEngine().getEventProcessor().processEvent(taskEventInfo);
    }

    private void validateForExcludedOwner(String callerId) {
        if(isExcludedOwner(callerId))
        {
            String errorMessage = "Access Denied. You are not authorized to access this task";
            log.error("Current user " + callerId + " is in EXCLUDED_OWNERS role");
            throw new HumanTaskIllegalAccessException(errorMessage);
        }
    }

    protected boolean isExcludedOwner(String callerId) {
        GenericHumanRoleDAO excludedOwnerRole = task.getGenericHumanRole(GenericHumanRoleDAO.GenericHumanRoleType.EXCLUDED_OWNERS);
        if (excludedOwnerRole != null) {
            List<OrganizationalEntityDAO> orgEntities = excludedOwnerRole.getOrgEntities();
            for (OrganizationalEntityDAO orgEntity : orgEntities) {
                if (orgEntity.getOrgEntityType() == OrganizationalEntityDAO.OrganizationalEntityType.USER &&
                        orgEntity.getName().equals(callerId)) {
                    return true;

                } else if (orgEntity.getOrgEntityType() == OrganizationalEntityDAO.OrganizationalEntityType.GROUP) {
                    List<String> roleNameListForUser = engine.getPeopleQueryEvaluator().getRoleNameListForUser(callerId);
                    for (String roleName : roleNameListForUser) {
                        String orgEntityName = orgEntity.getName();
                        if (roleName.equals(orgEntityName)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }
}
