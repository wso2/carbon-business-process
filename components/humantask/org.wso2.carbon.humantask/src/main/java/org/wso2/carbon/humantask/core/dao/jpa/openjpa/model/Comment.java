/*
 * Copyright (c) 2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.apache.commons.lang.Validate;
import org.wso2.carbon.humantask.core.dao.CommentDAO;
import org.wso2.carbon.humantask.core.dao.TaskDAO;

import javax.persistence.*;
import java.util.Date;

/**
 * The task comment JPA implementation..
 */
@Entity
@Table(name = "HT_TASK_COMMENT")
@NamedQueries(
        @NamedQuery(name = Comment.DELETE_COMMENTS_BY_TASK, query = "delete from org.wso2.carbon.humantask.core.dao.jpa.openjpa.model.Comment as c where c.task = :task")
)
public class Comment implements CommentDAO {

    public static final String DELETE_COMMENTS_BY_TASK = "DELETE_COMMENTS_BY_TASK";

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "COMMENTED_ON")
    @Temporal(TemporalType.TIMESTAMP)
    private Date commentedDate;

    @Column(name = "MODIFIED_ON", nullable = true)
    @Temporal(TemporalType.TIMESTAMP)
    private Date modifiedDate;

    @Column(name = "COMMENT_TEXT", length = 4000)
    private String commentText;

    @Column(name = "COMMENTED_BY", length = 100)
    private String commentedBy;

    @Column(name = "MODIFIED_BY", length = 100, nullable = true)
    private String modifiedBy;

    @ManyToOne
    private Task task;

    public Comment() {
    }

    /**
     * The comment constructor.
     *
     * @param commentText : The comment text.
     * @param commentedBy : The commented by user id.
     */
    public Comment(String commentText, String commentedBy) {
        Validate.notNull(commentText);
        Validate.notNull(commentedBy);
        this.commentText = commentText;
        this.commentedBy = commentedBy;
        this.commentedDate = new Date();
    }

    
    public Long getId() {
        return id;
    }

    
    public Date getCommentedDate() {
        return commentedDate;
    }

    
    public void setCommentedDate(Date commentedDate) {
        this.commentedDate = commentedDate;
    }

    
    public String getCommentText() {
        return commentText;
    }

    
    public void setCommentText(String commentText) {
        this.commentText = commentText;
    }

    
    public String getCommentedBy() {
        return commentedBy;
    }

    
    public void setCommentedBy(String commentedBy) {
        this.commentedBy = commentedBy;
    }

    
    public TaskDAO getTask() {
        return task;
    }

    
    public void setTask(TaskDAO task) {
        this.task = (Task) task;
    }

    
    public Date getModifiedDate() {
        return modifiedDate;
    }

    
    public void setModifiedDate(Date modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    
    public String getModifiedBy() {
        return modifiedBy;
    }

    
    public void setModifiedBy(String modifiedBy) {
        this.modifiedBy = modifiedBy;
    }
}
