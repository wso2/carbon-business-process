package org.wso2.carbon.bpmn.core.internal;

import org.activiti.engine.ProcessEngine;
import org.wso2.carbon.bpmn.core.BPMNEngineService;
import org.wso2.carbon.security.caas.user.core.service.RealmService;

/**
 *
 */
public class BPMNEngineServiceImpl implements BPMNEngineService {

    private ProcessEngine processEngine;
    private RealmService realmService;

    @Override
    public ProcessEngine getProcessEngine() {
        return processEngine;
    }

    public void setProcessEngine(ProcessEngine processEngine) {
        this.processEngine = processEngine;
    }

    public void setCarbonRealmService(RealmService service) {
        this.realmService = service;
    }

    public RealmService getCarbonRealmService() {
        return realmService;
    }
}
