/**
 * Copyright (c) 2015 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.bpmn.rest.service.stats;

import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.HistoryService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricProcessInstanceQuery;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.impl.RepositoryServiceImpl;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.bpmn.rest.common.utils.BPMNOSGIService;
import org.wso2.carbon.bpmn.rest.model.stats.TaskInstanceAverageInfo;
import org.wso2.carbon.bpmn.rest.model.stats.ProcessInstanceAverageInfo;
import org.wso2.carbon.bpmn.rest.model.stats.ProcessInstanceStatInfo;
import org.wso2.carbon.bpmn.rest.model.stats.ProcessInstanceStatusCountInfo;
import org.wso2.carbon.bpmn.rest.model.stats.InstanceCountInfo;
import org.wso2.carbon.bpmn.rest.model.stats.ResponseHolder;
import org.wso2.carbon.bpmn.rest.model.stats.TaskInfo;
import org.wso2.carbon.bpmn.rest.model.stats.UserTaskCountInfo;
import org.wso2.carbon.bpmn.rest.model.stats.UserTaskDuration;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;

import javax.jws.WebService;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * Service class which includes functionality related to processes and tasks
 */

@WebService
@Path("/")
public class ProcessStatisticsService {

    private static final Log log = LogFactory.getLog(ProcessStatisticsService.class);
    private static final String[] MONTHS = {"Jan", "Feb", "March", "April", "May", "June", "July", "Aug", "Sep", "Oct", "Nov", "Dec"};

    /**
     * Get the deployed processes count
     *
     * @return a list of deployed processes with their instance count
     */
    @GET
    @Path("/process-instances/count")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public ResponseHolder getProcessInstanceCount() {

        List<ProcessDefinition> processDefinitionList = BPMNOSGIService.getRepositoryService().
                createProcessDefinitionQuery().processDefinitionTenantId(getTenantIdStr()).list();

        List<Object> bpmnProcessInstancesList = new ArrayList<>();
        ResponseHolder response = new ResponseHolder();
        for (ProcessDefinition processDefinition : processDefinitionList) {
            InstanceCountInfo instanceCountInfo = new InstanceCountInfo();
            instanceCountInfo.setProcessDefinitionId(processDefinition.getId());
            long historicInstanceCount = getCompletedProcessInstanceCount(processDefinition.getId());
            long runningInstanceCount = getActiveProcessInstanceCount(processDefinition.getId());
            long noOfInstances = historicInstanceCount + runningInstanceCount;
            instanceCountInfo.setInstanceCount(noOfInstances);
            bpmnProcessInstancesList.add(instanceCountInfo);
        }
        response.setData(bpmnProcessInstancesList);
        return response;
    }

    /**
     * Get the completed process instance count for a given process definition
     *
     * @param processDefinitionName process definition name
     * @return number of process instances in the system
     */
    private long getCompletedProcessInstanceCount(String processDefinitionName) {

        return BPMNOSGIService.getHistoryService().createHistoricProcessInstanceQuery()
                .processDefinitionId(processDefinitionName).finished().count();
    }

    /**
     * Get the number of active process instances for a given process definition
     * @param processDefinitionName process definnition id
     * @return count of active process instances
     */
    private long getActiveProcessInstanceCount(String processDefinitionName) {
        return BPMNOSGIService.getRuntimeService()
                .createProcessInstanceQuery().processDefinitionId(processDefinitionName).count();
    }

    /**
     * Get the number of  processInstances with various States
     * States: Completed , Active, Suspended, Failed
     *
     * @return list with the states and the count of process instances in each state
     */
    @GET
    @Path("/process-instances/state/all/count/")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public ResponseHolder getCountOfProcessInstanceStatus() {
        List<Object> processCountList = new ArrayList<>();
        ResponseHolder response = new ResponseHolder();

        long completedInstanceCount = BPMNOSGIService.getHistoryService().
                createHistoricProcessInstanceQuery().processInstanceTenantId(getTenantIdStr()).finished().count();

        long activeInstanceCount = BPMNOSGIService.getRuntimeService()
                .createProcessInstanceQuery().processInstanceTenantId(getTenantIdStr()).active().count();

        long suspendedInstanceCount = BPMNOSGIService.getRuntimeService()
                .createProcessInstanceQuery().processInstanceTenantId(getTenantIdStr()).suspended().count();

        long failedInstanceCont = BPMNOSGIService.getManagementService()
                .createJobQuery().jobTenantId(getTenantIdStr()).withException().count();

        if ( completedInstanceCount == 0 && activeInstanceCount == 0 && suspendedInstanceCount == 0
                && failedInstanceCont == 0) {
            response.setData(processCountList);
        } else {
            processCountList.add(new ProcessInstanceStatusCountInfo("Completed", completedInstanceCount));
            processCountList.add(new ProcessInstanceStatusCountInfo("Active", activeInstanceCount));
            processCountList.add(new ProcessInstanceStatusCountInfo("Suspended", suspendedInstanceCount));
            processCountList.add(new ProcessInstanceStatusCountInfo("Failed", failedInstanceCont));
            response.setData(processCountList);
        }
        return response;
    }

    /**
     * Get the number of  Task Instances with various states
     * States: Completed , Active, Suspended, Failed
     *
     * @return list with the states and the count of task instances in each state
     */
    @GET
    @Path("/task-instances/status/all/count")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public ResponseHolder getCountOfTaskInstanceStatus() {
        List<Object> taskCountList = new ArrayList<>();
        ResponseHolder response = new ResponseHolder();
        ProcessInstanceStatusCountInfo completedTaskInstances, activeTaskInstances, suspendedTaskInstances,
                failedTaskInstances;

        TaskQuery taskQuery = BPMNOSGIService.getTaskService().createTaskQuery();
        long completedTaskInstanceCount = BPMNOSGIService.getHistoryService().
                createHistoricTaskInstanceQuery().taskTenantId(getTenantIdStr()).finished().count();

        long activeTaskInstanceCount = taskQuery.taskTenantId(getTenantIdStr()).active().count();

        long suspendedTaskInstanceCount = taskQuery.taskTenantId(getTenantIdStr()).suspended().count();
        //Check on this
        long failedTaskInstanceCount = BPMNOSGIService.getManagementService()
                .createJobQuery().jobTenantId(getTenantIdStr()).withException().count();

        if (completedTaskInstanceCount == 0 && activeTaskInstanceCount == 0 &&
                suspendedTaskInstanceCount == 0 && failedTaskInstanceCount == 0) {
            response.setData(taskCountList);
        } else {
            taskCountList.add(new ProcessInstanceStatusCountInfo("Completed", completedTaskInstanceCount));
            taskCountList.add(new ProcessInstanceStatusCountInfo("Active", activeTaskInstanceCount));
            taskCountList.add(new ProcessInstanceStatusCountInfo("Suspended", suspendedTaskInstanceCount));
            taskCountList.add(new ProcessInstanceStatusCountInfo("Failed", failedTaskInstanceCount));
            response.setData(taskCountList);
        }
        return response;
    }

    /**
     * Get the average time duration of completed processes
     *
     * @return list with the completed processes and the average time duration taken for each process
     */
    @GET
//    @Path("/avgDurationToCompleteProcess/")
    @Path("/process-instances/duration/average")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public ResponseHolder getAvgTimeDurationForCompletedProcesses() {

        ResponseHolder response = new ResponseHolder();
        List<Object> list = new ArrayList<>();

        HistoryService historyService = BPMNOSGIService.getHistoryService();
        RepositoryService repositoryService = BPMNOSGIService.getRepositoryService();
        List<ProcessDefinition> deployedProcessList = repositoryService.
                createProcessDefinitionQuery().processDefinitionTenantId(getTenantIdStr()).list();

        for (ProcessDefinition instance : deployedProcessList) {
            ProcessInstanceAverageInfo bpmnProcessInstance = new ProcessInstanceAverageInfo();
            bpmnProcessInstance.setProcessDefinitionId(instance.getId());
            double totalTime = 0;
            double averageTime;
            String processDefinitionID = instance.getId();

            HistoricProcessInstanceQuery historicProcessInstanceQuery = historyService.
                    createHistoricProcessInstanceQuery().processInstanceTenantId(getTenantIdStr()).
                        processDefinitionId(processDefinitionID).finished();
            long instanceCount = historicProcessInstanceQuery.count();

            if (instanceCount != 0) {
                List<HistoricProcessInstance> instanceList = historicProcessInstanceQuery.list();

                for (HistoricProcessInstance completedProcess : instanceList) {
                    double timeDurationOfTask = completedProcess.getDurationInMillis();
                    double timeInMins = timeDurationOfTask / (1000 * 60);
                    totalTime += timeInMins;
                }
                averageTime = totalTime / instanceCount;
                bpmnProcessInstance.setAverageTimeForCompletion(averageTime);
                list.add(bpmnProcessInstance);
            }
        }
        response.setData(list);
        return response;
    }

    /**
     * Average task duration for completed processes
     *
     * @param pId processDefintionId of the process selected to view the average time duration for each task
     * @return list of completed tasks with the average time duration for the selected process
     */
    @GET
//    @Path("/avgTaskDurationForCompletedProcess/{pId}")
    @Path("/task-instances/duration/avarage/{pid}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public ResponseHolder avgTaskTimeDurationForCompletedProcesses(@PathParam("pid") String pId) {

        RepositoryService repositoryService = BPMNOSGIService.getRepositoryService();
        HistoryService historyService = BPMNOSGIService.getHistoryService();

        long processCount = repositoryService.createProcessDefinitionQuery().
                processDefinitionTenantId(getTenantIdStr()).processDefinitionId(pId).count();

        if (processCount == 0) {
            throw new ActivitiObjectNotFoundException("Count not find a matching process with PID '" +
                    pId + "'.");
        }

        ResponseHolder response = new ResponseHolder();
        List<Object> taskListForProcess = new ArrayList<>();
        HashMap<String, Long> map = new HashMap<>();

        //Get the number of completed/finished process instance for each process definition
        HistoricProcessInstanceQuery historicProcessInstanceQuery = historyService
                .createHistoricProcessInstanceQuery().processInstanceTenantId(getTenantIdStr())
                .processDefinitionId(pId).finished();
        //Get the count of the complete process instances
        long noOfHistoricInstances = historicProcessInstanceQuery.count();

        //If the deployed process does not have any completed process instances --> Ignore
        if (noOfHistoricInstances == 0) {
            response.setData(taskListForProcess);
        }
        //If the deployed process has completed process instances --> then
        else {

            TaskInstanceAverageInfo tInstance;
            //Get the list of completed tasks/activities in the completed process instance by passing the
            //process definition id of the process
            List<HistoricTaskInstance> taskList = BPMNOSGIService.getHistoryService().
                    createHistoricTaskInstanceQuery().taskTenantId(getTenantIdStr()).processDefinitionId(pId)
                    .processFinished().list();
            //Iterate through each completed task/activity and get the task name and duration
            for (HistoricTaskInstance taskInstance : taskList) {
                //Get the task name
                String taskKey = taskInstance.getTaskDefinitionKey();
                //Get the time duration taken for the task to be completed
                long taskDuration = taskInstance.getDurationInMillis();

                if (map.containsKey(taskKey)) {
                    long tt = map.get(taskKey);
                    map.put(taskKey, taskDuration + tt);
                } else {
                    map.put(taskKey, taskDuration);
                }
                //Iterating Task List finished
            }
            Iterator iterator = map.keySet().iterator();
            while (iterator.hasNext()) {
                String key = iterator.next().toString();
                double value = map.get(key) / noOfHistoricInstances;
                tInstance = new TaskInstanceAverageInfo();
                tInstance.setTaskDefinitionKey(key);
                tInstance.setAverageTimeForCompletion(value);
                taskListForProcess.add(tInstance);
            }

            response.setData(taskListForProcess);
        }
        return response;
    }

    /**
     * Task variation over time i.e. tasks started and completed over the months
     *
     * @return array with the no. of tasks started and completed over the months
     */
    @GET
//    @Path("/taskVariation/
    @Path("/task-instances/count/variation")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public ResponseHolder taskVariationOverTime() {
        ResponseHolder response = new ResponseHolder();
        List list = new ArrayList();
        SimpleDateFormat ft = new SimpleDateFormat("M");

        ProcessInstanceStatInfo[] taskStatPerMonths = new ProcessInstanceStatInfo[12];
        for (int i = 0; i < taskStatPerMonths.length; i++) {
            taskStatPerMonths[i] = new ProcessInstanceStatInfo(MONTHS[i], 0, 0);
        }
        // Get completed tasks
        List<HistoricTaskInstance> taskList = BPMNOSGIService.getHistoryService().createHistoricTaskInstanceQuery().
                taskTenantId(getTenantIdStr()).finished().list();

        for (HistoricTaskInstance instance : taskList) {
            int startTime = Integer.parseInt(ft.format(instance.getCreateTime()));
            int endTime = Integer.parseInt(ft.format(instance.getEndTime()));
            taskStatPerMonths[startTime - 1].setInstancesStarted(taskStatPerMonths[startTime - 1].getInstancesStarted() + 1);
            taskStatPerMonths[endTime - 1].setInstancesCompleted(taskStatPerMonths[endTime - 1].getInstancesCompleted() + 1);

        }
        // Get active/started tasks
        List<Task> taskActive = BPMNOSGIService.getTaskService().createTaskQuery().taskTenantId(getTenantIdStr()).active().list();
        for (Task instance : taskActive) {

            int startTime = Integer.parseInt(ft.format(instance.getCreateTime()));
            taskStatPerMonths[startTime - 1].setInstancesStarted(taskStatPerMonths[startTime - 1].getInstancesStarted() + 1);
        }

        // Get suspended tasks
        List<Task> taskSuspended = BPMNOSGIService.getTaskService().createTaskQuery().taskTenantId(getTenantIdStr()).suspended()
                .list();
        for (Task instance : taskSuspended) {

            int startTime = Integer.parseInt(ft.format(instance.getCreateTime()));
            taskStatPerMonths[startTime - 1].setInstancesStarted(taskStatPerMonths[startTime - 1].getInstancesStarted() + 1);

        }
        Collections.addAll(list, taskStatPerMonths);
        response.setData(list);
        return response;
    }

    /**
     * Process variation over time i.e. tasks started and completed over the months
     *
     * @return array with the no. of processes started and completed over the months
     */
    @GET
    @Path("/process-instances/count/variation")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public ResponseHolder processVariationOverTime() {
        ResponseHolder response = new ResponseHolder();
        List list = new ArrayList();
        SimpleDateFormat ft = new SimpleDateFormat("M");
        ProcessInstanceStatInfo[] processStatPerMonths = new ProcessInstanceStatInfo[12];
        for (int i = 0; i < processStatPerMonths.length; i++) {
            processStatPerMonths[i] = new ProcessInstanceStatInfo(MONTHS[i], 0, 0);
        }

        //Get completed process instances
        List<HistoricProcessInstance> completedProcesses = BPMNOSGIService.getHistoryService().
                createHistoricProcessInstanceQuery().processInstanceTenantId(getTenantIdStr()).finished().list();
        for (HistoricProcessInstance instance : completedProcesses) {
            int startTime = Integer.parseInt(ft.format(instance.getStartTime()));
            int endTime = Integer.parseInt(ft.format(instance.getEndTime()));
            processStatPerMonths[startTime - 1].setInstancesStarted(processStatPerMonths[startTime - 1].getInstancesStarted() + 1);
            processStatPerMonths[endTime - 1].setInstancesCompleted(processStatPerMonths[endTime - 1].getInstancesCompleted() + 1);

        }
        // Get active process instances
        List<HistoricProcessInstance> activeProcesses = BPMNOSGIService.getHistoryService().
                createHistoricProcessInstanceQuery().processInstanceTenantId(getTenantIdStr()).unfinished().list();
        for (HistoricProcessInstance instance : activeProcesses) {
            int startTime = Integer.parseInt(ft.format(instance.getStartTime()));
            processStatPerMonths[startTime - 1].setInstancesStarted(processStatPerMonths[startTime - 1].getInstancesStarted() + 1);

        }

        Collections.addAll(list, processStatPerMonths);
        response.setData(list);
        return response;
    }

    /**
     * Get all deployed processes
     *
     * @return list with the processDefinitions of all deployed processes
     */
    @GET
    @Path("/processes/")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public ResponseHolder getAllProcesses() {
        //Get a list of the deployed processes
        RepositoryService repositoryService = BPMNOSGIService.getRepositoryService();
        List<ProcessDefinition> processDefinitionList = repositoryService.
                createProcessDefinitionQuery().processDefinitionTenantId(getTenantIdStr()).list();
        List<Object> listOfProcesses = new ArrayList<>();
        ResponseHolder response = new ResponseHolder();
        for (ProcessDefinition processDefinition : processDefinitionList) {
            listOfProcesses.add(processDefinition.getId());
        }

        response.setData(listOfProcesses);
        return response;
    }

    /**
     * Return the no. of processes deployed
     *
     * @return list with the processDefinitions of all deployed processes
     */
    @GET
    @Path("/processes/count")
    @Produces(MediaType.APPLICATION_JSON)
    public long getProcessCount() {
        //Get a list of the deployed processes
        return BPMNOSGIService.getRepositoryService().
                createProcessDefinitionQuery().processDefinitionTenantId(getTenantIdStr()).count();
    }

    /**
     * Return all the tasks/activities in a process
     * @param pId process instance id
     * @return all the tasks/activities in a process
     */
    @GET
    @Path("/task-instances/{pId}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public ResponseHolder getTasks(@PathParam("pId") String pId) {
        ResponseHolder response = new ResponseHolder();
        List<Object> list = new ArrayList();

        RepositoryService repositoryService = BPMNOSGIService.getRepositoryService();
        ProcessDefinitionEntity processDefinition =
                (ProcessDefinitionEntity) ((RepositoryServiceImpl) repositoryService).
                        getDeployedProcessDefinition(pId);

        if (processDefinition != null) {
            for (ActivityImpl activity : processDefinition.getActivities()) {
                TaskInfo taskInfo = new TaskInfo();
                String taskDefKey = activity.getId();
                String type = (String) activity.getProperty("type");
                String taskName = (String) activity.getProperty("name");
                taskInfo.setTaskDefinitionKey(taskDefKey);
                taskInfo.setType(type);
                taskInfo.setName(taskName);
                list.add(taskInfo);
            }
        }

        response.setData(list);
        return response;
    }

    /**
     * Task variation of user over time i.e. tasks started and completed by the user -- User Performance
     *
     * @param assignee taskAssignee/User selected to view the user performance of task completion over time
     * @return array with the tasks started and completed of the selected user
     */
    @GET
    @Path("/user-performance/variation/{assignee}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public ResponseHolder taskVariationOverTime(@PathParam("assignee") String assignee) throws UserStoreException {

        if (!validateCurrentUser(assignee)) {
            throw  new ActivitiObjectNotFoundException("User with user id " + assignee
                    + "not defined in the system" );
        }

        ResponseHolder response = new ResponseHolder();
        List list = new ArrayList();
        SimpleDateFormat ft = new SimpleDateFormat("M");

        ProcessInstanceStatInfo[] taskStatPerMonths = new ProcessInstanceStatInfo[12];
        for (int i = 0; i < taskStatPerMonths.length; i++) {
            taskStatPerMonths[i] = new ProcessInstanceStatInfo(MONTHS[i], 0, 0);
        }
        // Get completed tasks
        List<HistoricTaskInstance> taskList = BPMNOSGIService.getHistoryService()
                .createHistoricTaskInstanceQuery().taskTenantId(getTenantIdStr()).taskAssignee(assignee).finished().list();
        for (HistoricTaskInstance instance : taskList) {
            int startTime = Integer.parseInt(ft.format(instance.getCreateTime()));
            int endTime = Integer.parseInt(ft.format(instance.getEndTime()));
            taskStatPerMonths[startTime - 1].setInstancesStarted(taskStatPerMonths[startTime - 1].getInstancesStarted() + 1);
            taskStatPerMonths[endTime - 1].setInstancesCompleted(taskStatPerMonths[endTime - 1].getInstancesCompleted() + 1);

        }
        // Get active/started tasks
        List<Task> taskActive = BPMNOSGIService.getTaskService().
                createTaskQuery().taskTenantId(getTenantIdStr()).taskAssignee(assignee).active().list();
        for (Task instance : taskActive) {
            int startTime = Integer.parseInt(ft.format(instance.getCreateTime()));
            taskStatPerMonths[startTime - 1].setInstancesStarted(taskStatPerMonths[startTime - 1].getInstancesStarted() + 1);
        }

        // Get suspended tasks
        List<Task> taskSuspended = BPMNOSGIService.getTaskService().createTaskQuery().
                taskTenantId(getTenantIdStr()).taskAssignee(assignee).suspended().list();

        for (Task instance : taskSuspended) {
            int startTime = Integer.parseInt(ft.format(instance.getCreateTime()));
            taskStatPerMonths[startTime - 1].setInstancesStarted(taskStatPerMonths[startTime - 1].getInstancesStarted() + 1);
        }

        for (int i = 0; i < taskStatPerMonths.length; i++) {
            list.add(taskStatPerMonths[i]);
        }
        response.setData(list);
        return response;
    }

//    /**
//     * @return list of users retrieved from the UserStore
//     */
//    @GET
//    @Path("/users/")
//    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
//    public ResponseHolder getUserList() throws UserStoreException {
//        Object[] users = null;
//        ResponseHolder response = new ResponseHolder();
//        users = (Object[]) BPMNOSGIService.getUserRealm().getUserStoreManager().listUsers("*", -1);
//        response.setData(Arrays.asList(users));
//        return response;
//    }

    /**
     * Get the No.of tasks completed by each user
     *
     * @return list with the no.of tasks completed by each user
     */
//    @GET
//    @Path("/user-performance/task-instances/count")
//    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
//    public ResponseHolder getNoOfTasksCompletedByUser() throws UserStoreException {
//
//        List listOfUsers = new ArrayList<>();
//        ResponseHolder response = new ResponseHolder();
//        String[] users = (String[]) getUserList().getData().toArray();
//        for (String u : users) {
//            UserTaskCountInfo userInfo = new UserTaskCountInfo();
//            userInfo.setUserName(u);
//            String assignee;
//            if (getTenantDomain().equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
//                assignee = u;
//            } else {
//                assignee = u.concat("@").concat(getTenantDomain());
//            }
//            long count = BPMNOSGIService.getHistoryService()
//                    .createHistoricTaskInstanceQuery().taskTenantId(getTenantIdStr()).taskAssignee(assignee).finished().count();
//
//            userInfo.setTaskCount(count);
//            listOfUsers.add(userInfo);
//        }
//        response.setData(listOfUsers);
//
//        return response;
//    }
//
//    /**
//     * Get the average time duration taken by each user to complete tasks
//     *
//     * @return list with the average time duration taken by each user to complete tasks
//     */
//    @GET
//    @Path("/user-performance/task-instances/duration/avarage/")
//    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
//    public ResponseHolder getAvgDurationForTasksCompletedByUser() throws UserStoreException {
//
//        HistoryService historyService = BPMNOSGIService.getHistoryService();
//        List listOfUsers = new ArrayList<>();
//        ResponseHolder response = new ResponseHolder();
//
//        String[] users = (String[]) getUserList().getData().toArray();
//        for (String u : users) {
//
//            UserTaskDuration userInfo = new UserTaskDuration();
//            userInfo.setUserName(u);
//
//            String assignee;
//            if (getTenantDomain().equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
//                assignee = u;
//            } else {
//                assignee = u.concat("@").concat(getTenantDomain());
//            }
//
//            long count = historyService.createHistoricTaskInstanceQuery()
//                            .taskTenantId(getTenantIdStr()).taskAssignee(assignee).finished().count();
//            if (count == 0) {
//                userInfo.setAvgTimeDuration(0);
//            } else {
//                List<HistoricTaskInstance> taskList = historyService.createHistoricTaskInstanceQuery()
//                        .taskTenantId(getTenantIdStr()).taskAssignee(assignee).finished().list();
//                double totalTime = 0;
//                double avgTime = 0;
//                for (HistoricTaskInstance instance : taskList) {
//                    double taskDuration = instance.getDurationInMillis();
//                    totalTime = totalTime + taskDuration;
//                }
//                avgTime = (totalTime / count) / 1000;
//                userInfo.setAvgTimeDuration(avgTime);
//            }
//            listOfUsers.add(userInfo);
//        }
//        response.setData(listOfUsers);
//
//        return response;
//    }



    private int getTenantId() {
        return PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
    }

    private String getTenantIdStr() {
        return String.valueOf(getTenantId());
    }

    private String getTenantDomain() {
        return PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain(true);
    }

    private ProcessInstanceStatusCountInfo createStatusInfo(String status, long instanceCount) {
        return new ProcessInstanceStatusCountInfo(status, instanceCount);
    }

    private boolean validateCurrentUser(String user) throws UserStoreException {
        UserStoreManager userStoreManager = BPMNOSGIService.getUserRealm().getUserStoreManager();
        return userStoreManager.isExistingUser(user);
    }
}
