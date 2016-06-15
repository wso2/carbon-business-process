package org.wso2.carbon.bpmn.core;

import org.activiti.engine.ProcessEngine;
import org.wso2.carbon.bpmn.core.deployment.BPMNDeployer;
import org.wso2.carbon.security.caas.user.core.service.RealmService;

/**
 *
 */
public class BPMNEngineServiceImpl implements BPMNEngineService {

    private ProcessEngine processEngine;
    private RealmService carbonRealmService;
    private BPMNDeployer bpmnDeployer;

    @Override
    public ProcessEngine getProcessEngine() {
        return processEngine;
    }

    public void setProcessEngine(ProcessEngine processEngine) {
        this.processEngine = processEngine;
    }

    public RealmService getCarbonRealmService() {
        return carbonRealmService;
    }

    public void setCarbonRealmService(RealmService service) {
        this.carbonRealmService = service;
    }

    @Override
    public BPMNDeployer getBpmnDeployer() {
        return bpmnDeployer;
    }

    public void setBpmnDeployer(BPMNDeployer bpmnDeployer) {
        this.bpmnDeployer = bpmnDeployer;
    }
}
