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

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.HistoryService;
import org.activiti.engine.history.HistoricIdentityLink;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.history.HistoricTaskInstanceQuery;
import org.activiti.engine.impl.persistence.entity.HistoricTaskInstanceEntity;
import org.activiti.engine.impl.persistence.entity.VariableInstanceEntity;
import org.wso2.carbon.bpmn.rest.common.RequestUtil;
import org.wso2.carbon.bpmn.rest.common.RestResponseFactory;
import org.wso2.carbon.bpmn.rest.common.utils.BPMNOSGIService;
import org.wso2.carbon.bpmn.rest.engine.variable.RestVariable;
import org.wso2.carbon.bpmn.rest.model.common.DataResponse;
import org.wso2.carbon.bpmn.rest.model.history.HistoricIdentityLinkResponse;
import org.wso2.carbon.bpmn.rest.model.history.HistoricIdentityLinkResponseCollection;
import org.wso2.carbon.bpmn.rest.model.history.HistoricTaskInstanceQueryRequest;
import org.wso2.carbon.bpmn.rest.model.runtime.HistoricTaskInstanceResponse;
import org.wso2.carbon.bpmn.rest.service.base.BaseHistoricTaskInstanceService;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Path("/historic-task-instances")
public class HistoricTaskInstanceService extends BaseHistoricTaskInstanceService {

    @GET
    @Path("/")
    @Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
    public Response getHistoricProcessInstances() {

        Map<String, String> allRequestParams = new HashMap<>();

        for (String property:allPropertiesList){
            String value= uriInfo.getQueryParameters().getFirst(property);

            if(value != null){
                allRequestParams.put(property, value);
            }
        }

        // Populate query based on request
        HistoricTaskInstanceQueryRequest queryRequest = new HistoricTaskInstanceQueryRequest();

        if (allRequestParams.get("taskId") != null) {
            queryRequest.setTaskId(allRequestParams.get("taskId"));
        }

        if (allRequestParams.get("processInstanceId") != null) {
            queryRequest.setProcessInstanceId(allRequestParams.get("processInstanceId"));
        }

        if (allRequestParams.get("processBusinessKey") != null) {
            queryRequest.setProcessBusinessKey(allRequestParams.get("processBusinessKey"));
        }

        if (allRequestParams.get("processDefinitionKey") != null) {
            queryRequest.setProcessDefinitionKey(allRequestParams.get("processDefinitionKey"));
        }

        if (allRequestParams.get("processDefinitionId") != null) {
            queryRequest.setProcessDefinitionId(allRequestParams.get("processDefinitionId"));
        }

        if (allRequestParams.get("processDefinitionName") != null) {
            queryRequest.setProcessDefinitionName(allRequestParams.get("processDefinitionName"));
        }

        if (allRequestParams.get("executionId") != null) {
            queryRequest.setExecutionId(allRequestParams.get("executionId"));
        }

        if (allRequestParams.get("taskName") != null) {
            queryRequest.setTaskName(allRequestParams.get("taskName"));
        }

        if (allRequestParams.get("taskNameLike") != null) {
            queryRequest.setTaskNameLike(allRequestParams.get("taskNameLike"));
        }

        if (allRequestParams.get("taskDescription") != null) {
            queryRequest.setTaskDescription(allRequestParams.get("taskDescription"));
        }

        if (allRequestParams.get("taskDescriptionLike") != null) {
            queryRequest.setTaskDescriptionLike(allRequestParams.get("taskDescriptionLike"));
        }

        if (allRequestParams.get("taskDefinitionKey") != null) {
            queryRequest.setTaskDefinitionKey(allRequestParams.get("taskDefinitionKey"));
        }

        if (allRequestParams.get("taskDeleteReason") != null) {
            queryRequest.setTaskDeleteReason(allRequestParams.get("taskDeleteReason"));
        }

        if (allRequestParams.get("taskDeleteReasonLike") != null) {
            queryRequest.setTaskDeleteReasonLike(allRequestParams.get("taskDeleteReasonLike"));
        }

        if (allRequestParams.get("taskAssignee") != null) {
            queryRequest.setTaskAssignee(allRequestParams.get("taskAssignee"));
        }

        if (allRequestParams.get("taskAssigneeLike") != null) {
            queryRequest.setTaskAssigneeLike(allRequestParams.get("taskAssigneeLike"));
        }

        if (allRequestParams.get("taskOwner") != null) {
            queryRequest.setTaskOwner(allRequestParams.get("taskOwner"));
        }

        if (allRequestParams.get("taskOwnerLike") != null) {
            queryRequest.setTaskOwnerLike(allRequestParams.get("taskOwnerLike"));
        }

        if (allRequestParams.get("taskInvolvedUser") != null) {
            queryRequest.setTaskInvolvedUser(allRequestParams.get("taskInvolvedUser"));
        }

        if (allRequestParams.get("taskPriority") != null) {
            queryRequest.setTaskPriority(Integer.valueOf(allRequestParams.get("taskPriority")));
        }

        if (allRequestParams.get("finished") != null) {
            queryRequest.setFinished(Boolean.valueOf(allRequestParams.get("finished")));
        }

        if (allRequestParams.get("processFinished") != null) {
            queryRequest.setProcessFinished(Boolean.valueOf(allRequestParams.get("processFinished")));
        }

        if (allRequestParams.get("parentTaskId") != null) {
            queryRequest.setParentTaskId(allRequestParams.get("parentTaskId"));
        }

        if (allRequestParams.get("dueDate") != null) {
            queryRequest.setDueDate(RequestUtil.getDate(allRequestParams, "dueDate"));
        }

        if (allRequestParams.get("dueDateAfter") != null) {
            queryRequest.setDueDateAfter(RequestUtil.getDate(allRequestParams, "dueDateAfter"));
        }

        if (allRequestParams.get("dueDateBefore") != null) {
            queryRequest.setDueDateBefore(RequestUtil.getDate(allRequestParams, "dueDateBefore"));
        }

        if (allRequestParams.get("taskCreatedOn") != null) {
            queryRequest.setTaskCreatedOn(RequestUtil.getDate(allRequestParams, "taskCreatedOn"));
        }

        if (allRequestParams.get("taskCreatedBefore") != null) {
            queryRequest.setTaskCreatedBefore(RequestUtil.getDate(allRequestParams, "taskCreatedBefore"));
        }

        if (allRequestParams.get("taskCreatedAfter") != null) {
            queryRequest.setTaskCreatedAfter(RequestUtil.getDate(allRequestParams, "taskCreatedAfter"));
        }

        if (allRequestParams.get("taskCompletedOn") != null) {
            queryRequest.setTaskCompletedOn(RequestUtil.getDate(allRequestParams, "taskCompletedOn"));
        }

        if (allRequestParams.get("taskCompletedBefore") != null) {
            queryRequest.setTaskCompletedBefore(RequestUtil.getDate(allRequestParams, "taskCompletedBefore"));
        }

        if (allRequestParams.get("taskCompletedAfter") != null) {
            queryRequest.setTaskCompletedAfter(RequestUtil.getDate(allRequestParams, "taskCompletedAfter"));
        }

        if (allRequestParams.get("includeTaskLocalVariables") != null) {
            queryRequest.setIncludeTaskLocalVariables(Boolean.valueOf(allRequestParams.get("includeTaskLocalVariables")));
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

        if (allRequestParams.get("taskCandidateGroup") != null) {
            queryRequest.setTaskCandidateGroup(allRequestParams.get("taskCandidateGroup"));
        }

        String serverRootUrl = uriInfo.getBaseUri().toString().replace("/history/historic-task-instances", "");

        DataResponse dataResponse = getQueryResponse(queryRequest, allRequestParams, serverRootUrl);
        return Response.ok().entity(dataResponse).build();
    }


    @GET
    @Path("/{taskId}")
    @Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
    public Response getTaskInstance(@PathParam("taskId") String taskId) {
        HistoricTaskInstanceResponse historicTaskInstanceResponse = new RestResponseFactory()
                .createHistoricTaskInstanceResponse(getHistoricTaskInstanceFromRequest(taskId), uriInfo.getBaseUri()
                        .toString());
        return Response.ok().entity(historicTaskInstanceResponse).build();
    }

    @DELETE
    @Path("/{taskId}")
    public Response deleteTaskInstance(@PathParam("taskId") String taskId) {
        HistoryService historyService = BPMNOSGIService.getHistoryService();
        historyService.deleteHistoricTaskInstance(taskId);
        return Response.ok().status(Response.Status.NO_CONTENT).build();
    }

    @GET
    @Path("/{taskId}/identitylinks")
    @Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
    public Response getTaskIdentityLinks(@PathParam("taskId") String taskId) {
        HistoryService historyService = BPMNOSGIService.getHistoryService();
        List<HistoricIdentityLink> identityLinks = historyService.getHistoricIdentityLinksForTask(taskId);

        List<HistoricIdentityLinkResponse> historicIdentityLinkResponseList = new ArrayList<>();
        if (identityLinks != null) {

            historicIdentityLinkResponseList =  new RestResponseFactory().createHistoricIdentityLinkResponseList
                    (identityLinks, uriInfo.getBaseUri
                    ().toString());
        }

        HistoricIdentityLinkResponseCollection historicIdentityLinkResponseCollection = new
                HistoricIdentityLinkResponseCollection();
        historicIdentityLinkResponseCollection.setHistoricIdentityLinkResponses(historicIdentityLinkResponseList);

        return Response.ok().entity(historicIdentityLinkResponseCollection).build();
    }

    @GET
    @Path("/{taskId}/variables/{variableName}/data")
    public  byte[] getVariableData(@PathParam("taskId") String taskId,
                                                @PathParam("variableName") String variableName) {

        String scope = uriInfo.getQueryParameters().getFirst("scope");
        Response.ResponseBuilder response = Response.ok();
        try {
            byte[] result = null;
            RestVariable variable = getVariableFromRequest(true, taskId, variableName, scope);
            if (RestResponseFactory.BYTE_ARRAY_VARIABLE_TYPE.equals(variable.getType())) {
                result = (byte[]) variable.getValue();
                response.type("application/octet-stream");

            } else if(RestResponseFactory.SERIALIZABLE_VARIABLE_TYPE.equals(variable.getType())) {
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                ObjectOutputStream outputStream = new ObjectOutputStream(buffer);
                outputStream.writeObject(variable.getValue());
                outputStream.close();
                result = buffer.toByteArray();
                response.type("application/x-java-serialized-object");

            } else {
                throw new ActivitiObjectNotFoundException("The variable does not have a binary data stream.", null);
            }
            return result;

        } catch(IOException ioe) {
            // Re-throw IOException
            throw new ActivitiException("Unexpected exception getting variable data", ioe);
        }
    }

    @OPTIONS
    @Path("{path : .*}")
    public Response options() {
        return Response.ok("").build();
    }

    protected RestVariable getVariableFromRequest(boolean includeBinary, String taskId, String variableName, String scope) {
        HistoryService historyService = BPMNOSGIService.getHistoryService();
        RestVariable.RestVariableScope variableScope = RestVariable.getScopeFromString(scope);
        HistoricTaskInstanceQuery taskQuery = historyService.createHistoricTaskInstanceQuery().taskId(taskId);

        if (variableScope != null) {
            if (variableScope == RestVariable.RestVariableScope.GLOBAL) {
                taskQuery.includeProcessVariables();
            } else {
                taskQuery.includeTaskLocalVariables();
            }
        } else {
            taskQuery.includeTaskLocalVariables().includeProcessVariables();
        }

        HistoricTaskInstance taskObject = taskQuery.singleResult();

        if (taskObject == null) {
            throw new ActivitiObjectNotFoundException("Historic task instance '" + taskId + "' couldn't be found.", HistoricTaskInstanceEntity.class);
        }

        Object value = null;
        if (variableScope != null) {
            if (variableScope == RestVariable.RestVariableScope.GLOBAL) {
                value = taskObject.getProcessVariables().get(variableName);
            } else {
                value = taskObject.getTaskLocalVariables().get(variableName);
            }
        } else {
            // look for local task restVariables first
            if (taskObject.getTaskLocalVariables().containsKey(variableName)) {
                value = taskObject.getTaskLocalVariables().get(variableName);
            } else {
                value = taskObject.getProcessVariables().get(variableName);
            }
        }

        if (value == null) {
            throw new ActivitiObjectNotFoundException("Historic task instance '" + taskId + "' variable value for " + variableName + " couldn't be found.", VariableInstanceEntity.class);
        } else {
            return new RestResponseFactory().createRestVariable(variableName, value, null, taskId,
                    RestResponseFactory.VARIABLE_HISTORY_TASK, includeBinary, uriInfo.getBaseUri().toString());
        }
    }

}
