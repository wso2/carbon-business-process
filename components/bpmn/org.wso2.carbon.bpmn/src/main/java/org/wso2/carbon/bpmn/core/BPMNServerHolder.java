package org.wso2.carbon.bpmn.core;

import com.hazelcast.core.HazelcastInstance;
import org.activiti.engine.ProcessEngine;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bpmn.core.deployment.TenantManager;
import org.wso2.carbon.registry.core.service.RegistryService;

public class BPMNServerHolder {

    private static Log log = LogFactory.getLog(BPMNServerHolder.class);

    private ProcessEngine engine = null;
    private TenantManager tenantManager = null;
    private RegistryService registryService = null;
    private HazelcastInstance hazelcastInstance;

    private static BPMNServerHolder bpmnServerHolder = null;

    private BPMNServerHolder() {}

    public static BPMNServerHolder getInstance() {
        if (bpmnServerHolder == null) {
            bpmnServerHolder = new BPMNServerHolder();
        }
        return bpmnServerHolder;
    }

    public ProcessEngine getEngine() {
        return engine;
    }

    public void setEngine(ProcessEngine engine) {
        this.engine = engine;
    }

    public TenantManager getTenantManager() {
        return tenantManager;
    }

    public void setTenantManager(TenantManager tenantManager) {
        this.tenantManager = tenantManager;
    }

    public void setRegistryService(RegistryService registryService) {
        this.registryService = registryService;
    }

    public void unsetRegistryService(RegistryService registryService) {
        this.registryService = null;
    }

    public RegistryService getRegistryService() {
        return registryService;
    }

    public HazelcastInstance getHazelcastInstance() {
        return hazelcastInstance;
    }

    public void setHazelcastInstance(HazelcastInstance hazelcastInstance) {
        this.hazelcastInstance = hazelcastInstance;
    }
}
