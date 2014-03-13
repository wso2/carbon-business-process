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
package org.wso2.carbon.bpel.core.ode.integration.config;

import org.apache.commons.collections.map.MultiKeyMap;
import org.wso2.carbon.bpel.common.config.EndpointConfiguration;

/**
 * Package Configuration
 * Contains the global level endpoint configurations as well as package level endpoint
 * configurations
 * TODO add a mechanism to configure global level package configurations
 */
public class PackageConfiguration {

    /**
     * This map contains endpoint configuration instances stored according to the service,
     * service namespace and  port they are associated with.
     * Endpoint configuration common to all services is associated with [null, null, null] key.
     * Configuration common to all ports in a service is associated with [service, ns, null] or
     * [service, null, null] keys.
     * Configuration for service and port couple is associated with [service, ns, port] or
     * [service, null, port] keys.
     */
    private MultiKeyMap endpointConfigurations = new MultiKeyMap();

    public MultiKeyMap getEndpoints() {
        return endpointConfigurations;
    }

    public void setEndpoints(MultiKeyMap endpointConfigurations) {
        this.endpointConfigurations = endpointConfigurations;
    }

    public void addEndpoint(EndpointConfiguration endpoint) {
        String serviceName = endpoint.getServiceName();
        String port = endpoint.getServicePort();
        String ns = endpoint.getServiceNS();

        endpointConfigurations.put(serviceName, ns, port, endpoint);
    }
}
