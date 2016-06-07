/**
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.bpmn.core.db.mapper;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.wso2.carbon.bpmn.core.BPMNConstants;
import org.wso2.carbon.bpmn.core.db.model.DeploymentMetaDataModel;

import java.util.List;

/**
 * Mapper class maps with mybatis session layer of activiti engine to execute custom sql with
 * BPS_BPMN_DEPLOYMENT_METADATA table.
 * <p/>
 * This Mapper class can be mapped with acitviti engine by using below mentioned property element in acitiviti.xml
 * <p/>
 * <property name="customMybatisMappers">
 * <set>
 * <value>org.wso2.carbon.bpmn.core.mgt.model.DeploymentWrapper</value>
 * </set>
 * </property>
 */

public interface DeploymentMapper {

    final String SELECT_TENANT_PACKAGE =
            "SELECT * FROM " + BPMNConstants.BPS_BPMN_DEPLOYMENT_METADATA_TABLE +
                    " WHERE NAME_ = #{name}";
    final String SELECT_ALL_PACKAGE =
            "SELECT * FROM " + BPMNConstants.BPS_BPMN_DEPLOYMENT_METADATA_TABLE + " ; ";
    final String INSERT_META_DATA =
            "INSERT INTO " + BPMNConstants.BPS_BPMN_DEPLOYMENT_METADATA_TABLE +
                    "  (ID_, NAME_,CHECK_SUM_) VALUES (#{id}, #{packageName}, #{checkSum})";
    final String UPDATE_META_DATA = "UPDATE " + BPMNConstants.BPS_BPMN_DEPLOYMENT_METADATA_TABLE +
            "  SET CHECK_SUM_ = #{checkSum} WHERE  NAME_= #{packageName} ";
    final String DELETE_META_DATA =
            "DELETE FROM " + BPMNConstants.BPS_BPMN_DEPLOYMENT_METADATA_TABLE +
                    "  WHERE  NAME_= #{packageName} ";

    /**
     * Select the DeploymentMetaDataModel object for a given tenant id and package name
     *
     * @param bpmnPackageName package name
     * @return DeploymentMetaDataModel object
     */
    @Select(SELECT_TENANT_PACKAGE)
    @Results(value = {@Result(property = "id", column = "ID_"),
            @Result(property = "packageName", column = "NAME_"),
            @Result(property = "checkSum", column = "CHECK_SUM_")})
    DeploymentMetaDataModel selectMetaData(@Param("name") String bpmnPackageName);

    /**
     * Select All the DeploymentMetaDataModel objects form the table
     *
     * @return Each row of BPS_BPMN_DEPLOYMENT_METADATA ie being wrapped inside DeploymentMetaDataModel
     * object and returns a list
     */
    @Select(SELECT_ALL_PACKAGE)
    @Results(value = {@Result(property = "id", column = "ID_"),
            @Result(property = "packageName", column = "NAME_"),
            @Result(property = "checkSum", column = "CHECK_SUM_")})
    List<DeploymentMetaDataModel> selectAllMetaData(); //@Param("name") String name, @Param("id") int id

    /**
     * Inserts a new row in to the BPS_BPMN_DEPLOYMENT_METADATA table
     *
     * @param deploymentMetaDataModel object to be inserted in to the table
     * @return returns the inserted row count
     */
    @Insert(INSERT_META_DATA)
    int insertDeploymentMetaData(DeploymentMetaDataModel deploymentMetaDataModel);

    /**
     * Updates a row in to the BPS_BPMN_DEPLOYMENT_METADATA table
     *
     * @param deploymentMetaDataModel object to be updated in to the table
     * @return returns the updated row count
     */
    @Update(UPDATE_META_DATA)
    int updateDeploymentMetaData(DeploymentMetaDataModel deploymentMetaDataModel);

    /**
     * Deletes a row in to the BPS_BPMN_DEPLOYMENT_METADATA table
     *
     * @param deploymentMetaDataModel object to be deleted in the table
     * @return returns the delete row count
     */
    @Delete(DELETE_META_DATA)
    int deleteDeploymentMetaData(DeploymentMetaDataModel deploymentMetaDataModel);
}
