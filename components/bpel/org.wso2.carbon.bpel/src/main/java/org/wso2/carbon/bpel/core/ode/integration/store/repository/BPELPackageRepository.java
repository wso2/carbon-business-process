/**
 *  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.bpel.core.ode.integration.store.repository;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.iapi.ProcessState;
import org.wso2.carbon.bpel.core.BPELConstants;
import org.wso2.carbon.bpel.core.internal.BPELServiceComponent;
import org.wso2.carbon.bpel.core.ode.integration.store.BPELDeploymentContext;
import org.wso2.carbon.bpel.core.ode.integration.store.ProcessConfigurationImpl;
import org.wso2.carbon.bpel.core.ode.integration.store.Utils;
import org.wso2.carbon.bpel.skeleton.ode.integration.mgt.services.ProcessManagementException;
import org.wso2.carbon.bpel.skeleton.ode.integration.mgt.services.types.*;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.utils.RegistryClientUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Central manager who is responsible for managing BPEL archives, BPEL deployment units repository
 * and registry based BPEL deployment unit repository.
 * <p/>
 * How BPEL Packages will get stored in the registry
 * <p/>
 * <BPEL_ROOT>/<BPEL_PACKAGE_NAME>
 * |
 * + prop_bpel.package.latest.version
 * + prop_bpel.package.status
 * + prop_bpel.package.error.log
 * + prop_bpel.package.latest.checksum
 * + versions
 * |
 * +<BPEL_PACKAGE_NAME-version1>
 * +<BPEL_PACKAGE_NAME-version2>
 * +<BPEL_PACKAGE_NAME-version3>
 */
public class BPELPackageRepository {
    private static Log log = LogFactory.getLog(BPELConstants.LOGGER_DEPLOYMENT);

    // Config registry instance use to store BPEL package information for this repository instance.
    private Registry configRegistry;

    // Location of local file system where we keep BPEL deployment units.
    //private File bpelDURepo;

    // Location of local file system where we keep BPEL archives.
    private File bpelArchiveRepo;

    public BPELPackageRepository(Registry configRegistry, File bpelDURepo, File bpelArchiveRepo) {
        this.configRegistry = configRegistry;
        //this.bpelDURepo = bpelDURepo;
        this.bpelArchiveRepo = bpelArchiveRepo;
    }

    /**
     * Initialize the BPEL repository. This method is used to sync the registry and the tenant repositories.
     *
     * @throws RegistryException if there is registry access issue
     */
    public void init() throws Exception {
        if (BPELServiceComponent.getBPELServer().getBpelServerConfiguration().isSyncWithRegistry()) {
            fixLocalBPELArchiveRepository();
        }
    }

    private void addLatestArchiveToRegistryCollection(BPELDeploymentContext bpelDeploymentContext)
            throws FileNotFoundException, RegistryException {
        Resource latestBPELArchive = configRegistry.newResource();
        latestBPELArchive.setContent(new FileInputStream(bpelDeploymentContext.getBpelArchive()));
        configRegistry.put(BPELPackageRepositoryUtils.
                getBPELPackageArchiveResourcePath(bpelDeploymentContext.getBpelPackageName()),
                latestBPELArchive);
    }

//    private File buildBPELDeploymentUnitPath(String bpelArchiveName, long version) {
//        return new File(bpelDURepo,
//                        bpelArchiveName.substring(0,
//                                                  bpelArchiveName.lastIndexOf("." +
//                                                          BPELConstants.BPEL_PACKAGE_EXTENSION)) +
//                                "-" + version);
//    }

    /**
     * Handles deployment of new BPEL packages. Stores all the meta data and BPEL Package content
     * in the registry.
     *
     * @param deploymentContext containing information about current deployment
     * @throws RegistryException on registry access error.
     */
    public void handleNewBPELPackageAddition(BPELDeploymentContext deploymentContext)
            throws Exception {
        try {
            if (!isDUCollectionIsThere(deploymentContext)) {
                configRegistry.beginTransaction();
                createBPELPackageParentCollectionWithProperties(deploymentContext);
                addLatestArchiveToRegistryCollection(deploymentContext);
                createCollectionWithBPELPackageContentForCurrentVersion(deploymentContext);
                configRegistry.commitTransaction();
            }
        } catch (RegistryException re) {
            handleExceptionWithRollback("Unable to handle new BPEL Package addition."
                    + " Package: " + deploymentContext.getBpelPackageName(), re);
        } catch (NoSuchAlgorithmException e) {
            handleExceptionWithRollback("Unable to generate MD5. Adding BPEL package "
                    + deploymentContext.getBpelPackageName() + " to registry failed.", e);
        } catch (IOException e) {
            handleExceptionWithRollback("Unable to find file to generate MD5. Adding BPEL package "
                    + deploymentContext.getBpelPackageName() + " to registry failed.", e);
        }
    }


    /**
     * Update the registry meta data on BPEL package update.
     *
     * @param deploymentContext containing information about current deployment
     * @throws RegistryException on registry access error.
     */
    public void handleBPELPackageUpdate(BPELDeploymentContext deploymentContext)
            throws Exception {
        try {
            if (!isDUCollectionIsThere(deploymentContext)) {
                configRegistry.beginTransaction();
                updateBPELPackageProperties(deploymentContext);
                addLatestArchiveToRegistryCollection(deploymentContext);
                createCollectionWithBPELPackageContentForCurrentVersion(deploymentContext);
                configRegistry.commitTransaction();
            }
        } catch (RegistryException re) {
            handleExceptionWithRollback("Unable to handle BPEL package update."
                    + " Package: " + deploymentContext.getBpelPackageName(), re);
        } catch (NoSuchAlgorithmException e) {
            handleExceptionWithRollback("Unable to generate MD5. Adding BPEL package "
                    + deploymentContext.getBpelPackageName() + " to registry failed.", e);
        } catch (IOException e) {
            handleExceptionWithRollback("Unable to find file to generate MD5. Adding BPEL package "
                    + deploymentContext.getBpelPackageName() + " to registry failed.", e);
        }
    }

    /**
     * Update or create meta data for failed deployment.
     *
     * @param deploymentContext containing information about current deployment
     * @throws RegistryException on registry access error
     */
    public void handleBPELPackageDeploymentError(BPELDeploymentContext deploymentContext)
            throws Exception {
        try {
            if (isExistingBPELPackage(deploymentContext)) {
                handleBPELPackageUpdate(deploymentContext);
            } else {
                handleNewBPELPackageAddition(deploymentContext);
            }
        } catch (RegistryException re) {
            String errMessage = "Unable to handle BPEL package deployment error persistence."
                    + " Package: " + deploymentContext.getBpelPackageName();
            log.error(errMessage, re);
            throw re;
        } catch (NoSuchAlgorithmException e) {
            handleExceptionWithRollback("Unable to generate MD5. Adding BPEL package "
                    + deploymentContext.getBpelPackageName() + " to registry failed.", e);
        } catch (IOException e) {
            handleExceptionWithRollback("Unable to find file to generate MD5. Adding BPEL package "
                    + deploymentContext.getBpelPackageName() + " to registry failed.", e);
        }
    }

    public void handleBPELPackageUndeploy(String packageName) throws RegistryException {
        // TODO : Change this to correct logic after deciding re-deploying logic.
        try {
            String packageLocation =
                    BPELPackageRepositoryUtils.getResourcePathForBPELPackage(packageName);
            if (!configRegistry.getRegistryContext().isReadOnly() && configRegistry.resourceExists(packageLocation)) {
                configRegistry.delete(packageLocation);
            } else {
                //TODO fix this logic
                throw new IllegalAccessException();
            }

        } catch (RegistryException re) {
            String errMessage = "Unable to access registry for handling BPEL package undeployment."
                    + " Package: " + packageName;
            log.error(errMessage, re);
            throw re;
        } catch (IllegalAccessException e) {
            log.error("Trying to update a Read-only registry", e);
        }
    }

    /**
     * Check whether BPEL package with same name exists in the repository.
     *
     * @param deploymentContext containing information about current deployment                 zip
     * @return true if BPEL package exist with same name
     * @throws RegistryException when error occurred while accessing registry
     */
    public Boolean isExistingBPELPackage(BPELDeploymentContext deploymentContext)
            throws RegistryException {
        // We consider a package to be existing, if there is a successfully deployed package.
        // Therefore, if a package is successfully deployed then there should be a corresponding
        // version collection
        return configRegistry.resourceExists(
                BPELPackageRepositoryUtils.getResourcePathForBPELPackage(deploymentContext) +
                        BPELConstants.BPEL_PACKAGE_VERSIONS);
    }

    public Boolean isExistingBPELPackage(String packageName) throws RegistryException {
        return configRegistry.resourceExists(
                BPELPackageRepositoryUtils.getResourcePathForBPELPackage(packageName));
    }

    /**
     * Check for reloading BPEL package.
     * Check is based on last modified time of BPEL archive.
     *
     * @param deploymentContext containing information about current deployment
     * @return true if timestamps match
     * @throws RegistryException   when error occurred while accessing registry
     * @throws java.io.IOException Error occurred while calculating checksum
     * @throws java.security.NoSuchAlgorithmException
     *                             Error occurred while calculating checksum
     */
    public Boolean isBPELPackageReload(BPELDeploymentContext deploymentContext)
            throws RegistryException, IOException, NoSuchAlgorithmException {
        String resourceLocation =
                BPELPackageRepositoryUtils.getResourcePathForBPELPackage(deploymentContext);
        if (configRegistry.resourceExists(resourceLocation)) {
            String md5Checksum = configRegistry.get(resourceLocation).
                    getProperty(BPELConstants.BPEL_PACKAGE_PROP_LATEST_CHECKSUM);
            if (md5Checksum == null) {
                //To make it backward compatible with carbon 3.1.0
                md5Checksum = configRegistry.get(resourceLocation).
                        getProperty(BPELConstants.BPEL_PACKAGE_PROP_LATEST_CHECKSUM_DEPRECATED);
            }
            if (log.isDebugEnabled()) {
                log.debug(deploymentContext.getBpelPackageName() + " Checksum in registry: " +
                        md5Checksum + " : File checksum: " +
                        Utils.getMD5Checksum(deploymentContext.getBpelArchive()));
            }
            return md5Checksum.equals(Utils.getMD5Checksum(deploymentContext.getBpelArchive()));
        }
        return false;
    }

    public BPELPackageInfo getBPELPackageInfo(BPELDeploymentContext deploymentContext)
            throws RegistryException {
        return getBPELPackageInfo(
                BPELPackageRepositoryUtils.getResourcePathForBPELPackage(deploymentContext));
    }

    public List<BPELPackageInfo> getBPELPackages() throws Exception {
        List<BPELPackageInfo> bpelPackages = new ArrayList<BPELPackageInfo>();
        try {
            if (configRegistry.resourceExists(BPELConstants.REG_PATH_OF_BPEL_PACKAGES)) {
                Resource parentCollection = configRegistry.get(
                        BPELConstants.REG_PATH_OF_BPEL_PACKAGES);
                // The above registry resource we retrieve only contains set of child collections.
                // So we can directly cast the returned object to a string array.
                String[] children = (String[]) parentCollection.getContent();
                for (int i = children.length - 1; i >= 0; i--) {
                    bpelPackages.add(getBPELPackageInfo(children[i]));
                }
                return sortByPackageName(bpelPackages);
            }
        } catch (RegistryException re) {
            handleExceptionWithRollback("Unable to get BPEL Packages from Repository.", re);
        }

        return null;
    }

    private List<BPELPackageInfo> sortByPackageName(List<BPELPackageInfo> packageList) {

        List<BPELPackageInfo> sortedPackageList = new ArrayList<BPELPackageInfo>();
        Map<String, BPELPackageInfo> packageInfoMap = new HashMap<String, BPELPackageInfo>();
        for(BPELPackageInfo packageInfo : packageList) {
            packageInfoMap.put(packageInfo.getName(), packageInfo);
        }
        SortedSet<String> sortedPackageNames = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
        sortedPackageNames.addAll(packageInfoMap.keySet());
        for ( String packageName : sortedPackageNames) {
            sortedPackageList.add(packageInfoMap.get(packageName));
        }

        return sortedPackageList;
    }

    /**
     * Get the BPEL Package list from registry. Search for archives which are not present in the
     * local BPEL archive repo. Restore missing archives.
     *
     * @throws Exception if error occurred during registry access
     */
    private void fixLocalBPELArchiveRepository() throws Exception {
        List<BPELPackageInfo> bpelPackages = getBPELPackages();
        //TODO we need to improve this logic to identify the multiple versions deployed under a
        // single bpel package. Probably we need to compare the checksum
        if (bpelArchiveRepo != null && bpelPackages != null) {
            for (BPELPackageInfo bpelPackage : bpelPackages) {
                restoreBPELArchive(bpelPackage);
            }
        }
    }

    public void restoreBPELArchive(BPELPackageInfo bpelPackage) throws RegistryException,
            IOException, NoSuchAlgorithmException {
        File bpelArchive = new File(bpelArchiveRepo, bpelPackage.getBPELArchiveFileName());

        if (log.isDebugEnabled() && bpelArchive.exists()) {
            log.debug(bpelPackage.getName() + " File checksum: " +
                    Utils.getMD5Checksum(bpelArchive) + " : " + bpelPackage.getChecksum());
        }
        if (!bpelArchive.exists() ||
                (bpelArchive.exists() &&
                        !Utils.getMD5Checksum(bpelArchive).equals(bpelPackage.getChecksum()))) {
            if (bpelArchive.exists()) {
                log.info("Outdated BPEL archive " + bpelArchiveRepo.getAbsolutePath() +
                        File.separator + bpelPackage.getBPELArchiveFileName() +
                        " in the local repository. Re-storing from the registry");
            } else {
                log.info("BPEL archive not found " + bpelArchiveRepo.getAbsolutePath() +
                        File.separator + bpelPackage.getBPELArchiveFileName() +
                        " Re-storing from the registry");
            }
            String bpelPackageZipLocation = bpelPackage.getPackageLocationInRegistry() +
                    BPELConstants.PATH_SEPARATOR + bpelPackage.getBPELArchiveFileName();
            if (!configRegistry.resourceExists(bpelPackageZipLocation)) {
                log.warn("Cannot restore " + bpelPackage.getName() +
                        " from registry. The resource: " + bpelPackageZipLocation +
                        " does not exist.");
                return;
            }

//            Resource latestArchive = configRegistry.get(bpelPackageZipLocation);
//            InputStream bpelArchiveInputStream = latestArchive.getContentStream();
//            FileOutputStream bpelArchiveOutputStream = new FileOutputStream(bpelArchive);
//            byte[] buffer = new byte[1024];
//            int len;
//            while ((len = bpelArchiveInputStream.read(buffer)) > 0) {
//                bpelArchiveOutputStream.write(buffer, 0, len);
//            }
//            bpelArchiveOutputStream.close();
//            bpelArchiveInputStream.close();
            RegistryClientUtils.exportFromRegistry(new File(bpelArchiveRepo,
                    bpelPackage.getBPELArchiveFileName()), bpelPackageZipLocation, configRegistry);

        }
    }

    public BPELPackageInfo getBPELPackageInfo(String packageLocationInRegistry)
            throws RegistryException {
        BPELPackageInfo bpelPackage = new BPELPackageInfo();
        String name = packageLocationInRegistry.substring(
                packageLocationInRegistry.lastIndexOf('/') + 1);
        bpelPackage.setName(name);
        bpelPackage.setPackageLocationInRegistry(packageLocationInRegistry);

        Resource packageResource = configRegistry.get(packageLocationInRegistry);
        bpelPackage.setLatestVersion(
                packageResource.getProperty(BPELConstants.BPEL_PACKAGE_PROP_LATEST_VERSION));
        bpelPackage.setChecksum(
                packageResource.getProperty(BPELConstants.BPEL_PACKAGE_PROP_LATEST_CHECKSUM));
        BPELPackageInfo.Status status =
                getStatus(packageResource.getProperty(BPELConstants.BPEL_PACKAGE_PROP_STATUS));
        bpelPackage.setStatus(status);

        if (status.equals(BPELPackageInfo.Status.FAILED)) {
            bpelPackage.setCauseForDeploymentFailure(
                    packageResource.getProperty(BPELConstants.BPEL_PACKAGE_PROP_DEPLOYMENT_ERROR_LOG));
        }

        bpelPackage.setAvailableVersions(getVersionsOfPackage(packageLocationInRegistry));

        return bpelPackage;
    }

    public BPELPackageInfo getBPELPackageInfoForPackage(String packageName)
            throws RegistryException {
        String packageLocationInRegistry =
                BPELPackageRepositoryUtils.getResourcePathForBPELPackage(packageName.substring(0,
                        packageName.lastIndexOf('-')));
        return getBPELPackageInfo(packageLocationInRegistry);
    }

    public List<String> getAllVersionsForPackage(String packageName) throws RegistryException {
        return getVersionsOfPackage(
                BPELPackageRepositoryUtils.getResourcePathForBPELPackage(packageName));
    }

    private List<String> getVersionsOfPackage(String packageLocation) throws RegistryException {
        List<String> versions = new ArrayList<String>();
        String versionsLocation = packageLocation + BPELConstants.BPEL_PACKAGE_VERSIONS;
        // If a new addition of a process fails to deploy there there will not be a collection called
        // version
        if (configRegistry.resourceExists(versionsLocation)) {
            Resource versionsResource = configRegistry.get(versionsLocation);
            // The above registry resource we retrieve only contains set of child collections.
            // So we can directly cast the returned object to a string array.
            String[] children = (String[]) versionsResource.getContent();
            for (String child : children) {
                versions.add(child.substring(child.lastIndexOf("/") + 1));
            }
            Collections.sort(versions, Utils.BY_VERSION);
        }
        return versions;
    }

    private BPELPackageInfo.Status getStatus(String status) {
        if (status.equals(BPELConstants.STATUS_DEPLOYED)) {
            return BPELPackageInfo.Status.DEPLOYED;
        } else if (status.equals(BPELConstants.STATUS_FAILED)) {
            return BPELPackageInfo.Status.FAILED;
        } else if (status.equals(BPELConstants.STATUS_UNDEPLOYED)) {
            return BPELPackageInfo.Status.UNDEPLOYED;
        } else if (status.equals(BPELConstants.STATUS_UPDATED)) {
            return BPELPackageInfo.Status.UPDATED;
        }

        return BPELPackageInfo.Status.UNDEFINED;
    }

    /**
     * Create parent collection to persisting BPEL package information. For example, if you deploy
     * a BPEL archive called 'HelloWorld.zip', we store information of that package in collection
     * named 'HelloWorld'. This will be the root for 'HelloWorld' BPEL package information and
     * there will several versions of this BPEL package in this registry collection which relates
     * to the versions deployed in BPEL engine.
     *
     * @param deploymentContext containing information on current deployment
     * @throws RegistryException        when there is a error accessing registry
     * @throws IOException              if file access error occurred during MD5 checksum generation
     * @throws NoSuchAlgorithmException when there is a error during MD5 generation
     */
    private void createBPELPackageParentCollectionWithProperties(
            BPELDeploymentContext deploymentContext)
            throws RegistryException, IOException, NoSuchAlgorithmException {
        Collection bpelPackage = configRegistry.newCollection();
        bpelPackage.setProperty(BPELConstants.BPEL_PACKAGE_PROP_LATEST_CHECKSUM,
                Utils.getMD5Checksum(deploymentContext.getBpelArchive()));
        if (log.isDebugEnabled()) {
            log.debug(deploymentContext.getBpelPackageName() + " updating checksum: " +
                    Utils.getMD5Checksum(deploymentContext.getBpelArchive()) + " in registry");
        }
        if (deploymentContext.isFailed()) {
            bpelPackage.setProperty(BPELConstants.BPEL_PACKAGE_PROP_STATUS,
                    BPELConstants.STATUS_FAILED);
            bpelPackage.setProperty(BPELConstants.BPEL_PACKAGE_PROP_DEPLOYMENT_ERROR_LOG,
                    deploymentContext.getDeploymentFailureCause());
//            bpelPackage.setProperty(BPELConstants.BPEL_PACKAGE_PROP_DEPLOYMENT_STACK_TRACE,
//                    ExceptionUtils.getStackTrace(deploymentContext.getStackTrace()));
        } else {
            bpelPackage.setProperty(BPELConstants.BPEL_PACKAGE_PROP_STATUS,
                    BPELConstants.STATUS_DEPLOYED);
        }
        bpelPackage.setProperty(BPELConstants.BPEL_PACKAGE_PROP_LATEST_VERSION,
                Long.toString(deploymentContext.getVersion()));

        configRegistry.put(
                BPELPackageRepositoryUtils.getResourcePathForBPELPackage(deploymentContext),
                bpelPackage);
    }

    /**
     * This repository persist extracted BPEL package content inside the BPEL Package collection
     * under the child collection 'versions'. The collection name is same as directory with version
     * attached to it's name(Example: HelloWorld-3).
     * <p/>
     * For the 'HelloWorld' BPEL package, extracted BPEL package will be stored in a registry
     * location like '<config_registry_root>/bpel/packages/HelloWorld/versions/HelloWorld-3'.
     *
     * @param deploymentContext containing information on current BPEL deployment.
     * @throws RegistryException if an error occurred during import of file system content to
     *                           registry.
     */
    private void createCollectionWithBPELPackageContentForCurrentVersion(
            BPELDeploymentContext deploymentContext) throws RegistryException {
        String collectionLocation =
                BPELPackageRepositoryUtils.getResourcePathForBPELPackageVersions(deploymentContext);
        RegistryClientUtils.importToRegistry(deploymentContext.getBPELPackageContent(),
                collectionLocation,
                configRegistry);
    }

    private boolean isDUCollectionIsThere(BPELDeploymentContext deploymentContext)
            throws RegistryException {
        String collectionLocation =
                BPELPackageRepositoryUtils.getResourcePathForBPELPackageContent(deploymentContext);
        return configRegistry.resourceExists(collectionLocation);
    }

    private void updateBPELPackageProperties(BPELDeploymentContext deploymentContext)
            throws RegistryException, IOException, NoSuchAlgorithmException {
        String packageLocation =
                BPELPackageRepositoryUtils.getResourcePathForBPELPackage(deploymentContext);
        Resource bpelPackage = configRegistry.get(packageLocation);
        bpelPackage.setProperty(BPELConstants.BPEL_PACKAGE_PROP_LATEST_CHECKSUM,
                Utils.getMD5Checksum(deploymentContext.getBpelArchive()));
        if (log.isDebugEnabled()) {
            log.debug(deploymentContext.getBpelPackageName() + " updated checksum to: " +
                    Utils.getMD5Checksum(deploymentContext.getBpelArchive()));
        }
        if (deploymentContext.isFailed()) {
            bpelPackage.setProperty(BPELConstants.BPEL_PACKAGE_PROP_STATUS,
                    BPELConstants.STATUS_FAILED);
            bpelPackage.setProperty(BPELConstants.BPEL_PACKAGE_PROP_DEPLOYMENT_ERROR_LOG,
                    deploymentContext.getDeploymentFailureCause());
//            bpelPackage.setProperty(BPELConstants.BPEL_PACKAGE_PROP_DEPLOYMENT_STACK_TRACE,
//                    ExceptionUtils.getStackTrace(deploymentContext.getStackTrace()));
        } else {
            bpelPackage.setProperty(BPELConstants.BPEL_PACKAGE_PROP_STATUS,
                    BPELConstants.STATUS_UPDATED);
        }
        bpelPackage.setProperty(BPELConstants.BPEL_PACKAGE_PROP_LATEST_VERSION,
                Long.toString(deploymentContext.getVersion()));
        configRegistry.put(packageLocation, bpelPackage);
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

    /**
     * Creates new properties for the details of updated deployment descriptor information
     * for a process  in the package location of the registry
     *
     * @param processConfiguration - Process's configuration details after updated
     * @throws RegistryException          on registry rollback error case, we'll init the cause to the
     *                                    original exception we got when accessing registry
     * @throws IOException                if file access error occurred during MD5 checksum generation
     * @throws NoSuchAlgorithmException   when there is a error during MD5 generation
     * @throws ProcessManagementException
     */
    public void createPropertiesForUpdatedDeploymentInfo(ProcessConfigurationImpl processConfiguration)
            throws RegistryException, IOException, NoSuchAlgorithmException, ProcessManagementException {

        String versionlessPackageName = BPELPackageRepositoryUtils.getVersionlessPackageName(processConfiguration.getPackage());
        String packageLocation =
                BPELPackageRepositoryUtils.getResourcePathForDeployInfoUpdatedBPELPackage(processConfiguration.getPackage(), versionlessPackageName);
        Resource bpelPackage = configRegistry.get(packageLocation);

        bpelPackage.setProperty(BPELConstants.BPEL_INSTANCE_CLEANUP_FAILURE + processConfiguration.getProcessId(),
                BPELPackageRepositoryUtils.getBPELPackageFailureCleanUpsAsString(processConfiguration.getCleanupCategories(false)));
        bpelPackage.setProperty(BPELConstants.BPEL_INSTANCE_CLEANUP_SUCCESS + processConfiguration.getProcessId(),
                BPELPackageRepositoryUtils.getBPELPackageSuccessCleanUpsInList(processConfiguration.getCleanupCategories(true)));
        bpelPackage.setProperty(BPELConstants.BPEL_PROCESS_EVENT_GENERATE + processConfiguration.getProcessId(),
                BPELPackageRepositoryUtils.getBPELPackageProcessGenerateType(processConfiguration.getGenerateType()));
        bpelPackage.setProperty(BPELConstants.BPEL_PROCESS_EVENTS + processConfiguration.getProcessId(),
                BPELPackageRepositoryUtils.getBPELPackageProcessEventsInList(processConfiguration.getEvents()));
        bpelPackage.setProperty(BPELConstants.BPEL_PROCESS_INMEMORY + processConfiguration.getProcessId(),
                String.valueOf(processConfiguration.isTransient()));
        bpelPackage.setProperty(BPELConstants.BPEL_PROCESS_STATE + processConfiguration.getProcessId(),
                processConfiguration.getState().name());
        // ScopeLevelEnabledEvents list of a process in a bpel package
        List<String> scopeEvents;
        scopeEvents = BPELPackageRepositoryUtils.getBPELPackageScopeEventsInList(processConfiguration.getEvents());
        if (!scopeEvents.isEmpty()) {
            for (int k = 0; k < scopeEvents.size(); k++) {
                bpelPackage.setProperty(BPELConstants.BPEL_PROCESS_SCOPE_EVENT + (k + 1) + processConfiguration.getProcessId(),
                        scopeEvents.get(k));
            }
        }
        configRegistry.put(packageLocation, bpelPackage);
    }

    /**
     * Reads the updated properties from registry and sets the process configuration fields
     *
     * @param processConfiguration - Process's configuration details after updated
     * @param bpelPackageName      - the relevant bpel package
     * @throws RegistryException          on registry rollback error case, we'll init the cause to the
     *                                    original exception we got when accessing registry
     * @throws ProcessManagementException
     */
    public void readPropertiesOfUpdatedDeploymentInfo(ProcessConfigurationImpl processConfiguration, String bpelPackageName) throws RegistryException, ProcessManagementException {
        String versionlessPackageName = BPELPackageRepositoryUtils.getVersionlessPackageName(bpelPackageName);
        String packageLocation =
                BPELPackageRepositoryUtils.getResourcePathForDeployInfoUpdatedBPELPackage(processConfiguration.getPackage(), versionlessPackageName);
        Resource bpelPackage = configRegistry.get(packageLocation);

        String stateInString = bpelPackage.getProperty(BPELConstants.BPEL_PROCESS_STATE +
                processConfiguration.getProcessId());
        String inMemoryInString = bpelPackage.getProperty(BPELConstants.BPEL_PROCESS_INMEMORY +
                processConfiguration.getProcessId());
        String processEventsInString = bpelPackage.getProperty(BPELConstants.BPEL_PROCESS_EVENTS +
                processConfiguration.getProcessId());
        String generateTypeString = bpelPackage.getProperty(BPELConstants.BPEL_PROCESS_EVENT_GENERATE +
                processConfiguration.getProcessId());
        String successCleanupsInString = bpelPackage.getProperty(BPELConstants.BPEL_INSTANCE_CLEANUP_SUCCESS +
                processConfiguration.getProcessId());
        String failureCleanupsInString = bpelPackage.getProperty(BPELConstants.BPEL_INSTANCE_CLEANUP_FAILURE +
                processConfiguration.getProcessId());

        //  first checks whether the process state is written to the registry, if so that implies the
        //  editor has been updated, read the updated fields
        if (stateInString != null) {
            ProcessState state = BPELPackageRepositoryUtils.getProcessState(stateInString);
            processConfiguration.setState(state);
            processConfiguration.setIsTransient(Boolean.parseBoolean(inMemoryInString));
            ProcessEventsListType processEventsList = new ProcessEventsListType();
            EnableEventListType enabledEventList =
                    BPELPackageRepositoryUtils.getEnabledEventsListFromString(processEventsInString);
            processEventsList.setEnableEventsList(enabledEventList);
            Generate_type1 generateType =
                    BPELPackageRepositoryUtils.getProcessGenerateTypeFromString(generateTypeString);
            processEventsList.setGenerate(generateType);
            ScopeEventListType scopeEventList = new ScopeEventListType();

            int j = 0;
            while (bpelPackage.getProperty(BPELConstants.BPEL_PROCESS_SCOPE_EVENT + (j + 1) +
                    processConfiguration.getProcessId()) != null) {
                ScopeEventType scopeEvent =
                        BPELPackageRepositoryUtils.getScopeEventFromString(bpelPackage.
                                getProperty(BPELConstants.BPEL_PROCESS_SCOPE_EVENT + (j + 1) +
                                        processConfiguration.getProcessId()));
                scopeEventList.addScopeEvent(scopeEvent);
                j++;
            }

            processEventsList.setScopeEventsList(scopeEventList);
            processConfiguration.setProcessEventsList(processEventsList);
            CleanUpListType cleanUpList = new CleanUpListType();
            CleanUpType successCleanUp =
                    BPELPackageRepositoryUtils.getSuccessCleanUpType(successCleanupsInString);
            cleanUpList.addCleanUp(successCleanUp);
            CleanUpType failureCleanUp =
                    BPELPackageRepositoryUtils.getFailureCleanUpType(failureCleanupsInString);
            cleanUpList.addCleanUp(failureCleanUp);
            processConfiguration.setProcessCleanupConfImpl(cleanUpList);

        }

    }

}
