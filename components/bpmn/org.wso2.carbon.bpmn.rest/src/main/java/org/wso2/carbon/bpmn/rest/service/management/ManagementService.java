/*
 * Copyright (c) 2015-2016 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * /
 */

package org.wso2.carbon.bpmn.rest.service.management;

import io.netty.handler.codec.http.QueryStringDecoder;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineInfo;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.impl.JobQueryProperty;
import org.activiti.engine.management.TableMetaData;
import org.activiti.engine.management.TablePage;
import org.activiti.engine.management.TablePageQuery;
import org.activiti.engine.query.QueryProperty;
import org.activiti.engine.runtime.Job;
import org.activiti.engine.runtime.JobQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.bpmn.rest.common.RequestUtil;
import org.wso2.carbon.bpmn.rest.common.RestResponseFactory;
import org.wso2.carbon.bpmn.rest.common.utils.Utils;
import org.wso2.carbon.bpmn.rest.internal.RestServiceContentHolder;
import org.wso2.carbon.bpmn.rest.model.common.DataResponse;
import org.wso2.carbon.bpmn.rest.model.runtime.JobPaginateList;
import org.wso2.carbon.bpmn.rest.model.runtime.JobResponse;
import org.wso2.carbon.bpmn.rest.model.runtime.ProcessEngineInfoResponse;
import org.wso2.carbon.bpmn.rest.model.runtime.RestActionRequest;
import org.wso2.carbon.bpmn.rest.model.runtime.TableResponse;
import org.wso2.msf4j.Request;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

/**
 *
 */
//@Component(
//        name = "org.wso2.carbon.bpmn.rest.service.management.ManagementService",
//        service = Microservice.class,
//        immediate = true)
public class ManagementService { //} implements Microservice {

    protected static final Integer DEFAULT_RESULT_SIZE = 10;
    private static final Logger log = LoggerFactory.getLogger(ManagementService.class);
    private static final String EXECUTE_ACTION = "execute";
    private static final List<String> jobPropertiesList = new ArrayList<>();
    private static Map<String, QueryProperty> properties;

    static {
        jobPropertiesList.add("id");
        jobPropertiesList.add("processInstanceId");
        jobPropertiesList.add("executionId");
        jobPropertiesList.add("processDefinitionId");
        jobPropertiesList.add("dueBefore");
        jobPropertiesList.add("dueAfter");
        jobPropertiesList.add("exceptionMessage");
        jobPropertiesList.add("tenantId");
        jobPropertiesList.add("tenantIdLike");
        jobPropertiesList.add("withRetriesLeft");
        jobPropertiesList.add("executable");
        jobPropertiesList.add("timersOnly");
        jobPropertiesList.add("messagesOnly");
        jobPropertiesList.add("withException");
        jobPropertiesList.add("withoutTenantId");
    }

    static {
        properties = new HashMap<String, QueryProperty>();
        properties.put("id", JobQueryProperty.JOB_ID);
        properties.put("dueDate", JobQueryProperty.DUEDATE);
        properties.put("executionId", JobQueryProperty.EXECUTION_ID);
        properties.put("processInstanceId", JobQueryProperty.PROCESS_INSTANCE_ID);
        properties.put("retries", JobQueryProperty.RETRIES);
        properties.put("tenantId", JobQueryProperty.TENANT_ID);
    }

    protected RestResponseFactory restResponseFactory = new RestResponseFactory();


    public DataResponse getJobs(Request request) {
        JobQuery query = RestServiceContentHolder.getInstance().getRestService().getManagementService()
                .createJobQuery();
        QueryStringDecoder decoder = new QueryStringDecoder(request.getUri());
        Map<String, List<String>> queryParams = decoder.parameters();
        Map<String, String> allRequestParams =
                Utils.populateRequestParams(jobPropertiesList, queryParams);
        allRequestParams = Utils.prepareCommonParameters(allRequestParams, queryParams);

        if (allRequestParams.containsKey("id")) {
            query.jobId(allRequestParams.get("id"));
        }
        if (allRequestParams.containsKey("processInstanceId")) {
            query.processInstanceId(allRequestParams.get("processInstanceId"));
        }
        if (allRequestParams.containsKey("executionId")) {
            query.executionId(allRequestParams.get("executionId"));
        }
        if (allRequestParams.containsKey("processDefinitionId")) {
            query.processDefinitionId(allRequestParams.get("processDefinitionId"));
        }
        if (allRequestParams.containsKey("withRetriesLeft")) {
            if (Boolean.valueOf(allRequestParams.get("withRetriesLeft"))) {
                query.withRetriesLeft();
            }
        }
        if (allRequestParams.containsKey("executable")) {
            if (Boolean.valueOf(allRequestParams.get("executable"))) {
                query.executable();
            }
        }
        if (allRequestParams.containsKey("timersOnly")) {
            if (allRequestParams.containsKey("messagesOnly")) {
                throw new ActivitiIllegalArgumentException(
                        "Only one of 'timersOnly' or 'messagesOnly' can be provided.");
            }
            if (Boolean.valueOf(allRequestParams.get("timersOnly"))) {
                query.timers();
            }
        }
        if (allRequestParams.containsKey("messagesOnly")) {
            if (Boolean.valueOf(allRequestParams.get("messagesOnly"))) {
                query.messages();
            }
        }
        if (allRequestParams.containsKey("dueBefore")) {
            query.duedateLowerThan(RequestUtil.getDate(allRequestParams, "dueBefore"));
        }
        if (allRequestParams.containsKey("dueAfter")) {
            query.duedateHigherThan(RequestUtil.getDate(allRequestParams, "dueAfter"));
        }
        if (allRequestParams.containsKey("withException")) {
            if (Boolean.valueOf(allRequestParams.get("withException"))) {
                query.withException();
            }
        }
        if (allRequestParams.containsKey("exceptionMessage")) {
            query.exceptionMessage(allRequestParams.get("exceptionMessage"));
        }
        if (allRequestParams.containsKey("tenantId")) {
            query.jobTenantId(allRequestParams.get("tenantId"));
        }
        if (allRequestParams.containsKey("tenantIdLike")) {
            query.jobTenantIdLike(allRequestParams.get("tenantIdLike"));
        }
        if (allRequestParams.containsKey("withoutTenantId")) {
            if (Boolean.valueOf(allRequestParams.get("withoutTenantId"))) {
                query.jobWithoutTenantId();
            }
        }

        return new JobPaginateList(restResponseFactory, request.getUri())
                .paginateList(allRequestParams, query, "id", properties);
    }


    public String getJobStacktrace(String jobId) {
        Job job = getJobFromResponse(jobId);

        String stackTrace = RestServiceContentHolder.getInstance().getRestService().getManagementService()
                .getJobExceptionStacktrace(job.getId());

        if (stackTrace == null) {
            throw new ActivitiObjectNotFoundException(
                    "Job with id '" + job.getId() + "' doesn't have an exception stacktrace.",
                    String.class);
        }

        Response.ok(stackTrace, "plain");
        return stackTrace;
    }

    protected Job getJobFromResponse(String jobId) {
        Job job = RestServiceContentHolder.getInstance().getRestService().getManagementService().createJobQuery()
                .jobId(jobId).singleResult();

        if (job == null) {
            throw new ActivitiObjectNotFoundException(
                    "Could not find a job with id '" + jobId + "'.", Job.class);
        }
        return job;
    }

    public JobResponse getJob(String jobId, Request request) {
        Job job = getJobFromResponse(jobId);

        return restResponseFactory.createJobResponse(job, request.getUri());
    }

    public void deleteJob(String jobId) {
        try {
            RestServiceContentHolder.getInstance().getRestService().getManagementService().deleteJob(jobId);
        } catch (ActivitiObjectNotFoundException aonfe) {
            // Re-throw to have consistent error-messaging acrosse REST-api
            throw new ActivitiObjectNotFoundException(
                    "Could not find a job with id '" + jobId + "'.", Job.class);
        }
        Response.noContent().build();
    }

    public void executeJobAction(String jobId, RestActionRequest actionRequest) {

        if (actionRequest == null || !EXECUTE_ACTION.equals(actionRequest.getAction())) {
            throw new ActivitiIllegalArgumentException(
                    "Invalid action, only 'execute' is supported.");
        }

        try {
            RestServiceContentHolder.getInstance().getRestService().getManagementService().executeJob(jobId);
        } catch (ActivitiObjectNotFoundException aonfe) {
            // Re-throw to have consistent error-messaging acrosse REST-api
            throw new ActivitiObjectNotFoundException(
                    "Could not find a job with id '" + jobId + "'.", Job.class);
        }

        Response.noContent().build();
    }

    public ProcessEngineInfoResponse getEngineInfo() {
        ProcessEngineInfoResponse response = new ProcessEngineInfoResponse();
        ProcessEngine engine = RestServiceContentHolder.getInstance().getRestService().getBPMNEngineService()
                .getProcessEngine();

        try {
            ProcessEngineInfo engineInfo = ProcessEngines.getProcessEngineInfo(engine.getName());
            if (engineInfo != null) {
                response.setName(engineInfo.getName());
                response.setResourceUrl(engineInfo.getResourceUrl());
                response.setException(engineInfo.getException());
            } else {
                // Revert to using process-engine directly
                response.setName(engine.getName());
            }
        } catch (Exception e) {
            throw new ActivitiException("Error retrieving process info", e);
        }

        response.setVersion(ProcessEngine.VERSION);
        return response;
    }

    public Map<String, String> getProperties() {
        return RestServiceContentHolder.getInstance().getRestService().getManagementService().getProperties();
    }

    public List<TableResponse> getTables(@Context Request request) {
        return restResponseFactory
                .createTableResponseList(RestServiceContentHolder.getInstance().getRestService().getManagementService
                        ().getTableCount(), request.getUri());
    }

    public TableMetaData getTableMetaData(String tableName) {
        TableMetaData response = RestServiceContentHolder.getInstance().getRestService().getManagementService()
                .getTableMetaData(tableName);

        if (response == null) {
            throw new ActivitiObjectNotFoundException(
                    "Could not find a table with name '" + tableName + "'.", String.class);
        }
        return response;
    }

    public DataResponse getTableData(String tableName, Request request, String orderAsc, String orderDesc) {

        Map<String, String> allRequestParams = new HashMap<>();
        // Check if table exists before continuing
        if (RestServiceContentHolder.getInstance().getRestService().getManagementService().getTableMetaData
                (tableName) == null) {
            throw new ActivitiObjectNotFoundException(
                    "Could not find a table with name '" + tableName + "'.", String.class);
        }

        if (orderAsc != null && orderDesc != null) {
            throw new ActivitiIllegalArgumentException(
                    "Only one of 'orderAscendingColumn' or 'orderDescendingColumn' can be supplied.");
        }
        QueryStringDecoder decoder = new QueryStringDecoder(request.getUri());
        Map<String, List<String>> queryParams = decoder.parameters();
        allRequestParams = Utils.prepareCommonParameters(allRequestParams, queryParams);

        Integer start = null;
        if (allRequestParams.containsKey("start")) {
            start = Integer.valueOf(allRequestParams.get("start"));
        }

        if (start == null) {
            start = 0;
        }

        Integer size = null;
        if (allRequestParams.containsKey("size")) {
            size = Integer.valueOf(allRequestParams.get("size"));
        }

        if (size == null) {
            size = DEFAULT_RESULT_SIZE;
        }

        DataResponse response = new DataResponse();

        TablePageQuery tablePageQuery =
                RestServiceContentHolder.getInstance().getRestService().getManagementService().createTablePageQuery()
                        .tableName(tableName);

        if (orderAsc != null) {
            allRequestParams.put("orderAscendingColumn", orderAsc);
            tablePageQuery.orderAsc(orderAsc);
            response.setOrder("asc");
            response.setSort(orderAsc);
        }

        if (orderDesc != null) {
            allRequestParams.put("orderDescendingColumn", orderDesc);
            tablePageQuery.orderDesc(orderDesc);
            response.setOrder("desc");
            response.setSort(orderDesc);
        }

        TablePage listPage = tablePageQuery.listPage(start, size);
        response.setSize(((Long) listPage.getSize()).intValue());
        response.setStart(((Long) listPage.getFirstResult()).intValue());
        response.setTotal(listPage.getTotal());
        response.setData(listPage.getRows());

        return response;
    }

    public TableResponse getTable(String tableName, Request request) {
        Map<String, Long> tableCounts = RestServiceContentHolder.getInstance().getRestService().getManagementService
                ().getTableCount();

        TableResponse response = null;
        for (Map.Entry<String, Long> entry : tableCounts.entrySet()) {
            if (entry.getKey().equals(tableName)) {
                response = restResponseFactory
                        .createTableResponse(entry.getKey(), entry.getValue(), request.getUri());
                break;
            }
        }

        if (response == null) {
            throw new ActivitiObjectNotFoundException(
                    "Could not find a table with name '" + tableName + "'.", String.class);
        }
        return response;
    }
}
