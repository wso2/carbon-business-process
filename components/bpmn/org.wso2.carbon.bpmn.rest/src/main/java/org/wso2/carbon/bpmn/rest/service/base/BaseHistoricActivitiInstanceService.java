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

import org.activiti.engine.HistoryService;
import org.activiti.engine.history.HistoricActivityInstanceQuery;
import org.activiti.engine.impl.HistoricActivityInstanceQueryProperty;
import org.activiti.engine.query.QueryProperty;
import org.wso2.carbon.bpmn.rest.common.RestResponseFactory;
import org.wso2.carbon.bpmn.rest.internal.BPMNOSGIService;
import org.wso2.carbon.bpmn.rest.model.common.DataResponse;
import org.wso2.carbon.bpmn.rest.model.history.HistoricActivityInstancePaginateList;
import org.wso2.carbon.bpmn.rest.model.history.HistoricActivityInstanceQueryRequest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class BaseHistoricActivitiInstanceService {

    protected static final Map<String, QueryProperty> ALLOWED_SORT_PROPERTIES;
    protected static final List<String> ALL_PROPERTIES_LIST;

    static {
        Map<String, QueryProperty> allowedPropertiesMap = new HashMap<>();
        allowedPropertiesMap.put("activityId", HistoricActivityInstanceQueryProperty.ACTIVITY_ID);
        allowedPropertiesMap
                .put("activityName", HistoricActivityInstanceQueryProperty.ACTIVITY_NAME);
        allowedPropertiesMap
                .put("activityType", HistoricActivityInstanceQueryProperty.ACTIVITY_TYPE);
        allowedPropertiesMap.put("duration", HistoricActivityInstanceQueryProperty.DURATION);
        allowedPropertiesMap.put("endTime", HistoricActivityInstanceQueryProperty.END);
        allowedPropertiesMap
                .put("executionId", HistoricActivityInstanceQueryProperty.EXECUTION_ID);
        allowedPropertiesMap.put("activityInstanceId",
                HistoricActivityInstanceQueryProperty.HISTORIC_ACTIVITY_INSTANCE_ID);
        allowedPropertiesMap.put("processDefinitionId",
                HistoricActivityInstanceQueryProperty.PROCESS_DEFINITION_ID);
        allowedPropertiesMap.put("processInstanceId",
                HistoricActivityInstanceQueryProperty.PROCESS_INSTANCE_ID);
        allowedPropertiesMap.put("startTime", HistoricActivityInstanceQueryProperty.START);
        allowedPropertiesMap.put("tenantId", HistoricActivityInstanceQueryProperty.TENANT_ID);
        ALLOWED_SORT_PROPERTIES = Collections.unmodifiableMap(allowedPropertiesMap);
    }

    static {
        List<String> properties = new ArrayList<>();
        properties.add("activityId");
        properties.add("activityInstanceId");
        properties.add("activityName");
        properties.add("activityType");
        properties.add("executionId");
        properties.add("finished");
        properties.add("taskAssignee");
        properties.add("processInstanceId");
        properties.add("processDefinitionId");
        properties.add("tenantId");
        properties.add("tenantIdLike");
        properties.add("withoutTenantId");
        properties.add("start");
        properties.add("size");
        properties.add("order");
        properties.add("sort");
        ALL_PROPERTIES_LIST = Collections.unmodifiableList(properties);
    }

    protected HistoricActivityInstanceQueryRequest getHistoricActivityInstanceQueryRequest(
            Map<String, String> allRequestParams) {

        HistoricActivityInstanceQueryRequest query = new HistoricActivityInstanceQueryRequest();

        // Populate query based on request
        if (allRequestParams.get("activityId") != null) {
            query.setActivityId(allRequestParams.get("activityId"));
        }

        if (allRequestParams.get("activityInstanceId") != null) {
            query.setActivityInstanceId(allRequestParams.get("activityInstanceId"));
        }

        if (allRequestParams.get("activityName") != null) {
            query.setActivityName(allRequestParams.get("activityName"));
        }

        if (allRequestParams.get("activityType") != null) {
            query.setActivityType(allRequestParams.get("activityType"));
        }

        if (allRequestParams.get("executionId") != null) {
            query.setExecutionId(allRequestParams.get("executionId"));
        }

        if (allRequestParams.get("finished") != null) {
            query.setFinished(Boolean.valueOf(allRequestParams.get("finished")));
        }

        if (allRequestParams.get("taskAssignee") != null) {
            query.setTaskAssignee(allRequestParams.get("taskAssignee"));
        }

        if (allRequestParams.get("processInstanceId") != null) {
            query.setProcessInstanceId(allRequestParams.get("processInstanceId"));
        }

        if (allRequestParams.get("processDefinitionId") != null) {
            query.setProcessDefinitionId(allRequestParams.get("processDefinitionId"));
        }

        if (allRequestParams.get("tenantId") != null) {
            query.setTenantId(allRequestParams.get("tenantId"));
        }

        if (allRequestParams.get("tenantIdLike") != null) {
            query.setTenantIdLike(allRequestParams.get("tenantIdLike"));
        }

        if (allRequestParams.get("withoutTenantId") != null) {
            query.setWithoutTenantId(Boolean.valueOf(allRequestParams.get("withoutTenantId")));
        }

        return query;
    }

    protected DataResponse getQueryResponse(HistoricActivityInstanceQueryRequest queryRequest,
                                            Map<String, String> allRequestParams, String baseName) {
        HistoryService historyService = BPMNOSGIService.getHistoryService();
        HistoricActivityInstanceQuery query = historyService.createHistoricActivityInstanceQuery();

        // Populate query based on request
        if (queryRequest.getActivityId() != null) {
            query.activityId(queryRequest.getActivityId());
        }

        if (queryRequest.getActivityInstanceId() != null) {
            query.activityInstanceId(queryRequest.getActivityInstanceId());
        }

        if (queryRequest.getActivityName() != null) {
            query.activityName(queryRequest.getActivityName());
        }

        if (queryRequest.getActivityType() != null) {
            query.activityType(queryRequest.getActivityType());
        }

        if (queryRequest.getExecutionId() != null) {
            query.executionId(queryRequest.getExecutionId());
        }

        if (queryRequest.getFinished() != null) {
            Boolean finished = queryRequest.getFinished();
            if (finished) {
                query.finished();
            } else {
                query.unfinished();
            }
        }

        if (queryRequest.getTaskAssignee() != null) {
            query.taskAssignee(queryRequest.getTaskAssignee());
        }

        if (queryRequest.getProcessInstanceId() != null) {
            query.processInstanceId(queryRequest.getProcessInstanceId());
        }

        if (queryRequest.getProcessDefinitionId() != null) {
            query.processDefinitionId(queryRequest.getProcessDefinitionId());
        }

        if (queryRequest.getTenantId() != null) {
            query.activityTenantId(queryRequest.getTenantId());
        }

        if (queryRequest.getTenantIdLike() != null) {
            query.activityTenantIdLike(queryRequest.getTenantIdLike());
        }

        if (Boolean.TRUE.equals(queryRequest.getWithoutTenantId())) {
            query.activityWithoutTenantId();
        }

        return new HistoricActivityInstancePaginateList(new RestResponseFactory(), baseName)
                .paginateList(allRequestParams, queryRequest, query, "startTime",
                        ALLOWED_SORT_PROPERTIES);
    }
}
