/*
 * Copyright (c) 2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.humantask.core.internal;

import org.wso2.carbon.attachment.mgt.server.AttachmentServerService;
import org.wso2.carbon.humantask.core.HumanTaskServer;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.core.service.RealmService;

/**
 * Data Holder for the HumanTaskServiceComponent
 */
public final class HumanTaskServerHolder {
    private static HumanTaskServerHolder instance = new HumanTaskServerHolder();

    private RealmService realmService = null;
    private boolean dataSourceInfoRepoProvided = false;
    private HumanTaskServer htServer = null;
    private RegistryService registryService;
    private AttachmentServerService attachmentService;

    /**
     * The HumanTask UI Resource Provider
     */
    private HumanTaskUIResourceProvider humanTaskUIResourceProvider;

    private HumanTaskServerHolder() {
    }

    public static HumanTaskServerHolder getInstance() {
        return instance;
    }

    public RealmService getRealmService() {
        return realmService;
    }

    public void setRealmService(RealmService realmService) {
        this.realmService = realmService;
    }

    public boolean isDataSourceInfoRepoProvided() {
        return dataSourceInfoRepoProvided;
    }

    public void setDataSourceInfoRepoProvided(boolean dataSourceInfoRepoProvided) {
        this.dataSourceInfoRepoProvided = dataSourceInfoRepoProvided;
    }

    public HumanTaskServer getHtServer() {
        return htServer;
    }

    public void setHtServer(HumanTaskServer htServer) {
        this.htServer = htServer;
    }

    public RegistryService getRegistryService() {
        return registryService;
    }

    public void setRegistryService(RegistryService registryService) {
        this.registryService = registryService;
    }

    public HumanTaskUIResourceProvider getHumanTaskUIResourceProvider() {
        return humanTaskUIResourceProvider;
    }

    public void setHumanTaskUIResourceProvider(HumanTaskUIResourceProvider humanTaskUIResourceProvider) {
        this.humanTaskUIResourceProvider = humanTaskUIResourceProvider;
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
}
