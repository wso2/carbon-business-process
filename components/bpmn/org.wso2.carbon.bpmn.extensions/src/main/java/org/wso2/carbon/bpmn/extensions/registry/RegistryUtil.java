/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.bpmn.extensions.registry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bpmn.extensions.internal.BPMNExtensionsComponent;
import org.wso2.carbon.bpmn.extensions.registry.impl.CarbonResource;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.registry.api.Registry;
import org.wso2.carbon.registry.core.config.RegistryContext;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

/**
 * Utility class for registry related operations
 */
public class RegistryUtil {

    private static final String GOVERNANCE_REGISTRY_PREFIX = "gov:/";
    private static final String CONFIGURATION_REGISTRY_PREFIX = "conf:/";

    private static final Log LOG = LogFactory.getLog(RegistryUtil.class);

    /**
     * Function to retrieve registry resource for given registry path
     *
     * @param resourcePath registry path for resource
     * @param tenantIdInt tenant ID
     * @return resource
     * @throws RegistryException
     */
    public static Resource getRegistryResource(String resourcePath, int tenantIdInt) throws RegistryException {

        RealmService realmService = RegistryContext.getBaseInstance().getRealmService();
        String domain;
        try {
            domain = realmService.getTenantManager().getDomain(tenantIdInt);
        } catch (UserStoreException e) {
            throw new RegistryException("Error occurred while retrieving tenant domain", e);
        }
        Registry registry;
        Resource resource;
        try {
            String registryPath;
            PrivilegedCarbonContext.startTenantFlow();
            if (domain != null) {
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(domain);
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantIdInt);
            } else {
                PrivilegedCarbonContext.getThreadLocalCarbonContext()
                        .setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantIdInt);
            }

            if (resourcePath.startsWith(GOVERNANCE_REGISTRY_PREFIX)) {
                registryPath = resourcePath.substring(GOVERNANCE_REGISTRY_PREFIX.length());
                registry = BPMNExtensionsComponent.getRegistryService().getGovernanceSystemRegistry(tenantIdInt);
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Reading registry resource : " + registryPath + " from governance registry");
                }
            } else if (resourcePath.startsWith(CONFIGURATION_REGISTRY_PREFIX)) {
                registryPath = resourcePath.substring(CONFIGURATION_REGISTRY_PREFIX.length());
                registry = BPMNExtensionsComponent.getRegistryService().getConfigSystemRegistry(tenantIdInt);
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Reading registry resource : " + registryPath + " from configuration registry");
                }
            } else {
                String msg =
                        "Registry type is not specified for the resource. Resource path should begin with gov:/ or " +
                                "conf:/ to indicate the registry type.";
                throw new RegistryException(msg);
            }
            resource = new CarbonResource(registry.get(registryPath));

        } catch (org.wso2.carbon.registry.api.RegistryException e) {
            throw new RegistryException(e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
        return resource;
    }

}
