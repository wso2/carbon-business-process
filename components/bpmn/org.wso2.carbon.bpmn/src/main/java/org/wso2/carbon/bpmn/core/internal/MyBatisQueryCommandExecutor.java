/**
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.bpmn.core.internal;

import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.interceptor.Command;

/**
 * Helper to initialize a minimized process engine and execute queries.
 */
public class MyBatisQueryCommandExecutor {
	private MyBatisExtendedSessionFactory myBatisExtendedSessionFactory;

	public MyBatisQueryCommandExecutor(ProcessEngineConfigurationImpl processEngineConfiguration,
	                                   String mappingResourceName) {
		myBatisExtendedSessionFactory = new MyBatisExtendedSessionFactory();
		myBatisExtendedSessionFactory.initFromProcessEngineConfiguration(processEngineConfiguration,
		                                                                 mappingResourceName);
	}

	public <T> T executeQueryCommand(Command<T> command) {
		return myBatisExtendedSessionFactory.getCommandExecutorTxRequired().execute(command);
	}

}




