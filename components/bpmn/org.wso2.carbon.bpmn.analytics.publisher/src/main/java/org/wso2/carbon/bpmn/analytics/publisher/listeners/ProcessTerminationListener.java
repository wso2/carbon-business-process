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
package org.wso2.carbon.bpmn.analytics.publisher.listeners;

import org.activiti.engine.HistoryService;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.ExecutionListener;
import org.activiti.engine.history.HistoricProcessInstance;
import org.wso2.carbon.bpmn.analytics.publisher.internal.BPMNAnalyticsHolder;

import java.util.List;

public class ProcessTerminationListener implements ExecutionListener {

    @Override
    public void notify(DelegateExecution delegateExecution) throws Exception {

        HistoryService historyService = delegateExecution.getEngineServices().getHistoryService();
        List<HistoricProcessInstance> historicProcessInstances = historyService.createHistoricProcessInstanceQuery().processInstanceId(delegateExecution.getProcessInstanceId()).list();
        if (historicProcessInstances.size() == 1) {
            HistoricProcessInstance instance = historicProcessInstances.get(0);
            BPMNAnalyticsHolder.getInstance().getBpmnDataPublisher().publishProcessEvent(instance);
        }
    }
}
