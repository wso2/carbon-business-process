/**
 *  Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */


package org.wso2.carbon.bpmn.core.internal;

import org.activiti.engine.ProcessEngines;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.bpmn.core.ActivitiEngineBuilder;
import org.wso2.carbon.bpmn.core.BPMNServerHolder;
import org.wso2.carbon.bpmn.core.deployment.TenantManager;
import org.wso2.carbon.bpmn.core.integration.BPMNEngineShutdown;
import org.wso2.carbon.bpmn.extensions.jms.BPMNJMSExtensionHolder;
import org.wso2.carbon.bpmn.extensions.jms.JMSInvoker;
import org.wso2.carbon.bpmn.extensions.jms.JMSStartTask;
import org.wso2.carbon.bpmn.extensions.rest.BPMNRestExtensionHolder;
import org.wso2.carbon.bpmn.extensions.rest.RESTInvoker;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.utils.WaitBeforeShutdownObserver;

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
            BundleContext bundleContext = ctxt.getBundleContext();
            BPMNServerHolder holder = BPMNServerHolder.getInstance();
            ActivitiEngineBuilder activitiEngineBuilder = new ActivitiEngineBuilder();
            holder.setEngine(activitiEngineBuilder.buildEngine());
            holder.setTenantManager(new TenantManager());

            BPMNRestExtensionHolder restHolder = BPMNRestExtensionHolder.getInstance();

            restHolder.setRestInvoker(new RESTInvoker());

            BPMNJMSExtensionHolder jmsHolder = BPMNJMSExtensionHolder.getInstance();
            jmsHolder.setJmsInvoker(new JMSInvoker());

            bundleContext.registerService(WaitBeforeShutdownObserver.class.getName(), new BPMNEngineShutdown(), null);

//            DataSourceHandler dataSourceHandler = new DataSourceHandler();
//            dataSourceHandler.initDataSource(activitiEngineBuilder.getDataSourceJndiName());
//            dataSourceHandler.closeDataSource();
//        } catch (BPMNMetaDataTableCreationException e) {
//            log.error("Could not create BPMN checksum table", e);
//        } catch (DatabaseConfigurationException e) {
//            log.error("Could not create BPMN checksum table", e);
        }catch (Throwable e) {
            log.error("Failed to initialize the BPMN core component.", e);
        }
    }

	protected void deactivate(ComponentContext ctxt) {
		log.info("Stopping the BPMN core component...");
//		ProcessEngines.destroy();
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
