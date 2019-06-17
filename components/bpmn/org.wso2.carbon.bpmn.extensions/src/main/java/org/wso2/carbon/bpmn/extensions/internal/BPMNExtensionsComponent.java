/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.bpmn.extensions.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.bpmn.core.BPMNEngineService;
import org.wso2.carbon.bpmn.extensions.rest.BPMNRestExtensionHolder;
import org.wso2.carbon.bpmn.extensions.rest.RESTClientShutdownObserver;
import org.wso2.carbon.bpmn.extensions.rest.RESTInvoker;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.utils.WaitBeforeShutdownObserver;

@Component(
        name = "org.wso2.carbon.bpel.BPMNExtensionsComponent",
        immediate = true)
public class BPMNExtensionsComponent {

    private static final Log log = LogFactory.getLog(BPMNExtensionsComponent.class);

    public static RegistryService getRegistryService() {

        return BPMNExtensionsHolder.getInstance().getRegistryService();
    }

    @Reference(
            name = "registry.service",
            service = org.wso2.carbon.registry.core.service.RegistryService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetRegistryService")
    protected void setRegistryService(RegistryService registryService) {

        if (log.isDebugEnabled()) {
            log.debug("RegistryService bound to the BPMN Extensions component");
        }
        BPMNExtensionsHolder.getInstance().setRegistryService(registryService);
    }

    public static BPMNEngineService getEngineService() {

        return BPMNExtensionsHolder.getInstance().getEngineService();
    }

    @Activate
    protected void activate(ComponentContext ctxt) {

        BundleContext bundleContext = ctxt.getBundleContext();
        RESTInvoker restInvoker = new RESTInvoker();
        BPMNRestExtensionHolder.getInstance().setRestInvoker(restInvoker);
        if (log.isDebugEnabled()) {
            log.debug("Activated bpmn extensions component and configured rest invoker");
        }
        bundleContext.registerService(WaitBeforeShutdownObserver.class, new RESTClientShutdownObserver(), null);
    }

    @Deactivate
    protected void deactivate(ComponentContext ctxt) {

        if (log.isDebugEnabled()) {
            log.debug("deactivated bpmn extensions component");
        }
    }

    protected void unsetRegistryService(RegistryService registryService) {

        if (log.isDebugEnabled()) {
            log.debug("RegistryService unbound from the BPMN Extensions component");
        }
        BPMNExtensionsHolder.getInstance().setRegistryService(null);
    }

    @Reference(
            name = "bpmnengine.service",
            service = org.wso2.carbon.bpmn.core.BPMNEngineService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetBPMNEngineService")
    protected void setBPMNEngineService(BPMNEngineService engineService) {

        if (log.isDebugEnabled()) {
            log.debug("BPMNEngineService bound to the BPMN Extensions component");
        }
        BPMNExtensionsHolder.getInstance().setEngineService(engineService);
    }

    protected void unsetBPMNEngineService(BPMNEngineService engineService) {

    }
}
