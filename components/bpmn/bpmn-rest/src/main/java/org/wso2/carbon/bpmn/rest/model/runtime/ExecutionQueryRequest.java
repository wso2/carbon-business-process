package org.wso2.carbon.bpmn.rest.model.runtime;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.wso2.carbon.bpmn.rest.common.PaginateRequest;
import org.wso2.carbon.bpmn.rest.engine.variable.QueryVariable;

import javax.xml.bind.annotation.*;
import java.util.List;

@XmlRootElement(name = "ExecutionQueryRequest")
@XmlAccessorType(XmlAccessType.FIELD)
public class ExecutionQueryRequest extends PaginateRequest {
    private String id;
    private String activityId;
    private String parentId;
    private String processInstanceId;
    private String processBusinessKey;
    private String processDefinitionId;
    private String processDefinitionKey;
    private String signalEventSubscriptionName;
    private String messageEventSubscriptionName;
    @XmlElementWrapper(name = "QueryVariables")
    @XmlElement(name = "QueryVariable", type = QueryVariable.class)
    private List<QueryVariable> variables;
    @XmlElementWrapper(name = "ProcessInstanceQueryVariable")
    @XmlElement(name = "QueryVariables", type = QueryVariable.class)
    private List<QueryVariable> processInstanceVariables;
    private String tenantId;
    private String tenantIdLike;
    private Boolean withoutTenantId;

    @JsonTypeInfo(use= JsonTypeInfo.Id.CLASS, defaultImpl=QueryVariable.class)
    public List<QueryVariable> getVariables() {
        return variables;
    }

    public void setVariables(List<QueryVariable> variables) {
        this.variables = variables;
    }

    public List<QueryVariable> getProcessInstanceVariables() {
        return processInstanceVariables;
    }
    @JsonTypeInfo(use= JsonTypeInfo.Id.CLASS, defaultImpl=QueryVariable.class)
    public void setProcessInstanceVariables(List<QueryVariable> processInstanceVariables) {
        this.processInstanceVariables = processInstanceVariables;
    }


    public String getId() {
        return id;
    }


    public void setId(String id) {
        this.id = id;
    }


    public String getProcessInstanceId() {
        return processInstanceId;
    }


    public void setProcessInstanceId(String processInstanceId) {
        this.processInstanceId = processInstanceId;
    }


    public String getProcessBusinessKey() {
        return processBusinessKey;
    }


    public void setProcessBusinessKey(String processBusinessKey) {
        this.processBusinessKey = processBusinessKey;
    }


    public String getProcessDefinitionId() {
        return processDefinitionId;
    }


    public void setProcessDefinitionId(String processDefinitionId) {
        this.processDefinitionId = processDefinitionId;
    }


    public String getProcessDefinitionKey() {
        return processDefinitionKey;
    }


    public void setProcessDefinitionKey(String processDefinitionKey) {
        this.processDefinitionKey = processDefinitionKey;
    }


    public String getSignalEventSubscriptionName() {
        return signalEventSubscriptionName;
    }


    public void setSignalEventSubscriptionName(String signalEventSubscriptionName) {
        this.signalEventSubscriptionName = signalEventSubscriptionName;
    }


    public String getMessageEventSubscriptionName() {
        return messageEventSubscriptionName;
    }


    public void setMessageEventSubscriptionName(String messageEventSubscriptionName) {
        this.messageEventSubscriptionName = messageEventSubscriptionName;
    }


    public String getActivityId() {
        return activityId;
    }

    public void setActivityId(String activityId) {
        this.activityId = activityId;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getTenantIdLike() {
        return tenantIdLike;
    }

    public void setTenantIdLike(String tenantIdLike) {
        this.tenantIdLike = tenantIdLike;
    }

    public Boolean getWithoutTenantId() {
        return withoutTenantId;
    }

    public void setWithoutTenantId(Boolean withoutTenantId) {
        this.withoutTenantId = withoutTenantId;
    }

}