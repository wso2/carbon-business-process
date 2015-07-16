/*
 * Copyright (c) , WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.bpmn.analytics.publisher.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bpmn.analytics.publisher.AnalyticsPublisherConstants;
import org.wso2.carbon.bpmn.analytics.publisher.internal.BPMNAnalyticsHolder;
import org.wso2.carbon.registry.api.Registry;
import org.wso2.carbon.registry.api.RegistryException;
import org.wso2.carbon.registry.api.Resource;
import org.wso2.carbon.user.api.UserStoreException;

/**
 * BPMNDataReceiverConfig is used by AnalyticsPublisher to retrieve user name and password
 */
public class BPMNDataReceiverConfig {
	private static Log log = LogFactory.getLog(BPMNDataReceiverConfig.class);

	/**
	 * Get thrift url of data receiver
	 *
	 * @return thrift url of data receiver
	 * @throws RegistryException
	 */
	public static String getThriftURL() throws RegistryException {
		String url;
		Registry registry =
				BPMNAnalyticsHolder.getInstance().getRegistryService().getConfigSystemRegistry();

		if (registry.resourceExists(AnalyticsPublisherConstants.DATA_RECEIVER_RESOURCE_PATH)) {
			Resource resource =
					registry.get(AnalyticsPublisherConstants.DATA_RECEIVER_RESOURCE_PATH);
			url = resource.getProperty(AnalyticsPublisherConstants.THRIFT_URL_PROPERTY);
		}else{
			url = AnalyticsPublisherConstants.LOCAL_THRIFT_URL;
		}

		return url;
	}

	/**
	 * Get user name
	 *
	 * @return user name
	 * @throws RegistryException
	 * @throws UserStoreException
	 */
	public static String getUserName() throws RegistryException, UserStoreException {
		String userName;
		Registry registry =
				BPMNAnalyticsHolder.getInstance().getRegistryService().getConfigSystemRegistry();

		if (registry.resourceExists(AnalyticsPublisherConstants.DATA_RECEIVER_RESOURCE_PATH)) {
			Resource resource =
					registry.get(AnalyticsPublisherConstants.DATA_RECEIVER_RESOURCE_PATH);
			userName = resource.getProperty(AnalyticsPublisherConstants.USER_NAME_PROPERTY);
		}else{
			userName = AnalyticsPublisherConstants.USER_NAME;
		}
		return userName;
	}

	/**
	 * Get password
	 *
	 * @return password
	 * @throws RegistryException
	 * @throws UserStoreException
	 */
	public static String getPassword() throws RegistryException, UserStoreException {
		String password;
		Registry registry =
				BPMNAnalyticsHolder.getInstance().getRegistryService().getConfigSystemRegistry();

		if (registry.resourceExists(AnalyticsPublisherConstants.DATA_RECEIVER_RESOURCE_PATH)) {
			Resource resource =
					registry.get(AnalyticsPublisherConstants.DATA_RECEIVER_RESOURCE_PATH);
			password = resource.getProperty(AnalyticsPublisherConstants.PASSWORD_PROPERTY);
		}else{
			password = AnalyticsPublisherConstants.PASSWORD;
		}
		return password;
	}

}
