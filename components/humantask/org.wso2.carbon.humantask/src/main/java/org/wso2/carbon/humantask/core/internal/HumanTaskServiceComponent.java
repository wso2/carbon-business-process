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
import org.wso2.carbon.attachment.mgt.server.AttachmentServerService;
import org.wso2.carbon.core.ServerStartupHandler;
import org.wso2.carbon.datasource.DataSourceInformationRepositoryService;
import org.wso2.carbon.humantask.core.*;
import org.wso2.carbon.humantask.core.engine.HumanTaskServerException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.ui.util.UIResourceProvider;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.Axis2ConfigurationContextObserver;

/**
 * @scr.component name="org.wso2.carbon.humantask.HumanTaskServiceComponent" immediate="true"
 * @scr.reference name="datasource.information.repository.service"
 * interface="org.wso2.carbon.datasource.DataSourceInformationRepositoryService"
 * cardinality="1..1" policy="dynamic"  bind="setDataSourceInformationRepositoryService"
 * unbind="unsetDataSourceInformationRepositoryService"
 * @scr.reference name="registry.service" interface="org.wso2.carbon.registry.core.service.RegistryService"
 * cardinality="1..1" policy="dynamic"  bind="setRegistryService" unbind="unsetRegistryService"
 * @scr.reference name="user.realmservice.default"
 * interface="org.wso2.carbon.user.core.service.RealmService"
 * cardinality="1..1" policy="dynamic" bind="setRealmService"
 * unbind="unsetRealmService"
 * @scr.reference name="attachment.mgt.service"
 * interface="org.wso2.carbon.attachment.mgt.server.AttachmentServerService"
 * cardinality="1..1" policy="dynamic"  bind="setAttachmentMgtService"
 * unbind="unsetAttachmentMgtService"
 */

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
    protected void activate(ComponentContext ctxt) {
        try {
            this.bundleContext = ctxt.getBundleContext();
            HumanTaskServerHolder htServerHolder = HumanTaskServerHolder.getInstance();
            if (htServerHolder.isDataSourceInfoRepoProvided() &&
                htServerHolder.getRealmService() != null) {
                if (log.isDebugEnabled()) {
                    log.debug("Activating the HumanTaskServiceComponent....");
                }

                initHumanTaskServer(htServerHolder);
                registerAxis2ConfigurationContextObserver();
                registerHumanTaskServerService();

                if(HumanTaskServerHolder.getInstance().getHtServer().getServerConfig().isUiRenderingEnabled()) {
                    registerHumanTaskUIResourceProvider(htServerHolder);
                }
                bundleContext.registerService(ServerStartupHandler.class.getName(), new HumanTaskSchedulerInitializer(), null);

            } else {
                log.warn("Couldn't initialize Human Task Server, " +
                         "realmService == null or dataSourceInfoRepo not provided.");
            }
        } catch (Throwable t) {
            log.error("Failed to activate the HumanTaskServiceComponent.", t);
        }
    }

    // Initializing the human task server.
    private void initHumanTaskServer(HumanTaskServerHolder htServerHolder)
            throws HumanTaskServerException {
        htServerHolder.setHtServer(new HumanTaskServer());
        log.info("Initialising HumanTask Server");
        htServerHolder.getHtServer().init();
    }

    // Registering the Axis2ConfigurationContextObserver.
    private void registerAxis2ConfigurationContextObserver() {
        log.info("Registering Axis2ConfigurationContextObserver");
        bundleContext.registerService(Axis2ConfigurationContextObserver.class.getName(),
                                      new Axis2ConfigurationContextObserverImpl(),
                                      null);
    }

    // Registering the HumanTaskUIResourceProvider.
    private void registerHumanTaskUIResourceProvider(HumanTaskServerHolder htServerHolder) {
        log.info("Registering HumanTaskUIResourceProvider");
        htServerHolder.setHumanTaskUIResourceProvider(new HumanTaskUIResourceProvider());
        bundleContext.registerService(UIResourceProvider.class.getName(),
                                      htServerHolder.getHumanTaskUIResourceProvider(), null);
    }

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

    protected void setDataSourceInformationRepositoryService(
            DataSourceInformationRepositoryService dsInformationRepo) {
        if (log.isDebugEnabled()) {
            log.debug("DataSourceInformationRepositoryService bound to HumanTask component");
        }
        HumanTaskServerHolder.getInstance().setDataSourceInfoRepoProvided(true);
    }

    protected void unsetDataSourceInformationRepositoryService(
            DataSourceInformationRepositoryService dsInformationRepo) {
        if (log.isDebugEnabled()) {
            log.debug("DataSourceInformationRepositoryService unbound from HumanTask component");
        }
        HumanTaskServerHolder.getInstance().setDataSourceInfoRepoProvided(false);
    }

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

    public static HumanTaskServer getHumanTaskServer() {
        return HumanTaskServerHolder.getInstance().getHtServer();
    }

    private void registerHumanTaskServerService() {
        this.bundleContext.registerService(HumanTaskEngineService.class.getName(),
                                                          new HumanTaskEngineServiceImpl(), null);
    }

    /**
     * Initializes the Attachment-Mgt Service dependency
     *
     * @param attMgtService Attachment-Mgt Service reference
     */
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
}
