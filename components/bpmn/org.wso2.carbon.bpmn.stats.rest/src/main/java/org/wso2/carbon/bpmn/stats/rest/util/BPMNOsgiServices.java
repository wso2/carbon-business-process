package org.wso2.carbon.bpmn.stats.rest.util;

import org.activiti.engine.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bpmn.core.BPMNEngineService;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.user.api.UserRealm;

/**
 * Define the BPMN Osgi Services
 */
public class BPMNOsgiServices {
    private static final Log log = LogFactory.getLog(BPMNOsgiServices.class);

    public static RepositoryService getRepositoryService() {
        RepositoryService repositoryService = null;

        if (getBPMNEngineService().getProcessEngine() != null) {
            repositoryService = getBPMNEngineService().getProcessEngine().getRepositoryService();

            if (repositoryService == null) {
                log.info("Repository Service is null");
            }
        }

        return repositoryService;
    }

    public static BPMNEngineService getBPMNEngineService() {

        BPMNEngineService bpmnEngineService = (BPMNEngineService) PrivilegedCarbonContext.
                getThreadLocalCarbonContext().getOSGiService(BPMNEngineService.class, null);

        if (bpmnEngineService == null) {
            log.info("BPMNEngineService service couldn't be identified");
        }

        return bpmnEngineService;
    }

    public static RuntimeService getRumtimeService() {

        RuntimeService runtimeService = null;

        if (getBPMNEngineService().getProcessEngine() != null) {
            runtimeService = getBPMNEngineService().getProcessEngine().getRuntimeService();

            if (runtimeService == null) {
                log.info("Runtime Service is null");
            }
        }

        return runtimeService;
    }

    public static HistoryService getHistoryService() {

        HistoryService historyService = null;
        if (getBPMNEngineService().getProcessEngine() != null) {
            historyService = getBPMNEngineService().getProcessEngine().getHistoryService();

            if (historyService == null) {
                log.info("History Service is null");
            }
        }
        return historyService;
    }

    public static TaskService getTaskService() {

        TaskService taskService = null;
        if (getBPMNEngineService().getProcessEngine() != null) {
            taskService = getBPMNEngineService().getProcessEngine().getTaskService();


            if (taskService == null) {
                log.info("Task Service is null");
            }
        }

        return taskService;
    }

    public static ProcessEngineConfiguration getProcessEngineConfiguration() {

        ProcessEngineConfiguration processEngineConfiguration = null;

        if (getBPMNEngineService().getProcessEngine() != null) {
            processEngineConfiguration = getBPMNEngineService().getProcessEngine().getProcessEngineConfiguration();

            if (processEngineConfiguration == null) {
                log.info("ProcessEngineConfiguration couldn't be identified");
            }
        }

        return processEngineConfiguration;
    }

    public static IdentityService getIdentityService() {

        IdentityService identityService = null;
        if (getBPMNEngineService().getProcessEngine() != null) {
            identityService = getBPMNEngineService().getProcessEngine().getIdentityService();

            if (identityService == null) {
                log.info("Identity Service is null");
            }
        }

        return identityService;
    }

    public static ManagementService getManagementService() {

        ManagementService managementService = null;
        if (getBPMNEngineService().getProcessEngine() != null) {
            managementService = getBPMNEngineService().getProcessEngine().getManagementService();

            if (managementService == null) {
                log.info("Management Service is null");
            }
        }

        return managementService;
    }

    public static UserRealm getUserRealm(){
        //PrivilegedCarbonContext.getThreadLocalCarbonContext().getUserRealm().getUserStoreManager().listUsers();
        return PrivilegedCarbonContext.getThreadLocalCarbonContext().getUserRealm();
    }
}