/*
 * Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.humantask.coordination.module.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.humantask.core.HumanTaskEngineService;

@Component(
        name = "org.wso2.carbon.humantask.HTCoordinationModuleServiceComponent",
        immediate = true)
public class HTCoordinationModuleServiceComponent {

    private static Log log = LogFactory.getLog(HTCoordinationModuleContentHolder.class);

    @Activate
    protected void activate(ComponentContext ctxt) {

        if (log.isDebugEnabled()) {
            log.debug("HumanTask Coordination Module is activated.");
        }
    }

    @Reference(
            name = "humantask.engine",
            service = org.wso2.carbon.humantask.core.HumanTaskEngineService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetHTServer")
    protected void setHTServer(HumanTaskEngineService humanTaskEngineService) {

        if (log.isDebugEnabled()) {
            log.debug("HTServer bound from the coordination module");
        }
        HTCoordinationModuleContentHolder.getInstance().setHtServer(humanTaskEngineService.getHumanTaskServer());
    }

    protected void unsetHTServer(HumanTaskEngineService humanTaskEngineService) {

        if (log.isDebugEnabled()) {
            log.debug("HTServer unbound from the coordination module");
        }
        HTCoordinationModuleContentHolder.getInstance().setHtServer(null);
    }
}
