/*
 * Copyright 2005-2015 WSO2, Inc. (http://wso2.com)
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
 */
package org.wso2.carbon.bpmn.rest.service.analytics;

import org.activiti.engine.RepositoryService;
import org.activiti.engine.delegate.ExecutionListener;
import org.activiti.engine.delegate.TaskListener;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.pvm.PvmEvent;
import org.activiti.engine.impl.task.TaskDefinition;
import org.activiti.engine.repository.ProcessDefinition;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bpmn.analytics.publisher.listeners.ProcessTerminationListener;
import org.wso2.carbon.bpmn.analytics.publisher.listeners.TaskCompletionListener;
import org.wso2.carbon.bpmn.rest.common.exception.BPMNRestException;
import org.wso2.carbon.bpmn.rest.common.utils.BPMNOSGIService;
import org.wso2.carbon.bpmn.rest.model.analytics.DataPublisherConfig;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.Map;

@Path("/config")
public class AnalyticsConfigurationService {

    private static final Log log = LogFactory.getLog(AnalyticsConfigurationService.class);

    @PUT
    @Path("/processes/{process_id}")
    @Consumes({MediaType.APPLICATION_JSON})
    public void configureProcessLevelEvents(@PathParam("process_id") String processDefinitionId, DataPublisherConfig dataPublisherConfig) {

        try {
            RepositoryService repositoryService = BPMNOSGIService.getRepositoryService();
            ProcessDefinition process = repositoryService.getProcessDefinition(processDefinitionId);

            if (process != null && process instanceof ProcessDefinitionEntity) {
                ProcessDefinitionEntity processDefinitionEntity = (ProcessDefinitionEntity) process;
                if (dataPublisherConfig.isEnabled()) {
                    List<ExecutionListener> endListeners = processDefinitionEntity.getExecutionListeners(PvmEvent.EVENTNAME_END);
                    ExecutionListener processTerminationListener = null;
                    for (ExecutionListener listener : endListeners) {
                        if (listener instanceof ProcessTerminationListener) {
                            processTerminationListener = listener;
                            break;
                        }
                    }
                    if (processTerminationListener == null) {
                        if (log.isDebugEnabled()) {
                            log.debug("Adding process termination listener to process: " + processDefinitionId);
                        }
                        processDefinitionEntity.addExecutionListener(PvmEvent.EVENTNAME_END, new ProcessTerminationListener());
                    }

                } else {
                    List<ExecutionListener> endListeners = processDefinitionEntity.getExecutionListeners(PvmEvent.EVENTNAME_END);
                    ExecutionListener processTerminationListener = null;
                    for (ExecutionListener listener : endListeners) {
                        if (listener instanceof ProcessTerminationListener) {
                            processTerminationListener = listener;
                            break;
                        }
                    }
                    if (processTerminationListener != null) {
                        if (log.isDebugEnabled()) {
                            log.debug("Removing process termination listener from process: " + processDefinitionId);
                        }
                        endListeners.remove(processTerminationListener);
                    }
                }
            }
        } catch (Exception e) {
            String msg = "Failed to configure events for process: " + processDefinitionId;
            log.error(msg, e);
            throw new BPMNRestException(msg, e);
        }
    }

    @PUT
    @Path("/processes/{process_id}/tasks/{task_id}")
    @Consumes({MediaType.APPLICATION_JSON})
    public void configureTaskLevelEvents(@PathParam("process_id") String processDefinitionId, @PathParam("task_id") String taskId, DataPublisherConfig dataPublisherConfig) {

        try {
            RepositoryService repositoryService = BPMNOSGIService.getRepositoryService();
            ProcessDefinition process = repositoryService.getProcessDefinition(processDefinitionId);

            if (process != null && process instanceof ProcessDefinitionEntity) {
                ProcessDefinitionEntity processDefinitionEntity = (ProcessDefinitionEntity) process;
                Map<String, TaskDefinition> taskDefinitions = processDefinitionEntity.getTaskDefinitions();
                TaskDefinition taskDefinition = taskDefinitions.get(taskId);
                if (taskDefinition != null) {

                    if (dataPublisherConfig.isEnabled()) {
                        List<TaskListener> completionListeners = taskDefinition.getTaskListener(TaskListener.EVENTNAME_COMPLETE);
                        TaskListener taskCompletionListener = null;
                        for (TaskListener listener : completionListeners) {
                            if (listener instanceof TaskCompletionListener) {
                                taskCompletionListener = listener;
                                break;
                            }
                        }
                        if (taskCompletionListener == null) {
                            if (log.isDebugEnabled()) {
                                log.debug("Adding task completion listener to task: " + taskId + " of process: " + processDefinitionId);
                            }
                            taskDefinition.addTaskListener(TaskListener.EVENTNAME_COMPLETE, new TaskCompletionListener());
                        }

                    } else {

                        List<TaskListener> completionListeners = taskDefinition.getTaskListener(TaskListener.EVENTNAME_COMPLETE);
                        TaskListener taskCompletionListener = null;
                        for (TaskListener listener : completionListeners) {
                            if (listener instanceof TaskCompletionListener) {
                                taskCompletionListener = listener;
                                break;
                            }
                        }
                        if (taskCompletionListener != null) {
                            if (log.isDebugEnabled()) {
                                log.debug("Removing task completion listener from task: " + taskId + " of process: " + processDefinitionId);
                            }
                            completionListeners.remove(taskCompletionListener);
                        }
                    }
                }
            }
        } catch (Exception e) {
            String msg = "Failed to configure events for task: " + taskId + " of process: " + processDefinitionId;
            log.error(msg, e);
            throw new BPMNRestException(msg, e);
        }
    }



}
