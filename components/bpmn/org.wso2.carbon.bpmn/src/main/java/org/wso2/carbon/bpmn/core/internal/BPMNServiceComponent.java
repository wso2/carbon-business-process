package org.wso2.carbon.bpmn.core.internal;

import org.activiti.engine.ProcessEngines;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.bpmn.core.ActivitiEngineBuilder;
import org.wso2.carbon.bpmn.core.BPMNServerHolder;
import org.wso2.carbon.bpmn.core.BPSException;
import org.wso2.carbon.bpmn.core.deployment.TenantManager;
import org.wso2.carbon.registry.core.service.RegistryService;
//import org.wso2.carbon.registry.api.RegistryService;

/**
 * @scr.component name="org.wso2.carbon.bpmn.core.internal.BPMNServiceComponent" immediate="true"
 * @scr.reference name="registry.service" interface="org.wso2.carbon.registry.core.service.RegistryService"
 * cardinality="1..1" policy="dynamic"  bind="setRegistryService" unbind="unsetRegistryService"
 */
public class BPMNServiceComponent {

    private static Log log = LogFactory.getLog(BPMNServiceComponent.class);

    protected void activate(ComponentContext ctxt) {
        log.info("Initializing the BPMN core component...");
        try {
            BPMNServerHolder holder = BPMNServerHolder.getInstance();
            ActivitiEngineBuilder activitiEngineBuilder = new ActivitiEngineBuilder();
            holder.setEngine(activitiEngineBuilder.buildEngine());
            holder.setTenantManager(new TenantManager());
        } catch (BPSException e) {
            String msg = "Failed to initialize the BPMN core component.";
            log.error(msg, e);
        }
    }

    protected void deactivate(ComponentContext ctxt) {
        log.info("Stopping the BPMN core component...");
        ProcessEngines.destroy();
    }

    protected void setRegistryService(RegistryService registrySvc) {
        if (log.isDebugEnabled()) {
            log.debug("RegistryService bound to the BPMN component");
        }
        BPMNServerHolder.getInstance().setRegistryService(registrySvc);
    }

    public void unsetRegistryService(RegistryService registryService) {
        if (log.isDebugEnabled()) {
            log.debug("RegistryService unbound from the BPMN component");
        }
        BPMNServerHolder.getInstance().unsetRegistryService(registryService);
    }
}
