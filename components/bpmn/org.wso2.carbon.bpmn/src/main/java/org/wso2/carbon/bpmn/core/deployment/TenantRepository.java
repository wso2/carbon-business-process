package org.wso2.carbon.bpmn.core.deployment;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.DeploymentBuilder;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.apache.axis2.deployment.DeploymentException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bpmn.core.BPMNConstants;
import org.wso2.carbon.bpmn.core.BPMNServerHolder;
import org.wso2.carbon.bpmn.core.BPSException;
import org.wso2.carbon.bpmn.core.Utils;
import org.wso2.carbon.registry.api.*;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipInputStream;

public class TenantRepository {

    private static Log log = LogFactory.getLog(TenantRepository.class);

    private Integer tenantId;

    private File repoFolder;

    public TenantRepository(Integer tenantId) {
        this.tenantId = tenantId;
    }

    public File getRepoFolder() {
        return repoFolder;
    }

    public void setRepoFolder(File repoFolder) {
        this.repoFolder = repoFolder;
    }

    public void deploy(BPMNDeploymentContext deploymentContext) throws DeploymentException {

        try {
            //TODO: validate package

            String deploymentName = FilenameUtils.getBaseName(deploymentContext.getBpmnArchive().getName());
            String checksum = Utils.getMD5Checksum(deploymentContext.getBpmnArchive());

            RegistryService registryService = BPMNServerHolder.getInstance().getRegistryService();
            Registry tenantRegistry = registryService.getConfigSystemRegistry(tenantId);
            String deploymentRegistryPath = BPMNConstants.BPMN_REGISTRY_PATH + BPMNConstants.REGISTRY_PATH_SEPARATOR + deploymentName;
            Resource deploymentEntry = null;
            if (tenantRegistry.resourceExists(deploymentRegistryPath)) {
                deploymentEntry = tenantRegistry.get(deploymentRegistryPath);
            } else {
                // this is a new deployment
                deploymentEntry = tenantRegistry.newCollection();
            }

            String latestChecksum = deploymentEntry.getProperty(BPMNConstants.LATEST_CHECKSUM_PROPERTY);
            if (latestChecksum != null && checksum.equals(latestChecksum)) {
                // this is a server restart
                return;
            }
            deploymentEntry.setProperty(BPMNConstants.LATEST_CHECKSUM_PROPERTY, checksum);
            tenantRegistry.put(deploymentRegistryPath, deploymentEntry);

            ProcessEngine engine = BPMNServerHolder.getInstance().getEngine();
            RepositoryService repositoryService = engine.getRepositoryService();

            DeploymentBuilder deploymentBuilder = repositoryService.createDeployment().tenantId(tenantId.toString()).name(deploymentName);
            ZipInputStream archiveStream = new ZipInputStream(new FileInputStream(deploymentContext.getBpmnArchive()));
            deploymentBuilder.addZipInputStream(archiveStream);
            Deployment deployment = deploymentBuilder.deploy();
            archiveStream.close();

        } catch (Exception e) {
            String errorMessage = "Failed to deploy the archive: " + deploymentContext.getBpmnArchive().getName();
            log.error(errorMessage, e);
            throw new DeploymentException(errorMessage, e);
        }
    }

    public void undeploy(String deploymentName, boolean force) throws BPSException {

        try {
            // remove the deployment from the tenant's registry
            RegistryService registryService = BPMNServerHolder.getInstance().getRegistryService();
            Registry tenantRegistry = registryService.getConfigSystemRegistry(tenantId);
            String deploymentRegistryPath = BPMNConstants.BPMN_REGISTRY_PATH + BPMNConstants.REGISTRY_PATH_SEPARATOR + deploymentName;
            if (!tenantRegistry.resourceExists(deploymentRegistryPath) && !force) {
                String msg = "Deployment: " + deploymentName + " does not exist.";
                log.warn(msg);
                return;
            }
            tenantRegistry.delete(deploymentRegistryPath);

            // remove the deployment archive from the tenant's deployment folder
            File deploymentArchive = new File(repoFolder, deploymentName + ".bar");
            FileUtils.deleteQuietly(deploymentArchive);

            ProcessEngine engine = BPMNServerHolder.getInstance().getEngine();
            RepositoryService repositoryService = engine.getRepositoryService();
            List<Deployment> deployments =
                    repositoryService.createDeploymentQuery().deploymentTenantId(tenantId.toString()).deploymentName(deploymentName).list();
            if (deployments.isEmpty()) {
                return;
            }
            for (Deployment deployment : deployments) {
                try {
                    undeployById(deployment.getId());
                } catch (IllegalAccessException e) {
                    String msg = "Deployment ID: " + deployment.getId() + " of the deployment " + deploymentName +
                            " does not belong to tenant " + tenantId + ". Skipping the undeployment.";
                    log.error(msg);
                }
            }

        } catch (RegistryException e) {
            String msg = "Failed to undeploy BPMN deployment: " + deploymentName + " for tenant: " + tenantId;
            log.error(msg, e);
            throw new BPSException(msg, e);
        }

    }

    private void undeployById(String deploymentId) throws IllegalAccessException {


        ProcessEngine engine = BPMNServerHolder.getInstance().getEngine();
        RepositoryService repositoryService = engine.getRepositoryService();

        List<ProcessDefinition> processDefinitions =
                repositoryService.createProcessDefinitionQuery().processDefinitionTenantId(tenantId.toString()).deploymentId(deploymentId).list();

        repositoryService.deleteDeployment(deploymentId, true);

    }

    public List<Deployment> getDeployments() throws BPSException {

        ProcessEngine engine = BPMNServerHolder.getInstance().getEngine();
        List<Deployment> tenantDeployments =
                engine.getRepositoryService().createDeploymentQuery().deploymentTenantId(tenantId.toString()).list();

        return tenantDeployments;
    }

    public List<ProcessDefinition> getDeployedProcessDefinitions() throws BPSException {

        ProcessEngine engine = BPMNServerHolder.getInstance().getEngine();
        List<ProcessDefinition> processDefinitions = engine.getRepositoryService().createProcessDefinitionQuery().processDefinitionTenantId(tenantId.toString()).list();
        return processDefinitions;
    }


    /*
    This method will fix the deployment conflicts when
    truncating activiti db or
    registry db or
    delete from file system.
    */
    public void fixDeployments() throws BPSException {

        List<String> fileArchiveNames = new ArrayList<String>();
        File[] fileDeployments = repoFolder.listFiles();
        for (File fileDeployment : fileDeployments) {
            String deploymentName = FilenameUtils.getBaseName(fileDeployment.getName());
            fileArchiveNames.add(deploymentName);
        }

        List<String> activitiDeploymentNames = new ArrayList<String>();
        ProcessEngine engine = BPMNServerHolder.getInstance().getEngine();
        RepositoryService repositoryService = engine.getRepositoryService();
        List<Deployment> tenantDeployments = repositoryService.createDeploymentQuery().deploymentCategory(tenantId.toString()).list();
        for (Deployment deployment : tenantDeployments) {
            String deploymentName = deployment.getName();
            activitiDeploymentNames.add(deploymentName);
        }

        List<String> registryDeploymentNames = new ArrayList<String>();
        try {
            RegistryService registryService = BPMNServerHolder.getInstance().getRegistryService();
            Registry tenantRegistry = registryService.getConfigSystemRegistry(tenantId);
            String deploymentRegistryPath = BPMNConstants.BPMN_REGISTRY_PATH;
            if (tenantRegistry.resourceExists(deploymentRegistryPath)) {
                Collection registryDeployments = (Collection) tenantRegistry.get(deploymentRegistryPath);
                String[] deploymentPaths = registryDeployments.getChildren();
                for (String deploymentPath : deploymentPaths) {
                    String deploymentName = deploymentPath.substring(deploymentPath.lastIndexOf("/") + 1, deploymentPath.length());
                    registryDeploymentNames.add(deploymentName);
                }
            }
        } catch (RegistryException e) {
            String msg = "Failed to obtain BPMN deployments from the Registry.";
            log.error(msg, e);
            throw new BPSException(msg, e);
        }

        Set<String> allDeploymentNames = new HashSet<String>();
        allDeploymentNames.addAll(fileArchiveNames);
        allDeploymentNames.addAll(activitiDeploymentNames);
        allDeploymentNames.addAll(registryDeploymentNames);

        for (String deploymentName : allDeploymentNames) {
            // TODO: need to check two scenarios when truncating activiti db or registry db.
            if (!(fileArchiveNames.contains(deploymentName))) {
                try {
                    undeploy(deploymentName, true);
                } catch (BPSException e) {
                    String msg = "Failed undeploy inconsistent deployment: " + deploymentName;
                    log.error(msg, e);
                    throw new BPSException(msg, e);
                }

            }
        }

    }

    public Integer getTenantId() {
        return tenantId;
    }

}

