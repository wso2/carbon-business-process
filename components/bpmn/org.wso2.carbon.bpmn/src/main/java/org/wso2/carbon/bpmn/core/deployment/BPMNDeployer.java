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
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.repository.DeploymentBuilder;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bpmn.core.BPMNConstants;
import org.wso2.carbon.bpmn.core.BPMNServerHolder;
import org.wso2.carbon.bpmn.core.BPSFault;
import org.wso2.carbon.bpmn.core.Utils;
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
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.zip.ZipInputStream;
import org.wso2.carbon.bpmn.core.mgt.model.DeploymentMetaDataModel;

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
 //   private ActivitiDAO activitiDAO;
    private Integer tenantId;
    /**
     * Initializes the deployment per tenant
     *
     * @param configurationContext axis2 configurationContext
     */
    @Override
    public void init() {
//        log.info("BPMNDeployer initializing");
//        artifactType = new ArtifactType<>("BPMN");
//        this.tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
//        this.activitiDAO = new ActivitiDAO();
//        //Integer tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
//        // log.info("Initializing BPMN Deployer for tenant " + tenantId + ".");
//        try {
//           /* File tenantRepoFolder = createTenantRepo(configurationContext);
//            tenantRepository = BPMNServerHolder.getInstance().getTenantManager().createTenantRepository(tenantId);
//            tenantRepository.setRepoFolder(tenantRepoFolder);
//
//             if (!CarbonUtils.isWorkerNode()) {
//                tenantRepository.fixDeployments();
//            }
//        }  catch (BPSFault e) {
//            String msg = "Tenant Error: " + tenantId;
//            log.error(msg, e);*/
//            deploymentLocation = new URL(DEPLOYMENT_PATH);
//        } catch (MalformedURLException e) {
//            log.error("BPMN deployer location error");
//        }
    }

    /**
     * Deploys a given bpmn package in acitiviti bpmn engine.
     * @param
     * @throws CarbonDeploymentException On failure , deployment exception is thrown
     */

    public Object deploy(Artifact artifact) throws CarbonDeploymentException{
        File artifactFile = artifact.getFile();
        String artifactPath = artifactFile.getAbsolutePath();
        String checksum = "";
        ZipInputStream archiveStream = null;
        //check if extension is bar
//        if (isSupportedFile(artifactFile)) {
//
//           // Integer tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
//            String deploymentName = FilenameUtils.getBaseName(artifactFile.getName());
//            //get checksum value of new file
//            try {
//                checksum = Utils.getMD5Checksum(artifactFile);
//            } catch (NoSuchAlgorithmException e) {
//                log.error("Checksum generation algorithm not found", e);
//            } catch (IOException e) {
//                log.error("Checksum generation failed for IO operation", e);
//            }
//
//            // get stored metadata model from activiti reg table if available
//            DeploymentMetaDataModel deploymentMetaDataModel =
//                    activitiDAO.selectTenantAwareDeploymentModel(tenantId.toString(), deploymentName);
//
//            if (log.isDebugEnabled()) {
//                log.debug("deploymentName=" + deploymentName + " checksum=" + checksum);
//                log.debug("deploymentMetaDataModel=" + deploymentMetaDataModel.toString());
//            }
//
//            if (deploymentMetaDataModel == null) {
//                ProcessEngine engine = BPMNServerHolder.getInstance().getEngine();
//                RepositoryService repositoryService = engine.getRepositoryService();
//                DeploymentBuilder deploymentBuilder =
//                        repositoryService.createDeployment().tenantId(tenantId.toString()).
//                                name(deploymentName);
//                try {
//                    archiveStream =
//                            new ZipInputStream(new FileInputStream(artifact.getFile()));
//                } catch (FileNotFoundException e) {
//                    String errMsg = "Archive stream not found for BPMN repsoitory";
//                    throw new CarbonDeploymentException(errMsg, e);
//                }
//
//                deploymentBuilder.addZipInputStream(archiveStream);
//                Deployment deployment = deploymentBuilder.deploy();
//
//                //Store deployed metadata record in activiti
//                deploymentMetaDataModel = new DeploymentMetaDataModel();
//                deploymentMetaDataModel.setPackageName(deploymentName);
//                deploymentMetaDataModel.setCheckSum(checksum);
//                deploymentMetaDataModel.setTenantID(tenantId.toString());
//                deploymentMetaDataModel.setId(deployment.getId());
//
//                //call for insertion
//                activitiDAO.insertDeploymentMetaDataModel(deploymentMetaDataModel);
//            }
//            /////
//            else if(deploymentMetaDataModel != null) { //deployment exists
//                // not the same version that is already deployed
//                if (!checksum.equalsIgnoreCase(deploymentMetaDataModel.getCheckSum())) {
//                    // It is not a new deployment, but a version update
//                    update(artifact); //TODO update new version deployment and file repo
//                    deploymentMetaDataModel.setCheckSum(checksum);
//                    activitiDAO.updateDeploymentMetaDataModel(deploymentMetaDataModel);
//                }
//
//            }/////
       // }
        return artifactPath;

    }

    public void undeploy(Object key) throws CarbonDeploymentException {
        //TODO : cluster workernode
        String deploymentName = "";
       // Integer tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
      //  try {
          //   deploymentName = FilenameUtils.getBaseName(key.toString());//CHECK

            // Remove the deployment from the activiti registry

        //    DeploymentMetaDataModel undeployModel = activitiDAO.selectTenantAwareDeploymentModel(tenantId.toString(),deploymentName);

//            if(undeployModel != null)
//            {
//                activitiDAO.deleteDeploymentMetaDataModel(undeployModel);
//            }
//            //TODO: Remove from file repo
//
//            // Delete all versions of this package from the Activiti engine.
//            ProcessEngine engine = BPMNServerHolder.getInstance().getEngine();
//            RepositoryService repositoryService = engine.getRepositoryService();
//            List<Deployment> deployments =
//                    repositoryService.createDeploymentQuery().deploymentTenantId(tenantId.toString()).deploymentName(deploymentName).list();
//            for (Deployment deployment : deployments) {
//                repositoryService.deleteDeployment(deployment.getId(), true);
//            }
//
//
//        }
//        catch(ActivitiException e) {
//            String msg = "Failed to undeploy BPMN deployment: " + deploymentName + " for tenant: " + tenantId;
//            log.error(msg, e);
//            throw new CarbonDeploymentException(msg, e);
//        }
    }

  //Perform version support : update activiti deployment and file repo /registry done
    public Object update(Artifact artifact) throws CarbonDeploymentException {
        File artifactFile = artifact.getFile();
        String artifactPath = artifactFile.getAbsolutePath();
        String deploymentName = artifactFile.getName();

        //Update activiti engine based deployment
//        ProcessEngine engine = BPMNServerHolder.getInstance().getEngine();
//        RepositoryService repositoryService = engine.getRepositoryService();
//        DeploymentBuilder deploymentBuilder =
//                repositoryService.createDeployment().tenantId(tenantId.toString()).
//                        name(deploymentName);
//        try {
//            ZipInputStream archiveStream =
//                    new ZipInputStream(new FileInputStream(artifact.getFile()));
//            deploymentBuilder.addZipInputStream(archiveStream);
//            deploymentBuilder.deploy();
//        } catch (FileNotFoundException e) {
//            String errMsg = "Archive stream not found for BPMN repsoitory";
//            throw new CarbonDeploymentException(errMsg, e);
//        }

        //TODO: update file repo
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

