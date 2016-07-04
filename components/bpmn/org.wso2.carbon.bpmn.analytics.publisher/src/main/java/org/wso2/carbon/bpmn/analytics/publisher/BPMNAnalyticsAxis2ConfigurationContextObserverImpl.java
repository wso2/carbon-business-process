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
package org.wso2.carbon.bpmn.analytics.publisher;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bpmn.analytics.publisher.internal.BPMNAnalyticsHolder;
import org.wso2.carbon.bpmn.analytics.publisher.utils.BPMNDataReceiverConfig;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.utils.AbstractAxis2ConfigurationContextObserver;

public class BPMNAnalyticsAxis2ConfigurationContextObserverImpl extends AbstractAxis2ConfigurationContextObserver {

	private static Log log = LogFactory.getLog(BPMNAnalyticsAxis2ConfigurationContextObserverImpl.class);

	@Override
	public void createdConfigurationContext(ConfigurationContext configContext) {

		Integer tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();

		log.info("Loading analytics publisher for tenant " + tenantId + ".");
		BPMNDataReceiverConfig config = new BPMNDataReceiverConfig(tenantId);
		AnalyticsPublisher analyticsPublisher = new AnalyticsPublisher();
		try {
			analyticsPublisher.initialize(config);
		} catch (Exception e) {
			log.error("Analytics Publisher Initialization error", e);
		}
		BPMNAnalyticsHolder.getInstance().addAnalyticsPublisher(tenantId, analyticsPublisher);

	}

	@Override
	public void terminatingConfigurationContext(ConfigurationContext configCtx) {
		Integer tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
		log.info("Unloading analytics publisher for tenant " + tenantId + ".");
		BPMNAnalyticsHolder.getInstance().removeAnalyticsPublisher(tenantId);
	}
}
