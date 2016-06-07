/**
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.bpmn.core.db.dao;

import org.activiti.engine.ManagementService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.impl.cmd.AbstractCustomSqlExecution;
import org.activiti.engine.impl.cmd.CustomSqlExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.bpmn.core.db.mapper.DeploymentMapper;
import org.wso2.carbon.bpmn.core.db.model.DeploymentMetaDataModel;
import org.wso2.carbon.bpmn.core.internal.BPMNServerHolder;

import java.util.List;

/**
 * This class provides the implementation interface between TenantRepository object and DeploymentMapper interface.
 * Implements the execute method for each type of queries of CustomSqlExecution interface
 */
public class ActivitiDAO {

    private static final Logger log = LoggerFactory.getLogger(DeploymentMapper.class);

    private ManagementService managementService = null;

    public ActivitiDAO() {
        ProcessEngine engine = BPMNServerHolder.getInstance().getEngine();
        managementService = engine.getManagementService();
    }

    /**
     * invokes the DeploymentMapper.selectMetaData for a given tenant id and package name.
     *
     * @param bpmnPackageName package name
     * @return DeploymentMetaDataModel object
     */
    public DeploymentMetaDataModel selectDeploymentModel(final String bpmnPackageName) {
        CustomSqlExecution<DeploymentMapper, DeploymentMetaDataModel> customSqlExecution =
                new GetDeploymentMetaDataModelSqlExecution(bpmnPackageName);

        DeploymentMetaDataModel deploymentMetaDataModel =
                managementService.executeCustomSql(customSqlExecution);
        if (log.isDebugEnabled()) {

            if (deploymentMetaDataModel != null) {
                log.debug("DeploymentDataModel exists when selecting models=" +
                        deploymentMetaDataModel.getId());
            } else {
                log.debug("DeploymentDataModel null when selecting models");
            }
        }
        return deploymentMetaDataModel;
    }

    /**
     * invokes the DeploymentMapper.selectAllMetaData to retrieve all rows from BPS_BPMN_DEPLOYMENT_METADATA.
     *
     * @return each row will be returned as DeploymentMetaDataModel with in list
     */
    public List<DeploymentMetaDataModel> selectAllDeploymentModels() {

        CustomSqlExecution<DeploymentMapper, List<DeploymentMetaDataModel>> customSqlExecution =
                new GetAllDeploymentMetaDataModelSqlExecution();

        return managementService.executeCustomSql(customSqlExecution);
    }

    /**
     * invokes the DeploymentMapper.insertDeploymentMetaData to insert a new row from BPS_BPMN_DEPLOYMENT_METADATA
     *
     * @param deploymentMetaDataModel Object to be inserted in to the table
     */
    public void insertDeploymentMetaDataModel(
            final DeploymentMetaDataModel deploymentMetaDataModel) {

        CustomSqlExecution<DeploymentMapper, Integer> customSqlExecution =
                new InsertDeploymentMetaDataModelSqlExecution(deploymentMetaDataModel);

        Integer rowCount = managementService.executeCustomSql(customSqlExecution);

        if (log.isDebugEnabled()) {
            log.debug("insertDeploymentMetaDataModel" + rowCount);
        }

    }

    /**
     * invokes the DeploymentMapper.updateDeploymentMetaData to insert a new row from BPS_BPMN_DEPLOYMENT_METADATA
     *
     * @param deploymentMetaDataModel Object to be updated in to the table
     */
    public void updateDeploymentMetaDataModel(
            final DeploymentMetaDataModel deploymentMetaDataModel) {

        CustomSqlExecution<DeploymentMapper, Integer> customSqlExecution =
                new UpdateDeploymentMetaDataModelSqlExecution(deploymentMetaDataModel);

        Integer rowCount = managementService.executeCustomSql(customSqlExecution);

        if (log.isDebugEnabled()) {
            log.debug("updated DeploymentMetaDataModel" + rowCount);
        }
    }

    /**
     * invokes the DeploymentMapper.deleteDeploymentMetaData to delete a new row from BPS_BPMN_DEPLOYMENT_METADATA
     *
     * @param deploymentMetaDataModel the object to be deleted
     * @return total number of rows deleted
     */
    public int deleteDeploymentMetaDataModel(
            final DeploymentMetaDataModel deploymentMetaDataModel) {

        CustomSqlExecution<DeploymentMapper, Integer> customSqlExecution =
                new DeleteDeploymentMetaDataModelSqlExecution(deploymentMetaDataModel);

        Integer rowCount = managementService.executeCustomSql(customSqlExecution);

        if (log.isDebugEnabled()) {
            log.debug("deleteDeploymentMetaDataModel" + rowCount);
        }

        return rowCount;
    }

    static class GetDeploymentMetaDataModelSqlExecution extends AbstractCustomSqlExecution<DeploymentMapper,
            DeploymentMetaDataModel> {

        String bpmnPackageName;

        GetDeploymentMetaDataModelSqlExecution(String packageName) {
            super(DeploymentMapper.class);
            bpmnPackageName = packageName;
        }

        public DeploymentMetaDataModel execute(DeploymentMapper deploymentMapper) {
            return deploymentMapper.selectMetaData(bpmnPackageName);
        }

    }

    static class GetAllDeploymentMetaDataModelSqlExecution
            extends AbstractCustomSqlExecution<DeploymentMapper, List<DeploymentMetaDataModel>> {

        GetAllDeploymentMetaDataModelSqlExecution() {
            super(DeploymentMapper.class);
        }

        public List<DeploymentMetaDataModel> execute(DeploymentMapper deploymentMapper) {
            return deploymentMapper.selectAllMetaData();
        }

    }

    static class InsertDeploymentMetaDataModelSqlExecution extends AbstractCustomSqlExecution<DeploymentMapper,
            Integer> {

        DeploymentMetaDataModel deploymentMetaDataModel;

        InsertDeploymentMetaDataModelSqlExecution(DeploymentMetaDataModel model) {
            super(DeploymentMapper.class);
            deploymentMetaDataModel = model;
        }

        public Integer execute(DeploymentMapper deploymentMapper) {
            return deploymentMapper.insertDeploymentMetaData(deploymentMetaDataModel);
        }

    }

    static class UpdateDeploymentMetaDataModelSqlExecution extends AbstractCustomSqlExecution<DeploymentMapper,
            Integer> {

        DeploymentMetaDataModel deploymentMetaDataModel;

        UpdateDeploymentMetaDataModelSqlExecution(DeploymentMetaDataModel model) {
            super(DeploymentMapper.class);
            deploymentMetaDataModel = model;
        }

        public Integer execute(DeploymentMapper deploymentMapper) {
            return deploymentMapper.updateDeploymentMetaData(deploymentMetaDataModel);
        }

    }

    static class DeleteDeploymentMetaDataModelSqlExecution extends AbstractCustomSqlExecution<DeploymentMapper,
            Integer> {

        DeploymentMetaDataModel deploymentMetaDataModel;

        DeleteDeploymentMetaDataModelSqlExecution(DeploymentMetaDataModel model) {
            super(DeploymentMapper.class);
            deploymentMetaDataModel = model;
        }

        public Integer execute(DeploymentMapper deploymentMapper) {
            return deploymentMapper.deleteDeploymentMetaData(deploymentMetaDataModel);
        }

    }

}
