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

package org.wso2.carbon.humantask.core.dao;

import java.util.List;

/**
 * The organizational entity DAO representation.
 */
public interface OrganizationalEntityDAO {

    /**
     * The organizational entity types.
     */
    public static enum OrganizationalEntityType {
        USER,
        GROUP
    }

    /**
     * @return : The org entity id.
     */
    Long getId();

    /**
     * @param name : The org entity name to set.
     */
    void setName(String name);

    /**
     * @return : the org entity name.
     */
    String getName();

    /**
     * @param orgEntityType : The org entity type to set.
     */
    void setOrgEntityType(OrganizationalEntityType orgEntityType);

    /**
     * Adds the provided human role to the org entity.
     * @param genericHumanRole : The generic human role to add.
     */
    void addGenericHumanRole(GenericHumanRoleDAO genericHumanRole);

    /**
     * @return : The list of generic human roles of the org entity.
     */
    List<GenericHumanRoleDAO> getGenericHumanRoles();

    /**
     * @param genericHumanRoleDAOs : The general human roles to set.
     */
    void setGenericHumanRoles(List<GenericHumanRoleDAO> genericHumanRoleDAOs);

    /**
     * @return : The org entity type.
     */
    OrganizationalEntityType getOrgEntityType();
}
