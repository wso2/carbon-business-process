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
package org.wso2.carbon.humantask.core.api.client;

import org.apache.axis2.databinding.types.NCName;
import org.apache.axis2.databinding.types.URI;
import org.apache.axis2.databinding.utils.ConverterUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.humantask.client.api.*;
import org.wso2.carbon.humantask.client.api.types.*;
import org.wso2.carbon.humantask.core.HumanTaskConstants;
import org.wso2.carbon.humantask.core.dao.AttachmentDAO;
import org.wso2.carbon.humantask.core.dao.CommentDAO;
import org.wso2.carbon.humantask.core.dao.GenericHumanRoleDAO;
import org.wso2.carbon.humantask.core.dao.HumanTaskDAOConnection;
import org.wso2.carbon.humantask.core.dao.OrganizationalEntityDAO;
import org.wso2.carbon.humantask.core.dao.SimpleQueryCriteria;
import org.wso2.carbon.humantask.core.dao.TaskDAO;
import org.wso2.carbon.humantask.core.dao.TaskStatus;
import org.wso2.carbon.humantask.core.engine.HumanTaskCommand;
import org.wso2.carbon.humantask.core.engine.HumanTaskEngine;
import org.wso2.carbon.humantask.core.engine.HumanTaskException;
import org.wso2.carbon.humantask.core.engine.PeopleQueryEvaluator;
import org.wso2.carbon.humantask.core.engine.commands.Activate;
import org.wso2.carbon.humantask.core.engine.commands.AddComment;
import org.wso2.carbon.humantask.core.engine.commands.Claim;
import org.wso2.carbon.humantask.core.engine.commands.Complete;
import org.wso2.carbon.humantask.core.engine.commands.Delegate;
import org.wso2.carbon.humantask.core.engine.commands.DeleteComment;
import org.wso2.carbon.humantask.core.engine.commands.DeleteFault;
import org.wso2.carbon.humantask.core.engine.commands.DeleteOutput;
import org.wso2.carbon.humantask.core.engine.commands.Fail;
import org.wso2.carbon.humantask.core.engine.commands.GetComments;
import org.wso2.carbon.humantask.core.engine.commands.GetFault;
import org.wso2.carbon.humantask.core.engine.commands.GetInput;
import org.wso2.carbon.humantask.core.engine.commands.GetOutput;
import org.wso2.carbon.humantask.core.engine.commands.GetTaskDescription;
import org.wso2.carbon.humantask.core.engine.commands.Nominate;
import org.wso2.carbon.humantask.core.engine.commands.Release;
import org.wso2.carbon.humantask.core.engine.commands.Remove;
import org.wso2.carbon.humantask.core.engine.commands.Resume;
import org.wso2.carbon.humantask.core.engine.commands.SetFault;
import org.wso2.carbon.humantask.core.engine.commands.SetOutput;
import org.wso2.carbon.humantask.core.engine.commands.SetPriority;
import org.wso2.carbon.humantask.core.engine.commands.Skip;
import org.wso2.carbon.humantask.core.engine.commands.Start;
import org.wso2.carbon.humantask.core.engine.commands.Stop;
import org.wso2.carbon.humantask.core.engine.commands.Suspend;
import org.wso2.carbon.humantask.core.engine.commands.UpdateComment;
import org.wso2.carbon.humantask.core.engine.runtime.api.HumanTaskIllegalAccessException;
import org.wso2.carbon.humantask.core.engine.runtime.api.HumanTaskIllegalArgumentException;
import org.wso2.carbon.humantask.core.engine.runtime.api.HumanTaskIllegalOperationException;
import org.wso2.carbon.humantask.core.engine.runtime.api.HumanTaskIllegalStateException;
import org.wso2.carbon.humantask.core.engine.runtime.api.HumanTaskRuntimeException;
import org.wso2.carbon.humantask.core.engine.util.CommonTaskUtil;
import org.wso2.carbon.humantask.core.internal.HumanTaskServiceComponent;
import org.wso2.carbon.humantask.core.store.HumanTaskBaseConfiguration;
import org.wso2.carbon.humantask.core.utils.DOMUtils;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.UserStoreException;

import javax.xml.namespace.QName;
import java.util.*;
import java.util.concurrent.Callable;

/**
 * The implementation of the WS Human Task API Operations.
 */
public class TaskOperationsImpl extends AbstractAdmin
        implements HumanTaskClientAPIAdminSkeletonInterface {

    private static Log log = LogFactory.getLog(TaskOperationsImpl.class);

    /**
     * Simple query operation allows to search tasks based on given query criteria.
     *
     * @param tSimpleQueryInput : Query Criteria
     * @return : Query result
     * @throws IllegalStateFault
     * @throws IllegalArgumentFault
     */
    public TTaskSimpleQueryResultSet simpleQuery(final TSimpleQueryInput tSimpleQueryInput)
            throws IllegalStateFault, IllegalArgumentFault {

        final int[] taskCount = new int[1];

        try {
            List<TaskDAO> matchingTasks = HumanTaskServiceComponent.getHumanTaskServer().
                    getTaskEngine().getScheduler().execTransaction(new Callable<List<TaskDAO>>() {
                public List<TaskDAO> call() throws Exception {
                    HumanTaskDAOConnection daoConn = HumanTaskServiceComponent.getHumanTaskServer().
                            getDaoConnectionFactory().getConnection();
                    SimpleQueryCriteria queryCriteria = TransformerUtils.
                            transformSimpleTaskQuery(tSimpleQueryInput);
                    queryCriteria.setCallerTenantId(CarbonContext.getThreadLocalCarbonContext().getTenantId());
                    queryCriteria.setCaller(getCaller());
//                  queryCriteria.setPageSize(HumanTaskConstants.ITEMS_PER_PAGE);
                    TStatus[] statuses = tSimpleQueryInput.getStatus();
                    Set<TaskStatus> statusSet = new HashSet<TaskStatus>();
                    if (statuses != null && statuses.length > 0) {
                        for (TStatus status : statuses) {
                            try {
                                TaskStatus taskStatus = TaskStatus.valueOf(status.getTStatus().toUpperCase());
                                statusSet.add(taskStatus);
                            } catch (IllegalArgumentException ex) {
                                new IllegalArgumentFault(" Invalid Status ");
                            }
                        }
                    }
                    if (!statusSet.isEmpty()) {
                        queryCriteria.setStatuses(new ArrayList(statusSet));
                    }

                    if (statuses != null && statuses.length > 0) {
                        for (TStatus status : statuses) {
                            try {
                                TaskStatus taskStatus = TaskStatus.valueOf(status.getTStatus().toUpperCase());
                                statusSet.add(taskStatus);
                            } catch (IllegalArgumentException ex) {
                                new IllegalArgumentFault("Invalid Status");
                            }
                        }
                    }
                    if (!statusSet.isEmpty()) {
                        queryCriteria.setStatuses(new ArrayList(statusSet));
                    }
                    taskCount[0] =daoConn.getTasksCount(queryCriteria);
                    if(log.isDebugEnabled()) {
                        log.debug("No of tasks in the db : " + taskCount[0]);
                    }
                    return daoConn.searchTasks(queryCriteria);
                }
            });

            int taskListSize = matchingTasks.size();
            int pageSize = tSimpleQueryInput.getPageSize() > 0 ? tSimpleQueryInput.getPageSize() :
                    HumanTaskConstants.ITEMS_PER_PAGE;

            int pages = (int) Math.ceil((double) taskCount[0] / pageSize);

            if(log.isDebugEnabled()) {
                log.debug("No of task pages : " + pages + " with " + pageSize + " tasks per page");
            }
            TaskDAO[] instanceArray =
                    matchingTasks.toArray(new TaskDAO[taskListSize]);
            TTaskSimpleQueryResultSet resultSet = new TTaskSimpleQueryResultSet();
            resultSet.setPages(pages);
            for (int i = 0; i < taskListSize; i++) {
                resultSet.addRow(TransformerUtils.transformToSimpleQueryRow(instanceArray[i]));
            }
            return resultSet;
        } catch (HumanTaskIllegalStateException ex) {
            log.error(ex);
            throw new IllegalStateFault(ex);
        } catch (Exception ex) {
            log.error(ex);
            throw new IllegalArgumentFault(ex);
        }
    }


    public TBatchResponse[] batchStop(URI[] uris) {
        handleUnsupportedOperation();
        return new TBatchResponse[0];
    }

	private void handleUnsupportedOperation() {
        throw new UnsupportedOperationException("This operation is not currently supported in " +
                "this version of WSO2 BPS.");
    }


    public TTaskAbstract[] getMyTaskAbstracts(String s, String s1, String s2, TStatus[] tStatuses,
                                              String s3, String s4, String s5, int i, int i1)
            throws IllegalStateFault, IllegalOperationFault, IllegalArgumentFault {
        handleUnsupportedOperation();
        return new TTaskAbstract[0];
    }

    /**
     * Cancel/stop the processing of the task. The task returns to the Reserved state.
     *
     * @param taskId : task identifier
     * @throws IllegalStateFault
     * @throws IllegalOperationFault
     * @throws IllegalArgumentFault
     * @throws IllegalAccessFault
     */
    public void stop(final URI taskId) throws IllegalStateFault, IllegalOperationFault, IllegalArgumentFault,
            IllegalAccessFault {
        try {
            validateTaskId(taskId);
            HumanTaskServiceComponent.getHumanTaskServer().getTaskEngine().getScheduler().
                    execTransaction(new Callable<Object>() {
                        public Object call() throws Exception {
                            HumanTaskCommand stop = new Stop(getCaller(), new Long(taskId.toString()));
                            stop.execute();
                            return null;
                        }
                    });
        } catch (Exception ex) {
            handleException(ex);
        }
    }


    public TBatchResponse[] batchComplete(URI[] taskIds, Object o) {
        handleUnsupportedOperation();
        return new TBatchResponse[0];
    }

    /**
     * Resume a suspended task.
     *
     * @param taskId : task identifier
     * @throws IllegalStateFault
     * @throws IllegalOperationFault
     * @throws IllegalArgumentFault
     * @throws IllegalAccessFault
     */
    public void resume(final URI taskId)
            throws IllegalStateFault, IllegalOperationFault, IllegalArgumentFault,
            IllegalAccessFault {
        try {
            validateTaskId(taskId);
            HumanTaskServiceComponent.getHumanTaskServer().getTaskEngine().getScheduler().
                    execTransaction(new Callable<Object>() {
                        public Object call() throws Exception {
                            HumanTaskCommand resumeCommand =
                                    new Resume(getCaller(), new Long(taskId.toString()));
                            resumeCommand.execute();
                            return null;
                        }
                    });
        } catch (Exception ex) {
            handleException(ex);
        }
    }

    /**
     * Applies to both tasks and notifications.
     * Returns the rendering  types available for the task or notification.
     *
     * @param taskIdURI : task identifier
     * @return : Array of QNames
     * @throws IllegalArgumentFault
     */
    public QName[] getRenderingTypes(URI taskIdURI) throws IllegalArgumentFault {
        try {
            final Long taskId = validateTaskId(taskIdURI);

            TaskDAO task = HumanTaskServiceComponent.getHumanTaskServer().getTaskEngine().getScheduler().
                    execTransaction(new Callable<TaskDAO>() {
                        public TaskDAO call() throws Exception {

                            HumanTaskEngine engine = HumanTaskServiceComponent.getHumanTaskServer().getTaskEngine();
                            HumanTaskDAOConnection daoConn = engine.getDaoConnectionFactory().getConnection();
                            TaskDAO task = daoConn.getTask(taskId);
                            validateTaskTenant(task);
                            return task;
                        }
                    });
            HumanTaskBaseConfiguration taskConfiguration = HumanTaskServiceComponent.getHumanTaskServer().
                    getTaskStoreManager().getHumanTaskStore(task.getTenantId()).
                    getTaskConfiguration(QName.valueOf(task.getName()));
            QName[] types = (QName[]) taskConfiguration.getRenderingTypes().toArray();
        } catch (Exception ex) {
            log.error(ex);
            throw new IllegalArgumentFault(ex);
        }
        return new QName[0];
    }


    public void setTaskCompletionDeadlineExpression(URI taskId, NCName ncName, String s)
            throws IllegalStateFault, IllegalOperationFault, IllegalArgumentFault,
            IllegalAccessFault {
        validateTaskId(taskId);
        handleUnsupportedOperation();
    }

    /**
     * Set the data for the part of the task's output message.
     *
     * @param taskIdURI : task identifier
     * @param ncName    : PartName
     * @param o
     * @throws IllegalStateFault
     * @throws IllegalOperationFault
     * @throws IllegalArgumentFault
     * @throws IllegalAccessFault
     */
    public void setOutput(URI taskIdURI, NCName ncName, Object o)
            throws IllegalStateFault, IllegalOperationFault, IllegalArgumentFault,
            IllegalAccessFault {
        try {
            final Long taskId = validateTaskId(taskIdURI);
            if (ncName != null && o != null) {
                final String outputName = ncName.toString();
                final Element outputData = DOMUtils.stringToDOM((String)o);
                HumanTaskServiceComponent.getHumanTaskServer().getTaskEngine().getScheduler().
                        execTransaction(new Callable<Object>() {
                            public Object call() throws Exception {
                                SetOutput setOutputCommand =
                                        new SetOutput(getCaller(), taskId, outputName, outputData);
                                setOutputCommand.execute();
                                return null;
                            }
                        });

            } else {
                log.error("The output data for setOutput operation cannot be empty");
                throw new IllegalArgumentFault("The output data cannot be empty!");
            }
        } catch (Exception ex) {
            handleException(ex);
        }
    }


    public TTaskOperations getTaskOperations(URI taskId)
            throws IllegalOperationFault, IllegalArgumentFault {
        validateTaskId(taskId);
        handleUnsupportedOperation();
        return null;
    }


    public TBatchResponse[] batchRelease(URI[] taskId) {
        handleUnsupportedOperation();
        return new TBatchResponse[0];
    }


    public TTaskDetails getTaskDetails(URI taskId) throws IllegalArgumentFault {
        validateTaskId(taskId);
        handleUnsupportedOperation();
        return null;
    }


    public void forward(URI taskId, TOrganizationalEntity tOrganizationalEntity)
            throws IllegalStateFault, IllegalOperationFault, IllegalArgumentFault,
            IllegalAccessFault {
        validateTaskId(taskId);
        handleUnsupportedOperation();
    }


    public boolean isSubtask(URI taskId)
            throws IllegalStateFault, IllegalOperationFault, IllegalArgumentFault,
            IllegalAccessFault {
        validateTaskId(taskId);
        // As we do not support sub task with this release.
        return false;
    }

    /**
     * Suspend the task.
     *
     * @param taskId : task identifier
     * @throws IllegalStateFault
     * @throws IllegalOperationFault
     * @throws IllegalArgumentFault
     * @throws IllegalAccessFault
     */
    public void suspend(final URI taskId)
            throws IllegalStateFault, IllegalOperationFault, IllegalArgumentFault,
            IllegalAccessFault {

        try {
            validateTaskId(taskId);
            HumanTaskServiceComponent.getHumanTaskServer().getTaskEngine().getScheduler().
                    execTransaction(new Callable<Object>() {
                        public Object call() throws Exception {
                            HumanTaskCommand suspendCommand =
                                    new Suspend(getCaller(), new Long(taskId.toString()));
                            suspendCommand.execute();
                            return null;
                        }
                    });
        } catch (Exception ex) {
            handleException(ex);
        }
    }

    /**
     * Get Potential Owners of given Task.
     * @param taskIdURI : task identifier
     * @return : User name List
     * @throws IllegalStateFault
     * @throws IllegalArgumentFault
     */
    public TUser[] getAssignableUserList(URI taskIdURI) throws IllegalStateFault, IllegalArgumentFault {
        final int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();

        try {
            final Long taskId = validateTaskId(taskIdURI);
            return HumanTaskServiceComponent.getHumanTaskServer().getTaskEngine().getScheduler().
                    execTransaction(new Callable<TUser[]>() {
                        public TUser[] call() throws Exception {
                            HumanTaskEngine engine =
                                    HumanTaskServiceComponent.getHumanTaskServer().getTaskEngine();
                            HumanTaskDAOConnection daoConn =
                                    engine.getDaoConnectionFactory().getConnection();
                            TaskDAO task = daoConn.getTask(taskId);
                            validateTaskTenant(task);
                            List<String> roleNameList = CommonTaskUtil.getGenericHumanRoleGroupList(task,
                                    GenericHumanRoleDAO.GenericHumanRoleType.POTENTIAL_OWNERS);
                            Set<String> assignableUsers = CommonTaskUtil.getGenericHumanRoleUserList(task,
                                    GenericHumanRoleDAO.GenericHumanRoleType.POTENTIAL_OWNERS);
                            String actualOwnerUserName = null;
                            OrganizationalEntityDAO actualOwner =
                                    CommonTaskUtil.getUserEntityForRole(task,GenericHumanRoleDAO.GenericHumanRoleType.ACTUAL_OWNER);
                            if (actualOwner != null) {
                                actualOwnerUserName = actualOwner.getName();
                            }
                            if (roleNameList.size() > 0) {
                                for (String roleName : roleNameList) {
                                    assignableUsers.addAll(getUserListForRole(roleName, tenantId));
                                }
                            }
                            if (actualOwnerUserName != null && actualOwnerUserName.length() > 1) {
                                assignableUsers.remove(actualOwnerUserName);
                            }
                            List<String> excludedOwnersRoles = CommonTaskUtil.getGenericHumanRoleGroupList(task,
                                    GenericHumanRoleDAO.GenericHumanRoleType.EXCLUDED_OWNERS);
                            Set<String> excludedOwners = CommonTaskUtil.getGenericHumanRoleUserList(task,
                                    GenericHumanRoleDAO.GenericHumanRoleType.EXCLUDED_OWNERS);
                            if (excludedOwnersRoles.size() > 0) {
                                for (String roleName : excludedOwnersRoles) {
                                    excludedOwners.addAll(getUserListForRole(roleName, tenantId));
                                }
                            }
                            assignableUsers.removeAll(excludedOwners);
                            TUser[] tUserList = new TUser[assignableUsers.size()];
                            TUser user = null;
                            int i = 0;
                            for (String s : assignableUsers) {
                                user = new TUser();
                                user.setTUser(s);
                                tUserList[i] = user;
                                i++;
                            }
                            return tUserList;
                        }
                    });
        } catch (HumanTaskIllegalStateException ex) {
            log.error(ex);
            throw new IllegalStateFault(ex);
        } catch (Exception ex) {
            log.error(ex);
            throw new IllegalArgumentFault(ex);
        }
    }

    /**
     * Updates the identified comment with the supplied new text.
     * @param taskIdURI : task identifier
     * @param commentId : comment identifier
     * @param s : new comment in plain text.
     * @throws IllegalStateFault
     * @throws IllegalOperationFault
     * @throws IllegalArgumentFault
     * @throws IllegalAccessFault
     */
    public void updateComment(final URI taskIdURI, final URI commentId, final String s)
            throws IllegalStateFault, IllegalOperationFault, IllegalArgumentFault,
            IllegalAccessFault {
        try {
            final Long taskId = validateTaskId(taskIdURI);
            HumanTaskServiceComponent.getHumanTaskServer().getTaskEngine().getScheduler().
                    execTransaction(new Callable<Object>() {
                        public Object call() throws Exception {
                            UpdateComment updateCommentCommand =
                                    new UpdateComment(getCaller(), taskId, new Long(commentId.toString()), s);
                            updateCommentCommand.execute();
                            return null;
                        }
                    });
        } catch (Exception ex) {
            handleException(ex);
        }
    }

    /**
     * Return Task Abstract details for give Task ID. (Custom API) Similar to getMyTaskAbstracts method in HumanTask API
     * @param taskIdURI : task identifier
     * @return Task Abstract
     * @throws IllegalAccessFault
     */
    public TTaskAbstract loadTask(URI taskIdURI) throws IllegalAccessFault {
        try {
            final Long taskId = validateTaskId(taskIdURI);
            TaskDAO task = HumanTaskServiceComponent.getHumanTaskServer().getTaskEngine().getScheduler().
                    execTransaction(new Callable<TaskDAO>() {
                        public TaskDAO call() throws Exception {
                            HumanTaskEngine engine = HumanTaskServiceComponent.getHumanTaskServer().getTaskEngine();
                            HumanTaskDAOConnection daoConn = engine.getDaoConnectionFactory().getConnection();
                            TaskDAO task = daoConn.getTask(taskId);
                            validateTaskTenant(task);
                            return task;
                        }
                    });
            return TransformerUtils.transformTask(task, getCaller());
        }  catch (Exception ex) {
            log.error("Error occurred while load task.",ex);
            throw new IllegalAccessFault(ex);
        }

    }


    public TTaskDetails[] getMyTaskDetails(String s, String s1, String s2, TStatus[] tStatuses,
                                           String s3, String s4, String s5, int i, int i1)
            throws IllegalStateFault, IllegalOperationFault, IllegalArgumentFault {
        handleUnsupportedOperation();
        return new TTaskDetails[0];
    }


    public TBatchResponse[] batchNominate(URI[] uris) {
        handleUnsupportedOperation();
        return new TBatchResponse[0];
    }


    public URI[] getSubtaskIdentifiers(URI uri)
            throws IllegalStateFault, IllegalOperationFault, IllegalArgumentFault,
            IllegalAccessFault {
        handleUnsupportedOperation();
        return new URI[0];
    }


    public String getOutcome(URI uri) throws IllegalOperationFault, IllegalArgumentFault {
        validateTaskId(uri);
        handleUnsupportedOperation();
        return null;
    }

    /**
     * Returns the rendering specified by the type parameter.
     * @param taskIdURI task identifier
     * @param qName rendering type
     * @return rendering string
     * @throws IllegalArgumentFault
     */
    public Object getRendering(final URI taskIdURI,final QName qName) throws IllegalArgumentFault {
        final Long taskId = validateTaskId(taskIdURI);
        try {
            String rendering =  HumanTaskServiceComponent.getHumanTaskServer().getTaskEngine().getScheduler().
                    execTransaction(new Callable<String>() {
                        public String call() throws Exception {
                            HumanTaskEngine engine = HumanTaskServiceComponent.getHumanTaskServer().getTaskEngine();
                            HumanTaskDAOConnection daoConn = engine.getDaoConnectionFactory().getConnection();
                            TaskDAO task = daoConn.getTask(taskId);
                            validateTaskTenant(task);
                            HumanTaskBaseConfiguration taskConfiguration = HumanTaskServiceComponent.getHumanTaskServer().
                                    getTaskStoreManager().getHumanTaskStore(task.getTenantId()).
                                    getTaskConfiguration(QName.valueOf(task.getName()));
                            return CommonTaskUtil.getRendering(task, taskConfiguration, qName);

                        }
                    });
	        return rendering;
        } catch (Exception e) {
            log.error(e);
            throw new IllegalArgumentFault(e);
        }
    }

    /**
     * Skip the task.
     * @param taskIdURI : task identifier
     * @throws IllegalStateFault
     * @throws IllegalOperationFault
     * @throws IllegalArgumentFault
     * @throws IllegalAccessFault
     */
    public void skip(URI taskIdURI) throws IllegalStateFault, IllegalOperationFault, IllegalArgumentFault,
            IllegalAccessFault {
        try {
            final Long taskId = validateTaskId(taskIdURI);
            HumanTaskServiceComponent.getHumanTaskServer().getTaskEngine().getScheduler().
                    execTransaction(new Callable<Object>() {
                        public Object call() throws Exception {
                            Skip skipCommand = new Skip(getCaller(), taskId);
                            skipCommand.execute();
                            return null;
                        }
                    });
        } catch (Exception ex) {
            handleException(ex);
        }
    }


    public TBatchResponse[] batchFail(URI[] taskIds, TFault tFault) {
        handleUnsupportedOperation();
        return new TBatchResponse[0];
    }


    public void setTaskCompletionDurationExpression(URI uri, NCName ncName, String s)
            throws IllegalStateFault, IllegalOperationFault, IllegalArgumentFault,
            IllegalAccessFault {
        handleUnsupportedOperation();
    }

    /**
     * Start the task
     * @param taskId : task identifier
     * @throws IllegalStateFault
     * @throws IllegalOperationFault
     * @throws IllegalArgumentFault
     * @throws IllegalAccessFault
     */
    public void start(final URI taskId)
            throws IllegalStateFault, IllegalOperationFault, IllegalArgumentFault,
            IllegalAccessFault {

        try {
            validateTaskId(taskId);
            HumanTaskServiceComponent.getHumanTaskServer().getTaskEngine().getScheduler().
                    execTransaction(new Callable<Object>() {
                        public Object call() throws Exception {
                            Start startCommand = new Start(getCaller(), new Long(taskId.toString()));
                            startCommand.execute();
                            return null;
                        }
                    });
        } catch (Exception ex) {
            handleException(ex);
        }
    }

    /**
     * Execution of the task fails
     * @param taskIdURI : task identifier
     * @param tFault
     * @throws IllegalStateFault
     * @throws IllegalOperationFault
     * @throws IllegalArgumentFault
     * @throws IllegalAccessFault
     */
    public void fail(final URI taskIdURI, final TFault tFault)
            throws IllegalStateFault, IllegalOperationFault, IllegalArgumentFault,
            IllegalAccessFault {

        try {
            final Long taskID = validateTaskId(taskIdURI);

            HumanTaskServiceComponent.getHumanTaskServer().getTaskEngine().getScheduler().
                    execTransaction(new Callable<Object>() {
                        public Object call() throws Exception {
                            String faultName = null;
                            Element faultData = null;
                            if (tFault != null) {
                                faultName = tFault.getFaultName().toString();
                                faultData = DOMUtils.getElementFromObject(tFault.getFaultData());
                            }
                            Fail failCommand = new Fail(getCaller(), taskID, faultName, faultData);
                            failCommand.execute();
                            return null;
                        }
                    });
        } catch (Exception ex) {
            handleException(ex);
        }
    }

    /**
     * Activate the task, i.e. set the task to status Ready (Administrative Operations)
     * @param taskIdURI : task identifier
     * @throws IllegalStateFault
     * @throws IllegalOperationFault
     * @throws IllegalArgumentFault
     * @throws IllegalAccessFault
     */
    public void activate(URI taskIdURI)
            throws IllegalStateFault, IllegalOperationFault, IllegalArgumentFault,
            IllegalAccessFault {

        try {
            final Long taskID = validateTaskId(taskIdURI);

            HumanTaskServiceComponent.getHumanTaskServer().getTaskEngine().getScheduler().
                    execTransaction(new Callable<Object>() {
                        public Object call() throws Exception {
                            Activate activateCommand = new Activate(getCaller(), taskID);
                            activateCommand.execute();
                            return null;
                        }
                    });
        } catch (Exception ex) {
            handleException(ex);
        }
    }

    /**
     * Add a comment to a task.
     * @param taskIdURI : task identifier
     * @param commentString : comment in plain text
     * @return an identifier that can be used to later update or delete the comment.
     * @throws IllegalStateFault
     * @throws IllegalOperationFault
     * @throws IllegalArgumentFault
     * @throws IllegalAccessFault
     */
    public URI addComment(final URI taskIdURI, final String commentString)
            throws IllegalStateFault, IllegalOperationFault, IllegalArgumentFault,
            IllegalAccessFault {

        try {
            validateTaskId(taskIdURI);
            Validate.notEmpty(commentString, "The comment string cannot be empty");
            return HumanTaskServiceComponent.getHumanTaskServer().getTaskEngine().getScheduler().
                    execTransaction(new Callable<URI>() {
                        public URI call() throws Exception {
                            AddComment addComment = new AddComment(getCaller(), new Long(taskIdURI.toString()), commentString);
                            addComment.execute();
                            CommentDAO persisted = addComment.getPersistedComment();
                            if (persisted != null) {
                                return ConverterUtil.convertToURI(persisted.getId().toString());
                            } else {
                                throw new IllegalStateFault("The persisted comment is null. " +
                                        "See error log for more details.");
                            }
                        }
                    });
        } catch (Exception ex) {
            handleException(ex);
        }
        return null;
    }

    /**
     * Deletes the identified comment.
     * @param taskIdURI : task identifier
     * @param commentId : comment identifier
     * @throws IllegalStateFault
     * @throws IllegalOperationFault
     * @throws IllegalArgumentFault
     * @throws IllegalAccessFault
     */
    public void deleteComment(final URI taskIdURI, final URI commentId)
            throws IllegalStateFault, IllegalOperationFault, IllegalArgumentFault,
            IllegalAccessFault {

        try {
            final Long taskId = validateTaskId(taskIdURI);

            HumanTaskServiceComponent.getHumanTaskServer().getTaskEngine().getScheduler().
                    execTransaction(new Callable<Object>() {
                        public Object call() throws Exception {
                            DeleteComment deleteComment =
                                    new DeleteComment(getCaller(), taskId, new Long(commentId.toString()));
                            deleteComment.execute();
                            return null;
                        }
                    });
        } catch (Exception ex) {
            handleException(ex);
        }
    }

    /**
     * Assign the task to one user and set the task to state Reserved.
     * @param taskId : task identifier
     * @param delegatee : organizational entity (htt:tOrganizationalEntity)
     * @throws IllegalStateFault
     * @throws IllegalOperationFault
     * @throws IllegalArgumentFault
     * @throws RecipientNotAllowedException
     * @throws IllegalAccessFault
     */
    public void delegate(final URI taskId, final TOrganizationalEntity delegatee)
            throws IllegalStateFault, IllegalOperationFault, IllegalArgumentFault,
            RecipientNotAllowedException, IllegalAccessFault {

        try {
            validateTaskId(taskId);
            if (delegatee == null) {
                throw new IllegalArgumentFault("The delegatee cannot be null!");
            }
            HumanTaskServiceComponent.getHumanTaskServer().getTaskEngine().getScheduler().
                    execTransaction(new Callable<Object>() {
                        public Object call() throws Exception {
                            List<OrganizationalEntityDAO> orgEntities = TransformerUtils.
                                    transformOrganizationalEntityList(delegatee);
                            if (orgEntities.size() > 1) {
                                throw new IllegalArgumentFault("There can be only 1 delegatee of type user!");
                            }

                            Delegate delegateCommand = new Delegate(getCaller(),
                                    new Long(taskId.toString()), orgEntities.get(0));
                            delegateCommand.execute();
                            return null;
                        }
                    });
        } catch (Exception ex) {
            handleException(ex);
        }
    }

    /**
     * Get all comments of a task
     * @param taskIdURI : task identifier
     * @return : All comments
     * @throws IllegalStateFault
     * @throws IllegalOperationFault
     * @throws IllegalArgumentFault
     * @throws IllegalAccessFault
     */
    public TComment[] getComments(URI taskIdURI)
            throws IllegalStateFault, IllegalOperationFault, IllegalArgumentFault,
            IllegalAccessFault {
        try {
            final Long taskId = validateTaskId(taskIdURI);
            return HumanTaskServiceComponent.getHumanTaskServer().getTaskEngine().getScheduler().
                    execTransaction(new Callable<TComment[]>() {
                        public TComment[] call() throws Exception {
                            GetComments getComments = new GetComments(getCaller(), taskId);
                            getComments.execute();
                            List<TComment> comments =
                                    TransformerUtils.transformComments(getComments.getComments());
                            return comments.toArray(new TComment[comments.size()]);
                        }
                    });
        } catch (Exception ex) {
            handleException(ex);
        }
        return null;
    }

    /**
     * Get details of a task including rendering elements, similar to load task operation.
     * @param taskIdentifier : task identifier
     * @param properties : properties for getTaskInstanceData
     * @param tRenderingTypes : rendering types.
     * @return
     * @throws IllegalOperationFault
     * @throws IllegalArgumentFault
     * @throws IllegalAccessFault
     */
    public TTaskInstanceData getTaskInstanceData(URI taskIdentifier, String  properties,
                                                 TRenderingTypes[] tRenderingTypes)
            throws IllegalOperationFault, IllegalArgumentFault, IllegalAccessFault {
        final Long taskId = validateTaskId(taskIdentifier);
        return null;
    }


    public TTaskDetails getParentTask(URI taskId)
            throws IllegalStateFault, IllegalOperationFault, IllegalArgumentFault,
            IllegalAccessFault {
        handleUnsupportedOperation();
        return null;
    }


    public TBatchResponse[] batchResume(URI[] taskIds) {
        handleUnsupportedOperation();
        return new TBatchResponse[0];
    }


    public TBatchResponse[] batchRemove(URI[] taskIds) {
        handleUnsupportedOperation();
        return new TBatchResponse[0];
    }


    public TAttachment[] getAttachment(URI taskIdentifier, URI attachmentIdentifier)
            throws IllegalStateFault, IllegalOperationFault, IllegalArgumentFault,
            IllegalAccessFault {

        handleUnsupportedOperation();
        return new TAttachment[0];
    }

    /**
     * Add attachment to a task. Returns an identifier for the attachment.
     * @param taskIdentifier : task identifier
     * @param name : attachment name
     * @param accessType : access type
     * @param contentType : content type
     * @param attachment : attachment ID (String)
     * @return
     * @throws IllegalStateFault
     * @throws IllegalOperationFault
     * @throws IllegalArgumentFault
     * @throws IllegalAccessFault
     */
    public boolean addAttachment(URI taskIdentifier, String name, String accessType, String contentType, Object attachment)
            throws IllegalStateFault, IllegalOperationFault, IllegalArgumentFault,
                   IllegalAccessFault {

        final Long taskId = validateTaskId(taskIdentifier);
        final String attachmentID = (String) attachment;

        try {
            Boolean isAdded = HumanTaskServiceComponent.getHumanTaskServer().getTaskEngine()
                        .getScheduler().execTransaction(new Callable<Boolean>() {
                public Boolean call() throws Exception {
                    HumanTaskEngine engine = HumanTaskServiceComponent.getHumanTaskServer().getTaskEngine();
                    HumanTaskDAOConnection daoConn = engine.getDaoConnectionFactory().getConnection();
                    TaskDAO taskDAO = daoConn.getTask(taskId);
                    validateTaskTenant(taskDAO);
                    try {
                        boolean isAdded = taskDAO.addAttachment(TransformerUtils.generateAttachmentDAOFromID(taskDAO,
                                                                                                    attachmentID));

                        if (!isAdded) {
                            throw new HumanTaskException("Attachment with id: " + attachmentID +  "was not associated " +
                                                         "task with id:" + taskId);
                        }

                        return isAdded;
                    } catch (HumanTaskException ex) {
                        String errMsg = "getAttachmentInfos operation failed. Reason: ";
                        log.error(errMsg + ex.getLocalizedMessage(), ex);
                        throw ex;
                    }
                }
            });
            return isAdded;
        } catch (Exception ex) {
            handleException(ex);
        }
        return false;
    }

    /**
     * Get attachment information for all attachments associated with the task.
     * @param taskIdentifier : task identifier
     * @return
     * @throws IllegalAccessFault
     */
    public TAttachmentInfo[] getAttachmentInfos(final URI taskIdentifier) throws IllegalAccessFault {

        final Long taskId = validateTaskId(taskIdentifier);
        try {
            List<AttachmentDAO> attachmentList = HumanTaskServiceComponent.getHumanTaskServer().getTaskEngine()
                    .getScheduler().
                    execTransaction(new Callable<List<AttachmentDAO>>() {
                        public List<AttachmentDAO> call() throws Exception {
                            HumanTaskEngine engine = HumanTaskServiceComponent.getHumanTaskServer().getTaskEngine();
                            HumanTaskDAOConnection daoConn = engine.getDaoConnectionFactory().getConnection();
                            TaskDAO task = daoConn.getTask(taskId);
                            validateTaskTenant(task);
                            return task.getAttachments();
                        }
                    });
            return TransformerUtils.transformAttachments(attachmentList);
        } catch (Exception ex) {
            log.error(ex);
            throw new IllegalAccessFault(ex);
        }
    }

    /**
     * Applies to notifications only.
     * Used by notification recipients to remove the notification permanently from their task list client.
     * @param taskId : Notification identifier
     * @throws IllegalOperationFault
     * @throws IllegalArgumentFault
     * @throws IllegalAccessFault
     */
    public void remove(URI taskId)
            throws IllegalOperationFault, IllegalArgumentFault, IllegalAccessFault {

        try {
            final Long notificationId = validateTaskId(taskId);
            HumanTaskServiceComponent.getHumanTaskServer().getTaskEngine().getScheduler().
                    execTransaction(new Callable<Object>() {
                        public Object call() throws Exception {
                            Remove removeCommand = new Remove(getCaller(), notificationId);
                            removeCommand.execute();
                            return null;
                        }
                    });
        } catch (HumanTaskIllegalOperationException ex) {
            log.error(ex);
            throw new IllegalOperationFault(ex);
        } catch (HumanTaskIllegalAccessException ex) {
            log.error(ex);
            throw new IllegalAccessFault(ex);
        } catch (Exception ex) {
            log.error(ex);
            throw new IllegalArgumentFault(ex);
        }
    }


    public TBatchResponse[] batchStart(URI[] taskIds) {

        handleUnsupportedOperation();
        return new TBatchResponse[0];
    }


    public URI instantiateSubtask(URI taskId, String s)
            throws IllegalStateFault, IllegalOperationFault, IllegalArgumentFault,
            IllegalAccessFault {
        handleUnsupportedOperation();
        return null;
    }

    /**
     * Returns authorisation parameters for given task id (Custom API)
     * @param taskIdURI : task identifier
     * @return
     * @throws IllegalStateFault
     * @throws IllegalArgumentFault
     */
    public TTaskAuthorisationParams loadAuthorisationParams(URI taskIdURI)
            throws IllegalStateFault, IllegalArgumentFault {

        try {
            final Long taskId = validateTaskId(taskIdURI);
            return HumanTaskServiceComponent.getHumanTaskServer().getTaskEngine().getScheduler().
                    execTransaction(new Callable<TTaskAuthorisationParams>() {
                        public TTaskAuthorisationParams call() throws Exception {
                            HumanTaskEngine engine =
                                    HumanTaskServiceComponent.getHumanTaskServer().getTaskEngine();
                            HumanTaskDAOConnection daoConn =
                                    engine.getDaoConnectionFactory().getConnection();
                            TaskDAO task = daoConn.getTask(taskId);
                            validateTaskTenant(task);
                            return TransformerUtils.transformTaskAuthorization(task, getCaller());
                        }
                    });
        } catch (HumanTaskIllegalArgumentException ex) {
            log.error(ex);
            throw new IllegalArgumentFault(ex);
        } catch (Exception ex) {
            log.error(ex);
            throw new IllegalStateFault(ex);
        }
    }


    public TTaskEventType[] getTaskHistory(URI uri, TTaskHistoryFilter tTaskHistoryFilter, int i,
                                           int i1, boolean b)
            throws IllegalOperationFault, IllegalArgumentFault, IllegalAccessFault {

        handleUnsupportedOperation();
        return new TTaskEventType[0];
    }


    public void setTaskStartDeadlineExpression(URI uri, NCName ncName, String s)
            throws IllegalStateFault, IllegalOperationFault, IllegalArgumentFault,
            IllegalAccessFault {

        handleUnsupportedOperation();
    }

    /**
     * Load all task events. (Custom API)
     * @param taskIdURI : task identifier
     * @return
     * @throws IllegalArgumentFault
     * @throws IllegalStateFault
     */
    public TTaskEvents loadTaskEvents(URI taskIdURI)
            throws IllegalArgumentFault, IllegalStateFault {

        try {
            final Long taskId = validateTaskId(taskIdURI);
            return HumanTaskServiceComponent.getHumanTaskServer().getTaskEngine().getScheduler().
                    execTransaction(new Callable<TTaskEvents>() {
                        public TTaskEvents call() throws Exception {
                            HumanTaskEngine engine =
                                    HumanTaskServiceComponent.getHumanTaskServer().getTaskEngine();
                            HumanTaskDAOConnection daoConn =
                                    engine.getDaoConnectionFactory().getConnection();
                            TaskDAO task = daoConn.getTask(taskId);
                            validateTaskTenant(task);
                            return TransformerUtils.transformTaskEvents(task, getCaller());
                        }
                    });
        } catch (HumanTaskIllegalArgumentException ex) {
            log.error(ex);
            throw new IllegalArgumentFault(ex);
        } catch (Exception ex) {
            log.error(ex);
            throw new IllegalStateFault(ex);
        }
    }


    public TBatchResponse[] batchDelegate(URI[] uris, TOrganizationalEntity tOrganizationalEntity) {

        handleUnsupportedOperation();
        return new TBatchResponse[0];
    }


    public TBatchResponse[] batchSetGenericHumanRole(URI[] uris, String s,
                                                     TOrganizationalEntity tOrganizationalEntity) {

        handleUnsupportedOperation();
        return new TBatchResponse[0];
    }


    public void setGenericHumanRole(URI uri, String s, TOrganizationalEntity tOrganizationalEntity)
            throws IllegalStateFault, IllegalOperationFault, IllegalArgumentFault,
            IllegalAccessFault {

        validateTaskId(uri);
        handleUnsupportedOperation();
    }

    /**
     * Get the data for the part of the task's input message.
     * @param taskIdURI : task identifier
     * @param inputIdentifier : input part name
     * @return : Input String
     * @throws IllegalStateFault
     * @throws IllegalOperationFault
     * @throws IllegalArgumentFault
     * @throws IllegalAccessFault
     */
    public Object getInput(final URI taskIdURI, final NCName inputIdentifier)
            throws IllegalStateFault, IllegalOperationFault, IllegalArgumentFault,
            IllegalAccessFault {

        try {
            validateTaskId(taskIdURI);
            return HumanTaskServiceComponent.getHumanTaskServer().getTaskEngine().getScheduler().
                    execTransaction(new Callable<Object>() {
                        public Object call() throws Exception {
                            Long taskId = validateTaskId(taskIdURI);
                            String partName = "";
                            if (inputIdentifier != null) {
                                partName = inputIdentifier.toString().trim();
                            }

                            GetInput getInput = new GetInput(getCaller(), taskId, partName);
                            getInput.execute();
                            Node input = getInput.getInputElement();
                            return DOMUtils.domToString(input);
                        }
                    });
        } catch (Exception ex) {
            handleException(ex);
        }
        return null;
    }


    public TBatchResponse[] batchSkip(URI[] taskIds) {

        handleUnsupportedOperation();
        return new TBatchResponse[0];
    }

    /**
     * Execution of the task finished successfully.
     * @param taskIdURI : task identifier
     * @param outputStr : task outcome (String)
     * @throws IllegalStateFault
     * @throws IllegalOperationFault
     * @throws IllegalArgumentFault
     * @throws IllegalAccessFault
     */
    public void complete(final URI taskIdURI, final String outputStr)
            throws IllegalStateFault, IllegalOperationFault, IllegalArgumentFault,
            IllegalAccessFault {

        try {
            final Long taskId = validateTaskId(taskIdURI);
            HumanTaskServiceComponent.getHumanTaskServer().getTaskEngine().getScheduler().
                    execTransaction(new Callable<Object>() {
                        public Object call() throws Exception {
                            Element output = DOMUtils.stringToDOM(outputStr);
                            Complete completeCommand = new Complete(getCaller(), taskId, output);
                            completeCommand.execute();
                            return null;
                        }
                    });
        } catch (Exception ex) {
            handleException(ex);
        }
    }

    /**
     * Checks given task has sub tasks.
     * @param taskId : task identifier
     * @return : false, Since current Implementation doesn't support sub tasks.
     * @throws IllegalStateFault
     * @throws IllegalOperationFault
     * @throws IllegalArgumentFault
     * @throws IllegalAccessFault
     */
    public boolean hasSubtasks(URI taskId)
            throws IllegalStateFault, IllegalOperationFault, IllegalArgumentFault,
            IllegalAccessFault {

        validateTaskId(taskId);
        return false;
    }


    public TBatchResponse[] batchActivate(URI[] taskIds) {
        handleUnsupportedOperation();
        return new TBatchResponse[0];
    }

    /**
     * Claim responsibility for a task, i.e. set the task to status Reserved
     * @param taskIdURI : task identifier
     * @throws IllegalStateFault
     * @throws IllegalOperationFault
     * @throws IllegalArgumentFault
     * @throws IllegalAccessFault
     */
    public void claim(final URI taskIdURI)
            throws IllegalStateFault, IllegalOperationFault, IllegalArgumentFault,
            IllegalAccessFault {
        try {
            final Long taskId = validateTaskId(taskIdURI);
            HumanTaskServiceComponent.getHumanTaskServer().getTaskEngine().getScheduler().
                    execTransaction(new Callable<Object>() {
                        public Object call() throws Exception {
                            HumanTaskCommand claim = new Claim(getCaller(), taskId);
                            claim.execute();
                            return null;
                        }
                    });
        } catch (Exception ex) {
            handleException(ex);
        }
    }


    public TTaskQueryResultSet query(String s, String s1, String s2, int i, int i1)
            throws IllegalStateFault, IllegalArgumentFault {
        handleUnsupportedOperation();
        return null;
    }


    public TBatchResponse[] batchClaim(URI[] uris) {
        handleUnsupportedOperation();
        return new TBatchResponse[0];
    }


    public TBatchResponse[] batchSetPriority(URI[] uris, TPriority tPriority) {
        handleUnsupportedOperation();
        return new TBatchResponse[0];
    }

    /**
     * Set the fault data of the task.
     * @param taskIdURI : task identifier
     * @param tFault fault – contains the fault name and fault data
     * @throws IllegalStateFault
     * @throws IllegalOperationFault
     * @throws IllegalArgumentFault
     * @throws IllegalAccessFault
     */
    public void setFault(final URI taskIdURI, final TFault tFault)
            throws IllegalStateFault, IllegalOperationFault, IllegalArgumentFault,
            IllegalAccessFault {
        try {
            final Long taskId = validateTaskId(taskIdURI);
            HumanTaskServiceComponent.getHumanTaskServer().getTaskEngine().getScheduler().
                    execTransaction(new Callable<Object>() {
                        public Object call() throws Exception {
                            String faultName = null;
                            Element faultData = null;
                            if (tFault != null) {
                                faultName = tFault.getFaultName().toString();
                                faultData = DOMUtils.getElementFromObject(tFault.getFaultData());
                            }
                            SetFault setFault = new SetFault(getCaller(), taskId, faultName, faultData);
                            setFault.execute();
                            return null;
                        }
                    });
        } catch (Exception ex) {
            handleException(ex);
        }
    }


    public void suspendUntil(URI taskId, TTime suspendUntilTime)
            throws IllegalStateFault, IllegalOperationFault, IllegalArgumentFault,
            IllegalAccessFault {
        validateTaskId(taskId);
        handleUnsupportedOperation();
    }


    public void setTaskStartDurationExpression(URI taskId, NCName ncName, String s)
            throws IllegalStateFault, IllegalOperationFault, IllegalArgumentFault,
            IllegalAccessFault {
        validateTaskId(taskId);
        handleUnsupportedOperation();
    }

    /**
     * Returns the presentation description in the specified mime type.
     * @param taskIdURI : task identifier
     * @param contentTypeStr : content Type Optional, Default "text/plain"
     * @return Task Description (String)
     * @throws IllegalArgumentFault
     */
    public String getTaskDescription(final URI taskIdURI, final String contentTypeStr) throws IllegalArgumentFault {
        try {
            final Long taskId = validateTaskId(taskIdURI);
            return HumanTaskServiceComponent.getHumanTaskServer().getTaskEngine().getScheduler().
                    execTransaction(new Callable<String>() {
                        public String call() throws Exception {
                            String contentType;
                            if (StringUtils.isNotEmpty(contentTypeStr)) {
                                contentType = contentTypeStr;
                            } else {
                                contentType = "text/plain";
                            }

                            GetTaskDescription taskDescriptionCommand =
                                    new GetTaskDescription(getCaller(), taskId, contentType);
                            taskDescriptionCommand.execute();
                            return taskDescriptionCommand.getTaskDescription();
                        }
                    });
        } catch (HumanTaskIllegalArgumentException ex) {
            log.error(ex);
            throw new IllegalArgumentFault(ex);
        } catch (Exception ex) {
            log.error(ex);
            throw new IllegalArgumentFault(ex);
        }
    }


    public void deleteAttachment(URI taskId, URI attachmentId)
            throws IllegalStateFault, IllegalOperationFault, IllegalArgumentFault,
            IllegalAccessFault {
        validateTaskId(taskId);
        handleUnsupportedOperation();
    }

    /**
     * Nominate an organization entity to process the task. (An Administrative Operation)
     * @param taskIdURI : task identifier
     * @param nomineeOrgEntity : TOrganizationalEntity
     * @throws IllegalStateFault
     * @throws IllegalOperationFault
     * @throws IllegalArgumentFault
     * @throws IllegalAccessFault
     */
    public void nominate(final URI taskIdURI, final TOrganizationalEntity nomineeOrgEntity)
            throws IllegalStateFault, IllegalOperationFault, IllegalArgumentFault,
            IllegalAccessFault {
        try {
            final Long taskId = validateTaskId(taskIdURI);
            HumanTaskServiceComponent.getHumanTaskServer().getTaskEngine().getScheduler().
                    execTransaction(new Callable<Object>() {
                        public Object call() throws Exception {
                            List<OrganizationalEntityDAO> nominees = TransformerUtils.
                                    transformOrganizationalEntityList(nomineeOrgEntity);
                            Nominate nominateCommand = new Nominate(getCaller(), taskId, nominees);
                            nominateCommand.execute();
                            return null;
                        }
                    });
        } catch (Exception ex) {
            handleException(ex);
        }
    }

    /**
     * Deletes the output data of the task.
     * @param taskIdURI : task identifier
     * @throws IllegalStateFault
     * @throws IllegalOperationFault
     * @throws IllegalArgumentFault
     * @throws IllegalAccessFault
     */
    public void deleteOutput(final URI taskIdURI)
            throws IllegalStateFault, IllegalOperationFault, IllegalArgumentFault,
            IllegalAccessFault {
        try {
            final Long taskId = validateTaskId(taskIdURI);
            HumanTaskServiceComponent.getHumanTaskServer().getTaskEngine().getScheduler().
                    execTransaction(new Callable<Object>() {
                        public Object call() throws Exception {
                            DeleteOutput deleteOutput = new DeleteOutput(getCaller(), taskId);
                            deleteOutput.execute();
                            return null;
                        }
                    });
        } catch (Exception ex) {
            handleException(ex);
        }
    }


    public TBatchResponse[] batchForward(URI[] taskIds, TOrganizationalEntity tOrganizationalEntity) {
        handleUnsupportedOperation();
        return new TBatchResponse[0];
    }


    public TBatchResponse[] batchSuspend(URI[] taskIds) {
        handleUnsupportedOperation();
        return new TBatchResponse[0];
    }


    public TTaskDetails[] getSubtasks(URI taskId)
            throws IllegalStateFault, IllegalOperationFault, IllegalArgumentFault,
            IllegalAccessFault {

        validateTaskId(taskId);
        handleUnsupportedOperation();
        return new TTaskDetails[0];
    }

    /**
     * Deletes the fault name and fault data of the task.
     * @param taskIdURI : task identifier
     * @throws IllegalStateFault
     * @throws IllegalOperationFault
     * @throws IllegalArgumentFault
     * @throws IllegalAccessFault
     */
    public void deleteFault(final URI taskIdURI)
            throws IllegalStateFault, IllegalOperationFault, IllegalArgumentFault,
            IllegalAccessFault {
        try {
            final Long taskId = validateTaskId(taskIdURI);
            HumanTaskServiceComponent.getHumanTaskServer().getTaskEngine().getScheduler().
                    execTransaction(new Callable<Object>() {
                        public Object call() throws Exception {
                            DeleteFault deleteFaultCommand = new DeleteFault(getCaller(), taskId);
                            deleteFaultCommand.execute();
                            return null;
                        }
                    });
        } catch (Exception ex) {
            handleException(ex);
        }
    }

    /**
     * Get the data for the part of the task's output message.
     * @param taskIdURI : task identifier
     * @param partNCName : part name (String)
     * @return Task output (string)
     * @throws IllegalStateFault
     * @throws IllegalOperationFault
     * @throws IllegalArgumentFault
     * @throws IllegalAccessFault
     */
    public Object getOutput(final URI taskIdURI, final NCName partNCName)
            throws IllegalStateFault, IllegalOperationFault, IllegalArgumentFault,
            IllegalAccessFault {
        try {
            final Long taskId = validateTaskId(taskIdURI);
            return HumanTaskServiceComponent.getHumanTaskServer().getTaskEngine().getScheduler().
                    execTransaction(new Callable<Object>() {
                        public Object call() throws Exception {
                            String partName = "";
                            if (partNCName != null) {
                                partName = partNCName.toString().trim();
                            }
                            GetOutput getOutputCommand = new GetOutput(getCaller(), taskId, partName);
                            getOutputCommand.execute();
                            if (getOutputCommand.getOutputData() != null) {
                                Node outputElement = getOutputCommand.getOutputData().getFirstChild();
                                try {
                                    return DOMUtils.domToString(outputElement);
                                } catch (Exception e) {
                                    log.error("Error occurred when converting the output to OMElement",
                                            e);
                                    throw new IllegalStateFault("Error occurred when converting the " +
                                            "output to OMElement", e);
                                }
                            }else {
                                return "";
                            }
                        }
                    });
        } catch (Exception ex) {
            handleException(ex);
        }
        return null;
    }

    /**
     * Release the task, i.e. set the task back to status Ready.
     * @param taskId : task identifier
     * @throws IllegalStateFault
     * @throws IllegalOperationFault
     * @throws IllegalArgumentFault
     * @throws IllegalAccessFault
     */
    public void release(final URI taskId)
            throws IllegalStateFault, IllegalOperationFault, IllegalArgumentFault,
            IllegalAccessFault {
        try {
            validateTaskId(taskId);
            HumanTaskServiceComponent.getHumanTaskServer().getTaskEngine().getScheduler().
                    execTransaction(new Callable<Object>() {
                        public Object call() throws Exception {
                            HumanTaskCommand releaseCommand =
                                    new Release(getCaller(), new Long(taskId.toString()));
                            releaseCommand.execute();
                            return null;
                        }
                    });
        } catch (Exception ex) {
            handleException(ex);
        }
    }

    /**
     * Get the fault data of the task.
     * @param taskIdURI : task identifier
     * @return contains the fault name and fault data
     * @throws IllegalStateFault
     * @throws IllegalOperationFault
     * @throws IllegalArgumentFault
     * @throws IllegalAccessFault
     */
    public TFault getFault(URI taskIdURI)
            throws IllegalStateFault, IllegalOperationFault, IllegalArgumentFault,
            IllegalAccessFault {
        try {
            final Long taskId = validateTaskId(taskIdURI);
            return HumanTaskServiceComponent.getHumanTaskServer().getTaskEngine().getScheduler().
                    execTransaction(new Callable<TFault>() {
                        public TFault call() throws Exception {
                            GetFault getFault = new GetFault(getCaller(), taskId);
                            getFault.execute();
                            TFault fault = new TFault();
                            if (getFault.getFaultData() != null &&
                                    StringUtils.isNotEmpty(getFault.getFaultName())) {
                                fault.setFaultName(new NCName(getFault.getFaultName()));
                                fault.setFaultData(DOMUtils.domToString(getFault.getFaultData()));
                            }
                            return fault;
                        }
                    });
        } catch (Exception ex) {
            handleException(ex);
        }

        return null;
    }

    /**
     * Change the priority of the task.
     * @param taskIdURI : task identifier
     * @param tPriority : The WS-HumanTask Client MUST specify the integer value of the new priority.
     * @throws IllegalStateFault
     * @throws IllegalOperationFault
     * @throws IllegalArgumentFault
     * @throws IllegalAccessFault
     */
    public void setPriority(final URI taskIdURI, final TPriority tPriority)
            throws IllegalStateFault, IllegalOperationFault, IllegalArgumentFault,
            IllegalAccessFault {
        if (tPriority.getTPriority().intValue() < 1 || tPriority.getTPriority().intValue() > 10) {
            log.warn("The priority value should be between 1 and 10. " +
                     "Hence ignoring the provided priority :" + tPriority.getTPriority());
        }

        try {
            final Long taskId = validateTaskId(taskIdURI);
            HumanTaskServiceComponent.getHumanTaskServer().getTaskEngine().getScheduler().
                    execTransaction(new Callable<Object>() {
                        public Object call() throws Exception {
                            int newPriority = tPriority.getTPriority().intValue();
                            SetPriority setPriorityCommand =
                                    new SetPriority(getCaller(), taskId, newPriority);
                            setPriorityCommand.execute();
                            return null;
                        }
                    });
        } catch (Exception ex) {
            handleException(ex);
        }
    }


    public TBatchResponse[] batchSuspendUntil(URI[] taskIds, TTime suspendUntilTime) {
        handleUnsupportedOperation();
        return new TBatchResponse[0];
    }


    public URI getParentTaskIdentifier(URI taskId)
            throws IllegalStateFault, IllegalOperationFault, IllegalArgumentFault,
            IllegalAccessFault {
        handleUnsupportedOperation();
        return null;

    }

    // Validates the provided task ID URI and returns it in case of a valid ID.
    private Long validateTaskId(URI taskId) {
        if (taskId == null || StringUtils.isEmpty(taskId.toString())) {
            throw new IllegalArgumentException("The task id cannot be null or empty");
        }

        try {
            return Long.valueOf(taskId.toString());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("The task id must be a number", e);
        }
    }

    private String getCaller() {
        // TODO - remove hard coded user name value once moved to task view page.
        String userName = "admin";

        PeopleQueryEvaluator pqe = HumanTaskServiceComponent.getHumanTaskServer().
                getTaskEngine().getPeopleQueryEvaluator();

        if (StringUtils.isNotEmpty(pqe.getLoggedInUser())) {
            userName = pqe.getLoggedInUser();
        }

        // We cannot perform any task operation without resolving the user name of the currently
        // logged in user.
        if (StringUtils.isEmpty(userName)) {
            throw new HumanTaskRuntimeException("Cannot determine the user name of the user " +
                    "performing the task operation!");
        }

        return userName;
    }

    private void validateTaskTenant(TaskDAO task) {
        final int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        if (tenantId != task.getTenantId()) {
            log.error(getCaller() + " can't perform other tenant's task");
            throw new HumanTaskIllegalAccessException("Access Denied. You are not authorized to perform this task");
        }
    }


    private TUser[] getUserListForRole(String roleName, int tenantId, String actualOwnerUserName)
            throws RegistryException, UserStoreException {
        TUser[] userList = new TUser[0];

        RegistryService registryService = HumanTaskServiceComponent.getRegistryService();
        if(registryService != null && registryService.getUserRealm(tenantId) != null) {
            UserRealm userRealm = registryService.getUserRealm(tenantId);
            String[] assignableUserNameList = userRealm.getUserStoreManager().getUserListOfRole(roleName);
            if(assignableUserNameList != null) {
                userList = new TUser[assignableUserNameList.length];
                for(int i= 0 ; i < assignableUserNameList.length ; i++) {
                    TUser user  = new TUser();
                    user.setTUser(assignableUserNameList[i]);
                    if(StringUtils.isEmpty(actualOwnerUserName)) {
                        userList[i] = user;
                    } else if(StringUtils.isNotEmpty(actualOwnerUserName) &&
                              !actualOwnerUserName.equals(assignableUserNameList[i])) {
                        userList[i] = user;
                    }
                }
            }
        } else {
            log.warn("Cannot load User Realm for Tenant Id: " + tenantId);
        }
        return userList;
    }

    private Set getUserListForRole(String roleName, int tenantId)
            throws RegistryException, UserStoreException {
        Set<String> userList = new LinkedHashSet<String>();
        RegistryService registryService = HumanTaskServiceComponent.getRegistryService();
        if (registryService != null && registryService.getUserRealm(tenantId) != null) {
            UserRealm userRealm = registryService.getUserRealm(tenantId);
            String[] assignableUserNameList = userRealm.getUserStoreManager().getUserListOfRole(roleName);
            if (assignableUserNameList != null) {
                for (String username : assignableUserNameList) {
                    userList.add(username);
                }
            }
        } else {
            log.warn("Cannot load User Realm for Tenant Id: " + tenantId);
        }
        return userList;
    }


    private void handleException(Exception ex) throws IllegalStateFault, IllegalOperationFault, IllegalArgumentFault,
            IllegalAccessFault {
        log.error(ex);

        if(ex instanceof HumanTaskIllegalAccessException) {
            throw new IllegalAccessFault(ex.getMessage());
        } else if(ex instanceof HumanTaskIllegalArgumentException) {
            throw new  IllegalArgumentFault(ex.getMessage());
        } else if (ex instanceof HumanTaskIllegalOperationException) {
            throw new IllegalOperationFault(ex.getMessage());
        } else if (ex instanceof HumanTaskIllegalStateException) {
            throw new  IllegalStateFault(ex.getMessage());
        }  else {
            throw new IllegalStateFault(ex.getMessage());
        }
    }
}
