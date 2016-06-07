/**
 * Copyright (c) 2015 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.bpmn.rest.service.base;

import org.activiti.engine.HistoryService;
import org.activiti.engine.history.HistoricDetailQuery;
import org.activiti.engine.impl.HistoricDetailQueryProperty;
import org.activiti.engine.query.QueryProperty;
import org.wso2.carbon.bpmn.rest.common.RestResponseFactory;
import org.wso2.carbon.bpmn.rest.internal.RestServiceContentHolder;
import org.wso2.carbon.bpmn.rest.model.common.DataResponse;
import org.wso2.carbon.bpmn.rest.model.history.HistoricDetailPaginateList;
import org.wso2.carbon.bpmn.rest.model.history.HistoricDetailQueryRequest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class BaseHistoricDetailService {

    protected static final Map<String, QueryProperty> ALLOWED_SORT_PROPERTIES;
    protected static final List<String> ALL_PROPERTIES_LIST;

    static {
        List<String> properties = new ArrayList<>();
        properties.add("id");
        properties.add("processInstanceId");
        properties.add("executionId");
        properties.add("activityInstanceId");
        properties.add("taskId");
        properties.add("selectOnlyFormProperties");
        properties.add("selectOnlyVariableUpdates");
        properties.add("start");
        properties.add("size");
        properties.add("order");
        properties.add("sort");
        ALL_PROPERTIES_LIST = Collections.unmodifiableList(properties);
    }

    static {
        HashMap<String, QueryProperty> sortMap = new HashMap<>();
        sortMap.put("processInstanceId", HistoricDetailQueryProperty.PROCESS_INSTANCE_ID);
        sortMap.put("time", HistoricDetailQueryProperty.TIME);
        sortMap.put("name", HistoricDetailQueryProperty.VARIABLE_NAME);
        sortMap.put("revision", HistoricDetailQueryProperty.VARIABLE_REVISION);
        sortMap.put("variableType", HistoricDetailQueryProperty.VARIABLE_TYPE);
        ALLOWED_SORT_PROPERTIES = Collections.unmodifiableMap(sortMap);
    }

    protected DataResponse getQueryResponse(HistoricDetailQueryRequest queryRequest,
                                            Map<String, String> allRequestParams, String baseName) {
        HistoryService historyService = RestServiceContentHolder.getInstance().getRestService().getHistoryService();
        HistoricDetailQuery query = historyService.createHistoricDetailQuery();

        // Populate query based on request
        if (queryRequest.getProcessInstanceId() != null) {
            query.processInstanceId(queryRequest.getProcessInstanceId());
        }
        if (queryRequest.getExecutionId() != null) {
            query.executionId(queryRequest.getExecutionId());
        }
        if (queryRequest.getActivityInstanceId() != null) {
            query.activityInstanceId(queryRequest.getActivityInstanceId());
        }
        if (queryRequest.getTaskId() != null) {
            query.taskId(queryRequest.getTaskId());
        }
        if (queryRequest.getSelectOnlyFormProperties() != null) {
            if (queryRequest.getSelectOnlyFormProperties()) {
                query.formProperties();
            }
        }
        if (queryRequest.getSelectOnlyVariableUpdates() != null) {
            if (queryRequest.getSelectOnlyVariableUpdates()) {
                query.variableUpdates();
            }
        }

        return new HistoricDetailPaginateList(new RestResponseFactory(), baseName)
                .paginateList(allRequestParams, queryRequest, query, "processInstanceId",
                        ALLOWED_SORT_PROPERTIES);
    }
}
