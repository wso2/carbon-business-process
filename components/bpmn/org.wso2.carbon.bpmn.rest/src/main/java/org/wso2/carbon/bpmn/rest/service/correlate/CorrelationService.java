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

package org.wso2.carbon.bpmn.rest.service.correlate;

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.query.QueryProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.bpmn.rest.common.CorrelationProcess;
import org.wso2.carbon.bpmn.rest.model.common.CorrelationQueryProperty;
import org.wso2.carbon.bpmn.rest.model.correlation.CorrelationActionRequest;
import org.wso2.msf4j.Request;

import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 *
 */
//@Component(
//        name = "org.wso2.carbon.bpmn.rest.service.correlate.CorrelationService",
//        service = Microservice.class,
//        immediate = true)
//@Path("/receive")
public class CorrelationService { //s implements Microservice {
    private static final Logger log = LoggerFactory.getLogger(CorrelationService.class);
    private static Map<String, QueryProperty> allowedSortProperties = new HashMap<>();

    static {
        allowedSortProperties
                .put("processInstanceId", CorrelationQueryProperty.PROCESS_INSTANCE_ID);
    }

//    public CorrelationService() {
//        log.info("Activated CorrelationService");
//    }
//    @Activate
//    protected void activate(BundleContext bundleContext) {
//        // Nothing to do.
//    }
//
//    @Deactivate
//    protected void deactivate(BundleContext bundleContext) {
//        // Nothing to do
//    }


    @POST
    @Path("/")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response recieveMessage(CorrelationActionRequest correlationActionRequest,
                                   @Context Request request) {

        if (correlationActionRequest.getProcessDefinitionId() == null &&
                correlationActionRequest.getProcessDefinitionKey() == null &&
                (correlationActionRequest.getMessageName() == null &&
                        correlationActionRequest.getSignalName() == null)) {
            throw new ActivitiIllegalArgumentException(
                    "Either processDefinitionId, processDefinitionKey, signal or " +
                            "message is required.");
        }

        int paramsSet = ((correlationActionRequest.getProcessDefinitionId() != null) ? 1 : 0) +
                ((correlationActionRequest.getProcessDefinitionKey() != null) ? 1 : 0);

        if (paramsSet > 1) {
            throw new ActivitiIllegalArgumentException(
                    "Only one of processDefinitionId or processDefinitionKey should be set.");
        }

        paramsSet = ((correlationActionRequest.getMessageName() != null) ? 1 : 0) +
                ((correlationActionRequest.getSignalName() != null) ? 1 : 0);

        if (paramsSet > 1) {
            throw new ActivitiIllegalArgumentException(
                    "Only one of message name or signal should be " + "set.");
        }

        CorrelationProcess correlationProcess = new CorrelationProcess();
        return correlationProcess.getQueryResponse(correlationActionRequest, request.getUri());

    }

   /* protected Response getQueryResponse(CorrelationActionRequest correlationActionRequest) {

        RuntimeService runtimeService = BPMNRestServiceImpl.getRumtimeService();
        ExecutionQuery query = runtimeService.createExecutionQuery();

        String value = correlationActionRequest.getProcessDefinitionId();
        if (value != null) {
            query.processDefinitionId(value);
        }

        value = correlationActionRequest.getProcessDefinitionKey();
        if (value != null) {
            query.processDefinitionKey(value);
        }

        value = correlationActionRequest.getMessageName();
        if (value != null) {
            query.messageEventSubscriptionName(value);
        }

        value = correlationActionRequest.getSignalName();
        if (value != null) {
            query.signalEventSubscriptionName(value);
        }

        List<QueryVariable> queryVariableList = correlationActionRequest.getCorrelationVariables();

        if (queryVariableList != null) {

            List<QueryVariable> updatedQueryVariableList = new ArrayList<>();
            for (QueryVariable queryVariable : queryVariableList) {
                if (queryVariable.getVariableOperation() == null) {
                    queryVariable.setOperation("equals");
                }
                updatedQueryVariableList.add(queryVariable);
            }
            addVariables(query, updatedQueryVariableList, true);
        }


*//*        value = correlationActionRequest.getActivityId();
        if (value != null) {
            query.activityId(value);
        }*//*

        value = correlationActionRequest.getTenantId();
        if (value != null) {
            query.executionTenantId(value);
        }

        QueryProperty qp = ALLOWED_SORT_PROPERTIES.get("processInstanceId");
        ((AbstractQuery) query).orderBy(qp);
        query.asc();

        List<Execution> executionList = query.listPage(0, 10);
        int size = executionList.size();
        if (size == 0) {
            throw new ActivitiIllegalArgumentException("No Executions found to correlate with
            given information");
        }

        if (size > 1) {
            throw new ActivitiIllegalArgumentException("More than one Executions found to
            correlate with given information");
        }

        Execution execution = executionList.get(0);

        String action = correlationActionRequest.getAction();
        if (CorrelationActionRequest.ACTION_SIGNAL.equals(action)) {
            if (correlationActionRequest.getVariables() != null) {
                runtimeService.signal(execution.getId(), getVariablesToSet(correlationActionRequest));
            } else {
                runtimeService.signal(execution.getId());
            }
        } else if (CorrelationActionRequest.ACTION_SIGNAL_EVENT_RECEIVED.equals(action)) {
            if (correlationActionRequest.getSignalName() == null) {
                throw new ActivitiIllegalArgumentException("Signal name is required");
            }
            if (correlationActionRequest.getVariables() != null) {
                runtimeService.signalEventReceived(correlationActionRequest.getSignalName(),
                 execution.getId(), getVariablesToSet(correlationActionRequest));
            } else {
                runtimeService.signalEventReceived(correlationActionRequest.getSignalName(),
                 execution.getId());
            }
        } else if (CorrelationActionRequest.ACTION_MESSAGE_EVENT_RECEIVED.equals(action)) {
            if (correlationActionRequest.getMessageName() == null) {
                throw new ActivitiIllegalArgumentException("Message name is required");
            }
            if (correlationActionRequest.getVariables() != null) {
                runtimeService.messageEventReceived(correlationActionRequest.getMessageName(),
                 execution.getId(), getVariablesToSet(correlationActionRequest));
            } else {
                runtimeService.messageEventReceived(correlationActionRequest.getMessageName(),
                execution.getId());
            }
        } else {
            throw new ActivitiIllegalArgumentException("Invalid action: '" +
            correlationActionRequest.getAction() + "'.");
        }

        Response.ResponseBuilder responseBuilder = Response.ok();
        // Re-fetch the execution, could have changed due to action or even completed
        execution = runtimeService.createExecutionQuery().executionId(execution.getId())
        .singleResult();
        if (execution == null) {
            // Execution is finished, return empty body to inform user
            responseBuilder.status(Response.Status.NO_CONTENT);
            return responseBuilder.build();
        } else {
            return responseBuilder.entity(new RestResponseFactory().
            createExecutionResponse(execution, uriInfo.getBaseUri()
                    .toString())).build();
        }
    }

    protected void addVariables(ExecutionQuery processInstanceQuery,
     List<QueryVariable> variables, boolean process) {
        for (QueryVariable variable : variables) {
            if (variable.getVariableOperation() == null) {
                throw new ActivitiIllegalArgumentException("Variable operation is missing
                for variable: " + variable.getName());
            }
            if (variable.getValue() == null) {
                throw new ActivitiIllegalArgumentException("Variable value is missing
                for variable: " + variable.getName());
            }

            boolean nameLess = variable.getName() == null;

            Object actualValue = new RestResponseFactory().getVariableValue(variable);

            // A value-only query is only possible using equals-operator
            if (nameLess && variable.getVariableOperation() !=
            QueryVariable.QueryVariableOperation.EQUALS) {
                throw new ActivitiIllegalArgumentException("Value-only query
                 (without a variable-name) is only supported when using 'equals' operation.");
            }

            switch (variable.getVariableOperation()) {

                case EQUALS:
                    if (nameLess) {
                        if (process) {
                            processInstanceQuery.processVariableValueEquals(actualValue);
                        } else {
                            processInstanceQuery.variableValueEquals(actualValue);
                        }
                    } else {
                        if (process) {
                            processInstanceQuery.processVariableValueEquals(
                            variable.getName(), actualValue);
                        } else {
                            processInstanceQuery.variableValueEquals(
                            variable.getName(), actualValue);
                        }
                    }
                    break;

                case EQUALS_IGNORE_CASE:
                    if (actualValue instanceof String) {
                        if (process) {
                            processInstanceQuery.processVariableValueEqualsIgnoreCase(
                            variable.getName(), (String) actualValue);
                        } else {
                            processInstanceQuery.variableValueEqualsIgnoreCase(
                            variable.getName(), (String) actualValue);
                        }
                    } else {
                        throw new ActivitiIllegalArgumentException("
                        Only string variable values are supported when ignoring casing, but was: "
                                + actualValue.getClass().getName());
                    }
                    break;

                case NOT_EQUALS:
                    if (process) {
                        processInstanceQuery.processVariableValueNotEquals(
                        variable.getName(), actualValue);
                    } else {
                        processInstanceQuery.variableValueNotEquals(
                        variable.getName(), actualValue);
                    }
                    break;

                case NOT_EQUALS_IGNORE_CASE:
                    if (actualValue instanceof String) {
                        if (process) {
                            processInstanceQuery.processVariableValueNotEqualsIgnoreCase
                            (variable.getName(), (String) actualValue);
                        } else {
                            processInstanceQuery.variableValueNotEqualsIgnoreCase
                            (variable.getName(), (String) actualValue);
                        }
                    } else {
                        throw new ActivitiIllegalArgumentException
                        ("Only string variable values are supported when ignoring
                         casing, but was: "
                                + actualValue.getClass().getName());
                    }
                    break;
                default:
                    throw new ActivitiIllegalArgumentException("
                    Unsupported variable query operation: " + variable.getVariableOperation());
            }
        }
    }
*/
}
