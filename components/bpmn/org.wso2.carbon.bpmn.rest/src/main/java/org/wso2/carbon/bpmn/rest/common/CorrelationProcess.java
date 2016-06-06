package org.wso2.carbon.bpmn.rest.common;

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.query.QueryProperty;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ExecutionQuery;
import org.wso2.carbon.bpmn.rest.engine.variable.QueryVariable;
import org.wso2.carbon.bpmn.rest.engine.variable.RestVariable;
import org.wso2.carbon.bpmn.rest.internal.RestServiceContentHolder;
import org.wso2.carbon.bpmn.rest.model.common.CorrelationQueryProperty;
import org.wso2.carbon.bpmn.rest.model.correlation.CorrelationActionRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.Response;

//import org.activiti.engine.impl.AbstractQuery;
//import org.activiti.engine.query.Query;
//import org.wso2.carbon.bpmn.rest.service.base.BaseExecutionService;

/**
 *
 */
public class CorrelationProcess {

    private static Map<String, QueryProperty> allowedSortProperties = new HashMap<>();

    static {
        allowedSortProperties
                .put("processInstanceId", CorrelationQueryProperty.PROCESS_INSTANCE_ID);
    }

    public CorrelationProcess() {
    }

    public Response getQueryResponse(CorrelationActionRequest correlationActionRequest,
                                     String name) {

        RuntimeService runtimeService = RestServiceContentHolder.getInstance().getRestService().getRumtimeService();
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

        value = correlationActionRequest.getTenantId();
        if (value != null) {
            query.executionTenantId(value);
        }

        //QueryProperty qp = ALLOWED_SORT_PROPERTIES.get("processInstanceId");
        // ((AbstractQuery) query).orderBy(qp);
        query.orderByProcessInstanceId();
        query.asc();

        List<Execution> executionList = query.listPage(0, 10);
        int size = executionList.size();
        if (size == 0) {
            throw new ActivitiIllegalArgumentException(
                    "No Executions found " + "to correlate with given information");
        }

        if (size > 1) {
            throw new ActivitiIllegalArgumentException(
                    "More than one Executions found " + "to correlate with given information");
        }

        Execution execution = executionList.get(0);

        String action = correlationActionRequest.getAction();
        if (CorrelationActionRequest.ACTION_SIGNAL.equals(action)) {
            if (correlationActionRequest.getVariables() != null) {
                runtimeService
                        .signal(execution.getId(), getVariablesToSet(correlationActionRequest));
            } else {
                runtimeService.signal(execution.getId());
            }
        } else if (CorrelationActionRequest.ACTION_SIGNAL_EVENT_RECEIVED.equals(action)) {
            if (correlationActionRequest.getSignalName() == null) {
                throw new ActivitiIllegalArgumentException("Signal name is required");
            }
            if (correlationActionRequest.getVariables() != null) {
                runtimeService.signalEventReceived(correlationActionRequest.getSignalName(),
                        execution.getId(),
                        getVariablesToSet(correlationActionRequest));
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
                        execution.getId(),
                        getVariablesToSet(correlationActionRequest));
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
        execution = runtimeService.createExecutionQuery().executionId(execution.getId()).
                singleResult();
        if (execution == null) {
            // Execution is finished, return empty body to inform user
            responseBuilder.status(Response.Status.NO_CONTENT);
            return responseBuilder.build();
        } else {
            return responseBuilder
                    .entity(new RestResponseFactory().createExecutionResponse(execution, name))
                    .build();
        }
    }

    protected void addVariables(ExecutionQuery processInstanceQuery, List<QueryVariable> variables,
                                boolean process) {
        for (QueryVariable variable : variables) {
            if (variable.getVariableOperation() == null) {
                throw new ActivitiIllegalArgumentException("Variable operation is" +
                        " missing for variable: " +
                        variable.getName());
            }
            if (variable.getValue() == null) {
                throw new ActivitiIllegalArgumentException("Variable value is missing " +
                        "for variable: " + variable.getName());
            }

            boolean nameLess = variable.getName() == null;

            Object actualValue = new RestResponseFactory().getVariableValue(variable);

            // A value-only query is only possible using equals-operator
            if (nameLess &&
                    variable.getVariableOperation() != QueryVariable.QueryVariableOperation.EQUALS) {
                throw new ActivitiIllegalArgumentException("Value-only query" +
                        " (without a variable-name) is only " +
                        "supported when using 'equals' operation.");
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
                            processInstanceQuery
                                    .processVariableValueEquals(variable.getName(), actualValue);
                        } else {
                            processInstanceQuery
                                    .variableValueEquals(variable.getName(), actualValue);
                        }
                    }
                    break;

                case EQUALS_IGNORE_CASE:
                    if (actualValue instanceof String) {
                        if (process) {
                            processInstanceQuery
                                    .processVariableValueEqualsIgnoreCase(variable.getName(),
                                            (String) actualValue);
                        } else {
                            processInstanceQuery.variableValueEqualsIgnoreCase(variable.getName(),
                                    (String) actualValue);
                        }
                    } else {
                        throw new ActivitiIllegalArgumentException("Only string variable values" +
                                " are supported when ignoring casing, but was: " +
                                actualValue.getClass()
                                        .getName());
                    }
                    break;

                case NOT_EQUALS:
                    if (process) {
                        processInstanceQuery
                                .processVariableValueNotEquals(variable.getName(), actualValue);
                    } else {
                        processInstanceQuery
                                .variableValueNotEquals(variable.getName(), actualValue);
                    }
                    break;

                case NOT_EQUALS_IGNORE_CASE:
                    if (actualValue instanceof String) {
                        if (process) {
                            processInstanceQuery
                                    .processVariableValueNotEqualsIgnoreCase(variable.getName(),
                                            (String) actualValue);
                        } else {
                            processInstanceQuery
                                    .variableValueNotEqualsIgnoreCase(variable.getName(),
                                            (String) actualValue);
                        }
                    } else {
                        throw new ActivitiIllegalArgumentException(
                                "Only string variable values are" +
                                        " supported when ignoring casing," +
                                        " but was: " + actualValue.getClass().getName());
                    }
                    break;
                default:
                    throw new ActivitiIllegalArgumentException(
                            "Unsupported variable query operation:" +
                                    " " + variable.getVariableOperation());
            }
        }
    }

    protected Map<String, Object> getVariablesToSet(
            CorrelationActionRequest correlationActionRequest) {
        Map<String, Object> variablesToSet = new HashMap<String, Object>();
        for (RestVariable var : correlationActionRequest.getVariables()) {
            if (var.getName() == null) {
                throw new ActivitiIllegalArgumentException("Variable name is required");
            }

            Object actualVariableValue = new RestResponseFactory().getVariableValue(var);

            variablesToSet.put(var.getName(), actualVariableValue);
        }
        return variablesToSet;
    }

}
