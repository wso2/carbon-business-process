package org.wso2.carbon.bpmn.core.integration;

import org.activiti.engine.impl.interceptor.Session;
import org.activiti.engine.impl.interceptor.SessionFactory;
import org.activiti.engine.impl.persistence.entity.UserIdentityManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bpmn.core.BPMNServerHolder;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.core.UserStoreManager;

public class BPSUserManagerFactory implements SessionFactory {

    private static Log log = LogFactory.getLog(BPSUserManagerFactory.class);

    @Override
    public Class<?> getSessionType() {
        return UserIdentityManager.class;
    }

    @Override
    public Session openSession() {
        try {
            RegistryService registryService = BPMNServerHolder.getInstance().getRegistryService();
            Integer tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
            UserStoreManager userStoreManager = registryService.getUserRealm(tenantId).getUserStoreManager();
            BPSUserIdentityManager bpsUserIdentityManager = new BPSUserIdentityManager(userStoreManager);
            return bpsUserIdentityManager;
        } catch (Exception e) {
            String msg = "Failed to obtain an user identity manager.";
            log.error(msg, e);
            return null;
        }
    }
}
