/*
 * Copyright (c) 2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.humantask.core;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.humantask.core.internal.HumanTaskServerHolder;
import org.wso2.carbon.utils.AbstractAxis2ConfigurationContextObserver;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

public class Axis2ConfigurationContextObserverImpl extends
        AbstractAxis2ConfigurationContextObserver {
    private static Log log = LogFactory.getLog(Axis2ConfigurationContextObserverImpl.class);

    private HumanTaskServer humanTaskServer;

    public Axis2ConfigurationContextObserverImpl() {
        humanTaskServer = HumanTaskServerHolder.getInstance().getHtServer();
    }

    public void createdConfigurationContext(ConfigurationContext configurationContext) {
//        log.info("HUMANTASK createdConfigurationContext");
//        Integer tenantId = MultitenantUtils.getTenantId(configurationContext);
//        AxisConfiguration axisConfig = configurationContext.getAxisConfiguration();
//        DeploymentEngine deploymentEngine = (DeploymentEngine) axisConfig.getConfigurator();
//
//        HumanTaskStore taskStoreForTenant =
//                htServer.getTaskStoreManager().createHumanTaskStoreForTenant(tenantId, axisConfig);
//
//        AxisHumanTaskDeployer humantaskDeployer = new AxisHumanTaskDeployer();
//        humantaskDeployer.setHumanTaskStore(taskStoreForTenant);
//        deploymentEngine.addDeployer(humantaskDeployer,
//                HumanTaskConstants.HUMANTASK_REPO_DIRECTORY,
//                HumanTaskConstants.HUMANTASK_PACKAGE_EXTENSION);

    }

    public void terminatingConfigurationContext(ConfigurationContext configurationContext) {
        Integer tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        log.info("Unloading TenantTaskStore for tenant " + tenantId + ".");
        humanTaskServer.getTaskStoreManager().unloadTenantTaskStore(tenantId);
    }
}
