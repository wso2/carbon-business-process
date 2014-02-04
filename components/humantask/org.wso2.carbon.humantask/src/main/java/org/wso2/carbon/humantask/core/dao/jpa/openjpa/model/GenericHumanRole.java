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
import org.wso2.carbon.humantask.core.dao.TaskDAO;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Generic human roles define what a person or a group of people resulting from a
 * people query can do with tasks and notifications. The following generic human roles
 * are taken into account in this specification:
 * - Task Initiator
 * - Task Stakeholders
 * - Potential Owners
 * - Actual Owner
 * - Excluded Owner
 * - Business Administrators
 * - Notification Recipients
 */
@Entity
@Table(name = "HT_GENERIC_HUMAN_ROLE")
@NamedQueries(
        @NamedQuery(name=GenericHumanRole.DELETE_GHR_BY_TASK, query = "delete from org.wso2.carbon.humantask.core.dao.jpa.openjpa.model.GenericHumanRole as g where g.task = :task")
)
public class GenericHumanRole implements GenericHumanRoleDAO {

    public static final String DELETE_GHR_BY_TASK = "DELETE_GHR_BY_TASK";

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "GHR_ID")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "GHR_TYPE")
    private GenericHumanRoleType type;

    @ManyToOne
    private Task task;

    @ManyToMany(targetEntity = OrganizationalEntity.class, fetch = FetchType.EAGER, cascade = {CascadeType.ALL})
    @JoinTable(name = "HT_HUMANROLE_ORGENTITY", joinColumns = {@JoinColumn(name = "HUMANROLE_ID", referencedColumnName = "GHR_ID")}, inverseJoinColumns = {@JoinColumn(name = "ORGENTITY_ID", referencedColumnName = "ORG_ENTITY_ID")})
    private List<OrganizationalEntityDAO> orgEntities = new ArrayList<OrganizationalEntityDAO>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public GenericHumanRoleType getType() {
        return type;
    }

    public void setType(GenericHumanRoleType type) {
        this.type = type;
    }

    public Task getTask() {
        return task;
    }

    public void setTask(TaskDAO task) {
        this.task = (Task) task;
    }
    public List<OrganizationalEntityDAO> getOrgEntities() {
        return orgEntities;
    }

    public void setOrgEntities(List<OrganizationalEntityDAO> orgEntities) {
        this.orgEntities = orgEntities;
    }

    public void addOrgEntity(OrganizationalEntityDAO orgEntity) {
        this.orgEntities.add(orgEntity);
    }

}
