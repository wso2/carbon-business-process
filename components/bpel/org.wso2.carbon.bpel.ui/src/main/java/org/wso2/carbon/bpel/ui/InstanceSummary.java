/*
 * Copyright (c) 2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.bpel.ui;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bpel.stub.mgt.types.InstanceSummaryE;
import org.wso2.carbon.bpel.stub.mgt.types.Instances_type0;

/**
 * Instance summary contains number of active, completed, failed, suspended, and terminated instances.
 */
public class InstanceSummary {
    private static Log log = LogFactory.getLog(InstanceSummary.class);

    private int activeInstances;
    private int completedInstances;
    private int failedInstances;
    private int suspendedInstances;
    private int terminatedInstances;
    private int totalInstances;

    public InstanceSummary() {
        this.activeInstances = 0;
        this.completedInstances = 0;
        this.failedInstances = 0;
        this.suspendedInstances = 0;
        this.terminatedInstances = 0;
        this.totalInstances = 0;
    }

    /**
     * Create instance summary object from process instances array.
     *
     * @param processInstances process instance array
     * @return instance summary object
     */
    public static InstanceSummary createInstanceSummary(Instances_type0[] processInstances) {
        InstanceSummary summary = new InstanceSummary();
        for (Instances_type0 processInstance : processInstances) {
            String state = processInstance.getState().getValue();
            if (state.equals("ACTIVE")) {
                summary.activeInstances = processInstance.getCount();
            } else if (state.equals("COMPLETED")) {
                summary.completedInstances = processInstance.getCount();
            } else if (state.equals("TERMINATED")) {
                summary.terminatedInstances = processInstance.getCount();
            } else if (state.equals("FAILED")) {
                summary.failedInstances = processInstance.getCount();
            } else if (state.equals("SUSPENDED")) {
                summary.suspendedInstances = processInstance.getCount();
            } else {
                log.error("Invalid State " + state);
            }
            summary.totalInstances += processInstance.getCount();
        }
        return summary;
    }

    /**
     * Create instance summary from return value of getInstanceSummary operation of instance management web service.
     *
     * @param globalInstanceSummary instance summary from instance management API(global counts)
     * @return global instance count
     */
    public static InstanceSummary createInstanceSummary(InstanceSummaryE globalInstanceSummary) {
        InstanceSummary summary = new InstanceSummary();

        summary.activeInstances = globalInstanceSummary.getActive();
        summary.completedInstances = globalInstanceSummary.getCompleted();
        summary.terminatedInstances = globalInstanceSummary.getTerminated();
        summary.suspendedInstances = globalInstanceSummary.getSuspended();
        summary.failedInstances = globalInstanceSummary.getFailed();

        return summary;
    }

    public int getActiveInstances() {
        return activeInstances;
    }

    public int getCompletedInstances() {
        return completedInstances;
    }

    public int getFailedInstances() {
        return failedInstances;
    }

    public int getSuspendedInstances() {
        return suspendedInstances;
    }

    public int getTerminatedInstances() {
        return terminatedInstances;
    }

    public int getTotalInstances() {
        return totalInstances;
    }
}
