/*
 * Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.humantask.core.dao.sql;

import org.wso2.carbon.humantask.core.dao.GenericHumanRoleDAO;
import org.wso2.carbon.humantask.core.dao.SimpleQueryCriteria;
import org.wso2.carbon.humantask.core.dao.TaskStatus;
import org.wso2.carbon.humantask.core.dao.TaskType;
import org.wso2.carbon.humantask.core.engine.PeopleQueryEvaluator;
import org.wso2.carbon.humantask.core.internal.HumanTaskServiceComponent;
import org.wso2.carbon.utils.xml.StringUtils;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A builder class to create SQL statements for the task filtering.
 */
public class HumanTaskJPQLQueryBuilder {

    private static final String SELECT_TASKS = " SELECT t FROM org.wso2.carbon.humantask.core.dao.jpa.openjpa.model.Task t ";
    private static final String SELECT_DISTINCT_TASKS = " SELECT DISTINCT t FROM org.wso2.carbon.humantask.core.dao.jpa.openjpa.model.Task t ";
    private static final String SELECT_DISTINCT_TASKS_COUNT = " SELECT COUNT(DISTINCT t) FROM org.wso2.carbon" +
            ".humantask.core" +
            ".dao.jpa.openjpa.model.Task t ";
    private static final String JOIN_HUMAN_ROLES_JOIN_ORG_ENTITIES = " JOIN t.humanRoles hr JOIN hr.orgEntities oe WHERE  ";
    private static final String OE_NAME_IN_NAMES = " oe.name IN :names ";
    private static final String AND = " AND ";
    private static final String HR_TYPE_ROLE_TYPE = " hr.type = :roleType ";
    private static final String T_TYPE_TASK_TYPE = " t.type = :taskType ";
    private static final String T_TENANT_ID_TENANT_ID = " t.tenantId = :tenantId ";
    private static final String ORDER_BY_T_CREATED_ON_DESC = " ORDER BY t.createdOn DESC ";
    private static final String T_STATUS_IN_TASK_STATUSES = " t.status IN :taskStatuses ";
    private static final String T_STATUS_NOT_IN_TASK_STATUSES = " t.status NOT IN :taskStatuses ";
    private static final String NAMES = "names";
    private static final String TENANT_ID = "tenantId";
    private static final String ROLE_TYPE = "roleType";
    private static final String TASK_TYPE = "taskType";
    private static final String TASK_STATUSES = "taskStatuses";
    //private static final String FILTER_BY_TASKNAME = " t.name = :taskName ";
    private static final String FILTER_BY_TASKNAME = " t.taskDefinitionName = :taskName ";
    private static final String TASK_NAME = "taskName";

    private static final String ORDER_BY = " ORDER BY ";
    private static final String DESC = " DESC ";
    private static final String ASC = " ASC ";
    private static final String COLUMN_TASK_NAME = " t.taskDefinitionName ";
    private static final String COLUMN_CREATED_DATE = " t.createdOn ";
    private static final String COLUMN_UPDATED_DATE = " t.updatedOn ";
    private static final String COLUMN_PRIORITY = " t.priority ";
    private static final String COLUMN_STATUS = " t.status ";

    private SimpleQueryCriteria queryCriteria;

    private EntityManager em;

    /**
     * @param criteria : The query criteria on which the tasks will be filtered.
     * @param em       : The EntityManager which would create the Query object based on the JPQL string.
     */
    public HumanTaskJPQLQueryBuilder(SimpleQueryCriteria criteria, EntityManager em) {
        this.queryCriteria = criteria;
        this.em = em;
    }

    /**
     * Build the specific sql query based on the query type.
     *
     * @return : The sql query
     */
    public Query build() {
        switch (queryCriteria.getSimpleQueryType()) {
            case ASSIGNED_TO_ME:
                return buildAssignedToMeQuery();
            case ASSIGNABLE:
                return buildAdministrableTasksQuery();
            case CLAIMABLE:
                return buildClaimableQuery();
            case ALL_TASKS:
                return buildAllTasksQuery();
            case NOTIFICATIONS:
                return buildNotificationsQuery();
            case REMOVE_TASKS:
                return buildRemoveTasksQuery();
        }
        return null;
    }

    public Query buildCount() {
        switch (queryCriteria.getSimpleQueryType()) {
            case ASSIGNED_TO_ME:
                return buildAssignedToMeCountQuery();
            case ASSIGNABLE:
                return buildAdministrableTasksCountQuery();
            case CLAIMABLE:
                return buildClaimableCountQuery();
            case ALL_TASKS:
                return buildAllTasksCountQuery();
            case NOTIFICATIONS:
                return buildNotificationsCountQuery();
            case REMOVE_TASKS:
                break;
            case ADVANCED:
                break;
        }
        return null;
    }

    private Query buildAdministrableTasksQuery() {

        boolean hasStatus = false;
        List<TaskStatus> statuses = queryCriteria.getStatuses();
        if (statuses != null && !statuses.isEmpty()) {
            hasStatus = true;
        }

        boolean hasTaskName = false;
        if (!StringUtils.isEmpty(queryCriteria.getTaskName())) {
            hasTaskName = true;
        }
        Query administrableTasksQuery = em.createQuery(SELECT_DISTINCT_TASKS +
                JOIN_HUMAN_ROLES_JOIN_ORG_ENTITIES +
                OE_NAME_IN_NAMES +
                AND +
                HR_TYPE_ROLE_TYPE + (hasTaskName ? AND + FILTER_BY_TASKNAME : "") +
                AND +
                T_TYPE_TASK_TYPE +
                AND +
                T_TENANT_ID_TENANT_ID +
                (hasStatus ? AND + T_STATUS_IN_TASK_STATUSES : "") +
                generateOrderedByQuery()).setMaxResults(queryCriteria.getPageSize()).setFirstResult(queryCriteria
                .getPageSize() * queryCriteria.getPageNumber());

        List<String> rolesAndNamesList = getNameListForUser(queryCriteria.getCaller(), true);
        administrableTasksQuery.setParameter(NAMES, rolesAndNamesList);
        administrableTasksQuery.setParameter(TENANT_ID, queryCriteria.getCallerTenantId());
        administrableTasksQuery.setParameter(ROLE_TYPE, GenericHumanRoleDAO.GenericHumanRoleType.BUSINESS_ADMINISTRATORS);
        administrableTasksQuery.setParameter(TASK_TYPE, TaskType.TASK);
        if (hasStatus) {
            administrableTasksQuery.setParameter(TASK_STATUSES, statuses);
        }
        if (hasTaskName) {
            administrableTasksQuery.setParameter(TASK_NAME, queryCriteria.getTaskName());
        }
        return administrableTasksQuery;
    }

    private Query buildClaimableQuery() {

        boolean hasStatus = false;
        List<TaskStatus> statuses = queryCriteria.getStatuses();
        if (statuses != null && !statuses.isEmpty()) {
            hasStatus = true;
        }
        boolean hasTaskName = false;
        if (!StringUtils.isEmpty(queryCriteria.getTaskName())) {
            hasTaskName = true;
        }
        StringBuilder query = new StringBuilder(SELECT_DISTINCT_TASKS +
                JOIN_HUMAN_ROLES_JOIN_ORG_ENTITIES +
                OE_NAME_IN_NAMES +
                AND +
                HR_TYPE_ROLE_TYPE + (hasTaskName ? AND + FILTER_BY_TASKNAME : "") +
                AND +
                T_TYPE_TASK_TYPE +
                AND +
                T_TENANT_ID_TENANT_ID +
                AND +
                T_STATUS_IN_TASK_STATUSES +
                generateOrderedByQuery());

        Query claimableTasksQuery = em.createQuery(query.toString()).setMaxResults(queryCriteria.getPageSize()).setFirstResult(queryCriteria
                .getPageSize() * queryCriteria.getPageNumber());

        List<String> rolesAndNamesList = getNameListForUser(queryCriteria.getCaller(), true);
        claimableTasksQuery.setParameter(NAMES, rolesAndNamesList);
        claimableTasksQuery.setParameter(TENANT_ID, queryCriteria.getCallerTenantId());
        claimableTasksQuery.setParameter(ROLE_TYPE, GenericHumanRoleDAO.GenericHumanRoleType.POTENTIAL_OWNERS);
        claimableTasksQuery.setParameter(TASK_TYPE, TaskType.TASK);

        if (hasStatus) {
            claimableTasksQuery.setParameter(TASK_STATUSES, statuses);
        } else {
            List<TaskStatus> statusList = Arrays.asList(TaskStatus.READY);
            claimableTasksQuery.setParameter(TASK_STATUSES, statusList);
        }

        if (hasTaskName) {
            claimableTasksQuery.setParameter(TASK_NAME, queryCriteria.getTaskName());
        }

        return claimableTasksQuery;
    }

    private Query buildAllTasksQuery() {
        boolean hasStatus = false;
        List<TaskStatus> statuses = queryCriteria.getStatuses();
        if (statuses != null && !statuses.isEmpty()) {
            hasStatus = true;
        }
        boolean hasTaskName = false;
        if (!StringUtils.isEmpty(queryCriteria.getTaskName())) {
            hasTaskName = true;
        }
        Query allTasksQuery = em.createQuery(" SELECT DISTINCT t FROM org.wso2.carbon.humantask.core.dao.jpa.openjpa.model.Task t " +
                JOIN_HUMAN_ROLES_JOIN_ORG_ENTITIES +
                OE_NAME_IN_NAMES +
                AND +
                (hasStatus ? T_STATUS_IN_TASK_STATUSES + AND  : "") +
                T_TYPE_TASK_TYPE + (hasTaskName ? AND + FILTER_BY_TASKNAME : "") +
                AND +
                T_TENANT_ID_TENANT_ID +
                generateOrderedByQuery()).setMaxResults(queryCriteria.getPageSize()).setFirstResult(queryCriteria
                .getPageSize() * queryCriteria.getPageNumber());

        List<String> rolesAndNamesList = getNameListForUser(queryCriteria.getCaller(), true);
        allTasksQuery.setParameter(NAMES, rolesAndNamesList);
        allTasksQuery.setParameter(TENANT_ID, queryCriteria.getCallerTenantId());
        allTasksQuery.setParameter(TASK_TYPE, TaskType.TASK);
        if (hasStatus) {
            allTasksQuery.setParameter(TASK_STATUSES, statuses);
        }
        if (hasTaskName) {
            allTasksQuery.setParameter(TASK_NAME, queryCriteria.getTaskName());
        }
        return allTasksQuery;
    }

    //Creates the JPQL query to list tasks assigned for the particular user.
    private Query buildAssignedToMeQuery() {
        boolean hasStatus = false;
        List<TaskStatus> statuses = queryCriteria.getStatuses();
        if (statuses != null && !statuses.isEmpty()) {
            hasStatus = true;
        }
        boolean hasTaskName = false;
        if (!StringUtils.isEmpty(queryCriteria.getTaskName())) {
            hasTaskName = true;
        }
        Query assignedToMeQuery = em.createQuery(SELECT_DISTINCT_TASKS +
                JOIN_HUMAN_ROLES_JOIN_ORG_ENTITIES +
                " oe.name = :name " +
                AND +
                HR_TYPE_ROLE_TYPE +
                AND +
                T_TENANT_ID_TENANT_ID +
                AND +
                T_TYPE_TASK_TYPE + (hasTaskName ? AND + FILTER_BY_TASKNAME : "") +
                AND +
                T_STATUS_IN_TASK_STATUSES +
                generateOrderedByQuery()).setMaxResults(queryCriteria.getPageSize()).setFirstResult(queryCriteria
                .getPageSize() * queryCriteria.getPageNumber());

        assignedToMeQuery.setParameter(TENANT_ID, queryCriteria.getCallerTenantId());
        assignedToMeQuery.setParameter("name", queryCriteria.getCaller());
        assignedToMeQuery.setParameter(TASK_TYPE, TaskType.TASK);
        assignedToMeQuery.setParameter(ROLE_TYPE, GenericHumanRoleDAO.GenericHumanRoleType.ACTUAL_OWNER);

        if (hasStatus) {
            assignedToMeQuery.setParameter(TASK_STATUSES, statuses);
        } else {
            List<TaskStatus> statusList = Arrays.asList(TaskStatus.RESERVED,
                    TaskStatus.IN_PROGRESS,
                    TaskStatus.SUSPENDED);
            assignedToMeQuery.setParameter(TASK_STATUSES, statusList);
        }
        if (hasTaskName) {
            assignedToMeQuery.setParameter(TASK_NAME, queryCriteria.getTaskName());
        }
        return assignedToMeQuery;
    }

    private Query buildRemoveTasksQuery() {
        boolean hasStatus = false;
        List<TaskStatus> statuses = queryCriteria.getStatuses();
        if (statuses != null && !statuses.isEmpty()) {
            hasStatus = true;
        }
        boolean hasTaskName = false;
        if (!StringUtils.isEmpty(queryCriteria.getTaskName())) {
            hasTaskName = true;
        }

        Query removeTasksQuery = em.createQuery(" DELETE FROM  org.wso2.carbon.humantask.core.dao.jpa.openjpa.model.Task t  " +
                " WHERE t.status in :removableStatuses" + (hasTaskName ? AND + FILTER_BY_TASKNAME : ""));

        removeTasksQuery.setParameter("removableStatuses", queryCriteria.getStatuses());

        if (hasTaskName) {
            removeTasksQuery.setParameter(TASK_NAME, queryCriteria.getTaskName());
        }
        return removeTasksQuery;
    }


    //Creates the JPQL query to list notifications applicable for a particular user.
    private Query buildNotificationsQuery() {
        boolean hasStatus = false;
        List<TaskStatus> statuses = queryCriteria.getStatuses();
        if (statuses != null && !statuses.isEmpty()) {
            hasStatus = true;
        }
        boolean hasTaskName = false;
        if (!StringUtils.isEmpty(queryCriteria.getTaskName())) {
            hasTaskName = true;
        }

        Query notificationsQuery = em.createQuery(SELECT_DISTINCT_TASKS +
                JOIN_HUMAN_ROLES_JOIN_ORG_ENTITIES +
                OE_NAME_IN_NAMES +
                AND +
                HR_TYPE_ROLE_TYPE +
                AND +
                T_TENANT_ID_TENANT_ID +
                AND +
                T_TYPE_TASK_TYPE + (hasTaskName ? AND + FILTER_BY_TASKNAME : "") +
                (hasStatus ? AND + T_STATUS_IN_TASK_STATUSES : "") +
                generateOrderedByQuery()).setMaxResults(queryCriteria.getPageSize()).setFirstResult(queryCriteria
                .getPageSize() * queryCriteria.getPageNumber());

        notificationsQuery.setParameter(TENANT_ID, queryCriteria.getCallerTenantId());
        List<String> rolesAndNamesList = getNameListForUser(queryCriteria.getCaller(), true);
        notificationsQuery.setParameter(NAMES, rolesAndNamesList);
        notificationsQuery.setParameter(ROLE_TYPE, GenericHumanRoleDAO.GenericHumanRoleType.NOTIFICATION_RECIPIENTS);
        notificationsQuery.setParameter(TASK_TYPE, TaskType.NOTIFICATION);

        if (hasTaskName) {
            notificationsQuery.setParameter(TASK_NAME, queryCriteria.getTaskName());
        }
        if (hasStatus) {
            notificationsQuery.setParameter(TASK_STATUSES, statuses);
        }
        return notificationsQuery;
    }


    private Query buildAdministrableTasksCountQuery() {

        boolean hasStatus = false;
        List<TaskStatus> statuses = queryCriteria.getStatuses();
        if (statuses != null && !statuses.isEmpty()) {
            hasStatus = true;
        }
        boolean hasTaskName = false;
        if (!StringUtils.isEmpty(queryCriteria.getTaskName())) {
            hasTaskName = true;
        }

        Query administrableTasksQuery = em.createQuery(SELECT_DISTINCT_TASKS_COUNT +
                JOIN_HUMAN_ROLES_JOIN_ORG_ENTITIES +
                OE_NAME_IN_NAMES +
                AND +
                HR_TYPE_ROLE_TYPE + (hasTaskName ? AND + FILTER_BY_TASKNAME : "") +
                AND +
                T_TYPE_TASK_TYPE +
                AND +
                T_TENANT_ID_TENANT_ID +
                (hasStatus ? AND + T_STATUS_IN_TASK_STATUSES : ""));

        List<String> rolesAndNamesList = getNameListForUser(queryCriteria.getCaller(), true);
        administrableTasksQuery.setParameter(NAMES, rolesAndNamesList);
        administrableTasksQuery.setParameter(TENANT_ID, queryCriteria.getCallerTenantId());
        administrableTasksQuery.setParameter(ROLE_TYPE, GenericHumanRoleDAO.GenericHumanRoleType.BUSINESS_ADMINISTRATORS);
        administrableTasksQuery.setParameter(TASK_TYPE, TaskType.TASK);
        if (hasStatus) {
            administrableTasksQuery.setParameter(TASK_STATUSES, statuses);
        }
        if (hasTaskName) {
            administrableTasksQuery.setParameter(TASK_NAME, queryCriteria.getTaskName());
        }
        return administrableTasksQuery;
    }

    private Query buildClaimableCountQuery() {

        boolean hasStatus = false;
        List<TaskStatus> statuses = queryCriteria.getStatuses();
        if (statuses != null && !statuses.isEmpty()) {
            hasStatus = true;
        }
        boolean hasTaskName = false;
        if (!StringUtils.isEmpty(queryCriteria.getTaskName())) {
            hasTaskName = true;
        }

        StringBuilder query = new StringBuilder(SELECT_DISTINCT_TASKS_COUNT +
                JOIN_HUMAN_ROLES_JOIN_ORG_ENTITIES +
                OE_NAME_IN_NAMES +
                AND +
                HR_TYPE_ROLE_TYPE + (hasTaskName ? AND + FILTER_BY_TASKNAME : "") +
                AND +
                T_TYPE_TASK_TYPE +
                AND +
                T_TENANT_ID_TENANT_ID +
                AND +
                T_STATUS_IN_TASK_STATUSES);

        Query claimableTasksQuery = em.createQuery(query.toString());

        List<String> rolesAndNamesList = getNameListForUser(queryCriteria.getCaller(), true);
        claimableTasksQuery.setParameter(NAMES, rolesAndNamesList);
        claimableTasksQuery.setParameter(TENANT_ID, queryCriteria.getCallerTenantId());
        claimableTasksQuery.setParameter(ROLE_TYPE, GenericHumanRoleDAO.GenericHumanRoleType.POTENTIAL_OWNERS);
        claimableTasksQuery.setParameter(TASK_TYPE, TaskType.TASK);
        List<TaskStatus> statusList = Arrays.asList(TaskStatus.READY);
        claimableTasksQuery.setParameter(TASK_STATUSES, statusList);
        if (hasStatus) {
            claimableTasksQuery.setParameter(TASK_STATUSES, statuses);
        } else {
            List<TaskStatus> readyList = Arrays.asList(TaskStatus.READY);
            claimableTasksQuery.setParameter(TASK_STATUSES, readyList);
        }

        if (hasTaskName) {
            claimableTasksQuery.setParameter(TASK_NAME, queryCriteria.getTaskName());
        }

        return claimableTasksQuery;
    }

    private Query buildAllTasksCountQuery() {

        boolean hasStatus = false;
        List<TaskStatus> statuses = queryCriteria.getStatuses();
        if (statuses != null && !statuses.isEmpty()) {
            hasStatus = true;
        }
        boolean hasTaskName = false;
        if (!StringUtils.isEmpty(queryCriteria.getTaskName())) {
            hasTaskName = true;
        }

        Query allTasksQuery = em.createQuery(" SELECT COUNT(DISTINCT t) FROM org.wso2.carbon.humantask.core.dao.jpa" +
                ".openjpa.model.Task t " +
                JOIN_HUMAN_ROLES_JOIN_ORG_ENTITIES +
                OE_NAME_IN_NAMES +
                AND +
                (hasStatus ? T_STATUS_IN_TASK_STATUSES + AND : "") +
                T_TYPE_TASK_TYPE + (hasTaskName ? AND + FILTER_BY_TASKNAME : "") +
                AND +
                T_TENANT_ID_TENANT_ID);

        List<String> rolesAndNamesList = getNameListForUser(queryCriteria.getCaller(), true);
        allTasksQuery.setParameter(NAMES, rolesAndNamesList);
        allTasksQuery.setParameter(TENANT_ID, queryCriteria.getCallerTenantId());
        allTasksQuery.setParameter(TASK_TYPE, TaskType.TASK);
        if (hasStatus) {
            allTasksQuery.setParameter(TASK_STATUSES, statuses);
        }
        if (hasTaskName) {
            allTasksQuery.setParameter(TASK_NAME, queryCriteria.getTaskName());
        }
        return allTasksQuery;
    }

    //Creates the JPQL query to list tasks assigned for the particular user.
    private Query buildAssignedToMeCountQuery() {
        boolean hasStatus = false;
        List<TaskStatus> statuses = queryCriteria.getStatuses();
        if (statuses != null && !statuses.isEmpty()) {
            hasStatus = true;
        }
        boolean hasTaskName = false;
        if (!StringUtils.isEmpty(queryCriteria.getTaskName())) {
            hasTaskName = true;
        }

        Query assignedToMeQuery = em.createQuery(SELECT_DISTINCT_TASKS_COUNT +
                JOIN_HUMAN_ROLES_JOIN_ORG_ENTITIES +
                " oe.name = :name " +
                AND +
                HR_TYPE_ROLE_TYPE +
                AND +
                T_TENANT_ID_TENANT_ID +
                AND +
                T_TYPE_TASK_TYPE + (hasTaskName ? AND + FILTER_BY_TASKNAME : "") +
                AND +
                T_STATUS_IN_TASK_STATUSES);

        assignedToMeQuery.setParameter(TENANT_ID, queryCriteria.getCallerTenantId());
        assignedToMeQuery.setParameter("name", queryCriteria.getCaller());
        assignedToMeQuery.setParameter(TASK_TYPE, TaskType.TASK);
        assignedToMeQuery.setParameter(ROLE_TYPE, GenericHumanRoleDAO.GenericHumanRoleType.ACTUAL_OWNER);

        if (hasStatus) {
            assignedToMeQuery.setParameter(TASK_STATUSES, statuses);
        } else {
        List<TaskStatus> statusList = Arrays.asList(TaskStatus.RESERVED,
                TaskStatus.IN_PROGRESS,
                TaskStatus.SUSPENDED);
        assignedToMeQuery.setParameter(TASK_STATUSES, statusList);
        }
        if (hasTaskName) {
            assignedToMeQuery.setParameter(TASK_NAME, queryCriteria.getTaskName());
        }

        return assignedToMeQuery;
    }


    //Creates the JPQL query to list notifications applicable for a particular user.
    private Query buildNotificationsCountQuery() {

        boolean hasStatus = false;
        List<TaskStatus> statuses = queryCriteria.getStatuses();
        if (statuses != null && !statuses.isEmpty()) {
            hasStatus = true;
        }
        boolean hasTaskName = false;
        if (!StringUtils.isEmpty(queryCriteria.getTaskName())) {
            hasTaskName = true;
        }

        Query notificationsQuery = em.createQuery(SELECT_DISTINCT_TASKS_COUNT +
                JOIN_HUMAN_ROLES_JOIN_ORG_ENTITIES +
                OE_NAME_IN_NAMES +
                AND +
                HR_TYPE_ROLE_TYPE +
                AND +
                T_TENANT_ID_TENANT_ID +
                AND +
                T_TYPE_TASK_TYPE + (hasTaskName ? AND + FILTER_BY_TASKNAME : "") +
                (hasStatus ? AND + T_STATUS_IN_TASK_STATUSES : ""));

        notificationsQuery.setParameter(TENANT_ID, queryCriteria.getCallerTenantId());
        List<String> rolesAndNamesList = getNameListForUser(queryCriteria.getCaller(), true);
        notificationsQuery.setParameter(NAMES, rolesAndNamesList);
        notificationsQuery.setParameter(ROLE_TYPE, GenericHumanRoleDAO.GenericHumanRoleType.NOTIFICATION_RECIPIENTS);
        notificationsQuery.setParameter(TASK_TYPE, TaskType.NOTIFICATION);

        if (hasTaskName) {
            notificationsQuery.setParameter(TASK_NAME, queryCriteria.getTaskName());
        }
        if (hasStatus) {
            notificationsQuery.setParameter(TASK_STATUSES, statuses);
        }
        return notificationsQuery;
    }


    private List<String> getNameListForUser(String userName, boolean includeUserName) {
        List<String> roleNameList = new ArrayList<String>();
        if (includeUserName) {
            roleNameList.add(userName);
        }
        PeopleQueryEvaluator peopleQueryEvaluator = HumanTaskServiceComponent.
                getHumanTaskServer().getTaskEngine().getPeopleQueryEvaluator();
        roleNameList.addAll(peopleQueryEvaluator.getRoleNameListForUser(userName));
        return roleNameList;
    }

    /**
     * Generates Ordered by Query. Default we are sorting based on created date.
     *
     * @return
     */
    private String generateOrderedByQuery() {
        String query = "";
        String order = "";
        boolean hasOrdered = false;
        if (queryCriteria.getQueryOrder() != null) {
            hasOrdered = true;
            if (SimpleQueryCriteria.QueryOrder.ASCENDING.equals(queryCriteria.getQueryOrder())) {
                order = ASC;
            } else if (SimpleQueryCriteria.QueryOrder.DESCENDING.equals(queryCriteria.getQueryOrder())) {
                order = DESC;
            }
        } else {
            hasOrdered = false;
        }
        if (queryCriteria.getQueryOrderBy() != null) {
            if (SimpleQueryCriteria.QueryOrderBy.TASK_NAME.equals(queryCriteria.getQueryOrderBy())) {
                query = ORDER_BY + COLUMN_TASK_NAME + order + " , " + COLUMN_CREATED_DATE;
            } else if (SimpleQueryCriteria.QueryOrderBy.PRIORITY.equals(queryCriteria.getQueryOrderBy())) {
                query = ORDER_BY + COLUMN_PRIORITY + order + " , " + COLUMN_CREATED_DATE;
            } else if (SimpleQueryCriteria.QueryOrderBy.UPDATED_DATE.equals(queryCriteria.getQueryOrderBy())) {
                query = ORDER_BY + COLUMN_UPDATED_DATE + order;
            } else if (SimpleQueryCriteria.QueryOrderBy.CREATED_DATE.equals(queryCriteria.getQueryOrderBy())) {
                query = ORDER_BY + COLUMN_CREATED_DATE + order;
            } else if (SimpleQueryCriteria.QueryOrderBy.CREATED_DATE.equals(queryCriteria.getQueryOrderBy())) {
                query = ORDER_BY + COLUMN_STATUS + order + " , " + COLUMN_CREATED_DATE;
            }
        } else {
            query = (hasOrdered ? ORDER_BY + COLUMN_CREATED_DATE + order : ORDER_BY_T_CREATED_ON_DESC);
        }
        return query;
    }

}
