/*
 * Copyright (c) 2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.humantask.core.dao.jpa.openjpa.model;

import org.wso2.carbon.humantask.core.dao.GenericHumanRoleDAO;
import org.wso2.carbon.humantask.core.dao.OrganizationalEntityDAO;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Domain object for organizational entity representation.
 */
@Entity
@Table(name = "HT_ORG_ENTITY")
public class OrganizationalEntity extends OpenJPAEntity implements OrganizationalEntityDAO {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ORG_ENTITY_ID")
    private Long id;

    @Column(name = "ORG_ENTITY_NAME")
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "ORG_ENTITY_TYPE")
    private OrganizationalEntityType orgEntityType;

    @ManyToMany(targetEntity = GenericHumanRole.class, mappedBy = "orgEntities", cascade = {CascadeType.ALL})
    private List<GenericHumanRoleDAO> genericHumanRoles = new ArrayList<GenericHumanRoleDAO>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public OrganizationalEntityType getOrgEntityType() {
        return orgEntityType;
    }

    public void setOrgEntityType(OrganizationalEntityType orgEntityType) {
        this.orgEntityType = orgEntityType;
    }

    public List<GenericHumanRoleDAO> getGenericHumanRoles() {
        return this.genericHumanRoles;
    }

    public void setGenericHumanRoles(List<GenericHumanRoleDAO> genericHumanRoleDAOs) {
        this.genericHumanRoles = genericHumanRoleDAOs;
    }

    public void addGenericHumanRole(GenericHumanRoleDAO genericHumanRole) {
        genericHumanRoles.add(genericHumanRole);
    }

    @Override
    public boolean equals(Object other) {
        boolean result = false;
        if (other instanceof OrganizationalEntity) {
            OrganizationalEntity that = (OrganizationalEntity) other;
            if (this.id != null && that.id != null && this.id.equals(that.id)) {
                return true;
            }
            result = this.name.equals(that.name);
        }
        return result;
    }

    @Override
    public int hashCode() {
        return (int) (((long) this.id.hashCode()) +
                this.getClass().getCanonicalName().hashCode());
    }
}
