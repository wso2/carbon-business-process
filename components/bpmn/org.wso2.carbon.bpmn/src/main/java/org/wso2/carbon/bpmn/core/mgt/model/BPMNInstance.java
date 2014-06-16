package org.wso2.carbon.bpmn.core.mgt.model;

import java.util.Date;

public class BPMNInstance {

    private String instanceId;
    private String processId;
    private BPMNVariable[] variables;
    private boolean suspended;
    private Date startTime;

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public String getProcessId() {
        return processId;
    }

    public void setProcessId(String processId) {
        this.processId = processId;
    }

    public BPMNVariable[] getVariables() {
        return variables;
    }

    public void setVariables(BPMNVariable[] variables) {
        this.variables = variables;
    }

    public boolean isSuspended() {
        return suspended;
    }

    public void setSuspended(boolean suspended) {
        this.suspended = suspended;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }
}
