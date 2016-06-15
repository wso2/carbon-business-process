/**
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 **/


package org.wso2.carbon.bpmn.tests.osgi;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.repository.Deployment;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerSuite;
import org.ops4j.pax.exam.testng.listener.PaxExam;
import org.osgi.framework.BundleContext;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.wso2.carbon.bpmn.core.BPMNEngineService;
import org.wso2.carbon.bpmn.tests.osgi.utils.BasicServerConfigurationUtil;
import org.wso2.carbon.deployment.engine.Artifact;
import org.wso2.carbon.deployment.engine.ArtifactType;
import org.wso2.carbon.deployment.engine.exception.CarbonDeploymentException;
import org.wso2.carbon.kernel.utils.CarbonServerInfo;
import org.wso2.carbon.osgi.test.util.CarbonSysPropConfiguration;
import org.wso2.carbon.osgi.test.util.OSGiTestConfigurationUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import javax.inject.Inject;


@Listeners(PaxExam.class)
@ExamReactorStrategy(PerSuite.class)
public class BPMNDeploymentTest {

    private static final Log log = LogFactory.getLog(BPMNDeploymentTest.class);

    @Inject
    private BundleContext bundleContext;

    @Inject
    private BPMNEngineService bpmnEngineService;

    @Inject
    private CarbonServerInfo carbonServerInfo;

    @Configuration
    public Option[] createConfiguration() {

        List<Option> optionList = BasicServerConfigurationUtil.createBasicConfiguration();
        Path carbonHome = BasicServerConfigurationUtil.getCarbonHome();

        CarbonSysPropConfiguration sysPropConfiguration = new CarbonSysPropConfiguration();
        sysPropConfiguration.setCarbonHome(carbonHome.toString());
        sysPropConfiguration.setServerKey("carbon-bpmn");
        sysPropConfiguration.setServerName("WSO2 Carbon BPMN Server");
        sysPropConfiguration.setServerVersion("1.0.0");

        optionList = OSGiTestConfigurationUtils.getConfiguration(optionList, sysPropConfiguration);
        Option[] options = optionList.toArray(new Option[optionList.size()]);
        return options;
    }

    @Test
    public void testHelloWorldBarDeployment() throws CarbonDeploymentException {
        log.info("[Test] Deployment test - HelloWorld.bar : Started");
        try {
            File ab = new File(Paths.get(BasicServerConfigurationUtil.getArtifactHome().toString(), "HelloWorld.bar")
                    .toString());
            Artifact artifact = new Artifact(ab);
            ArtifactType artifactType = new ArtifactType<>("bar");
            artifact.setKey("HelloWorld.bar");
            artifact.setType(artifactType);
            bpmnEngineService.getBpmnDeployer().deploy(artifact);

            RepositoryService repositoryService = bpmnEngineService.getProcessEngine().getRepositoryService();
            List<Deployment> activitiDeployments = repositoryService.createDeploymentQuery().list();
            if (activitiDeployments != null) {
                Assert.assertEquals(activitiDeployments.size(), 1, "Expected Deployment count");
                Deployment deployment = activitiDeployments.get(0);
                //log.info("USUAL DEP NAME" + deployment.getName());
                Assert.assertTrue(artifact.getName().toString().startsWith(deployment.getName()), "Artifact Name " +
                        "mismatched.");
            } else {
                Assert.fail("There is no artifacts deployed.");
            }
        } catch (Exception e) {
            log.error("Erro while deploying HelloWorld Artifact.", e);
            Assert.fail("There is no artifacts deployed.");
            throw e;
        }
        log.info("[Test] Deployment test - HelloWorld.bar : Completed");
    }

    @Test(dependsOnMethods = "testHelloWorldBarDeployment")
    public void testStartHelloWorldBarProcess() {
        log.info("[Test] Deployment test - HelloWorld.bar instance creating : Completed");
        ProcessEngine processEngine = bpmnEngineService.getProcessEngine();
        RuntimeService runtimeService = processEngine.getRuntimeService();
        runtimeService.startProcessInstanceByKey("helloWorldProcess");
        log.info("[Test] Deployment test - HelloWorld.bar instance creating : Started");
    }

    @Test(dependsOnMethods = "testStartHelloWorldBarProcess")
    public void testHelloWorldVersionDeployment() throws CarbonDeploymentException, IOException {
        log.info("[Test] Version Deployment test - HelloWorld.bar new version : Started");
        try {
            File ab = new File(Paths.get(BasicServerConfigurationUtil.getVersionArtifactHome().toString(),
                    "HelloWorld.bar")
                    .toString());
            Artifact artifact = new Artifact(ab);
            ArtifactType artifactType = new ArtifactType<>("bar");
            artifact.setKey("HelloWorld.bar");
            artifact.setType(artifactType);
            bpmnEngineService.getBpmnDeployer().deploy(artifact);

            RepositoryService repositoryService = bpmnEngineService.getProcessEngine().getRepositoryService();
            List<Deployment> activitiDeployments = repositoryService.createDeploymentQuery().list();
            if (activitiDeployments != null) {
                Assert.assertEquals(activitiDeployments.size(), 2, "Expected Deployment count");
                Deployment deployment = activitiDeployments.get(1);
                Assert.assertTrue(artifact.getName().toString().startsWith(deployment.getName()), "Artifact Name " +
                        "mismatched.");
            } else {
                Assert.fail("There is no artifacts deployed.");
            }
        } catch (Exception e) {
            log.error("Error while deploying HelloWorld Artifact.", e);
            Assert.fail("There is no artifacts deployed.");
            throw e;
        }
        log.info("[Test] Version Deployment test - HelloWorld.bar new version : Completed");
    }

    @Test(dependsOnMethods = "testHelloWorldVersionDeployment")
    public void testStartNewVersionHelloWorldBarProcess() throws CarbonDeploymentException {
        log.info("[Test] Deployment test - HelloWorld.bar new instance creating : Started");
        ProcessEngine processEngine = bpmnEngineService.getProcessEngine();
        RuntimeService runtimeService = processEngine.getRuntimeService();
        runtimeService.startProcessInstanceByKey("helloWorldProcess");
        log.info("[Test] Deployment test - HelloWorld.bar new instance creating : Completed");

    }

    @Test(dependsOnMethods = "testStartNewVersionHelloWorldBarProcess")
    public void testHelloWorldBarUndeployment() throws CarbonDeploymentException, IOException {
        log.info("[Test] Undeployment test - HelloWorld.bar : Started");
        try {

            String key = "HelloWorld.bar";
            bpmnEngineService.getBpmnDeployer().undeploy(key);
            RepositoryService repositoryService = bpmnEngineService.getProcessEngine().getRepositoryService();
            List<Deployment> activitiDeployments = repositoryService.createDeploymentQuery().list();
            Assert.assertEquals(activitiDeployments.size(), 0, "There is an active deployment count of new version" +
                    activitiDeployments.size());

        } catch (Exception e) {
            log.error("Error  while undeploying HelloWorld Artifact.", e);
            Assert.fail("There are no artifacts undeployed.");
            throw e;

        }
        log.info("[Test] Undeployment test - HelloWorld.bar : Completed");
    }

}