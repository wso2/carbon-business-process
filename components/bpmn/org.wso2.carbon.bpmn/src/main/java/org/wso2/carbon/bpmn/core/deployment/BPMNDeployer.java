/**
 *  Copyright (c) 2014-2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

<<<<<<< HEAD
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.repository.DeploymentBuilder;
=======
import org.activiti.engine.ActivitiException;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.DeploymentBuilder;
>>>>>>> a97b84a03c59ff4570ff155e58145868fd7a12b6
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bpmn.core.BPMNConstants;
import org.wso2.carbon.bpmn.core.BPMNServerHolder;
import org.wso2.carbon.bpmn.core.Utils;
<<<<<<< HEAD
import org.wso2.carbon.bpmn.core.mgt.dao.CamundaDAO;
import org.wso2.carbon.context.CarbonContext;
=======
import org.wso2.carbon.kernel.utils.Utils.*;
import org.apache.commons.io.FileUtils;
>>>>>>> a97b84a03c59ff4570ff155e58145868fd7a12b6
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
<<<<<<< HEAD

import org.wso2.carbon.kernel.internal.deployment.DeploymentEngine;
import org.wso2.carbon.kernel.utils.Utils.*;
import org.apache.commons.io.FileUtils;

=======
>>>>>>> a97b84a03c59ff4570ff155e58145868fd7a12b6
import java.nio.file.Path;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.Set;
<<<<<<< HEAD

import org.wso2.carbon.bpmn.core.mgt.model.DeploymentMetaDataModelEntity;
import org.wso2.carbon.bpmn.core.mgt.dao.CamundaDAO;
=======
import org.wso2.carbon.bpmn.core.mgt.dao.ActivitiDAO;
import org.wso2.carbon.bpmn.core.mgt.model.DeploymentMetaDataModel;
>>>>>>> a97b84a03c59ff4570ff155e58145868fd7a12b6

/**
 * Deployer implementation for BPMN Packages. This deployer is associated with bpmn directory
 * under repository/deployment/server directory. Currently associated file extension is .bar.
 * Camunda Engine versions same package if deployed twice. In order to overcome this issue,
 * we are using an additional table which will keep track of the deployed package's md5sum in-order to
 * identify the deployment of a new package.
 */
public class BPMNDeployer implements Deployer {

<<<<<<< HEAD
=======

>>>>>>> a97b84a03c59ff4570ff155e58145868fd7a12b6
	private static final Logger log = LoggerFactory.getLogger(BPMNDeployer.class);
	private static final String DEPLOYMENT_PATH = "file:bpmn";
	private static final String SUPPORTED_EXTENSIONS = "bar";
	private URL deploymentLocation;
	private ArtifactType artifactType;
	private HashMap<Object, List<Object>> deployedArtifacts = new HashMap<>();
<<<<<<< HEAD
	private CamundaDAO camundaDAO;
	//private Integer tenantId;
	private String deploymentDir;
	private File destinationFolder;
	private Path home;
=======
	private String deploymentDir;
	private File destinationFolder;
	private Path home;;
    private ActivitiDAO activitiDAO;

    /**
     * Initializes the deployment per tenant
     *
     */
    @Override
    public void init() {
	    artifactType = new ArtifactType<>("bar");
	    try {
		    deploymentLocation = new URL(DEPLOYMENT_PATH);
		    //TEST
		    //            DeploymentEngine d;
		    //			deploymentDir = d.getRepositoryDirectory();
		    //
		    //Get carbon home
		    home = org.wso2.carbon.kernel.utils.Utils.getCarbonHome();
		    deploymentDir = home + File.separator + "repository" + File.separator + "deployment" +
		                    File.separator + "server" + File.separator + deploymentLocation;
		    this.activitiDAO = new ActivitiDAO();
	    } catch (MalformedURLException | ExceptionInInitializerError e) {
		    String msg = "Failed to initialize BPMNDeployer: ";
		    log.error(msg, e);
	    }
	    destinationFolder = new File(deploymentDir);
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
                    archiveStream =
                            new ZipInputStream(new FileInputStream(artifact.getFile()));
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

	            //TODO:add to file repo
	            try {
		            FileUtils.copyFileToDirectory(artifactFile, destinationFolder);
	            } catch (IOException e) {
		            log.error("Unable to add file " + artifactFile + "to directory" +
		                      destinationFolder);
	            }
            }
            else if(deploymentMetaDataModel != null) { //deployment exists
                // not the same version that is already deployed
                if (!checksum.equalsIgnoreCase(deploymentMetaDataModel.getCheckSum())) {
                    // It is not a new deployment, but a version update
                    update(artifact); 
                    deploymentMetaDataModel.setCheckSum(checksum);
                    activitiDAO.updateDeploymentMetaDataModel(deploymentMetaDataModel);
                }

            }
        }
        return artifactPath;
>>>>>>> a97b84a03c59ff4570ff155e58145868fd7a12b6

	/**
	 * Initializes the DAO for camunda registry queries
	 *
	 * @param
	 */
	@Override
	public void init() {
		log.info("BPMNDeployer initializing");
		artifactType = new ArtifactType<>("bar");
		try {
			deploymentLocation = new URL(DEPLOYMENT_PATH);
			//TEST
//            DeploymentEngine d;
//			deploymentDir = d.getRepositoryDirectory();
			//
			//Get carbon home
			home = org.wso2.carbon.kernel.utils.Utils.getCarbonHome();
			deploymentDir = home + File.separator + "repository" + File.separator + "deployment" +
			                File.separator + "server" + File.separator + deploymentLocation;
			this.camundaDAO = new CamundaDAO();
		} catch (MalformedURLException | ExceptionInInitializerError e) {
			String msg = "Failed to initialize BPMNDeployer: ";
			log.error(msg, e);
		}
		destinationFolder = new File(deploymentDir);
	}

<<<<<<< HEAD
	/**
	 * Deploys a given bpmn package in acitiviti bpmn engine.
	 *
	 * @param artifact represents the bpmn.bar file to be deployed
	 * @throws CarbonDeploymentException On failure , deployment exception is thrown
	 */
=======
    public void undeploy(Object key) throws CarbonDeploymentException {
        String deploymentName = "";
	    try {
             deploymentName = FilenameUtils.getBaseName(key.toString());//CHECK
>>>>>>> a97b84a03c59ff4570ff155e58145868fd7a12b6

	public Object deploy(Artifact artifact) throws CarbonDeploymentException {
		File artifactFile = artifact.getFile();
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

<<<<<<< HEAD
			// get stored metadata model from camunda reg table if available
			DeploymentMetaDataModelEntity deploymentMetaDataModel = camundaDAO
					.selectDeploymentModel(deploymentName);

			if (log.isDebugEnabled()) {
				log.debug("deploymentName=" + deploymentName + " checksum=" + checksum);
				log.debug("deploymentMetaDataModel=" + deploymentMetaDataModel.toString());
			}
			//A new deployment of artifact
			if (deploymentMetaDataModel == null) {
				//deploy to camunda engine
				ProcessEngine engine = BPMNServerHolder.getInstance().getEngine();
				RepositoryService repositoryService = engine.getRepositoryService();
				DeploymentBuilder deploymentBuilder = repositoryService.createDeployment().
						name(deploymentName);
				try {
					archiveStream = new ZipInputStream(new FileInputStream(artifact.getFile()));
				} catch (FileNotFoundException e) {
					String errMsg = "Archive stream not found for BPMN repsoitory";
					throw new CarbonDeploymentException(errMsg, e);
				}

				deploymentBuilder.addZipInputStream(archiveStream);
				Deployment deployment = deploymentBuilder.deploy();

				//Store deployed metadata record in camunda registry

				deploymentMetaDataModel = new DeploymentMetaDataModelEntity();
				deploymentMetaDataModel.setPackageName(deploymentName);
				deploymentMetaDataModel.setCheckSum(checksum);
				//deploymentMetaDataModel.setTenantID(tenantId.toString());
				deploymentMetaDataModel.setId(deployment.getId());
				camundaDAO.insertDeploymentMetaDataModel(deploymentMetaDataModel);

				//TODO:add to file repo
				try {
					FileUtils.copyFileToDirectory(artifactFile, destinationFolder);
				} catch (IOException e) {
					log.error("Unable to add file " + artifactFile + "to directory" +
					          destinationFolder);
				}

			} else if (deploymentMetaDataModel != null) { //deployment exists
				// not the same version that is already deployed
				if (!checksum.equalsIgnoreCase(deploymentMetaDataModel.getCheckSum())) {
					//  version update to existing deployment
					update(artifact);
					deploymentMetaDataModel.setCheckSum(checksum);//set new checksum value to model
					// update new version in camunda registry
					camundaDAO.updateDeploymentMetaDataModel(deploymentMetaDataModel);
				}

			}
		}
		return artifactPath;

	}

	/**
	 * Undeploy the artifact related to provided deployment key from file repository,
	 * camunda and metadata
	 *
	 * @param key deployedArtifactName
	 * @throws CarbonDeploymentException
	 */

	public void undeploy(Object key) throws CarbonDeploymentException {

		String deploymentName = "";

		try {
			deploymentName = FilenameUtils.getBaseName(key.toString());//CHECK

			// Remove metadata record from  camunda registry

			DeploymentMetaDataModelEntity undeployModel = camundaDAO
					.selectDeploymentModel(deploymentName);

			if (undeployModel != null) {
				camundaDAO.deleteDeploymentMetaDataModel(undeployModel);
			} else {
				log.error("File" + deploymentName + "does not exist in camunda metadata registry");
			}

			//TODO: Remove from file repo
			File fileToUndeploy = new File(deploymentDir + File.separator + key);
			if (fileToUndeploy != null) {
				FileUtils.deleteQuietly(fileToUndeploy);
			} else {
				log.error("File" + fileToUndeploy + "does not exist in file repository" +
				          deploymentDir);
			}

			// Delete all versions of this package from the Camunda engine.
			ProcessEngine engine = BPMNServerHolder.getInstance().getEngine();
			RepositoryService repositoryService = engine.getRepositoryService();
			List<Deployment> deployments =
					repositoryService.createDeploymentQuery().deploymentName(deploymentName).list();
			if (deployments != null) {
				for (Deployment deployment : deployments) {
					repositoryService.deleteDeployment(deployment.getId(), true);
				}
			} else {
				log.error("Deployment" + deploymentName + "does not exist in camunda database");
			}

		} catch (ProcessEngineException e) {
			String msg = "Failed to undeploy BPMN deployment: " + deploymentName ;
			log.error(msg, e);
			throw new CarbonDeploymentException(msg, e);
		}
	}

	/**
	 * Perform version support in deployments : update camunda deployment and file repository
	 *
	 * @param artifact to be deployed
	 * @return artifactPath
	 * @throws CarbonDeploymentException
	 */
	public Object update(Artifact artifact) throws CarbonDeploymentException {
		File artifactFile = artifact.getFile();
		String artifactPath = artifactFile.getAbsolutePath();
		String deploymentName = artifactFile.getName();

		//Update camunda engine based deployment
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

		//TODO: update file repo
		File fileToUpdate = new File(deploymentDir + File.separator + deploymentName);
		try {
			FileUtils.copyFile(artifactFile, fileToUpdate);
		} catch (IOException e) {
			log.error("Unable to copy from " + artifactFile + "to" + fileToUpdate);
		}

		return artifactPath;
	}
=======
            DeploymentMetaDataModel undeployModel = activitiDAO.selectDeploymentModel(deploymentName);

            if(undeployModel != null)
            {
                activitiDAO.deleteDeploymentMetaDataModel(undeployModel);
            }
            else {
	            log.error("File" + deploymentName + "does not exist in camunda metadata registry");
            }
		    //TODO: Remove from file repo
		    File fileToUndeploy = new File(deploymentDir + File.separator + key);
		    if (fileToUndeploy != null) {
			    FileUtils.deleteQuietly(fileToUndeploy);
		    } else {
			    log.error("File" + fileToUndeploy + "does not exist in file repository" +
			              deploymentDir);
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
			    log.error("Deployment" + deploymentName + "does not exist in camunda database");
		    }

        }
        catch(ActivitiException e) {
            String msg = "Failed to undeploy BPMN deployment: " + deploymentName;
            log.error(msg, e);
            throw new CarbonDeploymentException(msg, e);
        }
    }

  //Perform version support : update activiti deployment and file repo /registry done
    public Object update(Artifact artifact) throws CarbonDeploymentException {
        File artifactFile = artifact.getFile();
        String artifactPath = artifactFile.getAbsolutePath();
        String deploymentName = artifactFile.getName();

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

        //TODO: update file repo
	    File fileToUpdate = new File(deploymentDir + File.separator + deploymentName);
	    try {
		    FileUtils.copyFile(artifactFile, fileToUpdate);
	    } catch (IOException e) {
		    log.error("Unable to copy from " + artifactFile + "to" + fileToUpdate);
	    }
        return artifactPath;
    }
>>>>>>> a97b84a03c59ff4570ff155e58145868fd7a12b6

	/**
	 * Information about BPMN deployments are recorded in 3 places:
	 * Camunda database, camunda metadata registry and the file system (deployment folder).
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
<<<<<<< HEAD
		}

		// get all deployments in  Camunda
		List<String> camundaDeploymentNames = new ArrayList<String>();
		ProcessEngine engine = BPMNServerHolder.getInstance().getEngine();
		RepositoryService repositoryService = engine.getRepositoryService();
		List<Deployment> camundaDeployments = repositoryService.createDeploymentQuery().list();
		for (Deployment deployment : camundaDeployments) {
			String deploymentName = deployment.getName();
			camundaDeploymentNames.add(deploymentName);
		}
		// get all metadata in Camunda registry
		List<String> metaDataDeploymentNames = new ArrayList<String>();
		List<DeploymentMetaDataModelEntity> deploymentMetaDataModelList =
				camundaDAO.selectAllDeploymentModels();

		if (deploymentMetaDataModelList != null) {
			int deploymentMetaDataModelListSize = deploymentMetaDataModelList.size();
			for (int i = 0; i < deploymentMetaDataModelListSize; i++) {
				DeploymentMetaDataModelEntity deploymentMetaDataModel =
						deploymentMetaDataModelList.get(i);

				if (deploymentMetaDataModel != null) {
					String deploymentMetadataName = deploymentMetaDataModel.getPackageName();
					metaDataDeploymentNames.add(deploymentMetadataName);
				}
			}
		} else {
			log.error("No metadata models can be found in camunda registry");
		}
		// construct the union of all deployments
		Set<String> allDeploymentNames = new HashSet<String>();
		allDeploymentNames.addAll(fileArchiveNames);
		allDeploymentNames.addAll(camundaDeploymentNames);
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
					if (camundaDeploymentNames.contains(deploymentName) &&
					    !metaDataDeploymentNames.contains(deploymentName)) {
						if (log.isDebugEnabled()) {
							log.debug(deploymentName +
							          " is missing in camunda metadata registry. Undeploying the package to avoid inconsistencies...");
						}
						undeploy(deploymentName);
					}

					if (!camundaDeploymentNames.contains(deploymentName) &&
					    metaDataDeploymentNames.contains(deploymentName)) {
						if (log.isDebugEnabled()) {
							log.debug(deploymentName +
							          " is missing in the BPS database. Undeploying the package to avoid inconsistencies...");
						}
						undeploy(deploymentName);
					}
				}
			} catch (CarbonDeploymentException e) {
				log.error("Unable to deploy artifact" + deploymentName + e);
			}
		}
	}

	public void setLocation(URL deploymentLocation) {
		this.deploymentLocation = deploymentLocation;
	}

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
	 *
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
=======

			// get all deployments in Activiti
			List<String> camundaDeploymentNames = new ArrayList<String>();
			ProcessEngine engine = BPMNServerHolder.getInstance().getEngine();
			RepositoryService repositoryService = engine.getRepositoryService();
			List<Deployment> camundaDeployments = repositoryService.createDeploymentQuery().list();
			for (Deployment deployment : camundaDeployments) {
				String deploymentName = deployment.getName();
				camundaDeploymentNames.add(deploymentName);
			}
			// get all metadata in Activiti registry
			List<String> metaDataDeploymentNames = new ArrayList<String>();
			List<DeploymentMetaDataModel> deploymentMetaDataModelList =
					activitiDAO.selectAllDeploymentModels();

			if (deploymentMetaDataModelList != null) {
				int deploymentMetaDataModelListSize = deploymentMetaDataModelList.size();
				for (int i = 0; i < deploymentMetaDataModelListSize; i++) {
					DeploymentMetaDataModel deploymentMetaDataModel = deploymentMetaDataModelList.get(i);

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
			allDeploymentNames.addAll(camundaDeploymentNames);
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
						if (camundaDeploymentNames.contains(deploymentName) && !metaDataDeploymentNames.contains(deploymentName)) {
							if (log.isDebugEnabled()) {
								log.debug(deploymentName +
								          " is missing in activiti metadata registry. Undeploying the package to avoid inconsistencies...");
							}
							undeploy(deploymentName);
						}

						if (!camundaDeploymentNames.contains(deploymentName) &&
						    metaDataDeploymentNames.contains(deploymentName)) {
							if (log.isDebugEnabled()) {
								log.debug(deploymentName +
								          " is missing in the BPS database. Undeploying the package to avoid inconsistencies...");
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
    public URL getLocation() {
        return deploymentLocation;
    }

    public ArtifactType getArtifactType() {
        return artifactType;
    }

    private boolean isSupportedFile(File file) {
        return SUPPORTED_EXTENSIONS.equalsIgnoreCase(getFileExtension(file));
    }

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
>>>>>>> a97b84a03c59ff4570ff155e58145868fd7a12b6
}

