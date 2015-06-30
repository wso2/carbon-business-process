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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.humantask.core.HumanTaskConstants;
import org.wso2.carbon.humantask.core.dao.DeploymentUnitDAO;
import org.wso2.carbon.humantask.core.deployment.HumanTaskDeploymentUnit;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.utils.RegistryClientUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * HumanTask Package is responsible for manage registry based human task deployment units
 * How HumanTask Packages will get stored in the registry
 * <p/>
 * <HumanTask_ROOT>/<HumanTask_PACKAGE_NAME>
 * |
 * + prop_humantask.package.latest.version
 * + prop_humantask.package.status
 * + prop_humantask.package.latest.checksum
 * + versions
 * |
 * +<HumanTask_PACKAGE_NAME-version1>
 * +<HumanTask_PACKAGE_NAME-version2>
 * +<HumanTask_PACKAGE_NAME-version3>
 */

public class HumanTaskPackageRepository {

    private static Log log = LogFactory.getLog(HumanTaskPackageRepository.class);

    // Config registry instance use to store human task package information for this repository instance.
    private Registry configRegistry;

    // Location of local file system where we keep human task archives.
    private File humanTaskArchiveRepo;

    public HumanTaskPackageRepository(Registry configRegistry, File humanTaskArchiveRepo) {
        this.configRegistry = configRegistry;
        this.humanTaskArchiveRepo = humanTaskArchiveRepo;
    }

    /**
     * Handles deployment of new human task packages. Stores all the meta data and human task Package content
     * in the registry.
     * Update the registry meta data on human task package if package already exists.
     *
     * @param humanTaskDeploymentUnit containing information about current deployment
     * @throws Exception on registry access error.
     */
    public void handleNewHumanTaskPackageAddition(HumanTaskDeploymentUnit humanTaskDeploymentUnit, File humanTaskFile)
            throws Exception {
        try {
            if (!isDUCollectionIsThere(humanTaskDeploymentUnit)) {
                configRegistry.beginTransaction();
                createOrUpdateHumanTaskPackageParentCollectionWithProperties(humanTaskDeploymentUnit);
                addLatestArchiveToRegistryCollection(humanTaskDeploymentUnit, humanTaskFile);
                createCollectionWithHumanTaskPackageContentForCurrentVersion(humanTaskDeploymentUnit);
                configRegistry.commitTransaction();
            }
        } catch (RegistryException re) {
            handleExceptionWithRollback("Unable to handle new HumanTask Package addition."
                                        + " Package: " + humanTaskDeploymentUnit.getPackageName(), re);
        }
    }

    /**
     * Restore the extracted human task package file from file system
     *
     * @param deploymentUnitDAO
     * @throws Exception on registry access error.
     */
    public void restoreHumanTaskPackageContentInRegistry(DeploymentUnitDAO deploymentUnitDAO, File humanTaskFile)
            throws Exception {
        try {
            if (!isDUCollectionIsThere(deploymentUnitDAO)) {

                String packageLocation = HumanTaskPackageRepositoryUtils.getResourcePathForHumanTaskPackage
                        (deploymentUnitDAO);
                configRegistry.beginTransaction();
                if (!configRegistry.resourceExists(packageLocation)) {
                    createHumanTaskPackageParentCollectionWithProperties(deploymentUnitDAO);
                    addLatestArchiveToRegistryCollection(deploymentUnitDAO, humanTaskFile);
                }
                createCollectionWithHumanTaskPackageContentForCurrentVersion(deploymentUnitDAO);
                configRegistry.commitTransaction();
            }
        } catch (RegistryException re) {
            handleExceptionWithRollback("Unable to handle new HumanTask Package addition."
                                        + " Package: " + deploymentUnitDAO.getPackageName(), re);
        }
    }

    /**
     * Check whether collection for human task package is exist in the registry
     *
     * @param humanTaskDeploymentUnit
     * @throws RegistryException
     */
    private boolean isDUCollectionIsThere(HumanTaskDeploymentUnit humanTaskDeploymentUnit)
            throws RegistryException {
        String collectionLocation = HumanTaskPackageRepositoryUtils.getResourcePathForHumanTaskPackageContent
                (humanTaskDeploymentUnit);
        return configRegistry.resourceExists(collectionLocation);
    }

    /**
     * Check whether collection for human task package is exist in the registry
     *
     * @param deploymentUnitDAO
     * @throws RegistryException
     */
    private boolean isDUCollectionIsThere(DeploymentUnitDAO deploymentUnitDAO)
            throws RegistryException {
        String collectionLocation = HumanTaskPackageRepositoryUtils.getResourcePathForHumanTaskPackageContent
                (deploymentUnitDAO);
        return configRegistry.resourceExists(collectionLocation);
    }


    /**
     * If registry contain the collection for human task package then package properties will get update
     * If not new collection will be created for human task package.
     *
     * @param humanTaskDeploymentUnit
     * @throws RegistryException
     */
    private void createOrUpdateHumanTaskPackageParentCollectionWithProperties(
            HumanTaskDeploymentUnit humanTaskDeploymentUnit) throws RegistryException {
        String packageLocation = HumanTaskPackageRepositoryUtils.getResourcePathForHumanTaskPackage
                (humanTaskDeploymentUnit);
        if (configRegistry.resourceExists(packageLocation)) {
            updateHumanTaskPackageProperties(humanTaskDeploymentUnit);
        } else {
            createHumanTaskPackageParentCollectionWithProperties(humanTaskDeploymentUnit);
        }
    }

    /**
     * Create parent collection to persisting human task package information. For example, if you deploy
     * a human task archive called 'ClaimsApprovalTask.zip', we store information of that package in collection
     * named 'ClaimsApprovalTask'. This will be the root for 'ClaimsApprovalTask' human task package information and
     * there will several versions of this human task package in this registry collection which relates
     * to the versions deployed in human task engine.
     *
     * @param humanTaskDeploymentUnit containing information on current deployment
     * @throws RegistryException when there is a error accessing registry
     */
    private void createHumanTaskPackageParentCollectionWithProperties(HumanTaskDeploymentUnit humanTaskDeploymentUnit)
            throws RegistryException {
        Collection humanPackage = configRegistry.newCollection();
        humanPackage.setProperty(HumanTaskConstants.HUMANTASK_PACKAGE_PROP_LATEST_CHECKSUM, humanTaskDeploymentUnit
                .getMd5sum());
        if (log.isDebugEnabled()) {
            log.debug(humanTaskDeploymentUnit.getPackageName() + " updating checksum: " + humanTaskDeploymentUnit
                    .getMd5sum() + " in registry");
        }
        humanPackage.setProperty(HumanTaskConstants.HUMANTASK_PACKAGE_PROP_STATUS, String.valueOf
                (humanTaskDeploymentUnit.getTaskPackageStatus()));
        humanPackage.setProperty(HumanTaskConstants.HUMANTASK_PACKAGE_PROP_LATEST_VERSION,
                                 Long.toString(humanTaskDeploymentUnit.getVersion()));
        configRegistry.put(HumanTaskPackageRepositoryUtils.getResourcePathForHumanTaskPackage
                (humanTaskDeploymentUnit), humanPackage);
    }

    /**
     * Create parent collection for human task package using DeploymentUnitDAO
     *
     * @param deploymentUnitDAO
     * @throws RegistryException
     */
    private void createHumanTaskPackageParentCollectionWithProperties(DeploymentUnitDAO deploymentUnitDAO)
            throws RegistryException {
        Collection humanPackage = configRegistry.newCollection();
        humanPackage.setProperty(HumanTaskConstants.HUMANTASK_PACKAGE_PROP_LATEST_CHECKSUM, deploymentUnitDAO
                .getChecksum());
        if (log.isDebugEnabled()) {
            log.debug(deploymentUnitDAO.getPackageName() + " updating checksum: " + deploymentUnitDAO
                    .getChecksum() + " in registry");
        }
        humanPackage.setProperty(HumanTaskConstants.HUMANTASK_PACKAGE_PROP_STATUS, String.valueOf
                (deploymentUnitDAO.getStatus()));
        humanPackage.setProperty(HumanTaskConstants.HUMANTASK_PACKAGE_PROP_LATEST_VERSION,
                                 Long.toString(deploymentUnitDAO.getVersion()));
        configRegistry.put(HumanTaskPackageRepositoryUtils.getResourcePathForHumanTaskPackage
                (deploymentUnitDAO), humanPackage);
    }

    /**
     * Update the properties of existing human task package in the registry
     *
     * @param humanTaskDeploymentUnit
     * @throws RegistryException
     */
    private void updateHumanTaskPackageProperties(HumanTaskDeploymentUnit humanTaskDeploymentUnit)
            throws RegistryException {
        String packageLocation =
                HumanTaskPackageRepositoryUtils.getResourcePathForHumanTaskPackage(humanTaskDeploymentUnit);
        Resource humanTaskPackage = configRegistry.get(packageLocation);
        humanTaskPackage.setProperty(HumanTaskConstants.HUMANTASK_PACKAGE_PROP_LATEST_CHECKSUM,
                                     humanTaskDeploymentUnit.getMd5sum());
        if (log.isDebugEnabled()) {
            log.debug(humanTaskDeploymentUnit.getPackageName() + " updated checksum to: " +
                      humanTaskDeploymentUnit.getMd5sum());
        }
        humanTaskPackage.setProperty(HumanTaskConstants.HUMANTASK_PACKAGE_PROP_STATUS,
                                     String.valueOf(humanTaskDeploymentUnit.getTaskPackageStatus()));
        humanTaskPackage.setProperty(HumanTaskConstants.HUMANTASK_PACKAGE_PROP_LATEST_VERSION,
                                     Long.toString(humanTaskDeploymentUnit.getVersion()));
        configRegistry.put(packageLocation, humanTaskPackage);
    }

    /**
     * Add latest human task package zip to the registry
     *
     * @param humanTaskDeploymentUnit
     * @param humanTaskFile
     * @throws FileNotFoundException
     * @throws RegistryException
     */
    private void addLatestArchiveToRegistryCollection(HumanTaskDeploymentUnit humanTaskDeploymentUnit,
                                                      File humanTaskFile)
            throws FileNotFoundException, RegistryException {
        Resource latestHumanTaskArchive = configRegistry.newResource();
        latestHumanTaskArchive.setContent(new FileInputStream(humanTaskFile));
        configRegistry.put(HumanTaskPackageRepositoryUtils.getHumanTaskPackageArchiveResourcePath
                (humanTaskDeploymentUnit.getPackageName()), latestHumanTaskArchive);
    }

    /**
     * Add latest human task package zip to the registry
     *
     * @param deploymentUnitDAO
     * @param humanTaskFile
     * @throws FileNotFoundException
     * @throws RegistryException
     */
    private void addLatestArchiveToRegistryCollection(DeploymentUnitDAO deploymentUnitDAO,
                                                      File humanTaskFile)
            throws FileNotFoundException, RegistryException {
        Resource latestHumanTaskArchive = configRegistry.newResource();
        latestHumanTaskArchive.setContent(new FileInputStream(humanTaskFile));
        configRegistry.put(HumanTaskPackageRepositoryUtils.getHumanTaskPackageArchiveResourcePath
                (deploymentUnitDAO.getPackageName()), latestHumanTaskArchive);
    }

    /**
     * This repository persist extracted human task package content inside the human task Package collection
     * under the child collection 'versions'. The collection name is same as directory with version
     * attached to it's name(Example: ClaimsApprovalTask-3).
     * <p/>
     * For the 'ClaimsApprovalTask' human task package, extracted human task package will be stored in a registry
     * location like '<config_registry_root>/humantask/packages/ClaimsApprovalTask/versions/ClaimsApprovalTask-3'.
     *
     * @param humanTaskDeploymentUnit containing information on current human task deployment.
     * @throws RegistryException if an error occurred during import of file system content to
     *                           registry.
     */
    private void createCollectionWithHumanTaskPackageContentForCurrentVersion(
            HumanTaskDeploymentUnit humanTaskDeploymentUnit) throws RegistryException {
        String collectionLocation =
                HumanTaskPackageRepositoryUtils.getResourcePathForHumanTaskPackageVersions(humanTaskDeploymentUnit);
        RegistryClientUtils.importToRegistry(new File(humanTaskArchiveRepo + File.separator +
                                                      humanTaskDeploymentUnit.getName()), collectionLocation,
                                             configRegistry);
    }

    /**
     * This repository persist extracted human task package content inside the human task Package collection
     * under the child collection 'versions'.
     *
     * @param deploymentUnitDAO
     * @throws RegistryException
     */
    private void createCollectionWithHumanTaskPackageContentForCurrentVersion(
            DeploymentUnitDAO deploymentUnitDAO) throws RegistryException {
        String collectionLocation =
                HumanTaskPackageRepositoryUtils.getResourcePathForHumanTaskPackageVersions(deploymentUnitDAO);
        RegistryClientUtils.importToRegistry(new File(humanTaskArchiveRepo + File.separator +
                                                      deploymentUnitDAO.getName()), collectionLocation,
                                             configRegistry);
    }

    /**
     * Un-deploy the human task package from registry
     *
     * @param packageName
     * @throws RegistryException
     */
    public void handleHumanTaskPackageUndeploy(String packageName) throws RegistryException {

        try {
            String packageLocation =
                    HumanTaskPackageRepositoryUtils.getResourcePathForHumanTaskPackage(packageName);
            if (!configRegistry.getRegistryContext().isReadOnly() && configRegistry.resourceExists(packageLocation)) {
                configRegistry.delete(packageLocation);
            }
        } catch (RegistryException re) {
            String errMessage = "Unable to access registry for handling HumanTask package undeployment."
                                + " Package: " + packageName;
            log.error(errMessage, re);
            throw re;
        }
    }

    /**
     * Handles exception and rollbacks an already started transaction. Don't use this method if
     * you haven't already started a registry transaction
     *
     * @param msg - Message to log
     * @param e   - original exception
     * @throws RegistryException on registry rollback error case, we'll init the cause to the
     *                           original exception we got when accessing registry
     */
    protected void handleExceptionWithRollback(String msg, Exception e)
            throws Exception {
        Exception cachedException = null;
        log.error(msg, e);
        try {
            configRegistry.rollbackTransaction();
        } catch (RegistryException re) {
            cachedException = re;
            log.error("Transaction rollback failed", re);
        }

        if (cachedException != null) {
            cachedException.initCause(e);
            throw cachedException;
        } else {
            throw e;
        }
    }
}
