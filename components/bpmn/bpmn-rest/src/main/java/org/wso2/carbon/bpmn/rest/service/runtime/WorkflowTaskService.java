/**
 *  Copyright (c) 2015 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.bpmn.rest.service.runtime;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.activiti.engine.*;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.impl.persistence.entity.VariableInstanceEntity;
import org.activiti.engine.task.*;
import org.activiti.engine.task.Attachment;
import org.apache.commons.io.IOUtils;
import org.apache.cxf.io.CachedOutputStream;
import org.apache.cxf.jaxrs.ext.multipart.*;
import org.wso2.carbon.bpmn.rest.common.RequestUtil;
import org.wso2.carbon.bpmn.rest.common.RestResponseFactory;
import org.wso2.carbon.bpmn.rest.common.RestUrls;
import org.wso2.carbon.bpmn.rest.common.exception.BPMNConflictException;
import org.wso2.carbon.bpmn.rest.common.exception.BPMNForbiddenException;
import org.wso2.carbon.bpmn.rest.common.exception.BPMNOSGIServiceException;
import org.wso2.carbon.bpmn.rest.common.utils.BPMNOSGIService;
import org.wso2.carbon.bpmn.rest.common.utils.Utils;
import org.wso2.carbon.bpmn.rest.engine.variable.RestVariable;
import org.wso2.carbon.bpmn.rest.model.common.DataResponse;
import org.wso2.carbon.bpmn.rest.model.common.RestIdentityLink;
import org.wso2.carbon.bpmn.rest.model.runtime.*;
import org.wso2.carbon.bpmn.rest.service.base.BaseTaskService;

import javax.activation.DataHandler;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.*;
import java.util.*;

@Path("/tasks")
public class WorkflowTaskService extends BaseTaskService {
    @Context
    UriInfo uriInfo;


    @GET
    @Path("/")
    @Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
    public Response getTasks() {

        // Create a Task query request
        TaskQueryRequest request = new TaskQueryRequest();

        Map<String, String> requestParams = new HashMap<>();

        for (String property:allPropertiesList){
            String value= uriInfo.getQueryParameters().getFirst(property);

            if(value != null){
                requestParams.put(property, value);
            }
        }

        // Populate filter-parameters
        if (requestParams.containsKey("name")) {
            request.setName(requestParams.get("name"));
        }

        if (requestParams.containsKey("nameLike")) {
            request.setNameLike(requestParams.get("nameLike"));
        }

        if (requestParams.containsKey("description")) {
            request.setDescription(requestParams.get("description"));
        }

        if (requestParams.containsKey("descriptionLike")) {
            request.setDescriptionLike(requestParams.get("descriptionLike"));
        }

        if (requestParams.containsKey("priority")) {
            request.setPriority(Integer.valueOf(requestParams.get("priority")));
        }

        if (requestParams.containsKey("minimumPriority")) {
            request.setMinimumPriority(Integer.valueOf(requestParams.get("minimumPriority")));
        }

        if (requestParams.containsKey("maximumPriority")) {
            request.setMaximumPriority(Integer.valueOf(requestParams.get("maximumPriority")));
        }

        if (requestParams.containsKey("assignee")) {
            request.setAssignee(requestParams.get("assignee"));
        }

        if (requestParams.containsKey("owner")) {
            request.setOwner(requestParams.get("owner"));
        }

        if (requestParams.containsKey("unassigned")) {
            request.setUnassigned(Boolean.valueOf(requestParams.get("unassigned")));
        }

        if (requestParams.containsKey("delegationState")) {
            request.setDelegationState(requestParams.get("delegationState"));
        }

        if (requestParams.containsKey("candidateUser")) {
            request.setCandidateUser(requestParams.get("candidateUser"));
        }

        if (requestParams.containsKey("involvedUser")) {
            request.setInvolvedUser(requestParams.get("involvedUser"));
        }

        if (requestParams.containsKey("candidateGroup")) {
            request.setCandidateGroup(requestParams.get("candidateGroup"));
        }

        if (requestParams.containsKey("candidateGroups")) {
            String[] candidateGroups = requestParams.get("candidateGroups").split(",");
            List<String> groups = new ArrayList<String>(candidateGroups.length);
            for (String candidateGroup : candidateGroups) {
                groups.add(candidateGroup);
            }
            request.setCandidateGroupIn(groups);
        }

        if (requestParams.containsKey("processDefinitionKey")) {
            request.setProcessDefinitionKey(requestParams.get("processDefinitionKey"));
        }

        if (requestParams.containsKey("processDefinitionKeyLike")) {
            request.setProcessDefinitionKeyLike(requestParams.get("processDefinitionKeyLike"));
        }

        if (requestParams.containsKey("processDefinitionName")) {
            request.setProcessDefinitionName(requestParams.get("processDefinitionName"));
        }

        if (requestParams.containsKey("processDefinitionNameLike")) {
            request.setProcessDefinitionNameLike(requestParams.get("processDefinitionNameLike"));
        }

        if (requestParams.containsKey("processInstanceId")) {
            request.setProcessInstanceId(requestParams.get("processInstanceId"));
        }

        if (requestParams.containsKey("processInstanceBusinessKey")) {
            request.setProcessInstanceBusinessKey(requestParams.get("processInstanceBusinessKey"));
        }

        if (requestParams.containsKey("executionId")) {
            request.setExecutionId(requestParams.get("executionId"));
        }

        if (requestParams.containsKey("createdOn")) {
            request.setCreatedOn(RequestUtil.getDate(requestParams, "createdOn"));
        }

        if (requestParams.containsKey("createdBefore")) {
            request.setCreatedBefore(RequestUtil.getDate(requestParams, "createdBefore"));
        }

        if (requestParams.containsKey("createdAfter")) {
            request.setCreatedAfter(RequestUtil.getDate(requestParams, "createdAfter"));
        }

        if (requestParams.containsKey("excludeSubTasks")) {
            request.setExcludeSubTasks(Boolean.valueOf(requestParams.get("excludeSubTasks")));
        }

        if (requestParams.containsKey("taskDefinitionKey")) {
            request.setTaskDefinitionKey(requestParams.get("taskDefinitionKey"));
        }

        if (requestParams.containsKey("taskDefinitionKeyLike")) {
            request.setTaskDefinitionKeyLike(requestParams.get("taskDefinitionKeyLike"));
        }

        if (requestParams.containsKey("dueDate")) {
            request.setDueDate(RequestUtil.getDate(requestParams, "dueDate"));
        }

        if (requestParams.containsKey("dueBefore")) {
            request.setDueBefore(RequestUtil.getDate(requestParams, "dueBefore"));
        }

        if (requestParams.containsKey("dueAfter")) {
            request.setDueAfter(RequestUtil.getDate(requestParams, "dueAfter"));
        }

        if (requestParams.containsKey("active")) {
            request.setActive(Boolean.valueOf(requestParams.get("active")));
        }

        if (requestParams.containsKey("includeTaskLocalVariables")) {
            request.setIncludeTaskLocalVariables(Boolean.valueOf(requestParams.get("includeTaskLocalVariables")));
        }

        if (requestParams.containsKey("includeProcessVariables")) {
            request.setIncludeProcessVariables(Boolean.valueOf(requestParams.get("includeProcessVariables")));
        }

        if (requestParams.containsKey("tenantId")) {
            request.setTenantId(requestParams.get("tenantId"));
        }

        if (requestParams.containsKey("tenantIdLike")) {
            request.setTenantIdLike(requestParams.get("tenantIdLike"));
        }

        if (requestParams.containsKey("withoutTenantId") && Boolean.valueOf(requestParams.get("withoutTenantId"))) {
            request.setWithoutTenantId(Boolean.TRUE);
        }

        if (requestParams.containsKey("candidateOrAssigned")) {
            request.setCandidateOrAssigned(requestParams.get("candidateOrAssigned"));
        }

        DataResponse dataResponse = getTasksFromQueryRequest(request, uriInfo, requestParams);
        return Response.ok().entity(dataResponse).build();
        //return getTasksFromQueryRequest(request, requestParams);
    }

    @GET
    @Path("/{taskId}")
    @Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
    public Response getTask(@PathParam("taskId") String taskId) {
        TaskResponse taskResponse = new RestResponseFactory().createTaskResponse(getTaskFromRequest(taskId), uriInfo
                .getBaseUri()
                .toString());

        return Response.ok().entity(taskResponse).build();
    }

    @PUT
    @Path("/{taskId}")
    @Consumes({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
    public Response updateTask(@PathParam("taskId") String taskId,
                                   TaskRequest taskRequest) {

        if (taskRequest == null) {
            throw new ActivitiException("A request body was expected when updating the task.");
        }

        Task task = getTaskFromRequest(taskId);

        // Populate the task properties based on the request
        populateTaskFromRequest(task, taskRequest);

        TaskService taskService = BPMNOSGIService.getTaskService();
        if(taskService == null){
            throw new BPMNOSGIServiceException("Taskservice couldn't be identified");
        }

        // Save the task and fetch agian, it's possible that an assignment-listener has updated
        // fields after it was saved so we can't use the in-memory task
        taskService.saveTask(task);
        task = taskService.createTaskQuery().taskId(task.getId()).singleResult();

        return Response.ok().entity(new RestResponseFactory().createTaskResponse(task, uriInfo.getBaseUri().toString())).build();
    }

    @POST
    @Path("/{taskId}")
    @Consumes({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
    public Response executeTaskAction(@PathParam("taskId") String taskId, TaskActionRequest actionRequest) {
        if (actionRequest == null) {
            throw new ActivitiException("A request body was expected when executing a task action.");
        }

        Task task = getTaskFromRequest(taskId);

        TaskService taskService = BPMNOSGIService.getTaskService();
        if(taskService == null){
            throw new BPMNOSGIServiceException("Taskservice couldn't be identified");
        }

        if (TaskActionRequest.ACTION_COMPLETE.equals(actionRequest.getAction())) {
            completeTask(task, actionRequest, taskService);

        } else if (TaskActionRequest.ACTION_CLAIM.equals(actionRequest.getAction())) {
            claimTask(task, actionRequest, taskService);

        } else if (TaskActionRequest.ACTION_DELEGATE.equals(actionRequest.getAction())) {
            delegateTask(task, actionRequest, taskService);

        } else if (TaskActionRequest.ACTION_RESOLVE.equals(actionRequest.getAction())) {
            resolveTask(task, taskService);

        } else {
            throw new ActivitiIllegalArgumentException("Invalid action: '" + actionRequest.getAction() + "'.");
        }

        return Response.ok().status(Response.Status.OK).build();
    }

    @DELETE
    @Path("/{taskId}")
    public Response deleteTask(@PathParam("taskId") String taskId) {

        Boolean cascadeHistory = false;

        if(uriInfo.getQueryParameters().getFirst("cascadeHistory") != null){
            cascadeHistory = Boolean.valueOf(uriInfo.getQueryParameters().getFirst("cascadeHistory"));
        }
        String deleteReason = uriInfo.getQueryParameters().getFirst("deleteReason");


        Task taskToDelete = getTaskFromRequest(taskId);
        if (taskToDelete.getExecutionId() != null) {
            // Can't delete a task that is part of a process instance
            throw new BPMNForbiddenException("Cannot delete a task that is part of a process-instance.");
        }

        TaskService taskService = BPMNOSGIService.getTaskService();
        if(taskService == null){
            throw new BPMNOSGIServiceException("Taskservice couldn't be identified");
        }

        if (cascadeHistory != null) {
            // Ignore delete-reason since the task-history (where the reason is recorded) will be deleted anyway
            taskService.deleteTask(taskToDelete.getId(), cascadeHistory);
        } else {
            // Delete with delete-reason
            taskService.deleteTask(taskToDelete.getId(), deleteReason);
        }
        return Response.ok().status(Response.Status.NO_CONTENT).build();
    }

    @GET
    @Path("/{taskId}/variables")
    @Consumes({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
    public Response getVariables(@PathParam("taskId")  String taskId) {

        String scope = uriInfo.getQueryParameters().getFirst("scope");

        List<RestVariable> result = new ArrayList<>();
        Map<String, RestVariable> variableMap = new HashMap<String, RestVariable>();

        // Check if it's a valid task to get the restVariables for
        Task task = getTaskFromRequest(taskId);

        RestVariable.RestVariableScope variableScope = RestVariable.getScopeFromString(scope);
        if (variableScope == null) {
            // Use both local and global restVariables
            addLocalVariables(task, variableMap, uriInfo.getBaseUri().toString());
            addGlobalVariables(task, variableMap, uriInfo.getBaseUri().toString());

        } else if(variableScope == RestVariable.RestVariableScope.GLOBAL) {
            addGlobalVariables(task, variableMap, uriInfo.getBaseUri().toString());

        } else if(variableScope == RestVariable.RestVariableScope.LOCAL) {
            addLocalVariables(task, variableMap, uriInfo.getBaseUri().toString());
        }

        // Get unique restVariables from map
        result.addAll(variableMap.values());

        RestVariableCollection restVariableCollection = new RestVariableCollection();
        restVariableCollection.setRestVariables(result);
        return Response.ok().entity(restVariableCollection).build();
    }

    @GET
    @Path("/{taskId}/variables/{variableName}")
    @Consumes({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
    public RestVariable getVariable(@PathParam("taskId") String taskId,
                                    @PathParam("variableName") String variableName) {

        String scope = uriInfo.getQueryParameters().getFirst("scope");
        return getVariableFromRequest(taskId, variableName, scope, false, uriInfo.getBaseUri().toString());
    }

    @GET
    @Path("/{taskId}/variables/{variableName}/data")
    @Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
    public Response getVariableData(@PathParam("taskId") String taskId, @PathParam("variableName") String
            variableName) {

        String scope = uriInfo.getQueryParameters().getFirst("scope");
        Response.ResponseBuilder responseBuilder = Response.ok();
        try {
            byte[] result = null;

            RestVariable variable = getVariableFromRequest(taskId, variableName, scope, true, uriInfo.getBaseUri().toString());
            if (RestResponseFactory.BYTE_ARRAY_VARIABLE_TYPE.equals(variable.getType())) {
                result = (byte[]) variable.getValue();
                responseBuilder.type(MediaType.APPLICATION_OCTET_STREAM);

            } else if(RestResponseFactory.SERIALIZABLE_VARIABLE_TYPE.equals(variable.getType())) {
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                ObjectOutputStream outputStream = new ObjectOutputStream(buffer);
                outputStream.writeObject(variable.getValue());
                outputStream.close();
                result = buffer.toByteArray();
                responseBuilder.type("application/x-java-serialized-object");

            } else {
                throw new ActivitiObjectNotFoundException("The variable does not have a binary data stream.", null);
            }
            return responseBuilder.entity(result).build();
        } catch(IOException ioe) {
            // Re-throw IOException
            throw new ActivitiException("Unexpected error getting variable data", ioe);
        }
    }

    @POST
    @Path("/{taskId}/variables")
    @Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
    public Response createTaskVariable(@PathParam("taskId") String taskId,  @Context HttpServletRequest
            httpServletRequest) throws IOException {

        Task task = getTaskFromRequest(taskId);

        Object result = null;

        if (httpServletRequest.getContentType().startsWith(MediaType.MULTIPART_FORM_DATA)) {
            result = setBinaryVariable(httpServletRequest, task, true, uriInfo);
        } else {

            List<RestVariable> inputVariables = new ArrayList<RestVariable>();
            List<RestVariable> resultVariables = new ArrayList<RestVariable>();
            result = resultVariables;

            try {
                String contentType = httpServletRequest.getContentType();
                if (contentType.equals(MediaType.APPLICATION_JSON)) {
                    try {
                        @SuppressWarnings("unchecked")
                        List<Object> variableObjects = (List<Object>) new ObjectMapper().readValue(httpServletRequest.getInputStream(),
                                List.class);
                        for (Object restObject : variableObjects) {
                            RestVariable restVariable = new ObjectMapper().convertValue(restObject, RestVariable.class);
                            inputVariables.add(restVariable);
                        }
                    } catch (IOException e) {
                        throw new ActivitiIllegalArgumentException("request body could not be transformed to a RestVariable " +
                                "instance.", e);
                    }

                } else if(contentType.equals(MediaType.APPLICATION_XML)){

                    JAXBContext jaxbContext = null;
                    try {
                        jaxbContext = JAXBContext.newInstance(RestVariableCollection.class);
                        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
                        RestVariableCollection restVariableCollection = (RestVariableCollection) jaxbUnmarshaller.
                                unmarshal(httpServletRequest.getInputStream());
                        if(restVariableCollection == null){
                            throw new ActivitiIllegalArgumentException("xml request body could not be transformed to a " +
                                    "RestVariable Collection instance.");
                        }
                        List<RestVariable> restVariableList = restVariableCollection.getRestVariables();

                        if(restVariableList.size() == 0){
                            throw new ActivitiIllegalArgumentException("xml request body could not identify any rest " +
                                    "variables to be updated");
                        }
                        for (RestVariable restVariable:restVariableList){
                            inputVariables.add(restVariable);
                        }

                    } catch (JAXBException | IOException e) {
                        throw new ActivitiIllegalArgumentException("xml request body could not be transformed to a " +
                                "RestVariable instance.", e);
                    }
                }


            } catch (Exception e) {
                throw new ActivitiIllegalArgumentException("Failed to serialize to a RestVariable instance", e);
            }

            if ( inputVariables.size() == 0) {
                throw new ActivitiIllegalArgumentException("Request didn't contain a list of restVariables to create.");
            }

            RestVariable.RestVariableScope sharedScope = null;
            RestVariable.RestVariableScope varScope = null;
            Map<String, Object> variablesToSet = new HashMap<>();

            RestResponseFactory restResponseFactory = new RestResponseFactory();

            for (RestVariable var : inputVariables) {
                // Validate if scopes match
                varScope = var.getVariableScope();
                if (var.getName() == null) {
                    throw new ActivitiIllegalArgumentException("Variable name is required");
                }

                if (varScope == null) {
                    varScope = RestVariable.RestVariableScope.LOCAL;
                }
                if (sharedScope == null) {
                    sharedScope = varScope;
                }
                if (varScope != sharedScope) {
                    throw new ActivitiIllegalArgumentException("Only allowed to update multiple restVariables in the same scope.");
                }

                if (hasVariableOnScope(task, var.getName(), varScope)) {
                    throw new BPMNConflictException("Variable '" + var.getName() + "' is already present on task '" + task.getId() +
                            "'.");
                }

                Object actualVariableValue = restResponseFactory.getVariableValue(var);
                variablesToSet.put(var.getName(), actualVariableValue);
                resultVariables.add(restResponseFactory.createRestVariable(var.getName(), actualVariableValue, varScope,
                        task.getId(), RestResponseFactory.VARIABLE_TASK, false, uriInfo.getBaseUri().toString()));
            }

            RuntimeService runtimeService = BPMNOSGIService.getRumtimeService();
            if(runtimeService == null){
                throw new BPMNOSGIServiceException("RuntimeService couldn't be identified");
            }

            TaskService taskService = BPMNOSGIService.getTaskService();
            if(taskService == null){
                throw new BPMNOSGIServiceException("Taskservice couldn't be identified");
            }

            if (!variablesToSet.isEmpty()) {
                if (sharedScope == RestVariable.RestVariableScope.LOCAL) {
                    taskService.setVariablesLocal(task.getId(), variablesToSet);
                } else {
                    if (task.getExecutionId() != null) {
                        // Explicitly set on execution, setting non-local restVariables on task will override local-restVariables if exists
                        runtimeService.setVariables(task.getExecutionId(), variablesToSet);
                    } else {
                        // Standalone task, no global restVariables possible
                        throw new ActivitiIllegalArgumentException("Cannot set global restVariables on task '" + task.getId() +"', task is not part of process.");
                    }
                }
            }
        }

        return Response.ok().status(Response.Status.CREATED).build();
    }

    @PUT
    @Path("/{taskId}/variables/{variableName}")
    @Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
    public Response updateVariable(@PathParam("taskId") String taskId,
                                       @PathParam("variableName") String variableName, @Context HttpServletRequest httpServletRequest) {
        Task task = getTaskFromRequest(taskId);

        RestVariable result = null;
        if (httpServletRequest.getContentType().startsWith(MediaType.MULTIPART_FORM_DATA)) {
            try {
                result = setBinaryVariable(httpServletRequest, task, false, uriInfo);
            } catch (IOException e) {
                throw new ActivitiIllegalArgumentException("Error Reading attachment", e);
            }

            if (result != null) {
                if (!result.getName().equals(variableName)) {
                    throw new ActivitiIllegalArgumentException("Variable name in the body should be equal to the name used in the requested URL.");
                }
            }

        } else {

            RestVariable restVariable = null;
            String contentType = httpServletRequest.getContentType();

            if(MediaType.APPLICATION_JSON.equals(contentType)) {
                try {
                    restVariable = new ObjectMapper().readValue(httpServletRequest.getInputStream(), RestVariable.class);
                } catch (Exception e) {
                    throw new ActivitiIllegalArgumentException("Error converting request body to RestVariable instance", e);
                }
            } else if(MediaType.APPLICATION_XML.equals(contentType)){
                JAXBContext jaxbContext = null;
                try {
                    jaxbContext = JAXBContext.newInstance(RestVariable.class);
                    Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
                    restVariable = (RestVariable) jaxbUnmarshaller.
                            unmarshal(httpServletRequest.getInputStream());

                } catch (JAXBException | IOException e) {
                    throw new ActivitiIllegalArgumentException("xml request body could not be transformed to a " +
                            "Rest Variable instance.", e);
                }
            }
            if (restVariable == null) {
                throw new ActivitiException("Invalid body was supplied");
            }
            if (!restVariable.getName().equals(variableName)) {
                throw new ActivitiIllegalArgumentException("Variable name in the body should be equal to the name used in the requested URL.");
            }

            result = setSimpleVariable(restVariable, task, false);
        }
        return Response.ok().entity(result).build();
    }

    @DELETE
    @Path("/{taskId}/variables/{variableName}")
    @Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
    public Response deleteVariable(@PathParam("taskId") String taskId,
                               @PathParam("variableName") String variableName){
        String scopeString = uriInfo.getQueryParameters().getFirst("scope");
        Task task = getTaskFromRequest(taskId);

        // Determine scope
        RestVariable.RestVariableScope scope = RestVariable.RestVariableScope.LOCAL;
        if (scopeString != null) {
            scope = RestVariable.getScopeFromString(scopeString);
        }

        if (!hasVariableOnScope(task, variableName, scope)) {
            throw new ActivitiObjectNotFoundException("Task '" + task.getId() + "' doesn't have a variable '" +
                    variableName + "' in scope " + scope.name().toLowerCase(), VariableInstanceEntity.class);
        }

        RuntimeService runtimeService = BPMNOSGIService.getRumtimeService();
        if(runtimeService == null){
            throw new BPMNOSGIServiceException("RuntimeService couldn't be identified");
        }

        TaskService taskService = BPMNOSGIService.getTaskService();
        if(taskService == null){
            throw new BPMNOSGIServiceException("Taskservice couldn't be identified");
        }

        if (scope == RestVariable.RestVariableScope.LOCAL) {
            taskService.removeVariableLocal(task.getId(), variableName);
        } else {
            // Safe to use executionId, as the hasVariableOnScope whould have stopped a global-var update on standalone task
            runtimeService.removeVariable(task.getExecutionId(), variableName);
        }

        return Response.ok().status(Response.Status.NO_CONTENT).build();
    }

    @DELETE
    @Path("/{taskId}/variables")
    @Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
    public Response deleteAllLocalTaskVariables(@PathParam("taskId") String taskId) {
        TaskService taskService = BPMNOSGIService.getTaskService();
        if(taskService == null){
            throw new BPMNOSGIServiceException("Taskservice couldn't be identified");
        }

        Task task = getTaskFromRequest(taskId);
        Collection<String> currentVariables = taskService.getVariablesLocal(task.getId()).keySet();
        taskService.removeVariablesLocal(task.getId(), currentVariables);

        return Response.ok().status(Response.Status.NO_CONTENT).build();
    }

    @GET
    @Path("/{taskId}/identitylinks")
    @Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
    public Response getIdentityLinks(@PathParam("taskId") String taskId) {
        Task task = getTaskFromRequest(taskId);
        TaskService taskService = BPMNOSGIService.getTaskService();
        if(taskService == null){
            throw new BPMNOSGIServiceException("Taskservice couldn't be identified");
        }

        List<RestIdentityLink> restIdentityLinks = new RestResponseFactory().createRestIdentityLinks(taskService.getIdentityLinksForTask(task.getId()),
                uriInfo.getBaseUri().toString());
        RestIdentityLinkCollection restIdentityLinkCollection = new RestIdentityLinkCollection();
        restIdentityLinkCollection.setRestIdentityLinks(restIdentityLinks);
        return Response.ok().entity(restIdentityLinkCollection).build();
    }

    @GET
    @Path("/{taskId}/identitylinks/{family}")
    @Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
    public Response getIdentityLinksForFamily(@PathParam("taskId") String taskId,
                                                            @PathParam("family") String family) {

        Task task = getTaskFromRequest(taskId);

        TaskService taskService = BPMNOSGIService.getTaskService();
        if(taskService == null){
            throw new BPMNOSGIServiceException("Taskservice couldn't be identified");
        }

        if (family == null || (!RestUrls.SEGMENT_IDENTITYLINKS_FAMILY_GROUPS.equals(family)
                && !RestUrls.SEGMENT_IDENTITYLINKS_FAMILY_USERS.equals(family))) {
            throw new ActivitiIllegalArgumentException("Identity link family should be 'users' or 'groups'.");
        }

        boolean isUser = family.equals(RestUrls.SEGMENT_IDENTITYLINKS_FAMILY_USERS);
        List<RestIdentityLink> results = new ArrayList<RestIdentityLink>();

        List<IdentityLink> allLinks = taskService.getIdentityLinksForTask(task.getId());
        for (IdentityLink link : allLinks) {
            boolean match = false;
            if (isUser) {
                match = link.getUserId() != null;
            } else {
                match = link.getGroupId() != null;
            }

            if (match) {
                results.add(new RestResponseFactory().createRestIdentityLink(link, uriInfo.getBaseUri().toString()));
            }
        }
        RestIdentityLinkCollection restIdentityLinkCollection = new RestIdentityLinkCollection();
        restIdentityLinkCollection.setRestIdentityLinks(results);
        return Response.ok().entity(restIdentityLinkCollection).build();
    }

    @GET
    @Path("/{taskId}/identitylinks/{family}/{identityId}/{type}")
    @Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
    public Response getIdentityLink(@PathParam("taskId") String taskId,
                                            @PathParam("family") String family, @PathParam("identityId") String identityId,
                                            @PathParam("type") String type,
                                            @Context HttpServletRequest httpServletRequest) {

        Task task = getTaskFromRequest(taskId);
        validateIdentityLinkArguments(family, identityId, type);

        IdentityLink link = getIdentityLink(family, identityId, type, task.getId());

        return Response.ok().entity(new RestResponseFactory().createRestIdentityLink(link, uriInfo.getBaseUri().toString()))
                .build();
    }

    @DELETE
    @Path("/{taskId}/identitylinks/{family}/{identityId}/{type}")
    public Response deleteIdentityLink(@PathParam("taskId") String taskId,
                                   @PathParam("family") String family, @PathParam("identityId") String identityId,
                                   @PathParam("type") String type) {

        Task task = getTaskFromRequest(taskId);

        validateIdentityLinkArguments(family, identityId, type);

        // Check if identitylink to delete exists
        getIdentityLink(family, identityId, type, task.getId());


        TaskService taskService = BPMNOSGIService.getTaskService();
        if(taskService == null){
            throw new BPMNOSGIServiceException("Taskservice couldn't be identified");
        }

        if (RestUrls.SEGMENT_IDENTITYLINKS_FAMILY_USERS.equals(family)) {
            taskService.deleteUserIdentityLink(task.getId(), identityId, type);
        } else {
            taskService.deleteGroupIdentityLink(task.getId(), identityId, type);
        }

        return Response.ok().status(Response.Status.NO_CONTENT).build();
    }

    @POST
    @Path("/{taskId}/identitylinks")
    @Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
    public Response createIdentityLink(@PathParam("taskId") String taskId,
                                               RestIdentityLink identityLink) {

        Task task = getTaskFromRequest(taskId);

        if (identityLink.getGroup() == null && identityLink.getUser() == null) {
            throw new ActivitiIllegalArgumentException("A group or a user is required to create an identity link.");
        }

        if (identityLink.getGroup() != null && identityLink.getUser() != null) {
            throw new ActivitiIllegalArgumentException("Only one of user or group can be used to create an identity link.");
        }

        if (identityLink.getType() == null) {
            throw new ActivitiIllegalArgumentException("The identity link type is required.");
        }
        TaskService taskService = BPMNOSGIService.getTaskService();
        if(taskService == null){
            throw new BPMNOSGIServiceException("Taskservice couldn't be identified");
        }

        if (identityLink.getGroup() != null) {
            taskService.addGroupIdentityLink(task.getId(), identityLink.getGroup(), identityLink.getType());
        } else {
            taskService.addUserIdentityLink(task.getId(), identityLink.getUser(), identityLink.getType());
        }

        RestIdentityLink restIdentityLink = new RestResponseFactory().createRestIdentityLink(identityLink.getType(),
                identityLink.getUser(),
                identityLink.getGroup(), task.getId(), null, null, uriInfo.getBaseUri().toString());

        return Response.ok().status(Response.Status.CREATED).entity(restIdentityLink).build();
    }

    /*@POST
    @Path("/{taskId}/attachments")
    @Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
    @Consumes(MediaType.MULTIPART_FORM_DATA)*/
    public AttachmentResponse createAttachmentForBinary(Task task, MultipartBody multipartBody, HttpServletRequest
            httpServletRequest) throws IOException{
        List<org.apache.cxf.jaxrs.ext.multipart.Attachment> attachments = multipartBody.getAllAttachments();

        int attachmentSize = attachments.size();
        AttachmentDataHolder attachmentDataHolder = new AttachmentDataHolder();
        for (int i =0; i < attachmentSize; i++){
           org.apache.cxf.jaxrs.ext.multipart.Attachment attachment =  attachments.get(i);

            attachment.getHeaderAsList()

            String contentDisposition = attachment.getHeader("Content-Disposition");
            String contentType = attachment.getHeader("Content-Type");
            System.out.println("Going to iterate:" + i);
            System.out.println("contentDisposition:" + contentDisposition);
            if(contentType != null){

                System.out.println("contentType:" + contentType);
            }
            if(contentDisposition != null){
                String nameValue = Utils.getValues(contentDisposition, "name");
                if("name".equals(nameValue)){
                    DataHandler dataHandler = attachment.getDataHandler();
                    CachedOutputStream bos = new CachedOutputStream();
                    IOUtils.copy(dataHandler.getInputStream(), bos);
                    dataHandler.getInputStream().close();
                    bos.close();
                    String fileName = bos.getOut().toString();
                    attachmentDataHolder.setName(fileName);
                    System.out.println(fileName+"FFFFFFFFFFFFFFFFFFFFFFF");
                } else if("type".equals(nameValue)){
                    //String typeValue = Utils.getValues(contentDisposition, "filename");
                    DataHandler dataHandler = attachment.getDataHandler();
                    CachedOutputStream bos = new CachedOutputStream();
                    IOUtils.copy(dataHandler.getInputStream(), bos);
                    dataHandler.getInputStream().close();
                    bos.close();
                    String typeName = bos.getOut().toString();
                    attachmentDataHolder.setType(typeName);
                } else if("description".equals(nameValue)){
                    //String typeValue = Utils.getValues(contentDisposition, "filename");
                    DataHandler dataHandler = attachment.getDataHandler();
                    CachedOutputStream bos = new CachedOutputStream();
                    IOUtils.copy(dataHandler.getInputStream(), bos);
                    dataHandler.getInputStream().close();
                    bos.close();
                    String description = bos.getOut().toString();
                    attachmentDataHolder.setDescription(description);
                }

                if(contentType != null){
                    if("file".equals(nameValue)){
                        //String filename = Utils.getValues(contentDisposition, "filename");
                        attachmentDataHolder.setContentType(contentType);
                        DataHandler dataHandler = attachment.getDataHandler();
                        byte[] attachmentArray2 = IOUtils.toByteArray(dataHandler.getInputStream());
                        attachmentDataHolder.setAttachmentArray(attachmentArray2);
                        System.out.println("YYYYYYYYYYYYYYYYYYYYYYYY:" + new String(attachmentDataHolder.getAttachmentArray()));
                    }
                }
            }
        }

        try {
            attachmentDataHolder.printDebug();
        } catch (Exception e){
            e.printStackTrace();
        }



        //Task task = getTaskFromRequest(taskId);
        Response.ResponseBuilder responseBuilder = Response.ok();


       // byte[] attachmentArray = null;
        if(attachmentDataHolder.getAttachmentArray() == null){
            throw new ActivitiIllegalArgumentException("Empty attachment body was found in request body after " +
                    "decoding the request" +
                    ".");
        }
        AttachmentResponse result = null;
        TaskService taskService = BPMNOSGIService.getTaskService();
        if(taskService == null){
            throw new BPMNOSGIServiceException("Taskservice couldn't be identified");
        }
        try {

            InputStream inputStream = new ByteArrayInputStream(attachmentDataHolder.getAttachmentArray());
            Attachment createdAttachment = taskService.createAttachment(attachmentDataHolder.getType(), task.getId(), task
                            .getProcessInstanceId(),attachmentDataHolder.getName(),attachmentDataHolder
                            .getDescription(),inputStream);

            //responseBuilder.status(Response.Status.CREATED);
            result = new RestResponseFactory().createAttachmentResponse(createdAttachment, uriInfo.getBaseUri()
                    .toString
                    ());

        } catch (Exception e) {
            throw new ActivitiException("Error creating attachment response", e);
        }
        return result;
    }
    @POST
    @Path("/{taskId}/attachments")
    @Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
   // @Consumes({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
    public Response createAttachment(@PathParam("taskId") String taskId, @Context HttpServletRequest
            httpServletRequest/*,  MultipartBody multipartBody*/) {

        AttachmentResponse result = null;
        Task task = getTaskFromRequest(taskId);
        Response.ResponseBuilder responseBuilder = Response.ok();
      //  if(multipartBody != null && multipartBody.getAllAttachments().size() > 0){
        if (httpServletRequest.getContentType().startsWith(MediaType.MULTIPART_FORM_DATA)) {
            try {
                result = createBinaryAttachment( httpServletRequest, task, responseBuilder);
                //result = createAttachmentForBinary(task, multipartBody, httpServletRequest);
            } catch (IOException e) {
                throw new ActivitiIllegalArgumentException("Error Reading attachment", e);

            }
        } else {
            String contentType = httpServletRequest.getContentType();

            AttachmentRequest attachmentRequest = null;

            if(MediaType.APPLICATION_JSON.equals(contentType)) {
                try {
                    attachmentRequest = new ObjectMapper().readValue(httpServletRequest.getInputStream(), AttachmentRequest.class);

                } catch (Exception e) {
                    throw new ActivitiIllegalArgumentException("Failed to serialize to a AttachmentRequest instance", e);
                }
            } else if(contentType.equals(MediaType.APPLICATION_XML)){

                JAXBContext jaxbContext = null;
                try {
                    jaxbContext = JAXBContext.newInstance(AttachmentRequest.class);
                    Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
                    attachmentRequest = (AttachmentRequest) jaxbUnmarshaller.
                            unmarshal(httpServletRequest.getInputStream());

                } catch (JAXBException | IOException e) {
                    throw new ActivitiIllegalArgumentException("xml request body could not be transformed to a " +
                            "AttachmentRequest instance.", e);
                }
            }

            if (attachmentRequest == null) {
                throw new ActivitiIllegalArgumentException("AttachmentRequest properties not found in request");
            }

            result = createSimpleAttachment(attachmentRequest, task);
        }

        return responseBuilder.status(Response.Status.CREATED).entity(result).build();
    }


    @GET
    @Path("/{taskId}/attachments")
    @Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
    public Response getAttachments(@PathParam("taskId") String taskId) {
        List<AttachmentResponse> result = new ArrayList<AttachmentResponse>();
        HistoricTaskInstance task = getHistoricTaskFromRequest(taskId);

        TaskService taskService = BPMNOSGIService.getTaskService();
        if(taskService == null){
            throw new BPMNOSGIServiceException("Taskservice couldn't be identified");
        }

        RestResponseFactory restResponseFactory = new RestResponseFactory();
        String baseUri = uriInfo.getBaseUri().toString();

        for (Attachment attachment : taskService.getTaskAttachments(task.getId())) {
            result.add(restResponseFactory.createAttachmentResponse(attachment, baseUri));
        }

        AttachmentResponseCollection attachmentResponseCollection = new AttachmentResponseCollection();
        attachmentResponseCollection.setAttachmentResponseList(result);

        return Response.ok().entity(attachmentResponseCollection).build();
    }

    @GET
    @Path("/{taskId}/attachments/{attachmentId}")
    @Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
    public Response getAttachment(@PathParam("taskId") String taskId,
                                            @PathParam("attachmentId") String attachmentId) {

        HistoricTaskInstance task = getHistoricTaskFromRequest(taskId);
        TaskService taskService = BPMNOSGIService.getTaskService();

        Attachment attachment = taskService.getAttachment(attachmentId);
        if (attachment == null || !task.getId().equals(attachment.getTaskId())) {
            throw new ActivitiObjectNotFoundException("Task '" + task.getId() +"' doesn't have an attachment with id '" + attachmentId + "'.", Comment.class);
        }

        return Response.ok().entity(new RestResponseFactory().createAttachmentResponse(attachment, uriInfo.getBaseUri
                ().toString())).build();
    }

    @DELETE
    @Path("/{taskId}/attachments/{attachmentId}")
    public Response deleteAttachment(@PathParam("taskId") String taskId,
                                 @PathParam("attachmentId") String attachmentId) {

        Task task = getTaskFromRequest(taskId);
        TaskService taskService = BPMNOSGIService.getTaskService();
        Attachment attachment = taskService.getAttachment(attachmentId);
        if (attachment == null || !task.getId().equals(attachment.getTaskId())) {
            throw new ActivitiObjectNotFoundException("Task '" + task.getId() +"' doesn't have an attachment with id '" + attachmentId + "'.", Comment.class);
        }

        taskService.deleteAttachment(attachmentId);

        return Response.ok().status(Response.Status.NO_CONTENT).build();
    }

    @GET
    @Path("/{taskId}/attachments/{attachmentId}/content")
    public Response getAttachmentContent(@PathParam("taskId") String taskId,
                                                       @PathParam("attachmentId") String attachmentId) {

        TaskService taskService = BPMNOSGIService.getTaskService();
        HistoricTaskInstance task = getHistoricTaskFromRequest(taskId);
        Attachment attachment = taskService.getAttachment(attachmentId);

        if (attachment == null || !task.getId().equals(attachment.getTaskId())) {
            throw new ActivitiObjectNotFoundException("Task '" + task.getId() +"' doesn't have an attachment with id '" + attachmentId + "'.", Attachment.class);
        }

        InputStream attachmentStream = taskService.getAttachmentContent(attachmentId);
        if (attachmentStream == null) {
            throw new ActivitiObjectNotFoundException("Attachment with id '" + attachmentId +
                    "' doesn't have content associated with it.", Attachment.class);
        }

        Response.ResponseBuilder responseBuilder = Response.ok();

        String type = attachment.getType();
        if(type != null){
            responseBuilder.type(attachment.getType());
        } else {
            responseBuilder.type("application/octet-stream");
        }

        byte[] attachmentArray = null;
        try {
            attachmentArray = IOUtils.toByteArray(attachmentStream);
        } catch (IOException e) {
            throw new ActivitiException("Error creating attachment data", e);
        }
        return responseBuilder.entity(attachmentArray).build();
    }


    @GET
    @Path("/{taskId}/comments")
    @Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
    public Response getComments(@PathParam("taskId") String taskId) {
        HistoricTaskInstance task = getHistoricTaskFromRequest(taskId);
        TaskService taskService = BPMNOSGIService.getTaskService();
        List<CommentResponse> commentResponseList = new RestResponseFactory().createRestCommentList( taskService
                .getTaskComments(task.getId()), uriInfo.getBaseUri().toString());
        CommentResponseCollection commentResponseCollection = new CommentResponseCollection();
        commentResponseCollection.setCommentResponseList(commentResponseList);

        return Response.ok().entity(commentResponseCollection).build();
    }

    @POST
    @Path("/{taskId}/comments")
    @Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
    @Consumes({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
    public Response createComment(@PathParam("taskId")  String taskId, CommentRequest comment) {

        Task task = getTaskFromRequest(taskId);

        if (comment.getMessage() == null) {
            throw new ActivitiIllegalArgumentException("Comment text is required.");
        }

        String processInstanceId = null;
        TaskService taskService = BPMNOSGIService.getTaskService();
        if (comment.isSaveProcessInstanceId()) {
            Task taskEntity = taskService.createTaskQuery().taskId(task.getId()).singleResult();
            processInstanceId = taskEntity.getProcessInstanceId();
        }
        Comment createdComment = taskService.addComment(task.getId(), processInstanceId, comment.getMessage());

        CommentResponse commentResponse =  new RestResponseFactory().createRestComment(createdComment, uriInfo
                .getBaseUri().toString());
        return Response.ok().status(Response.Status.CREATED).entity(commentResponse).build();
    }

    @GET
    @Path("/{taskId}/comments/{commentId}")
    @Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
    public Response getComment(@PathParam("taskId") String taskId,
                                      @PathParam("commentId") String commentId) {

        HistoricTaskInstance task = getHistoricTaskFromRequest(taskId);
        TaskService taskService = BPMNOSGIService.getTaskService();
        Comment comment = taskService.getComment(commentId);
        if (comment == null || !task.getId().equals(comment.getTaskId())) {
            throw new ActivitiObjectNotFoundException("Task '" + task.getId() +"' doesn't have a comment with id '" + commentId + "'.", Comment.class);
        }

        return Response.ok().entity(new RestResponseFactory().createRestComment(comment, uriInfo.getBaseUri()
                .toString())).build();
    }

    @DELETE
    @Path("/{taskId}/comments/{commentId}")
    @Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
    public Response deleteComment(@PathParam("taskId") String taskId,
                              @PathParam("commentId") String commentId) {

        // Check if task exists
        Task task = getTaskFromRequest(taskId);
        TaskService taskService = BPMNOSGIService.getTaskService();

        Comment comment = taskService.getComment(commentId);
        if (comment == null || comment.getTaskId() == null || !comment.getTaskId().equals(task.getId())) {
            throw new ActivitiObjectNotFoundException("Task '" + task.getId() +"' doesn't have a comment with id '" + commentId + "'.", Comment.class);
        }

        taskService.deleteComment(commentId);
        return Response.ok().status(Response.Status.NO_CONTENT).build();
    }

    @GET
    @Path("/{taskId}/events")
    @Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
    public Response getEvents(@PathParam("taskId") String taskId) {
        HistoricTaskInstance task = getHistoricTaskFromRequest(taskId);
        TaskService taskService = BPMNOSGIService.getTaskService();
        List<EventResponse> eventResponseList = new RestResponseFactory().createEventResponseList(taskService
                .getTaskEvents(task.getId()),uriInfo.getBaseUri().toString());

        EventResponseCollection eventResponseCollection = new EventResponseCollection();
        eventResponseCollection.setEventResponseList(eventResponseList);

        return Response.ok().entity(eventResponseCollection).build();
    }

    @GET
    @Path("/{taskId}/events/{eventId}")
    @Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
    public Response getEvent(@PathParam("taskId") String taskId,
                                  @PathParam("eventId") String eventId) {

        HistoricTaskInstance task = getHistoricTaskFromRequest(taskId);
        TaskService taskService = BPMNOSGIService.getTaskService();

        Event event = taskService.getEvent(eventId);
        if (event == null || !task.getId().equals(event.getTaskId())) {
            throw new ActivitiObjectNotFoundException("Task '" + task.getId() +"' doesn't have an event with id '" + eventId + "'.", Event.class);
        }

        EventResponse eventResponse = new RestResponseFactory().createEventResponse(event, uriInfo.getBaseUri()
                .toString());
        return Response.ok().entity(eventResponse).build();
    }

    @DELETE
    @Path("/{taskd}/events/{eventId}")
    public Response deleteEvent(@PathParam("taskId") String taskId,
                            @PathParam("eventId") String eventId) {

        // Check if task exists
        Task task = getTaskFromRequest(taskId);
        TaskService taskService = BPMNOSGIService.getTaskService();
        Event event = taskService.getEvent(eventId);
        if (event == null || event.getTaskId() == null || !event.getTaskId().equals(task.getId())) {
            throw new ActivitiObjectNotFoundException("Task '" + task.getId() +"' doesn't have an event with id '" + event + "'.", Event.class);
        }

        taskService.deleteComment(eventId);
        return Response.ok().status(Response.Status.NO_CONTENT).build();
    }

    protected HistoricTaskInstance getHistoricTaskFromRequest(String taskId) {

        HistoryService historyService = BPMNOSGIService.getHistoryService();
        if(historyService == null){
            throw new BPMNOSGIServiceException("HistoryService couldn't be identified");
        }


        HistoricTaskInstance task = historyService.createHistoricTaskInstanceQuery().taskId(taskId).singleResult();
        if (task == null) {
            throw new ActivitiObjectNotFoundException("Could not find a task with id '" + taskId + "'.", Task.class);
        }
        return task;
    }


    protected AttachmentResponse createSimpleAttachment(AttachmentRequest attachmentRequest, Task task) {

        if (attachmentRequest.getName() == null) {
            throw new ActivitiIllegalArgumentException("Attachment name is required.");
        }

        TaskService taskService = BPMNOSGIService.getTaskService();
        if(taskService == null){
            throw new BPMNOSGIServiceException("Taskservice couldn't be identified");
        }
        Attachment createdAttachment = taskService.createAttachment(attachmentRequest.getType(), task.getId(),
                task.getProcessInstanceId(), attachmentRequest.getName(), attachmentRequest.getDescription(), attachmentRequest.getExternalUrl());

        return new RestResponseFactory().createAttachmentResponse(createdAttachment, uriInfo.getBaseUri().toString());
    }

    protected AttachmentResponse createBinaryAttachment(HttpServletRequest httpServletRequest, Task task, Response.ResponseBuilder responseBuilder) throws
            IOException {

        String name  = uriInfo.getQueryParameters().getFirst("name");
        String description  = uriInfo.getQueryParameters().getFirst("description");
        String type  = uriInfo.getQueryParameters().getFirst("type");


        byte[] byteArray = Utils.processMultiPartFile(httpServletRequest, "Attachment content");
        if(byteArray == null){
            throw new ActivitiIllegalArgumentException("Empty attachment body was found in request body after " +
                    "decoding the request" +
                    ".");
        }

        if (name == null) {
            throw new ActivitiIllegalArgumentException("Attachment name is required.");
        }


        TaskService taskService = BPMNOSGIService.getTaskService();
        if(taskService == null){
            throw new BPMNOSGIServiceException("Taskservice couldn't be identified");
        }


        try {
            InputStream inputStream = new ByteArrayInputStream(byteArray);
            Attachment createdAttachment = taskService.createAttachment(type, task.getId(), task.getProcessInstanceId(), name,
                    description, inputStream);

            responseBuilder.status(Response.Status.CREATED);
            return new RestResponseFactory().createAttachmentResponse(createdAttachment, uriInfo.getBaseUri().toString());

        } catch (Exception e) {
            throw new ActivitiException("Error creating attachment response", e);
        }
    }

    protected IdentityLink getIdentityLink(String family, String identityId, String type, String taskId) {
        boolean isUser = family.equals(RestUrls.SEGMENT_IDENTITYLINKS_FAMILY_USERS);

        TaskService taskService = BPMNOSGIService.getTaskService();
        if(taskService == null){
            throw new BPMNOSGIServiceException("Taskservice couldn't be identified");
        }

        // Perhaps it would be better to offer getting a single identitylink from the API
        List<IdentityLink> allLinks = taskService.getIdentityLinksForTask(taskId);
        for (IdentityLink link : allLinks) {
            boolean rightIdentity = false;
            if (isUser) {
                rightIdentity = identityId.equals(link.getUserId());
            } else {
                rightIdentity = identityId.equals(link.getGroupId());
            }

            if (rightIdentity && link.getType().equals(type)) {
                return link;
            }
        }
        throw new ActivitiObjectNotFoundException("Could not find the requested identity link.", IdentityLink.class);
    }
    protected void validateIdentityLinkArguments(String family, String identityId, String type) {
        if (family == null || (!RestUrls.SEGMENT_IDENTITYLINKS_FAMILY_GROUPS.equals(family)
                && !RestUrls.SEGMENT_IDENTITYLINKS_FAMILY_USERS.equals(family))) {
            throw new ActivitiIllegalArgumentException("Identity link family should be 'users' or 'groups'.");
        }
        if (identityId == null) {
            throw new ActivitiIllegalArgumentException("IdentityId is required.");
        }
        if (type == null) {
            throw new ActivitiIllegalArgumentException("Type is required.");
        }
    }

    protected RestVariable setSimpleVariable(RestVariable restVariable, Task task, boolean isNew) {
        if (restVariable.getName() == null) {
            throw new ActivitiIllegalArgumentException("Variable name is required");
        }

        // Figure out scope, revert to local is omitted
        RestVariable.RestVariableScope scope = restVariable.getVariableScope();
        if (scope == null) {
            scope = RestVariable.RestVariableScope.LOCAL;
        }

        RestResponseFactory restResponseFactory = new RestResponseFactory();
        Object actualVariableValue = restResponseFactory.getVariableValue(restVariable);
        setVariable(task, restVariable.getName(), actualVariableValue, scope, isNew);

        return restResponseFactory.createRestVariable(restVariable.getName(), actualVariableValue, scope,
                task.getId(), RestResponseFactory.VARIABLE_TASK, false, uriInfo.getBaseUri().toString());
    }


    protected void completeTask(Task task, TaskActionRequest actionRequest, TaskService taskService) {

        if(actionRequest.getVariables() != null) {
            Map<String, Object> variablesToSet = new HashMap<String, Object>();

            RestResponseFactory restResponseFactory = new RestResponseFactory();
            for(RestVariable var : actionRequest.getVariables()) {
                if(var.getName() == null) {
                    throw new ActivitiIllegalArgumentException("Variable name is required");
                }

                Object actualVariableValue = restResponseFactory.getVariableValue(var);
                variablesToSet.put(var.getName(), actualVariableValue);
            }

            taskService.complete(task.getId(), variablesToSet);
        } else {
            taskService.complete(task.getId());
        }

    }

    protected void claimTask(Task task, TaskActionRequest actionRequest, TaskService taskService) {
        // In case the task is already claimed, a ActivitiTaskAlreadyClaimedException is thrown and converted to
        // a CONFLICT response by the ExceptionHandlerAdvice
        taskService.claim(task.getId(), actionRequest.getAssignee());
    }

    protected void delegateTask(Task task, TaskActionRequest actionRequest, TaskService taskService) {
        if (actionRequest.getAssignee() == null) {
            throw new ActivitiIllegalArgumentException("An assignee is required when delegating a task.");
        }

        taskService.delegateTask(task.getId(), actionRequest.getAssignee());
    }

    protected void resolveTask(Task task, TaskService taskService) {
        taskService.resolveTask(task.getId());
    }

    protected void populateTaskFromRequest(Task task, TaskRequest taskRequest) {
        if (taskRequest.isNameSet()) {
            task.setName(taskRequest.getName());
        }
        if (taskRequest.isAssigneeSet()) {
            task.setAssignee(taskRequest.getAssignee());
        }
        if (taskRequest.isDescriptionSet()) {
            task.setDescription(taskRequest.getDescription());
        }
        if (taskRequest.isDuedateSet()) {
            task.setDueDate(taskRequest.getDueDate());
        }
        if (taskRequest.isOwnerSet()) {
            task.setOwner(taskRequest.getOwner());
        }
        if (taskRequest.isParentTaskIdSet()) {
            task.setParentTaskId(taskRequest.getParentTaskId());
        }
        if (taskRequest.isPrioritySet()) {
            task.setPriority(taskRequest.getPriority());
        }
        if (taskRequest.isCategorySet()) {
            task.setCategory(taskRequest.getCategory());
        }
        if (taskRequest.isTenantIdSet()) {
            task.setTenantId(taskRequest.getTenantId());
        }
        if (taskRequest.isFormKeySet()) {
            task.setFormKey(taskRequest.getFormKey());
        }

        if (taskRequest.isDelegationStateSet()) {
            DelegationState delegationState = getDelegationState(taskRequest.getDelegationState());
            task.setDelegationState(delegationState);
        }
    }
}
