/**
 *  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.bpel.analytics.publisher;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bpel.core.ode.integration.BPELServerImpl;
import org.wso2.carbon.bpel.core.ode.integration.store.TenantProcessStore;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.databridge.agent.DataPublisher;
import org.wso2.carbon.databridge.agent.exception.DataEndpointException;
import org.wso2.carbon.utils.AbstractAxis2ConfigurationContextObserver;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

/**
 * Listen to Axis2ConfigurationContext life cycle events and stop the DataPublisher when the tenant is unloaded
 */
public class Axis2ConfigurationContextObserverImpl extends
                                                        AbstractAxis2ConfigurationContextObserver {
    private static Log log = LogFactory.getLog(Axis2ConfigurationContextObserverImpl.class);
    private BPELServerImpl bpelServer;

    public Axis2ConfigurationContextObserverImpl(){
        bpelServer = BPELServerImpl.getInstance();
    }

    public void createdConfigurationContext(ConfigurationContext configurationContext) {

    }

    public void terminatingConfigurationContext(ConfigurationContext configurationContext) {
        Integer tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();

        log.info("Removing data publishers for this tenant " + tenantId + ".");

        TenantProcessStore tenantsProcessStore = bpelServer.getMultiTenantProcessStore().getTenantsProcessStore(
                tenantId);
        if(tenantsProcessStore != null) {
            Map dataPublisherMap = tenantsProcessStore.getDataPublisherMap();
            if(dataPublisherMap != null) {
                Collection<DataPublisher> dataPublisherCollection = dataPublisherMap.values();
                Iterator<DataPublisher> iterator = dataPublisherCollection.iterator();
                while(iterator.hasNext()){
	                DataPublisher dataPublisher = iterator.next();
	                try {
		                dataPublisher.shutdown();
	                } catch (DataEndpointException e) {
		                String errMsg = "Error while shutting down tenant Data Publisher";
		                log.error(errMsg, e);
	                }
                }
	            dataPublisherCollection.clear();
            }
        }
    }
}
