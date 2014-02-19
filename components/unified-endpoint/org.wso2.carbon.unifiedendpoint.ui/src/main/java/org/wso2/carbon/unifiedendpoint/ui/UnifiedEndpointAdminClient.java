/*
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
package org.wso2.carbon.unifiedendpoint.ui;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.wso2.carbon.unifiedendpoint.core.UnifiedEndpoint;
import org.wso2.carbon.unifiedendpoint.core.UnifiedEndpointFactory;
import org.wso2.carbon.unifiedendpoint.stub.types.UnifiedEndpointAdminStub;


public class UnifiedEndpointAdminClient {

    private UnifiedEndpointAdminStub stub;


    public UnifiedEndpointAdminClient(String cookie,
                              String backendServerURL,
                              ConfigurationContext configCtx)throws AxisFault {
        String serviceURL = backendServerURL + "UnifiedEndpointAdmin";
        stub = new UnifiedEndpointAdminStub(configCtx, serviceURL);
        ServiceClient client = stub._getServiceClient();
        Options option = client.getOptions();
        option.setManageSession(true);
        option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);

    }

    public String saveUEP(String key, String ele) {
        String temp = "";
        try {
            temp = stub.saveUnifiedEP(key, ele);
        } catch (Exception e) {
        }

        return temp;
    }


    public String[] getAllEndpoints(int pageNumber, int endpointsPerPage) {
        String[] epList = null;
        try {
            epList = stub.getDynamicEndpoints(pageNumber, endpointsPerPage);
        } catch (Exception e) {

        }
        return epList;

    }

    public UnifiedEndpoint getEndpoint(String uepId) {

        UnifiedEndpoint unifiedEndpoint = null;
        String[] uepArray = null;
        try {
            /*ToDo : Fix this... Need a new service method here*/
            uepArray = stub.getDynamicEndpoints(10, 10);
            for (String uepStr : uepArray) {
                if (uepStr != null) {
                    UnifiedEndpoint uep = new UnifiedEndpointFactory().createEndpoint(uepStr);
                    if (uep != null && uep.getUepId().equals(uepId)) {
                        unifiedEndpoint = uep;
                    }
                }
            }

        } catch (Exception e) {

        }

        return unifiedEndpoint;
    }


}
