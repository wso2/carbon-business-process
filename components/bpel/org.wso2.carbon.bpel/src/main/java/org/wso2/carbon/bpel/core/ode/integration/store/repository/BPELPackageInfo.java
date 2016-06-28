/**
 * Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.bpel.core.ode.integration.store.repository;

import org.wso2.carbon.bpel.core.BPELConstants;

import java.util.List;

/**
 * Data transfer object which holds information about BPEL package contained in the
 * BPEL package repository.
 */
public class BPELPackageInfo {
    private String name;

    private String latestVersion;

    /**
     * BPEL package status.
     */
    public static enum Status {
        // Newly deployed BPEL Package
        DEPLOYED,

        // Undeployed BPEL package
        UNDEPLOYED,

        // Last deployment failed BPEL package
        FAILED,

        // BPEL package is updated (versioned)
        UPDATED,

        UNDEFINED
    }

    private Status status;

    // Can be null most of the times for good deployments.
    private String causeForDeploymentFailure;

    private List<String> availableVersions;

    private String packageLocationInRegistry;

    private String checksum;

    public BPELPackageInfo() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLatestVersion() {
        return latestVersion;
    }

    public void setLatestVersion(String latestVersion) {
        this.latestVersion = latestVersion;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getCauseForDeploymentFailure() {
        return causeForDeploymentFailure;
    }

    public void setCauseForDeploymentFailure(String causeForDeploymentFailure) {
        this.causeForDeploymentFailure = causeForDeploymentFailure;
    }

    public List<String> getAvailableVersions() {
        return availableVersions;
    }

    public void setAvailableVersions(List<String> availableVersions) {
        this.availableVersions = availableVersions;
    }

    public String getPackageLocationInRegistry() {
        return packageLocationInRegistry;
    }

    public void setPackageLocationInRegistry(String packageLocationInRegistry) {
        this.packageLocationInRegistry = packageLocationInRegistry;
    }

    public String getBPELArchiveFileName() {
        return this.name.concat("." + BPELConstants.BPEL_PACKAGE_EXTENSION);
    }

    public String getChecksum() {
        return checksum;
    }

    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }
}
