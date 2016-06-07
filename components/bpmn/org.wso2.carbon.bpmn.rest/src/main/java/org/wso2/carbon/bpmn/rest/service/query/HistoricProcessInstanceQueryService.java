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
package org.wso2.carbon.bpmn.rest.service.query;

import io.netty.handler.codec.http.QueryStringDecoder;
import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.HistoryService;
import org.activiti.engine.impl.HistoricProcessInstanceQueryProperty;
import org.activiti.engine.query.QueryProperty;
import org.wso2.carbon.bpmn.rest.common.RestResponseFactory;
import org.wso2.carbon.bpmn.rest.engine.variable.QueryVariable;
import org.wso2.carbon.bpmn.rest.internal.RestServiceContentHolder;
import org.wso2.carbon.bpmn.rest.model.common.DataResponse;
import org.wso2.carbon.bpmn.rest.model.common.HistoricProcessInstanceQueryRequest;
import org.wso2.carbon.bpmn.rest.model.history.HistoricProcessInstancePaginateList;
import org.wso2.msf4j.Request;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 *
 */
@Path("/historic-process-instances")
public class HistoricProcessInstanceQueryService {

    private static Map<String, QueryProperty> allowedSortProperties = new HashMap<>();
    private static final List<String> allPropertiesList = new ArrayList<>();

    static {
        allowedSortProperties.put("processInstanceId",
                HistoricProcessInstanceQueryProperty.PROCESS_INSTANCE_ID_);
        allowedSortProperties.put("processDefinitionId",
                HistoricProcessInstanceQueryProperty.PROCESS_DEFINITION_ID);
        allowedSortProperties.put("businessKey", HistoricProcessInstanceQueryProperty.BUSINESS_KEY);
        allowedSortProperties.put("startTime", HistoricProcessInstanceQueryProperty.START_TIME);
        allowedSortProperties.put("endTime", HistoricProcessInstanceQueryProperty.END_TIME);
        allowedSortProperties.put("duration", HistoricProcessInstanceQueryProperty.DURATION);
        allowedSortProperties.put("tenantId", HistoricProcessInstanceQueryProperty.TENANT_ID);
    }

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

    @POST
    @Path("/")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response queryProcessInstances(HistoricProcessInstanceQueryRequest queryRequest,
                                          @Context Request request) {

        Map<String, String> allRequestParams = new HashMap<>();
        QueryStringDecoder decoder = new QueryStringDecoder(request.getUri());

        for (String property : allPropertiesList) {
            if (decoder.parameters().size() > 0) {
                String value = decoder.parameters().get(property).get(0);

                if (value != null) {
                    allRequestParams.put(property, value);
                }
            }
        }

        HistoryService historyService = RestServiceContentHolder.getInstance().getRestService().getHistoryService();
        org.activiti.engine.history.HistoricProcessInstanceQuery query =
                historyService.createHistoricProcessInstanceQuery();

        // Populate query based on request
        if (queryRequest.getProcessInstanceId() != null) {
            query.processInstanceId(queryRequest.getProcessInstanceId());
        }
        if (queryRequest.getProcessInstanceIds() != null &&
                !queryRequest.getProcessInstanceIds().isEmpty()) {
            query.processInstanceIds(new HashSet<>(queryRequest.getProcessInstanceIds()));
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

        DataResponse dataResponse =
                new HistoricProcessInstancePaginateList(new RestResponseFactory(), request.getUri())
                        .paginateList(allRequestParams, queryRequest, query, "processInstanceId",
                                allowedSortProperties);
        return Response.ok().entity(dataResponse).build();

    }

    protected void addVariables(
            org.activiti.engine.history.HistoricProcessInstanceQuery processInstanceQuery,
            List<QueryVariable> variables) {
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

            Object actualValue = new RestResponseFactory().getVariableValue(variable);

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
                                "Only string variable values are supported when ignoring casing," +
                                        " but was: " +
                                        actualValue.getClass().getName());
                    }
                    break;

                case NOT_EQUALS:
                    processInstanceQuery.variableValueNotEquals(variable.getName(), actualValue);
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
}
