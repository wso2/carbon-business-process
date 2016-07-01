/**
 *  Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.bpmn.core.mgt.services;

import org.activiti.engine.HistoryService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricProcessInstanceQuery;
import org.activiti.engine.impl.RepositoryServiceImpl;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.task.TaskDefinition;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.runtime.ProcessInstanceQuery;
import org.activiti.image.impl.DefaultProcessDiagramGenerator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bpmn.core.BPMNServerHolder;
import org.wso2.carbon.bpmn.core.BPSFault;
import org.wso2.carbon.bpmn.core.mgt.model.BPMNInstance;
import org.wso2.carbon.bpmn.core.mgt.model.BPMNVariable;
import org.wso2.carbon.context.CarbonContext;
import sun.misc.BASE64Encoder;
import org.activiti.engine.delegate.Expression;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class BPMNInstanceService {

    private static Log log = LogFactory.getLog(BPMNInstanceService.class);
    private int processInstanceCount = -1;
    private int historyInstanceCount = -1;

    /**
     * Start process by process ID
     *
     * @param processID
     * @throws BPSFault
     */
    public void startProcess(String processID) throws BPSFault {

            ProcessEngine engine = BPMNServerHolder.getInstance().getEngine();
            RuntimeService runtimeService = engine.getRuntimeService();
            runtimeService.startProcessInstanceById(processID);
    }

    /**
     * Get All process instances
     *
     * @return list of BPMNInstances
     * @throws BPSFault
     */
    public BPMNInstance[] getProcessInstances() throws BPSFault {
        Integer tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        ProcessEngine engine = BPMNServerHolder.getInstance().getEngine();
        RuntimeService runtimeService = engine.getRuntimeService();
        List<ProcessInstance> instances = runtimeService.createProcessInstanceQuery().processInstanceTenantId(tenantId.toString()).list();
        BPMNInstance[] bpmnInstances = getTenantBPMNInstances(instances);
        return bpmnInstances;
    }

    /**
     * Returns Paginated Instances by passing filter parameters
     *
     * @param finished
     * @param instanceId
     * @param startAfter
     * @param startBefore
     * @param processId
     * @param isActive
     * @param variables
     * @param start
     * @param size
     * @return list of BPMNInstances for given filter parameters
     */
    public BPMNInstance[] getPaginatedInstanceByFilter(boolean finished, String instanceId,  Date startAfter,
                                                       Date startBefore, String processId, boolean isActive, String variables,
                                                       int start, int size) {
        List<BPMNInstance> bpmnInstanceList = new ArrayList<>();
        Integer tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        ProcessEngine engine = BPMNServerHolder.getInstance().getEngine();
        RuntimeService runtimeService = engine.getRuntimeService();
        ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery().processInstanceTenantId(tenantId.toString());
        HistoricProcessInstanceQuery historicQuery = BPMNServerHolder.getInstance().getEngine().getHistoryService()
                .createHistoricProcessInstanceQuery().processInstanceTenantId(tenantId.toString()).includeProcessVariables();
        query = query.includeProcessVariables();
        if (finished) {
            historicQuery = historicQuery.finished();
            if (instanceId != null && !instanceId.equals("")) {
                return getInstanceById(instanceId , finished);
            }
            if (processId != null && !processId.equals("")) {
                historicQuery = historicQuery.processDefinitionId(processId);
            }
            if (variables != null && !variables.trim().equals("")) {
                String variablePairs[] = variables.split(",");
                BPMNVariable[] bpmnVariables = new BPMNVariable[variablePairs.length];
                for (int i = 0; i < variablePairs.length; i++) {
                    String pair[] = variablePairs[i].split(":");
                    if (pair.length == 1) {
                        bpmnVariables[i] = new BPMNVariable(pair[0], "");
                    } else {
                        bpmnVariables[i] = new BPMNVariable(pair[0], pair[1]);
                    }
                }
                if(variablePairs != null && variablePairs.length > 0){
                    for(BPMNVariable variable: bpmnVariables){
                        if (variable.getName() != null && !variable.getName().equals("")) {
                            historicQuery = historicQuery
                                    .variableValueLike(variable.getName(), "%" + variable.getValue().toString() + "%");
                        }
                    }
                }
            }
            if (startAfter != null) {
                historicQuery = historicQuery.startedAfter(startAfter);
            }
            if (startBefore != null) {
                historicQuery = historicQuery.startedBefore(startBefore);
            }
            processInstanceCount = (int) historicQuery.count();
            List<HistoricProcessInstance> instances = historicQuery.listPage(start, size);
            for (HistoricProcessInstance instance: instances) {
                BPMNInstance bpmnInstance = new BPMNInstance();
                bpmnInstance.setInstanceId(instance.getId());
                bpmnInstance.setProcessId(instance.getProcessDefinitionId());
                List<ProcessDefinition> processes = BPMNServerHolder.getInstance().getEngine().getRepositoryService()
                        .createProcessDefinitionQuery().processDefinitionTenantId(tenantId.toString())
                        .processDefinitionId(instance.getProcessDefinitionId()).list();
                String processName = instance.getProcessDefinitionId();
                if (!processes.isEmpty()) {
                    processName = processes.get(0).getName();
                }
                bpmnInstance.setProcessName(processName);
                bpmnInstance.setStartTime(instance.getStartTime());
                bpmnInstance.setEndTime(instance.getEndTime());
                bpmnInstance.setVariables(formatVariables(instance.getProcessVariables()));
                bpmnInstanceList.add(bpmnInstance);
            }
        } else {
            historicQuery = historicQuery.unfinished();
            if (instanceId != null && !instanceId.equals("")) {
                return getInstanceById(instanceId, finished);
            }
            if (processId != null && !processId.equals("")) {
                historicQuery = historicQuery.processDefinitionId(processId);
            }
            if (variables != null && !variables.trim().equals("")) {
                String variablePairs[] = variables.split(",");
                BPMNVariable[] bpmnVariables = new BPMNVariable[variablePairs.length];
                for (int i = 0; i < variablePairs.length; i++) {
                    String pair[] = variablePairs[i].split(":");
                    if (pair.length == 1) {
                        bpmnVariables[i] = new BPMNVariable(pair[0], "");
                    } else {
                        bpmnVariables[i] = new BPMNVariable(pair[0], pair[1]);
                    }
                }
                if(variablePairs != null && variablePairs.length > 0){
                    for(BPMNVariable variable: bpmnVariables){
                        if (variable.getName() != null && !variable.getName().equals("")) {
                            historicQuery = historicQuery
                                    .variableValueLike(variable.getName(), "%" + variable.getValue().toString() + "%");
                        }
                    }
                }
            }
            if (startAfter != null) {
                historicQuery = historicQuery.startedAfter(startAfter);
            }
            if (startBefore != null) {
                historicQuery = historicQuery.startedBefore(startBefore);
            }
            processInstanceCount = (int) historicQuery.count();
            List<HistoricProcessInstance> instances = historicQuery.listPage(start, size);
            for (HistoricProcessInstance instance: instances) {
                boolean isSuspended = query.processInstanceId(instance.getId()).list().get(0).isSuspended();
                if( isSuspended == !isActive) {
                    BPMNInstance bpmnInstance = new BPMNInstance();
                    bpmnInstance.setInstanceId(instance.getId());
                    bpmnInstance.setProcessId(instance.getProcessDefinitionId());
                    List<ProcessDefinition> processes = BPMNServerHolder.getInstance().getEngine().getRepositoryService()
                            .createProcessDefinitionQuery().processDefinitionTenantId(tenantId.toString())
                            .processDefinitionId(instance.getProcessDefinitionId()).list();
                    String processName = instance.getProcessDefinitionId();
                    if (!processes.isEmpty()) {
                        processName = processes.get(0).getName();
                    }
                    bpmnInstance.setProcessName(processName);
                    if (!query.processInstanceId(instance.getId()).list().isEmpty()) {
                        bpmnInstance.setSuspended(isSuspended);
                    }
                    bpmnInstance.setStartTime(instance.getStartTime());
                    bpmnInstance.setEndTime(instance.getEndTime());
                    bpmnInstance.setVariables(formatVariables(instance.getProcessVariables()));
                    bpmnInstanceList.add(bpmnInstance);
                }
            }
        }
        return bpmnInstanceList.toArray(new BPMNInstance[bpmnInstanceList.size()]);
    }

    /**
     * Get instances by instance Id and state
     *
     * @param instanceId
     * @param finished
     * @return list of BPMNInstances
     */
    private BPMNInstance[] getInstanceById(String instanceId, boolean finished) {
        List<BPMNInstance> bpmnInstanceList = new ArrayList<>();
        Integer tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        ProcessEngine engine = BPMNServerHolder.getInstance().getEngine();
        RuntimeService runtimeService = engine.getRuntimeService();
        ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery().processInstanceTenantId(tenantId.toString());
        HistoricProcessInstanceQuery historicQuery = BPMNServerHolder.getInstance().getEngine().getHistoryService()
                .createHistoricProcessInstanceQuery().processInstanceTenantId(tenantId.toString()).includeProcessVariables();
        query = query.includeProcessVariables();
        if (finished) {
            historicQuery.finished();
        } else {
            historicQuery.unfinished();
        }
        historicQuery = historicQuery.processInstanceId(instanceId);
        HistoricProcessInstance instance = historicQuery.singleResult();
        if (instance != null) {
            processInstanceCount = 1;
            BPMNInstance bpmnInstance = new BPMNInstance();
            bpmnInstance.setInstanceId(instance.getId());
            bpmnInstance.setProcessId(instance.getProcessDefinitionId());
            List<ProcessDefinition> processes = BPMNServerHolder.getInstance().getEngine().getRepositoryService()
                    .createProcessDefinitionQuery().processDefinitionTenantId(tenantId.toString())
                    .processDefinitionId(instance.getProcessDefinitionId()).list();
            String processName = instance.getProcessDefinitionId();
            if (!processes.isEmpty()) {
                processName = processes.get(0).getName();
            }
            bpmnInstance.setProcessName(processName);
            if (!query.processInstanceId(instance.getId()).list().isEmpty()) {
                bpmnInstance.setSuspended(query.processInstanceId(instance.getId()).list().get(0).isSuspended());
            }
            bpmnInstance.setStartTime(instance.getStartTime());
            bpmnInstance.setEndTime(instance.getEndTime());
            bpmnInstance.setVariables(formatVariables(instance.getProcessVariables()));
            bpmnInstanceList.add(bpmnInstance);
        } else {
            processInstanceCount = 0;
        }
        return bpmnInstanceList.toArray(new BPMNInstance[bpmnInstanceList.size()]);
    }

    /**
     * Get paginated instances
     *
     * @param start
     * @param size
     * @return list of BPMNInstances
     * @throws BPSFault
     */
    public BPMNInstance[] getPaginatedInstances(int start, int size) throws BPSFault {
        List<BPMNInstance> bpmnInstanceList = new ArrayList<>();
        Integer tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        ProcessEngine engine = BPMNServerHolder.getInstance().getEngine();
        RuntimeService runtimeService = engine.getRuntimeService();
        ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery().processInstanceTenantId(tenantId.toString());
        HistoricProcessInstanceQuery historicQuery = BPMNServerHolder.getInstance().getEngine().getHistoryService()
                .createHistoricProcessInstanceQuery().processInstanceTenantId(tenantId.toString());
        processInstanceCount = (int) query.count();
        List<ProcessInstance> instances = query.includeProcessVariables().listPage(start, size);
        for (ProcessInstance instance: instances) {
            BPMNInstance bpmnInstance = new BPMNInstance();
            bpmnInstance.setInstanceId(instance.getId());
            bpmnInstance.setProcessId(instance.getProcessDefinitionId());
            bpmnInstance.setSuspended(instance.isSuspended());
            bpmnInstance.setStartTime(historicQuery.processInstanceId(instance.getId()).singleResult().getStartTime());
            bpmnInstance.setVariables(formatVariables(instance.getProcessVariables()));
            bpmnInstanceList.add(bpmnInstance);
        }
        return bpmnInstanceList.toArray(new BPMNInstance[bpmnInstanceList.size()]);
    }

    /**
     * Get total instance count
     *
     * @return count int
     * @throws BPSFault
     */
    public int getInstanceCount() throws BPSFault {
        if(processInstanceCount == -1) {
            Integer tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
            ProcessEngine engine = BPMNServerHolder.getInstance().getEngine();
            RuntimeService runtimeService = engine.getRuntimeService();
            ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery().processInstanceTenantId(tenantId.toString());
            processInstanceCount = (int) query.includeProcessVariables().count();
        }
        return processInstanceCount;
    }

    /**
     * Get paginated history instances
     *
     * @param start
     * @param size
     * @return list of BPMNInstances
     */
    public BPMNInstance[] getPaginatedHistoryInstances(int start, int size){
        BPMNInstance bpmnInstance;
        Integer tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        List<BPMNInstance> bpmnInstances = new ArrayList<>();
        HistoryService historyService = BPMNServerHolder.getInstance().getEngine().getHistoryService();
        HistoricProcessInstanceQuery query =
                historyService.createHistoricProcessInstanceQuery().processInstanceTenantId(tenantId.toString())
                        .finished().includeProcessVariables();
        historyInstanceCount = (int) query.count();
        List<HistoricProcessInstance> historicProcessInstances = query.listPage(start, size);
        for(HistoricProcessInstance instance: historicProcessInstances){
            bpmnInstance = new BPMNInstance();
            bpmnInstance.setInstanceId(instance.getId());
            bpmnInstance.setProcessId(instance.getProcessDefinitionId());
            bpmnInstance.setStartTime(instance.getStartTime());
            bpmnInstance.setEndTime(instance.getEndTime());
            bpmnInstance.setVariables(formatVariables(instance.getProcessVariables()));
            bpmnInstances.add(bpmnInstance);
        }
        return bpmnInstances.toArray(new BPMNInstance[bpmnInstances.size()]);
    }

    /**
     * Get total history instance count
     *
     * @return count int
     * @throws BPSFault
     */
    public int getHistoryInstanceCount() throws BPSFault {
        if(historyInstanceCount == -1){
            Integer tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
            HistoryService historyService = BPMNServerHolder.getInstance().getEngine().getHistoryService();
            HistoricProcessInstanceQuery query =
                    historyService.createHistoricProcessInstanceQuery().processInstanceTenantId(tenantId.toString()).finished();
            historyInstanceCount = (int) query.count();
        }
        return historyInstanceCount;
    }

    /**
     * Delete history instance by instance ID
     * @param instanceId
     */
    public void deleteHistoryInstance(String instanceId){
        HistoryService historyService = BPMNServerHolder.getInstance().getEngine().getHistoryService();
        historyService.deleteHistoricProcessInstance(instanceId);
    }

    /**
     * Delete all completed instances
     */
    public void deleteAllCompletedInstances(){
        Integer tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        HistoryService historyService = BPMNServerHolder.getInstance().getEngine().getHistoryService();
        HistoricProcessInstanceQuery query =
                historyService.createHistoricProcessInstanceQuery().processInstanceTenantId(tenantId.toString()).finished();
        for(HistoricProcessInstance instance: query.list()){
            historyService.deleteHistoricProcessInstance(instance.getId());
        }
    }

    /**
     * Get tenant history instances from a passed list
     *
     * @param instances
     * @return list of BPMNInstances
     */
    private BPMNInstance[] getTenantBPMNHistoryInstances(List<ProcessInstance> instances) {
        BPMNInstance bpmnInstance;
        Integer tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        List<BPMNInstance> bpmnInstances = new ArrayList<BPMNInstance>();
        RuntimeService runtimeService = BPMNServerHolder.getInstance().getEngine().getRuntimeService();
        HistoricProcessInstanceQuery query = BPMNServerHolder.getInstance().getEngine().getHistoryService()
                .createHistoricProcessInstanceQuery().processInstanceTenantId(tenantId.toString());
        for (ProcessInstance instance : instances) {
            bpmnInstance = new BPMNInstance();
            bpmnInstance.setInstanceId(instance.getId());
            bpmnInstance.setProcessId(instance.getProcessDefinitionId());
            bpmnInstance.setSuspended(instance.isSuspended());
            bpmnInstance.setStartTime(query.processInstanceId(instance.getId()).singleResult().getStartTime());
            bpmnInstance.setVariables(formatVariables(runtimeService.getVariables(instance.getId())));
            bpmnInstances.add(bpmnInstance);
        }
        return bpmnInstances.toArray(new BPMNInstance[bpmnInstances.size()]);
    }

    /**
     * Suspend process instance by instance ID
     *
     * @param instanceId
     * @throws BPSFault
     */
    public void suspendProcessInstance(String instanceId) throws BPSFault {
        Integer tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
            RuntimeService runtimeService = BPMNServerHolder.getInstance().getEngine().getRuntimeService();
            List<ProcessInstance> processInstances = runtimeService.createProcessInstanceQuery().processInstanceTenantId(tenantId.toString()).processInstanceId(instanceId).list();
            if (processInstances.isEmpty()) {
                String msg = "No process instances with the ID: " + instanceId;
                log.error(msg);
                throw new BPSFault(msg);
            }
            runtimeService.suspendProcessInstanceById(instanceId);
    }

    /**
     * Delete process instances by passing a list of IDs
     *
     * @param instanceIdSet
     * @throws BPSFault
     */
    public void deleteProcessInstanceSet(String[] instanceIdSet) throws BPSFault {
        for (String instanceId : instanceIdSet) {
            deleteProcessInstance(instanceId);
        }
    }

    /**
     * Delete process instance by passing instance ID
     *
     * @param instanceId
     * @throws BPSFault
     */
    public void deleteProcessInstance(String instanceId) throws BPSFault {
        Integer tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        RuntimeService runtimeService = BPMNServerHolder.getInstance().getEngine().getRuntimeService();
        List<ProcessInstance> processInstances = runtimeService.createProcessInstanceQuery()
                .processInstanceTenantId(tenantId.toString()).processInstanceId(instanceId).list();
        if (processInstances.isEmpty()) {
            HistoryService historyService = BPMNServerHolder.getInstance().getEngine().getHistoryService();
            List<HistoricProcessInstance> historicProcessInstances = historyService.createHistoricProcessInstanceQuery()
                    .processInstanceTenantId(tenantId.toString()).processInstanceId(instanceId).list();
            if (historicProcessInstances.isEmpty()) {
                String msg = "No process instances with the ID: " + instanceId;
                log.error(msg);
                throw new BPSFault(msg);
            }
            historyService.deleteHistoricProcessInstance(instanceId);
            return;
        }
        runtimeService.deleteProcessInstance(instanceId, "Deleted by user: " + tenantId);
    }

    /**
     * Activate a process instance by passing the instance id
     *
     * @param instanceId
     * @throws BPSFault
     */
    public void activateProcessInstance(String instanceId) throws BPSFault {
        Integer tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
            RuntimeService runtimeService = BPMNServerHolder.getInstance().getEngine().getRuntimeService();

            List<ProcessInstance> processInstances = runtimeService.createProcessInstanceQuery()
                    .processInstanceTenantId(tenantId.toString()).processInstanceId(instanceId).list();
            if (processInstances.isEmpty()) {
                String msg = "No process instances with the ID: " + instanceId;
                log.error(msg);
                throw new BPSFault(msg);
            }
            runtimeService.activateProcessInstanceById(instanceId);
    }

    /**
     * Get process instance diagram as a byte stream by passing the instance ID
     *
     * @param instanceId
     * @return Byte array String
     * @throws BPSFault
     */
    public String getProcessInstanceDiagram(String instanceId) throws BPSFault {
        Integer tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            RuntimeService runtimeService = BPMNServerHolder.getInstance().getEngine().getRuntimeService();
            RepositoryService repositoryService = BPMNServerHolder.getInstance().getEngine().getRepositoryService();

            List<ProcessInstance> processInstances = runtimeService.createProcessInstanceQuery()
                    .processInstanceTenantId(tenantId.toString()).processInstanceId(instanceId).list();
            if (processInstances.isEmpty()) {
                String msg = "No process instance diagram for ID: " + instanceId;
                log.info(msg);
                return null;
            }
            ProcessDefinitionEntity processDefinition = (ProcessDefinitionEntity) ((RepositoryServiceImpl) repositoryService)
                    .getDeployedProcessDefinition(processInstances.get(0).getProcessDefinitionId());
            if(processDefinition != null && processDefinition.isGraphicalNotationDefined()){
                InputStream diagramStream = new DefaultProcessDiagramGenerator().generateDiagram(repositoryService
                                .getBpmnModel(processDefinition.getId()), "png",
                        runtimeService.getActiveActivityIds(instanceId));
                BufferedImage bufferedImage = ImageIO.read(diagramStream);

                return encodeToString(bufferedImage, "png");
            }else {
                String msg = "Process definition graphical notations doesn't exists: " + instanceId;
                log.debug(msg);
            }
        } catch (IOException e) {
            String msg = "Failed to get the process instance.";
            log.error(msg, e);
        }
        return null;
    }

    /**
     * Internally used method to get all tenant BPMN instances from a passed instance list
     *
     * @param instances
     * @return list of BPMNInstances
     */
    private BPMNInstance[] getTenantBPMNInstances(List<ProcessInstance> instances) {
        BPMNInstance bpmnInstance;
        Integer tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        List<BPMNInstance> bpmnInstances = new ArrayList<BPMNInstance>();
        RuntimeService runtimeService = BPMNServerHolder.getInstance().getEngine().getRuntimeService();
        HistoricProcessInstanceQuery query = BPMNServerHolder.getInstance().getEngine().getHistoryService()
                .createHistoricProcessInstanceQuery().processInstanceTenantId(tenantId.toString());
        for (ProcessInstance instance : instances) {
            bpmnInstance = new BPMNInstance();
            bpmnInstance.setInstanceId(instance.getId());
            bpmnInstance.setProcessId(instance.getProcessDefinitionId());
            bpmnInstance.setSuspended(instance.isSuspended());
            bpmnInstance.setStartTime(query.processInstanceId(instance.getId()).singleResult().getStartTime());
            bpmnInstance.setVariables(formatVariables(runtimeService.getVariables(instance.getId())));
            bpmnInstances.add(bpmnInstance);
        }
        return bpmnInstances.toArray(new BPMNInstance[bpmnInstances.size()]);
    }

    /**
     * Internally used method to format variables
     *
     * @param processVariables
     * @return list of BPMNInstances
     */
    private BPMNVariable[] formatVariables(Map<String, Object> processVariables) {
        BPMNVariable[] vars = new BPMNVariable[processVariables.size()];
        int currentVar = 0;
        for (Map.Entry entry : processVariables.entrySet()) {
            Object value = processVariables.get(entry.getKey().toString());
            if (value == null) {
                value = "null";
            } else {
                value = String.valueOf(value);
            }
            vars[currentVar] = new BPMNVariable(entry.getKey().toString(), value.toString());
            currentVar++;
        }
        return vars;
    }

    /**
     * Internally used method to encode a image to String
     *
     * @param image
     * @param type
     * @return encoded String
     */
    private String encodeToString(BufferedImage image, String type) {
        String imageString = null;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            ImageIO.write(image, type, bos);
            byte[] imageBytes = bos.toByteArray();
            BASE64Encoder encoder = new BASE64Encoder();
            imageString = encoder.encode(imageBytes);
        } catch (IOException e) {
            log.error("Could not write image data", e);
        } finally {
            try {
                bos.close();
            } catch (IOException e) {
                log.error("Could not close the byte stream", e);
            }
        }
        return imageString;
    }

    /**
     *
     * @param instanceId
     * @return a 2D array of String containing information of current task(s) of an active BPMN instance
     */
    public String[][] getCurrentTaskInformation(String instanceId){
        Integer tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        RuntimeService runtimeService = BPMNServerHolder.getInstance().getEngine().getRuntimeService();
        RepositoryService repositoryService = BPMNServerHolder.getInstance().getEngine().getRepositoryService();

        List<ProcessInstance> processInstances = runtimeService.createProcessInstanceQuery()
                .processInstanceTenantId(tenantId.toString()).processInstanceId(instanceId).list();
        if (processInstances.isEmpty()) {
            String msg = "No current task information for ID: " + instanceId;
            log.info(msg);
            return null;
        }

        ProcessDefinitionEntity processDefinition = (ProcessDefinitionEntity) ((RepositoryServiceImpl) repositoryService)
                .getDeployedProcessDefinition(processInstances.get(0).getProcessDefinitionId());

        //get the ids of current task(s)
        List<String> ids = runtimeService.getActiveActivityIds(instanceId);
        Iterator<String> iterator = ids.iterator();

        Map<String, TaskDefinition> taskDefinitions = processDefinition.getTaskDefinitions();
        String currentTaskDefinitions[][] = new String[ids.size()][12];
        int count = 0;

        //for each current task
        while (iterator.hasNext()) {
            String key = iterator.next();
            TaskDefinition currentTaskDef = taskDefinitions.get(key);
            //currentTaskDefinitions will keep 12 properties of each task.
            //1. task key
            currentTaskDefinitions[count][0] = currentTaskDef.getKey();
            //2. task name
            currentTaskDefinitions[count][1] = currentTaskDef.getNameExpression().getExpressionText();
            //3. assignee
            currentTaskDefinitions[count][2] = (currentTaskDef.getAssigneeExpression() != null) ?
                    currentTaskDef.getAssigneeExpression().getExpressionText() : null;
            //4. description
            currentTaskDefinitions[count][3] = (currentTaskDef.getDescriptionExpression() != null) ?
                    currentTaskDef.getDescriptionExpression().getExpressionText() : null;

            //5. category
            currentTaskDefinitions[count][4] = (currentTaskDef.getCategoryExpression() != null) ?
                    currentTaskDef.getCategoryExpression().getExpressionText() : null;
            //6. due date
            currentTaskDefinitions[count][5] = (currentTaskDef.getDueDateExpression() != null) ?
                    currentTaskDef.getDueDateExpression().getExpressionText() : null;
            //7. form key
            currentTaskDefinitions[count][6] = (currentTaskDef.getFormKeyExpression() != null) ?
                    currentTaskDef.getFormKeyExpression().getExpressionText() : null;
            //8. owner
            currentTaskDefinitions[count][7] = (currentTaskDef.getOwnerExpression() != null) ?
                    currentTaskDef.getOwnerExpression().getExpressionText() : null;
            //9. priority
            currentTaskDefinitions[count][8] = (currentTaskDef.getPriorityExpression() != null) ?
                    currentTaskDef.getPriorityExpression().getExpressionText() : null;
            //10. skip
            currentTaskDefinitions[count][9] = (currentTaskDef.getSkipExpression() != null) ?
                    currentTaskDef.getSkipExpression().getExpressionText() : null;

            //11. candidate group ids
            String candidateGroupIds = "";
            if(currentTaskDef.getCandidateGroupIdExpressions().size() != 0){
                Iterator<Expression> candIterator = currentTaskDef.getCandidateGroupIdExpressions().iterator();
                while(candIterator.hasNext()) {
                    candidateGroupIds = candidateGroupIds.concat(candIterator.next().getExpressionText() + ", ");
                }
                currentTaskDefinitions[count][10] = candidateGroupIds;
            }else{
                currentTaskDefinitions[count][10] = null;
            }

            //12. candidate user ids
            String candidateUserIds = "";
            if(currentTaskDef.getCandidateUserIdExpressions().size() != 0){
                Iterator<Expression> userIterator = currentTaskDef.getCandidateUserIdExpressions().iterator();
                while(userIterator.hasNext()){
                    candidateUserIds = candidateUserIds.concat(userIterator.next().getExpressionText() + ", ");
                }
                currentTaskDefinitions[count][11] = candidateGroupIds;
            }else{
                currentTaskDefinitions[count][11] = null;
            }
            count++;
        }
        return currentTaskDefinitions;
    }
}
