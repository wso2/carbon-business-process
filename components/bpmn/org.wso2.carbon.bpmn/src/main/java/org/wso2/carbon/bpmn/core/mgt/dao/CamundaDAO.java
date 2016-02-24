/*  Copyright (c) 2014-2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.camunda.bpm.engine.impl.db.ListQueryParameterObject;
import org.wso2.carbon.bpmn.core.internal.MyBatisQueryCommandExecutor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bpmn.core.BPMNServerHolder;
import org.wso2.carbon.bpmn.core.mgt.model.DeploymentMetaDataModelEntity;

import java.util.List;

import org.camunda.bpm.engine.ProcessEngines;
import org.camunda.bpm.engine.impl.ProcessEngineImpl;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;

public class CamundaDAO {

	//  private static final Log log = LogFactory.getLog(DeploymentMapper.class);
	private ProcessEngineConfigurationImpl processEngineConfiguration;
	private MyBatisQueryCommandExecutor commandExecutor;

	public CamundaDAO() {

		ProcessEngineImpl processEngine =
				(ProcessEngineImpl) ProcessEngines.getDefaultProcessEngine();
		processEngineConfiguration = processEngine.getProcessEngineConfiguration();
		//TODO: Add proper path to mappings.xml
		commandExecutor =
				new MyBatisQueryCommandExecutor(processEngineConfiguration, "mappings.xml");

	}

 /*Invokes mapping selectDeploymentMetaDataModel for a given tenant id and package name
     *
     * @param tenantID        tenant id
     * @param bpmnPackageName package name
     * @return                DeploymentMetaDataModelEntity object*/

	public DeploymentMetaDataModelEntity selectTenantAwareDeploymentModel(final String tenantID,
	                                                                      final String bpmnPackageName) {

		return commandExecutor.executeQueryCommand(new Command<DeploymentMetaDataModelEntity>() {

			@SuppressWarnings("unchecked")
			public DeploymentMetaDataModelEntity execute(CommandContext commandContext) {
				//Adding query parameters to one object
				ListQueryParameterObject queryParameterObject = new ListQueryParameterObject();
				queryParameterObject.setParameter(tenantID);
				queryParameterObject.setParameter(bpmnPackageName);

				return (DeploymentMetaDataModelEntity) commandContext.getDbEntityManager()
				                                                     .selectOne(
						                                                     "selectDeploymentMetaDataModel",
						                                                     queryParameterObject);
			}
		});
	}


    /* Invokes mapping DeploymentMapper.selectDeploymentMetaDataModels to retrieve all rows
     * from BPS_BPMN_DEPLOYMENT_METADATA
     *
     * @return each row will be returned as DeploymentMetaDataModelEntity with in list
     */

	//selectList method
	public List<DeploymentMetaDataModelEntity> selectAllDeploymentModel() {

		return commandExecutor
				.executeQueryCommand(new Command<List<DeploymentMetaDataModelEntity>>() {
					@SuppressWarnings("unchecked")
					public List<DeploymentMetaDataModelEntity> execute(
							CommandContext commandContext) {
						return commandContext.getDbEntityManager()
						                     .selectList("selectDeploymentMetaDataModels");

					}

				});

	}

    /* Invokes the DeploymentMapper.insertDeploymentMetaDataModel to insert a new row
     * from BPS_BPMN_DEPLOYMENT_METADATA
     *
     * @param deploymentMetaDataModel Object to be inserted in to the table
     */

	public void insertDeploymentMetaDataModel(
			final DeploymentMetaDataModelEntity deploymentMetaDataModel) {
		commandExecutor.executeQueryCommand(new Command<Void>() {
			@SuppressWarnings("unchecked")

			public Void execute(CommandContext commandContext) {
				commandContext.getDbEntityManager().insert(deploymentMetaDataModel);
				return null;
			}

		});
	}

    /*
     * Invokes the DeploymentMapper.updateDeploymentMetaDataModel to update  row
     * in BPS_BPMN_DEPLOYMENT_METADATA
     *
     * @param deploymentMetaDataModel Object to be updated in to the table
     */

	public void updateDeploymentMetaDataModel(
			final DeploymentMetaDataModelEntity deploymentMetaDataModel) {

		commandExecutor.executeQueryCommand(new Command<Void>() {
			@SuppressWarnings("unchecked")

			public Void execute(CommandContext commandContext) {
				commandContext.getDbEntityManager().update(DeploymentMetaDataModelEntity.class,
				                                           "updateDeploymentMetaDataModel",
				                                           deploymentMetaDataModel);

				return null;
			}

		});

	}

    /* Invokes the DeploymentMapper.deleteDeploymentMetaDataModel to delete a
     * row from BPS_BPMN_DEPLOYMENT_METADATA
     *
     * @param deploymentMetaDataModel the object to be deleted
     *
     */

	public void deleteDeploymentMetaDataModel(
			final DeploymentMetaDataModelEntity deploymentMetaDataModel) {
		commandExecutor.executeQueryCommand(new Command<Void>() {
			@SuppressWarnings("unchecked")

			public Void execute(CommandContext commandContext) {
				commandContext.getDbEntityManager().delete(DeploymentMetaDataModelEntity.class,
				                                           "deleteDeploymentMetaDataModel",
				                                           deploymentMetaDataModel);

				return null;
			}

		});
	}

}
