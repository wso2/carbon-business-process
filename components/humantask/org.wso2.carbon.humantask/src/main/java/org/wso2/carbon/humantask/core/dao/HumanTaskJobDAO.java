/*
 * Copyright (c) 2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

public interface HumanTaskJobDAO {
    /**
     * Get primary key
     * @return primary key
     */
    Long getId();

    /**
     * Set primary key
     * @param jobId Job ID
     */
    void setId(Long jobId);

    /**
     * Get the assigned node id of the job
     * @return node id
     */
    String getNodeId();

    /**
     * Set the assigned node id of the job
     * @param id node id
     */
    void setNodeId(String id);

    /**
     * Get the name of the job
     * @return name
     */
    String getName();

    /**
     * Set the name of the job
     * @param name Name of the job
     */
    void setName(String name);

    /**
     * Get scheduled time of the job execution
     * @return time
     */
    Long getTime();

    /**
     * Set scheduled time of the job execution
     * @param time Time
     */
    void setTime(Long time);

    /**
     * Set whether the job is scheduled
     * @param scheduled true or false
     */
    void setScheduled(boolean scheduled);

    /**
     * Check whether the job should be executed in a transaction
     * @return whether the transaction enabled
     */
    boolean isTransacted();

    /**
     * Set whether the job should be executed in a transaction
     * @param transacted true or false
     */
    void setTransacted(boolean transacted);

    /**
     * Get details of the job
     * @return details
     */
    String getDetails();

    /**
     * Set details of the job
     * @param details details
     */
    void setDetails(String details);

    /**
     * Get the task id of the job
     * @return task id
     */
    Long getTaskId();

    /**
     * Set the task id of the job
     * @param taskId Task id
     */
    void setTaskId(Long taskId);

    /**
     * Get the type of the job
     * @return job type
     */
    String getType();

    /**
     * Get the type of the job
     * @param type type of the job
     */
    void setType(String type);

    /**
     * Delete the object
     */
    void delete();
}
