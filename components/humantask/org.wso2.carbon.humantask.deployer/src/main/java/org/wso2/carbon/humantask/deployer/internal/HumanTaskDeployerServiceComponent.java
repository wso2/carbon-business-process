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
package org.wso2.carbon.humantask.deployer.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.humantask.core.HumanTaskEngineService;
import org.wso2.carbon.humantask.core.HumanTaskServer;
import org.wso2.carbon.registry.core.service.TenantRegistryLoader;

/**
 * @scr.component name="org.wso2.carbon.humantask.HumanTaskDeployerServiceComponent" immediate="true"
 * @scr.reference name="humantask.engine"
 * interface="org.wso2.carbon.humantask.core.HumanTaskEngineService"
 * cardinality="1..1" policy="dynamic" bind="setHumanTaskServer" unbind="unsetHumanTaskServer"
 * @scr.reference name="tenant.registryloader"
 * interface="org.wso2.carbon.registry.core.service.TenantRegistryLoader"
 * cardinality="1..1" policy="dynamic" bind="setTenantRegistryLoader"
 * unbind="unsetTenantRegistryLoader"
 */

public class HumanTaskDeployerServiceComponent {
    private static Log log = LogFactory.getLog(HumanTaskDeployerServiceComponent.class);

    protected void activate(ComponentContext ctxt) {
        if(log.isDebugEnabled()) {
            log.debug("HumanTask Deployer bundle is activated.");
        }
    }

    protected void setHumanTaskServer(HumanTaskEngineService humantaskEngineService) {
        if (log.isDebugEnabled()) {
            log.debug("DataSourceInformationRepositoryService bound to the HumanTask component");
        }
        HumanTaskDeployerContentHolder.getInstance().setHumanTaskServer(humantaskEngineService);
    }

    protected void unsetHumanTaskServer(
            HumanTaskEngineService humantaskEngineService) {
        if (log.isDebugEnabled()) {
            log.debug("HumanTaskServerService unbound from the HumanTask Deployer component");
        }
        HumanTaskDeployerContentHolder.getInstance().setHumanTaskServer(null);
    }

    protected void setTenantRegistryLoader(TenantRegistryLoader tenantRegLoader) {
        HumanTaskDeployerContentHolder.getInstance().setRegistryLoader(tenantRegLoader);
    }

    protected void unsetTenantRegistryLoader(TenantRegistryLoader tenantRegLoader) {
        HumanTaskDeployerContentHolder.getInstance().setRegistryLoader(null);
    }

    public static HumanTaskServer getHumanTaskServer() {
        return HumanTaskDeployerContentHolder.getInstance().getHumanTaskServer();
    }

    public static TenantRegistryLoader getTenantRegistryLoader(){
        return HumanTaskDeployerContentHolder.getInstance().getRegistryLoader();
    }
}
