/**
 * Copyright (c) 2015-2016 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.bpmn.rest.service.runtime;

import io.netty.handler.codec.http.QueryStringDecoder;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.HistoryService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.task.Attachment;
import org.activiti.engine.task.Comment;
import org.activiti.engine.task.DelegationState;
import org.activiti.engine.task.Event;
import org.activiti.engine.task.IdentityLink;
import org.activiti.engine.task.Task;
import org.apache.commons.io.IOUtils;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.bpmn.core.BPMNEngineService;
import org.wso2.carbon.bpmn.rest.common.RequestUtil;
import org.wso2.carbon.bpmn.rest.common.RestResponseFactory;
import org.wso2.carbon.bpmn.rest.common.RestUrls;
import org.wso2.carbon.bpmn.rest.common.exception.BPMNForbiddenException;
import org.wso2.carbon.bpmn.rest.engine.variable.RestVariable;
import org.wso2.carbon.bpmn.rest.internal.BPMNOSGIService;
import org.wso2.carbon.bpmn.rest.model.common.DataResponse;
import org.wso2.carbon.bpmn.rest.model.common.RestIdentityLink;
import org.wso2.carbon.bpmn.rest.model.runtime.AttachmentRequest;
import org.wso2.carbon.bpmn.rest.model.runtime.AttachmentResponse;
import org.wso2.carbon.bpmn.rest.model.runtime.AttachmentResponseCollection;
import org.wso2.carbon.bpmn.rest.model.runtime.CommentRequest;
import org.wso2.carbon.bpmn.rest.model.runtime.CommentResponse;
import org.wso2.carbon.bpmn.rest.model.runtime.CommentResponseCollection;
import org.wso2.carbon.bpmn.rest.model.runtime.EventResponse;
import org.wso2.carbon.bpmn.rest.model.runtime.EventResponseCollection;
import org.wso2.carbon.bpmn.rest.model.runtime.RestIdentityLinkCollection;
import org.wso2.carbon.bpmn.rest.model.runtime.RestVariableCollection;
import org.wso2.carbon.bpmn.rest.model.runtime.TaskActionRequest;
import org.wso2.carbon.bpmn.rest.model.runtime.TaskQueryRequest;
import org.wso2.carbon.bpmn.rest.model.runtime.TaskRequest;
import org.wso2.carbon.bpmn.rest.model.runtime.TaskResponse;
import org.wso2.carbon.bpmn.rest.service.base.BaseTaskService;
import org.wso2.msf4j.Microservice;
import org.wso2.msf4j.Request;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

//import org.apache.commons.lang.CharSet;
//import org.apache.cxf.jaxrs.ext.multipart.MultipartBody;
//import org.wso2.carbon.bpmn.rest.common.utils.Utils;
//import javax.activation.DataHandler;
//import javax.servlet.http.HttpServletRequest;

/**
 *
 */
@Component(
        name = "org.wso2.carbon.bpmn.rest.service.runtime.WorkflowTaskService",
        service = Microservice.class,
        immediate = true)
@Path("/tasks")
public class WorkflowTaskService extends BaseTaskService implements Microservice {

    private static final Logger log = LoggerFactory.getLogger(WorkflowTaskService.class);

    @Reference(
            name = "org.wso2.carbon.bpmn.core.BPMNEngineService",
            service = BPMNEngineService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unRegisterBPMNEngineService")
    public void setBpmnEngineService(BPMNEngineService engineService) {
        log.info("Setting BPMN engine " + engineService);

    }

    protected void unRegisterBPMNEngineService(BPMNEngineService engineService) {
        log.info("Unregister BPMNEngineService..");
    }


    @Activate
    protected void activate(BundleContext bundleContext) {
        // Nothing to do
    }

    @Deactivate
    protected void deactivate(BundleContext bundleContext) {
        // Nothing to do
    }

    @GET
    @Path("/")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    public Response getTasks(@Context Request currentRequest) {

        // Create a Task query request
        TaskQueryRequest request = new TaskQueryRequest();

        Map<String, String> requestParams = new HashMap<>();
        QueryStringDecoder decoder = new QueryStringDecoder(currentRequest.getUri());
        Map<String, List<String>> queryParams = decoder.parameters();
        if (decoder.parameters().size() > 0) {
            for (String property : ALL_PROPERTIES_LIST) {
                String value = decoder.parameters().get(property).get(0);

                if (value != null) {
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
                request.setIncludeTaskLocalVariables(
                        Boolean.valueOf(requestParams.get("includeTaskLocalVariables")));
            }

            if (requestParams.containsKey("includeProcessVariables")) {
                request.setIncludeProcessVariables(
                        Boolean.valueOf(requestParams.get("includeProcessVariables")));
            }

            if (requestParams.containsKey("tenantId")) {
                request.setTenantId(requestParams.get("tenantId"));
            }

            if (requestParams.containsKey("tenantIdLike")) {
                request.setTenantIdLike(requestParams.get("tenantIdLike"));
            }

            if (requestParams.containsKey("withoutTenantId") &&
                    Boolean.valueOf(requestParams.get("withoutTenantId"))) {
                request.setWithoutTenantId(Boolean.TRUE);
            }

            if (requestParams.containsKey("candidateOrAssigned")) {
                request.setCandidateOrAssigned(requestParams.get("candidateOrAssigned"));
            }
        }
            DataResponse dataResponse = getTasksFromQueryRequest(request, queryParams, requestParams,
                    currentRequest.getUri());
            return Response.ok().entity(dataResponse).build();
            //return getTasksFromQueryRequest(request, requestParams);

    }

    @GET
    @Path("/{task-id}")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    public Response getTask(@PathParam("task-id") String taskId, @Context Request request) {
        TaskResponse taskResponse = new RestResponseFactory()
                .createTaskResponse(getTaskFromRequest(taskId), request.getUri());

        return Response.ok().entity(taskResponse).build();
    }

    @PUT
    @Path("/{task-id}")
    @Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    public Response updateTask(@PathParam("task-id") String taskId, TaskRequest taskRequest,
                               @Context Request request) {

        if (taskRequest == null) {
            throw new ActivitiException("A request body was expected when updating the task.");
        }

        Task task = getTaskFromRequest(taskId);

        // Populate the task properties based on the request
        populateTaskFromRequest(task, taskRequest);

        TaskService taskService = BPMNOSGIService.getTaskService();

        // Save the task and fetch agian, it's possible that an assignment-listener has updated
        // fields after it was saved so we can't use the in-memory task
        taskService.saveTask(task);
        task = taskService.createTaskQuery().taskId(task.getId()).singleResult();

        return Response.ok()
                       .entity(new RestResponseFactory().createTaskResponse(task, request.getUri()))
                       .build();
    }

    @POST
    @Path("/{task-id}")
    @Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    public Response executeTaskAction(@PathParam("task-id") String taskId,
                                      TaskActionRequest actionRequest) {
        if (actionRequest == null) {
            throw new ActivitiException(
                    "A request body was expected when executing a task action.");
        }

        Task task = getTaskFromRequest(taskId);

        TaskService taskService = BPMNOSGIService.getTaskService();

        if (TaskActionRequest.ACTION_COMPLETE.equals(actionRequest.getAction())) {
            completeTask(task, actionRequest, taskService);

        } else if (TaskActionRequest.ACTION_CLAIM.equals(actionRequest.getAction())) {
            claimTask(task, actionRequest, taskService);

        } else if (TaskActionRequest.ACTION_DELEGATE.equals(actionRequest.getAction())) {
            delegateTask(task, actionRequest, taskService);

        } else if (TaskActionRequest.ACTION_RESOLVE.equals(actionRequest.getAction())) {
            resolveTask(task, taskService);

        } else {
            throw new ActivitiIllegalArgumentException(
                    "Invalid action: '" + actionRequest.getAction() + "'.");
        }

        return Response.ok().status(Response.Status.OK).build();
    }

    @DELETE
    @Path("/{task-id}")
    public Response deleteTask(@PathParam("task-id") String taskId,
                               @DefaultValue("false") @QueryParam("cascadeHistory") Boolean cascadeHistory,
                               @DefaultValue("false") @QueryParam("deleteReason") String deleteReason) {
        Task taskToDelete = getTaskFromRequest(taskId);
        if (taskToDelete.getExecutionId() != null) {
            // Can't delete a task that is part of a process instance
            throw new BPMNForbiddenException(
                    "Cannot delete a task that is part of a process-instance.");
        }

        TaskService taskService = BPMNOSGIService.getTaskService();

        if (cascadeHistory != null) {
            // Ignore delete-reason since the task-history (where the reason is recorded)
            // will be deleted anyway
            taskService.deleteTask(taskToDelete.getId(), cascadeHistory);
        } else {
            // Delete with delete-reason
            taskService.deleteTask(taskToDelete.getId(), deleteReason);
        }
        return Response.ok().status(Response.Status.NO_CONTENT).build();
    }

    @GET
    @Path("/{task-id}/variables")
    @Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    public Response getVariables(@PathParam("task-id") String taskId,
                                 @DefaultValue("false") @QueryParam("scope") String scope,
                                 @Context Request req) {

        List<RestVariable> result = new ArrayList<>();
        Map<String, RestVariable> variableMap = new HashMap<String, RestVariable>();

        // Check if it's a valid task to get the variables for
        Task task = getTaskFromRequest(taskId);

        RestVariable.RestVariableScope variableScope = RestVariable.getScopeFromString(scope);
        if (variableScope == null) {
            // Use both local and global variables
            addLocalVariables(task, variableMap, req.getUri());
            addGlobalVariables(task, variableMap, req.getUri());

        } else if (variableScope == RestVariable.RestVariableScope.GLOBAL) {
            addGlobalVariables(task, variableMap, req.getUri());

        } else if (variableScope == RestVariable.RestVariableScope.LOCAL) {
            addLocalVariables(task, variableMap, req.getUri());
        }

        // Get unique variables from map
        result.addAll(variableMap.values());

        RestVariableCollection restVariableCollection = new RestVariableCollection();
        restVariableCollection.setRestVariables(result);
        return Response.ok().entity(restVariableCollection).build();
    }

    @GET
    @Path("/{task-id}/variables/{variable-name}")
    @Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    public RestVariable getVariable(@PathParam("task-id") String taskId,
                                    @PathParam("variable-name") String variableName,
                                    @DefaultValue("false") @QueryParam("scope") String scope,
                                    @Context Request request) {

        return getVariableFromRequest(taskId, variableName, scope, false, request.getUri());
    }

    @GET
    @Path("/{task-id}/variables/{variable-name}/data")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    public Response getVariableData(@PathParam("task-id") String taskId,
                                    @PathParam("variable-name") String variableName,
                                    @DefaultValue("false") @QueryParam("scope") String scope,
                                    @Context Request request) {

        Response.ResponseBuilder responseBuilder = Response.ok();
        try {
            byte[] result = null;

            RestVariable variable =
                    getVariableFromRequest(taskId, variableName, scope, true, request.getUri());
            if (RestResponseFactory.BYTE_ARRAY_VARIABLE_TYPE.equals(variable.getType())) {
                result = (byte[]) variable.getValue();
                responseBuilder.type(MediaType.APPLICATION_OCTET_STREAM);

            } else if (RestResponseFactory.SERIALIZABLE_VARIABLE_TYPE.equals(variable.getType())) {
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                ObjectOutputStream outputStream = new ObjectOutputStream(buffer);
                outputStream.writeObject(variable.getValue());
                outputStream.close();
                result = buffer.toByteArray();
                responseBuilder.type("application/x-java-serialized-object");

            } else {
                throw new ActivitiObjectNotFoundException(
                        "The variable does not have a binary data stream.", null);
            }
            return responseBuilder.entity(result).build();
        } catch (IOException ioe) {
            // Re-throw IOException
            throw new ActivitiException("Unexpected error getting variable data", ioe);
        }
    }

    /* todo
        @POST
        @Path("/{taskId}/variables")
        @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
        @Consumes(MediaType.MULTIPART_FORM_DATA)
        public Response createBinaryTaskVariable(@PathParam("taskId") String taskId,
        MultipartBody multipartBody) {

            Task task = getTaskFromRequest(taskId);
            Object result = null;
            try {
                result = setBinaryVariable(multipartBody, task, true, uriInfo);
            } catch (IOException e) {
                throw new ActivitiIllegalArgumentException("Error Reading variable attachment", e);
            }

            if (result != null) {
                return Response.ok().status(Response.Status.CREATED).build();
            } else {
                throw new ActivitiIllegalArgumentException("Binary Task variable creation failed");
            }

        }
    */
    //TODO: CHECK HTTPSERVLETRequest
//    @POST
//    @Path("/{task-id}/variables")
//    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
//    @Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
//    public Response createTaskVariable(@PathParam("task-id") String taskId,
//                                       @Context HttpServletRequest httpServletRequest,
//                                       @Context Request request) {
//
//        Task task = getTaskFromRequest(taskId);
//
//        Object result = null;
//        List<RestVariable> inputVariables = new ArrayList<>();
//        List<RestVariable> resultVariables = new ArrayList<>();
//        //result = resultVariables;
//
//        //  try {
//        String contentType = httpServletRequest.getContentType();
//        if (contentType.equals(MediaType.APPLICATION_JSON)) {
//            try {
//                @SuppressWarnings("unchecked") List<Object> variableObjects =
//                        (List<Object>) new ObjectMapper()
//                                .readValue(httpServletRequest.getInputStream(), List.class);
//                for (Object restObject : variableObjects) {
//                    RestVariable restVariable =
//                            new ObjectMapper().convertValue(restObject, RestVariable.class);
//                    inputVariables.add(restVariable);
//                }
//            } catch (IOException e) {
//                throw new ActivitiIllegalArgumentException(
//                        "request body could not be transformed to a RestVariable " + "instance.",
//                        e);
//            }
//
//        } else if (contentType.equals(MediaType.APPLICATION_XML)) {
//
//            JAXBContext jaxbContext = null;
//            try {
//                jaxbContext = JAXBContext.newInstance(RestVariableCollection.class);
//                Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
//                RestVariableCollection restVariableCollection =
//                        (RestVariableCollection) jaxbUnmarshaller
//                                .unmarshal(httpServletRequest.getInputStream());
//                if (restVariableCollection == null) {
//                    throw new ActivitiIllegalArgumentException(
//                            "xml request body could not be transformed to a " +
//                            "RestVariable Collection instance.");
//                }
//                List<RestVariable> restVariableList = restVariableCollection.getRestVariables();
//
//                if (restVariableList.size() == 0) {
//                    throw new ActivitiIllegalArgumentException(
//                            "xml request body could not identify any rest " +
//                            "variables to be updated");
//                }
//                for (RestVariable restVariable : restVariableList) {
//                    inputVariables.add(restVariable);
//                }
//
//            } catch (JAXBException | IOException e) {
//                throw new ActivitiIllegalArgumentException(
//                        "xml request body could not be transformed to a " +
//                        "RestVariable instance.", e);
//            }
//        }
//
//        /*} catch (Exception e) {
//            throw new ActivitiIllegalArgumentException(
//                    "Failed to serialize to a RestVariable instance", e);
//        }*/
//
//        if (inputVariables.size() == 0) {
//            throw new ActivitiIllegalArgumentException(
//                    "Request didn't contain a list of variables to create.");
//        }
//
//        RestVariable.RestVariableScope sharedScope = null;
//        RestVariable.RestVariableScope varScope = null;
//        Map<String, Object> variablesToSet = new HashMap<>();
//
//        RestResponseFactory restResponseFactory = new RestResponseFactory();
//
//        for (RestVariable var : inputVariables) {
//            // Validate if scopes match
//            varScope = var.getVariableScope();
//            if (var.getName() == null) {
//                throw new ActivitiIllegalArgumentException("Variable name is required");
//            }
//
//            if (varScope == null) {
//                varScope = RestVariable.RestVariableScope.LOCAL;
//            }
//            if (sharedScope == null) {
//                sharedScope = varScope;
//            }
//            if (varScope != sharedScope) {
//                throw new ActivitiIllegalArgumentException(
//                        "Only allowed to update multiple variables in the same scope.");
//            }
//
//            if (hasVariableOnScope(task, var.getName(), varScope)) {
//                throw new BPMNConflictException(
//                        "Variable '" + var.getName() + "' is already present on task '" +
//                        task.getId() +
//                        "'.");
//            }
//
//            Object actualVariableValue = restResponseFactory.getVariableValue(var);
//            variablesToSet.put(var.getName(), actualVariableValue);
//            resultVariables.add(restResponseFactory
//                                        .createRestVariable(var.getName(), actualVariableValue,
//                                                            varScope, task.getId(),
//                                                            RestResponseFactory.VARIABLE_TASK,
//                                                            false, request.getUri()));
//        }
//
//        RuntimeService runtimeService = BPMNOSGIService.getRumtimeService();
//
//        TaskService taskService = BPMNOSGIService.getTaskService();
//
//        if (!variablesToSet.isEmpty()) {
//            if (sharedScope == RestVariable.RestVariableScope.LOCAL) {
//                taskService.setVariablesLocal(task.getId(), variablesToSet);
//            } else {
//                if (task.getExecutionId() != null) {
//                    // Explicitly set on execution, setting non-local variables on task will
//                    // override local-variables if exists
//                    runtimeService.setVariables(task.getExecutionId(), variablesToSet);
//                } else {
//                    // Standalone task, no global variables possible
//                    throw new ActivitiIllegalArgumentException(
//                            "Cannot set global variables on task '" + task.getId() +
//                            "', task is not part of process.");
//                }
//            }
//        }
//
//        return Response.ok().status(Response.Status.CREATED).build();
//    }

    /* TODO
        @PUT
        @Path("/{taskId}/variables/{variableName}")
        @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
        @Consumes(MediaType.MULTIPART_FORM_DATA)
        public Response updateBinaryTaskVariable(@PathParam("taskId") String taskId,
        @PathParam("variableName") String variableName,
                                                 MultipartBody multipartBody) {
            Task task = getTaskFromRequest(taskId);
            RestVariable result = null;
            try {
                result = setBinaryVariable(multipartBody, task, false, uriInfo);
            } catch (IOException e) {
                throw new ActivitiIllegalArgumentException("Error Reading variable attachment", e);
            }
            if (result != null) {
                if (!result.getName().equals(variableName)) {
                    throw new ActivitiIllegalArgumentException("Variable name in the body
                    should be equal to the name used in the requested URL.");
                }
            }

            return Response.ok().entity(result).build();
        }
    *///todo:
//    @PUT
//    @Path("/{task-id}/variables/{variable-name}")
//    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
//    @Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
//    public Response updateTaskVariable(@PathParam("task-id") String taskId,
//                                       @PathParam("variable-name") String variableName,
//                                       @Context HttpServletRequest httpServletRequest,
//                                       @Context Request request) {
//        Task task = getTaskFromRequest(taskId);
//
//        RestVariable restVariable = null;
//        String contentType = httpServletRequest.getContentType();
//
//        if (MediaType.APPLICATION_JSON.equals(contentType)) {
//            try {
//                restVariable = new ObjectMapper()
//                        .readValue(httpServletRequest.getInputStream(), RestVariable.class);
//            } catch (Exception e) {
//                throw new ActivitiIllegalArgumentException(
//                        "Error converting request body to RestVariable instance", e);
//            }
//        } else if (MediaType.APPLICATION_XML.equals(contentType)) {
//            JAXBContext jaxbContext = null;
//            try {
//                jaxbContext = JAXBContext.newInstance(RestVariable.class);
//                Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
//                restVariable = (RestVariable) jaxbUnmarshaller
//                        .unmarshal(httpServletRequest.getInputStream());
//
//            } catch (JAXBException | IOException e) {
//                throw new ActivitiIllegalArgumentException(
//                        "xml request body could not be transformed to a " +
//                        "Rest Variable instance.", e);
//            }
//        }
//        if (restVariable == null) {
//            throw new ActivitiException("Invalid body was supplied");
//        }
//        if (!restVariable.getName().equals(variableName)) {
//            throw new ActivitiIllegalArgumentException(
//                    "Variable name in the body should be equal to the name used in the requested" +
//                    " URL.");
//        }
//
//        RestVariable result = setSimpleVariable(restVariable, task, false, request.getUri());
//        return Response.ok().entity(result).build();
//    }
//
//    @DELETE
//    @Path("/{task-id}/variables/{variable-name}")
//    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
//    public Response deleteVariable(@PathParam("task-id") String taskId,
//                                   @PathParam("variable-name") String variableName,
//                                   @QueryParam("scope") String scopeString) {
//        Task task = getTaskFromRequest(taskId);
//
//        // Determine scope
//        RestVariable.RestVariableScope scope = RestVariable.RestVariableScope.LOCAL;
//        if (scopeString != null) {
//            scope = RestVariable.getScopeFromString(scopeString);
//        }
//
//        if (!hasVariableOnScope(task, variableName, scope)) {
//            throw new ActivitiObjectNotFoundException(
//                    "Task '" + task.getId() + "' doesn't have a variable '" +
//                    variableName + "' in scope " + scope.name().toLowerCase(Locale.getDefault()),
//                    VariableInstanceEntity.class);
//        }
//
//        RuntimeService runtimeService = BPMNOSGIService.getRumtimeService();
//
//        TaskService taskService = BPMNOSGIService.getTaskService();
//
//        if (scope == RestVariable.RestVariableScope.LOCAL) {
//            taskService.removeVariableLocal(task.getId(), variableName);
//        } else {
//            // Safe to use executionId, as the hasVariableOnScope whould have stopped a
//            // global-var update on standalone task
//            runtimeService.removeVariable(task.getExecutionId(), variableName);
//        }
//
//        return Response.ok().status(Response.Status.NO_CONTENT).build();
//    }

    @DELETE
    @Path("/{task-id}/variables")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    public Response deleteAllLocalTaskVariables(@PathParam("task-id") String taskId) {
        TaskService taskService = BPMNOSGIService.getTaskService();

        Task task = getTaskFromRequest(taskId);
        Collection<String> currentVariables = taskService.getVariablesLocal(task.getId()).keySet();
        taskService.removeVariablesLocal(task.getId(), currentVariables);

        return Response.ok().status(Response.Status.NO_CONTENT).build();
    }

    @GET
    @Path("/{task-id}/identity-links")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    public Response getIdentityLinks(@PathParam("task-id") String taskId,
                                     @Context Request request) {
        Task task = getTaskFromRequest(taskId);
        TaskService taskService = BPMNOSGIService.getTaskService();

        List<RestIdentityLink> restIdentityLinks = new RestResponseFactory()
                .createRestIdentityLinks(taskService.getIdentityLinksForTask(task.getId()),
                                         request.getUri());
        RestIdentityLinkCollection restIdentityLinkCollection = new RestIdentityLinkCollection();
        restIdentityLinkCollection.setRestIdentityLinks(restIdentityLinks);
        return Response.ok().entity(restIdentityLinkCollection).build();
    }

    @GET
    @Path("/{task-id}/identity-links/{family}")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    public Response getIdentityLinksForFamily(@PathParam("task-id") String taskId,
                                              @PathParam("family") String family,
                                              @Context Request request) {

        Task task = getTaskFromRequest(taskId);

        TaskService taskService = BPMNOSGIService.getTaskService();

        if (family == null || (!RestUrls.SEGMENT_IDENTITYLINKS_FAMILY_GROUPS.equals(family) &&
                               !RestUrls.SEGMENT_IDENTITYLINKS_FAMILY_USERS.equals(family))) {
            throw new ActivitiIllegalArgumentException(
                    "Identity link family should be 'users' or 'groups'.");
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
                results.add(
                        new RestResponseFactory().createRestIdentityLink(link, request.getUri()));
            }
        }
        RestIdentityLinkCollection restIdentityLinkCollection = new RestIdentityLinkCollection();
        restIdentityLinkCollection.setRestIdentityLinks(results);
        return Response.ok().entity(restIdentityLinkCollection).build();
    }

    //TODO:
//    @GET
//    @Path("/{task-id}/identity-links/{family}/{identity-id}/{type}")
//    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
//    public Response getIdentityLink(@PathParam("task-id") String taskId,
//                                    @PathParam("family") String family,
//                                    @PathParam("identity-id") String identityId,
//                                    @PathParam("type") String type,
//                                    @Context HttpServletRequest httpServletRequest,
//                                    @Context Request request) {
//
//        Task task = getTaskFromRequest(taskId);
//        validateIdentityLinkArguments(family, identityId, type);
//
//        IdentityLink link = getIdentityLink(family, identityId, type, task.getId());
//
//        return Response.ok().entity(new RestResponseFactory()
//                                            .createRestIdentityLink(link, request.getUri())).build();
//    }

    @DELETE
    @Path("/{task-id}/identity-links/{family}/{identity-id}/{type}")
    public Response deleteIdentityLink(@PathParam("task-id") String taskId,
                                       @PathParam("family") String family,
                                       @PathParam("identity-id") String identityId,
                                       @PathParam("type") String type) {

        Task task = getTaskFromRequest(taskId);

        validateIdentityLinkArguments(family, identityId, type);

        // Check if identitylink to delete exists
        getIdentityLink(family, identityId, type, task.getId());

        TaskService taskService = BPMNOSGIService.getTaskService();

        if (RestUrls.SEGMENT_IDENTITYLINKS_FAMILY_USERS.equals(family)) {
            taskService.deleteUserIdentityLink(task.getId(), identityId, type);
        } else {
            taskService.deleteGroupIdentityLink(task.getId(), identityId, type);
        }

        return Response.ok().status(Response.Status.NO_CONTENT).build();
    }

    @POST
    @Path("/{task-id}/identity-links")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    public Response createIdentityLink(@PathParam("task-id") String taskId,
                                       RestIdentityLink identityLink,
                                       @Context Request request) {

        Task task = getTaskFromRequest(taskId);

        if (identityLink.getGroup() == null && identityLink.getUser() == null) {
            throw new ActivitiIllegalArgumentException(
                    "A group or a user is required to create an identity link.");
        }

        if (identityLink.getGroup() != null && identityLink.getUser() != null) {
            throw new ActivitiIllegalArgumentException(
                    "Only one of user or group can be used to create an identity link.");
        }

        if (identityLink.getType() == null) {
            throw new ActivitiIllegalArgumentException("The identity link type is required.");
        }
        TaskService taskService = BPMNOSGIService.getTaskService();

        if (identityLink.getGroup() != null) {
            taskService.addGroupIdentityLink(task.getId(), identityLink.getGroup(),
                                             identityLink.getType());
        } else {
            taskService.addUserIdentityLink(task.getId(), identityLink.getUser(),
                                            identityLink.getType());
        }

        RestIdentityLink restIdentityLink = new RestResponseFactory()
                .createRestIdentityLink(identityLink.getType(), identityLink.getUser(),
                                        identityLink.getGroup(), task.getId(), null, null,
                                        request.getUri());

        return Response.ok().status(Response.Status.CREATED).entity(restIdentityLink).build();
    }

    /* todo
        @POST
        @Path("/{taskId}/attachments")
        @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
        @Consumes(MediaType.MULTIPART_FORM_DATA)
        public Response createAttachmentForBinary(@PathParam("taskId") String taskId, MultipartBody
         multipartBody,
                                                  @Context HttpServletRequest httpServletRequest) {

            boolean debugEnabled = log.isDebugEnabled();
            List<org.apache.cxf.jaxrs.ext.multipart.Attachment> attachments = multipartBody.
            getAllAttachments();

            int attachmentSize = attachments.size();

            if (attachmentSize <= 0) {
                throw new ActivitiIllegalArgumentException("No Attachments found with the
                request body");
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

                    Map<String, String> contentDispositionHeaderValueMap = Utils.
                    processContentDispositionHeader
                            (contentDispositionHeaderValue);
                    String dispositionName = contentDispositionHeaderValueMap.get("name");
                    DataHandler dataHandler = attachment.getDataHandler();

                    OutputStream outputStream = null;

                    if ("name".equals(dispositionName)) {
                        try {
                            outputStream = Utils.getAttachmentStream(dataHandler.getInputStream());
                        } catch (IOException e) {
                            throw new ActivitiIllegalArgumentException("Attachment
                            Name Reading error occured", e);
                        }

                        if (outputStream != null) {
                            String fileName = outputStream.toString();
                            attachmentDataHolder.setName(fileName);
                        }


                    } else if ("type".equals(dispositionName)) {
                        try {
                            outputStream = Utils.getAttachmentStream(dataHandler.getInputStream());
                        } catch (IOException e) {
                            throw new ActivitiIllegalArgumentException("Attachment
                            Type Reading error occured", e);
                        }

                        if (outputStream != null) {
                            String typeName = outputStream.toString();
                            attachmentDataHolder.setType(typeName);
                        }


                    } else if ("description".equals(dispositionName)) {
                        try {
                            outputStream = Utils.getAttachmentStream(dataHandler.getInputStream());
                        } catch (IOException e) {
                            throw new ActivitiIllegalArgumentException("Attachment
                            Description Reading error occured", e);
                        }

                        if (outputStream != null) {
                            String description = outputStream.toString();
                            attachmentDataHolder.setDescription(description);
                        }
                    }

                    if (contentType != null) {
                        if ("file".equals(dispositionName)) {

                            InputStream inputStream = null;
                            try {
                                inputStream = dataHandler.getInputStream();
                            } catch (IOException e) {
                                throw new ActivitiIllegalArgumentException("Error
                                 Occured During processing empty body.",
                                        e);
                            }

                            if (inputStream != null) {
                                attachmentDataHolder.setContentType(contentType);
                                byte[] attachmentArray = new byte[0];
                                try {
                                    attachmentArray = IOUtils.toByteArray(inputStream);
                                } catch (IOException e) {
                                    throw new ActivitiIllegalArgumentException("
                                    Processing Attachment Body Failed.", e);
                                }
                                attachmentDataHolder.setAttachmentArray(attachmentArray);
                            }
                        }
                    }
                }
            }


            attachmentDataHolder.printDebug();

            if (attachmentDataHolder.getName() == null) {
                throw new ActivitiIllegalArgumentException("Attachment name is required.");
            }

            if (attachmentDataHolder.getAttachmentArray() == null) {
                throw new ActivitiIllegalArgumentException("Empty attachment body
                 was found in request body after " +
                        "decoding the request" +
                        ".");
            }

            TaskService taskService = BPMNOSGIService.getTaskService();
            Task task = getTaskFromRequest(taskId);
            Response.ResponseBuilder responseBuilder = Response.ok();

            AttachmentResponse result = null;
            try {

                InputStream inputStream = new ByteArrayInputStream
                (attachmentDataHolder.getAttachmentArray());
                Attachment createdAttachment = taskService.createAttachment
                (attachmentDataHolder.getContentType(), task.getId(), task
                        .getProcessInstanceId(), attachmentDataHolder.getName(),
                        attachmentDataHolder
                        .getDescription(), inputStream);

                responseBuilder.status(Response.Status.CREATED);
                result = new RestResponseFactory().createAttachmentResponse(createdAttachment,
                uriInfo.getBaseUri()
                        .toString
                                ());
            } catch (Exception e) {
                throw new ActivitiException("Error creating attachment response", e);
            }

            return responseBuilder.status(Response.Status.CREATED).entity(result).build();
        }
    *///todo:
//    @POST
//    @Path("/{task-id}/attachments")
//    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
//    @Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
//    public Response createAttachmentForNonBinary(@PathParam("task-id") String taskId,
//                                                 @Context HttpServletRequest httpServletRequest,
//                                                 AttachmentRequest attachmentRequest,
//                                                 @Context Request request) {
//
//        AttachmentResponse result = null;
//        Task task = getTaskFromRequest(taskId);
//        Response.ResponseBuilder responseBuilder = Response.ok();
//        if (attachmentRequest == null) {
//            throw new ActivitiIllegalArgumentException(
//                    "AttachmentRequest properties not found in request");
//        }
//
//        result = createSimpleAttachment(attachmentRequest, task, request);
//        return responseBuilder.status(Response.Status.CREATED).entity(result).build();
//    }

    @GET
    @Path("/{task-id}/attachments")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    public Response getAttachments(@PathParam("task-id") String taskId,
                                   @Context Request request) {
        List<AttachmentResponse> result = new ArrayList<AttachmentResponse>();
        HistoricTaskInstance task = getHistoricTaskFromRequest(taskId);

        TaskService taskService = BPMNOSGIService.getTaskService();

        RestResponseFactory restResponseFactory = new RestResponseFactory();

        for (Attachment attachment : taskService
                .getProcessInstanceAttachments(task.getProcessInstanceId())) {
            result.add(restResponseFactory.createAttachmentResponse(attachment, request.getUri()));
        }

        AttachmentResponseCollection attachmentResponseCollection =
                new AttachmentResponseCollection();
        attachmentResponseCollection.setAttachmentResponseList(result);

        return Response.ok().entity(attachmentResponseCollection).build();
    }

    @GET
    @Path("/{task-id}/attachments/{attachment-id}")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    public Response getAttachment(@PathParam("task-id") String taskId,
                                  @PathParam("attachment-id") String attachmentId,
                                  @Context Request request) {

        HistoricTaskInstance task = getHistoricTaskFromRequest(taskId);
        TaskService taskService = BPMNOSGIService.getTaskService();

        Attachment attachment = taskService.getAttachment(attachmentId);
        if (attachment == null) {
            throw new ActivitiObjectNotFoundException(
                    "Task '" + task.getId() + "' doesn't have an attachment with id '" +
                    attachmentId + "'.", Comment.class);
        }

        return Response.ok().entity(new RestResponseFactory()
                                            .createAttachmentResponse(attachment, request.getUri()))
                       .build();
    }

    @DELETE
    @Path("/{task-id}/attachments/{attachment-id}")
    public Response deleteAttachment(@PathParam("task-id") String taskId,
                                     @PathParam("attachment-id") String attachmentId) {

        Task task = getTaskFromRequest(taskId);
        TaskService taskService = BPMNOSGIService.getTaskService();
        Attachment attachment = taskService.getAttachment(attachmentId);
        if (attachment == null) {
            throw new ActivitiObjectNotFoundException(
                    "Task '" + task.getId() + "' doesn't have an attachment with id '" +
                    attachmentId + "'.", Comment.class);
        }

        taskService.deleteAttachment(attachmentId);

        return Response.ok().status(Response.Status.NO_CONTENT).build();
    }

    @GET
    @Path("/{task-id}/attachments/{attachment-id}/content")
    public Response getAttachmentContent(@PathParam("task-id") String taskId,
                                         @PathParam("attachment-id") String attachmentId) {

        TaskService taskService = BPMNOSGIService.getTaskService();
        HistoricTaskInstance task = getHistoricTaskFromRequest(taskId);
        Attachment attachment = taskService.getAttachment(attachmentId);

        if (attachment == null) {
            throw new ActivitiObjectNotFoundException(
                    "Task '" + task.getId() + "' doesn't have an attachment with id '" +
                    attachmentId + "'.", Attachment.class);
        }

        InputStream attachmentStream = taskService.getAttachmentContent(attachmentId);
        if (attachmentStream == null) {
            throw new ActivitiObjectNotFoundException("Attachment with id '" + attachmentId +
                                                      "' doesn't have content associated with it.",
                                                      Attachment.class);
        }

        Response.ResponseBuilder responseBuilder = Response.ok();

        String type = attachment.getType();
        MediaType mediaType = MediaType.valueOf(type);
        if (mediaType != null) {
            responseBuilder.type(mediaType);
        } else {
            responseBuilder.type("application/octet-stream");
        }

        byte[] attachmentArray;
        try {
            attachmentArray = IOUtils.toByteArray(attachmentStream);
        } catch (IOException e) {
            throw new ActivitiException("Error creating attachment data", e);
        }
        String dispositionValue = "inline; filename=\"" + attachment.getName() + "\"";
        responseBuilder.header("Content-Disposition", dispositionValue);
        return responseBuilder.entity(attachmentArray).build();
    }

    @GET
    @Path("/{task-id}/comments")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    public Response getComments(@PathParam("task-id") String taskId, @Context Request request) {
        HistoricTaskInstance task = getHistoricTaskFromRequest(taskId);
        TaskService taskService = BPMNOSGIService.getTaskService();
        List<CommentResponse> commentResponseList = new RestResponseFactory()
                .createRestCommentList(taskService.getTaskComments(task.getId()), request.getUri());
        CommentResponseCollection commentResponseCollection = new CommentResponseCollection();
        commentResponseCollection.setCommentResponseList(commentResponseList);

        return Response.ok().entity(commentResponseCollection).build();
    }

    @POST
    @Path("/{task-id}/comments")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    public Response createComment(@PathParam("task-id") String taskId, CommentRequest comment,
                                  @Context Request request) {

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
        Comment createdComment =
                taskService.addComment(task.getId(), processInstanceId, comment.getMessage());

        CommentResponse commentResponse =
                new RestResponseFactory().createRestComment(createdComment, request.getUri());
        return Response.ok().status(Response.Status.CREATED).entity(commentResponse).build();
    }

    @GET
    @Path("/{task-id}/comments/{comment-id}")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    public Response getComment(@PathParam("task-id") String taskId,
                               @PathParam("comment-id") String commentId,
                               @Context Request request) {

        HistoricTaskInstance task = getHistoricTaskFromRequest(taskId);
        TaskService taskService = BPMNOSGIService.getTaskService();
        Comment comment = taskService.getComment(commentId);
        if (comment == null || !task.getId().equals(comment.getTaskId())) {
            throw new ActivitiObjectNotFoundException(
                    "Task '" + task.getId() + "' doesn't have a comment with id '" + commentId +
                    "'.", Comment.class);
        }

        return Response.ok().entity(new RestResponseFactory()
                                            .createRestComment(comment, request.getUri())).build();
    }

    @DELETE
    @Path("/{task-id}/comments/{comment-id}")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    public Response deleteComment(@PathParam("task-id") String taskId,
                                  @PathParam("comment-id") String commentId) {

        // Check if task exists
        Task task = getTaskFromRequest(taskId);
        TaskService taskService = BPMNOSGIService.getTaskService();

        Comment comment = taskService.getComment(commentId);
        if (comment == null || comment.getTaskId() == null ||
            !comment.getTaskId().equals(task.getId())) {
            throw new ActivitiObjectNotFoundException(
                    "Task '" + task.getId() + "' doesn't have a comment with id '" + commentId +
                    "'.", Comment.class);
        }

        taskService.deleteComment(commentId);
        return Response.ok().status(Response.Status.NO_CONTENT).build();
    }

    @GET
    @Path("/{task-id}/events")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    public Response getEvents(@PathParam("task-id") String taskId, @Context Request request) {
        HistoricTaskInstance task = getHistoricTaskFromRequest(taskId);
        TaskService taskService = BPMNOSGIService.getTaskService();
        List<EventResponse> eventResponseList = new RestResponseFactory()
                .createEventResponseList(taskService.getTaskEvents(task.getId()), request.getUri());

        EventResponseCollection eventResponseCollection = new EventResponseCollection();
        eventResponseCollection.setEventResponseList(eventResponseList);

        return Response.ok().entity(eventResponseCollection).build();
    }

    @GET
    @Path("/{task-id}/events/{event-id}")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    public Response getEvent(@PathParam("task-id") String taskId,
                             @PathParam("event-id") String eventId, @Context Request request) {

        HistoricTaskInstance task = getHistoricTaskFromRequest(taskId);
        TaskService taskService = BPMNOSGIService.getTaskService();

        Event event = taskService.getEvent(eventId);
        if (event == null || !task.getId().equals(event.getTaskId())) {
            throw new ActivitiObjectNotFoundException(
                    "Task '" + task.getId() + "' doesn't have an event with id '" + eventId + "'.",
                    Event.class);
        }

        EventResponse eventResponse =
                new RestResponseFactory().createEventResponse(event, request.getUri());
        return Response.ok().entity(eventResponse).build();
    }

    @DELETE
    @Path("/{task-id}/events/{event-id}")
    public Response deleteEvent(@PathParam("task-id") String taskId,
                                @PathParam("event-id") String eventId) {

        // Check if task exists
        Task task = getTaskFromRequest(taskId);
        TaskService taskService = BPMNOSGIService.getTaskService();
        Event event = taskService.getEvent(eventId);
        if (event == null || event.getTaskId() == null || !event.getTaskId().equals(task.getId())) {
            throw new ActivitiObjectNotFoundException(
                    "Task '" + task.getId() + "' doesn't have an event with id '" + event + "'.",
                    Event.class);
        }

        taskService.deleteComment(eventId);
        return Response.ok().status(Response.Status.NO_CONTENT).build();
    }

    protected HistoricTaskInstance getHistoricTaskFromRequest(String taskId) {
        HistoryService historyService = BPMNOSGIService.getHistoryService();

        HistoricTaskInstance task =
                historyService.createHistoricTaskInstanceQuery().taskId(taskId).singleResult();
        if (task == null) {
            throw new ActivitiObjectNotFoundException(
                    "Could not find a task with id '" + taskId + "'.", Task.class);
        }
        return task;
    }

    protected AttachmentResponse createSimpleAttachment(AttachmentRequest attachmentRequest,
                                                        Task task, @Context Request request) {

        if (attachmentRequest.getName() == null) {
            throw new ActivitiIllegalArgumentException("Attachment name is required.");
        }

        TaskService taskService = BPMNOSGIService.getTaskService();

        Attachment createdAttachment = taskService
                .createAttachment(attachmentRequest.getType(), task.getId(),
                                  task.getProcessInstanceId(), attachmentRequest.getName(),
                                  attachmentRequest.getDescription(),
                                  attachmentRequest.getExternalUrl());

        return new RestResponseFactory()
                .createAttachmentResponse(createdAttachment, request.getUri());
    }
    //TODO:
/*    protected AttachmentResponse createBinaryAttachment(HttpServletRequest httpServletRequest,
Task task, Response.ResponseBuilder responseBuilder) throws
            IOException {

        String name = uriInfo.getQueryParameters().getFirst("name");
        String description = uriInfo.getQueryParameters().getFirst("description");
        String type = uriInfo.getQueryParameters().getFirst("type");


        byte[] byteArray = Utils.processMultiPartFile(httpServletRequest, "Attachment content");
        if (byteArray == null) {
            throw new ActivitiIllegalArgumentException("Empty attachment body was found
            in request body after " +
                    "decoding the request" +
                    ".");
        }

        if (name == null) {
            throw new ActivitiIllegalArgumentException("Attachment name is required.");
        }


        TaskService taskService = BPMNOSGIService.getTaskService();

        try {
            InputStream inputStream = new ByteArrayInputStream(byteArray);
            Attachment createdAttachment = taskService.createAttachment(type, task.getId(),
            task.getProcessInstanceId(), name,
                    description, inputStream);

            responseBuilder.status(Response.Status.CREATED);
            return new RestResponseFactory().createAttachmentResponse(createdAttachment,
             uriInfo.getBaseUri().toString());

        } catch (Exception e) {
            throw new ActivitiException("Error creating attachment response", e);
        }
    }*/

    protected IdentityLink getIdentityLink(String family, String identityId, String type,
                                           String taskId) {
        boolean isUser = family.equals(RestUrls.SEGMENT_IDENTITYLINKS_FAMILY_USERS);

        TaskService taskService = BPMNOSGIService.getTaskService();
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
        throw new ActivitiObjectNotFoundException("Could not find the requested identity link.",
                                                  IdentityLink.class);
    }

    protected void validateIdentityLinkArguments(String family, String identityId, String type) {
        if (family == null || (!RestUrls.SEGMENT_IDENTITYLINKS_FAMILY_GROUPS.equals(family) &&
                               !RestUrls.SEGMENT_IDENTITYLINKS_FAMILY_USERS.equals(family))) {
            throw new ActivitiIllegalArgumentException(
                    "Identity link family should be 'users' or 'groups'.");
        }
        if (identityId == null) {
            throw new ActivitiIllegalArgumentException("IdentityId is required.");
        }
        if (type == null) {
            throw new ActivitiIllegalArgumentException("Type is required.");
        }
    }

    protected RestVariable setSimpleVariable(RestVariable restVariable, Task task, boolean isNew,
                                             String baseContext) {
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

        return restResponseFactory
                .createRestVariable(restVariable.getName(), actualVariableValue, scope,
                                    task.getId(), RestResponseFactory.VARIABLE_TASK, false,
                                    baseContext);
    }

    protected void completeTask(Task task, TaskActionRequest actionRequest,
                                TaskService taskService) {

        if (actionRequest.getVariables() != null) {
            Map<String, Object> variablesToSet = new HashMap<String, Object>();

            RestResponseFactory restResponseFactory = new RestResponseFactory();
            for (RestVariable var : actionRequest.getVariables()) {
                if (var.getName() == null) {
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
        // In case the task is already claimed, a ActivitiTaskAlreadyClaimedException is
        // thrown and converted to
        // a CONFLICT response by the ExceptionHandlerAdvice
        taskService.claim(task.getId(), actionRequest.getAssignee());
    }

    protected void delegateTask(Task task, TaskActionRequest actionRequest,
                                TaskService taskService) {
        if (actionRequest.getAssignee() == null) {
            throw new ActivitiIllegalArgumentException(
                    "An assignee is required when delegating a task.");
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
        if (taskRequest.isDueDateSet()) {
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
