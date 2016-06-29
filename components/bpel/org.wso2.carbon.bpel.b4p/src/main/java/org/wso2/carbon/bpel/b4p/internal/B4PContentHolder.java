/*
 * Copyright (c) 2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.bpel.b4p.internal;

import org.wso2.carbon.bpel.b4p.coordination.CoordinationController;
import org.wso2.carbon.bpel.core.ode.integration.BPELServer;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.core.service.RealmService;

/**
 * Data holder for the B4PServiceComponent
 */
public final class B4PContentHolder {

    private static B4PContentHolder instance;
    private static RealmService realmService;
    private BPELServer bpelServer;
    private CoordinationController coordinationController;
    private RegistryService registryService;

    private B4PContentHolder() {
    }

    public static B4PContentHolder getInstance() {
        if (instance == null) {
            instance = new B4PContentHolder();
        }
        return instance;
    }

    public BPELServer getBpelServer() {
        return bpelServer;
    }

    public void setBpelServer(BPELServer bpelServer) {
        this.bpelServer = bpelServer;
    }

    public RealmService getRealmService() {
        return this.realmService;
    }

    public void setRealmService(RealmService realm) {
        this.realmService = realm;
    }

    public CoordinationController getCoordinationController() {
        return coordinationController;
    }

    public void setCoordinationController(CoordinationController coordinationController) {
        this.coordinationController = coordinationController;
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
}
