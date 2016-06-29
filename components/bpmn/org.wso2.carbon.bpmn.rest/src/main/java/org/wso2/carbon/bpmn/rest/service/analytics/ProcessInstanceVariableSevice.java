/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.bpmn.rest.service.analytics;

import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.HistoryService;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricProcessInstanceQuery;
import org.wso2.carbon.bpmn.rest.common.utils.BPMNOSGIService;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import java.util.HashMap;

@Path("/process-instance-variables") public class ProcessInstanceVariableSevice {
    HashMap<String, Object> processInstanceVariables = new HashMap<String, Object>();

    @Path("/{processInstanceId}") public HashMap<String, Object> getProcessInstanceVariables(
            @PathParam("processInstanceId") String processInstanceId) {

        HistoryService historyService = BPMNOSGIService.getHistoryService();
        HistoricProcessInstanceQuery kk = historyService.createHistoricProcessInstanceQuery()
                .processInstanceId(processInstanceId);
        HistoricProcessInstance instance = getHistoricProcessInstanceFromRequest(processInstanceId);

        return processInstanceVariables;
    }

    protected HistoricProcessInstance getHistoricProcessInstanceFromRequest(String processInstanceId) {
        HistoryService historyService = BPMNOSGIService.getHistoryService();
        HistoricProcessInstance processInstance = historyService.createHistoricProcessInstanceQuery()
                .processInstanceId(processInstanceId).singleResult();
        if (processInstance == null) {
            throw new ActivitiObjectNotFoundException(
                    "Could not find a process instance with id '" + processInstanceId + "'.",
                    HistoricProcessInstance.class);
        }
        return processInstance;
    }
}
