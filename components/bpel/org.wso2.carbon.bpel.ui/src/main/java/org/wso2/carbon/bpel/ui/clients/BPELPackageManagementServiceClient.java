/*
 * Copyright (c) 2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.bpel.ui.clients;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bpel.stub.mgt.BPELPackageManagementServiceStub;
import org.wso2.carbon.bpel.stub.mgt.PackageManagementException;
import org.wso2.carbon.bpel.stub.mgt.types.DeployedPackagesPaginated;
import org.wso2.carbon.bpel.stub.mgt.types.PackageType;
import org.wso2.carbon.bpel.stub.mgt.types.UndeployStatus_type0;

import java.rmi.RemoteException;

/**
 * BPEL Package Management Service Client is wrapper class for BPEL Package Management stub.
 */
public class BPELPackageManagementServiceClient {
    private static Log log = LogFactory.getLog(BPELPackageManagementServiceClient.class);
    private BPELPackageManagementServiceStub stub;

    public BPELPackageManagementServiceClient(
            String cookie,
            String backendServerURL,
            ConfigurationContext configContext) throws AxisFault {
        String serviceURL = backendServerURL + "BPELPackageManagementService";
        stub = new BPELPackageManagementServiceStub(configContext, serviceURL);
        ServiceClient client = stub._getServiceClient();
        Options options = client.getOptions();
        options.setManageSession(true);
        options.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
    }

    public DeployedPackagesPaginated getPaginatedPackageList(int page, String packageSearchString)
            throws PackageManagementException, RemoteException {
        try {
            return stub.listDeployedPackagesPaginated(page, packageSearchString);
        } catch (RemoteException e) {
            log.error("listDeployedPackagesPaginated operation invocation failed.", e);
            throw e;
        } catch (PackageManagementException e) {
            log.error("listDeployedPackagesPaginated operation failed.", e);
            throw e;
        }
    }

    public UndeployStatus_type0 undeploy(String packageName)
            throws PackageManagementException, RemoteException {
        try {
            return stub.undeployBPELPackage(packageName);
        } catch (RemoteException e) {
            log.error("undeployBPELPackage operation invocation failed.", e);
            throw e;
        } catch (PackageManagementException e) {
            log.error("undeployBPELPackage operation failed.", e);
            throw e;
        }
    }

    public PackageType listProcessesInPackage(String packageName)
            throws PackageManagementException, RemoteException {
        try {
            return stub.listProcessesInPackage(packageName);
        } catch (RemoteException e) {
            log.error("listProcessesInPackage operation invocation failed.", e);
            throw e;
        } catch (PackageManagementException e) {
            log.error("listProcessesInPackage operation invocation failed.", e);
            throw e;
        }

    }
}
