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
import org.wso2.carbon.deployment.engine.exception.CarbonDeploymentException;
import org.wso2.carbon.osgi.test.util.CarbonSysPropConfiguration;
import org.wso2.carbon.osgi.test.util.OSGiTestConfigurationUtils;

import java.nio.file.Path;
import java.util.List;
import javax.inject.Inject;

/**
 * Invalid undeployment for bpmn artifacts
 */
@Listeners(PaxExam.class)
@ExamReactorStrategy(PerSuite.class)
public class BPMNInvalidUndeploymentTest {
    private static final Log log = LogFactory.getLog(BPMNInvalidUndeploymentTest.class);

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
    public void testNonExistentBarUndeployment() throws CarbonDeploymentException {
        log.info("[Test] Non existent undeployment test - sampleBPMN.bar : Started");
        String exceptionMessage = "";
        try {
            exceptionMessage = "File" + "sampleBPMN.bar" + "does not exist in activiti metadata registry";
            bpmnEngineService.getBpmnDeployer().undeploy("sampleBPMN.bar");
        } catch (Exception e) {
            Assert.assertTrue(exceptionMessage.equals(e.getMessage()), "Valid exception not thrown for non existent " +
                    "undeployment.");
        }
        log.info("[Test] Non existent undeployment test - sampleBPMN.bar : Completed");
    }

    @Test(priority = 1)
    public void testInvalidUndeployment() throws CarbonDeploymentException {
        log.info("[Test] Undeploying an already undeployed artifact test - HelloWorld.bar : Started");
        String exceptionMessage = "";
        try {
            bpmnEngineService.getBpmnDeployer().undeploy("HelloWorld.bar");
        } catch (Exception e) {
            Assert.assertTrue(exceptionMessage.equals(e.getMessage()), "Valid exception not thrown for non existent " +
                    "undeployment.");
        }
        log.info("[Test]  Undeploying an already undeployed artifact test - sampleBPMN.bar : Completed");

    }
}
