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
import org.activiti.engine.delegate.ExecutionListener;
import org.activiti.engine.impl.bpmn.parser.BpmnParse;
import org.activiti.engine.impl.bpmn.parser.handler.AbstractBpmnParseHandler;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.pvm.PvmEvent;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bpmn.analytics.publisher.listeners.ProcessTerminationListener;

import java.util.List;

public class ProcessParseHandler extends AbstractBpmnParseHandler {

    private static final Log log = LogFactory.getLog(ProcessParseHandler.class);

    @Override
    protected Class<? extends BaseElement> getHandledType() {
        return org.activiti.bpmn.model.Process.class;
    }

    @Override
    protected void executeParse(BpmnParse bpmnParse, BaseElement baseElement) {
        ProcessDefinitionEntity processDefinitionEntity = bpmnParse.getCurrentProcessDefinition();

        // We have to check if the data publishing listener has already been associated at server startup.
        ExecutionListener processTerminationListener = null;
        List<ExecutionListener> endListeners = processDefinitionEntity.getExecutionListeners(PvmEvent.EVENTNAME_END);
        if (endListeners != null) {
            for (ExecutionListener listener : endListeners) {
                if (listener instanceof ProcessTerminationListener) {
                    processTerminationListener = listener;
                    break;
                }
            }
        }
        if (processTerminationListener == null) {
            if (log.isDebugEnabled()) {
                log.debug("Enabling data publishing for process: " + processDefinitionEntity.getName());
            }
            processDefinitionEntity.addExecutionListener(PvmEvent.EVENTNAME_END, new ProcessTerminationListener());
        }
    }
}
