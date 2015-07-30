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

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.DeploymentQuery;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.repository.ProcessDefinitionQuery;
import org.apache.axiom.util.base64.Base64Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bpmn.core.BPMNServerHolder;
import org.wso2.carbon.bpmn.core.BPSFault;
import org.wso2.carbon.bpmn.core.deployment.TenantRepository;
import org.wso2.carbon.bpmn.core.mgt.model.BPMNDeployment;
import org.wso2.carbon.bpmn.core.mgt.model.BPMNProcess;
import org.wso2.carbon.context.CarbonContext;

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

    public BPMNDeployment[] getDeployments(){
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
        Integer tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        DeploymentQuery query = BPMNServerHolder.getInstance().getEngine().getRepositoryService().createDeploymentQuery();
        return  (int) query.deploymentTenantId(tenantId.toString()).count();
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
        TenantRepository tenantRepository = BPMNServerHolder.getInstance().getTenantManager().getTenantRepository(tenantId);
        tenantRepository.undeploy(deploymentName, false);
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
}
