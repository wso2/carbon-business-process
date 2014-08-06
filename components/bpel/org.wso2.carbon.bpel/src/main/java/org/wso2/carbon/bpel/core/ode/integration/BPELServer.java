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

package org.wso2.carbon.bpel.core.ode.integration;

import org.wso2.carbon.bpel.core.ode.integration.config.BPELServerConfiguration;
import org.wso2.carbon.bpel.core.ode.integration.store.MultiTenantProcessStore;
import org.apache.commons.httpclient.HttpConnectionManager;

/**
 * BPEL Server which initialize the data sources, data access layers, transaction managers,
 * BPEL Process stores, Http connection manager and other registered ODE extensions.
 *
 * This will describe the OSGi service interface where others can used to interact with BPEL
 * Server implementation.
 */
public interface BPELServer {
    /**
     * Register BPEL Event listener in ODE BPEL Engine.
     *
     * @param eventListenerClass  Fully qualified class name of BpelEventListener implementation.
     */
    void registerEventListener(String eventListenerClass);

    /**
     * Register ODE Message Exchange Interceptor in ODE BPEL Engine.
     *
     * @param mexInterceptorClass  Fully qualified class name of 
     * MessageExchangeInterceptor implementation
     */
    void registerMessageExchangeInterceptor(String mexInterceptorClass);


    /**
     * Returns multi-tenant BPEL process store. This process store will be a composition of
     * tenant specific process store for each tenant.
     *
     * @return MultiTenantProcessStore BPEL Process Store 
     */
    MultiTenantProcessStore getMultiTenantProcessStore();

    /**
     * Get the multi threaded http connection manager to use with external service invocations.
     * @return HttpConnectionManager instace(multi-threaded implementation).
     */
    HttpConnectionManager getHttpConnectionManager();

    /**
     * Get the BPEL server configuration which is derived from "bps.xml" file
     * @return BPELServerConfiguration
     */
    BPELServerConfiguration getBpelServerConfiguration();
}

