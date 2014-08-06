/*
 * Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import java.util.Date;
import java.util.List;

/**
 * Dao representation of deployment unit
 */
public interface DeploymentUnitDAO {

    /**
     * Set the name of the deployed package
     * This would mean the file name + Version Number
     * For example if the filename was ClaimApproval.zip with version 1, name of the package would be
     * ClaimApproval-1
     * @param  name name of the deployed package
     */

     void setName(String name);

    /**
     * Get the name of the deployed human task unit
     * @return Name of the deployment unit
     */
     String getName();

    /**
     *
     * @param packageName
     */

     void setPackageName(String packageName);

    /**
     *
     * @return
     */
     String getPackageName();

    /**
     * Set the deployment directory path  ( This would be the extracted file name )
     * @param deploymentDir  path
     * @return void
     */
      void setDeploymentUnitDir(String deploymentDir);


    /**
     * Get the packageName
     * @return deployment directory path
     */

     String getDeploymentUnitDir();

    /**
     * Get Deployed date
     * @return date
     */

     Date getDeployDate();

    /**
     * Set deploy date
     */
    void setDeployDate(Date date);

    /**
     * Get the tenant id of the deployer
     * @return tenantId
     */

     long getTenantId();

    /**
     * Delete all the contents of this directory and its children
     */
     void delete();


    /**
     * getCheckSum
     * @return string stored md4 checksum
     */

     String getChecksum();


    /**
     * Set the md5checksum value
     * @param  checksum
     */
     void setChecksum(String checksum);

    /**
     * Set the deployment status
     *
     */

     void setStatus(TaskPackageStatus status);

    /**
     * Get the deployment status
     * @return
     */

     TaskPackageStatus getStatus();

    /**
     * Get the version of the deployment unit
     */
    long getVersion();
    /**
     * Set the version
     */
    void setVersion(long version);
    /**
     * Set tenantId
     */
    public void setTenantId(long tenantId);


}
