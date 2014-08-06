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
package org.wso2.carbon.humantask.core.protocol.mgt.services;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.databinding.types.URI;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.humantask.core.dao.TaskDAO;
import org.wso2.carbon.humantask.core.dao.TaskStatus;
import org.wso2.carbon.humantask.core.engine.PeopleQueryEvaluator;
import org.wso2.carbon.humantask.core.engine.commands.Exit;
import org.wso2.carbon.humantask.core.engine.runtime.api.*;
import org.wso2.carbon.humantask.core.internal.HumanTaskServiceComponent;
import org.wso2.carbon.humantask.skeleton.protocol.mgt.services.HumanTaskProtocolHandlerSkeletonInterface;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

public class HumanTaskCoordinationProtocolManagementSkeleton extends AbstractAdmin implements HumanTaskProtocolHandlerSkeletonInterface {

    private static Log log = LogFactory.getLog(HumanTaskCoordinationProtocolManagementSkeleton.class);

    /**
     * exit given Task id(s)
     *
     * @param omElements is a list of task ids with localname "taskID"
     */
    @Override
    public void exitOperation(OMElement[] omElements) {

        if (omElements.length < 0) {
            log.error("Invalid humantask exit protocol message. No task found.");
            return;
        }

        for (OMElement omElement : omElements) {
            if ("taskid".equalsIgnoreCase(omElement.getLocalName())) {
                if (omElement.getText() != null) {
                    Long taskId;
                    try {
                        taskId = Long.valueOf(omElement.getText().trim());
                    } catch (NumberFormatException e) {
                        log.warn("The task id must be a number", e);
                        continue;
                    }
                    try {
                        if (isTaskInFinalState(taskId)) {
                            if (log.isDebugEnabled()) {
                                log.debug("Skipping Exit protocol message as Task id " + taskId + " is already in its final state.");
                            }
                            continue;
                        }
                        exitTask(taskId);
                    } catch (Exception ex) {
                        log.error(new HumanTaskRuntimeException("Failed to execute Exit operation for task ID = " + taskId, ex));
                        continue;
                    }
                }
            }
        }

    }

    private boolean isTaskInFinalState(final Long taskID) throws Exception {
        TaskDAO task = HumanTaskServiceComponent.getHumanTaskServer().getTaskEngine().getScheduler().
                execTransaction(new Callable<TaskDAO>() {
                    public TaskDAO call() throws Exception {
                        return HumanTaskServiceComponent.getHumanTaskServer().getTaskEngine().getDaoConnectionFactory().getConnection().getTask(taskID);
                    }
                });
        TaskStatus status = task.getStatus();
        boolean isInFinalState = false;
        if (TaskStatus.EXITED.equals(status) || TaskStatus.ERROR.equals(status)
                || TaskStatus.FAILED.equals(status) || TaskStatus.OBSOLETE.equals(status) ||
                TaskStatus.COMPLETED.equals(status)) {
            isInFinalState = true;
        }
        return isInFinalState;
    }

    private void exitTask(final Long taskId) throws Exception {
        HumanTaskServiceComponent.getHumanTaskServer().getTaskEngine().getScheduler().
                execTransaction(new Callable<Object>() {
                    public Object call() throws Exception {
                        Exit exitCommand = new Exit(getCaller(), taskId);
                        exitCommand.execute();
                        return null;
                    }
                });
        if (log.isDebugEnabled()) {
            log.debug("HumanTask " + taskId + " is exited via HT coordination.");
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
