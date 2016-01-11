/**
 * Copyright (c) 2005-2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.bpmn.analytics.publisher.utils;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bpmn.analytics.publisher.AnalyticsPublisherConstants;
import org.wso2.carbon.bpmn.analytics.publisher.internal.BPMNAnalyticsHolder;
import org.wso2.carbon.bpmn.core.BPMNConstants;
import org.wso2.carbon.core.util.CryptoException;
import org.wso2.carbon.core.util.CryptoUtil;
import org.wso2.carbon.registry.api.Registry;
import org.wso2.carbon.registry.api.RegistryException;
import org.wso2.carbon.registry.api.Resource;
import org.wso2.carbon.utils.CarbonUtils;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Properties;

/**
 * BPMNDataReceiverConfig is used by AnalyticsPublisher to retrieve data publisher config from registry.
 */
public class BPMNDataReceiverConfig {
	private static final Log log = LogFactory.getLog(BPMNDataReceiverConfig.class);

	private int tenantID;
	// Config Registry instance.
	private Registry registry;

	/**
	 * Initinilze  BPMNDataReceiverConfig.
	 *
	 * @param tenantID current logged in tenant ID.
	 */
	public BPMNDataReceiverConfig(int tenantID) {
		this.tenantID = tenantID;
	}

	/**
	 * Check BPMN Data Publisher Configuration is activated or not in activiti.xml
	 *
	 * @return true if the BPMN Data Publisher is activated
	 * @throws IOException
	 * @throws XMLStreamException
	 */
	public static boolean isDASPublisherActivated() throws IOException, XMLStreamException {
		String carbonConfigDirPath = CarbonUtils.getCarbonConfigDirPath();
		String activitiConfigPath =
				carbonConfigDirPath + File.separator + BPMNConstants.ACTIVITI_CONFIGURATION_FILE_NAME;
		File configFile = new File(activitiConfigPath);
		String configContent = FileUtils.readFileToString(configFile);
		OMElement configElement = AXIOMUtil.stringToOM(configContent);
		Iterator beans =
				configElement.getChildrenWithName(new QName(BPMNConstants.SPRING_NAMESPACE, BPMNConstants.BEAN));
		while (beans.hasNext()) {
			OMElement bean = (OMElement) beans.next();
			String beanId = bean.getAttributeValue(new QName(null, BPMNConstants.BEAN_ID));
			if (AnalyticsPublisherConstants.BEAN_ID_VALUE.equals(beanId)) {
				Iterator beanProps =
						bean.getChildrenWithName(new QName(BPMNConstants.SPRING_NAMESPACE, BPMNConstants.PROPERTY));
				while (beanProps.hasNext()) {
					OMElement beanProp = (OMElement) beanProps.next();
					if (AnalyticsPublisherConstants.ACTIVATE.
							                                        equals(beanProp.getAttributeValue(
									                                        new QName(null, BPMNConstants.NAME)))) {
						String value = beanProp.getAttributeValue(new QName(null, BPMNConstants.VALUE));
						if (AnalyticsPublisherConstants.TRUE.equals(value)) {
							return true;
						}
					}
				}
			}
		}
		return false;
	}

	/**
	 * Initialize BPMNDataReceiverConfig instance.
	 *
	 * @return true if success.
	 */
	public boolean init() {
		try {
			// Get tenant specific configuration registry.
			this.registry = BPMNAnalyticsHolder.getInstance().getRegistryService().getConfigSystemRegistry(tenantID);
			// Populating registry resource with default values if not exist.
			if (registry.resourceExists(AnalyticsPublisherConstants.PATH_PUBLISHER_CONFIGURATION)) {
				if (log.isDebugEnabled()) {
					log.debug("Registry resource exists for tenant : " + tenantID + ", Path : " +
					          AnalyticsPublisherConstants.PATH_PUBLISHER_CONFIGURATION);
				}
				Resource resource = registry.get(AnalyticsPublisherConstants.PATH_PUBLISHER_CONFIGURATION);
				populateDefaultConfigurationProperties(resource);
			} else {
				if (log.isDebugEnabled()) {
					log.debug("Registry resource doesn't exist for tenant : " + tenantID + ", Path : " +
					          AnalyticsPublisherConstants.PATH_PUBLISHER_CONFIGURATION);
				}
				Resource resource = registry.newResource();
				populateDefaultConfigurationProperties(resource);
				registry.put(AnalyticsPublisherConstants.PATH_PUBLISHER_CONFIGURATION, resource);
			}
		} catch (RegistryException e) {
			log.warn("Error while accessing registry for tenant id : " + tenantID, e);
			registry = null;
			return false;
		}
		return true;
	}

	/**
	 * Populate default data publisher Configuration Properties if not exists for given registry resource.
	 *
	 * @param resource given registry resources
	 */
	private void populateDefaultConfigurationProperties(Resource resource) {
		if (log.isDebugEnabled()) {
			log.debug("Populating Data publisher configuration. Tenant ID : " + tenantID + ", Registry :" +
			          resource.getPath());
		}

		Properties properties = resource.getProperties();
		if (!properties.contains(AnalyticsPublisherConstants.PUBLISHER_ENABLED_PROPERTY))
			resource.addProperty(AnalyticsPublisherConstants.PUBLISHER_ENABLED_PROPERTY, String.valueOf(false));

		if (!properties.contains(AnalyticsPublisherConstants.PUBLISHER_TYPE_PROPERTY))
			resource.addProperty(AnalyticsPublisherConstants.PUBLISHER_TYPE_PROPERTY, "");

		if (!properties.contains(AnalyticsPublisherConstants.PUBLISHER_RECEIVER_URL_SET_PROPERTY))
			resource.addProperty(AnalyticsPublisherConstants.PUBLISHER_RECEIVER_URL_SET_PROPERTY,
			                     "tcp://localhost:7611");

		if (!properties.contains(AnalyticsPublisherConstants.PUBLISHER_AUTH_URL_SET_PROPERTY))
			resource.addProperty(AnalyticsPublisherConstants.PUBLISHER_AUTH_URL_SET_PROPERTY, "");

		if (!properties.contains(AnalyticsPublisherConstants.PUBLISHER_USER_NAME_PROPERTY))
			resource.addProperty(AnalyticsPublisherConstants.PUBLISHER_USER_NAME_PROPERTY, "admin");

		if (!properties.contains(AnalyticsPublisherConstants.PUBLISHER_PASSWORD_PROPERTY))
			resource.addProperty(AnalyticsPublisherConstants.PUBLISHER_PASSWORD_PROPERTY, "configure me");

		String documentation =
				"Configure following registry properties in this registry resource to enable BPMN analytics publisher\n\n" +
				AnalyticsPublisherConstants.PUBLISHER_ENABLED_PROPERTY +
				"\t: set this value to true/false to enable/disable data publisher.\n" +
				AnalyticsPublisherConstants.PUBLISHER_TYPE_PROPERTY +
				"\t: The Agent name from which the DataPublisher that needs to be created." +
				" By default Thrift, and Binary is supported. Leave empty for default.\n " +
				AnalyticsPublisherConstants.PUBLISHER_RECEIVER_URL_SET_PROPERTY +
				"\t : The receiving endpoint URL Set. This can be either load balancing URL set or Failover URL set." +
				" Eg : tcp://localhost:7611|tcp://localhost:7612|tcp://localhost:7613\n" +
				AnalyticsPublisherConstants.PUBLISHER_AUTH_URL_SET_PROPERTY +
				"\t: The authenticating URL Set for the endpoints given in receiverURLSet parameter. This should be in the same format" +
				" as receiverURL set parameter. Leave it empty fro default value." +
				AnalyticsPublisherConstants.PUBLISHER_USER_NAME_PROPERTY +
				"\t: Authorized username at receiver. (For Tenant include tenant domain)" +
				AnalyticsPublisherConstants.PUBLISHER_PASSWORD_PROPERTY +
				"\t: The encrypted Password of the username provided.";

		try {
			resource.setContent(documentation);
		} catch (RegistryException e) {
			log.error("Error while adding content to registry resource : " + resource.getPath(), e);
		}
	}

	/**
	 * Get registry resource which contains data publisher configurations.
	 *
	 * @return registry resource which contains data publisher configurations
	 */
	public Resource getRegistryResourceConfig() {
		try {
			if (registry == null) {
				log.info("BPMNDataReceiverConfig is not initialized properly. Initializing it now for tenant :" +
				         this.tenantID);
				init();
			}
			if (registry.resourceExists(AnalyticsPublisherConstants.PATH_PUBLISHER_CONFIGURATION)) {
				return registry.get(AnalyticsPublisherConstants.PATH_PUBLISHER_CONFIGURATION);
			} else {
				log.warn("Registry resource is not initialized properly tenant :" + this.tenantID);
			}
		} catch (RegistryException e) {
			log.error("Error while accessing ");
		}
		return null;
	}

	/**
	 * Is Data Publisher Enabled
	 *
	 * @return true if AnalyticsPublisherConstants.PUBLISHER_ENABLED_PROPERTY property is set to true
	 */
	public boolean isDataPublisherEnabled() {
		Resource resource = getRegistryResourceConfig();
		if (resource != null) {
			String property = resource.getProperty(AnalyticsPublisherConstants.PUBLISHER_ENABLED_PROPERTY);
			if (property != null && property.trim().length() != 0) {
				if (property.trim().toLowerCase().equals("true")) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * The Agent name from which the DataPublisher that needs to be created.
	 *
	 * @return configured agent name.
	 */
	public String getType() {
		Resource resource = getRegistryResourceConfig();
		if (resource != null) {
			String property = resource.getProperty(AnalyticsPublisherConstants.PUBLISHER_TYPE_PROPERTY);
			if (property != null && property.trim().length() != 0) {
				if (property.trim().toLowerCase().equals("default")) {
					return null;
				}
				return property;
			}
		}
		return null;
	}

	/**
	 * The receiving endpoint URL Set.
	 *
	 * @return configured The receiving endpoint URL Set.
	 */
	public String getReceiverURLsSet() {
		Resource resource = getRegistryResourceConfig();
		if (resource != null) {
			String property = resource.getProperty(AnalyticsPublisherConstants.PUBLISHER_RECEIVER_URL_SET_PROPERTY);
			if (property != null && property.trim().length() != 0) {
				return property;
			}
		}
		return null;
	}

	/**
	 * The authenticating URL Set for the endpoints
	 * @return
	 */
	public String getAuthURLsSet() {
		Resource resource = getRegistryResourceConfig();
		if (resource != null) {
			String property = resource.getProperty(AnalyticsPublisherConstants.PUBLISHER_AUTH_URL_SET_PROPERTY);
			if (property != null && property.trim().length() != 0) {
				return property;
			}
		}
		return null;
	}

	/**
	 * Get Authorized username at receiver.
	 *
	 * @return configured username
	 */
	public String getUserName() {
		Resource resource = getRegistryResourceConfig();
		if (resource != null) {
			return resource.getProperty(AnalyticsPublisherConstants.PUBLISHER_USER_NAME_PROPERTY);
		}
		return null;
	}

	/**
	 * Get password of Authorized username at receiver
	 *
	 * @return decrypted password
	 */
	public String getPassword() {
		String password = null;
		Resource resource = getRegistryResourceConfig();
		if (resource != null) {

			String encryptedPassword = resource.getProperty(AnalyticsPublisherConstants.PUBLISHER_PASSWORD_PROPERTY);
			try {
				byte[] decryptedPassword = CryptoUtil.getDefaultCryptoUtil(BPMNAnalyticsHolder.getInstance().
						getServerConfigurationService(), BPMNAnalyticsHolder.getInstance().getRegistryService()).
						                                     base64DecodeAndDecrypt(encryptedPassword);
				password = new String(decryptedPassword);
			} catch (CryptoException e) {
				String errMsg = "CryptoUtils Error while reading the password from the carbon registry.";
				log.error(errMsg, e);
			}
		}
		return password;
	}

	/**
	 * Get Tenant ID.
	 * @return
	 */
	public int getTenantID() {
		return tenantID;
	}
}
