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

package org.wso2.carbon.bpmn.rest.model.history;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.wso2.carbon.bpmn.rest.engine.variable.QueryVariable;

import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 */
@XmlRootElement(name = "HistoricActivityInstanceQueryRequest")
@XmlAccessorType(XmlAccessType.FIELD)
public class HistoricVariableInstanceQueryRequest {

    private Boolean excludeTaskVariables;
    private String taskId;
    private String executionId;
    private String processInstanceId;
    private String variableName;
    private String variableNameLike;
    @XmlElementWrapper(name = "QueryVariables")
    @XmlElement(name = "QueryVariable", type = QueryVariable.class)
    private List<QueryVariable> variables;

    public Boolean getExcludeTaskVariables() {
        return excludeTaskVariables;
    }

    public void setExcludeTaskVariables(Boolean excludeTaskVariables) {
        this.excludeTaskVariables = excludeTaskVariables;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getExecutionId() {
        return executionId;
    }

    public void setExecutionId(String executionId) {
        this.executionId = executionId;
    }

    public String getProcessInstanceId() {
        return processInstanceId;
    }

    public void setProcessInstanceId(String processInstanceId) {
        this.processInstanceId = processInstanceId;
    }

    public String getVariableName() {
        return variableName;
    }

    public void setVariableName(String variableName) {
        this.variableName = variableName;
    }

    public String getVariableNameLike() {
        return variableNameLike;
    }

    public void setVariableNameLike(String variableNameLike) {
        this.variableNameLike = variableNameLike;
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, defaultImpl = QueryVariable.class)
    public List<QueryVariable> getVariables() {
        return variables;
    }

    public void setVariables(List<QueryVariable> variables) {
        this.variables = variables;
    }
}
