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

import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.base.api.ServerConfigurationService;
import org.wso2.carbon.bpmn.analytics.publisher.*;
import org.wso2.carbon.bpmn.analytics.publisher.utils.BPMNDataReceiverConfig;
import org.wso2.carbon.bpmn.core.BPMNEngineService;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.databridge.agent.DataPublisher;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.Axis2ConfigurationContextObserver;

/**
 * @scr.component name="org.wso2.carbon.bpmn.analytics.publisher.internal.BPMNAnalyticsServiceComponent" immediate="true"
 * @scr.reference name="realm.service" interface="org.wso2.carbon.user.core.service.RealmService"
 * cardinality="1..1" policy="dynamic" bind="setRealmService" unbind="unsetRealmService"
 * @scr.reference name="registry.service" interface="org.wso2.carbon.registry.core.service.RegistryService"
 * cardinality="1..1" policy="dynamic"  bind="setRegistryService" unbind="unsetRegistryService"
 * @scr.reference name="server.configuration" interface="org.wso2.carbon.base.api.ServerConfigurationService"
 * cardinality="1..1" policy="dynamic" bind="setServerConfiguration" unbind="unsetServerConfiguration"
 * @scr.reference name="bpmn.service" interface="org.wso2.carbon.bpmn.core.BPMNEngineService"
 * cardinality="1..1" policy="dynamic" bind="setBPMNEngineService" unbind="unsetBPMNEngineService"
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
			BPSDataPublisher bpsDataPublisher = new BPSDataPublisher();
			BPMNAnalyticsHolder.getInstance().setBpsDataPublisher(bpsDataPublisher);
			bpsDataPublisher.configure();

			// data publishers for other tenants will be created upon tenant loading and removed upon unloading tenents
			ctxt.getBundleContext().registerService(
					Axis2ConfigurationContextObserver.class.getName(),	new BPSAnalyticsConfigContextObserver(), null);

//			ConfigurationContext cxt = new ConfigurationContext(new AxisConfiguration());
//			BPMNAnalyticsAxis2ConfigurationContextObserverImpl configCtx =
//					new BPMNAnalyticsAxis2ConfigurationContextObserverImpl();
//			configCtx.createdConfigurationContext(cxt);
//
//			ctxt.getBundleContext()
//			    .registerService(WaitBeforeShutdownObserver.class, new AnalyticsSchedulerShutdown(),
//			                     null);
		} catch (Throwable e) {
			log.error("Failed to initialize the Analytics Service component.", e);
		}
	}

	public void setBPMNEngineService(BPMNEngineService bpmnEngineService) {
		BPMNAnalyticsHolder.getInstance().setBpmnEngineService(bpmnEngineService);
	}

	public void unsetBPMNEngineService(BPMNEngineService bpmnEngineService) {
		BPMNAnalyticsHolder.getInstance().setBpmnEngineService(null);
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
	 *
	 * @param serverConfiguration
	 */
	public void setServerConfiguration(ServerConfigurationService serverConfiguration) {
		BPMNAnalyticsHolder.getInstance().setServerConfiguration(serverConfiguration);
	}

	/**
	 * Unset ServerConfigurationService instance when bundle get unbind from OSGI runtime.
	 *
	 * @param serverConfiguration
	 */
	public void unsetServerConfiguration(ServerConfigurationService serverConfiguration) {
		BPMNAnalyticsHolder.getInstance().setServerConfiguration(null);
	}
}
