/*
 * Copyright (c) 2016 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.bpmn.core.integration;


import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.User;
import org.activiti.engine.identity.UserQuery;
import org.activiti.engine.impl.UserQueryImpl;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.persistence.entity.GroupEntity;
import org.activiti.engine.impl.persistence.entity.IdentityInfoEntity;
import org.activiti.engine.impl.persistence.entity.UserEntity;
import org.activiti.engine.impl.persistence.entity.UserEntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.bpmn.core.internal.BPMNServerHolder;
import org.wso2.carbon.security.caas.user.core.claim.Claim;
import org.wso2.carbon.security.caas.user.core.context.AuthenticationContext;
import org.wso2.carbon.security.caas.user.core.exception.AuthenticationFailure;
import org.wso2.carbon.security.caas.user.core.exception.ClaimManagerException;
import org.wso2.carbon.security.caas.user.core.exception.IdentityStoreException;
import org.wso2.carbon.security.caas.user.core.exception.UserNotFoundException;
import org.wso2.carbon.security.caas.user.core.store.CredentialStore;
import org.wso2.carbon.security.caas.user.core.store.IdentityStore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;


/**
 * CASS based User Identity Manager.
 */
public class BPSUserIdentityManager extends UserEntityManager {

    //list of Claim URIs
    private static final String ID_CLAIM_URI = "urn:scim:schemas:core:1.0:id";
    private static final String FIRST_NAME_CLAIM_URI = "http://wso2.org/claims/firstName";
    private static final String LAST_NAME_CLAIM_URI = "http://wso2.org/claims/lastName";
    private static final String FULL_NAME_CLAIM_URI = "http://wso2.org/claims/fullname";
    private static final String EMAIL_CLAIM_URI = "http://wso2.org/claims/email";
    private static final String ROLE_CLAIM_URI = "http://wso2.org/claims/role";

    private static Logger log = LoggerFactory.getLogger(BPSUserIdentityManager.class);

    private IdentityStore identityStore;
    private CredentialStore credentialStore;

    private List<String> claims = new ArrayList<String>();

    public BPSUserIdentityManager() {
        this.credentialStore = BPMNServerHolder.getInstance().getCarbonRealmService().getCredentialStore();
        this.identityStore = BPMNServerHolder.getInstance().getCarbonRealmService().getIdentityStore();
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
    public UserEntity findUserById(String userName) {
        try {
            org.wso2.carbon.security.caas.user.core.bean.User user = identityStore.getUser(userName);
            //if user exists
            if (user != null) {
                // create claim list
                claims.add(FIRST_NAME_CLAIM_URI);
                claims.add(LAST_NAME_CLAIM_URI);
                claims.add(EMAIL_CLAIM_URI);

                UserEntity userEntity = new UserEntity(userName);
                List<Claim> userClaimList = user.getClaims(claims);
                if (userClaimList != null) {
                    for (Claim claim : userClaimList) {
                        if (claim.getClaimURI().equals(FIRST_NAME_CLAIM_URI)) {
                            String firstName = claim.getValue();
                            userEntity.setFirstName(firstName);
                        }
                        if (claim.getClaimURI().equals(LAST_NAME_CLAIM_URI)) {
                            String lastName = claim.getValue();
                            userEntity.setLastName(lastName);
                        }
                        if (claim.getClaimURI().equals(EMAIL_CLAIM_URI)) {
                            String email = claim.getValue();
                            userEntity.setEmail(email);
                        }
                    }

                } else {
                    log.error("No claims found for user: " + user);
                }
                return userEntity;
            } else {
                log.error("No user exist with userId:" + userName);
                return null;
            }

        } catch (ClaimManagerException | IdentityStoreException | UserNotFoundException e) {
            log.error("Error retrieving user info by id for: " + userName, e);
            return null;
        }
    }

    @Override
    public void deleteUser(String userId) {
        String msg = "Invoked UserIdentityManager method is not implemented in BPSUserIdentityManager.";
        throw new UnsupportedOperationException(msg);
    }

//  @Override
    //   public List<User> findUserByQueryCriteria(UserQueryImpl userQuery, Page page) {
//
//todo:
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
//    private List<User> pageUserList(Page page, String[] users)
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
//        List<String,String> claimList = new ArrayList<String,String>();
//
//        if (userQuery.getEmail() != null) {
//             claimList.add
//
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


    @Override
    public long findUserCountByQueryCriteria(UserQueryImpl userQuery) {
        return findUserByQueryCriteria(userQuery, null).size();
    }

    @Override
    public List<Group> findGroupsByUser(String userName) {

        List<Group> groups = new ArrayList<Group>();
        try {
            if (!userName.isEmpty()) {
                org.wso2.carbon.security.caas.user.core.bean.User user = identityStore.getUser(userName);
                // updated according to c5 user-core:set of roles belongs to a group
                List<org.wso2.carbon.security.caas.user.core.bean.Group> groupsOfUser = identityStore
                        .getGroupsOfUser(user.getUserId(), user.getIdentityStoreId());
                if (groupsOfUser != null) {
                    GroupEntity groupEntity;
                    for (org.wso2.carbon.security.caas.user.core.bean.Group group : groupsOfUser) {
                        groupEntity = new GroupEntity(group.getGroupId());
                        groupEntity.setName(group.getName());
                        groups.add(groupEntity);
                    }
                }
            }
        } catch (IdentityStoreException e) {
            String msg = "Failed to get roles of the user: " + userName + ". Returning an empty roles list.";
            log.error(msg, e);
        } catch (Exception e) {
            log.error("error retrieving user tenant info", e);
        }

        return groups;
    }

    @Override
    public UserQuery createNewUserQuery() {
        return new UserQueryImpl(((ProcessEngineConfigurationImpl) BPMNServerHolder.getInstance()
                .getEngine().getProcessEngineConfiguration()).getCommandExecutor());
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
    public Boolean checkPassword(String userName, String password) {
        boolean authStatus = false;
        NameCallback usernameCallback = new NameCallback("username");
        PasswordCallback passwordCallback = new PasswordCallback("password", false);
        usernameCallback.setName(userName);
        passwordCallback.setPassword(password.toCharArray());
        Callback[] callbacks = {usernameCallback, passwordCallback};
        try {
            //Authentication
            AuthenticationContext authenticationContext = credentialStore.authenticate(callbacks);
            org.wso2.carbon.security.caas.user.core.bean.User user = authenticationContext.getUser();

            if (user != null) {
                authStatus = true;
            }
        } catch (AuthenticationFailure authenticationFailure) {
            String msg = "Authentication failure while authenticating user :" + userName;
            log.error(msg, authenticationFailure);
        }

        if (log.isDebugEnabled()) {
            log.debug("Basic authentication request completed. " +
                    "Username : " + userName +
                    ", Authentication State : " + authStatus);
        }
        return authStatus;
    }


    @Override
    public List<User> findPotentialStarterUsers(String processDefId) {
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
