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
package org.wso2.carbon.bpmn.rest.model.stats;

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
import java.util.List;

/**
 * Holds the response of each REST call
 */
@XmlRootElement(name = "Response")
@XmlAccessorType(XmlAccessType.FIELD)
public class ResponseHolder {
    @XmlElementWrapper(name = "DataResponse")
    @XmlElements(value = {
            @XmlElement(name = "Task",
                    type = BPMNTaskInstance.class),
            @XmlElement(name = "CompletedProcess",
                    type = CompletedProcesses.class),
            @XmlElement(name = "DeployedProcess",
                    type = DeployedProcesses.class),
            @XmlElement(name = "InstanceVariation",
                    type = InstanceStatPerMonth.class),
            @XmlElement(name = "ProcessTaskCount",
                    type = ProcessTaskCount.class),
            @XmlElement(name = "UserTaskCount",
                    type = UserTaskCount.class),
            @XmlElement(name = "UserTaskDuration",
                    type = UserTaskDuration.class)
    })
    List<Object> data;

    public List<Object> getData() {
        return data;
    }

    public void setData(List<Object> data) {
        this.data = data;
    }
}
