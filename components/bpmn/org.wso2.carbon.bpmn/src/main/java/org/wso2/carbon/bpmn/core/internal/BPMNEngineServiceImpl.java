package org.wso2.carbon.bpmn.core.internal;

import org.camunda.bpm.engine.ProcessEngine;
import org.wso2.carbon.bpmn.core.BPMNEngineService;

public class BPMNEngineServiceImpl implements BPMNEngineService {

    private ProcessEngine processEngine;
    @Override
    public ProcessEngine getProcessEngine() {
        return processEngine;
    }

    public void setProcessEngine(ProcessEngine processEngine){
        this.processEngine = processEngine;
    }
}
