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
 * Invoking a bpmn soap task
 */
@Listeners(PaxExam.class)
@ExamReactorStrategy(PerSuite.class)

public class BPMNSOAPTaskTest {
    private static final Log log = LogFactory.getLog(BPMNSOAPTaskTest.class);

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
    public void testDeploySoaptask() throws CarbonDeploymentException {
        log.info("[Test] Deploying  soap task : Started");
        try {
            File userArtifact = new File("/home/natasha/workspace/soapTask/deployment/soapTask.bar");
           // File userArtifact = new File("/home/natasha/Documents/SoapInvoker.bar");
            Artifact artifact = new Artifact(userArtifact);
            ArtifactType artifactType = new ArtifactType<>("bar");
            artifact.setKey("soapTask.bar");
           // artifact.setKey("SoapInvoker.bar");
            artifact.setType(artifactType);
            bpmnDeployer.deploy(artifact);

        } catch (Exception e) {
            log.error("Error while deploying soapTask.bar Artifact.", e);
            Assert.fail("There are no artifacts deployed.");
            throw e;
        }
        log.info("[Test] Deploying soap task : Completed");

    }

    @Test(dependsOnMethods = "testDeploySoaptask")
    public void testInvokeSoapTask() throws CarbonDeploymentException {
        log.info("[Tet] soap task -  Process Started");
        try {
            // start process instance

            ProcessEngine processEngine = bpmnEngineService.getProcessEngine();
            RuntimeService runtimeService = processEngine.getRuntimeService();
           /* Map<String, Object> taskVariables = new HashMap<>();
            taskVariables.put("serviceURL", "http://10.100.4.192:9763/services/HelloService");
            taskVariables.put("payload", "<ns1:hello xmlns:ns1='http://ode/bpel/unit-test.wsdl'>\" +\n" +
                    "                \"<TestPart>Hello</TestPart></ns1:hello>");
            taskVariables.put("headers", "<hello> header </hello> <world> header 2 </world>");
            taskVariables.put("soapVersion", "soap11");
            taskVariables.put("httpConnection", "");
            taskVariables.put("httpTransferEncoding", "");
            runtimeService.startProcessInstanceByKey("soapprocess", taskVariables); */
            runtimeService.startProcessInstanceByKey("soapTask");

        } catch (Exception e) {
            log.info("Error in invoking soap task", e);
            Assert.fail("Error in invoking soap task.");
            throw e;
        } finally {
            bpmnDeployer.undeploy("soapTask.bar");
           // bpmnDeployer.undeploy("SoapInvoker.bar");
        }
        log.info("[Test] soap task - Process Completed.");
    }
}
