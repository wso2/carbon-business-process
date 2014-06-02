package org.wso2.carbon.bpmn.rest.integration;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineInfo;
import org.activiti.rest.common.api.ActivitiUtilProvider;

public class BPSActivitiUtilProvider implements ActivitiUtilProvider {

    private ProcessEngine processEngine;

    private ProcessEngineInfo processEngineInfo;

    @Override
    public ProcessEngine getProcessEngine() {
        return processEngine;
    }

    @Override
    public ProcessEngineInfo getProcessEngineInfo() {
        return processEngineInfo;
    }

    public void setProcessEngine(ProcessEngine processEngine) {
        this.processEngine = processEngine;
    }

    public void setProcessEngineInfo(ProcessEngineInfo processEngineInfo) {
        this.processEngineInfo = processEngineInfo;
    }
}
