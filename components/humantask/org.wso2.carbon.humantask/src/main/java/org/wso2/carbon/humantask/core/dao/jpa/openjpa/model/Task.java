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

import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.wso2.carbon.humantask.core.dao.*;
import org.wso2.carbon.humantask.core.utils.DOMUtils;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * The task model object. Represents the task data.
 */
@Entity
@Table(name = "HT_TASK")
public class Task extends OpenJPAEntity implements TaskDAO {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /**
     * The tenant id of the tenant which this task belongs to.
     */
    @Column(name = "TENANT_ID", nullable = false)
    private Integer tenantId;

    /**
     * Task name must be a QName. To make it easy to store in DB we are using string type.
     * When setting the task name, we convert QName to string. When getting name, caller must
     * convert name back to a QName.
     */
    @Column(name = "TASK_NAME", nullable = false)
    private String name;

    /**
     * Task name must be a QName. To make it easy to store in DB we are using string type.
     * When setting the task name, we convert QName to string. When getting name, caller must
     * convert name back to a QName. This is same as name except name has version appended to it.
     */
    @Column(name ="TASK_DEF_NAME", nullable = false)
    private String taskDefinitionName;

    @Enumerated(EnumType.STRING)
    @Column(name = "TASK_TYPE", nullable = false)
    private TaskType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", nullable = false)
    private TaskStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS_BEFORE_SUSPENSION", nullable = true)
    private TaskStatus statusBeforeSuspension;

    /**
     * Tasks priority. 0 is the highest priority.
     * If this field is null it means task's priority is unspecified.
     */
    @Column(name = "PRIORITY", nullable = false)
    private Integer priority = 5;


    /**
     * The list of sub tasks for a given task
     */
    @OneToMany(targetEntity = Task.class, mappedBy = "parentTask", fetch = FetchType.LAZY, cascade = {CascadeType.ALL})
    private List<TaskDAO> subTasks = new ArrayList<TaskDAO>();

    /**
     * The parent task.
     */
    @ManyToOne
    private Task parentTask;

    /**
     * GenericHumanRole has a type and GenericHumanRole has many-to-many relationship with OrganizationalEntity.
     * Through the GenericHumanRole type we get the assigned OrganizationalEntities for each role.
     */
    @OneToMany(targetEntity = GenericHumanRole.class, mappedBy = "task", fetch = FetchType.LAZY, cascade = {CascadeType.ALL})
    private List<GenericHumanRoleDAO> humanRoles = new ArrayList<GenericHumanRoleDAO>();

    /**
     * Time related fields.
     * Created On - Task creation time. Set at the task creation
     * Activation time - The time in UTC when the task has been activated.
     * Expiration time - The time in UTC when the task will expire.
     */
    @Column(name = "CREATED_ON")
    @Temporal(value = TemporalType.TIMESTAMP)
    private Date createdOn;

    @Column(name = "UPDATED_ON")
    @Temporal(value = TemporalType.TIMESTAMP)
    private Date updatedOn;

    @Column(name = "ACTIVATION_TIME")
    @Temporal(value = TemporalType.TIMESTAMP)
    private Date activationTime;


    @Column(name = "EXPIRATION_TIME")
    @Temporal(value = TemporalType.TIMESTAMP)
    private Date expirationTime;


    @Column(name = "START_BY_TIME")
    @Temporal(value = TemporalType.TIMESTAMP)
    private Date startByTime;


    @Column(name = "COMPLETE_BY_TIME")
    @Temporal(value = TemporalType.TIMESTAMP)
    private Date completeByTime;

    /**
     * input
     */
    @Column(name = "INPUT_MESSAGE")
    @OneToOne(targetEntity = Message.class, fetch = FetchType.LAZY, cascade = {CascadeType.ALL})
    private MessageDAO inputMessage;

    /**
     * output
     */
    @Column(name = "OUTPUT_MESSAGE")
    @OneToOne(targetEntity = Message.class, fetch = FetchType.LAZY, cascade = {CascadeType.ALL})
    private MessageDAO outputMessage;

    /**
     * failure
     */
    @Column(name = "FAILURE_MESSAGE")
    @OneToOne(targetEntity = Message.class, fetch = FetchType.LAZY, cascade = {CascadeType.ALL})
    private MessageDAO failureMessage;

    /**
     * Comments by people who can influence on task's progress
     */
    @OneToMany(targetEntity = Comment.class, mappedBy = "task", fetch = FetchType.LAZY, cascade = {CascadeType.ALL})
    private List<CommentDAO> comments = new ArrayList<CommentDAO>();

    /**
     * Attachments attached during the life cycle of the task
     */
    @OneToMany(targetEntity = Attachment.class, mappedBy = "task", fetch = FetchType.LAZY, cascade = {CascadeType.ALL})
    private List<AttachmentDAO> attachments = new ArrayList<AttachmentDAO>();

    /**
     * Skippable flag.
     */
    @Column(name = "SKIPABLE", length = 1)
    private String skipableStr = "N";

    @Transient
    private Boolean skipable;

    /**
     * Escalated flag.
     */
    @Column(name = "ESCALATED", length = 1)
    private String escalatedStr = "N";

    @Transient
    private Boolean escalated;

    /**
     * Task presentation parameters.
     */
    @OneToMany(targetEntity = PresentationParameter.class, mappedBy = "task",
            fetch = FetchType.LAZY, cascade = {CascadeType.ALL})
    private List<PresentationParameterDAO> presentationParameters =
            new ArrayList<PresentationParameterDAO>();

    /**
     * Task presentation subjects.
     */
    @OneToMany(targetEntity = PresentationSubject.class, mappedBy = "task",
            fetch = FetchType.EAGER, cascade = {CascadeType.ALL})
    private List<PresentationSubjectDAO> presentationSubjects = new ArrayList<PresentationSubjectDAO>();

    /**
     * Task presentation names.
     */
    @OneToMany(targetEntity = PresentationName.class, mappedBy = "task",
            fetch = FetchType.EAGER, cascade = {CascadeType.ALL})
    private List<PresentationNameDAO> presentationNames = new ArrayList<PresentationNameDAO>();

    /**
     *
     */
    @OneToMany(targetEntity = PresentationDescription.class, mappedBy = "task",
            fetch = FetchType.LAZY, cascade = {CascadeType.ALL})
    private List<PresentationDescriptionDAO> presentationDescriptions =
            new ArrayList<PresentationDescriptionDAO>();

    /**
     * Deadlines.
     * This version use a list to store dealines. Need to check whether we can use
     * startDeadline and completionDeadline attributes separately without using many-to-one
     * relationship.
     */
    @OneToMany(targetEntity = Deadline.class, mappedBy = "task", fetch = FetchType.EAGER, cascade = {CascadeType.ALL})
    private List<DeadlineDAO> deadlines = new ArrayList<DeadlineDAO>();

    /**
     * Events which records changes in task. For example change from CREATED state to RESERVED.
     */
    @OneToMany(targetEntity = Event.class, mappedBy = "task", fetch = FetchType.LAZY, cascade = {CascadeType.ALL})
    private List<EventDAO> events = new ArrayList<EventDAO>();

    @Column(name="TASK_VERSION", nullable = false)
    private long taskVersion;

    @Column(name="PACKAGE_NAME", nullable = false)
    private String packageName;

    public Task() {
    }

    public Task(QName name, TaskType type, Integer tenantId) {
        this.createdOn = new Date();
        this.status = TaskStatus.UNDEFINED;
        this.name = name.toString();
        this.type = type;
        this.tenantId = tenantId;
    }

    /**
     * Get the Task ID.
     *
     * @return The task id.
     */
    
    public Long getId() {
        return id;
    }

    /**
     * The task id.
     *
     * @param id primary key
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * This method assumes that external modules only set input message at the creation of task
     *
     * @param input Input MessageDAO
     */
    
    public void setInputMessage(MessageDAO input) {
        inputMessage = input;
    }

    
    public MessageDAO getFailureMessage() {
        return failureMessage;
    }

    
    public void setFailureMessage(MessageDAO failureMessage) {
        this.failureMessage = failureMessage;
    }

    
    public void setTenantId(Integer tenantId) {
        this.tenantId = tenantId;
    }

    
    public Integer getTenantId() {
        return this.tenantId;
    }

    
    public void addPresentationParameter(PresentationParameterDAO param) {
        presentationParameters.add(param);
    }

    
    public List<PresentationParameterDAO> getPresentationParameters() {
        return presentationParameters;
    }

    
    public void addPresentationName(PresentationNameDAO preName) {
        this.presentationNames.add(preName);
    }

    
    public void addPresentationSubject(PresentationSubjectDAO preSubject) {
        this.presentationSubjects.add(preSubject);
    }

    
    public void addPresentationDescription(PresentationDescriptionDAO preDesc) {
        this.presentationDescriptions.add(preDesc);
    }

    
    public void addHumanRole(GenericHumanRoleDAO humanRole) {
        this.humanRoles.add(humanRole);
    }

    
    public List<GenericHumanRoleDAO> getHumanRoles() {
        List<GenericHumanRoleDAO> humanRoleDAOs = new ArrayList<GenericHumanRoleDAO>();
        if (this.humanRoles != null) {
            humanRoleDAOs.addAll(this.humanRoles);
        }
        return humanRoleDAOs;
    }

    
    public String getName() {
        return name;
    }

    public String getDefinitionName() {
        return this.taskDefinitionName;
    }

    public void setDefinitionName(QName name) {
        this.taskDefinitionName = name.toString();
    }


    public void setName(String name) {
        this.name = name;
    }

    
    public TaskType getType() {
        return type;
    }

    public void setType(TaskType type) {
        this.type = type;
    }

    
    public TaskStatus getStatusBeforeSuspension() {
        return statusBeforeSuspension;
    }

    
    public void setStatusBeforeSuspension(TaskStatus statusBeforeSuspension) {
        this.statusBeforeSuspension = statusBeforeSuspension;
    }

    
    public Integer getPriority() {
        return priority;
    }

    
    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    
    public Date getCreatedOn() {
        return new Date(createdOn.getTime());
    }

    
    public void setCreatedOn(Date createdOn) {
        this.createdOn = new Date(createdOn.getTime());
    }

    
    public Date getUpdatedOn() {
        return updatedOn;
    }

    
    public void setUpdatedOn(Date updatedOn) {
        this.updatedOn = updatedOn;
    }

    
    public Date getActivationTime() {
        if (activationTime != null) {
            return new Date(activationTime.getTime());
        } else {
            return null;
        }
    }

    
    public void setActivationTime(Date activationTime) {
        if (activationTime != null) {
            this.activationTime = new Date(activationTime.getTime());
        }
    }

    
    public Date getExpirationTime() {
        if (expirationTime != null) {
            return new Date(expirationTime.getTime());
        } else {
            return null;
        }
    }

    
    public void setExpirationTime(Date expirationTime) {
        if (expirationTime != null) {
            this.expirationTime = new Date(expirationTime.getTime());
        }
    }

    
    public Date getStartByTime() {
        return startByTime;
    }

    
    public void setStartByTime(Date startByTime) {
        this.startByTime = startByTime;
    }

    
    public Date getCompleteByTime() {
        return completeByTime;
    }

    
    public void setCompleteByTime(Date completeByTime) {
        this.completeByTime = completeByTime;
    }

    
    public MessageDAO getOutputMessage() {
        return outputMessage;
    }

    
    public void setOutputMessage(MessageDAO outputMessage) {
        this.outputMessage = outputMessage;
    }

    
    public List<AttachmentDAO> getAttachments() {
        return attachments;
    }

    
    public void setAttachments(List<AttachmentDAO> attachments) {
        this.attachments = attachments;
    }

    
    public boolean addAttachment(AttachmentDAO attachment) {
        return this.attachments.add(attachment);
    }

    
    public Boolean isEscalated() {
        return "Y".equalsIgnoreCase(escalatedStr);
    }

    
    public void setEscalated(Boolean escalated) {
        this.escalated = escalated;
        if( escalated) {
            this.escalatedStr = "Y";
        } else {
            this.escalatedStr = "N";
        }
    }

    public String getSkipableStr() {
        return skipable ? "Y" : "N";
    }

    public void setSkipableStr(String skipableStr) {
        this.skipableStr = skipableStr;
        this.skipable = "Y".equalsIgnoreCase(skipableStr);
    }

    public String getEscalatedStr() {
        return escalated ? "Y" : "N";
    }

    public void setEscalatedStr(String escalatedStr) {
        this.escalatedStr = escalatedStr;
    }

    
    public List<PresentationSubjectDAO> getPresentationSubjects() {
        return presentationSubjects;
    }


    
    public List<PresentationNameDAO> getPresentationNames() {
        return presentationNames;
    }

    
    public List<PresentationDescriptionDAO> getPresentationDescriptions() {
        return presentationDescriptions;
    }

    
    public List<DeadlineDAO> getDeadlines() {
        return deadlines;
    }

    
    public void addDeadline(DeadlineDAO deadlineDAO) {
        deadlines.add(deadlineDAO);
    }

    
    public List<EventDAO> getEvents() {
        return events;
    }

    
    public void setEvents(List<EventDAO> events) {
        this.events = events;
    }

    
    public void addEvent(EventDAO event) {
        this.events.add(event);
    }

    
    public void persistEvent(EventDAO event) {
        event.setTask(this);
        this.getEvents().add(event);
    }



    
    public List<TaskDAO> getSubTasks() {
        return subTasks;
    }

    
    public void setSubTasks(List<TaskDAO> subTasks) {
        this.subTasks = subTasks;
    }

    
    public Task getParentTask() {
        return parentTask;
    }

    
    public void setParentTask(TaskDAO parentTask) {
        this.parentTask = (Task) parentTask;
    }

    
    public TaskStatus getStatus() {
        return status;
    }

    
    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    
    public void start() {
        this.setStatus(TaskStatus.IN_PROGRESS);
        getEntityManager().merge(this);
    }

    
    public void stop() {
        this.setStatus(TaskStatus.RESERVED);
        getEntityManager().merge(this);
    }

    
    public void suspend() {
        this.setStatusBeforeSuspension(this.getStatus());
        this.setStatus(TaskStatus.SUSPENDED);
        getEntityManager().merge(this);
    }

    
    public void complete(MessageDAO response) {
        response.setTask(this);
        this.setOutputMessage(response);
        this.setStatus(TaskStatus.COMPLETED);
        getEntityManager().merge(this);
    }

    
    public void claim(OrganizationalEntityDAO caller) {
        List<OrganizationalEntityDAO> organizationalEntities = new ArrayList<OrganizationalEntityDAO>();
        organizationalEntities.add(caller);

        GenericHumanRoleDAO actualOwnerRole = new GenericHumanRole();
        actualOwnerRole.setType(GenericHumanRoleDAO.GenericHumanRoleType.ACTUAL_OWNER);
        actualOwnerRole.setOrgEntities(organizationalEntities);
        actualOwnerRole.setTask(this);

        caller.addGenericHumanRole(actualOwnerRole);

        this.addHumanRole(actualOwnerRole);
        this.setStatus(TaskStatus.RESERVED);
    }

    
    public void exit() {
        this.setStatus(TaskStatus.EXITED);
        getEntityManager().merge(this);
    }

    
    public void delegate(OrganizationalEntityDAO delegatee) {
        claim(delegatee);
    }

    
    public CommentDAO persistComment(CommentDAO comment) {
        List<CommentDAO> originalCommentList = new ArrayList<CommentDAO>(this.getComments());
        comment.setTask(this);
        this.getComments().add(comment);
        getEntityManager().merge(this);
        List<CommentDAO> newCommentList = this.getComments();
        if (newCommentList.size() - originalCommentList.size() == 1) {
            return (CommentDAO) ListUtils.subtract(newCommentList, originalCommentList).get(0);
        } else {
            return null;
        }
    }

    
    public void deleteComment(Long commentId) {

        for (Iterator<CommentDAO> i = this.getComments().iterator(); i.hasNext(); ) {
            CommentDAO comment = i.next();
            if (comment.getId().equals(commentId)) {
                getEntityManager().remove(comment);
                i.remove();
                getEntityManager().merge(this);
                break;
            }
        }
    }

    
    public void forward(OrganizationalEntityDAO orgEntity) {
        throw new UnsupportedOperationException("The delegate operation is no supported currently.");
    }

    
    public List<CommentDAO> getComments() {
        return comments;
    }

    
    public String getTaskDescription(String contentType) {
        String presentationDescriptionString = null;
        for (PresentationDescriptionDAO preDesc : this.getPresentationDescriptions()) {
            if (preDesc.getContentType().trim().equals(contentType.trim())) {
                presentationDescriptionString = preDesc.getValue();
                break;
            }
        }
        return presentationDescriptionString;
    }

    
    public void release() {
        for (Iterator<GenericHumanRoleDAO> iterator = getHumanRoles().iterator();
             iterator.hasNext(); ) {
            GenericHumanRoleDAO ghr = iterator.next();
            if (GenericHumanRoleDAO.GenericHumanRoleType.ACTUAL_OWNER.equals(ghr.getType())) {
                for (OrganizationalEntityDAO orgEntity : ghr.getOrgEntities()) {
                    orgEntity.getGenericHumanRoles().clear();
                    orgEntity.setGenericHumanRoles(null);
                }

                ghr.getOrgEntities().clear();
                ghr.setOrgEntities(null);
                ghr.setTask(null);
                iterator.remove();
                getEntityManager().remove(ghr);
                break;
            }
        }
        this.setStatus(TaskStatus.READY);
    }

    
    public void remove() {
        this.setStatus(TaskStatus.REMOVED);
    }

    
    public void resume() {
        this.setStatus(this.getStatusBeforeSuspension());
        this.setStatusBeforeSuspension(null);
        getEntityManager().merge(this);
    }

    
    public void activate() {
        this.setStatus(TaskStatus.READY);
        getEntityManager().merge(this);
    }

    
    public void skip() {
        this.setStatus(TaskStatus.OBSOLETE);
    }

    
    public void updateAndPersistComment(Long commentId, String newComment, String modifiedBy) {
        for (CommentDAO comment : this.getComments()) {
            if (comment.getId().equals(commentId)) {
                comment.setModifiedBy(modifiedBy);
                comment.setModifiedDate(new Date());
                comment.setCommentText(newComment);
                break;
            }
        }
        getEntityManager().merge(this);
    }

    
    public Boolean isSkipable() {
        return "Y".equalsIgnoreCase(skipableStr);
    }

    
    public MessageDAO getInputMessage() {
        return inputMessage;
    }

    
    public void setSkipable(Boolean skipable) {
        this.skipable = skipable;
        if( skipable) {
            this.skipableStr = "Y";
        } else {
            this.skipableStr = "N";
        }
    }

    
    public void nominate(List<OrganizationalEntityDAO> nominees) {
        if (nominees != null && nominees.size() > 0) {
            if (nominees.size() == 1) {
                OrganizationalEntityDAO nominee = nominees.get(0);
                if (OrganizationalEntityDAO.OrganizationalEntityType.GROUP.
                        equals(nominee.getOrgEntityType())) {
                    //TODO - implement me.
                    throw new UnsupportedOperationException("Group nominees are not supported");

                } else {
                    GenericHumanRoleDAO actualOwnerRole = new GenericHumanRole();
                    actualOwnerRole.setType(GenericHumanRole.GenericHumanRoleType.ACTUAL_OWNER);
                    actualOwnerRole.setTask(this);
                    actualOwnerRole.setOrgEntities(nominees);

                    for (OrganizationalEntityDAO orgE : nominees) {
                        orgE.addGenericHumanRole(actualOwnerRole);
                    }

                    this.addHumanRole(actualOwnerRole);
                    this.setStatus(TaskStatus.RESERVED);
                }

            } else {
                throw new UnsupportedOperationException("Multiple nominees are not supported yet.");
            }

        }

        getEntityManager().merge(this);
    }

    
    public void persistPriority(Integer newPriority) {
        this.setPriority(newPriority);
        this.getEntityManager().merge(this);
    }

    
    public void fail(String faultName, Element faultData) {
        if (faultData != null && StringUtils.isNotEmpty(faultName)) {
            MessageDAO faultMessage = new Message();
            faultMessage.setMessageType(MessageDAO.MessageType.FAILURE);
            faultMessage.setData(faultData);
            faultMessage.setName(QName.valueOf(faultName));
            faultMessage.setTask(this);
            this.setFailureMessage(faultMessage);
        }
        this.setStatus(TaskStatus.FAILED);
        this.getEntityManager().merge(this);
    }

    
    public void deleteFault() {
        this.getEntityManager().remove(this.getFailureMessage());
        this.setFailureMessage(null);
        this.getEntityManager().merge(this);
    }

    
    public void deleteOutput() {
        this.getEntityManager().remove(this.getOutputMessage());
        this.setOutputMessage(null);
        this.getEntityManager().merge(this);
    }

    
    public void persistFault(String faultName, Element faultData) {
        if (StringUtils.isNotEmpty(faultName) && faultData != null) {
            MessageDAO faultMessage = new Message();
            faultMessage.setMessageType(MessageDAO.MessageType.FAILURE);
            faultMessage.setData(faultData);
            faultMessage.setName(QName.valueOf(faultName));
            faultMessage.setTask(this);
            this.setFailureMessage(faultMessage);
            this.getEntityManager().merge(this);
        }
    }

    
    public void persistOutput(String outputName, Element outputData) {
        if (StringUtils.isNotEmpty(outputName) && outputData != null) {
            MessageDAO output = new Message();
            output.setMessageType(MessageDAO.MessageType.OUTPUT);

            Document doc = DOMUtils.newDocument();
            Element message = doc.createElement("message");
            doc.appendChild(message);
            Node importedNode = doc.importNode(outputData, true);
            message.appendChild(importedNode);
            output.setData(message);

            output.setName(QName.valueOf(outputName));
            output.setTask(this);
            this.setOutputMessage(output);
            this.getEntityManager().merge(this);
        }
    }

    
    public void persistToPotentialOwners(OrganizationalEntityDAO delegatee) {
        for (GenericHumanRoleDAO role : this.getHumanRoles()) {
            if (GenericHumanRoleDAO.GenericHumanRoleType.POTENTIAL_OWNERS.equals(role.getType())) {
                delegatee.addGenericHumanRole(role);
                role.addOrgEntity(delegatee);
                break;
            }
        }
    }

    
    public GenericHumanRoleDAO getGenericHumanRole(GenericHumanRoleDAO.GenericHumanRoleType type) {
        GenericHumanRoleDAO matchingRole = null;
        for (GenericHumanRoleDAO role : getHumanRoles()) {
            if (type.equals(role.getType())) {
                matchingRole = role;
            }
        }

        return matchingRole;
    }

    
    public void replaceOrgEntitiesForLogicalPeopleGroup(
            GenericHumanRoleDAO.GenericHumanRoleType type,
            List<OrganizationalEntityDAO> orgEntities) {
        for (GenericHumanRoleDAO role : this.getHumanRoles()) {
            if (type.equals(role.getType())) {
                role.getOrgEntities().clear();

                for (OrganizationalEntityDAO orgEntity : orgEntities) {
                    orgEntity.addGenericHumanRole(role);
                    role.addOrgEntity(orgEntity);
                }

                break;
            }
        }
        this.release();
    }


    // Update the updated on field before persisting the task object.
    @PrePersist
    @PreUpdate
    private void persistLastUpdated() {
        this.setUpdatedOn(new Date());
    }

    public void setTaskVersion(long version) {
        this.taskVersion = version;
    }

    public long getTaskVersion() {
        return taskVersion;
    }

    public void setTaskPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getTaskPackageName() {
        return this.packageName;
    }

    public void delete(){
        deletePresentationDescription();
        deletePresentationName();
        deletePresentationParameter();
        deletePresentationSubject();
        deletePresentationElements();

        deleteComments();
        deleteEvents();
        deleteGenericHumanRoles();
        deleteDeadlines();
        deleteMessages();
        deleteAttachments();
    }

    public void deleteInstance(){
        getEntityManager().remove(this);
    }

    private void deleteEvents(){
        getEntityManager().createNamedQuery(Event.DELETE_EVENTS_BY_TASK).setParameter("task", this).executeUpdate();
    }

    private void deleteAttachments(){
        getEntityManager().createNamedQuery(Attachment.DELETE_ATTACHMENTS_BY_TASK).setParameter("task", this).executeUpdate();
    }

    private void deletePresentationElements(){
        getEntityManager().createNamedQuery(PresentationElement.DELETE_PRESENTATION_ELEMENTS_FOR_TASK).setParameter("task", this).executeUpdate();
    }

    private void deletePresentationSubject(){
        getEntityManager().createNamedQuery(PresentationSubject.DELETE_PRESENTATION_SUBJECT_BY_TASK).setParameter("task", this).executeUpdate();
    }

    private void deletePresentationName(){
        getEntityManager().createNamedQuery(PresentationName.DELETE_PRESENTATION_NAME_BY_TASK).setParameter("task", this).executeUpdate();
    }

    private void deletePresentationParameter(){
        getEntityManager().createNamedQuery(PresentationParameter.DELETE_PRESENTATION_PARAMETERS_BY_TASK).setParameter("task", this).executeUpdate();
    }

    private void deletePresentationDescription(){
        getEntityManager().createNamedQuery(PresentationDescription.DELETE_PRESENTATION_DESCRIPTIONS_BY_TASK).setParameter("task", this).executeUpdate();
    }

    private void deleteGenericHumanRoles(){
        for(GenericHumanRoleDAO role:humanRoles){
            List<OrganizationalEntityDAO> orgEntities = role.getOrgEntities();
            for(OrganizationalEntityDAO orgEntity:orgEntities){
                getEntityManager().remove(orgEntity);
            }
        }
        getEntityManager().createNamedQuery(GenericHumanRole.DELETE_GHR_BY_TASK).setParameter("task", this).executeUpdate();
    }

    private void deleteDeadlines(){
        getEntityManager().createNamedQuery(Deadline.DELETE_DEADLINES_BY_TASK).setParameter("task", this).executeUpdate();
    }

    private void deleteComments(){
        getEntityManager().createNamedQuery(Comment.DELETE_COMMENTS_BY_TASK).setParameter("task", this).executeUpdate();
    }

    private void deleteMessages(){
        getEntityManager().createNamedQuery(Message.DELETE_MESSAGE_BY_TASK).setParameter("task", this).executeUpdate();
    }
}
