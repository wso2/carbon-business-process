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

import javax.inject.Inject;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Deploying a new version of an existing deployment
 */
@Listeners(PaxExam.class)
@ExamReactorStrategy(PerSuite.class)
public class BPMNVersionDeploymentTest {
    private static final Log log = LogFactory.getLog(BPMNVersionDeploymentTest.class);
    @Inject
    private BPMNEngineService bpmnEngineService;

    @Inject
    private BPMNDeployer bpmnDeployer;

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
    public void testHelloWorldVersionDeployment() throws CarbonDeploymentException{
        log.info("[Test] Version Deployment test - HelloWorld.bar new version : Started");
        try {
            File ab = new File(Paths.get(BasicServerConfigurationUtil.getVersionArtifactHome().toString(), "HelloWorld.bar")
                    .toString());
            Artifact artifact = new Artifact(ab);
            ArtifactType artifactType = new ArtifactType<>("bar");
            artifact.setKey("HelloWorld.bar");
            artifact.setType(artifactType);
            bpmnDeployer.deploy(artifact);

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
        }catch (Exception e){
            log.error("Error while deploying HelloWorld Artifact." , e);
            Assert.fail("There is no artifacts deployed.");
            throw  e;
        }
        log.info("[Test] Version Deployment test - HelloWorld.bar new version : Completed");
    }
    @Test(dependsOnMethods = "testHelloWorldVersionDeployment")
    public void testStartNewHelloWorldBarProcess() throws CarbonDeploymentException {
        log.info("[Test] Deployment test - HelloWorld.bar new instance creating : Started");
        ProcessEngine processEngine = bpmnEngineService.getProcessEngine();
        RuntimeService runtimeService = processEngine.getRuntimeService();
        runtimeService.startProcessInstanceByKey("helloWorldProcess");
        log.info("[Test] Deployment test - HelloWorld.bar new instance creating : Completed");

    }

}
