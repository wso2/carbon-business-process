package org.wso2.carbon.humantask.core.integration.jmx;

import javax.management.DescriptorKey;

public interface DeployedTasksMXBean {
    @DescriptorKey("number of cache slots in use")
    public String[] getAllTasks();

    public String[] showAllDeployedTasks(int tenantID);

    public String getName();
}
