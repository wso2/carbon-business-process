package org.wso2.carbon.humantask.core.engine;

import org.wso2.carbon.humantask.core.dao.GenericHumanRoleDAO;
import org.wso2.carbon.humantask.core.dao.OrganizationalEntityDAO;

import java.util.List;

/**
 * The people query functionality.
 */
public interface PeopleQueryEvaluator {
    /**
     * Check whether if the given username exist in the user store.
     *
     * @param userName : The user name to be checked.
     * @return : true if the user exist.
     */
    boolean isExistingUser(String userName);

    /**
     * Check whether if the given roleName exists in the user store.
     *
     * @param roleName : The roleName to be checked.
     * @return : true if the roleName exists in the user store.
     */
    boolean isExistingRole(String roleName);

    /**
     * Checks whether the are users for the given roleName.
     *
     * @param roleName : The role name.
     * @return : True if there are users for the given roleName. false otherwise.
     */
    boolean hasUsersForRole(String roleName);

    /**
     * Returns the list of user names in the user store for the given role name.
     *
     * @param roleName : The role name.
     * @return : The list of user names for the given role name.
     */
    List<String> getUserNameListForRole(String roleName);

    /**
     * Returns the list of matching roles for a given user name.
     *
     * @param userName : The user name to get the list of roles.
     * @return : The list of matching role names.
     */
    List<String> getRoleNameListForUser(String userName);

    /**
     * Creates a new org entity object for the given roleName
     *
     * @param roleName : The name of the role.
     * @return : the created object.
     */
    OrganizationalEntityDAO createGroupOrgEntityForRole(String roleName);

    /**
     * Creates a new org entity object for the given user name
     *
     * @param userName : The name of the user.
     * @return : the created object.
     */
    OrganizationalEntityDAO createUserOrgEntityForName(String userName);

    /**
     * Creates the GenericHumanRoleDAO object for the given role and the role type.
     *
     * @param roleName : the name of the human role.
     * @param type     : The type of the human role.
     * @return : the created GenericHumanRoleDAO object.
     */
    GenericHumanRoleDAO createGHRForRoleName(String roleName,
                                             GenericHumanRoleDAO.GenericHumanRoleType type);

    /**
     * Create a GenericHumanRoleDAO Object for a given user name and role type
     */
    GenericHumanRoleDAO createGHRForUsername(String username,
                                             GenericHumanRoleDAO.GenericHumanRoleType type);

    /**
     * checks if the given list of orgEntities exists in the user store.
     *
     * @param orgEntities : The list of orgEntities to be checked.
     */
    void checkOrgEntitiesExist(List<OrganizationalEntityDAO> orgEntities);

    /**
     * checks if the given list orgEntity exists in the user store.
     *
     * @param orgEntity : The orgEntity to be checked.
     * @throws : @see: HumanTaskRuntimeException if the org entities does not exist.
     */
    void checkOrgEntityExists(OrganizationalEntityDAO orgEntity);

    boolean isOrgEntityInRole(OrganizationalEntityDAO entity,
                                     GenericHumanRoleDAO role);

    /**
     * Returns the currently logged in user's user name.
     *
     * @return : The user name of the logged in user.
     */
    String getLoggedInUser();
}
