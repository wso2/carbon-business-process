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
package org.wso2.carbon.bpel.core.ode.integration.mgt.services;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bpel.core.BPELConstants;
import org.wso2.carbon.bpel.core.ode.integration.BPELServerImpl;
import org.wso2.carbon.bpel.core.ode.integration.store.BPELUIException;
import org.wso2.carbon.bpel.core.ode.integration.store.TenantProcessStoreImpl;
import org.wso2.carbon.bpel.core.ode.integration.store.repository.BPELPackageInfo;
import org.wso2.carbon.bpel.core.ode.integration.store.repository.BPELPackageRepository;
import org.wso2.carbon.bpel.core.ode.integration.utils.AdminServiceUtils;
import org.wso2.carbon.bpel.skeleton.ode.integration.mgt.services.BPELPackageManagementServiceSkeletonInterface;
import org.wso2.carbon.bpel.skeleton.ode.integration.mgt.services.PackageManagementException;
import org.wso2.carbon.bpel.skeleton.ode.integration.mgt.services.types.DeployedPackagesPaginated;
import org.wso2.carbon.bpel.skeleton.ode.integration.mgt.services.types.PackageStatusType;
import org.wso2.carbon.bpel.skeleton.ode.integration.mgt.services.types.PackageType;
import org.wso2.carbon.bpel.skeleton.ode.integration.mgt.services.types.Processes_type0;
import org.wso2.carbon.bpel.skeleton.ode.integration.mgt.services.types.UndeployStatus_type0;
import org.wso2.carbon.bpel.skeleton.ode.integration.mgt.services.types.Version_type0;
import org.wso2.carbon.bpel.skeleton.ode.integration.mgt.services.types.Versions_type0;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.core.AbstractAdmin;

import java.util.Collections;
import java.util.List;
import javax.xml.namespace.QName;

/**
 * BPEL Package management admin service.
 */
public class BPELPackageManagementServiceSkeleton extends AbstractAdmin
        implements BPELPackageManagementServiceSkeletonInterface {
    private static Log log = LogFactory.getLog(BPELPackageManagementServiceSkeleton.class);

    public PackageType listProcessesInPackage(String packageName) throws PackageManagementException {
        TenantProcessStoreImpl tenantProcessStore = getTenantProcessStore();
        BPELPackageRepository packageRepo = tenantProcessStore.getBPELPackageRepository();
        try {
            return getPackageInfo(packageRepo.getBPELPackageInfoForPackage(packageName.substring(0,packageName.lastIndexOf("-"))));
        } catch (Exception e) {
            String errMsg = "BPEL package: " + packageName + " failed to load from registry.";
            log.error(errMsg, e);
            throw new PackageManagementException(errMsg, e);
        }
    }

    public String getLatestVersionInPackage(String packageName) throws PackageManagementException {
        TenantProcessStoreImpl tenantProcessStore = getTenantProcessStore();
        BPELPackageRepository packageRepo = tenantProcessStore.getBPELPackageRepository();
        try {
            return packageRepo.getBPELPackageInfoForPackage(packageName).getLatestVersion();
        } catch (Exception e) {
            String errMsg = "BPEL package: " + packageName + " failed to get latest version.";
            log.error(errMsg, e);
            throw new PackageManagementException(errMsg, e);
        }
    }

    public UndeployStatus_type0 undeployBPELPackage(String packageName) {
        if (log.isDebugEnabled()) {
            log.debug("Starting un deployment of BPEL package " + packageName);
        }
        TenantProcessStoreImpl tenantProcessStore = getTenantProcessStore();
        try {
            tenantProcessStore.undeploy(packageName);
        } catch (BPELUIException e) {
            //There are instances more than then deletion limit. Not an error, better to abort un deploy to avoid
            // timeout exceptions
            log.warn("Instance deletion limit reached, aborting un deploy. Try deleting instances manually.");
            return UndeployStatus_type0.INSTANCE_DELETE_LIMIT_REACHED;
        } catch (Exception e) {
            log.error("Un-deploying BPEL package " + packageName + " failed.", e);
            return UndeployStatus_type0.FAILED;
        }

        return UndeployStatus_type0.SUCCESS;
    }

    public DeployedPackagesPaginated listDeployedPackagesPaginated(int page)
            throws PackageManagementException {
        int tPage = page;
        List<BPELPackageInfo> packages;
        DeployedPackagesPaginated paginatedPackages = new DeployedPackagesPaginated();
        TenantProcessStoreImpl tenantProcessStore = getTenantProcessStore();

        BPELPackageRepository packageRepo = tenantProcessStore.getBPELPackageRepository();
        try {
            packages = packageRepo.getBPELPackages();   // Can return null and we should handle that
        } catch (Exception e) {
            String errorMessage = "Cannot get the BPEL Package list from repository.";
            log.error(errorMessage, e);
            throw new PackageManagementException(errorMessage, e);
        }

        if (packages != null) {
            // Calculating pagination information
            if (tPage < 0 || tPage == Integer.MAX_VALUE) {
                tPage = 0;
            }
            int startIndex = tPage * BPELConstants.ITEMS_PER_PAGE;
            int endIndex = (tPage + 1) * BPELConstants.ITEMS_PER_PAGE;

            int numberOfPackages = packages.size();
            int totalPackages = 0;
            BPELPackageInfo[] packagesArray =
                    packages.toArray(new BPELPackageInfo[numberOfPackages]);

            for (int i = 0; i < numberOfPackages; i++) {
                int count = getPackageVersionCount(packagesArray[i]);
                if (totalPackages + count > startIndex && totalPackages < endIndex) {
//                    In-order to get the total number of packages count
//                    if (totalPackages >= endIndex) {
//                        break;
//                    }
                    int maxRemainingPackages =
                            totalPackages < startIndex && (totalPackages + count) > startIndex ?
                                    startIndex - (totalPackages + count) :
                                    endIndex - totalPackages;
                    PackageType packageType = getPackageInfo(packagesArray[i], maxRemainingPackages);
                    paginatedPackages.add_package(packageType);
                }
                totalPackages += count;
            }
            int pages = (int) Math.ceil((double) totalPackages / BPELConstants.ITEMS_PER_PAGE);
            paginatedPackages.setPages(pages);
        } else {
            // Returning empty result set with pages equal to zero for cases where null is returned from
            // BPEL repo.
            paginatedPackages.setPages(0);
        }

        return paginatedPackages;
    }

    public DeployedPackagesPaginated listDeployedPackagesPaginated(int page, String packageSearchString)
            throws PackageManagementException {
        int tPage = page;
        List<BPELPackageInfo> packages;
        DeployedPackagesPaginated paginatedPackages = new DeployedPackagesPaginated();
        TenantProcessStoreImpl tenantProcessStore = getTenantProcessStore();

        BPELPackageRepository packageRepo = tenantProcessStore.getBPELPackageRepository();
        try {
            packages = packageRepo.getBPELPackages();   // Can return null and we should handle that
        } catch (Exception e) {
            String errorMessage = "Cannot get the BPEL Package list from repository.";
            log.error(errorMessage, e);
            throw new PackageManagementException(errorMessage, e);
        }

        if (packages != null) {
            // Calculating pagination information
            if (tPage < 0 || tPage == Integer.MAX_VALUE) {
                tPage = 0;
            }
            int startIndex = tPage * BPELConstants.ITEMS_PER_PAGE;
            int endIndex = (tPage + 1) * BPELConstants.ITEMS_PER_PAGE;

            int numberOfPackages = packages.size();
            int totalPackages = 0;
            BPELPackageInfo[] packagesArray =
                    packages.toArray(new BPELPackageInfo[numberOfPackages]);

            for (int i = 0; i < numberOfPackages; i++) {
                if (!packagesArray[i].getName().toLowerCase().contains(packageSearchString.toLowerCase())) {
                    continue;
                }
                int count = getPackageVersionCount(packagesArray[i]);
                if (totalPackages + count > startIndex && totalPackages < endIndex) {
//                    In-order to get the total number of packages count
//                    if (totalPackages >= endIndex) {
//                        break;
//                    }
                    int maxRemainingPackages =
                            totalPackages < startIndex && (totalPackages + count) > startIndex ?
                                    startIndex - (totalPackages + count) :
                                    endIndex - totalPackages;
                    PackageType packageType = getPackageInfo(packagesArray[i], maxRemainingPackages);
                    paginatedPackages.add_package(packageType);
                }
                totalPackages += count;
            }
            int pages = (int) Math.ceil((double) totalPackages / BPELConstants.ITEMS_PER_PAGE);
            paginatedPackages.setPages(pages);
        } else {
            // Returning empty result set with pages equal to zero for cases where null is returned from
            // BPEL repo.
            paginatedPackages.setPages(0);
        }

        return paginatedPackages;
    }

    private PackageType getPackageInfo(BPELPackageInfo packageInfo, int maxRemainingPackages)
            throws PackageManagementException {
        PackageType bpelPackage = new PackageType();
        bpelPackage.setName(packageInfo.getName());
        bpelPackage.setState(convertToPackageStatusType(packageInfo.getStatus()));
        bpelPackage.setVersions(getAllVersionsOfPackage(packageInfo, maxRemainingPackages));
        bpelPackage.setErrorLog(packageInfo.getCauseForDeploymentFailure());
        return bpelPackage;
    }

    private Versions_type0 getAllVersionsOfPackage(BPELPackageInfo packageInfo,
                                                   int maxRemainingPackages)
            throws PackageManagementException {
        Versions_type0 versionsList = new Versions_type0();
        List<String> versions = packageInfo.getAvailableVersions();
        Collections.reverse(versions);
        int count = 0;
        int startIndex = maxRemainingPackages < 0 ? versions.size() + maxRemainingPackages : 0;
        int endIndex = versions.size() - 1;
        for (int i = startIndex; i <= endIndex && Math.abs(maxRemainingPackages) > count; i++) {
            String version = versions.get(i);
            Version_type0 packageVersion = new Version_type0();
            packageVersion.setName(version);
            packageVersion.setProcesses(getProcessesForPackage(version));
            if (version.equals(packageInfo.getName() + "-" + packageInfo.getLatestVersion())) {
                packageVersion.setIsLatest(true);
            } else {
                packageVersion.setIsLatest(false);
            }
            versionsList.addVersion(packageVersion);
            count++;
        }

        return versionsList;
    }

    private int getPackageVersionCount(BPELPackageInfo bpelPackageInfo) {
        return bpelPackageInfo.getAvailableVersions().size();
    }

    private PackageType getPackageInfo(BPELPackageInfo packageInfo)
            throws PackageManagementException {
        return getPackageInfo(packageInfo, packageInfo.getAvailableVersions().size());
    }

    private Processes_type0 getProcessesForPackage(String version) throws PackageManagementException {
        Processes_type0 processes = new Processes_type0();
        try {
            List<QName> processIds = getTenantProcessStore().getProcessesInPackage(version);
            for (QName pid : processIds) {
                processes.addProcess(AdminServiceUtils.createLimitedProcessInfoObject(
                        AdminServiceUtils.getTenantProcessStore().getProcessConfiguration(pid)));
            }
        } catch (Exception e) {
            String errMsg = "Error occurred while listing processes in BPEL package: " + version;
            log.error(errMsg, e);
            throw new PackageManagementException(errMsg, e);
        }

        return processes;
    }

    private PackageStatusType convertToPackageStatusType(BPELPackageInfo.Status status) {
        if (status.equals(BPELPackageInfo.Status.DEPLOYED)) {
            return PackageStatusType.DEPLOYED;
        } else if (status.equals(BPELPackageInfo.Status.UNDEPLOYED)) {
            return PackageStatusType.UNDEPLOYED;
        } else if (status.equals(BPELPackageInfo.Status.FAILED)) {
            return PackageStatusType.FAILED;
        } else if (status.equals(BPELPackageInfo.Status.UPDATED)) {
            return PackageStatusType.UPDATED;
        }
        return PackageStatusType.UNDEFINED;
    }

    private TenantProcessStoreImpl getTenantProcessStore() {
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();

        BPELServerImpl bpelServer = BPELServerImpl.getInstance();

        return (TenantProcessStoreImpl) bpelServer.getMultiTenantProcessStore().
                getTenantsProcessStore(tenantId);
    }

}
    
