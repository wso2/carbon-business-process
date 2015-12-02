package org.wso2.carbon.bpmn.stats.rest.internal;

import org.activiti.engine.EngineServices;
import org.activiti.engine.ProcessEngine;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.bpmn.stats.rest.Test;
import org.wso2.carbon.bpmn.stats.rest.UserAdminClient;
import org.wso2.carbon.um.ws.api.stub.UserStoreExceptionException;
import org.wso2.carbon.user.core.UserStoreException;

import java.util.Arrays;
/**
 * Created by natasha on 11/20/15.
 */

/**
 * @scr.component name="org.wso2.carbon.bpmn.stats.rest.internal.BPMNStatsServiceComponent" immediate="true"
 * @scr.reference name="bpmn.service" interface="org.activiti.engine.ProcessEngine"
 * cardinality="1..1" policy="dynamic"  bind="setProcessEngine" unbind="unsetProcessEngine"
 */
public class BPMNStatsServiceComponent {

    private static final Log log = LogFactory.getLog(BPMNStatsServiceComponent.class);

    protected void activate(ComponentContext ctxt) {
        log.info("Initializing the BPMN Stats component...");
      
    }

    protected void deactivate(ComponentContext ctxt) {
        log.info("Stopping the BPMN Stats component...");
    }


    protected void setProcessEngine(ProcessEngine processEngine) {
        BPMNStatsHolder.setEngine( processEngine);
       Test tm= new Test();
        //UserAdminClient.getUserList();
        log.info(" - - - - - - - - - - - - - - - ");
      // log.info(" ! ! ! ! ! ! ! ! !"+tm.getDeployedBPMNProcesses());
      // tm.getCountOfProcessInstanceStatus();
        log.info(" - -  - - - - - - - - - - ");
    //   tm.getCountOfTaskInstanceStatus();
      // tm.getAvgTimeDurationForCompletedProcesses();
       // tm.getNoOfTasksCompletedByUser();
      // tm.taskVariationOverTime();
     //   tm.processVariationOverTime();
       // tm.avgTaskTimeDurationForCompletedProcesses();
        // tm.avgTaskTimeDurationForCompletedProcesses();
       // tm.getAvgTimeTakenToCompleteTasksByUser();
      //  tm.getNoOfTasksCompletedByUser();
        //tm.getDeployedProcesses();
       //tm.getCountOfProcessInstanceStatus();
      //  tm.getCountOfTaskInstanceStatus();




    }

    protected void unsetProcessEngine(ProcessEngine processEngine){
        BPMNStatsHolder.setEngine(null);
    }
}
