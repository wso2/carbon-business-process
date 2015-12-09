/**
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.bpmn.rest.model.stats;

import org.wso2.carbon.bpmn.core.mgt.model.BPMNVariable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Date;
import java.util.List;

/**
 * Model object of a bpmn process instance
 */
@XmlRootElement(name = "Process")
@XmlAccessorType(XmlAccessType.FIELD)

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
    private long deployedProcessCount;
    private double averageTimeForCompletion;
    //private BPMNVariable[] variables;
    private List<BPMNTaskInstance> taskList;

    public List<BPMNTaskInstance> getTaskList() {
        return taskList;
    }

    public void setTaskList(List<BPMNTaskInstance> taskList) {
        this.taskList = taskList;
    }

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

    public long getDeployedProcessCount() {
        return deployedProcessCount;
    }

    public void setDeployedProcessCount(long deployedProcessCount) {
        this.deployedProcessCount = deployedProcessCount;
    }

    public double getAverageTimeForCompletion() {
        return averageTimeForCompletion;
    }

    public void setAverageTimeForCompletion(double averageTimeForCompletion) {
        this.averageTimeForCompletion = averageTimeForCompletion;
    }
}
