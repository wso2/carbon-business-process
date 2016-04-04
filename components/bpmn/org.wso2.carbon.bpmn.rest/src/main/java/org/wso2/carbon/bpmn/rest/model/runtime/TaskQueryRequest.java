/**
 *  Copyright (c) 2015 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.bpmn.rest.model.runtime;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.wso2.carbon.bpmn.rest.common.PaginateRequest;
import org.wso2.carbon.bpmn.rest.engine.variable.QueryVariable;

import java.util.Date;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 */

@XmlRootElement(name = "TaskQueryRequest")
@XmlAccessorType(XmlAccessType.FIELD)
public class TaskQueryRequest extends PaginateRequest {

    private String name;
    private String nameLike;
    private String description;
    private String descriptionLike;
    private Integer priority;
    private Integer minimumPriority;
    private Integer maximumPriority;
    private String assignee;
    private String assigneeLike;
    private String owner;
    private String ownerLike;
    private Boolean unassigned;
    private String delegationState;
    private String candidateUser;
    private String candidateGroup;
    @XmlElementWrapper(name = "CandidateGroupInCollection")
    @XmlElement(name = "CandidateGroupIn")
    private List<String> candidateGroupIn;
    private String involvedUser;
    private String processInstanceId;
    private String processInstanceBusinessKey;
    private String processInstanceBusinessKeyLike;
    private String processDefinitionKey;
    private String processDefinitionName;
    private String processDefinitionKeyLike;
    private String processDefinitionNameLike;
    private String executionId;
    private Date createdOn;
    private Date createdBefore;
    private Date createdAfter;
    private Boolean excludeSubTasks;
    private String taskDefinitionKey;
    private String taskDefinitionKeyLike;
    private Date dueDate;
    private Date dueBefore;
    private Date dueAfter;
    private Boolean withoutDueDate;
    private Boolean active;
    private Boolean includeTaskLocalVariables;
    private Boolean includeProcessVariables;
    private String tenantId;
    private String tenantIdLike;
    private Boolean withoutTenantId;
    private String candidateOrAssigned;

    @XmlElementWrapper(name = "QueryTaskVariables")
    @XmlElement(name = "QueryVariable")
    private List<QueryVariable> taskVariables;

    @XmlElementWrapper(name = "QueryProcessVariables")
    @XmlElement(name = "QueryVariable")
    private List<QueryVariable> processInstanceVariables;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNameLike() {
        return nameLike;
    }

    public void setNameLike(String nameLike) {
        this.nameLike = nameLike;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescriptionLike() {
        return descriptionLike;
    }

    public void setDescriptionLike(String descriptionLike) {
        this.descriptionLike = descriptionLike;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public Integer getMinimumPriority() {
        return minimumPriority;
    }

    public void setMinimumPriority(Integer minimumPriority) {
        this.minimumPriority = minimumPriority;
    }

    public Integer getMaximumPriority() {
        return maximumPriority;
    }

    public void setMaximumPriority(Integer maximumPriority) {
        this.maximumPriority = maximumPriority;
    }

    public String getAssignee() {
        return assignee;
    }

    public void setAssignee(String assignee) {
        this.assignee = assignee;
    }

    public String getAssigneeLike() {
        return assigneeLike;
    }

    public void setAssigneeLike(String assigneeLike) {
        this.assigneeLike = assigneeLike;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getOwnerLike() {
        return ownerLike;
    }

    public void setOwnerLike(String ownerLike) {
        this.ownerLike = ownerLike;
    }

    public Boolean getUnassigned() {
        return unassigned;
    }

    public void setUnassigned(Boolean unassigned) {
        this.unassigned = unassigned;
    }

    public String getDelegationState() {
        return delegationState;
    }

    public void setDelegationState(String delegationState) {
        this.delegationState = delegationState;
    }

    public String getCandidateUser() {
        return candidateUser;
    }

    public void setCandidateUser(String candidateUser) {
        this.candidateUser = candidateUser;
    }

    public String getCandidateGroup() {
        return candidateGroup;
    }

    public void setCandidateGroup(String candidateGroup) {
        this.candidateGroup = candidateGroup;
    }

    public List<String> getCandidateGroupIn() {
        return candidateGroupIn;
    }

    public void setCandidateGroupIn(List<String> candidateGroupIn) {
        this.candidateGroupIn = candidateGroupIn;
    }

    public String getInvolvedUser() {
        return involvedUser;
    }

    public void setInvolvedUser(String involvedUser) {
        this.involvedUser = involvedUser;
    }

    public String getProcessInstanceId() {
        return processInstanceId;
    }

    public void setProcessInstanceId(String processInstanceId) {
        this.processInstanceId = processInstanceId;
    }

    public String getProcessInstanceBusinessKey() {
        return processInstanceBusinessKey;
    }

    public void setProcessInstanceBusinessKey(String processInstanceBusinessKey) {
        this.processInstanceBusinessKey = processInstanceBusinessKey;
    }

    public String getProcessInstanceBusinessKeyLike() {
        return processInstanceBusinessKeyLike;
    }

    public void setProcessInstanceBusinessKeyLike(String processInstanceBusinessKeyLike) {
        this.processInstanceBusinessKeyLike = processInstanceBusinessKeyLike;
    }

    public String getExecutionId() {
        return executionId;
    }

    public void setExecutionId(String executionId) {
        this.executionId = executionId;
    }

    public Date getCreatedOn() {
        return createdOn == null ? null : (Date) this.createdOn.clone();
    }

    public void setCreatedOn(Date createdOn) {
        this.createdOn = createdOn == null ? null : (Date) this.createdOn.clone();
    }

    public Date getCreatedBefore() {
        return createdBefore == null ? null : (Date) this.createdBefore.clone();
    }

    public void setCreatedBefore(Date createdBefore) {
        this.createdBefore = createdBefore == null ? null : (Date) this.createdBefore.clone();
    }

    public Date getCreatedAfter() {
        return createdAfter == null ? null : (Date) this.createdAfter.clone();
    }

    public void setCreatedAfter(Date createdAfter) {
        this.createdAfter = createdAfter == null ? null : (Date) this.createdAfter.clone();
    }

    public Boolean getExcludeSubTasks() {
        return excludeSubTasks;
    }

    public void setExcludeSubTasks(Boolean excludeSubTasks) {
        this.excludeSubTasks = excludeSubTasks;
    }

    public String getTaskDefinitionKey() {
        return taskDefinitionKey;
    }

    public void setTaskDefinitionKey(String taskDefinitionKey) {
        this.taskDefinitionKey = taskDefinitionKey;
    }

    public String getTaskDefinitionKeyLike() {
        return taskDefinitionKeyLike;
    }

    public void setTaskDefinitionKeyLike(String taskDefinitionKeyLike) {
        this.taskDefinitionKeyLike = taskDefinitionKeyLike;
    }

    public Date getDueDate() {
        return dueDate == null ? null : (Date) this.dueDate.clone();
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate == null ? null : (Date) this.dueDate.clone();
    }

    public Date getDueBefore() {
        return dueBefore == null ? null : (Date) this.dueBefore.clone();
    }

    public void setDueBefore(Date dueBefore) {
        this.dueBefore = dueBefore == null ? null : (Date) this.dueBefore.clone();
    }

    public Date getDueAfter() {
        return dueAfter == null ? null : (Date) this.dueAfter.clone();
    }

    public void setDueAfter(Date dueAfter) {
        this.dueAfter = dueAfter == null ? null : (Date) this.dueAfter.clone();
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Boolean getIncludeTaskLocalVariables() {
        return includeTaskLocalVariables;
    }

    public void setIncludeTaskLocalVariables(Boolean includeTaskLocalVariables) {
        this.includeTaskLocalVariables = includeTaskLocalVariables;
    }

    public Boolean getIncludeProcessVariables() {
        return includeProcessVariables;
    }

    public void setIncludeProcessVariables(Boolean includeProcessVariables) {
        this.includeProcessVariables = includeProcessVariables;
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, defaultImpl = QueryVariable.class)
    public List<QueryVariable> getTaskVariables() {
        return taskVariables;
    }

    public void setTaskVariables(List<QueryVariable> taskVariables) {
        this.taskVariables = taskVariables;
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, defaultImpl = QueryVariable.class)
    public List<QueryVariable> getProcessInstanceVariables() {
        return processInstanceVariables;
    }

    public void setProcessInstanceVariables(List<QueryVariable> processInstanceVariables) {
        this.processInstanceVariables = processInstanceVariables;
    }

    public void setProcessDefinitionNameLike(String processDefinitionNameLike) {
        this.processDefinitionNameLike = processDefinitionNameLike;
    }

    public String getProcessDefinitionNameLike() {
        return processDefinitionNameLike;
    }

    public String getProcessDefinitionKeyLike() {
        return processDefinitionKeyLike;
    }

    public void setProcessDefinitionKeyLike(String processDefinitionKeyLike) {
        this.processDefinitionKeyLike = processDefinitionKeyLike;
    }

    public void setWithoutDueDate(Boolean withoutDueDate) {
        this.withoutDueDate = withoutDueDate;
    }

    public Boolean getWithoutDueDate() {
        return withoutDueDate;
    }

    public String getProcessDefinitionKey() {
        return processDefinitionKey;
    }

    public void setProcessDefinitionKey(String processDefinitionKey) {
        this.processDefinitionKey = processDefinitionKey;
    }

    public String getProcessDefinitionName() {
        return processDefinitionName;
    }

    public void setProcessDefinitionName(String processDefinitionName) {
        this.processDefinitionName = processDefinitionName;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantIdLike(String tenantIdLike) {
        this.tenantIdLike = tenantIdLike;
    }

    public String getTenantIdLike() {
        return tenantIdLike;
    }

    public void setWithoutTenantId(Boolean withoutTenantId) {
        this.withoutTenantId = withoutTenantId;
    }

    public Boolean getWithoutTenantId() {
        return withoutTenantId;
    }

    public String getCandidateOrAssigned() {
        return candidateOrAssigned;
    }

    public void setCandidateOrAssigned(String candidateOrAssigned) {
        this.candidateOrAssigned = candidateOrAssigned;
    }
}
