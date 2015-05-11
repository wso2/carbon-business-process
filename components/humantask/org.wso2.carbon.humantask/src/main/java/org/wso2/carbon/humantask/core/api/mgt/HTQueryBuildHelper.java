/*
 *
 *   Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 * /
 */

package org.wso2.carbon.humantask.core.api.mgt;

import org.apache.axis2.databinding.types.URI;
import org.wso2.carbon.humantask.client.api.IllegalAccessFault;
import org.wso2.carbon.humantask.client.api.IllegalArgumentFault;
import org.wso2.carbon.humantask.client.api.IllegalOperationFault;
import org.wso2.carbon.humantask.client.api.IllegalStateFault;
import org.wso2.carbon.humantask.core.store.HumanTaskBaseConfiguration;
import org.wso2.carbon.humantask.skeleton.mgt.services.PackageManagementException;
import org.wso2.carbon.humantask.types.TPredefinedStatus;

import java.util.List;


public interface HTQueryBuildHelper {

    public int getTaskCountsForStateAndTaskName(String taskName, TPredefinedStatus.Enum status) throws IllegalStateFault, IllegalOperationFault, IllegalArgumentFault, IllegalAccessFault;

    public int getTaskCountsForStateAndTaskDefName(String taskName, TPredefinedStatus.Enum status) throws IllegalStateFault, IllegalOperationFault, IllegalArgumentFault, IllegalAccessFault;

    public DeployedTaskDetail[][] getAllDeployedTasksDetails() throws IllegalAccessFault, IllegalArgumentFault, IllegalStateFault, IllegalOperationFault ;


    public String[] getAllDeployedTasks() throws IllegalAccessFault, IllegalArgumentFault, IllegalStateFault, IllegalOperationFault, PackageManagementException;

    public List<HumanTaskBaseConfiguration> getAlldeployedTasks(long tenantID);

    public String[] getTaskInstanceCountsByState(String taskname) throws IllegalAccessFault, IllegalArgumentFault, IllegalStateFault, IllegalOperationFault;

    public String[] getTaskInstances(final TPredefinedStatus.Enum status) throws IllegalAccessFault, IllegalArgumentFault, IllegalStateFault, IllegalOperationFault;

    public String[] getTaskDataById(String taskid) throws IllegalAccessFault, IllegalArgumentFault, IllegalStateFault, IllegalOperationFault, URI.MalformedURIException;

    public long[] getAllTenantIDs() throws IllegalAccessFault, IllegalArgumentFault, IllegalStateFault, IllegalOperationFault;

    public int getTaskInstanceCountForTaskName(final String taskname) throws IllegalAccessFault, IllegalArgumentFault, IllegalStateFault, IllegalOperationFault;
    }
