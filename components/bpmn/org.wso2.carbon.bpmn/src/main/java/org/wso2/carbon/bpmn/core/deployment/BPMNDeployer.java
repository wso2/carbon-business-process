/**
 * Copyright (c) 2014-2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.bpmn.core.deployment;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.DeploymentBuilder;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.bpmn.core.Utils;
import org.wso2.carbon.bpmn.core.db.dao.ActivitiDAO;
import org.wso2.carbon.bpmn.core.db.model.DeploymentMetaDataModel;
import org.wso2.carbon.bpmn.core.internal.BPMNServerHolder;
import org.wso2.carbon.deployment.engine.Artifact;
import org.wso2.carbon.deployment.engine.ArtifactType;
import org.wso2.carbon.deployment.engine.Deployer;
import org.wso2.carbon.deployment.engine.exception.CarbonDeploymentException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipInputStream;

/**
 * Deployer implementation for BPMN Packages. This deployer is associated with bpmn directory located
 * at <BPS_HOME>/deployment/server directory. Currently associated file extension is .bar. A single
 * deployer instance is created and registerd as an osgi service. Activiti Engine versions same
 * package if deployed twice. In order to overcome this issue, we are using an additional table which
 * will keep track of the deployed package's md5sum in-order to identify the deployment of a new package.
 */

public class BPMNDeployer implements Deployer {

    private static final Logger log = LoggerFactory.getLogger(BPMNDeployer.class);

    private static final String DEPLOYMENT_PATH = "file:bpmn";
    private static final String SUPPORTED_EXTENSIONS = "bar";
    private static final String ARTIFACT_TYPE = "bar";

    private URL deploymentLocation;
    private ArtifactType artifactType;
    //    private HashMap<Object, List<Object>> deployedArtifacts =
    private String deploymentDir;
    private File destinationFolder;
    private Path home;


    private ActivitiDAO activitiDAO;

    public BPMNDeployer() {
        this.activitiDAO = new ActivitiDAO();
    }

    /**
     * Init method for the BPMN Deployer
     */
    @Override
    public void init() {
        artifactType = new ArtifactType<>(ARTIFACT_TYPE);

        try {
            deploymentLocation = new URL(DEPLOYMENT_PATH);
            home = org.wso2.carbon.kernel.utils.Utils.getCarbonHome();
            deploymentDir = home + File.separator + "deployment" + File.separator + "bpmn";

        } catch (MalformedURLException | ExceptionInInitializerError e) {
            String msg = "BPMN Deployer Initialization failed : ";
            log.error(msg, e);
        }
        destinationFolder = new File(deploymentDir);
        log.info("Initialized BPMN Deployer Successfully.");
    }

    /**
     * Deploys a given bpmn package in acitiviti bpmn engine.
     *
     * @param
     * @throws CarbonDeploymentException On failure , deployment exception is thrown
     */
    @Override
    public Object deploy(Artifact artifact) throws CarbonDeploymentException {
        File artifactFile = artifact.getFile();
        if (artifactFile.exists()) {
            String artifactPath = artifactFile.getAbsolutePath();
            String checksum = "";
            ZipInputStream archiveStream = null;
            //check if extension is bar
            if (isSupportedFile(artifactFile)) {

                String deploymentName = FilenameUtils.getBaseName(artifactFile.getName());

                //get checksum value of new file
                try {
                    checksum = Utils.getMD5Checksum(artifactFile);
                } catch (NoSuchAlgorithmException e) {
                    log.error("Checksum generation algorithm not found", e);
                } catch (IOException e) {
                    log.error("Checksum generation failed for IO operation", e);
                }

                // get stored metadata model from activiti reg table if available
                DeploymentMetaDataModel deploymentMetaDataModel =
                        activitiDAO.selectDeploymentModel(deploymentName);

                if (log.isDebugEnabled()) {
                    log.debug("deploymentName=" + deploymentName + " checksum=" + checksum);
                    log.debug("deploymentMetaDataModel=" + deploymentMetaDataModel.toString());
                }

                if (deploymentMetaDataModel == null) {
                    ProcessEngine engine = BPMNServerHolder.getInstance().getEngine();
                    RepositoryService repositoryService = engine.getRepositoryService();
                    DeploymentBuilder deploymentBuilder =
                            repositoryService.createDeployment().name(deploymentName);
                    try {
                        archiveStream = new ZipInputStream(new FileInputStream(artifact.getFile()));
                    } catch (FileNotFoundException e) {
                        String errMsg = "Archive stream not found for BPMN repsoitory";
                        throw new CarbonDeploymentException(errMsg, e);
                    }

                    deploymentBuilder.addZipInputStream(archiveStream);
                    Deployment deployment = deploymentBuilder.deploy();

                    //Store deployed metadata record in activiti
                    deploymentMetaDataModel = new DeploymentMetaDataModel();
                    deploymentMetaDataModel.setPackageName(deploymentName);
                    deploymentMetaDataModel.setCheckSum(checksum);
                    deploymentMetaDataModel.setId(deployment.getId());

                    //call for insertion
                    activitiDAO.insertDeploymentMetaDataModel(deploymentMetaDataModel);

//                    try {
//                        FileUtils.copyFileToDirectory(artifactFile, destinationFolder);
//                    } catch (IOException e) {
//                        log.error("Unable to add file " + artifactFile + "to directory" +
//                                destinationFolder);
//                    }
                } else { //deployment exists
                    // not the same version that is already deployed
                    if (!checksum.equalsIgnoreCase(deploymentMetaDataModel.getCheckSum())) {
                        // It is not a new deployment, but a version update
                        update(artifact);
                        deploymentMetaDataModel.setCheckSum(checksum);
                        activitiDAO.updateDeploymentMetaDataModel(deploymentMetaDataModel);
                    }

                }
            } else {
                throw new CarbonDeploymentException("Unsupported Artifact type. Support only .bar files.");
            }
            return artifactPath;
        } else {
            throw new CarbonDeploymentException("Artifact " + artifactFile.getName() + " doesn't exists.");
        }

    }

    @Override
    public void undeploy(Object key) throws CarbonDeploymentException {
        String deploymentName = "";
        try {
            deploymentName = FilenameUtils.getBaseName(key.toString()); //CHECK

            // Remove the deployment from the activiti registry

            DeploymentMetaDataModel undeployModel =
                    activitiDAO.selectDeploymentModel(deploymentName);

            if (undeployModel != null) {
                activitiDAO.deleteDeploymentMetaDataModel(undeployModel);
            } else {
                log.error("File" + deploymentName + "does not exist in activiti metadata registry");
            }
            try {
                File fileToUndeploy = new File(deploymentDir + File.separator + key);

                FileUtils.deleteQuietly(fileToUndeploy);

            } catch (NullPointerException e) {
                log.error("File does not exist in file repository" +
                        deploymentDir + e);
            }
            // Delete all versions of this package from the Activiti engine.
            ProcessEngine engine = BPMNServerHolder.getInstance().getEngine();
            RepositoryService repositoryService = engine.getRepositoryService();
            List<Deployment> deployments =
                    repositoryService.createDeploymentQuery().deploymentName(deploymentName).list();
            if (deployments != null) {
                for (Deployment deployment : deployments) {
                    repositoryService.deleteDeployment(deployment.getId(), true);
                }
            } else {
                log.error("Deployment" + deploymentName + "does not exist in activiti database");
            }

        } catch (ActivitiException e) {
            String msg = "Failed to undeploy BPMN deployment: " + deploymentName;
            log.error(msg, e);
            throw new CarbonDeploymentException(msg, e);
        }
    }

    //Perform version support : update activiti deployment and file repo /registry done
    @Override
    public Object update(Artifact artifact) throws CarbonDeploymentException {
        File artifactFile = artifact.getFile();
        String artifactPath = artifactFile.getAbsolutePath();
        String deploymentName = FilenameUtils.getBaseName(artifactFile.getName());

        //Update activiti engine based deployment
        ProcessEngine engine = BPMNServerHolder.getInstance().getEngine();
        RepositoryService repositoryService = engine.getRepositoryService();
        DeploymentBuilder deploymentBuilder =
                repositoryService.createDeployment().name(deploymentName);
        try {
            ZipInputStream archiveStream =
                    new ZipInputStream(new FileInputStream(artifact.getFile()));
            deploymentBuilder.addZipInputStream(archiveStream);
            deploymentBuilder.deploy();
        } catch (FileNotFoundException e) {
            String errMsg = "Archive stream not found for BPMN repsoitory";
            throw new CarbonDeploymentException(errMsg, e);
        }

        File fileToUpdate = new File(deploymentDir + File.separator + deploymentName);
        try {
            FileUtils.copyFile(artifactFile, fileToUpdate);
        } catch (IOException e) {
            log.error("Unable to copy from " + artifactFile + "to" + fileToUpdate);
        }
        return artifactPath;
    }

    /**
     * Information about BPMN deployments are recorded in 3 places:
     * Activiti database, activiti metadata registry and the file system (deployment folder).
     * If information about a particular deployment is not recorded in all these 3 places, BPS may not work correctly.
     * Therefore, this method checks whether deployments are recorded in all these places and undeploys packages, if
     * they are missing in few places in an inconsistent way.
     */
    public void fixDeployments() {

        // get all added files from file directory
        List<String> fileArchiveNames = new ArrayList<String>();
        File[] fileDeployments = destinationFolder.listFiles();
        if (fileDeployments != null) {
            for (File fileDeployment : fileDeployments) {
                String deploymentName = FilenameUtils.getBaseName(fileDeployment.getName());
                fileArchiveNames.add(deploymentName);
            }
        } else {
            log.error("File deployments returned null");

            // get all deployments in Activiti
            List<String> activitiDeploymentNames = new ArrayList<String>();
            ProcessEngine engine = BPMNServerHolder.getInstance().getEngine();
            RepositoryService repositoryService = engine.getRepositoryService();
            List<Deployment> activitiDeployments = repositoryService.createDeploymentQuery().list();
            for (Deployment deployment : activitiDeployments) {
                String deploymentName = deployment.getName();
                activitiDeploymentNames.add(deploymentName);
            }
            // get all metadata in Activiti registry
            List<String> metaDataDeploymentNames = new ArrayList<String>();
            List<DeploymentMetaDataModel> deploymentMetaDataModelList =
                    activitiDAO.selectAllDeploymentModels();

            if (deploymentMetaDataModelList != null) {
                int deploymentMetaDataModelListSize = deploymentMetaDataModelList.size();
                for (int i = 0; i < deploymentMetaDataModelListSize; i++) {
                    DeploymentMetaDataModel deploymentMetaDataModel =
                            deploymentMetaDataModelList.get(i);

                    if (deploymentMetaDataModel != null) {
                        String deploymentMetadataName = deploymentMetaDataModel.getPackageName();
                        metaDataDeploymentNames.add(deploymentMetadataName);
                    }
                }
            } else {
                log.error("No metadata models can be found in activiti registry");
            }
            // construct the union of all deployments
            Set<String> allDeploymentNames = new HashSet<String>();
            allDeploymentNames.addAll(fileArchiveNames);
            allDeploymentNames.addAll(activitiDeploymentNames);
            allDeploymentNames.addAll(metaDataDeploymentNames);

            for (String deploymentName : allDeploymentNames) {
                try {
                    if (!(fileArchiveNames.contains(deploymentName))) {
                        if (log.isDebugEnabled()) {
                            log.debug(deploymentName +
                                    " has been removed from the deployment folder. Undeploying the package...");
                        }

                        undeploy(deploymentName);
                    } else {
                        if (activitiDeploymentNames.contains(deploymentName) &&
                                !metaDataDeploymentNames.contains(deploymentName)) {
                            if (log.isDebugEnabled()) {
                                log.debug(deploymentName +
                                        " is missing in activiti metadata registry. Undeploying " +
                                        "the package to avoid inconsistencies...");
                            }
                            undeploy(deploymentName);
                        }

                        if (!activitiDeploymentNames.contains(deploymentName) &&
                                metaDataDeploymentNames.contains(deploymentName)) {
                            if (log.isDebugEnabled()) {
                                log.debug(deploymentName +
                                        " is missing in the BPS database. Undeploying the package" +
                                        " to avoid inconsistencies...");
                            }
                            undeploy(deploymentName);
                        }
                    }
                } catch (CarbonDeploymentException e) {
                    log.error("Unable to deploy artifact" + deploymentName + e);
                }
            }
        }
    }

    @Override
    public URL getLocation() {
        return deploymentLocation;
    }

    @Override
    public ArtifactType getArtifactType() {
        return artifactType;
    }

    private boolean isSupportedFile(File file) {
        return SUPPORTED_EXTENSIONS.equalsIgnoreCase(
                FilenameUtils.getExtension(file.getAbsolutePath()));
    }


}

