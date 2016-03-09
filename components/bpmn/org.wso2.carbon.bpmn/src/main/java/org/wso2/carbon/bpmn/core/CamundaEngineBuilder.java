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

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.camunda.bpm.engine.impl.identity.db.DbIdentityServiceProvider;
import org.wso2.carbon.bpmn.core.integration.BPSUserManagerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.file.Path;

/**
 * Class responsible for building and initiating the activiti engine
 */
public class CamundaEngineBuilder {

	private static final Log log = LogFactory.getLog(CamundaEngineBuilder.class);

	private String dataSourceJndiName = null;
	private static ProcessEngine processEngine = null;


	 /* Instantiates the engine. Builds the state of the engine
	 *
	 * @return  ProcessEngineImpl object
	 * @throws BPSFault  Throws in the event of failure of ProcessEngine
	 */

	public ProcessEngine buildEngine() throws BPSFault {

		try {

			Path carbonConfigDirPath = org.wso2.carbon.kernel.utils.Utils.getCarbonConfigHome();
			String camundaConfigPath = carbonConfigDirPath + File.separator +
			                           BPMNConstants.CAMUNDA_CONFIGURATION_FILE_NAME;
			// TODO: remove after TEST PURPOSES
			//            String camundaConfigPath = "/Users/himasha/Desktop/351R/wso2bps-3.5.1/repository/conf" + File.separator +
			//                     BPMNConstants.CAMUNDA_CONFIGURATION_FILE_NAME;
			File camundaConfigFile = new File(camundaConfigPath);
			ProcessEngineConfigurationImpl processEngineConfigurationImpl =
					(ProcessEngineConfigurationImpl) ProcessEngineConfiguration
							.createProcessEngineConfigurationFromInputStream(
									new FileInputStream(camundaConfigFile));
			// we have to build the process engine first to initialize session factories.
			processEngine = processEngineConfigurationImpl.buildProcessEngine();

			processEngineConfigurationImpl.getSessionFactories()
			                              .put(DbIdentityServiceProvider.class,
			                                   new BPSUserManagerFactory());
			//TODO
			// processEngineConfigurationImpl.getSessionFactories().put(DbIdentityServiceProvider.class,
			//  new BPSGroupManagerFactory());

			dataSourceJndiName = processEngineConfigurationImpl.getDataSourceJndiName();

		} catch (FileNotFoundException e) {
			String msg = "Failed to create Camunda engine. Camunda configuration file not found";
			throw new BPSFault(msg, e);
		}
		return processEngine;
	}

	public String getDataSourceJndiName() {
		return dataSourceJndiName;
	}

	public static ProcessEngine getProcessEngine() {
		return CamundaEngineBuilder.processEngine;
	}
}
