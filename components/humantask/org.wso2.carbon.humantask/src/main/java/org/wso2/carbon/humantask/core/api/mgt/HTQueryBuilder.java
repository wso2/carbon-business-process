/*
 *
 *   Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 * /
 */

package org.wso2.carbon.humantask.core.api.mgt;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.humantask.core.dao.SimpleQueryCriteria;
import org.wso2.carbon.humantask.core.dao.TaskStatus;
import org.wso2.carbon.humantask.types.TPredefinedStatus;

import javax.persistence.EntityManager;
import javax.persistence.Query;


public class HTQueryBuilder {
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
    private static final String FILTER_BY_TASKNAME = " t.taskDefinitionName = :taskName ";
    private static final String FILTER_BY_TASK_STATUS = " t.status = :status ";
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
    private TPredefinedStatus.Enum queryStatus;
    private TaskStatus taskStatus;
    private String taskName;
    private String taskDefName;
    private static final Log log = LogFactory.getLog(HTQueryBuilder.class);

    /**
     * @param criteria : The query criteria on which the tasks will be filtered.
     * @param em       : The EntityManager which would create the Query object based on the JPQL string.
     */
    public HTQueryBuilder(SimpleQueryCriteria criteria, EntityManager em) {
        this.queryCriteria = criteria;
        this.em = em;
    }

    public HTQueryBuilder() {
    }

    /**
     * @param taskName :Task name
     * @return to count number of instances in each state for a given task
     */
    public Query buildQueryToCountTaskInstances(String taskName) {
        this.taskName = taskName;
        Query query = null;
        return query;
    }


    public Query buildQueryToCountTaskInstancesByTaskName(String taskName,
                                                          TPredefinedStatus.Enum status,
                                                          EntityManager entityManager) {
        this.taskName = taskName;
        this.queryStatus = status;

        taskStatus = TaskStatus.valueOf(status.toString());
        this.em = entityManager;
        Query query = null;
        query = entityManager.createQuery("SELECT DISTINCT t FROM Task t WHERE t.name = :name AND t.status = :status");
        query.setParameter("name", taskName);
        query.setParameter("status", taskStatus);
        return query;
    }

    public Query buildQueryToCountTaskInstancesByTaskDefName(String taskDefName,
                                                             TPredefinedStatus.Enum status,
                                                             EntityManager entityManager) {
        this.taskDefName = taskDefName;
        this.queryStatus = status;
        taskStatus = TaskStatus.valueOf(status.toString());
        this.em = entityManager;
        Query query = null;
        query = entityManager.createQuery("SELECT COUNT (DISTINCT t) FROM Task t WHERE t.taskDefinitionName = :name AND t.status = :status");
        query.setParameter("name", taskDefName);
        query.setParameter("status", taskStatus);
        return query;
    }


    public Query buildQueryToFindTaskInstances(TPredefinedStatus.Enum status,
                                               EntityManager entityManager) {
        this.taskName = taskName;
        this.queryStatus = status;
        taskStatus = TaskStatus.valueOf(status.toString());
        this.em = entityManager;
        Query query = null;
        query = entityManager.createQuery("SELECT DISTINCT t FROM Task t WHERE t.status = :status");
        query.setParameter("status", taskStatus);
        return query;
    }

    public Query buildQueryToFindTaskInstances(String taskName, EntityManager entityManager) {
        this.taskName = taskName;
        this.em = entityManager;
        Query query = null;
        query = entityManager.createQuery("SELECT DISTINCT t FROM Task t WHERE t.name = :name");
        query.setParameter("name", taskName);
        return query;
    }

    public Query buildQueryToGetAllDeployedTasks(EntityManager entityManager) {
        this.em = entityManager;
        Query query = null;
        query = entityManager.createQuery("SELECT DISTINCT du FROM DeploymentUnit du");
        return query;
    }

    public Query buildQueryToGetTaskDefinitionName(String taskPackageName,
                                                   EntityManager entityManager) {
        this.em = entityManager;
        Query query = null;
        query = entityManager.createQuery("SELECT DISTINCT t FROM Task t WHERE t.packageName LIKE '%:duName'");
        query.setParameter("duName", taskPackageName);
        return query;
    }

    public Query getTenantIDs(EntityManager entityManager) {
        Query query = null;
        query = entityManager.createQuery("SELECT DISTINCT du FROM DeploymentUnit du");
        return query;
    }

}
