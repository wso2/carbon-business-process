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
package org.wso2.carbon.bpmn.people.substitution;

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

public class UserSubstitutionUtils {

    private static final Log log = LogFactory.getLog(UserSubstitutionUtils.class);

    public static final String LIST_SEPARATOR = ",";
    public static final String TRUE = "true";


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
            Date endDate, String taskListString, int tenantId) throws SubstitutionException {
        ActivitiDAO dao = SubstitutionDataHolder.getInstance().getActivitiDAO();
        //at any given time there could be only one substitute for a single user
        if (dao.selectSubstituteInfo(assignee, tenantId) != null) {
            log.error("Substitute for user: " + assignee + ", already exist. Try to update the substitute info");
            throw new SubstitutionException(
                    "Substitute for user: " + assignee + ", already exist. Try to update the substitute info");
        } else {
            SubstitutesDataModel dataModel = new SubstitutesDataModel();
            dataModel.setUser(assignee);
            dataModel.setSubstitute(MultitenantUtils.getTenantAwareUsername(substitute));
            dataModel.setSubstitutionStart(startDate);
            if (endDate == null) {
                endDate = new Date(Long.MAX_VALUE);
            }
            dataModel.setSubstitutionEnd(endDate);
            dataModel.setEnabled(true); //by default enabled
            dataModel.setCreated(new Date());
            dataModel.setTenantId(tenantId);
            dataModel.setTaskList(taskListString);
            dao.insertSubstitute(dataModel);
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
            boolean enabled, List<String> taskList, int tenantId) throws SubstitutionException {
        String taskListStr = getTaskListString(taskList);
        SubstitutesDataModel dataModel = addSubstituteInfo(assignee, substitute, startTime, endTime, taskListStr, tenantId);
        ActivitiDAO dao = SubstitutionDataHolder.getInstance().getActivitiDAO();

        if (dataModel.isEnabled() && isBeforeActivationInterval(dataModel.getSubstitutionStart())) {
            boolean transitivityResolved = updateTransitiveSubstitutes(dataModel, tenantId);
            if (!transitivityResolved) {
                //remove added transitive record
                dao.removeSubstitute(assignee, tenantId);
                throw new SubstitutionException( //SubstitutionException
                        "Could not find an available substitute. Use a different user to substitute");
            }
            if (SubstitutionDataHolder.getInstance().isTransitivityEnabled()) {
                //transitive substitute maybe changed, need to retrieve again.
                dataModel = dao.selectSubstituteInfo(dataModel.getUser(), dataModel.getTenantId());
            }

            if (!SubstitutionDataHolder.getInstance().isTransitivityEnabled() || BPMNConstants.TRANSITIVE_SUB_NOT_APPLICABLE
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

        if (taskList != null && !taskList.isEmpty()) {
            StringBuffer list = new StringBuffer();
            for (String id : taskList) {
                list.append(id).append(LIST_SEPARATOR);
            }
            return list.toString();
        } else {
            return null;
        }
    }

    /**
     * Update all the transitive substitute fields if required
     * @param dataModel - dataModel of user getting unavailable
     */
    private static boolean updateTransitiveSubstitutes(SubstitutesDataModel dataModel, int tenantId) {

        if (SubstitutionDataHolder.getInstance().getTransitivityResolver().isResolvingRequired(dataModel.getUser(), tenantId)) {
            return SubstitutionDataHolder.getInstance().getTransitivityResolver().resolveTransitiveSubs(false, tenantId);
        } else {//need to update transitive sub for this user
            return SubstitutionDataHolder.getInstance().getTransitivityResolver().resolveSubstituteForSingleUser(dataModel, tenantId);
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
            TaskQuery taskQuery = BPMNServerHolder.getInstance().getEngine().getTaskService().createTaskQuery();
            taskQuery.taskAssignee(assignee);
            reassignFromTasksList(taskQuery.list(), substitute);
            transformUnclaimedTasks(assignee, substitute);
        }
        //should mark bulk reassign done
    }

    /**
     * Look for all the tasks the assignee is a candidate and add substitute as a candidate user.
     * @param assignee
     * @param substitute
     */
    private static void transformUnclaimedTasks(String assignee, String substitute) {
        TaskQuery taskQuery = BPMNServerHolder.getInstance().getEngine().getTaskService().createTaskQuery();
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
            TaskQuery taskQuery = BPMNServerHolder.getInstance().getEngine().getTaskService().createTaskQuery();
            for (String taskId : taskList) {
                taskQuery.taskId(taskId);
                taskQuery.taskAssignee(assignee);
                List<Task> tasks = taskQuery.list();//this should return a task if valid
                if (tasks == null || tasks.isEmpty()) {
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
                    BPMNServerHolder.getInstance().getEngine().getTaskService().setAssignee(taskId, substitute);
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
                    BPMNServerHolder.getInstance().getEngine().getTaskService().setAssignee(task.getId(), substitute);
                }
            }
        };

        executeInThreadPool(reassignThread);
    }

    private static void addAsCandidate(final List<Task> taskList, final String substitute) {
        if (taskList == null || taskList.isEmpty()) {
            return;
        }
        Thread reassignThread = new Thread() {

            public void run() {
                for (Task task : taskList) {
                    BPMNServerHolder.getInstance().getEngine().getTaskService().addCandidateUser(task.getId(), substitute);
                }
            }
        };
        executeInThreadPool(reassignThread);
    }

    public static void handleUpdateSubstitute(String assignee, String substitute, Date startTime, Date endTime,
            boolean enabled, List<String> taskList, int tenantId) {
        SubstitutesDataModel existingSubInfo = SubstitutionDataHolder.getInstance().getActivitiDAO().selectSubstituteInfo(assignee, tenantId);


        if (existingSubInfo != null) {

            //need to put existing values for null columns, if not existing data may replace by Null
            if (startTime == null) {
                startTime = existingSubInfo.getSubstitutionStart();
            }

            if (endTime == null) {
                endTime = existingSubInfo.getSubstitutionEnd();
            }

            String taskListString = getTaskListString(taskList);

            if (taskList == null) {
                taskListString = existingSubInfo.getTaskList();
            }

            SubstitutesDataModel dataModel = updateSubstituteInfo(assignee, substitute, startTime, endTime,
                    taskListString, tenantId);

            if (dataModel.isEnabled() && isBeforeActivationInterval(dataModel.getSubstitutionStart())) {
                boolean transitivityResolved = updateTransitiveSubstitutes(dataModel, tenantId);
                if (!transitivityResolved) {
                    //remove added transitive record
                    SubstitutionDataHolder.getInstance().getActivitiDAO().updateSubstituteInfo(existingSubInfo);
                    throw new SubstitutionException(
                            "Could not find an available substitute. Use a different user to substitute");
                }
                if (SubstitutionDataHolder.getInstance().isTransitivityEnabled()) {
                    //transitive substitute maybe changed, need to retrieve again.
                    dataModel = SubstitutionDataHolder.getInstance().getActivitiDAO().selectSubstituteInfo(dataModel.getUser(), dataModel.getTenantId());
                }

                if (!SubstitutionDataHolder.getInstance().isTransitivityEnabled() || BPMNConstants.TRANSITIVE_SUB_NOT_APPLICABLE
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
            Date endTime, String taskListString, int tenantId) {
        SubstitutesDataModel dataModel = new SubstitutesDataModel();
        dataModel.setUser(assignee);
        dataModel.setSubstitute(substitute);
        dataModel.setSubstitutionStart(startTime);
        dataModel.setSubstitutionEnd(endTime);
        dataModel.setEnabled(true); //by default enabled
        dataModel.setTenantId(tenantId);
        dataModel.setUpdated(new Date());
        dataModel.setTaskList(taskListString);
        SubstitutionDataHolder.getInstance().getActivitiDAO().updateSubstituteInfo(dataModel);
        return dataModel;
    }

    public static void handleChangeSubstitute(String assignee, String substitute, int tenantId) {
        ActivitiDAO activitiDAO = SubstitutionDataHolder.getInstance().getActivitiDAO();
        SubstitutesDataModel existingSubInfo = activitiDAO.selectSubstituteInfo(assignee, tenantId);
        if (existingSubInfo != null) {
            activitiDAO.updateSubstitute(assignee, substitute, tenantId, new Date());

            if (existingSubInfo.isEnabled() && isBeforeActivationInterval(existingSubInfo.getSubstitutionStart())) {
                String existingSub = existingSubInfo.getSubstitute();
                existingSubInfo.setSubstitute(substitute);
                boolean transitivityResolved = updateTransitiveSubstitutes(existingSubInfo, tenantId);
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
    public static SubstitutesDataModel getSubstituteOfUser(String assignee, int tenantId) {
        SubstitutesDataModel dataModel = SubstitutionDataHolder.getInstance().getActivitiDAO().selectSubstituteInfo(assignee, tenantId);
        return dataModel;
    }

    /**
     * Query substitution records by given properties.
     * Allowed properties: user, substitute, enabled.
     * Pagination parameters : start, size, sort, order
     * @param propertiesMap
     * @return Paginated list of PaginatedSubstitutesDataModel
     */
    public static List<PaginatedSubstitutesDataModel> querySubstitutions(Map<String, String> propertiesMap, int tenantId) {
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
        int start = Integer.parseInt(propertiesMap.get(SubstitutionQueryProperties.START));
        int size = Integer.parseInt(propertiesMap.get(SubstitutionQueryProperties.SIZE));
        model.setStart(start);
        model.setSize(size);
        model.setOrder(propertiesMap.get(SubstitutionQueryProperties.ORDER));
        model.setSort(propertiesMap.get(SubstitutionQueryProperties.SORT));

        if (!enabledProvided) {
            return SubstitutionDataHolder.getInstance().getActivitiDAO().querySubstituteInfoWithoutEnabled(model);
        } else {
            return SubstitutionDataHolder.getInstance().getActivitiDAO().querySubstituteInfo(model);
        }
    }

    /**
     * Return the maximum activation interval for a substitution.
     * @return activation interval in milliseconds
     */
    public static long getActivationInterval() {
        long activationInterval = BPMNConstants.DEFAULT_SUBSTITUTION_INTERVAL_IN_MINUTES * 60 * 1000;
        BPMNActivitiConfiguration bpmnActivitiConfiguration = BPMNActivitiConfiguration.getInstance();

        if (bpmnActivitiConfiguration != null) {
            String activationIntervalString = bpmnActivitiConfiguration
                    .getBPMNPropertyValue(BPMNConstants.SUBSTITUTION_CONFIG,
                            BPMNConstants.SUBSTITUTION_SCHEDULER_INTERVAL);
            if (activationIntervalString != null) {
                activationInterval = Long.parseLong(activationIntervalString) * 60 * 1000;
                if (log.isDebugEnabled()) {
                    log.debug("Using the substitution activation interval : " + activationIntervalString + " minutes");
                }
            }
        }
        return activationInterval;

    }

    public synchronized static boolean handleScheduledEventByTenant(int tenantId) {
        boolean result = true;
        ActivitiDAO activitiDAO = SubstitutionDataHolder.getInstance().getActivitiDAO();
        TransitivityResolver resolver = SubstitutionDataHolder.getInstance().getTransitivityResolver();
        if (SubstitutionDataHolder.getInstance().isTransitivityEnabled()) {
            result = resolver.resolveTransitiveSubs(true, tenantId); //update transitives, only the map is updated here
        } else {
            resolver.subsMap = activitiDAO.selectActiveSubstitutesByTenant(tenantId);
        }

        //bulk reassign
        //flush into db
        for (Map.Entry<String, SubstitutesDataModel> entry : resolver.subsMap.entrySet()) { //go through the updated map
            SubstitutesDataModel model = entry.getValue();

            try {
                //set carbon context
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext context = PrivilegedCarbonContext.getThreadLocalCarbonContext();
                context.setUsername(model.getUser());
                context.setTenantId(tenantId, true);

                if (SubstitutionDataHolder.getInstance().isTransitivityEnabled()) {
                    activitiDAO.updateSubstituteInfo(model);
                }

                if (!BPMNConstants.BULK_REASSIGN_PROCESSED.equals(model.getTaskList())) { //active substitution, not yet bulk reassigned

                    String sub = getActualSubstitute(model);
                    if (model.getTaskList() == null) {//reassign all
                        if (sub != null) {
                            bulkReassign(model.getUser(), sub, null);
                        } else {//transitivity undefined, assign to task owner or un-claim
                            assignToTaskOwner(model.getUser(), null);
                        }
                    } else {
                        List<String> taskList = getTaskListFromString(model.getTaskList());
                        if (sub != null) {
                            bulkReassign(model.getUser(), sub, taskList);
                        } else {//transitivity undefined, assign to task owner or un-claim
                            assignToTaskOwner(model.getUser(), taskList);
                        }
                    }
                    model.setTaskList(BPMNConstants.BULK_REASSIGN_PROCESSED);
                }

            } finally {
                PrivilegedCarbonContext.endTenantFlow();
                PrivilegedCarbonContext.destroyCurrentContext();
            }


        }

        //disable expired records
        disableExpiredRecords(tenantId);

        return result;

    }

    /**
     * Disable the records that are still enabled but expired
     * @param tenantId
     */
    public static void disableExpiredRecords(int tenantId) {
        ActivitiDAO activitiDAO = SubstitutionDataHolder.getInstance().getActivitiDAO();
        Map<String, SubstitutesDataModel> map = activitiDAO.getEnabledExpiredRecords(tenantId);
        for (Map.Entry<String, SubstitutesDataModel> entry : map.entrySet()) {
            activitiDAO.enableSubstitution(false, entry.getKey(), tenantId);
        }

    }

    /**
     * Handle the transitivity resolving, disabling expired records and task reassignments for all substitutions.
     * @return true if successfully completed
     */
    public static boolean handleScheduledEvent() {

        //should do this for each tenant that has substitutions
        List<Integer> tenantList = getTenantsList();
        if (tenantList != null && !tenantList.isEmpty()) {
            for (int tenantId : tenantList) {
                if (!handleScheduledEventByTenant(tenantId)) {
                    return false;
                }

            }
        }

        return true;
    }

    /**
     * Get the list of tenants that has substitutions
     * @return List<Integer> tenantID list
     */
    public static List<Integer> getTenantsList() {
        return SubstitutionDataHolder.getInstance().getActivitiDAO().getTenantsList();
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
        if (!SubstitutionDataHolder.getInstance().isTransitivityEnabled() || BPMNConstants.TRANSITIVE_SUB_NOT_APPLICABLE
                .equals(model.getTransitiveSub())) {
            return model.getSubstitute();
        } else if (BPMNConstants.TRANSITIVE_SUB_UNDEFINED.equals(model.getTransitiveSub())){
            return null;
        } else {
            return model.getTransitiveSub();
        }
    }

    /**
     * Check if an active substitution available for given substitute info
     * @param substitutesDataModel
     * @return true if substitution active
     */
    private static boolean isSubstitutionActive(SubstitutesDataModel substitutesDataModel) {
        long startDate = substitutesDataModel.getSubstitutionStart().getTime();
        long endDate = substitutesDataModel.getSubstitutionEnd().getTime();
        long currentTime = System.currentTimeMillis();

        if ((startDate < currentTime) && (endDate > currentTime) && substitutesDataModel.isEnabled() ) {
            return true;
        }
        return false;
    }

    /**
     * Disable the the substitution record of the given assignee
     * @param disable - true to disable
     * @param assignee - user of the substitution
     * @param tenantId - assignee's tenant id
     */
    public static void disableSubstitution(boolean disable, String assignee, int tenantId) {
        ActivitiDAO activitiDAO = SubstitutionDataHolder.getInstance().getActivitiDAO();
        if (activitiDAO.selectSubstituteInfo(assignee, tenantId) != null) {
            activitiDAO.enableSubstitution(!disable, assignee, tenantId);
        } else {
            throw new ActivitiIllegalArgumentException("No substitution record exist for the given user : " + assignee);
        }

    }
}
