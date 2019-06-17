/*
 * Copyright (c) 2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.humantask.core.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.attachment.mgt.server.AttachmentServerService;
import org.wso2.carbon.core.ServerStartupObserver;
import org.wso2.carbon.event.output.adapter.core.OutputEventAdapterService;
import org.wso2.carbon.humantask.core.Axis2ConfigurationContextObserverImpl;
import org.wso2.carbon.humantask.core.HumanTaskEngineService;
import org.wso2.carbon.humantask.core.HumanTaskEngineServiceImpl;
import org.wso2.carbon.humantask.core.HumanTaskSchedulerInitializer;
import org.wso2.carbon.humantask.core.HumanTaskServer;
import org.wso2.carbon.humantask.core.HumanTaskServerShutdown;
import org.wso2.carbon.humantask.core.TaskOperationService;
import org.wso2.carbon.humantask.core.TaskOperationServiceImpl;
import org.wso2.carbon.humantask.core.engine.HumanTaskServerException;
import org.wso2.carbon.humantask.core.integration.jmx.DeployedTasks;
import org.wso2.carbon.humantask.core.integration.jmx.HTTaskStatusMonitor;
import org.wso2.carbon.ndatasource.core.DataSourceService;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.ui.util.UIResourceProvider;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.Axis2ConfigurationContextObserver;
import org.wso2.carbon.utils.MBeanRegistrar;
import org.wso2.carbon.utils.WaitBeforeShutdownObserver;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.NotCompliantMBeanException;

@Component(
        name = "org.wso2.carbon.humantask.HumanTaskServiceComponent",
        immediate = true)
public class HumanTaskServiceComponent {

    /**
     * Class Logger
     */
    private static final Log log = LogFactory.getLog(HumanTaskServiceComponent.class);

    /**
     * The bundle context.
     */
    private BundleContext bundleContext;

    /**
     * Bundle activation method.
     *
     * @param ctxt : The component context.
     */
    @Activate
    protected void activate(ComponentContext ctxt) {

        try {
            this.bundleContext = ctxt.getBundleContext();
            HumanTaskServerHolder htServerHolder = HumanTaskServerHolder.getInstance();
            if (htServerHolder.isDataSourceServiceProvided() && htServerHolder.getRealmService() != null) {
                if (log.isDebugEnabled()) {
                    log.debug("Activating the HumanTaskServiceComponent....");
                }
                initHumanTaskServer(htServerHolder);
                registerAxis2ConfigurationContextObserver();
                registerHumanTaskServerService();
                registerTaskServer();
                registerMBeans();
                if (HumanTaskServerHolder.getInstance().getHtServer().getServerConfig().isUiRenderingEnabled()) {
                    registerHumanTaskUIResourceProvider(htServerHolder);
                }
                bundleContext.registerService(ServerStartupObserver.class.getName(), new
                        HumanTaskSchedulerInitializer(), null);
                bundleContext.registerService(WaitBeforeShutdownObserver.class.getName(), new HumanTaskServerShutdown
                        (), null);
            } else {
                log.warn("Couldn't initialize Human Task Server, " + "realmService == null or dataSourceInfoRepo not " +
                        "provided.");
            }
        } catch (Throwable t) {
            log.error("Failed to activate the HumanTaskServiceComponent.", t);
        }
    }

    // Initializing the human task server.
    private void initHumanTaskServer(HumanTaskServerHolder htServerHolder) throws HumanTaskServerException {

        htServerHolder.setHtServer(new HumanTaskServer());
        log.info("Initialising HumanTask Server");
        htServerHolder.getHtServer().init();
    }

    // Registering the Axis2ConfigurationContextObserver.
    private void registerAxis2ConfigurationContextObserver() {

        log.info("Registering Axis2ConfigurationContextObserver");
        bundleContext.registerService(Axis2ConfigurationContextObserver.class.getName(), new
                Axis2ConfigurationContextObserverImpl(), null);
    }

    // Registering the HumanTaskUIResourceProvider.
    private void registerHumanTaskUIResourceProvider(HumanTaskServerHolder htServerHolder) {

        log.info("Registering HumanTaskUIResourceProvider");
        htServerHolder.setHumanTaskUIResourceProvider(new HumanTaskUIResourceProvider());
        bundleContext.registerService(UIResourceProvider.class.getName(), htServerHolder
                .getHumanTaskUIResourceProvider(), null);
    }

    @Reference(
            name = "user.realmservice.default",
            service = org.wso2.carbon.user.core.service.RealmService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetRealmService")
    protected void setRealmService(RealmService realmService) {

        if (log.isDebugEnabled()) {
            log.debug("Setting the Realm Service");
        }
        HumanTaskServerHolder.getInstance().setRealmService(realmService);
    }

    protected void unsetRealmService(RealmService realmService) {

        if (log.isDebugEnabled()) {
            log.debug("Unsetting the Realm Service");
        }
        HumanTaskServerHolder.getInstance().setRealmService(null);
    }

    public static RealmService getRealmService() {

        return HumanTaskServerHolder.getInstance().getRealmService();
    }

    @Reference(
            name = "datasource.dataSourceService",
            service = org.wso2.carbon.ndatasource.core.DataSourceService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetDataSourceService")
    protected void setDataSourceService(DataSourceService dataSourceService) {

        if (log.isDebugEnabled()) {
            log.debug("DataSourceInformationRepositoryService bound to HumanTask component");
        }
        HumanTaskServerHolder.getInstance().setDataSourceServiceProvided(true);
    }

    protected void unsetDataSourceService(DataSourceService dataSourceService) {

        if (log.isDebugEnabled()) {
            log.debug("DataSourceInformationRepositoryService unbound from HumanTask component");
        }
        HumanTaskServerHolder.getInstance().setDataSourceServiceProvided(false);
    }

    @Reference(
            name = "registry.service",
            service = org.wso2.carbon.registry.core.service.RegistryService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetRegistryService")
    protected void setRegistryService(RegistryService registrySvc) {

        if (log.isDebugEnabled()) {
            log.debug("RegistryService bound to the HumanTask component");
        }
        HumanTaskServerHolder.getInstance().setRegistryService(registrySvc);
    }

    protected void unsetRegistryService(RegistryService registrySvc) {

        if (log.isDebugEnabled()) {
            log.debug("RegistryService unbound from the HumanTask component");
        }
        HumanTaskServerHolder.getInstance().setRegistryService(null);
    }

    public static RegistryService getRegistryService() {

        return HumanTaskServerHolder.getInstance().getRegistryService();
    }

    public static OutputEventAdapterService getOutputEventAdapterService() {

        return HumanTaskServerHolder.getInstance().getOutputEventAdapterService();
    }

    public static HumanTaskServer getHumanTaskServer() {

        return HumanTaskServerHolder.getInstance().getHtServer();
    }

    private void registerHumanTaskServerService() {

        this.bundleContext.registerService(HumanTaskEngineService.class.getName(), new HumanTaskEngineServiceImpl(),
                null);
    }

    /**
     * Initializes the Attachment-Mgt Service dependency
     *
     * @param attMgtService Attachment-Mgt Service reference
     */
    @Reference(
            name = "attachment.mgt.service",
            service = org.wso2.carbon.attachment.mgt.server.AttachmentServerService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetAttachmentMgtService")
    protected void setAttachmentMgtService(AttachmentServerService attMgtService) {

        HumanTaskServerHolder.getInstance().setAttachmentService(attMgtService);
    }

    /**
     * De-reference the Attachment-Mgt Service dependency
     *
     * @param attMgtService Attachment-Mgt Service reference
     */
    protected void unsetAttachmentMgtService(AttachmentServerService attMgtService) {

        HumanTaskServerHolder.getInstance().setAttachmentService(null);
    }

    /**
     * Initialize the Output EventAdapter Service dependency
     *
     * @param outputEventAdapterService Output EventAdapter Service reference
     */
    @Reference(
            name = "event.output.adapter.service",
            service = org.wso2.carbon.event.output.adapter.core.OutputEventAdapterService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetOutputEventAdapterService")
    protected void setOutputEventAdapterService(OutputEventAdapterService outputEventAdapterService) {

        HumanTaskServerHolder.getInstance().setOutputEventAdapterService(outputEventAdapterService);
    }

    /**
     * De-reference the Output EventAdapter Service dependency.
     *
     * @param outputEventAdapterService
     */
    protected void unsetOutputEventAdapterService(OutputEventAdapterService outputEventAdapterService) {

        HumanTaskServerHolder.getInstance().setOutputEventAdapterService(null);
    }

    private void registerTaskServer() {

        this.bundleContext.registerService(TaskOperationService.class.getName(), new TaskOperationServiceImpl(), null);
    }

    /**
     * to register all the MBeans.
     *
     * @throws Exception
     * @throws MBeanRegistrationException
     * @throws InstanceAlreadyExistsException
     * @throws NotCompliantMBeanException
     */
    public void registerMBeans() throws Exception, MBeanRegistrationException, InstanceAlreadyExistsException,
            NotCompliantMBeanException {

        log.info("Registering HT related MBeans");
        HTTaskStatusMonitor taskStatusMonitor = new HTTaskStatusMonitor();
        DeployedTasks deployedTasks = new DeployedTasks();
        MBeanRegistrar.registerMBean(taskStatusMonitor, "org.wso2.carbon.humantask.core.integration" +
                ".jmx:type=HTTaskStatusMonitorMXBean");
        MBeanRegistrar.registerMBean(deployedTasks, "org.wso2.carbon.humantask.core.integration" +
                ".jmx:type=DeployedTasksMXBean");
        log.info("MXBean for Human tasks registered successfully");
    }
}
