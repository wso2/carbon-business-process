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
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.bpel.core.BPELEngineService;
import org.wso2.carbon.bpel.core.ode.integration.BPELServer;
import org.wso2.carbon.registry.core.service.TenantRegistryLoader;

@Component(
        name = "org.wso2.carbon.bpel.BPELDeployerServiceComponent",
        immediate = true)
public class BPELDeployerServiceComponent {

    private static Log log = LogFactory.getLog(BPELDeployerServiceComponent.class);

    @Activate
    protected void activate(ComponentContext ctxt) {

        if (log.isDebugEnabled()) {
            log.debug("BPEL Deployer bundle is activated.");
        }
    }

    @Reference(
            name = "bpel.engine",
            service = org.wso2.carbon.bpel.core.BPELEngineService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetBPELServer")
    protected void setBPELServer(BPELEngineService bpelEngineService) {

        if (log.isDebugEnabled()) {
            log.debug("DataSourceInformationRepositoryService bound to the BPEL component");
        }
        BPELDeployerContentHolder.getInstance().setBPELServer(bpelEngineService);
    }

    protected void unsetBPELServer(BPELEngineService bpelEngineService) {

        if (log.isDebugEnabled()) {
            log.debug("DataSourceInformationRepositoryService unbound from the BPEL component");
        }
        BPELDeployerContentHolder.getInstance().setBPELServer(null);
    }

    @Reference(
            name = "tenant.registryloader",
            service = org.wso2.carbon.registry.core.service.TenantRegistryLoader.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetTenantRegistryLoader")
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
