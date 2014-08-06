/*
 * Copyright (c), WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.humantask.core.integration.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.OutOnlyAxisOperation;
import org.apache.axis2.description.OutInAxisOperation;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.AxisFault;
import org.apache.axis2.client.ServiceClient;

import javax.xml.namespace.QName;

/**
 * This utility class is inspired by Apache Synapse AnonymousServiceFactory.
 *
 * Responsible for creating AxisServices to send out messages
 */

public final class AnonymousServiceFactory {
    private static Log log = LogFactory.getLog(AnonymousServiceFactory.class);

    private AnonymousServiceFactory() {
    }

    /**
     * Create AxisService for the requested endpoint config to sending out messages to external services.
     *
     *
     * @param serviceName    External service QName
     * @param servicePort    service port
     * @param axisConfig     AxisConfiguration object
     * @param caller Identifier for the service caller
     * @return AxisService
     */
    public static AxisService getAnonymousService(QName serviceName, String servicePort,
                                                  AxisConfiguration axisConfig, String caller) {
        String serviceKey = "axis_service_for_" + caller + "#" + serviceName.getLocalPart() + "#" +
                servicePort;

        try {
            AxisService service = axisConfig.getService(serviceKey);
            if (service == null) {
                synchronized (AnonymousServiceFactory.class) {
                    /* Fix for bugs due to high concurrency. If there are number of service calls to same service from
                     * different process instances, two process instances will try to add same service to axis config.
                     */
                    service = axisConfig.getService(serviceKey);
                    if (service != null) {
                        return service;
                    }

                    service = createAnonymousService(axisConfig, serviceKey);
                }
            }
            return service;
        } catch (AxisFault axisFault) {
            handleException("Error retrieving service for key " + serviceKey, axisFault);
        }
        return null;
    }

    private static void handleException(String msg, Exception e) {
        log.error(msg, e);
        throw new RuntimeException(msg, e);
    }

    private static AxisService createAnonymousService(AxisConfiguration axisCfg, String serviceKey) {
        try {
            OutOnlyAxisOperation outOnlyOperation = new OutOnlyAxisOperation(ServiceClient.ANON_OUT_ONLY_OP);
            OutInAxisOperation outInOperation = new OutInAxisOperation(ServiceClient.ANON_OUT_IN_OP);

            AxisService axisAnonymousService = new AxisService(serviceKey);
            axisAnonymousService.addOperation(outOnlyOperation);
            axisAnonymousService.addOperation(outInOperation);

            // set a right default action *after* operations have been added to the service.
            outOnlyOperation.setSoapAction("");
            outInOperation.setSoapAction("");

            if (log.isDebugEnabled()) {
                log.debug("Creating Client Service: " + serviceKey);
            }
            axisAnonymousService.setClientSide(true);

            axisCfg.addService(axisAnonymousService);

            return axisAnonymousService;
        } catch (AxisFault axisFault) {
            handleException("Adding service to axis configuration failed.", axisFault);
        }
        return null;
    }
}
