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
import org.activiti.engine.task.IdentityLink;
import org.activiti.engine.task.IdentityLinkType;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bpmn.core.BPMNConstants;
import org.wso2.carbon.bpmn.core.BPMNServerHolder;
import org.wso2.carbon.bpmn.core.mgt.dao.ActivitiDAO;
import org.wso2.carbon.bpmn.core.mgt.model.PaginatedSubstitutesDataModel;
import org.wso2.carbon.bpmn.core.mgt.model.SubstitutesDataModel;
import org.wso2.carbon.bpmn.core.utils.BPMNActivitiConfiguration;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class UserSubstitutionOperations {

    private static final Log log = LogFactory.getLog(UserSubstitutionOperations.class);
    private static ActivitiDAO activitiDAO = new ActivitiDAO();
    private static TaskService taskService = BPMNServerHolder.getInstance().getEngine().getTaskService();
    private static int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
    private static TransitivityResolver resolver = new TransitivityResolver(activitiDAO, tenantId);
    public static Integer activationInterval = null;
    public static final String LIST_SEPARATOR = ",";

    /**
     * Persist the substitute info. Transitive substitute is not added here.
     * @param assignee - User becomes unavailable
     * @param substitute - substitute for the assignee
     * @param startDate - start od the substitution
     * @param endDate - end of the substitution
     * @param taskListString - Comma separated String of task Ids
     * @return added row count.
     * @throws SubstitutionException
     */
    public static SubstitutesDataModel addSubstituteInfo(String assignee, String substitute, Date startDate,
            Date endDate, String taskListString) throws SubstitutionException {

        //at any given time there could be only one substitute for a single user
        if (activitiDAO.selectSubstituteInfo(assignee, tenantId) != null) {
            log.error("Substitute for user: " + assignee + ", already exist. Try to update the substitute info");
            throw new SubstitutionException(
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
            dataModel.setTaskList(taskListString);
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
     * @throws SubstitutionException
     */
    public static void handleNewSubstituteAddition(String assignee, String substitute, Date startTime, Date endTime,
            boolean enabled, List<String> taskList) throws SubstitutionException {
        String taskListStr = getTaskListString(taskList);
        SubstitutesDataModel dataModel = addSubstituteInfo(assignee, substitute, startTime, endTime, taskListStr);

        if (dataModel.isEnabled() && isBeforeActivationInterval(dataModel.getSubstitutionStart())) {
            boolean transitivityResolved = updateTransitiveSubstitutes(dataModel);
            if (!transitivityResolved) {
                //remove added transitive record
                activitiDAO.removeSubstitute(assignee, tenantId);
                throw new SubstitutionException( //SubstitutionException
                        "Could not find an available substitute. Use a different user to substitute");
            }
            if (resolver.transitivityEnabled) {
                //transitive substitute maybe changed, need to retrieve again.
                dataModel = activitiDAO.selectSubstituteInfo(dataModel.getUser(), dataModel.getTenantId());
            }

            if (!resolver.transitivityEnabled || BPMNConstants.TRANSITIVE_SUB_NOT_APPLICABLE
                    .equals(dataModel.getTransitiveSub())) {
                bulkReassign(dataModel.getUser(), dataModel.getSubstitute(), taskList);
            } else {
                bulkReassign(dataModel.getUser(), dataModel.getTransitiveSub(), taskList);
            }

        }
    }

    /**
     * Return a LIST_SEPARATOR separated String from given list
     * @param taskList
     * @return a LIST_SEPARATOR separated String from given item list
     */
    private static String getTaskListString(List<String> taskList) {

        if (taskList != null & !taskList.isEmpty()) {
            String list = "";
            for (String id : taskList) {
                list = list + id + LIST_SEPARATOR;
            }
            return list;
        } else {
            return null;
        }
    }

    /**
     * Update all the transitive substitute fields if required
     * @param dataModel - dataModel of user getting unavailable
     */
    private static boolean updateTransitiveSubstitutes(SubstitutesDataModel dataModel) {

        if (resolver.isResolvingRequired(dataModel.getUser())) {
            return resolver.resolveTransitiveSubs(false);
        } else {//need to update transitive sub for this user
            return resolver.resolveSubstituteForSingleUser(dataModel);
        }
    }

    private static boolean isBeforeActivationInterval(Date substitutionStart) {
        Date bufferedTime = new Date(System.currentTimeMillis() + getActivationInterval());
        if (substitutionStart.compareTo(bufferedTime) < 0) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Reassign the given tasks or all the tasks if the given task list is null, to the given substitute
     * @param assignee - original user of the tasks
     * @param substitute - user who getting assigned
     * @param taskList - list of tasks to reassign. Leave this null to reassign all tha tasks of the assignee.
     */
    public static void bulkReassign(String assignee, String substitute, List<String> taskList) {

        if (taskList != null) { //reassign the given tasks
            reassignFromTaskIdsList(taskList, substitute);
        } else { //reassign all existing tasks for assignee
            TaskQuery taskQuery = taskService.createTaskQuery();
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
        TaskQuery taskQuery = taskService.createTaskQuery();
        taskQuery.taskCandidateUser(assignee);
        List<Task> candidateTasks = taskQuery.list();
        addAsCandidate(candidateTasks, substitute);
    }

    /**
     * Check whether each given task is a candidate for substitution by assignee
     * @param taskList
     * @param assignee
     */
    public static boolean validateTasksList(List<String> taskList, String assignee) {
        if (taskList != null) {
            TaskQuery taskQuery = taskService.createTaskQuery();
            for (String taskId : taskList) {
                taskQuery.taskId(taskId);
                taskQuery.taskAssignee(assignee);
                List<Task> tasks = taskQuery.list();//this should return a task if valid
                if (tasks == null) {
                    return false;
                }
            }
        }
        return true;
    }

    private static void reassignFromTaskIdsList(final List<String> taskList, final String substitute) {

        Thread reassignThread = new Thread() {

            public void run() {
                for (String taskId : taskList) {
                    taskService.setAssignee(taskId, substitute);
                }
            }
        };

        executeInThreadPool(reassignThread);
    }

    private static void executeInThreadPool(Runnable runnable) {
        ExecutorService pool = Executors.newCachedThreadPool();
        pool.execute(runnable);

    }

    private static void reassignFromTasksList(final List<Task> taskList, final String substitute) {
        Thread reassignThread = new Thread() {

            public void run() {
                for (Task task : taskList) {
                    taskService.setAssignee(task.getId(), substitute);
                }
            }
        };

        executeInThreadPool(reassignThread);
    }

    private static void addAsCandidate(final List<Task> taskList, final String substitute) {
        Thread reassignThread = new Thread() {

            public void run() {
                for (Task task : taskList) {
                    taskService.addCandidateUser(task.getId(), substitute);
                }
            }
        };
        executeInThreadPool(reassignThread);
    }

    public static void handleUpdateSubstitute(String assignee, String substitute, Date startTime, Date endTime,
            boolean enabled, List<String> taskList) {
        SubstitutesDataModel existingSubInfo = activitiDAO.selectSubstituteInfo(assignee, tenantId);
        String taskListString = getTaskListString(taskList);
        if (existingSubInfo != null) {
            SubstitutesDataModel dataModel = updateSubstituteInfo(assignee, substitute, startTime, endTime,
                    taskListString);

            if (dataModel.isEnabled() && isBeforeActivationInterval(dataModel.getSubstitutionStart())) {
                boolean transitivityResolved = updateTransitiveSubstitutes(dataModel);
                if (!transitivityResolved) {
                    //remove added transitive record
                    activitiDAO.updateSubstituteInfo(existingSubInfo);
                    throw new SubstitutionException(
                            "Could not find an available substitute. Use a different user to substitute");
                }
                if (resolver.transitivityEnabled) {
                    //transitive substitute maybe changed, need to retrieve again.
                    dataModel = activitiDAO.selectSubstituteInfo(dataModel.getUser(), dataModel.getTenantId());
                }

                if (!resolver.transitivityEnabled || BPMNConstants.TRANSITIVE_SUB_NOT_APPLICABLE
                        .equals(dataModel.getTransitiveSub())) {
                    bulkReassign(dataModel.getUser(), dataModel.getSubstitute(), taskList);
                } else {
                    bulkReassign(dataModel.getUser(), dataModel.getTransitiveSub(), taskList);
                }

            }
        } else {
            throw new SubstitutionException(
                    "Substitute for user: " + assignee + ", does not exist. Try to add new substitute info record");

        }

    }

    private static SubstitutesDataModel updateSubstituteInfo(String assignee, String substitute, Date startTime,
            Date endTime, String taskListString) {
        SubstitutesDataModel dataModel = new SubstitutesDataModel();
        dataModel.setUser(assignee);
        dataModel.setSubstitute(MultitenantUtils.getTenantAwareUsername(substitute));
        dataModel.setSubstitutionStart(startTime);
        dataModel.setSubstitutionEnd(endTime);
        dataModel.setEnabled(true); //by default enabled
        dataModel.setTenantId(tenantId);
        dataModel.setUpdated(new Date());
        dataModel.setTaskList(taskListString);
        activitiDAO.updateSubstituteInfo(dataModel);
        return dataModel;
    }

    public static void handleChangeSubstitute(String assignee, String substitute) {
        SubstitutesDataModel existingSubInfo = activitiDAO.selectSubstituteInfo(assignee, tenantId);
        if (existingSubInfo != null) {
            activitiDAO.updateSubstitute(assignee, substitute, tenantId, new Date());

            if (existingSubInfo.isEnabled() && isBeforeActivationInterval(existingSubInfo.getSubstitutionStart())) {
                String existingSub = existingSubInfo.getSubstitute();
                existingSubInfo.setSubstitute(substitute);
                boolean transitivityResolved = updateTransitiveSubstitutes(existingSubInfo);
                if (!transitivityResolved) {
                    //remove added record
                    activitiDAO.updateSubstitute(assignee, existingSub, tenantId, existingSubInfo.getUpdated());
                    throw new SubstitutionException(
                            "Could not find an available substitute. Use a different user to substitute");
                }
            }
        } else {
            throw new SubstitutionException("No substitution record found for the user: " + assignee);

        }
    }

    /**
     * Get the substitute info of the given user.
     * @param assignee
     * @return SubstitutesDataModel
     */
    public static SubstitutesDataModel getSubstituteOfUser(String assignee) {
        SubstitutesDataModel dataModel = activitiDAO.selectSubstituteInfo(assignee, tenantId);
        return dataModel;
    }

    /**
     * Query substitution records by given properties.
     * Allowed properties: user, substitute, enabled.
     * Pagination parameters : start, size, sort, order
     * @param propertiesMap
     * @return Paginated list of PaginatedSubstitutesDataModel
     */
    public static List<PaginatedSubstitutesDataModel> querySubstitutions(Map<String, String> propertiesMap) {
        PaginatedSubstitutesDataModel model = new PaginatedSubstitutesDataModel();
        if (propertiesMap.get(SubstitutionQueryProperties.SUBSTITUTE) != null) {
            model.setSubstitute(propertiesMap.get(SubstitutionQueryProperties.SUBSTITUTE));
        }

        if (propertiesMap.get(SubstitutionQueryProperties.USER) != null) {
            model.setUser(propertiesMap.get(SubstitutionQueryProperties.USER));
        }

        String enabled = propertiesMap.get(SubstitutionQueryProperties.ENABLED);
        boolean enabledProvided = false;
        if (enabled != null) {
            enabledProvided = true;
            if (enabled.equalsIgnoreCase("true")) {
                model.setEnabled(true);
            } else if (enabled.equalsIgnoreCase("false")) {
                model.setEnabled(false);
            } else {
                throw new ActivitiIllegalArgumentException("Invalid parameter " + enabled + " for enabled property.");
            }
        }

        model.setTenantId(tenantId);
        int start = Integer.valueOf(propertiesMap.get(SubstitutionQueryProperties.START));
        int size = Integer.valueOf(propertiesMap.get(SubstitutionQueryProperties.SIZE));
        model.setStart(start);
        model.setSize(size);
        model.setOrder(propertiesMap.get(SubstitutionQueryProperties.ORDER));
        model.setSort(propertiesMap.get(SubstitutionQueryProperties.SORT));

        if (!enabledProvided) {
            return activitiDAO.querySubstituteInfoWithoutEnabled(model);
        } else {
            return activitiDAO.querySubstituteInfo(model);
        }
    }

    /**
     * Return the maximum activation interval for a substitution.
     * @return activation interval in milliseconds
     */
    public static int getActivationInterval() {
        if (activationInterval == null) {
            BPMNActivitiConfiguration bpmnActivitiConfiguration = BPMNActivitiConfiguration.getInstance();

            if (bpmnActivitiConfiguration != null) {
                String activationIntervalString = bpmnActivitiConfiguration
                        .getBPMNPropertyValue(BPMNConstants.SUBSTITUTION_CONFIG,
                                BPMNConstants.SUBSTITUTION_SCHEDULER_INTERVAL);

                if (activationInterval != null) {
                    activationInterval = Integer.parseInt(activationIntervalString);
                }
            }

            if (log.isDebugEnabled()) {
                log.debug("Using the substitution activation interval : " + activationInterval + " minutes");
            }
            activationInterval = activationInterval * 60 * 1000;
        }

        if (activationInterval == null) { //if still null, should assign default
            activationInterval = BPMNConstants.DEFAULT_SUBSTITUTION_INTERVAL_IN_MINUTES;
            if (log.isDebugEnabled()) {
                log.debug("Using the default substitution activation interval : " + activationInterval + " minutes");
            }
            activationInterval = activationInterval * 60 * 1000;
        }

        return activationInterval;
    }

    public static void handleSheduledEvent() {
        if (resolver.transitivityEnabled) {
            resolver.resolveTransitiveSubs(true); //update transitives, only the map is updated here
        } else {
            resolver.subsMap = activitiDAO.selectActiveSubstitutesByTenant(tenantId);
        }

        //bulk reassign
        //flush into db
        for (Map.Entry<String, SubstitutesDataModel> entry : resolver.subsMap.entrySet()) { //go through the updated map
            SubstitutesDataModel model = entry.getValue();
            if (!BPMNConstants.BULK_REASSIGN_PROCESSED.equals(model.getTaskList())) { //active substitution, not yet bulk reassigned

                String sub = getActualSubstitute(model);
                if (model.getTaskList() == null) {//reassign all
                    if (sub != null) {
                        bulkReassign(model.getUser(), sub, null);
                    } else {
                        assignToTaskOwner(model.getUser(), null);
                    }
                } else {
                    List<String> taskList = getTaskListFromString(model.getTaskList());
                    if (sub != null) {
                        bulkReassign(model.getUser(), sub, taskList);
                    } else {
                        assignToTaskOwner(model.getUser(), taskList);
                    }
                }
            }
        }

    }

    private static void assignToTaskOwner(String assignee, List<String> taskList) {
        TaskService taskService = BPMNServerHolder.getInstance().getEngine().getTaskService();

        if (taskList != null) {
            for (String taskId : taskList) {
                String taskOwner = null;
                List<IdentityLink> identityLinks = taskService.getIdentityLinksForTask(taskId);
                for (IdentityLink link : identityLinks) {
                    if(IdentityLinkType.OWNER.equals(link.getType())) {
                        taskOwner = link.getUserId();
                    }
                }
                if (taskOwner != null) {//assign to task owner
                    taskService.setAssignee(taskId, taskOwner);
                } else {
                    taskService.addCandidateUser(taskId, assignee);
                    taskService.unclaim(taskId);
                }
            }
        } else {//reassign all tasks
            TaskQuery taskQuery = taskService.createTaskQuery();
            taskQuery.taskAssignee(assignee);
            List<Task> list = taskQuery.list();

            for (Task task : list) {
                String taskOwner = task.getOwner();
                if (taskOwner != null) {//assign to task owner
                    taskService.setAssignee(task.getId(), taskOwner);
                } else {
                    taskService.addCandidateUser(task.getId(), assignee);
                    taskService.unclaim(task.getId());
                }
            }
        }
    }

    private static List<String> getTaskListFromString(String taskList) {
        return Arrays.asList(taskList.split("\\s*,\\s*"));
    }

    private static String getActualSubstitute(SubstitutesDataModel model) {
        if (!resolver.transitivityEnabled || BPMNConstants.TRANSITIVE_SUB_NOT_APPLICABLE
                .equals(model.getTransitiveSub())) {
            return model.getSubstitute();
        } else if (BPMNConstants.TRANSITIVE_SUB_UNDEFINED.equals(model.getTransitiveSub())){
            return null;
        } else {
            return model.getTransitiveSub();
        }
    }

}
