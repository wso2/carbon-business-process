package org.wso2.carbon.bpmn.rest.model.correlation;

import org.wso2.carbon.bpmn.rest.engine.variable.QueryVariable;
import org.wso2.carbon.bpmn.rest.model.runtime.ExecutionActionRequest;

import javax.xml.bind.annotation.*;
import java.util.List;

@XmlRootElement(name = "CorrelationActionRequest")
@XmlAccessorType(XmlAccessType.FIELD)
public class CorrelationActionRequest extends ExecutionActionRequest {

    private String processDefinitionId = null;
    private String processDefinitionKey = null;
    @XmlElementWrapper(name = "QueryVariables")
    @XmlElement(name = "QueryVariable", type = QueryVariable.class)
    private List<QueryVariable> variables;

    @XmlElementWrapper(name = "QueryProcessInstanceVariables")
    @XmlElement(name = "QueryVariable", type = QueryVariable.class)
    private List<QueryVariable> processInstanceVariables;
  /*
    @XmlElementWrapper(name = "CorrelationRestVariables")
    @XmlElement(name = "QueryVariable", type = QueryVariable.class)
    private List<QueryVariable> correlationVariables;*/
    private String activityId;
    private String tenantId;

    private String action;

    public CorrelationActionRequest(){}

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

    public List<QueryVariable> getVariables() {
        return variables;
    }

    public void setVariables(List<QueryVariable> variables) {
        this.variables = variables;
    }

   /* public List<QueryVariable> getCorrelationVariables() {
        return correlationVariables;
    }

    public void setCorrelationVariables(List<QueryVariable> correlationVariables) {
        this.correlationVariables = correlationVariables;
    }*/

    public String getActivityId() {
        return activityId;
    }

    public void setActivityId(String activityId) {
        this.activityId = activityId;
    }


    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getSignalName() {
        return signalName;
    }

    public void setSignalName(String signalName) {
        this.signalName = signalName;
    }


    public String getMessageName() {
        return messageName;
    }

    public void setMessageName(String messageName) {
        this.messageName = messageName;
    }

    public List<QueryVariable> getProcessInstanceVariables() {
        return processInstanceVariables;
    }

    public void setProcessInstanceVariables(List<QueryVariable> processInstanceVariables) {
        this.processInstanceVariables = processInstanceVariables;
    }

    @Override
    public String getAction() {
        return action;
    }

    @Override
    public void setAction(String action) {
        this.action = action;
    }
}
