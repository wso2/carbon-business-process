package org.wso2.carbon.bpmn.analytics.publisher;

import org.activiti.engine.HistoryService;
import org.activiti.engine.history.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bpmn.analytics.publisher.models.BPMNProcessInstance;
import org.wso2.carbon.bpmn.analytics.publisher.models.BPMNTaskInstance;
import org.wso2.carbon.bpmn.analytics.publisher.utils.BPMNXMLDataOperator;
import org.wso2.carbon.bpmn.analytics.publisher.utils.DateConverter;
import org.wso2.carbon.bpmn.core.BPMNServerHolder;

import org.wso2.carbon.bpmn.core.mgt.model.BPMNVariable;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by isuruwi on 6/23/15.
 */
public class AnalyticsPublishServiceUtils {
    private static Log log = LogFactory.getLog(AnalyticsPublishServiceUtils.class);
    private static AnalyticsPublishServiceUtils dasPublishServiceComp;

    private AnalyticsPublishServiceUtils(){

    }

    public static AnalyticsPublishServiceUtils getInstance(){
        if(dasPublishServiceComp == null){
            dasPublishServiceComp = new AnalyticsPublishServiceUtils();
        }
        return dasPublishServiceComp;
    }

    public BPMNProcessInstance[] getCompletedProcessInstances() {
        HistoryService historyService = BPMNServerHolder.getInstance().getEngine().getHistoryService();
        HistoricProcessInstanceQuery instanceQuery = historyService.createHistoricProcessInstanceQuery();
        List<HistoricProcessInstance> historicProcessInstanceList = null;
        String timeInXML = readLastCompletedProcessInstanceEndTimeFromXML();
        if (timeInXML == null) {
            if (instanceQuery.finished().list().size() != 0) {
                // if the time value is null in the xml file then send all completed process instances.
                historicProcessInstanceList = instanceQuery.finished().orderByProcessInstanceEndTime().asc().list();
            }
        } else {
            if (instanceQuery.finishedAfter(new DateConverter().convertStringToDate(timeInXML)).list().size() != 0) {
                //send the process instances which are finished after the given date/time in XML to publish the data
                historicProcessInstanceList = instanceQuery.finishedAfter(new DateConverter().convertStringToDate(timeInXML)).orderByProcessInstanceEndTime().asc().list();
            }
        }
        if (historicProcessInstanceList != null) {
            writeLastCompletedProcessInstanceEndTimeToXML(historicProcessInstanceList);
            //return ProcessInstances set as BPMNProcessInstance array
            return getBPMNProcessInstances(historicProcessInstanceList);
        }
        return null;
    }

    public BPMNTaskInstance[] getCompletedTasks() {
        HistoryService historyService = BPMNServerHolder.getInstance().getEngine().getHistoryService();
        HistoricTaskInstanceQuery taskInstanceQuery = historyService.createHistoricTaskInstanceQuery();
        List<HistoricTaskInstance> historicTaskInstanceList = null;
        String timeInXML = readLastCompletedTaskInstanceEndTimeFromXML();
        if (timeInXML == null) {
            if (taskInstanceQuery.finished().list().size() != 0) {
                historicTaskInstanceList = taskInstanceQuery.finished().orderByHistoricTaskInstanceEndTime().asc().list();
            }
        } else {
            if (taskInstanceQuery.finished().taskCompletedAfter(new DateConverter().convertStringToDate(timeInXML)).list().size() != 0) {
                historicTaskInstanceList = taskInstanceQuery.finished().taskCompletedAfter(new DateConverter().convertStringToDate(timeInXML)).orderByHistoricTaskInstanceEndTime().asc().list();
            }
        }
        if (historicTaskInstanceList != null) {
            writeLastCompletedTaskInstanceEndTimeToXML(historicTaskInstanceList);
            return getBPMNTaskInstances(historicTaskInstanceList);
        }
        return null;
    }

    private BPMNProcessInstance[] getBPMNProcessInstances(List<HistoricProcessInstance> historicProcessInstanceList) {
        BPMNProcessInstance bpmnProcessInstance;
        List<BPMNProcessInstance> bpmnProcessInstances = new ArrayList<>();
        for (HistoricProcessInstance instance : historicProcessInstanceList) {
            bpmnProcessInstance = new BPMNProcessInstance();
            bpmnProcessInstance.setProcessDefinitionId(instance.getProcessDefinitionId());
            bpmnProcessInstance.setTenantId(instance.getTenantId());
            bpmnProcessInstance.setName(instance.getName());
            bpmnProcessInstance.setInstanceId(instance.getId());
            bpmnProcessInstance.setBusinessKey(instance.getBusinessKey());
            bpmnProcessInstance.setStartTime(instance.getStartTime());
            bpmnProcessInstance.setEndTime(instance.getEndTime());
            bpmnProcessInstance.setDuration(instance.getDurationInMillis());
            bpmnProcessInstance.setStartUserId(instance.getStartUserId());
            bpmnProcessInstance.setStartActivityId(instance.getStartActivityId());
            bpmnProcessInstance.setVariables(formatVariables(instance.getProcessVariables()));
            bpmnProcessInstances.add(bpmnProcessInstance);
        }
        return bpmnProcessInstances.toArray(new BPMNProcessInstance[bpmnProcessInstances.size()]);
    }

    private BPMNTaskInstance[] getBPMNTaskInstances(List<HistoricTaskInstance> historicTaskInstanceList) {
        BPMNTaskInstance bpmnTaskInstance;
        List<BPMNTaskInstance> bpmnTaskInstances = new ArrayList<>();
        for (HistoricTaskInstance taskInstance : historicTaskInstanceList) {
            bpmnTaskInstance = new BPMNTaskInstance();
            bpmnTaskInstance.setTaskDefinitionKey(taskInstance.getTaskDefinitionKey());
            bpmnTaskInstance.setTaskInstanceId(taskInstance.getId());
            bpmnTaskInstance.setAssignee(taskInstance.getAssignee());
            //claim time is not stored in the DB
            //bpmnTaskInstance.setClaimTime(taskInstance.getClaimTime());
            bpmnTaskInstance.setStartTime(taskInstance.getStartTime());
            bpmnTaskInstance.setEndTime(taskInstance.getEndTime());
            bpmnTaskInstance.setTaskName(taskInstance.getName());
            bpmnTaskInstance.setDurationInMills(taskInstance.getDurationInMillis());
            //bpmnTaskInstance.setWorkTimeInMills(taskInstance.getWorkTimeInMillis());
            bpmnTaskInstance.setCreateTime(taskInstance.getCreateTime());
            bpmnTaskInstance.setOwner(taskInstance.getOwner());
            bpmnTaskInstance.setProcessInstanceId(taskInstance.getProcessInstanceId());
            bpmnTaskInstances.add(bpmnTaskInstance);
        }
        return bpmnTaskInstances.toArray(new BPMNTaskInstance[bpmnTaskInstances.size()]);
    }

    private BPMNVariable[] formatVariables(Map<String, Object> processVariables) {
        if (processVariables == null) {
            return null;
        }
        BPMNVariable[] vars = new BPMNVariable[processVariables.size()];
        int currentVar = 0;
        for (Map.Entry entry : processVariables.entrySet()) {
            vars[currentVar] = new BPMNVariable(entry.getKey().toString(), processVariables.get(entry.getKey().toString()).toString());
            currentVar++;
        }
        return vars;
    }

    private void writeLastCompletedProcessInstanceEndTimeToXML(List<HistoricProcessInstance> historicProcessInstanceList) {
        Date lastProcessInstanceDate = historicProcessInstanceList.get(historicProcessInstanceList.size() - 1).getEndTime();
        BPMNXMLDataOperator.getInstance(BPMNConstants.PROCESS_INSTANCE).timeWriteToXML(lastProcessInstanceDate, BPMNConstants.LAST_PROCESS_INSTANCE_END_TIME);
    }

    private String readLastCompletedProcessInstanceEndTimeFromXML() {
        BPMNXMLDataOperator dataOperator = BPMNXMLDataOperator.getInstance(BPMNConstants.PROCESS_INSTANCE);
        return dataOperator.timeReadFromXML(BPMNConstants.LAST_PROCESS_INSTANCE_END_TIME);
    }

    private void writeLastCompletedTaskInstanceEndTimeToXML(List<HistoricTaskInstance> historicTaskInstanceList) {
        Date lastTaskInstanceDate = historicTaskInstanceList.get(historicTaskInstanceList.size() - 1).getEndTime();
        BPMNXMLDataOperator.getInstance(BPMNConstants.TASK_INSTANCE).timeWriteToXML(lastTaskInstanceDate, BPMNConstants.LAST_TASK_INSTANCE_END_TIME);
    }

    private String readLastCompletedTaskInstanceEndTimeFromXML() {
        BPMNXMLDataOperator dataOperator = BPMNXMLDataOperator.getInstance(BPMNConstants.TASK_INSTANCE);
        return dataOperator.timeReadFromXML(BPMNConstants.LAST_TASK_INSTANCE_END_TIME);
    }

}
