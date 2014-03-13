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

import org.apache.ode.bpel.iapi.ProcessConf;
import org.apache.ode.bpel.iapi.ProcessState;
import org.wso2.carbon.bpel.core.ode.integration.config.bam.BAMServerProfile;

import javax.xml.namespace.QName;
import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * Defines the interface for Tenant specific process store. TenantProcessStore will hold
 * the all information about tenant's BPEL packages and processes.
 */
public interface TenantProcessStore {

    void init() throws Exception;

    /**
     * Deploy a BPEL package in tenant specific process store.
     *
     * @param deploymentUnit BPEL package directory
     * @throws Exception if there is a error during process deployment
     */
    void deploy(File deploymentUnit) throws Exception;

    /**
     * Undeploy a BPEL package deployed in tenant's process store.
     *
     * @param bpelPackageName Name of the BPEL package which going to be undeployed
     * @throws Exception If there is a error when undeploying
     */
    void undeploy(String bpelPackageName) throws Exception;

    /**
     * Handle the tenant unloading. We need to clear process configuraions from memory
     * and cleanup all the resources we hold.
     */
    void handleTenantUnload();

    /**
     * Bring back the tenant process store's process configurations.
     */
    void hydrate();

    /**
     * Get the configuration for the given process id
     *
     * @param pid Process Id
     * @return Process configuration
     */
    ProcessConf getProcessConfiguration(QName pid);

    /**
     * Change the state of the process
     *
     * @param pid          Process ID
     * @param processState Process State
     * @throws Exception If an error occurred while setting state
     */
    void setState(final QName pid, final ProcessState processState) throws Exception;

    /**
     * Set the location where we keep BPEL archives
     *
     * @param bpelArchiveRepo Root directory where we keep BPEL archives
     */
    void setBpelArchiveRepo(File bpelArchiveRepo);

    /**
     * Get the list of Axis services deployed in this process store
     *
     * @return Service list
     */
    Map<QName, Object> getDeployedServices();

    void handleNewBPELPackageDeploymentNotification(String bpelPackageName);

    void handleBPELPackageUndeploymentNotification(String bpelPackageName,
                                                   List<String> versionsOfPackage);

    void handleBPELProcessStateChangedNotification(QName pid, ProcessState processState);

    ProcessConf removeProcessConfiguration(QName pid);

    /**
     * Retrieve the BAM server profile with the provided name
     * @param name BAM server profile name
     * @return BAM server profile
     */
    BAMServerProfile getBAMServerProfile(String name);

    Object getDataPublisher(String processName);

    void addDataPublisher(String processName, Object dataPublisher);

    public Map getDataPublisherMap();
}
