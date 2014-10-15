/*
 * Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.bpmn.rest.integration;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineInfo;
import org.activiti.rest.common.api.ActivitiUtilProvider;

public class BPSActivitiUtilProvider implements ActivitiUtilProvider {

    private ProcessEngine processEngine;

    private ProcessEngineInfo processEngineInfo;

    @Override
    public ProcessEngine getProcessEngine() {
        return processEngine;
    }

    @Override
    public ProcessEngineInfo getProcessEngineInfo() {
        return processEngineInfo;
    }

    public void setProcessEngine(ProcessEngine processEngine) {
        this.processEngine = processEngine;
    }

    public void setProcessEngineInfo(ProcessEngineInfo processEngineInfo) {
        this.processEngineInfo = processEngineInfo;
    }
}
