package org.wso2.carbon.bpmn.analytics.publisher.models;

import org.wso2.carbon.bpmn.core.mgt.model.BPMNVariable;

import java.util.Date;

/**
 * Created by isuruwi on 6/26/15.
 */
public class BPMNProcessInstance {
    private String processDefinitionId;
    private String instanceId;
    private Date startTime;
    private Date endTime;
    private long duration;
    private String startUserId;
    private String tenantId;
    private String businessKey;
    private String name;
    private String startActivityId;
    BPMNVariable[] variables;

    public String getProcessDefinitionId() {
        return processDefinitionId;
    }

    public void setProcessDefinitionId(String deploymentId) {
        this.processDefinitionId = deploymentId;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public String getStartUserId() {
        return startUserId;
    }

    public void setStartUserId(String startUserId) {
        this.startUserId = startUserId;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getBusinessKey() {
        return businessKey;
    }

    public void setBusinessKey(String businessKey) {
        this.businessKey = businessKey;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStartActivityId() {
        return startActivityId;
    }

    public void setStartActivityId(String startActivityId) {
        this.startActivityId = startActivityId;
    }

    public BPMNVariable[] getVariables() {
        return variables;
    }

    public void setVariables(BPMNVariable[] variables) {
        this.variables = variables;
    }
}
