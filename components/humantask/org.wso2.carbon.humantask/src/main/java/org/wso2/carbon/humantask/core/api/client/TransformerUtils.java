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

package org.wso2.carbon.humantask.core.api.client;

import org.apache.axis2.databinding.types.URI;
import org.apache.axis2.databinding.utils.ConverterUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.attachment.mgt.skeleton.AttachmentMgtException;
import org.wso2.carbon.humantask.client.api.types.*;
import org.wso2.carbon.humantask.core.HumanTaskConstants;
import org.wso2.carbon.humantask.core.dao.*;
import org.wso2.carbon.humantask.core.dao.jpa.openjpa.model.*;
import org.wso2.carbon.humantask.core.dao.jpa.openjpa.model.OrganizationalEntity;
import org.wso2.carbon.humantask.core.engine.HumanTaskEngine;
import org.wso2.carbon.humantask.core.engine.HumanTaskException;
import org.wso2.carbon.humantask.core.engine.PeopleQueryEvaluator;
import org.wso2.carbon.humantask.core.engine.runtime.api.HumanTaskRuntimeException;
import org.wso2.carbon.humantask.core.engine.util.CommonTaskUtil;
import org.wso2.carbon.humantask.core.engine.util.OperationAuthorizationUtil;
import org.wso2.carbon.humantask.core.internal.HumanTaskServerHolder;
import org.wso2.carbon.humantask.core.internal.HumanTaskServiceComponent;
import org.wso2.carbon.humantask.core.store.HumanTaskBaseConfiguration;
import org.wso2.carbon.humantask.core.store.TaskConfiguration;

import javax.xml.namespace.QName;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Data transformer util. Contains methods transforming adb data types
 * to human task engine's objects and vice versa.
 */
public final class TransformerUtils {

    public static final Log log = LogFactory.getLog(TransformerUtils.class);

    private TransformerUtils() {
    }

    /**
     * Transforms the given list of CommentDAO to a list of TComment.
     *
     * @param comments : The CommentDAO list to be transformed.
     * @return : The transformed TComment list.
     */
    public static List<TComment> transformComments(List<CommentDAO> comments) {
        List<TComment> transformedComments = new ArrayList<TComment>();
        for (CommentDAO comment : comments) {
            TComment transformedComment = transformComment(comment);
            transformedComments.add(transformedComment);
        }
        return transformedComments;
    }

    /**
     * Transforms the CommentDAO to TComment type.
     *
     * @param comment : The CommentDAO to be transformed.
     * @return : The transformed object.
     */
    public static TComment transformComment(CommentDAO comment) {

        TComment transformedComment = new TComment();
        Calendar addedAt = Calendar.getInstance();
        addedAt.setTime(comment.getCommentedDate());
        transformedComment.setAddedTime(addedAt);

        TUser commentedBy = new TUser();
        commentedBy.setTUser(comment.getCommentedBy());
        transformedComment.setAddedBy(commentedBy);

        transformedComment.setText(comment.getCommentText());
        transformedComment.setId(ConverterUtil.convertToURI(comment.getId().toString()));

        TUser lastModifiedBy = new TUser();
        if (StringUtils.isNotEmpty(comment.getModifiedBy())) {
            lastModifiedBy.setTUser(comment.getModifiedBy());
        } else {
            lastModifiedBy.setTUser(comment.getCommentedBy());
        }
        transformedComment.setLastModifiedBy(lastModifiedBy);

        Calendar modifiedAt = Calendar.getInstance();
        if (comment.getModifiedDate() != null) {
            modifiedAt.setTime(comment.getModifiedDate());
        } else {
            modifiedAt.setTime(comment.getCommentedDate());
        }
        transformedComment.setLastModifiedTime(modifiedAt);

        return transformedComment;
    }

//    /**
//     * @param matchingTasks :
//     * @return :
//     */
//    public static TTaskSimpleQueryResultSet createSimpleQueryResultSet(
//            List<TaskDAO> matchingTasks) {
//
//        TTaskSimpleQueryResultSet resultSet = new TTaskSimpleQueryResultSet();
//
//        for (TaskDAO matchingTask : matchingTasks) {
//            resultSet.addRow(transformToSimpleQueryRow(matchingTask));
//        }
//
//        return resultSet;
//    }

    /**
     * @param matchingTask :
     * @return :
     */
    public static TTaskSimpleQueryResultRow transformToSimpleQueryRow(TaskDAO matchingTask) {
        TTaskSimpleQueryResultRow row = new TTaskSimpleQueryResultRow();

        row.setName(QName.valueOf(matchingTask.getDefinitionName()));
        row.setTaskType(matchingTask.getType().toString());
        try {
            row.setId(new URI(matchingTask.getId().toString()));
        } catch (URI.MalformedURIException e) {
            throw new HumanTaskRuntimeException("The task id :[" + matchingTask.getId() +
                    "] is invalid", e);
        }
        Calendar createdTime = Calendar.getInstance();
        createdTime.setTime(matchingTask.getCreatedOn());
        row.setCreatedTime(createdTime);

        //set the task priority.
        TPriority priority = new TPriority();
        priority.setTPriority(BigInteger.valueOf(matchingTask.getPriority()));
        row.setPriority(priority);

        //set the task status
        TStatus taskStatus = new TStatus();
        taskStatus.setTStatus(matchingTask.getStatus().toString());
        row.setStatus(taskStatus);
        row.setPresentationSubject((transformPresentationSubject(CommonTaskUtil.
                getDefaultPresentationSubject(matchingTask))));
        row.setPresentationName(transformPresentationName(CommonTaskUtil.
                getDefaultPresentationName(matchingTask)));

        return row;
    }

    /**
     * Transforms the TOrganizationalEntity type to OrganizationalEntity.
     *
     * @param tOEntity : The object to be transformed.
     * @return : The transformed object list.
     */
    public static List<OrganizationalEntityDAO> transformOrganizationalEntityList(
            TOrganizationalEntity
                    tOEntity) {
        HumanTaskEngine taskEngine = HumanTaskServiceComponent.getHumanTaskServer().getTaskEngine();
        HumanTaskDAOConnection daoConn = taskEngine.getDaoConnectionFactory().getConnection();

        List<OrganizationalEntityDAO> organizationalEntities = new ArrayList<OrganizationalEntityDAO>();
        TOrganizationalEntityChoice[] usersAndGroups = tOEntity.getTOrganizationalEntityChoice();
        for (TOrganizationalEntityChoice userOrGroup : usersAndGroups) {

            String userName = null;
            OrganizationalEntityDAO.OrganizationalEntityType type = null;

            if (userOrGroup.getUser() != null) {
                TUser user = userOrGroup.getUser();
                userName = user.getTUser().trim();
                type = OrganizationalEntityDAO.OrganizationalEntityType.USER;
            } else if (userOrGroup.getGroup() != null) {
                TGroup group = userOrGroup.getGroup();
                userName = group.getTGroup().trim();
                type = OrganizationalEntityDAO.OrganizationalEntityType.GROUP;
            }

            if (org.h2.util.StringUtils.isNullOrEmpty(userName) || type == null) {
                throw new HumanTaskRuntimeException("Cannot extract OrganizationalEntity from :"
                        + tOEntity);
            }
            OrganizationalEntityDAO orgEntity = daoConn.createNewOrgEntityObject(userName, type);
            organizationalEntities.add(orgEntity);
        }

        return organizationalEntities;
    }

    /**
     * Transforms a TSimpleQueryInput object to SimpleQueryCriteria object.
     *
     * @param tSimpleQueryInput : The object to be transformed.
     * @return : The transformed object.
     */
    public static SimpleQueryCriteria transformSimpleTaskQuery(
            TSimpleQueryInput tSimpleQueryInput) {

        if (tSimpleQueryInput == null) {
            throw new IllegalArgumentException("TSimpleQueryInput parameter passed to " +
                    "transformSimpleTaskQuery cannot be null");
        }

        SimpleQueryCriteria simpleQueryCriteria = new SimpleQueryCriteria();
        simpleQueryCriteria.setCreatedBy("");
        if (tSimpleQueryInput.getCreatedDate() != null) {
            simpleQueryCriteria.setCreatedOn(tSimpleQueryInput.getCreatedDate().getTime());
        }
        simpleQueryCriteria.setUpdatedBy("");
        if (tSimpleQueryInput.getUndatedDate() != null) {
            simpleQueryCriteria.setUpdatedOn(tSimpleQueryInput.getUndatedDate().getTime());
        }

        simpleQueryCriteria.setPageNumber(tSimpleQueryInput.getPageNumber());

        if(tSimpleQueryInput.getPageSize() > 0) {
           simpleQueryCriteria.setPageSize(tSimpleQueryInput.getPageSize());
        } else {
            simpleQueryCriteria.setPageSize(HumanTaskConstants.ITEMS_PER_PAGE);
        }
        simpleQueryCriteria.setTaskName(tSimpleQueryInput.getTaskName());

        simpleQueryCriteria.setSimpleQueryType(transformQueryCategory(
                tSimpleQueryInput.getSimpleQueryCategory()));
        if (tSimpleQueryInput.getQueryOrder() != null) {
            try {
                simpleQueryCriteria.setQueryOrder(SimpleQueryCriteria.QueryOrder.valueOf(
                        tSimpleQueryInput.getQueryOrder().getValue().toUpperCase()));
            } catch (IllegalArgumentException ex) {
                throw new IllegalArgumentException("Invalid QueryOrderBy parameter. Supported parameters are ASCENDING, DESCENDING");
            }
        }
        if (tSimpleQueryInput.getQueryOrderBy() != null) {
            try {
                simpleQueryCriteria.setQueryOrderBy(SimpleQueryCriteria.QueryOrderBy.valueOf(
                        tSimpleQueryInput.getQueryOrderBy().getValue().toUpperCase()));
            } catch (IllegalArgumentException ex) {
                throw new IllegalArgumentException("Invalid QueryOrder parameter. Supported parameters are TASK_NAME, CREATED_DATE, UPDATED_DATE, PRIORITY");
            }
        }

        return simpleQueryCriteria;
    }

    private static SimpleQueryCriteria.QueryType transformQueryCategory(
            TSimpleQueryCategory tSimpleQueryInput) {
        if (TSimpleQueryCategory.ALL_TASKS.equals(tSimpleQueryInput)) {
            return SimpleQueryCriteria.QueryType.ALL_TASKS;
        } else if (TSimpleQueryCategory.ASSIGNED_TO_ME.equals(tSimpleQueryInput)) {
            return SimpleQueryCriteria.QueryType.ASSIGNED_TO_ME;
        } else if (TSimpleQueryCategory.CLAIMABLE.equals(tSimpleQueryInput)) {
            return SimpleQueryCriteria.QueryType.CLAIMABLE;
        } else if (TSimpleQueryCategory.ASSIGNABLE.equals(tSimpleQueryInput)) {
            return SimpleQueryCriteria.QueryType.ASSIGNABLE;
        } else if (TSimpleQueryCategory.NOTIFICATIONS.equals(tSimpleQueryInput)) {
            return SimpleQueryCriteria.QueryType.NOTIFICATIONS;
        } else if (TSimpleQueryCategory.ADVANCED_QUERY.equals(tSimpleQueryInput)) {
            return SimpleQueryCriteria.QueryType.ADVANCED;
        } else {
            return SimpleQueryCriteria.QueryType.ALL_TASKS;
        }
    }

    /**
     * Transform a TaskDAO object to a TTaskAbstract object.
     *
     * @param task           : The TaskDAO object to be transformed.
     * @param callerUserName : The user name of the caller.
     * @return : The transformed TTaskAbstract object.
     */
    public static TTaskAbstract transformTask(final TaskDAO task, final String callerUserName) {
        TTaskAbstract taskAbstract = new TTaskAbstract();

        //Set the task Id
        try {
            taskAbstract.setId(new URI(task.getId().toString()));
        } catch (URI.MalformedURIException e) {
            log.warn("Invalid task Id found");
        }

        taskAbstract.setName(QName.valueOf(task.getDefinitionName()));
        taskAbstract.setRenderingMethodExists(true);

        //Set the created time
        Calendar calCreatedOn = Calendar.getInstance();
        calCreatedOn.setTime(task.getCreatedOn());
        taskAbstract.setCreatedTime(calCreatedOn);

        if (task.getUpdatedOn() != null) {
            Calendar updatedTime = Calendar.getInstance();
            updatedTime.setTime(task.getUpdatedOn());
            taskAbstract.setUpdatedTime(updatedTime);
        }

        //Set the activation time if exists.
        if (task.getActivationTime() != null) {
            Calendar calActivationTime = Calendar.getInstance();
            calActivationTime.setTime(task.getActivationTime());
            taskAbstract.setActivationTime(calActivationTime);
        }

        //Set the expiration time if exists.
        if (task.getExpirationTime() != null) {
            Calendar expirationTime = Calendar.getInstance();
            expirationTime.setTime(task.getExpirationTime());
            taskAbstract.setExpirationTime(expirationTime);
        }

        if (task.getStartByTime() != null) {
            taskAbstract.setStartByTimeExists(true);
        } else {
            taskAbstract.setStartByTimeExists(false);
        }

        if (task.getCompleteByTime() != null) {
            taskAbstract.setCompleteByTimeExists(true);
        } else {
            taskAbstract.setCompleteByTimeExists(false);
        }

        taskAbstract.setTaskType(task.getType().toString());
        taskAbstract.setHasSubTasks(CommonTaskUtil.hasSubTasks(task));
        taskAbstract.setHasComments(CommonTaskUtil.hasComments(task));
        taskAbstract.setHasAttachments(CommonTaskUtil.hasAttachments(task));
        taskAbstract.setHasFault(CommonTaskUtil.hasFault(task));
        taskAbstract.setHasOutput(CommonTaskUtil.hasOutput(task));
        taskAbstract.setEscalated(task.isEscalated());
        taskAbstract.setIsSkipable(task.isSkipable());
        taskAbstract.setStatus(transformStatus(task.getStatus()));
        taskAbstract.setPriority(transformPriority(task.getPriority()));
        taskAbstract.setPreviousStatus(transformStatus(task.getStatusBeforeSuspension()));
        taskAbstract.setHasPotentialOwners(CommonTaskUtil.hasPotentialOwners(task));

        if (CommonTaskUtil.getUserEntityForRole(task,
                GenericHumanRoleDAO.GenericHumanRoleType.ACTUAL_OWNER) != null) {
            taskAbstract.setActualOwner(createTUser(
                    CommonTaskUtil.getUserEntityForRole(task,
                            GenericHumanRoleDAO.
                                    GenericHumanRoleType.ACTUAL_OWNER)));
        }
        taskAbstract.setPotentialOwners(
                transformOrganizationalEntityList(
                        CommonTaskUtil.getOrgEntitiesForRole(task,
                                GenericHumanRoleDAO.
                                        GenericHumanRoleType.POTENTIAL_OWNERS)));
        taskAbstract.setBusinessAdministrators(
                transformOrganizationalEntityList(
                        CommonTaskUtil.getOrgEntitiesForRole(task,
                                GenericHumanRoleDAO.
                                        GenericHumanRoleType.BUSINESS_ADMINISTRATORS)));
        taskAbstract.setNotificationRecipients(
                transformOrganizationalEntityList(
                        CommonTaskUtil.getOrgEntitiesForRole(task,
                                GenericHumanRoleDAO.
                                        GenericHumanRoleType.NOTIFICATION_RECIPIENTS)));
        taskAbstract.setTaskStakeholders(
                transformOrganizationalEntityList(
                        CommonTaskUtil.getOrgEntitiesForRole(task,
                                GenericHumanRoleDAO.
                                        GenericHumanRoleType.STAKEHOLDERS)));
        taskAbstract.setTaskInitiator(createTUser(
                CommonTaskUtil.getUserEntityForRole(task, GenericHumanRoleDAO.
                        GenericHumanRoleType.TASK_INITIATOR)));

        HumanTaskBaseConfiguration baseConfiguration = CommonTaskUtil.getTaskConfiguration(task);
        if (baseConfiguration == null) {
            throw new HumanTaskRuntimeException("There's not matching task configuration for " +
                    "task" + task.getName());
        }
        // Set the versioned package name
        taskAbstract.setPackageName(baseConfiguration.getPackageName()+"-"+baseConfiguration.getVersion());

        taskAbstract.setTenantId(task.getTenantId());
        // If this is a task set the response operation and the port type.
        if (TaskType.TASK.equals(task.getType())) {
            TaskConfiguration taskConfig = (TaskConfiguration) baseConfiguration;
            taskAbstract.setResponseOperationName(taskConfig.getResponseOperation());
            taskAbstract.setResponseServiceName(taskConfig.getResponsePortType().toString());
        }

        taskAbstract.setPresentationName(
                transformPresentationName(CommonTaskUtil.getDefaultPresentationName(task)));
        taskAbstract.setPresentationSubject(
                transformPresentationSubject(CommonTaskUtil.getDefaultPresentationSubject(task)));
        taskAbstract.setPresentationDescription(
                transformPresentationDescription(CommonTaskUtil.getDefaultPresentationDescription(task)));

        //Setting attachment specific information
        taskAbstract.setHasAttachments(!task.getAttachments().isEmpty());
        taskAbstract.setNumberOfAttachments(task.getAttachments().size());

        return taskAbstract;
    }

    public static TOrganizationalEntity transformOrganizationalEntityList(
            List<OrganizationalEntityDAO> orgEntitiesForRole) {
        TOrganizationalEntity organizationalEntity = null;
        if (orgEntitiesForRole != null && orgEntitiesForRole.size() > 0) {
            organizationalEntity = new TOrganizationalEntity();
            TOrganizationalEntityChoice[] orgEntityChoiceArray =
                    new TOrganizationalEntityChoice[orgEntitiesForRole.size()];

            for (int i = 0; i < orgEntitiesForRole.size(); i++) {
                TOrganizationalEntityChoice choice = new TOrganizationalEntityChoice();
                OrganizationalEntityDAO orgEntity = orgEntitiesForRole.get(i);
                if (OrganizationalEntityDAO.OrganizationalEntityType.USER.equals
                        (orgEntity.getOrgEntityType())) {
                    TUser user = new TUser();
                    user.setTUser(orgEntity.getName());
                    choice.setUser(user);
                } else if (OrganizationalEntityDAO.OrganizationalEntityType.GROUP.equals(
                        orgEntity.getOrgEntityType())) {
                    TGroup group = new TGroup();
                    group.setTGroup(orgEntity.getName());
                    choice.setGroup(group);
                }
                orgEntityChoiceArray[i] = choice;
            }
            organizationalEntity.setTOrganizationalEntityChoice(orgEntityChoiceArray);
        }

        return organizationalEntity;
    }

    /**
     * Transforms the given PresentationSubjectDAO object to TPresentationSubject object.
     *
     * @param presentationSubjectDAO : The PresentationSubjectDAO object to be transformed.
     * @return : The transformed TPresentationSubject object.
     */
    public static TPresentationSubject transformPresentationSubject(
            PresentationSubjectDAO presentationSubjectDAO) {
        if (presentationSubjectDAO != null
                && StringUtils.isNotEmpty(presentationSubjectDAO.getValue())) {
            TPresentationSubject presentationSubject = new TPresentationSubject();
            presentationSubject.setTPresentationSubject(presentationSubjectDAO.getValue().
                    replaceAll("\\s+", " "));
            return presentationSubject;
        }
        return null;
    }

    /**
     * Transforms the given PresentationSubjectDAO object to TPresentationSubject object.
     *
     * @param presentationDescription : The PresentationSubjectDAO object to be transformed.
     * @return : The transformed TPresentationSubject object.
     */
    public static TPresentationDescription transformPresentationDescription(
            PresentationDescriptionDAO presentationDescription) {
        if (presentationDescription != null
                && StringUtils.isNotEmpty(presentationDescription.getValue())) {
            TPresentationDescription pDesc = new TPresentationDescription();
            pDesc.setTPresentationDescription(presentationDescription.getValue().replaceAll("\\s+", " "));
            return pDesc;
        }
        return null;
    }

    /**
     * Transforms the given PresentationNameDAO object to TPresentationName object.
     *
     * @param presentationNameDAO : The PresentationNameDAO object to be transformed.
     * @return : The transformed TPresentationName object.
     */
    public static TPresentationName transformPresentationName(
            PresentationNameDAO presentationNameDAO) {
        if (presentationNameDAO != null &&
                StringUtils.isNotEmpty(presentationNameDAO.getValue())) {
            TPresentationName presentationName = new TPresentationName();
            presentationName.setTPresentationName(presentationNameDAO.getValue().replaceAll("\\s+", " "));
            return presentationName;
        }
        return null;
    }

    private static TUser createTUser(OrganizationalEntityDAO actualOwnerOrgEntity) {
        TUser actualOwner = new TUser();
        if (actualOwnerOrgEntity != null && StringUtils.isNotEmpty(actualOwnerOrgEntity.getName())) {
            actualOwner.setTUser(actualOwnerOrgEntity.getName());
        } else {
            actualOwner.setTUser("");
        }

        return actualOwner;
    }

    /**
     * Transforms the task priority.
     *
     * @param priorityValue : The task priority int value
     * @return : The transformed task priority object.
     */
    public static TPriority transformPriority(Integer priorityValue) {
        TPriority priority = new TPriority();
        priority.setTPriority(BigInteger.valueOf(priorityValue));
        return priority;
    }


    /**
     * Transforms the task status.
     *
     * @param ts : The status to be transformed.
     * @return : The transformed status.
     */
    public static TStatus transformStatus(TaskStatus ts) {
        TStatus status = new TStatus();
        if (ts != null) {
            status.setTStatus(ts.toString());
        } else {
            status.setTStatus("");
        }
        return status;
    }

    /**
     * Creates the TTaskAuthorisationParams object based on the authorisations the caller has on the
     * given task
     *
     * @param task       : The TaskDAO object for the authorisations to be checked.
     * @param callerName : The caller user name.
     * @return : The TTaskAuthorisationParams object containing the authorisation parameters as
     *         boolean flags.
     */
    public static TTaskAuthorisationParams transformTaskAuthorization(TaskDAO task,
                                                                      String callerName) {

        PeopleQueryEvaluator pqe = HumanTaskServiceComponent
                .getHumanTaskServer().getTaskEngine().getPeopleQueryEvaluator();
        OrganizationalEntityDAO caller = pqe.createUserOrgEntityForName(callerName);

        TTaskAuthorisationParams authParams = new TTaskAuthorisationParams();
        if (TaskType.TASK.equals(task.getType())) {
            authParams.setAuthorisedToActivate(OperationAuthorizationUtil.authorisedToActivate(task, caller, pqe));
            authParams.setAuthorisedToClaim(OperationAuthorizationUtil.authorisedToClaim(task, caller, pqe));
            authParams.setAuthorisedToComment(OperationAuthorizationUtil.authorisedToComment(task, caller, pqe));
            authParams.setAuthorisedToComplete(OperationAuthorizationUtil.authorisedToComplete(task, caller, pqe));
            authParams.setAuthorisedToDelegate(OperationAuthorizationUtil.authorisedToDelegate(task, caller, pqe));
            authParams.setAuthorisedToDeleteFault(OperationAuthorizationUtil.authorisedToDeleteFault(task, caller, pqe));
            authParams.setAuthorisedToDeleteComment(OperationAuthorizationUtil.authorisedToDeleteComment(task, caller, pqe));
            authParams.setAuthorisedToDeleteOutput(OperationAuthorizationUtil.authorisedToDeleteOutput(task, caller, pqe));
            authParams.setAuthorisedToExit(OperationAuthorizationUtil.authorisedToExit(task, caller, pqe));
            authParams.setAuthorisedToFail(OperationAuthorizationUtil.authorisedToFail(task, caller, pqe));
            authParams.setAuthorisedToForward(OperationAuthorizationUtil.authorisedToForward(task, caller, pqe));
            authParams.setAuthorisedToGetComments(OperationAuthorizationUtil.authorisedToGetComments(task, caller, pqe));
            authParams.setAuthorisedToGetDescription(OperationAuthorizationUtil.authorisedToGetDescription(task, caller, pqe));
            authParams.setAuthorisedToGetInput(OperationAuthorizationUtil.authorisedToGetInput(task, caller, pqe));
            authParams.setAuthorisedToNominate(OperationAuthorizationUtil.authorisedToNominate(task, caller, pqe));
            authParams.setAuthorisedToRelease(OperationAuthorizationUtil.authorisedToRelease(task, caller, pqe));
            authParams.setAuthorisedToResume(OperationAuthorizationUtil.authorisedToResume(task, caller, pqe));
            authParams.setAuthorisedToRemove(OperationAuthorizationUtil.authorisedToRemove(task, caller, pqe));
            authParams.setAuthorisedToSetFault(OperationAuthorizationUtil.authorisedToSetFault(task, caller, pqe));
            authParams.setAuthorisedToSetOutput(OperationAuthorizationUtil.authorisedToSetOutput(task, caller, pqe));
            authParams.setAuthorisedToSetPriority(OperationAuthorizationUtil.authorisedToSetPriority(task, caller, pqe));
            authParams.setAuthorisedToSkip(OperationAuthorizationUtil.authorisedToSkip(task, caller, pqe));
            authParams.setAuthorisedToStart(OperationAuthorizationUtil.authorisedToStart(task, caller, pqe));
            authParams.setAuthorisedToStop(OperationAuthorizationUtil.authorisedToStop(task, caller, pqe));
            authParams.setAuthorisedToSuspend(OperationAuthorizationUtil.authorisedToSuspend(task, caller, pqe));
            authParams.setAuthorisedToUpdateComment(OperationAuthorizationUtil.authorisedToUpdateComment(task, caller, pqe));
        } else if (TaskType.NOTIFICATION.equals(task.getType())) {
            authParams.setAuthorisedToGetDescription(OperationAuthorizationUtil.authorisedToGetDescription(task, caller, pqe));
        }
        return authParams;
    }

    public static TTaskEvents transformTaskEvents(TaskDAO task, String caller) {
        TTaskEvents taskEvents = new TTaskEvents();

        if (task.getEvents() != null) {
            for (EventDAO taskEvent : task.getEvents()) {
                TTaskEvent tEvent = new TTaskEvent();
                tEvent.setEventDetail(taskEvent.getDetails());
                tEvent.setEventId(ConverterUtil.convertToURI(taskEvent.getId().toString()));


                TUser user = new TUser();
                user.setTUser(taskEvent.getUser());
                tEvent.setEventInitiator(user);

                //Set the created time
                Calendar eventTime = Calendar.getInstance();
                eventTime.setTime(taskEvent.getTimeStamp());
                tEvent.setEventTime(eventTime);


                tEvent.setEventType(taskEvent.getType().toString().toLowerCase());
                tEvent.setNewState(transformStatus(taskEvent.getNewState()));
                tEvent.setOldState(transformStatus(taskEvent.getOldState()));

                taskEvents.addEvent(tEvent);
            }
        }

        return taskEvents;
    }

    public static TAttachmentInfo[] transformAttachments(List<AttachmentDAO> attachmentList) {
        TAttachmentInfo[] array = new TAttachmentInfo[attachmentList.size()];
        int counter = 0;
        for (AttachmentDAO attachmentDAO : attachmentList) {
            TAttachmentInfo attachmentInfo = new TAttachmentInfo();

            attachmentInfo.setAccessType(attachmentDAO.getAccessType());
            try {
                log.debug("TAttachmentInfo(DTO) has the contentCategory, but the AttachmentDAO(DAO) doesn't support " +
                         "that attribute. Assume default attachment category as mime: " + HumanTaskConstants.ATTACHMENT_CONTENT_CATEGORY_MIME);
                attachmentInfo.setContentCategory(new URI(HumanTaskConstants.ATTACHMENT_CONTENT_CATEGORY_MIME));
            } catch (URI.MalformedURIException e) {
                log.error(e.getLocalizedMessage(), e);
            }

            try {
                String attachmentURI = attachmentDAO.getValue();
                URI attachmentURL = HumanTaskServerHolder.getInstance().getAttachmentService().getAttachmentService()
                        .getAttachmentInfoFromURL(attachmentURI).getUrl();
                attachmentInfo.setIdentifier(attachmentURL);
            } catch (AttachmentMgtException e) {
                log.error(e.getLocalizedMessage(), e);
            }

            attachmentInfo.setContentType(attachmentDAO.getContentType());

            Calendar cal = Calendar.getInstance();
            cal.setTime(attachmentDAO.getAttachedAt());
            attachmentInfo.setAttachedTime(cal);

            TUser user = new TUser();
            user.setTUser(attachmentDAO.getAttachedBy().getName());
            attachmentInfo.setAttachedBy(user);

            attachmentInfo.setName(attachmentDAO.getName());

            array[counter] = attachmentInfo;
            counter++;
        }
        return array;
    }

    /**
     * Generate an {@code AttachmentDAO} for a given attachment-id
     *
     * @param task task to be associated with the particular attachment
     * @param attachmentID id of the attachment, so this will be used to extract attachment information from the
     * attachment-mgt OSGi service
     *
     * @return reference to the created {@code AttachmentDAO}
     * @throws HumanTaskException If if was failed to retrieve data from the attachment-mgt OSGi service
     */
    public static AttachmentDAO generateAttachmentDAOFromID(TaskDAO task, String attachmentID) throws HumanTaskException {
        try {
            org.wso2.carbon.attachment.mgt.skeleton.types.TAttachment attachment = HumanTaskServerHolder.getInstance().getAttachmentService()
                    .getAttachmentService()
                    .getAttachmentInfo(attachmentID);

            AttachmentDAO dao = HumanTaskServiceComponent.getHumanTaskServer().getTaskEngine()
                    .getDaoConnectionFactory().getConnection().createAttachment();

            //Constructing the attachment DAO from the DTO
            dao.setName(attachment.getName());
            dao.setContentType(attachment.getContentType());
            dao.setTask((Task) task);

            String attachmentURL = attachment.getUrl().toString();
            //Extracting the attachment uri
            String attachmentUniqueID = attachmentURL.substring(attachmentURL.lastIndexOf("/") + 1);
            dao.setValue(attachmentUniqueID);

            dao.setAttachedAt(attachment.getCreatedTime().getTime());

            OrganizationalEntityDAO orgEntityDAO = HumanTaskServiceComponent.getHumanTaskServer().getTaskEngine()
                    .getDaoConnectionFactory().getConnection().createNewOrgEntityObject(attachment.getCreatedBy(),
                    OrganizationalEntityDAO.OrganizationalEntityType.USER);
            dao.setAttachedBy((OrganizationalEntity) orgEntityDAO);

            //TODO : "AccessType is not supported by Attachment-Mgt DTOs. So using a dummy value: " + HumanTaskConstants.DEFAULT_ATTACHMENT_ACCESS_TYPE);
            dao.setAccessType(HumanTaskConstants.DEFAULT_ATTACHMENT_ACCESS_TYPE);

            return dao;
        } catch (AttachmentMgtException e) {
            String errorMsg = "Attachment Data retrieval operation failed for attachment id:" + attachmentID + ". " + "Reason:";
            log.error(e.getLocalizedMessage(), e);
            throw new HumanTaskException(errorMsg + e.getLocalizedMessage(), e);
        }
    }

    /**
     * Generate a list of {@code AttachmentDAO} for a given list of attachment-id
     * @param task task to be associated with the particular attachment
     * @param attachmentIDs list of ids of the attachments, so these will be used to extract attachment information
     * from the attachment-mgt OSGi service
     *
     * @return a list of references to the created {@code AttachmentDAO}s
     * @throws HumanTaskException
     */
    public static List<AttachmentDAO> generateAttachmentDAOListFromIDs(TaskDAO task,
                                                                       List<String> attachmentIDs) throws HumanTaskException {
        List<AttachmentDAO> attachmentDAOList = new ArrayList<AttachmentDAO>();

        for (String attachmentID : attachmentIDs) {
            AttachmentDAO dao = generateAttachmentDAOFromID(task, attachmentID);
            attachmentDAOList.add(dao);
        }
        return attachmentDAOList;
    }
}
