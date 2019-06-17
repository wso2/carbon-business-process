package org.wso2.carbon.attachment.mgt.server.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.utils.ConfigurationContextService;

@Component(
        name = "org.wso2.carbon.attachment.mgt.server.internal.AttachmentDependencyResolverServiceComponent",
        immediate = true)
public class AttachmentDependencyResolverServiceComponent {

    private static final Log log = LogFactory.getLog(AttachmentDependencyResolverServiceComponent.class);

    @Activate
    protected void activate(ComponentContext cmpCtx) {

    }

    @Reference(
            name = "org.wso2.carbon.configCtx.service",
            service = org.wso2.carbon.utils.ConfigurationContextService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetConfigurationContextService")
    protected void setConfigurationContextService(ConfigurationContextService configurationContextService) {

        if (log.isDebugEnabled()) {
            log.debug("ConfigurationContext service bound to the Attachment-Mgt component");
        }
        AttachmentServerHolder.getInstance().setConfigurationContextService(configurationContextService);
    }

    protected void unsetConfigurationContextService(ConfigurationContextService configurationContextService) {

        if (log.isDebugEnabled()) {
            log.debug("ConfigurationContext service unbound from the Attachment-Mgt component");
        }
        AttachmentServerHolder.getInstance().setConfigurationContextService(null);
    }

    @Deactivate
    protected void deactivate(ComponentContext cmpCtx) {

    }
}
