package org.wso2.carbon.bpmn.core.integration;

import org.activiti.engine.impl.interceptor.Session;
import org.activiti.engine.impl.interceptor.SessionFactory;
import org.activiti.engine.impl.persistence.entity.GroupIdentityManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bpmn.core.BPMNServerHolder;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.core.UserStoreManager;

public class BPSGroupManagerFactory implements SessionFactory {

    private static Log log = LogFactory.getLog(BPSGroupManagerFactory.class);

    @Override
    public Class<?> getSessionType() {
        return GroupIdentityManager.class;
    }

    @Override
    public Session openSession() {
        try {
            RegistryService registryService = BPMNServerHolder.getInstance().getRegistryService();
            Integer tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
            UserStoreManager userStoreManager = registryService.getUserRealm(tenantId).getUserStoreManager();
            BPSGroupIdentityManager bpsGroupIdentityManager = new BPSGroupIdentityManager(userStoreManager);
            return bpsGroupIdentityManager;
        } catch (Exception e) {
            String msg = "Failed to obtain a group identity manager.";
            log.error(msg, e);
            return null;
        }
    }
}
