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
import org.activiti.engine.impl.persistence.entity.GroupEntity;
import org.activiti.engine.impl.persistence.entity.GroupEntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.bpmn.core.internal.BPMNServerHolder;
import org.wso2.carbon.security.caas.user.core.bean.User;
import org.wso2.carbon.security.caas.user.core.exception.IdentityStoreException;
import org.wso2.carbon.security.caas.user.core.exception.UserNotFoundException;
import org.wso2.carbon.security.caas.user.core.store.IdentityStore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * CAAS based Group Identity Manager.
 */
public class BPSGroupIdentityManager extends GroupEntityManager {

    private static Logger log = LoggerFactory.getLogger(BPSUserIdentityManager.class);

    private IdentityStore identityStore;

    public BPSGroupIdentityManager() {
        this.identityStore = BPMNServerHolder.getInstance().getCarbonRealmService().getIdentityStore();
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
    public List<Group> findGroupsByUser(String userName) {

        List<Group> groups = new ArrayList<Group>();
        if (!userName.isEmpty()) {
            try {
                User user = identityStore.getUser(userName);
                if (user != null) {
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
            } catch (IdentityStoreException | UserNotFoundException e) {
                String msg = "Failed to get groups of the user: " + userName + "." +
                        " Returning an empty roles list.";
                log.error(msg, e);
            }
        }
        return groups;

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

