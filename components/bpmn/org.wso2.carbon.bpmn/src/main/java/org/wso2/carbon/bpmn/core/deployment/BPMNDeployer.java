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

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.deployment.AbstractDeployer;
import org.apache.axis2.deployment.DeploymentException;
import org.apache.axis2.deployment.repository.util.DeploymentFileData;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bpmn.core.BPMNConstants;
import org.wso2.carbon.bpmn.core.BPMNServerHolder;
import org.wso2.carbon.bpmn.core.BPSException;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.registry.api.RegistryException;
import org.wso2.carbon.registry.core.config.RegistryContext;
import org.wso2.carbon.utils.CarbonUtils;

import java.io.File;

/*
Deployer for BPMN packages. There is a separate deployer for each tenant.
 */
public class BPMNDeployer extends AbstractDeployer {

    private static Log log = LogFactory.getLog(BPMNDeployer.class);
    private TenantRepository tenantRepository = null;

    @Override
    public void init(ConfigurationContext configurationContext) {

        Integer tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        log.info("Initializing BPMN Deployer for tenant " + tenantId + ".");
        try {
            File tenantRepoFolder = createTenantRepo(configurationContext);
            tenantRepository = BPMNServerHolder.getInstance().getTenantManager().createTenantRepository(tenantId);
            tenantRepository.setRepoFolder(tenantRepoFolder);

            // Currently using the registry read/write mount property to determine whether this node is a master node or a slave node.
            // Only master node can fix deployment issues in BPMN packages
            if (!isServerReadOnly()) {
                tenantRepository.fixDeployments();
            }
        } catch (BPSException e) {
            String msg = "Failed to create a tenant store for tenant: " + tenantId;
            log.error(msg, e);
        }
    }

    public void deploy(DeploymentFileData deploymentFileData) throws DeploymentException {

        // Currently using the registry read/write mount property to determine whether this node is a master node or a slave node.
        boolean isMasterServer = !isServerReadOnly();
        // Worker nodes cannot deploy BPMN packages
        if (!isMasterServer) {
            return;
        }

        Integer tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        log.info("Deploying BPMN archive " + deploymentFileData.getFile().getName() + " for tenant: " + tenantId);
        try {
            BPMNDeploymentContext deploymentContext = new BPMNDeploymentContext(tenantId);
            deploymentContext.setBpmnArchive(deploymentFileData.getFile());
            tenantRepository.deploy(deploymentContext);

        } catch (Exception e) {
            String errorMessage = "Failed to deploy the archive: " + deploymentFileData.getAbsolutePath();
            log.error(errorMessage, e);
            throw new DeploymentException(errorMessage, e);
        }
    }

    public void undeploy(String bpmnArchivePath) throws DeploymentException {

        // Currently using the registry read/write mount property to determine whether this node is a master node or a slave node.
        boolean isMasterServer = !isServerReadOnly();

        //worker nodes cannot un deploy BPMN packages
        if (!isMasterServer) {
            return;
        }
        File bpmnArchiveFile = new File(bpmnArchivePath);
        if (bpmnArchiveFile.exists()) {
            if (log.isTraceEnabled()) {
                log.trace("BPMN package: " + bpmnArchivePath + " exists in the deployment folder. " +
                          "Therefore, this can be an update of the package and the undeployment will be aborted.");
            }
            return;
        }
        Integer tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        log.info("Undeploying BPMN archive " + bpmnArchivePath + " for tenant: " + tenantId);
        try {
            String deploymentName = FilenameUtils.getBaseName(bpmnArchivePath);
            tenantRepository.undeploy(deploymentName, true);

        } catch (Exception e) {
            String errorMessage = "Failed to undeploy the archive: " + bpmnArchivePath;
            log.error(errorMessage, e);
            throw new DeploymentException(errorMessage, e);
        }

    }

    private File createTenantRepo(ConfigurationContext configurationContext) throws BPSException {
        String axisRepoPath = configurationContext.getAxisConfiguration().getRepository().getPath();
        if (CarbonUtils.isURL(axisRepoPath)) {
            String msg = "URL Repositories are not supported: " + axisRepoPath;
            log.error(msg);
            throw new BPSException(msg);
        }
        File tenantsRepository = new File(axisRepoPath);
        File bpmnRepo = new File(tenantsRepository, BPMNConstants.BPMN_REPO_NAME);

        if (!bpmnRepo.exists()) {
            boolean status = bpmnRepo.mkdir();
            if (!status) {
                String msg = "Failed to create BPMN repository folder " + bpmnRepo.getAbsolutePath() + ".";
                log.error(msg);
                throw new BPSException(msg);
            }
        }
        return bpmnRepo;
    }

    @Override
    public void setDirectory(String s) {
    }

    @Override
    public void setExtension(String s) {
    }

    private boolean isServerReadOnly() {
        try {
            RegistryContext registryContext = BPMNServerHolder.getInstance().getRegistryService().getConfigSystemRegistry().getRegistryContext();
            if (registryContext.isReadOnly()) {
                return true;
            }
        } catch (RegistryException e) {
            log.error("Error while reading registry status");
        }
        return false;
    }

}
