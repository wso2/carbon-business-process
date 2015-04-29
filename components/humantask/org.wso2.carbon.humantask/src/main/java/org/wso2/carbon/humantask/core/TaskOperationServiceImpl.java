/**
 *  Copyright (c) 2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.wso2.carbon.humantask.core;

import org.apache.axis2.databinding.types.URI;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Element;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.humantask.client.api.IllegalAccessFault;
import org.wso2.carbon.humantask.client.api.IllegalArgumentFault;
import org.wso2.carbon.humantask.client.api.IllegalOperationFault;
import org.wso2.carbon.humantask.client.api.IllegalStateFault;
import org.wso2.carbon.humantask.client.api.types.TSimpleQueryInput;
import org.wso2.carbon.humantask.client.api.types.TStatus;
import org.wso2.carbon.humantask.client.api.types.TTaskAbstract;
import org.wso2.carbon.humantask.client.api.types.TTaskSimpleQueryResultSet;
import org.wso2.carbon.humantask.core.api.client.TransformerUtils;
import org.wso2.carbon.humantask.core.dao.HumanTaskDAOConnection;
import org.wso2.carbon.humantask.core.dao.SimpleQueryCriteria;
import org.wso2.carbon.humantask.core.dao.TaskDAO;
import org.wso2.carbon.humantask.core.dao.TaskStatus;
import org.wso2.carbon.humantask.core.engine.HumanTaskEngine;
import org.wso2.carbon.humantask.core.engine.PeopleQueryEvaluator;
import org.wso2.carbon.humantask.core.engine.commands.Complete;
import org.wso2.carbon.humantask.core.engine.commands.Start;
import org.wso2.carbon.humantask.core.engine.runtime.api.*;
import org.wso2.carbon.humantask.core.internal.HumanTaskServiceComponent;
import org.wso2.carbon.humantask.core.utils.DOMUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

/**
 * Created by chandana on 4/22/15.
 */
public class TaskOperationServiceImpl implements TaskOperationService {

    private static Log log = LogFactory.getLog(TaskOperationServiceImpl.class);


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

    public void complete(URI taskIdURI, final String outputStr)
            throws IllegalStateFault, IllegalOperationFault, IllegalArgumentFault, IllegalAccessFault {
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
            log.error(ex);
            throw new IllegalAccessFault(ex);
        }
    }

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

    private void validateTaskTenant(TaskDAO task) {
        final int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        if (tenantId != task.getTenantId()) {
            log.error(getCaller() + " can't perform other tenant's task");
            throw new HumanTaskIllegalAccessException("Access Denied. You are not authorized to perform this task");
        }
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

}
