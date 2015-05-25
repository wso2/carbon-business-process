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
package org.wso2.carbon.bpel.ui.clients;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bpel.stub.mgt.*;
import org.wso2.carbon.bpel.stub.mgt.InstanceManagementException;
import org.wso2.carbon.bpel.stub.mgt.types.*;

import java.rmi.RemoteException;

/**
 * Client which handle instance management service invocations
 */
public class InstanceManagementServiceClient {
    private static Log log = LogFactory.getLog(InstanceManagementServiceClient.class);
    private InstanceManagementServiceStub stub;

    public InstanceManagementServiceClient(String cookie,
                                           String backendServerURL,
                                           ConfigurationContext configContext) throws AxisFault {
        String serviceURL = backendServerURL + "InstanceManagementService";
        stub = new InstanceManagementServiceStub(configContext, serviceURL);
        ServiceClient client = stub._getServiceClient();
        Options option = client.getOptions();
        option.setManageSession(true);
        option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
    }

    public PaginatedInstanceList getPaginatedInstanceList(String instanceFilter,
                                                          String orderByKey,
                                                          int limit,
                                                          int page)
            throws Exception {
        try {
            return stub.getPaginatedInstanceList(instanceFilter, orderByKey, limit, page);
        } catch (Exception e) {
            log.error("getPaginatedInstanceList operation failed", e);
            throw e;
        }
    }

    /**
     * Return the BPELInstanceVariableSize allowed, defined in bps.xml
     * @return BPELInstanceVariableSize
     * @throws Exception
     */
    public int getBPELInstanceVariableSize() throws Exception {
        try {
            return stub.getBPELInstanceVariableSize();
        } catch (Exception e) {
            log.error("getBPELInstanceVariableSize operation failed", e);
            throw e;
        }
    }

    public InstanceInfoType getInstanceInfo(long iid) throws Exception {
        try {
            return stub.getInstanceInfo(iid);
        } catch (Exception e) {
            log.error("getInstanceInfo operation failed.", e);
            throw e;
        }
    }

    /**
     * Use to get activity-life-cycle events to log the instance execution
     *
     * @param iid - instance id
     * @return - a full set of activity life cycle events
     * @throws Exception If an error occurred while collecting life cycle info
     */
    public ActivityLifeCycleEventsType getActivityLifeCycleFilter(long iid) throws Exception {
        try {
            return stub.getActivityLifeCycleFilter(iid);
        } catch (Exception e) {
            log.error("getActivityLifeCycleFilter operation failed", e);
            throw e;
        }
    }

    /**
     * Use to get a full detail set of the instance execution
     * Have analysed the events and generate the InstanceInfoWithEventsType
     *
     * @param iid - instance id
     * @return - a summary of the instance
     * @throws Exception If an error occurred while collecting instance with events
     */
    public InstanceInfoWithEventsType getInstanceInfoWithEvents(long iid) throws Exception {
        try {
            return stub.getInstanceInfoWithEvents(iid);
        } catch (Exception e) {
            log.error("getInstanceInfo operation failed", e);
            throw e;
        }
    }

    public void suspendInstance(long iid) throws RemoteException, InstanceManagementException {
        try {
            stub.suspendInstance(iid);
        } catch (RemoteException re) {
            log.error("suspendInstance operation failed.", re);
            throw re;
        } catch (InstanceManagementException e) {
            log.error("suspendInstance operation failed: " + e.getFaultMessage().getResult(), e);
            throw e;
        }
    }

    public void resumeInstance(long iid) throws RemoteException, InstanceManagementException {
        try {
            stub.resumeInstance(iid);
        } catch (RemoteException re) {
            log.error("resumeInstance operation failed.", re);
            throw re;
        } catch (InstanceManagementException e) {
            log.error("resumeInstance operation failed: " + e.getFaultMessage().getResult(), e);
            throw e;
        }
    }

    public void deleteInstance(long iid, boolean deleteMessageExchanges)
            throws RemoteException, InstanceManagementException {
        String instanceFilter = "IID=" + iid;
        try {
            stub.deleteInstances(instanceFilter, deleteMessageExchanges);
        } catch (RemoteException re) {
            log.error("deleteInstances operation failed.", re);
            throw re;
        } catch (InstanceManagementException e) {
            log.error("deleteInstances operation failed: " + e.getFaultMessage().getResult(), e);
            throw e;
        }
    }

    public void deleteInstances(String filter, boolean deleteMessageExchanges)
            throws RemoteException, InstanceManagementException {
        try {
            stub.deleteInstances(filter, deleteMessageExchanges);
        } catch (RemoteException re) {
            log.error("deleteInstances operation failed.", re);
            throw re;
        } catch (InstanceManagementException e) {
            log.error("deleteInstances operation failed: " + e.getFaultMessage().getResult(), e);
            throw e;
        }
    }

    public void terminateInstance(long iid) throws RemoteException, InstanceManagementException {
        try {
            stub.terminateInstance(iid);
        } catch (RemoteException re) {
            log.error("terminateInstance operation failed.", re);
            throw re;
        } catch (InstanceManagementException e) {
            log.error("terminateInstance operation failed: " + e.getFaultMessage().getResult(), e);
            throw e;
        }
    }

    public void recoverActivity(long iid, long aid, Action_type1 action)
            throws RemoteException, InstanceManagementException {
        try {
            stub.recoverActivity(iid, aid, action);
        } catch (RemoteException re) {
            log.error("recoverActivity operation failed.", re);
            throw re;
        } catch (InstanceManagementException e) {
            log.error("recoverActivity operation failed: " + e.getFaultMessage().getResult(), e);
            throw e;
        }
    }

    public InstanceSummaryE getInstanceSummary()
            throws RemoteException, InstanceManagementException {
        try {
            return stub.getInstanceSummary();
        } catch (RemoteException e) {
            log.error("getInstanceSummary operation failed.", e);
            throw e;
        } catch (InstanceManagementException e) {
            log.error("getInstanceSummary operation failed: " + e.getFaultMessage().getResult(), e);
            throw e;
        }
    }
}
