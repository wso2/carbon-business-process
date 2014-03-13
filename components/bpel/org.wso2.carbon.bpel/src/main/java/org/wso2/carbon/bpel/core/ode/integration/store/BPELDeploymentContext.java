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

package org.wso2.carbon.bpel.core.ode.integration.store;

import org.wso2.carbon.bpel.core.BPELConstants;

import javax.xml.namespace.QName;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Data transfer object which holds BPEL deployment specific state information.
 * <p/>
 * New BPELDeploymentContext object will be created for each new BPEL archive deployment and
 * it'll use as the container for state information for that deployment flow.
 */
public class BPELDeploymentContext {
    private Integer tenantId;

    private String bpelFileSystemRepoRoot;

    private long version;

    private String archiveName;

    private String bpelPackageName;

    private String bpelPackageNameWithVersion;

    private String bpelPackageLocationInFileSystem;

    private File bpelArchive;

    private File currentVersionOfBPELPackage;

    private String errorCause;

    private Throwable stackTrace;

    private List<QName> processIds = new ArrayList<QName>();

    private boolean isExistingPackage;

    private boolean isFailed;

    public BPELDeploymentContext(Integer tenantId,
                                 String bpelFileSystemRepoRoot,
                                 File bpelArchive,
                                 long version) {
        this.tenantId = tenantId;
        this.bpelArchive = bpelArchive;
        this.version = version;
        this.bpelFileSystemRepoRoot = bpelFileSystemRepoRoot;
        calculateMetaData();
    }

    private void calculateMetaData() {
        archiveName = bpelArchive.getName();
        bpelPackageName = archiveName.substring(0, archiveName.
                lastIndexOf("." + BPELConstants.BPEL_PACKAGE_EXTENSION));
        bpelPackageNameWithVersion = bpelPackageName + "-" + version;
        bpelPackageLocationInFileSystem = getTheLocationToExtract(bpelPackageNameWithVersion);
        currentVersionOfBPELPackage = new File(bpelPackageLocationInFileSystem);
    }

    private String getTheLocationToExtract(String bpelPackageNameWithVersion) {
        return bpelFileSystemRepoRoot + File.separator + tenantId + File.separator
                + bpelPackageNameWithVersion;
    }

    public String getArchiveName() {
        return archiveName;
    }

    public File getBPELPackageContent() {
        return currentVersionOfBPELPackage;
    }

    public String getBpelPackageName() {
        return bpelPackageName;
    }

    public String getBpelPackageNameWithVersion() {
        return bpelPackageNameWithVersion;
    }

    public String getBpelPackageLocationInFileSystem() {
        return bpelPackageLocationInFileSystem;
    }

    public File getBpelArchive() {
        return bpelArchive;
    }

    public String getDeploymentFailureCause() {
        return errorCause;
    }

    public void setDeploymentFailureCause(String errorCause) {
        this.errorCause = errorCause;
    }

    public long getVersion() {
        return version;
    }

    public void addProcessId(QName pid) {
        processIds.add(pid);
    }

    public List<QName> getProcessIdsForCurrentDeployment(){
        return processIds;     
    }

    public String getProcessIdsInCurrentVersion() {
        String pids = "";
        for (QName n : processIds) {
            pids = pids.concat(n.toString()).concat(",");
        }

        return pids.substring(0, pids.length() - 1);
    }

    public Throwable getStackTrace() {
        return stackTrace;
    }

    public void setStackTrace(Throwable stackTrace) {
        this.stackTrace = stackTrace;
    }

    public boolean isExistingPackage() {
        return isExistingPackage;
    }

    public void setExistingPackage(boolean existingPackage) {
        isExistingPackage = existingPackage;
    }

    public boolean isFailed() {
        return isFailed;
    }

    public void setFailed(boolean failed) {
        isFailed = failed;
    }
}