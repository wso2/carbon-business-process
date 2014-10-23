/**
 *  Copyright (c) 2014 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.bpmn.core;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.persistence.entity.GroupIdentityManager;
import org.activiti.engine.impl.persistence.entity.UserIdentityManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bpmn.core.integration.BPSGroupManagerFactory;
import org.wso2.carbon.bpmn.core.integration.BPSUserManagerFactory;
import org.wso2.carbon.utils.CarbonUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * Class responsible for building and initiating the activiti engine
 *
 */
public class ActivitiEngineBuilder {

	private static final Log log = LogFactory.getLog(ActivitiEngineBuilder.class);

	private String dataSourceJndiName = null;

	/**
	 * Instantiates the engine. Builds the state of the engine
	 *
	 * @return  ProcessEngineImpl object
	 * @throws BPSException  Throws in the event of failure of ProcessEngine
	 */

    public ProcessEngine buildEngine() throws BPSException {
        ProcessEngine engine = null;
        try {
            String carbonConfigDirPath = CarbonUtils.getCarbonConfigDirPath();
            String activitiConfigPath = carbonConfigDirPath + File.separator +
                    BPMNConstants.ACTIVITI_CONFIGURATION_FILE_NAME;
            File activitiConfigFile = new File(activitiConfigPath);
            ProcessEngineConfigurationImpl processEngineConfigurationImpl =
                    (ProcessEngineConfigurationImpl) ProcessEngineConfiguration.createProcessEngineConfigurationFromInputStream(
                            new FileInputStream(
                                    activitiConfigFile));
            // we have to build the process engine first to initialize session factories.
            engine = processEngineConfigurationImpl.buildProcessEngine();
            processEngineConfigurationImpl.getSessionFactories().put(UserIdentityManager.class,
                    new BPSUserManagerFactory());
            processEngineConfigurationImpl.getSessionFactories().put(GroupIdentityManager.class,
                    new BPSGroupManagerFactory());

            dataSourceJndiName = processEngineConfigurationImpl.getProcessEngineConfiguration()
                    .getDataSourceJndiName();

        } catch (FileNotFoundException e) {
            String msg = "Failed to create an Activiti engine. Activiti configuration file not found";
            throw new BPSException(msg, e);
        }
        return engine;
    }

    public String getDataSourceJndiName() {
		return dataSourceJndiName;
	}
}
