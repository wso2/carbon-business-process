package org.wso2.carbon.bpmn.rest.service.runtime;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.impl.persistence.entity.VariableInstanceEntity;
import org.activiti.engine.runtime.Execution;
import org.apache.cxf.jaxrs.ext.multipart.MultipartBody;
import org.wso2.carbon.bpmn.rest.common.RestResponseFactory;
import org.wso2.carbon.bpmn.rest.common.utils.BPMNOSGIService;
import org.wso2.carbon.bpmn.rest.common.utils.Utils;
import org.wso2.carbon.bpmn.rest.engine.variable.RestVariable;
import org.wso2.carbon.bpmn.rest.model.common.DataResponse;
import org.wso2.carbon.bpmn.rest.model.runtime.*;
import org.wso2.carbon.bpmn.rest.service.base.BaseExecutionService;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Path("/executions")
public class ExecutionService  extends BaseExecutionService {

    @Context
    protected UriInfo uriInfo;
    /**
     * Get the process execution identified by given execution ID
     * @param executionId
     * @return ExecutionResponse
     */
    @GET
    @Path("/{executionId}")
    @Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
    public Response getExecution(@PathParam("executionId") String executionId, @Context UriInfo uriInfo) {

        ExecutionResponse executionResponse = new RestResponseFactory()
                .createExecutionResponse(getExecutionFromRequest(executionId), uriInfo.getBaseUri().toString());
        return Response.ok().entity(executionResponse).build();
    }

    /**
     * Execute an action on an execution
     * @param executionId
     * @param actionRequest
     * @return Response
     */
    @PUT
    @Path("/{executionId}")
    @Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
    @Consumes({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
    public Response performExecutionAction(@PathParam("executionId") String executionId, ExecutionActionRequest
            actionRequest) {

        Execution execution = getExecutionFromRequest(executionId);
        RuntimeService runtimeService = BPMNOSGIService.getRuntimeService();

        if (ExecutionActionRequest.ACTION_SIGNAL.equals(actionRequest.getAction())) {
            if (actionRequest.getVariables() != null) {
                runtimeService.signal(execution.getId(), getVariablesToSet(actionRequest));
            } else {
                runtimeService.signal(execution.getId());
            }
        } else if (ExecutionActionRequest.ACTION_SIGNAL_EVENT_RECEIVED.equals(actionRequest.getAction())) {
            if (actionRequest.getSignalName() == null) {
                throw new ActivitiIllegalArgumentException("Signal name is required");
            }
            if (actionRequest.getVariables() != null) {
                runtimeService.signalEventReceived(actionRequest.getSignalName(), execution.getId(),
                        getVariablesToSet(actionRequest));
            } else {
                runtimeService.signalEventReceived(actionRequest.getSignalName(), execution.getId());
            }
        } else if (ExecutionActionRequest.ACTION_MESSAGE_EVENT_RECEIVED.equals(actionRequest.getAction())) {
            if (actionRequest.getMessageName() == null) {
                throw new ActivitiIllegalArgumentException("Message name is required");
            }
            if (actionRequest.getVariables() != null) {
                runtimeService.messageEventReceived(actionRequest.getMessageName(), execution.getId(),
                        getVariablesToSet(actionRequest));
            } else {
                runtimeService.messageEventReceived(actionRequest.getMessageName(), execution.getId());
            }
        } else {
            throw new ActivitiIllegalArgumentException("Invalid action: '" + actionRequest.getAction() + "'.");
        }

        Response.ResponseBuilder response = Response.ok();
        // Re-fetch the execution, could have changed due to action or even completed
        execution = runtimeService.createExecutionQuery().executionId(execution.getId()).singleResult();
        if (execution == null) {
            // Execution is finished, return empty body to inform user
            response.status(Response.Status.NO_CONTENT);
        } else {
            response.entity(new RestResponseFactory()
                    .createExecutionResponse(execution, uriInfo.getBaseUri().toString())).build();
        }

        return response.build();
    }

    @GET
    @Path("/{executionId}/activities")
    @Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
    public Response getActiveActivities(@PathParam("executionId") String executionId) {
        Execution execution = getExecutionFromRequest(executionId);
        RuntimeService runtimeService = BPMNOSGIService.getRuntimeService();

        List<String> activityIdList = runtimeService.getActiveActivityIds(execution.getId());
        ActiveActivityCollection activeActivityCollection = new ActiveActivityCollection();
        activeActivityCollection.setActiveActivityList(activityIdList);
        return Response.ok().entity(activeActivityCollection).build();
    }

    @GET
    @Path("/")
    @Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
    public Response getProcessInstances() {
        // Populate query based on request
        ExecutionQueryRequest queryRequest = new ExecutionQueryRequest();
        Map<String, String> allRequestParams = new HashMap<>();

        for (String property:allPropertiesList){
            String value= uriInfo.getQueryParameters().getFirst(property);

            if(value != null){
                allRequestParams.put(property, value);
            }
        }

        String id = uriInfo.getQueryParameters().getFirst("id");
        if (id != null) {
            queryRequest.setId(id);
        }

        String processInstanceId = uriInfo.getQueryParameters().getFirst("processInstanceId");
        if (processInstanceId != null) {
            queryRequest.setProcessInstanceId(processInstanceId);
        }

        String processInstanceBusinessKey = uriInfo.getQueryParameters().getFirst("processInstanceBusinessKey");
        if (processInstanceBusinessKey != null) {
            queryRequest.setProcessBusinessKey(processInstanceBusinessKey);
        }

        String processDefinitionKey = uriInfo.getQueryParameters().getFirst("processDefinitionKey");
        if (processDefinitionKey != null) {
            queryRequest.setProcessDefinitionKey(processDefinitionKey);
        }

        String processDefinitionId = uriInfo.getQueryParameters().getFirst("processDefinitionId");
        if (processDefinitionId != null) {
            queryRequest.setProcessDefinitionId(processDefinitionId);
        }

        String messageEventSubscriptionName = uriInfo.getQueryParameters().getFirst("messageEventSubscriptionName");
        if (messageEventSubscriptionName != null) {
            queryRequest.setMessageEventSubscriptionName(messageEventSubscriptionName);
        }

        String signalEventSubscriptionName = uriInfo.getQueryParameters().getFirst("signalEventSubscriptionName");
        if (signalEventSubscriptionName != null) {
            queryRequest.setSignalEventSubscriptionName(signalEventSubscriptionName);
        }

        String activityId = uriInfo.getQueryParameters().getFirst("activityId");
        if (activityId != null) {
            queryRequest.setActivityId(activityId);
        }

        String parentId = uriInfo.getQueryParameters().getFirst("parentId");
        if (parentId != null) {
            queryRequest.setParentId(parentId);
        }

        String tenantId = uriInfo.getQueryParameters().getFirst("tenantId");
        if (tenantId != null) {
            queryRequest.setTenantId(tenantId);
        }

        String tenantIdLike = uriInfo.getQueryParameters().getFirst("tenantIdLike");
        if (tenantIdLike != null) {
            queryRequest.setTenantIdLike(tenantIdLike);
        }

        String withoutTenantId = uriInfo.getQueryParameters().getFirst("withoutTenantId");
        if (withoutTenantId != null) {
            if (Boolean.valueOf(withoutTenantId)) {
                queryRequest.setWithoutTenantId(Boolean.TRUE);
            }

        }

        //add common parameters such as sort,order,start etc.
        //allRequestParams = Utils.prepareCommonParameters(allRequestParams, uriInfo);

        DataResponse dataResponse = getQueryResponse(queryRequest, allRequestParams, uriInfo);

        return Response.ok().entity(dataResponse).build();
    }



    @PUT
    @Path("/")
    public Response executeExecutionAction(ExecutionActionRequest actionRequest) {
        if (!ExecutionActionRequest.ACTION_SIGNAL_EVENT_RECEIVED.equals(actionRequest.getAction())) {
            throw new ActivitiIllegalArgumentException("Illegal action: '" + actionRequest.getAction() +"'.");
        }

        if (actionRequest.getSignalName() == null) {
            throw new ActivitiIllegalArgumentException("Signal name is required.");
        }
        RuntimeService runtimeService = BPMNOSGIService.getRuntimeService();

        if (actionRequest.getVariables() != null) {
            runtimeService.signalEventReceived(actionRequest.getSignalName(), getVariablesToSet(actionRequest));
        } else {
            runtimeService.signalEventReceived(actionRequest.getSignalName());
        }
        return Response.ok().status(Response.Status.NO_CONTENT).build();
    }



    @GET
    @Path("/{executionId}/variables")
    @Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
    public Response getVariables(@PathParam("executionId") String executionId) {

        String scope = uriInfo.getQueryParameters().getFirst("scope");
        Execution execution = getExecutionFromRequest(executionId);
        List<RestVariable> restVariableList = processVariables(execution, scope, RestResponseFactory
                .VARIABLE_EXECUTION, uriInfo);
        RestVariableCollection restVariableCollection = new RestVariableCollection();
        restVariableCollection.setRestVariables(restVariableList);
        return Response.ok().entity(restVariableCollection).build();
    }

    @PUT
    @Path("/{executionId}/variables")
    @Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
    @Consumes({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
    public Response createOrUpdateExecutionVariable(@PathParam("executionId") String executionId, @Context
                                                  HttpServletRequest httpServletRequest) {
        Execution execution = getExecutionFromRequest(executionId);
        return createExecutionVariable(execution, true, RestResponseFactory.VARIABLE_EXECUTION, httpServletRequest,
                uriInfo );
    }

    @PUT
    @Path("/{executionId}/variables")
    @Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response createOrUpdateBinaryExecutionVariable(@PathParam("executionId") String executionId,
                                                          MultipartBody multipartBody) {
        Execution execution = getExecutionFromRequest(executionId);
        RestVariable restVariable = createBinaryExecutionVariable(execution,RestResponseFactory.VARIABLE_EXECUTION,
                uriInfo, true, multipartBody);
        return Response.ok().status(Response.Status.CREATED).entity(restVariable).build();
    }


    @POST
    @Path("/{executionId}/variables")
    @Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
    @Consumes({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
    public Response createExecutionVariable(@PathParam("executionId") String executionId, @Context HttpServletRequest
            httpServletRequest) {

        Execution execution = getExecutionFromRequest(executionId);
        return createExecutionVariable(execution, false, RestResponseFactory.VARIABLE_EXECUTION, httpServletRequest,
                uriInfo);
    }

    @POST
    @Path("/{executionId}/variables")
    @Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response createBinaryExecutionVariable(@PathParam("executionId") String executionId, MultipartBody
            multipartBody) {

        Execution execution = getExecutionFromRequest(executionId);
        RestVariable restVariable = createBinaryExecutionVariable(execution,RestResponseFactory.VARIABLE_EXECUTION,
                uriInfo, true, multipartBody);
        return Response.ok().status(Response.Status.CREATED).entity(restVariable).build();
    }

    @DELETE
    @Path("/{executionId}/variables")
    public Response deleteLocalVariables(@PathParam("executionId") String executionId) {
        Execution execution = getExecutionFromRequest(executionId);
        deleteAllLocalVariables(execution);
        return Response.ok().status(Response.Status.NO_CONTENT).build();
    }

    @GET
    @Path("/{executionId}/variables/{variableName}")
    @Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
    public RestVariable getVariable(@PathParam("executionId") String executionId,
                                    @PathParam("variableName") String variableName) {
       String scope = uriInfo.getQueryParameters().getFirst("scope");
        Execution execution = getExecutionFromRequest(executionId);
        return getVariableFromRequest(execution, variableName, scope, false, uriInfo);
    }


    @PUT
    @Path("/{executionId}/variables/{variableName}")
    @Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response updateBinaryVariable(@PathParam("executionId") String executionId,
                                       @PathParam("variableName") String variableName,
                                             MultipartBody multipartBody) {
        Execution execution = getExecutionFromRequest(executionId);
        RestVariable result = createBinaryExecutionVariable(execution,RestResponseFactory.VARIABLE_EXECUTION,
                uriInfo, false, multipartBody);

        return Response.ok().status(Response.Status.CREATED).entity(result).build();
    }

    @PUT
    @Path("/{executionId}/variables/{variableName}")
    @Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
    @Consumes({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
    public Response updateVariable(@PathParam("executionId") String executionId,
                                         @PathParam("variableName") String variableName,
                                         @Context HttpServletRequest httpServletRequest) {
        Execution execution = getExecutionFromRequest(executionId);
        RestVariable result = null;

        RestVariable restVariable = null;

        if (Utils.isApplicationJsonRequest(httpServletRequest)) {
            try {
                restVariable = new ObjectMapper().readValue(httpServletRequest.getInputStream(), RestVariable.class);
            } catch (Exception e) {
                throw new ActivitiIllegalArgumentException("Error converting request body to RestVariable instance", e);
            }
        } else if (Utils.isApplicationXmlRequest(httpServletRequest)) {
            JAXBContext jaxbContext;
            try {
                jaxbContext = JAXBContext.newInstance(RestVariable.class);
                Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
                restVariable = (RestVariable) jaxbUnmarshaller.unmarshal(httpServletRequest.getInputStream());

            } catch (JAXBException | IOException e) {
                throw new ActivitiIllegalArgumentException("xml request body could not be transformed to a RestVariable instance.", e);
            }
        }

        if (restVariable == null) {
            throw new ActivitiException("Invalid body was supplied");
        }
        if (!restVariable.getName().equals(variableName)) {
            throw new ActivitiIllegalArgumentException("Variable name in the body should be equal to the name used in the requested URL.");
        }

        result = setSimpleVariable(restVariable, execution, false, uriInfo);

        return Response.ok().status(Response.Status.CREATED).entity(result).build();
    }

    @DELETE
    @Path("/{executionId}/variables/{variableName}")
    @Consumes({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
    public Response deleteVariable(@PathParam("executionId") String executionId,
                               @PathParam("variableName") String variableName) {
        String scope = uriInfo.getQueryParameters().getFirst("scope");
        Execution execution = getExecutionFromRequest(executionId);
        // Determine scope
        RestVariable.RestVariableScope variableScope = RestVariable.RestVariableScope.LOCAL;
        if (scope != null) {
            variableScope = RestVariable.getScopeFromString(scope);
        }

        if (!hasVariableOnScope(execution, variableName, variableScope)) {
            throw new ActivitiObjectNotFoundException("Execution '" + execution.getId() + "' doesn't have a variable '" +
                    variableName + "' in scope " + variableScope.name().toLowerCase(), VariableInstanceEntity.class);
        }

        RuntimeService runtimeService = BPMNOSGIService.getRuntimeService();
        if (variableScope == RestVariable.RestVariableScope.LOCAL) {
            runtimeService.removeVariableLocal(execution.getId(), variableName);
        } else {
            // Safe to use parentId, as the hasVariableOnScope would have stopped a global-var update on a root-execution
            runtimeService.removeVariable(execution.getParentId(), variableName);
        }

        return Response.ok().status(Response.Status.NO_CONTENT).build();
    }

    @GET
    @Path("/{executionId}/variables/{variableName}/data")
    public Response getVariableData(@PathParam("executionId") String executionId,
                                    @PathParam("variableName") String variableName) {

        String scope = uriInfo.getQueryParameters().getFirst("scope");

        try {
            byte[] result = null;
            Response.ResponseBuilder response = Response.ok();
            Execution execution = getExecutionFromRequest(executionId);
            RestVariable variable = getVariableFromRequest(execution, variableName, scope, true, uriInfo);
            if (RestResponseFactory.BYTE_ARRAY_VARIABLE_TYPE.equals(variable.getType())) {
                result = (byte[]) variable.getValue();
                response.type("application/octet-stream");
            } else if(RestResponseFactory.SERIALIZABLE_VARIABLE_TYPE.equals(variable.getType())) {
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                ObjectOutputStream outputStream = new ObjectOutputStream(buffer);
                outputStream.writeObject(variable.getValue());
                outputStream.close();
                result = buffer.toByteArray();
                response.type("application/x-java-serialized-object");
            } else {
                throw new ActivitiObjectNotFoundException("The variable does not have a binary data stream.", null);
            }
            return response.entity(result).build();

        } catch (IOException ioe) {
            throw new ActivitiException("Error getting variable " + variableName, ioe);
        }
    }

    @OPTIONS
    @Path("{path : .*}")
    public Response options() {
        return Response.ok("").build();
    }

}