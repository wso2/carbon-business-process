/**
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.bpmn.analytics.publisher.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.base.api.ServerConfigurationService;
import org.wso2.carbon.bpmn.core.BPMNServerHolder;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.core.service.RealmService;

import java.util.concurrent.ExecutorService;

/**
 *
 */
public class BPMNAnalyticsHolder {
	private static final Log log = LogFactory.getLog(BPMNAnalyticsHolder.class);

	private static BPMNAnalyticsHolder bpmnAnalyticsHolder = null;

	private RegistryService registryService;
	private RealmService realmService;
	private ServerConfigurationService serverConfigurationService;
	private ExecutorService executorService = null;

	private BPMNAnalyticsHolder(){
	}

	public static BPMNAnalyticsHolder getInstance(){
		if(bpmnAnalyticsHolder == null){
			bpmnAnalyticsHolder = new BPMNAnalyticsHolder();
		}
		return bpmnAnalyticsHolder;
	}

	public RegistryService getRegistryService() {
		return registryService;
	}

	public RealmService getRealmService(){
		return realmService;
	}

	public ServerConfigurationService getServerConfigurationService(){
		return serverConfigurationService;
	}

	public void setRegistryService(RegistryService registryService) {
		this.registryService = registryService;
	}

	public void setRealmService(RealmService realmService){
		this.realmService = realmService;
	}

	public void setServerConfiguration(ServerConfigurationService serverConfiguration){
		this.serverConfigurationService = serverConfiguration;
	}

	public void setExecutorService(ExecutorService executorService){
		this.executorService = executorService;
	}

	public ExecutorService getExecutorService() {
		return executorService;
	}

	private static class BPMNServerInstanceHolder{
		private static final BPMNServerHolder INSTANCE = BPMNServerHolder.getInstance();
	}

	public static BPMNServerHolder getThreadSafeBPMNServerInstance(){
		return BPMNServerInstanceHolder.INSTANCE;
	}
}
