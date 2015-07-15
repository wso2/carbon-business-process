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
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.bpmn.analytics.publisher.AnalyticsPublisher;
import org.wso2.carbon.registry.core.service.RegistryService;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @scr.component name="org.wso2.carbon.bpmn.analytics.publisher.internal.BPMNAnalyticsServiceComponent" immediate="true"
 * @scr.reference name="registry.service" interface="org.wso2.carbon.registry.core.service.RegistryService"
 * cardinality="1..1" policy="dynamic"  bind="setRegistryService" unbind="unsetRegistryService"
 */
public class BPMNAnalyticsServiceComponent {
	private static Log log = LogFactory.getLog(BPMNAnalyticsServiceComponent.class);

	protected void activate(ComponentContext ctxt) {
		log.info("Initializing the BPMN Analytics Service component...");
		try {
			AnalyticsPublisher analyticsPublisher = new AnalyticsPublisher();
			analyticsPublisher.initialize();
		} catch (Throwable e) {
			log.error("Failed to initialize the Analytics Service component.", e);
		}
	}

	public void setRegistryService(RegistryService registryService) {
		BPMNAnalyticsHolder.getInstance().setRegistryService(registryService);
	}

	public void unsetRegistryService(RegistryService registryService) {
		BPMNAnalyticsHolder.getInstance().setRegistryService(null);
	}
}
