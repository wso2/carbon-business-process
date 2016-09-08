/*
 * Copyright (c) 2015. WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.activiti.engine.*;
import org.activiti.engine.impl.JobQueryProperty;
import org.activiti.engine.management.TableMetaData;
import org.activiti.engine.management.TablePage;
import org.activiti.engine.management.TablePageQuery;
import org.activiti.engine.query.QueryProperty;
import org.activiti.engine.runtime.Job;
import org.activiti.engine.runtime.JobQuery;
import org.wso2.carbon.bpmn.rest.common.RequestUtil;
import org.wso2.carbon.bpmn.rest.common.RestResponseFactory;
import org.wso2.carbon.bpmn.rest.common.utils.BPMNOSGIService;
import org.wso2.carbon.bpmn.rest.common.utils.Utils;
import org.wso2.carbon.bpmn.rest.model.common.DataResponse;
import org.wso2.carbon.bpmn.rest.model.runtime.*;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ManagementService {
    @Context
    UriInfo uriInfo;

    private static final String EXECUTE_ACTION = "execute";
    protected static final Integer DEFAULT_RESULT_SIZE = 10;
    protected static Map<String, QueryProperty> properties;
    protected static final List<String> jobPropertiesList = new ArrayList<>();

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

    protected org.activiti.engine.ManagementService managementService = BPMNOSGIService.getManagementService();

    @GET
    @Path("/jobs")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public DataResponse getJobs() {
        JobQuery query = managementService.createJobQuery();
        Map<String, String> allRequestParams = Utils.populateRequestParams(jobPropertiesList, uriInfo);
        allRequestParams = Utils.prepareCommonParameters(allRequestParams, uriInfo);

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
                throw new ActivitiIllegalArgumentException("Only one of 'timersOnly' or 'messagesOnly' can be provided.");
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

        return new JobPaginateList(restResponseFactory, uriInfo)
                .paginateList(allRequestParams, query, "id", properties);
    }

    @GET
    @Path("/jobs/{jobId}/exception-stacktrace")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public String getJobStacktrace(@PathParam("jobId") String jobId) {
        Job job = getJobFromResponse(jobId);

        String stackTrace = managementService.getJobExceptionStacktrace(job.getId());

        if (stackTrace == null) {
            throw new ActivitiObjectNotFoundException("Job with id '" + job.getId() + "' doesn't have an exception stacktrace.", String.class);
        }

        Response.ok(stackTrace, "plain");
        return stackTrace;
    }

    protected Job getJobFromResponse(String jobId) {
        Job job = managementService.createJobQuery().jobId(jobId).singleResult();

        if (job == null) {
            throw new ActivitiObjectNotFoundException("Could not find a job with id '" + jobId + "'.", Job.class);
        }
        return job;
    }

    @GET
    @Path("/jobs/{jobId}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public JobResponse getJob(@PathParam("jobId") String jobId) {
        Job job = getJobFromResponse(jobId);

        return restResponseFactory.createJobResponse(job, uriInfo.getBaseUri().toString());
    }

    @DELETE
    @Path("/jobs/{jobId}")
    public void deleteJob(@PathParam("jobId") String jobId) {
        try {
            managementService.deleteJob(jobId);
        } catch(ActivitiObjectNotFoundException aonfe) {
            // Re-throw to have consistent error-messaging acrosse REST-api
            throw new ActivitiObjectNotFoundException("Could not find a job with id '" + jobId + "'.", Job.class);
        }
        Response.noContent().build();
    }

    @POST
    @Path("/jobs/{jobId}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public void executeJobAction(@PathParam("jobId") String jobId, RestActionRequest actionRequest) {

        if (actionRequest == null || ! EXECUTE_ACTION.equals(actionRequest.getAction())) {
            throw new ActivitiIllegalArgumentException("Invalid action, only 'execute' is supported.");
        }

        try {
            managementService.executeJob(jobId);
        } catch(ActivitiObjectNotFoundException aonfe) {
            // Re-throw to have consistent error-messaging acrosse REST-api
            throw new ActivitiObjectNotFoundException("Could not find a job with id '" + jobId + "'.", Job.class);
        }

        Response.noContent().build();
    }

    @GET
    @Path("/engine")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public ProcessEngineInfoResponse getEngineInfo() {
        ProcessEngineInfoResponse response = new ProcessEngineInfoResponse();
        ProcessEngine engine = BPMNOSGIService.getBPMNEngineService().getProcessEngine();

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

    @GET
    @Path("/properties")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Map<String, String> getProperties() {
        return managementService.getProperties();
    }

    @GET
    @Path("/tables")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public List<TableResponse> getTables() {
        return restResponseFactory.createTableResponseList(managementService.getTableCount(), uriInfo.getBaseUri().toString());
    }

    @GET
    @Path("/tables/{tableName}/columns")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public TableMetaData getTableMetaData(@PathParam("tableName") String tableName) {
        TableMetaData response = managementService.getTableMetaData(tableName);

        if (response == null) {
            throw new ActivitiObjectNotFoundException("Could not find a table with name '" + tableName + "'.", String.class);
        }
        return response;
    }

    @GET
    @Path("/tables/{tableName}/data")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public DataResponse getTableData(@PathParam("tableName") String tableName) {

        Map<String,String> allRequestParams = new HashMap<>();
        // Check if table exists before continuing
        if (managementService.getTableMetaData(tableName) == null) {
            throw new ActivitiObjectNotFoundException("Could not find a table with name '" + tableName + "'.", String.class);
        }

        String orderAsc = uriInfo.getQueryParameters().getFirst("orderAscendingColumn");
        String orderDesc = uriInfo.getQueryParameters().getFirst("orderDescendingColumn");

        if (orderAsc != null && orderDesc != null) {
            throw new ActivitiIllegalArgumentException("Only one of 'orderAscendingColumn' or 'orderDescendingColumn' can be supplied.");
        }

        allRequestParams = Utils.prepareCommonParameters(allRequestParams, uriInfo);

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

        TablePageQuery tablePageQuery = managementService.createTablePageQuery().tableName(tableName);

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
        response.setData((List)listPage.getRows());

        return response;
    }

    @GET
    @Path("/tables/{tableName}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public TableResponse getTable(@PathParam("tableName") String tableName) {
        Map<String, Long> tableCounts = managementService.getTableCount();

        TableResponse response = null;
        for (Map.Entry<String, Long> entry : tableCounts.entrySet()) {
            if (entry.getKey().equals(tableName)) {
                response = restResponseFactory.createTableResponse(entry.getKey(), entry.getValue(), uriInfo.getBaseUri().toString());
                break;
            }
        }

        if (response == null) {
            throw new ActivitiObjectNotFoundException("Could not find a table with name '" + tableName + "'.", String.class);
        }
        return response;
    }

    @OPTIONS
    @Path("{path : .*}")
    public Response options() {
        return Response.ok("").build();
    }
}
