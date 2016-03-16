/**
 *  Copyright (c) 2014-2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.bpmn.core.internal;

//import org.osgi.framework.BundleContext;

import org.osgi.service.component.ComponentContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.jndi.JNDIContextManager;
import org.wso2.carbon.bpmn.core.ActivitiEngineBuilder;
import org.wso2.carbon.bpmn.core.BPMNServerHolder;
import org.wso2.carbon.kernel.startupresolver.RequiredCapabilityListener;

//import org.wso2.carbon.bpmn.core.BPMNEngineService;
//import org.wso2.carbon.bpmn.core.db.*;
//import org.wso2.carbon.bpmn.core.deployment.BPMNDeployer;
//import org.wso2.carbon.bpmn.core.exception.BPMNMetaDataTableCreationException;
//import org.wso2.carbon.bpmn.core.exception.DatabaseConfigurationException;
//import org.activiti.engine.RepositoryService;
//import org.activiti.engine.ProcessEngine;
//import org.activiti.engine.repository.ProcessDefinition;

//import java.io.File;
//import java.util.List;

//import org.wso2.carbon.kernel.deployment.Artifact;
//import org.wso2.carbon.kernel.deployment.ArtifactType;
//import org.wso2.carbon.kernel.startupresolver.RequiredCapabilityListener;

/**
 *
 */

public class BPMNServiceComponent {

    private static final Logger log = LoggerFactory.getLogger(BPMNServiceComponent.class);

//    @Reference(
//            name = "org.wso2.carbon.kernel.datasource.core.internal.DataSourceListenerComponent",
//            service = RequiredCapabilityListener.class,
//            cardinality = ReferenceCardinality.MANDATORY,
//            policy = ReferencePolicy.DYNAMIC,
//            unbind = "deactivate"
//    )
@Reference(
		name = "org.wso2.carbon.datasource.jndi",
		service = JNDIContextManager.class,
		cardinality = ReferenceCardinality.AT_LEAST_ONE,
		policy = ReferencePolicy.DYNAMIC,
		unbind = "onJNDIUnregister"
)
    protected void activate(ComponentContext ctxt) {
        log.info("Initializing the BPMN core component...");
        try {
            // BundleContext bundleContext = ctxt.getBundleContext();
            BPMNServerHolder holder = BPMNServerHolder.getInstance();
            ActivitiEngineBuilder activitiEngineBuilder = new ActivitiEngineBuilder();
            holder.setEngine(activitiEngineBuilder.buildEngine());

            BPMNEngineServiceImpl bpmnEngineService = new BPMNEngineServiceImpl();
            bpmnEngineService.setProcessEngine(ActivitiEngineBuilder.getProcessEngine());
            // DataSourceHandler dataSourceHandler = new DataSourceHandler();
            //dataSourceHandler.initDataSource(activitiEngineBuilder.getDataSourceJndiName());
            // dataSourceHandler.closeDataSource();

        } catch (Throwable e) {
            log.error("Failed to initialize the BPMN core component.", e);
        }
    }

    protected void deactivate(ComponentContext ctxt) {
        log.info("Stopping the BPMN core component...");
        //ProcessEngines.destroy();
    }

}

