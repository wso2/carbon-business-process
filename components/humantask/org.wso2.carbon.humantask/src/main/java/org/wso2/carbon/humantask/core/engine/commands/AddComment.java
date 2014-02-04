/*
 * Copyright (c) 2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.humantask.core.engine.commands;

import org.wso2.carbon.humantask.core.dao.CommentDAO;
import org.wso2.carbon.humantask.core.dao.EventDAO;
import org.wso2.carbon.humantask.core.dao.GenericHumanRoleDAO;

import java.util.ArrayList;
import java.util.List;

/**
 * Add commentString operation.
 */
public class AddComment extends AbstractHumanTaskCommand {

    /** The comment string to be added to the task */
    private String commentString;

    /** The persisted commend object after operation completes. */
    private CommentDAO persistedComment;

    public AddComment(String callerId, Long taskId, String commentString) {
        super(callerId, taskId);
        this.commentString = commentString;
    }

    /**
     * Checks the Pre-conditions before executing the task operation.
     */
    @Override
    protected void checkPreConditions() {

    }

    /**
     * Perform the authorization checks before executing the task operation.
     */
    @Override
    protected void authorise() {
        List<GenericHumanRoleDAO.GenericHumanRoleType> allowedRoles = new ArrayList<GenericHumanRoleDAO.GenericHumanRoleType>();
        allowedRoles.add(GenericHumanRoleDAO.GenericHumanRoleType.ACTUAL_OWNER);
        allowedRoles.add(GenericHumanRoleDAO.GenericHumanRoleType.POTENTIAL_OWNERS);
        allowedRoles.add(GenericHumanRoleDAO.GenericHumanRoleType.BUSINESS_ADMINISTRATORS);
        allowedRoles.add(GenericHumanRoleDAO.GenericHumanRoleType.STAKEHOLDERS);

        authoriseRoles(allowedRoles);
    }

    /**
     * Perform the state checks before executing the task operation.
     */
    @Override
    protected void checkState() {

    }

    /**
     * Checks the post-conditions after executing the task operation.
     */
    @Override
    protected void checkPostConditions() {

    }

    @Override
    protected EventDAO createTaskEvent() {
        EventDAO taskEvent = super.createTaskEvent();
        taskEvent.setDetails("");
        return taskEvent;
    }

    @Override
    public void execute() {
        authorise();
        checkPreConditions();
        checkState();
        persistedComment = getTask().persistComment(getEngine().getDaoConnectionFactory().
                getConnection().getCommentDAO(commentString, getOperationInvoker().getName()));
        processTaskEvent();
        checkPostConditions();
    }

    public CommentDAO getPersistedComment() {
        return persistedComment;
    }
}
