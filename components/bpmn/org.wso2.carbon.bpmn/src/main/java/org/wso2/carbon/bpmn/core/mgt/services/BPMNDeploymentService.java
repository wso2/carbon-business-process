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
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.DeploymentQuery;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.repository.ProcessDefinitionQuery;
import org.activiti.engine.runtime.ProcessInstance;
import org.apache.axiom.util.base64.Base64Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bpmn.core.BPMNConstants;
import org.wso2.carbon.bpmn.core.BPMNServerHolder;
import org.wso2.carbon.bpmn.core.BPSFault;
import org.wso2.carbon.bpmn.core.deployment.TenantRepository;
import org.wso2.carbon.bpmn.core.mgt.model.BPMNDeletableInstances;
import org.wso2.carbon.bpmn.core.mgt.model.BPMNDeployment;
import org.wso2.carbon.bpmn.core.mgt.model.BPMNProcess;
import org.wso2.carbon.bpmn.core.utils.BPMNActivitiConfiguration;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.registry.api.Registry;
import org.wso2.carbon.registry.api.RegistryException;
import org.wso2.carbon.registry.api.RegistryService;
import org.wso2.carbon.registry.api.Resource;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class BPMNDeploymentService {

    private static Log log = LogFactory.getLog(BPMNDeploymentService.class);
    private int deploymentCount = -1;
    private int maximumDeleteCount = BPMNConstants.ACTIVITI_INSTANCE_MAX_DELETE_COUNT;

    public BPMNDeploymentService(){
        initializeVariable();
    }

    public BPMNProcess[] getDeployedProcesses() throws BPSFault {
        Integer tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        TenantRepository tenantRepository = BPMNServerHolder.getInstance().getTenantManager().getTenantRepository(tenantId);
        List<ProcessDefinition> processDefinitions = tenantRepository.getDeployedProcessDefinitions();

        BPMNProcess[] bpmnProcesses = new BPMNProcess[processDefinitions.size()];
        for (int i = 0; i < processDefinitions.size(); i++) {
            ProcessDefinition def = processDefinitions.get(i);
            BPMNProcess bpmnProcess = new BPMNProcess();
            bpmnProcess.setProcessId(def.getId());
            bpmnProcess.setDeploymentId(def.getDeploymentId());
            bpmnProcess.setKey(def.getKey());
            bpmnProcess.setVersion(def.getVersion());
            bpmnProcesses[i] = bpmnProcess;
        }
        return bpmnProcesses;

    }

    public BPMNProcess getProcessById(String processId) {
        Integer tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        ProcessEngine engine = BPMNServerHolder.getInstance().getEngine();
        ProcessDefinitionQuery query = engine.getRepositoryService().createProcessDefinitionQuery();
        ProcessDefinition process = query.processDefinitionTenantId(tenantId.toString())
                .processDefinitionId(processId).singleResult();
        DeploymentQuery deploymentQuery = engine.getRepositoryService().createDeploymentQuery();
        Deployment deployment = deploymentQuery.deploymentId(process.getDeploymentId()).singleResult();
        BPMNProcess bpmnProcess = new BPMNProcess();
        bpmnProcess.setDeploymentId(process.getDeploymentId());
        bpmnProcess.setName(process.getName());
        bpmnProcess.setKey(process.getKey());
        bpmnProcess.setProcessId(process.getId());
        bpmnProcess.setVersion(process.getVersion());
        bpmnProcess.setDeploymentTime(deployment.getDeploymentTime());
        bpmnProcess.setDeploymentName(deployment.getName());
        return bpmnProcess;
    }

    public BPMNProcess[] getProcessesByDeploymentId(String deploymentId) {
        List<BPMNProcess> bpmnProcesses = new ArrayList<>();
        Integer tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        ProcessEngine engine = BPMNServerHolder.getInstance().getEngine();
        ProcessDefinitionQuery query = engine.getRepositoryService().createProcessDefinitionQuery();
        DeploymentQuery deploymentQuery = engine.getRepositoryService().createDeploymentQuery();
        Deployment deployment = deploymentQuery.deploymentId(deploymentId).singleResult();
        List<ProcessDefinition> processes = query.processDefinitionTenantId(tenantId.toString())
                .deploymentId(deploymentId).list();
        for(ProcessDefinition process: processes){
            BPMNProcess bpmnProcess = new BPMNProcess();
            bpmnProcess.setDeploymentId(process.getDeploymentId());
            bpmnProcess.setName(process.getName());
            bpmnProcess.setKey(process.getKey());
            bpmnProcess.setProcessId(process.getId());
            bpmnProcess.setVersion(process.getVersion());
            bpmnProcess.setDeploymentTime(deployment.getDeploymentTime());
            bpmnProcess.setDeploymentName(deployment.getName());
            bpmnProcesses.add(bpmnProcess);
        }
        return bpmnProcesses.toArray(new BPMNProcess[bpmnProcesses.size()]);
    }

    public BPMNDeployment[] getDeployments() {
        List<BPMNDeployment> bpmnDeploymentList = new ArrayList<>();
        Integer tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        DeploymentQuery query = BPMNServerHolder.getInstance().getEngine().getRepositoryService().createDeploymentQuery();
        query = query.deploymentTenantId(tenantId.toString());
        List<Deployment> deployments = query.list();
        for(Deployment deployment: deployments){
            BPMNDeployment bpmnDeployment = new BPMNDeployment();
            bpmnDeployment.setDeploymentId(deployment.getId());
            bpmnDeployment.setDeploymentName(deployment.getName());
            bpmnDeployment.setDeploymentTime(deployment.getDeploymentTime());
            bpmnDeploymentList.add(bpmnDeployment);
        }
        return bpmnDeploymentList.toArray(new BPMNDeployment[bpmnDeploymentList.size()]);
    }

    /**
     * Get the deployments for given package name , order by deploymentID
     *
     * @param deploymentName
     * @return
     */
    public BPMNDeployment[] getDeploymentsByName(String deploymentName) {
        List<BPMNDeployment> bpmnDeploymentList = new ArrayList<>();
        Integer tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        DeploymentQuery query = BPMNServerHolder.getInstance().getEngine().getRepositoryService()
                .createDeploymentQuery();
        // Set deployment name and order by ID
        query = query.deploymentTenantId(tenantId.toString()).deploymentName(deploymentName).orderByDeploymentId()
                .desc();
        List<Deployment> deployments = query.list();
        for (Deployment deployment : deployments) {
            BPMNDeployment bpmnDeployment = new BPMNDeployment();
            bpmnDeployment.setDeploymentId(deployment.getId());
            bpmnDeployment.setDeploymentName(deployment.getName());
            bpmnDeployment.setDeploymentTime(deployment.getDeploymentTime());
            bpmnDeploymentList.add(bpmnDeployment);
        }
        return bpmnDeploymentList.toArray(new BPMNDeployment[bpmnDeploymentList.size()]);
    }

    public BPMNDeployment[] getPaginatedDeploymentsByFilter(String method, String filter, int start, int size) {
        List<BPMNDeployment> bpmnDeploymentList = new ArrayList<>();
        Integer tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        DeploymentQuery query = BPMNServerHolder.getInstance().getEngine().getRepositoryService().createDeploymentQuery();
        query = query.deploymentTenantId(tenantId.toString());
        if(filter != null && !filter.equals("") && method != null && !method.equals("")){
            if(method.equals("byDeploymentNameLike")){
                query = query.deploymentNameLike("%" + filter + "%");
            } else {
                query = query.processDefinitionKeyLike("%" + filter + "%");
            }
        }
        deploymentCount = (int) query.count();
        List<Deployment> deployments = query.listPage(start, size);
        for(Deployment deployment: deployments){
            BPMNDeployment bpmnDeployment = new BPMNDeployment();
            bpmnDeployment.setDeploymentId(deployment.getId());
            bpmnDeployment.setDeploymentName(deployment.getName());
            bpmnDeployment.setDeploymentTime(deployment.getDeploymentTime());
            bpmnDeploymentList.add(bpmnDeployment);
        }
        return bpmnDeploymentList.toArray(new BPMNDeployment[bpmnDeploymentList.size()]);
    }

    public int getDeploymentCount() throws BPSFault {
        if (deploymentCount == -1) {
            Integer tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
            DeploymentQuery query = BPMNServerHolder.getInstance().getEngine().getRepositoryService().createDeploymentQuery();
            deploymentCount = (int) query.deploymentTenantId(tenantId.toString()).count();
        }
        return deploymentCount;
    }

    public String getProcessDiagram(String processId) throws BPSFault {
        Integer tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            RepositoryService repositoryService = BPMNServerHolder.getInstance().getEngine().getRepositoryService();
            ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionTenantId(tenantId.toString())
                    .processDefinitionId(processId)
                    .singleResult();
            String diagramResourceName = processDefinition.getDiagramResourceName();
            InputStream imageStream = repositoryService.getResourceAsStream(processDefinition.getDeploymentId(), diagramResourceName);
            BufferedImage bufferedImage = ImageIO.read(imageStream);
            return encodeToString(bufferedImage, "png");
        }
        catch (IOException e) {
            String msg = "Failed to create the diagram for process: " + processId;
//            log.error(msg, e);
            throw new BPSFault(msg, e);
        }
    }

    public String getProcessModel(String processId) throws BPSFault {
        Integer tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        BufferedReader br = null;
        try {
            RepositoryService repositoryService = BPMNServerHolder.getInstance().getEngine().getRepositoryService();
            ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionTenantId(tenantId.toString())
                    .processDefinitionId(processId)
                    .singleResult();
            InputStream stream = repositoryService.getProcessModel(processDefinition.getId());
            br = new BufferedReader(new InputStreamReader(stream, Charset.defaultCharset()));
            StringBuilder sb = new StringBuilder();
            String line;

            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();

        } catch (IOException e) {

            String msg = "Failed to create the diagram for process: " + processId;
            log.error(msg, e);
            throw new BPSFault(msg, e);

        } finally {
            try {
                if (br != null) {
                    br.close();
                }
            } catch (IOException e) {
                log.error("Could not close the reader", e);
            }
        }
    }

    public void undeploy (String deploymentName ) throws BPSFault {

        Integer tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();

        ProcessEngine processEngine = BPMNServerHolder.getInstance().getEngine();
        DeploymentQuery query = processEngine.getRepositoryService().createDeploymentQuery();
        query = query.deploymentTenantId(tenantId.toString());
        query = query.deploymentNameLike("%" + deploymentName + "%");
        int deploymentCount = (int) query.count();

        log.info("Package " + deploymentName + " id going to be undeployed for the deployment count : " + deploymentCount);
        BPMNDeletableInstances bpmnDeletableInstances = new BPMNDeletableInstances();
        bpmnDeletableInstances.setTenantId(tenantId);

        List<Deployment> deployments = query.listPage(0, deploymentCount+1);
        for(Deployment deployment: deployments){
            aggregateRemovableProcessInstances(bpmnDeletableInstances, deployment.getId(), tenantId, processEngine);
        }

        if( (bpmnDeletableInstances.getActiveInstanceCount() + bpmnDeletableInstances.getCompletedInstanceCount()) > maximumDeleteCount){
            String errorMessage = " Failed to un deploy the package. Please delete the instances before un deploying " +
                    "the package";
            throw  new BPSFault(errorMessage, new Exception(errorMessage));
        }

        deleteInstances(bpmnDeletableInstances, processEngine);
        TenantRepository tenantRepository = BPMNServerHolder.getInstance().getTenantManager().getTenantRepository(tenantId);
        tenantRepository.undeploy(deploymentName, false);
    }


    private void aggregateRemovableProcessInstances(BPMNDeletableInstances bpmnDeletableInstances, String
            deploymentId, Integer tenantId, ProcessEngine processEngine) throws BPSFault {
        ProcessDefinitionQuery query = processEngine.getRepositoryService().createProcessDefinitionQuery();
        List<ProcessDefinition> processes = query.processDefinitionTenantId(tenantId.toString())
                .deploymentId(deploymentId).list();

        for(ProcessDefinition process: processes){
            if( !constructBPMNInstancesByProcessID(bpmnDeletableInstances, process.getId(), tenantId, processEngine) ){
                String errorMessage = " Failed to undeploy the package. Please delete the instances before undeploying " +
                        "the package";
                throw  new BPSFault(errorMessage);
            }
        }
    }

    private boolean constructBPMNInstancesByProcessID(BPMNDeletableInstances bpmnDeletableInstances, String processId, Integer
            tenantId, ProcessEngine processEngine){

        //first going to get the instances list of unfinished instances
        HistoricProcessInstanceQuery runtimeQuery = processEngine.getHistoryService()
                .createHistoricProcessInstanceQuery().processInstanceTenantId(tenantId.toString())
                .includeProcessVariables().unfinished().processDefinitionId(processId);
        int processInstanceCount = (int) runtimeQuery.count();
        bpmnDeletableInstances.setActiveInstanceCount(processInstanceCount);

        if(bpmnDeletableInstances.getActiveInstanceCount() > maximumDeleteCount){
            return false;
        }
        if(log.isDebugEnabled()){
            log.debug("Process ID has un completed instances count : " + processInstanceCount);
        }
        if(processInstanceCount > 0){
            List<HistoricProcessInstance> instances = runtimeQuery.listPage(0, processInstanceCount + 1);
            bpmnDeletableInstances.setActiveProcessInstance(instances);
        }

        //next get the count of finished instance for the same process id
        runtimeQuery =  processEngine.getHistoryService()
                .createHistoricProcessInstanceQuery().processInstanceTenantId(tenantId.toString())
                .includeProcessVariables().finished().processDefinitionId(processId);
        int completedProcessInstanceCount = (int) runtimeQuery.count();

        if((completedProcessInstanceCount + bpmnDeletableInstances.getActiveInstanceCount()) > maximumDeleteCount){
            return false;
        }

        bpmnDeletableInstances.setCompletedInstanceCount(completedProcessInstanceCount);
        bpmnDeletableInstances.addCompletedProcessDefinitionIds(processId);

        if(log.isDebugEnabled()){
            log.debug("Process ID has completed instances count : " + completedProcessInstanceCount);
        }
        return true;
    }

    private void initializeVariable(){

        BPMNActivitiConfiguration bpmnActivitiConfiguration = BPMNActivitiConfiguration.getInstance();

        if(bpmnActivitiConfiguration != null){
            String countPropertyValue = bpmnActivitiConfiguration.getBPMNPropertyValue(BPMNConstants
                    .ACTIVITI_INSTANCE_MAX_DELETE_CONFIG, BPMNConstants
                    .ACTIVITI_INSTANCE_MAX_DELETE_CONFIG_MAX_COUNT_PROPERTY);

            if(countPropertyValue != null) {
                maximumDeleteCount = Integer.valueOf(countPropertyValue);
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("Maximum Delete count : " + maximumDeleteCount);
        }
    }

    private void deleteInstances(BPMNDeletableInstances bpmnDeletableInstances, ProcessEngine processEngine){

        List<HistoricProcessInstance> activeHistoricProcessInstance = bpmnDeletableInstances
                .getActiveHistoricProcessInstance();

        for (HistoricProcessInstance instance: activeHistoricProcessInstance) {
            String instanceId = instance.getId();
            RuntimeService runtimeService = processEngine.getRuntimeService();
            List<ProcessInstance> processInstances = runtimeService.createProcessInstanceQuery()
                    .processInstanceTenantId(bpmnDeletableInstances.getTenantId().toString()).processInstanceId
                            (instanceId).list();

            if(!processInstances.isEmpty()){
                runtimeService.deleteProcessInstance(instance.getId(), "Deleted by user: " + bpmnDeletableInstances.getTenantId());
            }
        }

        List<String> completedProcessDefinitionIds = bpmnDeletableInstances.getCompletedProcessDefinitionIds();
        for (String processId:completedProcessDefinitionIds){
            HistoricProcessInstanceQuery runtimeQuery =  processEngine.getHistoryService()
                    .createHistoricProcessInstanceQuery().processInstanceTenantId(bpmnDeletableInstances.getTenantId()
                            .toString()).includeProcessVariables().finished().processDefinitionId(processId);

            int completedProcessInstanceCount = (int) runtimeQuery.count();

            if(completedProcessInstanceCount > 0){

                List<HistoricProcessInstance> instances = runtimeQuery.listPage(0, completedProcessInstanceCount + 1);
                HistoryService historyService = processEngine.getHistoryService();
                for (HistoricProcessInstance instance: instances) {
                    String instanceId = instance.getId();
                    historyService.deleteHistoricProcessInstance(instanceId);
                }
            }

        }
    }

    private String encodeToString(BufferedImage image, String type) {

        String imageString = null;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            ImageIO.write(image, type, bos);
            byte[] imageBytes = bos.toByteArray();
            imageString = Base64Utils.encode(imageBytes);
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
     * Get the checksum of latest deployment for given deployment name
     *
     * @param deploymentName
     * @return
     * @throws BPSFault
     */
    public String getLatestChecksum(String deploymentName) throws BPSFault {

        try {
            Integer tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
            RegistryService registryService = BPMNServerHolder.getInstance().getRegistryService();
            Registry tenantRegistry = registryService.getConfigSystemRegistry(tenantId);
            String deploymentRegistryPath = BPMNConstants.BPMN_REGISTRY_PATH + BPMNConstants.REGISTRY_PATH_SEPARATOR
                    + deploymentName;
            if (tenantRegistry.resourceExists(deploymentRegistryPath)) {
                Resource deploymentEntry = tenantRegistry.get(deploymentRegistryPath);
                return deploymentEntry.getProperty(BPMNConstants.LATEST_CHECKSUM_PROPERTY);
            } else {
                return null;
            }
        } catch (RegistryException e) {
            String msg = "Error while accessing registry to get latest checksum for package : " + deploymentName;
            throw new BPSFault(msg, e);
        }
    }

}
