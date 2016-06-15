/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License, 
 * Version 2.0 (the "License"); you may not use this file except 
 * in compliance with the License.
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
package org.wso2.carbon.bpmn.core.internal.mapper;

import org.apache.ibatis.annotations.*;
import org.wso2.carbon.bpmn.core.BPMNConstants;
import org.wso2.carbon.bpmn.core.mgt.model.SubstitutesDataModel;

import java.util.Date;
import java.util.Map;

public interface SubstitutesMapper {

    final String INSERT_SUBSTITUTE = "INSERT INTO " + BPMNConstants.ACT_BPS_SUBSTITUTES_TABLE +
            "  (USER, SUBSTITUTE, SUBSTITUTION_START, SUBSTITUTION_END, ENABLED, TRANSITIVE_SUBSTITUTE, CREATED, UPDATED, TENANT_ID) VALUES (#{user}, #{substitute}, #{substitutionStart}, #{substitutionEnd}, #{enabled}, #{transitiveSub}, #{created}, #{updated}, #{tenantId})";
    final String SELECT_ALL_BY_USER = "SELECT * FROM " + BPMNConstants.ACT_BPS_SUBSTITUTES_TABLE +
            " WHERE USER = #{user} AND TENANT_ID = #{tenantId}";
    final String UPDATE_ENABLED = "UPDATE " + BPMNConstants.ACT_BPS_SUBSTITUTES_TABLE +
            "  SET ENABLED = #{enabled} WHERE USER = #{user} AND TENANT_ID=#{tenantId}";
    final String UPDATE = "<script>" + " UPDATE " + BPMNConstants.ACT_BPS_SUBSTITUTES_TABLE + " SET "
            + "<if test=\"substitute != null\">" + " SUBSTITUTE = #{substitute}, </if> "
            + "<if test=\"substitutionStart != null\">\" + \" SUBSTITUTION_START = #{substitutionStart}, </if>"
            + "<if test=\"substitutionEnd != null\">\" + \" SUBSTITUTION_END = #{substitutionEnd}, </if>"
            + "<if test=\"enabled != null\">\" + \" ENABLED = #{enabled}, </if>"
            + "<if test=\"transitiveSub != null\">\" + \" TRANSITIVE_SUBSTITUTE = #{transitiveSub}, </if>"
            + "UPDATED = #{updated} " + "WHERE USER = #{user} AND TENANT_ID=#{tenantId}" + "</script>";
    final String COUNT_USER_AS_SUBSTITUTE = "SELECT COUNT(*) FROM " + BPMNConstants.ACT_BPS_SUBSTITUTES_TABLE
            + " WHERE SUBSTITUTE = #{substitute} AND TENANT_ID = #{tenantId}";
    final String SELECT_ALL_SUBSTITUTES = "SELECT USER, SUBSTITUTE, SUBSTITUTION_START, SUBSTITUTION_END, ENABLED from " + BPMNConstants.ACT_BPS_SUBSTITUTES_TABLE + " WHERE TENANT_ID = #{tenantId}";
    final String UPDATE_TRANSITIVE_SUB = "UPDATE " + BPMNConstants.ACT_BPS_SUBSTITUTES_TABLE +
            "  SET TRANSITIVE_SUBSTITUTE = #{transitiveSub}, UPDATED = #{updated} WHERE USER = #{user} AND TENANT_ID=#{tenantId}";
    final String DELETE_SUBSTITUTE = "DELETE FROM " + BPMNConstants.ACT_BPS_SUBSTITUTES_TABLE + " WHERE USER = #{user} AND TENANT_ID=#{tenantId}";

    /**
     * Insert new row in ACT_BPS_SUBSTITUTES table
     * @param substitutesDataModel
     * @return
     */
    @Insert(INSERT_SUBSTITUTE)
    int insertSubstitute(SubstitutesDataModel substitutesDataModel);

    /**
     * Select the SubstitutesDataModel from the given tenantId and username
     * @param user assignee that required the substitution info
     * @return the substitution info for the given user
     */
    @Select(SELECT_ALL_BY_USER)
    @Results(value = {
            @Result(property = "user", column = "USER"),
            @Result(property = "substitute", column = "SUBSTITUTE"),
            @Result(property = "substitutionStart", column = "SUBSTITUTION_START"),
            @Result(property = "substitutionEnd", column = "SUBSTITUTION_END"),
            @Result(property = "enabled", column = "ENABLED"),
            @Result(property = "tenantId", column = "TENANT_ID"),
            @Result(property = "transitiveSub", column = "TRANSITIVE_SUBSTITUTE"),
            @Result(property = "created", column = "CREATED"),
            @Result(property = "updated", column = "UPDATED")
    })
    SubstitutesDataModel selectSubstitute( @Param("user") String user, @Param("tenantId") int tenantId);

    /**
     * Update the ACT_BPS_SUBSTITUTES table with all the none null values
     * @param substitutesDataModel
     * @return number of rows updated
     */
    @Update(UPDATE)
    int updateSubstitute(SubstitutesDataModel substitutesDataModel);

    /**
     * Return the row count where the given user acts as the substitute
     * @param substitute
     * @param tenantId
     * @return
     */
    @Select(COUNT_USER_AS_SUBSTITUTE)
    int countUserAsSubstitute( @Param("substitute") String substitute, @Param("tenantId") int tenantId);

    /**
     * Select all Substitute info for given tenant
     * @param tenantId
     * @return Map with key USER and value SubstitutesDataModel
     */
    @Select(SELECT_ALL_SUBSTITUTES)
    @MapKey("user")
    @Results(value = {
            @Result(property = "user", column = "USER"),
            @Result(property = "substitute", column = "SUBSTITUTE"),
            @Result(property = "substitutionStart", column = "SUBSTITUTION_START"),
            @Result(property = "substitutionEnd", column = "SUBSTITUTION_END"),
            @Result(property = "enabled", column = "ENABLED")
    })
    Map<String, SubstitutesDataModel> selectAllSubstituteInfo(@Param("tenantId") int tenantId);

    /**
     * Update Transitive substitute for the given user and tenant
     * @param user
     * @param tenantId
     * @param transitiveSub
     * @param date
     * @return
     */
    @Update(UPDATE_TRANSITIVE_SUB)
    int updateTransitiveSub(@Param("user") String user, @Param("tenantId") int tenantId, @Param("transitiveSub") String transitiveSub, @Param("updated") Date date);

    /**
     * Remove substitute info for given user
     * @param user
     * @param tenantId
     * @return
     */
    @Delete(DELETE_SUBSTITUTE)
    int removeSubstitute(@Param("user") String user, @Param("tenantId") int tenantId);
}
