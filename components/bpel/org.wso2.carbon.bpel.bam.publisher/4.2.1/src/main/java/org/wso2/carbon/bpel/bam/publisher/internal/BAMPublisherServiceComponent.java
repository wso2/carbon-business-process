/*
 * Copyright (c) , WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.bpel.bam.publisher.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.bpel.bam.publisher.Axis2ConfigurationContextObserverImpl;
import org.wso2.carbon.bpel.core.BPELEngineService;
import org.wso2.carbon.bpel.core.ode.integration.BPELServer;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.utils.Axis2ConfigurationContextObserver;

/**
 * @scr.component name="org.wso2.carbon.bpel.BamPublisherServiceComponent" immediate="true"
 * @scr.reference name="bpel.engine"
 * interface="org.wso2.carbon.bpel.core.BPELEngineService"
 * cardinality="1..1" policy="dynamic" bind="setBPELServer" unbind="unsetBPELServer"
 * @scr.reference name="registry.service" interface="org.wso2.carbon.registry.core.service.RegistryService"
 * cardinality="1..1" policy="dynamic"  bind="setRegistryService" unbind="unsetRegistryService"
 */
public class BAMPublisherServiceComponent {

    private static Log log = LogFactory.getLog(BAMPublisherServiceComponent.class);

    private BundleContext bundleContext;

    protected void activate(ComponentContext ctx) {
        this.bundleContext = ctx.getBundleContext();
        if(log.isDebugEnabled()) {
            log.debug("BPEL BAM publisher bundle is activated.");
        }
        registerAxis2ConfigurationContextObserver();
    }


    protected void setBPELServer(BPELEngineService bpelEngineService) {
        if (log.isDebugEnabled()) {
            log.debug("BPELEngineService bound to BEPL BAM Publisher component");
        }
        BPELBamPublisherContentHolder.getInstance().setBpelServer(bpelEngineService.getBPELServer());
    }

    protected void unsetBPELServer(
            BPELEngineService bpelEngineService) {
        if (log.isDebugEnabled()) {
            log.debug("BPELEngineService unbound from the BPEL Bam Publisher component");
        }
        BPELBamPublisherContentHolder.getInstance().setBpelServer(null);
    }

    protected  void setRegistryService(RegistryService registryService){
        if(log.isDebugEnabled()){
            log.debug("Registry service bound to BPEL BAM publisher component ");
        }
        BPELBamPublisherContentHolder.getInstance().setRegistryService(registryService);
    }

    private void registerAxis2ConfigurationContextObserver() {
        this.bundleContext.registerService(Axis2ConfigurationContextObserver.class.getName(),
                new Axis2ConfigurationContextObserverImpl(),
                null);
    }

    protected void unsetRegistryService(RegistryService registrySvc) {
        if (log.isDebugEnabled()) {
            log.debug("RegistryService unbound from the BPEL component");
        }
        BPELBamPublisherContentHolder.getInstance().setRegistryService(null);
    }

    public static BPELServer getBPELServer() {
        return BPELBamPublisherContentHolder.getInstance().getBpelServer();
    }

    public static RegistryService getRegistryService(){
        return BPELBamPublisherContentHolder.getInstance().getRegistryService();
    }

    protected void deactivate(ComponentContext componentContext) throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("Stopping the BPEL BAM publisher component");
        }
    }
}
