/**
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 **/


package org.wso2.carbon.bpmn.rest;

import org.activiti.engine.FormService;
import org.activiti.engine.HistoryService;
import org.activiti.engine.IdentityService;
import org.activiti.engine.ManagementService;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.wso2.carbon.bpmn.core.BPMNEngineService;
import org.wso2.carbon.bpmn.core.integration.BPSGroupIdentityManager;
import org.wso2.carbon.security.caas.user.core.service.RealmService;

/**
 * BPMN Rest Service Component Service.
 */
public interface BPMNRestService {

    RepositoryService getRepositoryService();

    BPMNEngineService getBPMNEngineService();

    RuntimeService getRumtimeService();

    HistoryService getHistoryService();

    TaskService getTaskService();

    ProcessEngineConfiguration getProcessEngineConfiguration();

    FormService getFormService();

    IdentityService getIdentityService();

    RealmService getUserRealm();

    ManagementService getManagementService();

    BPSGroupIdentityManager getGroupIdentityManager();
}
