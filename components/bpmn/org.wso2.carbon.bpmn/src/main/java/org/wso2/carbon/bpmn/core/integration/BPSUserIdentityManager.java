package org.wso2.carbon.bpmn.core.integration;

import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.User;
import org.activiti.engine.identity.UserQuery;
import org.activiti.engine.impl.Page;
import org.activiti.engine.impl.UserQueryImpl;
import org.activiti.engine.impl.persistence.entity.IdentityInfoEntity;
import org.activiti.engine.impl.persistence.entity.UserEntity;
import org.activiti.engine.impl.persistence.entity.UserEntityManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bpmn.core.BPMNConstants;
import org.wso2.carbon.bpmn.core.BPMNServerHolder;
import org.wso2.carbon.tenant.mgt.services.TenantMgtAdminService;
import org.wso2.carbon.user.core.UserStoreException;

import java.util.List;
import java.util.Map;

public class BPSUserIdentityManager extends UserEntityManager {

    private static Log log = LogFactory.getLog(BPSUserIdentityManager.class);

    private TenantMgtAdminService tenantMgtAdminService;

    public BPSUserIdentityManager() {
        this.tenantMgtAdminService = new TenantMgtAdminService();
    }

    @Override
    public User createNewUser(String s) {
        String msg = "Invoked UserIdentityManager method is not implemented in BPSUserIdentityManager.";
        log.error(msg);
        throw new UnsupportedOperationException(msg);
    }

    @Override
    public void insertUser(User user) {
        String msg = "Invoked UserIdentityManager method is not implemented in BPSUserIdentityManager.";
        log.error(msg);
        throw new UnsupportedOperationException(msg);
    }

    @Override
    public void updateUser(UserEntity userEntity) {
        String msg = "Invoked UserIdentityManager method is not implemented in BPSUserIdentityManager.";
        log.error(msg);
        throw new UnsupportedOperationException(msg);
    }

    @Override
    public UserEntity findUserById(String s) {
        String msg = "Invoked UserIdentityManager method is not implemented in BPSUserIdentityManager.";
        log.error(msg);
        throw new UnsupportedOperationException(msg);
    }

    @Override
    public void deleteUser(String s) {
        String msg = "Invoked UserIdentityManager method is not implemented in BPSUserIdentityManager.";
        log.error(msg);
        throw new UnsupportedOperationException(msg);
    }

    @Override
    public List<User> findUserByQueryCriteria(UserQueryImpl userQuery, Page page) {
        String msg = "Invoked UserIdentityManager method is not implemented in BPSUserIdentityManager.";
        log.error(msg);
        throw new UnsupportedOperationException(msg);
    }

    @Override
    public long findUserCountByQueryCriteria(UserQueryImpl userQuery) {
        String msg = "Invoked UserIdentityManager method is not implemented in BPSUserIdentityManager.";
        log.error(msg);
        throw new UnsupportedOperationException(msg);
    }

    @Override
    public List<Group> findGroupsByUser(String s) {
        String msg = "Invoked UserIdentityManager method is not implemented in BPSUserIdentityManager.";
        log.error(msg);
        throw new UnsupportedOperationException(msg);
    }

    @Override
    public UserQuery createNewUserQuery() {
        String msg = "Invoked UserIdentityManager method is not implemented in BPSUserIdentityManager.";
        log.error(msg);
        throw new UnsupportedOperationException(msg);
    }

    @Override
    public IdentityInfoEntity findUserInfoByUserIdAndKey(String s, String s1) {
        String msg = "Invoked UserIdentityManager method is not implemented in BPSUserIdentityManager.";
        log.error(msg);
        throw new UnsupportedOperationException(msg);
    }

    @Override
    public List<String> findUserInfoKeysByUserIdAndType(String s, String s1) {
        String msg = "Invoked UserIdentityManager method is not implemented in BPSUserIdentityManager.";
        log.error(msg);
        throw new UnsupportedOperationException(msg);
    }

    @Override
    public Boolean checkPassword(String s, String s1) {
        boolean authenticated = false;
        try {
            /*
            TenantManagement Service will be used to get the domain of user who sent service request.
            */

            String[] userNameTokens = s.split("@");
            int tenantId = BPMNConstants.SUPER_TENANT_ID;

            if (userNameTokens.length > 1) {
                tenantId = tenantMgtAdminService.getTenant(userNameTokens[userNameTokens.length - 1]).getTenantId();
            }

            log.debug("Rest Service request from user:" + s);
            return BPMNServerHolder.getInstance().getRegistryService().getUserRealm(tenantId).getUserStoreManager().authenticate(userNameTokens[0], s1);

        } catch (UserStoreException e) {
            String msg = "Error in authenticating user: " + s;
            log.error(msg, e);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return authenticated;
    }

    @Override
    public List<User> findPotentialStarterUsers(String s) {
        String msg = "Invoked UserIdentityManager method is not implemented in BPSUserIdentityManager.";
        log.error(msg);
        throw new UnsupportedOperationException(msg);
    }

    @Override
    public List<User> findUsersByNativeQuery(Map<String, Object> stringObjectMap, int i, int i1) {
        String msg = "Invoked UserIdentityManager method is not implemented in BPSUserIdentityManager.";
        log.error(msg);
        throw new UnsupportedOperationException(msg);
    }

    @Override
    public long findUserCountByNativeQuery(Map<String, Object> stringObjectMap) {
        String msg = "Invoked UserIdentityManager method is not implemented in BPSUserIdentityManager.";
        log.error(msg);
        throw new UnsupportedOperationException(msg);
    }
}
