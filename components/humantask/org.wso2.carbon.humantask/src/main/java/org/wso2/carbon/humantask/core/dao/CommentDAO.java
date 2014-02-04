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
 * DAO representation of the task comment,
 */
public interface CommentDAO {

    /**
     * @return : The comment id.
     */
    Long getId();

    /**
     * @return : The comment time
     */
    Date getCommentedDate();

    /**
     * @param date : The comment date to set.
     */
    void setCommentedDate(Date date);

    /**
     * @return : The comment text
     */
    String getCommentText();

    /**
     * @param commentText : The comment text to set.
     */
    void setCommentText(String commentText);

    /**
     * @return : The commented user id.
     */
    String getCommentedBy();

    /**
     * @param commentedBy : The id of the user who's adding the comment to set.
     */
    void setCommentedBy(String commentedBy);

    /**
     * @return : The task which this comment belongs to.
     */
    TaskDAO getTask();

    /**
     * @param task : The task to set.
     */
    void setTask(TaskDAO task);

    /**
     * @return : The last modified date of the comment.
     */
    Date getModifiedDate();

    /**
     * @param modifiedDate : The last modified date to set.
     */
    void setModifiedDate(Date modifiedDate);

    /**
     * @return : The last modified user name.
     */
    String getModifiedBy();

    /**
     * @param modifiedBy : The last modified user name.
     */
    void setModifiedBy(String modifiedBy);

}