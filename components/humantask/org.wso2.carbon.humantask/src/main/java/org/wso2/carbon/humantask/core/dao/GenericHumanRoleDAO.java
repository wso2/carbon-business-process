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
 * The Generic Human Role DAO representation.
 */
public interface GenericHumanRoleDAO {

    /**
     * The human role types.
     */
    public static enum GenericHumanRoleType {
        TASK_INITIATOR,
        STAKEHOLDERS,
        POTENTIAL_OWNERS,
        ACTUAL_OWNER,
        EXCLUDED_OWNERS,
        BUSINESS_ADMINISTRATORS,
        NOTIFICATION_RECIPIENTS
    }

    /**
     * @return : The role id.
     */
    Long getId();

    /**
     * @return : the human role type.
     */
    GenericHumanRoleType getType();

    /**
     * @return : The org entities for this human role.
     */
    List<OrganizationalEntityDAO> getOrgEntities();

    /**
     * @param orgEntities : The orgEntities to set.
     */
    void setOrgEntities(List<OrganizationalEntityDAO> orgEntities);

    /**
     * @param task : the task to set.
     */
    void setTask(TaskDAO task);

    /**
     * @param type : The human role type to set.
     */
    void setType(GenericHumanRoleType type);

    /**
     * Adds the provided org entity object to the generic human role.
     * @param orgEntity : The org entity to add.
     */
    void addOrgEntity(OrganizationalEntityDAO orgEntity);
}
