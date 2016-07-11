/**
 * Copyright (c) 2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.bpel.ui.clients;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bpel.stub.mgt.ProcessManagementException;
import org.wso2.carbon.bpel.stub.mgt.ProcessManagementServiceStub;
import org.wso2.carbon.bpel.stub.mgt.types.PaginatedProcessInfoList;
import org.wso2.carbon.bpel.stub.mgt.types.ProcessDeployDetailsList_type0;
import org.wso2.carbon.bpel.stub.mgt.types.ProcessInfoType;
import org.wso2.carbon.service.mgt.stub.types.carbon.ServiceMetaData;
import org.wso2.carbon.service.mgt.ui.ServiceAdminClient;

import java.rmi.RemoteException;
import java.util.Locale;
import javax.xml.namespace.QName;

/**
 * Client which invokes process management service.
 */
public class ProcessManagementServiceClient {
    private static Log log = LogFactory.getLog(ProcessManagementServiceClient.class);
    //    private static final String BUNDLE = "org.wso2.carbon.bpel.ui.i18n.Resources";
//    private ResourceBundle bundle;
    private ProcessManagementServiceStub stub;

    private String cookie;
    private String backendServerURL;
    private ConfigurationContext configContext;
    private Locale locale;

    public ProcessManagementServiceClient(
            String cookie,
            String backendServerURL,
            ConfigurationContext configContext, Locale locale) throws AxisFault {
        this.cookie = cookie;
        this.backendServerURL = backendServerURL;
        this.configContext = configContext;
        this.locale = locale;
//        bundle = ResourceBundle.getBundle(BUNDLE, locale);
        String serviceURL = backendServerURL + "ProcessManagementService";
        stub = new ProcessManagementServiceStub(configContext, serviceURL);
        ServiceClient client = stub._getServiceClient();
        Options option = client.getOptions();
        option.setManageSession(true);
        option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
    }

    public PaginatedProcessInfoList getPaginatedProcessList(String filter,
                                                            String orderByKeys,
                                                            int page)
            throws Exception {
        try {
            return stub.getPaginatedProcessList(filter, orderByKeys, page);
        } catch (Exception e) {
            log.error("getPaginatedProcessList operation failed.", e);
            throw e;
        }
    }

    public void activateProcess(QName pid) throws Exception {
        try {
            stub.activateProcess(pid);
        } catch (Exception e) {
            log.error("activateProcess operation failed", e);
            throw e;
        }
    }

    public ProcessInfoType getProcessInfo(QName pid)
            throws RemoteException, ProcessManagementException {
        try {
            return stub.getProcessInfo(pid);
        } catch (RemoteException e) {
            log.error("getProcessInfo operation failed", e);
            throw e;
        } catch (ProcessManagementException e) {
            log.error("getProcessInfo operation failed: " + e.getFaultMessage().getResult(), e);
            throw e;
        }
    }

    public void retireProcess(QName pid) throws Exception {
        try {
            stub.retireProcess(pid);
        } catch (Exception e) {
            log.error("retireProcess operation failed", e);
            throw e;
        }
    }

    public String[] getAllProcesses() throws Exception {
        try {
            return stub.getAllProcesses("ignore");
        } catch (Exception e) {
            log.error("getAllProcesses operation failed.", e);
            throw e;
        }
    }

    public ServiceMetaData getServiceMetaData(String serviceName) throws Exception {
        ServiceAdminClient client;
        try {
            client = new ServiceAdminClient(cookie, backendServerURL, configContext, locale);
            return client.getServiceData(serviceName);
        } catch (Exception e) {
            log.error("getServiceDate operation failed.", e);
            throw e;
        }
    }

    public ProcessDeployDetailsList_type0 getProcessDeploymentInfo(QName pid)
            throws Exception {
        try {
            return stub.getProcessDeploymentInfo(pid);
        } catch (Exception e) {
            log.error("getProcessDeploymentInfo operation failed", e);
            throw e;
        }
    }

    public void updateDeployInfo(ProcessDeployDetailsList_type0 processDeployDetailsList)
            throws Exception {
        try {
            stub.updateDeployInfo(processDeployDetailsList);
        } catch (Exception e) {
            log.error("updateDeployInfo operation failed", e);
            throw e;
        }
    }
}
