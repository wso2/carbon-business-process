/**
 * Copyright (c) 2015 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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


package org.wso2.carbon.bpmn.rest.model.common;

import org.wso2.carbon.bpmn.rest.model.history.HistoricActivityInstanceResponse;
import org.wso2.carbon.bpmn.rest.model.history.HistoricDetailResponse;
import org.wso2.carbon.bpmn.rest.model.history.HistoricProcessInstanceResponse;
import org.wso2.carbon.bpmn.rest.model.history.HistoricVariableInstanceResponse;
import org.wso2.carbon.bpmn.rest.model.repository.DeploymentResponse;
import org.wso2.carbon.bpmn.rest.model.repository.ModelResponse;
import org.wso2.carbon.bpmn.rest.model.repository.ProcessDefinitionResponse;
import org.wso2.carbon.bpmn.rest.model.runtime.ExecutionResponse;
import org.wso2.carbon.bpmn.rest.model.runtime.HistoricTaskInstanceResponse;
import org.wso2.carbon.bpmn.rest.model.runtime.ProcessInstanceResponse;
import org.wso2.carbon.bpmn.rest.model.runtime.TaskResponse;

import javax.xml.bind.annotation.*;

@XmlRootElement(name = "DataResponse")
@XmlAccessorType(XmlAccessType.FIELD)
public class DataResponse {

    //
    @XmlElementWrapper(name = "Responses")
    @XmlElements(value = {
            @XmlElement(name = "ProcessInstanceResponse",
                    type = ProcessInstanceResponse.class),
            @XmlElement(name = "DeploymentResponse",
                    type = DeploymentResponse.class),
            @XmlElement(name = "HistoricActivityInstanceResponse",
                    type = HistoricActivityInstanceResponse.class),
            @XmlElement(name = "HistoricDetailResponse",
                    type = HistoricDetailResponse.class),
            @XmlElement(name = "HistoricTaskInstanceResponse",
                    type = HistoricTaskInstanceResponse.class),
            @XmlElement(name = "TaskResponse",
                    type = TaskResponse.class),
            @XmlElement(name = "ProcessDefinitionResponse",
                    type = ProcessDefinitionResponse.class),
            @XmlElement(name = "ModelResponse",
                    type = ModelResponse.class),
            @XmlElement(name = "HistoricVariableInstanceResponse",
                    type = HistoricVariableInstanceResponse.class),
            @XmlElement(name = "HistoricProcessInstanceResponse",
                    type = HistoricProcessInstanceResponse.class),
            @XmlElement(name = "ExecutionResponse",
                    type = ExecutionResponse.class)
    })
    List<Object> data;
    long total;
    int start;
    String sort;
    String order;
    int size;
    String message;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getData() {
        return data;
    }

    public DataResponse setData(List<Object> data) {
        this.data = data;
        return this;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public String getSort() {
        return sort;
    }

    public void setSort(String sort) {
        this.sort = sort;
    }

    public String getOrder() {
        return order;
    }

    public void setOrder(String order) {
        this.order = order;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }
}
