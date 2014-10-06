/*
 *
 *  * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package org.wso2.carbon.bpmn.core.deployment;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.impl.ProcessEngineImpl;
import org.activiti.engine.impl.db.DbSqlSession;
import org.activiti.engine.impl.db.DbSqlSessionFactory;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.DeploymentBuilder;
import org.activiti.engine.repository.ProcessDefinition;
import org.apache.axis2.deployment.DeploymentException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.wso2.carbon.bpmn.core.BPMNServerHolder;
import org.wso2.carbon.bpmn.core.BPSException;
import org.wso2.carbon.bpmn.core.Utils;
import org.wso2.carbon.bpmn.core.internal.mapper.DeploymentMapper;
import org.wso2.carbon.bpmn.core.mgt.dao.ActivitiDAO;
import org.wso2.carbon.bpmn.core.mgt.model.DeploymentMetaDataModel;

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
	private ActivitiDAO activitiDAO;

	public TenantRepository(Integer tenantId) {
		this.tenantId = tenantId;
		this.activitiDAO = new ActivitiDAO();
	}

	public File getRepoFolder() {
		return repoFolder;
	}

	public void setRepoFolder(File repoFolder) {
		this.repoFolder = repoFolder;
	}

	/**
	 * Deploys a BPMN package in the Activiti engine. Each BPMN package has an entry in the registry.
	 * Checksum of the latest version of the BPMN package is stored in this entry.
	 * This checksum is used to determine whether a package is a new deployment
	 * (or a new version of an existing package) or a redeployment of an existing package.
	 * We have to ignor the later case. If a package is a new deployment, it is deployed in the Activiti engine.
	 *
	 * @param deploymentContext DeploymentContext
	 * @return true, if artifact was deployed, false, if the artifact has not changed & hence not deployed
	 * @throws DeploymentException if deployment fails
	 */
	public boolean deploy(BPMNDeploymentContext deploymentContext) throws DeploymentException {
		ZipInputStream archiveStream = null;

		try {

			String deploymentName =
					FilenameUtils.getBaseName(deploymentContext.getBpmnArchive().getName());

			// Compare the checksum of the BPMN archive with the currently available checksum in the registry
			// to determine whether this is a new deployment.
			String checksum = Utils.getMD5Checksum(deploymentContext.getBpmnArchive());

			DeploymentMetaDataModel deploymentMetaDataModel =
					activitiDAO.selectTenantAwareDeploymentModel(tenantId.toString(), deploymentName);

			if (log.isDebugEnabled()) {
				log.debug("deploymentName=" + deploymentName + " checksum=" + checksum);
				log.debug("deploymentMetaDataModel=" + deploymentMetaDataModel.toString());
			}

			if (deploymentMetaDataModel != null) {
				if (checksum.equalsIgnoreCase(deploymentMetaDataModel.getCheckSum())) {
					return false;
				}
			}

			ProcessEngineImpl engine =
					(ProcessEngineImpl) BPMNServerHolder.getInstance().getEngine();

			RepositoryService repositoryService = engine.getRepositoryService();
			DeploymentBuilder deploymentBuilder =
					repositoryService.createDeployment().tenantId(tenantId.toString()).
							name(deploymentName);
			archiveStream =
					new ZipInputStream(new FileInputStream(deploymentContext.getBpmnArchive()));
			deploymentBuilder.addZipInputStream(archiveStream);
			Deployment deployment = deploymentBuilder.deploy();

			if (deploymentMetaDataModel == null) {

				deploymentMetaDataModel = new DeploymentMetaDataModel();
				deploymentMetaDataModel.setPackageName(deploymentName);
				deploymentMetaDataModel.setCheckSum(checksum);
				deploymentMetaDataModel.setTenantID(tenantId.toString());
				deploymentMetaDataModel.setId(deployment.getId());

				//call for insertion
				this.activitiDAO.insertDeploymentMetaDataModel(deploymentMetaDataModel);
			} else {
				//call for update
				deploymentMetaDataModel.setCheckSum(checksum);
				this.activitiDAO.updateDeploymentMetaDataModel(deploymentMetaDataModel);
			}

		} catch (Exception e) {
			String errorMessage =
					"Failed to deploy the archive: " + deploymentContext.getBpmnArchive().getName();
			log.error(errorMessage, e);
			throw new DeploymentException(errorMessage, e);
		} finally {
			if (archiveStream != null) {
				try {
					archiveStream.close();
				} catch (IOException e) {
					log.error("Could not close archive stream", e);
					throw new DeploymentException("Could not close archive stream", e);
				}
			}
		}

		return true;
	}

	/**
	 * Undeploys a BPMN package.
	 * This may be called by the BPMN deployer, when a BPMN package is deleted from the deployment folder or by admin services
	 *
	 * @param deploymentName package name to be undeployed
	 * @param force          forceful deletion of package
	 * @throws BPSException Throws if the deployment fails
	 */

	public void undeploy(String deploymentName, boolean force) throws BPSException {

		DeploymentMetaDataModel deploymentMetaDataModel = null;
		SqlSession sqlSession = null;
		try {
			// Remove the deployment from the tenant's registry
			deploymentMetaDataModel = activitiDAO
					.selectTenantAwareDeploymentModel(tenantId.toString(), deploymentName);

			if ((deploymentMetaDataModel == null) && !force) {
				String msg = "Deployment: " + deploymentName + " does not exist.";
				log.warn(msg);
				return;
			}

			ProcessEngineImpl engine = (ProcessEngineImpl) BPMNServerHolder.getInstance().getEngine();

			DbSqlSessionFactory dbSqlSessionFactory =
					(DbSqlSessionFactory) engine.getProcessEngineConfiguration().
					                            getSessionFactories().get(DbSqlSession.class);

			SqlSessionFactory sqlSessionFactory = dbSqlSessionFactory.getSqlSessionFactory();
			sqlSession = sqlSessionFactory.openSession();
			DeploymentMapper deploymentMapper = sqlSession.getMapper(DeploymentMapper.class);
			int rowCount = deploymentMapper.deleteDeploymentMetaData(deploymentMetaDataModel);

			if (log.isDebugEnabled()) {
				log.debug("Total row count deleted=" + rowCount);
			}

			// Remove the deployment archive from the tenant's deployment folder
			File deploymentArchive = new File(repoFolder, deploymentName + ".bar");
			FileUtils.deleteQuietly(deploymentArchive);

			// Delete all versions of this package from the Activiti engine.
			RepositoryService repositoryService = engine.getRepositoryService();
			List<Deployment> deployments =
					repositoryService.createDeploymentQuery().
							deploymentTenantId(tenantId.toString())
					                 .deploymentName(deploymentName).list();
			for (Deployment deployment : deployments) {
				repositoryService.deleteDeployment(deployment.getId());
			}

			//commit metadata
			sqlSession.commit();
		} catch (Exception e) {
			String msg = "Failed to undeploy BPMN deployment: " + deploymentName + " for tenant: " +
			             tenantId;
			log.error(msg, e);

			if (sqlSession != null)
				sqlSession.rollback();
			throw new BPSException(msg, e);
		} finally {
			if (sqlSession != null) {
				sqlSession.close();
			}
		}

	}

	public List<Deployment> getDeployments() throws BPSException {

		ProcessEngine engine = BPMNServerHolder.getInstance().getEngine();
		List<Deployment> tenantDeployments =
				engine.getRepositoryService().createDeploymentQuery()
				      .deploymentTenantId(tenantId.toString()).list();

		return tenantDeployments;
	}

	public List<ProcessDefinition> getDeployedProcessDefinitions() throws BPSException {

		ProcessEngine engine = BPMNServerHolder.getInstance().getEngine();
		return engine.getRepositoryService().createProcessDefinitionQuery()
		      .processDefinitionTenantId(tenantId.toString()).list();
	}

	/**
	 * Information about BPMN deployments are recorded in 3 places:
	 * Activiti database, Registry and the file system (deployment folder).
	 * If information about a particular deployment is not recorded in all these 3 places, BPS may not work correctly.
	 * Therefore, this method checks whether deployments are recorded in all these places and undeploys packages, if
	 * they are missing in few places in an inconsistent way.
	 * <p/>
	 * As there are 3 places, there are 8 ways a package can be placed. These cases are handled as follows:
	 * (1) Whenever a package is not in the deployment folder, it is undeploye (this covers 4 combinations).
	 * (2) If a package is in all 3 places, it is a proper deployment and it is left untouched.
	 * (3) If a package is only in the deployment folder, it is a new deployment. This will be handled by the deployer.
	 * (4) If a package is in the deployment folder AND it is in either registry or Activiti DB (but not both), then it is an inconsistent deployment. This will be undeployed.
	 *
	 * @throws BPSException //TThrows exception if the fix deployment failed
	 */
	public void fixDeployments() throws BPSException {

		// get all deployments in the deployment folder
		List<String> fileArchiveNames = new ArrayList<String>();
		File[] fileDeployments = repoFolder.listFiles();
		if(fileDeployments != null){
			for (File fileDeployment : fileDeployments) {
				String deploymentName = FilenameUtils.getBaseName(fileDeployment.getName());
				fileArchiveNames.add(deploymentName);
			}
		}else{
			log.error("File deployments returned null for tenant"+tenantId);
		}


		// get all deployments in the Activiti DB
		List<String> activitiDeploymentNames = new ArrayList<String>();
		ProcessEngine engine = BPMNServerHolder.getInstance().getEngine();
		RepositoryService repositoryService = engine.getRepositoryService();
		List<Deployment> tenantDeployments =
				repositoryService.createDeploymentQuery().deploymentTenantId(tenantId.toString())
				                 .list();
		for (Deployment deployment : tenantDeployments) {
			String deploymentName = deployment.getName();
			activitiDeploymentNames.add(deploymentName);
		}

		// get all deployments in the registry
		List<String> metaDataDeploymentNames = new ArrayList<String>();
		List<DeploymentMetaDataModel> deploymentMetaDataModelList =
				activitiDAO.selectAllDeploymentModel();

		int deploymentMetaDataModelListSize = deploymentMetaDataModelList.size();

		try {
			for (int i = 0; i < deploymentMetaDataModelListSize; i++) {
				DeploymentMetaDataModel deploymentMetaDataModel =
						deploymentMetaDataModelList.get(i);

				if (deploymentMetaDataModel != null) {
					String deploymentMetadataName = deploymentMetaDataModel.getPackageName();
					metaDataDeploymentNames.add(deploymentMetadataName);
				}
			}
		} catch (Exception ex) {
			String msg = "Failed to obtain all  deploymentMetaDataModel size: " +
			             deploymentMetaDataModelListSize;
			log.error(msg, ex);
			throw new BPSException(msg, ex);
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
					undeploy(deploymentName, true);
				} else {
					if (activitiDeploymentNames.contains(deploymentName) &&
					    !metaDataDeploymentNames.contains(deploymentName)) {
						if (log.isDebugEnabled()) {
							log.debug(deploymentName +
							          " is missing in the registry. Undeploying the package to avoid inconsistencies...");
						}
						undeploy(deploymentName, true);
					}

					if (!activitiDeploymentNames.contains(deploymentName) &&
					    metaDataDeploymentNames.contains(deploymentName)) {
						if (log.isDebugEnabled()) {
							log.debug(deploymentName +
							          " is missing in the BPS database. Undeploying the package to avoid inconsistencies...");
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

