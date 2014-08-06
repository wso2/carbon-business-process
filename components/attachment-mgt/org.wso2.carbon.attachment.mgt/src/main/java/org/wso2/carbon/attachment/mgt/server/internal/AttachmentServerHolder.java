/*
 * Copyright (c) 2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.attachment.mgt.server.internal;

import org.wso2.carbon.attachment.mgt.server.Server;
import org.wso2.carbon.utils.ConfigurationContextService;

/**
 * [Singleton Class] Act as the main resource holder for Attachment Server
 */
public final class AttachmentServerHolder {
    private static AttachmentServerHolder instance = new AttachmentServerHolder();

    /**
     * Reference to Attachment-Mgt object.
     * So using this reference, clients will be able to gather details related to configured
     * data-sources etc.
     */
    private Server attachmentServer;

    /** Reference to ConfigurationContextService
     *
     */

    private ConfigurationContextService configurationContextService;

    /**
     * Private constructor disables new objects creation
     */
    private AttachmentServerHolder() {
    }

    /**
     * Returns the singleton object reference for the AttachmentServerHolder
     *
     * @return singleton object reference for the AttachmentServerHolder
     */
    public static AttachmentServerHolder getInstance() {
        return instance;
    }

    /**
     * returns a reference to {@link Server} object.
     *
     * @return
     */
    public Server getAttachmentServer() {
        if (attachmentServer != null) {
            return attachmentServer;
        } else  {
            throw new NullPointerException("AttachmentServer is still not initialized.");
        }
    }

    /**
     * set the reference to {@link Server} object.
     *
     * @param attachmentServer
     */
    public void setAttachmentServer(Server attachmentServer) {
        this.attachmentServer = attachmentServer;
    }

    public void setConfigurationContextService(ConfigurationContextService service) {
        this.configurationContextService = service;
    }

    public ConfigurationContextService getConfigurationContextService() {
        return this.configurationContextService;
    }
}
