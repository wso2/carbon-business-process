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
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.task.Task;
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
import org.wso2.carbon.security.caas.user.core.bean.User;
import org.wso2.carbon.security.caas.user.core.exception.AuthorizationStoreException;
import org.wso2.carbon.security.caas.user.core.exception.IdentityStoreException;
import org.wso2.carbon.security.caas.user.core.exception.UserNotFoundException;
import org.wso2.carbon.security.caas.user.core.service.RealmService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;

/**
 * Invoking a bpmn user task
 */
@Listeners(PaxExam.class)
@ExamReactorStrategy(PerSuite.class)
public class BPMNUserTaskTest {

    private static final Log log = LogFactory.getLog(BPMNUserTaskTest.class);
    @Inject
    private RealmService realmService;
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
    public void testDeployUserTask() throws CarbonDeploymentException, IOException {
        log.info("[Test] Deploying user task - VacationRequest.bar: Started");
        try {
            File userArtifact = new File(Paths.get(BasicServerConfigurationUtil.getArtifactHome().toString(),
                    "VacationRequest.bar")
                    .toString());
            Artifact artifact = new Artifact(userArtifact);
            ArtifactType artifactType = new ArtifactType<>("bar");
            artifact.setKey("VacationRequest.bar");
            artifact.setType(artifactType);
            bpmnEngineService.getBpmnDeployer().deploy(artifact);

            RepositoryService repositoryService = bpmnEngineService.getProcessEngine().getRepositoryService();
            List<Deployment> activitiDeployments = repositoryService.createDeploymentQuery().list();
            if (activitiDeployments != null) {
                Assert.assertEquals(activitiDeployments.size(), 1, "Expected Deployment count");
                Deployment deployment = activitiDeployments.get(0);
                Assert.assertTrue(artifact.getName().toString().startsWith(deployment.getName()), "Artifact Name " +
                        "mismatched.");
            } else {
                Assert.fail("There are no artifacts deployed.");
            }
        } catch (Exception e) {
            log.error("Erro while deploying VacationRequest Artifact.", e);
            Assert.fail("There are no artifacts deployed.");
            throw e;
        }
        log.info("[Test] Deploying user task - VacationRequest.bar: Completed");
    }

    @Test(dependsOnMethods = "testDeployUserTask")
    public void testInvokeUserTask() throws CarbonDeploymentException, IdentityStoreException,
            UserNotFoundException, AuthorizationStoreException, IOException {
        log.info("[Test] Invoking User task - VacationRequest.bar : Started");
        try {
            User user = realmService.getIdentityStore().getUser("admin");
            Assert.assertEquals(user.getUserName(), "admin", "No matching user called admin is found");
            // start process instance
            Map<String, Object> variables = new HashMap<String, Object>();
            variables.put("employeeName", "John");
            variables.put("numberOfDays", 4);
            variables.put("vacationMotivation", "I'm really tired!");
            ProcessEngine processEngine = bpmnEngineService.getProcessEngine();
            RuntimeService runtimeService = processEngine.getRuntimeService();
            runtimeService.startProcessInstanceByKey("myProcess", variables);
            log.info("Number of process instances started: " + runtimeService.createProcessInstanceQuery().count());

            //query tasks  assigned to user admin
            TaskService taskService = processEngine.getTaskService();
            List<Task> tasks = taskService.createTaskQuery().taskCandidateOrAssigned(user.getUserName())
                    .list();
            if (tasks != null) {
                Assert.assertEquals(tasks.size(), 1, "Expected task count for admin");
                Task task = tasks.get(0);

                Map<String, Object> taskVariables = new HashMap<String, Object>();
                taskVariables.put("vacationApproved", "true");

                taskService.complete(task.getId(), taskVariables);
                log.info("Task with id : " + task.getId() + " is completed.");
                List<Task> newTasks = taskService.createTaskQuery().taskCandidateOrAssigned(user.getUserName()).list();
                Assert.assertEquals(newTasks.size(), 0, "New expected task count for admin");

            } else {
                Assert.fail("No tasks assigned for user admin");
            }

        } catch (Exception e) {
            log.info("Error in invoking user task", e);
            Assert.fail("Error in invoking user task.");
            throw e;
        } finally {
            bpmnEngineService.getBpmnDeployer().undeploy("VacationRequest.bar");
        }


        log.info("[Test] Invoking User task - VacationRequest.bar : Completed");

    }
}
