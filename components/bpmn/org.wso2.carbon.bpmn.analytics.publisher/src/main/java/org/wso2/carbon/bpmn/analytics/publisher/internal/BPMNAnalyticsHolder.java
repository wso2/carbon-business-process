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
package org.wso2.carbon.bpmn.analytics.publisher.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.registry.core.service.RegistryService;

/**
 *
 */
public class BPMNAnalyticsHolder {
	private static Log log = LogFactory.getLog(BPMNAnalyticsHolder.class);

	private static BPMNAnalyticsHolder bpmnAnalyticsHolder = null;

	private RegistryService registryService;

	public static BPMNAnalyticsHolder getInstance(){
		if(bpmnAnalyticsHolder == null){
			bpmnAnalyticsHolder = new BPMNAnalyticsHolder();
		}
		return bpmnAnalyticsHolder;
	}

	public RegistryService getRegistryService() {
		return registryService;
	}

	public void setRegistryService(RegistryService registryService) {
		this.registryService = registryService;
	}
}
