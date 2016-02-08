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

package org.wso2.carbon.bpmn.core.deployment;

/*import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.deployment.AbstractDeployer;
import org.apache.axis2.deployment.DeploymentException;
import org.apache.axis2.deployment.repository.util.DeploymentFileData;*/
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.DeploymentBuilder;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bpmn.core.BPMNConstants;
import org.wso2.carbon.bpmn.core.BPMNServerHolder;
import org.wso2.carbon.bpmn.core.BPSFault;
import org.wso2.carbon.context.CarbonContext;
/*import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.utils.CarbonUtils;*/
import org.wso2.carbon.kernel.deployment.Artifact;
import org.wso2.carbon.kernel.deployment.ArtifactType;
import org.wso2.carbon.kernel.deployment.Deployer;
import org.wso2.carbon.kernel.deployment.exception.CarbonDeploymentException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.zip.ZipInputStream;

/**
 * Deployer implementation for BPMN Packages. This deployer is associated with bpmn directory
 * under repository/deployment/server directory. Currently associated file extension is .bar.
 * Separate deployer instance is created for each tenant.
 * Activiti Engine versions same package if deployed twice. In order to overcome this issue,
 * we are using an additional table which will keep track of the deployed package's md5sum in-order to
 * identify the deployment of a new package.
 *
 */
public class BPMNDeployer implements Deployer {

    //private static Log log = LogFactory.getLog(BPMNDeployer.class);
   // private TenantRepository tenantRepository = null;
 private static final Logger log = LoggerFactory.getLogger(BPMNDeployer.class);
    private static final String DEPLOYMENT_PATH = "file:bpmn";
   private static final String SUPPORTED_EXTENSIONS = "bar";
    private URL deploymentLocation;
    private ArtifactType artifactType;
    private HashMap<Object, List<Object>> deployedArtifacts = new HashMap<>();
	/**
	 * Initializes the deployment per tenant
	 *
	 * @param configurationContext axis2 configurationContext
	 */
    @Override
    public void init() {
        log.info("BPMNDeployer initializing");
       artifactType = new ArtifactType<>("BPMN");
        //Integer tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
       // log.info("Initializing BPMN Deployer for tenant " + tenantId + ".");
        try {
           /* File tenantRepoFolder = createTenantRepo(configurationContext);
            tenantRepository = BPMNServerHolder.getInstance().getTenantManager().createTenantRepository(tenantId);
            tenantRepository.setRepoFolder(tenantRepoFolder);

             if (!CarbonUtils.isWorkerNode()) {
                tenantRepository.fixDeployments();
            }
        }  catch (BPSFault e) {
            String msg = "Tenant Error: " + tenantId;
            log.error(msg, e);*/
            deploymentLocation = new URL(DEPLOYMENT_PATH);
        } catch (MalformedURLException e) {
           log.error("BPMN deployer location error");
        }
    }

	/**
	 * Deploys a given bpmn package in acitiviti bpmn engine.
	 * @param deploymentFileData Provide information about the deployment file
	 * @throws DeploymentException On failure , deployment exception is thrown
	 */

    public Object deploy(Artifact
         artifact) throws CarbonDeploymentException{
	    File artifactFile = artifact.getFile();
        String artifactPath = artifactFile.getAbsolutePath();
        if (isSupportedFile(artifactFile)) {
            try {
                Integer tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
               String deploymentName = FilenameUtils.getBaseName(artifact.getFile().getName());
                ProcessEngine engine = BPMNServerHolder.getInstance().getEngine();
                RepositoryService repositoryService = engine.getRepositoryService();
                DeploymentBuilder deploymentBuilder = repositoryService.createDeployment().tenantId(tenantId.toString()).name(deploymentName);
                ZipInputStream archiveStream = new ZipInputStream(new FileInputStream(artifact.getFile()));
                deploymentBuilder.addZipInputStream(archiveStream);
                deploymentBuilder.deploy();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        return artifactPath;

      /*  Integer tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        log.info("Deploying BPMN archive " + deploymentFileData.getFile().getName() +
                 " for tenant: " + tenantId);
        try {
            BPMNDeploymentContext deploymentContext = new BPMNDeploymentContext(tenantId);
            deploymentContext.setBpmnArchive(deploymentFileData.getFile());
            tenantRepository.deploy(deploymentContext);

	        //log.info( "Deployment Status " + deploymentFileData.getFile() + " deployed = " + deployed );

        } catch (DeploymentException e) {
            String errorMessage = "Failed to deploy the archive: " + deploymentFileData.getAbsolutePath();
            throw new DeploymentException(errorMessage, e);
        }*/
    }

	/**
	 * Undeployment operation for Bpmn Deployer
	 *
	 * @param bpmnArchivePath        archivePatch
	 * @throws DeploymentException   Deployment failure will result in this exception
	 */

    /*public void undeploy(String bpmnArchivePath) throws DeploymentException {

	    // Worker nodes does not perform any action related to bpmn undeployment, manager node takes
	    // care of all deployment/undeployment actions


	    if (isWorkerNode()) {
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
        String deploymentName = FilenameUtils.getBaseName(bpmnArchivePath);
        try {
           tenantRepository.undeploy(deploymentName, true);
        } catch (BPSFault be) {
            String errorMsg = "Error un deploying BPMN Package " + deploymentName;
            throw new DeploymentException(errorMsg, be);
        }

    }*/
     public void undeploy(Object var1) throws CarbonDeploymentException {
        //TODO
    }
 
    public Object update(Artifact artifact) throws CarbonDeploymentException {
        File artifactFile = artifact.getFile();
       String artifactPath = artifactFile.getAbsolutePath();
       //TODO
       return artifactPath;
     }

	/**
	 *
	 * @param configurationContext axis2 configurationContext
	 * @return                     bpmn repo file
	 * @throws BPSFault        repo creation failure will result in this xception
	 */
   /* private File createTenantRepo(ConfigurationContext configurationContext) throws BPSFault {
        String axisRepoPath = configurationContext.getAxisConfiguration().getRepository().getPath();
        if (CarbonUtils.isURL(axisRepoPath)) {
            String msg = "URL Repositories are not supported: " + axisRepoPath;
            throw new BPSFault(msg);
        }
        File tenantsRepository = new File(axisRepoPath);
        File bpmnRepo = new File(tenantsRepository, BPMNConstants.BPMN_REPO_NAME);

        if (!bpmnRepo.exists()) {
            boolean status = bpmnRepo.mkdir();
            if (!status) {
                String msg = "Failed to create BPMN repository folder " + bpmnRepo.getAbsolutePath() + ".";
                throw new BPSFault(msg);
            }
        }
        return bpmnRepo;
    }*/
    public URL getLocation() {
        return deploymentLocation;
     }

     public ArtifactType getArtifactType() {
        return artifactType;
     }

    private boolean isSupportedFile(File file) {
        return SUPPORTED_EXTENSIONS.equalsIgnoreCase(getFileExtension(file));
     }
    /**
     * Whether a bps node is worker ( a node that does not participate in archive deployment and only handles
     * input/output . This is determined by looking at the registry read/only property
     * @return
     */
   /* private boolean isWorkerNode() {
        RegistryService registryService = BPMNServerHolder.getInstance().getRegistryService();
        boolean isWorker = true;
        try {
            isWorker = (registryService.getConfigSystemRegistry().getRegistryContext().isReadOnly());
        } catch (RegistryException e) {
            log.error("Error accessing the configuration registry");
        }
        return isWorker;
    }*/
     private String getFileExtension(File file) {
        String fileName = file.getName();
        String extension = "";
        if (file.isFile()) {
           int i = fileName.lastIndexOf('.');
           if (i > 0) {
                extension = fileName.substring(i + 1);
            }
         }
          return extension;
     }
}

