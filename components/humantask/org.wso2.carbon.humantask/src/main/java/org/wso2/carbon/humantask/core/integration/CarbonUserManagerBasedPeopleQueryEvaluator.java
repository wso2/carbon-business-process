package org.wso2.carbon.humantask.core.integration;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.humantask.core.HumanTaskConstants;
import org.wso2.carbon.humantask.core.dao.GenericHumanRoleDAO;
import org.wso2.carbon.humantask.core.dao.HumanTaskDAOConnection;
import org.wso2.carbon.humantask.core.dao.OrganizationalEntityDAO;
import org.wso2.carbon.humantask.core.engine.PeopleQueryEvaluator;
import org.wso2.carbon.humantask.core.engine.runtime.api.HumanTaskRuntimeException;
import org.wso2.carbon.humantask.core.internal.HumanTaskServiceComponent;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.UserStoreException;

import javax.cache.Cache;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * PeopleQuery evaluator is used to get the set of users from user manager giving set
 * of arguments as defined in human interaction file's logical people groups
 */
public class CarbonUserManagerBasedPeopleQueryEvaluator implements PeopleQueryEvaluator {
    private static Log log = LogFactory.getLog(CarbonUserManagerBasedPeopleQueryEvaluator.class);

    private RegistryService registryService;

    private static final boolean cachingEnabled = HumanTaskServiceComponent.getHumanTaskServer().getServerConfig().isCachingEnabled();

    public CarbonUserManagerBasedPeopleQueryEvaluator() {
        this.registryService = HumanTaskServiceComponent.getRegistryService();
    }

    public boolean isExistingUser(String userName) {
        try {
            if (cachingEnabled) {
                Cache<String, Boolean> userNameListCache = getUserNameListCache();
                if(userNameListCache != null && userNameListCache.containsKey(userName)) {
                    return userNameListCache.get(userName);
                }
            }
            boolean isExistingUser = getUserRealm().getUserStoreManager().isExistingUser(userName);
            if (cachingEnabled) {
                Cache<String, Boolean> userNameListCache = getUserNameListCache();
                if(userNameListCache != null) {
                    userNameListCache.put(userName, isExistingUser);
                }
            }
            return isExistingUser;
        } catch (UserStoreException e) {
            throw new HumanTaskRuntimeException("Error occurred while calling to realm service", e);
        }
    }

    /**
     * Return true if the provided role name exist.
     *
     * @param roleName :  The role name to check.
     * @return : True is the role exists, false otherwise.
     */
    public boolean isExistingRole(String roleName) {
        try {
            if (cachingEnabled) {
                Cache<String, Boolean> roleNameListCache = getRoleNameListCache();
                if(roleNameListCache != null && roleNameListCache.containsKey(roleName)) {
                    return getRoleNameListCache().get(roleName);
                }
            }
            boolean isExistingRole = getUserRealm().getUserStoreManager().isExistingRole(roleName);
            if (cachingEnabled) {
                Cache<String, Boolean> roleNameListCache = getRoleNameListCache();
                if(roleNameListCache != null){
                    getRoleNameListCache().put(roleName, isExistingRole);
                }
            }
            return isExistingRole;
        } catch (UserStoreException e) {
            throw new HumanTaskRuntimeException("Error occurred while calling to realm service " +
                                                "for operation isExistingRole", e);
        }
    }

    public boolean hasUsersForRole(String roleName) {
        throw new UnsupportedOperationException("TODO Implement me");
    }

    public List<String> getUserNameListForRole(String roleName) {
        if (isExistingRole(roleName)) {
            if (cachingEnabled) {
                Cache<String, List<String>> userNameListForRoleCache = getUserNameListForRoleCache();
                if(userNameListForRoleCache != null && userNameListForRoleCache.containsKey(roleName)) {
                    return getUserNameListForRoleCache().get(roleName);
                }
            }
            try {
                ArrayList<String> usernameList = new ArrayList<String>(Arrays.asList(getUserRealm().getUserStoreManager().getUserListOfRole(roleName)));
                if (cachingEnabled) {
                    Cache<String, List<String>> userNameListForRoleCache = getUserNameListForRoleCache();
                    if(userNameListForRoleCache != null) {
                        getUserNameListForRoleCache().put(roleName, usernameList);
                    }
                }
                return usernameList;
            } catch (UserStoreException e) {
                throw new HumanTaskRuntimeException("Error occurred while calling" +
                        " to realm service for operation isExistingRole", e);
            }
        } else {
            throw new HumanTaskRuntimeException(String.format("The role name[%s] does not exist.", roleName));
        }
    }

    public List<String> getRoleNameListForUser(String userName) {
        String tUserName  = userName;
        List<String> matchingRoleNames = new ArrayList<String>();
        if (StringUtils.isNotEmpty(tUserName)) {
            tUserName = tUserName.trim();
            if (cachingEnabled) {
                Cache<String, List<String>> roleNameListForUserCache = getRoleNameListForUserCache();
                if(roleNameListForUserCache != null && roleNameListForUserCache.containsKey(tUserName)){
                    return roleNameListForUserCache.get(tUserName);
                }
            }
            if (isExistingUser(tUserName)) {
                try {
                    matchingRoleNames.addAll(
                            Arrays.asList(
                                    getUserRealm().getUserStoreManager().
                                            getRoleListOfUser(tUserName)));
                    if (cachingEnabled) {
                        getRoleNameListForUserCache().put(tUserName, matchingRoleNames);
                    }
                } catch (UserStoreException ex) {
                    throw new HumanTaskRuntimeException("Error occurred while calling" +
                            " to realm service for operation isExistingRole", ex);
                }
            }
        }
        return matchingRoleNames;
    }

    public OrganizationalEntityDAO createGroupOrgEntityForRole(String roleName) {
        String tRoleName = roleName.trim();
        if (isExistingRole(tRoleName)) {
            return getConnection().createNewOrgEntityObject(tRoleName, OrganizationalEntityDAO.OrganizationalEntityType.GROUP);
        } else {
            throw new HumanTaskRuntimeException(String.format("The role name[%s] does not exist.", tRoleName));
        }
    }

    public OrganizationalEntityDAO createUserOrgEntityForName(String userName) {
        if (isExistingUser(userName)) {
            return getConnection().createNewOrgEntityObject(userName, OrganizationalEntityDAO.OrganizationalEntityType.USER);
        } else {
            throw new HumanTaskRuntimeException(String.format("The user name[%s] does not exist.", userName));
        }
    }

    public GenericHumanRoleDAO createGHRForRoleName(String roleName,
                                                    GenericHumanRoleDAO.GenericHumanRoleType type) {
        if (isExistingRole(roleName)) {

            List<String> userNames = getUserNameListForRole(roleName);
            GenericHumanRoleDAO ghr = getConnection().createNewGHRObject(type);
            List<OrganizationalEntityDAO> orgEntities = new ArrayList<OrganizationalEntityDAO>();
            for (String userName : userNames) {
                OrganizationalEntityDAO orgEntity =
                        getConnection().createNewOrgEntityObject(userName,
                                                                 OrganizationalEntityDAO.OrganizationalEntityType.USER);
                orgEntity.addGenericHumanRole(ghr);
                orgEntities.add(orgEntity);
            }

            ghr.setOrgEntities(orgEntities);

            return ghr;

        } else {
            throw new HumanTaskRuntimeException(String.format("The role name[%s] does not exist.", roleName));
        }
    }

    public GenericHumanRoleDAO createGHRForUsername(String username, GenericHumanRoleDAO.GenericHumanRoleType type) {
        if (isExistingUser(username)) {

            GenericHumanRoleDAO ghr = getConnection().createNewGHRObject(type);
            List<OrganizationalEntityDAO> orgEntities = new ArrayList<OrganizationalEntityDAO>();
            OrganizationalEntityDAO orgEntity = getConnection().createNewOrgEntityObject(username,
                                OrganizationalEntityDAO.OrganizationalEntityType.USER);
            orgEntity.addGenericHumanRole(ghr);
            orgEntities.add(orgEntity);
            ghr.setOrgEntities(orgEntities);

            return ghr;

        } else {
            throw new HumanTaskRuntimeException(String.format("The username [%s] does not exist.", username));
        }
    }

    public void checkOrgEntitiesExist(List<OrganizationalEntityDAO> orgEntities) {
        if (orgEntities != null) {
            for (OrganizationalEntityDAO orgEntity : orgEntities) {
                checkOrgEntityExists(orgEntity);
            }
        }
    }

    public void checkOrgEntityExists(OrganizationalEntityDAO orgEntity) {
        if (orgEntity != null) {
            if (OrganizationalEntityDAO.OrganizationalEntityType.USER.equals(orgEntity.getOrgEntityType())) {
                if (!isExistingUser(orgEntity.getName())) {
                    throw new HumanTaskRuntimeException(String.format("The user name:[%s] " +
                                                                      "does not exist in the user store!",
                                                                      orgEntity.getName()));
                }
            } else if (OrganizationalEntityDAO.OrganizationalEntityType.GROUP.equals(orgEntity.getOrgEntityType())) {
                if (!isExistingRole(orgEntity.getName())) {
                    throw new HumanTaskRuntimeException(String.format("The group name:[%s] " +
                                                                      "does not exist in the user store!",
                                                                      orgEntity.getName()));
                }
            }
        }
    }

    public boolean isOrgEntityInRole(OrganizationalEntityDAO entity, GenericHumanRoleDAO role) {
        boolean isOrgEntityInRole = false;

        for (OrganizationalEntityDAO orgEntity : role.getOrgEntities()) {
            if (OrganizationalEntityDAO.OrganizationalEntityType.USER.equals(orgEntity.getOrgEntityType())) {
                if (orgEntity.getName().equals(entity.getName())) {
                    isOrgEntityInRole = true;
                }
            } else if (OrganizationalEntityDAO.OrganizationalEntityType.GROUP.equals(orgEntity.getOrgEntityType())) {
                if (getUserNameListForRole(orgEntity.getName()).contains(entity.getName())) {
                    isOrgEntityInRole = true;
                }
            }

            if (isOrgEntityInRole) {
                break;
            }
        }

        return isOrgEntityInRole;
    }

    public String getLoggedInUser() {
        String userName = null;
        if (StringUtils.isNotEmpty(CarbonContext.getThreadLocalCarbonContext().getUsername())) {
            userName = CarbonContext.getThreadLocalCarbonContext().getUsername();
        }
        return userName;
    }

    private HumanTaskDAOConnection getConnection() {
        return HumanTaskServiceComponent.getHumanTaskServer().getTaskEngine().
                getDaoConnectionFactory().getConnection();
    }

    private UserRealm getUserRealm() {
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            return this.registryService.getUserRealm(tenantId);
        } catch (RegistryException e) {
            throw new HumanTaskRuntimeException("Error occurred while retrieving " +
                                                "User Realm for tenant :" + tenantId, e);
        }

    }

    private Cache<String, Boolean> getUserNameListCache() {
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        return HumanTaskServiceComponent.getHumanTaskServer().getTaskStoreManager()
                .getHumanTaskStore(tenantId).getCache(HumanTaskConstants.HT_CACHE_USER_NAME_LIST);
    }

    private Cache<String, Boolean> getRoleNameListCache() {
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        return HumanTaskServiceComponent.getHumanTaskServer().getTaskStoreManager()
                .getHumanTaskStore(tenantId).getCache(HumanTaskConstants.HT_CACHE_ROLE_NAME_LIST);
    }

    private Cache<String, List<String>> getRoleNameListForUserCache() {
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        return HumanTaskServiceComponent.getHumanTaskServer().getTaskStoreManager()
                .getHumanTaskStore(tenantId).getCache(HumanTaskConstants.HT_CACHE_ROLE_NAME_LIST_FOR_USER);
    }

    private Cache<String, List<String>> getUserNameListForRoleCache() {
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        return HumanTaskServiceComponent.getHumanTaskServer().getTaskStoreManager()
                .getHumanTaskStore(tenantId).getCache(HumanTaskConstants.HT_CACHE_USER_NAME_LIST_FOR_ROLE);
    }

}
