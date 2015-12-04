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

package org.wso2.carbon.bpmn.rest.service.history;

import org.activiti.engine.*;
import org.activiti.engine.history.HistoricIdentityLink;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricProcessInstanceQuery;
import org.activiti.engine.impl.HistoricProcessInstanceQueryProperty;
import org.activiti.engine.impl.persistence.entity.HistoricProcessInstanceEntity;
import org.activiti.engine.impl.persistence.entity.VariableInstanceEntity;
import org.activiti.engine.query.QueryProperty;
import org.activiti.engine.task.Comment;
import org.wso2.carbon.bpmn.rest.model.common.DataResponse;
import org.wso2.carbon.bpmn.rest.common.RequestUtil;
import org.wso2.carbon.bpmn.rest.common.RestResponseFactory;
import org.wso2.carbon.bpmn.rest.common.utils.BPMNOSGIService;
import org.wso2.carbon.bpmn.rest.engine.variable.QueryVariable;
import org.wso2.carbon.bpmn.rest.engine.variable.RestVariable;
import org.wso2.carbon.bpmn.rest.model.common.HistoricProcessInstanceQueryRequest;
import org.wso2.carbon.bpmn.rest.model.history.HistoricIdentityLinkResponse;
import org.wso2.carbon.bpmn.rest.model.history.HistoricIdentityLinkResponseCollection;
import org.wso2.carbon.bpmn.rest.model.history.HistoricProcessInstancePaginateList;
import org.wso2.carbon.bpmn.rest.model.history.HistoricProcessInstanceResponse;
import org.wso2.carbon.bpmn.rest.model.runtime.CommentResponse;
import org.wso2.carbon.bpmn.rest.model.runtime.CommentResponseCollection;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.*;

@Path("/historic-process-instances")
public class HistoricProcessInstanceService {

    protected static final List<String> allPropertiesList  = new ArrayList<>();
    private static Map<String, QueryProperty> allowedSortProperties = new HashMap<String, QueryProperty>();



    @Context
    UriInfo uriInfo;

    static {
        allPropertiesList.add("processInstanceId");
        allPropertiesList.add("processDefinitionKey");
        allPropertiesList.add("processDefinitionId");
        allPropertiesList.add("businessKey");
        allPropertiesList.add("involvedUser");
        allPropertiesList.add("finished");
        allPropertiesList.add("superProcessInstanceId");
        allPropertiesList.add("excludeSubprocesses");
        allPropertiesList.add("finishedAfter");
        allPropertiesList.add("finishedBefore");
        allPropertiesList.add("startedAfter");
        allPropertiesList.add("startedBefore");
        allPropertiesList.add("startedBy");
        allPropertiesList.add("includeProcessVariables");
        allPropertiesList.add("tenantId");
        allPropertiesList.add("tenantIdLike");
        allPropertiesList.add("withoutTenantId");
        allPropertiesList.add("start");
        allPropertiesList.add("size");
        allPropertiesList.add("order");
        allPropertiesList.add("sort");

    }

    static {
        allowedSortProperties.put("processInstanceId", HistoricProcessInstanceQueryProperty.PROCESS_INSTANCE_ID_);
        allowedSortProperties.put("processDefinitionId", HistoricProcessInstanceQueryProperty.PROCESS_DEFINITION_ID);
        allowedSortProperties.put("businessKey", HistoricProcessInstanceQueryProperty.BUSINESS_KEY);
        allowedSortProperties.put("startTime", HistoricProcessInstanceQueryProperty.START_TIME);
        allowedSortProperties.put("endTime", HistoricProcessInstanceQueryProperty.END_TIME);
        allowedSortProperties.put("duration", HistoricProcessInstanceQueryProperty.DURATION);
        allowedSortProperties.put("tenantId", HistoricProcessInstanceQueryProperty.TENANT_ID);
    }

    @GET
    @Path("/")
    @Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
    @Consumes({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
    public Response getHistoricProcessInstances() {

        Map<String, String> allRequestParams = new HashMap<>();

        for (String property:allPropertiesList){
            String value= uriInfo.getQueryParameters().getFirst(property);

            if(value != null){
                allRequestParams.put(property, value);
            }
        }

        // Populate query based on request
        HistoricProcessInstanceQueryRequest queryRequest = new HistoricProcessInstanceQueryRequest();

        if (allRequestParams.get("processInstanceId") != null) {
            queryRequest.setProcessInstanceId(allRequestParams.get("processInstanceId"));
        }

        if (allRequestParams.get("processDefinitionKey") != null) {
            queryRequest.setProcessDefinitionKey(allRequestParams.get("processDefinitionKey"));
        }

        if (allRequestParams.get("processDefinitionId") != null) {
            queryRequest.setProcessDefinitionId(allRequestParams.get("processDefinitionId"));
        }

        if (allRequestParams.get("businessKey") != null) {
            queryRequest.setProcessBusinessKey(allRequestParams.get("businessKey"));
        }

        if (allRequestParams.get("involvedUser") != null) {
            queryRequest.setInvolvedUser(allRequestParams.get("involvedUser"));
        }

        if (allRequestParams.get("finished") != null) {
            queryRequest.setFinished(Boolean.valueOf(allRequestParams.get("finished")));
        }

        if (allRequestParams.get("superProcessInstanceId") != null) {
            queryRequest.setSuperProcessInstanceId(allRequestParams.get("superProcessInstanceId"));
        }

        if (allRequestParams.get("excludeSubprocesses") != null) {
            queryRequest.setExcludeSubprocesses(Boolean.valueOf(allRequestParams.get("excludeSubprocesses")));
        }

        if (allRequestParams.get("finishedAfter") != null) {
            queryRequest.setFinishedAfter(RequestUtil.getDate(allRequestParams, "finishedAfter"));
        }

        if (allRequestParams.get("finishedBefore") != null) {
            queryRequest.setFinishedBefore(RequestUtil.getDate(allRequestParams, "finishedBefore"));
        }

        if (allRequestParams.get("startedAfter") != null) {
            queryRequest.setStartedAfter(RequestUtil.getDate(allRequestParams, "startedAfter"));
        }

        if (allRequestParams.get("startedBefore") != null) {
            queryRequest.setStartedBefore(RequestUtil.getDate(allRequestParams, "startedBefore"));
        }

        if (allRequestParams.get("startedBy") != null) {
            queryRequest.setStartedBy(allRequestParams.get("startedBy"));
        }

        if (allRequestParams.get("includeProcessVariables") != null) {
            queryRequest.setIncludeProcessVariables(Boolean.valueOf(allRequestParams.get("includeProcessVariables")));
        }

        if (allRequestParams.get("tenantId") != null) {
            queryRequest.setTenantId(allRequestParams.get("tenantId"));
        }

        if (allRequestParams.get("tenantIdLike") != null) {
            queryRequest.setTenantIdLike(allRequestParams.get("tenantIdLike"));
        }

        if (allRequestParams.get("withoutTenantId") != null) {
            queryRequest.setWithoutTenantId(Boolean.valueOf(allRequestParams.get("withoutTenantId")));
        }

        return Response.ok().entity(getQueryResponse(queryRequest, allRequestParams)).build();
    }

    @GET
    @Path("/{processInstanceId}")
    @Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
    @Consumes({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
    public Response getProcessInstance(@PathParam("processInstanceId") String processInstanceId) {
        HistoricProcessInstanceResponse historicProcessInstanceResponse = new RestResponseFactory()
                .createHistoricProcessInstanceResponse(getHistoricProcessInstanceFromRequest(processInstanceId),
                        uriInfo.getBaseUri().toString());
        return Response.ok().entity(historicProcessInstanceResponse).build();
    }

    @DELETE
    @Path("/{processInstanceId}")
    public Response deleteProcessInstance(@PathParam("processInstanceId") String processInstanceId) {
        HistoryService historyService = BPMNOSGIService.getHistoryService();
        historyService.deleteHistoricProcessInstance(processInstanceId);
        return Response.ok().status(Response.Status.NO_CONTENT).build();
    }

    @GET
    @Path("/{processInstanceId}/identitylinks")
    @Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
    @Consumes({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
    public Response getProcessIdentityLinks(@PathParam("processInstanceId")  String processInstanceId) {

        HistoryService historyService = BPMNOSGIService.getHistoryService();
        List<HistoricIdentityLink> identityLinks = historyService.getHistoricIdentityLinksForProcessInstance(processInstanceId);
        if (identityLinks != null) {
            List<HistoricIdentityLinkResponse> historicIdentityLinkResponses = new RestResponseFactory()
                    .createHistoricIdentityLinkResponseList(identityLinks, uriInfo.getBaseUri
                    ().toString());
            HistoricIdentityLinkResponseCollection historicIdentityLinkResponseCollection = new
                    HistoricIdentityLinkResponseCollection();
            historicIdentityLinkResponseCollection.setHistoricIdentityLinkResponses(historicIdentityLinkResponses);
            return Response.ok().entity(historicIdentityLinkResponseCollection).build();
        }

        return Response.ok().build();
    }


    @GET
    @Path("/{processInstanceId}/variables/{variableName}/data")
    public Response getVariableData(@PathParam("processInstanceId") String processInstanceId,
                                    @PathParam("variableName") String variableName) {

        try {

            Response.ResponseBuilder responseBuilder = Response.ok();
            byte[] result = null;
            RestVariable variable = getVariableFromRequest(true, processInstanceId, variableName);
            if (RestResponseFactory.BYTE_ARRAY_VARIABLE_TYPE.equals(variable.getType())) {
                result = (byte[]) variable.getValue();
                responseBuilder.type("application/octet-stream");

            } else if (RestResponseFactory.SERIALIZABLE_VARIABLE_TYPE.equals(variable.getType())) {
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
            throw new ActivitiException("Unexpected exception getting variable data", ioe);
        }
    }

    @GET
    @Path("/{processInstanceId}/comments")
    @Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
    public Response getComments(@PathParam("processInstanceId") String processInstanceId) {
        TaskService taskService = BPMNOSGIService.getTaskService();
        HistoricProcessInstance instance = getHistoricProcessInstanceFromRequest(processInstanceId);
        List<CommentResponse> commentResponseList = new RestResponseFactory().createRestCommentList(taskService
                .getProcessInstanceComments(instance.getId()), uriInfo.getBaseUri().toString());
        CommentResponseCollection commentResponseCollection = new CommentResponseCollection();
        commentResponseCollection.setCommentResponseList(commentResponseList);
        return Response.ok().entity(commentResponseCollection).build();
    }

    @POST
    @Path("/{processInstanceId}/comments")
    @Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
    @Consumes({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
    public Response createComment(@PathParam("processInstanceId") String processInstanceId, CommentResponse comment) {

        HistoricProcessInstance instance = getHistoricProcessInstanceFromRequest(processInstanceId);

        if (comment.getMessage() == null) {
            throw new ActivitiIllegalArgumentException("Comment text is required.");
        }

        TaskService taskService = BPMNOSGIService.getTaskService();
        Comment createdComment = taskService.addComment(null, instance.getId(), comment.getMessage());

        CommentResponse commentResponse = new RestResponseFactory().createRestComment(createdComment, uriInfo
                .getBaseUri().toString());

        return Response.ok().status(Response.Status.CREATED).entity(commentResponse).build();
    }

    @GET
    @Path("/{processInstanceId}/comments/{commentId}")
    @Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
    public Response getComment(@PathParam("processInstanceId") String processInstanceId,
                                      @PathParam("commentId") String commentId) {

        HistoricProcessInstance instance = getHistoricProcessInstanceFromRequest(processInstanceId);
        TaskService taskService = BPMNOSGIService.getTaskService();

        Comment comment = taskService.getComment(commentId);
        if (comment == null || comment.getProcessInstanceId() == null || !comment.getProcessInstanceId().equals(instance.getId())) {
            throw new ActivitiObjectNotFoundException("Process instance '" + instance.getId() + "' doesn't have a comment with id '" + commentId + "'.", Comment.class);
        }
        CommentResponse commentResponse = new RestResponseFactory().createRestComment(comment, uriInfo.getBaseUri().toString());
        return Response.ok().entity(commentResponse).build();
    }

    @DELETE
    @Path("/{processInstanceId}/comments/{commentId}")
    public Response deleteComment(@PathParam("processInstanceId") String processInstanceId,
                              @PathParam("commentId") String commentId) {

        TaskService taskService = BPMNOSGIService.getTaskService();
        HistoricProcessInstance instance = getHistoricProcessInstanceFromRequest(processInstanceId);

        Comment comment = taskService.getComment(commentId);
        if (comment == null || comment.getProcessInstanceId() == null || !comment.getProcessInstanceId().equals(instance.getId())) {
            throw new ActivitiObjectNotFoundException("Process instance '" + instance.getId() + "' doesn't have a comment with id '" + commentId + "'.", Comment.class);
        }

        taskService.deleteComment(commentId);
        return Response.ok().status(Response.Status.NO_CONTENT).build();
    }

    public RestVariable getVariableFromRequest(boolean includeBinary, String processInstanceId, String variableName) {

        HistoryService historyService = BPMNOSGIService.getHistoryService();
        HistoricProcessInstance processObject = historyService.createHistoricProcessInstanceQuery()
                .processInstanceId(processInstanceId).includeProcessVariables().singleResult();

        if (processObject == null) {
            throw new ActivitiObjectNotFoundException("Historic process instance '" + processInstanceId + "' couldn't be found.", HistoricProcessInstanceEntity.class);
        }

        Object value = processObject.getProcessVariables().get(variableName);

        if (value == null) {
            throw new ActivitiObjectNotFoundException("Historic process instance '" + processInstanceId + "' variable value for " + variableName + " couldn't be found.", VariableInstanceEntity.class);
        } else {
            return new RestResponseFactory().createRestVariable(variableName, value, null, processInstanceId,
                    RestResponseFactory.VARIABLE_HISTORY_PROCESS, includeBinary, uriInfo.getBaseUri().toString());
        }
    }


    protected DataResponse getQueryResponse(HistoricProcessInstanceQueryRequest queryRequest, Map<String, String>
            allRequestParams) {

        HistoryService historyService = BPMNOSGIService.getHistoryService();
        HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery();

        // Populate query based on request
        if (queryRequest.getProcessInstanceId() != null) {
            query.processInstanceId(queryRequest.getProcessInstanceId());
        }
        if (queryRequest.getProcessInstanceIds() != null && !queryRequest.getProcessInstanceIds().isEmpty()) {
            query.processInstanceIds(new HashSet<String>(queryRequest.getProcessInstanceIds()));
        }
        if (queryRequest.getProcessDefinitionKey() != null) {
            query.processDefinitionKey(queryRequest.getProcessDefinitionKey());
        }
        if (queryRequest.getProcessDefinitionId() != null) {
            query.processDefinitionId(queryRequest.getProcessDefinitionId());
        }
        if (queryRequest.getProcessBusinessKey() != null) {
            query.processInstanceBusinessKey(queryRequest.getProcessBusinessKey());
        }
        if (queryRequest.getInvolvedUser() != null) {
            query.involvedUser(queryRequest.getInvolvedUser());
        }
        if (queryRequest.getSuperProcessInstanceId() != null) {
            query.superProcessInstanceId(queryRequest.getSuperProcessInstanceId());
        }
        if (queryRequest.getExcludeSubprocesses() != null) {
            query.excludeSubprocesses(queryRequest.getExcludeSubprocesses());
        }
        if (queryRequest.getFinishedAfter() != null) {
            query.finishedAfter(queryRequest.getFinishedAfter());
        }
        if (queryRequest.getFinishedBefore() != null) {
            query.finishedBefore(queryRequest.getFinishedBefore());
        }
        if (queryRequest.getStartedAfter() != null) {
            query.startedAfter(queryRequest.getStartedAfter());
        }
        if (queryRequest.getStartedBefore() != null) {
            query.startedBefore(queryRequest.getStartedBefore());
        }
        if (queryRequest.getStartedBy() != null) {
            query.startedBy(queryRequest.getStartedBy());
        }
        if (queryRequest.getFinished() != null) {
            if (queryRequest.getFinished()) {
                query.finished();
            } else {
                query.unfinished();
            }
        }
        if (queryRequest.getIncludeProcessVariables() != null) {
            if (queryRequest.getIncludeProcessVariables()) {
                query.includeProcessVariables();
            }
        }
        if (queryRequest.getVariables() != null) {
            addVariables(query, queryRequest.getVariables());
        }

        if (queryRequest.getTenantId() != null) {
            query.processInstanceTenantId(queryRequest.getTenantId());
        }

        if (queryRequest.getTenantIdLike() != null) {
            query.processInstanceTenantIdLike(queryRequest.getTenantIdLike());
        }

        if (Boolean.TRUE.equals(queryRequest.getWithoutTenantId())) {
            query.processInstanceWithoutTenantId();
        }

        RestResponseFactory restResponseFactory = new RestResponseFactory();
        DataResponse dataResponse = new HistoricProcessInstancePaginateList(restResponseFactory, uriInfo).paginateList(
                allRequestParams, queryRequest, query, "processInstanceId", allowedSortProperties);

        return dataResponse;
    }

    protected void addVariables(HistoricProcessInstanceQuery processInstanceQuery, List<QueryVariable> variables) {
        for (QueryVariable variable : variables) {
            if (variable.getVariableOperation() == null) {
                throw new ActivitiIllegalArgumentException("Variable operation is missing for variable: " + variable.getName());
            }
            if (variable.getValue() == null) {
                throw new ActivitiIllegalArgumentException("Variable value is missing for variable: " + variable.getName());
            }

            boolean nameLess = variable.getName() == null;

            RestResponseFactory restResponseFactory = new RestResponseFactory();

            Object actualValue = restResponseFactory.getVariableValue(variable);

            // A value-only query is only possible using equals-operator
            if (nameLess && variable.getVariableOperation() != QueryVariable.QueryVariableOperation.EQUALS) {
                throw new ActivitiIllegalArgumentException("Value-only query (without a variable-name) is only supported when using 'equals' operation.");
            }

            switch (variable.getVariableOperation()) {

                case EQUALS:
                    if (nameLess) {
                        processInstanceQuery.variableValueEquals(actualValue);
                    } else {
                        processInstanceQuery.variableValueEquals(variable.getName(), actualValue);
                    }
                    break;

                case EQUALS_IGNORE_CASE:
                    if (actualValue instanceof String) {
                        processInstanceQuery.variableValueEqualsIgnoreCase(variable.getName(), (String) actualValue);
                    } else {
                        throw new ActivitiIllegalArgumentException("Only string variable values are supported when ignoring casing, but was: "
                                + actualValue.getClass().getName());
                    }
                    break;

                case NOT_EQUALS:
                    processInstanceQuery.variableValueNotEquals(variable.getName(), actualValue);
                    break;

                case LIKE:
                    if (actualValue instanceof String) {
                        processInstanceQuery.variableValueLike(variable.getName(), (String) actualValue);
                    } else {
                        throw new ActivitiIllegalArgumentException("Only string variable values are supported for like, but was: "
                                + actualValue.getClass().getName());
                    }
                    break;

                case GREATER_THAN:
                    processInstanceQuery.variableValueGreaterThan(variable.getName(), actualValue);
                    break;

                case GREATER_THAN_OR_EQUALS:
                    processInstanceQuery.variableValueGreaterThanOrEqual(variable.getName(), actualValue);
                    break;

                case LESS_THAN:
                    processInstanceQuery.variableValueLessThan(variable.getName(), actualValue);
                    break;

                case LESS_THAN_OR_EQUALS:
                    processInstanceQuery.variableValueLessThanOrEqual(variable.getName(), actualValue);
                    break;

                default:
                    throw new ActivitiIllegalArgumentException("Unsupported variable query operation: " + variable.getVariableOperation());
            }
        }
    }

    protected HistoricProcessInstance getHistoricProcessInstanceFromRequest(String processInstanceId) {
        HistoryService historyService = BPMNOSGIService.getHistoryService();
        HistoricProcessInstance processInstance = historyService.createHistoricProcessInstanceQuery()
                .processInstanceId(processInstanceId).singleResult();
        if (processInstance == null) {
            throw new ActivitiObjectNotFoundException("Could not find a process instance with id '" + processInstanceId + "'.", HistoricProcessInstance.class);
        }
        return processInstance;
    }

}
