/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.bpmn.extensions.substitution;

import org.activiti.engine.*;
import org.activiti.engine.impl.TaskQueryImpl;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;
import org.joda.time.DateTime;
import org.wso2.carbon.bpmn.core.BPMNServerHolder;
import org.wso2.carbon.bpmn.core.mgt.dao.ActivitiDAO;
import org.wso2.carbon.bpmn.core.mgt.model.SubstitutesDataModel;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.util.Date;
import java.util.List;

public class UserSubstitutionOperations {

    private static ActivitiDAO activitiDAO = new ActivitiDAO();
    private static TaskService taskService = BPMNServerHolder.getInstance().getEngine().getTaskService();
    private static int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();

    /**
     * Persist the substitute info. Transitive substitute is not added here.
     * @param assignee - User becomes unavailable
     * @param substitute - substitute for the assignee
     * @param startDate - start od the substitution
     * @param endDate - end of the substitution
     * @return added row count.
     * @throws SubstitutesException
     */
    public static SubstitutesDataModel addSubstituteInfo(String assignee, String substitute, Date startDate,
            Date endDate) throws SubstitutesException {

        if (activitiDAO.selectSubstituteInfo(assignee, tenantId) != null) {
            throw new SubstitutesException(
                    "Substitute for user: " + assignee + ", already exist. Try to update the substitute info");
        } else {
            SubstitutesDataModel dataModel = new SubstitutesDataModel();
            dataModel.setUser(assignee);
            dataModel.setSubstitute(MultitenantUtils.getTenantAwareUsername(substitute));
            dataModel.setSubstitutionStart(startDate);
            dataModel.setSubstitutionEnd(endDate);
            dataModel.setEnabled(true); //by default enabled
            dataModel.setCreated(new Date());
            dataModel.setTenantId(tenantId);
            //            String transitiveSub = getTransitiveSubstitute(substitute);
            //            if (transitiveSub != substitute) { //if the current substitute available, trans sub remains null
            //                dataModel.setTransitiveSub(transitiveSub);
            //            }

            activitiDAO.insertSubstitute(dataModel);
            return dataModel;
        }
    }

    /**
     * Handles addition of new substitute record and it's post conditions.
     * @param assignee
     * @param substitute
     * @param startTime
     * @param endTime
     * @param enabled
     * @param taskList
     * @throws SubstitutesException
     */
    public static void handleNewSubstituteAddition(String assignee, String substitute, Date startTime, Date endTime,
            boolean enabled, List<String> taskList) throws SubstitutesException {
        SubstitutesDataModel dataModel = addSubstituteInfo(assignee, substitute, startTime, endTime);

        if (dataModel.isEnabled() && isBeforeBufferTime(dataModel.getSubstitutionStart())) {
            updateTransitiveSubstitutes(substitute);
            //substitute maybe changed, need to retrieve again.
            dataModel = activitiDAO.selectSubstituteInfo(dataModel.getUser(), dataModel.getTenantId());
            if (dataModel.getTransitiveSub() != null) {
                bulkReassign(dataModel.getUser(), dataModel.getTransitiveSub(), taskList);
            } else {
                bulkReassign(dataModel.getUser(), dataModel.getSubstitute(), taskList);
            }
        }
    }

    //TODO : implement this method
    private static void updateTransitiveSubstitutes(String substitute) {

        if (activitiDAO.countUserAsSubstitute(substitute, tenantId) > 0) {

        } else { //assignee is not involved in any substitutes, we don't need to update.
            return;
        }


    }

    private static boolean isBeforeBufferTime(Date substitutionStart) {
        Date bufferedTime = new Date(SubstitutionConstants.BUFFER_TIME_IN_SECONDS * 1000);
        if (substitutionStart.compareTo(bufferedTime) < 0) {
            return true;
        } else {
            return false;
        }
    }

    public static String addTransitiveSub(String assignee, String substitute) {
        String transitiveSub = getTransitiveSubstitute(substitute);
        if (transitiveSub != substitute) {
            SubstitutesDataModel dataModel = new SubstitutesDataModel();
            dataModel.setUser(assignee);
            dataModel.setTransitiveSub(transitiveSub);
            activitiDAO.updateSubstituteInfo(dataModel);
        }

        return transitiveSub;
    }

    //Reassign all the tasks
    public static void bulkReassign(String assignee, String substitute, List<String> taskList) {

        if (taskList != null) { //reassign the given tasks TODO : should we consider enabled and date here?
            validateTasksList(taskList, assignee);
            reassignFromTaskIdsList(taskList, substitute);
        } else { //reassign all existing tasks for assignee
            TaskQuery taskQuery = new TaskQueryImpl();
            taskQuery.taskAssignee(assignee);
            reassignFromTasksList(taskQuery.list(), substitute);
            transformUnclaimedTasks(assignee, substitute);
        }
    }

    /**
     * Look for all the tasks the assignee is a candidate and add substitute as a candidate user.
     * @param assignee
     * @param substitute
     */
    private static void transformUnclaimedTasks(String assignee, String substitute) {
        TaskQuery taskQuery = new TaskQueryImpl();
        taskQuery.taskCandidateUser(assignee);
        List<Task> candidateTasks = taskQuery.list();
        addAsCandidate(candidateTasks, substitute);
    }

    /**
     * Check whether each given task is a candidate for substitution by assignee
     * @param taskList
     * @param assignee
     */
    private static void validateTasksList(List<String> taskList, String assignee) {
        TaskQuery taskQuery = new TaskQueryImpl();
        for (String taskId : taskList) {
            taskQuery.taskId(taskId);
            taskQuery.taskAssignee(assignee);
            List<Task> tasks = taskQuery.list();//this should return a task if valid
            if (tasks == null) {
                throw new ActivitiIllegalArgumentException("Invalid task Id : " + taskId + ", for substitution.");
            }
        }
    }

    private static void reassignFromTaskIdsList(final List<String> taskList, final String substitute) {
        Thread reassignThread = new Thread() {

            public void run() {
                for (String taskId : taskList) {
                    taskService.setAssignee(taskId, substitute);
                }
            }
        };
        reassignThread.start();
    }

    private static void reassignFromTasksList(final List<Task> taskList, final String substitute) {
        Thread reassignThread = new Thread() {

            public void run() {
                for (Task task : taskList) {
                    taskService.setAssignee(task.getId(), substitute);
                }
            }
        };
        reassignThread.start();
    }

    private static void addAsCandidate(final List<Task> taskList, final String substitute) {
        Thread reassignThread = new Thread() {

            public void run() {
                for (Task task : taskList) {
                    taskService.addCandidateUser(task.getId(), substitute);
                }
            }
        };
        reassignThread.start();
    }

    /**
     * Recursively look for a available substitute. Makes a DB call for each recursive iteration.
     * @param substitute to evaluate
     * @return available addSubstituteInfo name
     */
    protected static String getTransitiveSubstitute(String substitute) {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        SubstitutesDataModel substitutesDataModel = activitiDAO.selectSubstituteInfo(substitute, tenantId);

        if (substitutesDataModel != null && isSubstitutionActive(substitutesDataModel)) {
            getTransitiveSubstitute(substitutesDataModel.getSubstitute());
        }
        return substitute;
    }

    /**
     * Check if an active substitution available
     * @param substitutesDataModel
     * @return true if substitution active
     */
    protected static boolean isSubstitutionActive(SubstitutesDataModel substitutesDataModel) {
        long startDate = substitutesDataModel.getSubstitutionStart().getTime();
        long endDate = substitutesDataModel.getSubstitutionEnd().getTime();
        long currentTime = System.currentTimeMillis();

        if (substitutesDataModel.isEnabled() && (startDate < currentTime) && (endDate > currentTime)) {
            return true;
        }
        return false;
    }
}
