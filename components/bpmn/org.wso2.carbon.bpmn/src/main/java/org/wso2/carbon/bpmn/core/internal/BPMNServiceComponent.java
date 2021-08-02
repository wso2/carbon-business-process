/**
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.bpmn.core.internal;

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
import org.wso2.carbon.bpmn.core.ActivitiEngineBuilder;
import org.wso2.carbon.bpmn.core.BPMNEngineService;
import org.wso2.carbon.bpmn.core.BPMNServerHolder;
import org.wso2.carbon.bpmn.core.MailEventListener;
import org.wso2.carbon.bpmn.core.deployment.TenantManager;
import org.wso2.carbon.bpmn.core.integration.BPMNEngineServerStartupObserver;
import org.wso2.carbon.bpmn.core.integration.BPMNEngineWaitBeforeShutdownObserver;
import org.wso2.carbon.core.ServerStartupObserver;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.utils.WaitBeforeShutdownObserver;

@Component(
        name = "org.wso2.carbon.bpmn.core.internal.BPMNServiceComponent",
        immediate = true)
public class BPMNServiceComponent {

    private static Log log = LogFactory.getLog(BPMNServiceComponent.class);
    private MailEventListener mailEventListener;

    @Activate
    protected void activate(ComponentContext ctxt) {
        mailEventListener = new MailEventListener();
        log.info("Initializing the BPMN core component...");
        try {
            BundleContext bundleContext = ctxt.getBundleContext();
            BPMNServerHolder holder = BPMNServerHolder.getInstance();
            ActivitiEngineBuilder activitiEngineBuilder = new ActivitiEngineBuilder();
            holder.setEngine(activitiEngineBuilder.buildEngine());
            holder.setTenantManager(new TenantManager());
            /*BPMNRestExtensionHolder restHolder = BPMNRestExtensionHolder.getInstance();

            restHolder.setRestInvoker(new RESTInvoker());*/

            ActivitiEngineBuilder.getProcessEngine().getRuntimeService().addEventListener(mailEventListener);

            BPMNEngineServiceImpl bpmnEngineService = new BPMNEngineServiceImpl();
            bpmnEngineService.setProcessEngine(ActivitiEngineBuilder.getProcessEngine());
            bundleContext.registerService(BPMNEngineService.class, bpmnEngineService, null);
            bundleContext.registerService(ServerStartupObserver.class.getName(), new BPMNEngineServerStartupObserver
                    (), null);
            bundleContext.registerService(WaitBeforeShutdownObserver.class, new BPMNEngineWaitBeforeShutdownObserver
                    (), null);
            // DataSourceHandler dataSourceHandler = new DataSourceHandler();
            // dataSourceHandler.initDataSource(activitiEngineBuilder.getDataSourceJndiName());
            // dataSourceHandler.closeDataSource();
            // } catch (BPMNMetaDataTableCreationException e) {
            // log.error("Could not create BPMN checksum table", e);
            // } catch (DatabaseConfigurationException e) {
            // log.error("Could not create BPMN checksum table", e);
        } catch (Throwable e) {
            log.error("Failed to initialize the BPMN core component.", e);
        }
    }

    @Deactivate
    protected void deactivate(ComponentContext ctxt) {

        log.info("Stopping the BPMN core component...");
        // ProcessEngines.destroy();
    }

    @Reference(
            name = "registry.service",
            service = org.wso2.carbon.registry.core.service.RegistryService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetRegistryService")
    protected void setRegistryService(RegistryService registrySvc) {

        if (log.isDebugEnabled()) {
            log.debug("RegistryService bound to the BPMN component");
        }
        BPMNServerHolder.getInstance().setRegistryService(registrySvc);
    }

    public void unsetRegistryService(RegistryService registryService) {

        if (log.isDebugEnabled()) {
            log.debug("RegistryService unbound from the BPMN component");
        }
        BPMNServerHolder.getInstance().unsetRegistryService(registryService);
    }
}
