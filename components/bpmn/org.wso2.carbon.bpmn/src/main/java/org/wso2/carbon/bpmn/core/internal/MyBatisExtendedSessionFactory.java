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

import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.cfg.StandaloneProcessEngineConfiguration;
import org.camunda.bpm.engine.impl.interceptor.CommandContextInterceptor;
import org.camunda.bpm.engine.impl.interceptor.CommandInterceptor;
import org.camunda.bpm.engine.impl.interceptor.LogInterceptor;
import org.camunda.bpm.engine.impl.util.ReflectUtil;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class MyBatisExtendedSessionFactory extends StandaloneProcessEngineConfiguration {
	private String resourceName;

	protected void init() {
		throw new IllegalArgumentException(
				"Normal 'init' on process engine only used for extended MyBatis mappings is not allowed, please use 'initFromProcessEngineConfiguration'. You cannot construct a process engine with this configuration.");
	}

	/**
	 * initialize the {@link ProcessEngineConfiguration} from an existing one,
	 * just using the database settings and initialize the database / MyBatis
	 * stuff.
	 */
	public void initFromProcessEngineConfiguration(
			ProcessEngineConfigurationImpl processEngineConfiguration, String resourceName) {
		this.resourceName = resourceName;

		setDatabaseType(processEngineConfiguration.getDatabaseType());
		setDataSource(processEngineConfiguration.getDataSource());
		setDatabaseTablePrefix(processEngineConfiguration.getDatabaseTablePrefix());
		initDataSource();
		initSerialization();
		initCommandContextFactory();
		initTransactionFactory();
		initTransactionContextFactory();
		initCommandExecutors();
		initSqlSessionFactory();
		initIncidentHandlers();
		initIdentityProviderSessionFactory();
		initSessionFactories();
	}

	/**
	 * CommandInterceptor to create command object
	 */
	@Override
	protected Collection<? extends CommandInterceptor> getDefaultCommandInterceptorsTxRequired() {
		List<CommandInterceptor> defaultCommandInterceptorsTxRequired =
				new ArrayList<CommandInterceptor>();
		defaultCommandInterceptorsTxRequired.add(new LogInterceptor());
		defaultCommandInterceptorsTxRequired
				.add(new CommandContextInterceptor(commandContextFactory, this, true));
		return defaultCommandInterceptorsTxRequired;
	}

	/**
	 * +
	 * Return the name of the custom Mybatis configuration file
	 *
	 * @return
	 */

	@Override
	protected InputStream getMyBatisXmlConfigurationSteam() {
		return ReflectUtil.getResourceAsStream(resourceName);
	}

}


