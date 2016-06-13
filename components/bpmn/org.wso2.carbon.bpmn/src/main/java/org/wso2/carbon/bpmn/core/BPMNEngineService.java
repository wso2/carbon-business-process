package org.wso2.carbon.bpmn.core;

import org.activiti.engine.ProcessEngine;
import org.wso2.carbon.bpmn.core.deployment.BPMNDeployer;
import org.wso2.carbon.security.caas.user.core.service.RealmService;

/**
 *
 */
public interface BPMNEngineService {

    public ProcessEngine getProcessEngine();

    public RealmService getCarbonRealmService();

    public BPMNDeployer getBpmnDeployer();
}
