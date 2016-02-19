/**
 *  Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *//*


package org.wso2.carbon.bpmn.core.integration;

import org.camunda.bpm.engine.impl.interceptor.Session;
import org.camunda.bpm.engine.impl.interceptor.SessionFactory;
import org.camunda.bpm.engine.impl.persistence.entity.GroupIdentityManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
*/
/*import org.wso2.carbon.bpmn.core.BPMNServerHolder;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.core.UserStoreManager;*//*


public class BPSGroupManagerFactory implements SessionFactory {

    private static Log log = LogFactory.getLog(BPSGroupManagerFactory.class);

    @Override
    public Class<?> getSessionType() {
        return GroupIdentityManager.class;
    }

    @Override
    public Session openSession() {
        try {
            */
/*RegistryService registryService = BPMNServerHolder.getInstance().getRegistryService();
            Integer tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
            UserStoreManager userStoreManager = registryService.getUserRealm(tenantId).getUserStoreManager();
            BPSGroupIdentityManager bpsGroupIdentityManager = new BPSGroupIdentityManager(userStoreManager);
            *//*

            BPSGroupIdentityManager bpsGroupIdentityManager = new BPSGroupIdentityManager();
            return bpsGroupIdentityManager;
        } catch (Exception e) {
            String msg = "Failed to obtain a group identity manager.";
            log.error(msg, e);
            return null;
        }
    }
}
*/
