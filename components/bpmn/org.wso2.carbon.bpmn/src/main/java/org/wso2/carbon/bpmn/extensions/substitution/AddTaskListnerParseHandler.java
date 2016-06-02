/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
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
package org.wso2.carbon.bpmn.extensions.substitution;

import org.activiti.bpmn.model.BaseElement;
import org.activiti.bpmn.model.UserTask;
import org.activiti.engine.delegate.TaskListener;
import org.activiti.engine.impl.bpmn.parser.BpmnParse;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.task.TaskDefinition;
import org.activiti.engine.parse.BpmnParseHandler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Custom parse handler.
 * Add the UserSubstitutionTaskListener task listener for all the user tasks at the task creation phase.
 */
public class AddTaskListnerParseHandler implements BpmnParseHandler{

    /**
     * Decides which elements to intercept while parsing.
     */
    @Override
    public Collection<Class<? extends BaseElement>> getHandledTypes() {
        List<Class<? extends BaseElement>> elements = new ArrayList<>();
        elements.add(UserTask.class);

        return elements;
    }

    /**
     * Allows to intercept the parsing of given baseElement type
     * @param bpmnParse
     * @param baseElement
     */
    @Override
    public void parse(BpmnParse bpmnParse, BaseElement baseElement) {

        UserSubstitutionTaskListener listener = new UserSubstitutionTaskListener();
        String taskDefinitionKey = baseElement.getId();
        TaskDefinition taskDefinition = ((ProcessDefinitionEntity) bpmnParse.getCurrentScope().getProcessDefinition())
                .getTaskDefinitions().get(taskDefinitionKey);
        if (taskDefinition != null) {
            taskDefinition.addTaskListener(TaskListener.EVENTNAME_CREATE, listener);
        }
    }
}