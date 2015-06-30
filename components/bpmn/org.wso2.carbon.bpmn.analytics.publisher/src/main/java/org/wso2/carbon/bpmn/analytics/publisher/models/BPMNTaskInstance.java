package org.wso2.carbon.bpmn.analytics.publisher.models;

import java.util.Date;

/**
 * Created by isuruwi on 6/28/15.
 */
public class BPMNTaskInstance {
    private String taskDefinitionKey;
    private String taskInstanceId;
    private String processInstanceId;
    private String taskName;
    private String assignee;
    private String owner;
    private Date createTime;
    private Date claimTime;
    private Date startTime;
    private Date endTime;
    private long durationInMills;
    private long workTimeInMills;

    public String getTaskDefinitionKey() {
        return taskDefinitionKey;
    }

    public void setTaskDefinitionKey(String taskDefinitionKey) {
        this.taskDefinitionKey = taskDefinitionKey;
    }

    public String getTaskInstanceId() {
        return taskInstanceId;
    }

    public void setTaskInstanceId(String taskInstanceId) {
        this.taskInstanceId = taskInstanceId;
    }

    public String getProcessInstanceId() {
        return processInstanceId;
    }

    public void setProcessInstanceId(String processInstanceId) {
        this.processInstanceId = processInstanceId;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getAssignee() {
        return assignee;
    }

    public void setAssignee(String assignee) {
        this.assignee = assignee;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getClaimTime() {
        return claimTime;
    }

    public void setClaimTime(Date claimTime) {
        this.claimTime = claimTime;
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

    public long getDurationInMills() {
        return durationInMills;
    }

    public void setDurationInMills(long durationInMills) {
        this.durationInMills = durationInMills;
    }

    public long getWorkTimeInMills() {
        return workTimeInMills;
    }

    public void setWorkTimeInMills(long workTimeInMills) {
        this.workTimeInMills = workTimeInMills;
    }
}
