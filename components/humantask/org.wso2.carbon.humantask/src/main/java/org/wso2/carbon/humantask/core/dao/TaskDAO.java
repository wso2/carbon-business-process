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

import org.w3c.dom.Element;

import javax.xml.namespace.QName;
import java.util.Date;
import java.util.List;

/**
 * The data access interface to interact with the persistable task object.
 * <p/>
 * NOTE: It is the responsibility of the caller to handle all the authorization and pre/post condition checks.
 */
public interface TaskDAO {

    void setStatus(TaskStatus status);

    /**
     * @return : Return the task status.
     */
    TaskStatus getStatus();

    /**
     * @return The task type.
     */
    TaskType getType();

    /**
     * @return : the task versioned name.
     */
    String getName();

    /**
     * @return : the task defined name
     */
    String getDefinitionName();

    void setDefinitionName(QName name);

    /**
     * @param tenantId : The tenant Id to set.
     */
    void setTenantId(Integer tenantId);

    /**
     * @return : The tenant id of the task.
     */
    Integer getTenantId();

    /**
     * Starts this task. i.e Updates the task's status to IN_PROGRESS.
     */
    void start();

    /**
     * Stops this task. i.e. Updates the task's status to STOPPED.
     */
    void stop();

    /**
     * Suspends this task. i.e. Updates the task's status to SUSPENDED.
     * Also the status before suspension field is updated.
     */
    void suspend();

    /**
     * Completes the task with the provided response message.
     *
     * @param response : The task completion message.
     */
    void complete(MessageDAO response);

    /**
     * Claim this task.
     *
     * @param caller : The person claiming the task.
     */
    void claim(OrganizationalEntityDAO caller);

    /**
     * Delegates this task to the provided org entity.
     *
     * @param delegatee : The delegatee.
     */
    void delegate(OrganizationalEntityDAO delegatee);

    /**
     * Persist the provided comment to this task.
     *
     * @param comment : The comment to persist.
     * @return : The persisted comment.
     */
    CommentDAO persistComment(CommentDAO comment);

    /**
     * Deletes the given comment  from the task.
     *
     * @param commentId : The id of the comment to be deleted.
     */
    void deleteComment(Long commentId);

    /**
     * Forward this task to the provided Org Entity.
     *
     * @param orgEntity : The forwadee.
     */
    void forward(OrganizationalEntityDAO orgEntity);

    /**
     * Returns the comments of this task.
     *
     * @return : This task's comment list.
     */
    List<CommentDAO> getComments();

    /**
     * @return : the input message for the task.
     */
    MessageDAO getInputMessage();

    /**
     * Get the task description for the provided content type.
     *
     * @param contentType : The content type
     *
     * @return : The matching task description.
     */
    String getTaskDescription(String contentType);

    /**
     * Release the task. i.e. remove the actual owner from the task.
     */
    void release();

    /**
     * Removes the task object. Note: Applicable for notification only.
     */
    void remove();

    /**
     * Resume the task from the suspended state.
     */
    void resume();

    /**
     * Activates the task.
     */
    void activate();

    /**
     * Skips the task. i.e : Makes the task OBSOLETE.
     */
    void skip();

    /**
     * Update the provided comment id with the new comment content.
     *
     * @param commentId : The id of the comment to be updated.
     * @param newComment : The new comment content.
     * @param modifiedBy : The modified by user name.
     */
    void updateAndPersistComment(Long commentId, String newComment, String modifiedBy);

    /**
     * @param skipable : the skipable flag to set.
     */
    void setSkipable(Boolean skipable);

    /**
     * @return : If the task is skippable. False otherwise.
     */
    Boolean isSkipable();

    /**
     * @return : The list of sub tasks for this task.
     */
    List<TaskDAO> getSubTasks();

    /**
     * @param subTasks : The list of sub tasks of this task to set.
     */
    void setSubTasks(List<TaskDAO> subTasks);

    /**
     * @return : The parent task of this task if one exists.
     */
    TaskDAO getParentTask();

    /**
     * @param parentTask : The parent of this task to set.
     */
    void setParentTask(TaskDAO parentTask);

    /**
     * @return : The list of attachment objects for this task.
     */
    List<AttachmentDAO> getAttachments();

    /**
     * @param attachments : The set of attachments for this task to set.
     */
    void setAttachments(List<AttachmentDAO> attachments);

    /**
     * Add an attachment to this task.
     *
     * @param attachment : The attachment to be added.
     * @return <tt>true</tt> (as specified by {@link java.util.Collection#add})
     */
    boolean addAttachment(AttachmentDAO attachment);

    /**
     * @return : The task Id.
     */
    Long getId();

    /**
     * @return : The list of GHRs of this task.
     */
    List<GenericHumanRoleDAO> getHumanRoles();

    /**
     * @param humanRole : The new generic human role to be added.
     */
    void addHumanRole(GenericHumanRoleDAO humanRole);

    /**
     * @param param : the new presentation parameter to be added.
     */
    void addPresentationParameter(PresentationParameterDAO param);

    /**
     * @return : The list of presentation parameters of this task.
     */
    List<PresentationParameterDAO> getPresentationParameters();

    /**
     * @param preName : The presentation name to be added.
     */
    void addPresentationName(PresentationNameDAO preName);

    /**
     * @param preSubject : the presentation subject to be added.
     */
    void addPresentationSubject(PresentationSubjectDAO preSubject);

    /**
     * @param preDesc : The presentation description to be added.
     */
    void addPresentationDescription(PresentationDescriptionDAO preDesc);

    /**
     * @return : The status of the task before the task suspension. (if a suspension has happened.)
     */
    TaskStatus getStatusBeforeSuspension();

    /**
     * @param statusBeforeSuspension : The status before suspension to set.
     */
    void setStatusBeforeSuspension(TaskStatus statusBeforeSuspension);

    /**
     * Exit the task. i.e Set the task status to TaskStatus.Exit.
     */
    void exit();

    /**
     * @param input : The input message of the task to set.
     */
    void setInputMessage(MessageDAO input);

    /**
     * @return : The failure message of the task if one exists. Null otherwise
     */
    MessageDAO getFailureMessage();

    /**
     * @param failureMessage : The failure message of the task to set.
     */
    void setFailureMessage(MessageDAO failureMessage);

    /**
     * @return : The priority of the task
     */
    Integer getPriority();

    /**
     * @param priority : The task priority to set.
     */
    void setPriority(Integer priority);

    /**
     * @return : The created on date of the task.
     */
    Date getCreatedOn();

    /**
     * @param createdOn : The created on date to set.
     */
    void setCreatedOn(Date createdOn);

    /**
     * @return : The last updated date of the task.
     */
    Date getUpdatedOn();

    /**
     * @param updatedOn : The last updated date of the task to set.
     */
    void setUpdatedOn(Date updatedOn);

    /**
     * @return : The activation time of the task.
     */
    Date getActivationTime();

    /**
     * @return : True if the task is escalated.
     */
    Boolean isEscalated();

    /**
     * @param escalated The task escalation flag to set.
     */
    void setEscalated(Boolean escalated);

    /**
     * @param activationTime : The activation time to set.
     */
    void setActivationTime(Date activationTime);

    /**
     * @return : The expiration time of the task.
     */
    Date getExpirationTime();

    /**
     * @param expirationTime : The expiration time of the task to set.
     */
    void setExpirationTime(Date expirationTime);

    /**
     * @return : The start by time of the task.
     */
    Date getStartByTime();

    /**
     * @param startByTime : The start by time to set.
     */
    void setStartByTime(Date startByTime);

    /**
     * @return : The complete by time of the task.
     */
    Date getCompleteByTime();

    /**
     * @param completeByTime : The complete by time of the task to set.
     */
    void setCompleteByTime(Date completeByTime);

    /**
     * @return : the output message of the task.
     */
    MessageDAO getOutputMessage();

    /**
     * @param outputMessage : The output message of the task to set.
     */
    void setOutputMessage(MessageDAO outputMessage);

    /**
     * @return : The presentation subjects of the task.
     */
    List<PresentationSubjectDAO> getPresentationSubjects();

    /**
     * @return : The presentation names of the task.
     */
    List<PresentationNameDAO> getPresentationNames();

    /**
     * @return : The presentation descriptions of the task.
     */
    List<PresentationDescriptionDAO> getPresentationDescriptions();

    /**
     * Persist a new task priority.
     *
     * @param newPriority : The new priority to set.
     */
    void persistPriority(Integer newPriority);


    /**
     * Nominate the task
     * @param nominees : The list of nominees.
     */
    void nominate(List<OrganizationalEntityDAO> nominees);

    /**
     * Fails this task . Changes the task's status to FAIL. Also a failure message and
     * the failure name is persisted.
     *
     * @param faultName : The failure name.
     * @param faultData : The fault data to persist.
     */
    void fail(String faultName, Element faultData);

    /**
     * Delete the fault message of this task.
     */
    void deleteFault();

    /**
     * Delete the output message of this task.
     */
    void deleteOutput();

    /**
     * Persist the fault message.
     *
     * @param faultName    : The fault name to persist. .
     * @param faultElement : The fault data.
     */
    void persistFault(String faultName, Element faultElement);

    /**
     * Persist the output message of the task.
     *
     * @param outputName : The name of the output
     * @param outputData : The output mesage data.
     */
    void persistOutput(String outputName, Element outputData);

    /**
     * Persist a new org entity to the potential owners.
     *
     * @param delegatee : The org entity to be persisted to potential owners.
     */
    void persistToPotentialOwners(OrganizationalEntityDAO delegatee);

    /**
     * Gets the GenericHumanRoleDAO of the provided GenericHumanRoleType.
     *
     * @param type : The type of the required human role.
     * @return : The matching GenericHumanRoleDAO.
     */
    GenericHumanRoleDAO getGenericHumanRole(GenericHumanRoleDAO.GenericHumanRoleType type);

    /**
     * Adds a new deadline to this task
     *
     * @param deadlineDAO : The new deadline object.
     */
    void addDeadline(DeadlineDAO deadlineDAO);

    /**
     * @return : The list of deadlines of this task.
     */
    List<DeadlineDAO> getDeadlines();

    /**
     * Replaces the organizational entities for the provided human role type with the
     * set of provided entities.
     * @param type : The human role type of which the org entities should be replaced.
     * @param orgEntities : The new list of organizational entities to be added to the human role
     */
    void replaceOrgEntitiesForLogicalPeopleGroup(GenericHumanRoleDAO.GenericHumanRoleType type,
                                                 List<OrganizationalEntityDAO> orgEntities);

    /**
     * Gets the task events for the particular task.
     * @return : The list of task events for a particular task.
     */
    List<EventDAO> getEvents();

    /**
     * @param events : The list of persisted events of this task.
     */
    void setEvents(List<EventDAO> events);

    /**
     * Adds a new event to the task.
     *
     * @param eventDAO : The new event to be added to the task.
     */
    void addEvent(EventDAO eventDAO);

    /**
     * @param eventDAO : the event to be persisted.
     */
    void persistEvent(EventDAO eventDAO);

    /**
     *
     */
    void setTaskVersion(long version);

    /**
     *
     * @return
     */
    long getTaskVersion();

    /**
     *
     * @param packageName
     */
    void setTaskPackageName(String packageName);

    /**
     *
     * @return
     */
    String getTaskPackageName();


    /**
     *
     */
    void delete();

    /**
     *
     */
    void deleteInstance();
}

