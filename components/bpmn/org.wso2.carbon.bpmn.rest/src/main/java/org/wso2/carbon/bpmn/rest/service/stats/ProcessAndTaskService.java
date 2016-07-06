/**
 *  Copyright (c) 2015 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.bpmn.rest.service.stats;

import org.activiti.engine.ActivitiObjectNotFoundException;
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
import org.wso2.carbon.bpmn.rest.common.utils.BPMNOSGIService;
import org.wso2.carbon.bpmn.rest.model.stats.BPMNTaskInstance;
import org.wso2.carbon.bpmn.rest.model.stats.CompletedProcesses;
import org.wso2.carbon.bpmn.rest.model.stats.DeployedProcesses;
import org.wso2.carbon.bpmn.rest.model.stats.InstanceStatPerMonth;
import org.wso2.carbon.bpmn.rest.model.stats.ProcessTaskCount;
import org.wso2.carbon.bpmn.rest.model.stats.ResponseHolder;
import org.wso2.carbon.bpmn.rest.model.stats.TaskInfo;
import org.wso2.carbon.context.PrivilegedCarbonContext;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * Service class which includes functionalities related to processes and tasks
 */

@Path("/processTaskServices/")
public class ProcessAndTaskService {
    private static final Log log = LogFactory.getLog(ProcessAndTaskService.class);
    int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
    String str = String.valueOf(tenantId);
    public static final String[] MONTHS = {"Jan", "Feb", "March", "April", "May", "June", "July", "Aug", "Sep", "Oct", "Nov", "Dec"};

    /**
     * Get the deployed processes count
     *
     * @return a list of deployed processes with their instance count
     */
    @GET
    @Path("/deployedProcessCount/")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public ResponseHolder getDeployedProcesses() {
        List<ProcessDefinition> deployments = BPMNOSGIService.getRepositoryService().
                createProcessDefinitionQuery().processDefinitionTenantId(str).list();
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

        long countOfFinishedInstances = BPMNOSGIService.getHistoryService().createHistoricProcessInstanceQuery()
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

        long countOfRunningInstances = BPMNOSGIService.getRumtimeService()
                .createProcessInstanceQuery().processDefinitionId(processDefinitionName).count();

        return countOfRunningInstances;
    }

    /**
     * Get the number of  processInstances with various States
     * States: Completed , Active, Suspended, Failed
     *
     * @return list with the states and the count of process instances in each state
     */
    @GET
    @Path("/processStatusCount/")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public ResponseHolder getCountOfProcessInstanceStatus() {
        List processCountList = new ArrayList<>();
        ResponseHolder response = new ResponseHolder();
        ProcessTaskCount completedProcessInstances, activeProcessInstances, suspendedProcessInstances,
                failedProcessInstances;
        long countOfCompletedProcessInstances = BPMNOSGIService.getHistoryService().
                createHistoricProcessInstanceQuery().processInstanceTenantId(str).finished().count();

        long countOfActiveProcessInstances = BPMNOSGIService.getRumtimeService()
                .createProcessInstanceQuery().processInstanceTenantId(str).active().count();

        long countOfSuspendedProcessInstances = BPMNOSGIService.getRumtimeService()
                .createProcessInstanceQuery().processInstanceTenantId(str).suspended().count();

        long countOfFailedProcessInstances = BPMNOSGIService.getManagementService()
                .createJobQuery().jobTenantId(str).withException().count();

        if(countOfCompletedProcessInstances == 0 && countOfActiveProcessInstances == 0 &&
                countOfSuspendedProcessInstances == 0 && countOfFailedProcessInstances == 0){
            response.setData(processCountList);
        }
        else {
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
    @GET
    @Path("/taskStatusCount/")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public ResponseHolder getCountOfTaskInstanceStatus() {

        List taskCountList = new ArrayList<>();
        ResponseHolder response = new ResponseHolder();
        ProcessTaskCount completedTaskInstances, activeTaskInstances, suspendedTaskInstances,
                failedTaskInstances;

        TaskQuery taskQuery = BPMNOSGIService.getTaskService().createTaskQuery();
        long countOfCompletedTaskInstances = BPMNOSGIService.getHistoryService().
                createHistoricTaskInstanceQuery().taskTenantId(str).finished().count();

        long countOfActiveTaskInstances = taskQuery.taskTenantId(str).active().count();

        long countOfSuspendedTaskInstances = taskQuery.taskTenantId(str).suspended().count();
        //Check on this
        long countOfFailedTaskInstances = BPMNOSGIService.getManagementService()
                .createJobQuery().jobTenantId(str).withException().count();

        if(countOfCompletedTaskInstances == 0 && countOfActiveTaskInstances == 0 &&
                countOfSuspendedTaskInstances == 0 && countOfFailedTaskInstances == 0){
            response.setData(taskCountList);
        }
        else {
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
    @GET
    @Path("/avgDurationToCompleteProcess/")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public ResponseHolder getAvgTimeDurationForCompletedProcesses() {
        List<ProcessDefinition> deployements = BPMNOSGIService.getRepositoryService().
                createProcessDefinitionQuery().processDefinitionTenantId(str).list();

        ResponseHolder response = new ResponseHolder();
        List list = new ArrayList<>();

        for (ProcessDefinition instance : deployements) {
            CompletedProcesses bpmnProcessInstance = new CompletedProcesses();
            bpmnProcessInstance.setProcessDefinitionId(instance.getId());
            double totalTime = 0;
            double averageTime = 0;
            String processDefinitionID = instance.getId();

            HistoricProcessInstanceQuery historicProcessInstanceQuery = BPMNOSGIService.
                    getHistoryService().createHistoricProcessInstanceQuery().
                    processInstanceTenantId(str).
                    processDefinitionId(processDefinitionID).finished();

            long noOfHistoricInstances = historicProcessInstanceQuery.count();

            if (noOfHistoricInstances != 0) {
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
     * @param pId processDefintionId of the process selected to view the average time duration for each task
     * @return list of completed tasks with the average time duration for the selected process
     */
    @GET
    @Path("/avgTaskDurationForCompletedProcess/{pId}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public ResponseHolder avgTaskTimeDurationForCompletedProcesses(@PathParam("pId") String pId) {
        long countOfProcesses = BPMNOSGIService.getRepositoryService().
                createProcessDefinitionQuery().processDefinitionTenantId(str).processDefinitionId(pId).count();
        if(countOfProcesses == 0){
            throw new ActivitiObjectNotFoundException("Could not find process with process definition id '" +
                    pId + "'.");
        }

        ResponseHolder response = new ResponseHolder();
        List taskListForProcess = new ArrayList<>();
        HashMap<String, Long> map = new HashMap<String, Long>();
        //Get the number of completed/finished process instance for each process definition
        HistoricProcessInstanceQuery historicProcessInstanceQuery = BPMNOSGIService.getHistoryService()
                .createHistoricProcessInstanceQuery().processInstanceTenantId(str)
                .processDefinitionId(pId).finished();
        //Get the count of the complete process instances
        long noOfHistoricInstances = historicProcessInstanceQuery.count();

        //If the deployed process doesnot have any completed process instances --> Ignore
        if (noOfHistoricInstances == 0) {
            response.setData(taskListForProcess);
        }
        //If the deployed process has completed process instances --> then
        else {

            BPMNTaskInstance tInstance = new BPMNTaskInstance();
            //Get the list of completed tasks/activities in the completed process instance by passing the
            //process definition id of the process
            List<HistoricTaskInstance> taskList = BPMNOSGIService.getHistoryService().
                    createHistoricTaskInstanceQuery().taskTenantId(str).processDefinitionId(pId)
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
    @GET
    @Path("/taskVariation/")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public ResponseHolder taskVariationOverTime() {
        ResponseHolder response = new ResponseHolder();
        List list = new ArrayList();
        SimpleDateFormat ft = new SimpleDateFormat("M");

        InstanceStatPerMonth[] taskStatPerMonths = new InstanceStatPerMonth[12];
        for (int i = 0; i < taskStatPerMonths.length; i++) {
            taskStatPerMonths[i] = new InstanceStatPerMonth();
            taskStatPerMonths[i].setMonth(MONTHS[i]);
            taskStatPerMonths[i].setCompletedInstances(0);
            taskStatPerMonths[i].setStartedInstances(0);
        }
        // Get completed tasks
        List<HistoricTaskInstance> taskList = BPMNOSGIService.getHistoryService().createHistoricTaskInstanceQuery().
                taskTenantId(str).finished().list();

        for (HistoricTaskInstance instance : taskList) {
            int startTime = Integer.parseInt(ft.format(instance.getCreateTime()));
            int endTime = Integer.parseInt(ft.format(instance.getEndTime()));
            taskStatPerMonths[startTime - 1].setStartedInstances(taskStatPerMonths[startTime - 1].getStartedInstances() + 1);
            taskStatPerMonths[endTime - 1].setCompletedInstances(taskStatPerMonths[endTime - 1].getCompletedInstances() + 1);

        }
        // Get active/started tasks
        List<Task> taskActive = BPMNOSGIService.getTaskService().createTaskQuery().taskTenantId(str).active().list();
        for (Task instance : taskActive) {

            int startTime = Integer.parseInt(ft.format(instance.getCreateTime()));
            taskStatPerMonths[startTime - 1].setStartedInstances(taskStatPerMonths[startTime - 1].getStartedInstances() + 1);
        }

        // Get suspended tasks
        List<Task> taskSuspended = BPMNOSGIService.getTaskService().createTaskQuery().taskTenantId(str).suspended()
                .list();
        for (Task instance : taskSuspended) {

            int startTime = Integer.parseInt(ft.format(instance.getCreateTime()));
            taskStatPerMonths[startTime - 1].setStartedInstances(taskStatPerMonths[startTime - 1].getStartedInstances() + 1);

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
    @GET
    @Path("/processVariation/")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public ResponseHolder processVariationOverTime() {
        ResponseHolder response = new ResponseHolder();
        List list = new ArrayList();
        SimpleDateFormat ft = new SimpleDateFormat("M");
        InstanceStatPerMonth[] processStatPerMonths = new InstanceStatPerMonth[12];
        for (int i = 0; i < processStatPerMonths.length; i++) {
            processStatPerMonths[i] = new InstanceStatPerMonth();
            processStatPerMonths[i].setMonth(MONTHS[i]);
            processStatPerMonths[i].setCompletedInstances(0);
            processStatPerMonths[i].setStartedInstances(0);
        }

        //Get completed process instances
        List<HistoricProcessInstance> completedProcesses = BPMNOSGIService.getHistoryService().
                createHistoricProcessInstanceQuery().processInstanceTenantId(str).finished().list();
        for (HistoricProcessInstance instance : completedProcesses) {
            int startTime = Integer.parseInt(ft.format(instance.getStartTime()));
            int endTime = Integer.parseInt(ft.format(instance.getEndTime()));
            processStatPerMonths[startTime - 1].setStartedInstances(processStatPerMonths[startTime - 1].getStartedInstances() + 1);
            processStatPerMonths[endTime - 1].setCompletedInstances(processStatPerMonths[endTime - 1].getCompletedInstances() + 1);

        }
        // Get active process instances
        List<HistoricProcessInstance> activeProcesses = BPMNOSGIService.getHistoryService().
                createHistoricProcessInstanceQuery().processInstanceTenantId(str).unfinished().list();
        for (HistoricProcessInstance instance : activeProcesses) {
            int startTime = Integer.parseInt(ft.format(instance.getStartTime()));
            processStatPerMonths[startTime - 1].setStartedInstances(processStatPerMonths[startTime - 1].getStartedInstances() + 1);

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
    @GET
    @Path("/allProcesses/")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public ResponseHolder getAllProcesses() {
        //Get a list of the deployed processes
        List<ProcessDefinition> deployements = BPMNOSGIService.getRepositoryService().
                createProcessDefinitionQuery().processDefinitionTenantId(str).list();
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
    @GET
    @Path("/countOfProcesses/")
    @Produces(MediaType.APPLICATION_JSON)
    public long getProcessCount() {
        //Get a list of the deployed processes
        long processCount = BPMNOSGIService.getRepositoryService().
                createProcessDefinitionQuery().processDefinitionTenantId(str).count();
        return processCount;
    }

    /**
     * Return the bpmn resource/process diagram
     * @param pId process instance id
     * @return bpmn resource/process diagram
     */
    @GET
    @Path("/resourceDiagram/{pId}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public ResponseHolder getResourceDiagram(@PathParam("pId") String pId) {
        //Get a list of the deployed processes
        List<ProcessDefinition> deployements = BPMNOSGIService.getRepositoryService().
                createProcessDefinitionQuery().processDefinitionTenantId(str).processDefinitionId(pId).list();

        List list = new ArrayList<>();
        ResponseHolder response = new ResponseHolder();
        for (ProcessDefinition processDefinition : deployements) {
            list.add(processDefinition.getDiagramResourceName());
        }

        response.setData(list);
        return response;
    }

    /**
     * Return all the tasks/activities in a process
     * @param pId process instance id
     * @return all the tasks/activities in a process
     */
    @GET
    @Path("/allTasks/{pId}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public ResponseHolder getTasks(@PathParam("pId") String pId) {
        ResponseHolder response = new ResponseHolder();
        List list = new ArrayList();

        RepositoryService repositoryService = BPMNOSGIService.getRepositoryService();
        ProcessDefinitionEntity processDefinition = (ProcessDefinitionEntity) ((RepositoryServiceImpl) repositoryService).getDeployedProcessDefinition(pId);
        if (processDefinition!=null) {

            for (ActivityImpl activity: processDefinition.getActivities()) {
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
}






