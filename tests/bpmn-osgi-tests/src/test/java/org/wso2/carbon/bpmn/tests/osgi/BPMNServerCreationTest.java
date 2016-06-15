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
import org.wso2.carbon.kernel.utils.CarbonServerInfo;
import org.wso2.carbon.osgi.test.util.CarbonSysPropConfiguration;
import org.wso2.carbon.osgi.test.util.OSGiTestConfigurationUtils;
import org.wso2.carbon.security.caas.user.core.service.RealmService;

import java.nio.file.Path;
import java.util.List;
import javax.inject.Inject;

@Listeners(PaxExam.class)
@ExamReactorStrategy(PerSuite.class)
public class BPMNServerCreationTest {

    private static final Log log = LogFactory.getLog(BPMNServerCreationTest.class);

    @Inject
    private BundleContext bundleContext;

    @Inject
    private BPMNEngineService bpmnEngineService;

    @Inject
    private RealmService realmService;

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

    @Test(priority = 0)
    public void testProcessEngineCreation() {
        log.info("[Test] Process engine creation : Started");
        ProcessEngine processEngine = bpmnEngineService.getProcessEngine();
        Assert.assertNotNull(processEngine, "processEngine is not set");
        String name = processEngine.getName();
        Assert.assertNotNull(name, "processEngine name is null.");
        log.info("[Test] Process engine creation : Completed..");
    }

//    public void createUser() {
//        CredentialStore credentialStore = realmService.getCredentialStore();
//        IdentityStore identityStore = realmService.getIdentityStore();
//        AuthorizationStore authorizationStore = realmService.getAuthorizationStore();
//        ClaimManager claimManager = realmService.getClaimManager();
//        authorizationStore.
//    }

}
