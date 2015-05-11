
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

package org.wso2.carbon.humantask.core.integration.jmx;

import org.apache.axis2.databinding.types.URI;
import org.wso2.carbon.humantask.client.api.IllegalAccessFault;
import org.wso2.carbon.humantask.client.api.IllegalArgumentFault;
import org.wso2.carbon.humantask.client.api.IllegalOperationFault;
import org.wso2.carbon.humantask.client.api.IllegalStateFault;
import org.wso2.carbon.humantask.skeleton.mgt.services.PackageManagementException;

/**
 ???? devide this into two MBean classes??
 */
public interface HTTaskStatusMonitorMXBean {

    //@DescriptorKey("number of cache slots in use")

    /**
     *  @return String[] contains all deployed tasks details (task instances count, tenant ID, task def name, task name, operation, port name) for all tenants
     */
    public String[] showAllTaskDefinitions() throws IllegalAccessFault, IllegalArgumentFault, IllegalStateFault, IllegalOperationFault, PackageManagementException;


    /**
     *
     * @param taskName  can be a task name or task def name, based on that the result will be given, preferred: task def name
     * @return String[] of task instances' details ( task instances count for each  status)
     * @throws IllegalAccessFault
     * @throws IllegalArgumentFault
     * @throws IllegalStateFault
     * @throws IllegalOperationFault
     */
    public String[] getInstanceCountForTaskDefinition(String taskName) throws IllegalAccessFault, IllegalArgumentFault, IllegalStateFault, IllegalOperationFault;

    /**
     *
     * @param status give a valid status to search the task instances in that status
     * @return  String[] of all task instances for the given status
     * @throws IllegalStateFault
     * @throws IllegalOperationFault
     * @throws IllegalArgumentFault
     * @throws IllegalAccessFault
     */
    public String[] getInstancesListForTaskState(String status) throws IllegalStateFault, IllegalOperationFault, IllegalArgumentFault, IllegalAccessFault;

    /**
     *
     * @param taskID  taskId of the task instance which details should be displayed
     * @return all the available details of the task instance
     * @throws IllegalAccessFault
     * @throws IllegalArgumentFault
     * @throws IllegalStateFault
     * @throws IllegalOperationFault
     * @throws URI.MalformedURIException
     */
    public String[] getTaskInstanceDetails(String taskID) throws IllegalAccessFault, IllegalArgumentFault, IllegalStateFault, IllegalOperationFault, URI.MalformedURIException;

    /**
     *
     * @return
     */
    public String getName();
}

