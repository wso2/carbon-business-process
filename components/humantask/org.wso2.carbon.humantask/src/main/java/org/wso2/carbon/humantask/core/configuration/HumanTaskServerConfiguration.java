/*
 * Copyright (c) 2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.humantask.core.configuration;

import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xmlbeans.XmlException;
import org.quartz.CronExpression;
import org.wso2.carbon.humantask.core.HumanTaskConstants;
import org.wso2.carbon.humantask.core.dao.TaskStatus;
import org.wso2.carbon.humantask.server.config.*;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.securevault.SecretResolver;
import org.wso2.securevault.SecretResolverFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The memory model of the humantask configuration - humantask.xml.
 */
public class HumanTaskServerConfiguration {

	private static final Log log = LogFactory.getLog(HumanTaskServerConfiguration.class);

	private HumanTaskServerConfigDocument htServerConfigDocument;

	private String dataSourceName;

	private String dataSourceJNDIRepoInitialContextFactory;

	private String dataSourceJNDIRepoProviderURL;

	private boolean generateDdl = false;

	private boolean showSql = false;

	private String daoConnectionFactoryClass;

	private String peopleQueryEvaluatorClass = "org.wso2.carbon.humantask.core.integration.CarbonUserManagerBasedPeopleQueryEvaluator";

	private int threadPoolMaxSize = 50;

	// private String transactionFactoryClass =
	// "com.atomikos.icatch.jta.UserTransactionManager";
	private String transactionFactoryClass = "org.apache.ode.il.EmbeddedGeronimoFactory";

	private List<TaskStatus> removableTaskStatuses = Collections.emptyList();

	private List<String> eventListenerClassNames = new ArrayList<String>();

	private String taskCleanupCronExpression;

	private boolean uiRenderingEnabled = false;

	// HT-Coordination Related Properties
	private boolean htCoordinationEnabled = false;
	private boolean taskRegistrationEnabled = false;
	private String registrationServiceAuthUsername;
	private String registrationServiceAuthPassword;
	private boolean clusteredTaskEngines = false;
	private String loadBalancerURL;

	private boolean cachingEnabled = false;

	private int cacheExpiryDuration = HumanTaskConstants.DEFAULT_CACHE_EXPIRY_DURATION;

	private File htServerConfigurationFile;

	private boolean isTaskOperationsForBusinessAdministratorEnabled = false;

	private boolean validateTaskBeforeDeployment = false;

	private boolean emailNotification = false;

	private boolean smsNotification = false;

	

	/**
	 * Create Human Task Server Configuration from a configuration file. If
	 * error occurred while parsing configuration file, default configuration
	 * will be created.
	 * 
	 * @param htServerConfig
	 *            XMLBeans object of human task server configuration file
	 */
	public HumanTaskServerConfiguration(File htServerConfig) {
		htServerConfigDocument = readConfigurationFromFile(htServerConfig);

		if (htServerConfigDocument == null) {
			return;
		}

		// This is for HT-Coordination configuration.
		this.htServerConfigurationFile = htServerConfig;
		initConfigurationFromFile();
	}

	public HumanTaskServerConfiguration() {
		this.dataSourceName = "bpsds";
		this.daoConnectionFactoryClass = "org.wso2.carbon.humantask.dao.jpa.openjpa.HumanTaskDAOConnectionFactoryImpl";
		this.dataSourceJNDIRepoInitialContextFactory = "com.sun.jndi.rmi.registry.RegistryContextFactory";
		this.dataSourceJNDIRepoProviderURL = "rmi://localhost:2199";
		this.peopleQueryEvaluatorClass = "org.wso2.carbon.humantask.core.integration.CarbonUserManagerBasedPeopleQueryEvaluator";
		// this.threadPoolMaxSize = 50;
		this.htCoordinationEnabled = false;
		this.taskRegistrationEnabled = false;
		this.clusteredTaskEngines = false;
	}

	private HumanTaskServerConfigDocument readConfigurationFromFile(File htServerConfiguration) {
		try {
			return HumanTaskServerConfigDocument.Factory.parse(new FileInputStream(
					htServerConfiguration));
		} catch (XmlException e) {
			log.error("Error parsing human task server configuration.", e);
		} catch (FileNotFoundException e) {
			log.info("Cannot find the human task server configuration in specified location "
					+ htServerConfiguration.getPath() + " . Loads the default configuration.");
		} catch (IOException e) {
			log.error("Error reading human task server configuration file"
					+ htServerConfiguration.getPath() + " .");
		}

		return null;
	}

	// Initialise the configuration object from the properties in the human task
	// server config xml file.
	private void initConfigurationFromFile() {
		THumanTaskServerConfig tHumanTaskServerConfig = htServerConfigDocument
				.getHumanTaskServerConfig();
		if (tHumanTaskServerConfig == null) {
			return;
		}

		if (tHumanTaskServerConfig.getPersistenceConfig() != null) {
			initPersistenceConfig(tHumanTaskServerConfig.getPersistenceConfig());
		}

		if (tHumanTaskServerConfig.getPeopleQueryEvaluatorConfig() != null) {
			initPeopleQueryEvaluator(tHumanTaskServerConfig.getPeopleQueryEvaluatorConfig());
		}

		if (tHumanTaskServerConfig.getEnableTaskOperationsForBusinessAdministrator()) {
			this.isTaskOperationsForBusinessAdministratorEnabled = true;
		}

		if (tHumanTaskServerConfig.getSchedulerConfig() != null) {
			initSchedulerConfig(tHumanTaskServerConfig.getSchedulerConfig());
		}

		if (tHumanTaskServerConfig.getTransactionManagerConfig() != null) {
			initTransactionManagerConfig(tHumanTaskServerConfig.getTransactionManagerConfig());
		}

		if (tHumanTaskServerConfig.getTaskCleanupConfig() != null) {
			iniTaskCleanupConfig(tHumanTaskServerConfig.getTaskCleanupConfig());
		}

		if (tHumanTaskServerConfig.getTaskEventListeners() != null) {
			initEventListeners(tHumanTaskServerConfig.getTaskEventListeners());
		}

		if (tHumanTaskServerConfig.getUIRenderingEnabled()) {
			uiRenderingEnabled = true;
		}

		if (tHumanTaskServerConfig.getHumanTaskCoordination() != null) {
			initCoordinationConfiguration(tHumanTaskServerConfig.getHumanTaskCoordination());
		}

		if (tHumanTaskServerConfig.getCacheConfiguration() != null) {
			initCacheConfiguration(tHumanTaskServerConfig.getCacheConfiguration());
		}
		if (tHumanTaskServerConfig.getEnableTaskValidationBeforeDeployment()) {
			validateTaskBeforeDeployment = true;
		}
		if(tHumanTaskServerConfig.getEnableEMailNotificaion()) {
            emailNotification = true;
        }
        if(tHumanTaskServerConfig.getEnableSMSNotificaion()) {
            smsNotification = true;
        }
	}

	private void iniTaskCleanupConfig(TTaskCleanupConfig taskCleanupConfig) {

		if (taskCleanupConfig != null) {
			if (StringUtils.isNotEmpty(taskCleanupConfig.getCronExpression())) {
				if (CronExpression.isValidExpression(taskCleanupConfig.getCronExpression().trim())) {
					this.taskCleanupCronExpression = taskCleanupConfig.getCronExpression();
				} else {
					String warnMsg = String.format(
							"The task clean up cron expression[%s] is invalid."
									+ " Ignoring task clean up configurations! ",
							taskCleanupConfig.getCronExpression());
					log.warn(warnMsg);
					return;
				}
			}

			if (StringUtils.isNotEmpty(taskCleanupConfig.getStatuses())) {
				String[] removableStatusesArray = taskCleanupConfig.getStatuses().split(",");

				List<TaskStatus> removableTaskStatusList = new ArrayList<TaskStatus>();
				for (String removableStatus : removableStatusesArray) {
					for (TaskStatus taskStatusEnum : TaskStatus.values()) {
						if (taskStatusEnum.toString().equals(removableStatus.trim())) {
							removableTaskStatusList.add(taskStatusEnum);
							break;
						}
					}
				}
				this.removableTaskStatuses = removableTaskStatusList;
			}
		}
	}

	private void initEventListeners(TTaskEventListeners taskEventListeners) {
		if (taskEventListeners.getClassNameArray() != null) {
			for (String eventListenerClassName : taskEventListeners.getClassNameArray()) {
				if (StringUtils.isNotEmpty(eventListenerClassName)) {
					this.eventListenerClassNames.add(eventListenerClassName.trim());
				}
			}
		}
	}

	private void initTransactionManagerConfig(TTransactionManagerConfig tTransactionManagerConfig) {
		if (tTransactionManagerConfig.getTransactionManagerClass() != null) {
			this.transactionFactoryClass = tTransactionManagerConfig.getTransactionManagerClass()
					.trim();
		} else {
			log.debug("TransactionManagerClass not provided with HumanTask configuration."
					+ "Using default TransactionManagerClass :" + transactionFactoryClass);
		}
	}

	private void initSchedulerConfig(TSchedulerConfig tSchedulerConfig) {
		if (tSchedulerConfig.getMaxThreadPoolSize() > 0) {
			this.threadPoolMaxSize = tSchedulerConfig.getMaxThreadPoolSize();
		} else {
			log.debug("ThreadPoolMaxSize not provided with HumanTask configuration."
					+ "Using default thread pool max value of :" + threadPoolMaxSize);
		}
	}

	private void initPeopleQueryEvaluator(TPeopleQueryEvaluatorConfig tUserManagerConfig) {
		if (tUserManagerConfig.getPeopleQueryEvaluatorClass() != null) {
			this.peopleQueryEvaluatorClass = tUserManagerConfig.getPeopleQueryEvaluatorClass()
					.trim();
		} else {
			log.debug("PeopleQueryEvaluatorConfig is not provided with HumanTask configuration."
					+ "Using default PeopleQueryEvaluatorClass: " + peopleQueryEvaluatorClass);
		}
	}

	private void initPersistenceConfig(TPersistenceConfig tPersistenceConfig) {
		if (tPersistenceConfig.getDataSource() != null) {
			this.dataSourceName = tPersistenceConfig.getDataSource().trim();
		}
		if (tPersistenceConfig.getJNDIInitialContextFactory() != null) {
			this.dataSourceJNDIRepoInitialContextFactory = tPersistenceConfig
					.getJNDIInitialContextFactory().trim();
		}
		if (tPersistenceConfig.getJNDIProviderUrl() != null) {
			this.dataSourceJNDIRepoProviderURL = tPersistenceConfig.getJNDIProviderUrl().trim();
			int portOffset = getCarbonPortOffset();

			// We need to adjust the port value according to the offset defined
			// in the carbon configuration.
			String portValueString = dataSourceJNDIRepoProviderURL.substring(
					dataSourceJNDIRepoProviderURL.lastIndexOf(':') + 1,
					dataSourceJNDIRepoProviderURL.length());

			String urlWithoutPort = dataSourceJNDIRepoProviderURL.substring(0,
					dataSourceJNDIRepoProviderURL.lastIndexOf(':') + 1);

			int actualPortValue = Integer.parseInt(portValueString);
			int correctedPortValue = actualPortValue + portOffset;

			this.dataSourceJNDIRepoProviderURL = urlWithoutPort.concat(Integer
					.toString(correctedPortValue));

		}

		if (tPersistenceConfig.getDAOConnectionFactoryClass() != null) {
			this.daoConnectionFactoryClass = tPersistenceConfig.getDAOConnectionFactoryClass()
					.trim();
		}

		this.generateDdl = tPersistenceConfig.getGenerateDdl();
		this.showSql = tPersistenceConfig.getShowSql();
	}

	private void initCoordinationConfiguration(THumanTaskCoordination humanTaskCoordination) {
		this.htCoordinationEnabled = humanTaskCoordination.getTaskCoordinationEnabled();
		this.taskRegistrationEnabled = humanTaskCoordination.getTaskRegistrationEnabled();
		if (humanTaskCoordination.getRegistrationServiceAuthentication() != null) {
			getAuthenticationConfig(htServerConfigurationFile,
					humanTaskCoordination.getRegistrationServiceAuthentication());
		}

		if (humanTaskCoordination.getClusteredTaskEngines() != null) {
			this.clusteredTaskEngines = true;
			parseClusterDetails(humanTaskCoordination.getClusteredTaskEngines());
		}
	}

	private void parseClusterDetails(TClusterConfig clusterConfig) {
		if (clusterConfig.getLoadBalancerURL() != null) {
			this.loadBalancerURL = clusterConfig.getLoadBalancerURL();
		} else {
			this.clusteredTaskEngines = false;
		}
	}

	private void getAuthenticationConfig(File file, TRegServiceAuth authentication) {
		// Since secretResolver only accept Element we have to build Element
		// here.
		SecretResolver secretResolver = null;
		InputStream in = null;
		try {
			in = new FileInputStream(file);
			StAXOMBuilder builder = new StAXOMBuilder(in);
			secretResolver = SecretResolverFactory.create(builder.getDocumentElement(), true);
		} catch (Exception e) {
			log.warn(
					"Error occurred while retrieving secured TaskEngineProtocolHandler configuration.",
					e);
		} finally {
			try {
				in.close();
			} catch (IOException e) {
				log.error(e.getLocalizedMessage(), e);
			}
		}
		// Get Username
		if (secretResolver != null
				&& secretResolver.isInitialized()
				&& secretResolver
						.isTokenProtected(HumanTaskConstants.B4P_REGISTRATIONS_USERNAME_ALIAS)) {
			this.registrationServiceAuthUsername = secretResolver
					.resolve(HumanTaskConstants.B4P_REGISTRATIONS_USERNAME_ALIAS);
			if (log.isDebugEnabled()) {
				log.debug("Loaded Registration service admin username from secure vault");
			}
		} else {
			if (authentication.getUsername() != null) {
				this.registrationServiceAuthUsername = authentication.getUsername();
			}
		}
		// Get Password
		if (secretResolver != null
				&& secretResolver.isInitialized()
				&& secretResolver
						.isTokenProtected(HumanTaskConstants.B4P_REGISTRATIONS_PASSWORD_ALIAS)) {
			this.registrationServiceAuthPassword = secretResolver
					.resolve(HumanTaskConstants.B4P_REGISTRATIONS_PASSWORD_ALIAS);
			if (log.isDebugEnabled()) {
				log.debug("Loaded  Registration service admin password from secure vault");
			}
		} else {
			if (authentication.getPassword() != null) {
				this.registrationServiceAuthPassword = authentication.getPassword();
			}
		}
	}

	private void initCacheConfiguration(TCacheConfiguration cacheConfiguration) {

		this.cachingEnabled = cacheConfiguration.getEnableCaching();
		if (cachingEnabled) {
			if (cacheConfiguration.getCacheExpiryDuration() >= 0) {
				this.cacheExpiryDuration = cacheConfiguration.getCacheExpiryDuration();
			} else {
				log.warn("The Cache Expiry duration is invalid. Using default Cache expiry duration :"
						+ HumanTaskConstants.DEFAULT_CACHE_EXPIRY_DURATION + " seconds");
			}
		}
	}

	// gets the carbon port offset value.
	private int getCarbonPortOffset() {

		String offset = CarbonUtils.getServerConfiguration().getFirstProperty(
				HumanTaskConstants.CARBON_CONFIG_PORT_OFFSET_NODE);

		try {
			return ((offset != null) ? Integer.parseInt(offset.trim()) : 0);
		} catch (NumberFormatException e) {
			log.warn("Error occurred while reading port offset. Invalid port offset: " + offset
					+ " Setting the port offset to 0", e);
			return 0;
		}
	}

	/**
	 * @return : The data source name.
	 */
	public String getDataSourceName() {
		return dataSourceName;
	}

	/**
	 * @return :
	 */
	public String getDataSourceJNDIRepoInitialContextFactory() {
		return dataSourceJNDIRepoInitialContextFactory;
	}

	/**
	 * @return : the JNDI repo provider URL.
	 */
	public String getDataSourceJNDIRepoProviderURL() {
		return dataSourceJNDIRepoProviderURL;
	}

	/**
	 * @return : Return the is generate DDL option in the server configuration.
	 */
	public boolean isGenerateDdl() {
		return generateDdl;
	}

	/**
	 * @return : The value of the show sqp property in the config file.
	 */
	public boolean isShowSql() {
		return showSql;
	}

	/**
	 * @return : the dao connection factory class.
	 */
	public String getDaoConnectionFactoryClass() {
		return daoConnectionFactoryClass;
	}

	/**
	 * @return : The class name of the people query evaluation implementation.
	 */
	public String getPeopleQueryEvaluatorClass() {
		return peopleQueryEvaluatorClass;
	}

	/**
	 * @return : The thread pool max size.
	 */
	public int getThreadPoolMaxSize() {
		return threadPoolMaxSize;
	}

	/**
	 * @return : The transaction factory class.
	 */
	public String getTransactionFactoryClass() {
		return transactionFactoryClass;
	}

	/**
	 * @return : The task cleanup cron expression.
	 */
	public String getTaskCleanupCronExpression() {
		return taskCleanupCronExpression;
	}

	/**
	 * @return : The list of event listener classes in the humantask config
	 *         file.
	 */
	public List<String> getEventListenerClassNames() {
		return this.eventListenerClassNames;
	}

	/**
	 * @return : Return the list of removable task statuses in the humantask
	 *         config file.
	 */
	public List<TaskStatus> getRemovableTaskStatuses() {
		return removableTaskStatuses;
	}

	/**
	 * @return : true if we have a valid task cleanup configuration parameters.
	 *         False otherwise.
	 */
	public boolean isTaskCleanupEnabled() {
		return StringUtils.isNotEmpty(this.taskCleanupCronExpression)
				&& CronExpression.isValidExpression(taskCleanupCronExpression)
				&& removableTaskStatuses.size() > 0;
	}

	public boolean isUiRenderingEnabled() {
		return uiRenderingEnabled;
	}

	/**
	 * @return true if HumanTask coordination enabled.
	 */
	public boolean isHtCoordinationEnabled() {
		return htCoordinationEnabled;
	}

	public boolean isTaskRegistrationEnabled() {
		return taskRegistrationEnabled;
	}

	/**
	 * 
	 * @return Username of the B4P coordination component's registration service
	 */
	public String getRegistrationServiceAuthUsername() {
		return registrationServiceAuthUsername;
	}

	/**
	 * @return User Password of the B4P coordination component's registration
	 *         service.
	 */
	public String getRegistrationServiceAuthPassword() {
		return registrationServiceAuthPassword;
	}

	public String getLoadBalancerURL() {
		return loadBalancerURL;
	}

	public boolean isClusteredTaskEngines() {
		return clusteredTaskEngines;
	}

	public boolean isCachingEnabled() {
		return cachingEnabled;
	}

	public int getCacheExpiryDuration() {
		return cacheExpiryDuration;
	}

	public boolean isTaskOperationsForBusinessAdministratorEnabled() {
		return isTaskOperationsForBusinessAdministratorEnabled;
	}

	public boolean getEnableTaskValidationBeforeDeployment() {
		return validateTaskBeforeDeployment;
	}
	
	public boolean getEnableEMailNotificaion() {
		return emailNotification;
	}

	public boolean getEnableSMSNotificaion() {
		return smsNotification;
	}

}
