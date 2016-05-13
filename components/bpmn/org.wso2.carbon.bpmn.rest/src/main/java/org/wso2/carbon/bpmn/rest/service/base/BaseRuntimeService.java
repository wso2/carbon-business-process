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

package org.wso2.carbon.bpmn.rest.service.base;

//import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.handler.codec.http.HttpRequest;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.impl.ExecutionQueryProperty;
import org.activiti.engine.impl.persistence.entity.VariableInstanceEntity;
import org.activiti.engine.query.QueryProperty;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ExecutionQuery;
import org.wso2.carbon.bpmn.rest.common.RestResponseFactory;
import org.wso2.carbon.bpmn.rest.engine.variable.QueryVariable;
import org.wso2.carbon.bpmn.rest.engine.variable.RestVariable;
import org.wso2.carbon.bpmn.rest.internal.BPMNOSGIService;
import org.wso2.carbon.bpmn.rest.model.common.DataResponse;
import org.wso2.carbon.bpmn.rest.model.runtime.ExecutionActionRequest;
import org.wso2.carbon.bpmn.rest.model.runtime.ExecutionPaginateList;
import org.wso2.carbon.bpmn.rest.model.runtime.ExecutionQueryRequest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

/**
 *
 */
public class BaseRuntimeService {

    @Context
    protected UriInfo uriInfo;
    @Context
    protected HttpRequest req;
    @Context
    protected Request request;

    @Context
    protected HttpServletRequest httpServletRequest;

    private static Map<String, QueryProperty> allowedSortProperties =
            new HashMap<String, QueryProperty>();

    static {
        allowedSortProperties
                .put("processDefinitionId", ExecutionQueryProperty.PROCESS_DEFINITION_ID);
        allowedSortProperties
                .put("processDefinitionKey", ExecutionQueryProperty.PROCESS_DEFINITION_KEY);
        allowedSortProperties.put("processInstanceId", ExecutionQueryProperty.PROCESS_INSTANCE_ID);
        allowedSortProperties.put("tenantId", ExecutionQueryProperty.TENANT_ID);
    }

    protected RuntimeService runtimeService = BPMNOSGIService.getRumtimeService();
    //TODO:
   // protected ObjectMapper objectMapper = new ObjectMapper();

    protected DataResponse getQueryResponse(ExecutionQueryRequest queryRequest,
                                            Map<String, String> requestParams,
                                            String serverRootUrl) {

        ExecutionQuery query = runtimeService.createExecutionQuery();

        // Populate query based on request
        if (queryRequest.getId() != null) {
            query.executionId(queryRequest.getId());
            requestParams.put("id", queryRequest.getId());
        }
        if (queryRequest.getProcessInstanceId() != null) {
            query.processInstanceId(queryRequest.getProcessInstanceId());
            requestParams.put("processInstanceId", queryRequest.getProcessInstanceId());
        }
        if (queryRequest.getProcessDefinitionKey() != null) {
            query.processDefinitionKey(queryRequest.getProcessDefinitionKey());
            requestParams.put("processDefinitionKey", queryRequest.getProcessDefinitionKey());
        }
        if (queryRequest.getProcessDefinitionId() != null) {
            query.processDefinitionId(queryRequest.getProcessDefinitionId());
            requestParams.put("processDefinitionId", queryRequest.getProcessDefinitionId());
        }
        if (queryRequest.getProcessBusinessKey() != null) {
            query.processInstanceBusinessKey(queryRequest.getProcessBusinessKey());
            requestParams.put("processInstanceBusinessKey", queryRequest.getProcessBusinessKey());
        }
        if (queryRequest.getActivityId() != null) {
            query.activityId(queryRequest.getActivityId());
            requestParams.put("activityId", queryRequest.getActivityId());
        }
        if (queryRequest.getParentId() != null) {
            query.parentId(queryRequest.getParentId());
            requestParams.put("parentId", queryRequest.getParentId());
        }
        if (queryRequest.getMessageEventSubscriptionName() != null) {
            query.messageEventSubscriptionName(queryRequest.getMessageEventSubscriptionName());
            requestParams.put("messageEventSubscriptionName",
                              queryRequest.getMessageEventSubscriptionName());
        }
        if (queryRequest.getSignalEventSubscriptionName() != null) {
            query.signalEventSubscriptionName(queryRequest.getSignalEventSubscriptionName());
            requestParams.put("signalEventSubscriptionName",
                              queryRequest.getSignalEventSubscriptionName());
        }

        if (queryRequest.getVariables() != null) {
            addVariables(query, queryRequest.getVariables(), false);
        }

        if (queryRequest.getProcessInstanceVariables() != null) {
            addVariables(query, queryRequest.getProcessInstanceVariables(), true);
        }

        if (queryRequest.getTenantId() != null) {
            query.executionTenantId(queryRequest.getTenantId());
            requestParams.put("tenantId", queryRequest.getTenantId());
        }

        if (queryRequest.getTenantIdLike() != null) {
            query.executionTenantIdLike(queryRequest.getTenantIdLike());
            requestParams.put("tenantIdLike", queryRequest.getTenantIdLike());
        }

        if (Boolean.TRUE.equals(queryRequest.getWithoutTenantId())) {
            query.executionWithoutTenantId();
            requestParams.put("withoutTenantId", queryRequest.getWithoutTenantId().toString());
        }

        return new ExecutionPaginateList(new RestResponseFactory(), req.getUri())
                .paginateList(requestParams, queryRequest, query, "processInstanceId",
                              allowedSortProperties);
    }

    protected void addVariables(ExecutionQuery processInstanceQuery, List<QueryVariable> variables,
                                boolean process) {
        for (QueryVariable variable : variables) {
            if (variable.getVariableOperation() == null) {
                throw new ActivitiIllegalArgumentException(
                        "Variable operation is missing for variable: " + variable.getName());
            }
            if (variable.getValue() == null) {
                throw new ActivitiIllegalArgumentException(
                        "Variable value is missing for variable: " + variable.getName());
            }

            boolean nameLess = variable.getName() == null;

            Object actualValue = new RestResponseFactory().getVariableValue(variable);

            // A value-only query is only possible using equals-operator
            if (nameLess &&
                variable.getVariableOperation() != QueryVariable.QueryVariableOperation.EQUALS) {
                throw new ActivitiIllegalArgumentException(
                        "Value-only query (without a variable-name) is only supported when using " +
                        "'equals' operation.");
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
                        throw new ActivitiIllegalArgumentException(
                                "Only string variable values are supported when ignoring casing," +
                                " but was: " +
                                actualValue.getClass().getName());
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
                                "Only string variable values are supported when ignoring casing, " +
                                "but was: " +
                                actualValue.getClass().getName());
                    }
                    break;
                default:
                    throw new ActivitiIllegalArgumentException(
                            "Unsupported variable query operation: " +
                            variable.getVariableOperation());
            }
        }
    }

    protected Execution getExecutionFromRequest(String executionId) {
        Execution execution =
                runtimeService.createExecutionQuery().executionId(executionId).singleResult();
        if (execution == null) {
            throw new ActivitiObjectNotFoundException(
                    "Could not find an execution with id '" + executionId + "'.", Execution.class);
        }
        return execution;
    }

    protected Map<String, Object> getVariablesToSet(ExecutionActionRequest actionRequest) {
        Map<String, Object> variablesToSet = new HashMap<String, Object>();
        for (RestVariable var : actionRequest.getVariables()) {
            if (var.getName() == null) {
                throw new ActivitiIllegalArgumentException("Variable name is required");
            }

            Object actualVariableValue = new RestResponseFactory().getVariableValue(var);

            variablesToSet.put(var.getName(), actualVariableValue);
        }
        return variablesToSet;
    }

    protected List<RestVariable> processVariables(Execution execution, String scope,
                                                  int variableType) {
        List<RestVariable> result = new ArrayList<RestVariable>();
        Map<String, RestVariable> variableMap = new HashMap<String, RestVariable>();

        // Check if it's a valid execution to get the variables for
        RestVariable.RestVariableScope variableScope = RestVariable.getScopeFromString(scope);

        if (variableScope == null) {
            // Use both local and global variables
            addLocalVariables(execution, variableType, variableMap);
            addGlobalVariables(execution, variableType, variableMap);

        } else if (variableScope == RestVariable.RestVariableScope.GLOBAL) {
            addGlobalVariables(execution, variableType, variableMap);

        } else if (variableScope == RestVariable.RestVariableScope.LOCAL) {
            addLocalVariables(execution, variableType, variableMap);
        }

        // Get unique variables from map
        result.addAll(variableMap.values());
        return result;
    }

    protected void addLocalVariables(Execution execution, int variableType,
                                     Map<String, RestVariable> variableMap) {
        Map<String, Object> rawLocalvariables = runtimeService.getVariablesLocal(execution.getId());
        List<RestVariable> localVariables = new RestResponseFactory()
                .createRestVariables(rawLocalvariables, execution.getId(), variableType,
                                     RestVariable.RestVariableScope.LOCAL, req.getUri());

        for (RestVariable var : localVariables) {
            variableMap.put(var.getName(), var);
        }
    }

    protected void addGlobalVariables(Execution execution, int variableType,
                                      Map<String, RestVariable> variableMap) {
        Map<String, Object> rawVariables = runtimeService.getVariables(execution.getId());
        List<RestVariable> globalVariables = new RestResponseFactory()
                .createRestVariables(rawVariables, execution.getId(), variableType,
                                     RestVariable.RestVariableScope.GLOBAL, req.getUri());

        // Overlay global variables over local ones. In case they are present the values are
        // not overridden,
        // since local variables get precedence over global ones at all times.
        for (RestVariable var : globalVariables) {
            if (!variableMap.containsKey(var.getName())) {
                variableMap.put(var.getName(), var);
            }
        }
    }

    public void deleteAllLocalVariables(Execution execution) {
        Collection<String> currentVariables =
                runtimeService.getVariablesLocal(execution.getId()).keySet();
        runtimeService.removeVariablesLocal(execution.getId(), currentVariables);

        Response.noContent().build();
    }

/*    protected Object createExecutionVariable(Execution execution, boolean override,
int variableType) {

        Object result = null;
        if (request instanceof MultipartHttpServletRequest) {
            result = setBinaryVariable((MultipartHttpServletRequest) request, execution,
             variableType, true);
        } else {

            List<RestVariable> inputVariables = new ArrayList<RestVariable>();
            List<RestVariable> resultVariables = new ArrayList<RestVariable>();
            result = resultVariables;

            try {
                @SuppressWarnings("unchecked") List<Object> variableObjects = (List<Object>)
                objectMapper.readValue(httpServletRequest.getInputStream(), List.class);
                for (Object restObject : variableObjects) {
                    RestVariable restVariable = objectMapper.convertValue(restObject,
                    RestVariable.class);
                    inputVariables.add(restVariable);
                }
            } catch (Exception e) {
                throw new ActivitiIllegalArgumentException("Failed to serialize to a
                 RestVariable instance", e);
            }

            if (inputVariables == null || inputVariables.size() == 0) {
                throw new ActivitiIllegalArgumentException("Request didn't contain
                a list of variables to create.");
            }

            RestVariable.RestVariableScope sharedScope = null;
            RestVariable.RestVariableScope varScope = null;
            Map<String, Object> variablesToSet = new HashMap<String, Object>();

            for (RestVariable var : inputVariables) {
                // Validate if scopes match
                varScope = var.getVariableScope();
                if (var.getName() == null) {
                    throw new ActivitiIllegalArgumentException("Variable name is required");
                }

                if (varScope == null) {
                    varScope = RestVariable.RestVariableScope.LOCAL;
                }
                if (sharedScope == null) {
                    sharedScope = varScope;
                }
                if (varScope != sharedScope) {
                    throw new ActivitiIllegalArgumentException("Only allowed to
                    update multiple variables in the same scope.");
                }

                if (!override && hasVariableOnScope(execution, var.getName(), varScope)) {
                    throw new BPMNConflictException("Variable '" + var.getName() +
                     "' is already present on execution '" + execution.getId() + "'.");
                }

                Object actualVariableValue = new RestResponseFactory().getVariableValue(var);
                variablesToSet.put(var.getName(), actualVariableValue);
                resultVariables.add(new RestResponseFactory().createRestVariable(var.getName(),
                 actualVariableValue, varScope,
                        execution.getId(), variableType, false, uriInfo.getBaseUri().toString()));
            }

            if (!variablesToSet.isEmpty()) {
                if (sharedScope == RestVariable.RestVariableScope.LOCAL) {
                    runtimeService.setVariablesLocal(execution.getId(), variablesToSet);
                } else {
                    if (execution.getParentId() != null) {
                        // Explicitly set on parent, setting non-local variables on execution
                        itself will override local-variables if exists
                        runtimeService.setVariables(execution.getParentId(), variablesToSet);
                    } else {
                        // Standalone task, no global variables possible
                        throw new ActivitiIllegalArgumentException("Cannot set global variables
                        on execution '" + execution.getId() +"', task is not part of process.");
                    }
                }
            }
        }
        Response.created(uriInfo.getRequestUri());
        return result;
    }*/

/*
    protected RestVariable setBinaryVariable(MultipartHttpServletRequest request,
            Execution execution, int responseVariableType, boolean isNew) {

        // Validate input and set defaults
        if (request.getFileMap().size() == 0) {
            throw new ActivitiIllegalArgumentException("No file content was found in request body.");
        }

        // Get first file in the map, ignore possible other files
        MultipartFile file = request.getFile(request.getFileMap().keySet().iterator().next());

        if (file == null) {
            throw new ActivitiIllegalArgumentException("No file content was found in request body.");
        }

        String variableScope = null;
        String variableName = null;
        String variableType = null;

        Map<String, String[]> paramMap = request.getParameterMap();
        for (String parameterName : paramMap.keySet()) {

            if (paramMap.get(parameterName).length > 0) {

                if (parameterName.equalsIgnoreCase("scope")) {
                    variableScope = paramMap.get(parameterName)[0];

                } else if (parameterName.equalsIgnoreCase("name")) {
                    variableName = paramMap.get(parameterName)[0];

                } else if (parameterName.equalsIgnoreCase("type")) {
                    variableType = paramMap.get(parameterName)[0];
                }
            }
        }

        try {

            // Validate input and set defaults
            if (variableName == null) {
                throw new ActivitiIllegalArgumentException("No variable name was found in request
                 body.");
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
                byte[] variableBytes = IOUtils.toByteArray(file.getInputStream());
                setVariable(execution, variableName, variableBytes, scope, isNew);

            } else {
                // Try deserializing the object
                ObjectInputStream stream = new ObjectInputStream(file.getInputStream());
                Object value = stream.readObject();
                setVariable(execution, variableName, value, scope, isNew);
                stream.close();
            }

            if (responseVariableType == RestResponseFactory.VARIABLE_PROCESS) {
                return new RestResponseFactory().createBinaryRestVariable(variableName, scope,
                 variableType,
                        null, null, execution.getId(), uriInfo.getBaseUri().toString());
            } else {
                return new RestResponseFactory().createBinaryRestVariable(variableName, scope,
                 variableType, null,
                        execution.getId(), null, uriInfo.getBaseUri().toString());
            }

        } catch (IOException ioe) {
            throw new ActivitiIllegalArgumentException("Could not process multipart content", ioe);
        } catch (ClassNotFoundException ioe) {
            throw new BPMNContentNotSupportedException("The provided body contains a serialized
            object for which the class is nog found: " + ioe.getMessage());
        }

    }
*/

    protected void setVariable(Execution execution, String name, Object value,
                               RestVariable.RestVariableScope scope, boolean isNew) {
        // Create can only be done on new variables. Existing variables should be updated using PUT
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
        boolean variableFound = false;

        if (scope == RestVariable.RestVariableScope.GLOBAL) {
            if (execution.getParentId() != null &&
                runtimeService.hasVariable(execution.getParentId(), variableName)) {
                variableFound = true;
            }

        } else if (scope == RestVariable.RestVariableScope.LOCAL) {
            if (runtimeService.hasVariableLocal(execution.getId(), variableName)) {
                variableFound = true;
            }
        }
        return variableFound;
    }

    public RestVariable getVariableFromRequest(Execution execution, String variableName,
                                               String scope, boolean includeBinary) {

        boolean variableFound = false;
        Object value = null;

        if (execution == null) {
            throw new ActivitiObjectNotFoundException("Could not find an execution",
                                                      Execution.class);
        }

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
                                         includeBinary);
        }
    }

    protected RestVariable constructRestVariable(String variableName, Object value,
                                                 RestVariable.RestVariableScope variableScope,
                                                 String executionId, boolean includeBinary) {

        return new RestResponseFactory()
                .createRestVariable(variableName, value, variableScope, executionId,
                                    RestResponseFactory.VARIABLE_EXECUTION, includeBinary,
                                    req.getUri());
    }

}
