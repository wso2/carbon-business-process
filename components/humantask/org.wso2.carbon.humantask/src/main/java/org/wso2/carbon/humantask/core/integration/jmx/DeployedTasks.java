/*
 *
 *   Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 * /
 */

package org.wso2.carbon.humantask.core.integration.jmx;

import org.wso2.carbon.humantask.core.HumanTaskServer;
import org.wso2.carbon.humantask.core.internal.HumanTaskServiceComponent;
import org.wso2.carbon.humantask.core.store.HumanTaskBaseConfiguration;
import org.wso2.carbon.humantask.core.store.HumanTaskStore;

import java.util.List;


public class DeployedTasks implements DeployedTasksMXBean {

    private final String name = "DeployedTasksMXBeanImplementedClass";

    private String[] deployedTasks;


    public String[] getAllTasks() {
        return getAlldeployedTasks(-1234);
    }

    public String[] getAlldeployedTasks(int tenantID) {

        String[] noTask = {"No deployed task for the specified tenant"};
        String[] noStore = {"No Human Tasks Store found for the given tenantID"};
        HumanTaskServer humanTaskServer = HumanTaskServiceComponent.getHumanTaskServer();
        HumanTaskStore humanTaskStore = humanTaskServer.getTaskStoreManager().getHumanTaskStore(tenantID);
        if (humanTaskStore == null) {
            return noStore;
        }
        List<HumanTaskBaseConfiguration> humanTaskConfigurations = humanTaskStore.getTaskConfigurations();
        deployedTasks = new String[humanTaskConfigurations.size()];
        for (int i = 0; i < humanTaskConfigurations.size(); i++) {
            deployedTasks[i] = humanTaskConfigurations.get(i).getName() + "\t" + humanTaskConfigurations.get(i).getDefinitionName() + "\t" + humanTaskConfigurations.get(i).getOperation();
        }

        if (deployedTasks.length == 0) {
            return noTask;
        }
        return deployedTasks;
    }

    public String[] showAllDeployedTasks(int tenantID) {
        return getAlldeployedTasks(tenantID);
    }

    public String getName() {
        return name;
    }
}
