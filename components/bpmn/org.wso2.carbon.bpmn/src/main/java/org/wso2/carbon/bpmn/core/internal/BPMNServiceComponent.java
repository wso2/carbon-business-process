/**
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.activiti.engine.ProcessEngines;
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
import org.wso2.carbon.bpmn.core.BPMNConstants;
import org.wso2.carbon.bpmn.core.BPMNEngineService;
import org.wso2.carbon.bpmn.core.BPMNEngineServiceImpl;
import org.wso2.carbon.bpmn.core.config.ProcessEngineConfiguration;
import org.wso2.carbon.bpmn.core.config.YamlBasedProcessEngineConfigurationFactory;
import org.wso2.carbon.bpmn.core.deployment.BPMNDeployer;
import org.wso2.carbon.datasource.core.api.DataSourceManagementService;
import org.wso2.carbon.datasource.core.api.DataSourceService;
import org.wso2.carbon.datasource.core.exception.DataSourceException;
import org.wso2.carbon.deployment.engine.Deployer;
import org.wso2.carbon.security.caas.user.core.service.RealmService;

import javax.naming.Context;
import javax.naming.NamingException;

/**
 * BPMN Service Component.
 */

@Component(
        name = "org.wso2.carbon.bpmn.core.BPMNEngineService",
        service = BPMNEngineServiceImpl.class,
        immediate = true)

public class BPMNServiceComponent {

    private static final Logger log = LoggerFactory.getLogger(BPMNServiceComponent.class);
    private DataSourceService datasourceService;
    private DataSourceManagementService datasourceManagementService;
    private JNDIContextManager jndiContextManager;
    private BundleContext bundleContext;

    //  Set CarbonRealmService
    @Reference(
            name = "org.wso2.carbon.security.CarbonRealmServiceImpl",
            service = RealmService.class,
            cardinality = ReferenceCardinality.AT_LEAST_ONE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unregisterCarbonRealm"
    )
    public void registerCarbonRealm(RealmService carbonRealmService) {
        if (log.isDebugEnabled()) {
            log.debug("register CarbonRealmService...");
        }
        BPMNServerHolder.getInstance().registerCarbonRealmService(carbonRealmService);
    }

    public void unregisterCarbonRealm(RealmService carbonRealmService) {
        if (log.isDebugEnabled()) {
            log.debug("Unregister CarbonRealmService...");
        }
    }

    @Reference(
            name = "org.wso2.carbon.datasource.jndi.JNDIContextManager",
            service = JNDIContextManager.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unRegisterJNDIContext")

    public void registerJNDIContext(JNDIContextManager contextManager) {
        if (log.isDebugEnabled()) {
            log.debug("register JNDI Context");
        }
        this.jndiContextManager = contextManager;
    }

    public void unRegisterJNDIContext(JNDIContextManager contextManager) {
        if (log.isDebugEnabled()) {
            log.debug("Unregister JNDI Context");
        }
    }

    @Reference(
            name = "org.wso2.carbon.datasource.core.api.DataSourceService",
            service = DataSourceService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unRegisterDataSourceService")
    public void registerDataSourceService(DataSourceService datasource) {
        if (log.isDebugEnabled()) {
            log.debug("register Datasource service");
        }
        this.datasourceService = datasource;
    }

    public void unRegisterDataSourceService(DataSourceService datasource) {
        if (log.isDebugEnabled()) {
            log.debug("unregister datasource service");
        }
    }

    @Reference(
            name = "org.wso2.carbon.datasource.core.api.DataSourceManagementService",
            service = DataSourceManagementService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unRegisterDataSourceManagementService")

    public void registerDataSourceManagementService(
            DataSourceManagementService datasourceMgtService) {
        if (log.isDebugEnabled()) {
            log.debug("register Datasource Management service");
        }
        this.datasourceManagementService = datasourceMgtService;
    }

    public void unRegisterDataSourceManagementService(DataSourceManagementService datasource) {
        if (log.isDebugEnabled()) {
            log.debug("unregister datasource service");
        }
    }

    @Activate
    protected void activate(ComponentContext ctxt) {
        try {
            this.bundleContext = ctxt.getBundleContext();
            registerJNDIContextForActiviti();
            BPMNServerHolder holder = BPMNServerHolder.getInstance();

            // Reading Process engine configuration.
            YamlBasedProcessEngineConfigurationFactory yamlBasedProcessEngineConfigurationFactory = new
                    YamlBasedProcessEngineConfigurationFactory();
            ProcessEngineConfiguration processEngineConfiguration =
                    yamlBasedProcessEngineConfigurationFactory.getProcessEngineConfiguration();
            holder.setProcessEngineConfiguration(processEngineConfiguration);

            ActivitiEngineBuilder.getInstance();
            holder.setEngine(ActivitiEngineBuilder.getInstance().buildEngine());
            BPMNEngineServiceImpl bpmnEngineService = new BPMNEngineServiceImpl();
            bpmnEngineService
                    .setProcessEngine(ActivitiEngineBuilder.getInstance().getProcessEngine());
            bpmnEngineService.setCarbonRealmService(holder.getInstance().getCarbonRealmService());
            bundleContext
                    .registerService(BPMNEngineService.class.getName(), bpmnEngineService, null);

            BPMNDeployer deployer = new BPMNDeployer();
            bpmnEngineService.setBpmnDeployer(deployer);
            bundleContext.registerService(Deployer.class.getName(), deployer, null);

        } catch (Throwable t) {
            log.error("Error initializing bpmn component " + t);
        }
    }

    @Deactivate
    protected void deactivate(ComponentContext ctxt) {
        if (log.isDebugEnabled()) {
            log.debug("Stopping the BPMN core component...");
        }
        ProcessEngines.destroy();
    }

    private void registerJNDIContextForActiviti() throws DataSourceException, NamingException {
        //DataSourceMetadata activitiDB = datasourceManagementService.getDataSource(BPMNConstants.BPMN_DB_NAME);
        //JNDIConfig jndiConfig = activitiDB.getJndiConfig();
        Context context = jndiContextManager.newInitialContext();

        Context subcontext = context.createSubcontext("java:comp/jdbc");
        subcontext.bind(BPMNConstants.BPMN_DB_CONTEXT_NAME,
                datasourceService.getDataSource(BPMNConstants.BPMN_DB_NAME));
    }

}

