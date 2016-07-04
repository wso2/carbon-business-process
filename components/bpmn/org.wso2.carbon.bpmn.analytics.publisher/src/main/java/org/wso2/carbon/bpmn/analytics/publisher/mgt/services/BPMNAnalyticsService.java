/*
 * Copyright 2005-2015 WSO2, Inc. (http://wso2.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.bpmn.analytics.publisher.mgt.services;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bpmn.core.BPMNConstants;
import org.wso2.carbon.bpmn.core.BPMNServerHolder;
import org.wso2.carbon.bpmn.core.BPSFault;
import org.wso2.carbon.bpmn.core.mgt.model.BPMNAnalyticsConfiguration;
import org.wso2.carbon.core.util.CryptoUtil;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.session.UserRegistry;

public class BPMNAnalyticsService {

    public static final Log log = LogFactory.getLog(BPMNAnalyticsService.class);

    public BPMNAnalyticsConfiguration getAnalyticsConfiguration() throws BPSFault {
        RegistryService registryService = BPMNServerHolder.getInstance().getRegistryService();
        try {
            BPMNAnalyticsConfiguration config = new BPMNAnalyticsConfiguration();
            UserRegistry registry = registryService.getConfigSystemRegistry();
            if (registry.resourceExists(BPMNConstants.DATA_PUBLISHER_CONFIG_PATH)) {
                Resource resource = registry.get(BPMNConstants.DATA_PUBLISHER_CONFIG_PATH);
                config.setPublisherEnabled(resource.getProperty("dataPublishingEnabled"));
                config.setPublisherType(resource.getProperty("type"));
                config.setThriftURL(resource.getProperty("receiverURLSet"));
                config.setAuthURL(resource.getProperty("authURLSet"));
                config.setUsername(resource.getProperty("username"));
                if (resource.getProperty("password") != null) {
                    byte[] decryptedPassword = CryptoUtil.getDefaultCryptoUtil().base64DecodeAndDecrypt(
                            resource.getProperty("password"));
                    config.setPassword(new String(decryptedPassword));
                }
            }
            return config;

        } catch (Exception e) {
            String msg = "Failed to get BPS analytics configuration.";
            log.error(msg, e);
            throw new BPSFault(msg, e);
        }
    }

    public void configureAnalytics(BPMNAnalyticsConfiguration config) throws BPSFault {

        RegistryService registryService = BPMNServerHolder.getInstance().getRegistryService();
        try {
            UserRegistry registry = registryService.getConfigSystemRegistry();
            Resource resource = registry.get(BPMNConstants.DATA_PUBLISHER_CONFIG_PATH);
            if (resource == null) {
                resource = registry.newResource();
            }
            resource.setProperty("dataPublishingEnabled", config.getPublisherEnabled());
            resource.setProperty("type", config.getPublisherType());
            resource.setProperty("receiverURLSet", config.getThriftURL());
            resource.setProperty("authURLSet", config.getAuthURL());
            resource.setProperty("username", config.getUsername());
            String encryptedPassword =
                    CryptoUtil.getDefaultCryptoUtil().encryptAndBase64Encode(config.getPassword().getBytes());
            resource.setProperty("password", encryptedPassword);
            registry.put(BPMNConstants.DATA_PUBLISHER_CONFIG_PATH, resource);

        } catch (Exception e) {
            String msg = "Failed to update BPMN analytics configuration.";
            log.error(msg, e);
            throw new BPSFault(msg, e);
        }
    }
}
