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
import org.activiti.engine.impl.ProcessEngineImpl;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.persistence.entity.UserEntity;
import org.activiti.engine.impl.persistence.entity.UserIdentityManager;
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
import org.wso2.carbon.bpmn.core.integration.BPSUserIdentityManager;
import org.wso2.carbon.bpmn.core.integration.BPSUserManagerFactory;
import org.wso2.carbon.bpmn.tests.osgi.utils.BasicServerConfigurationUtil;
import org.wso2.carbon.deployment.engine.Artifact;
import org.wso2.carbon.deployment.engine.ArtifactType;
import org.wso2.carbon.deployment.engine.exception.CarbonDeploymentException;
import org.wso2.carbon.osgi.test.util.CarbonSysPropConfiguration;
import org.wso2.carbon.osgi.test.util.OSGiTestConfigurationUtils;
import org.wso2.carbon.security.caas.user.core.bean.Group;
import org.wso2.carbon.security.caas.user.core.bean.User;
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
 * Invoking a bpmn user claim task with group management
 */
@Listeners(PaxExam.class)
@ExamReactorStrategy(PerSuite.class)
public class BPMNClaimUserTaskTest {
    private static final Log log = LogFactory.getLog(BPMNClaimUserTaskTest.class);

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
    public void testDeployUserClaimTask() throws CarbonDeploymentException, IOException {
        log.info("[Test] Deploying  claim user task - VacationClaimRequest.bar: Started");
        try {
            File userArtifact = new File(Paths.get(BasicServerConfigurationUtil.getArtifactHome().toString(),
                    "VacationClaimRequest.bar")
                    .toString());
            // Creating Artifact type for Validation.
            Artifact artifact = new Artifact(userArtifact);
            ArtifactType artifactType = new ArtifactType<>("bar");
            artifact.setKey("AcceptRequest.bar");
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
            log.error("Error while deploying VacationClaim Artifact.", e);
            Assert.fail("There are no artifacts deployed.");
            throw e;
        }
        log.info("[Test] Deploying user task - VacationClaimRequest.bar: Completed");

    }

    @Test
    public void testRetrieveUserClaimValues() {
        log.info("[Test] Retrieving user claim values for admin: Started");
        try {
            ProcessEngineImpl engineImpl = (ProcessEngineImpl) bpmnEngineService.getProcessEngine();
            ProcessEngineConfigurationImpl config = engineImpl.getProcessEngineConfiguration();
            BPSUserManagerFactory factory = (BPSUserManagerFactory) config.getSessionFactories().
                    get(UserIdentityManager.class);
            BPSUserIdentityManager manager = (BPSUserIdentityManager) factory.openSession();
            String userId = "admin";
            UserEntity u = manager.findUserById(userId);
            Assert.assertEquals(u.getFirstName(), "Kermit");
            Assert.assertEquals(u.getLastName(), "The Frog");
        } catch (Exception e) {
            log.info("Error in retrieving user claims for user.", e);
            Assert.fail("Error in retrieving user claims for user.");
            throw e;
        }
        log.info("[Test] Retrieving user claim values for admin: Completed");
    }

    @Test(dependsOnMethods = "testDeployUserClaimTask")
    public void testInvokeClaimedUserTask() throws CarbonDeploymentException, IdentityStoreException,
            UserNotFoundException, IOException {
        log.info("[Test] Claiming User task - VacationClaimRequest.bar : Started");
        try {
            // retrieve user admin and authenticate
            ProcessEngineImpl engineImpl = (ProcessEngineImpl) bpmnEngineService.getProcessEngine();
            ProcessEngineConfigurationImpl config = engineImpl.getProcessEngineConfiguration();
            BPSUserManagerFactory factory = (BPSUserManagerFactory) config.getSessionFactories().
                    get(UserIdentityManager.class);
            BPSUserIdentityManager manager = (BPSUserIdentityManager) factory.openSession();

            User user = realmService.getIdentityStore().getUser("admin");
            Assert.assertEquals(user.getUserName().toString(), "admin",
                    "No matching user called admin is found");
            Assert.assertTrue(manager.checkPassword(user.getUserName(), "admin"),
                    "Unable to authenticate user" + user.getUserName());

            // start process instance
            Map<String, Object> variables = new HashMap<String, Object>();
            variables.put("employeeName", "John");
            variables.put("numberOfDays", new Integer(4));
            variables.put("vacationMotivation", "I'm really tired!");
            ProcessEngine processEngine = bpmnEngineService.getProcessEngine();
            RuntimeService runtimeService = processEngine.getRuntimeService();
            runtimeService.startProcessInstanceByKey("myProcess", variables);
            log.info("Number of process instances started: " + runtimeService.
                    createProcessInstanceQuery().count());

            // get task list belonging to given group : management
            TaskService taskService = processEngine.getTaskService();
            List<Task> tasks = taskService.createTaskQuery().taskCandidateGroup("management").list();
            // get the groups of the user admin
            List<org.activiti.engine.identity.Group> groupsOfUser = manager.
                    findGroupsByUser(user.getUserName());
            Assert.assertEquals(groupsOfUser.size(), 1, "Expected group count for admin user");
            // check if group management is in the list
            Group group = realmService.getIdentityStore().getGroupFromId(groupsOfUser.get(0).getId(),
                    user.getIdentityStoreId());
            Assert.assertTrue(group.getName().startsWith("management"), "Admin user" +
                    " does not belong to given group" + "management");

            if (tasks != null) {
                taskService.claim(tasks.get(0).getId(), "admin");
                List<Task> claimedTasks = taskService.createTaskQuery().taskAssignee("admin").list();
                Assert.assertEquals(claimedTasks.size(), 1, "User was unable to claim any task");
                //complete claimed task
                Map<String, Object> taskVariables = new HashMap<String, Object>();
                taskVariables.put("vacationApproved", "true");
                taskService.complete(tasks.get(0).getId());

            } else {
                Assert.fail("No tasks assigned for user admin");
            }
        } catch (Exception e) {
            log.info("Error in invoking user claim task", e);
            Assert.fail("Error in invoking user claim task.");
            throw e;
        } finally {
            bpmnEngineService.getBpmnDeployer().undeploy("VacationClaimRequest.bar");
        }
        log.info("[Test] Claiming User task - VacationClaimRequest.bar : Completed.");
    }
}
