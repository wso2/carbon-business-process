/*
 * Copyright (c) 2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.humantask.core.engine.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.humantask.core.dao.GenericHumanRoleDAO;
import org.wso2.carbon.humantask.core.dao.OrganizationalEntityDAO;
import org.wso2.carbon.humantask.core.dao.TaskDAO;
import org.wso2.carbon.humantask.core.dao.TaskStatus;
import org.wso2.carbon.humantask.core.dao.TaskType;
import org.wso2.carbon.humantask.core.engine.PeopleQueryEvaluator;
import org.wso2.carbon.humantask.core.engine.people.eval.PeopleQueryComparators;
import org.wso2.carbon.humantask.core.internal.HumanTaskServerHolder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Common functionality used in operation authentications
 */
public final class OperationAuthorizationUtil {

    private static final Log log = LogFactory.getLog(OperationAuthorizationUtil.class);
    private OperationAuthorizationUtil() {
    }

    /**
     * @param task             : The task against which the user being validated.
     * @param validatee        : The OrganizationalEntityDAO being validated.
     * @param allowedRoleTypes : The allowed role types for the validatee object.
     * @param pqe              : PeopleQueryEvaluator for people queries.
     * @return : true if the user is in the specified roles for the given task. false otherwise.
     */
    public static boolean authoriseUser(TaskDAO task, OrganizationalEntityDAO validatee,
                                        List<GenericHumanRoleDAO.GenericHumanRoleType> allowedRoleTypes,
                                        PeopleQueryEvaluator pqe) {
        List<GenericHumanRoleDAO> humanRolesInTask = task.getHumanRoles();

        if(isExcludedEntity(task, validatee, pqe)){
            return false;
        }

        for (GenericHumanRoleDAO role : humanRolesInTask) {
            if (allowedRoleTypes.contains(role.getType())) {

                // check for groups
                for (OrganizationalEntityDAO entityForRole : getGroupOrganizationalEntities(role)) {
                    if (OrganizationalEntityDAO.OrganizationalEntityType.GROUP.equals(
                            entityForRole.getOrgEntityType())) {
                        String roleName = entityForRole.getName();
                        List<String> userListForRole = pqe.getUserNameListForRole(roleName);
                        if (userListForRole.contains(validatee.getName())) {
                            return true;
                        }
                    }
                }

                //check for users
                //TODO validate user existance in the user store.
                List<OrganizationalEntityDAO> orgEntities = getUserOrganizationalEntities(role);
                Collections.sort(orgEntities, PeopleQueryComparators.peopleNameComparator());
                if (Collections.binarySearch(orgEntities, validatee,
                        PeopleQueryComparators.peopleNameComparator()) >= 0) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean isExcludedEntity(TaskDAO task, OrganizationalEntityDAO validatee, PeopleQueryEvaluator pqe) {

        GenericHumanRoleDAO excludedOwners = task.getGenericHumanRole(GenericHumanRoleDAO.GenericHumanRoleType.EXCLUDED_OWNERS);
        if(excludedOwners != null) {
            for (OrganizationalEntityDAO entityForRole : getGroupOrganizationalEntities(excludedOwners)) {
                if (OrganizationalEntityDAO.OrganizationalEntityType.GROUP.equals(
                        entityForRole.getOrgEntityType())) {
                    String roleName = entityForRole.getName();
                    List<String> userListForRole = pqe.getUserNameListForRole(roleName);
                    if (userListForRole.contains(validatee.getName())) {
                        log.error("User " + validatee.getName() + " is in EXCLUDED_OWNERS role");
                        return true;
                    }
                }
            }


            List<OrganizationalEntityDAO> orgEntities = getUserOrganizationalEntities(excludedOwners);
            Collections.sort(orgEntities, PeopleQueryComparators.peopleNameComparator());
            if (Collections.binarySearch(orgEntities, validatee,
                    PeopleQueryComparators.peopleNameComparator()) >= 0) {
                log.error("User " + validatee.getName() + " is in EXCLUDED_OWNERS role");
                return true;
            }
        }
        return false;
    }

    private static List<OrganizationalEntityDAO> getGroupOrganizationalEntities(
            GenericHumanRoleDAO role) {
        List<OrganizationalEntityDAO> groupOrgEntities = new ArrayList<OrganizationalEntityDAO>();
        for (OrganizationalEntityDAO orgEntity : role.getOrgEntities()) {
            if (OrganizationalEntityDAO.OrganizationalEntityType.GROUP.equals(
                    orgEntity.getOrgEntityType())) {
                groupOrgEntities.add(orgEntity);
            }
        }
        return groupOrgEntities;
    }

    private static List<OrganizationalEntityDAO> getUserOrganizationalEntities(
            GenericHumanRoleDAO role) {
        List<OrganizationalEntityDAO> userOrgEntities = new ArrayList<OrganizationalEntityDAO>();
        for (OrganizationalEntityDAO orgEntity : role.getOrgEntities()) {
            if (OrganizationalEntityDAO.OrganizationalEntityType.USER.equals(
                    orgEntity.getOrgEntityType())) {
                userOrgEntities.add(orgEntity);
            }
        }
        return userOrgEntities;
    }

    /**
     * Checks whether the provided user is authorised to activate the given task.
     *
     * @param task   : The TaskDAO object
     * @param caller : The user  being authorised.
     * @param pqe    : The people query evaluator.
     * @return : true if the task has the required state and the user is authorised to perform activate
     *         operation.
     */
    public static boolean authorisedToActivate(TaskDAO task, OrganizationalEntityDAO caller,
                                               PeopleQueryEvaluator pqe) {
        if (task.getActivationTime() == null || task.getActivationTime().before(new Date())) {
            return false;
        }

        if (CommonTaskUtil.getOrgEntitiesForRole(task, GenericHumanRoleDAO.
                GenericHumanRoleType.POTENTIAL_OWNERS).size() < 1) {
            return false;
        }

        List<GenericHumanRoleDAO.GenericHumanRoleType> allowedRoles =
                new ArrayList<GenericHumanRoleDAO.GenericHumanRoleType>();
        allowedRoles.add(GenericHumanRoleDAO.GenericHumanRoleType.TASK_INITIATOR);
        allowedRoles.add(GenericHumanRoleDAO.GenericHumanRoleType.BUSINESS_ADMINISTRATORS);
        allowedRoles.add(GenericHumanRoleDAO.GenericHumanRoleType.STAKEHOLDERS);
        return authoriseUser(task, caller, allowedRoles, pqe);
    }

    /**
     * Checks whether the provided user is authorised to update comments of the given task.
     *
     * @param task   : The TaskDAO object
     * @param caller : The user  being authorised.
     * @param pqe    : The people query evaluator.
     * @return : true if the task has the required state and the user is authorised to update comments.
     */
    public static boolean authorisedToUpdateComment(TaskDAO task, OrganizationalEntityDAO caller,
                                                    PeopleQueryEvaluator pqe) {
        List<GenericHumanRoleDAO.GenericHumanRoleType> allowedRoles = new ArrayList<GenericHumanRoleDAO.GenericHumanRoleType>();
        allowedRoles.add(GenericHumanRoleDAO.GenericHumanRoleType.ACTUAL_OWNER);
        allowedRoles.add(GenericHumanRoleDAO.GenericHumanRoleType.POTENTIAL_OWNERS);
        allowedRoles.add(GenericHumanRoleDAO.GenericHumanRoleType.BUSINESS_ADMINISTRATORS);
        allowedRoles.add(GenericHumanRoleDAO.GenericHumanRoleType.STAKEHOLDERS);
        return authoriseUser(task, caller, allowedRoles, pqe);
    }

    /**
     * Checks whether the provided user is authorised to suspend the given task.
     *
     * @param task   : The TaskDAO object
     * @param caller : The user  being authorised.
     * @param pqe    : The people query evaluator.
     * @return : true if the task has the required state and the user is authorised to perform suspend
     *         operation.
     */
    public static boolean authorisedToSuspend(TaskDAO task, OrganizationalEntityDAO caller,
                                              PeopleQueryEvaluator pqe) {

        if (!TaskStatus.IN_PROGRESS.equals(task.getStatus()) &&
                !TaskStatus.READY.equals(task.getStatus()) &&
                !TaskStatus.RESERVED.equals(task.getStatus())) {
            return false;
        }


        List<GenericHumanRoleDAO.GenericHumanRoleType> allowedRoles = new ArrayList<GenericHumanRoleDAO.GenericHumanRoleType>();
        allowedRoles.add(GenericHumanRoleDAO.GenericHumanRoleType.ACTUAL_OWNER);
        allowedRoles.add(GenericHumanRoleDAO.GenericHumanRoleType.BUSINESS_ADMINISTRATORS);
        allowedRoles.add(GenericHumanRoleDAO.GenericHumanRoleType.STAKEHOLDERS);
        return authoriseUser(task, caller, allowedRoles, pqe);
    }

    /**
     * Checks whether the provided user is authorised to stop the given task.
     *
     * @param task   : The TaskDAO object
     * @param caller : The user  being authorised.
     * @param pqe    : The people query evaluator.
     * @return : true if the task has the required state and the user is authorised to perform stop
     *         operation.
     */
    public static boolean authorisedToStop(TaskDAO task, OrganizationalEntityDAO caller,
                                           PeopleQueryEvaluator pqe) {

        if (!TaskStatus.IN_PROGRESS.equals(task.getStatus())) {
            return false;
        }
        List<GenericHumanRoleDAO.GenericHumanRoleType> allowedRoles = new ArrayList<GenericHumanRoleDAO.GenericHumanRoleType>();
        allowedRoles.add(GenericHumanRoleDAO.GenericHumanRoleType.ACTUAL_OWNER);
        if (HumanTaskServerHolder.getInstance().getHtServer().getServerConfig().isTaskOperationsForBusinessAdministratorEnabled()) {
            allowedRoles.add(GenericHumanRoleDAO.GenericHumanRoleType.BUSINESS_ADMINISTRATORS);
        }
        return authoriseUser(task, caller, allowedRoles, pqe);
    }

    /**
     * Checks whether the provided user is authorised to start the given task.
     *
     * @param task   : The TaskDAO object
     * @param caller : The user  being authorised.
     * @param pqe    : The people query evaluator.
     * @return : true if the task has the required state and the user is authorised to perform start
     *         operation.
     */
    public static boolean authorisedToStart(TaskDAO task, OrganizationalEntityDAO caller,
                                            PeopleQueryEvaluator pqe) {
        if (TaskStatus.READY.equals(task.getStatus())) {
            List<GenericHumanRoleDAO.GenericHumanRoleType> allowedRoles = new ArrayList
                    <GenericHumanRoleDAO.GenericHumanRoleType>();
            allowedRoles.add(GenericHumanRoleDAO.GenericHumanRoleType.POTENTIAL_OWNERS);
            if (HumanTaskServerHolder.getInstance().getHtServer().getServerConfig().isTaskOperationsForBusinessAdministratorEnabled()) {
                allowedRoles.add(GenericHumanRoleDAO.GenericHumanRoleType.BUSINESS_ADMINISTRATORS);
            }
            return authoriseUser(task, caller, allowedRoles, pqe);
        } else if (TaskStatus.RESERVED.equals(task.getStatus())) {
            List<GenericHumanRoleDAO.GenericHumanRoleType> allowedRoles = new ArrayList
                    <GenericHumanRoleDAO.GenericHumanRoleType>();
            allowedRoles.add(GenericHumanRoleDAO.GenericHumanRoleType.ACTUAL_OWNER);
            if (HumanTaskServerHolder.getInstance().getHtServer().getServerConfig().isTaskOperationsForBusinessAdministratorEnabled()) {
                allowedRoles.add(GenericHumanRoleDAO.GenericHumanRoleType.BUSINESS_ADMINISTRATORS);
            }
            return authoriseUser(task, caller, allowedRoles, pqe);
        } else {
            return false;
        }
    }

    /**
     * Checks whether the provided user is authorised to skip the given task.
     *
     * @param task   : The TaskDAO object
     * @param caller : The user  being authorised.
     * @param pqe    : The people query evaluator.
     * @return : true if the task has the required state and the user is authorised to perform skip
     *         operation.
     */
    public static boolean authorisedToSkip(TaskDAO task, OrganizationalEntityDAO caller,
                                           PeopleQueryEvaluator pqe) {

        if (!task.isSkipable()) {
            return false;
        }

        if (TaskStatus.CREATED.equals(task.getStatus())
                || TaskStatus.READY.equals(task.getStatus())
                || TaskStatus.RESERVED.equals(task.getStatus())
                || TaskStatus.IN_PROGRESS.equals(task.getStatus())) {
            List<GenericHumanRoleDAO.GenericHumanRoleType> allowedRoles = new ArrayList<GenericHumanRoleDAO.GenericHumanRoleType>();
            allowedRoles.add(GenericHumanRoleDAO.GenericHumanRoleType.TASK_INITIATOR);
            allowedRoles.add(GenericHumanRoleDAO.GenericHumanRoleType.BUSINESS_ADMINISTRATORS);
            allowedRoles.add(GenericHumanRoleDAO.GenericHumanRoleType.STAKEHOLDERS);

            return authoriseUser(task, caller, allowedRoles, pqe);
        }

        return false;
    }

    /**
     * Checks whether the provided user is authorised to setPriority of the given task.
     *
     * @param task   : The TaskDAO object
     * @param caller : The user  being authorised.
     * @param pqe    : The people query evaluator.
     * @return : true if the task has the required state and the user is authorised to perform setPriority
     *         operation.
     */
    public static boolean authorisedToSetPriority(TaskDAO task, OrganizationalEntityDAO caller,
                                                  PeopleQueryEvaluator pqe) {
        List<GenericHumanRoleDAO.GenericHumanRoleType> allowedRoles = new ArrayList<GenericHumanRoleDAO.GenericHumanRoleType>();
        allowedRoles.add(GenericHumanRoleDAO.GenericHumanRoleType.BUSINESS_ADMINISTRATORS);
        allowedRoles.add(GenericHumanRoleDAO.GenericHumanRoleType.STAKEHOLDERS);
        return authoriseUser(task, caller, allowedRoles, pqe);
    }

    /**
     * Checks whether the provided user is authorised to setOutput of the given task.
     *
     * @param task   : The TaskDAO object
     * @param caller : The user  being authorised.
     * @param pqe    : The people query evaluator.
     * @return : true if the task has the required state and the user is authorised to perform setOutput
     *         operation.
     */
    public static boolean authorisedToSetOutput(TaskDAO task, OrganizationalEntityDAO caller,
                                                PeopleQueryEvaluator pqe) {
        List<GenericHumanRoleDAO.GenericHumanRoleType> allowedRoles = new ArrayList<GenericHumanRoleDAO.GenericHumanRoleType>();
        allowedRoles.add(GenericHumanRoleDAO.GenericHumanRoleType.ACTUAL_OWNER);
        if (HumanTaskServerHolder.getInstance().getHtServer().getServerConfig().isTaskOperationsForBusinessAdministratorEnabled()) {
            allowedRoles.add(GenericHumanRoleDAO.GenericHumanRoleType.BUSINESS_ADMINISTRATORS);
        }
        return authoriseUser(task, caller, allowedRoles, pqe);
    }

    /**
     * Checks whether the provided user is authorised to set fault for the given task.
     *
     * @param task   : The TaskDAO object
     * @param caller : The user  being authorised.
     * @param pqe    : The people query evaluator.
     * @return : true if the task has the required state and the user is authorised to perform setFault
     *         operation.
     */
    public static boolean authorisedToSetFault(TaskDAO task, OrganizationalEntityDAO caller,
                                               PeopleQueryEvaluator pqe) {
        List<GenericHumanRoleDAO.GenericHumanRoleType> allowedRoles = new ArrayList<GenericHumanRoleDAO.GenericHumanRoleType>();
        allowedRoles.add(GenericHumanRoleDAO.GenericHumanRoleType.ACTUAL_OWNER);
        if (HumanTaskServerHolder.getInstance().getHtServer().getServerConfig().isTaskOperationsForBusinessAdministratorEnabled()) {
            allowedRoles.add(GenericHumanRoleDAO.GenericHumanRoleType.BUSINESS_ADMINISTRATORS);
        }
        return authoriseUser(task, caller, allowedRoles, pqe);
    }


    /**
     * Checks whether the provided user is authorised to remove the given task.
     *
     * @param task   : The TaskDAO object
     * @param caller : The user  being authorised.
     * @param pqe    : The people query evaluator.
     * @return : true if the task is in the required state and the user is authorised to perform remove
     *         operation.
     */
    public static boolean authorisedToRemove(TaskDAO task, OrganizationalEntityDAO caller,
                                             PeopleQueryEvaluator pqe) {

        if (!TaskType.NOTIFICATION.equals(task.getType())) {
            return false;
        }

        if (!TaskStatus.READY.equals(task.getStatus())) {
            return false;
        }

        List<GenericHumanRoleDAO.GenericHumanRoleType> allowedRoles = new ArrayList<GenericHumanRoleDAO.GenericHumanRoleType>();
        allowedRoles.add(GenericHumanRoleDAO.GenericHumanRoleType.BUSINESS_ADMINISTRATORS);
        return authoriseUser(task, caller, allowedRoles, pqe);
    }

    /**
     * Checks whether the provided user is authorised to resume the given task.
     *
     * @param task   : The TaskDAO object
     * @param caller : The user  being authorised.
     * @param pqe    : The people query evaluator.
     * @return : true if the task has the required state and the user is authorised to perform resume
     *         operation.
     */
    public static boolean authorisedToResume(TaskDAO task, OrganizationalEntityDAO caller,
                                             PeopleQueryEvaluator pqe) {

        if (!TaskStatus.SUSPENDED.equals(task.getStatus())) {
            return false;
        }

        if (task.getStatusBeforeSuspension() == null) {
            return false;
        }

        if (!TaskStatus.IN_PROGRESS.equals(task.getStatusBeforeSuspension()) &&
                !TaskStatus.READY.equals(task.getStatusBeforeSuspension()) &&
                !TaskStatus.RESERVED.equals(task.getStatusBeforeSuspension())) {
            return false;
        }

        List<GenericHumanRoleDAO.GenericHumanRoleType> allowedRoles = new ArrayList<GenericHumanRoleDAO.GenericHumanRoleType>();
        allowedRoles.add(GenericHumanRoleDAO.GenericHumanRoleType.ACTUAL_OWNER);
        allowedRoles.add(GenericHumanRoleDAO.GenericHumanRoleType.BUSINESS_ADMINISTRATORS);
        allowedRoles.add(GenericHumanRoleDAO.GenericHumanRoleType.STAKEHOLDERS);
        allowedRoles.add(GenericHumanRoleDAO.GenericHumanRoleType.POTENTIAL_OWNERS);
        return authoriseUser(task, caller, allowedRoles, pqe);
    }

    /**
     * Checks whether the provided user is authorised to release the given task.
     *
     * @param task   : The TaskDAO object
     * @param caller : The user  being authorised.
     * @param pqe    : The people query evaluator.
     * @return : true if the task has the required state and the user is authorised to perform release
     *         operation.
     */
    public static boolean authorisedToRelease(TaskDAO task, OrganizationalEntityDAO caller,
                                              PeopleQueryEvaluator pqe) {
        List<GenericHumanRoleDAO.GenericHumanRoleType> allowedRoles = new ArrayList<GenericHumanRoleDAO.GenericHumanRoleType>();
        if (TaskStatus.IN_PROGRESS.equals(task.getStatus()) ||
                TaskStatus.RESERVED.equals(task.getStatus())) {
            allowedRoles.add(GenericHumanRoleDAO.GenericHumanRoleType.ACTUAL_OWNER);
            if (HumanTaskServerHolder.getInstance().getHtServer().getServerConfig().isTaskOperationsForBusinessAdministratorEnabled()) {
                allowedRoles.add(GenericHumanRoleDAO.GenericHumanRoleType.BUSINESS_ADMINISTRATORS);
            }
            return authoriseUser(task, caller, allowedRoles, pqe);
        } else {
            return false;
        }
    }

    /**
     * Checks whether the provided user is authorised to nominate the given task.
     *
     * @param task   : The TaskDAO object
     * @param caller : The user  being authorised.
     * @param pqe    : The people query evaluator.
     * @return : true if the task has the required state and the user is authorised to perform nominate
     *         operation.
     */
    public static boolean authorisedToNominate(TaskDAO task, OrganizationalEntityDAO caller,
                                               PeopleQueryEvaluator pqe) {

        if (!TaskStatus.CREATED.equals(task.getStatus())) {
            return false;
        }

        List<GenericHumanRoleDAO.GenericHumanRoleType> allowedRoles = new ArrayList<GenericHumanRoleDAO.GenericHumanRoleType>();
        allowedRoles.add(GenericHumanRoleDAO.GenericHumanRoleType.BUSINESS_ADMINISTRATORS);
        return authoriseUser(task, caller, allowedRoles, pqe);
    }

    /**
     * Checks whether the provided user is authorised to get the input of the task.
     *
     * @param task   : The TaskDAO object
     * @param caller : The user  being authorised.
     * @param pqe    : The people query evaluator.
     * @return : true if the task has the required state and the user is authorised to get the task input.
     */
    public static boolean authorisedToGetInput(TaskDAO task, OrganizationalEntityDAO caller,
                                               PeopleQueryEvaluator pqe) {
        List<GenericHumanRoleDAO.GenericHumanRoleType> allowedRoles = new ArrayList<GenericHumanRoleDAO.GenericHumanRoleType>();
        allowedRoles.add(GenericHumanRoleDAO.GenericHumanRoleType.ACTUAL_OWNER);
        allowedRoles.add(GenericHumanRoleDAO.GenericHumanRoleType.POTENTIAL_OWNERS);
        allowedRoles.add(GenericHumanRoleDAO.GenericHumanRoleType.BUSINESS_ADMINISTRATORS);
        allowedRoles.add(GenericHumanRoleDAO.GenericHumanRoleType.STAKEHOLDERS);
        allowedRoles.add(GenericHumanRoleDAO.GenericHumanRoleType.TASK_INITIATOR);
        return authoriseUser(task, caller, allowedRoles, pqe);
    }

    /**
     * Checks whether the provided user is authorised to get task description.
     *
     * @param task   : The TaskDAO object
     * @param caller : The user  being authorised.
     * @param pqe    : The people query evaluator.
     * @return : true if the task has the required state and the user is authorised to get description.
     */
    public static boolean authorisedToGetDescription(TaskDAO task, OrganizationalEntityDAO caller,
                                                     PeopleQueryEvaluator pqe) {
        List<GenericHumanRoleDAO.GenericHumanRoleType> allowedRoles = new ArrayList<GenericHumanRoleDAO.GenericHumanRoleType>();
        allowedRoles.add(GenericHumanRoleDAO.GenericHumanRoleType.ACTUAL_OWNER);
        allowedRoles.add(GenericHumanRoleDAO.GenericHumanRoleType.POTENTIAL_OWNERS);
        allowedRoles.add(GenericHumanRoleDAO.GenericHumanRoleType.BUSINESS_ADMINISTRATORS);
        allowedRoles.add(GenericHumanRoleDAO.GenericHumanRoleType.STAKEHOLDERS);
        allowedRoles.add(GenericHumanRoleDAO.GenericHumanRoleType.TASK_INITIATOR);
        allowedRoles.add(GenericHumanRoleDAO.GenericHumanRoleType.NOTIFICATION_RECIPIENTS);
        return authoriseUser(task, caller, allowedRoles, pqe);
    }

    /**
     * Checks whether the provided user is authorised to get comments of the task.
     *
     * @param task   : The TaskDAO object
     * @param caller : The user  being authorised.
     * @param pqe    : The people query evaluator.
     * @return : true if the task has the required state and the user is authorised to get comments.
     */
    public static boolean authorisedToGetComments(TaskDAO task, OrganizationalEntityDAO caller,
                                                  PeopleQueryEvaluator pqe) {
        List<GenericHumanRoleDAO.GenericHumanRoleType> allowedRoles = new ArrayList<GenericHumanRoleDAO.GenericHumanRoleType>();
        allowedRoles.add(GenericHumanRoleDAO.GenericHumanRoleType.ACTUAL_OWNER);
        allowedRoles.add(GenericHumanRoleDAO.GenericHumanRoleType.POTENTIAL_OWNERS);
        allowedRoles.add(GenericHumanRoleDAO.GenericHumanRoleType.BUSINESS_ADMINISTRATORS);
        allowedRoles.add(GenericHumanRoleDAO.GenericHumanRoleType.STAKEHOLDERS);
        return authoriseUser(task, caller, allowedRoles, pqe);
    }

    /**
     * Checks whether the provided user is authorised to forward the given task.
     *
     * @param task   : The TaskDAO object
     * @param caller : The user  being authorised.
     * @param pqe    : The people query evaluator.
     * @return : true if the task has the required state and the user is authorised to perform forward
     *         operation.
     */
    public static boolean authorisedToForward(TaskDAO task, OrganizationalEntityDAO caller,
                                              PeopleQueryEvaluator pqe) {
        List<GenericHumanRoleDAO.GenericHumanRoleType> allowedRoles = new ArrayList<GenericHumanRoleDAO.GenericHumanRoleType>();
        return authoriseUser(task, caller, allowedRoles, pqe);
    }

    /**
     * Checks whether the provided user is authorised to fail the given task.
     *
     * @param task   : The TaskDAO object
     * @param caller : The user  being authorised.
     * @param pqe    : The people query evaluator.
     * @return : true if the task has the required state and the user is authorised to perform fail
     *         operation.
     */
    public static boolean authorisedToFail(TaskDAO task, OrganizationalEntityDAO caller,
                                           PeopleQueryEvaluator pqe) {
        if (!TaskStatus.IN_PROGRESS.equals(task.getStatus())) {
            return false;
        }
        List<GenericHumanRoleDAO.GenericHumanRoleType> allowedRoles = new ArrayList<GenericHumanRoleDAO.GenericHumanRoleType>();
        allowedRoles.add(GenericHumanRoleDAO.GenericHumanRoleType.ACTUAL_OWNER);
        if (HumanTaskServerHolder.getInstance().getHtServer().getServerConfig().isTaskOperationsForBusinessAdministratorEnabled()) {
            allowedRoles.add(GenericHumanRoleDAO.GenericHumanRoleType.BUSINESS_ADMINISTRATORS);
        }
        return authoriseUser(task, caller, allowedRoles, pqe);
    }

    /**
     * Checks whether the provided user is authorised to exit the given task.
     *
     * @param task   : The TaskDAO object
     * @param caller : The user  being authorised.
     * @param pqe    : The people query evaluator.
     * @return : true if the task has the required state and the user is authorised to perform exit
     *         operation.
     */
    public static boolean authorisedToExit(TaskDAO task, OrganizationalEntityDAO caller,
                                           PeopleQueryEvaluator pqe) {
        if (TaskStatus.EXITED.equals(task.getStatus()) || TaskStatus.ERROR.equals(task.getStatus())
                || TaskStatus.FAILED.equals(task.getStatus()) || TaskStatus.OBSOLETE.equals(task.getStatus()) ||
                TaskStatus.COMPLETED.equals(task.getStatus())) {
            return false;
        }
        List<GenericHumanRoleDAO.GenericHumanRoleType> allowedRoles = new ArrayList<GenericHumanRoleDAO.GenericHumanRoleType>();

        return authoriseUser(task, caller, allowedRoles, pqe);
    }

    /**
     * Checks whether the provided user is authorised to delete output of the given task.
     *
     * @param task   : The TaskDAO object
     * @param caller : The user  being authorised.
     * @param pqe    : The people query evaluator.
     * @return : true if the task has the required state and the user is authorised to delete comment.
     */
    public static boolean authorisedToDeleteOutput(TaskDAO task, OrganizationalEntityDAO caller,
                                                   PeopleQueryEvaluator pqe) {

        if (!TaskStatus.IN_PROGRESS.equals(task.getStatus())) {
            return false;
        }
        List<GenericHumanRoleDAO.GenericHumanRoleType> allowedRoles = new ArrayList<GenericHumanRoleDAO.GenericHumanRoleType>();
        allowedRoles.add(GenericHumanRoleDAO.GenericHumanRoleType.ACTUAL_OWNER);
        if (HumanTaskServerHolder.getInstance().getHtServer().getServerConfig().isTaskOperationsForBusinessAdministratorEnabled()) {
            allowedRoles.add(GenericHumanRoleDAO.GenericHumanRoleType.BUSINESS_ADMINISTRATORS);
        }
        return authoriseUser(task, caller, allowedRoles, pqe);
    }

    /**
     * Checks whether the provided user is authorised to delete a comment from the given task.
     *
     * @param task   : The TaskDAO object
     * @param caller : The user  being authorised.
     * @param pqe    : The people query evaluator.
     * @return : true if the task has the required state and the user is authorised to delete comment operation.
     */
    public static boolean authorisedToDeleteComment(TaskDAO task, OrganizationalEntityDAO caller,
                                                    PeopleQueryEvaluator pqe) {
        List<GenericHumanRoleDAO.GenericHumanRoleType> allowedRoles = new ArrayList<GenericHumanRoleDAO.GenericHumanRoleType>();
        allowedRoles.add(GenericHumanRoleDAO.GenericHumanRoleType.ACTUAL_OWNER);
        allowedRoles.add(GenericHumanRoleDAO.GenericHumanRoleType.POTENTIAL_OWNERS);
        allowedRoles.add(GenericHumanRoleDAO.GenericHumanRoleType.BUSINESS_ADMINISTRATORS);
        allowedRoles.add(GenericHumanRoleDAO.GenericHumanRoleType.STAKEHOLDERS);
        return authoriseUser(task, caller, allowedRoles, pqe);
    }

    /**
     * Checks whether the provided user is authorised to delete fault of the given task.
     *
     * @param task   : The TaskDAO object
     * @param caller : The user  being authorised.
     * @param pqe    : The people query evaluator.
     * @return : true if the task has the required state and the user is authorised to delete tasl fault.
     */
    public static boolean authorisedToDeleteFault(TaskDAO task, OrganizationalEntityDAO caller,
                                                  PeopleQueryEvaluator pqe) {
        if (!TaskStatus.IN_PROGRESS.equals(task.getStatus())) {
            return false;
        }
        List<GenericHumanRoleDAO.GenericHumanRoleType> allowedRoles = new ArrayList<GenericHumanRoleDAO.GenericHumanRoleType>();
        allowedRoles.add(GenericHumanRoleDAO.GenericHumanRoleType.ACTUAL_OWNER);
        if (HumanTaskServerHolder.getInstance().getHtServer().getServerConfig().isTaskOperationsForBusinessAdministratorEnabled()) {
            allowedRoles.add(GenericHumanRoleDAO.GenericHumanRoleType.BUSINESS_ADMINISTRATORS);
        }
        return authoriseUser(task, caller, allowedRoles, pqe);
    }

    /**
     * Checks whether the provided user is authorised to delegate the given task.
     *
     * @param task             : The TaskDAO object
     * @param operationInvoker : The user  being authorised.
     * @param pqe              : The people query evaluator.
     * @return : true if the task has the required state and the user is authorised to perform delegate
     *         operation.
     */
    public static boolean authorisedToDelegate(TaskDAO task,
                                               OrganizationalEntityDAO operationInvoker,
                                               PeopleQueryEvaluator pqe) {

        if (TaskStatus.READY.equals(task.getStatus()) || TaskStatus.IN_PROGRESS.equals(task.getStatus()) ||
            TaskStatus.RESERVED.equals(task.getStatus())) {

            // If there are no users qualified for the task to be assigned, then fail the authorisation.
            List<String> assignableUsersWithoutActualOwner = CommonTaskUtil.getAssignableUserNameList(task, true);
            if (assignableUsersWithoutActualOwner.size() < 1) {
                return false;
            }

            List<GenericHumanRoleDAO.GenericHumanRoleType> allowedRoles = new ArrayList<GenericHumanRoleDAO.GenericHumanRoleType>();
            allowedRoles.add(GenericHumanRoleDAO.GenericHumanRoleType.BUSINESS_ADMINISTRATORS);
            allowedRoles.add(GenericHumanRoleDAO.GenericHumanRoleType.ACTUAL_OWNER);
            return authoriseUser(task, operationInvoker, allowedRoles, pqe);

        } else {
            return false;
        }
    }

    /**
     * Checks whether the provided user is authorised to complete the given task.
     *
     * @param task   : The TaskDAO object
     * @param operationInvoker : The user  being authorised.
     * @param pqe    : The people query evaluator.
     * @return : true if the task has the required state and the user is authorised to complete activate
     *         operation.
     */
    public static boolean authorisedToComplete(TaskDAO task, OrganizationalEntityDAO operationInvoker,
                                               PeopleQueryEvaluator pqe) {

        if (!TaskStatus.IN_PROGRESS.equals(task.getStatus())) {
            return false;
        }
        List<GenericHumanRoleDAO.GenericHumanRoleType> allowedRoles = new ArrayList<GenericHumanRoleDAO.GenericHumanRoleType>();
        allowedRoles.add(GenericHumanRoleDAO.GenericHumanRoleType.ACTUAL_OWNER);
        return authoriseUser(task, operationInvoker, allowedRoles, pqe);
    }

    /**
     * Checks whether the provided user is authorised to comment on the given task.
     *
     * @param task   : The TaskDAO object
     * @param caller : The user  being authorised.
     * @param pqe    : The people query evaluator.
     * @return : true if the task has the required state and the user is authorised to comment.
     */
    public static boolean authorisedToComment(TaskDAO task, OrganizationalEntityDAO caller,
                                              PeopleQueryEvaluator pqe) {
        List<GenericHumanRoleDAO.GenericHumanRoleType> allowedRoles = new ArrayList<GenericHumanRoleDAO.GenericHumanRoleType>();
        allowedRoles.add(GenericHumanRoleDAO.GenericHumanRoleType.ACTUAL_OWNER);
        allowedRoles.add(GenericHumanRoleDAO.GenericHumanRoleType.POTENTIAL_OWNERS);
        allowedRoles.add(GenericHumanRoleDAO.GenericHumanRoleType.BUSINESS_ADMINISTRATORS);
        allowedRoles.add(GenericHumanRoleDAO.GenericHumanRoleType.STAKEHOLDERS);
        return authoriseUser(task, caller, allowedRoles, pqe);
    }

    /**
     * Checks whether the provided user is authorised to claim the given task.
     *
     * @param task   : The TaskDAO object
     * @param caller : The user  being authorised.
     * @param pqe    : The people query evaluator.
     * @return : true if the task has the required state and the user is authorised to perform claim
     *         operation.
     */
    public static boolean authorisedToClaim(TaskDAO task, OrganizationalEntityDAO caller,
                                            PeopleQueryEvaluator pqe) {

        if (!TaskStatus.READY.equals(task.getStatus())) {
            return false;
        }
        List<GenericHumanRoleDAO.GenericHumanRoleType> allowedRoles = new ArrayList<GenericHumanRoleDAO.GenericHumanRoleType>();
        allowedRoles.add(GenericHumanRoleDAO.GenericHumanRoleType.POTENTIAL_OWNERS);
        if (HumanTaskServerHolder.getInstance().getHtServer().getServerConfig().isTaskOperationsForBusinessAdministratorEnabled()) {
            allowedRoles.add(GenericHumanRoleDAO.GenericHumanRoleType.BUSINESS_ADMINISTRATORS);
        }
        return authoriseUser(task, caller, allowedRoles, pqe);
    }
}
