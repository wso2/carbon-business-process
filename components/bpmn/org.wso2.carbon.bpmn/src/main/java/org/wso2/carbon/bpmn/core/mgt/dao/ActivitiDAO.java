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

package org.wso2.carbon.bpmn.core.mgt.dao;

import org.activiti.engine.ManagementService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.impl.cmd.AbstractCustomSqlExecution;
import org.activiti.engine.impl.cmd.CustomSqlExecution;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ibatis.session.RowBounds;
import org.wso2.carbon.bpmn.core.BPMNServerHolder;
import org.wso2.carbon.bpmn.core.internal.mapper.DeploymentMapper;
import org.wso2.carbon.bpmn.core.internal.mapper.SubstitutesMapper;
import org.wso2.carbon.bpmn.core.mgt.model.DeploymentMetaDataModel;
import org.wso2.carbon.bpmn.core.mgt.model.PaginatedSubstitutesDataModel;
import org.wso2.carbon.bpmn.core.mgt.model.SubstitutesDataModel;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * This class provides the implementation interface between TenantRepository object and DeploymentMapper interface.
 * Implements the execute method for each type of queries of CustomSqlExecution interface
 */
public class ActivitiDAO {

    private static final Log log = LogFactory.getLog(DeploymentMapper.class);

    private ManagementService managementService = null;

    public ActivitiDAO() {
        ProcessEngine engine = BPMNServerHolder.getInstance().getEngine();
        managementService = engine.getManagementService();
    }

	/**
	 * invokes the DeploymentMapper.selectMetaData for a given tenant id and package name
	 *
	 * @param tenantID        tenant id
	 * @param bpmnPackageName package name
	 * @return                DeploymentMetaDataModel object
	 */
    public DeploymentMetaDataModel selectTenantAwareDeploymentModel(final String tenantID, final String bpmnPackageName){

        CustomSqlExecution<DeploymentMapper, DeploymentMetaDataModel> customSqlExecution =
                new AbstractCustomSqlExecution<DeploymentMapper, DeploymentMetaDataModel>(DeploymentMapper.class) {
                    public DeploymentMetaDataModel execute(DeploymentMapper deploymentMapper) {
                        return deploymentMapper.selectMetaData(tenantID, bpmnPackageName);
                    }
                };

        DeploymentMetaDataModel deploymentMetaDataModel = managementService.executeCustomSql(customSqlExecution);
	    if(log.isDebugEnabled()) {

		    if(deploymentMetaDataModel != null) {
			    log.debug("DeploymentDataModel exists when selecting models=" + deploymentMetaDataModel.getId());
		    }
		    else {
			    log.debug("DeploymentDataModel null when selecting models");
		    }
	    }
        return deploymentMetaDataModel;
    }

	/**
	 * invokes the DeploymentMapper.selectAllMetaData to retrieve all rows from BPS_BPMN_DEPLOYMENT_METADATA
	 *
	 * @return each row will be returned as DeploymentMetaDataModel with in list
	 */
    public List<DeploymentMetaDataModel> selectAllDeploymentModel(){

        CustomSqlExecution<DeploymentMapper,  List<DeploymentMetaDataModel> > customSqlExecution =
                new AbstractCustomSqlExecution<DeploymentMapper,  List<DeploymentMetaDataModel> >(DeploymentMapper.class) {
                    public  List<DeploymentMetaDataModel>  execute(DeploymentMapper deploymentMapper) {
                        return deploymentMapper.selectAllMetaData();
                    }
                };

        return managementService.executeCustomSql(customSqlExecution);
	}

	/**
	 * invokes the DeploymentMapper.insertDeploymentMetaData to insert a new row from BPS_BPMN_DEPLOYMENT_METADATA
	 *
	 * @param deploymentMetaDataModel Object to be inserted in to the table
	 */
    public void insertDeploymentMetaDataModel(final DeploymentMetaDataModel deploymentMetaDataModel){

        CustomSqlExecution<DeploymentMapper, Integer> customSqlExecution =
                new AbstractCustomSqlExecution<DeploymentMapper, Integer>(DeploymentMapper.class) {
                    public Integer execute(DeploymentMapper deploymentMapper) {
                        return deploymentMapper.insertDeploymentMetaData(deploymentMetaDataModel);
                    }
                };

        Integer rowCount = managementService.executeCustomSql(customSqlExecution);

	    if(log.isDebugEnabled()) {
		    log.debug("insertDeploymentMetaDataModel" + rowCount);
	    }

    }

	/**
	 * invokes the DeploymentMapper.updateDeploymentMetaData to insert a new row from BPS_BPMN_DEPLOYMENT_METADATA
	 *
	 * @param deploymentMetaDataModel Object to be updated in to the table
	 */
    public void updateDeploymentMetaDataModel(final DeploymentMetaDataModel deploymentMetaDataModel){

        CustomSqlExecution<DeploymentMapper, Integer> customSqlExecution =
                new AbstractCustomSqlExecution<DeploymentMapper, Integer>(DeploymentMapper.class) {
                    public Integer execute(DeploymentMapper deploymentMapper) {
                        return deploymentMapper.updateDeploymentMetaData(deploymentMetaDataModel);
                    }
                };

        Integer rowCount = managementService.executeCustomSql(customSqlExecution);

	    if(log.isDebugEnabled()) {
		    log.debug("updated DeploymentMetaDataModel" + rowCount);
	    }
    }

	/**
	 * invokes the DeploymentMapper.deleteDeploymentMetaData to delete a new row from BPS_BPMN_DEPLOYMENT_METADATA
	 *
	 * @param deploymentMetaDataModel the object to be deleted
	 * @return total number of rows deleted
	 */
    public int deleteDeploymentMetaDataModel(final DeploymentMetaDataModel deploymentMetaDataModel){

        CustomSqlExecution<DeploymentMapper, Integer> customSqlExecution =
                new AbstractCustomSqlExecution<DeploymentMapper, Integer>(DeploymentMapper.class) {
                    public Integer execute(DeploymentMapper deploymentMapper) {
                        return deploymentMapper.deleteDeploymentMetaData(deploymentMetaDataModel);
                    }
                };

        Integer rowCount = managementService.executeCustomSql(customSqlExecution);

	    if(log.isDebugEnabled()) {
		    log.debug("deleteDeploymentMetaDataModel" + rowCount);
	    }

        return rowCount;
    }

    /**
     * Insert new substitute record. Transitive substitute field is not updated here.
     * @param substitutesDataModel
     * @return inserted row count. Ideally should return 1.
     */
    public int insertSubstitute(final SubstitutesDataModel substitutesDataModel){

        CustomSqlExecution<SubstitutesMapper, Integer> customSqlExecution =
                new AbstractCustomSqlExecution<SubstitutesMapper, Integer>(SubstitutesMapper.class) {
                    public Integer execute(SubstitutesMapper substitutesMapper) {
                        return substitutesMapper.insertSubstitute(substitutesDataModel);
                    }
                };

        return managementService.executeCustomSql(customSqlExecution);
    }

    /**
     * Return substitute information for the given user
     * @param user
     * @param tenantId
     * @return SubstituteDataModel or null if not exist
     */
    public SubstitutesDataModel selectSubstituteInfo(final String user, final int tenantId){

        CustomSqlExecution<SubstitutesMapper,  SubstitutesDataModel > customSqlExecution =
                new AbstractCustomSqlExecution<SubstitutesMapper, SubstitutesDataModel>(SubstitutesMapper.class) {
                    public  SubstitutesDataModel  execute(SubstitutesMapper substitutesMapper) {
                        return substitutesMapper.selectSubstitute(user, tenantId);
                    }
                };

        return managementService.executeCustomSql(customSqlExecution);
    }

    /**
     * Update the substitute record for given user
     * @param substitutesDataModel
     * @return updated row count. Ideally should return 1.
     */
    public int updateSubstituteInfo(final SubstitutesDataModel substitutesDataModel) {
        substitutesDataModel.setUpdated(new Date());
        CustomSqlExecution<SubstitutesMapper, Integer> customSqlExecution =
                new AbstractCustomSqlExecution<SubstitutesMapper, Integer>(SubstitutesMapper.class) {
                    public Integer execute(SubstitutesMapper substitutesMapper) {
                        return substitutesMapper.updateSubstitute(substitutesDataModel);
                    }
                };
        return managementService.executeCustomSql(customSqlExecution);
    }

    /**
     * Return the row count where the given user acts as the substitute
     * @param substitute
     * @param tenantId
     * @return row count
     */
    public int countUserAsSubstitute(final String substitute, final int tenantId){

        CustomSqlExecution<SubstitutesMapper, Integer> customSqlExecution =
                new AbstractCustomSqlExecution<SubstitutesMapper, Integer>(SubstitutesMapper.class) {
                    public Integer execute(SubstitutesMapper substitutesMapper) {
                        return substitutesMapper.countUserAsSubstitute(substitute, tenantId);
                    }
                };

        return managementService.executeCustomSql(customSqlExecution);
    }

    /**
     * Select all substitutes for given tenant Id
     * @param tenantId
     * @return Map with User as key and SubstitutesDataModel as value
     */
    public Map<String, SubstitutesDataModel> selectAllSubstitutesByTenant(final int tenantId){

        CustomSqlExecution<SubstitutesMapper,  Map<String, SubstitutesDataModel> > customSqlExecution =
                new AbstractCustomSqlExecution<SubstitutesMapper, Map<String, SubstitutesDataModel>>(SubstitutesMapper.class) {
                    public  Map<String, SubstitutesDataModel>  execute(SubstitutesMapper substitutesMapper) {
                        return substitutesMapper.selectAllSubstituteInfo(tenantId);
                    }
                };

        return managementService.executeCustomSql(customSqlExecution);
    }


    /**
     * Select all active substitutes for given tenant Id
     * @param tenantId
     * @return Map with User as key and SubstitutesDataModel as value
     */
    public Map<String, SubstitutesDataModel> selectActiveSubstitutesByTenant(final int tenantId){

        CustomSqlExecution<SubstitutesMapper,  Map<String, SubstitutesDataModel> > customSqlExecution =
                new AbstractCustomSqlExecution<SubstitutesMapper, Map<String, SubstitutesDataModel>>(SubstitutesMapper.class) {
                    public  Map<String, SubstitutesDataModel>  execute(SubstitutesMapper substitutesMapper) {
                        return substitutesMapper.selectActiveSubstitutesInfo(tenantId);
                    }
                };

        return managementService.executeCustomSql(customSqlExecution);
    }

    /**
     * Update transitive substitute for the given user.
     * @param user
     * @param tenantId
     * @param transitiveSub
     */
    public int updateTransitiveSub(final String user, final int tenantId, final String transitiveSub) {
        CustomSqlExecution<SubstitutesMapper, Integer> customSqlExecution =
                new AbstractCustomSqlExecution<SubstitutesMapper, Integer>(SubstitutesMapper.class) {
                    public Integer execute(SubstitutesMapper substitutesMapper) {
                        return substitutesMapper.updateTransitiveSub(user, tenantId, transitiveSub, new Date());
                    }
                };
        return managementService.executeCustomSql(customSqlExecution);
    }

    /**
     * Remove the substitute record for the given user.
     * @param assignee
     * @param tenantId
     * @return
     */
    public int removeSubstitute(final String assignee, final int tenantId) {
        CustomSqlExecution<SubstitutesMapper, Integer> customSqlExecution =
                new AbstractCustomSqlExecution<SubstitutesMapper, Integer>(SubstitutesMapper.class) {
                    public Integer execute(SubstitutesMapper substitutesMapper) {
                        return substitutesMapper.removeSubstitute(assignee, tenantId);
                    }
                };
        return managementService.executeCustomSql(customSqlExecution);
    }

    /**
     * Update the substitute of the given user.
     * @param assignee
     * @param substitute
     * @param tenantId
     * @param updated
     * @return
     */
    public int updateSubstitute(final String assignee, final String substitute, final int tenantId, final Date updated) {
        CustomSqlExecution<SubstitutesMapper, Integer> customSqlExecution =
                new AbstractCustomSqlExecution<SubstitutesMapper, Integer>(SubstitutesMapper.class) {
                    public Integer execute(SubstitutesMapper substitutesMapper) {
                        return substitutesMapper.updateSubstituteUser(assignee, substitute, tenantId, updated);
                    }
                };
        return managementService.executeCustomSql(customSqlExecution);
    }

    /**
     * Return the list of substitute info based on query parameters.
     * @param model model with only required query parameter values. Leave others as null. By default enabled=false.
     * @return List<SubstitutesDataModel> Result set of substitute info
     */
    public List<PaginatedSubstitutesDataModel> querySubstituteInfo(final PaginatedSubstitutesDataModel model) {

        final RowBounds rw = new RowBounds(model.getStart(), model.getSize());
        CustomSqlExecution<SubstitutesMapper, List<PaginatedSubstitutesDataModel>> customSqlExecution =
                new AbstractCustomSqlExecution<SubstitutesMapper, List<PaginatedSubstitutesDataModel>>(SubstitutesMapper.class) {
                    public List<PaginatedSubstitutesDataModel> execute(SubstitutesMapper substitutesMapper) {
                        return substitutesMapper.querySubstitutes(rw, model);
                    }
                };

        return managementService.executeCustomSql(customSqlExecution);

    }

    /**
     * Return the list of substitute info based on query parameters except enabled property.
     * @param model data model with only required query parameter values. Leave others as null.
     * @return List<PaginatedSubstitutesDataModel> Result set of substitute info
     */
    public List<PaginatedSubstitutesDataModel> querySubstituteInfoWithoutEnabled(final PaginatedSubstitutesDataModel model) {
        final RowBounds rw = new RowBounds(model.getStart(), model.getSize());
        CustomSqlExecution<SubstitutesMapper, List<PaginatedSubstitutesDataModel>> customSqlExecution =
                new AbstractCustomSqlExecution<SubstitutesMapper, List<PaginatedSubstitutesDataModel>>(SubstitutesMapper.class) {
                    public List<PaginatedSubstitutesDataModel> execute(SubstitutesMapper substitutesMapper) {
                        return substitutesMapper.querySubstitutesWithoutEnabled(rw, model);
                    }
                };

        return managementService.executeCustomSql(customSqlExecution);
    }

    /**
     * Return the list of tenant Ids that has substitutions.
     * @return tenant ID list
     */
    public List<Integer> getTenantsList() {
        CustomSqlExecution<SubstitutesMapper, List<Integer>> customSqlExecution =
                new AbstractCustomSqlExecution<SubstitutesMapper, List<Integer>>(SubstitutesMapper.class) {
                    public List<Integer> execute(SubstitutesMapper substitutesMapper) {
                        return substitutesMapper.getDistinctTenantList();
                    }
                };

        return managementService.executeCustomSql(customSqlExecution);
    }
}
