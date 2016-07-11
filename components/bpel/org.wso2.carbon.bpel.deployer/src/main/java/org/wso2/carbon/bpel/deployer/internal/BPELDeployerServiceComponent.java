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
package org.wso2.carbon.bpel.deployer.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.bpel.core.BPELEngineService;
import org.wso2.carbon.bpel.core.ode.integration.BPELServer;
import org.wso2.carbon.registry.core.service.TenantRegistryLoader;

/**
 * @scr.component name="org.wso2.carbon.bpel.BPELDeployerServiceComponent" immediate="true"
 * @scr.reference name="bpel.engine"
 * interface="org.wso2.carbon.bpel.core.BPELEngineService"
 * cardinality="1..1" policy="dynamic" bind="setBPELServer" unbind="unsetBPELServer"
 * @scr.reference name="tenant.registryloader"
 * interface="org.wso2.carbon.registry.core.service.TenantRegistryLoader"
 * cardinality="1..1" policy="dynamic" bind="setTenantRegistryLoader"
 * unbind="unsetTenantRegistryLoader"
 */

public class BPELDeployerServiceComponent {
    private static Log log = LogFactory.getLog(BPELDeployerServiceComponent.class);

    protected void activate(ComponentContext ctxt) {
        if (log.isDebugEnabled()) {
            log.debug("BPEL Deployer bundle is activated.");
        }
    }

    protected void setBPELServer(BPELEngineService bpelEngineService) {
        if (log.isDebugEnabled()) {
            log.debug("DataSourceInformationRepositoryService bound to the BPEL component");
        }
        BPELDeployerContentHolder.getInstance().setBPELServer(bpelEngineService);
    }

    protected void unsetBPELServer(
            BPELEngineService bpelEngineService) {
        if (log.isDebugEnabled()) {
            log.debug("DataSourceInformationRepositoryService unbound from the BPEL component");
        }
        BPELDeployerContentHolder.getInstance().setBPELServer(null);
    }

    protected void setTenantRegistryLoader(TenantRegistryLoader tenantRegLoader) {
        BPELDeployerContentHolder.getInstance().setRegistryLoader(tenantRegLoader);
    }

    protected void unsetTenantRegistryLoader(TenantRegistryLoader tenantRegLoader) {
        BPELDeployerContentHolder.getInstance().setRegistryLoader(null);
    }

    public static BPELServer getBPELServer() {
        return BPELDeployerContentHolder.getInstance().getBPELServer();
    }

    public static TenantRegistryLoader getTenantRegistryLoader() {
        return BPELDeployerContentHolder.getInstance().getRegistryLoader();
    }
}
