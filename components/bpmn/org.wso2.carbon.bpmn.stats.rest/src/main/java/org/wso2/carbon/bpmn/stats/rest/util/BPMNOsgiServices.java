package org.wso2.carbon.bpmn.stats.rest.util;
/**
 * Created by natasha on 11/23/15.
 */

import org.activiti.engine.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bpmn.core.BPMNEngineService;
import org.wso2.carbon.context.PrivilegedCarbonContext;

public class BPMNOsgiServices {
    private static final Log log = LogFactory.getLog(BPMNOsgiServices.class);

    public static RepositoryService getRepositoryService(){
        RepositoryService repositoryService = null;

        if(getBPMNEngineService().getProcessEngine() != null) {
            repositoryService = getBPMNEngineService().getProcessEngine().getRepositoryService();

            if (repositoryService == null) {
                log.info("No repo service");
            }
        }

        return repositoryService;
    }

    public static BPMNEngineService getBPMNEngineService(){

        BPMNEngineService bpmnEngineService = (BPMNEngineService) PrivilegedCarbonContext.
                getThreadLocalCarbonContext().getOSGiService(BPMNEngineService.class, null);

        System.out.println("Name:" + bpmnEngineService.getProcessEngine());
        if(bpmnEngineService == null){
            log.info("BPMNEngineService service couldn't be identified");
        }

        return bpmnEngineService;
    }

    public static RuntimeService getRumtimeService(){

        RuntimeService runtimeService = null;

        if(getBPMNEngineService().getProcessEngine() != null) {
            runtimeService = getBPMNEngineService().getProcessEngine().getRuntimeService();

            if (runtimeService == null) {
                log.info("No runtime service");
            }
        }

        return runtimeService;
    }

    public static HistoryService getHistoryService(){

        HistoryService historyService = null;
        if(getBPMNEngineService().getProcessEngine() != null) {
            historyService = getBPMNEngineService().getProcessEngine().getHistoryService();

            if (historyService == null) {
                log.info("No history service");
            }
        }
        return historyService;
    }

    public static TaskService getTaskService(){

        TaskService taskService = null;
        if(getBPMNEngineService().getProcessEngine() != null) {
            taskService = getBPMNEngineService().getProcessEngine().getTaskService();


            if (taskService == null) {
                log.info("No task service");
            }
        }

        return taskService;
    }

    public static ProcessEngineConfiguration getProcessEngineConfiguration(){

        ProcessEngineConfiguration processEngineConfiguration = null;

        if(getBPMNEngineService().getProcessEngine() != null) {
            processEngineConfiguration = getBPMNEngineService().getProcessEngine().getProcessEngineConfiguration();

            if (processEngineConfiguration == null) {
                log.info("No processEngineConfiguration");
            }
        }

        return processEngineConfiguration;
    }

    public static FormService getFormService(){

        FormService formService = null;
        if(getBPMNEngineService().getProcessEngine() != null) {
            formService = getBPMNEngineService().getProcessEngine().getFormService();
            if (formService == null) {
                log.info("No form service");
            }
        }


        return formService;
    }

    public static IdentityService getIdentityService(){

        IdentityService identityService = null;
        if(getBPMNEngineService().getProcessEngine() != null) {
            identityService = getBPMNEngineService().getProcessEngine().getIdentityService();

            if (identityService == null) {
                log.info("No identity service");
            }
        }

        return identityService;
    }

    public static ManagementService getManagementService(){

        ManagementService managementService = null;
        if(getBPMNEngineService().getProcessEngine() != null) {
            managementService = getBPMNEngineService().getProcessEngine().getManagementService();

            if (managementService == null) {
                log.info("No management service");
            }
        }

        return managementService;
    }
}