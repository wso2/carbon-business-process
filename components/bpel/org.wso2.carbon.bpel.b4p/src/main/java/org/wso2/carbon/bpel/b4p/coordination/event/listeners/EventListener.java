/*
 * Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.bpel.b4p.coordination.event.listeners;

import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.common.ProcessState;
import org.apache.ode.bpel.evt.BpelEvent;
import org.apache.ode.bpel.evt.ProcessInstanceEvent;
import org.apache.ode.bpel.evt.ProcessTerminationEvent;
import org.apache.ode.bpel.evt.ProcessInstanceStateChangeEvent;
import org.apache.ode.bpel.iapi.BpelEventListener;
import org.wso2.carbon.bpel.b4p.coordination.configuration.CoordinationConfiguration;
import org.wso2.carbon.bpel.b4p.internal.B4PContentHolder;
import org.wso2.carbon.bpel.b4p.internal.B4PServiceComponent;
import org.wso2.carbon.bpel.core.ode.integration.store.ProcessConfigurationImpl;
import org.wso2.carbon.context.CarbonContext;

public class EventListener implements BpelEventListener {

    private static final Log log = LogFactory.getLog(EventListener.class);

    private boolean isCoordinationEnabled = false;

    public void onEvent(BpelEvent bpelEvent) {
        if (isCoordinationEnabled) {
            if (bpelEvent instanceof ProcessTerminationEvent) {

                ProcessTerminationEvent event = (ProcessTerminationEvent) bpelEvent;

                ProcessConfigurationImpl processConf = getProcessConfiguration(event);
                if (processConf.isB4PTaskIncluded()) {
                    if (log.isDebugEnabled()) {
                        log.debug("TERMINATED Process instance " + event.getProcessInstanceId()
                                + " has a B4P activity. Initiating Exit Protocol Messages to task(s).");
                    }
                    TerminationTask terminationTask = new TerminationTask(Long.toString(event.getProcessInstanceId()));
                    terminationTask.setTenantID(CarbonContext.getThreadLocalCarbonContext().getTenantId());
                    B4PContentHolder.getInstance().getCoordinationController().runTask(terminationTask);
                }

            } else if (bpelEvent instanceof ProcessInstanceStateChangeEvent) {
                ProcessInstanceStateChangeEvent instanceStateChangeEvent = (ProcessInstanceStateChangeEvent) bpelEvent;
                if (ProcessState.STATE_COMPLETED_WITH_FAULT == instanceStateChangeEvent.getNewState()) {

                    ProcessConfigurationImpl processConf = getProcessConfiguration(instanceStateChangeEvent);
                    if (processConf.isB4PTaskIncluded()) {
                        if (log.isDebugEnabled()) {
                            log.debug("Process Instance, COMPLETED WITH FAULT " + instanceStateChangeEvent.getProcessInstanceId()
                                    + " has a B4P activity. Initiating Exit Protocol Messages to task(s)");
                        }
                        TerminationTask terminationTask = new TerminationTask(Long.toString(instanceStateChangeEvent.getProcessInstanceId()));
                        B4PContentHolder.getInstance().getCoordinationController().runTask(terminationTask);
                    }
                }
            }
        }
    }

    public void startup(Properties properties) {
        this.isCoordinationEnabled = CoordinationConfiguration.getInstance().isHumantaskCoordinationEnabled();
    }

    public void shutdown() {
    }

    private ProcessConfigurationImpl getProcessConfiguration(ProcessInstanceEvent event) {
        int tenantId = B4PServiceComponent.getBPELServer().getMultiTenantProcessStore().getTenantId(event.getProcessId());
        return (ProcessConfigurationImpl) B4PServiceComponent.
                getBPELServer().getMultiTenantProcessStore().getTenantsProcessStore(tenantId).
                getProcessConfiguration(event.getProcessId());
    }

}
