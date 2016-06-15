/**
 *  Copyright (c) 2015 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.wso2.carbon.bpmn.extensions.internal;


import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
/**
 * @scr.component name="org.wso2.carbon.bpmn.extensions.internal.ServiceComponent" immediate="true"
 * @scr.reference name="configuration.context.service"
 * interface="org.apache.axis2.context.ConfigurationContext"
 * cardinality="1..1" policy="dynamic"  bind="setConfigurationContextService"
 * unbind="unsetConfigurationContextService"
 */

public class ServiceComponent {
    private static Log log = LogFactory.getLog(ServiceComponent.class);
    private BundleContext bundleContext;

    protected void activate(ComponentContext ctxt) {
        log.info(" * * * * * * * * * * * * * * * * * * * * * ");
        log.info("Initializing the service component for the soap task...");
        try {
            bundleContext = ctxt.getBundleContext();
            ServerHolder serverHolder = ServerHolder.getInstance();

        }catch (Throwable e) {
            log.error("Failed to initialize the service component.", e);
        }
    }

    protected void deactivate(ComponentContext ctxt) {
        log.info("Stopping the component...");
    }

    protected void setConfigurationContextService(ConfigurationContext configCtxService) {
        ServerHolder.getInstance().setConfigCtxService(configCtxService);
    }

    protected void unsetConfigurationContextService(ConfigurationContext configCtxService) {
        ServerHolder.getInstance().setConfigCtxService(null);
    }

    public static ConfigurationContext getConfigurationContext() {
        return ServerHolder.getInstance().getConfigCtxService();
    }



}