/**
 * Copyright (c) 2015 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.bpmn.rest.service.base;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.impl.ExecutionQueryProperty;
import org.activiti.engine.impl.persistence.entity.VariableInstanceEntity;
import org.activiti.engine.query.QueryProperty;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ExecutionQuery;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bpmn.rest.common.RestResponseFactory;
import org.wso2.carbon.bpmn.rest.engine.variable.QueryVariable;
import org.wso2.carbon.bpmn.rest.engine.variable.RestVariable;
import org.wso2.carbon.bpmn.rest.internal.RestServiceContentHolder;
import org.wso2.carbon.bpmn.rest.model.common.DataResponse;
import org.wso2.carbon.bpmn.rest.model.correlation.CorrelationActionRequest;
import org.wso2.carbon.bpmn.rest.model.runtime.ExecutionActionRequest;
import org.wso2.carbon.bpmn.rest.model.runtime.ExecutionPaginateList;
import org.wso2.carbon.bpmn.rest.model.runtime.ExecutionQueryRequest;
import org.wso2.msf4j.HttpStreamHandler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.Context;

//import org.wso2.carbon.bpmn.rest.common.exception.BPMNContentNotSupportedException;
//import org.wso2.carbon.bpmn.rest.common.utils.Utils;
//import org.wso2.msf4j.HttpStreamer;
//import javax.activation.DataHandler;

/**
 *
 */
public class BaseExecutionService {

    private static final Log log = LogFactory.getLog(BaseExecutionService.class);

    @Context //TODO:
            HttpStreamHandler multiPartBody;

    protected static final Map<String, QueryProperty> ALLOWED_SORT_PROPERTIES;
    protected static final List<String> ALL_PROPERTIES_LIST;

    static {
        HashMap<String, QueryProperty> sortMap = new HashMap<>();
        sortMap.put("processDefinitionId", ExecutionQueryProperty.PROCESS_DEFINITION_ID);
        sortMap.put("processDefinitionKey", ExecutionQueryProperty.PROCESS_DEFINITION_KEY);
        sortMap.put("processInstanceId", ExecutionQueryProperty.PROCESS_INSTANCE_ID);
        sortMap.put("tenantId", ExecutionQueryProperty.TENANT_ID);
        ALLOWED_SORT_PROPERTIES = Collections.unmodifiableMap(sortMap);
    }

    static {
        List<String> properties = new ArrayList<>();
        properties.add("start");
        properties.add("size");
        properties.add("order");
        properties.add("sort");
        properties.add("id");
        properties.add("activityId");
        properties.add("processDefinitionKey");
        properties.add("processDefinitionId");
        properties.add("processInstanceId");
        properties.add("messageEventSubscriptionName");
        properties.add("signalEventSubscriptionName");
        properties.add("parentId");
        properties.add("tenantId");
        properties.add("tenantIdLike");
        properties.add("withoutTenantId");
        ALL_PROPERTIES_LIST = Collections.unmodifiableList(properties);
    }

    protected DataResponse getQueryResponse(ExecutionQueryRequest queryRequest,
                                            Map<String, String> requestParams, String baseName) {

        RuntimeService runtimeService = RestServiceContentHolder.getInstance().getRestService().getRumtimeService();
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

        DataResponse dataResponse = new ExecutionPaginateList(new RestResponseFactory(), baseName)
                .paginateList(requestParams, queryRequest, query, "processInstanceId",
                        ALLOWED_SORT_PROPERTIES);
        return dataResponse;
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
        RuntimeService runtimeService = RestServiceContentHolder.getInstance().getRestService().getRumtimeService();
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

    protected List<RestVariable> processVariables(Execution execution, String scope,
                                                  int variableType, String baseName) {
        List<RestVariable> result = new ArrayList<RestVariable>();
        Map<String, RestVariable> variableMap = new HashMap<String, RestVariable>();

        // Check if it's a valid execution to get the variables for
        RestVariable.RestVariableScope variableScope = RestVariable.getScopeFromString(scope);

        if (variableScope == null) {
            // Use both local and global variables
            addLocalVariables(execution, variableType, variableMap, baseName);
            addGlobalVariables(execution, variableType, variableMap, baseName);

        } else if (variableScope == RestVariable.RestVariableScope.GLOBAL) {
            addGlobalVariables(execution, variableType, variableMap, baseName);

        } else if (variableScope == RestVariable.RestVariableScope.LOCAL) {
            addLocalVariables(execution, variableType, variableMap, baseName);
        }

        // Get unique variables from map
        result.addAll(variableMap.values());
        return result;
    }

    protected void addLocalVariables(Execution execution, int variableType,
                                     Map<String, RestVariable> variableMap, String baseName) {
        RuntimeService runtimeService = RestServiceContentHolder.getInstance().getRestService().getRumtimeService();
        Map<String, Object> rawLocalvariables = runtimeService.getVariablesLocal(execution.getId());
        List<RestVariable> localVariables = new RestResponseFactory()
                .createRestVariables(rawLocalvariables, execution.getId(), variableType,
                        RestVariable.RestVariableScope.LOCAL, baseName);

        for (RestVariable var : localVariables) {
            variableMap.put(var.getName(), var);
        }
    }

    protected void addGlobalVariables(Execution execution, int variableType,
                                      Map<String, RestVariable> variableMap, String baseName) {
        RuntimeService runtimeService = RestServiceContentHolder.getInstance().getRestService().getRumtimeService();
        Map<String, Object> rawVariables = runtimeService.getVariables(execution.getId());
        List<RestVariable> globalVariables = new RestResponseFactory()
                .createRestVariables(rawVariables, execution.getId(), variableType,
                        RestVariable.RestVariableScope.GLOBAL, baseName);

        // Overlay global variables over local ones. In case they are present the values are not
        // overridden,
        // since local variables get precedence over global ones at all times.
        for (RestVariable var : globalVariables) {
            if (!variableMap.containsKey(var.getName())) {
                variableMap.put(var.getName(), var);
            }
        }
    }

    public void deleteAllLocalVariables(Execution execution) {
        RuntimeService runtimeService = RestServiceContentHolder.getInstance().getRestService().getRumtimeService();
        Collection<String> currentVariables =
                runtimeService.getVariablesLocal(execution.getId()).keySet();
        runtimeService.removeVariablesLocal(execution.getId(), currentVariables);
    }
    //TODO:multipart


/*
    protected RestVariable createBinaryExecutionVariable(Execution execution, int
    responseVariableType, UriInfo
            uriInfo, boolean isNew, HttpStreamHandler httpStreamer) {
            //TEST
            httpStreamer.callback(new HttpStreamHandlerImpl());

        boolean debugEnabled = log.isDebugEnabled();
        Response.ResponseBuilder responseBuilder = Response.ok();

        List<org.apache.cxf.jaxrs.ext.multipart.Attachment> attachments =
        multipartBody.getAllAttachments();

        int attachmentSize = attachments.size();

        if (attachmentSize <= 0) {
            throw new ActivitiIllegalArgumentException("No Attachments found with the request body");
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

                Map<String, String> contentDispositionHeaderValueMap =
                Utils.processContentDispositionHeader
                        (contentDispositionHeaderValue);
                String dispositionName = contentDispositionHeaderValueMap.get("name");
                DataHandler dataHandler = attachment.getDataHandler();

                OutputStream outputStream = null;

                if ("name".equals(dispositionName)) {
                    try {
                        outputStream = Utils.getAttachmentStream(dataHandler.getInputStream());
                    } catch (IOException e) {
                        throw new ActivitiIllegalArgumentException("Attachment Name
                        Reading error occured", e);
                    }

                    if (outputStream != null) {
                        String fileName = outputStream.toString();
                        attachmentDataHolder.setName(fileName);
                    }


                } else if ("type".equals(dispositionName)) {
                    try {
                        outputStream = Utils.getAttachmentStream(dataHandler.getInputStream());
                    } catch (IOException e) {
                        throw new ActivitiIllegalArgumentException("Attachment Type
                         Reading error occured", e);
                    }

                    if (outputStream != null) {
                        String typeName = outputStream.toString();
                        attachmentDataHolder.setType(typeName);
                    }


                } else if ("scope".equals(dispositionName)) {
                    try {
                        outputStream = Utils.getAttachmentStream(dataHandler.getInputStream());
                    } catch (IOException e) {
                        throw new ActivitiIllegalArgumentException("Attachment Description
                         Reading error occured", e);
                    }

                    if (outputStream != null) {
                        String description = outputStream.toString();
                        attachmentDataHolder.setScope(description);
                    }
                }

                if (contentType != null) {
                    if ("file".equals(dispositionName)) {

                        InputStream inputStream = null;
                        try {
                            inputStream = dataHandler.getInputStream();
                        } catch (IOException e) {
                            throw new ActivitiIllegalArgumentException("Error Occured During
                             processing empty body.",
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

        if (attachmentDataHolder.getName() == null) {
            throw new ActivitiIllegalArgumentException("Attachment name is required.");
        }

        if (attachmentDataHolder.getAttachmentArray() == null) {
            throw new ActivitiIllegalArgumentException("Empty attachment body was found
             in request body after " +
                    "decoding the request" +
                    ".");
        }
        String variableScope = attachmentDataHolder.getScope();
        String variableName = attachmentDataHolder.getName();
        String variableType = attachmentDataHolder.getType();

        if (log.isDebugEnabled()) {
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
                !RestResponseFactory
                        .SERIALIZABLE_VARIABLE_TYPE.equals(variableType)) {
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
                InputStream inputStream = new ByteArrayInputStream
                (attachmentDataHolder.getAttachmentArray());
                ObjectInputStream stream = new ObjectInputStream(inputStream);
                Object value = stream.readObject();
                setVariable(execution, variableName, value, scope, isNew);
                stream.close();
            }

            if (responseVariableType == RestResponseFactory.VARIABLE_PROCESS) {
                return new RestResponseFactory().createBinaryRestVariable(variableName, scope,
                variableType,null, null, execution.getId(), uriInfo.getBaseUri().toString());
            } else {
                return new RestResponseFactory().createBinaryRestVariable(variableName,
                 scope, variableType, null,execution.getId(), null, uriInfo.getBaseUri().toString());
            }

        } catch (IOException ioe) {
            throw new ActivitiIllegalArgumentException("Could not process multipart content", ioe);
        } catch (ClassNotFoundException ioe) {
            throw new BPMNContentNotSupportedException("The provided body contains a serialized
             object for which the class is nog found: " + ioe
                    .getMessage());
        }

    }*/
//todo:
//    protected Response createExecutionVariable(Execution execution, boolean override,
//                                               int variableType,
//                                           Request httpServletRequest,
//                                               String baseName) {
//
//        Object result = null;
//        Response.ResponseBuilder responseBuilder = Response.ok();
//
//        List<RestVariable> inputVariables = new ArrayList<>();
//        List<RestVariable> resultVariables = new ArrayList<>();
//        String contentType = httpServletRequest.getContentType();
//
//        if (MediaType.APPLICATION_JSON.equals(contentType)) {
//            try {
//                ObjectMapper objectMapper = new ObjectMapper();
//                @SuppressWarnings("unchecked") List<Object> variableObjects =
//                        (List<Object>) objectMapper
//                                .readValue(httpServletRequest.getInputStream(), List.class);
//                for (Object restObject : variableObjects) {
//                    RestVariable restVariable =
//                            objectMapper.convertValue(restObject, RestVariable.class);
//                    inputVariables.add(restVariable);
//                }
//            } catch (IOException e) {
//                throw new ActivitiIllegalArgumentException(
//                        "Failed to serialize to a RestVariable instance", e);
//            }
//        } else if (MediaType.APPLICATION_XML.equals(contentType)) {
//            JAXBContext jaxbContext = null;
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
//
//        if (inputVariables.size() == 0) {
//            throw new ActivitiIllegalArgumentException(
//                    "Request didn't contain a list of variables to create.");
//        }
//
//        RestVariable.RestVariableScope sharedScope = null;
//        RestVariable.RestVariableScope varScope = null;
//        Map<String, Object> variablesToSet = new HashMap<String, Object>();
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
//            Object actualVariableValue = new RestResponseFactory().getVariableValue(var);
//            variablesToSet.put(var.getName(), actualVariableValue);
//            resultVariables.add(new RestResponseFactory()
//                                        .createRestVariable(var.getName(), actualVariableValue,
//                                                            varScope, execution.getId(),
//                                                            variableType, false, baseName));
//        }
//
//        if (!variablesToSet.isEmpty()) {
//            RuntimeService runtimeService = BPMNRestServiceImpl.getRumtimeService();
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
//        return responseBuilder.status(Response.Status.CREATED).build();
//    }

/*    protected RestVariable setBinaryVariable(@Context HttpServletRequest httpServletRequest,
                                             Execution execution, int responseVariableType,
                                              boolean isNew, UriInfo uriInfo) {

        byte[] byteArray = null;
        try {
            byteArray = Utils.processMultiPartFile(httpServletRequest, "file content");
        } catch (IOException e) {
            throw new ActivitiIllegalArgumentException("No file content was found in request
            body during multipart " +
                    "processing" +
                    ".");
        }

        if (byteArray == null) {
            throw new ActivitiIllegalArgumentException("No file content was found in request body.");

        }
        String variableScope = uriInfo.getQueryParameters().getFirst("scope");
        String variableName = uriInfo.getQueryParameters().getFirst("name");
        String variableType = uriInfo.getQueryParameters().getFirst("type");

        if (log.isDebugEnabled()) {
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
                    throw new ActivitiIllegalArgumentException("Only 'binary' and 'serializable'
                     are supported as variable type.");
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
                setVariable(execution, variableName, byteArray, scope, isNew);

            } else {
                // Try deserializing the object
                InputStream inputStream = new ByteArrayInputStream(byteArray);
                ObjectInputStream stream = new ObjectInputStream(inputStream);
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
             object for which the class is nog found: " + ioe
                    .getMessage());
        }

    }*/

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
        boolean variableFound = false;
        RuntimeService runtimeService = RestServiceContentHolder.getInstance().getRestService().getRumtimeService();
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
                                               String scope, boolean includeBinary,
                                               String baseName) {

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
                    includeBinary, baseName);
        }
    }

    protected RestVariable constructRestVariable(String variableName, Object value,
                                                 RestVariable.RestVariableScope variableScope,
                                                 String executionId, boolean includeBinary,
                                                 String baseName) {

        return new RestResponseFactory()
                .createRestVariable(variableName, value, variableScope, executionId,
                        RestResponseFactory.VARIABLE_EXECUTION, includeBinary,
                        baseName);
    }

    protected RestVariable setSimpleVariable(RestVariable restVariable, Execution execution,
                                             boolean isNew, String baseName) {
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

        return constructRestVariable(restVariable.getName(), restVariable.getValue(), scope,
                execution.getId(), false, baseName);
    }
}
