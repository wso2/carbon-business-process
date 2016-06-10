/**
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 **/


package org.wso2.carbon.bpmn.rest.service;

import org.activiti.engine.management.TableMetaData;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.bpmn.core.BPMNEngineService;
import org.wso2.carbon.bpmn.rest.BPMNRestServiceImpl;
import org.wso2.carbon.bpmn.rest.engine.variable.RestVariable;
import org.wso2.carbon.bpmn.rest.internal.RestServiceContentHolder;
import org.wso2.carbon.bpmn.rest.model.common.DataResponse;
import org.wso2.carbon.bpmn.rest.model.common.RestIdentityLink;
import org.wso2.carbon.bpmn.rest.model.correlation.CorrelationActionRequest;
import org.wso2.carbon.bpmn.rest.model.error.ErrorResponse;
import org.wso2.carbon.bpmn.rest.model.form.SubmitFormRequest;
import org.wso2.carbon.bpmn.rest.model.identity.GroupResponse;
import org.wso2.carbon.bpmn.rest.model.identity.UserInfoResponse;
import org.wso2.carbon.bpmn.rest.model.identity.UserResponse;
import org.wso2.carbon.bpmn.rest.model.repository.ProcessDefinitionResponse;
import org.wso2.carbon.bpmn.rest.model.runtime.CommentRequest;
import org.wso2.carbon.bpmn.rest.model.runtime.CommentResponse;
import org.wso2.carbon.bpmn.rest.model.runtime.ExecutionActionRequest;
import org.wso2.carbon.bpmn.rest.model.runtime.JobResponse;
import org.wso2.carbon.bpmn.rest.model.runtime.ProcessEngineInfoResponse;
import org.wso2.carbon.bpmn.rest.model.runtime.ProcessInstanceActionRequest;
import org.wso2.carbon.bpmn.rest.model.runtime.ProcessInstanceCreateRequest;
import org.wso2.carbon.bpmn.rest.model.runtime.RestActionRequest;
import org.wso2.carbon.bpmn.rest.model.runtime.TableResponse;
import org.wso2.carbon.bpmn.rest.model.runtime.TaskActionRequest;
import org.wso2.carbon.bpmn.rest.model.runtime.TaskRequest;
import org.wso2.carbon.bpmn.rest.model.stats.ResponseHolder;
import org.wso2.carbon.bpmn.rest.service.correlate.CorrelationService;
import org.wso2.carbon.bpmn.rest.service.form.FormDataService;
import org.wso2.carbon.bpmn.rest.service.form.ProcessDefinitionFormPropertyService;
import org.wso2.carbon.bpmn.rest.service.history.HistoricActivitiInstanceService;
import org.wso2.carbon.bpmn.rest.service.history.HistoricDetailService;
import org.wso2.carbon.bpmn.rest.service.history.HistoricProcessInstanceService;
import org.wso2.carbon.bpmn.rest.service.history.HistoricTaskInstanceService;
import org.wso2.carbon.bpmn.rest.service.history.HistoricVariableInstanceService;
import org.wso2.carbon.bpmn.rest.service.identity.IdentityService;
import org.wso2.carbon.bpmn.rest.service.management.ManagementService;
import org.wso2.carbon.bpmn.rest.service.repository.DeploymentService;
import org.wso2.carbon.bpmn.rest.service.repository.ModelService;
import org.wso2.carbon.bpmn.rest.service.repository.ProcessDefinitionService;
import org.wso2.carbon.bpmn.rest.service.runtime.ExecutionService;
import org.wso2.carbon.bpmn.rest.service.runtime.ProcessInstanceService;
import org.wso2.carbon.bpmn.rest.service.runtime.WorkflowTaskService;
import org.wso2.carbon.bpmn.rest.service.stats.ProcessAndTaskService;
import org.wso2.msf4j.Microservice;
import org.wso2.msf4j.Request;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Rest component lookup for bpmnEngineService
 */
@Component(
        name = "org.wso2.carbon.bpmn.rest.service.BPMNRestAPI",
        service = Microservice.class,
        immediate = true)
@Path("/")
public class BPMNRestAPI implements Microservice {

    private static final Logger log = LoggerFactory.getLogger(BPMNRestAPI.class);

    private CorrelationService correlationService;
    private FormDataService formDataService;
    private ProcessDefinitionFormPropertyService processDefinitionFormPropertyService;
    private HistoricActivitiInstanceService historicActivitiInstanceService;
    private HistoricDetailService historicDetailService;
    private HistoricProcessInstanceService historicProcessInstanceService;
    private HistoricTaskInstanceService historicTaskInstanceService;
    private HistoricVariableInstanceService historicVariableInstanceService;
    private IdentityService identityService;
    private ManagementService managementService;
    private DeploymentService deploymentService;
    private ModelService modelService;
    private ProcessDefinitionService processDefinitionService;
    private ExecutionService executionService;
    private ProcessInstanceService processInstanceService;
    private WorkflowTaskService workflowTaskService;
    private ProcessAndTaskService processAndTaskService;

    @Reference(
            name = "org.wso2.carbon.bpmn.core.BPMNEngineService",
            service = BPMNEngineService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unRegisterBPMNEngineService")
    public void setBpmnEngineService(BPMNEngineService engineService) {
        RestServiceContentHolder.getInstance().setBpmnEngineService(engineService);
    }

    protected void unRegisterBPMNEngineService(BPMNEngineService engineService) {
        RestServiceContentHolder.getInstance().setBpmnEngineService(null);
        if (log.isDebugEnabled()) {
            log.debug("Unregistered BPMNEngineService..");
        }
    }

    @Activate
    protected void activate(ComponentContext ctxt) {
        RestServiceContentHolder restServiceContentHolder = RestServiceContentHolder.getInstance();
        BPMNRestServiceImpl restService = new BPMNRestServiceImpl();
        restServiceContentHolder.setRestService(restService);

        correlationService = new CorrelationService();
        formDataService = new FormDataService();
        processDefinitionFormPropertyService = new ProcessDefinitionFormPropertyService();
        historicActivitiInstanceService = new HistoricActivitiInstanceService();
        historicDetailService = new HistoricDetailService();
        historicProcessInstanceService = new HistoricProcessInstanceService();
        historicTaskInstanceService = new HistoricTaskInstanceService();
        historicVariableInstanceService = new HistoricVariableInstanceService();
        identityService = new IdentityService();
        managementService = new ManagementService();
        deploymentService = new DeploymentService();
        modelService = new ModelService();
        processDefinitionService = new ProcessDefinitionService();
        executionService = new ExecutionService();
        processInstanceService = new ProcessInstanceService();
        workflowTaskService = new WorkflowTaskService();
        processAndTaskService = new ProcessAndTaskService();

        log.info("Activated BPMN Rest API Successfully.");
    }

    @Deactivate
    protected void deactivate(BundleContext bundleContext) {
        // Nothing to do
    }

    private Response handleServiceError(Request request, String errorMsg) {
        Response.ResponseBuilder responseBuilder = Response.serverError();
        responseBuilder.status(Response.Status.SERVICE_UNAVAILABLE);
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setErrorMessage(errorMsg);
        errorResponse.setErrorReference(UUID.randomUUID().toString());
        responseBuilder.entity(errorResponse);
        log.error(errorResponse.toString());
        return responseBuilder.build();
    }

    @POST
    @Path("/receive")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response receiveMessage(CorrelationActionRequest correlationActionRequest, @Context Request request) {
        if (correlationService != null) {
            return correlationService.recieveMessage(correlationActionRequest, request);
        } else {
            return handleServiceError(request, "Server not initialized yet.");
        }
    }

    @GET
    @Path("/form-data")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getFormData(@Context Request request, @QueryParam("taskId") String taskId,
                                @QueryParam("processDefinitionId") String processDefinitionId) {

        if (formDataService != null) {
            return formDataService.getFormData(request, taskId, processDefinitionId);
        } else {
            return handleServiceError(request, "Server not initialized yet.");
        }
    }

    @POST
    @Path("/form-data")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response submitForm(SubmitFormRequest submitRequest, @Context Request request) {
        if (formDataService != null) {
            return formDataService.submitForm(submitRequest, request);
        } else {
            return handleServiceError(request, "Server not initialized yet.");
        }
    }

    @GET
    @Path("/{process-definition-id}/properties")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getStartFormProperties(@PathParam("process-definition-id") String processDefinitionId) {

        return processDefinitionFormPropertyService.getStartFormProperties(processDefinitionId);
    }

    @GET
    @Path("/historic-activity-instances")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getHistoricActivityInstances(@Context Request request) {

        return historicActivitiInstanceService.getHistoricActivityInstances(request);
    }

    @GET
    @Path("/historic-detail")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getHistoricDetailInfo(@Context Request request) {

        return historicDetailService.getHistoricDetailInfo(request);
    }

    @GET
    @Path("/historic-detail/{detail-id}/data")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getHistoricVariableData(@PathParam("detail-id") String detailId, @Context Request request) {
        return historicDetailService.getVariableData(detailId, request);
    }

    @GET
    @Path("/historic-process-instances")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getHistoricProcessInstances(@Context Request request) {

        return historicProcessInstanceService.getHistoricProcessInstances(request);
    }

    @GET
    @Path("/historic-process-instances/{process-instance-id}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getHistoricProcessInstance(@PathParam("process-instance-id") String processInstanceId,
                                               @Context Request request) {

        return historicProcessInstanceService.getProcessInstance(processInstanceId, request);

    }

    @DELETE
    @Path("/historic-process-instances/{process-instance-id}")
    public Response deleteHistoricProcessInstance(
            @PathParam("process-instance-id") String processInstanceId) {
        return historicProcessInstanceService.deleteProcessInstance(processInstanceId);
    }

    @GET
    @Path("/historic-process-instances/{process-instance-id}/identity-links")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getHistoricProcessIdentityLinks(
            @PathParam("process-instance-id") String processInstanceId,
            @Context Request request) {

        return historicProcessInstanceService.getProcessIdentityLinks(processInstanceId, request);
    }

    @GET
    @Path("/historic-process-instances/{process-instance-id}/variables/{variable-name}/data")
    public Response getHistoricProcessVariableData(@PathParam("process-instance-id") String processInstanceId,
                                                   @PathParam("variable-name") String variableName,
                                                   @Context Request request) {

        return historicProcessInstanceService.getVariableData(processInstanceId, variableName, request);
    }

    @GET
    @Path("/historic-process-instances/{process-instance-id}/comments")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getHistoricProcessComments(@PathParam("process-instance-id") String processInstanceId,
                                               @Context Request request) {
        return historicProcessInstanceService.getComments(processInstanceId, request);
    }

    @POST
    @Path("/historic-process-instances/{process-instance-id}/comments")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response createHistoricProcessComment(@PathParam("process-instance-id") String processInstanceId,
                                                 CommentResponse comment, @Context Request request) {

        return historicProcessInstanceService.createComment(processInstanceId, comment, request);
    }

    @GET
    @Path("/historic-process-instances/{process-instance-id}/comments/{comment-id}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getHistoricProcessComment(@PathParam("process-instance-id") String processInstanceId,
                                              @PathParam("comment-id") String commentId,
                                              @Context Request request) {

        return historicProcessInstanceService.getComment(processInstanceId, commentId, request);
    }

    @DELETE
    @Path("/historic-process-instances/{process-instance-id}/comments/{comment-id}")
    public Response deleteHistoricProcessComment(@PathParam("process-instance-id") String processInstanceId,
                                                 @PathParam("comment-id") String commentId) {

        return historicProcessInstanceService.deleteComment(processInstanceId, commentId);
    }

    @GET
    @Path("/historic-task-instances")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getHistoricTaskInstances(@Context Request request) {

        return historicTaskInstanceService.getHistoricTaskInstances(request);
    }

    @GET
    @Path("/historic-task-instances/{task-id}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getHistoricTaskInstance(@PathParam("task-id") String taskId,
                                            @Context Request request) {
        return historicTaskInstanceService.getTaskInstance(taskId, request);
    }

    @DELETE
    @Path("/historic-task-instances/{task-id}")
    public Response deleteHistoricTaskInstance(@PathParam("task-id") String taskId) {
        return historicTaskInstanceService.deleteTaskInstance(taskId);
    }

    @GET
    @Path("/historic-task-instances/{task-id}/identitylinks")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getHistoricTaskIdentityLinks(@PathParam("task-id") String taskId,
                                                 @Context Request request) {
        return historicTaskInstanceService.getTaskIdentityLinks(taskId, request);
    }

    @GET
    @Path("/historic-task-instances/{task-id}/variables/{variable-name}/data")
    public byte[] getHistoricskTaVariableData(@PathParam("task-id") String taskId,
                                              @PathParam("variable-name") String variableName,
                                              @QueryParam("scope") String scope, @Context Request request) {

        return historicTaskInstanceService.getVariableData(taskId, variableName, scope, request);
    }

    @GET
    @Path("/historic-variable-instances")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getHistoricVariableInstances(@Context Request request) {
        return historicVariableInstanceService.getHistoricVariableInstances(request);
    }

    @GET
    @Path("/identity/groups")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public DataResponse getGroups(@Context Request request) {
        return identityService.getGroups(request);
    }

    @GET
    @Path("/identity/groups/{group-id}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public GroupResponse getGroup(@PathParam("group-id") String groupId,
                                  @Context Request request) {
        return identityService.getGroup(groupId, request);
    }

    @GET
    @Path("/identity/users")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public DataResponse getUsers(@Context Request request) {
        return identityService.getUsers(request);
    }

    /**
     * Get the user information of the user identified by given user ID.
     *
     * @param userId
     * @return
     */
    @GET
    @Path("/identity/users/{user-id}/info")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public List<UserInfoResponse> getUserInfo(@PathParam("user-id") String userId,
                                              @Context Request request) {
        return identityService.getUserInfo(userId, request);
    }

    /**
     * Get the user identified by given user ID,
     *
     * @param userId
     * @return
     */
    @GET
    @Path("/identity/users/{user-id}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public UserResponse getUser(@PathParam("user-id") String userId, @Context Request request) {
        return identityService.getUser(userId, request);
    }

    @GET
    @Path("/jobs")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public DataResponse getJobs(@Context Request request) {
        return managementService.getJobs(request);
    }

    @GET
    @Path("/jobs/{job-id}/exception-stacktrace")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public String getJobStacktrace(@PathParam("job-id") String jobId) {
        return managementService.getJobStacktrace(jobId);
    }


    @GET
    @Path("/jobs/{job-id}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public JobResponse getJob(@PathParam("job-id") String jobId, @Context Request request) {
        return managementService.getJob(jobId, request);
    }

    @DELETE
    @Path("/jobs/{job-id}")
    public void deleteJob(@PathParam("job-id") String jobId) {
        managementService.deleteJob(jobId);
    }

    @POST
    @Path("/jobs/{job-id}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public void executeJobAction(@PathParam("job-id") String jobId,
                                 RestActionRequest actionRequest) {
        managementService.executeJobAction(jobId, actionRequest);
    }

    @GET
    @Path("/engine")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public ProcessEngineInfoResponse getEngineInfo() {
        return managementService.getEngineInfo();
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
    public List<TableResponse> getTables(@Context Request request) {
        return managementService.getTables(request);
    }

    @GET
    @Path("/tables/{table-name}/columns")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public TableMetaData getTableMetaData(@PathParam("table-name") String tableName) {
        return managementService.getTableMetaData(tableName);
    }

    @GET
    @Path("/tables/{table-name}/data")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public DataResponse getTableData(@PathParam("table-name") String tableName,
                                     @Context Request request,
                                     @QueryParam("orderAscendingColumn") String orderAsc,
                                     @QueryParam("orderDescendingColumn") String orderDesc) {

        return managementService.getTableData(tableName, request, orderAsc, orderDesc);
    }

    @GET
    @Path("/tables/{table-name}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public TableResponse getTable(@PathParam("table-name") String tableName,
                                  @Context Request request) {
        return managementService.getTable(tableName, request);
    }


    @GET
    @Path("/deployments")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getDeployments(@Context Request request) {
        return deploymentService.getDeployments(request);
    }

    @GET
    @Path("/deployments/{deployment-id}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getDeployment(@PathParam("deployment-id") String deploymentId,
                                  @Context Request request) {

        return deploymentService.getDeployment(deploymentId, request);
    }

    @GET
    @Path("/deployments/{deployment-id}/resources/{resource-path:.*}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getDeploymentResourceForDifferentUrl(
            @PathParam("deployment-id") String deploymentId,
            @PathParam("resource-path") String resourcePath, @Context Request request) {

        return deploymentService.getDeploymentResourceForDifferentUrl(deploymentId, resourcePath, request);

    }

    @GET
    @Path("/deployments/{deployment-id}/resources")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getDeploymentResources(@PathParam("deployment-id") String deploymentId,
                                           @Context Request request) {

        return deploymentService.getDeploymentResources(deploymentId, request);
    }

    @GET
    @Path("/deployments/{deployment-id}/resource-data/{resource-id}")
    public Response getDeploymentResource(@PathParam("deployment-id") String deploymentId,
                                          @PathParam("resource-id") String resourceId) {
        return deploymentService.getDeploymentResource(deploymentId, resourceId);
    }

    @GET
    @Path("/models")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getModels(@Context Request request) {

        return modelService.getModels(request);
    }

    @GET
    @Path("/process-definitions")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getProcessDefinitions(@Context Request request) {

        return processDefinitionService.getProcessDefinitions(request);
    }

    @GET
    @Path("/process-definitions/{process-definition-id}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public ProcessDefinitionResponse getProcessDefinition(
            @PathParam("process-definition-id") String processDefinitionId,
            @Context Request request) {
        return processDefinitionService.getProcessDefinition(processDefinitionId, request);
    }

    @GET
    @Path("/process-definitions/{process-definition-id}/resource-data")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getProcessDefinitionResource(
            @PathParam("process-definition-id") String processDefinitionId) {

        return processDefinitionService.getProcessDefinitionResource(processDefinitionId);
    }

    @GET
    @Path("/process-definitions/{process-definition-id}/identity-links")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getProcessDefintionsIdentityLinks(@PathParam("process-definition-id") String processDefinitionId,
                                                      @Context Request request) {

        return processDefinitionService.getIdentityLinks(processDefinitionId, request);
    }

    @GET
    @Path("/process-definitions/{process-definition-id}/identity-links/{family}/{identity-id}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getIdentityLinks(@PathParam("process-definition-id") String processDefinitionId,
                                     @PathParam("family") String family,
                                     @PathParam("identity-id") String identityId,
                                     @Context Request request) {

        return processDefinitionService.getIdentityLinks(processDefinitionId, family, identityId, request);
    }


    /**
     * Get the process execution identified by given execution ID
     *
     * @param executionId
     * @return ExecutionResponse
     */
    @GET
    @Path("/executions/{execution-id}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getExecution(@PathParam("execution-id") String executionId,
                                 @Context Request request) {

        return executionService.getExecution(executionId, request);
    }

    /**
     * Execute an action on an execution
     *
     * @param executionId
     * @param actionRequest
     * @return Response
     */
    @PUT
    @Path("/executions/{execution-id}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response performExecutionAction(@PathParam("execution-id") String executionId,
                                           ExecutionActionRequest actionRequest,
                                           @Context Request request) {

        return executionService.performExecutionAction(executionId, actionRequest, request);
    }

    @GET
    @Path("/executions/{execution-id}/activities")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getActiveActivities(@PathParam("execution-id") String executionId) {
        return executionService.getActiveActivities(executionId);
    }

    @GET
    @Path("/executions")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getExecutionsProcessInstances(@Context Request request) {
        return executionService.getProcessInstances(request);
    }

    @PUT
    @Path("/executions")
    public Response executeExecutionAction(ExecutionActionRequest actionRequest) {
        return executionService.executeExecutionAction(actionRequest);
    }

    @GET
    @Path("/executions/{execution-id}/variables")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getExecutionVariables(@PathParam("execution-id") String executionId,
                                          @QueryParam("scope") String scope, @Context Request req) {

        return executionService.getVariables(executionId, scope, req);
    }

    // TODO
//    @PUT
//    @Path("/executions/{execution-id}/variables")
//    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
//    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
//    public Response createOrUpdateExecutionVariable(@PathParam("execution-id") String executionId,
//                                                    @Context HttpServletRequest httpServletRequest,
//                                                    @Context Request req) {
//        return executionService.
//    }

    // TODO
//    @PUT
//    @Path("/executions/{executionId}/variables")
//    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
//    @Consumes(MediaType.MULTIPART_FORM_DATA)
//    public Response createOrUpdateBinaryExecutionVariable(@PathParam("executionId")
//                                                                  String executionId,
//                                                          MultipartBody multipartBody) {
//        return executionService.
//    }

    // TODO
//    @POST
//    @Path("/executions/{execution-id}/variables")
//    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
//    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
//    public Response createExecutionVariable(@PathParam("execution-id") String executionId,
//                                            @Context HttpServletRequest httpServletRequest,
//                                            @Context Request req) {
//
//        return executionService.
//    }

    // TODO
//    @POST
//    @Path("/executions/{executionId}/variables")
//    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
//    @Consumes(MediaType.MULTIPART_FORM_DATA)
//    public Response createBinaryExecutionVariable(@PathParam("executionId") String executionId,
//                                                  @Context HttpStreamer httpStreamer, @Context Request request) {
//
//        return executionService.
//    }

    @DELETE
    @Path("/executions/{execution-id}/variables")
    public Response deleteLocalVariables(@PathParam("execution-id") String executionId) {
        return executionService.deleteLocalVariables(executionId);
    }

    @GET
    @Path("/executions/{execution-id}/variables/{variable-name}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public RestVariable getExecutionVariable(@PathParam("execution-id") String executionId,
                                             @PathParam("variable-name") String variableName,
                                             @QueryParam("scope") String scope, @Context Request req) {
        return executionService.getVariable(executionId, variableName, scope, req);
    }

// TODO
//    @PUT
//    @Path("/executions/{executionId}/variables/{variableName}")
//    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
//    @Consumes(MediaType.MULTIPART_FORM_DATA)
//    public Response updateBinaryVariable(@PathParam("executionId") String executionId,
//                                         @PathParam("variableName") String variableName,
//                                         MultipartBody multipartBody) {
//        return executionService.
//    }

    // TODO
//    @PUT
//    @Path("/executions/{execution-id}/variables/{variable-name}")
//    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
//    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
//    public Response updateVariable(@PathParam("execution-id") String executionId,
//                                   @PathParam("variable-name") String variableName,
//                                   @Context HttpServletRequest httpServletRequest,
//                                   @Context Request req) {
//        return executionService.
//    }

    @DELETE
    @Path("/executions/{execution-id}/variables/{variable-name}")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response deleteVariable(@PathParam("execution-id") String executionId,
                                   @PathParam("variable-name") String variableName,
                                   @QueryParam("scope") String scope) {
        return executionService.deleteVariable(executionId, variableName, scope);
    }

    @GET
    @Path("/executions/{execution-id}/variables/{variable-name}/data")
    public Response getExecutionVariableData(@PathParam("execution-id") String executionId,
                                             @PathParam("variable-name") String variableName,
                                             @QueryParam("scope") String scope, @Context Request req) {

        return executionService.getVariableData(executionId, variableName, scope, req);
    }


    @GET
    @Path("/process-instances")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getProcessInstances(@Context Request request) {

        return processInstanceService.getProcessInstances(request);
    }

    @POST
    @Path("/process-instances")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response startInstance(ProcessInstanceCreateRequest processInstanceCreateRequest,
                                  @Context Request request) {

        return processInstanceService.startInstance(processInstanceCreateRequest, request);
    }

    @GET
    @Path("/process-instances/{process-instance-id}/diagram")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getProcessInstanceDiagram(
            @PathParam("process-instance-id") String processInstanceId) {
        return processInstanceService.getProcessInstanceDiagram(processInstanceId);
    }

    @GET
    @Path("/process-instances/{process-instance-id}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getProcessInstance(@PathParam("process-instance-id") String processInstanceId,
                                       @Context Request request) {

        return processInstanceService.getProcessInstance(processInstanceId, request);
    }

    @DELETE
    @Path("/process-instances/{process-instance-id}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response deleteProcessInstance(
            @PathParam("process-instance-id") String processInstanceId,
            @DefaultValue("") @QueryParam("deleteReason") String deleteReason) {

        return processInstanceService.deleteProcessInstance(processInstanceId, deleteReason);
    }

    @PUT
    @Path("/process-instances/{process-instance-id}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response performProcessInstanceAction(
            @PathParam("process-instance-id") String processInstanceId,
            ProcessInstanceActionRequest actionRequest, @Context Request request) {

        return processInstanceService.performProcessInstanceAction(processInstanceId, actionRequest, request);
    }

    @GET
    @Path("/process-instances/{process-instance-id}/identity-links")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public List<RestIdentityLink> getIdentityLinks(
            @PathParam("process-instance-id") String processInstanceId,
            @Context Request request) {
        return processInstanceService.getIdentityLinks(processInstanceId, request);
    }

    @POST
    @Path("/process-instances/{process-instance-id}/identity-links")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response createIdentityLink(@PathParam("process-instance-id") String processInstanceId,
                                       RestIdentityLink identityLink,
                                       @Context Request request) {

        return processInstanceService.createIdentityLink(processInstanceId, identityLink, request);
    }

    @GET
    @Path("/process-instances/{process-instance-id}/identity-links/users/{identity-id}/{type}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getIdentityLink(@PathParam("process-instance-id") String processInstanceId,
                                    @PathParam("identity-id") String identityId,
                                    @PathParam("type") String type, @Context Request request) {

        return processInstanceService.getIdentityLink(processInstanceId, identityId, type, request);
    }

    @DELETE
    @Path("/process-instances/{process-instance-id}/identity-links/users/{identity-id}/{type}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response deleteIdentityLink(@PathParam("process-instance-id") String processInstanceId,
                                       @PathParam("identity-id") String identityId,
                                       @PathParam("type") String type) {

        return processInstanceService.deleteIdentityLink(processInstanceId, identityId, type);
    }

    @GET
    @Path("/process-instances/{process-instance-id}/variables")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getVariables(@PathParam("process-instance-id") String processInstanceId,
                                 @QueryParam("scope") String scope, @Context Request request) {

        return processInstanceService.getVariables(processInstanceId, scope, request);
    }

    @GET
    @Path("/process-instances/{process-instance-id}/variables/{variable-name}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getVariable(@PathParam("process-instance-id") String processInstanceId,
                                @PathParam("variable-name") String variableName,
                                @QueryParam("scope") String scope, @Context Request request) {

        return processInstanceService.getVariable(processInstanceId, variableName, scope, request);
    }

    // TODO
//    @PUT
//    @Path("/process-instances/{processInstanceId}/variables/{variableName}")
//    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
//    @Consumes(MediaType.MULTIPART_FORM_DATA)
//    public Response updateBinaryVariable(@PathParam("processInstanceId")
//                                                 String processInstanceId,
//                                         @PathParam("variableName") String variableName,
//                                         MultipartBody multipartBody) {
//
//        return processInstanceService.
//    }

    //TODO:
//    @PUT
//    @Path("/process-instances/{process-instance-id}/variables/{variable-name}")
//    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
//    @Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
//    public RestVariable updateVariable(@PathParam("process-instance-id") String processInstanceId,
//                                       @PathParam("variable-name") String variableName,
//                                       @Context HttpServletRequest httpServletRequest,
//                                       @Context Request request) {
//
//        return processInstanceService.
//    }

    // TODO
//    @POST
//    @Path("/process-instances/{processInstanceId}/variables")
//    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
//    @Consumes(MediaType.MULTIPART_FORM_DATA)
//    public Response createBinaryExecutionVariable(@PathParam("processInstanceId")
//                                                          String processInstanceId, @Context
//                                                          HttpServletRequest httpServletRequest, MultipartBody
//            multipartBody) {
//
//        return processInstanceService.
//    }

    // TODO:
//    @POST
//    @Path("/process-instances/{process-instance-id}/variables")
//    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
//    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
//    public Response createExecutionVariable(
//            @PathParam("process-instance-id") String processInstanceId,
//            @Context HttpServletRequest httpServletRequest, @Context Request request) {
//
//        return processInstanceService.
//    }

    // TODO
//    @PUT
//    @Path("/process-instances/{processInstanceId}/variables")
//    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
//    @Consumes(MediaType.MULTIPART_FORM_DATA)
//    public Response createOrUpdateBinaryExecutionVariable(@PathParam("processInstanceId")
//                                                                  String processInstanceId,
//                                                          MultipartBody multipartBody) {
//
//        return processInstanceService.
//    }

    // TODO
//    @PUT
//    @Path("/process-instances/{process-instance-id}/variables")
//    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
//    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
//    public Response createOrUpdateExecutionVariable(
//            @PathParam("process-instance-id") String processInstanceId,
//            @Context HttpServletRequest httpServletRequest, @Context Request request) {
//
//        return processInstanceService.
//    }

    // TODO
//    @GET
//    @Path("/process-instances/{process-instance-id}/variables/{variable-name}/data")
//    public Response getVariableData(@PathParam("process-instance-id") String processInstanceId,
//                                    @PathParam("variable-name") String variableName,
//                                    @Context HttpServletRequest request,
//                                    @QueryParam("scope") String scope,
//                                    @Context Request request1) {
//        return processInstanceService.
//    }

    @DELETE
    @Path("/process-instances/{process-instance-id}/variables/{variable-name}")
    public Response deleteVariable(@PathParam("process-instance-id") String processInstanceId,
                                   @PathParam("variable-name") String variableName,
                                   @QueryParam("scope") String scope,
                                   @Context Request request) {

        return processInstanceService.deleteVariable(processInstanceId, variableName, scope, request);
    }


    @GET
    @Path("/tasks")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getTasks(@Context Request currentRequest) {

        return workflowTaskService.getTasks(currentRequest);

    }

    @GET
    @Path("/tasks/{task-id}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getTask(@PathParam("task-id") String taskId, @Context Request request) {
        return workflowTaskService.getTask(taskId, request);
    }

    @PUT
    @Path("/tasks/{task-id}")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response updateTask(@PathParam("task-id") String taskId, TaskRequest taskRequest,
                               @Context Request request) {

        return workflowTaskService.updateTask(taskId, taskRequest, request);
    }

    @POST
    @Path("/tasks/{task-id}")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response executeTaskAction(@PathParam("task-id") String taskId,
                                      TaskActionRequest actionRequest) {
        return workflowTaskService.executeTaskAction(taskId, actionRequest);
    }

    @DELETE
    @Path("/tasks/{task-id}")
    public Response deleteTask(@PathParam("task-id") String taskId,
                               @DefaultValue("false") @QueryParam("cascadeHistory") Boolean cascadeHistory,
                               @DefaultValue("false") @QueryParam("deleteReason") String deleteReason) {
        return workflowTaskService.deleteTask(taskId, cascadeHistory, deleteReason);
    }

    @GET
    @Path("/tasks/{task-id}/variables")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getTaskVariables(@PathParam("task-id") String taskId,
                                     @DefaultValue("false") @QueryParam("scope") String scope,
                                     @Context Request req) {

        return workflowTaskService.getVariables(taskId, scope, req);
    }

    @GET
    @Path("/tasks/{task-id}/variables/{variable-name}")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public RestVariable getTaskVariable(@PathParam("task-id") String taskId,
                                        @PathParam("variable-name") String variableName,
                                        @DefaultValue("false") @QueryParam("scope") String scope,
                                        @Context Request request) {

        return workflowTaskService.getVariable(taskId, variableName, scope, request);
    }

    @GET
    @Path("/tasks/{task-id}/variables/{variable-name}/data")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response geTaskTaVariableData(@PathParam("task-id") String taskId,
                                         @PathParam("variable-name") String variableName,
                                         @DefaultValue("false") @QueryParam("scope") String scope,
                                         @Context Request request) {

        return workflowTaskService.getVariableData(taskId, variableName, scope, request);
    }

    // TODO
//    @POST
//    @Path("/tasks/{taskId}/variables")
//    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
//    @Consumes(MediaType.MULTIPART_FORM_DATA)
//    public Response createBinaryTaskVariable(@PathParam("taskId") String taskId,
//                                             MultipartBody multipartBody) {
//
//        return workflowTaskService.
//
//    }

    //TODO: CHECK HTTPSERVLETRequest
//    @POST
//    @Path("/tasks/{task-id}/variables")
//    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
//    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
//    public Response createTaskVariable(@PathParam("task-id") String taskId,
//                                       @Context HttpServletRequest httpServletRequest,
//                                       @Context Request request) {
//
//        return workflowTaskService.
//    }

    // TODO
//    @PUT
//    @Path("/tasks/{taskId}/variables/{variableName}")
//    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
//    @Consumes(MediaType.MULTIPART_FORM_DATA)
//    public Response updateBinaryTaskVariable(@PathParam("taskId") String taskId,
//                                             @PathParam("variableName") String variableName,
//                                             MultipartBody multipartBody) {
//        return workflowTaskService.
//    }

    // TODO
//    @PUT
//    @Path("/tasks/{task-id}/variables/{variable-name}")
//    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
//    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
//    public Response updateTaskVariable(@PathParam("task-id") String taskId,
//                                       @PathParam("variable-name") String variableName,
//                                       @Context HttpServletRequest httpServletRequest,
//                                       @Context Request request) {
//        return workflowTaskService.
//    }

    // TODO
//    @DELETE
//    @Path("/tasks/{task-id}/variables/{variable-name}")
//    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
//    public Response deleteVariable(@PathParam("task-id") String taskId,
//                                   @PathParam("variable-name") String variableName,
//                                   @QueryParam("scope") String scopeString) {
//        return workflowTaskService.
//    }

    @DELETE
    @Path("/tasks/{task-id}/variables")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response deleteAllLocalTaskVariables(@PathParam("task-id") String taskId) {
        return workflowTaskService.deleteAllLocalTaskVariables(taskId);
    }

    @GET
    @Path("/tasks/{task-id}/identity-links")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getTasksIdentityLinks(@PathParam("task-id") String taskId,
                                          @Context Request request) {
        return workflowTaskService.getIdentityLinks(taskId, request);
    }

    @GET
    @Path("/tasks/{task-id}/identity-links/{family}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getTasksIdentityLinksForFamily(@PathParam("task-id") String taskId,
                                                   @PathParam("family") String family,
                                                   @Context Request request) {

        return workflowTaskService.getIdentityLinksForFamily(taskId, family, request);
    }

    // TODO
//    @GET
//    @Path("/tasks/{task-id}/identity-links/{family}/{identity-id}/{type}")
//    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
//    public Response getIdentityLink(@PathParam("task-id") String taskId,
//                                    @PathParam("family") String family,
//                                    @PathParam("identity-id") String identityId,
//                                    @PathParam("type") String type,
//                                    @Context HttpServletRequest httpServletRequest,
//                                    @Context Request request) {
//
//        return workflowTaskService.
//    }

    @DELETE
    @Path("/tasks/{task-id}/identity-links/{family}/{identity-id}/{type}")
    public Response deleteTasksIdentityLink(@PathParam("task-id") String taskId,
                                            @PathParam("family") String family,
                                            @PathParam("identity-id") String identityId,
                                            @PathParam("type") String type) {

        return workflowTaskService.deleteIdentityLink(taskId, family, identityId, type);
    }

    @POST
    @Path("/tasks/{task-id}/identity-links")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response createTasksIdentityLink(@PathParam("task-id") String taskId,
                                            RestIdentityLink identityLink,
                                            @Context Request request) {

        return workflowTaskService.createIdentityLink(taskId, identityLink, request);
    }

    // TODO
//    @POST
//    @Path("/tasks/{taskId}/attachments")
//    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
//    @Consumes(MediaType.MULTIPART_FORM_DATA)
//    public Response createAttachmentForBinary(@PathParam("taskId") String taskId, MultipartBody
//            multipartBody,
//                                              @Context HttpServletRequest httpServletRequest) {
//
//        return workflowTaskService.
//    }
//
    // TODO:
//    @POST
//    @Path("/tasks/{task-id}/attachments")
//    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
//    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
//    public Response createAttachmentForNonBinary(@PathParam("task-id") String taskId,
//                                                 @Context HttpServletRequest httpServletRequest,
//                                                 AttachmentRequest attachmentRequest,
//                                                 @Context Request request) {
//
//        return workflowTaskService.
//    }

    @GET
    @Path("/tasks/{task-id}/attachments")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getAttachments(@PathParam("task-id") String taskId,
                                   @Context Request request) {
        return workflowTaskService.getAttachments(taskId, request);
    }

    @GET
    @Path("/tasks/{task-id}/attachments/{attachment-id}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getAttachment(@PathParam("task-id") String taskId,
                                  @PathParam("attachment-id") String attachmentId,
                                  @Context Request request) {

        return workflowTaskService.getAttachment(taskId, attachmentId, request);
    }

    @DELETE
    @Path("/tasks/{task-id}/attachments/{attachment-id}")
    public Response deleteAttachment(@PathParam("task-id") String taskId,
                                     @PathParam("attachment-id") String attachmentId) {

        return workflowTaskService.deleteAttachment(taskId, attachmentId);
    }

    @GET
    @Path("/tasks/{task-id}/attachments/{attachment-id}/content")
    public Response getAttachmentContent(@PathParam("task-id") String taskId,
                                         @PathParam("attachment-id") String attachmentId) {

        return workflowTaskService.getAttachmentContent(taskId, attachmentId);
    }

    @GET
    @Path("/tasks/{task-id}/comments")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getTasksComments(@PathParam("task-id") String taskId, @Context Request request) {

        return workflowTaskService.getComments(taskId, request);
    }

    @POST
    @Path("/tasks/{task-id}/comments")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response createTasksComment(@PathParam("task-id") String taskId, CommentRequest comment,
                                       @Context Request request) {

        return workflowTaskService.createComment(taskId, comment, request);
    }

    @GET
    @Path("/tasks/{task-id}/comments/{comment-id}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getTasksComment(@PathParam("task-id") String taskId,
                                    @PathParam("comment-id") String commentId,
                                    @Context Request request) {

        return workflowTaskService.getComment(taskId, commentId, request);
    }

    @DELETE
    @Path("/tasks/{task-id}/comments/{comment-id}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response deleteTasksComment(@PathParam("task-id") String taskId,
                                       @PathParam("comment-id") String commentId) {

        return workflowTaskService.deleteComment(taskId, commentId);
    }

    @GET
    @Path("/tasks/{task-id}/events")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getTasksEvents(@PathParam("task-id") String taskId, @Context Request request) {
        return workflowTaskService.getEvents(taskId, request);
    }

    @GET
    @Path("/tasks/{task-id}/events/{event-id}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getTasksEvent(@PathParam("task-id") String taskId,
                                  @PathParam("event-id") String eventId, @Context Request request) {

        return workflowTaskService.getEvent(taskId, eventId, request);
    }

    @DELETE
    @Path("/tasks/{task-id}/events/{event-id}")
    public Response deleteTasksEvent(@PathParam("task-id") String taskId,
                                     @PathParam("event-id") String eventId) {

        return workflowTaskService.deleteEvent(taskId, eventId);
    }


    /**
     * Get the deployed processes count
     *
     * @return a list of deployed processes with their instance count
     */
    @GET
    @Path("/process-task-services/deployed-process-count/")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public ResponseHolder getDeployedProcesses() {
        return processAndTaskService.getDeployedProcesses();

    }


    /**
     * Get the number of  processInstances with various States
     * States: Completed , Active, Suspended, Failed
     *
     * @return list with the states and the count of process instances in each state
     */
    @GET
    @Path("/process-task-services/process-status-count/")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public ResponseHolder getCountOfProcessInstanceStatus() {
        return processAndTaskService.getCountOfProcessInstanceStatus();
    }

    /**
     * Get the number of  Task Instances with various states
     * States: Completed , Active, Suspended, Failed
     *
     * @return list with the states and the count of task instances in each state
     */
    @GET
    @Path("/process-task-services/task-status-count/")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public ResponseHolder getCountOfTaskInstanceStatus() {

        return processAndTaskService.getCountOfTaskInstanceStatus();
    }

    /**
     * Get the average time duration of completed processes
     *
     * @return list with the completed processes and the average time duration taken for each process
     */
    @GET
    @Path("/process-task-services/avg-duration-to-complete-process/")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public ResponseHolder getAvgTimeDurationForCompletedProcesses() {
        return processAndTaskService.getAvgTimeDurationForCompletedProcesses();
    }

    /**
     * Average task duration for completed processes
     *
     * @param pId processDefintionId of the process selected to view the average time duration
     *            for each task
     * @return list of completed tasks with the average time duration for the selected process
     */
    @GET
    @Path("/process-task-services/avg-task-duration-for-completed-process/{p-id}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public ResponseHolder avgTaskTimeDurationForCompletedProcesses(@PathParam("p-id") String pId) {
        return processAndTaskService.avgTaskTimeDurationForCompletedProcesses(pId);
    }

    /**
     * Task variation over time i.e. tasks started and completed over the months
     *
     * @return array with the no. of tasks started and completed over the months
     */
    @GET
    @Path("/process-task-services/task-variation/")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public ResponseHolder taskVariationOverTime() {
        return processAndTaskService.taskVariationOverTime();
    }

    /**
     * Process variation over time i.e. tasks started and completed over the months
     *
     * @return array with the no. of processes started and completed over the months
     */
    @GET
    @Path("/process-task-services/process-variation/")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public ResponseHolder processVariationOverTime() {
        return processAndTaskService.processVariationOverTime();
    }

    /**
     * Get all deployed processes
     *
     * @return list with the processDefinitions of all deployed processes
     */
    @GET
    @Path("/process-task-services/all-processes/")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public ResponseHolder getAllProcesses() {
        return processAndTaskService.getAllProcesses();
    }

    /**
     * Return the no. of processes deployed
     *
     * @return list with the processDefinitions of all deployed processes
     */
    @GET
    @Path("/process-task-services/count-of-processes/")
    @Produces(MediaType.APPLICATION_JSON)
    public long getProcessCount() {
        return processAndTaskService.getProcessCount();
    }
}



