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
import org.wso2.carbon.bpmn.analytics.publisher.BPMNDataPublisher;
import org.wso2.carbon.bpmn.analytics.publisher.BPSAnalyticsServer;
import org.wso2.carbon.bpmn.core.BPMNEngineService;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.core.service.RealmService;

/**
 * Content holder for BPMN Analytics component.
 */
public class BPMNAnalyticsHolder {

    private static final Log log = LogFactory.getLog(BPMNAnalyticsHolder.class);

    private static BPMNAnalyticsHolder bpmnAnalyticsHolder = null;

    private RegistryService registryService;
    private RealmService realmService;
    private BPMNDataPublisher bpmnDataPublisher;
    private BPMNEngineService bpmnEngineService;
    private boolean asyncDataPublishingEnabled;
    private BPSAnalyticsServer bpsAnalyticsServer;

    private BPMNAnalyticsHolder() {
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

    public RegistryService getRegistryService() {
        return registryService;
    }

    public void setRegistryService(RegistryService registryService) {
        this.registryService = registryService;
    }

    public RealmService getRealmService() {
        return realmService;
    }

    public void setRealmService(RealmService realmService) {
        this.realmService = realmService;
    }

    public BPMNDataPublisher getBpmnDataPublisher() {
        return bpmnDataPublisher;
    }

    public void setBpmnDataPublisher(BPMNDataPublisher bpmnDataPublisher) {
        this.bpmnDataPublisher = bpmnDataPublisher;
    }

    public boolean isAsyncDataPublishingEnabled() {
        return asyncDataPublishingEnabled;
    }

    public boolean getAsyncDataPublishingEnabled() {
        return asyncDataPublishingEnabled;
    }

    public void setAsyncDataPublishingEnabled(boolean asyncDataPublishingEnabled) {
        this.asyncDataPublishingEnabled = asyncDataPublishingEnabled;
    }

    public BPSAnalyticsServer getBPSAnalyticsServer() {
        return bpsAnalyticsServer;
    }

    public void setBPSAnalyticsServer(BPSAnalyticsServer bpsAnalyticsServer) {
        this.bpsAnalyticsServer = bpsAnalyticsServer;
    }
}
