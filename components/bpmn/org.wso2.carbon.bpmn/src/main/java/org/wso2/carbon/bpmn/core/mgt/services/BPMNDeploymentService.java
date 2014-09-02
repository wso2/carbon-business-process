package org.wso2.carbon.bpmn.core.mgt.services;

import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bpmn.core.BPMNServerHolder;
import org.wso2.carbon.bpmn.core.BPSException;
import org.wso2.carbon.bpmn.core.deployment.TenantRepository;
import org.wso2.carbon.bpmn.core.mgt.model.BPMNDeployment;
import org.wso2.carbon.bpmn.core.mgt.model.BPMNProcess;
import org.wso2.carbon.context.CarbonContext;
import sun.misc.BASE64Encoder;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.List;

public class BPMNDeploymentService {

    private static Log log = LogFactory.getLog(BPMNDeploymentService.class);

    public BPMNDeployment[] getDeployments() throws BPSException {

        Integer tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {

            TenantRepository tenantRepository = BPMNServerHolder.getInstance().getTenantManager().getTenantRepository(tenantId);
            List<Deployment> deployments = tenantRepository.getDeployments();
            BPMNDeployment[] bpmnDeployments = new BPMNDeployment[deployments.size()];
            for (int i = 0; i < deployments.size(); i++) {
                Deployment deployment = deployments.get(i);
                BPMNDeployment bpmnDeployment = new BPMNDeployment();
                bpmnDeployment.setDeploymentId(deployment.getId());
                bpmnDeployment.setDeploymentName(deployment.getName());
                bpmnDeployment.setDeploymentTime(deployment.getDeploymentTime());
                bpmnDeployments[i] = bpmnDeployment;
            }
            return bpmnDeployments;
        } catch (Exception e) {
            String msg = "Failed to get deployments.";
            log.error(msg, e);
            throw new BPSException(msg, e);
        }
    }

    public BPMNProcess[] getDeployedProcesses() throws BPSException {

        Integer tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
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
        } catch (Exception e) {
            String msg = "Failed to get deployed processes.";
            log.error(msg, e);
            throw new BPSException(msg, e);
        }
    }

    public String getProcessDiagram(String processId) throws BPSException {

        Integer tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        try{
            RepositoryService repositoryService = BPMNServerHolder.getInstance().getEngine().getRepositoryService();
            ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionTenantId(tenantId.toString())
                    .processDefinitionId(processId)
                    .singleResult();
            String diagramResourceName = processDefinition.getDiagramResourceName();
            InputStream imageStream = repositoryService.getResourceAsStream(processDefinition.getDeploymentId(), diagramResourceName);
            BufferedImage bufferedImage = ImageIO.read(imageStream);

            return encodeToString(bufferedImage, "png");
        }catch(Exception e){
            String msg = "Failed to create the diagram for process: " + processId;
            log.error(msg, e);
            throw new BPSException(msg, e);
        }
    }

    public String getProcessModel(String processId) throws BPSException {

        Integer tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        try{
            RepositoryService repositoryService = BPMNServerHolder.getInstance().getEngine().getRepositoryService();
            ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionTenantId(tenantId.toString())
                    .processDefinitionId(processId)
                    .singleResult();
            InputStream stream = repositoryService.getProcessModel(processDefinition.getId());
            BufferedReader br = new BufferedReader(new InputStreamReader(stream));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        }catch(Exception e){
            String msg = "Failed to create the diagram for process: " + processId;
            log.error(msg, e);
            throw new BPSException(msg, e);
        }
    }

    public void undeploy(String deploymentName) throws BPSException {

        Integer tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            TenantRepository tenantRepository = BPMNServerHolder.getInstance().getTenantManager().getTenantRepository(tenantId);
            tenantRepository.undeploy(deploymentName, false);

        } catch (Exception e) {
            String msg = "Failed to undeploy the BPMN deployment: " + deploymentName;
            log.error(msg, e);
            throw new BPSException(msg, e);
        }
    }

    private String encodeToString(BufferedImage image, String type) {
        String imageString = null;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        try {
            ImageIO.write(image, type, bos);
            byte[] imageBytes = bos.toByteArray();

            BASE64Encoder encoder = new BASE64Encoder();
            imageString = encoder.encode(imageBytes);

            bos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return imageString;
    }
}
