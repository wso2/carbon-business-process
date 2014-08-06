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

package org.wso2.carbon.humantask.core.dao.jpa.openjpa.model;

import org.wso2.carbon.humantask.core.dao.EventDAO;
import org.wso2.carbon.humantask.core.dao.TaskDAO;
import org.wso2.carbon.humantask.core.dao.TaskEventType;
import org.wso2.carbon.humantask.core.dao.TaskStatus;

import javax.persistence.*;
import java.util.Date;

/**
 * Row presentation of a human task event.
 */
@Entity
@Table(name = "HT_EVENT")
@NamedQueries(
        @NamedQuery(name = Event.DELETE_EVENTS_BY_TASK, query = "delete from org.wso2.carbon.humantask.core.dao.jpa.openjpa.model.Event as e where e.task = :task")
)
public class Event implements EventDAO {

    public static final String DELETE_EVENTS_BY_TASK = "DELETE_EVENTS_BY_TASK";
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "EVENT_TIMESTAMP", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date timeStamp;

    @Enumerated(EnumType.STRING)
    @Column(name = "EVENT_TYPE", nullable = false)
    private TaskEventType type;

    @Column(name = "EVENT_DETAILS", nullable = true)
    private String details;

    @Column(name = "EVENT_USER", nullable = false)
    private String user;

    @Enumerated(EnumType.STRING)
    @Column(name = "OLD_STATE", nullable = true)
    private TaskStatus oldState;

    @Enumerated(EnumType.STRING)
    @Column(name = "NEW_STATE", nullable = true)
    private TaskStatus newState;

    @ManyToOne
    private Task task;

    public Event() {
    }

    public Long getId() {
        return id;
    }

    
    public Date getTimeStamp() {
        return timeStamp;
    }

    
    public void setTimeStamp(Date timeStamp) {
        this.timeStamp = timeStamp;
    }

    
    public TaskEventType getType() {
        return type;
    }

    
    public void setType(TaskEventType type) {
        this.type = type;
    }

    
    public String getDetails() {
        return details;
    }

    
    public void setDetails(String details) {
        this.details = details;
    }

    
    public String getUser() {
        return user;
    }

    
    public void setUser(String user) {
        this.user = user;
    }

    
    public Task getTask() {
        return task;
    }

    
    public void setTask(TaskDAO task) {
        this.task = (Task)task;
    }

    
    public TaskStatus getOldState() {
        return oldState;
    }

    
    public void setOldState(TaskStatus oldState) {
        this.oldState = oldState;
    }

    
    public TaskStatus getNewState() {
        return newState;
    }

    
    public void setNewState(TaskStatus newState) {
        this.newState = newState;
    }
}
