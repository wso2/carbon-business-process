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

package org.wso2.carbon.bpel.core.ode.integration.store.clustering;

import org.apache.axis2.clustering.ClusteringFault;
import org.apache.axis2.clustering.state.StateClusteringCommand;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bpel.core.BPELConstants;
import org.wso2.carbon.bpel.core.ode.integration.store.ProcessStoreImpl;
import org.wso2.carbon.bpel.core.ode.integration.store.TenantProcessStore;

import java.util.List;

/**
 * This command is used when the BPEL package is undeployed
 */
@Deprecated
public class BPELPackageUndeployedCommand extends StateClusteringCommand {
    private static final Log log = LogFactory.getLog(BPELConstants.LOGGER_DEPLOYMENT);

    private Integer tenantId;

    private List<String> bpelPackageVersions;

    private String bpelPackageName;

    public BPELPackageUndeployedCommand(List<String> bpelPackageVersions, String bpelPackageName,
                                        Integer tenantId) {
        this.tenantId = tenantId;
        this.bpelPackageVersions = bpelPackageVersions;
        this.bpelPackageName = bpelPackageName;
    }

    @Override
    public void execute(ConfigurationContext configurationContext) throws ClusteringFault {
        if (log.isDebugEnabled()) {
            String debugMsg = "New un-deployment command received. Tenant: " + tenantId +
                            " Package: " + bpelPackageName + " Versions: ";
            for (String version : bpelPackageVersions) {
                debugMsg = debugMsg.concat(version + " ,");
            }
            log.debug(debugMsg);
        }
        ProcessStoreImpl parentProcessStore =
                (ProcessStoreImpl)configurationContext.getAxisConfiguration().
                        getParameter(BPELConstants.PARAM_PARENT_PROCESS_STORE).getValue();
        TenantProcessStore tenantProcessStore = parentProcessStore.getTenantsProcessStore(tenantId);
        tenantProcessStore.handleBPELPackageUndeploymentNotification(bpelPackageName,
                                                                     bpelPackageVersions);
    }
}
