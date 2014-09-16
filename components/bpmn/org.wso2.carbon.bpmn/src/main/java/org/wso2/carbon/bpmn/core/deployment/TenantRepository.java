/**
 *  Copyright (c) 2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.bpmn.core.deployment;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.DeploymentBuilder;
import org.activiti.engine.repository.ProcessDefinition;
import org.apache.axis2.deployment.DeploymentException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bpmn.core.BPMNConstants;
import org.wso2.carbon.bpmn.core.BPMNServerHolder;
import org.wso2.carbon.bpmn.core.BPSException;
import org.wso2.carbon.bpmn.core.Utils;
import org.wso2.carbon.registry.api.Collection;
import org.wso2.carbon.registry.api.Registry;
import org.wso2.carbon.registry.api.RegistryException;
import org.wso2.carbon.registry.api.RegistryService;
import org.wso2.carbon.registry.api.Resource;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipInputStream;

/**
 * Manages BPMN deployments of a tenant.
 */
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

    /*
    Deploys a BPMN package in the Activiti engine. Each BPMN package has an entry in the registry. Checksum of the latest version of the BPMN package is stored in this entry.
    This checksum is used to determine whether a package is a new deployment (or a new version of an existing package) or a redeployment of an existing package. We have to ignore
    the later case. If a package is a new deployment, it is deployed in the Activiti engine.
     */
    public void deploy(BPMNDeploymentContext deploymentContext) throws DeploymentException {
        ZipInputStream archiveStream = null;

        try {
            //TODO: validate package

            String deploymentName = FilenameUtils.getBaseName(deploymentContext.getBpmnArchive().getName());

            // Compare the checksum of the BPMN archive with the currently available checksum in the registry to determine whether this is a new deployment.
            String checksum = Utils.getMD5Checksum(deploymentContext.getBpmnArchive());
            RegistryService registryService = BPMNServerHolder.getInstance().getRegistryService();
            Registry tenantRegistry = registryService.getConfigSystemRegistry(tenantId);
            String deploymentRegistryPath = BPMNConstants.BPMN_REGISTRY_PATH + BPMNConstants.REGISTRY_PATH_SEPARATOR + deploymentName;
            Resource deploymentEntry = null;
            if (tenantRegistry.resourceExists(deploymentRegistryPath)) {
                deploymentEntry = tenantRegistry.get(deploymentRegistryPath);
            } else {
                // This is a new deployment
                deploymentEntry = tenantRegistry.newCollection();
            }

            String latestChecksum = deploymentEntry.getProperty(BPMNConstants.LATEST_CHECKSUM_PROPERTY);
            if (latestChecksum != null && checksum.equals(latestChecksum)) {
                // This is a server restart
                return;
            }
            deploymentEntry.setProperty(BPMNConstants.LATEST_CHECKSUM_PROPERTY, checksum);
            tenantRegistry.put(deploymentRegistryPath, deploymentEntry);

            // Deploy the package in the Activiti engine
            ProcessEngine engine = BPMNServerHolder.getInstance().getEngine();
            RepositoryService repositoryService = engine.getRepositoryService();
            DeploymentBuilder deploymentBuilder = repositoryService.createDeployment().tenantId(tenantId.toString()).name(deploymentName);
            archiveStream = new ZipInputStream(new FileInputStream(deploymentContext.getBpmnArchive()));
            deploymentBuilder.addZipInputStream(archiveStream);
            deploymentBuilder.deploy();
        } catch (Exception e) {
            String errorMessage = "Failed to deploy the archive: " + deploymentContext.getBpmnArchive().getName();
            log.error(errorMessage, e);
            throw new DeploymentException(errorMessage, e);
        } finally {
            if (archiveStream != null) {
                try {
                    archiveStream.close();
                } catch (IOException e) {
                    log.error("Could not close archive stream", e);
                }
            }
        }
    }

    /*
    Undeploys a BPMN package. This may be called by the BPMN deployer, when a BPMN package is deleted from the deployment folder or by admin services.
     */
    public void undeploy(String deploymentName, boolean force) throws BPSException {

        try {
            // Remove the deployment from the tenant's registry
            RegistryService registryService = BPMNServerHolder.getInstance().getRegistryService();
            Registry tenantRegistry = registryService.getConfigSystemRegistry(tenantId);
            String deploymentRegistryPath = BPMNConstants.BPMN_REGISTRY_PATH + BPMNConstants.REGISTRY_PATH_SEPARATOR + deploymentName;
            if (!tenantRegistry.resourceExists(deploymentRegistryPath) && !force) {
                String msg = "Deployment: " + deploymentName + " does not exist.";
                log.warn(msg);
                return;
            }
            tenantRegistry.delete(deploymentRegistryPath);

            // Remove the deployment archive from the tenant's deployment folder
            File deploymentArchive = new File(repoFolder, deploymentName + ".bar");
            FileUtils.deleteQuietly(deploymentArchive);

            // Delete all versions of this package from the Activiti engine.
            ProcessEngine engine = BPMNServerHolder.getInstance().getEngine();
            RepositoryService repositoryService = engine.getRepositoryService();
            List<Deployment> deployments =
                    repositoryService.createDeploymentQuery().deploymentTenantId(tenantId.toString()).deploymentName(deploymentName).list();
            for (Deployment deployment : deployments) {
                repositoryService.deleteDeployment(deployment.getId());
            }

        } catch (RegistryException e) {
            String msg = "Failed to undeploy BPMN deployment: " + deploymentName + " for tenant: " + tenantId;
            log.error(msg, e);
            throw new BPSException(msg, e);
        }

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
    Information about BPMN deployments are recorded in 3 places: Activiti database, Registry and the file system (deployment folder). If information about a particular deployment
    is not recorded in all these 3 places, BPS may not work correctly. Therefore, this method checks whether deployments are recorded in all these places and undeploys packages, if
    they are missing in few places in an inconsistent way.

    As there are 3 places, there are 8 ways a package can be placed. These cases are handled as follows:
    (1) Whenever a package is not in the deployment folder, it is undeploye (this covers 4 combinations).
    (2) If a package is in all 3 places, it is a proper deployment and it is left untouched.
    (3) If a package is only in the deployment folder, it is a new deployment. This will be handled by the deployer.
    (4) If a package is in the deployment folder AND it is in either registry or Activiti DB (but not both), then it is an inconsistent deployment. This will be undeployed.
    */
    public void fixDeployments() throws BPSException {

        // get all deployments in the deployment folder
        List<String> fileArchiveNames = new ArrayList<String>();
        File[] fileDeployments = repoFolder.listFiles();
        for (File fileDeployment : fileDeployments) {
            String deploymentName = FilenameUtils.getBaseName(fileDeployment.getName());
            fileArchiveNames.add(deploymentName);
        }

        // get all deployments in the Activiti DB
        List<String> activitiDeploymentNames = new ArrayList<String>();
        ProcessEngine engine = BPMNServerHolder.getInstance().getEngine();
        RepositoryService repositoryService = engine.getRepositoryService();
        List<Deployment> tenantDeployments = repositoryService.createDeploymentQuery().deploymentTenantId(tenantId.toString()).list();
        for (Deployment deployment : tenantDeployments) {
            String deploymentName = deployment.getName();
            activitiDeploymentNames.add(deploymentName);
        }

        // get all deployments in the registry
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

        // construct the union of all deployments
        Set<String> allDeploymentNames = new HashSet<String>();
        allDeploymentNames.addAll(fileArchiveNames);
        allDeploymentNames.addAll(activitiDeploymentNames);
        allDeploymentNames.addAll(registryDeploymentNames);

        for (String deploymentName : allDeploymentNames) {
            try {
                if (!(fileArchiveNames.contains(deploymentName))) {
                    if (log.isDebugEnabled()) {
                        log.debug(deploymentName + " has been removed from the deployment folder. Undeploying the package...");
                    }
                    undeploy(deploymentName, true);
                } else {
                    if (activitiDeploymentNames.contains(deploymentName) && !registryDeploymentNames.contains(deploymentName)) {
                        if (log.isDebugEnabled()) {
                            log.debug(deploymentName + " is missing in the registry. Undeploying the package to avoid inconsistencies...");
                        }
                        undeploy(deploymentName, true);
                    }

                    if (!activitiDeploymentNames.contains(deploymentName) && registryDeploymentNames.contains(deploymentName)) {
                        if (log.isDebugEnabled()) {
                            log.debug(deploymentName + " is missing in the BPS database. Undeploying the package to avoid inconsistencies...");
                        }
                        undeploy(deploymentName, true);
                    }
                }
            } catch (BPSException e) {
                String msg = "Failed undeploy inconsistent deployment: " + deploymentName;
                log.error(msg, e);
                throw new BPSException(msg, e);
            }
        }
    }
}

