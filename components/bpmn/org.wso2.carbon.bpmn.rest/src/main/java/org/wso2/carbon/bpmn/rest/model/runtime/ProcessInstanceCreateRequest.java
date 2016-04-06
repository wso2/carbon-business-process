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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.apache.commons.lang3.StringUtils;
import org.wso2.carbon.bpmn.rest.common.RestResponseFactory;
import org.wso2.carbon.bpmn.rest.engine.variable.QueryVariable;
import org.wso2.carbon.bpmn.rest.engine.variable.RestVariable;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;


/**
 *
 */
@XmlRootElement(name = "ProcessInstanceCreateRequest")
@XmlAccessorType(XmlAccessType.FIELD)
public class ProcessInstanceCreateRequest {

    private String processDefinitionId;
    private String processDefinitionKey;
    private String message;
    private String messageName;
    private String businessKey;
    @XmlElementWrapper(name = "Variables")
    @XmlElement(name = "Variable", type = RestVariable.class)
    private List<RestVariable> variables;
    @XmlElementWrapper(name = "AdditionalVariabls")
    @XmlElement(name = "AdditionalVariable", type = RestVariable.class)
    private List<RestVariable> additionalVariables;
    private String tenantId;
    private Boolean skipInstanceCreationIfExist = false;
    private Boolean correlate = false;

    public ProcessInstanceCreateRequest() {
    }

    public boolean isArrayIterated() {
        return arrayIterated;
    }

    public void setArrayIterated(boolean arrayIterated) {
        this.arrayIterated = arrayIterated;
    }

    private boolean arrayIterated;

    private boolean returnVariables;

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

    public String getBusinessKey() {
        return businessKey;
    }

    public void setBusinessKey(String businessKey) {
        this.businessKey = businessKey;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getTenantId() {
        return tenantId;
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, defaultImpl = RestVariable.class)
    public List<RestVariable> getVariables() {
        return variables;
    }

    public void setVariables(List<RestVariable> variables) {
        this.variables = variables;
    }

    @JsonIgnore
    public boolean isCustomTenantSet() {
        return tenantId != null && !StringUtils.isEmpty(tenantId);
    }

    public boolean getReturnVariables() {
        return returnVariables;
    }

    public void setReturnVariables(boolean returnVariables) {
        this.returnVariables = returnVariables;
    }

    public Boolean getSkipInstanceCreationIfExist() {
        return skipInstanceCreationIfExist;
    }

    public void setSkipInstanceCreationIfExist(Boolean skipInstanceCreationIfExist) {
        this.skipInstanceCreationIfExist = skipInstanceCreationIfExist;
    }

    public boolean isReturnVariables() {
        return returnVariables;
    }

    public List<RestVariable> getAdditionalVariables() {
        return additionalVariables;
    }

    public void setAdditionalVariables(List<RestVariable> additionalVariables) {
        this.additionalVariables = additionalVariables;
    }

    public Boolean getCorrelate() {
        return correlate;
    }

    public void setCorrelate(Boolean correlate) {
        this.correlate = correlate;
    }

    public String getMessageName() {
        return messageName;
    }

    public void setMessageName(String messageName) {
        this.messageName = messageName;
    }

    public ProcessInstanceQueryRequest cloneInstanceCreationRequest() {

        ProcessInstanceQueryRequest processInstanceQueryRequest = new ProcessInstanceQueryRequest();

        if (processDefinitionId != null) {
            processInstanceQueryRequest.setProcessDefinitionId(processDefinitionId);
        }

        if (businessKey != null) {
            processInstanceQueryRequest.setProcessBusinessKey(businessKey);
        }

        if (tenantId != null) {
            processInstanceQueryRequest.setTenantId(tenantId);
        }

        if (variables != null) {
            RestResponseFactory restResponseFactory = new RestResponseFactory();
            List<QueryVariable> queryVariableList = new ArrayList<>();
            for (RestVariable restVariable : variables) {
                QueryVariable queryVariable = new QueryVariable();
                queryVariable.setName(restVariable.getName());
                queryVariable.setOperation("equals");
                queryVariable.setType(restVariable.getType());
                queryVariable.setValue(restResponseFactory.getVariableValue(restVariable));
                queryVariableList.add(queryVariable);
            }
            processInstanceQueryRequest.setVariables(queryVariableList);
        }

        processInstanceQueryRequest.setIncludeProcessVariables(true);

        return processInstanceQueryRequest;
    }
}
