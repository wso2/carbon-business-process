/**
 * Copyright (c) 2016 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.bpmn.rest.common.utils;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.bpmn.core.BPMNEngineService;

/**
 * Rest component lookup for bpmnEngineService
 */
@Component(
        name = "org.wso2.carbon.bpmn.rest.common.utils.RestServiceLookupComponent",
        service = RestServiceLookUpComponent.class,
        immediate = true)

public class RestServiceLookUpComponent {
    private static final Logger log = LoggerFactory.getLogger(RestServiceLookUpComponent.class);
    private static RestServiceLookUpComponent instance = new RestServiceLookUpComponent();
    private BPMNEngineService engineService;

    public RestServiceLookUpComponent() {
        engineService = null;
    }

    public static RestServiceLookUpComponent getInstance() {
        return instance;
    }

    @Reference(
            name = "org.wso2.carbon.bpmn.core.BPMNEngineService",
            service = BPMNEngineService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unRegisterBPMNEngineService")
    public void setBpmnEngineService(BPMNEngineService engineService) {
        log.info("Setting BPMN engine " + engineService);
        this.engineService = engineService;
    }

    public BPMNEngineService getBpmnEngineService() {
        return this.engineService;
    }

    protected void unRegisterBPMNEngineService(BPMNEngineService engineService) {
        log.info("Unregister BPMNEngineService..");
    }

    @Activate
    protected void activate(BundleContext bundleContext) {
        log.info("Activated RestServiceLookUpComponent");
    }

    @Deactivate
    protected void deactivate(BundleContext bundleContext) {
        // Nothing to do
    }

}
