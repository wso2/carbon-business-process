/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.carbon.bpel.core.internal;

import com.hazelcast.core.HazelcastInstance;
import org.wso2.carbon.attachment.mgt.server.AttachmentServerService;
import org.wso2.carbon.bpel.core.ode.integration.BPELServerImpl;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.service.TenantRegistryLoader;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.ConfigurationContextService;

import java.util.Observable;

/**
 * Data holder for the BPELServiceComponent
 */
public final class BPELServerHolder extends Observable {
    private static BPELServerHolder instance;

    private RegistryService registryService;

    private BPELServerImpl bpelServer;

    private TenantRegistryLoader registryLoader;

    private AttachmentServerService attachmentService;

    private RealmService realmService;

    private HazelcastInstance hazelcastInstance;

    private ConfigurationContextService configCtxService;

    private BPELServerHolder() {
    }

    public static BPELServerHolder getInstance() {
        if (instance == null) {
            instance = new BPELServerHolder();
        }
        return instance;
    }

    public RegistryService getRegistryService() {
        return registryService;
    }

    public void setRegistryService(RegistryService registryService) {
        this.registryService = registryService;
    }

    public BPELServerImpl getBpelServer() {
        return bpelServer;
    }

    public void setBpelServer(BPELServerImpl bpelServer) {
        this.bpelServer = bpelServer;
    }

    public TenantRegistryLoader getRegistryLoader() {
        return registryLoader;
    }

    public void setRegistryLoader(TenantRegistryLoader registryLoader) {
        this.registryLoader = registryLoader;
    }

    /**
     * Returns the Attachment-Mgt service dependency
     *
     * @return Attachment-Mgt service reference
     */
    public AttachmentServerService getAttachmentService() {
        return attachmentService;
    }

    /**
     * Initialize the Attachment-Mgt service dependency
     *
     * @param attachmentService Attachment-Mgt service reference
     */
    public void setAttachmentService(AttachmentServerService attachmentService) {
        this.attachmentService = attachmentService;
    }

    public void setRealmService(RealmService realm) {
        this.realmService = realm;
    }

    public RealmService getRealmService() {
        return this.realmService;
    }

    public void setHazelcastInstance(HazelcastInstance instance) {
        this.hazelcastInstance = instance;
        setChanged();
        notifyObservers();
    }

    public HazelcastInstance getHazelcastInstance() {
        return this.hazelcastInstance;
    }

    public void setConfigCtxService(ConfigurationContextService configCtxService) {
        this.configCtxService = configCtxService;
    }

    public ConfigurationContextService getConfigCtxService() {
        return this.configCtxService;
    }
}
