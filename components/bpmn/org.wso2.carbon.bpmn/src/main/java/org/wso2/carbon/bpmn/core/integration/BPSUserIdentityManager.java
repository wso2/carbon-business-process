///*
//*
// *  Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
// *
// *  Licensed under the Apache License, Version 2.0 (the "License");
// *  you may not use this file except in compliance with the License.
// *  You may obtain a copy of the License at
// *
// *        http://www.apache.org/licenses/LICENSE-2.0
// *
// *  Unless required by applicable law or agreed to in writing, software
// *  distributed under the License is distributed on an "AS IS" BASIS,
// *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// *  See the License for the specific language governing permissions and
// *  limitations under the License.
//*/
//
//
//package org.wso2.carbon.bpmn.core.integration;
//
//import org.activiti.engine.ActivitiObjectNotFoundException;
//import org.activiti.engine.identity.Group;
//import org.activiti.engine.identity.User;
//import org.activiti.engine.identity.UserQuery;
//import org.activiti.engine.impl.Page;
//import org.activiti.engine.impl.UserQueryImpl;
//import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
//import org.activiti.engine.impl.persistence.entity.GroupEntity;
//import org.activiti.engine.impl.persistence.entity.IdentityInfoEntity;
//import org.activiti.engine.impl.persistence.entity.UserEntity;
//import org.activiti.engine.impl.persistence.entity.UserEntityManager;
//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;
//import org.wso2.carbon.bpmn.core.BPMNConstants;
//import org.wso2.carbon.bpmn.core.BPMNServerHolder;
//import org.wso2.carbon.bpmn.core.exception.BPMNAuthenticationException;
//import org.wso2.carbon.context.CarbonContext;
//import org.wso2.carbon.registry.core.config.RegistryContext;
//import org.wso2.carbon.registry.core.exceptions.RegistryException;
//import org.wso2.carbon.registry.core.service.RegistryService;
//import org.wso2.carbon.security.caas.internal.CarbonSecurityDataHolder;
//import org.wso2.carbon.security.caas.user.core.context.AuthenticationContext;
//import org.wso2.carbon.stratos.common.beans.TenantInfoBean;
//import org.wso2.carbon.tenant.mgt.services.TenantMgtAdminService;
//import org.wso2.carbon.user.api.UserStoreException;
//import org.wso2.carbon.user.core.UserStoreManager;
//import org.wso2.carbon.user.core.claim.Claim;
//import org.wso2.carbon.user.core.service.RealmService;
//import org.wso2.carbon.user.core.tenant.TenantManager;
//import org.wso2.carbon.user.mgt.UserAdmin;
//import org.wso2.carbon.utils.multitenancy.MultitenantUtils;
//
//import org.wso2.carbon.security.caas.user.core.bean.User;
//
//import javax.security.auth.callback.Callback;
//import javax.security.auth.callback.NameCallback;
//import javax.security.auth.callback.PasswordCallback;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//
//public class BPSUserIdentityManager extends UserEntityManager {
//
//    private static Log log = LogFactory.getLog(BPSUserIdentityManager.class);
//    private TenantMgtAdminService tenantMgtAdminService;
//    private UserAdmin userAdmin;
//    private RegistryService registryService;
//
//    //list of Claim URIs
//    private static final String ID_CLAIM_URI = "urn:scim:schemas:core:1.0:id";
//    private static final String FIRST_NAME_CLAIM_URI = "http://axschema.org/namePerson/first";
//    private static final String LAST_NAME_CLAIM_URI = "http://wso2.org/claims/lastname";
//    private static final String FULL_NAME_CLAIM_URI = "http://wso2.org/claims/fullname";
//    private static final String EMAIL_CLAIM_URI = "http://wso2.org/claims/emailaddress";
//    private static final String ROLE_CLAIM_URI = "http://wso2.org/claims/role";
//
//
//
//    public BPSUserIdentityManager() {
//        this.tenantMgtAdminService = new TenantMgtAdminService();
//        this.userAdmin = new UserAdmin();
//        this.registryService = BPMNServerHolder.getInstance().getRegistryService();
//    }
//
//    @Override
//    public User createNewUser(String userId) {
//        String msg = "Invoked UserIdentityManager method is not implemented in BPSUserIdentityManager.";
//        throw new UnsupportedOperationException(msg);
//    }
//
//    @Override
//    public void insertUser(User user) {
//        String msg = "Invoked UserIdentityManager method is not implemented in BPSUserIdentityManager.";
//        throw new UnsupportedOperationException(msg);
//    }
//
//    @Override
//    public UserEntity findUserById(String userId) {
//        try {
//            UserStoreManager userStoreManager = registryService.getUserRealm(getTenantIdFromUserId(userId)).getUserStoreManager();
//
//            if (userStoreManager.isExistingUser(userId)) {
//                UserEntity userEntity = new UserEntity(userId);
//
//                String firstName = userStoreManager.getUserClaimValue(userId, FIRST_NAME_CLAIM_URI, null);
//                userEntity.setFirstName(firstName);
//
//                String lastName = userStoreManager.getUserClaimValue(userId, LAST_NAME_CLAIM_URI, null);
//                userEntity.setLastName(lastName);
//
//                String email = userStoreManager.getUserClaimValue(userId, EMAIL_CLAIM_URI, null);
//                userEntity.setEmail(email);
//
//                return userEntity;
//            } else {
//                log.error("No user exist with userId:" + userId);
//                return null;
//            }
//
//        } catch (Exception e) {
//            log.error("Error retrieving user info by id for: " + userId, e);
//            return null;
//        }
//    }
//
//    @Override
//    public void deleteUser(String userId) {
//        String msg = "Invoked UserIdentityManager method is not implemented in BPSUserIdentityManager.";
//        throw new UnsupportedOperationException(msg);
//    }
//
//    @Override
//    public List<User> findUserByQueryCriteria(UserQueryImpl userQuery, Page page) {
//
//        //get current tenant id
//        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
//        try {
//            List<Claim> claimList = transformQueryToClaim(userQuery);
//            if (claimList.size() > 0) {
//                //todo: need to add support to search by query
//                String msg = "Invoked UserIdentityManager method is not implemented in BPSUserIdentityManager.";
//                throw new UnsupportedOperationException(msg);
//            } else {
//                //return all users
//                String[] userList = registryService.getUserRealm(tenantId).getUserStoreManager().listUsers("*", -1);
//                return pageUserList(page, userList, tenantId);
//            }
//
//        } catch (org.wso2.carbon.user.core.UserStoreException e) {
//            log.error("error getting user list", e);
//            return new ArrayList<>();
//        } catch (RegistryException e) {
//            log.error("error getting user list", e);
//            return new ArrayList<>();
//        }
//    }
//
//    private List<User> generateFinalUserList(List<String[]> resultUserList) {
//        List<String> mergedList = new ArrayList<>();
//        //first result list is considered as merged list first
//        for (String user : resultUserList.get(0)) {
//            mergedList.add(user);
//        }
//        for (int i = 1; i < resultUserList.size(); i++) {
//            List<String> newList = new ArrayList<>();
//            for (String user : resultUserList.get(i)) {
//                if (mergedList.contains(user)) {
//                    newList.add(user);
//                }
//            }
//            //make new list the merged list
//            mergedList = newList;
//        }
//
//        List<User> result = new ArrayList<>();
//        //prepare User list
//        for (String userName : mergedList) {
//            result.add(new UserEntity(userName));
//        }
//        return result;
//    }
//
//    private List<User> pageUserList(Page page, String[] users, int tenantId)
//            throws RegistryException, org.wso2.carbon.user.core.UserStoreException {
//        List<User> userList = new ArrayList<>();
//        int resultLength = users.length;
//        int max;
//        if (page != null) {
//            if (page.getFirstResult() > resultLength) {
//                //no more result left, sending empty list
//                return new ArrayList<>();
//            }
//
//            if (page.getMaxResults() > resultLength) {
//                max = resultLength;
//            } else {
//                max = page.getMaxResults();
//            }
//            for (int i = page.getFirstResult(); i < max; i++) {
//                userList.add(new UserEntity(users[i]));
//            }
//        } else {
//            for (int i = 0; i < resultLength; i++) {
//                userList.add(new UserEntity(users[i]));
//            }
//        }
//
//        return userList;
//    }
//
//    private List<Claim> transformQueryToClaim(UserQueryImpl userQuery) {
//        List<Claim> claimList = new ArrayList<Claim>();
//
//        if (userQuery.getEmail() != null) {
//            Claim claim = new Claim();
//            claim.setClaimUri(EMAIL_CLAIM_URI);
//            claim.setValue(userQuery.getEmail());
//            claimList.add(claim);
//        }
//
//        if (userQuery.getEmailLike() != null) {
//            Claim claim = new Claim();
//            claim.setClaimUri(EMAIL_CLAIM_URI);
//            claim.setValue("*" + userQuery.getEmailLike() + "*");
//            claimList.add(claim);
//        }
//
//        if (userQuery.getFirstName() != null) {
//            Claim claim = new Claim();
//            claim.setClaimUri(FIRST_NAME_CLAIM_URI);
//            claim.setValue(userQuery.getFirstName());
//            claimList.add(claim);
//        }
//
//        if (userQuery.getFirstNameLike() != null) {
//            Claim claim = new Claim();
//            claim.setClaimUri(FIRST_NAME_CLAIM_URI);
//            claim.setValue("*" + userQuery.getFirstNameLike() + "*");
//            claimList.add(claim);
//        }
//
//        if (userQuery.getFullNameLike() != null) {
//            Claim claim = new Claim();
//            claim.setClaimUri(FULL_NAME_CLAIM_URI);
//            claim.setValue("*" + userQuery.getFullNameLike() + "*");
//            claimList.add(claim);
//        }
//
//        if (userQuery.getGroupId() != null) {
//            Claim claim = new Claim();
//            claim.setClaimUri(ROLE_CLAIM_URI);
//            claim.setValue(userQuery.getGroupId());
//            claimList.add(claim);
//        }
//
//        if (userQuery.getId() != null) {
//            Claim claim = new Claim();
//            claim.setClaimUri(ID_CLAIM_URI);
//            claim.setValue(userQuery.getId());
//            claimList.add(claim);
//        }
//
//        if (userQuery.getLastName() != null) {
//            Claim claim = new Claim();
//            claim.setClaimUri(LAST_NAME_CLAIM_URI);
//            claim.setValue(userQuery.getLastName());
//            claimList.add(claim);
//        }
//
//        if (userQuery.getLastNameLike() != null) {
//            Claim claim = new Claim();
//            claim.setClaimUri(LAST_NAME_CLAIM_URI);
//            claim.setValue("*" + userQuery.getLastNameLike() + "*");
//            claimList.add(claim);
//        }
//
//        return claimList;
//    }
//
//
//    @Override
//    public long findUserCountByQueryCriteria(UserQueryImpl userQuery) {
//        return findUserByQueryCriteria(userQuery, null).size();
//    }
//
//    @Override
//    public List<Group> findGroupsByUser(String userId) {
//
//        List<Group> groups = new ArrayList<Group>();
//        try {
//            String[] userNameTokens = userId.split("@");
//            int tenantId = BPMNConstants.SUPER_TENANT_ID;
//            if (userNameTokens.length > 1) {
//                TenantInfoBean tenantInfoBean = tenantMgtAdminService
//                        .getTenant(userNameTokens[userNameTokens.length - 1]);
//                if (tenantInfoBean != null) {
//                    tenantId = tenantInfoBean.getTenantId();
//                } else {
//                    log.error("Could not retrieve tenant ID for tenant domain : " + userNameTokens[userNameTokens.length
//                            - 1]);
//                    return new ArrayList<Group>();
//                }
//            }
//
//            String[] roles = registryService.getUserRealm(tenantId).getUserStoreManager().getRoleListOfUser(userId);
//            for (String role : roles) {
//                Group group = new GroupEntity(role);
//                groups.add(group);
//            }
//        } catch (UserStoreException e) {
//            String msg = "Failed to get roles of the user: " + userId + ". Returning an empty roles list.";
//            log.error(msg, e);
//        }catch (Exception e) {
//            log.error("error retrieving user tenant info", e);
//        }
//
//        return groups;
//    }
//
//    @Override
//    public UserQuery createNewUserQuery() {
//        return new UserQueryImpl(((ProcessEngineConfigurationImpl)BPMNServerHolder.getInstance().getEngine().getProcessEngineConfiguration()).getCommandExecutor());
//    }
//
//    @Override
//    public IdentityInfoEntity findUserInfoByUserIdAndKey(String userId, String key) {
//        String msg = "Invoked UserIdentityManager method is not implemented in BPSUserIdentityManager.";
//        throw new UnsupportedOperationException(msg);
//    }
//
//    @Override
//    public List<String> findUserInfoKeysByUserIdAndType(String userId, String type) {
//        String msg = "Invoked UserIdentityManager method is not implemented in BPSUserIdentityManager.";
//        throw new UnsupportedOperationException(msg);
//    }
//
//    @Override
//    public Boolean checkPassword(String userId, String password) {
//        String userName = getUserNameForGivenUserId(userId);
//        NameCallback usernameCallback = new NameCallback("username");
//        PasswordCallback passwordCallback = new PasswordCallback("password", false);
//        usernameCallback.setName(userName);
//        passwordCallback.setPassword(password.toCharArray());
//        Callback[] callbacks = {usernameCallback, passwordCallback};
//        try {
//            //Authentication
//            AuthenticationContext authenticationContext = CarbonSecurityDataHolder.getInstance().getCarbonRealmService()
//                    .getCredentialStore().authenticate(callbacks);
//           org.wso2.carbon.security.caas.user.core.bean.User user = authenticationContext.getUser();
//            //Authorization
//            user.isAuthorized(new Permission(carbonPermission.getName(), carbonPermission.getActions()));
//        } catch (AuthenticationFailure authenticationFailure) {
//            throw new LoginException("Authentication failure.");
//        }
//
//
//
//
//        org.wso2.carbon.user.api.UserStoreManager userStoreManager = null;
//        boolean authStatus = false;
//
//        try {
//            userStoreManager = realmService.getTenantUserRealm(tenantId).getUserStoreManager();
//            authStatus = userStoreManager.authenticate(tenantAwareUserName, password);
//        } catch (UserStoreException e) {
//            throw new BPMNAuthenticationException(
//                    "User store exception thrown while authenticating user : " + userNameWithTenantDomain, e);
//        }
//
//       /* IdentityService identityService = BPMNOSGIService.getIdentityService();
//        authStatus = identityService.checkPassword(userName, password);*/
//        if (log.isDebugEnabled()) {
//            log.debug("Basic authentication request completed. " +
//                    "Username : " + userNameWithTenantDomain +
//                    ", Authentication State : " + authStatus);
//        }
//
//        return authStatus;
//
//    }
//    todo: get matching username for userid
//	private String getUserNameForGivenUserId(String userId){
//		String userName = "";
//		List<org.wso2.carbon.security.caas.user.core.bean.User> Users = identityStore.listUsers("*",-1,-1);
//		for(org.wso2.carbon.security.caas.user.core.bean.User u : Users){
//			if(u.getUserId().equals(userId)){
//				userName = u.getUserName();
//				return userName;
//			}
//		}
//
//	}
//
////    private int getTenantIdFromUserId(String userId) throws Exception {
////        String[] userNameTokens = userId.split("@");
////        int tenantId = BPMNConstants.SUPER_TENANT_ID;
////        if (userNameTokens.length > 1) {
////            TenantInfoBean tenantInfoBean = tenantMgtAdminService.getTenant(userNameTokens[userNameTokens.length - 1]);
////            if (tenantInfoBean != null) {
////                tenantId = tenantInfoBean.getTenantId();
////            } else {
////                throw new Exception("Error retrieving tenant id from userId :" + userId);
////            }
////        }
////
////        return tenantId;
////    }
//
//    @Override
//    public List<User> findPotentialStarterUsers(String processDefId) {
//        String msg = "Invoked UserIdentityManager method is not implemented in BPSUserIdentityManager.";
//        throw new UnsupportedOperationException(msg);
//    }
//
//    @Override
//    public List<User> findUsersByNativeQuery(Map<String, Object> parameterMap, int firstResult,
//                                             int maxResults) {
//        String msg = "Invoked UserIdentityManager method is not implemented in BPSUserIdentityManager.";
//        throw new UnsupportedOperationException(msg);
//    }
//
//    @Override
//    public long findUserCountByNativeQuery(Map<String, Object> parameterMap) {
//        String msg = "Invoked UserIdentityManager method is not implemented in BPSUserIdentityManager.";
//        throw new UnsupportedOperationException(msg);
//    }
//}