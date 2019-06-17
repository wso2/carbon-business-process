/*
 * Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.attachment.mgt.server.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.attachment.mgt.server.AttachmentServer;
import org.wso2.carbon.attachment.mgt.server.AttachmentServerService;
import org.wso2.carbon.attachment.mgt.server.AttachmentServerServiceImpl;
import org.wso2.carbon.attachment.mgt.servlet.AttachmentDownloadServlet;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.ndatasource.core.DataSourceService;

import java.util.Dictionary;
import java.util.Hashtable;
import javax.servlet.Servlet;
import javax.servlet.http.HttpServlet;

@Component(
        name = "org.wso2.carbon.attachment.mgt.server.internal.AttachmentServiceComponent",
        immediate = true)
public class AttachmentServiceComponent {

    /**
     * Class Logger
     */
    private static final Log log = LogFactory.getLog(AttachmentServiceComponent.class);

    /**
     * Equals to true if the data-source is available when Attachment-Mgt bundle is resolved.
     */
    private boolean dataSourceServiceProvided = false;

    /**
     * The bundle context.
     */
    private BundleContext bundleContext;

    /**
     * Bundle activation method.
     *
     * @param componentContext : The component context.
     */
    @Activate
    protected void activate(ComponentContext componentContext) {

        try {
            PrivilegedCarbonContext.startTenantFlow();
            this.bundleContext = componentContext.getBundleContext();
            if (dataSourceServiceProvided) {
                initAttachmentServer();
                registerAttachmentDownloadServlet();
                registerAttachmentServerService();
            }
            PrivilegedCarbonContext.endTenantFlow();
        } catch (Throwable t) {
            log.error("Failed to activate Attachment management bundle", t);
        }
        if (log.isDebugEnabled()) {
            log.debug("Attachment management bundle is activated.");
        }
    }

    /**
     * Initializing Attachment Server
     */
    private void initAttachmentServer() {

        AttachmentServerHolder.getInstance().setAttachmentServer(AttachmentServer.getInstance());
        log.info("Initialising Attachment Server");
        AttachmentServerHolder.getInstance().getAttachmentServer().init();
    }

    /**
     * Registering the Axis2ConfigurationContextObserver.
     */
    private void registerAttachmentServerService() {

        log.info("Registering AttachmentServerService");
        bundleContext.registerService(AttachmentServerService.class.getName(), new AttachmentServerServiceImpl(), null);
    }

    /**
     * Register the Servlet used to download attachments
     */
    private void registerAttachmentDownloadServlet() {

        HttpServlet attachmentDownloadServlet = new AttachmentDownloadServlet();
        Dictionary redirectorParams = new Hashtable(1);
        redirectorParams.put("url-pattern", "/attachment-mgt/download");
        // redirectorParams.put("url-pattern", "/t/carbon.super" + AttachmentMgtConfigurationConstants
        // .ATTACHMENT_DOWNLOAD_SERVELET_URL_PATTERN);
        bundleContext.registerService(Servlet.class.getName(), attachmentDownloadServlet, redirectorParams);
        if (log.isDebugEnabled()) {
            log.debug("Attachment Download Servlet registered.");
        }
    }

    /**
     * Bundle deactivation method.
     *
     * @param componentContext : The component context.
     */
    @Deactivate
    protected void deactivate(ComponentContext componentContext) {

        log.info("org.wso2.carbon.attachment.mgt.server.internal.AttachmentServiceComponent.deactivate");
        throw new UnsupportedOperationException("org.wso2.carbon.attachment.mgt.server.internal" +
                ".AttachmentServiceComponent.deactivate");
    }

    /**
     * Invoked when the Attachment-Mgt bundle starts.
     *
     * @param dataSourceService
     */
    @Reference(
            name = "datasource.dataSourceService",
            service = org.wso2.carbon.ndatasource.core.DataSourceService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetDataSourceService")
    protected void setDataSourceService(DataSourceService dataSourceService) {

        if (log.isDebugEnabled()) {
            log.debug("DataSourceInformationRepositoryService bound to the Attachment-Mgt component");
        }
        this.dataSourceServiceProvided = true;
    }

    /**
     * Invoked when the Attachment-Mgt bundle stops.
     *
     * @param dataSourceService
     */
    protected void unsetDataSourceService(DataSourceService dataSourceService) {

        if (log.isDebugEnabled()) {
            log.debug("DataSourceInformationRepositoryService unbound from the Attachment-Mgt component");
        }
        this.dataSourceServiceProvided = false;
    }
}
