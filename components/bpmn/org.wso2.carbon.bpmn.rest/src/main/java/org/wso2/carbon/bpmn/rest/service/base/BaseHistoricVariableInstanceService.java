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

package org.wso2.carbon.bpmn.rest.service.base;

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.HistoryService;
import org.activiti.engine.history.HistoricVariableInstanceQuery;
import org.activiti.engine.impl.HistoricVariableInstanceQueryProperty;
import org.activiti.engine.query.QueryProperty;
import org.wso2.carbon.bpmn.rest.common.RestResponseFactory;
import org.wso2.carbon.bpmn.rest.common.utils.BPMNOSGIService;
import org.wso2.carbon.bpmn.rest.engine.variable.QueryVariable;
import org.wso2.carbon.bpmn.rest.model.common.DataResponse;
import org.wso2.carbon.bpmn.rest.model.history.HistoricVariableInstancePaginateList;
import org.wso2.carbon.bpmn.rest.model.history.HistoricVariableInstanceQueryRequest;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class BaseHistoricVariableInstanceService {

    protected static final Map<String, QueryProperty> ALLOWED_SORT_PROPERTIES;
    protected static final List<String> ALL_PROPERTIES_LIST = Arrays.asList();

    static {
        HashMap<String, QueryProperty> sortMap = new HashMap<>();
        sortMap.put("processInstanceId", HistoricVariableInstanceQueryProperty.PROCESS_INSTANCE_ID);
        sortMap.put("variableName", HistoricVariableInstanceQueryProperty.VARIABLE_NAME);
        ALLOWED_SORT_PROPERTIES = Collections.unmodifiableMap(sortMap);
    }

    static {
        ALL_PROPERTIES_LIST.add("processInstanceId");
        ALL_PROPERTIES_LIST.add("taskId");
        ALL_PROPERTIES_LIST.add("excludeTaskVariables");
        ALL_PROPERTIES_LIST.add("variableName");
        ALL_PROPERTIES_LIST.add("variableNameLike");
        ALL_PROPERTIES_LIST.add("start");
        ALL_PROPERTIES_LIST.add("size");
        ALL_PROPERTIES_LIST.add("order");
        ALL_PROPERTIES_LIST.add("sort");
    }

    protected DataResponse getQueryResponse(HistoricVariableInstanceQueryRequest queryRequest,
                                            Map<String, String> allRequestParams, String baseName) {
        HistoryService historyService = BPMNOSGIService.getHistoryService();
        HistoricVariableInstanceQuery query = historyService.createHistoricVariableInstanceQuery();

        // Populate query based on request
        if (queryRequest.getExcludeTaskVariables() != null) {
            if (queryRequest.getExcludeTaskVariables()) {
                query.excludeTaskVariables();
            }
        }

        if (queryRequest.getTaskId() != null) {
            query.taskId(queryRequest.getTaskId());
        }

        if (queryRequest.getExecutionId() != null) {
            query.executionId(queryRequest.getExecutionId());
        }

        if (queryRequest.getProcessInstanceId() != null) {
            query.processInstanceId(queryRequest.getProcessInstanceId());
        }

        if (queryRequest.getVariableName() != null) {
            query.variableName(queryRequest.getVariableName());
        }

        if (queryRequest.getVariableNameLike() != null) {
            query.variableNameLike(queryRequest.getVariableNameLike());
        }

        if (queryRequest.getVariables() != null) {
            addVariables(query, queryRequest.getVariables());
        }

        return new HistoricVariableInstancePaginateList(new RestResponseFactory(), baseName)
                .paginateList(allRequestParams, query, "variableName", ALLOWED_SORT_PROPERTIES);
    }

    protected void addVariables(HistoricVariableInstanceQuery variableInstanceQuery,
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
            if (nameLess) {
                throw new ActivitiIllegalArgumentException(
                        "Value-only query (without a variable-name) is not supported");
            }

            switch (variable.getVariableOperation()) {

                case EQUALS:
                    variableInstanceQuery.variableValueEquals(variable.getName(), actualValue);
                    break;

                default:
                    throw new ActivitiIllegalArgumentException(
                            "Unsupported variable query operation: " +
                            variable.getVariableOperation());
            }
        }
    }

}
