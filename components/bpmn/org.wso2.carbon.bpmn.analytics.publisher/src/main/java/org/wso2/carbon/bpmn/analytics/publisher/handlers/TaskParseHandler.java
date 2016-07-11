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
package org.wso2.carbon.bpmn.analytics.publisher.handlers;

import org.activiti.bpmn.model.BaseElement;
import org.activiti.bpmn.model.UserTask;
import org.activiti.engine.delegate.TaskListener;
import org.activiti.engine.impl.bpmn.parser.BpmnParse;
import org.activiti.engine.impl.bpmn.parser.handler.AbstractBpmnParseHandler;
import org.activiti.engine.impl.bpmn.parser.handler.UserTaskParseHandler;
import org.activiti.engine.impl.task.TaskDefinition;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bpmn.analytics.publisher.listeners.TaskCompletionListener;

import java.util.List;

public class TaskParseHandler extends AbstractBpmnParseHandler {

    private static final Log log = LogFactory.getLog(TaskParseHandler.class);

    @Override
    protected Class<? extends BaseElement> getHandledType() {
        return UserTask.class;
    }

    @Override
    protected void executeParse(BpmnParse bpmnParse, BaseElement element) {
        TaskDefinition taskDefinition = (TaskDefinition) bpmnParse.getCurrentActivity().getProperty(UserTaskParseHandler.PROPERTY_TASK_DEFINITION);

        // We have to check if data publishing listener has already been associated at server startup
        TaskListener taskCompletionListener = null;
        List<TaskListener> completionListeners = taskDefinition.getTaskListener(TaskListener.EVENTNAME_COMPLETE);
        if (completionListeners != null) {
            for (TaskListener listener : completionListeners) {
                if (listener instanceof TaskCompletionListener) {
                    taskCompletionListener = listener;
                }
            }
        }
        if (taskCompletionListener == null) {
            if (log.isDebugEnabled()) {
                log.debug("Adding data publishing listener to task: " + taskDefinition.getKey());
            }
            taskDefinition.addTaskListener(TaskListener.EVENTNAME_COMPLETE, new TaskCompletionListener());
        }
    }
}
