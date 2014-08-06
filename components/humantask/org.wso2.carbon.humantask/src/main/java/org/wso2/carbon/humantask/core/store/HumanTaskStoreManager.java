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

package org.wso2.carbon.humantask.core.store;

import org.apache.axis2.context.ConfigurationContext;
import org.wso2.carbon.humantask.core.HumanTaskConstants;
import org.wso2.carbon.utils.CarbonUtils;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Contains all the tenant task stores. Also responsible for creation of new task stores when new tenant is loading,
 * re-load and un-load of tenant task stores.
 */
public class HumanTaskStoreManager {

    private Map<Integer, HumanTaskStore> tenantTaskStoreMap = new ConcurrentHashMap<Integer, HumanTaskStore>();

    /**
     * Create Human Task for a tenant and add it to the internal task store map.
     * @param tenantId  ID of the tenant which requested a task store
     * @param configContext axis configuration of the tenant which requested a task store
     * @return HumanTaskStore instance
     */
    public HumanTaskStore createHumanTaskStoreForTenant(int tenantId, ConfigurationContext configContext) {
        HumanTaskStore taskStore = new HumanTaskStore(tenantId, configContext);
        taskStore.setHumanTaskDeploymentRepo(new File(CarbonUtils.getCarbonHome() +
                File.separator + "repository" + File.separator + HumanTaskConstants.HUMANTASK_REPO_DIRECTORY));
        tenantTaskStoreMap.put(tenantId, taskStore);

        return taskStore;
    }

    /**
     * Returns the HumanTaskStore instance of tenant specified, null if the HumanTaskStore instance is not available for
     * that tenant.
     * @param tenantId Tenant's Identifier
     * @return HumanTaskStore instance
     */
    public HumanTaskStore getHumanTaskStore(int tenantId){
        return tenantTaskStoreMap.get(tenantId);
    }

    /**
     * Tenant unloading logic for human tasks.
     *
     * @param tenantId : tenant id  being unloaded.
     */
    public void unloadTenantTaskStore(int tenantId) {
        if (tenantTaskStoreMap.get(tenantId) != null) {
            HumanTaskStore taskStore = tenantTaskStoreMap.get(tenantId);
            for(HumanTaskBaseConfiguration taskBaseConfig : taskStore.getTaskConfigurations()) {
                taskStore.removeAxisServiceForTaskConfiguration(taskBaseConfig);
            }
            taskStore.unloadCaches();
            tenantTaskStoreMap.remove(tenantId);
        }
    }
}
