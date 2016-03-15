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


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.bpmn.core.ActivitiEngineBuilder;
import org.wso2.carbon.bpmn.core.BPMNEngineService;
import org.wso2.carbon.bpmn.core.BPMNServerHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.bpmn.core.db.*;
import org.wso2.carbon.bpmn.core.deployment.BPMNDeployer;
import org.wso2.carbon.bpmn.core.exception.BPMNMetaDataTableCreationException;
import org.wso2.carbon.bpmn.core.exception.DatabaseConfigurationException;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.repository.ProcessDefinition;
import java.io.File;
import java.util.List;
import org.wso2.carbon.kernel.deployment.Artifact;
import org.wso2.carbon.bpmn.core.db.DataSourceServiceListenerComponent;
import org.wso2.carbon.kernel.deployment.ArtifactType;
import org.wso2.carbon.kernel.startupresolver.RequiredCapabilityListener;

/**
 * @scr.component name="org.wso2.carbon.bpmn.core.internal.BPMNServiceComponent" immediate="true"
 * //@scr.reference name="registry.service" interface="org.wso2.carbon.registry.core.service.RegistryService"
 * //cardinality="1..1" policy="dynamic"  bind="setRegistryService" unbind="unsetRegistryService"
 */

public class BPMNServiceComponent {

	private static final Logger log = LoggerFactory.getLogger(BPMNServiceComponent.class);

	/*@Reference(
			name = "org.wso2.carbon.kernel.datasource.core.internal.DataSourceListenerComponent",
			service = RequiredCapabilityListener.class,
			cardinality = ReferenceCardinality.MANDATORY,
			policy = ReferencePolicy.DYNAMIC,
			unbind = "deactivate"
	)*/
	protected void activate(ComponentContext ctxt) {
		log.info("Initializing the BPMN core component...");
		try {
			BundleContext bundleContext = ctxt.getBundleContext();
			BPMNServerHolder holder = BPMNServerHolder.getInstance();
			ActivitiEngineBuilder activitiEngineBuilder = new ActivitiEngineBuilder();
			holder.setEngine(activitiEngineBuilder.buildEngine());

			BPMNEngineServiceImpl bpmnEngineService = new BPMNEngineServiceImpl();

			bpmnEngineService.setProcessEngine(ActivitiEngineBuilder.getProcessEngine());

			//TEST PURPOSES
//			BPMNDeployer customDeployer = new BPMNDeployer();
//			customDeployer.init();
//			File ab = new File("/Users/himasha/Desktop/Latest/new/wso2bps-3.5.1/repository/samples/bpmn/HelloWorld.bar");
//			Artifact artifact =new Artifact( ab);
//			ArtifactType artifactType = new ArtifactType<>("bar");
//			artifact.setKey("HelloWorld.bar");
//			artifact.setType(artifactType);
//			customDeployer.deploy(artifact);
//			log.error("Deployed in c5");


			// DataSourceHandler dataSourceHandler = new DataSourceHandler();
			//dataSourceHandler.initDataSource(activitiEngineBuilder.getDataSourceJndiName());
			// dataSourceHandler.closeDataSource();


		} catch (Throwable e) {
			log.error("Failed to initialize the BPMN core component.", e);
		}
	}

	protected void deactivate(ComponentContext ctxt) {
		log.info("Stopping the BPMN core component...");
		//		ProcessEngines.destroy();
	}



}