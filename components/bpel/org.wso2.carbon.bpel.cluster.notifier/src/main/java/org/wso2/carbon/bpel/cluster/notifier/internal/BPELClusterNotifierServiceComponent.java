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
package org.wso2.carbon.bpel.cluster.notifier.internal;

import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.utils.ConfigurationContextService;

/**
 * @scr.component name="org.wso2.carbon.bpel.cluster.notifier.BPELServiceComponent" immediate="true"
 * @scr.reference name="config.context.service" interface="org.wso2.carbon.utils.ConfigurationContextService"
 * cardinality="1..1" policy="dynamic"  bind="setConfigurationContextService" unbind="unsetConfigurationContextService"
 */

public class BPELClusterNotifierServiceComponent {
    private static Log log = LogFactory.getLog(BPELClusterNotifierServiceComponent.class);

    public static AxisConfiguration getAxisConfiguration() {
        return BPELServerHolder.getInstance().getCcServiceInstance().getServerConfigContext().
                getAxisConfiguration();
    }

    protected void activate(ComponentContext ctxt) {
        if (log.isDebugEnabled()) {
            log.debug("BPEL BPELClusterNotifier bundle is activated.");
        }
    }

    protected void setConfigurationContextService(ConfigurationContextService contextService) {
        if (log.isDebugEnabled()) {
            log.debug("ConfigurationContextService bound to the BPEL component");
        }
        BPELServerHolder.getInstance().setCcServiceInstance(contextService);
    }

    protected void unsetConfigurationContextService(ConfigurationContextService contextService) {
        if (log.isDebugEnabled()) {
            log.debug("ConfigurationContextService unbound from the BPEL component");
        }
        BPELServerHolder.getInstance().setCcServiceInstance(null);
    }
}
