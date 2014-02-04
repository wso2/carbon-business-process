/*
 * Copyright (c) 2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.bpel.deployer;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.deployment.AbstractDeployer;
import org.apache.axis2.deployment.DeploymentException;
import org.apache.axis2.deployment.repository.util.DeploymentFileData;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bpel.core.BPELConstants;
import org.wso2.carbon.bpel.core.ode.integration.BPELServer;
import org.wso2.carbon.bpel.core.ode.integration.store.TenantProcessStore;
import org.wso2.carbon.bpel.deployer.internal.BPELDeployerServiceComponent;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.io.File;

/**
 * Deployer for the BPEL packages.
 * <p/>
 * There is separate deployer for each tenant.
 *
 * @see org.wso2.carbon.bpel.core.Axis2ConfigurationContextObserverImpl
 */
public class BPELDeployer extends AbstractDeployer {
    private static Log log = LogFactory.getLog(BPELDeployer.class);

    private TenantProcessStore tenantProcessStore;

    public void init(ConfigurationContext configurationContext) {

        Integer tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        log.info("Initializing BPEL Deployer for tenant " + tenantId + ".");

        BPELDeployerServiceComponent.getTenantRegistryLoader().
                loadTenantRegistry(tenantId);

        File bpelRepo = null;
        try {
            bpelRepo = createBPELRepository(configurationContext);
        } catch (DeploymentException e) {
            log.warn("BPEL repository creation failed.", e);
        }
        BPELServer bpsServer = BPELDeployerServiceComponent.getBPELServer();
        tenantProcessStore = bpsServer.getMultiTenantProcessStore().
                createProcessStoreForTenant(configurationContext);

        tenantProcessStore.setBpelArchiveRepo(bpelRepo);
        configurationContext.setProperty(HTTPConstants.MULTITHREAD_HTTP_CONNECTION_MANAGER,
                                         bpsServer.getHttpConnectionManager());

        try {
            tenantProcessStore.init();
        } catch (Exception re) {
            log.warn("Initialization of tenant process store failed for tenant: " +
                    tenantId +
                    " This can cause issues in deployment of BPEL packages.", re);
        }
    }

    private File createBPELRepository(ConfigurationContext configCtx) throws DeploymentException {
        String axisRepoPath = configCtx.getAxisConfiguration().getRepository().getPath();
        if (CarbonUtils.isURL(axisRepoPath)) {
            throw new DeploymentException("URL Repositories are not supported: " + axisRepoPath);
        }
        File tenantsRepository = new File(axisRepoPath);
        File bpelRepo = new File(tenantsRepository, BPELConstants.BPEL_REPO_DIRECTORY);

        if (!bpelRepo.exists()) {
            boolean status = bpelRepo.mkdir();
            if (!status) {
                throw new DeploymentException("Failed to create BPEL repository directory " +
                        bpelRepo.getAbsolutePath() + ".");
            }
        }

        return bpelRepo;
    }

    public void deploy(DeploymentFileData deploymentFileData) throws DeploymentException {
        try {
            tenantProcessStore.deploy(deploymentFileData.getFile());
        } catch (Exception e) {
            String errorMessage = "Error deploying BPEL package: " + deploymentFileData.getName();
            log.error(errorMessage, e);
            throw new DeploymentException(errorMessage, e);
        }
    }

    public void setDirectory(String repoDir) {
//        this.repoDir = repoDir;
    }

    public void setExtension(String extension) {
//        this.extension = extension;
    }

    public void undeploy(String bpelArchivePath) throws DeploymentException {
        File bpelArchiveFile = new File(bpelArchivePath);
        if (bpelArchiveFile.exists()) {
            if (log.isTraceEnabled()) {
                log.trace("Assumption: this as an update of the bpel package: " + bpelArchivePath +
                        ", Therefore no need to do undeploy");
            }

            // We assume this as an update of the bpel package, Therefore no need to do anything
            // here
            return;
        }

        log.info("Undeploying BPEL archive " + bpelArchivePath);
        try {
            String archiveName = bpelArchivePath.substring(bpelArchivePath.
                    lastIndexOf(File.separator) + 1);
            String bpelPackageName = archiveName.substring(0, archiveName.lastIndexOf("." +
                    BPELConstants.BPEL_PACKAGE_EXTENSION));
            tenantProcessStore.undeploy(bpelPackageName);
        } catch (Exception e) {
            String errMsg = "BPEL Package: " + bpelArchivePath + " undeployment failed.";
            log.error(errMsg, e);
            throw new DeploymentException(errMsg, e);
        }
    }

//    public void cleanup() throws DeploymentException {
//    }
}
