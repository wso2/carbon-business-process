/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.humantask.core.store.repository;

import org.wso2.carbon.humantask.core.HumanTaskConstants;
import org.wso2.carbon.humantask.core.dao.DeploymentUnitDAO;
import org.wso2.carbon.humantask.core.deployment.HumanTaskDeploymentUnit;

/**
 * Utility methods for HumanTaskPackageRepository
 */
public class HumanTaskPackageRepositoryUtils {

    /**
     * Registry resource path for human task package parent collection given humanTaskDeploymentUnit
     *
     * @param humanTaskDeploymentUnit
     * @return registry resource path
     */
    public static String getResourcePathForHumanTaskPackage(HumanTaskDeploymentUnit humanTaskDeploymentUnit) {
        return HumanTaskConstants.REG_PATH_OF_HUMANTASK_PACKAGES + humanTaskDeploymentUnit.getPackageName();
    }

    /**
     * Registry resource path for human task package parent collection on given deploymentDAO
     *
     * @param deploymentUnitDAO
     * @return registry resource path
     */
    public static String getResourcePathForHumanTaskPackage(DeploymentUnitDAO deploymentUnitDAO) {
        return HumanTaskConstants.REG_PATH_OF_HUMANTASK_PACKAGES + deploymentUnitDAO.getPackageName();
    }

    /**
     * Registry resource path for human task package parent collection on given package name
     *
     * @param humanTaskPackageName
     * @return registry resource path
     */
    public static String getResourcePathForHumanTaskPackage(String humanTaskPackageName) {
        return HumanTaskConstants.REG_PATH_OF_HUMANTASK_PACKAGES + humanTaskPackageName;
    }

    /**
     * Registry resource path for human task package zip file
     *
     * @param humanTaskPackageName
     * @return registry resource path
     */
    public static String getHumanTaskPackageArchiveResourcePath(String humanTaskPackageName) {
        return HumanTaskConstants.REG_PATH_OF_HUMANTASK_PACKAGES.concat(humanTaskPackageName).concat
                (HumanTaskConstants.PATH_SEPARATOR).concat(humanTaskPackageName).concat(".zip");
    }

    /**
     * Registry resource path for human task package versions collection
     *
     * @param humanTaskDeploymentUnit
     * @return registry resource path
     */
    public static String getResourcePathForHumanTaskPackageVersions(HumanTaskDeploymentUnit humanTaskDeploymentUnit) {
        return HumanTaskConstants.REG_PATH_OF_HUMANTASK_PACKAGES + humanTaskDeploymentUnit.getPackageName() +
               HumanTaskConstants.HUMANTASK_PACKAGE_VERSIONS;
    }

    /**
     * @param deploymentUnitDAO
     * @return registry resource path
     */
    public static String getResourcePathForHumanTaskPackageVersions(DeploymentUnitDAO deploymentUnitDAO) {
        return HumanTaskConstants.REG_PATH_OF_HUMANTASK_PACKAGES + deploymentUnitDAO.getPackageName() +
               HumanTaskConstants.HUMANTASK_PACKAGE_VERSIONS;
    }

    /**
     * Registry resource path for human task package content collection on given version
     *
     * @param humanTaskDeploymentUnit
     * @return registry resource path registry resource path
     */
    public static String getResourcePathForHumanTaskPackageContent(HumanTaskDeploymentUnit humanTaskDeploymentUnit) {
        return HumanTaskConstants.REG_PATH_OF_HUMANTASK_PACKAGES + humanTaskDeploymentUnit.getPackageName() +
               HumanTaskConstants.HUMANTASK_PACKAGE_VERSIONS +
               humanTaskDeploymentUnit.getName() + humanTaskDeploymentUnit.getVersion();
    }

    /**
     * Registry resource path for human task package content collection on given version
     *
     * @param deploymentUnitDAO
     * @return registry resource path
     */
    public static String getResourcePathForHumanTaskPackageContent(DeploymentUnitDAO deploymentUnitDAO) {
        return HumanTaskConstants.REG_PATH_OF_HUMANTASK_PACKAGES + deploymentUnitDAO.getPackageName() +
               HumanTaskConstants.HUMANTASK_PACKAGE_VERSIONS +
               deploymentUnitDAO.getName() + deploymentUnitDAO.getVersion();
    }

    /**
     * Registry resource path for human task package content collection on given version
     *
     * @param humanTaskPackageName
     * @param humanTaskPackageNameWithVersion
     * @return registry resource path
     */
    public static String getResourcePathForHumanTaskPackageContent(String humanTaskPackageName,
                                                                   String humanTaskPackageNameWithVersion) {
        return HumanTaskConstants.REG_PATH_OF_HUMANTASK_PACKAGES + humanTaskPackageName +
               HumanTaskConstants.HUMANTASK_PACKAGE_VERSIONS + humanTaskPackageNameWithVersion;
    }
}
