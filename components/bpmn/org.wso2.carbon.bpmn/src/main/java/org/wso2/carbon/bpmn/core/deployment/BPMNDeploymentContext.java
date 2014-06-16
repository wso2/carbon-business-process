package org.wso2.carbon.bpmn.core.deployment;

import org.wso2.carbon.utils.CarbonUtils;

import java.io.File;

public class BPMNDeploymentContext {

    private int tenantId;

    private File bpmnArchive;

    public BPMNDeploymentContext(int tenantId) {
        this.tenantId = tenantId;
    }

    public int getTenantId() {
        return tenantId;
    }

    public void setTenantId(int tenantId) {
        this.tenantId = tenantId;
    }

    public File getBpmnArchive() {
        return bpmnArchive;
    }

    public void setBpmnArchive(File bpmnArchive) {
        this.bpmnArchive = bpmnArchive;
    }
}
