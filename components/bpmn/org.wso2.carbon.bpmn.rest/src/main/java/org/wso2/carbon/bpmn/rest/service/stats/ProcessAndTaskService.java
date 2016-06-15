/**
 * Copyright (c) 2015-2016 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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
package org.wso2.carbon.bpmn.rest.service.stats;

import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricProcessInstanceQuery;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.bpmn.rest.internal.RestServiceContentHolder;
import org.wso2.carbon.bpmn.rest.model.stats.BPMNTaskInstance;
import org.wso2.carbon.bpmn.rest.model.stats.CompletedProcesses;
import org.wso2.carbon.bpmn.rest.model.stats.DeployedProcesses;
import org.wso2.carbon.bpmn.rest.model.stats.InstanceStatPerMonth;
import org.wso2.carbon.bpmn.rest.model.stats.ProcessTaskCount;
import org.wso2.carbon.bpmn.rest.model.stats.ResponseHolder;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

//import org.wso2.carbon.context.PrivilegedCarbonContext;

/**
 * Service class which includes functionalities related to processes and tasks
 */

//@Component(
//        name = "org.wso2.carbon.bpmn.rest.service.stats.ProcessAndTaskService",
//        service = Microservice.class,
//        immediate = true)
//
//@Path("/process-task-services/")
public class ProcessAndTaskService { //} implements Microservice {

    private static final Logger log = LoggerFactory.getLogger(ProcessAndTaskService.class);


    /**
     * Get the deployed processes count
     *
     * @return a list of deployed processes with their instance count
     */

    public ResponseHolder getDeployedProcesses() {
        List<ProcessDefinition> deployments = RestServiceContentHolder.getInstance().getRestService()
                .getRepositoryService().
                        createProcessDefinitionQuery().list();
        List bpmnProcessInstancesList = new ArrayList<>();
        ResponseHolder response = new ResponseHolder();
        for (ProcessDefinition instance : deployments) {
            DeployedProcesses bpmnProcessInstance = new DeployedProcesses();
            bpmnProcessInstance.setProcessDefinitionId(instance.getId());
            String pId = instance.getId();
            long count1 = getCountOfHistoricProcessInstances(pId);
            long count2 = getCountOfRunningProcessInstances(pId);
            long noOfInstances = count1 + count2;
            bpmnProcessInstance.setDeployedProcessCount(noOfInstances);
            bpmnProcessInstancesList.add(bpmnProcessInstance);

        }
        response.setData(bpmnProcessInstancesList);
        return response;

    }

    /**
     * Get the count of historic processInstances for a process definition
     *
     * @param processDefinitionName processDefintionId of the process
     * @return count of historic processInstances
     */
    private long getCountOfHistoricProcessInstances(String processDefinitionName) {

        long countOfFinishedInstances =
                RestServiceContentHolder.getInstance().getRestService().getHistoryService()
                        .createHistoricProcessInstanceQuery()
                        .processDefinitionId(processDefinitionName).finished().count();

        return countOfFinishedInstances;
    }

    /**
     * Get the number of running/active processInstances for a process definition
     *
     * @param processDefinitionName processDefintionId of the process
     * @return count of active processInstances
     */
    private long getCountOfRunningProcessInstances(String processDefinitionName) {

        long countOfRunningInstances =
                RestServiceContentHolder.getInstance().getRestService().getRumtimeService().createProcessInstanceQuery()
                        .processDefinitionId(processDefinitionName).count();

        return countOfRunningInstances;
    }

    /**
     * Get the number of  processInstances with various States
     * States: Completed , Active, Suspended, Failed
     *
     * @return list with the states and the count of process instances in each state
     */


    public ResponseHolder getCountOfProcessInstanceStatus() {
        List processCountList = new ArrayList<>();
        ResponseHolder response = new ResponseHolder();
        ProcessTaskCount completedProcessInstances, activeProcessInstances,
                suspendedProcessInstances,
                failedProcessInstances;
        long countOfCompletedProcessInstances = RestServiceContentHolder.getInstance().getRestService()
                .getHistoryService().
                        createHistoricProcessInstanceQuery().finished().count();

        long countOfActiveProcessInstances = RestServiceContentHolder.getInstance().getRestService()
                .getRumtimeService().
                        createProcessInstanceQuery().active().count();

        long countOfSuspendedProcessInstances = RestServiceContentHolder.getInstance().getRestService()
                .getRumtimeService().
                        createProcessInstanceQuery().suspended().count();

        long countOfFailedProcessInstances =
                RestServiceContentHolder.getInstance().getRestService().getManagementService().createJobQuery()
                        .withException().count();

        if (countOfCompletedProcessInstances == 0 && countOfActiveProcessInstances == 0 &&
                countOfSuspendedProcessInstances == 0 && countOfFailedProcessInstances == 0) {
            response.setData(processCountList);
        } else {
            completedProcessInstances = new ProcessTaskCount();
            completedProcessInstances.setStatusOfProcessOrTask("Completed");
            completedProcessInstances.setCount(countOfCompletedProcessInstances);
            processCountList.add(completedProcessInstances);

            activeProcessInstances = new ProcessTaskCount();
            activeProcessInstances.setStatusOfProcessOrTask("Active");
            activeProcessInstances.setCount(countOfActiveProcessInstances);
            processCountList.add(activeProcessInstances);

            suspendedProcessInstances = new ProcessTaskCount();
            suspendedProcessInstances.setStatusOfProcessOrTask("Suspended");
            suspendedProcessInstances.setCount(countOfSuspendedProcessInstances);
            processCountList.add(suspendedProcessInstances);

            failedProcessInstances = new ProcessTaskCount();
            failedProcessInstances.setStatusOfProcessOrTask("Failed");
            failedProcessInstances.setCount(countOfFailedProcessInstances);
            processCountList.add(failedProcessInstances);

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
    public ResponseHolder getCountOfTaskInstanceStatus() {

        List taskCountList = new ArrayList<>();
        ResponseHolder response = new ResponseHolder();
        ProcessTaskCount completedTaskInstances, activeTaskInstances, suspendedTaskInstances,
                failedTaskInstances;

        TaskQuery taskQuery = RestServiceContentHolder.getInstance().getRestService().getTaskService()
                .createTaskQuery();
        long countOfCompletedTaskInstances = RestServiceContentHolder.getInstance().getRestService()
                .getHistoryService().
                        createHistoricTaskInstanceQuery().finished().count();

        long countOfActiveTaskInstances = taskQuery.active().count();

        long countOfSuspendedTaskInstances = taskQuery.suspended().count();
        //Check on this
        long countOfFailedTaskInstances =
                RestServiceContentHolder.getInstance().getRestService().getManagementService().createJobQuery()
                        .withException().count();

        if (countOfCompletedTaskInstances == 0 && countOfActiveTaskInstances == 0 &&
                countOfSuspendedTaskInstances == 0 && countOfFailedTaskInstances == 0) {
            response.setData(taskCountList);
        } else {
            completedTaskInstances = new ProcessTaskCount();
            completedTaskInstances.setStatusOfProcessOrTask("Completed");
            completedTaskInstances.setCount(countOfCompletedTaskInstances);
            taskCountList.add(completedTaskInstances);

            activeTaskInstances = new ProcessTaskCount();
            activeTaskInstances.setStatusOfProcessOrTask("Active");
            activeTaskInstances.setCount(countOfActiveTaskInstances);
            taskCountList.add(activeTaskInstances);

            suspendedTaskInstances = new ProcessTaskCount();
            suspendedTaskInstances.setStatusOfProcessOrTask("Suspended");
            suspendedTaskInstances.setCount(countOfSuspendedTaskInstances);
            taskCountList.add(suspendedTaskInstances);

            failedTaskInstances = new ProcessTaskCount();
            failedTaskInstances.setStatusOfProcessOrTask("Failed");
            failedTaskInstances.setCount(countOfFailedTaskInstances);
            taskCountList.add(failedTaskInstances);

            response.setData(taskCountList);
        }
        return response;
    }

    /**
     * Get the average time duration of completed processes
     *
     * @return list with the completed processes and the average time duration taken for each process
     */
    public ResponseHolder getAvgTimeDurationForCompletedProcesses() {
        List<ProcessDefinition> deployements = RestServiceContentHolder.getInstance().getRestService()
                .getRepositoryService().
                        createProcessDefinitionQuery().list();

        ResponseHolder response = new ResponseHolder();
        List list = new ArrayList<>();

        for (ProcessDefinition instance : deployements) {
            CompletedProcesses bpmnProcessInstance = new CompletedProcesses();
            bpmnProcessInstance.setProcessDefinitionId(instance.getId());

            double totalTime = 0;
            double averageTime = 0;
            String processDefinitionID = instance.getId();

            HistoricProcessInstanceQuery historicProcessInstanceQuery =
                    RestServiceContentHolder.getInstance().getRestService().getHistoryService()
                            .createHistoricProcessInstanceQuery()
                            .processDefinitionId(processDefinitionID).finished();

            long noOfHistoricInstances = historicProcessInstanceQuery.count();

            if (noOfHistoricInstances == 0) {
            } else {
                List<HistoricProcessInstance> instanceList = historicProcessInstanceQuery.list();

                for (HistoricProcessInstance completedProcess : instanceList) {
                    double timeDurationOfTask = completedProcess.getDurationInMillis();
                    double timeInMins = timeDurationOfTask / (1000 * 60);
                    totalTime += timeInMins;
                }
                averageTime = totalTime / noOfHistoricInstances;
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
     * @param pId processDefintionId of the process selected to view the average time duration
     *            for each task
     * @return list of completed tasks with the average time duration for the selected process
     */
    public ResponseHolder avgTaskTimeDurationForCompletedProcesses(String pId) {
        long countOfProcesses = RestServiceContentHolder.getInstance().getRestService().getRepositoryService().
                createProcessDefinitionQuery().processDefinitionId(pId).count();
        if (countOfProcesses == 0) {
            throw new ActivitiObjectNotFoundException(
                    "Could not find process with process definition id '" +
                            pId + "'.");
        }

        ResponseHolder response = new ResponseHolder();
        List taskListForProcess = new ArrayList<>();
        HashMap<String, Long> map = new HashMap<String, Long>();
        //Get the number of completed/finished process instance for each process definition
        HistoricProcessInstanceQuery historicProcessInstanceQuery =
                RestServiceContentHolder.getInstance().getRestService().getHistoryService()
                        .createHistoricProcessInstanceQuery()
                        .processDefinitionId(pId).finished();
        //Get the count of the complete process instances
        long noOfHistoricInstances = historicProcessInstanceQuery.count();

        //If the deployed process doesnot have any completed process instances --> Ignore
        if (noOfHistoricInstances == 0) {
            response.setData(taskListForProcess);
            //If the deployed process has completed process instances --> then
        } else {

            BPMNTaskInstance tInstance;
            //Get the list of completed tasks/activities in the completed process instance
            // by passing the
            //process definition id of the process
            List<HistoricTaskInstance> taskList = RestServiceContentHolder.getInstance().getRestService()
                    .getHistoryService().
                            createHistoricTaskInstanceQuery().processDefinitionId(pId)
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
                double value = map.get(key) / (double) noOfHistoricInstances;
                tInstance = new BPMNTaskInstance();
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
    public ResponseHolder taskVariationOverTime() {
        ResponseHolder response = new ResponseHolder();
        List list = new ArrayList();
        String[] months =
                {"Jan", "Feb", "March", "April", "May", "June", "July", "Aug", "Sep", "Oct", "Nov",
                        "Dec"};
        SimpleDateFormat ft = new SimpleDateFormat("M");

        InstanceStatPerMonth[] taskStatPerMonths = new InstanceStatPerMonth[12];
        for (int i = 0; i < taskStatPerMonths.length; i++) {
            taskStatPerMonths[i] = new InstanceStatPerMonth();
            taskStatPerMonths[i].setMonth(months[i]);
            taskStatPerMonths[i].setCompletedInstances(0);
            taskStatPerMonths[i].setStartedInstances(0);
        }
        // Get completed tasks
        List<HistoricTaskInstance> taskList =
                RestServiceContentHolder.getInstance().getRestService().getHistoryService()
                        .createHistoricTaskInstanceQuery().
                        finished().list();

        for (HistoricTaskInstance instance : taskList) {
            int startTime = Integer.parseInt(ft.format(instance.getCreateTime()));
            int endTime = Integer.parseInt(ft.format(instance.getEndTime()));
            taskStatPerMonths[startTime - 1].setStartedInstances(
                    taskStatPerMonths[startTime - 1].getStartedInstances() + 1);
            taskStatPerMonths[endTime - 1].setCompletedInstances(
                    taskStatPerMonths[endTime - 1].getCompletedInstances() + 1);

        }
        // Get active/started tasks
        List<Task> taskActive =
                RestServiceContentHolder.getInstance().getRestService().getTaskService().createTaskQuery().active()
                        .list();
        for (Task instance : taskActive) {

            int startTime = Integer.parseInt(ft.format(instance.getCreateTime()));
            taskStatPerMonths[startTime - 1].setStartedInstances(
                    taskStatPerMonths[startTime - 1].getStartedInstances() + 1);
        }

        // Get suspended tasks
        List<Task> taskSuspended =
                RestServiceContentHolder.getInstance().getRestService().getTaskService().createTaskQuery().suspended()
                        .list();
        for (Task instance : taskSuspended) {

            int startTime = Integer.parseInt(ft.format(instance.getCreateTime()));
            taskStatPerMonths[startTime - 1].setStartedInstances(
                    taskStatPerMonths[startTime - 1].getStartedInstances() + 1);

        }
        for (int i = 0; i < taskStatPerMonths.length; i++) {
            list.add(taskStatPerMonths[i]);
        }
        response.setData(list);
        return response;
    }

    /**
     * Process variation over time i.e. tasks started and completed over the months
     *
     * @return array with the no. of processes started and completed over the months
     */

    public ResponseHolder processVariationOverTime() {
        ResponseHolder response = new ResponseHolder();
        List list = new ArrayList();
        String[] months =
                {"Jan", "Feb", "March", "April", "May", "June", "July", "Aug", "Sep", "Oct", "Nov",
                        "Dec"};
        SimpleDateFormat ft = new SimpleDateFormat("M");
        InstanceStatPerMonth[] processStatPerMonths = new InstanceStatPerMonth[12];
        for (int i = 0; i < processStatPerMonths.length; i++) {
            processStatPerMonths[i] = new InstanceStatPerMonth();
            processStatPerMonths[i].setMonth(months[i]);
            processStatPerMonths[i].setCompletedInstances(0);
            processStatPerMonths[i].setStartedInstances(0);
        }

        //Get completed process instances
        List<HistoricProcessInstance> completedProcesses = RestServiceContentHolder.getInstance().getRestService()
                .getHistoryService().
                        createHistoricProcessInstanceQuery().finished().list();
        for (HistoricProcessInstance instance : completedProcesses) {
            int startTime = Integer.parseInt(ft.format(instance.getStartTime()));
            int endTime = Integer.parseInt(ft.format(instance.getEndTime()));
            processStatPerMonths[startTime - 1].setStartedInstances(
                    processStatPerMonths[startTime - 1].getStartedInstances() + 1);
            processStatPerMonths[endTime - 1].setCompletedInstances(
                    processStatPerMonths[endTime - 1].getCompletedInstances() + 1);

        }
        // Get active process instances
        List<HistoricProcessInstance> activeProcesses = RestServiceContentHolder.getInstance().getRestService()
                .getHistoryService().
                        createHistoricProcessInstanceQuery().unfinished().list();
        for (HistoricProcessInstance instance : activeProcesses) {
            int startTime = Integer.parseInt(ft.format(instance.getStartTime()));
            processStatPerMonths[startTime - 1].setStartedInstances(
                    processStatPerMonths[startTime - 1].getStartedInstances() + 1);

        }

        for (int i = 0; i < processStatPerMonths.length; i++) {
            list.add(processStatPerMonths[i]);
        }
        response.setData(list);
        return response;
    }

    /**
     * Get all deployed processes
     *
     * @return list with the processDefinitions of all deployed processes
     */
    public ResponseHolder getAllProcesses() {
        //Get a list of the deployed processes
        List<ProcessDefinition> deployements = RestServiceContentHolder.getInstance().getRestService()
                .getRepositoryService().
                        createProcessDefinitionQuery().list();
        List listOfProcesses = new ArrayList<>();
        ResponseHolder response = new ResponseHolder();
        for (ProcessDefinition processDefinition : deployements) {
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
    public long getProcessCount() {
        //Get a list of the deployed processes
        long processCount = RestServiceContentHolder.getInstance().getRestService().getRepositoryService().
                createProcessDefinitionQuery().count();
        return processCount;
    }
}

