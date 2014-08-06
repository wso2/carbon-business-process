/*
 * Copyright (c) 2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.bpel.core.ode.integration.store;

import org.apache.axis2.context.ConfigurationContext;

import javax.xml.namespace.QName;
import java.io.File;

/**
 * Multi-tenant process store object. Multi-tenant process store will contain several
 * TenantProcessStores and will handle
 */
public interface MultiTenantProcessStore {

    /**
     * Creates tenant specific process store for given tenant ID. BPEL Deployer instance must use
     * this to create a process store for itself given that there is BPEL Deployer instance for
     * each tenant.
     *
     * @param tenantConfigurationContext Configuration context of the tenant which we are creating
     *                                   process store.
     * @return TenantProcessStore instance
     */
    TenantProcessStore createProcessStoreForTenant(ConfigurationContext tenantConfigurationContext);

    /**
     * There should be a directory for each tenant where, deployer used to extract deployed
     * BPEL packages. Location of these tenant specific directories can be configured. This method
     * return the root directory for tenant specific process stores.
     *
     * @return Parent directory of tenant specific process stores.
     */
    File getLocalDeploymentUnitRepo();

    /**
     * Returns the process store for specific tenant.
     *
     * @param tenantId tenant ID
     * @return TenantProcessStore object.
     */
    TenantProcessStore getTenantsProcessStore(Integer tenantId);

    /**
     * Get tenant id of the given process
     *
     * @param pid QName of the process
     * @return Tenant id of the process
     */
    Integer getTenantId(QName pid);

    void removeFromProcessToTenantMap(QName pid);
}
