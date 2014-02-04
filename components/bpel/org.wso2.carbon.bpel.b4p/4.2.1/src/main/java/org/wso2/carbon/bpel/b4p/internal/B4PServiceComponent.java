/*
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.bpel.b4p.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.bpel.b4p.coordination.B4PCoordinationException;
import org.wso2.carbon.bpel.b4p.coordination.CoordinationController;
import org.wso2.carbon.bpel.core.BPELEngineService;
import org.wso2.carbon.bpel.core.ode.integration.BPELServer;
import org.wso2.carbon.user.core.service.RealmService;

/**
 * @scr.component name="org.wso2.carbon.bpel.B4PServiceComponent" immediate="true"
 * @scr.reference name="bpel.engine"
 * interface="org.wso2.carbon.bpel.core.BPELEngineService"
 * cardinality="1..1" policy="dynamic" bind="setBPELServer" unbind="unsetBPELServer"
 * @scr.reference name="user.realmservice.default"
 * interface="org.wso2.carbon.user.core.service.RealmService"
 * cardinality="1..1" policy="dynamic" bind="setRealmService"
 * unbind="unsetRealmService"
 */

public class B4PServiceComponent {
    private static Log log = LogFactory.getLog(B4PServiceComponent.class);

    protected void activate(ComponentContext ctxt) {
        try {
            initHumanTaskCoordination();
            if(log.isDebugEnabled()) {
                log.debug("B4P bundle is activated.");
            }
        } catch (Throwable t) {
            log.error("Failed to activate the B4P component.", t);
        }
    }

    protected void setBPELServer(BPELEngineService bpelEngineService) {
        if (log.isDebugEnabled()) {
            log.debug("BPELEngineService bound to the B4P component");
        }
        B4PContentHolder.getInstance().setBpelServer(bpelEngineService.getBPELServer());
    }

    protected void unsetBPELServer(
            BPELEngineService bpelEngineService) {
        if (log.isDebugEnabled()) {
            log.debug("BPELEngineService unbound from the B4P component");
        }
        B4PContentHolder.getInstance().setBpelServer(null);
    }

    public static BPELServer getBPELServer() {
        return B4PContentHolder.getInstance().getBpelServer();
    }

    protected void setRealmService(RealmService realmService) {
        B4PContentHolder.getInstance().setRealmService(realmService);
    }

    protected void unsetRealmService(RealmService realmService) {
        B4PContentHolder.getInstance().setRealmService(null);
    }

    private void initHumanTaskCoordination() throws B4PCoordinationException {
        B4PContentHolder.getInstance().setCoordinationController(new CoordinationController());
        B4PContentHolder.getInstance().getCoordinationController().init();
    }
}
