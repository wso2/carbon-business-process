/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.businessprocesses.common.handler;

import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.handlers.AbstractHandler;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.utils.ServerConstants;


/**
 * This class will configure carbon context for threads that does not have carbon context information configured
 * and are dispatched with requests to
 */
public class CarbonContextConfigurator extends AbstractHandler {
    @Override
    public InvocationResponse invoke(MessageContext messageContext) throws AxisFault {
        PrivilegedCarbonContext cc = PrivilegedCarbonContext.getThreadLocalCarbonContext();

        // If this request is not dispatching to BPS published axis2 services, just return
        if (!isDispatchingToBPS(messageContext)) {
            return InvocationResponse.CONTINUE;
        }

        // If carbonContext is already populated with tenant domain and tenant id properly configured ,just return
        if (cc.getTenantDomain() != null && cc.getTenantId() != MultitenantConstants.INVALID_TENANT_ID) {
            return InvocationResponse.CONTINUE;
        }

        // If incoming transport is non http, assume super tenant domain and tenant id and configure
        if (messageContext.getTransportIn() != null && messageContext.getTransportIn().getName() != null &&
                !messageContext.getTransportIn().getName().contains("http")) {
            cc.setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
            cc.setTenantId(MultitenantConstants.SUPER_TENANT_ID);
            return InvocationResponse.CONTINUE;
        }

        try {
            EndpointReference epr = messageContext.getTo();
            if (epr != null) {
                String to = epr.getAddress();
                if (to != null && to.indexOf("/" + MultitenantConstants.TENANT_AWARE_URL_PREFIX + "/") != -1) {
                    String str1 = to.substring(to.indexOf("/t/") + 3);
                    String domain = str1.substring(0, str1.indexOf("/"));
                    cc.setTenantDomain(domain, true);
                } else {
                    cc.setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
                    cc.setTenantId(MultitenantConstants.SUPER_TENANT_ID);
                }
            }
        } catch (Throwable ignore) {
            //don't care if anything failed
        }
        return InvocationResponse.CONTINUE;
    }

    private Boolean isDispatchingToBPS(MessageContext messageContext) {
        AxisService axisService = messageContext.getAxisService();
        if (null != axisService) {
            Parameter parameter = axisService.getParameter(ServerConstants.SERVICE_TYPE);
            if (parameter != null && (parameter.getValue().toString().equalsIgnoreCase("bpel") ||
                    parameter.getValue().toString().equalsIgnoreCase("humantask"))) {
                return true;
            }

        }
        return false;
    }
}
