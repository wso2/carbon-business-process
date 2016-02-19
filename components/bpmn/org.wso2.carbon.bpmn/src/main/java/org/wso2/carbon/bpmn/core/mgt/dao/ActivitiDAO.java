 /*  Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.camunda.bpm.engine.impl.db.ListQueryParameterObject;
import  org.wso2.carbon.bpmn.core.internal.MyBatisQueryCommandExecutor;
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.ProcessEngine;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bpmn.core.BPMNServerHolder;
import org.wso2.carbon.bpmn.core.mgt.model.DeploymentMetaDataModel;

import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.engine.ProcessEngines;
import org.camunda.bpm.engine.impl.ProcessEngineImpl;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;


//import org.wso2.carbon.bpmn.core.internal.BuildSqlSessionFactoryBuilder;


public class ActivitiDAO {

  //  private static final Log log = LogFactory.getLog(DeploymentMapper.class);

    private ManagementService managementService = null;
    private ProcessEngineConfigurationImpl processEngineConfiguration;
    private MyBatisQueryCommandExecutor commandExecutor;
// private SqlSessionFactory s;

    public ActivitiDAO() {

      //  SqlSession sqlSession = BuildSqlSessionFactoryBuilder.getSqlSessionFactory().openSession();

       // ProcessEngineImpl processEngine = (ProcessEngineImpl)BPMNServerHolder.getInstance().getEngine();
        //managementService = engine.getManagementService();
       ProcessEngineImpl processEngine = (ProcessEngineImpl) ProcessEngines.getDefaultProcessEngine();
         processEngineConfiguration = processEngine.getProcessEngineConfiguration();
         commandExecutor = new MyBatisQueryCommandExecutor(processEngineConfiguration, "/Users/himasha/localCBP16/carbon-business-process/components/bpmn/org.wso2.carbon.bpmn/src/main/resources/META-INF/services/mappings.xml");

        //
      // s= (SqlSessionFactory)new BuildSqlSessionFactoryBuilder();
    }


 /*invokes the DeploymentMapper.selectMetaData for a given tenant id and package name
     *
     * @param tenantID        tenant id
     * @param bpmnPackageName package name
     * @return                DeploymentMetaDataModel object*/



    public DeploymentMetaDataModel selectTenantAwareDeploymentModel(final String tenantID, final String bpmnPackageName){
             //TESt
       // MyBatisQueryCommandExecutor commandExecutor = new MyBatisQueryCommandExecutor(processEngineConfiguration, "DeploymentMapper");
        return commandExecutor.executeQueryCommand(new Command<DeploymentMetaDataModel>() {
            @SuppressWarnings("unchecked")
            public DeploymentMetaDataModel execute(CommandContext commandContext) {
               // SqlSession ss =  s.openSession();
               // ss.getMapper(DeploymentMapper.class).selectMetaData(tenantID, bpmnPackageName);
               // return commandContext.getDbSqlSession().getSqlSession().getMapper(DeploymentMapper.class).selectMetaData(tenantID,bpmnPackageName);
               ListQueryParameterObject queryParameterObject = new ListQueryParameterObject();
                queryParameterObject.setParameter(tenantID);
               queryParameterObject.setParameter(bpmnPackageName);

                // select for this query
                //
                return (DeploymentMetaDataModel) commandContext.getDbEntityManager().selectOne("selectMetaData", queryParameterObject);


            }

        });
    }

//        CustomSqlExecution<DeploymentMapper, DeploymentMetaDataModel> customSqlExecution =
//                new AbstractCustomSqlExecution<DeploymentMapper, DeploymentMetaDataModel>(DeploymentMapper.class) {
//                    public DeploymentMetaDataModel execute(DeploymentMapper deploymentMapper) {
//                        return deploymentMapper.selectMetaData(tenantID, bpmnPackageName);
//                    }
//                };

//        DeploymentMetaDataModel deploymentMetaDataModel = managementService.executeCustomSql(customSqlExecution);
//        if(log.isDebugEnabled()) {
//
//            if(deploymentMetaDataModel != null) {
//                log.debug("DeploymentDataModel exists when selecting models=" + deploymentMetaDataModel.getId());
//            }
//            else {
//                log.debug("DeploymentDataModel null when selecting models");
//            }
//        }
//        return deploymentMetaDataModel;
//    }

     /* invokes the DeploymentMapper.selectAllMetaData to retrieve all rows from BPS_BPMN_DEPLOYMENT_METADATA
     *
     * @return each row will be returned as DeploymentMetaDataModel with in list
*/


    //selectList method
    public List<DeploymentMetaDataModel> selectAllDeploymentModel(){

        return commandExecutor.executeQueryCommand(new Command <List<DeploymentMetaDataModel>>() {
            @SuppressWarnings("unchecked")
            public List<DeploymentMetaDataModel> execute(CommandContext commandContext) {
              // return commandContext.getDbSqlSession().getSqlSession().getMapper(DeploymentMapper.class).selectAllMetaData();
               return commandContext.getDbEntityManager().selectList("selectAllMetaData");
               // return modelList;


            }

        });


        /*CustomSqlExecution<DeploymentMapper,  List<DeploymentMetaDataModel> > customSqlExecution =
                new AbstractCustomSqlExecution<DeploymentMapper,  List<DeploymentMetaDataModel> >(DeploymentMapper.class) {
                    public  List<DeploymentMetaDataModel>  execute(DeploymentMapper deploymentMapper) {
                        return deploymentMapper.selectAllMetaData();
                    }
                };

       return managementService.executeCustomSql(customSqlExecution);*/
   }

    /* invokes the DeploymentMapper.insertDeploymentMetaData to insert a new row from BPS_BPMN_DEPLOYMENT_METADATA
     *
     * @param deploymentMetaDataModel Object to be inserted in to the table
            */


     public void insertDeploymentMetaDataModel(final DeploymentMetaDataModel deploymentMetaDataModel) {
         commandExecutor.executeQueryCommand(new Command<Integer>() {
             @SuppressWarnings("unchecked")

             public Integer execute(CommandContext commandContext) {

              //  Integer count = commandContext.getDbSqlSession().getSqlSession().
                  //      getMapper(DeploymentMapper.class).insertDeploymentMetaData(deploymentMetaDataModel);

                 Integer count = commandContext.getDbSqlSession().getSqlSession().insert("insertMetaData", deploymentMetaDataModel);

                 return count;
             }

         });
     }
       /* CustomSqlExecution<DeploymentMapper, Integer> customSqlExecution =
                new AbstractCustomSqlExecution<DeploymentMapper, Integer>(DeploymentMapper.class) {
                    public Integer execute(DeploymentMapper deploymentMapper) {
                        return deploymentMapper.insertDeploymentMetaData(deploymentMetaDataModel);
                    }
                };

        Integer rowCount = managementService.executeCustomSql(customSqlExecution);

	    if(log.isDebugEnabled()) {
		    log.debug("insertDeploymentMetaDataModel" + rowCount);
	    }*/


/*
     * invokes the DeploymentMapper.updateDeploymentMetaData to insert a new row from BPS_BPMN_DEPLOYMENT_METADATA
     *
     * @param deploymentMetaDataModel Object to be updated in to the table
*/


    public void updateDeploymentMetaDataModel(final DeploymentMetaDataModel deploymentMetaDataModel){

        commandExecutor.executeQueryCommand(new Command<Integer>() {
            @SuppressWarnings("unchecked")

            public Integer execute(CommandContext commandContext) {
               // Integer count = commandContext.getDbSqlSession().getSqlSession().getMapper(DeploymentMapper.class).updateDeploymentMetaData(deploymentMetaDataModel);
                Integer count = commandContext.getDbSqlSession().getSqlSession().update("updateMetaData", deploymentMetaDataModel);

                return count;
            }

        });
       /* Integer rowCount = managementService.executeCustomSql(customSqlExecution);

        if(log.isDebugEnabled()) {
            log.debug("updated DeploymentMetaDataModel" + rowCount);
        }*/
    }

    /* invokes the DeploymentMapper.deleteDeploymentMetaData to delete a new row from BPS_BPMN_DEPLOYMENT_METADATA
     *
     * @param deploymentMetaDataModel the object to be deleted
     * @return total number of rows deleted
*/


    public int deleteDeploymentMetaDataModel(final DeploymentMetaDataModel deploymentMetaDataModel) {
        return commandExecutor.executeQueryCommand(new Command<Integer>() {
            @SuppressWarnings("unchecked")

            public Integer execute(CommandContext commandContext) {
                //Integer count = commandContext.getDbSqlSession().getSqlSession().getMapper(DeploymentMapper.class).deleteDeploymentMetaData(deploymentMetaDataModel);
                  Integer count =  commandContext.getDbSqlSession().getSqlSession(). delete("deleteMetaData", deploymentMetaDataModel);
//                );
//
//

                return count;
            }

        });

/*
       CustomSqlExecution<DeploymentMapper, Integer> customSqlExecution = new AbstractCustomSqlExecution<DeploymentMapper, Integer>(DeploymentMapper.class) {
                    public Integer execute(DeploymentMapper deploymentMapper) {
                       return deploymentMapper.deleteDeploymentMetaData(deploymentMetaDataModel);
                   }
                };
        Integer rowCount = managementService.executeCustomSql(customSqlExecution);

        if(log.isDebugEnabled()) {
            log.debug("deleteDeploymentMetaDataModel" + rowCount);
        }

        return rowCount;
    }*/
    }
}
