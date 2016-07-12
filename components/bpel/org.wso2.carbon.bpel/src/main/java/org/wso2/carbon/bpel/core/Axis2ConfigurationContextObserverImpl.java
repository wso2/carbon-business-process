/**
 * Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 */
package org.wso2.carbon.bpel.core;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bpel.core.ode.integration.BPELServerImpl;
import org.wso2.carbon.bpel.core.ode.integration.store.ProcessStoreImpl;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.utils.AbstractAxis2ConfigurationContextObserver;

/**
 * Listen to Axis2ConfigurationContext life cycle events and do the necessary tasks to register
 * BPEL Deployer and unregister BPEL Deployer.
 */
public class Axis2ConfigurationContextObserverImpl extends
        AbstractAxis2ConfigurationContextObserver {
    private static Log log = LogFactory.getLog(Axis2ConfigurationContextObserverImpl.class);
    private BPELServerImpl bpelServer;

    public Axis2ConfigurationContextObserverImpl() {
        bpelServer = BPELServerImpl.getInstance();
    }

    public void createdConfigurationContext(ConfigurationContext configurationContext) {

    }

    public void terminatingConfigurationContext(ConfigurationContext configurationContext) {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();

        log.info("Unloading TenantProcessStore for tenant " + tenantId + ".");

        ((ProcessStoreImpl) bpelServer.getMultiTenantProcessStore()).unloadTenantProcessStore(tenantId);
    }

}
