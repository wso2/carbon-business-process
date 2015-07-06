/*
 * Copyright (c) , WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.bpmn.analytics.publisher.utils;

import org.activiti.engine.HistoryService;
import org.activiti.engine.history.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bpmn.analytics.publisher.AnalyticsPublisherConstants;
import org.wso2.carbon.bpmn.analytics.publisher.models.BPMNProcessInstance;
import org.wso2.carbon.bpmn.analytics.publisher.models.BPMNTaskInstance;
import org.wso2.carbon.bpmn.core.BPMNServerHolder;

import org.wso2.carbon.bpmn.core.mgt.model.BPMNVariable;
import org.wso2.carbon.registry.api.Registry;
import org.wso2.carbon.registry.api.RegistryException;
import org.wso2.carbon.registry.api.Resource;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * AnalyticsPublishServiceUtils is used by the AnalyticsPublisher to fetch the BPMN process instances and the task instances
 */
public class AnalyticsPublishServiceUtils {
	private static Log log = LogFactory.getLog(AnalyticsPublishServiceUtils.class);

	/**
	 * Get completed process instances which were finished after the given date and time
	 *
	 * @return BPMNProcessInstance array if the historic process instance list is not null
	 */
	public BPMNProcessInstance[] getCompletedProcessInstances() {
		HistoryService historyService =
				BPMNServerHolder.getInstance().getEngine().getHistoryService();
		HistoricProcessInstanceQuery instanceQuery =
				historyService.createHistoricProcessInstanceQuery();
		List<HistoricProcessInstance> historicProcessInstanceList = null;
		String timeInXML = readLastCompletedProcessInstanceEndTimeFromRegistry();
		if (timeInXML == null) {
			if (instanceQuery.finished().list().size() != 0) {
				// if the time value is null in the xml file then send all completed process instances.
				historicProcessInstanceList =
						instanceQuery.finished().orderByProcessInstanceEndTime().asc().list();
			}
		} else {
			if (instanceQuery.finishedAfter(DateConverter.convertStringToDate(timeInXML)).list()
			                 .size() != 0) {
				//send the process instances which are finished after the given date/time in XML
				historicProcessInstanceList =
						instanceQuery.finishedAfter(DateConverter.convertStringToDate(timeInXML))
						             .orderByProcessInstanceEndTime().asc().list();
			}
		}
		if (historicProcessInstanceList != null) {
			writeLastCompletedProcessInstanceEndTimeToRegistry(historicProcessInstanceList);
			//return ProcessInstances set as BPMNProcessInstance array
			return getBPMNProcessInstances(historicProcessInstanceList);
		}
		return null;
	}

	/**
	 * Get completed task instances which were finished after the given date and time
	 *
	 * @return BPMNTaskInstance array if the historic task instance list is not null
	 */
	public BPMNTaskInstance[] getCompletedTasks() {
		HistoryService historyService =
				BPMNServerHolder.getInstance().getEngine().getHistoryService();
		HistoricTaskInstanceQuery taskInstanceQuery =
				historyService.createHistoricTaskInstanceQuery();
		List<HistoricTaskInstance> historicTaskInstanceList = null;
		String timeInXML = readLastCompletedTaskInstanceEndTimeFromRegistry();
		if (timeInXML == null) {
			if (taskInstanceQuery.finished().list().size() != 0) {
				historicTaskInstanceList =
						taskInstanceQuery.finished().orderByHistoricTaskInstanceEndTime().asc()
						                 .list();
			}
		} else {
			if (taskInstanceQuery.finished()
			                     .taskCompletedAfter(DateConverter.convertStringToDate(timeInXML))
			                     .list().size() != 0) {
				historicTaskInstanceList = taskInstanceQuery.finished().taskCompletedAfter(
						DateConverter.convertStringToDate(timeInXML))
				                                            .orderByHistoricTaskInstanceEndTime()
				                                            .asc().list();
			}
		}
		if (historicTaskInstanceList != null) {
			writeLastCompletedTaskInstanceEndTimeToRegistry(historicTaskInstanceList);
			return getBPMNTaskInstances(historicTaskInstanceList);
		}
		return null;
	}

	/**
	 * Convert historic process instances to BPMN process instances
	 *
	 * @param historicProcessInstanceList List of historic process instances
	 * @return BPMNProcessInstance array
	 */
	private BPMNProcessInstance[] getBPMNProcessInstances(
			List<HistoricProcessInstance> historicProcessInstanceList) {
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

	/**
	 * convert historic task instances to BPMN task instances
	 *
	 * @param historicTaskInstanceList List of historic task instances
	 * @return BPMNTaskInstance array
	 */
	private BPMNTaskInstance[] getBPMNTaskInstances(
			List<HistoricTaskInstance> historicTaskInstanceList) {
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

	/**
	 * Format the process instance variables as BPMNVariable objects
	 *
	 * @param processVariables variables which belongs to a given process instance as key, value pairs
	 * @return BPMNVariable objects array
	 */
	private BPMNVariable[] formatVariables(Map<String, Object> processVariables) {
		if (processVariables == null) {
			return null;
		}
		BPMNVariable[] vars = new BPMNVariable[processVariables.size()];
		int currentVar = 0;
		for (Map.Entry entry : processVariables.entrySet()) {
			vars[currentVar] = new BPMNVariable(entry.getKey().toString(),
			                                    processVariables.get(entry.getKey().toString())
			                                                    .toString());
			currentVar++;
		}
		return vars;
	}

	/**
	 * Write last completed process instance end time to carbon registry
	 *
	 * @param historicProcessInstanceList List of historic process instances
	 */
	private void writeLastCompletedProcessInstanceEndTimeToRegistry(
			List<HistoricProcessInstance> historicProcessInstanceList) {
		Date lastProcessInstanceDate =
				historicProcessInstanceList.get(historicProcessInstanceList.size() - 1)
				                           .getEndTime();
		try {
			Registry registry = BPMNServerHolder.getInstance().getRegistryService().getRegistry();
			Resource resource = registry.newResource();
			resource.setProperty(AnalyticsPublisherConstants.LAST_PROCESS_INSTANCE_END_TIME,
			                     lastProcessInstanceDate.toString());
			registry.put(AnalyticsPublisherConstants.PROCESS_RESOURCE_PATH, resource);
		} catch (RegistryException e) {
			String errMsg = "Registry error while writing the process instance end time.";
			log.error(errMsg, e);
		}
	}

	/**
	 * Write last completed task instance end time to carbon registry
	 *
	 * @param historicTaskInstanceList List of historic task instances
	 */
	private void writeLastCompletedTaskInstanceEndTimeToRegistry(
			List<HistoricTaskInstance> historicTaskInstanceList) {
		Date lastTaskInstanceDate =
				historicTaskInstanceList.get(historicTaskInstanceList.size() - 1).getEndTime();
		try {
			Registry registry = BPMNServerHolder.getInstance().getRegistryService().getRegistry();
			Resource resource = registry.newResource();
			resource.setProperty(AnalyticsPublisherConstants.LAST_TASK_INSTANCE_END_TIME,
			                     lastTaskInstanceDate.toString());
			registry.put(AnalyticsPublisherConstants.TASK_RESOURCE_PATH, resource);
		} catch (RegistryException e) {
			String errMsg = "Registry error while writing the task instance end time.";
			log.error(errMsg, e);
		}
	}

	/**
	 * Read last completed process instance end time from carbon registry
	 *
	 * @return the end time of last completed process instance
	 */
	private String readLastCompletedProcessInstanceEndTimeFromRegistry() {
		String time = null;
		try {
			Registry registry = BPMNServerHolder.getInstance().getRegistryService().getRegistry();
			Resource resource = registry.get(AnalyticsPublisherConstants.PROCESS_RESOURCE_PATH);
			time = resource.getProperty(AnalyticsPublisherConstants.LAST_PROCESS_INSTANCE_END_TIME);
		} catch (RegistryException e) {
			String errMsg = "Registry error while reading the process instance end time.";
			log.error(errMsg, e);
		}
		return time;
	}

	/**
	 * Read last completed task instance end time from carbon registry
	 *
	 * @return the end time of last completed task instance
	 */
	private String readLastCompletedTaskInstanceEndTimeFromRegistry() {
		String time = null;
		try {
			Registry registry = BPMNServerHolder.getInstance().getRegistryService().getRegistry();
			Resource resource = registry.get(AnalyticsPublisherConstants.TASK_RESOURCE_PATH);
			time = resource.getProperty(AnalyticsPublisherConstants.LAST_TASK_INSTANCE_END_TIME);
		} catch (RegistryException e) {
			String errMsg = "Registry error while reading the task instance end time.";
			log.error(errMsg, e);
		}
		return time;
	}
}
