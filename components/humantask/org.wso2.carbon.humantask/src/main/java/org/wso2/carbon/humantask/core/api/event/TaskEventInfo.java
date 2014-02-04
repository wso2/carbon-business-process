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

import org.wso2.carbon.humantask.core.dao.TaskEventType;
import org.wso2.carbon.humantask.core.dao.TaskStatus;

import java.util.Date;

/**
 * This class holds information about particular task events and the related task information.
 */
public class TaskEventInfo {

    /** Information about the task related to this event */
    private TaskInfo taskInfo;

    /** The user who initiated this event */
    private String eventInitiator;

    /** The event timestamp */
    private Date timestamp;

    /** The event type */
    private TaskEventType eventType;

    /** The event details */
    private String details;

    /** The status of the task before this event occurred */
    private TaskStatus oldState;

    /** The status of the task after this event occurred. */
    private TaskStatus newState;


    /**
     * @return : The task information.
     */
    public TaskInfo getTaskInfo() {
        return taskInfo;
    }

    /**
     * @param taskInfo : The task information to set.
     */
    public void setTaskInfo(TaskInfo taskInfo) {
        this.taskInfo = taskInfo;
    }

    /**
     * @return : The event initiator of this event.
     */
    public String getEventInitiator() {
        return eventInitiator;
    }

    /**
     * @param eventInitiator : The event initiator to set.
     */
    public void setEventInitiator(String eventInitiator) {
        this.eventInitiator = eventInitiator;
    }

    /**
     * @return  : the timestamp
     */
    public Date getTimestamp() {
        return timestamp;
    }

    /**
     * @param timestamp : The timestamp to set.
     */
    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * @return : The event type.
     */
    public TaskEventType getEventType() {
        return eventType;
    }

    /**
     * @param eventType : The event type to set.
     */
    public void setEventType(TaskEventType eventType) {
        this.eventType = eventType;
    }

    /**
     * @return : The event details
     */
    public String getDetails() {
        return details;
    }

    /**
     * @param details : The event details to set.
     */
    public void setDetails(String details) {
        this.details = details;
    }

    /**
     * @return : The task status before the event happened to set.
     */
    public TaskStatus getOldState() {
        return oldState;
    }

    /**
     * @param oldState :The task status before the event happened
     */
    public void setOldState(TaskStatus oldState) {
        this.oldState = oldState;
    }

    /**
     * @return : The new state of the task after the event occurred.
     */
    public TaskStatus getNewState() {
        return newState;
    }

    /**
     * @param newState : The new state of the task after the event occurred to set.
     */
    public void setNewState(TaskStatus newState) {
        this.newState = newState;
    }
}
