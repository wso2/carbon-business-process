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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.base.api.ServerConfigurationService;
import org.wso2.carbon.bpmn.analytics.publisher.AnalyticsPublisher;
import org.wso2.carbon.bpmn.analytics.publisher.BPSDataPublisher;
import org.wso2.carbon.bpmn.core.BPMNEngineService;
import org.wso2.carbon.bpmn.core.BPMNServerHolder;
import org.wso2.carbon.databridge.agent.DataPublisher;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.core.service.RealmService;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;

/**
 * Content holder for BPMN Analytics component.
 */
public class BPMNAnalyticsHolder {

	private static final Log log = LogFactory.getLog(BPMNAnalyticsHolder.class);

	private static BPMNAnalyticsHolder bpmnAnalyticsHolder = null;

	private RegistryService registryService;
	private RealmService realmService;
	private ServerConfigurationService serverConfigurationService;
	private ExecutorService executorService = null;

	private BPSDataPublisher bpsDataPublisher;
	private Map<Integer, AnalyticsPublisher> tenantAnalyticsPublisherMap;
	private Map<Integer, DataPublisher> tenantDataPublisherMap;
	private BPMNEngineService bpmnEngineService;

	private BPMNAnalyticsHolder() {
		tenantAnalyticsPublisherMap = new HashMap<>();
		tenantDataPublisherMap = new HashMap<>();
	}

	/**
	 * Get the BPMNAnalyticsHolder instance.
	 *
	 * @return BPMNAnalyticsHolder instance
	 */
	public static BPMNAnalyticsHolder getInstance() {
		if (bpmnAnalyticsHolder == null) {
			bpmnAnalyticsHolder = new BPMNAnalyticsHolder();
		}
		return bpmnAnalyticsHolder;
	}

	public BPMNEngineService getBpmnEngineService() {
		return bpmnEngineService;
	}

	public void setBpmnEngineService(BPMNEngineService bpmnEngineService) {
		this.bpmnEngineService = bpmnEngineService;
	}

	public static BPMNServerHolder getThreadSafeBPMNServerInstance() {
		return BPMNServerInstanceHolder.INSTANCE;
	}

	/**
	 * Get RegistryService instance.
	 *
	 * @return RegistryService instance
	 */
	public RegistryService getRegistryService() {
		return registryService;
	}

	public void setRegistryService(RegistryService registryService) {
		this.registryService = registryService;
	}

	/**
	 * Get RealmService instance.
	 *
	 * @return RealmService instance
	 */
	public RealmService getRealmService() {
		return realmService;
	}

	public void setRealmService(RealmService realmService) {
		this.realmService = realmService;
	}

	/**
	 * Get ServerConfigurationService instance.
	 *
	 * @return ServerConfigurationService instance
	 */
	public ServerConfigurationService getServerConfigurationService() {
		return serverConfigurationService;
	}

	/**
	 * Set ServerConfigurationService instance.
	 *
	 * @param serverConfiguration instance for BPMN analytics component
	 */
	public void setServerConfiguration(ServerConfigurationService serverConfiguration) {
		this.serverConfigurationService = serverConfiguration;
	}

	/**
	 * Get ExecutorService instance of BPMN analytics component.
	 *
	 * @return ExecutorService instance
	 */
	public ExecutorService getExecutorService() {
		return executorService;
	}

	/**
	 * Set ExecutorService instance for BPMN analytics component
	 *
	 * @param executorService instance
	 */
	public void setExecutorService(ExecutorService executorService) {
		this.executorService = executorService;
	}

	/**
	 * Add Analytics Publisher instance for given tenant ID.
	 *
	 * @param tenantID
	 * @param analyticsPublisher instance
	 */
	public void addAnalyticsPublisher(int tenantID, AnalyticsPublisher analyticsPublisher) {
		this.tenantAnalyticsPublisherMap.put(tenantID, analyticsPublisher);
	}

	/**
	 * Remove Analytics Publisher instance for tenant id.
	 *
	 * @param tenantID
	 */
	public void removeAnalyticsPublisher(int tenantID) {
		AnalyticsPublisher analyticsPublisher = tenantAnalyticsPublisherMap.get(tenantID);
		analyticsPublisher.stopDataPublisher();
		this.tenantAnalyticsPublisherMap.remove(tenantID);
	}

	/**
	 * Get Analytics Publisher instance for given tenant id.
	 *
	 * @param tenantID
	 * @return AnalyticsPublisher instance
	 */
	public AnalyticsPublisher getAnalyticsPublisher(int tenantID) {
		return this.tenantAnalyticsPublisherMap.get(tenantID);
	}

	public Map<Integer, AnalyticsPublisher> getAllPublishers() {
		return this.tenantAnalyticsPublisherMap;
	}

	public BPSDataPublisher getBpsDataPublisher() {
		return bpsDataPublisher;
	}

	public void setBpsDataPublisher(BPSDataPublisher bpsDataPublisher) {
		this.bpsDataPublisher = bpsDataPublisher;
	}

	public DataPublisher getDataPublisher(int tenantId) {
		return tenantDataPublisherMap.get(tenantId);
	}

	public void setDataPublisher(int tenantId, DataPublisher dataPublisher) {
		tenantDataPublisherMap.put(tenantId, dataPublisher);
	}

	public void removeDataPublisher(int tenantId) {
		tenantDataPublisherMap.remove(tenantId);
	}

	private static class BPMNServerInstanceHolder {
		private static final BPMNServerHolder INSTANCE = BPMNServerHolder.getInstance();
	}
}
