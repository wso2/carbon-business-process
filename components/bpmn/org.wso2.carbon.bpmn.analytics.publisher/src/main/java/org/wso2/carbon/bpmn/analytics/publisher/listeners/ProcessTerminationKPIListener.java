/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.ExecutionListener;
import org.activiti.engine.runtime.ProcessInstance;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bpmn.analytics.publisher.BPMNDataPublisherException;
import org.wso2.carbon.bpmn.analytics.publisher.internal.BPMNAnalyticsHolder;

import java.util.List;

public class ProcessTerminationKPIListener implements ExecutionListener {
    private static final Log log = LogFactory.getLog(ProcessTerminationKPIListener.class);

    @Override
    public void notify(DelegateExecution delegateExecution) {
        try {
            List<ProcessInstance> runtimeProcessInstances = delegateExecution.getEngineServices().getRuntimeService()
                    .createProcessInstanceQuery().processInstanceId(delegateExecution.getProcessInstanceId()).list();
            if (runtimeProcessInstances.size() == 1) {
                ProcessInstance runtimeProcessInstance = runtimeProcessInstances.get(0);
                BPMNAnalyticsHolder.getInstance().getBpmnDataPublisher().publishKPIvariableData(runtimeProcessInstance);
            }
        } catch (BPMNDataPublisherException e) {
            String errMsg = "Process Variable Data Publishing failed.";
            log.error(errMsg, e);
            // Caught exception is not thrown as we do not need to stop the process termination, due to an error in
            // process data publishing (for analytics)
        }
    }
}
