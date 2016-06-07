/**
 * Copyright (c) 2014 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.bpmn.core;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.persistence.entity.GroupIdentityManager;
import org.activiti.engine.impl.persistence.entity.UserIdentityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.bpmn.core.db.mapper.DeploymentMapper;
import org.wso2.carbon.bpmn.core.integration.BPSGroupManagerFactory;
import org.wso2.carbon.bpmn.core.integration.BPSUserManagerFactory;
import org.wso2.carbon.bpmn.core.internal.BPMNServerHolder;

import java.util.HashSet;

/**
 * Class responsible for building and initiating the activiti engine.
 */
public class ActivitiEngineBuilder {

    private static final Logger log = LoggerFactory.getLogger(ActivitiEngineBuilder.class);
    private static ActivitiEngineBuilder instance = new ActivitiEngineBuilder();
    private String dataSourceJndiName;
    private ProcessEngine processEngine;

    private ActivitiEngineBuilder() {
        dataSourceJndiName = null;
        processEngine = null;
    }

    public static ActivitiEngineBuilder getInstance() {
        return instance;
    }

     /* Instantiates the engine. Builds the state of the engine
     *
     * @return  ProcessEngineImpl object
     * @throws BPSFault  Throws in the event of failure of ProcessEngine
     */

    public ProcessEngine buildEngine() throws BPSFault {

        ProcessEngineConfigurationImpl processEngineConfiguration = BPMNServerHolder.getInstance()
                .getProcessEngineConfiguration().getActivitiEngineConfiguration();

        // Adding custom mybatics mappers.
        if (processEngineConfiguration.getCustomMybatisMappers() == null) {
            processEngineConfiguration.setCustomMybatisMappers(new HashSet<>());
        }
        processEngineConfiguration.getCustomMybatisMappers().add(DeploymentMapper.class);

        // we have to build the process engine first to initialize session factories.
        processEngine = processEngineConfiguration.buildProcessEngine();
        processEngineConfiguration.getSessionFactories().put(UserIdentityManager.class,
                new BPSUserManagerFactory());
        processEngineConfiguration.getSessionFactories().put(GroupIdentityManager.class,
                new BPSGroupManagerFactory());

        dataSourceJndiName = processEngineConfiguration.getProcessEngineConfiguration()
                .getDataSourceJndiName();

        return processEngine;
    }

    public String getDataSourceJndiName() throws BPSFault {
        if (dataSourceJndiName == null) {
            buildEngine();
        }
        return dataSourceJndiName;

    }

    public ProcessEngine getProcessEngine() throws BPSFault {
        if (processEngine == null) {
            buildEngine();
        }
        return this.processEngine;
    }
}
