/**
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p>
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 **/
package org.wso2.carbon.bpmn.tests.osgi;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RuntimeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerSuite;
import org.ops4j.pax.exam.testng.listener.PaxExam;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.wso2.carbon.bpmn.core.BPMNEngineService;
import org.wso2.carbon.bpmn.core.deployment.BPMNDeployer;
import org.wso2.carbon.bpmn.tests.osgi.utils.BasicServerConfigurationUtil;
import org.wso2.carbon.deployment.engine.Artifact;
import org.wso2.carbon.deployment.engine.ArtifactType;
import org.wso2.carbon.deployment.engine.exception.CarbonDeploymentException;
import org.wso2.carbon.osgi.test.util.CarbonSysPropConfiguration;
import org.wso2.carbon.osgi.test.util.OSGiTestConfigurationUtils;
import org.wso2.carbon.security.caas.user.core.service.RealmService;

import javax.inject.Inject;
import java.io.File;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Invoking a bpmn rest task
 */
@Listeners(PaxExam.class)
@ExamReactorStrategy(PerSuite.class)

public class BPMNRESTTaskTest {
    private static final Log log = LogFactory.getLog(BPMNRESTTaskTest.class);

    @Inject
    private BPMNDeployer bpmnDeployer;
    @Inject
    private BPMNEngineService bpmnEngineService;
    @Inject
    private RealmService realmService;

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
    public void testDeployRESTtask() throws CarbonDeploymentException {
        log.info("[Test] Deploying  rest task : Started");
        try {
            File userArtifact = new File("/home/natasha/Downloads/RestCaller.bar");
            Artifact artifact = new Artifact(userArtifact);
            ArtifactType artifactType = new ArtifactType<>("bar");
            artifact.setKey("RestCaller.bar");
            artifact.setType(artifactType);
            bpmnDeployer.deploy(artifact);

        } catch (Exception e) {
            log.error("Error while deploying restTask.bar Artifact.", e);
            Assert.fail("There are no artifacts deployed.");
            throw e;
        }
        log.info("[Test] Deploying rest task : Completed");

    }

    @Test(dependsOnMethods = "testDeployRESTtask")
    public void testInvokeRESTTask() throws CarbonDeploymentException {
        log.info("[Test] rest task -  Process Started");
        try {
            // start process instance

            ProcessEngine processEngine = bpmnEngineService.getProcessEngine();
            RuntimeService runtimeService = processEngine.getRuntimeService();

            runtimeService.startProcessInstanceByKey("restProcess");

        } catch (Exception e) {
            log.info("Error in invoking rest task", e);
            Assert.fail("Error in invoking rest task.");
            throw e;
        } finally {
            bpmnDeployer.undeploy("RestCaller.bar");

        }
        log.info("[Test] rest task - Process Completed.");
    }
}
