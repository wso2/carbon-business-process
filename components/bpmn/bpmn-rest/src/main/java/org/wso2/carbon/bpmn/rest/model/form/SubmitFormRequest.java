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


package org.wso2.carbon.bpmn.rest.model.form;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import javax.xml.bind.annotation.*;
import java.util.List;

@XmlRootElement(name = "SubmitFormRequest")
@XmlAccessorType(XmlAccessType.FIELD)
public class SubmitFormRequest extends RestActionRequest {

    protected String processDefinitionId;
    protected String taskId;
    protected String businessKey;
    @XmlElementWrapper(name = "RestFormProperties")
    @XmlElement(name = "RestFormProperty", type = RestFormProperty.class)
    protected List<RestFormProperty> properties;

    public String getProcessDefinitionId() {
        return processDefinitionId;
    }
    public void setProcessDefinitionId(String processDefinitionId) {
        this.processDefinitionId = processDefinitionId;
    }
    public String getTaskId() {
        return taskId;
    }
    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }
    public String getBusinessKey() {
        return businessKey;
    }
    public void setBusinessKey(String businessKey) {
        this.businessKey = businessKey;
    }
    public void setProperties(List<RestFormProperty> properties) {
        this.properties = properties;
    }
    @JsonTypeInfo(use= JsonTypeInfo.Id.CLASS, defaultImpl=RestFormProperty.class)
    public List<RestFormProperty> getProperties() {
        return properties;
    }
}
