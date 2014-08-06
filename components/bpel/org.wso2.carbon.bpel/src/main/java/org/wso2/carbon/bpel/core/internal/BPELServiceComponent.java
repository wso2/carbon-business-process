/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.bpel.core.internal;

import com.hazelcast.core.HazelcastInstance;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.bpel.core.Axis2ConfigurationContextObserverImpl;
import org.wso2.carbon.bpel.core.BPELEngineService;
import org.wso2.carbon.bpel.core.BPELEngineServiceImpl;
import org.wso2.carbon.bpel.core.ode.integration.BPELSchedulerInitializer;
import org.wso2.carbon.bpel.core.ode.integration.BPELServer;
import org.wso2.carbon.bpel.core.ode.integration.BPELServerImpl;
import org.wso2.carbon.core.ServerStartupHandler;
import org.wso2.carbon.datasource.DataSourceInformationRepositoryService;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.service.TenantRegistryLoader;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.Axis2ConfigurationContextObserver;
import org.wso2.carbon.attachment.mgt.server.AttachmentServerService;
import org.wso2.carbon.utils.ConfigurationContextService;

/**
 * @scr.component name="org.wso2.carbon.bpel.BPELServiceComponent" immediate="true"
 * @scr.reference name="datasource.information.repository.service"
 * interface="org.wso2.carbon.datasource.DataSourceInformationRepositoryService"
 * cardinality="1..1" policy="dynamic"  bind="setDataSourceInformationRepositoryService"
 * unbind="unsetDataSourceInformationRepositoryService"
 * @scr.reference name="registry.service" interface="org.wso2.carbon.registry.core.service.RegistryService"
 * cardinality="1..1" policy="dynamic"  bind="setRegistryService" unbind="unsetRegistryService"
 * @scr.reference name="attachment.mgt.service" interface="org.wso2.carbon.attachment.mgt.server.AttachmentServerService"
 * cardinality="1..1" policy="dynamic"  bind="setAttachmentMgtService"
 * unbind="unsetAttachmentMgtService"
 * @scr.reference name="user.realmservice.default"
 * interface="org.wso2.carbon.user.core.service.RealmService"
 * cardinality="1..1" policy="dynamic" bind="setRealmService"
 * unbind="unsetRealmService"
 * @scr.reference name="hazelcast.instance.service"
 * interface="com.hazelcast.core.HazelcastInstance" cardinality="0..1"
 * policy="dynamic" bind="setHazelcastInstance" unbind="unsetHazelcastInstance"
 */

public class BPELServiceComponent {
    private static Log log = LogFactory.getLog(BPELServiceComponent.class);
    private BundleContext bundleContext;
    private boolean dataSourceInfoRepoProvided = false;
    private ServiceRegistration registration;

    protected void activate(ComponentContext ctxt) {
        try {
            this.bundleContext = ctxt.getBundleContext();
            if (dataSourceInfoRepoProvided) {
                initializeBPELServer();
                registerAxis2ConfigurationContextObserver();
                registerBPELServerService();
            }

            bundleContext.registerService(ServerStartupHandler.class.getName(),
                    new BPELSchedulerInitializer(), null);

        } catch (Throwable t) {
            log.error("Failed to activate BPEL Core bundle", t);
        }
        if (log.isDebugEnabled()) {
            log.debug("BPEL Core bundle is activated.");
        }
    }

    protected void setDataSourceInformationRepositoryService(
            DataSourceInformationRepositoryService repositoryService) {
        if (log.isDebugEnabled()) {
            log.debug("DataSourceInformationRepositoryService bound to the BPEL component");
        }
        this.dataSourceInfoRepoProvided = true;
    }

    protected void unsetDataSourceInformationRepositoryService(
            DataSourceInformationRepositoryService repositoryService) {
        if (log.isDebugEnabled()) {
            log.debug("DataSourceInformationRepositoryService unbound from the BPEL component");
        }
        this.dataSourceInfoRepoProvided = false;
    }

    protected void setRegistryService(RegistryService registrySvc) {
        if (log.isDebugEnabled()) {
            log.debug("RegistryService bound to the BPEL component");
        }
        BPELServerHolder.getInstance().setRegistryService(registrySvc);
    }

    protected void unsetRegistryService(RegistryService registrySvc) {
        if (log.isDebugEnabled()) {
            log.debug("RegistryService unbound from the BPEL component");
        }
        BPELServerHolder.getInstance().setRegistryService(null);
    }

    public static TenantRegistryLoader getTenantRegistryLoader() {
        return BPELServerHolder.getInstance().getRegistryLoader();
    }

    public static RegistryService getRegistryService() {
        return BPELServerHolder.getInstance().getRegistryService();
    }

    public static BPELServer getBPELServer() {
        return BPELServerHolder.getInstance().getBpelServer();
    }

    private void registerAxis2ConfigurationContextObserver() {
        this.bundleContext.registerService(Axis2ConfigurationContextObserver.class.getName(),
                new Axis2ConfigurationContextObserverImpl(),
                null);
    }

    private static void initializeBPELServer() throws Exception {
        BPELServerHolder.getInstance().setBpelServer(BPELServerImpl.getInstance());
        log.info("Initializing BPEL Engine........");
        BPELServerHolder.getInstance().getBpelServer().init();

        // To handle JVM shutdowns
        Runtime.getRuntime().addShutdownHook(new BPELServerShutDown(BPELServerHolder.getInstance().
                getBpelServer()));
    }

    private void registerBPELServerService() {
        registration = this.bundleContext.registerService(BPELEngineService.class.getName(),
                new BPELEngineServiceImpl(), null);
    }

    protected void deactivate(ComponentContext componentContext) throws Exception {

        if (log.isDebugEnabled()) {
            log.debug("Stopping the BPEL Core Component");
        }
        componentContext.getBundleContext().ungetService(registration.getReference());
    }

    private static final class BPELServerShutDown extends Thread {
        private BPELServerImpl bpsServer;

        private BPELServerShutDown(BPELServerImpl bpsServer) {
            super();
            this.bpsServer = bpsServer;
        }

        @Override
        public void run() {
            log.info("Shutting down BPEL Server........");
            try {
                bpsServer.shutdown();
            } catch (Exception e) {
                log.error("Error when shutting down BPEL Server.", e);
            }
        }
    }

    /**
     * Initializes the Attachment-Mgt Service dependency
     *
     * @param attMgtService Attachment-Mgt Service reference
     */
    protected void setAttachmentMgtService(AttachmentServerService attMgtService) {
        BPELServerHolder.getInstance().setAttachmentService(attMgtService);
    }

    /**
     * De-reference the Attachment-Mgt Service dependency
     *
     * @param attMgtService Attachment-Mgt Service reference
     */
    protected void unsetAttachmentMgtService(AttachmentServerService attMgtService) {
        BPELServerHolder.getInstance().setAttachmentService(null);
    }

    protected void setRealmService(RealmService realmService) {
        BPELServerHolder.getInstance().setRealmService(realmService);
    }

    protected void unsetRealmService(RealmService realmService) {
        BPELServerHolder.getInstance().setRealmService(null);
    }

    public static RealmService getRealmService(){
        return BPELServerHolder.getInstance().getRealmService();
    }

    public void setHazelcastInstance(HazelcastInstance hazelcastInstance) {
        BPELServerHolder.getInstance().setHazelcastInstance(hazelcastInstance);
    }

    public void unsetHazelcastInstance(HazelcastInstance hazelcastInstance) {
        BPELServerHolder.getInstance().setHazelcastInstance(null);
    }

    public static HazelcastInstance getHazelcastInstance(){
        return BPELServerHolder.getInstance().getHazelcastInstance();
    }

    protected void setConfigurationContextService(ConfigurationContextService configCtxService) {
        BPELServerHolder.getInstance().setConfigCtxService(configCtxService);
    }

    protected void unsetConfigurationContextService(ConfigurationContextService configCtxService) {
        BPELServerHolder.getInstance().setConfigCtxService(null);
    }

}
