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

import java.util.Date;

/**
 * The task event interface.
 */
public interface EventDAO {
    /**
     * @return : the event id.
     */
    Long getId();

    /**
     * @return : The timestamp on which the event occurred
     */
    Date getTimeStamp();

    /**
     * @param timeStamp : The timestamp on which the event occurred to set.
     */
    void setTimeStamp(Date timeStamp);

    /**
     * @return : The type of this event.
     */
    TaskEventType getType();

    /**
     * @param type : The type of this event to set.
     */
    void setType(TaskEventType type);

    /**
     * @return : The details of this event.
     */
    String getDetails();

    /**
     * @param details : The details of this event to set.
     */
    void setDetails(String details);

    /**
     * @return :    the name of the user who performed this operation
     */
    String getUser();

    /**
     * @param user : the name of the user who performed this operation to set.
     */
    void setUser(String user);

    /**
     * @return : The related task of this event.
     */
    TaskDAO getTask();


    /**
     * @param task : The task to set.
     */
    void setTask(TaskDAO task);

    /**
     * @return : The state of the task before this event occurred.
     */
    TaskStatus getOldState();

    /**
     * @param oldState : The old state to set.
     */
    void setOldState(TaskStatus oldState);

    /**
     * @return : The state of the task after this event occurred.
     */
    TaskStatus getNewState();

    /**
     * @param newState : The new state of the task to set.
     */
    void setNewState(TaskStatus newState);
}
