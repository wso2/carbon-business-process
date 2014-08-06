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

/**
 * Representation of the Task Status.
 */
public enum TaskStatus {

    /**
     * Status before getting persisted
     */
    UNDEFINED,

    /*
     * Upon creation, a task goes into its initial state Created.
     * Remains in created state until it is activated and has potential owners.
     */
    CREATED,

    /*
     * Created task with multiple potential owners or is assigned to a work queue
     */
    READY,

    /*
     * task has a single potential owner
     */
    RESERVED,

    /*
     * work is started on a task that is in state Ready or Reserved
     */
    IN_PROGRESS,

    /*
     * In any of its active states (Ready, Reserved, InProgress), a task can be suspended,
     * transitioning it into the Suspended state. The Suspended state has sub-states to
     * indicate the original state of the task.
     */
    SUSPENDED,

    /*
     * On successful completion of the work. One of the final states.
     */
    COMPLETED,

    /*
     * On unsuccessful completion of the work. One of the final states.
     */
    FAILED,

    /*
     * human task encounters a non-recoverable error in any of its state
     */
    ERROR,

    /*
     * parent application needs to end prematurely before the invoked
     * human task has been completed, it sends an exit coordination protocol message to
     * the human task, causing the human task to end its processing. No response message
     * is passed back.
     */
    EXITED,

    /*
     * task is no longer needed and terminate it, either because a timeout has reached
     * in that enclosing context (i.e., the task has expired),
     * or because the enclosing environment itself is terminated
     */
    OBSOLETE,

    /*
    * Applies to notifications only. If the notification recipient wishes to remove the notification the status would be set to this.
    */
    REMOVED
}
