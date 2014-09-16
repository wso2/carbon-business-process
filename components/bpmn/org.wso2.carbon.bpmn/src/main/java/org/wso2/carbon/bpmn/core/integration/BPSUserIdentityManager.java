/**
 *  Copyright (c) 2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 */
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
import org.wso2.carbon.stratos.common.beans.TenantInfoBean;
import org.wso2.carbon.tenant.mgt.services.TenantMgtAdminService;

import java.util.List;
import java.util.Map;

public class BPSUserIdentityManager extends UserEntityManager {

    private static Log log = LogFactory.getLog(BPSUserIdentityManager.class);
    private TenantMgtAdminService tenantMgtAdminService;

    public BPSUserIdentityManager() {
        this.tenantMgtAdminService = new TenantMgtAdminService();
    }

    @Override
    public User createNewUser(String userId) {
        String msg = "Invoked UserIdentityManager method is not implemented in BPSUserIdentityManager.";
        throw new UnsupportedOperationException(msg);
    }

    @Override
    public void insertUser(User user) {
        String msg = "Invoked UserIdentityManager method is not implemented in BPSUserIdentityManager.";
        throw new UnsupportedOperationException(msg);
    }

    @Override
    public UserEntity findUserById(String userId) {
        String msg = "Invoked UserIdentityManager method is not implemented in BPSUserIdentityManager.";
        throw new UnsupportedOperationException(msg);
    }

    @Override
    public void deleteUser(String userId) {
        String msg = "Invoked UserIdentityManager method is not implemented in BPSUserIdentityManager.";
        throw new UnsupportedOperationException(msg);
    }

    @Override
    public List<User> findUserByQueryCriteria(UserQueryImpl userQuery, Page page) {
        String msg = "Invoked UserIdentityManager method is not implemented in BPSUserIdentityManager.";
        throw new UnsupportedOperationException(msg);
    }

    @Override
    public long findUserCountByQueryCriteria(UserQueryImpl userQuery) {
        String msg = "Invoked UserIdentityManager method is not implemented in BPSUserIdentityManager.";
        throw new UnsupportedOperationException(msg);
    }

    @Override
    public List<Group> findGroupsByUser(String userId) {
        String msg = "Invoked UserIdentityManager method is not implemented in BPSUserIdentityManager.";
        throw new UnsupportedOperationException(msg);
    }

    @Override
    public UserQuery createNewUserQuery() {
        String msg = "Invoked UserIdentityManager method is not implemented in BPSUserIdentityManager.";
        throw new UnsupportedOperationException(msg);
    }

    @Override
    public IdentityInfoEntity findUserInfoByUserIdAndKey(String userId, String key) {
        String msg = "Invoked UserIdentityManager method is not implemented in BPSUserIdentityManager.";
        throw new UnsupportedOperationException(msg);
    }

    @Override
    public List<String> findUserInfoKeysByUserIdAndType(String userId, String type) {
        String msg = "Invoked UserIdentityManager method is not implemented in BPSUserIdentityManager.";
        throw new UnsupportedOperationException(msg);
    }

    @Override
    public Boolean checkPassword(String userId, String password) {
        try {
            //TenantManagement Service will be used to get the domain of user who sent service request.
            String[] userNameTokens = userId.split("@");
            int tenantId = BPMNConstants.SUPER_TENANT_ID;
            if (userNameTokens.length > 1) {
                TenantInfoBean tenantInfoBean = tenantMgtAdminService.getTenant(userNameTokens[userNameTokens.length - 1]);
                if (tenantInfoBean != null) {
                    tenantId = tenantInfoBean.getTenantId();
                } else {
                    throw new Exception("Could not find tenant on given tenant id");
                }
            }
            if (log.isDebugEnabled()) {
                log.debug("Rest Service request from user:" + userId);
            }
            return BPMNServerHolder.getInstance().getRegistryService().getUserRealm(tenantId).getUserStoreManager().authenticate(userNameTokens[0], password);
        } catch (Exception e) {
            String msg = "Error in authenticating user: " + userId;
            log.error(msg, e);
        }
        return false;
    }

    @Override
    public List<User> findPotentialStarterUsers(String proceDefId) {
        String msg = "Invoked UserIdentityManager method is not implemented in BPSUserIdentityManager.";
        throw new UnsupportedOperationException(msg);
    }

    @Override
    public List<User> findUsersByNativeQuery(Map<String, Object> parameterMap, int firstResult,
                                             int maxResults) {
        String msg = "Invoked UserIdentityManager method is not implemented in BPSUserIdentityManager.";
        throw new UnsupportedOperationException(msg);
    }

    @Override
    public long findUserCountByNativeQuery(Map<String, Object> parameterMap) {
        String msg = "Invoked UserIdentityManager method is not implemented in BPSUserIdentityManager.";
        throw new UnsupportedOperationException(msg);
    }
}
