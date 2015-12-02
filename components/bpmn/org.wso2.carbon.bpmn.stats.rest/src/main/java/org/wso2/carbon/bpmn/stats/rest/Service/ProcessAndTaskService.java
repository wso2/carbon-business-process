package org.wso2.carbon.bpmn.stats.rest.Service;

import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricProcessInstanceQuery;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bpmn.stats.rest.Model.*;
import org.wso2.carbon.bpmn.stats.rest.util.BPMNOsgiServices;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by natasha on 11/23/15.
 */

@Path("/bpmnServices/")
public class ProcessAndTaskService {
    private static final Log log = LogFactory.getLog(ProcessAndTaskService.class);
    /**
     * Get the deployed processes count
     * @return a list of deployed processes with their instance count
     */
    @GET
    @Path("/deployedProcessCount/")
    @Produces(MediaType.APPLICATION_JSON)
    public List<BPMNProcessInstance> getDeployedProcesses() {
        List<ProcessDefinition> deployments = BPMNOsgiServices.getRepositoryService().
                createProcessDefinitionQuery().list();
        List<BPMNProcessInstance> bpmnProcessInstancesList = new ArrayList<>();
        BPMNProcessInstance bpmnProcessInstance = new BPMNProcessInstance();
        for (ProcessDefinition instance : deployments) {
            bpmnProcessInstance = new BPMNProcessInstance();
            bpmnProcessInstance.setProcessDefinitionId(instance.getId());
            String pId = instance.getId();
            long count1 = getCountOfHistoricProcessInstances(pId);
            long count2 = getCountOfRunningProcessInstances(pId);
            long noOfInstances = count1 + count2;
            bpmnProcessInstance.setDeployedProcessCount(noOfInstances);
            bpmnProcessInstancesList.add(bpmnProcessInstance);

        }
        return bpmnProcessInstancesList;

    }

    /**
     * Get the count of historic processInstances for a process definition
     * @param processDefinitionName processDefintionId of the process
     * @return count of historic processInstances
     */
    private long getCountOfHistoricProcessInstances(String processDefinitionName) {

        long countOfFinishedInstances = BPMNOsgiServices.getHistoryService().createHistoricProcessInstanceQuery()
                .processDefinitionId(processDefinitionName).finished().count();

        return countOfFinishedInstances;
    }

    /**
     * Get the number of running/active processInstances for a process definition
     * @param processDefinitionName processDefintionId of the process
     * @return count of active processInstances
     */
    private long getCountOfRunningProcessInstances(String processDefinitionName) {

        long countOfRunningInstances = BPMNOsgiServices.getRumtimeService()
                .createProcessInstanceQuery().processDefinitionId(processDefinitionName).count();

        return countOfRunningInstances;
    }

    /**
     * Get the number of  processInstances with various States
     * States: Completed , Active, Suspended, Failed
     * @return list with the states and the count of process instances in each state
     */
    @GET
    @Path("/processStatusCount/")
    @Produces(MediaType.APPLICATION_JSON)
    public List<ProcessTaskCount> getCountOfProcessInstanceStatus() {
        List<ProcessTaskCount> processCountList = new ArrayList<>();
        ProcessTaskCount completedProcessInstances, activeProcessInstances, suspendedProcessInstances,
                failedProcessInstances;
        long countOfCompletedProcessInstances = BPMNOsgiServices.getHistoryService().
                createHistoricProcessInstanceQuery().finished().count();
        long countOfActiveProcessInstances = BPMNOsgiServices.getRumtimeService()
                .createProcessInstanceQuery().active().count();
        long countOfSuspendedProcessInstances = BPMNOsgiServices.getRumtimeService()
                .createProcessInstanceQuery().suspended().count();
        long countOfFailedProcessInstances = BPMNOsgiServices.getManagementService()
                .createJobQuery().withException().count();

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


        return processCountList;
    }

    /**
     * Get the number of  Task Instances with various states
     * States: Completed , Active, Suspended, Failed
     * @return list with the states and the count of task instances in each state
     */
    @GET
    @Path("/taskStatusCount/")
    @Produces(MediaType.APPLICATION_JSON)
    public List<ProcessTaskCount> getCountOfTaskInstanceStatus() {

        List<ProcessTaskCount> taskCountList = new ArrayList<>();

        ProcessTaskCount completedTaskInstances, activeTaskInstances, suspendedTaskInstances,
                failedTaskInstances;

        TaskQuery taskQuery = BPMNOsgiServices.getTaskService().createTaskQuery();
        long countOfCompletedTaskInstances = BPMNOsgiServices.getHistoryService().createHistoricTaskInstanceQuery().finished().count();
        long countOfActiveTaskInstances = taskQuery.active().count();
        long countOfSuspendedTaskInstances = taskQuery.suspended().count();
        //Check on this
        long countOfFailedTaskInstances = BPMNOsgiServices.getManagementService()
                .createJobQuery().withException().count();

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


        return taskCountList;
    }

    /**
     * Get the average time duration of completed processes
     * @return list with the completed processes and the average time duration taken for each process
     */
    @GET
    @Path("/avgDurationToCompleteProcess/")
    @Produces(MediaType.APPLICATION_JSON)
    public List<BPMNProcessInstance> getAvgTimeDurationForCompletedProcesses() {
        List<ProcessDefinition> deployements = BPMNOsgiServices.getRepositoryService().createProcessDefinitionQuery().list();


        List<BPMNProcessInstance> list = new ArrayList<>();
        BPMNProcessInstance bpmnProcessInstance = new BPMNProcessInstance();

        for (ProcessDefinition instance : deployements) {
            bpmnProcessInstance = new BPMNProcessInstance();
            bpmnProcessInstance.setProcessDefinitionId(instance.getId());

            double totalTime = 0;
            double averageTime = 0;
            String processDefinitionID = instance.getId();


            HistoricProcessInstanceQuery historicProcessInstanceQuery = BPMNOsgiServices.getHistoryService().createHistoricProcessInstanceQuery()
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

        return list;
    }

    /**
     * Average task duration for completed processes
     * @param pId processDefintionId of the process selected to view the average time duration for each task
     * @return list of completed tasks with the average time duration for the selected process
     */
    @GET
    @Path("/avgTaskDurationForCompletedProcess/{pId}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<ProcessForTask> avgTaskTimeDurationForCompletedProcesses(@PathParam("pId") String pId) {

        List<ProcessForTask> list = new ArrayList<>();
        ProcessForTask bpmnProcessInstance = new ProcessForTask();
        HashMap<String, Long> map = new HashMap<String, Long>();

        //Get the number of completed/finished process instance for each process definition
        HistoricProcessInstanceQuery historicProcessInstanceQuery = BPMNOsgiServices.getHistoryService()
                .createHistoricProcessInstanceQuery()
                .processDefinitionId(pId).finished();
        //Get the count of the complete process instances
        long noOfHistoricInstances = historicProcessInstanceQuery.count();
        bpmnProcessInstance = new ProcessForTask();
        //If the deployed process doesnot have any completed process instances --> Ignore
        if (noOfHistoricInstances == 0) {
            bpmnProcessInstance.setTaskList(null);
            list.add(bpmnProcessInstance);
        }
        //If the deployed process has completed process instances --> then
        else {
            List<BPMNTaskInstance> taskListForProcess = new ArrayList<>();
            BPMNTaskInstance tInstance = new BPMNTaskInstance();
            //Get the list of completed tasks/activities in the completed process instance by passing the
            //process definition id of the process
            List<HistoricTaskInstance> taskList = BPMNOsgiServices.getHistoryService().
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
                double value = map.get(key) / noOfHistoricInstances;
                tInstance = new BPMNTaskInstance();
                tInstance.setTaskDefinitionKey(key);
                tInstance.setAverageTimeForCompletion(value);
                taskListForProcess.add(tInstance);
            }
            bpmnProcessInstance.setTaskList(taskListForProcess);
            list.add(bpmnProcessInstance);
        }
        return list;
    }

    /**
     * Task variation over time i.e. tasks started and completed over the months
     * @return array with the no. of tasks started and completed over the months
     */
    @GET
    @Path("/taskVariation/")
    @Produces(MediaType.APPLICATION_JSON)
    public InstanceStatPerMonth[] taskVariationOverTime() {

        String[] MONTHS = {"Jan", "Feb", "March", "April", "May", "June", "July", "Aug", "Sep", "Oct", "Nov", "Dec"};
        SimpleDateFormat ft = new SimpleDateFormat("M");

        InstanceStatPerMonth[] taskStatPerMonths = new InstanceStatPerMonth[12];
        for (int i = 0; i < taskStatPerMonths.length; i++) {
            taskStatPerMonths[i] = new InstanceStatPerMonth();
            taskStatPerMonths[i].setMonth(MONTHS[i]);
            taskStatPerMonths[i].setCompletedInstances(0);
            taskStatPerMonths[i].setStartedInstances(0);
        }
        // Get completed tasks
        List<HistoricTaskInstance> taskList = BPMNOsgiServices.getHistoryService().createHistoricTaskInstanceQuery().finished().list();

        for (HistoricTaskInstance instance : taskList) {
            int startTime = Integer.parseInt(ft.format(instance.getCreateTime()));
            int endTime = Integer.parseInt(ft.format(instance.getEndTime()));
            taskStatPerMonths[startTime - 1].setStartedInstances(taskStatPerMonths[startTime - 1].getStartedInstances() + 1);
            taskStatPerMonths[endTime - 1].setCompletedInstances(taskStatPerMonths[endTime - 1].getCompletedInstances() + 1);

        }
        // Get active/started tasks
        List<Task> taskActive = BPMNOsgiServices.getTaskService().createTaskQuery().active().list();
        for (Task instance : taskActive) {

            int startTime = Integer.parseInt(ft.format(instance.getCreateTime()));
            taskStatPerMonths[startTime - 1].setStartedInstances(taskStatPerMonths[startTime - 1].getStartedInstances() + 1);
        }

        // Get suspended tasks
        List<Task> taskSuspended = BPMNOsgiServices.getTaskService().createTaskQuery().suspended().list();
        for (Task instance : taskSuspended) {

            int startTime = Integer.parseInt(ft.format(instance.getCreateTime()));
            taskStatPerMonths[startTime - 1].setStartedInstances(taskStatPerMonths[startTime - 1].getStartedInstances() + 1);

        }
        return taskStatPerMonths;
    }

    /**
     * Process variation over time i.e. tasks started and completed over the months
     * @return array with the no. of processes started and completed over the months
     */
    @GET
    @Path("/processVariation/")
    @Produces(MediaType.APPLICATION_JSON)
    public InstanceStatPerMonth[] processVariationOverTime() {

        String[] MONTHS = {"Jan", "Feb", "March", "April", "May", "June", "July", "Aug", "Sep", "Oct", "Nov", "Dec"};
        SimpleDateFormat ft = new SimpleDateFormat("M");
        InstanceStatPerMonth[] processStatPerMonths = new InstanceStatPerMonth[12];
        for (int i = 0; i < processStatPerMonths.length; i++) {
            processStatPerMonths[i] = new InstanceStatPerMonth();
            processStatPerMonths[i].setMonth(MONTHS[i]);
            processStatPerMonths[i].setCompletedInstances(0);
            processStatPerMonths[i].setStartedInstances(0);
        }

        //Get completed process instances
        List<HistoricProcessInstance> completedProcesses = BPMNOsgiServices.getHistoryService().
                createHistoricProcessInstanceQuery().finished().list();
        for (HistoricProcessInstance instance : completedProcesses) {
            int startTime = Integer.parseInt(ft.format(instance.getStartTime()));
            int endTime = Integer.parseInt(ft.format(instance.getEndTime()));
            processStatPerMonths[startTime - 1].setStartedInstances(processStatPerMonths[startTime - 1].getStartedInstances() + 1);
            processStatPerMonths[endTime - 1].setCompletedInstances(processStatPerMonths[endTime - 1].getCompletedInstances() + 1);

        }
        // Get active process instances
        List<HistoricProcessInstance> activeProcesses = BPMNOsgiServices.getHistoryService().
                createHistoricProcessInstanceQuery().unfinished().list();
        for (HistoricProcessInstance instance : activeProcesses) {
            int startTime = Integer.parseInt(ft.format(instance.getStartTime()));
            processStatPerMonths[startTime - 1].setStartedInstances(processStatPerMonths[startTime - 1].getStartedInstances() + 1);

        }

        return processStatPerMonths;
    }

    /**
     * Get all deployed processes
     * @return list with the processDefinitions of all deployed processes
     */
    @GET
    @Path("/allProcesses/")
    @Produces(MediaType.APPLICATION_JSON)
    public List<String> getAllProcesses() {
        //Get a list of the deployed processes
        List<ProcessDefinition> deployements = BPMNOsgiServices.getRepositoryService().
                createProcessDefinitionQuery().list();
        List<String> listOfProcesses = new ArrayList<>();
        for (ProcessDefinition processDefinition : deployements) {
            listOfProcesses.add(processDefinition.getId());
        }
        return listOfProcesses;
    }

}

