/**
 * Copyright (c) 2014-2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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


import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import org.osgi.service.jndi.JNDIContextManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.bpmn.core.ActivitiEngineBuilder;
import org.wso2.carbon.bpmn.core.BPMNEngineService;
import org.wso2.carbon.bpmn.core.BPMNServerHolder;
import org.wso2.carbon.bpmn.core.db.DataSourceHandler;
import org.wso2.carbon.kernel.startupresolver.RequiredCapabilityListener;

/**
 *
 */

@Component(
        name = "org.wso2.carbon.bpmn.core.internal.BPMNServiceComponent",
        immediate = true
)
public class BPMNServiceComponent implements RequiredCapabilityListener {

    private static final Logger log = LoggerFactory.getLogger(BPMNServiceComponent.class);


    @Reference(
            name = "org.wso2.carbon.datasource.jndi",
            service = JNDIContextManager.class,
            cardinality = ReferenceCardinality.AT_LEAST_ONE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unRegisterJNDIContext"
    )

    public void registerJNDIContext(JNDIContextManager contextManager) {
        log.info("Registering JNDI Context");

    }

    public void unRegisterJNDIContext(JNDIContextManager contextManager) {
        log.info("Unregister JNDI Context");
    }

    @Activate
    protected void activate(ComponentContext ctxt) {
        log.info("Initializing the BPMN core component...");

        try {
            BundleContext bundleContext = ctxt.getBundleContext();
            BPMNServerHolder holder = BPMNServerHolder.getInstance();
//            ActivitiEngineBuilder activitiEngineBuilder = new ActivitiEngineBuilder();
//            holder.setEngine(activitiEngineBuilder.buildEngine());
//
//            BPMNEngineServiceImpl bpmnEngineService = new BPMNEngineServiceImpl();
//            bpmnEngineService.setProcessEngine(activitiEngineBuilder.getProcessEngine());
//            bundleContext.registerService(BPMNEngineService.class.getName(), bpmnEngineService, null);
//            DataSourceHandler dataSourceHandler = new DataSourceHandler();
//            dataSourceHandler.initDataSource(activitiEngineBuilder.getDataSourceJndiName());
//            dataSourceHandler.closeDataSource();

        } catch (Throwable e) {
            log.error("Failed to initialize the BPMN core component.", e);
        }
    }
    @Deactivate
    protected void deactivate(ComponentContext ctxt) {
        log.info("Stopping the BPMN core component...");
        //ProcessEngines.destroy();
    }

    @Override
    public void onAllRequiredCapabilitiesAvailable() {
        log.info("OnAllRequiredCapabilitiesAvailable");
    }
}

