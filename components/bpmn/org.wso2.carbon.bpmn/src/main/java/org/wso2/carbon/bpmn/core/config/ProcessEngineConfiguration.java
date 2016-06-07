/**
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 **/


package org.wso2.carbon.bpmn.core.config;

import org.activiti.engine.impl.cfg.StandaloneProcessEngineConfiguration;

/**
 * BPMN Engine configuration.
 */
public class ProcessEngineConfiguration {

    // Activiti engine configuration.
    private StandaloneProcessEngineConfiguration activitiEngineConfiguration;

    // Define other configuration here.

    /**
     * Default process engine configuration.
     */
    public ProcessEngineConfiguration() {
        this.activitiEngineConfiguration = new StandaloneProcessEngineConfiguration();
    }

    public StandaloneProcessEngineConfiguration getActivitiEngineConfiguration() {
        return activitiEngineConfiguration;
    }

    public void setActivitiEngineConfiguration(StandaloneProcessEngineConfiguration activitiEngineConfiguration) {
        this.activitiEngineConfiguration = activitiEngineConfiguration;
    }
}
