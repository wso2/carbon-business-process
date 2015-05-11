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
