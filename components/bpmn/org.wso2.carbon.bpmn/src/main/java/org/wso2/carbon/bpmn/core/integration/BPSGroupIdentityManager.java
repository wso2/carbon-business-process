/**
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.wso2.carbon.bpmn.core.integration;

import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.GroupQuery;
import org.activiti.engine.impl.GroupQueryImpl;
import org.activiti.engine.impl.Page;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
//import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.persistence.entity.GroupEntity;
import org.activiti.engine.impl.persistence.entity.GroupEntityManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bpmn.core.BPMNServerHolder;
import org.wso2.carbon.bpmn.core.internal.IdentityDataHolder;
import org.wso2.carbon.security.caas.user.core.bean.Role;
import org.wso2.carbon.security.caas.user.core.bean.User;
import org.wso2.carbon.security.caas.user.core.exception.AuthorizationStoreException;
import org.wso2.carbon.security.caas.user.core.exception.IdentityStoreException;
import org.wso2.carbon.security.caas.user.core.exception.UserNotFoundException;
import org.wso2.carbon.security.caas.user.core.store.AuthorizationStore;
import org.wso2.carbon.security.caas.user.core.store.IdentityStore;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 *
 */
public class BPSGroupIdentityManager extends GroupEntityManager {

    private static Log log = LogFactory.getLog(BPSUserIdentityManager.class);

    private AuthorizationStore authorizationStore;
    private IdentityStore identityStore;

    public BPSGroupIdentityManager() {

        this.authorizationStore = IdentityDataHolder.getInstance().getCarbonRealmService().getAuthorizationStore();
        this.identityStore = IdentityDataHolder.getInstance().getCarbonRealmService().getIdentityStore();
    }


    @Override
    public Group createNewGroup(String groupId) {
        String msg = "Invoked GroupIdentityManager method is not supported by BPSGroupIdentityManager.";
        throw new UnsupportedOperationException(msg);
    }

    @Override
    public void insertGroup(Group group) {
        String msg = "Invoked GroupIdentityManager method is not supported by BPSGroupIdentityManager.";
        throw new UnsupportedOperationException(msg);
    }

    @Override
    public void deleteGroup(String groupId) {
        String msg = "Invoked GroupIdentityManager method is not supported by BPSGroupIdentityManager.";
        throw new UnsupportedOperationException(msg);
    }

    @Override
    public GroupQuery createNewGroupQuery() {
        return new GroupQueryImpl(((ProcessEngineConfigurationImpl) BPMNServerHolder.getInstance().
                getEngine().getProcessEngineConfiguration()).getCommandExecutor());

    }

    @Override
    public List<Group> findGroupByQueryCriteria(GroupQueryImpl query, Page page) {
        return getDbSqlSession().selectList("selectGroupByQueryCriteria", query, page);
    }

    @Override
    public long findGroupCountByQueryCriteria(GroupQueryImpl query) {
        String msg = "Invoked GroupIdentityManager method is not supported by BPSGroupIdentityManager.";
        throw new UnsupportedOperationException(msg);
    }

    @Override
    public List<Group> findGroupsByUser(String userId) {
        String userName = getUserNameForGivenUserId(userId);
        List<Group> groups = new ArrayList<Group>();
        if (userName.isEmpty()) {

        } else {
            try {

                List<Role> roles = authorizationStore.getRolesOfUser
                        (userId, identityStore.getUser(userName).getIdentityStoreId());

                groups = roles.stream().map(role -> new GroupEntity(role.getRoleId())).
                        collect(Collectors.toList());

//                for (Role role : roles) {
//                    Group group = new GroupEntity(role.getRoleId());
//                    groups.add(group);
//                }

            } catch (IdentityStoreException | UserNotFoundException | AuthorizationStoreException e) {
                String msg = "Failed to get roles of the user: " + userId + "." +
                        " Returning an empty roles list.";
                log.error(msg, e);
            }


        }
        return groups;

    }

    // todo: get matching username for userid
    private String getUserNameForGivenUserId(String userId) {
        String userName = "";
        try { //todo: need to set length to -1
            List<org.wso2.carbon.security.caas.user.core.bean.User> users =
                    identityStore.listUsers("%", 0, 10);
            if (!users.isEmpty()) {
                Optional<User> matchingObjects = users.stream().
                        filter(u -> u.getUserId().equals(userId)).
                        findFirst();
                if (matchingObjects.isPresent()) {
                    org.wso2.carbon.security.caas.user.core.bean.User filteredUser =
                            matchingObjects.get();
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
    public List<Group> findGroupsByNativeQuery(Map<String, Object> parameterMap, int firstResult,
                                               int maxResults) {
        String msg = "Invoked GroupIdentityManager method is not supported by" +
                " BPSGroupIdentityManager.";
        throw new UnsupportedOperationException(msg);
    }

    @Override
    public long findGroupCountByNativeQuery(Map<String, Object> parameterMap) {
        String msg = "Invoked GroupIdentityManager method is not supported by" +
                " BPSGroupIdentityManager.";
        throw new UnsupportedOperationException(msg);
    }
}

