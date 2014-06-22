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

package org.wso2.carbon.humantask.core.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * The simple query criteria.
 */

public class SimpleQueryCriteria {
    /**
     * The query order criteria.
     */
    public enum QueryOrder {
        ASCENDING,
        DESCENDING
    }

    /**
     * The query order by direction
     */
    public enum QueryOrderBy {
        TASK_NAME,
        CREATED_DATE,
        UPDATED_DATE,
        PRIORITY,
        STATUS
    }

    /**
     * The simple query type
     */
    public enum QueryType {
        /**
         * ASSIGNED_TO_ME - Returns matching tasks, where the current logged-in user
         * is the Actual Owner.
         */
        ASSIGNED_TO_ME,
        /**
         * ASSIGNABLE - Return matching tasks, where the current logged-in user act as a Business Administrator.
         */
        ASSIGNABLE,
        /**
         * CLAIMABLE - Return matching tasks where the current logged-in user can claim
         * (Potential Owner)
         */
        CLAIMABLE,
        /**
         * ALL_TASKS - Return all tasks where the current logged-in user can see.
         */
        ALL_TASKS,
        /**
         * NOTIFICATIONS - Return all Notification where the current logged-in user can see. (Notification recipient)
         */
        NOTIFICATIONS,
        /**
         * REMOVE_TASKS - Used to remove task based on status. External world can't use this queryType since this is
         * not supported by HumanTask Admin API. This is only use for HumanTask cron job.
         */
        REMOVE_TASKS,
        /**
         * ADVANCED - NOT supported yet.
         *
         */
        ADVANCED
    }

    /**
     * The user who's performing the query operations.
     */
    private String caller;

    private int callerTenantId;

    private QueryType simpleQueryType;

    /**
     * task created on date to filter
     */
    private Date createdOn;

    /**
     * The update on date to filter.
     */
    private Date updatedOn;

    /**
     * the created by user name to filter.
     */
    private String createdBy;

    /**
     * the updated by user name to filter.
     */
    private String updatedBy;

    /**
     * filtering task statuses
     */
    private List<TaskStatus> statuses = new ArrayList<TaskStatus>();

    /**
     * the task priority to filter.
     */
    private Integer priority;

    /**
     * pagination page number
     */
    private Integer pageNumber;

    /**
     * pagination page size
     */
    private Integer pageSize;

    /**
     * the task name to filter by
     */
    private String taskName;

    /**
     *  filter to Order Task list
     */
    private QueryOrder queryOrder;

    /**
     * Filter to query order by.
     */
    private QueryOrderBy queryOrderBy;

    /**
     * @return : the created on date.
     */
    public Date getCreatedOn() {
        if (createdOn != null) {
            return (Date) createdOn.clone();
        }
        return null;
    }

    /**
     * @param createdOn : The created on filter.
     */
    public void setCreatedOn(Date createdOn) {
        if (createdOn != null) {
            this.createdOn = (Date) createdOn.clone();
        } else {
            this.createdOn = null;
        }
    }

    /**
     * @return : The updated on date.
     */
    public Date getUpdatedOn() {
        if (updatedOn != null) {
            return (Date) updatedOn.clone();
        }
        return null;
    }

    /**
     * @param updatedOn : The updated on time to set.
     */
    public void setUpdatedOn(Date updatedOn) {
        if (updatedOn != null) {
            this.updatedOn = (Date) updatedOn.clone();
        } else {
            this.updatedOn = null;
        }
    }

    /**
     * @return : The created by user name.
     */
    public String getCreatedBy() {
        return createdBy;
    }

    /**
     * @param createdBy : The created by user name to set.
     */
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    /**
     * @return : the updated by user name.
     */
    public String getUpdatedBy() {
        return updatedBy;
    }

    /**
     * @param updatedBy : the updated by user name to set.
     */
    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    /**
     * @return : The list of filtering statuses.
     */
    public List<TaskStatus> getStatuses() {
        return statuses;
    }

    /**
     * @param statuses : The list of task statuses to be filtered.
     */
    public void setStatuses(List<TaskStatus> statuses) {
        this.statuses = statuses;
    }

    /**
     * @return : The page number for pagination.
     */
    public Integer getPageNumber() {
        return pageNumber;
    }

    /**
     * @param pageNumber : the page number to set.
     */
    public void setPageNumber(Integer pageNumber) {
        this.pageNumber = pageNumber;
    }

    /**
     * @return : the pagination page size.
     */
    public Integer getPageSize() {
        return pageSize;
    }

    /**
     * @param pageSize : The pagination page size to set.
     */
    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    /**
     * @return : The task name to filter.
     */
    public String getTaskName() {
        return taskName;
    }

    /**
     * @param taskName : The task name to be filter by to set.
     */
    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    /**
     * @return : The task priority to filter.
     */
    public Integer getPriority() {
        return priority;
    }

    /**
     * @param priority : The task priority to filter by.
     */
    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    /**
     * @return : The query type.
     */
    public QueryType getSimpleQueryType() {
        return simpleQueryType;
    }

    /**
     * @param simpleQueryType : The query type to set.
     */
    public void setSimpleQueryType(QueryType simpleQueryType) {
        this.simpleQueryType = simpleQueryType;
    }

    /**
     * @return : The caller user name.
     */
    public String getCaller() {
        return caller;
    }

    /**
     * @param caller : The user name of the caller.
     */
    public void setCaller(String caller) {
        this.caller = caller;
    }

    /**
     * @return : The tenant id of the caller who's doing the task query
     */
    public int getCallerTenantId() {
        return callerTenantId;
    }

    /**
     * @param callerTenantId :The tenant id of the caller who's doing the task query to set.
     */
    public void setCallerTenantId(int callerTenantId) {
        this.callerTenantId = callerTenantId;
    }

    /**
     * @return : Query order parameter
     */
    public QueryOrder getQueryOrder() {
        return queryOrder;
    }

    /**
     * Parameter which used to order task list
     * @param queryOrder
     */
    public void setQueryOrder(QueryOrder queryOrder) {
        this.queryOrder = queryOrder;
    }

    /**
     * @return : returns Ascending or Descending
     */
    public QueryOrderBy getQueryOrderBy() {
        return queryOrderBy;
    }

    /**
     * Parameter is used for specify QueryOrderedBy
     * @param queryOrderBy
     */
    public void setQueryOrderBy(QueryOrderBy queryOrderBy) {
        this.queryOrderBy = queryOrderBy;
    }
}
