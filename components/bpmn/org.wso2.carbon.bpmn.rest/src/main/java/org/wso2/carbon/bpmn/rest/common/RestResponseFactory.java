/**
 *  Copyright (c) 2015 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.bpmn.rest.common;

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.form.FormData;
import org.activiti.engine.form.FormProperty;
import org.activiti.engine.form.StartFormData;
import org.activiti.engine.form.TaskFormData;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricDetail;
import org.activiti.engine.history.HistoricFormProperty;
import org.activiti.engine.history.HistoricIdentityLink;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.history.HistoricVariableInstance;
import org.activiti.engine.history.HistoricVariableUpdate;
import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.User;
import org.activiti.engine.impl.bpmn.deployer.BpmnDeployer;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.Model;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.Job;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Attachment;
import org.activiti.engine.task.Comment;
import org.activiti.engine.task.Event;
import org.activiti.engine.task.IdentityLink;
import org.activiti.engine.task.Task;
import org.apache.commons.lang3.StringUtils;
import org.wso2.carbon.bpmn.rest.common.utils.Utils;
import org.wso2.carbon.bpmn.rest.engine.variable.QueryVariable;
import org.wso2.carbon.bpmn.rest.engine.variable.RestVariable;
import org.wso2.carbon.bpmn.rest.model.common.RestIdentityLink;
import org.wso2.carbon.bpmn.rest.model.form.FormDataResponse;
import org.wso2.carbon.bpmn.rest.model.form.RestEnumFormProperty;
import org.wso2.carbon.bpmn.rest.model.form.RestFormProperty;
import org.wso2.carbon.bpmn.rest.model.history.HistoricActivityInstanceResponse;
import org.wso2.carbon.bpmn.rest.model.history.HistoricDetailResponse;
import org.wso2.carbon.bpmn.rest.model.history.HistoricIdentityLinkResponse;
import org.wso2.carbon.bpmn.rest.model.history.HistoricProcessInstanceResponse;
import org.wso2.carbon.bpmn.rest.model.history.HistoricVariableInstanceResponse;
import org.wso2.carbon.bpmn.rest.model.identity.GroupResponse;
import org.wso2.carbon.bpmn.rest.model.identity.UserInfoResponse;
import org.wso2.carbon.bpmn.rest.model.identity.UserResponse;
import org.wso2.carbon.bpmn.rest.model.repository.DeploymentResourceResponse;
import org.wso2.carbon.bpmn.rest.model.repository.DeploymentResourceResponseCollection;
import org.wso2.carbon.bpmn.rest.model.repository.DeploymentResponse;
import org.wso2.carbon.bpmn.rest.model.repository.ModelResponse;
import org.wso2.carbon.bpmn.rest.model.repository.ProcessDefinitionResponse;
import org.wso2.carbon.bpmn.rest.model.runtime.AttachmentResponse;
import org.wso2.carbon.bpmn.rest.model.runtime.CommentResponse;
import org.wso2.carbon.bpmn.rest.model.runtime.EventResponse;
import org.wso2.carbon.bpmn.rest.model.runtime.ExecutionResponse;
import org.wso2.carbon.bpmn.rest.model.runtime.HistoricTaskInstanceResponse;
import org.wso2.carbon.bpmn.rest.model.runtime.JobResponse;
import org.wso2.carbon.bpmn.rest.model.runtime.ProcessInstanceResponse;
import org.wso2.carbon.bpmn.rest.model.runtime.TableResponse;
import org.wso2.carbon.bpmn.rest.model.runtime.TaskResponse;
import org.wso2.carbon.bpmn.rest.model.runtime.variable.BooleanRestVariableConverter;
import org.wso2.carbon.bpmn.rest.model.runtime.variable.DateRestVariableConverter;
import org.wso2.carbon.bpmn.rest.model.runtime.variable.DoubleRestVariableConverter;
import org.wso2.carbon.bpmn.rest.model.runtime.variable.IntegerRestVariableConverter;
import org.wso2.carbon.bpmn.rest.model.runtime.variable.LongRestVariableConverter;
import org.wso2.carbon.bpmn.rest.model.runtime.variable.RestVariableConverter;
import org.wso2.carbon.bpmn.rest.model.runtime.variable.ShortRestVariableConverter;
import org.wso2.carbon.bpmn.rest.model.runtime.variable.StringRestVariableConverter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class RestResponseFactory {

    public static final int VARIABLE_TASK = 1;
    public static final int VARIABLE_EXECUTION = 2;
    public static final int VARIABLE_PROCESS = 3;
    public static final int VARIABLE_HISTORY_TASK = 4;
    public static final int VARIABLE_HISTORY_PROCESS = 5;
    public static final int VARIABLE_HISTORY_VARINSTANCE = 6;
    public static final int VARIABLE_HISTORY_DETAIL = 7;

    public static final String BYTE_ARRAY_VARIABLE_TYPE = "binary";
    public static final String SERIALIZABLE_VARIABLE_TYPE = "serializable";

    protected List<RestVariableConverter> variableConverters = new ArrayList<>();

    public RestResponseFactory() {
        initializeVariableConverters();
    }

    public List<DeploymentResponse> createDeploymentResponseList(List<Deployment> deployments) {
        List<DeploymentResponse> responseList = new ArrayList<>();
        for (Deployment instance : deployments) {
            responseList.add(createDeploymentResponse(instance));
        }
        return responseList;
    }

    protected RestUrlBuilder createUrlBuilder(String baseUri) {
        return RestUrlBuilder.fromCurrentRequest(baseUri);
    }

    public DeploymentResponse createDeploymentResponse(Deployment deployment) {
        RestUrlBuilder urlBuilder = new RestUrlBuilder();
        return new DeploymentResponse(deployment, urlBuilder.buildUrl(RestUrls.URL_DEPLOYMENT, deployment.getId()));
    }

    public DeploymentResourceResponseCollection createDeploymentResourceResponseList(
            String deploymentId, List<String> resourceList) {
        // Add additional metadata to the artifact-strings before returning
        List<DeploymentResourceResponse> responseList = new ArrayList<>();
        for (String resourceId : resourceList) {
            responseList.add(createDeploymentResourceResponse(deploymentId, resourceId,
                                                              Utils.resolveContentType(
                                                                      resourceId)));
        }
        DeploymentResourceResponseCollection deploymentResourceResponseCollection =
                new DeploymentResourceResponseCollection();
        deploymentResourceResponseCollection.setDeploymentResourceResponseList(responseList);
        return deploymentResourceResponseCollection;
    }

    public DeploymentResourceResponse createDeploymentResourceResponse(String deploymentId,
                                                                       String resourceId,
                                                                       String contentType) {
        RestUrlBuilder urlBuilder = new RestUrlBuilder();
        // Create URL's
        String resourceUrl =
                urlBuilder.buildUrl(RestUrls.URL_DEPLOYMENT_RESOURCE, deploymentId, resourceId);
        String resourceContentUrl = urlBuilder
                .buildUrl(RestUrls.URL_DEPLOYMENT_RESOURCE_CONTENT, deploymentId, resourceId);

        // Determine type
        String type = "resource";
        for (String suffix : BpmnDeployer.BPMN_RESOURCE_SUFFIXES) {
            if (resourceId.endsWith(suffix)) {
                type = "processDefinition";
                break;
            }
        }
        return new DeploymentResourceResponse(resourceId, resourceUrl, resourceContentUrl,
                                              contentType, type);
    }

    public List<ModelResponse> createModelResponseList(List<Model> models) {
        // RestUrlBuilder urlBuilder = createUrlBuilder(baseUri);
        List<ModelResponse> responseList = new ArrayList<>();
        for (Model instance : models) {
            responseList.add(createModelResponse(instance));
        }
        return responseList;
    }

    public List<ProcessDefinitionResponse> createProcessDefinitionResponseList(
            List<ProcessDefinition> processDefinitions) {
        List<ProcessDefinitionResponse> responseList = new ArrayList<>();
        for (ProcessDefinition instance : processDefinitions) {
            responseList.add(createProcessDefinitionResponse(instance));
        }
        return responseList;
    }

    public ModelResponse createModelResponse(Model model) {
        RestUrlBuilder urlBuilder = new RestUrlBuilder();
        ModelResponse modelResponse = new ModelResponse();

        modelResponse.setCategory(model.getCategory());
        modelResponse.setCreateTime(model.getCreateTime());
        modelResponse.setId(model.getId());
        modelResponse.setKey(model.getKey());
        modelResponse.setLastUpdateTime(model.getLastUpdateTime());
        modelResponse.setMetaInfo(model.getMetaInfo());
        modelResponse.setName(model.getName());
        modelResponse.setDeploymentId(model.getDeploymentId());
        modelResponse.setVersion(model.getVersion());
        modelResponse.setTenantId(model.getTenantId());

        modelResponse.setUrl(urlBuilder.buildUrl(RestUrls.URL_MODEL, model.getId()));
        if (model.getDeploymentId() != null) {
            modelResponse.setDeploymentUrl(
                    urlBuilder.buildUrl(RestUrls.URL_DEPLOYMENT, model.getDeploymentId()));
        }

        if (model.hasEditorSource()) {
            modelResponse.setSourceUrl(urlBuilder.buildUrl(RestUrls.URL_MODEL_SOURCE, model.getId()));
        }

        if (model.hasEditorSourceExtra()) {
            modelResponse.setSourceExtraUrl(
                    urlBuilder.buildUrl(RestUrls.URL_MODEL_SOURCE_EXTRA, model.getId()));
        }

        return modelResponse;
    }

    public ProcessDefinitionResponse createProcessDefinitionResponse(
            ProcessDefinition processDefinition) {
        ProcessDefinitionResponse response = new ProcessDefinitionResponse();
        RestUrlBuilder urlBuilder = new RestUrlBuilder();

        response.setUrl(
                urlBuilder.buildUrl(RestUrls.URL_PROCESS_DEFINITION, processDefinition.getId()));
        response.setId(processDefinition.getId());
        response.setKey(processDefinition.getKey());
        response.setVersion(processDefinition.getVersion());
        response.setCategory(processDefinition.getCategory());
        response.setName(processDefinition.getName());
        response.setDescription(processDefinition.getDescription());
        response.setSuspended(processDefinition.isSuspended());
        response.setStartFormDefined(processDefinition.hasStartFormKey());
        response.setGraphicalNotationDefined(processDefinition.hasGraphicalNotation());
        response.setTenantId(processDefinition.getTenantId());

        // Links to other resources
        response.setDeploymentId(processDefinition.getDeploymentId());
        response.setDeploymentUrl(
                urlBuilder.buildUrl(RestUrls.URL_DEPLOYMENT, processDefinition.getDeploymentId()));
        response.setResource(urlBuilder.buildUrl(RestUrls.URL_DEPLOYMENT_RESOURCE,
                                                 processDefinition.getDeploymentId(),
                                                 processDefinition.getResourceName()));
        if (processDefinition.getDiagramResourceName() != null) {
            response.setDiagramResource(urlBuilder.buildUrl(RestUrls.URL_DEPLOYMENT_RESOURCE,
                                                            processDefinition.getDeploymentId(),
                                                            processDefinition
                                                                    .getDiagramResourceName()));
        }
        return response;
    }

    public List<RestIdentityLink> createRestIdentityLinks(List<IdentityLink> links) {
        List<RestIdentityLink> responseList = new ArrayList<>();
        for (IdentityLink instance : links) {
            responseList.add(createRestIdentityLink(instance));
        }
        return responseList;
    }

    public RestIdentityLink createRestIdentityLink(IdentityLink link) {

        return createRestIdentityLink(link.getType(), link.getUserId(), link.getGroupId(),
                                      link.getTaskId(), link.getProcessDefinitionId(),
                                      link.getProcessInstanceId());
    }

    public RestIdentityLink createRestIdentityLink(String type, String userId, String groupId,
                                                   String taskId, String processDefinitionId,
                                                   String processInstanceId) {
        RestUrlBuilder urlBuilder = new RestUrlBuilder();
        RestIdentityLink result = new RestIdentityLink();
        result.setUser(userId);
        result.setGroup(groupId);
        result.setType(type);

        String family;
        if (userId != null) {
            family = RestUrls.SEGMENT_IDENTITYLINKS_FAMILY_USERS;
        } else {
            family = RestUrls.SEGMENT_IDENTITYLINKS_FAMILY_GROUPS;
        }
        if (processDefinitionId != null) {
            result.setUrl(urlBuilder.buildUrl(RestUrls.URL_PROCESS_DEFINITION_IDENTITYLINK,
                                              processDefinitionId, family,
                                              (userId != null ? userId : groupId)));
        } else if (taskId != null) {
            result.setUrl(urlBuilder.buildUrl(RestUrls.URL_TASK_IDENTITYLINK, taskId, family,
                                              (userId != null ? userId : groupId), type));
        } else {
            result.setUrl(urlBuilder.buildUrl(RestUrls.URL_PROCESS_INSTANCE_IDENTITYLINK,
                                              processInstanceId,
                                              (userId != null ? userId : groupId), type));
        }
        return result;
    }

    public Object getVariableValue(RestVariable restVariable) {
        Object value;

        if (restVariable.getType() != null) {
            // Try locating a converter if the type has been specified
            RestVariableConverter converter = null;
            for (RestVariableConverter conv : variableConverters) {
                if (conv.getRestTypeName().equals(restVariable.getType())) {
                    converter = conv;
                    break;
                }
            }
            if (converter == null) {
                throw new ActivitiIllegalArgumentException(
                        "Variable '" + restVariable.getName() + "' has unsupported type: '" +
                        restVariable.getType() + "'.");
            }
            value = converter.getVariableValue(restVariable);

        } else {
            // Revert to type determined by REST-to-Java mapping when no explicit type has been provided
            value = restVariable.getValue();
        }
        return value;
    }

    public List<ProcessInstanceResponse> createProcessInstanceResponseList(
            List<ProcessInstance> processInstances) {
        List<ProcessInstanceResponse> responseList = new ArrayList<>();
        for (ProcessInstance instance : processInstances) {
            responseList.add(createProcessInstanceResponse(instance));
        }
        return responseList;
    }

/*    public ProcessInstanceResponse createProcessInstanceResponse(ProcessInstance processInstance, String baseUri) {
        return createProcessInstanceResponse(processInstance,baseUri);
    }*/

    public ProcessInstanceResponse createProcessInstanceResponse(ProcessInstance processInstance,
                                                                 boolean returnVariables,
                                                                 Map<String, Object> runtimeVariableMap,
                                                                 List<HistoricVariableInstance> historicVariableList) {

        RestUrlBuilder urlBuilder = new RestUrlBuilder();
        ProcessInstanceResponse result = new ProcessInstanceResponse();
        result.setActivityId(processInstance.getActivityId());
        result.setBusinessKey(processInstance.getBusinessKey());
        result.setId(processInstance.getId());
        result.setProcessDefinitionId(processInstance.getProcessDefinitionId());
        result.setProcessDefinitionUrl(urlBuilder.buildUrl(RestUrls.URL_PROCESS_DEFINITION,
                                                           processInstance.getProcessDefinitionId()));
        result.setEnded(processInstance.isEnded());
        result.setSuspended(processInstance.isSuspended());
        result.setUrl(urlBuilder.buildUrl(RestUrls.URL_PROCESS_INSTANCE, processInstance.getId()));
        result.setTenantId(processInstance.getTenantId());

        //Added by Ryan Johnston
        if (processInstance.isEnded()) {
            //Process complete. Note the same in the result.
            result.setCompleted(true);
        } else {
            //Process not complete. Note the same in the result.
            result.setCompleted(false);
        }

        if (returnVariables) {

            if (processInstance.isEnded()) {
                if (historicVariableList != null) {
                    for (HistoricVariableInstance historicVariable : historicVariableList) {
                        result.addVariable(createRestVariable(historicVariable.getVariableName(),
                                                              historicVariable.getValue(),
                                                              RestVariable.RestVariableScope.LOCAL,
                                                              processInstance.getId(),
                                                              VARIABLE_PROCESS, false));
                    }
                }

            } else {
                if (runtimeVariableMap != null) {
                    for (String name : runtimeVariableMap.keySet()) {
                        result.addVariable(createRestVariable(name, runtimeVariableMap.get(name),
                                                              RestVariable.RestVariableScope.LOCAL,
                                                              processInstance.getId(),
                                                              VARIABLE_PROCESS, false));
                    }
                }
            }
        }
        //End Added by Ryan Johnston

        return result;
    }

    /*public RestVariable createRestVariable(String name, Object value, RestVariable.RestVariableScope
     scope,
                                           String id, int variableType, boolean includeBinaryValue
                                           , String baseUri){

        return createRestVariable(name, value, scope, id, variableType, includeBinaryValue, baseUri);
    }*/

    public RestVariable createRestVariable(String name, Object value,
                                           RestVariable.RestVariableScope scope, String id,
                                           int variableType, boolean includeBinaryValue) {
        RestUrlBuilder urlBuilder = new RestUrlBuilder();
        RestVariableConverter converter = null;
        RestVariable restVar = new RestVariable();
        restVar.setVariableScope(scope);
        restVar.setName(name);

        if (value != null) {
            // Try converting the value
            for (RestVariableConverter c : variableConverters) {
                if (c.getVariableType().isAssignableFrom(value.getClass())) {
                    converter = c;
                    break;
                }
            }

            if (converter != null) {
                converter.convertVariableValue(value, restVar);
                restVar.setType(converter.getRestTypeName());
            } else {
                // Revert to default conversion, which is the serializable/byte-array form
                if (value instanceof Byte[] || value instanceof byte[]) {
                    restVar.setType(BYTE_ARRAY_VARIABLE_TYPE);
                } else {
                    restVar.setType(SERIALIZABLE_VARIABLE_TYPE);
                }

                if (includeBinaryValue) {
                    restVar.setValue(value);
                }

                if (variableType == VARIABLE_TASK) {
                    restVar.setValueUrl(
                            urlBuilder.buildUrl(RestUrls.URL_TASK_VARIABLE_DATA, id, name));
                } else if (variableType == VARIABLE_EXECUTION) {
                    restVar.setValueUrl(
                            urlBuilder.buildUrl(RestUrls.URL_EXECUTION_VARIABLE_DATA, id, name));
                } else if (variableType == VARIABLE_PROCESS) {
                    restVar.setValueUrl(urlBuilder.buildUrl(
                            RestUrls.URL_PROCESS_INSTANCE_VARIABLE_DATA, id, name));
                } else if (variableType == VARIABLE_HISTORY_TASK) {
                    restVar.setValueUrl(urlBuilder.buildUrl(
                            RestUrls.URL_HISTORIC_TASK_INSTANCE_VARIABLE_DATA, id, name));
                } else if (variableType == VARIABLE_HISTORY_PROCESS) {
                    restVar.setValueUrl(urlBuilder.buildUrl(
                            RestUrls.URL_HISTORIC_PROCESS_INSTANCE_VARIABLE_DATA, id, name));
                } else if (variableType == VARIABLE_HISTORY_VARINSTANCE) {
                    restVar.setValueUrl(
                            urlBuilder.buildUrl(RestUrls.URL_HISTORIC_VARIABLE_INSTANCE_DATA, id));
                } else if (variableType == VARIABLE_HISTORY_DETAIL) {
                    restVar.setValueUrl(
                            urlBuilder.buildUrl(RestUrls.URL_HISTORIC_DETAIL_VARIABLE_DATA, id));
                }
            }
        }
        return restVar;
    }

    public RestVariable createBinaryRestVariable(String name, RestVariable.RestVariableScope scope,
                                                 String type, String taskId, String executionId,
                                                 String processInstanceId, String baseUri) {

        RestUrlBuilder urlBuilder = createUrlBuilder(baseUri);
        RestVariable restVar = new RestVariable();
        restVar.setVariableScope(scope);
        restVar.setName(name);
        restVar.setType(type);

        if (taskId != null) {
            restVar.setValueUrl(urlBuilder.buildUrl(RestUrls.URL_TASK_VARIABLE_DATA, taskId, name));
        }
        if (executionId != null) {
            restVar.setValueUrl(
                    urlBuilder.buildUrl(RestUrls.URL_EXECUTION_VARIABLE_DATA, executionId, name));
        }
        if (processInstanceId != null) {
            restVar.setValueUrl(urlBuilder.buildUrl(RestUrls.URL_PROCESS_INSTANCE_VARIABLE_DATA,
                                                    processInstanceId, name));
        }

        return restVar;
    }

    public ProcessInstanceResponse createProcessInstanceResponse(ProcessInstance processInstance) {

        RestUrlBuilder urlBuilder = new RestUrlBuilder();
        ProcessInstanceResponse result = new ProcessInstanceResponse();
        result.setActivityId(processInstance.getActivityId());
        result.setBusinessKey(processInstance.getBusinessKey());
        result.setId(processInstance.getId());
        result.setProcessDefinitionId(processInstance.getProcessDefinitionId());
        result.setProcessDefinitionUrl(urlBuilder.buildUrl(RestUrls.URL_PROCESS_DEFINITION,
                                                           processInstance.getProcessDefinitionId()));
        result.setEnded(processInstance.isEnded());
        result.setSuspended(processInstance.isSuspended());
        result.setUrl(urlBuilder.buildUrl(RestUrls.URL_PROCESS_INSTANCE, processInstance.getId()));
        result.setTenantId(processInstance.getTenantId());

        //Added by Ryan Johnston
        if (processInstance.isEnded()) {
            //Process complete. Note the same in the result.
            result.setCompleted(true);
        } else {
            //Process not complete. Note the same in the result.
            result.setCompleted(false);
        }

        if (processInstance.getProcessVariables() != null) {
            Map<String, Object> variableMap = processInstance.getProcessVariables();
            for (String name : variableMap.keySet()) {
                result.addVariable(createRestVariable(name, variableMap.get(name),
                                                      RestVariable.RestVariableScope.LOCAL,
                                                      processInstance.getId(), VARIABLE_PROCESS,
                                                      false));
            }
        }

        return result;
    }

    public Object getVariableValue(QueryVariable restVariable) {
        Object value;

        if (restVariable.getType() != null) {
            // Try locating a converter if the type has been specified
            RestVariableConverter converter = null;
            for (RestVariableConverter conv : variableConverters) {
                if (conv.getRestTypeName().equals(restVariable.getType())) {
                    converter = conv;
                    break;
                }
            }
            if (converter == null) {
                throw new ActivitiIllegalArgumentException(
                        "Variable '" + restVariable.getName() + "' has unsupported type: '" +
                        restVariable.getType() + "'.");
            }

            RestVariable temp = new RestVariable();
            temp.setValue(restVariable.getValue());
            temp.setType(restVariable.getType());
            temp.setName(restVariable.getName());
            value = converter.getVariableValue(temp);

        } else {
            // Revert to type determined by REST-to-Java mapping when no explicit type has been provided
            value = restVariable.getValue();
        }
        return value;
    }

    public TaskResponse createTaskResponse(Task task) {
        RestUrlBuilder urlBuilder = new RestUrlBuilder();
        TaskResponse response = new TaskResponse(task);
        response.setUrl(urlBuilder.buildUrl(RestUrls.URL_TASK, task.getId()));

        // Add references to other resources, if needed
        if (response.getParentTaskId() != null) {
            response.setParentTaskUrl(
                    urlBuilder.buildUrl(RestUrls.URL_TASK, response.getParentTaskId()));
        }
        if (response.getProcessDefinitionId() != null) {
            response.setProcessDefinitionUrl(urlBuilder.buildUrl(RestUrls.URL_PROCESS_DEFINITION,
                                                                 response.getProcessDefinitionId()));
        }
        if (response.getExecutionId() != null) {
            response.setExecutionUrl(
                    urlBuilder.buildUrl(RestUrls.URL_EXECUTION, response.getExecutionId()));
        }
        if (response.getProcessInstanceId() != null) {
            response.setProcessInstanceUrl(urlBuilder.buildUrl(RestUrls.URL_PROCESS_INSTANCE,
                                                               response.getProcessInstanceId()));
        }

        if (task.getProcessVariables() != null) {
            Map<String, Object> variableMap = task.getProcessVariables();
            for (String name : variableMap.keySet()) {
                response.addVariable(createRestVariable(name, variableMap.get(name),
                                                        RestVariable.RestVariableScope.GLOBAL,
                                                        task.getId(), VARIABLE_TASK, false));
            }
        }
        if (task.getTaskLocalVariables() != null) {
            Map<String, Object> variableMap = task.getTaskLocalVariables();
            for (String name : variableMap.keySet()) {
                response.addVariable(createRestVariable(name, variableMap.get(name),
                                                        RestVariable.RestVariableScope.LOCAL,
                                                        task.getId(), VARIABLE_TASK, false));
            }
        }

        return response;
    }

    public List<TaskResponse> createTaskResponseList(List<Task> tasks) {
        List<TaskResponse> responseList = new ArrayList<>();
        for (Task instance : tasks) {
            responseList.add(createTaskResponse(instance));
        }
        return responseList;
    }

    public List<RestVariable> createRestVariables(Map<String, Object> variables, String id,
                                                  int variableType,
                                                  RestVariable.RestVariableScope scope) {
        List<RestVariable> result = new ArrayList<>();

        for (Map.Entry<String, Object> pair : variables.entrySet()) {
            result.add(createRestVariable(pair.getKey(), pair.getValue(), scope, id, variableType,
                                          false));
        }

        return result;
    }

    public AttachmentResponse createAttachmentResponse(Attachment attachment) {
        RestUrlBuilder urlBuilder = new RestUrlBuilder();
        AttachmentResponse result = new AttachmentResponse();
        result.setId(attachment.getId());
        result.setName(attachment.getName());
        result.setDescription(attachment.getDescription());
        result.setTime(attachment.getTime());
        result.setType(attachment.getType());
        result.setUserId(attachment.getUserId());

        if (attachment.getUrl() == null && attachment.getTaskId() != null) {
            // Attachment content can be streamed
            result.setContentUrl(urlBuilder.buildUrl(RestUrls.URL_TASK_ATTACHMENT_DATA,
                                                     attachment.getTaskId(), attachment.getId()));
        } else {
            result.setExternalUrl(attachment.getUrl());
        }

        if (attachment.getTaskId() != null) {
            result.setUrl(urlBuilder.buildUrl(RestUrls.URL_TASK_ATTACHMENT, attachment.getTaskId(),
                                              attachment.getId()));
            result.setTaskUrl(urlBuilder.buildUrl(RestUrls.URL_TASK, attachment.getTaskId()));
        }
        if (attachment.getProcessInstanceId() != null) {
            result.setProcessInstanceUrl(urlBuilder.buildUrl(RestUrls.URL_PROCESS_INSTANCE,
                                                             attachment.getProcessInstanceId()));
        }
        return result;
    }

    public CommentResponse createRestComment(Comment comment) {
        RestUrlBuilder urlBuilder = new RestUrlBuilder();
        CommentResponse result = new CommentResponse();
        result.setAuthor(comment.getUserId());
        result.setMessage(comment.getFullMessage());
        result.setId(comment.getId());
        result.setTime(comment.getTime());
        result.setTaskId(comment.getTaskId());
        result.setProcessInstanceId(comment.getProcessInstanceId());

        if (comment.getTaskId() != null) {
            result.setTaskUrl(urlBuilder.buildUrl(RestUrls.URL_TASK_COMMENT, comment.getTaskId(),
                                                  comment.getId()));
        }

        if (comment.getProcessInstanceId() != null) {
            result.setProcessInstanceUrl(urlBuilder.buildUrl(
                    RestUrls.URL_HISTORIC_PROCESS_INSTANCE_COMMENT, comment.getProcessInstanceId(),
                    comment.getId()));
        }

        return result;
    }

    public List<CommentResponse> createRestCommentList(List<Comment> comments) {
        List<CommentResponse> responseList = new ArrayList<>();
        for (Comment instance : comments) {
            responseList.add(createRestComment(instance));
        }
        return responseList;
    }

    public List<EventResponse> createEventResponseList(List<Event> events) {
        List<EventResponse> responseList = new ArrayList<>();
        for (Event instance : events) {
            responseList.add(createEventResponse(instance));
        }
        return responseList;
    }

    public EventResponse createEventResponse(Event event) {
        RestUrlBuilder urlBuilder = new RestUrlBuilder();
        EventResponse result = new EventResponse();
        result.setAction(event.getAction());
        result.setId(event.getId());
        result.setMessage(event.getMessageParts());
        result.setTime(event.getTime());
        result.setUserId(event.getUserId());

        result.setUrl(
                urlBuilder.buildUrl(RestUrls.URL_TASK_EVENT, event.getTaskId(), event.getId()));
        result.setTaskUrl(urlBuilder.buildUrl(RestUrls.URL_TASK, event.getTaskId()));

        if (event.getProcessInstanceId() != null) {
            result.setTaskUrl(urlBuilder.buildUrl(RestUrls.URL_PROCESS_INSTANCE,
                                                  event.getProcessInstanceId()));
        }
        return result;
    }

    public List<HistoricProcessInstanceResponse> createHistoricProcessInstanceResponseList(
            List<HistoricProcessInstance> processInstances) {
        List<HistoricProcessInstanceResponse> responseList = new ArrayList<>();
        for (HistoricProcessInstance instance : processInstances) {
            responseList.add(createHistoricProcessInstanceResponse(instance));
        }
        return responseList;
    }

    @SuppressWarnings("deprecation")
    public HistoricProcessInstanceResponse createHistoricProcessInstanceResponse(
            HistoricProcessInstance processInstance) {

        RestUrlBuilder urlBuilder = new RestUrlBuilder();
        HistoricProcessInstanceResponse result = new HistoricProcessInstanceResponse();
        result.setBusinessKey(processInstance.getBusinessKey());
        result.setDeleteReason(processInstance.getDeleteReason());
        result.setDurationInMillis(processInstance.getDurationInMillis());
        result.setEndActivityId(processInstance.getEndActivityId());
        result.setEndTime(processInstance.getEndTime());
        result.setId(processInstance.getId());
        result.setProcessDefinitionId(processInstance.getProcessDefinitionId());
        result.setProcessDefinitionUrl(urlBuilder.buildUrl(RestUrls.URL_PROCESS_DEFINITION,
                                                           processInstance.getProcessDefinitionId()));
        result.setStartActivityId(processInstance.getStartActivityId());
        result.setStartTime(processInstance.getStartTime());
        result.setStartUserId(processInstance.getStartUserId());
        result.setSuperProcessInstanceId(processInstance.getSuperProcessInstanceId());
        result.setUrl(urlBuilder.buildUrl(RestUrls.URL_HISTORIC_PROCESS_INSTANCE,
                                          processInstance.getId()));
        if (processInstance.getProcessVariables() != null) {
            Map<String, Object> variableMap = processInstance.getProcessVariables();
            for (String name : variableMap.keySet()) {
                result.addVariable(createRestVariable(name, variableMap.get(name),
                                                      RestVariable.RestVariableScope.LOCAL,
                                                      processInstance.getId(),
                                                      VARIABLE_HISTORY_PROCESS, false));
            }
        }
        result.setTenantId(processInstance.getTenantId());
        return result;
    }

    public List<HistoricIdentityLinkResponse> createHistoricIdentityLinkResponseList(
            List<HistoricIdentityLink> identityLinks) {
        List<HistoricIdentityLinkResponse> responseList = new ArrayList<>();
        for (HistoricIdentityLink instance : identityLinks) {
            responseList.add(createHistoricIdentityLinkResponse(instance));
        }
        return responseList;
    }

    public HistoricIdentityLinkResponse createHistoricIdentityLinkResponse(
            HistoricIdentityLink identityLink) {
        RestUrlBuilder urlBuilder = new RestUrlBuilder();
        HistoricIdentityLinkResponse result = new HistoricIdentityLinkResponse();
        result.setType(identityLink.getType());
        result.setUserId(identityLink.getUserId());
        result.setGroupId(identityLink.getGroupId());
        result.setTaskId(identityLink.getTaskId());
        if (StringUtils.isNotEmpty(identityLink.getTaskId())) {
            result.setTaskUrl(urlBuilder.buildUrl(RestUrls.URL_HISTORIC_TASK_INSTANCE,
                                                  identityLink.getTaskId()));
        }
        result.setProcessInstanceId(identityLink.getProcessInstanceId());
        if (StringUtils.isNotEmpty(identityLink.getProcessInstanceId())) {
            result.setProcessInstanceUrl(urlBuilder.buildUrl(RestUrls.URL_HISTORIC_PROCESS_INSTANCE,
                                                             identityLink.getProcessInstanceId()));
        }
        return result;
    }

    public List<HistoricTaskInstanceResponse> createHistoricTaskInstanceResponseList(
            List<HistoricTaskInstance> taskInstances) {
        List<HistoricTaskInstanceResponse> responseList = new ArrayList<>();
        for (HistoricTaskInstance instance : taskInstances) {
            responseList.add(createHistoricTaskInstanceResponse(instance));
        }
        return responseList;
    }

    public HistoricTaskInstanceResponse createHistoricTaskInstanceResponse(
            HistoricTaskInstance taskInstance) {
        RestUrlBuilder urlBuilder = new RestUrlBuilder();
        HistoricTaskInstanceResponse result = new HistoricTaskInstanceResponse();
        result.setAssignee(taskInstance.getAssignee());
        result.setClaimTime(taskInstance.getClaimTime());
        result.setDeleteReason(taskInstance.getDeleteReason());
        result.setDescription(taskInstance.getDescription());
        result.setDueDate(taskInstance.getDueDate());
        result.setDurationInMillis(taskInstance.getDurationInMillis());
        result.setEndTime(taskInstance.getEndTime());
        result.setExecutionId(taskInstance.getExecutionId());
        result.setFormKey(taskInstance.getFormKey());
        result.setId(taskInstance.getId());
        result.setName(taskInstance.getName());
        result.setOwner(taskInstance.getOwner());
        result.setParentTaskId(taskInstance.getParentTaskId());
        result.setPriority(taskInstance.getPriority());
        result.setProcessDefinitionId(taskInstance.getProcessDefinitionId());
        result.setTenantId(taskInstance.getTenantId());
        result.setCategory(taskInstance.getCategory());
        if (taskInstance.getProcessDefinitionId() != null) {
            result.setProcessDefinitionUrl(urlBuilder.buildUrl(RestUrls.URL_PROCESS_DEFINITION,
                                                               taskInstance.getProcessDefinitionId()));
        }
        result.setProcessInstanceId(taskInstance.getProcessInstanceId());
        if (taskInstance.getProcessInstanceId() != null) {
            result.setProcessInstanceUrl(urlBuilder.buildUrl(RestUrls.URL_HISTORIC_PROCESS_INSTANCE,
                                                             taskInstance.getProcessInstanceId()));
        }
        result.setStartTime(taskInstance.getStartTime());
        result.setTaskDefinitionKey(taskInstance.getTaskDefinitionKey());
        result.setWorkTimeInMillis(taskInstance.getWorkTimeInMillis());
        result.setUrl(
                urlBuilder.buildUrl(RestUrls.URL_HISTORIC_TASK_INSTANCE, taskInstance.getId()));
        if (taskInstance.getProcessVariables() != null) {
            Map<String, Object> variableMap = taskInstance.getProcessVariables();
            for (String name : variableMap.keySet()) {
                result.addVariable(createRestVariable(name, variableMap.get(name),
                                                      RestVariable.RestVariableScope.GLOBAL,
                                                      taskInstance.getId(), VARIABLE_HISTORY_TASK,
                                                      false));
            }
        }
        if (taskInstance.getTaskLocalVariables() != null) {
            Map<String, Object> variableMap = taskInstance.getTaskLocalVariables();
            for (String name : variableMap.keySet()) {
                result.addVariable(createRestVariable(name, variableMap.get(name),
                                                      RestVariable.RestVariableScope.LOCAL,
                                                      taskInstance.getId(), VARIABLE_HISTORY_TASK,
                                                      false));
            }
        }
        return result;
    }

    public List<HistoricActivityInstanceResponse> createHistoricActivityInstanceResponseList(
            List<HistoricActivityInstance> activityInstances) {
        List<HistoricActivityInstanceResponse> responseList = new ArrayList<>();
        for (HistoricActivityInstance instance : activityInstances) {
            responseList.add(createHistoricActivityInstanceResponse(instance));
        }
        return responseList;
    }

    public HistoricActivityInstanceResponse createHistoricActivityInstanceResponse(
            HistoricActivityInstance activityInstance) {
        RestUrlBuilder urlBuilder = new RestUrlBuilder();
        HistoricActivityInstanceResponse result = new HistoricActivityInstanceResponse();
        result.setActivityId(activityInstance.getActivityId());
        result.setActivityName(activityInstance.getActivityName());
        result.setActivityType(activityInstance.getActivityType());
        result.setAssignee(activityInstance.getAssignee());
        result.setCalledProcessInstanceId(activityInstance.getCalledProcessInstanceId());
        result.setDurationInMillis(activityInstance.getDurationInMillis());
        result.setEndTime(activityInstance.getEndTime());
        result.setExecutionId(activityInstance.getExecutionId());
        result.setId(activityInstance.getId());
        result.setProcessDefinitionId(activityInstance.getProcessDefinitionId());
        result.setProcessDefinitionUrl(urlBuilder.buildUrl(RestUrls.URL_PROCESS_DEFINITION,
                                                           activityInstance.getProcessDefinitionId()));
        result.setProcessInstanceId(activityInstance.getProcessInstanceId());
        result.setProcessInstanceUrl(urlBuilder.buildUrl(RestUrls.URL_HISTORIC_PROCESS_INSTANCE,
                                                         activityInstance.getId()));
        result.setStartTime(activityInstance.getStartTime());
        result.setTaskId(activityInstance.getTaskId());
        result.setTenantId(activityInstance.getTenantId());
        return result;
    }

    public List<HistoricVariableInstanceResponse> createHistoricVariableInstanceResponseList(
            List<HistoricVariableInstance> variableInstances) {

        List<HistoricVariableInstanceResponse> responseList = new ArrayList<>();
        for (HistoricVariableInstance instance : variableInstances) {
            responseList.add(createHistoricVariableInstanceResponse(instance));
        }
        return responseList;
    }

    public HistoricVariableInstanceResponse createHistoricVariableInstanceResponse(
            HistoricVariableInstance variableInstance) {
        RestUrlBuilder urlBuilder = new RestUrlBuilder();
        HistoricVariableInstanceResponse result = new HistoricVariableInstanceResponse();
        result.setId(variableInstance.getId());
        result.setProcessInstanceId(variableInstance.getProcessInstanceId());
        if (variableInstance.getProcessInstanceId() != null) {
            result.setProcessInstanceUrl(urlBuilder.buildUrl(RestUrls.URL_HISTORIC_PROCESS_INSTANCE,
                                                             variableInstance.getProcessInstanceId()));
        }
        result.setTaskId(variableInstance.getTaskId());
        result.setVariable(
                createRestVariable(variableInstance.getVariableName(), variableInstance.getValue(),
                                   null, variableInstance.getId(), VARIABLE_HISTORY_VARINSTANCE,
                                   false));
        return result;
    }

    public List<HistoricDetailResponse> createHistoricDetailResponse(
            List<HistoricDetail> detailList) {

        List<HistoricDetailResponse> responseList = new ArrayList<>();
        for (HistoricDetail instance : detailList) {
            responseList.add(createHistoricDetailResponse(instance));
        }
        return responseList;
    }

    public HistoricDetailResponse createHistoricDetailResponse(HistoricDetail detail) {
        RestUrlBuilder urlBuilder = new RestUrlBuilder();
        HistoricDetailResponse result = new HistoricDetailResponse();
        result.setId(detail.getId());
        result.setProcessInstanceId(detail.getProcessInstanceId());
        if (StringUtils.isNotEmpty(detail.getProcessInstanceId())) {
            result.setProcessInstanceUrl(urlBuilder.buildUrl(RestUrls.URL_HISTORIC_PROCESS_INSTANCE,
                                                             detail.getProcessInstanceId()));
        }
        result.setExecutionId(detail.getExecutionId());
        result.setActivityInstanceId(detail.getActivityInstanceId());
        result.setTaskId(detail.getTaskId());
        if (StringUtils.isNotEmpty(detail.getTaskId())) {
            result.setTaskUrl(
                    urlBuilder.buildUrl(RestUrls.URL_HISTORIC_TASK_INSTANCE, detail.getTaskId()));
        }
        result.setTime(detail.getTime());
        if (detail instanceof HistoricFormProperty) {
            HistoricFormProperty formProperty = (HistoricFormProperty) detail;
            result.setDetailType(HistoricDetailResponse.FORM_PROPERTY);
            result.setPropertyId(formProperty.getPropertyId());
            result.setPropertyValue(formProperty.getPropertyValue());
        } else if (detail instanceof HistoricVariableUpdate) {
            HistoricVariableUpdate variableUpdate = (HistoricVariableUpdate) detail;
            result.setDetailType(HistoricDetailResponse.VARIABLE_UPDATE);
            result.setRevision(variableUpdate.getRevision());
            result.setVariable(
                    createRestVariable(variableUpdate.getVariableName(), variableUpdate.getValue(),
                                       null, detail.getId(), VARIABLE_HISTORY_DETAIL, false));
        }
        return result;
    }

    public FormDataResponse createFormDataResponse(FormData formData) {
        FormDataResponse result = new FormDataResponse();
        result.setDeploymentId(formData.getDeploymentId());
        result.setFormKey(formData.getFormKey());
        if (formData.getFormProperties() != null) {
            for (FormProperty formProp : formData.getFormProperties()) {
                RestFormProperty restFormProp = new RestFormProperty();
                restFormProp.setId(formProp.getId());
                restFormProp.setName(formProp.getName());
                if (formProp.getType() != null) {
                    restFormProp.setType(formProp.getType().getName());
                }
                restFormProp.setValue(formProp.getValue());
                restFormProp.setReadable(formProp.isReadable());
                restFormProp.setRequired(formProp.isRequired());
                restFormProp.setWritable(formProp.isWritable());
                if ("enum".equals(restFormProp.getType())) {
                    Object values = formProp.getType().getInformation("values");
                    if (values != null) {
                        @SuppressWarnings("unchecked") Map<String, String> enumValues =
                                (Map<String, String>) values;
                        for (String enumId : enumValues.keySet()) {
                            RestEnumFormProperty enumProperty = new RestEnumFormProperty();
                            enumProperty.setId(enumId);
                            enumProperty.setName(enumValues.get(enumId));
                            restFormProp.addEnumValue(enumProperty);
                        }
                    }
                } else if ("date".equals(restFormProp.getType())) {
                    restFormProp.setDatePattern(
                            (String) formProp.getType().getInformation("datePattern"));
                }
                result.addFormProperty(restFormProp);
            }
        }
        RestUrlBuilder urlBuilder = new RestUrlBuilder();
        if (formData instanceof StartFormData) {
            StartFormData startFormData = (StartFormData) formData;
            if (startFormData.getProcessDefinition() != null) {
                result.setProcessDefinitionId(startFormData.getProcessDefinition().getId());
                result.setProcessDefinitionUrl(urlBuilder.buildUrl(RestUrls.URL_PROCESS_DEFINITION,
                                                                   startFormData
                                                                           .getProcessDefinition()
                                                                           .getId()));
            }
        } else if (formData instanceof TaskFormData) {
            TaskFormData taskFormData = (TaskFormData) formData;
            if (taskFormData.getTask() != null) {
                result.setTaskId(taskFormData.getTask().getId());
                result.setTaskUrl(
                        urlBuilder.buildUrl(RestUrls.URL_TASK, taskFormData.getTask().getId()));
            }
        }
        return result;
    }

    private void initializeVariableConverters() {
        variableConverters.add(new StringRestVariableConverter());
        variableConverters.add(new IntegerRestVariableConverter());
        variableConverters.add(new LongRestVariableConverter());
        variableConverters.add(new ShortRestVariableConverter());
        variableConverters.add(new DoubleRestVariableConverter());
        variableConverters.add(new BooleanRestVariableConverter());
        variableConverters.add(new DateRestVariableConverter());
    }

    public List<GroupResponse> createGroupResponseList(List<Group> groups) {
        List<GroupResponse> responseList = new ArrayList<>();
        for (Group instance : groups) {
            responseList.add(createGroupResponse(instance));
        }
        return responseList;
    }

    public GroupResponse createGroupResponse(Group group) {
        RestUrlBuilder urlBuilder = new RestUrlBuilder();
        GroupResponse response = new GroupResponse();
        response.setId(group.getId());
        response.setName(group.getName());
        response.setType(group.getType());
        response.setUrl(urlBuilder.buildUrl(org.wso2.carbon.bpmn.rest.common.RestUrls.URL_GROUP,
                                            group.getId()));

        return response;
    }

    public List<UserResponse> createUserResponseList(List<User> users, boolean includePassword) {
        List<UserResponse> responseList = new ArrayList<>();
        for (User instance : users) {
            responseList.add(createUserResponse(instance, includePassword));
        }
        return responseList;
    }

    public UserResponse createUserResponse(User user, boolean includePassword) {
        RestUrlBuilder urlBuilder = new RestUrlBuilder();
        UserResponse response = new UserResponse();
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setId(user.getId());
        response.setEmail(user.getEmail());
        response.setUrl(urlBuilder.buildUrl(RestUrls.URL_USER, user.getId()));

        if (includePassword) {
            response.setPassword(user.getPassword());
        }

        if (user.isPictureSet()) {
            response.setPictureUrl(urlBuilder.buildUrl(RestUrls.URL_USER_PICTURE, user.getId()));
        }
        return response;
    }

    public List<UserInfoResponse> createUserInfoKeysResponse(List<String> keys, String userId) {
        RestUrlBuilder urlBuilder = new RestUrlBuilder();
        List<UserInfoResponse> responseList = new ArrayList<>();
        for (String instance : keys) {
            responseList.add(createUserInfoResponse(instance, null, userId, urlBuilder));
        }
        return responseList;
    }

    public UserInfoResponse createUserInfoResponse(String key, String value, String userId,
                                                   String baseUrl) {
        return createUserInfoResponse(key, value, userId, createUrlBuilder(baseUrl));
    }

    public UserInfoResponse createUserInfoResponse(String key, String value, String userId,
                                                   RestUrlBuilder urlBuilder) {
        UserInfoResponse response = new UserInfoResponse();
        response.setKey(key);
        response.setValue(value);
        response.setUrl(urlBuilder.buildUrl(RestUrls.URL_USER_INFO, userId, key));
        return response;
    }

    public List createExecutionResponseList(List<Execution> executions) {
        List<ExecutionResponse> responseList = new ArrayList<>();
        for (Execution instance : executions) {
            responseList.add(createExecutionResponse(instance));
        }
        return responseList;
    }

    public ExecutionResponse createExecutionResponse(Execution execution) {
        RestUrlBuilder urlBuilder = new RestUrlBuilder();
        ExecutionResponse result = new ExecutionResponse();
        result.setActivityId(execution.getActivityId());
        result.setId(execution.getId());
        result.setUrl(urlBuilder.buildUrl(RestUrls.URL_EXECUTION, execution.getId()));
        result.setSuspended(execution.isSuspended());
        result.setTenantId(execution.getTenantId());

        result.setParentId(execution.getParentId());
        if (execution.getParentId() != null) {
            result.setParentUrl(
                    urlBuilder.buildUrl(RestUrls.URL_EXECUTION, execution.getParentId()));
        }

        result.setProcessInstanceId(execution.getProcessInstanceId());
        if (execution.getProcessInstanceId() != null) {
            result.setProcessInstanceUrl(urlBuilder.buildUrl(RestUrls.URL_PROCESS_INSTANCE,
                                                             execution.getProcessInstanceId()));
        }
        return result;
    }

    public List<JobResponse> createJobResponseList(List<Job> jobs) {
        RestUrlBuilder urlBuilder = new RestUrlBuilder();
        List<JobResponse> responseList = new ArrayList<JobResponse>();
        for (Job instance : jobs) {
            responseList.add(createJobResponse(instance));
        }
        return responseList;
    }

    public JobResponse createJobResponse(Job job) {
        RestUrlBuilder urlBuilder = new RestUrlBuilder();
        JobResponse response = new JobResponse();
        response.setId(job.getId());
        response.setDueDate(job.getDuedate());
        response.setExceptionMessage(job.getExceptionMessage());
        response.setExecutionId(job.getExecutionId());
        response.setProcessDefinitionId(job.getProcessDefinitionId());
        response.setProcessInstanceId(job.getProcessInstanceId());
        response.setRetries(job.getRetries());
        response.setTenantId(job.getTenantId());

        response.setUrl(urlBuilder.buildUrl(RestUrls.URL_JOB, job.getId()));

        if (job.getProcessDefinitionId() != null) {
            response.setProcessDefinitionUrl(urlBuilder.buildUrl(RestUrls.URL_PROCESS_DEFINITION,
                                                                 job.getProcessDefinitionId()));
        }

        if (job.getProcessInstanceId() != null) {
            response.setProcessInstanceUrl(
                    urlBuilder.buildUrl(RestUrls.URL_PROCESS_INSTANCE, job.getProcessInstanceId()));
        }

        if (job.getExecutionId() != null) {
            response.setExecutionUrl(
                    urlBuilder.buildUrl(RestUrls.URL_EXECUTION, job.getExecutionId()));
        }

        return response;
    }

    public List<TableResponse> createTableResponseList(Map<String, Long> tableCounts) {
        RestUrlBuilder urlBuilder = new RestUrlBuilder();
        List<org.wso2.carbon.bpmn.rest.model.runtime.TableResponse> tables =
                new ArrayList<org.wso2.carbon.bpmn.rest.model.runtime.TableResponse>();
        for (Map.Entry<String, Long> entry : tableCounts.entrySet()) {
            tables.add(createTableResponse(entry.getKey(), entry.getValue(), urlBuilder));
        }
        return tables;
    }

    public TableResponse createTableResponse(String name, Long count, RestUrlBuilder urlBuilder) {
        org.wso2.carbon.bpmn.rest.model.runtime.TableResponse result =
                new org.wso2.carbon.bpmn.rest.model.runtime.TableResponse();
        result.setName(name);
        result.setCount(count);
        result.setUrl(urlBuilder.buildUrl(RestUrls.URL_TABLE, name));
        return result;
    }

    public TableResponse createTableResponse(String name, Long count) {
        RestUrlBuilder urlBuilder = new RestUrlBuilder();
        return createTableResponse(name, count, urlBuilder);
    }
}
