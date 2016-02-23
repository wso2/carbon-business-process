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

import org.apache.axis2.context.ConfigurationContext;
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
import org.wso2.carbon.bpmn.core.mgt.dao.CamundaDAO;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.utils.CarbonUtils;
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
import org.apache.commons.io.FileUtils;


import org.wso2.carbon.bpmn.core.mgt.model.DeploymentMetaDataModelEntity;
import org.wso2.carbon.bpmn.core.mgt.dao.CamundaDAO;

/**
 * Deployer implementation for BPMN Packages. This deployer is associated with bpmn directory
 * under repository/deployment/server directory. Currently associated file extension is .bar.
 * Separate deployer instance is created for each tenant.
 * Camunda Engine versions same package if deployed twice. In order to overcome this issue,
 * we are using an additional table which will keep track of the deployed package's md5sum in-order to
 * identify the deployment of a new package.
 */
public class BPMNDeployer implements Deployer {

	//private static Log log = LogFactory.getLog(BPMNDeployer.class);
	private static final Logger log = LoggerFactory.getLogger(BPMNDeployer.class);
	private static final String DEPLOYMENT_PATH = "file:bpmn";
	private static final String SUPPORTED_EXTENSIONS = "bar";
	private URL deploymentLocation;
	private ArtifactType artifactType;
	private HashMap<Object, List<Object>> deployedArtifacts = new HashMap<>();
	private CamundaDAO camundaDAO;
	private Integer tenantId;
	private String testDir;


	/**
	 * Initializes the DAO for camunda registry queries
	 *
	 * @param
	 */
	@Override
	public void init() {
		log.info("BPMNDeployer initializing");
		artifactType = new ArtifactType<>("BPMN");
		this.tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
		//TODO:create File repository

		try {
			deploymentLocation = new URL(DEPLOYMENT_PATH);
			testDir = "src" + File.separator + "test" + File.separator + "resources" +
			                         File.separator + "carbon-repo" + File.separator + DEPLOYMENT_PATH;

			this.camundaDAO = new CamundaDAO();
		} catch (MalformedURLException |ExceptionInInitializerError e) {
			String msg = "Failed to initialize BPMNDeployer: " + " for tenant: " + tenantId;
			log.error(msg, e);
		}
	}

	/**
	 * Deploys a given bpmn package in acitiviti bpmn engine.
	 *
	 * @param
	 * @throws CarbonDeploymentException On failure , deployment exception is thrown
	 */

	public Object deploy(Artifact artifact) throws CarbonDeploymentException {
		File artifactFile = artifact.getFile();
		String artifactPath = artifactFile.getAbsolutePath();
		String checksum = "";
		ZipInputStream archiveStream = null;
		//check if extension is bar
		if (isSupportedFile(artifactFile)) {

			Integer tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
			String deploymentName = FilenameUtils.getBaseName(artifactFile.getName());
			//get checksum value of new file
			try {
				checksum = Utils.getMD5Checksum(artifactFile);
			} catch (NoSuchAlgorithmException e) {
				log.error("Checksum generation algorithm not found", e);
			} catch (IOException e) {
				log.error("Checksum generation failed for IO operation", e);
			}

			// get stored metadata model from camunda reg table if available
			DeploymentMetaDataModelEntity deploymentMetaDataModel = camundaDAO
					.selectTenantAwareDeploymentModel(tenantId.toString(), deploymentName);

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
				deploymentMetaDataModel.setTenantID(tenantId.toString());
				deploymentMetaDataModel.setId(deployment.getId());
				camundaDAO.insertDeploymentMetaDataModel(deploymentMetaDataModel);

				//TODO:add to file repo
				FileUtils.copyFileToDirectory(artifactFile,testDir);

			} else if (deploymentMetaDataModel != null) { //deployment exists
				// not the same version that is already deployed
				if (!checksum.equalsIgnoreCase(deploymentMetaDataModel.getCheckSum())) {
					// It is not a new deployment, but a version update
					update(artifact); //TODO update new version deployment and file repo
					deploymentMetaDataModel.setCheckSum(checksum);//set new checksum value to model
					// update new version in camunda registry
					camundaDAO.updateDeploymentMetaDataModel(deploymentMetaDataModel);
				}

			}
		}
		return artifactPath;

	}

	public void undeploy(Object key) throws CarbonDeploymentException {
		//TODO : cluster workernode
		String deploymentName = "";
		Integer tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
		try {
			deploymentName = FilenameUtils.getBaseName(key.toString());//CHECK

			// Remove metadata record from  camunda registry

			DeploymentMetaDataModelEntity undeployModel = camundaDAO
					.selectTenantAwareDeploymentModel(tenantId.toString(), deploymentName);

			if (undeployModel != null) {
				camundaDAO.deleteDeploymentMetaDataModel(undeployModel);
			}

			//TODO: Remove from file repo
			File fileToUndeploy = new File(testDir + File.separator + key);
			FileUtils.deleteQuietly(fileToUndeploy);
			////////
			// Delete all versions of this package from the Camunda engine.
			ProcessEngine engine = BPMNServerHolder.getInstance().getEngine();
			RepositoryService repositoryService = engine.getRepositoryService();
			List<Deployment> deployments =
					repositoryService.createDeploymentQuery().deploymentName(deploymentName).list();
			for (Deployment deployment : deployments) {
				repositoryService.deleteDeployment(deployment.getId(), true);
			}

		} catch (ProcessEngineException e) {
			String msg = "Failed to undeploy BPMN deployment: " + deploymentName + " for tenant: " +
			             tenantId;
			log.error(msg, e);
			throw new CarbonDeploymentException(msg, e);
		}
	}

	//Perform version support : update camunda deployment and file repo
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
		File fileToUpdate = new File(testDir + File.separator + deploymentName);
		try {
			FileUtils.copyFile(artifactFile,fileToUpdate);
		} catch (IOException e) {
			log.error("Unable to copy from " + artifactFile + "to" + fileToUpdate);
		}

		return artifactPath;
	}

	//TODO: add method fixDeployments()
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
}

