/*
 * Copyright (c) 2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.humantask.deployer;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.deployment.AbstractDeployer;
import org.apache.axis2.deployment.DeploymentException;
import org.apache.axis2.deployment.repository.util.DeploymentFileData;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.humantask.client.api.leantask.humantask.TLeanTask;
import org.wso2.carbon.humantask.core.HumanTaskConstants;
import org.wso2.carbon.humantask.core.HumanTaskServer;
import org.wso2.carbon.humantask.core.store.HumanTaskStore;
import org.wso2.carbon.humantask.deployer.internal.HumanTaskDeployerServiceComponent;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.utils.CarbonUtils;

import java.io.File;
import java.util.List;

/**
 * Handles the deployment of human task artifacts. Artifact can be a ZIP file or TODO registry collection.
 * Integration layer will delegate deployment of artifacts to this. There will be one deployer instance per each tenant.
 */
public class HumanTaskDeployer extends AbstractDeployer {
    private static Log log = LogFactory.getLog(HumanTaskDeployer.class);
    private HumanTaskStore humanTaskStore;

    public void init(ConfigurationContext configurationContext) {
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        log.info("Initializing HumanTask Deployer for tenant " + tenantId + ".");

        try {
            HumanTaskDeployerServiceComponent.getTenantRegistryLoader().
                    loadTenantRegistry(tenantId);
            createHumanTaskRepository(configurationContext);
        } catch (DeploymentException e) {
            log.warn(String.format("Human Task Repository creation failed for tenant id [%d]", tenantId), e);
        } catch (RegistryException e) {
            log.warn("Initializing HumanTask Deployer failed for tenant " + tenantId);
        }
        HumanTaskServer humantaskServer = HumanTaskDeployerServiceComponent.getHumanTaskServer();
        humanTaskStore = humantaskServer.getTaskStoreManager().
                createHumanTaskStoreForTenant(tenantId, configurationContext);
    }

    public void deploy(DeploymentFileData deploymentFileData) throws DeploymentException {
        log.info("Deploying HumanTask archive [ " + deploymentFileData.getName() + " ]");
        try {
            humanTaskStore.deploy(deploymentFileData.getFile());
        } catch (Exception e) {
            String errorMessage = "Error deploying HumanTask package : " + deploymentFileData.getName();
            log.error(errorMessage, e);
            throw new DeploymentException(errorMessage, e);
        }
    }

    public void setDirectory(String s) {
    }

    public void setExtension(String s) {
    }

    /**
     * Human Task Un-deployment.
     *
     * @param htArchiveFile : Un-deployed file path.
     * @throws DeploymentException :
     */
    public void undeploy(String htArchiveFile) throws DeploymentException {
        File humanTaskArchiveFile = new File(htArchiveFile);
        if (humanTaskArchiveFile.exists()) {
            // By default, axis2 deployer will try to un deploy the existing archive before trying to deploy the new
            // archive for update of the existing archive resulting in undeploy method being called before deploy method
            // However, for human task scenario, since we support task versioning, we should not undeploy for update
            if (log.isTraceEnabled()) {
                log.trace("Human task package was updated, hence no need to undeploy the HumanTask package  ");
            }
            return;
        }
        String unDeployedPackageName = FilenameUtils.removeExtension(humanTaskArchiveFile.getName());
        humanTaskStore.unDeploy(unDeployedPackageName);
    }

    private void createHumanTaskRepository(ConfigurationContext configCtx) throws DeploymentException {
        String axisRepoPath = configCtx.getAxisConfiguration().getRepository().getPath();
        if (CarbonUtils.isURL(axisRepoPath)) {
            throw new DeploymentException("URL Repositories are not supported: " + axisRepoPath);
        }
        File tenantsRepository = new File(axisRepoPath);
        File humanTaskRepo = new File(tenantsRepository, HumanTaskConstants.HUMANTASK_REPO_DIRECTORY);

        if (!humanTaskRepo.exists()) {
            boolean status = humanTaskRepo.mkdir();
            if (!status) {
                throw new DeploymentException("Failed to create HumanTask repository directory " +
                        humanTaskRepo.getAbsolutePath() + ".");
            }
        }
    }

<<<<<<< HEAD
    /**
     * can be used to load  lean task data when server is restarting
     * @param humanTaskServer
     */
    /*private void retrieveLeanTaskData(HumanTaskServer humanTaskServer) {
        String sql = "select * from HT_LEANTASK";
        ResultSet resultSet = null;
        try {
            Connection connection = humanTaskServer.getTaskEngine().getDaoConnectionFactory().getDataSource().getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(sql, ResultSet.TYPE_SCROLL_SENSITIVE,
                    ResultSet.CONCUR_UPDATABLE);
            resultSet = preparedStatement.executeQuery();
            while(resultSet.next()) {
                log.info("Testing DB data ****" + resultSet.getString(1) + resultSet.getString(2) + resultSet.getString(3) + resultSet.getString(4) + resultSet.getString(5));
            }
            preparedStatement.close();
            resultSet.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    } */
=======

>>>>>>> 8fad7dee1bf193d56667148f2ca320ceb615d5d2
}
