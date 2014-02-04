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

import org.wso2.carbon.humantask.core.dao.DeploymentUnitDAO;
import org.wso2.carbon.humantask.core.dao.TaskPackageStatus;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Task Attachment Persistent Class.
 */

@Entity
@Table(name = "HT_DEPLOYMENT_UNIT")
public class DeploymentUnit extends OpenJPAEntity implements DeploymentUnitDAO{


    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name="NAME", nullable = false)
    private String name;

    @Column(name="DEPLOY_DIR", nullable = false)
    private String deploymentDir;

    @Column(name="PACKAGE_NAME", nullable = false)
    private String packageName;


    @Column(name = "DEPLOYED_ON")
    @Temporal(value = TemporalType.TIMESTAMP)
    private Date deployedOn;

    @Column(name ="TENANT_ID", nullable = false)
    private long tenantId;

    @Column(name="CHECKSUM", nullable = false)
    private String checksum;

    @Column(name="VERSION", nullable = false)
    private long version;

    @Enumerated(EnumType.STRING)
    @Column(name="STATUS", nullable = false)
    private TaskPackageStatus status;

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getPackageName() {
        return packageName;
    }


    public void setDeploymentUnitDir(String deploymentDir) {
        this.deploymentDir = deploymentDir;
    }

    public String getDeploymentUnitDir() {
        return deploymentDir;
    }

    public Date getDeployDate() {
        return deployedOn;
    }

    public void setDeployDate(Date date) {
        this.deployedOn = date;
    }

    public long getTenantId() {
        return tenantId;
    }

    public void setTenantId(long tenantId){
        this.tenantId = tenantId;
    }

    public void delete() {
        
    }

    public String getChecksum() {
        return checksum;
    }

    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }

    public void setStatus(TaskPackageStatus status) {
        this.status = status;

    }

    public TaskPackageStatus getStatus() {
        return status;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public long getVersion() {
        return this.version;
    }

}
