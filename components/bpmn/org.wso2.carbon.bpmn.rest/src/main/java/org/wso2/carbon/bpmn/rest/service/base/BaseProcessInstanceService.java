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

import io.netty.handler.codec.http.QueryStringDecoder;
import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.impl.ProcessInstanceQueryProperty;
import org.activiti.engine.query.QueryProperty;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.runtime.ProcessInstanceQuery;
import org.activiti.engine.task.IdentityLink;
import org.wso2.carbon.bpmn.rest.common.RestResponseFactory;
import org.wso2.carbon.bpmn.rest.common.exception.BPMNConflictException;
import org.wso2.carbon.bpmn.rest.engine.variable.QueryVariable;
import org.wso2.carbon.bpmn.rest.internal.RestServiceContentHolder;
import org.wso2.carbon.bpmn.rest.model.common.DataResponse;
import org.wso2.carbon.bpmn.rest.model.runtime.ProcessInstancePaginateList;
import org.wso2.carbon.bpmn.rest.model.runtime.ProcessInstanceQueryRequest;
import org.wso2.carbon.bpmn.rest.model.runtime.ProcessInstanceResponse;
import org.wso2.msf4j.Request;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//import org.wso2.carbon.bpmn.rest.common.exception.BPMNOSGIServiceException;

/**
 *
 */
public class BaseProcessInstanceService {

    protected static final String DEFAULT_ENCODING = "UTF-8";
    protected static final List<String> ALL_PROPERTIES_LIST;
    protected static final Map<String, QueryProperty> ALLOWED_SORT_PROPERTIES;

    static {
        List<String> properties = new ArrayList<>();
        properties.add("id");
        properties.add("processDefinitionKey");
        properties.add("processDefinitionId");
        properties.add("businessKey");
        properties.add("involvedUser");
        properties.add("suspended");
        properties.add("superProcessInstanceId");
        properties.add("subProcessInstanceId");
        properties.add("excludeSubprocesses");
        properties.add("includeProcessVariables");
        properties.add("tenantId");
        properties.add("tenantIdLike");
        properties.add("withoutTenantId");
        properties.add("sort");
        properties.add("start");
        properties.add("size");
        properties.add("order");
        ALL_PROPERTIES_LIST = Collections.unmodifiableList(properties);
    }

    static {
        HashMap<String, QueryProperty> sortMap = new HashMap<>();
        sortMap.put("processDefinitionId", ProcessInstanceQueryProperty.PROCESS_DEFINITION_ID);
        sortMap.put("processDefinitionKey", ProcessInstanceQueryProperty.PROCESS_DEFINITION_KEY);
        sortMap.put("id", ProcessInstanceQueryProperty.PROCESS_INSTANCE_ID);
        sortMap.put("tenantId", ProcessInstanceQueryProperty.TENANT_ID);
        ALLOWED_SORT_PROPERTIES = Collections.unmodifiableMap(sortMap);
    }

    protected Map<String, String> allRequestParams(Request request) {
        Map<String, String> allRequestParams = new HashMap<>();
        QueryStringDecoder decoder = new QueryStringDecoder(request.getUri());
        if (decoder.parameters().size() > 0) {
            for (String property : ALL_PROPERTIES_LIST) {
                String value = decoder.parameters().get(property).get(0);

                if (value != null) {
                    allRequestParams.put(property, value);
                }
            }
        }

        return allRequestParams;
    }

    protected ProcessInstance getProcessInstanceFromRequest(String processInstanceId) {
        RuntimeService runtimeService = RestServiceContentHolder.getInstance().getRestService().getRumtimeService();
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().
                processInstanceId(processInstanceId).singleResult();
        if (processInstance == null) {
            throw new ActivitiObjectNotFoundException(
                    " Could not find a process instance with id " +
                            processInstanceId + "'.", ProcessInstance.class);
        }
        return processInstance;
    }

    protected Execution getExecutionInstanceFromRequest(String processInstanceId) {
        RuntimeService runtimeService = RestServiceContentHolder.getInstance().getRestService().getRumtimeService();
        Execution execution =
                runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId)
                        .singleResult();
        if (execution == null) {
            throw new ActivitiObjectNotFoundException(
                    "Could not find a process instance with id '" +
                            processInstanceId + "'.", ProcessInstance.class);
        }
        return execution;
    }

    protected ProcessInstanceResponse activateProcessInstance(ProcessInstance processInstance,
                                                              String baseContext) {
        if (!processInstance.isSuspended()) {
            throw new BPMNConflictException("Process instance with id '" +
                    processInstance.getId() + "' is already active.");
        }

        RuntimeService runtimeService = RestServiceContentHolder.getInstance().getRestService().getRumtimeService();
        runtimeService.activateProcessInstanceById(processInstance.getId());

        ProcessInstanceResponse response = new RestResponseFactory()
                .createProcessInstanceResponse(processInstance, baseContext);

        // No need to re-fetch the instance, just alter the suspended state of the result-object
        response.setSuspended(false);
        return response;
    }

    protected ProcessInstanceResponse suspendProcessInstance(ProcessInstance processInstance,
                                                             RestResponseFactory restResponseFactory,
                                                             String baseContext) {
        if (processInstance.isSuspended()) {
            throw new BPMNConflictException("Process instance with id '" +
                    processInstance.getId() + "' is already suspended.");
        }

        RuntimeService runtimeService = RestServiceContentHolder.getInstance().getRestService().getRumtimeService();
        runtimeService.suspendProcessInstanceById(processInstance.getId());

        ProcessInstanceResponse response =
                restResponseFactory.createProcessInstanceResponse(processInstance, baseContext);

        // No need to re-fetch the instance, just alter the suspended state of the result-object
        response.setSuspended(true);
        return response;
    }

    protected DataResponse getQueryResponse(ProcessInstanceQueryRequest queryRequest,
                                            Map<String, String> requestParams, String baseName) {

        RuntimeService runtimeService = RestServiceContentHolder.getInstance().getRestService().getRumtimeService();
        ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery();

        // Populate query based on request
        if (queryRequest.getProcessInstanceId() != null) {
            query.processInstanceId(queryRequest.getProcessInstanceId());
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
        if (queryRequest.getSuspended() != null) {
            if (queryRequest.getSuspended()) {
                query.suspended();
            } else {
                query.active();
            }
        }
        if (queryRequest.getSubProcessInstanceId() != null) {
            query.subProcessInstanceId(queryRequest.getSubProcessInstanceId());
        }
        if (queryRequest.getSuperProcessInstanceId() != null) {
            query.superProcessInstanceId(queryRequest.getSuperProcessInstanceId());
        }
        if (queryRequest.getExcludeSubprocesses() != null) {
            query.excludeSubprocesses(queryRequest.getExcludeSubprocesses());
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

        return new ProcessInstancePaginateList(new RestResponseFactory(), baseName)
                .paginateList(requestParams, queryRequest, query, "id", ALLOWED_SORT_PROPERTIES);
    }

    protected void addVariables(ProcessInstanceQuery processInstanceQuery,
                                List<QueryVariable> variables) {

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
                        processInstanceQuery.variableValueEquals(actualValue);
                    } else {
                        processInstanceQuery.variableValueEquals(variable.getName(), actualValue);
                    }
                    break;

                case EQUALS_IGNORE_CASE:
                    if (actualValue instanceof String) {
                        processInstanceQuery.variableValueEqualsIgnoreCase(variable.getName(),
                                (String) actualValue);
                    } else {
                        throw new ActivitiIllegalArgumentException(
                                "Only string variable values are supported when ignoring casing, " +
                                        "but was: " +
                                        actualValue.getClass().getName());
                    }
                    break;

                case NOT_EQUALS:
                    processInstanceQuery.variableValueNotEquals(variable.getName(), actualValue);
                    break;

                case NOT_EQUALS_IGNORE_CASE:
                    if (actualValue instanceof String) {
                        processInstanceQuery.variableValueNotEqualsIgnoreCase(variable.getName(),
                                (String) actualValue);
                    } else {
                        throw new ActivitiIllegalArgumentException(
                                "Only string variable values are supported when ignoring casing," +
                                        " but was: " +
                                        actualValue.getClass().getName());
                    }
                    break;

                case LIKE:
                    if (actualValue instanceof String) {
                        processInstanceQuery
                                .variableValueLike(variable.getName(), (String) actualValue);
                    } else {
                        throw new ActivitiIllegalArgumentException(
                                "Only string variable values are supported for like, but was: " +
                                        actualValue.getClass().getName());
                    }
                    break;

                case GREATER_THAN:
                    processInstanceQuery.variableValueGreaterThan(variable.getName(), actualValue);
                    break;

                case GREATER_THAN_OR_EQUALS:
                    processInstanceQuery
                            .variableValueGreaterThanOrEqual(variable.getName(), actualValue);
                    break;

                case LESS_THAN:
                    processInstanceQuery.variableValueLessThan(variable.getName(), actualValue);
                    break;

                case LESS_THAN_OR_EQUALS:
                    processInstanceQuery
                            .variableValueLessThanOrEqual(variable.getName(), actualValue);
                    break;

                default:
                    throw new ActivitiIllegalArgumentException(
                            "Unsupported variable query operation: " +
                                    variable.getVariableOperation());
            }
        }
    }

    protected ProcessInstanceQueryRequest getQueryRequest(Map<String, String> allRequestParams) {
        // Populate query based on request
        ProcessInstanceQueryRequest queryRequest = new ProcessInstanceQueryRequest();

        if (allRequestParams.containsKey("id")) {
            queryRequest.setProcessInstanceId(allRequestParams.get("id"));
        }

        if (allRequestParams.containsKey("processDefinitionKey")) {
            queryRequest.setProcessDefinitionKey(allRequestParams.get("processDefinitionKey"));
        }

        if (allRequestParams.containsKey("processDefinitionId")) {
            queryRequest.setProcessDefinitionId(allRequestParams.get("processDefinitionId"));
        }

        if (allRequestParams.containsKey("businessKey")) {
            queryRequest.setProcessBusinessKey(allRequestParams.get("businessKey"));
        }

        if (allRequestParams.containsKey("involvedUser")) {
            queryRequest.setInvolvedUser(allRequestParams.get("involvedUser"));
        }

        if (allRequestParams.containsKey("suspended")) {
            queryRequest.setSuspended(Boolean.valueOf(allRequestParams.get("suspended")));
        }

        if (allRequestParams.containsKey("superProcessInstanceId")) {
            queryRequest.setSuperProcessInstanceId(allRequestParams.get("superProcessInstanceId"));
        }

        if (allRequestParams.containsKey("subProcessInstanceId")) {
            queryRequest.setSubProcessInstanceId(allRequestParams.get("subProcessInstanceId"));
        }

        if (allRequestParams.containsKey("excludeSubprocesses")) {
            queryRequest.setExcludeSubprocesses(
                    Boolean.valueOf(allRequestParams.get("excludeSubprocesses")));
        }

        if (allRequestParams.containsKey("includeProcessVariables")) {
            queryRequest.setIncludeProcessVariables(
                    Boolean.valueOf(allRequestParams.get("includeProcessVariables")));
        }

        if (allRequestParams.containsKey("tenantId")) {
            queryRequest.setTenantId(allRequestParams.get("tenantId"));
        }

        if (allRequestParams.containsKey("tenantIdLike")) {
            queryRequest.setTenantIdLike(allRequestParams.get("tenantIdLike"));
        }

        if (allRequestParams.containsKey("withoutTenantId")) {
            if (Boolean.valueOf(allRequestParams.get("withoutTenantId"))) {
                queryRequest.setWithoutTenantId(Boolean.TRUE);
            }
        }

        return queryRequest;
    }

    protected void validateIdentityLinkArguments(String identityId, String type) {
        if (identityId == null) {
            throw new ActivitiIllegalArgumentException("IdentityId is required.");
        }
        if (type == null) {
            throw new ActivitiIllegalArgumentException("Type is required.");
        }
    }

    protected IdentityLink getIdentityLink(String identityId, String type,
                                           String processInstanceId) {
        RuntimeService runtimeService = RestServiceContentHolder.getInstance().getRestService().getRumtimeService();
        // Perhaps it would be better to offer getting a single identity link from the API
        List<IdentityLink> allLinks =
                runtimeService.getIdentityLinksForProcessInstance(processInstanceId);
        for (IdentityLink link : allLinks) {
            if (identityId.equals(link.getUserId()) && link.getType().equals(type)) {
                return link;
            }
        }
        throw new ActivitiObjectNotFoundException("Could not find the requested identity link.",
                IdentityLink.class);
    }
}
