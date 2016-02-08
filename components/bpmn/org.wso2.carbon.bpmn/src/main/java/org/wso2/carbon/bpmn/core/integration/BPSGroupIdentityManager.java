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
 */

package org.wso2.carbon.bpmn.core.integration;

import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.GroupQuery;
import org.activiti.engine.impl.GroupQueryImpl;
import org.activiti.engine.impl.Page;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.persistence.entity.GroupEntity;
import org.activiti.engine.impl.persistence.entity.GroupEntityManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bpmn.core.BPMNServerHolder;
//import org.wso2.carbon.user.api.UserStoreException;
//import org.wso2.carbon.user.core.UserStoreManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BPSGroupIdentityManager extends GroupEntityManager {

    private static Log log = LogFactory.getLog(BPSUserIdentityManager.class);

   // private UserStoreManager userStoreManager;

   // public BPSGroupIdentityManager(UserStoreManager userStoreManager) {
    //    this.userStoreManager = userStoreManager;
    //}

public BPSGroupIdentityManager() {

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
        return new GroupQueryImpl(((ProcessEngineConfigurationImpl) BPMNServerHolder.getInstance().getEngine().getProcessEngineConfiguration()).getCommandExecutor());

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
        List<Group> groups = new ArrayList<Group>();
       // try {
          //  String[] roles = userStoreManager.getRoleListOfUser(userId);
           //TODO
        String[] roles = {"admin","everyone"};
            for (String role : roles) {
                Group group = new GroupEntity(role);
                groups.add(group);
            }
        /*} catch (UserStoreException e) {
            String msg = "Failed to get roles of the user: " + userId + ". Returning an empty roles list.";
            log.error(msg, e);
        }*/
        return groups;
    }

    @Override
    public List<Group> findGroupsByNativeQuery(Map<String, Object> parameterMap, int firstResult,
                                               int maxResults) {
        String msg = "Invoked GroupIdentityManager method is not supported by BPSGroupIdentityManager.";
        throw new UnsupportedOperationException(msg);
    }

    @Override
    public long findGroupCountByNativeQuery(Map<String, Object> parameterMap) {
        String msg = "Invoked GroupIdentityManager method is not supported by BPSGroupIdentityManager.";
        throw new UnsupportedOperationException(msg);
    }
}
