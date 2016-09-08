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
import org.activiti.engine.history.HistoricActivityInstanceQuery;
import org.activiti.engine.impl.HistoricActivityInstanceQueryProperty;
import org.activiti.engine.query.QueryProperty;
import org.wso2.carbon.bpmn.rest.common.RestResponseFactory;
import org.wso2.carbon.bpmn.rest.common.utils.BPMNOSGIService;
import org.wso2.carbon.bpmn.rest.model.common.DataResponse;
import org.wso2.carbon.bpmn.rest.model.history.HistoricActivityInstancePaginateList;
import org.wso2.carbon.bpmn.rest.model.history.HistoricActivityInstanceQueryRequest;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BaseHistoricActivitiInstanceService {



    private static Map<String, QueryProperty> allowedSortProperties = new HashMap<String, QueryProperty>();
    protected static final List<String> allPropertiesList  = new ArrayList<>();



    static {
        allowedSortProperties.put("activityId", HistoricActivityInstanceQueryProperty.ACTIVITY_ID);
        allowedSortProperties.put("activityName", HistoricActivityInstanceQueryProperty.ACTIVITY_NAME);
        allowedSortProperties.put("activityType", HistoricActivityInstanceQueryProperty.ACTIVITY_TYPE);
        allowedSortProperties.put("duration", HistoricActivityInstanceQueryProperty.DURATION);
        allowedSortProperties.put("endTime", HistoricActivityInstanceQueryProperty.END);
        allowedSortProperties.put("executionId", HistoricActivityInstanceQueryProperty.EXECUTION_ID);
        allowedSortProperties.put("activityInstanceId", HistoricActivityInstanceQueryProperty.HISTORIC_ACTIVITY_INSTANCE_ID);
        allowedSortProperties.put("processDefinitionId", HistoricActivityInstanceQueryProperty.PROCESS_DEFINITION_ID);
        allowedSortProperties.put("processInstanceId", HistoricActivityInstanceQueryProperty.PROCESS_INSTANCE_ID);
        allowedSortProperties.put("startTime", HistoricActivityInstanceQueryProperty.START);
        allowedSortProperties.put("tenantId", HistoricActivityInstanceQueryProperty.TENANT_ID);

    }

    static {
        allPropertiesList.add("activityId");
        allPropertiesList.add("activityInstanceId");
        allPropertiesList.add("activityName");
        allPropertiesList.add("activityType");
        allPropertiesList.add("executionId");
        allPropertiesList.add("finished");
        allPropertiesList.add("taskAssignee");
        allPropertiesList.add("processInstanceId");
        allPropertiesList.add("processDefinitionId");
        allPropertiesList.add("tenantId");
        allPropertiesList.add("tenantIdLike");
        allPropertiesList.add("withoutTenantId");
        allPropertiesList.add("start");
        allPropertiesList.add("size");
        allPropertiesList.add("order");
        allPropertiesList.add("sort");
    }

    protected HistoricActivityInstanceQueryRequest getHistoricActivityInstanceQueryRequest (@Context UriInfo uriInfo, Map<String,String> allRequestParams){

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

        if(allRequestParams.get("withoutTenantId") != null) {
            query.setWithoutTenantId(Boolean.valueOf(allRequestParams.get("withoutTenantId")));
        }

        return query;
    }

    protected DataResponse getQueryResponse(HistoricActivityInstanceQueryRequest queryRequest, Map<String,String>
            allRequestParams, UriInfo uriInfo) {
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

        if(queryRequest.getTenantId() != null) {
            query.activityTenantId(queryRequest.getTenantId());
        }

        if(queryRequest.getTenantIdLike() != null) {
            query.activityTenantIdLike(queryRequest.getTenantIdLike());
        }

        if(Boolean.TRUE.equals(queryRequest.getWithoutTenantId())) {
            query.activityWithoutTenantId();
        }

        return new HistoricActivityInstancePaginateList(new RestResponseFactory(), uriInfo).paginateList(
                allRequestParams, queryRequest, query, "startTime", allowedSortProperties);
    }
}
