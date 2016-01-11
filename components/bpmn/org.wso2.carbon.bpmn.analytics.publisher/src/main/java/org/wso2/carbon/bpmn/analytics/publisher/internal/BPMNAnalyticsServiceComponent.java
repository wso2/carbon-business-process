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
package org.wso2.carbon.bpmn.analytics.publisher.internal;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.base.api.ServerConfigurationService;
import org.wso2.carbon.bpmn.analytics.publisher.AnalyticsPublisher;
import org.wso2.carbon.bpmn.analytics.publisher.BPMNAnalyticsAxis2ConfigurationContextObserverImpl;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.core.service.RealmService;

/**
 * @scr.component name="org.wso2.carbon.bpmn.analytics.publisher.internal.BPMNAnalyticsServiceComponent" immediate="true"
 * @scr.reference name="realm.service" interface="org.wso2.carbon.user.core.service.RealmService"
 * cardinality="1..1" policy="dynamic" bind="setRealmService" unbind="unsetRealmService"
 * @scr.reference name="registry.service" interface="org.wso2.carbon.registry.core.service.RegistryService"
 * cardinality="1..1" policy="dynamic"  bind="setRegistryService" unbind="unsetRegistryService"
 * @scr.reference name="server.configuration" interface="org.wso2.carbon.base.api.ServerConfigurationService"
 * cardinality="1..1" policy="dynamic" bind="setServerConfiguration" unbind="unsetServerConfiguration"
 */
public class BPMNAnalyticsServiceComponent {
    private static final Log log = LogFactory.getLog(BPMNAnalyticsServiceComponent.class);

	/**
	 * Activate BPMN analytics component.
	 *
	 * @param ctxt ComponentContext
	 */
    protected void activate(ComponentContext ctxt) {
        log.info("Initializing the BPMN Analytics Service component...");
        try {
//            AnalyticsPublisher analyticsPublisher = new AnalyticsPublisher();
//            analyticsPublisher.initialize();
            ConfigurationContext cxt = new ConfigurationContext(new AxisConfiguration());
            BPMNAnalyticsAxis2ConfigurationContextObserverImpl configCtx = new BPMNAnalyticsAxis2ConfigurationContextObserverImpl();
            configCtx.createdConfigurationContext(cxt);
        } catch (Throwable e) {
            log.error("Failed to initialize the Analytics Service component.", e);
        }
    }

	/**
	 * Set RegistryService instance when bundle get bind to OSGI runtime.
	 *
	 * @param registryService
	 */
    public void setRegistryService(RegistryService registryService) {
        BPMNAnalyticsHolder.getInstance().setRegistryService(registryService);
    }

	/**
	 * Unset RegistryService instance when bundle get unbind from OSGI runtime.
	 *
	 * @param registryService
	 */
    public void unsetRegistryService(RegistryService registryService) {
        BPMNAnalyticsHolder.getInstance().setRegistryService(null);
    }

	/**
	 * Set RealmService instance when bundle get bind to OSGI runtime.
	 *
	 * @param realmService
	 */
    public void setRealmService(RealmService realmService) {
        BPMNAnalyticsHolder.getInstance().setRealmService(realmService);
    }

	/**
	 * Unset RealmService instance when bundle get unbind from OSGI runtime.
	 *
	 * @param realmService
	 */
    public void unsetRealmService(RealmService realmService) {
        BPMNAnalyticsHolder.getInstance().setRealmService(null);
    }

	/**
	 * Set ServerConfigurationService instance when bundle get bind to OSGI runtime.
	 * @param serverConfiguration
	 */
    public void setServerConfiguration(ServerConfigurationService serverConfiguration) {
        BPMNAnalyticsHolder.getInstance().setServerConfiguration(serverConfiguration);
    }

	/**
	 * Unset ServerConfigurationService instance when bundle get unbind from OSGI runtime.
	 * @param serverConfiguration
	 */
    public void unsetServerConfiguration(ServerConfigurationService serverConfiguration) {
        BPMNAnalyticsHolder.getInstance().setServerConfiguration(null);
    }
}
