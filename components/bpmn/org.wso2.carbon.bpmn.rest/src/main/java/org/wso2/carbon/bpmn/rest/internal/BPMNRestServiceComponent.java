package org.wso2.carbon.bpmn.rest.internal;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineInfo;
import org.activiti.rest.common.api.ActivitiUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;
import org.restlet.ext.servlet.ServerServlet;
import org.wso2.carbon.bpmn.core.BPMNServerHolder;
import org.wso2.carbon.bpmn.rest.BPMNRestConstants;
import org.wso2.carbon.bpmn.rest.BPMNRestHolder;
import org.wso2.carbon.bpmn.rest.integration.BPSActivitiUtilProvider;
import org.wso2.carbon.registry.core.service.RegistryService;

import java.util.Dictionary;
import java.util.Hashtable;

/**
 * @scr.component name="org.wso2.carbon.bpmn.rest.internal.BPMNRestServiceComponent" immediate="true"
 * @scr.reference name="http.service" interface="org.osgi.service.http.HttpService"
 * cardinality="1..1" policy="dynamic"  bind="setHttpService" unbind="unsetHttpService"
 * @scr.reference name="registry.service" interface="org.wso2.carbon.registry.core.service.RegistryService"
 * cardinality="1..1" policy="dynamic"  bind="setRegistryService" unbind="unsetRegistryService"
 */
public class BPMNRestServiceComponent {

    private static Log log = LogFactory.getLog(BPMNRestServiceComponent.class);

    protected void activate(ComponentContext ctxt) {
        log.info("Initializing the BPMN REST component...");

        ProcessEngine processEngine = BPMNServerHolder.getInstance().getEngine();
        ProcessEngineInfo processEngineInfo = new ProcessEngineInfo() {
            @Override
            public String getName() {
                return "BPS Process Engine";
            }

            @Override
            public String getResourceUrl() {
                return "Not provided";
            }

            @Override
            public String getException() {
                return "BPS Process Engine Exception";
            }
        };

        BPSActivitiUtilProvider activitiUtilProvider = new BPSActivitiUtilProvider();
        activitiUtilProvider.setProcessEngine(processEngine);
        activitiUtilProvider.setProcessEngineInfo(processEngineInfo);
        ActivitiUtil.setActivitiProvider(activitiUtilProvider);
    }

    protected void deactivate(ComponentContext ctxt) {
        log.info("Stopping the BPMN REST component...");

        if (BPMNRestHolder.getInstance().getHttpService() != null) {
            BPMNRestHolder.getInstance().getHttpService().unregister(BPMNRestConstants.BPMN_REST_CONTEXT_PATH);
        }
    }

    public void setRegistryService(RegistryService registryService) {
        BPMNRestHolder.getInstance().setRegistryService(registryService);
    }

    public void unsetRegistryService(RegistryService registryService) {
        BPMNRestHolder.getInstance().setRegistryService(null);
    }

    public void setHttpService(HttpService httpService) {
        BPMNRestHolder.getInstance().setHttpService(httpService);

        ServerServlet restServlet = new ServerServlet();
        HttpContext defaultHttpContext = httpService.createDefaultHttpContext();
        Dictionary<String, String> initParams = new Hashtable<String, String>();
        String paramName = "org.restlet.application";
        String paramValue = "org.activiti.rest.service.application.ActivitiRestServicesApplication";
        initParams.put(paramName, paramValue);
        

        try {
            httpService.registerServlet(BPMNRestConstants.BPMN_REST_CONTEXT_PATH, restServlet, initParams, defaultHttpContext);
        } catch (Exception e) {
            String msg = "Failed to register the Activiti REST servlet.";
            log.error(msg, e);
        }
    }

    public void unsetHttpService(HttpService httpService) {
        BPMNRestHolder.getInstance().setHttpService(null);
    }
}
