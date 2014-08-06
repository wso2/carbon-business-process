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

/**
 * Representation of task events.
 * Note: This should match the all upper case command class name as we do perform TaskEventType.valueOf
 */
public enum TaskEventType {
    /**
     * Task creation event
     */
    CREATE,
    /**
     * Task activate event
     */
    ACTIVATE,

    ADDCOMMENT,

    CLAIM,

    COMPLETE,

    DELEGATE,

    DELETECOMMENT,

    DELETEFAULT,

    DELETEOUTPUT,

    EXIT,

    FAIL,

    FORWARD,

    GETCOMMENTS,

    GETFAULT,

    GETINPUT,

    GETOUTPUT,

    GETTASKDESCRIPTION,

    NOMINATE,

    RELEASE,

    REMOVE,

    RESUME,

    SETFAULT,

    SETOUTPUT,

    SETPRIORITY,

    SKIP,

    START,

    STOP,

    SUSPEND,

    UPDATECOMMENT

}
