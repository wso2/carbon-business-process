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

import io.netty.handler.codec.http.QueryStringDecoder;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.impl.persistence.entity.VariableInstanceEntity;
import org.activiti.engine.runtime.Execution;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.bpmn.core.BPMNEngineService;
import org.wso2.carbon.bpmn.rest.common.RestResponseFactory;
import org.wso2.carbon.bpmn.rest.engine.variable.RestVariable;
import org.wso2.carbon.bpmn.rest.internal.BPMNOSGIService;
import org.wso2.carbon.bpmn.rest.model.common.DataResponse;
import org.wso2.carbon.bpmn.rest.model.runtime.ActiveActivityCollection;
import org.wso2.carbon.bpmn.rest.model.runtime.ExecutionActionRequest;
import org.wso2.carbon.bpmn.rest.model.runtime.ExecutionQueryRequest;
import org.wso2.carbon.bpmn.rest.model.runtime.ExecutionResponse;
import org.wso2.carbon.bpmn.rest.model.runtime.RestVariableCollection;
import org.wso2.carbon.bpmn.rest.service.base.BaseExecutionService;
import org.wso2.msf4j.Microservice;
import org.wso2.msf4j.Request;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

//import org.apache.cxf.jaxrs.ext.multipart.MultipartBody;
//import org.wso2.carbon.bpmn.rest.model.runtime.*;
//import javax.servlet.http.HttpServletRequest;

/**
 *
 */
@Component(
        name = "org.wso2.carbon.bpmn.rest.service.runtime.ExecutionService",
        service = Microservice.class,
        immediate = true)

@Path("/executions")
public class ExecutionService extends BaseExecutionService implements Microservice {

    private static final Logger log = LoggerFactory.getLogger(ExecutionService.class);

    @Reference(
            name = "org.wso2.carbon.bpmn.core.BPMNEngineService",
            service = BPMNEngineService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unRegisterBPMNEngineService")
    public void setBpmnEngineService(BPMNEngineService engineService) {
        log.info("Setting BPMN engine " + engineService);

    }

    protected void unRegisterBPMNEngineService(BPMNEngineService engineService) {
        log.info("Unregister BPMNEngineService..");
    }


    @Activate
    protected void activate(BundleContext bundleContext) {
        // Nothing to do
    }

    @Deactivate
    protected void deactivate(BundleContext bundleContext) {
        // Nothing to do
    }

    /**
     * Get the process execution identified by given execution ID
     *
     * @param executionId
     * @return ExecutionResponse
     */
    @GET
    @Path("/{execution-id}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getExecution(@PathParam("execution-id") String executionId,
                                 @Context Request request) {

        ExecutionResponse executionResponse = new RestResponseFactory()
                .createExecutionResponse(getExecutionFromRequest(executionId), request.getUri());
        return Response.ok().entity(executionResponse).build();
    }

    /**
     * Execute an action on an execution
     *
     * @param executionId
     * @param actionRequest
     * @return Response
     */
    @PUT
    @Path("/{execution-id}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response performExecutionAction(@PathParam("execution-id") String executionId,
                                           ExecutionActionRequest actionRequest,
                                           @Context Request request) {

        Execution execution = getExecutionFromRequest(executionId);
        RuntimeService runtimeService = BPMNOSGIService.getRumtimeService();

        if (ExecutionActionRequest.ACTION_SIGNAL.equals(actionRequest.getAction())) {
            if (actionRequest.getVariables() != null) {
                runtimeService.signal(execution.getId(), getVariablesToSet(actionRequest));
            } else {
                runtimeService.signal(execution.getId());
            }
        } else if (ExecutionActionRequest.ACTION_SIGNAL_EVENT_RECEIVED
                .equals(actionRequest.getAction())) {
            if (actionRequest.getSignalName() == null) {
                throw new ActivitiIllegalArgumentException("Signal name is required");
            }
            if (actionRequest.getVariables() != null) {
                runtimeService.signalEventReceived(actionRequest.getSignalName(), execution.getId(),
                        getVariablesToSet(actionRequest));
            } else {
                runtimeService
                        .signalEventReceived(actionRequest.getSignalName(), execution.getId());
            }
        } else if (ExecutionActionRequest.ACTION_MESSAGE_EVENT_RECEIVED
                .equals(actionRequest.getAction())) {
            if (actionRequest.getMessageName() == null) {
                throw new ActivitiIllegalArgumentException("Message name is required");
            }
            if (actionRequest.getVariables() != null) {
                runtimeService
                        .messageEventReceived(actionRequest.getMessageName(), execution.getId(),
                                getVariablesToSet(actionRequest));
            } else {
                runtimeService
                        .messageEventReceived(actionRequest.getMessageName(), execution.getId());
            }
        } else {
            throw new ActivitiIllegalArgumentException(
                    "Invalid action: '" + actionRequest.getAction() + "'.");
        }

        Response.ResponseBuilder response = Response.ok();
        // Re-fetch the execution, could have changed due to action or even completed
        execution =
                runtimeService.createExecutionQuery().executionId(execution.getId()).singleResult();
        if (execution == null) {
            // Execution is finished, return empty body to inform user
            response.status(Response.Status.NO_CONTENT);
        } else {
            response.entity(
                    new RestResponseFactory().createExecutionResponse(execution, request.getUri()))
                    .build();
        }

        return response.build();
    }

    @GET
    @Path("/{execution-id}/activities")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getActiveActivities(@PathParam("execution-id") String executionId) {
        Execution execution = getExecutionFromRequest(executionId);
        RuntimeService runtimeService = BPMNOSGIService.getRumtimeService();

        List<String> activityIdList = runtimeService.getActiveActivityIds(execution.getId());
        ActiveActivityCollection activeActivityCollection = new ActiveActivityCollection();
        activeActivityCollection.setActiveActivityList(activityIdList);
        return Response.ok().entity(activeActivityCollection).build();
    }

    @GET
    @Path("/")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML}) //TODO:
    public Response getProcessInstances(@Context Request request) {
        // Populate query based on request
        ExecutionQueryRequest queryRequest = new ExecutionQueryRequest();
        Map<String, String> allRequestParams = new HashMap<>();

        QueryStringDecoder decoder = new QueryStringDecoder(request.getUri());
        if (decoder.parameters().size() > 0) {
            for (String property : ALL_PROPERTIES_LIST) {
                String value = decoder.parameters().get(property).get(0);

                if (value != null) {
                    allRequestParams.put(property, value);
                }
            }

            if (decoder.parameters().containsKey("id")) {
                String id = decoder.parameters().get("id").get(0);

                if (id != null) {
                    queryRequest.setId(id);
                }
            }

            if (decoder.parameters().containsKey("processInstanceId")) {
                String processInstanceId = decoder.parameters().get("processInstanceId").get(0);

                if (processInstanceId != null) {
                    queryRequest.setProcessInstanceId(processInstanceId);
                }
            }

            if (decoder.parameters().containsKey("processInstanceBusinessKey")) {
                String processInstanceBusinessKey =
                        decoder.parameters().get("processInstanceBusinessKey").get(0);

                if (processInstanceBusinessKey != null) {
                    queryRequest.setProcessBusinessKey(processInstanceBusinessKey);
                }
            }

            if (decoder.parameters().containsKey("processDefinitionKey")) {
                String processDefinitionKey = decoder.parameters().get("processDefinitionKey").get(0);
                if (processDefinitionKey != null) {
                    queryRequest.setProcessDefinitionKey(processDefinitionKey);
                }
            }

            if (decoder.parameters().containsKey("processDefinitionId")) {
                String processDefinitionId = decoder.parameters().get("processDefinitionId").get(0);

                if (processDefinitionId != null) {
                    queryRequest.setProcessDefinitionId(processDefinitionId);
                }
            }

            if (decoder.parameters().containsKey("messageEventSubscriptionName")) {
                String messageEventSubscriptionName =
                        decoder.parameters().get("messageEventSubscriptionName").get(0);

                if (messageEventSubscriptionName != null) {
                    queryRequest.setMessageEventSubscriptionName(messageEventSubscriptionName);
                }
            }
            if (decoder.parameters().containsKey("signalEventSubscriptionName")) {
                String signalEventSubscriptionName =
                        decoder.parameters().get("signalEventSubscriptionName").get(0);

                if (signalEventSubscriptionName != null) {
                    queryRequest.setSignalEventSubscriptionName(signalEventSubscriptionName);
                }
            }
            if (decoder.parameters().containsKey("activityId")) {
                String activityId = decoder.parameters().get("activityId").get(0);

                if (activityId != null) {
                    queryRequest.setActivityId(activityId);
                }
            }
            if (decoder.parameters().containsKey("parentId")) {
                String parentId = decoder.parameters().get("parentId").get(0);

                if (parentId != null) {
                    queryRequest.setParentId(parentId);
                }
            }
            if (decoder.parameters().containsKey("tenantId")) {
                String tenantId = decoder.parameters().get("tenantId").get(0);

                if (tenantId != null) {
                    queryRequest.setTenantId(tenantId);
                }
            }
            if (decoder.parameters().containsKey("tenantIdLike")) {
                String tenantIdLike = decoder.parameters().get("tenantIdLike").get(0);

                if (tenantIdLike != null) {
                    queryRequest.setTenantIdLike(tenantIdLike);
                }
            }
            if (decoder.parameters().containsKey("withoutTenantId")) {
                String withoutTenantId = decoder.parameters().get("withoutTenantId").get(0);

                if (withoutTenantId != null) {
                    if (Boolean.valueOf(withoutTenantId)) {
                        queryRequest.setWithoutTenantId(Boolean.TRUE);
                    }

                }
            }
        }

        //add common parameters such as sort,order,start etc.
        //allRequestParams = Utils.prepareCommonParameters(allRequestParams, uriInfo);

        DataResponse dataResponse =
                getQueryResponse(queryRequest, allRequestParams, request.getUri());

        return Response.ok().entity(dataResponse).build();
    }

    @PUT
    @Path("/")
    public Response executeExecutionAction(ExecutionActionRequest actionRequest) {
        if (!ExecutionActionRequest.ACTION_SIGNAL_EVENT_RECEIVED
                .equals(actionRequest.getAction())) {
            throw new ActivitiIllegalArgumentException(
                    "Illegal action: '" + actionRequest.getAction() + "'.");
        }

        if (actionRequest.getSignalName() == null) {
            throw new ActivitiIllegalArgumentException("Signal name is required.");
        }
        RuntimeService runtimeService = BPMNOSGIService.getRumtimeService();

        if (actionRequest.getVariables() != null) {
            runtimeService.signalEventReceived(actionRequest.getSignalName(),
                    getVariablesToSet(actionRequest));
        } else {
            runtimeService.signalEventReceived(actionRequest.getSignalName());
        }
        return Response.ok().status(Response.Status.NO_CONTENT).build();
    }

    @GET
    @Path("/{execution-id}/variables")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getVariables(@PathParam("execution-id") String executionId,
                                 @QueryParam("scope") String scope, @Context Request req) {

        Execution execution = getExecutionFromRequest(executionId);
        List<RestVariable> restVariableList =
                processVariables(execution, scope, RestResponseFactory.VARIABLE_EXECUTION,
                        req.getUri());
        RestVariableCollection restVariableCollection = new RestVariableCollection();
        restVariableCollection.setRestVariables(restVariableList);
        return Response.ok().entity(restVariableCollection).build();
    }
//todo:
//    @PUT
//    @Path("/{execution-id}/variables")
//    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
//    @Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
//    public Response createOrUpdateExecutionVariable(@PathParam("execution-id") String executionId,
//                                                    @Context HttpServletRequest httpServletRequest,
//                                                    @Context Request req) {
//        Execution execution = getExecutionFromRequest(executionId);
//        return createExecutionVariable(execution, true, RestResponseFactory.VARIABLE_EXECUTION,
//                                       httpServletRequest, req.getUri());
//    }
    //TODO
   /* @PUT
    @Path("/{executionId}/variables")
    @Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response createOrUpdateBinaryExecutionVariable(@PathParam("executionId")
    String executionId,
                                                          MultipartBody multipartBody) {
        Execution execution = getExecutionFromRequest(executionId);
        RestVariable restVariable = createBinaryExecutionVariable(execution,
        RestResponseFactory.VARIABLE_EXECUTION,
                uriInfo, true, multipartBody);
        return Response.ok().status(Response.Status.CREATED).entity(restVariable).build();
    }*/
//TODO:
//    @POST
//    @Path("/{execution-id}/variables")
//    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
//    @Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
//    public Response createExecutionVariable(@PathParam("execution-id") String executionId,
//                                            @Context HttpServletRequest httpServletRequest,
//                                            @Context Request req) {
//
//        Execution execution = getExecutionFromRequest(executionId);
//        return createExecutionVariable(execution, false, RestResponseFactory.VARIABLE_EXECUTION,
//                                       httpServletRequest, req.getUri());
//    }

    //TODO
/*
    @POST
    @Path("/{executionId}/variables")
    @Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response createBinaryExecutionVariable(@PathParam("executionId") String executionId,
    @Context HttpStreamer httpStreamer,@Context Request request) {

        Execution execution = getExecutionFromRequest(executionId);
        RestVariable restVariable = createBinaryExecutionVariable(execution,RestResponseFactory.
        VARIABLE_EXECUTION,
                uriInfo, true, httpStreamer);
        return Response.ok().status(Response.Status.CREATED).entity(restVariable).build();
    }
*/
    @DELETE
    @Path("/{execution-id}/variables")
    public Response deleteLocalVariables(@PathParam("execution-id") String executionId) {
        Execution execution = getExecutionFromRequest(executionId);
        deleteAllLocalVariables(execution);
        return Response.ok().status(Response.Status.NO_CONTENT).build();
    }

    @GET
    @Path("/{execution-id}/variables/{variable-name}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public RestVariable getVariable(@PathParam("execution-id") String executionId,
                                    @PathParam("variable-name") String variableName,
                                    @QueryParam("scope") String scope, @Context Request req) {
        Execution execution = getExecutionFromRequest(executionId);
        return getVariableFromRequest(execution, variableName, scope, false, req.getUri());
    }

    //TODO
/*
    @PUT
    @Path("/{executionId}/variables/{variableName}")
    @Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response updateBinaryVariable(@PathParam("executionId") String executionId,
                                       @PathParam("variableName") String variableName,
                                             MultipartBody multipartBody) {
        Execution execution = getExecutionFromRequest(executionId);
        RestVariable result = createBinaryExecutionVariable(execution,RestResponseFactory
        .VARIABLE_EXECUTION,
                uriInfo, false, multipartBody);

        return Response.ok().status(Response.Status.CREATED).entity(result).build();
    }
*/
    //TODO:
//    @PUT
//    @Path("/{execution-id}/variables/{variable-name}")
//    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
//    @Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
//    public Response updateVariable(@PathParam("execution-id") String executionId,
//                                   @PathParam("variable-name") String variableName,
//                                   @Context HttpServletRequest httpServletRequest,
//                                   @Context Request req) {
//        Execution execution = getExecutionFromRequest(executionId);
//        RestVariable result = null;
//
//        RestVariable restVariable = null;
//
//        try {
//            restVariable = new ObjectMapper()
//                    .readValue(httpServletRequest.getInputStream(), RestVariable.class);
//        } catch (Exception e) {
//            throw new ActivitiIllegalArgumentException(
//                    "Error converting request body to RestVariable instance", e);
//        }
//
//        if (restVariable == null) {
//            throw new ActivitiException("Invalid body was supplied");
//        }
//        if (!restVariable.getName().equals(variableName)) {
//            throw new ActivitiIllegalArgumentException(
//                    "Variable name in the body should be equal to the name used in the requested URL"
//                    +
//                    ".");
//        }
//
//        result = setSimpleVariable(restVariable, execution, false, req.getUri());
//
//        return Response.ok().status(Response.Status.CREATED).entity(result).build();
//    }

    @DELETE
    @Path("/{execution-id}/variables/{variable-name}")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response deleteVariable(@PathParam("execution-id") String executionId,
                                   @PathParam("variable-name") String variableName,
                                   @QueryParam("scope") String scope) {
        Execution execution = getExecutionFromRequest(executionId);
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

        RuntimeService runtimeService = BPMNOSGIService.getRumtimeService();
        if (variableScope == RestVariable.RestVariableScope.LOCAL) {
            runtimeService.removeVariableLocal(execution.getId(), variableName);
        } else {
            // Safe to use parentId, as the hasVariableOnScope would have stopped a
            // global-var update on a root-execution
            runtimeService.removeVariable(execution.getParentId(), variableName);
        }

        return Response.ok().status(Response.Status.NO_CONTENT).build();
    }

    @GET
    @Path("/{execution-id}/variables/{variable-name}/data")
    public Response getVariableData(@PathParam("execution-id") String executionId,
                                    @PathParam("variable-name") String variableName,
                                    @QueryParam("scope") String scope, @Context Request req) {

        try {
            byte[] result = null;
            Response.ResponseBuilder response = Response.ok();
            Execution execution = getExecutionFromRequest(executionId);
            RestVariable variable =
                    getVariableFromRequest(execution, variableName, scope, true, req.getUri());
            if (RestResponseFactory.BYTE_ARRAY_VARIABLE_TYPE.equals(variable.getType())) {
                result = (byte[]) variable.getValue();
                response.type("application/octet-stream");
            } else if (RestResponseFactory.SERIALIZABLE_VARIABLE_TYPE.equals(variable.getType())) {
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                ObjectOutputStream outputStream = new ObjectOutputStream(buffer);
                outputStream.writeObject(variable.getValue());
                outputStream.close();
                result = buffer.toByteArray();
                response.type("application/x-java-serialized-object");
            } else {
                throw new ActivitiObjectNotFoundException(
                        "The variable does not have a binary data stream.", null);
            }
            return response.entity(result).build();

        } catch (IOException ioe) {
            throw new ActivitiException("Error getting variable " + variableName, ioe);
        }
    }

}

