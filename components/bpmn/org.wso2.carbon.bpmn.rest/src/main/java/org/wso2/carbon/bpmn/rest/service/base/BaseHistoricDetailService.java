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

import org.activiti.engine.HistoryService;
import org.activiti.engine.history.HistoricDetailQuery;
import org.activiti.engine.impl.HistoricDetailQueryProperty;
import org.activiti.engine.query.QueryProperty;
import org.wso2.carbon.bpmn.rest.common.RestResponseFactory;
import org.wso2.carbon.bpmn.rest.common.utils.BPMNOSGIService;
import org.wso2.carbon.bpmn.rest.model.common.DataResponse;
import org.wso2.carbon.bpmn.rest.model.history.HistoricDetailPaginateList;
import org.wso2.carbon.bpmn.rest.model.history.HistoricDetailQueryRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class BaseHistoricDetailService {

    protected static Map<String, QueryProperty> allowedSortProperties = new HashMap<>();
    protected static final List<String> ALL_PROPERTIES_LIST = new ArrayList<>();

    static {
        ALL_PROPERTIES_LIST.add("id");
        ALL_PROPERTIES_LIST.add("processInstanceId");
        ALL_PROPERTIES_LIST.add("executionId");
        ALL_PROPERTIES_LIST.add("activityInstanceId");
        ALL_PROPERTIES_LIST.add("taskId");
        ALL_PROPERTIES_LIST.add("selectOnlyFormProperties");
        ALL_PROPERTIES_LIST.add("selectOnlyVariableUpdates");
        ALL_PROPERTIES_LIST.add("start");
        ALL_PROPERTIES_LIST.add("size");
        ALL_PROPERTIES_LIST.add("order");
        ALL_PROPERTIES_LIST.add("sort");
    }

    static {
        allowedSortProperties
                .put("processInstanceId", HistoricDetailQueryProperty.PROCESS_INSTANCE_ID);
        allowedSortProperties.put("time", HistoricDetailQueryProperty.TIME);
        allowedSortProperties.put("name", HistoricDetailQueryProperty.VARIABLE_NAME);
        allowedSortProperties.put("revision", HistoricDetailQueryProperty.VARIABLE_REVISION);
        allowedSortProperties.put("variableType", HistoricDetailQueryProperty.VARIABLE_TYPE);
    }

    protected DataResponse getQueryResponse(HistoricDetailQueryRequest queryRequest,
                                            Map<String, String> allRequestParams) {
        HistoryService historyService = BPMNOSGIService.getHistoryService();
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

        return new HistoricDetailPaginateList(new RestResponseFactory())
                .paginateList(allRequestParams, queryRequest, query, "processInstanceId",
                              allowedSortProperties);
    }
}
