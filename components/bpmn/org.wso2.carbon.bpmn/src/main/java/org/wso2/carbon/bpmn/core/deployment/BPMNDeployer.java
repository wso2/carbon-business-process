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

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.deployment.AbstractDeployer;
import org.apache.axis2.deployment.DeploymentException;
import org.apache.axis2.deployment.repository.util.DeploymentFileData;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.wso2.carbon.bpmn.core.BPMNConstants;
import org.wso2.carbon.bpmn.core.BPMNServerHolder;
import org.wso2.carbon.bpmn.core.BPSFault;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.registry.api.Registry;
import org.wso2.carbon.registry.api.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.utils.CarbonUtils;

import javax.ws.rs.core.MediaType;
import java.io.File;

/**
 * Deployer implementation for BPMN Packages. This deployer is associated with bpmn directory
 * under repository/deployment/server directory. Currently associated file extension is .bar.
 * Separate deployer instance is created for each tenant.
 * Activiti Engine versions same package if deployed twice. In order to overcome this issue,
 * we are using an additional table which will keep track of the deployed package's md5sum in-order to
 * identify the deployment of a new package.
 *
 */

public class BPMNDeployer extends AbstractDeployer {

    private static Log log = LogFactory.getLog(BPMNDeployer.class);
    private TenantRepository tenantRepository = null;

	/**
	 * Initializes the deployment per tenant
	 *
	 * @param configurationContext axis2 configurationContext
	 */
    @Override
    public void init(ConfigurationContext configurationContext) {

        Integer tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        log.info("Initializing BPMN Deployer for tenant " + tenantId + ".");
        try {
            File tenantRepoFolder = createTenantRepo(configurationContext);
            tenantRepository = BPMNServerHolder.getInstance().getTenantManager().createTenantRepository(tenantId);
            tenantRepository.setRepoFolder(tenantRepoFolder);

             if (!CarbonUtils.isWorkerNode()) {
                tenantRepository.fixDeployments();
            }
        }  catch (BPSFault e) {
            String msg = "Tenant Error: " + tenantId;
            log.error(msg, e);
        }
    }

	/**
	 * Deploys a given bpmn package in acitiviti bpmn engine.
	 * @param deploymentFileData Provide information about the deployment file
	 * @throws DeploymentException On failure , deployment exception is thrown
	 */

    public void deploy(DeploymentFileData deploymentFileData) throws DeploymentException {

	    // Deployment logic is dependent on whether a given node is a worker node or not.Since process
	    // information is shared though a persistence db and process is stored into the database, there
	    // is no need to deploy process in worker nodes.


        // Worker nodes cannot deploy BPMN packages, hence return
        if (isWorkerNode()) {
            return;
        }

        Integer tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
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
        }

        /*/////////Rsource adder tester
        String processId="tttt";
        //Integer tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        RegistryService registryService = BPMNServerHolder.getInstance().getRegistryService();
        Registry configRegistry = null;
        try {
            configRegistry = registryService.getConfigSystemRegistry(tenantId);
            Resource deploymentEntryorProcesses=configRegistry.newCollection();

            //create a resource registry collection "/bpmn/processes/" for processes if not available
            if (!configRegistry.resourceExists("/bpmn/processes")) {
                configRegistry.put("/bpmn/processes/",deploymentEntryorProcesses);
            }

            Resource deploymentEntryForProcess=configRegistry.newCollection();

            //create a resource registry collection "/bpmn/processes/" for processes if not available
            if (!configRegistry.resourceExists("/bpmn/processes/"+processId+"/")) {   //"/bpmn/processes/fff"
                configRegistry.put("/bpmn/processes/"+processId+"/",deploymentEntryForProcess);
            }

            //create a resource registry collection  for the process in collection path "/bpmn/processes/"


           //create a new resource text file to keep process variables
            Resource procVariableJsonResource=configRegistry.newResource();
            //File processFile=new File(processId+"xml")
            String processVariablesJSONString= "[ { \"type\": \"home\", \"number\": \"212 555-1234\" }, { \"type\": \"fax\", \"number\": \"646 555-4567\" } ]";

            //JSONArray jsonArray=new JSONArray(processVariablesJSONString);

            procVariableJsonResource.setContent(processVariablesJSONString);

            procVariableJsonResource.setMediaType(MediaType.APPLICATION_JSON);

            configRegistry.put("/bpmn/processes/"+processId+"/filename.json", procVariableJsonResource);
        } catch (org.wso2.carbon.registry.api.RegistryException e) {
            e.printStackTrace();
        }*/

        //test2
       // Integer tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        String procesDefinitionId="tttt";
        RegistryService registryService = BPMNServerHolder.getInstance().getRegistryService();
        Registry configRegistry = null;

        try {
            configRegistry = registryService.getConfigSystemRegistry(tenantId);
        } catch (org.wso2.carbon.registry.core.exceptions.RegistryException e) {
            e.printStackTrace();
        }

        try {
            Resource processRegistryResource=configRegistry.get("bpmn/processes/"+procesDefinitionId+"/"+"filename.json");
            //log.info("\ncontent:"+processRegistryResource.getContent().toString());

            Object content = processRegistryResource.getContent();
            String output = new String((byte[]) content);
            log.info("\ncontent:"+output);

        } catch (org.wso2.carbon.registry.api.RegistryException e) {
            e.printStackTrace();
        }

    }

	/**
	 * Undeployment operation for Bpmn Deployer
	 *
	 * @param bpmnArchivePath        archivePatch
	 * @throws DeploymentException   Deployment failure will result in this exception
	 */

    public void undeploy(String bpmnArchivePath) throws DeploymentException {

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

    }

	/**
	 *
	 * @param configurationContext axis2 configurationContext
	 * @return                     bpmn repo file
	 * @throws BPSFault        repo creation failure will result in this xception
	 */
    private File createTenantRepo(ConfigurationContext configurationContext) throws BPSFault {
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
    }

    @Override
    public void setDirectory(String s) {
    }

    @Override
    public void setExtension(String s) {
    }

    /**
     * Whether a bps node is worker ( a node that does not participate in archive deployment and only handles
     * input/output . This is determined by looking at the registry read/only property
     * @return
     */
    private boolean isWorkerNode() {
        RegistryService registryService = BPMNServerHolder.getInstance().getRegistryService();
        boolean isWorker = true;
        try {
            isWorker = (registryService.getConfigSystemRegistry().getRegistryContext().isReadOnly());
        } catch (RegistryException e) {
            log.error("Error accessing the configuration registry");
        }
        return isWorker;
    }
}
