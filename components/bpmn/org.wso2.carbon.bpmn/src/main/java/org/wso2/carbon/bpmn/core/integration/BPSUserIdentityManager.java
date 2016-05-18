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

//import org.activiti.engine.ActivitiObjectNotFoundException;

import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.User;
import org.activiti.engine.identity.UserQuery;
//import org.activiti.engine.impl.Page;
import org.activiti.engine.impl.UserQueryImpl;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.persistence.entity.GroupEntity;
import org.activiti.engine.impl.persistence.entity.IdentityInfoEntity;
import org.activiti.engine.impl.persistence.entity.UserEntity;
import org.activiti.engine.impl.persistence.entity.UserEntityManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bpmn.core.BPMNServerHolder;
import org.wso2.carbon.bpmn.core.internal.IdentityDataHolder;
import org.wso2.carbon.security.caas.user.core.bean.Role;
import org.wso2.carbon.security.caas.user.core.context.AuthenticationContext;
import org.wso2.carbon.security.caas.user.core.exception.AuthenticationFailure;
import org.wso2.carbon.security.caas.user.core.exception.IdentityStoreException;
import org.wso2.carbon.security.caas.user.core.exception.UserNotFoundException;
import org.wso2.carbon.security.caas.user.core.store.AuthorizationStore;
import org.wso2.carbon.security.caas.user.core.store.CredentialStore;
import org.wso2.carbon.security.caas.user.core.store.IdentityStore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;


/**
 *
 */
public class BPSUserIdentityManager extends UserEntityManager {

    private static Log log = LogFactory.getLog(BPSUserIdentityManager.class);
    private IdentityStore identityStore;
    private CredentialStore credentialStore;
    private AuthorizationStore authorizationStore;

    //list of Claim URIs
    private static final String ID_CLAIM_URI = "urn:scim:schemas:core:1.0:id";
    private static final String FIRST_NAME_CLAIM_URI = "http://axschema.org/namePerson/first";
    private static final String LAST_NAME_CLAIM_URI = "http://wso2.org/claims/lastname";
    private static final String FULL_NAME_CLAIM_URI = "http://wso2.org/claims/fullname";
    private static final String EMAIL_CLAIM_URI = "http://wso2.org/claims/emailaddress";
    private static final String ROLE_CLAIM_URI = "http://wso2.org/claims/role";

    private List<String> claims = new ArrayList<String>();

    public BPSUserIdentityManager() {

        this.credentialStore = IdentityDataHolder.getInstance().getCarbonRealmService().getCredentialStore();
        this.identityStore = IdentityDataHolder.getInstance().getCarbonRealmService().getIdentityStore();
        this.authorizationStore = IdentityDataHolder.getInstance().getCarbonRealmService().getAuthorizationStore();

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
        try {
            String userName = getUserNameForGivenUserId(userId);
            org.wso2.carbon.security.caas.user.core.bean.User user = identityStore.getUser(userName);
            //if user exists
            if (user != null) {
                // create claim list
                claims.add(FIRST_NAME_CLAIM_URI);
                claims.add(LAST_NAME_CLAIM_URI);
                claims.add(EMAIL_CLAIM_URI);

                UserEntity userEntity = new UserEntity(userId);
                Map<String, String> userClaimList = user.getClaims(claims);
                if (userClaimList.containsKey(FIRST_NAME_CLAIM_URI)) {
                    String firstName = userClaimList.get(FIRST_NAME_CLAIM_URI);
                    userEntity.setFirstName(firstName);
                }
                if (userClaimList.containsKey(LAST_NAME_CLAIM_URI)) {
                    String lastName = userClaimList.get(LAST_NAME_CLAIM_URI);
                    userEntity.setLastName(lastName);
                }
                if (userClaimList.containsKey(EMAIL_CLAIM_URI)) {
                    String email = userClaimList.get(EMAIL_CLAIM_URI);
                    userEntity.setEmail(email);
                }

                return userEntity;
            } else {
                log.error("No user exist with userId:" + userId);
                return null;
            }

        } catch (IdentityStoreException | UserNotFoundException e) {
            log.error("Error retrieving user info by id for: " + userId, e);
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
    public List<Group> findGroupsByUser(String userId) {

        List<Group> groups = new ArrayList<Group>();
        try {
            String userName = getUserNameForGivenUserId(userId);
            if (!userName.isEmpty()) {
                List<Role> roles = authorizationStore.getRolesOfUser(userId, identityStore.getUser(userName).
                        getIdentityStoreId());
                groups = roles.stream().map(role -> new GroupEntity(role.getRoleId())).collect(Collectors.toList());
//                for (Role role : roles) {
//                    Group group = new GroupEntity(role.getRoleId());
//                    groups.add(group);
//                }
            }
        } catch (IdentityStoreException e) {
            String msg = "Failed to get roles of the user: " + userId + ". Returning an empty roles list.";
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
    public Boolean checkPassword(String userId, String password) {
        boolean authStatus = false;
        String userName = getUserNameForGivenUserId(userId);
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


    // todo: get matching username for userid
    private String getUserNameForGivenUserId(String userId) {
        String userName = "";
        try { //todo: need to set length to -1
            List<org.wso2.carbon.security.caas.user.core.bean.User> users = identityStore.listUsers("%", 0, 10);
            if (!users.isEmpty()) {
                Optional<org.wso2.carbon.security.caas.user.core.bean.User> matchingObjects = users.stream().
                        filter(u -> u.getUserId().equals(userId)).
                        findFirst();
                if (matchingObjects.isPresent()) {
                    org.wso2.carbon.security.caas.user.core.bean.User filteredUser = matchingObjects.get();
                    userName = filteredUser.getUserName();
                } else {
                    log.info("No matching user found for userId: " + userId);
                }

            }

        } catch (IdentityStoreException e) {
            String msg = "Unable to get username for userId : " + userId;
            log.error(msg, e);
        }
        return userName;
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
