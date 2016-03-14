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

import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RuntimeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.bpmn.core.ActivitiEngineBuilder;
import org.wso2.carbon.bpmn.core.BPMNEngineService;
import org.wso2.carbon.bpmn.core.BPMNServerHolder;
import org.wso2.carbon.bpmn.core.db.*;
import org.wso2.carbon.bpmn.core.exception.BPMNMetaDataTableCreationException;
import org.wso2.carbon.bpmn.core.exception.DatabaseConfigurationException;
import org.wso2.carbon.bpmn.core.deployment.BPMNDeployer;
import org.wso2.carbon.bpmn.core.mgt.dao.ActivitiDAO;
import org.wso2.carbon.kernel.deployment.ArtifactType;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.repository.ProcessDefinition;
import java.io.File;
import java.util.List;
import org.wso2.carbon.kernel.deployment.Artifact;

/**
 * @scr.component name="org.wso2.carbon.bpmn.core.internal.BPMNServiceComponent" immediate="true"
 * //@scr.reference name="registry.service" interface="org.wso2.carbon.registry.core.service.RegistryService"
 * //cardinality="1..1" policy="dynamic"  bind="setRegistryService" unbind="unsetRegistryService"
 */
public class BPMNServiceComponent {

    private static Log log = LogFactory.getLog(BPMNServiceComponent.class);

    protected void activate(ComponentContext ctxt) {
        log.info("Initializing the BPMN core component...");
        try {
            BundleContext bundleContext = ctxt.getBundleContext();
            BPMNServerHolder holder = BPMNServerHolder.getInstance();
            ActivitiEngineBuilder activitiEngineBuilder = new ActivitiEngineBuilder();
            holder.setEngine(activitiEngineBuilder.buildEngine());
            //holder.setTenantManager(new TenantManager());

            //TODO:COMMENTED
           // BPMNRestExtensionHolder restHolder = BPMNRestExtensionHolder.getInstance();

            //restHolder.setRestInvoker(new RESTInvoker());
            //TODO:COMMENTED
            BPMNEngineServiceImpl bpmnEngineService = new BPMNEngineServiceImpl();
            bpmnEngineService.setProcessEngine(ActivitiEngineBuilder.getProcessEngine());
            //bundleContext.registerService(BPMNEngineService.class, bpmnEngineService, null);
            //bundleContext.registerService(WaitBeforeShutdownObserver.class, new BPMNEngineShutdown(), null);


           // DataSourceHandler dataSourceHandler = new DataSourceHandler();
           //dataSourceHandler.initDataSource(activitiEngineBuilder.getDataSourceJndiName());
           // dataSourceHandler.closeDataSource();

	        // ---- TEST DEPLOYER ------//
	        	       BPMNDeployer customDeployer = new BPMNDeployer();
	        	        customDeployer.init();
	        	        File ab = new File("/Users/himasha/Desktop/Latest/new/wso2bps-3.5.1/repository/samples/bpmn/HelloWorld.bar");
	        	        Artifact artifact =new Artifact( ab);
	        	       ArtifactType artifactType = new ArtifactType<>("bar");
	        	        artifact.setKey("HelloWorld.bar");
	        	        artifact.setType(artifactType);
	                   customDeployer.deploy(artifact);
	        	        log.error("Deployed in c5");
	        	        ProcessEngine eng = ActivitiEngineBuilder.getProcessEngine();
	        	        RepositoryService repositoryService = eng.getRepositoryService();
	        	        RuntimeService runtimeService = eng.getRuntimeService();
	        	       //   repositoryService.activateProcessDefinitionById("helloWorldProcess");
	        //	       //   repositoryService.activateProcessDefinitionById("helloWorldProcess");
	        	        log.error("activated");
	        	        List<ProcessDefinition> processDefinitions = repositoryService.createProcessDefinitionQuery()
	        	                                                                      .processDefinitionKey("helloWorldProcess")
	        	                                                                      .orderByProcessDefinitionVersion()
	        	                                                                      .asc()
	        	                                                                      .list();
	        	        log.error("DEFS" + processDefinitions);
	        	      //  runtimeService.createProcessInstanceByKey("helloWorldProcess");
	        	        runtimeService.startProcessInstanceByKey("helloWorldProcess");
	        	        log.error("STARTED");
	        //	        customDeployer.undeploy("HelloWorld.bar");
	        //	        log.error("Undeployed in c5");
	        // ---- TEST DEPLOYER ------//
        } catch (Throwable e) {
            log.error("Failed to initialize the BPMN core component.", e);
        }
    }

    protected void deactivate(ComponentContext ctxt) {
        log.info("Stopping the BPMN core component...");
		ProcessEngines.destroy();
    }



}