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

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.task.Task;
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
import org.wso2.carbon.bpmn.core.config.ProcessEngineConfiguration;
import org.wso2.carbon.bpmn.core.config.YamlBasedProcessEngineConfigurationFactory;
import org.wso2.carbon.bpmn.core.deployment.BPMNDeployer;
import org.wso2.carbon.datasource.core.api.DataSourceManagementService;
import org.wso2.carbon.datasource.core.api.DataSourceService;
import org.wso2.carbon.datasource.core.exception.DataSourceException;
import org.wso2.carbon.deployment.engine.Artifact;
import org.wso2.carbon.deployment.engine.ArtifactType;
import org.wso2.carbon.deployment.engine.Deployer;
import org.wso2.carbon.security.caas.user.core.service.RealmService;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
        log.info("register CarbonRealmService...");
        BPMNServerHolder.getInstance().registerCarbonRealmService(carbonRealmService);
    }

    public void unregisterCarbonRealm(RealmService carbonRealmService) {
        log.info("Unregister CarbonRealmService...");
    }

    @Reference(
            name = "org.wso2.carbon.datasource.jndi.JNDIContextManager",
            service = JNDIContextManager.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unRegisterJNDIContext")

    public void registerJNDIContext(JNDIContextManager contextManager) {
        log.info("register JNDI Context");
        this.jndiContextManager = contextManager;
    }

    public void unRegisterJNDIContext(JNDIContextManager contextManager) {
        log.info("Unregister JNDI Context");
    }

    @Reference(
            name = "org.wso2.carbon.datasource.core.api.DataSourceService",
            service = DataSourceService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unRegisterDataSourceService")
    public void registerDataSourceService(DataSourceService datasource) {
        log.info("register Datasource service");
        this.datasourceService = datasource;
    }

    public void unRegisterDataSourceService(DataSourceService datasource) {
        log.info("unregister datasource service");
    }

    @Reference(
            name = "org.wso2.carbon.datasource.core.api.DataSourceManagementService",
            service = DataSourceManagementService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unRegisterDataSourceManagementService")

    public void registerDataSourceManagementService(
            DataSourceManagementService datasourceMgtService) {
        log.info("register Datasource Management service");
        this.datasourceManagementService = datasourceMgtService;
    }

    public void unRegisterDataSourceManagementService(DataSourceManagementService datasource) {
        log.info("unregister datasource service");
    }

    @Activate
    protected void activate(ComponentContext ctxt) {
        log.info("BPMN core component activator...");
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
            bundleContext.registerService(Deployer.class.getName(), deployer, null);

            // Create metadata table for deployments
//            DataSourceHandler dataSourceHandler = new DataSourceHandler();
//            dataSourceHandler
//                    .initDataSource(ActivitiEngineBuilder.getInstance().getDataSourceJndiName());
//            dataSourceHandler.closeDataSource();

           /* BPMNDeployer customDeployer = new BPMNDeployer();
            customDeployer.init();

            File userArtifact = new File("/home/natasha/Downloads/RestCaller.bar");
            Artifact artifact = new Artifact(userArtifact);
            ArtifactType artifactType = new ArtifactType<>("bar");
            artifact.setKey("RestCaller.bar");
            artifact.setType(artifactType);
            customDeployer.deploy(artifact);

            log.info("Artifact Deployed");

            ProcessEngine eng = bpmnEngineService.getProcessEngine();
            RuntimeService runtimeService = eng.getRuntimeService();

            runtimeService.startProcessInstanceByKey("restProcess");
            log.info("Process Instance started");*/

            BPMNDeployer customDeployer = new BPMNDeployer();
            customDeployer.init();

            ProcessEngine eng = bpmnEngineService.getProcessEngine();
            RuntimeService runtimeService = eng.getRuntimeService();

            ///// Without expressions just string values --> works fine with fixed string values
            File ab = new File("/home/natasha/workspace/testSample/deployment/testprocess.bar");
            Artifact artifact = new Artifact(ab);
            ArtifactType artifactType = new ArtifactType<>("bar");
            artifact.setKey("testprocess.bar");
            artifact.setType(artifactType);
            customDeployer.deploy(artifact);
            log.info("Artifact Deployed");
            runtimeService.startProcessInstanceByKey("testprocess");

            ////////////////////////////////////////////////////////

            ///// Expressions ---> Works fine with expressions
          //  File ab = new File("/home/natasha/Documents/SoapInvoker.bar");
           /* File ab = new File("/home/natasha/workspace/SoapInvoker/deployment/SoapInvoker.bar");
            Artifact artifact = new Artifact(ab);
            ArtifactType artifactType = new ArtifactType<>("bar");
            artifact.setKey("SoapInvoker.bar");
            artifact.setType(artifactType);
            customDeployer.deploy(artifact);
            log.info("Artifact Deployed");
            Map<String, Object> taskVariables = new HashMap<>();
            taskVariables.put("serviceURL", "http://10.100.4.192:9763/services/HelloService");
            taskVariables.put("payload" , "<ns1:hello xmlns:ns1='http://ode/bpel/unit-test.wsdl'>\" +\n" +
                    "                \"<TestPart>Hello</TestPart></ns1:hello>");
            taskVariables.put("httpTransferEncoding" , "chunked");
           *//* taskVariables.put("headers", "<ns1:hello xmlns:ns1='http://ode/bpel/unit-test.wsdl'>" +
                    "<TestPart>HEADER11</TestPart></ns1:hello>");
            taskVariables.put("soapVersion" , "soap11");
            taskVariables.put("httpConnection", "");*//*
            runtimeService.startProcessInstanceByKey("myProcess", taskVariables);*/





            log.info("Process Instance started");

            TaskService taskService = eng.getTaskService();
            List<Task> tasks = taskService.createTaskQuery().list();
            for (Task task : tasks) {
                log.info("Task available: " + task.getName());
                log.info(" -------------------------------");
                log.info("Doc:  " + task.getDescription());
            }




        } catch (Throwable t) {
            log.error("Error initializing bpmn component ", t);
        }
    }

    @Deactivate
    protected void deactivate(ComponentContext ctxt) {
        log.info("Stopping the BPMN core component...");
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

