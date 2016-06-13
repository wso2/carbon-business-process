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
import org.wso2.carbon.bpmn.tests.osgi.utils.BasicServerConfigurationUtil;
import org.wso2.carbon.deployment.engine.Artifact;
import org.wso2.carbon.deployment.engine.ArtifactType;
import org.wso2.carbon.deployment.engine.exception.CarbonDeploymentException;
import org.wso2.carbon.osgi.test.util.CarbonSysPropConfiguration;
import org.wso2.carbon.osgi.test.util.OSGiTestConfigurationUtils;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import javax.inject.Inject;

/**
 * Deploying an invalid bpmn artifact to bpmnDeployer
 */
@Listeners(PaxExam.class)
@ExamReactorStrategy(PerSuite.class)
public class BPMNInvalidDeploymentTest {
    private static final Log log = LogFactory.getLog(BPMNInvalidDeploymentTest.class);
    @Inject
    private BPMNEngineService bpmnEngineService;


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
    public void testNonExistentBarDeployment() {
        log.info("[Test] Non existent deployment test - sampleBPMN.bar : Started");
        String exceptionMessage = "";
        boolean isFailed = false;
        try {
            File ab = new File(Paths.get(BasicServerConfigurationUtil.getArtifactHome().toString(), "sampleBPMN.bar")
                    .toString());
            Artifact artifact = new Artifact(ab);
            ArtifactType artifactType = new ArtifactType<>("bar");
            artifact.setKey("emptyBPMN.bar");
            artifact.setType(artifactType);
            exceptionMessage = "Artifact " + artifact.getName() + " doesn't exists.";
            bpmnEngineService.getBpmnDeployer().deploy(artifact);

        } catch (Exception e) {
            Assert.assertTrue(exceptionMessage.equals(e.getMessage()), "Valid exception not thrown for non existent " +
                    "bar deployment.");
            isFailed = true;
        }
        Assert.assertTrue(isFailed, "Deployment method didn't throw CarbonDeploymentException");
        log.info("[Test] Non existent deployment test  - sampleBPMN.bar : Completed");
    }

    @Test(priority = 1)
    public void testInvalidExtensionBarDeployment() {
        final String INVALID_FILE = "HelloWorld.zip";
        log.info("[Test] Invalid deployment test - HelloWorld.zip : Started");
        String exceptionMessage = "";
        boolean isFailed = false;
        try {
            File ab = new File(Paths.get(BasicServerConfigurationUtil.getArtifactHome().toString(), INVALID_FILE)
                    .toString());
            Artifact artifact = new Artifact(ab);
            ArtifactType artifactType = new ArtifactType<>("bar");
            artifact.setKey(INVALID_FILE);
            artifact.setType(artifactType);
            exceptionMessage = "Unsupported Artifact type. Support only .bar files.";
            bpmnEngineService.getBpmnDeployer().deploy(artifact);

        } catch (CarbonDeploymentException e) {
            Assert.assertTrue(exceptionMessage.equals(e.getMessage()), "Valid exception not thrown for non bar " +
                    "deployment.");
            isFailed = true;
        }
        Assert.assertTrue(isFailed, "Deployment method didn't throw CarbonDeploymentException");
        log.info("[Test] Invalid deployment test  - HelloWorld.zip : Completed");
    }


}