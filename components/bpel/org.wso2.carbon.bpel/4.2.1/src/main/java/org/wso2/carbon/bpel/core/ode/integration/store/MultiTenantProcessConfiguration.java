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

/**
 * In multi-tenant environment there will be ConfigurationContext instance for each and every
 * tenant. ProcessConfigurations which support multi-tenant interfaces should implement
 * this interface to provide necessary information to other layers.
 */
public interface MultiTenantProcessConfiguration {

    /**
     * Return the ConfigurationContext of the tenant where process has deployed.
     *
     * @return ConfigurationContext of the tenant
     */
    ConfigurationContext getTenantConfigurationContext();
}
