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
import org.activiti.bpmn.model.ServiceTask;
import org.activiti.engine.impl.bpmn.parser.BpmnParse;
import org.activiti.engine.impl.bpmn.parser.handler.AbstractBpmnParseHandler;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.pvm.PvmEvent;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bpmn.analytics.publisher.listeners.ServiceTaskCompletionListener;

import java.util.List;

public class ServiceTaskParseHandler extends AbstractBpmnParseHandler {

    private static final Log log = LogFactory.getLog(ServiceTaskParseHandler.class);

    @Override
    protected Class<? extends BaseElement> getHandledType() {
        return ServiceTask.class;
    }

    @Override
    protected void executeParse(BpmnParse bpmnParse, BaseElement element) {
        ProcessDefinitionEntity processDefinitionEntity = bpmnParse.getCurrentProcessDefinition();
        List<ActivityImpl> activities = processDefinitionEntity.getActivities();
        for (ActivityImpl activity : activities) {
            if (activity.getId().equals(element.getId())) {
                if (log.isDebugEnabled()) {
                    log.debug("Enabling data publishing for service task: " + element.getId());
                }
                activity.addExecutionListener(PvmEvent.EVENTNAME_END, new ServiceTaskCompletionListener());
            }
        }
    }
}