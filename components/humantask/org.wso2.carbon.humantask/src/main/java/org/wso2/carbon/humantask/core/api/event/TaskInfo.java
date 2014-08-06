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

package org.wso2.carbon.humantask.core.api.event;

import org.wso2.carbon.humantask.core.dao.TaskStatus;
import org.wso2.carbon.humantask.core.dao.TaskType;

import java.util.Date;
import java.util.List;

/**
 *  A basic representation of the TaskDAO to be accessible by 3rd parties.
 */
public class TaskInfo {

    /** The task id */
    private Long id;

    /** The task name */
    private String name;

    /** The default task description.  */
    private String description;

    /** The task subject */
    private String subject;

    /** The task type */
    private TaskType type;

    /** The task status */
    private TaskStatus status;

    /** The status of the task before a suspension event */
    private TaskStatus statusBeforeSuspension;

    /** The user name of the task owner */
    private String owner;

    /** The task created date. */
    private Date createdDate;

    /** The task last modified date.  */
    private Date modifiedDate;

    /**
     * @return : The task name.
     */
    public String getName() {
        return name;
    }

    /**
     * @param name : The task name to set.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return : The task subject.
     */
    public String getSubject() {
        return subject;
    }

    /**
     * @param subject : The task subject to set.
     */
    public void setSubject(String subject) {
        this.subject = subject;
    }

    /**
     * @return : The task type.
     */
    public TaskType getType() {
        return type;
    }

    /**
     * @param type : The task type to set.
     */
    public void setType(TaskType type) {
        this.type = type;
    }

    /**
     * @return : The task status.
     */
    public TaskStatus getStatus() {
        return status;
    }

    /**
     * @param status : The task status to set.
     */
    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    /**
     * @return : The status of the task before suspension.
     */
    public TaskStatus getStatusBeforeSuspension() {
        return statusBeforeSuspension;
    }

    /**
     * @param statusBeforeSuspension : The status of the task before suspension to set.
     */
    public void setStatusBeforeSuspension(TaskStatus statusBeforeSuspension) {
        this.statusBeforeSuspension = statusBeforeSuspension;
    }

    /**
     * @return : The task description.
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description : The task description to set.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return : The username of the task owner to set.
     */
    public String getOwner() {
        return owner;
    }

    /**
     * @param owner : The username of the task owner.
     */
    public void setOwner(String owner) {
        this.owner = owner;
    }

    /**
     * @return : The task created date.
     */
    public Date getCreatedDate() {
        return createdDate;
    }

    /**
     * @param createdDate :
     */
    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    /**
     * @return : The task last modified date.
     */
    public Date getModifiedDate() {
        return modifiedDate;
    }

    /**
     * @param modifiedDate : The task last modified date to set.
     */
    public void setModifiedDate(Date modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    /**
     * @return : The task id.
     */
    public Long getId() {
        return id;
    }

    /**
     * @param id : the task id to set.
     */
    public void setId(Long id) {
        this.id = id;
    }
}
