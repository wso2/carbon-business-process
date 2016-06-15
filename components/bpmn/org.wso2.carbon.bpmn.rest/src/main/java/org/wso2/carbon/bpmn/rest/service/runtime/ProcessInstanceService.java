/**
 * Copyright (c) 2015-2016 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.bpmn.rest.service.runtime;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.HistoryService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.history.HistoricVariableInstance;
import org.activiti.engine.identity.Group;
import org.activiti.engine.impl.ProcessEngineImpl;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntityManager;
import org.activiti.engine.impl.persistence.entity.VariableInstanceEntity;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.repository.ProcessDefinitionQuery;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.IdentityLink;
import org.activiti.image.ProcessDiagramGenerator;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.bpmn.core.integration.BPSGroupIdentityManager;
import org.wso2.carbon.bpmn.rest.common.CorrelationProcess;
import org.wso2.carbon.bpmn.rest.common.RestResponseFactory;
import org.wso2.carbon.bpmn.rest.common.exception.RestApiBasicAuthenticationException;
import org.wso2.carbon.bpmn.rest.engine.variable.QueryVariable;
import org.wso2.carbon.bpmn.rest.engine.variable.RestVariable;
import org.wso2.carbon.bpmn.rest.internal.RestServiceContentHolder;
import org.wso2.carbon.bpmn.rest.model.common.DataResponse;
import org.wso2.carbon.bpmn.rest.model.common.RestIdentityLink;
import org.wso2.carbon.bpmn.rest.model.correlation.CorrelationActionRequest;
import org.wso2.carbon.bpmn.rest.model.runtime.ProcessInstanceActionRequest;
import org.wso2.carbon.bpmn.rest.model.runtime.ProcessInstanceCreateRequest;
import org.wso2.carbon.bpmn.rest.model.runtime.ProcessInstanceQueryRequest;
import org.wso2.carbon.bpmn.rest.model.runtime.ProcessInstanceResponse;
import org.wso2.carbon.bpmn.rest.model.runtime.RestVariableCollection;
import org.wso2.carbon.bpmn.rest.service.base.BaseProcessInstanceService;
import org.wso2.msf4j.Request;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 *
 */
//@Component(
//        name = "org.wso2.carbon.bpmn.rest.service.runtime.ProcessInstanceService",
//        service = Microservice.class,
//        immediate = true)
//@Path("/process-instances")
public class ProcessInstanceService extends BaseProcessInstanceService { //implements Microservice {

    private static final Logger log = LoggerFactory.getLogger(ProcessInstanceService.class);


    public Response getProcessInstances(Request request) {

        Map<String, String> allRequestParams = allRequestParams(request);

        // Populate query based on request
        ProcessInstanceQueryRequest queryRequest = getQueryRequest(allRequestParams);

        return Response.ok()
                .entity(getQueryResponse(queryRequest, allRequestParams, request.getUri()))
                .build();
    }

    public Response startInstance(ProcessInstanceCreateRequest processInstanceCreateRequest, Request request) {

        if (log.isDebugEnabled()) {
            log.debug("ProcessInstanceCreateRequest:" +
                    processInstanceCreateRequest.getProcessDefinitionId());
            log.debug(" processInstanceCreateRequest.getVariables().size():" +
                    processInstanceCreateRequest.getVariables().size());
        }

        if (processInstanceCreateRequest.getProcessDefinitionId() == null &&
                processInstanceCreateRequest.getProcessDefinitionKey() == null &&
                processInstanceCreateRequest.getMessage() == null) {
            throw new ActivitiIllegalArgumentException(
                    "Either processDefinitionId, processDefinitionKey or message is required.");
        }

        int paramsSet = ((processInstanceCreateRequest.getProcessDefinitionId() != null) ? 1 : 0) +
                ((processInstanceCreateRequest.getProcessDefinitionKey() != null) ? 1 : 0) +
                ((processInstanceCreateRequest.getMessage() != null) ? 1 : 0);

        if (paramsSet > 1) {
            throw new ActivitiIllegalArgumentException(
                    "Only one of processDefinitionId, processDefinitionKey or message should be set.");
        }

        if (processInstanceCreateRequest.isCustomTenantSet()) {
            // Tenant-id can only be used with either key or message
            if (processInstanceCreateRequest.getProcessDefinitionId() != null) {
                throw new ActivitiIllegalArgumentException(
                        "TenantId can only be used with either processDefinitionKey or message.");
            }
        } else {
            if (processInstanceCreateRequest.getProcessDefinitionKey() == null &&
                    processInstanceCreateRequest.getMessage() == null) {
                throw new ActivitiIllegalArgumentException(
                        "TenantId should be specified to be used with either " +
                                "processDefinitionKey or message.");
            }
        }

        //Have to add the validation part here
        if (!isValidUserToStartProcess(processInstanceCreateRequest)) {
            throw new RestApiBasicAuthenticationException(
                    "User doesn't have the necessary permission to start the process");
        }

        if (processInstanceCreateRequest.getSkipInstanceCreationIfExist()) {
            ProcessInstanceQueryRequest processInstanceQueryRequest =
                    processInstanceCreateRequest.cloneInstanceCreationRequest();
            Map<String, String> allRequestParams = allRequestParams(request);
            DataResponse dataResponse =
                    getQueryResponse(processInstanceQueryRequest, allRequestParams,
                            request.getUri());

            if (log.isDebugEnabled()) {
                log.debug("ProcessInstanceCreation check:" + dataResponse.getSize());
            }

            int dataResponseSize = dataResponse.getSize();

            if (dataResponseSize > 0) {

                if (processInstanceCreateRequest.getCorrelate()) {

                    if (dataResponseSize != 1) {
                        String responseMessage =
                                "Join correlation failed as there are more than one instances with " +
                                        "given variables state";
                        throw new NotFoundException(Response.ok().entity(responseMessage)
                                .status(Response.Status.NOT_FOUND)
                                .build());
                    }

                    //process the correlation aspect now

                    if (processInstanceCreateRequest.getMessageName() == null) {
                        String responseMessage =
                                "Join correlation failed as message name has not been mentioned";
                        throw new ActivitiIllegalArgumentException(responseMessage);
                    }

                    return performCorrelation(processInstanceCreateRequest, request.getUri());
                } else {
                    dataResponse.setMessage("Returned the existing instances");
                    return Response.ok().entity(dataResponse).build();
                }
            }

        }

        RestResponseFactory restResponseFactory = new RestResponseFactory();

        Map<String, Object> startVariables = null;
        if (processInstanceCreateRequest.getVariables() != null) {
            startVariables = new HashMap<>();
            for (RestVariable variable : processInstanceCreateRequest.getVariables()) {
                if (variable.getName() == null) {
                    throw new ActivitiIllegalArgumentException("Variable name is required.");
                }
                startVariables
                        .put(variable.getName(), restResponseFactory.getVariableValue(variable));
            }
        }

        //updated the additional variables
        if (processInstanceCreateRequest.getAdditionalVariables() != null) {
            if (startVariables == null) {
                startVariables = new HashMap<>();
            }
            for (RestVariable variable : processInstanceCreateRequest.getAdditionalVariables()) {
                if (variable.getName() == null) {
                    throw new ActivitiIllegalArgumentException(
                            "Additional Variable name is required.");
                }
                startVariables
                        .put(variable.getName(), restResponseFactory.getVariableValue(variable));
            }
        }

        RuntimeService runtimeService = RestServiceContentHolder.getInstance().getRestService().getRumtimeService();

        ProcessInstanceResponse processInstanceResponse;
        // Actually start the instance based on key or id
        try {
            ProcessInstance instance;
            if (processInstanceCreateRequest.getProcessDefinitionId() != null) {
                instance = runtimeService.startProcessInstanceById(
                        processInstanceCreateRequest.getProcessDefinitionId(),
                        processInstanceCreateRequest.getBusinessKey(), startVariables);
            } else if (processInstanceCreateRequest.getProcessDefinitionKey() != null) {
                if (processInstanceCreateRequest.isCustomTenantSet()) {
                    instance = runtimeService.startProcessInstanceByKeyAndTenantId(
                            processInstanceCreateRequest.getProcessDefinitionKey(),
                            processInstanceCreateRequest.getBusinessKey(), startVariables,
                            processInstanceCreateRequest.getTenantId());
                } else {
                    instance = runtimeService.startProcessInstanceByKey(
                            processInstanceCreateRequest.getProcessDefinitionKey(),
                            processInstanceCreateRequest.getBusinessKey(), startVariables);
                }
            } else {
                if (processInstanceCreateRequest.isCustomTenantSet()) {
                    instance = runtimeService.startProcessInstanceByMessageAndTenantId(
                            processInstanceCreateRequest.getMessage(),
                            processInstanceCreateRequest.getBusinessKey(), startVariables,
                            processInstanceCreateRequest.getTenantId());
                } else {
                    instance = runtimeService.startProcessInstanceByMessage(
                            processInstanceCreateRequest.getMessage(),
                            processInstanceCreateRequest.getBusinessKey(), startVariables);
                }
            }

            HistoryService historyService = RestServiceContentHolder.getInstance().getRestService().getHistoryService();

            if (processInstanceCreateRequest.getReturnVariables()) {
                Map<String, Object> runtimeVariableMap = null;
                List<HistoricVariableInstance> historicVariableList = null;
                if (instance.isEnded()) {
                    historicVariableList = historyService.createHistoricVariableInstanceQuery()
                            .processInstanceId(instance.getId())
                            .list();
                } else {
                    runtimeVariableMap = runtimeService.getVariables(instance.getId());
                }
                processInstanceResponse = restResponseFactory
                        .createProcessInstanceResponse(instance, true, runtimeVariableMap,
                                historicVariableList, request.getUri());

            } else {
                processInstanceResponse = restResponseFactory
                        .createProcessInstanceResponse(instance, request.getUri());
            }

        } catch (ActivitiObjectNotFoundException aonfe) {
            throw new ActivitiIllegalArgumentException(aonfe.getMessage(), aonfe);
        }

        return Response.ok().status(Response.Status.CREATED).entity(processInstanceResponse)
                .build();
    }


    public Response getProcessInstanceDiagram(String processInstanceId) {
        ProcessInstance processInstance = getProcessInstanceFromRequest(processInstanceId);

        RepositoryService repositoryService = RestServiceContentHolder.getInstance().getRestService()
                .getRepositoryService();
        ProcessDefinition pde =
                repositoryService.getProcessDefinition(processInstance.getProcessDefinitionId());

        if (pde != null && pde.hasGraphicalNotation()) {
            BpmnModel bpmnModel = repositoryService.getBpmnModel(pde.getId());
            ProcessEngineConfiguration processEngineConfiguration =
                    RestServiceContentHolder.getInstance().getRestService().getProcessEngineConfiguration();
            RuntimeService runtimeService = RestServiceContentHolder.getInstance().getRestService().getRumtimeService();

            ProcessDiagramGenerator diagramGenerator =
                    processEngineConfiguration.getProcessDiagramGenerator();

            try (InputStream resource = diagramGenerator.generateDiagram(bpmnModel, "png", runtimeService
                            .getActiveActivityIds(
                                    processInstance
                                            .getId()),
                    Collections.<String>emptyList(),
                    processEngineConfiguration
                            .getActivityFontName(),
                    processEngineConfiguration
                            .getLabelFontName(),
                    processEngineConfiguration
                            .getClassLoader(), 1.0)) {
                return Response.ok().type("image/png").entity(IOUtils.toByteArray(resource))
                        .build();
            } catch (Exception e) {
                throw new ActivitiIllegalArgumentException("Error exporting diagram", e);
            }

        } else {
            throw new ActivitiIllegalArgumentException(
                    "Process instance with id '" + processInstance.getId() +
                            "' has no graphical notation defined.");
        }
    }

    public Response getProcessInstance(String processInstanceId, Request request) {

        ProcessInstance processInstance = getProcessInstanceFromRequest(processInstanceId);
        RestResponseFactory restResponseFactory = new RestResponseFactory();
        ProcessInstanceResponse processInstanceResponse = restResponseFactory
                .createProcessInstanceResponse(processInstance, request.getUri());

        return Response.ok().entity(processInstanceResponse).build();
    }


    public Response deleteProcessInstance(String processInstanceId, String deleteReason) {

        RuntimeService runtimeService = RestServiceContentHolder.getInstance().getRestService().getRumtimeService();
        ProcessInstance processInstance = getProcessInstanceFromRequest(processInstanceId);

        runtimeService.deleteProcessInstance(processInstance.getId(), deleteReason);
        return Response.ok().status(Response.Status.NO_CONTENT).build();
    }


    public Response performProcessInstanceAction(String processInstanceId, ProcessInstanceActionRequest
            actionRequest, Request request) {

        ProcessInstance processInstance = getProcessInstanceFromRequest(processInstanceId);

        RestResponseFactory restResponseFactory = new RestResponseFactory();

        if (ProcessInstanceActionRequest.ACTION_ACTIVATE.equals(actionRequest.getAction())) {
            return Response.ok().entity(activateProcessInstance(processInstance, request.getUri()))
                    .build();

        } else if (ProcessInstanceActionRequest.ACTION_SUSPEND.equals(actionRequest.getAction())) {
            return Response.ok().entity(suspendProcessInstance(processInstance, restResponseFactory,
                    request.getUri())).build();
        }
        throw new ActivitiIllegalArgumentException(
                "Invalid action: '" + actionRequest.getAction() + "'.");
    }


    public List<RestIdentityLink> getIdentityLinks(String processInstanceId, Request request) {
        RuntimeService runtimeService = RestServiceContentHolder.getInstance().getRestService().getRumtimeService();
        ProcessInstance processInstance = getProcessInstanceFromRequest(processInstanceId);
        return new RestResponseFactory().createRestIdentityLinks(
                runtimeService.getIdentityLinksForProcessInstance(processInstance.getId()),
                request.getUri());
    }

    public Response createIdentityLink(String processInstanceId, RestIdentityLink identityLink, Request request) {

        ProcessInstance processInstance = getProcessInstanceFromRequest(processInstanceId);

        if (identityLink.getGroup() != null) {
            throw new ActivitiIllegalArgumentException(
                    "Only user identity links are supported on a process instance.");
        }

        if (identityLink.getUser() == null) {
            throw new ActivitiIllegalArgumentException("The user is required.");
        }

        if (identityLink.getType() == null) {
            throw new ActivitiIllegalArgumentException("The identity link type is required.");
        }

        RuntimeService runtimeService = RestServiceContentHolder.getInstance().getRestService().getRumtimeService();
        runtimeService.addUserIdentityLink(processInstance.getId(), identityLink.getUser(),
                identityLink.getType());

        RestIdentityLink restIdentityLink = new RestResponseFactory()
                .createRestIdentityLink(identityLink.getType(), identityLink.getUser(),
                        identityLink.getGroup(), null, null,
                        processInstance.getId(), request.getUri());
        return Response.ok().status(Response.Status.CREATED).entity(restIdentityLink).build();
    }

    public Response getIdentityLink(String processInstanceId, String identityId, String type, Request request) {

        ProcessInstance processInstance = getProcessInstanceFromRequest(processInstanceId);

        validateIdentityLinkArguments(identityId, type);

        IdentityLink link = getIdentityLink(identityId, type, processInstance.getId());

        return Response.ok().entity(new RestResponseFactory()
                .createRestIdentityLink(link, request.getUri()))
                .build();
    }


    public Response deleteIdentityLink(String processInstanceId, String identityId, String type) {

        ProcessInstance processInstance = getProcessInstanceFromRequest(processInstanceId);

        validateIdentityLinkArguments(identityId, type);

        getIdentityLink(identityId, type, processInstance.getId());
        RuntimeService runtimeService = RestServiceContentHolder.getInstance().getRestService().getRumtimeService();
        runtimeService.deleteUserIdentityLink(processInstance.getId(), identityId, type);
        return Response.ok().status(Response.Status.NO_CONTENT).build();
    }


    public Response getVariables(String processInstanceId, String scope, Request request) {

        Execution execution = getExecutionInstanceFromRequest(processInstanceId);
        List<RestVariable> restVariableList =
                processVariables(execution, scope, RestResponseFactory.VARIABLE_PROCESS,
                        request.getUri());
        RestVariableCollection restVariableCollection = new RestVariableCollection();
        restVariableCollection.setRestVariables(restVariableList);
        return Response.ok().entity(restVariableCollection).build();
    }

    public Response getVariable(String processInstanceId, String variableName, String scope, Request request) {

        Execution execution = getExecutionInstanceFromRequest(processInstanceId);
        RestVariable restVariable =
                getVariableFromRequest(execution, variableName, scope, false, request.getUri());
        return Response.ok().entity(restVariable).build();
    }

    /* TODO
        @PUT
        @Path("/{processInstanceId}/variables/{variableName}")
        @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
        @Consumes(MediaType.MULTIPART_FORM_DATA)
        public Response updateBinaryVariable(@PathParam("processInstanceId")
        String processInstanceId,
                                             @PathParam("variableName") String variableName,
                                             MultipartBody multipartBody) {

            Execution execution = getExecutionInstanceFromRequest(processInstanceId);

            RestVariable result;
            try {
                result = setBinaryVariable(multipartBody, execution, RestResponseFactory.
                VARIABLE_PROCESS, false);
            } catch (IOException | ServletException e) {
                throw new BPMNRestException("Exception occured during creating binary
                execution variable", e);
            }

            return Response.ok().entity(result).build();
        }
    */ //TODO:
//    @PUT
//    @Path("/{process-instance-id}/variables/{variable-name}")
//    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
//    @Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
//    public RestVariable updateVariable(@PathParam("process-instance-id") String processInstanceId,
//                                       @PathParam("variable-name") String variableName,
//                                       @Context HttpServletRequest httpServletRequest,
//                                       @Context Request request) {
//
//        Execution execution = getExecutionInstanceFromRequest(processInstanceId);
//
//        RestVariable restVariable = null;
//
//        String contentType = httpServletRequest.getContentType();
//        if (MediaType.APPLICATION_JSON.equals(contentType)) {
//            try {
//                restVariable = new ObjectMapper()
//                        .readValue(httpServletRequest.getInputStream(), RestVariable.class);
//            } catch (IOException e) {
//                throw new ActivitiIllegalArgumentException(
//                        "request body could not be transformed to a RestVariable " + "instance.",
//                        e);
//            }
//        } else if (MediaType.APPLICATION_XML.equals(MediaType.APPLICATION_JSON)) {
//
//            JAXBContext jaxbContext;
//            try {
//                jaxbContext = JAXBContext.newInstance(RestVariable.class);
//                Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
//                restVariable = (RestVariable) jaxbUnmarshaller
//                        .unmarshal(httpServletRequest.getInputStream());
//
//            } catch (JAXBException | IOException e) {
//                throw new ActivitiIllegalArgumentException(
//                        "xml request body could not be transformed to a " +
//                        "RestVariable " +
//                        "instance.", e);
//            }
//        }
//
//        if (restVariable == null) {
//            throw new ActivitiException("Invalid body was supplied");
//        }
//        if (!restVariable.getName().equals(variableName)) {
//            throw new ActivitiIllegalArgumentException(
//                    "Variable name in the body should be equal to the name used in the requested URL"
//                    + ".");
//        }
//        // }
//
//        return setSimpleVariable(restVariable, execution, false, request.getUri());
//    }

    /* TODO
        @POST
        @Path("/{processInstanceId}/variables")
        @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
        @Consumes(MediaType.MULTIPART_FORM_DATA)
        public Response createBinaryExecutionVariable(@PathParam("processInstanceId")
         String processInstanceId, @Context
        HttpServletRequest httpServletRequest, MultipartBody multipartBody) {

            Execution execution = getExecutionInstanceFromRequest(processInstanceId);
            Response response;
            try {
                response = createBinaryExecutionVariable(execution, false, RestResponseFactory
                        .VARIABLE_PROCESS, multipartBody);
            } catch (IOException | ServletException e) {
                throw new BPMNRestException("Exception occured during creating binary
                execution variable", e);
            }
            return response;
        }
    */
    ///TODO:
//    @POST
//    @Path("/{process-instance-id}/variables")
//    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
//    @Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
//    public Response createExecutionVariable(
//            @PathParam("process-instance-id") String processInstanceId,
//            @Context HttpServletRequest httpServletRequest, @Context Request request) {
//
//        Execution execution = getExecutionInstanceFromRequest(processInstanceId);
//        Response response;
//        try {
//            response =
//                    createExecutionVariable(execution, false, RestResponseFactory.VARIABLE_PROCESS,
//                                            httpServletRequest, request);
//        } catch (IOException | ServletException e) {
//            throw new BPMNRestException("Exception occured during creating execution variable", e);
//        }
//        return response;
//    }
/*TODO
    @PUT
    @Path("/{processInstanceId}/variables")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response createOrUpdateBinaryExecutionVariable(@PathParam("processInstanceId")
     String processInstanceId,
                                                          MultipartBody multipartBody) {

        Execution execution = getExecutionInstanceFromRequest(processInstanceId);
        Response response;
        try {
            response = createBinaryExecutionVariable(execution, true, RestResponseFactory.
            VARIABLE_PROCESS,
                    multipartBody);
        } catch (IOException | ServletException e) {
            throw new BPMNRestException("Exception occured during creating execution variable", e);
        }
        return response;
    }
*/
//todo:
//    @PUT
//    @Path("/{process-instance-id}/variables")
//    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
//    @Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
//    public Response createOrUpdateExecutionVariable(
//            @PathParam("process-instance-id") String processInstanceId,
//            @Context HttpServletRequest httpServletRequest, @Context Request request) {
//
//        Execution execution = getExecutionInstanceFromRequest(processInstanceId);
//        Object result;
//        try {
//            result = createExecutionVariable(execution, true, RestResponseFactory.VARIABLE_PROCESS,
//                                             httpServletRequest, request);
//        } catch (IOException | ServletException e) {
//            throw new BPMNRestException("Exception occured during creating execution variable", e);
//        }
//        return Response.ok().status(Response.Status.CREATED).entity(result).build();
//    }
//
//    @GET
//    @Path("/{process-instance-id}/variables/{variable-name}/data")
//    public Response getVariableData(@PathParam("process-instance-id") String processInstanceId,
//                                    @PathParam("variable-name") String variableName,
//                                    @Context HttpServletRequest request,
//                                    @QueryParam("scope") String scope,
//                                    @Context Request request1) {
//
//        Execution execution = getExecutionInstanceFromRequest(processInstanceId);
//        Response.ResponseBuilder responseBuilder = Response.ok();
//        return responseBuilder
//                .entity(getVariableDataByteArray(execution, variableName, scope, responseBuilder,
//                                                 request1.getUri())).build();
//    }


    public Response deleteVariable(String processInstanceId, String variableName, String scope, Request request) {

        Execution execution = getExecutionInstanceFromRequest(processInstanceId);
        // Determine scope
        RestVariable.RestVariableScope variableScope = RestVariable.RestVariableScope.LOCAL;
        if (scope != null) {
            variableScope = RestVariable.getScopeFromString(scope);
        }

        if (!hasVariableOnScope(execution, variableName, variableScope)) {
            throw new ActivitiObjectNotFoundException(
                    "Execution '" + execution.getId() + "' doesn't have a variable '" +
                            variableName + "' in scope " +
                            variableScope.name().toLowerCase(Locale.getDefault()),
                    VariableInstanceEntity.class);
        }

        RuntimeService runtimeService = RestServiceContentHolder.getInstance().getRestService().getRumtimeService();

        if (variableScope == RestVariable.RestVariableScope.LOCAL) {
            runtimeService.removeVariableLocal(execution.getId(), variableName);
        } else {
            // Safe to use parentId, as the hasVariableOnScope would have stopped a global-var
            // update on a root-execution
            runtimeService.removeVariable(execution.getParentId(), variableName);
        }

        return Response.ok().status(Response.Status.NO_CONTENT).build();
    }

    protected byte[] getVariableDataByteArray(Execution execution, String variableName,
                                              String scope,
                                              Response.ResponseBuilder responseBuilder,
                                              String baseName) {

        try {
            byte[] result;

            RestVariable variable =
                    getVariableFromRequest(execution, variableName, scope, true, baseName);
            if (RestResponseFactory.BYTE_ARRAY_VARIABLE_TYPE.equals(variable.getType())) {
                result = (byte[]) variable.getValue();
                responseBuilder.type(MediaType.APPLICATION_OCTET_STREAM);
            } else if (RestResponseFactory.SERIALIZABLE_VARIABLE_TYPE.equals(variable.getType())) {
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                ObjectOutputStream outputStream = new ObjectOutputStream(buffer);
                outputStream.writeObject(variable.getValue());
                outputStream.close();
                result = buffer.toByteArray();
                responseBuilder.type("application/x-java-serialized-object");
            } else {
                throw new ActivitiObjectNotFoundException(
                        "The variable does not have a binary data stream.", null);
            }
            return result;

        } catch (IOException ioe) {
            throw new ActivitiException("Error getting variable " + variableName, ioe);
        }
    }

    protected RestVariable setSimpleVariable(RestVariable restVariable, Execution execution,
                                             boolean isNew, String baseContext) {
        if (restVariable.getName() == null) {
            throw new ActivitiIllegalArgumentException("Variable name is required");
        }

        // Figure out scope, revert to local is omitted
        RestVariable.RestVariableScope scope = restVariable.getVariableScope();
        if (scope == null) {
            scope = RestVariable.RestVariableScope.LOCAL;
        }

        Object actualVariableValue = new RestResponseFactory().getVariableValue(restVariable);
        setVariable(execution, restVariable.getName(), actualVariableValue, scope, isNew);

        return constructRestVariable(restVariable.getName(), actualVariableValue, scope,
                execution.getId(), false, baseContext);
    }

    /*TODO
        protected Response createBinaryExecutionVariable(Execution execution, boolean override,
         int variableType,
                                                         MultipartBody multipartBody) throws
                IOException, ServletException {

            Response.ResponseBuilder responseBuilder = Response.ok();
            Object result = setBinaryVariable(multipartBody, execution, variableType, !override);
            responseBuilder.entity(result);
            return responseBuilder.status(Response.Status.CREATED).build();
        }
    *////TODO:
//    protected Response createExecutionVariable(Execution execution, boolean override,
//                                               int variableType,
//                                               HttpServletRequest httpServletRequest,
//                                               @Context Request request)
//            throws IOException, ServletException {
//
//        boolean debugEnabled = log.isDebugEnabled();
//        if (debugEnabled) {
//            log.debug("httpServletRequest.getContentType():" + httpServletRequest.getContentType());
//        }
//        Response.ResponseBuilder responseBuilder = Response.ok();
//
//        if (debugEnabled) {
//            log.debug("Processing non binary variable");
//        }
//
//        List<RestVariable> inputVariables = new ArrayList<>();
//        List<RestVariable> resultVariables = new ArrayList<>();
//
//        String contentType = httpServletRequest.getContentType();
//
//        // try {
//        if (MediaType.APPLICATION_JSON.equals(contentType)) {
//            ObjectMapper objectMapper = new ObjectMapper();
//            @SuppressWarnings("unchecked") List<Object> variableObjects =
//                    (List<Object>) objectMapper
//                            .readValue(httpServletRequest.getInputStream(), List.class);
//            for (Object restObject : variableObjects) {
//                RestVariable restVariable =
//                        objectMapper.convertValue(restObject, RestVariable.class);
//                inputVariables.add(restVariable);
//            }
//        } else if (MediaType.APPLICATION_XML.equals(contentType)) {
//            JAXBContext jaxbContext;
//            try {
//                jaxbContext = JAXBContext.newInstance(RestVariableCollection.class);
//                Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
//                RestVariableCollection restVariableCollection =
//                        (RestVariableCollection) jaxbUnmarshaller
//                                .unmarshal(httpServletRequest.getInputStream());
//                if (restVariableCollection == null) {
//                    throw new ActivitiIllegalArgumentException(
//                            "xml request body could not be transformed to a " +
//                            "RestVariable Collection instance.");
//                }
//                List<RestVariable> restVariableList = restVariableCollection.getRestVariables();
//
//                if (restVariableList.size() == 0) {
//                    throw new ActivitiIllegalArgumentException(
//                            "xml request body could not identify any rest " +
//                            "variables to be updated");
//                }
//                for (RestVariable restVariable : restVariableList) {
//                    inputVariables.add(restVariable);
//                }
//
//            } catch (JAXBException | IOException e) {
//                throw new ActivitiIllegalArgumentException(
//                        "xml request body could not be transformed to a " +
//                        "RestVariable instance.", e);
//            }
//        }
       /* } catch (Exception e) {
            throw new ActivitiIllegalArgumentException(
                    "Failed to serialize to a RestVariable instance", e);
        }*/

//        if (inputVariables.size() == 0) {
//            throw new ActivitiIllegalArgumentException(
//                    "Request didn't contain a list of variables to create.");
//        }
//
//        RestVariable.RestVariableScope sharedScope = null;
//        RestVariable.RestVariableScope varScope;
//        Map<String, Object> variablesToSet = new HashMap<>();
//
//        for (RestVariable var : inputVariables) {
//            // Validate if scopes match
//            varScope = var.getVariableScope();
//            if (var.getName() == null) {
//                throw new ActivitiIllegalArgumentException("Variable name is required");
//            }
//
//            if (varScope == null) {
//                varScope = RestVariable.RestVariableScope.LOCAL;
//            }
//            if (sharedScope == null) {
//                sharedScope = varScope;
//            }
//            if (varScope != sharedScope) {
//                throw new ActivitiIllegalArgumentException(
//                        "Only allowed to update multiple variables in the same scope.");
//            }
//
//            if (!override && hasVariableOnScope(execution, var.getName(), varScope)) {
//                throw new BPMNConflictException(
//                        "Variable '" + var.getName() + "' is already present on execution '" +
//                        execution.getId() + "'.");
//            }
//
//            RestResponseFactory restResponseFactory = new RestResponseFactory();
//            Object actualVariableValue = restResponseFactory.getVariableValue(var);
//            variablesToSet.put(var.getName(), actualVariableValue);
//            resultVariables.add(restResponseFactory
//                                        .createRestVariable(var.getName(), actualVariableValue,
//                                                            varScope, execution.getId(),
//                                                            variableType, false, request.getUri()));
//        }
//
//        if (!variablesToSet.isEmpty()) {
//            RuntimeService runtimeService = BPMNRestServiceImpl.getRumtimeService();
//
//            if (sharedScope == RestVariable.RestVariableScope.LOCAL) {
//                runtimeService.setVariablesLocal(execution.getId(), variablesToSet);
//            } else {
//                if (execution.getParentId() != null) {
//                    // Explicitly set on parent, setting non-local variables on execution
//                    // itself will override local-variables if exists
//                    runtimeService.setVariables(execution.getParentId(), variablesToSet);
//                } else {
//                    // Standalone task, no global variables possible
//                    throw new ActivitiIllegalArgumentException(
//                            "Cannot set global variables on execution '" + execution.getId() +
//                            "', task is not part of process.");
//                }
//            }
//        }
//
//        RestVariableCollection restVariableCollection = new RestVariableCollection();
//        restVariableCollection.setRestVariables(resultVariables);
//        responseBuilder.entity(restVariableCollection);
//        // }
//        return responseBuilder.status(Response.Status.CREATED).build();
//    }

    /* TODO
        protected RestVariable setBinaryVariable(MultipartBody multipartBody, Execution execution,
                                                 int responseVariableType, boolean isNew) throws
                IOException, ServletException {

            boolean debugEnabled = log.isDebugEnabled();
            List<org.apache.cxf.jaxrs.ext.multipart.Attachment> attachments = multipartBody.
            getAllAttachments();

            int attachmentSize = attachments.size();

            if (attachmentSize <= 0) {
                throw new ActivitiIllegalArgumentException("No Attachments found with the
                request body");
            }
            AttachmentDataHolder attachmentDataHolder = new AttachmentDataHolder();

            for (int i = 0; i < attachmentSize; i++) {
                org.apache.cxf.jaxrs.ext.multipart.Attachment attachment = attachments.get(i);

                String contentDispositionHeaderValue = attachment.getHeader("Content-Disposition");
                String contentType = attachment.getHeader("Content-Type");

                if (debugEnabled) {
                    log.debug("Going to iterate:" + i);
                    log.debug("contentDisposition:" + contentDispositionHeaderValue);
                }

                if (contentDispositionHeaderValue != null) {
                    contentDispositionHeaderValue = contentDispositionHeaderValue.trim();

                    Map<String, String> contentDispositionHeaderValueMap = Utils.
                    processContentDispositionHeader
                            (contentDispositionHeaderValue);
                    String dispositionName = contentDispositionHeaderValueMap.get("name");
                    DataHandler dataHandler = attachment.getDataHandler();

                    OutputStream outputStream;

                    if ("name".equals(dispositionName)) {
                        try {
                            outputStream = Utils.getAttachmentStream(dataHandler.getInputStream());
                        } catch (IOException e) {
                            throw new ActivitiIllegalArgumentException("Attachment
                            Name Reading error occured", e);
                        }

                        if (outputStream != null) {
                            String fileName = outputStream.toString();
                            attachmentDataHolder.setName(fileName);
                        }


                    } else if ("type".equals(dispositionName)) {
                        try {
                            outputStream = Utils.getAttachmentStream(dataHandler.getInputStream());
                        } catch (IOException e) {
                            throw new ActivitiIllegalArgumentException("Attachment
                             Type Reading error occured", e);
                        }

                        if (outputStream != null) {
                            String typeName = outputStream.toString();
                            attachmentDataHolder.setType(typeName);
                        }


                    } else if ("scope".equals(dispositionName)) {
                        try {
                            outputStream = Utils.getAttachmentStream(dataHandler.getInputStream());
                        } catch (IOException e) {
                            throw new ActivitiIllegalArgumentException("Attachment
                             Description Reading error occured", e);
                        }

                        if (outputStream != null) {
                            String scope = outputStream.toString();
                            attachmentDataHolder.setScope(scope);
                        }
                    }
                    if (contentType != null) {
                        if ("file".equals(dispositionName)) {

                            InputStream inputStream;
                            try {
                                inputStream = dataHandler.getInputStream();
                            } catch (IOException e) {
                                throw new ActivitiIllegalArgumentException("Error
                                Occured During processing empty body.",
                                        e);
                            }

                            if (inputStream != null) {
                                attachmentDataHolder.setContentType(contentType);
                                byte[] attachmentArray = new byte[0];
                                try {
                                    attachmentArray = IOUtils.toByteArray(inputStream);
                                } catch (IOException e) {
                                    throw new ActivitiIllegalArgumentException("Processing
                                    Attachment Body Failed.", e);
                                }
                                attachmentDataHolder.setAttachmentArray(attachmentArray);
                            }
                        }
                    }
                }
            }


            attachmentDataHolder.printDebug();

            String variableScope = attachmentDataHolder.getScope();
            String variableName = attachmentDataHolder.getName();
            String variableType = attachmentDataHolder.getType();

            if (attachmentDataHolder.getName() == null) {
                throw new ActivitiIllegalArgumentException("Attachment name is required.");
            }

            if (attachmentDataHolder.getAttachmentArray() == null) {
                throw new ActivitiIllegalArgumentException("Empty attachment body was
                 found in request body after " +
                        "decoding the request" +
                        ".");
            }

            if (debugEnabled) {
                log.debug("variableScope:" + variableScope + " variableName:" + variableName +
                " variableType:" + variableType);
            }

            try {

                // Validate input and set defaults
                if (variableName == null) {
                    throw new ActivitiIllegalArgumentException("No variable name was found
                     in request body.");
                }

                if (variableType != null) {
                    if (!RestResponseFactory.BYTE_ARRAY_VARIABLE_TYPE.equals(variableType) &&
                     !RestResponseFactory.SERIALIZABLE_VARIABLE_TYPE.equals(variableType)) {
                        throw new ActivitiIllegalArgumentException("Only 'binary' and
                         'serializable' are supported as variable type.");
                    }
                } else {
                    variableType = RestResponseFactory.BYTE_ARRAY_VARIABLE_TYPE;
                }

                RestVariable.RestVariableScope scope = RestVariable.RestVariableScope.LOCAL;
                if (variableScope != null) {
                    scope = RestVariable.getScopeFromString(variableScope);
                }

                if (variableType.equals(RestResponseFactory.BYTE_ARRAY_VARIABLE_TYPE)) {
                    // Use raw bytes as variable value
                    setVariable(execution, variableName, attachmentDataHolder.getAttachmentArray(),
                     scope, isNew);

                } else {
                    // Try deserializing the object
                    InputStream inputStream = new ByteArrayInputStream(attachmentDataHolder.
                    getAttachmentArray());
                    ObjectInputStream stream = new ObjectInputStream(inputStream);
                    Object value = stream.readObject();
                    setVariable(execution, variableName, value, scope, isNew);
                    stream.close();
                }

                RestResponseFactory restResponseFactory = new RestResponseFactory();
                if (responseVariableType == RestResponseFactory.VARIABLE_PROCESS) {

                    return new RestResponseFactory().createBinaryRestVariable(variableName,
                     scope, variableType,
                            null, null, execution.getId(), uriInfo.getBaseUri().toString());
                } else {
                    return restResponseFactory.createBinaryRestVariable(variableName, scope,
                     variableType, null,
                            execution.getId(), null, uriInfo.getBaseUri().toString());
                }

            } catch (IOException ioe) {
                throw new ActivitiIllegalArgumentException("Could not process multipart content",
                 ioe);
            } catch (ClassNotFoundException ioe) {
                throw new BPMNContentNotSupportedException("The provided body contains a
                serialized object for which the " +
                        "class is not found: " + ioe.getMessage());
            }

        }
    */
    protected void setVariable(Execution execution, String name, Object value,
                               RestVariable.RestVariableScope scope, boolean isNew) {
        // Create can only be done on new variables. Existing variables should be updated using PUT
        if (log.isDebugEnabled()) {
            log.debug("Going to invoke has variable from set binary variable");
        }

        boolean hasVariable = hasVariableOnScope(execution, name, scope);
        if (isNew && hasVariable) {
            throw new ActivitiException(
                    "Variable '" + name + "' is already present on execution '" +
                            execution.getId() + "'.");
        }

        if (!isNew && !hasVariable) {
            throw new ActivitiObjectNotFoundException(
                    "Execution '" + execution.getId() + "' doesn't have a variable with name: '" +
                            name + "'.", null);
        }

        RuntimeService runtimeService = RestServiceContentHolder.getInstance().getRestService().getRumtimeService();
        if (scope == RestVariable.RestVariableScope.LOCAL) {
            runtimeService.setVariableLocal(execution.getId(), name, value);
        } else {
            if (execution.getParentId() != null) {
                runtimeService.setVariable(execution.getParentId(), name, value);
            } else {
                runtimeService.setVariable(execution.getId(), name, value);
            }
        }
    }

    protected boolean hasVariableOnScope(Execution execution, String variableName,
                                         RestVariable.RestVariableScope scope) {
        boolean logEnabled = log.isDebugEnabled();
        if (logEnabled) {
            log.debug("invoked hasVariableOnScope" + scope);
        }
        boolean variableFound = false;
        RuntimeService runtimeService = RestServiceContentHolder.getInstance().getRestService().getRumtimeService();

        if (scope == RestVariable.RestVariableScope.GLOBAL) {

            boolean test = false;
            if (execution.getParentId() != null) {
                test = true;
            }
            if (logEnabled) {
                log.debug("Execution has parentID:" + test);
            }
            if (execution.getParentId() != null &&
                    runtimeService.hasVariable(execution.getParentId(), variableName)) {
                variableFound = true;
            }

        } else if (scope == RestVariable.RestVariableScope.LOCAL) {
            if (runtimeService.hasVariableLocal(execution.getId(), variableName)) {
                variableFound = true;
            }
        }

        if (logEnabled) {
            log.debug("variableFound:" + variableFound);
        }
        return variableFound;
    }

    public RestVariable getVariableFromRequest(Execution execution, String variableName,
                                               String scope, boolean includeBinary,
                                               String baseContext) {

        boolean variableFound = false;
        Object value = null;

        if (execution == null) {
            throw new ActivitiObjectNotFoundException("Could not find an execution",
                    Execution.class);
        }

        RuntimeService runtimeService = RestServiceContentHolder.getInstance().getRestService().getRumtimeService();

        RestVariable.RestVariableScope variableScope = RestVariable.getScopeFromString(scope);
        if (variableScope == null) {
            // First, check local variables (which have precedence when no scope is supplied)
            if (runtimeService.hasVariableLocal(execution.getId(), variableName)) {
                value = runtimeService.getVariableLocal(execution.getId(), variableName);
                variableScope = RestVariable.RestVariableScope.LOCAL;
                variableFound = true;
            } else {
                if (execution.getParentId() != null) {
                    value = runtimeService.getVariable(execution.getParentId(), variableName);
                    variableScope = RestVariable.RestVariableScope.GLOBAL;
                    variableFound = true;
                }
            }
        } else if (variableScope == RestVariable.RestVariableScope.GLOBAL) {
            // Use parent to get variables
            if (execution.getParentId() != null) {
                value = runtimeService.getVariable(execution.getParentId(), variableName);
                variableScope = RestVariable.RestVariableScope.GLOBAL;
                variableFound = true;
            }
        } else if (variableScope == RestVariable.RestVariableScope.LOCAL) {

            value = runtimeService.getVariableLocal(execution.getId(), variableName);
            variableScope = RestVariable.RestVariableScope.LOCAL;
            variableFound = true;
        }

        if (!variableFound) {
            throw new ActivitiObjectNotFoundException("Execution '" + execution.getId() +
                    "' doesn't have a variable with name: '" +
                    variableName + "'.",
                    VariableInstanceEntity.class);
        } else {
            return constructRestVariable(variableName, value, variableScope, execution.getId(),
                    includeBinary, baseContext);
        }
    }

    protected List<RestVariable> processVariables(Execution execution, String scope,
                                                  int variableType, String baseContext) {
        List<RestVariable> result = new ArrayList<RestVariable>();
        Map<String, RestVariable> variableMap = new HashMap<>();

        // Check if it's a valid execution to get the variables for
        RestVariable.RestVariableScope variableScope = RestVariable.getScopeFromString(scope);

        if (variableScope == null) {
            // Use both local and global variables
            addLocalVariables(execution, variableType, variableMap, baseContext);
            addGlobalVariables(execution, variableType, variableMap, baseContext);

        } else if (variableScope == RestVariable.RestVariableScope.GLOBAL) {
            addGlobalVariables(execution, variableType, variableMap, baseContext);

        } else if (variableScope == RestVariable.RestVariableScope.LOCAL) {
            addLocalVariables(execution, variableType, variableMap, baseContext);
        }

        // Get unique variables from map
        result.addAll(variableMap.values());
        return result;
    }

    protected void addLocalVariables(Execution execution, int variableType,
                                     Map<String, RestVariable> variableMap, String baseContext) {
        RuntimeService runtimeService = RestServiceContentHolder.getInstance().getRestService().getRumtimeService();
        Map<String, Object> rawLocalvariables = runtimeService.getVariablesLocal(execution.getId());
        List<RestVariable> localVariables = new RestResponseFactory()
                .createRestVariables(rawLocalvariables, execution.getId(), variableType,
                        RestVariable.RestVariableScope.LOCAL, baseContext);

        for (RestVariable var : localVariables) {
            variableMap.put(var.getName(), var);
        }
    }

    protected void addGlobalVariables(Execution execution, int variableType,
                                      Map<String, RestVariable> variableMap, String baseContext) {
        RuntimeService runtimeService = RestServiceContentHolder.getInstance().getRestService().getRumtimeService();
        Map<String, Object> rawVariables = runtimeService.getVariables(execution.getId());
        List<RestVariable> globalVariables = new RestResponseFactory()
                .createRestVariables(rawVariables, execution.getId(), variableType,
                        RestVariable.RestVariableScope.GLOBAL, baseContext);

        // Overlay global variables over local ones. In case they are present the values are
        // not overridden,
        // since local variables get precedence over global ones at all times.
        for (RestVariable var : globalVariables) {
            if (!variableMap.containsKey(var.getName())) {
                variableMap.put(var.getName(), var);
            }
        }
    }

    protected RestVariable constructRestVariable(String variableName, Object value,
                                                 RestVariable.RestVariableScope variableScope,
                                                 String executionId, boolean includeBinary,
                                                 String baseContext) {

        return new RestResponseFactory()
                .createRestVariable(variableName, value, variableScope, executionId,
                        RestResponseFactory.VARIABLE_EXECUTION, includeBinary,
                        baseContext);
    }

    private boolean isValidUserToStartProcess(ProcessInstanceCreateRequest processInstanceCreateRequest) {

        //check whether the users/groups exist
        String processDefinitionId = processInstanceCreateRequest.getProcessDefinitionId();
        RepositoryService repositoryService = RestServiceContentHolder.getInstance().getRestService()
                .getRepositoryService();

        if (processDefinitionId == null) {

            final String processDefinitionKey = processInstanceCreateRequest.getProcessDefinitionKey();
            final String tenantId = processInstanceCreateRequest.getTenantId();

            ProcessEngine processEngine = RestServiceContentHolder.getInstance().getRestService()
                    .getBPMNEngineService().getProcessEngine();

            if (processEngine != null) {

                if (processDefinitionKey != null) {

                    if (((ProcessEngineImpl) processEngine).getProcessEngineConfiguration() != null) {
                        CommandExecutor commandExecutor = ((ProcessEngineImpl) processEngine).
                                getProcessEngineConfiguration().getCommandExecutor();
                        if (commandExecutor != null) {

                            processDefinitionId =
                                    (String) commandExecutor.execute(new Command<Object>() {
                                        public Object execute(CommandContext commandContext) {
                                            ProcessDefinitionEntityManager
                                                    processDefinitionEntityManager = commandContext.
                                                    getSession(ProcessDefinitionEntityManager.class);
                                            ProcessDefinitionEntity processDefinitionEntity =
                                                    processDefinitionEntityManager.
                                                            findLatestProcessDefinitionByKeyAndTenantId
                                                                    (processDefinitionKey, tenantId);
                                            if (processDefinitionEntity != null &&
                                                    processDefinitionEntity
                                                            .getProcessDefinition() != null) {
                                                return processDefinitionEntity.
                                                        getProcessDefinition().getId();
                                            }
                                            return null;
                                        }
                                    });
                        }
                    }
                    if (processDefinitionId == null) {
                        return false;
                    }
                }

                String messageName = processInstanceCreateRequest.getMessage();
                if (messageName != null && !messageName.isEmpty()) {

                    ProcessDefinitionQuery processDefinitionQuery =
                            repositoryService.createProcessDefinitionQuery()
                                    .messageEventSubscriptionName(messageName);
                    if (processDefinitionQuery != null) {
                        processDefinitionQuery = processDefinitionQuery.processDefinitionTenantId
                                (processInstanceCreateRequest.getTenantId());
                        if (processDefinitionQuery != null && processDefinitionQuery.count() > 1) {
                            processDefinitionQuery = processDefinitionQuery.latestVersion();
                        }
                    }

                    if (processDefinitionQuery != null) {
                        ProcessDefinition processDefinition = processDefinitionQuery.singleResult();
                        if (processDefinition != null) {
                            processDefinitionId = processDefinition.getId();
                        }
                    }

                    if (processDefinitionId == null) {
                        return false;
                    }
                }

            }

        }
//todo: Uncomment once user core permissions set
//        CarbonContext carbonContext = CarbonContext.getCurrentContext();
//        Principal principal = carbonContext.getUserPrincipal();
//         String userName = principal.getName();

        //todo: remove test value
        String userName = "admin";

        BPSGroupIdentityManager bpsGroupIdentityManager = RestServiceContentHolder.getInstance().getRestService()
                .getGroupIdentityManager();
        List<Group> groupList = bpsGroupIdentityManager.findGroupsByUser(userName);
        List<IdentityLink> identityLinkList = repositoryService.getIdentityLinksForProcessDefinition
                (processDefinitionId);


        boolean valueExistsForUserId = false;
        boolean valueExistsForGroupId = false;
        for (IdentityLink identityLink : identityLinkList) {

            String userId = identityLink.getUserId();
            if (userId != null) {
                valueExistsForUserId = true;
                if (userId.contains("$")) {
                    userId = resolveVariable(processInstanceCreateRequest, userId);
                }
                if (!userId.isEmpty() && (userId.equals(userName))) {
                    return true;
                }
                continue;
            }

            String groupId = identityLink.getGroupId();

            if (groupId != null) {
                valueExistsForGroupId = true;
                if (groupId.contains("$")) {
                    groupId = resolveVariable(processInstanceCreateRequest, groupId);
                }

                for (Group identityGroup : groupList) {
                    if (!groupId.isEmpty() && identityGroup.getId() != null &&
                            identityGroup.getId().equals(groupId)) {
                        return true;
                    }
                }
            }
        }

        if (!valueExistsForGroupId && !valueExistsForUserId) {
            return true;
        }

        return false;
    }

    private String resolveVariable(ProcessInstanceCreateRequest processInstanceCreateRequest,
                                   String resolvingName) {

        int initialIndex = resolvingName.indexOf("{");
        int lastIndex = resolvingName.indexOf("}");

        String variableName = null;
        if (initialIndex != -1 && lastIndex != -1 && initialIndex < lastIndex) {
            variableName = resolvingName.substring(initialIndex + 1, lastIndex);
        }

        List<RestVariable> variableList = processInstanceCreateRequest.getVariables();
        if (variableList != null && variableName != null) {
            for (RestVariable restVariable : variableList) {
                if (restVariable.getName().equals(variableName)) {
                    return restVariable.getValue().toString();
                }
            }
        }

        variableList = processInstanceCreateRequest.getAdditionalVariables();
        if (variableList != null && variableName != null) {
            for (RestVariable restVariable : variableList) {
                if (restVariable.getName().equals(variableName)) {
                    return restVariable.getValue().toString();
                }
            }
        }
        return "";
    }

    private Response performCorrelation(ProcessInstanceCreateRequest processInstanceCreateRequest,
                                        String baseName) {

        CorrelationActionRequest correlationActionRequest = new CorrelationActionRequest();

        String requestValue = processInstanceCreateRequest.getProcessDefinitionId();
        if (requestValue != null) {
            correlationActionRequest
                    .setProcessDefinitionId(processInstanceCreateRequest.getProcessDefinitionId());
        }

        requestValue = processInstanceCreateRequest.getProcessDefinitionKey();
        if (requestValue != null) {
            correlationActionRequest.setProcessDefinitionKey(requestValue);
        }

        if (processInstanceCreateRequest.isCustomTenantSet()) {
            correlationActionRequest.setTenantId(processInstanceCreateRequest.getTenantId());
        }

        requestValue = processInstanceCreateRequest.getMessageName();
        if (requestValue != null) {
            correlationActionRequest.setMessageName(requestValue);
        }

        List<RestVariable> variables = processInstanceCreateRequest.getVariables();
        if (variables != null) {
            RestResponseFactory restResponseFactory = new RestResponseFactory();
            List<QueryVariable> correlationVariableList = new ArrayList<>();
            for (RestVariable variable : variables) {
                QueryVariable correlationVariable = new QueryVariable();
                correlationVariable.setName(variable.getName());
                correlationVariable.setOperation("equals");
                correlationVariable.setType(variable.getType());
                correlationVariable.setValue(restResponseFactory.getVariableValue(variable));
                correlationVariableList.add(correlationVariable);
            }
            correlationActionRequest.setCorrelationVariables(correlationVariableList);
        }

        variables = processInstanceCreateRequest.getAdditionalVariables();
        if (variables != null) {
            correlationActionRequest.setVariables(variables);
        }

        correlationActionRequest.setAction(CorrelationActionRequest.ACTION_MESSAGE_EVENT_RECEIVED);
        return new CorrelationProcess().getQueryResponse(correlationActionRequest, baseName);
    }
}
