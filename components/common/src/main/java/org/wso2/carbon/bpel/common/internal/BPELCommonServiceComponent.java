/*
 * Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.bpel.common.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.registry.core.service.RegistryService;

@Component(
        name = "org.wso2.carbon.bpel.common.internal.BPELCommonServiceComponent",
        immediate = true)
public class BPELCommonServiceComponent {

    private static Log log = LogFactory.getLog(BPELCommonServiceComponent.class);

    private BundleContext bundleContext;

    private ServiceRegistration registration;

    @Activate
    protected void activate(ComponentContext ctxt) {

        try {
            this.bundleContext = ctxt.getBundleContext();
        } catch (Throwable t) {
            log.error("Failed to activate BPEL common bundle", t);
        }
        if (log.isDebugEnabled()) {
            log.debug("BPEL common bundle is activated.");
        }
    }

    @Reference(
            name = "registry.service",
            service = org.wso2.carbon.registry.core.service.RegistryService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetRegistryService")
    protected void setRegistryService(RegistryService registrySvc) {

        if (log.isDebugEnabled()) {
            log.debug("RegistryService bound to the BPEL common component");
        }
        BPELCommonServiceHolder.getInstance().setRegistryService(registrySvc);
    }

    protected void unsetRegistryService(RegistryService registrySvc) {

        if (log.isDebugEnabled()) {
            log.debug("RegistryService unbound from the BPEL common component");
        }
        BPELCommonServiceHolder.getInstance().setRegistryService(null);
    }

    public static RegistryService getRegistryService() {

        return BPELCommonServiceHolder.getInstance().getRegistryService();
    }

    @Deactivate
    protected void deactivate(ComponentContext componentContext) throws Exception {

        if (log.isDebugEnabled()) {
            log.debug("Stopping the BPEL common Component");
        }
        componentContext.getBundleContext().ungetService(registration.getReference());
    }
}
