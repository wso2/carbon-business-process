/**
 * Copyright (c) 2015 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.bpmn.rest.service.base;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.impl.TaskQueryProperty;
import org.activiti.engine.impl.persistence.entity.VariableInstanceEntity;
import org.activiti.engine.query.QueryProperty;
import org.activiti.engine.task.DelegationState;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bpmn.rest.common.RestResponseFactory;
import org.wso2.carbon.bpmn.rest.engine.variable.QueryVariable;
import org.wso2.carbon.bpmn.rest.engine.variable.RestVariable;
import org.wso2.carbon.bpmn.rest.internal.RestServiceContentHolder;
import org.wso2.carbon.bpmn.rest.model.common.DataResponse;
import org.wso2.carbon.bpmn.rest.model.runtime.TaskPaginateList;
import org.wso2.carbon.bpmn.rest.model.runtime.TaskQueryRequest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

//import org.apache.commons.io.IOUtils;
//import org.apache.cxf.jaxrs.ext.multipart.MultipartBody;
//import org.wso2.carbon.bpmn.rest.common.exception.BPMNOSGIServiceException;
//import org.wso2.carbon.bpmn.rest.common.utils.Utils;
//import org.wso2.carbon.bpmn.rest.model.runtime.AttachmentDataHolder;
//import javax.activation.DataHandler;
//import javax.servlet.http.HttpServletRequest;
//import java.io.*;

/**
 *
 */
public class BaseTaskService {

    protected static final List<String> ALL_PROPERTIES_LIST;
    protected static final Map<String, QueryProperty> PROPERTIES;
    protected static final String DEFAULT_ENCODING = "UTF-8";

    private static final Log log = LogFactory.getLog(BaseTaskService.class);

    static {
        HashMap<String, QueryProperty> propMap = new HashMap<>();
        propMap.put("id", TaskQueryProperty.TASK_ID);
        propMap.put("name", TaskQueryProperty.NAME);
        propMap.put("description", TaskQueryProperty.DESCRIPTION);
        propMap.put("dueDate", TaskQueryProperty.DUE_DATE);
        propMap.put("createTime", TaskQueryProperty.CREATE_TIME);
        propMap.put("priority", TaskQueryProperty.PRIORITY);
        propMap.put("executionId", TaskQueryProperty.EXECUTION_ID);
        propMap.put("processInstanceId", TaskQueryProperty.PROCESS_INSTANCE_ID);
        propMap.put("tenantId", TaskQueryProperty.TENANT_ID);
        PROPERTIES = Collections.unmodifiableMap(propMap);

    }

    static {
        List<String> properties = new ArrayList<>();
        properties.add("name");
        properties.add("nameLike");
        properties.add("description");
        properties.add("priority");
        properties.add("minimumPriority");
        properties.add("maximumPriority");
        properties.add("assignee");
        properties.add("assigneeLike");
        properties.add("owner");
        properties.add("ownerLike");
        properties.add("unassigned");
        properties.add("delegationState");
        properties.add("candidateUser");
        properties.add("candidateGroup");
        properties.add("candidateGroups");
        properties.add("involvedUser");
        properties.add("taskDefinitionKey");
        properties.add("taskDefinitionKeyLike");
        properties.add("processInstanceId");
        properties.add("processInstanceBusinessKey");
        properties.add("processInstanceBusinessKeyLike");
        properties.add("processDefinitionKey");
        properties.add("processDefinitionKeyLike");
        properties.add("processDefinitionName");
        properties.add("processDefinitionNameLike");
        properties.add("executionId");
        properties.add("createdOn");
        properties.add("createdBefore");
        properties.add("createdAfter");
        properties.add("dueOn");
        properties.add("dueBefore");
        properties.add("dueAfter");
        properties.add("withoutDueDate");
        properties.add("excludeSubTasks");
        properties.add("active");
        properties.add("includeTaskLocalVariables");
        properties.add("includeProcessVariables");
        properties.add("tenantId");
        properties.add("tenantIdLike");
        properties.add("withoutTenantId");
        properties.add("candidateOrAssigned");
        properties.add("sort");
        properties.add("start");
        properties.add("size");
        properties.add("order");
        ALL_PROPERTIES_LIST = Collections.unmodifiableList(properties);
    }

    protected DataResponse getTasksFromQueryRequest(TaskQueryRequest request,
                                                    Map<String, List<String>> queryParams,
                                                    Map<String, String> requestParams,
                                                    String baseName) {

        if (requestParams == null) {
            requestParams = new HashMap<>();

            for (String property : ALL_PROPERTIES_LIST) {
                String value = queryParams.get(property).get(0);

                if (value != null) {
                    requestParams.put(property, value);
                }
            }
        }

        TaskService taskService = RestServiceContentHolder.getInstance().getRestService().getTaskService();
        TaskQuery taskQuery = taskService.createTaskQuery();

        // Populate filter-parameters

        if (request.getName() != null) {
            taskQuery.taskName(request.getName());
        }
        if (request.getNameLike() != null) {
            taskQuery.taskNameLike(request.getNameLike());
        }
        if (request.getDescription() != null) {
            taskQuery.taskDescription(request.getDescription());
        }
        if (request.getDescriptionLike() != null) {
            taskQuery.taskDescriptionLike(request.getDescriptionLike());
        }
        if (request.getPriority() != null) {
            taskQuery.taskPriority(request.getPriority());
        }
        if (request.getMinimumPriority() != null) {
            taskQuery.taskMinPriority(request.getMinimumPriority());
        }
        if (request.getMaximumPriority() != null) {
            taskQuery.taskMaxPriority(request.getMaximumPriority());
        }
        if (request.getAssignee() != null) {
            taskQuery.taskAssignee(request.getAssignee());
        }
        if (request.getAssigneeLike() != null) {
            taskQuery.taskAssigneeLike(request.getAssigneeLike());
        }
        if (request.getOwner() != null) {
            taskQuery.taskOwner(request.getOwner());
        }
        if (request.getOwnerLike() != null) {
            taskQuery.taskOwnerLike(request.getOwnerLike());
        }
        if (request.getUnassigned() != null) {
            taskQuery.taskUnassigned();
        }
        if (request.getDelegationState() != null) {
            DelegationState state = getDelegationState(request.getDelegationState());
            if (state != null) {
                taskQuery.taskDelegationState(state);
            }
        }
        if (request.getCandidateUser() != null) {
            taskQuery.taskCandidateUser(request.getCandidateUser());
        }
        if (request.getInvolvedUser() != null) {
            taskQuery.taskInvolvedUser(request.getInvolvedUser());
        }
        if (request.getCandidateGroup() != null) {
            taskQuery.taskCandidateGroup(request.getCandidateGroup());
        }
        if (request.getCandidateGroupIn() != null) {
            taskQuery.taskCandidateGroupIn(request.getCandidateGroupIn());
        }
        if (request.getProcessInstanceId() != null) {
            taskQuery.processInstanceId(request.getProcessInstanceId());
        }
        if (request.getProcessInstanceBusinessKey() != null) {
            taskQuery.processInstanceBusinessKey(request.getProcessInstanceBusinessKey());
        }
        if (request.getExecutionId() != null) {
            taskQuery.executionId(request.getExecutionId());
        }
        if (request.getCreatedOn() != null) {
            taskQuery.taskCreatedOn(request.getCreatedOn());
        }
        if (request.getCreatedBefore() != null) {
            taskQuery.taskCreatedBefore(request.getCreatedBefore());
        }
        if (request.getCreatedAfter() != null) {
            taskQuery.taskCreatedAfter(request.getCreatedAfter());
        }
        if (request.getExcludeSubTasks() != null) {
            if (request.getExcludeSubTasks()) {
                taskQuery.excludeSubtasks();
            }
        }

        if (request.getTaskDefinitionKey() != null) {
            taskQuery.taskDefinitionKey(request.getTaskDefinitionKey());
        }

        if (request.getTaskDefinitionKeyLike() != null) {
            taskQuery.taskDefinitionKeyLike(request.getTaskDefinitionKeyLike());
        }
        if (request.getDueDate() != null) {
            taskQuery.taskDueDate(request.getDueDate());
        }
        if (request.getDueBefore() != null) {
            taskQuery.taskDueBefore(request.getDueBefore());
        }
        if (request.getDueAfter() != null) {
            taskQuery.taskDueAfter(request.getDueAfter());
        }
        if (request.getWithoutDueDate() != null && request.getWithoutDueDate()) {
            taskQuery.withoutTaskDueDate();
        }

        if (request.getActive() != null) {
            if (request.getActive().booleanValue()) {
                taskQuery.active();
            } else {
                taskQuery.suspended();
            }
        }

        if (request.getIncludeTaskLocalVariables() != null) {
            if (request.getIncludeTaskLocalVariables()) {
                taskQuery.includeTaskLocalVariables();
            }
        }
        if (request.getIncludeProcessVariables() != null) {
            if (request.getIncludeProcessVariables()) {
                taskQuery.includeProcessVariables();
            }
        }

        if (request.getProcessInstanceBusinessKeyLike() != null) {
            taskQuery.processInstanceBusinessKeyLike(request.getProcessInstanceBusinessKeyLike());
        }

        if (request.getProcessDefinitionKey() != null) {
            taskQuery.processDefinitionKey(request.getProcessDefinitionKey());
        }

        if (request.getProcessDefinitionKeyLike() != null) {
            taskQuery.processDefinitionKeyLike(request.getProcessDefinitionKeyLike());
        }

        if (request.getProcessDefinitionName() != null) {
            taskQuery.processDefinitionName(request.getProcessDefinitionName());
        }

        if (request.getProcessDefinitionNameLike() != null) {
            taskQuery.processDefinitionNameLike(request.getProcessDefinitionNameLike());
        }

        if (request.getTaskVariables() != null) {
            addTaskvariables(taskQuery, request.getTaskVariables());
        }

        if (request.getProcessInstanceVariables() != null) {
            addProcessvariables(taskQuery, request.getProcessInstanceVariables());
        }

        if (request.getTenantId() != null) {
            taskQuery.taskTenantId(request.getTenantId());
        }

        if (request.getTenantIdLike() != null) {
            taskQuery.taskTenantIdLike(request.getTenantIdLike());
        }

        if (Boolean.TRUE.equals(request.getWithoutTenantId())) {
            taskQuery.taskWithoutTenantId();
        }

        if (request.getCandidateOrAssigned() != null) {
            taskQuery.taskCandidateOrAssigned(request.getCandidateOrAssigned());
        }

        DataResponse dataResponse = new TaskPaginateList(new RestResponseFactory(), baseName)
                .paginateList(requestParams, request, taskQuery, "id", PROPERTIES);

        return dataResponse;
        //return Response.ok().entity(dataResponse).build();
    }

    protected void addTaskvariables(TaskQuery taskQuery, List<QueryVariable> variables) {

        RestResponseFactory restResponseFactory = new RestResponseFactory();

        for (QueryVariable variable : variables) {
            if (variable.getVariableOperation() == null) {
                throw new ActivitiIllegalArgumentException(
                        "Variable operation is missing for variable: " + variable.getName());
            }
            if (variable.getValue() == null) {
                throw new ActivitiIllegalArgumentException(
                        "Variable value is missing for variable: " + variable.getName());
            }

            boolean nameLess = variable.getName() == null;

            Object actualValue = restResponseFactory.getVariableValue(variable);

            // A value-only query is only possible using equals-operator
            if (nameLess &&
                    variable.getVariableOperation() != QueryVariable.QueryVariableOperation.EQUALS) {
                throw new ActivitiIllegalArgumentException(
                        "Value-only query (without a variable-name) is only supported when using " +
                                "'equals' operation.");
            }

            switch (variable.getVariableOperation()) {

                case EQUALS:
                    if (nameLess) {
                        taskQuery.taskVariableValueEquals(actualValue);
                    } else {
                        taskQuery.taskVariableValueEquals(variable.getName(), actualValue);
                    }
                    break;

                case EQUALS_IGNORE_CASE:
                    if (actualValue instanceof String) {
                        taskQuery.taskVariableValueEqualsIgnoreCase(variable.getName(),
                                (String) actualValue);
                    } else {
                        throw new ActivitiIllegalArgumentException(
                                "Only string variable values are supported when ignoring casing, " +
                                        "but was: " +
                                        actualValue.getClass().getName());
                    }
                    break;

                case NOT_EQUALS:
                    taskQuery.taskVariableValueNotEquals(variable.getName(), actualValue);
                    break;

                case NOT_EQUALS_IGNORE_CASE:
                    if (actualValue instanceof String) {
                        taskQuery.taskVariableValueNotEqualsIgnoreCase(variable.getName(),
                                (String) actualValue);
                    } else {
                        throw new ActivitiIllegalArgumentException(
                                "Only string variable values are supported when ignoring casing," +
                                        " but was: " +
                                        actualValue.getClass().getName());
                    }
                    break;

                case GREATER_THAN:
                    taskQuery.taskVariableValueGreaterThan(variable.getName(), actualValue);
                    break;

                case GREATER_THAN_OR_EQUALS:
                    taskQuery.taskVariableValueGreaterThanOrEqual(variable.getName(), actualValue);
                    break;

                case LESS_THAN:
                    taskQuery.taskVariableValueLessThan(variable.getName(), actualValue);
                    break;

                case LESS_THAN_OR_EQUALS:
                    taskQuery.taskVariableValueLessThanOrEqual(variable.getName(), actualValue);
                    break;

                case LIKE:
                    if (actualValue instanceof String) {
                        taskQuery.taskVariableValueLike(variable.getName(), (String) actualValue);
                    } else {
                        throw new ActivitiIllegalArgumentException(
                                "Only string variable values are supported using like, but was: " +
                                        actualValue.getClass().getName());
                    }
                    break;
                default:
                    throw new ActivitiIllegalArgumentException(
                            "Unsupported variable query operation: " +
                                    variable.getVariableOperation());
            }
        }
    }

    protected void addProcessvariables(TaskQuery taskQuery, List<QueryVariable> variables) {

        RestResponseFactory restResponseFactory = new RestResponseFactory();
        for (QueryVariable variable : variables) {
            if (variable.getVariableOperation() == null) {
                throw new ActivitiIllegalArgumentException(
                        "Variable operation is missing for variable: " + variable.getName());
            }
            if (variable.getValue() == null) {
                throw new ActivitiIllegalArgumentException(
                        "Variable value is missing for variable: " + variable.getName());
            }

            boolean nameLess = variable.getName() == null;

            Object actualValue = restResponseFactory.getVariableValue(variable);

            // A value-only query is only possible using equals-operator
            if (nameLess &&
                    variable.getVariableOperation() != QueryVariable.QueryVariableOperation.EQUALS) {
                throw new ActivitiIllegalArgumentException(
                        "Value-only query (without a variable-name) is only supported when using" +
                                " 'equals' operation.");
            }

            switch (variable.getVariableOperation()) {

                case EQUALS:
                    if (nameLess) {
                        taskQuery.processVariableValueEquals(actualValue);
                    } else {
                        taskQuery.processVariableValueEquals(variable.getName(), actualValue);
                    }
                    break;

                case EQUALS_IGNORE_CASE:
                    if (actualValue instanceof String) {
                        taskQuery.processVariableValueEqualsIgnoreCase(variable.getName(),
                                (String) actualValue);
                    } else {
                        throw new ActivitiIllegalArgumentException(
                                "Only string variable values are supported when ignoring casing," +
                                        " but was: " + actualValue.getClass().getName());
                    }
                    break;

                case NOT_EQUALS:
                    taskQuery.processVariableValueNotEquals(variable.getName(), actualValue);
                    break;

                case NOT_EQUALS_IGNORE_CASE:
                    if (actualValue instanceof String) {
                        taskQuery.processVariableValueNotEqualsIgnoreCase(variable.getName(),
                                (String) actualValue);
                    } else {
                        throw new ActivitiIllegalArgumentException(
                                "Only string variable values are supported when ignoring casing," +
                                        " but was: " + actualValue.getClass().getName());
                    }
                    break;

                case GREATER_THAN:
                    taskQuery.processVariableValueGreaterThan(variable.getName(), actualValue);
                    break;

                case GREATER_THAN_OR_EQUALS:
                    taskQuery.processVariableValueGreaterThanOrEqual(variable.getName(),
                            actualValue);
                    break;

                case LESS_THAN:
                    taskQuery.processVariableValueLessThan(variable.getName(), actualValue);
                    break;

                case LESS_THAN_OR_EQUALS:
                    taskQuery.processVariableValueLessThanOrEqual(variable.getName(), actualValue);
                    break;

                case LIKE:
                    if (actualValue instanceof String) {
                        taskQuery
                                .processVariableValueLike(variable.getName(), (String) actualValue);
                    } else {
                        throw new ActivitiIllegalArgumentException(
                                "Only string variable values are supported using like, but was: " +
                                        actualValue.getClass().getName());
                    }
                    break;

                default:
                    throw new ActivitiIllegalArgumentException(
                            "Unsupported variable query operation: " +
                                    variable.getVariableOperation());
            }
        }
    }

    protected Task getTaskFromRequest(String taskId) {
        TaskService taskService = RestServiceContentHolder.getInstance().getRestService().getTaskService();
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null) {
            throw new ActivitiObjectNotFoundException(
                    "Could not find a task with id '" + taskId + "'.", Task.class);
        }
        return task;
    }

    protected DelegationState getDelegationState(String delegationState) {
        if (delegationState != null) {
            if (DelegationState.RESOLVED.name().toLowerCase(Locale.getDefault())
                    .equals(delegationState)) {
                return DelegationState.RESOLVED;
            } else if (DelegationState.PENDING.name().toLowerCase(Locale.getDefault())
                    .equals(delegationState)) {
                return DelegationState.PENDING;
            } else {
                throw new ActivitiIllegalArgumentException(
                        "Illegal value for delegationState: " + delegationState);
            }
        }
        return null;
    }

    protected void addLocalVariables(Task task, Map<String, RestVariable> variableMap,
                                     String baseName) {
        TaskService taskService = RestServiceContentHolder.getInstance().getRestService().getTaskService();

        Map<String, Object> rawVariables = taskService.getVariablesLocal(task.getId());
        List<RestVariable> localVariables = new RestResponseFactory()
                .createRestVariables(rawVariables, task.getId(), RestResponseFactory.VARIABLE_TASK,
                        RestVariable.RestVariableScope.LOCAL, baseName);

        for (RestVariable var : localVariables) {
            variableMap.put(var.getName(), var);
        }
    }

    protected void addGlobalVariables(Task task, Map<String, RestVariable> variableMap,
                                      String baseName) {
        if (task.getExecutionId() != null) {
            RuntimeService runtimeService = RestServiceContentHolder.getInstance().getRestService().getRumtimeService();

            Map<String, Object> rawVariables = runtimeService.getVariables(task.getExecutionId());
            List<RestVariable> globalVariables = new RestResponseFactory()
                    .createRestVariables(rawVariables, task.getId(),
                            RestResponseFactory.VARIABLE_TASK,
                            RestVariable.RestVariableScope.GLOBAL, baseName);

            // Overlay global variables over local ones. In case they are present the
            // values are not overridden, since local variables get precedence over
            // global ones at all times.
            for (RestVariable var : globalVariables) {
                if (!variableMap.containsKey(var.getName())) {
                    variableMap.put(var.getName(), var);
                }
            }
        }
    }

    public RestVariable getVariableFromRequest(String taskId, String variableName, String scope,
                                               boolean includeBinary, String baseName) {

        boolean variableFound = false;
        Object value = null;
        RestVariable.RestVariableScope variableScope = RestVariable.getScopeFromString(scope);
        TaskService taskService = RestServiceContentHolder.getInstance().getRestService().getTaskService();
        RuntimeService runtimeService = RestServiceContentHolder.getInstance().getRestService().getRumtimeService();

        if (variableScope == null) {
            // First, check local variables (which have precedence when no scope is supplied)
            if (taskService.hasVariableLocal(taskId, variableName)) {
                value = taskService.getVariableLocal(taskId, variableName);
                variableScope = RestVariable.RestVariableScope.LOCAL;
                variableFound = true;
            } else {
                // Revert to execution-variable when not present local on the task
                Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
                if (task.getExecutionId() != null &&
                        runtimeService.hasVariable(task.getExecutionId(), variableName)) {
                    value = runtimeService.getVariable(task.getExecutionId(), variableName);
                    variableScope = RestVariable.RestVariableScope.GLOBAL;
                    variableFound = true;
                }
            }

        } else if (variableScope == RestVariable.RestVariableScope.GLOBAL) {
            Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
            if (task.getExecutionId() != null &&
                    runtimeService.hasVariable(task.getExecutionId(), variableName)) {
                value = runtimeService.getVariable(task.getExecutionId(), variableName);
                variableFound = true;
            }

        } else if (variableScope == RestVariable.RestVariableScope.LOCAL) {
            if (taskService.hasVariableLocal(taskId, variableName)) {
                value = taskService.getVariableLocal(taskId, variableName);
                variableFound = true;
            }
        }

        if (!variableFound) {
            throw new ActivitiObjectNotFoundException(
                    "Task '" + taskId + "' doesn't have a variable with name: '" + variableName +
                            "'.", VariableInstanceEntity.class);
        } else {
            return new RestResponseFactory()
                    .createRestVariable(variableName, value, variableScope, taskId,
                            RestResponseFactory.VARIABLE_TASK, includeBinary, baseName);
        }
    }
    //todo
/*
    protected RestVariable setBinaryVariable(MultipartBody multipartBody, Task
            task, boolean isNew, UriInfo uriInfo) throws IOException {

        boolean debugEnabled = log.isDebugEnabled();

        if(debugEnabled) {
            log.debug("Processing Binary variables");
        }

        Object result = null;

        List<org.apache.cxf.jaxrs.ext.multipart.Attachment> attachments = multipartBody
        .getAllAttachments();

        int attachmentSize = attachments.size();

        if (attachmentSize <= 0) {
            throw new ActivitiIllegalArgumentException("No Attachments found with the request
            body");
        }
        AttachmentDataHolder attachmentDataHolder = new AttachmentDataHolder();

        for (int i = 0; i < attachmentSize; i++) {
            org.apache.cxf.jaxrs.ext.multipart.Attachment attachment = attachments.get(i);

            String contentDispositionHeaderValue = attachment.getHeader("Content-Disposition");
            String contentType = attachment.getHeader("Content-Type");

            if (debugEnabled) {
                log.debug("Going to iterate:" + i);
                log.debug("contentDisposition:" + contentDispositionHeaderValue);
            }

            if (contentDispositionHeaderValue != null) {
                contentDispositionHeaderValue = contentDispositionHeaderValue.trim();

                Map<String, String> contentDispositionHeaderValueMap = Utils
                .processContentDispositionHeader
                        (contentDispositionHeaderValue);
                String dispositionName = contentDispositionHeaderValueMap.get("name");
                DataHandler dataHandler = attachment.getDataHandler();

                OutputStream outputStream = null;

                if ("name".equals(dispositionName)) {
                    try {
                        outputStream = Utils.getAttachmentStream(dataHandler.getInputStream());
                    } catch (IOException e) {
                        throw new ActivitiIllegalArgumentException("Binary Variable Name
                         Reading error occured", e);
                    }

                    if (outputStream != null) {
                        String fileName = outputStream.toString();
                        attachmentDataHolder.setName(fileName);
                    }


                } else if ("type".equals(dispositionName)) {
                    try {
                        outputStream = Utils.getAttachmentStream(dataHandler.getInputStream());
                    } catch (IOException e) {
                        throw new ActivitiIllegalArgumentException("\"Binary Variable Type
                         Reading error occured", e);
                    }

                    if (outputStream != null) {
                        String typeName = outputStream.toString();
                        attachmentDataHolder.setType(typeName);
                    }


                } else if ("scope".equals(dispositionName)) {
                    try {
                        outputStream = Utils.getAttachmentStream(dataHandler.getInputStream());
                    } catch (IOException e) {
                        throw new ActivitiIllegalArgumentException("Binary Variable
                         scopeDescription Reading error " +
                                "occured", e);
                    }

                    if (outputStream != null) {
                        String scope = outputStream.toString();
                        attachmentDataHolder.setScope(scope);
                    }
                }

                if (contentType != null) {
                    if ("file".equals(dispositionName)) {

                        InputStream inputStream = null;
                        try {
                            inputStream = dataHandler.getInputStream();
                        } catch (IOException e) {
                            throw new ActivitiIllegalArgumentException("Error Occured
                            During processing empty body.",
                                    e);
                        }

                        if (inputStream != null) {
                            attachmentDataHolder.setContentType(contentType);
                            byte[] attachmentArray = new byte[0];
                            try {
                                attachmentArray = IOUtils.toByteArray(inputStream);
                            } catch (IOException e) {
                                throw new ActivitiIllegalArgumentException("Processing
                                BinaryV variable Body Failed.",
                                        e);
                            }
                            attachmentDataHolder.setAttachmentArray(attachmentArray);
                        }
                    }
                }
            }
        }


        attachmentDataHolder.printDebug();

        String variableScope =attachmentDataHolder.getScope();
        String variableName = attachmentDataHolder.getName();
        String variableType =  attachmentDataHolder.getType();
        byte[] attachmentArray = attachmentDataHolder.getAttachmentArray();

        try {
            if (variableName == null) {
                throw new ActivitiIllegalArgumentException("No variable name was found
                in request body.");
            }

            if (attachmentArray == null) {
                throw new ActivitiIllegalArgumentException("Empty attachment body was found
                 in request body after " +
                        "decoding the request" +
                        ".");
            }

            if (variableType != null) {
                if (!RestResponseFactory.BYTE_ARRAY_VARIABLE_TYPE.equals(variableType) &&
                 !RestResponseFactory.SERIALIZABLE_VARIABLE_TYPE.equals(variableType)) {
                    throw new ActivitiIllegalArgumentException("Only 'binary' and
                    'serializable' are supported as variable type.");
                }
            } else {
                attachmentDataHolder.setType(RestResponseFactory.BYTE_ARRAY_VARIABLE_TYPE);
            }

            RestVariable.RestVariableScope scope = RestVariable.RestVariableScope.LOCAL;
            if (variableScope != null) {
                scope = RestVariable.getScopeFromString(variableScope);
            }

            if (variableScope != null) {
                scope = RestVariable.getScopeFromString(variableScope);
            }

            if (variableType.equals(RestResponseFactory.BYTE_ARRAY_VARIABLE_TYPE)) {
                // Use raw bytes as variable value
                setVariable(task, variableName, attachmentArray, scope, isNew);

            } else {
                // Try deserializing the object
                InputStream inputStream = new ByteArrayInputStream(attachmentArray);
                ObjectInputStream stream = new ObjectInputStream(inputStream);
                Object value = stream.readObject();
                setVariable(task, variableName, value, scope, isNew);
                stream.close();
            }

            return new RestResponseFactory().createBinaryRestVariable(variableName,
            scope, variableType, task.getId(),
                    null, null, uriInfo.getBaseUri().toString());

        } catch (IOException ioe) {
            throw new ActivitiIllegalArgumentException("Error getting binary variable", ioe);
        } catch (ClassNotFoundException ioe) {
            throw new BPMNContentNotSupportedException("The provided body contains a
            serialized object for which the class is nog found: " + ioe
                    .getMessage());
        }
    }

*/
    /* TODO
 protected RestVariable setBinaryVariable(HttpServletRequest httpServletRequest, Task task,
                                             boolean isNew, UriInfo uriInfo) throws IOException {


        boolean debugEnabled = log.isDebugEnabled();

        if(debugEnabled) {
            log.debug("Processing Binary variables");
        }

        byte[] byteArray = Utils.processMultiPartFile(httpServletRequest, "file content");
        if(byteArray == null){
            throw new ActivitiIllegalArgumentException("Empty file body was found in
            request body after " +
                    "decoding the request" +
                    ".");
        }
        String variableScope = uriInfo.getQueryParameters().getFirst("scope");
        String variableName = uriInfo.getQueryParameters().getFirst("name");
        String variableType =  uriInfo.getQueryParameters().getFirst("type");

        if(debugEnabled) {
            log.debug("variableScope:" + variableScope + " variableName:" + variableName +
             " variableType:" +
                    variableType);
        }

        try {

            if (variableName == null) {
                throw new ActivitiIllegalArgumentException("No variable name was found
                in request body.");
            }

            if (variableType != null) {
                if (!RestResponseFactory.BYTE_ARRAY_VARIABLE_TYPE.equals(variableType) &&
                 !RestResponseFactory.SERIALIZABLE_VARIABLE_TYPE.equals(variableType)) {
                    throw new ActivitiIllegalArgumentException("Only 'binary' and
                    'serializable' are supported as variable type.");
                }
            } else {
                variableType = RestResponseFactory.BYTE_ARRAY_VARIABLE_TYPE;
            }

            RestVariable.RestVariableScope scope = RestVariable.RestVariableScope.LOCAL;
            if (variableScope != null) {
                scope = RestVariable.getScopeFromString(variableScope);
            }

            if (variableType.equals(RestResponseFactory.BYTE_ARRAY_VARIABLE_TYPE)) {
                // Use raw bytes as variable value
                setVariable(task, variableName, byteArray, scope, isNew);

            } else {
                // Try deserializing the object
                InputStream inputStream = new ByteArrayInputStream(byteArray);
                ObjectInputStream stream = new ObjectInputStream(inputStream);
                Object value = stream.readObject();
                setVariable(task, variableName, value, scope, isNew);
                stream.close();
            }

            return new RestResponseFactory().createBinaryRestVariable(variableName,
             scope, variableType, task.getId(),
                    null, null, uriInfo.getBaseUri().toString());

        } catch (IOException ioe) {
            throw new ActivitiIllegalArgumentException("Error getting binary variable", ioe);
        } catch (ClassNotFoundException ioe) {
            throw new BPMNContentNotSupportedException("The provided body contains
             a serialized object for which the class is nog found: " + ioe
                    .getMessage());
        }

    }

*/

    protected void setVariable(Task task, String name, Object value,
                               RestVariable.RestVariableScope scope, boolean isNew) {
        // Create can only be done on new variables. Existing variables should be updated using PUT
        boolean hasVariable = hasVariableOnScope(task, name, scope);
        if (isNew && hasVariable) {
            throw new ActivitiException(
                    "Variable '" + name + "' is already present on task '" + task.getId() + "'.");
        }

        if (!isNew && !hasVariable) {
            throw new ActivitiObjectNotFoundException(
                    "Task '" + task.getId() + "' doesn't have a variable with name: '" + name +
                            "'.", null);
        }

        TaskService taskService = RestServiceContentHolder.getInstance().getRestService().getTaskService();
        RuntimeService runtimeService = RestServiceContentHolder.getInstance().getRestService().getRumtimeService();

        if (scope == RestVariable.RestVariableScope.LOCAL) {
            taskService.setVariableLocal(task.getId(), name, value);
        } else {
            if (task.getExecutionId() != null) {
                // Explicitly set on execution, setting non-local variable on task
                // will override local-variable if exists
                runtimeService.setVariable(task.getExecutionId(), name, value);
            } else {
                // Standalone task, no global variables possible
                throw new ActivitiIllegalArgumentException(
                        "Cannot set global variable '" + name + "' on task '" +
                                task.getId() + "', task is not part of process.");
            }
        }
    }

    protected boolean hasVariableOnScope(Task task, String variableName,
                                         RestVariable.RestVariableScope scope) {
        boolean variableFound = false;

        TaskService taskService = RestServiceContentHolder.getInstance().getRestService().getTaskService();
        RuntimeService runtimeService = RestServiceContentHolder.getInstance().getRestService().getRumtimeService();
        if (scope == RestVariable.RestVariableScope.GLOBAL) {
            if (task.getExecutionId() != null &&
                    runtimeService.hasVariable(task.getExecutionId(), variableName)) {
                variableFound = true;
            }

        } else if (scope == RestVariable.RestVariableScope.LOCAL) {
            if (taskService.hasVariableLocal(task.getId(), variableName)) {
                variableFound = true;
            }
        }
        return variableFound;
    }
}

