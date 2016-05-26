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

import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.Deployment;
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
import org.wso2.carbon.deployment.engine.exception.CarbonDeploymentException;
import org.wso2.carbon.osgi.test.util.CarbonSysPropConfiguration;
import org.wso2.carbon.osgi.test.util.OSGiTestConfigurationUtils;

import javax.inject.Inject;
import java.nio.file.Path;
import java.util.List;

/**
 * Testing undeployment of bpmn artifact HelloWorld.bar
 */
@Listeners(PaxExam.class)
@ExamReactorStrategy(PerSuite.class)
public class BPMNUndeploymentTest {

    private static final Log log = LogFactory.getLog(BPMNUndeploymentTest.class);

    @Inject
    private BPMNDeployer bpmnDeployer;
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
    public void testHelloWorldBarUndeployment() throws CarbonDeploymentException {
        log.info("[Test] Undeployment test - HelloWorld.bar : Started");
        try {

            Object key = "HelloWorld.bar";
            bpmnDeployer.undeploy(key);
            RepositoryService repositoryService = bpmnEngineService.getProcessEngine().getRepositoryService();
            List<Deployment> activitiDeployments = repositoryService.createDeploymentQuery().list();
            Assert.assertEquals(activitiDeployments.size(), 0, "There is an active deployment count of new version" + activitiDeployments.size());

        } catch (Exception e) {
            log.error("Error  while undeploying HelloWorld Artifact.", e);
            Assert.fail("There are no artifacts undeployed.");
            throw e;

        }
        log.info("[Test] Undeployment test - HelloWorld.bar : Completed");
    }

}
