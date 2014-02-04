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

import org.wso2.carbon.humantask.core.HumanTaskServer;
import org.wso2.carbon.registry.core.service.TenantRegistryLoader;

/**
 * Data holder for the HumanTaskServiceComponent
 */
public final class HumanTaskDeployerContentHolder {
    private static HumanTaskDeployerContentHolder instance;

    private HumanTaskServer humantaskServer;
    private TenantRegistryLoader registryLoader;

    private HumanTaskDeployerContentHolder() {}

    public static HumanTaskDeployerContentHolder getInstance() {
        if(instance == null) {
            instance = new HumanTaskDeployerContentHolder();
        }
        return instance;
    }

    public HumanTaskServer getHumanTaskServer() {
        return humantaskServer;
    }

    public void setHumanTaskServer(org.wso2.carbon.humantask.core.HumanTaskEngineService humantaskEngineService) {
        if (humantaskEngineService == null) {
            this.humantaskServer = null;
        } else {
            this.humantaskServer = humantaskEngineService.getHumanTaskServer();
        }
    }

    public TenantRegistryLoader getRegistryLoader() {
        return registryLoader;
    }

    public void setRegistryLoader(TenantRegistryLoader registryLoader) {
        this.registryLoader = registryLoader;
    }
}
