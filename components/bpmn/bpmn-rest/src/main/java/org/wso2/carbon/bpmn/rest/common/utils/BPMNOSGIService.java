/**
 * Copyright (c) 2015 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.wso2.carbon.bpmn.rest.common.utils;

import org.activiti.engine.*;
import org.wso2.carbon.bpmn.core.BPMNEngineService;
import org.wso2.carbon.bpmn.rest.common.exception.BPMNOSGIServiceException;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.user.api.UserRealm;

public class BPMNOSGIService {

    public static RepositoryService getRepositoryService() {
        return getBPMNEngineService().getProcessEngine().getRepositoryService();
    }

    public static BPMNEngineService getBPMNEngineService() {
        BPMNEngineService bpmnEngineService = (BPMNEngineService) PrivilegedCarbonContext.
                getThreadLocalCarbonContext().getOSGiService(BPMNEngineService.class, null);

        if (bpmnEngineService == null) {
            throw new BPMNOSGIServiceException("BPMNEngineService service couldn't be identified");
        }
        return bpmnEngineService;
    }

    public static RuntimeService getRumtimeService() {

        RuntimeService runtimeService = null;

        if (getBPMNEngineService().getProcessEngine() != null) {
            runtimeService = getBPMNEngineService().getProcessEngine().getRuntimeService();
            if (runtimeService == null) {
                throw new BPMNOSGIServiceException("Runtime service couldn't be identified");
            }
        }

        return runtimeService;
    }

    public static HistoryService getHistoryService() {

        HistoryService historyService = null;
        if (getBPMNEngineService().getProcessEngine() != null) {
            historyService = getBPMNEngineService().getProcessEngine().getHistoryService();
            if (historyService == null) {
                throw new BPMNOSGIServiceException("History service couldn't be identified");
            }
        }
        return historyService;
    }

    public static TaskService getTaskService() {

        TaskService taskService = null;
        if (getBPMNEngineService().getProcessEngine() != null) {
            taskService = getBPMNEngineService().getProcessEngine().getTaskService();
            if (taskService == null) {
                throw new BPMNOSGIServiceException("Taskservice couldn't be identified");
            }
        }


        return taskService;
    }

    public static ProcessEngineConfiguration getProcessEngineConfiguration() {

        ProcessEngineConfiguration processEngineConfiguration = null;

        if (getBPMNEngineService().getProcessEngine() != null) {
            processEngineConfiguration = getBPMNEngineService().getProcessEngine().getProcessEngineConfiguration();

            if (processEngineConfiguration == null) {
                throw new BPMNOSGIServiceException("ProcessEngineConfiguration couldn't be identified");
            }
        }

        return processEngineConfiguration;
    }

    public static FormService getFormService() {

        FormService formService = null;
        if (getBPMNEngineService().getProcessEngine() != null) {
            formService = getBPMNEngineService().getProcessEngine().getFormService();
            if (formService == null) {
                throw new BPMNOSGIServiceException("FormService couldn't be identified");
            }
        }

        return formService;
    }

    public static IdentityService getIdentityService() {

        IdentityService identityService = null;
        if (getBPMNEngineService().getProcessEngine() != null) {
            identityService = getBPMNEngineService().getProcessEngine().getIdentityService();
            if (identityService == null) {
                throw new BPMNOSGIServiceException("IdentityService couldn't be identified");
            }
        }

        return identityService;
    }


    public static UserRealm getUserRealm() {
        return PrivilegedCarbonContext.getThreadLocalCarbonContext().getUserRealm();
    }

    public static  ManagementService getManagementService() {
        return getBPMNEngineService().getProcessEngine().getManagementService();
   }

}
