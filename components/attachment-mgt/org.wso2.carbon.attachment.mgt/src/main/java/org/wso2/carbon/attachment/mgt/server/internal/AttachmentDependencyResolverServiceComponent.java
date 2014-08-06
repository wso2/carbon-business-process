package org.wso2.carbon.attachment.mgt.server.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.utils.ConfigurationContextService;

/**
 * @scr.component name="org.wso2.carbon.attachment.mgt.server.internal.AttachmentDependencyResolverServiceComponent"
 * immediate="true"
 * @scr.reference name="org.wso2.carbon.configCtx.service"
 * interface="org.wso2.carbon.utils.ConfigurationContextService" cardinality="1..1"
 * policy="dynamic" bind="setConfigurationContextService" unbind="unsetConfigurationContextService"
 */
public class AttachmentDependencyResolverServiceComponent {

    private static final Log log = LogFactory.getLog(AttachmentDependencyResolverServiceComponent.class);

    protected void activate(ComponentContext cmpCtx) {

    }

    protected void setConfigurationContextService(
            ConfigurationContextService configurationContextService) {
            if(log.isDebugEnabled()) {
                log.debug("ConfigurationContext service bound to the Attachment-Mgt component");
            }
            AttachmentServerHolder.getInstance().setConfigurationContextService(configurationContextService);
    }

    protected void unsetConfigurationContextService(
            ConfigurationContextService configurationContextService) {
            if(log.isDebugEnabled()) {
                log.debug("ConfigurationContext service unbound from the Attachment-Mgt component");
            }
            AttachmentServerHolder.getInstance().setConfigurationContextService(null);
    }

    protected void deactivate(ComponentContext cmpCtx) {

    }
}
