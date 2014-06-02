package org.wso2.carbon.bpmn.core.integration;

import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.GroupQuery;
import org.activiti.engine.impl.GroupQueryImpl;
import org.activiti.engine.impl.Page;
import org.activiti.engine.impl.persistence.entity.GroupEntity;
import org.activiti.engine.impl.persistence.entity.GroupEntityManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.user.core.UserStoreManager;

import java.util.List;
import java.util.Map;

public class BPSGroupIdentityManager extends GroupEntityManager {

    private static Log log = LogFactory.getLog(BPSUserIdentityManager.class);

    private UserStoreManager userStoreManager;

    public BPSGroupIdentityManager(UserStoreManager userStoreManager) {
        this.userStoreManager = userStoreManager;
    }

    @Override
    public Group createNewGroup(String groupId) {
        String msg = "Invoked GroupIdentityManager method is not supported by BPSGroupIdentityManager.";
        log.error(msg);
        throw new UnsupportedOperationException(msg);
    }

    @Override
    public void insertGroup(Group group) {
        String msg = "Invoked GroupIdentityManager method is not supported by BPSGroupIdentityManager.";
        log.error(msg);
        throw new UnsupportedOperationException(msg);
    }

    @Override
    public void updateGroup(GroupEntity updatedGroup) {
        String msg = "Invoked GroupIdentityManager method is not supported by BPSGroupIdentityManager.";
        log.error(msg);
        throw new UnsupportedOperationException(msg);
    }

    @Override
    public void deleteGroup(String groupId) {
        String msg = "Invoked GroupIdentityManager method is not supported by BPSGroupIdentityManager.";
        log.error(msg);
        throw new UnsupportedOperationException(msg);
    }

    @Override
    public GroupQuery createNewGroupQuery() {
        String msg = "Invoked GroupIdentityManager method is not supported by BPSGroupIdentityManager.";
        log.error(msg);
        throw new UnsupportedOperationException(msg);
    }

    @Override
    public List<Group> findGroupByQueryCriteria(GroupQueryImpl query, Page page) {
        String msg = "Invoked GroupIdentityManager method is not supported by BPSGroupIdentityManager.";
        log.error(msg);
        throw new UnsupportedOperationException(msg);
    }

    @Override
    public long findGroupCountByQueryCriteria(GroupQueryImpl query) {
        String msg = "Invoked GroupIdentityManager method is not supported by BPSGroupIdentityManager.";
        log.error(msg);
        throw new UnsupportedOperationException(msg);
    }

    @Override
    public List<Group> findGroupsByUser(String userId) {
        String msg = "Invoked GroupIdentityManager method is not supported by BPSGroupIdentityManager.";
        log.error(msg);
        throw new UnsupportedOperationException(msg);
    }

    @Override
    public List<Group> findGroupsByNativeQuery(Map<String, Object> parameterMap, int firstResult, int maxResults) {
        String msg = "Invoked GroupIdentityManager method is not supported by BPSGroupIdentityManager.";
        log.error(msg);
        throw new UnsupportedOperationException(msg);
    }

    @Override
    public long findGroupCountByNativeQuery(Map<String, Object> parameterMap) {
        String msg = "Invoked GroupIdentityManager method is not supported by BPSGroupIdentityManager.";
        log.error(msg);
        throw new UnsupportedOperationException(msg);
    }
}
