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

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class BPMNInstanceService {

    private static Log log = LogFactory.getLog(BPMNInstanceService.class);
    private int processInstanceCount = -1;
    private int historyInstanceCount = -1;

    public void startProcess(String processID) throws BPSFault {

            ProcessEngine engine = BPMNServerHolder.getInstance().getEngine();
            RuntimeService runtimeService = engine.getRuntimeService();
            runtimeService.startProcessInstanceById(processID);
    }

    public BPMNInstance[] getProcessInstances() throws BPSFault {
        Integer tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        ProcessEngine engine = BPMNServerHolder.getInstance().getEngine();
        RuntimeService runtimeService = engine.getRuntimeService();
        List<ProcessInstance> instances = runtimeService.createProcessInstanceQuery().processInstanceTenantId(tenantId.toString()).list();
        BPMNInstance[] bpmnInstances = getTenantBPMNInstances(instances);
        return bpmnInstances;
    }

    public BPMNInstance[] getPaginatedInstanceByFilter(boolean finished, String instanceId,  Date startAfter,
                                                       Date startBefore, String processId, String variable,
                                                       String value, int start, int size) {
        List<BPMNInstance> bpmnInstanceList = new ArrayList<>();
        Integer tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        ProcessEngine engine = BPMNServerHolder.getInstance().getEngine();
        RuntimeService runtimeService = engine.getRuntimeService();
        ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery().processInstanceTenantId(tenantId.toString());
        HistoricProcessInstanceQuery historicQuery = BPMNServerHolder.getInstance().getEngine().getHistoryService()
                .createHistoricProcessInstanceQuery().processInstanceTenantId(tenantId.toString()).includeProcessVariables();
        query = query.includeProcessVariables();
        if (instanceId != null && !instanceId.equals("")) {
            historicQuery = historicQuery.processInstanceId(instanceId);
            processInstanceCount = 1;
            HistoricProcessInstance instance = historicQuery.singleResult();
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
        } else if (finished) {
            historicQuery = historicQuery.finished();
            if (processId != null && !processId.equals("")) {
                historicQuery = historicQuery.processDefinitionId(processId);
            }
            if (variable != null && !variable.equals("") && value != null && !value.equals("")) {
                historicQuery = historicQuery.variableValueLike(variable, "%" + value + "%");
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
            if (processId != null && !processId.equals("")) {
                historicQuery = historicQuery.processDefinitionId(processId);
            }
            if (variable != null && !variable.equals("") && value != null && !value.equals("")) {
                historicQuery = historicQuery.variableValueLike(variable, "%" + value + "%");
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
                if (!query.processInstanceId(instance.getId()).list().isEmpty()) {
                    bpmnInstance.setSuspended(query.processInstanceId(instance.getId()).list().get(0).isSuspended());
                }
                bpmnInstance.setStartTime(instance.getStartTime());
                bpmnInstance.setEndTime(instance.getEndTime());
                bpmnInstance.setVariables(formatVariables(instance.getProcessVariables()));
                bpmnInstanceList.add(bpmnInstance);
            }
        }
        return bpmnInstanceList.toArray(new BPMNInstance[bpmnInstanceList.size()]);
    }

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

    public void deleteHistoryInstance(String instanceId){
        HistoryService historyService = BPMNServerHolder.getInstance().getEngine().getHistoryService();
        historyService.deleteHistoricProcessInstance(instanceId);
    }

    public void deleteAllCompletedInstances(){
        Integer tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        HistoryService historyService = BPMNServerHolder.getInstance().getEngine().getHistoryService();
        HistoricProcessInstanceQuery query =
                historyService.createHistoricProcessInstanceQuery().processInstanceTenantId(tenantId.toString()).finished();
        for(HistoricProcessInstance instance: query.list()){
            historyService.deleteHistoricProcessInstance(instance.getId());
        }
    }

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

    public void deleteProcessInstanceSet(String[] instanceIdSet) throws BPSFault {
        for (String instanceId : instanceIdSet) {
            deleteProcessInstance(instanceId);
        }
    }

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

    public String getProcessInstanceDiagram(String instanceId) throws BPSFault {
        Integer tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            RuntimeService runtimeService = BPMNServerHolder.getInstance().getEngine().getRuntimeService();
            RepositoryService repositoryService = BPMNServerHolder.getInstance().getEngine().getRepositoryService();

            List<ProcessInstance> processInstances = runtimeService.createProcessInstanceQuery()
                    .processInstanceTenantId(tenantId.toString()).processInstanceId(instanceId).list();
            if (processInstances.isEmpty()) {
                String msg = "No process instances with the ID: " + instanceId;
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

    private BPMNVariable[] formatVariables(Map<String, Object> processVariables) {
        BPMNVariable[] vars = new BPMNVariable[processVariables.size()];
        int currentVar = 0;
        for (Map.Entry entry : processVariables.entrySet()) {
            vars[currentVar] = new BPMNVariable(entry.getKey().toString(), processVariables.get(entry.getKey().toString()).toString());
            currentVar++;
        }
        return vars;
    }

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
}
