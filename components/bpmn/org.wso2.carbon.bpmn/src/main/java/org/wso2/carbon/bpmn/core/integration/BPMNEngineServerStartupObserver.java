/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License, 
 * Version 2.0 (the "License"); you may not use this file except 
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.bpmn.core.integration;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bpmn.core.BPMNConstants;
import org.wso2.carbon.bpmn.core.BPMNServerHolder;
import org.wso2.carbon.bpmn.core.utils.BPMNActivitiConfiguration;
import org.wso2.carbon.bpmn.people.substitution.scheduler.SubstitutionScheduler;
import org.wso2.carbon.core.ServerStartupObserver;
import org.wso2.carbon.utils.CarbonUtils;

public class BPMNEngineServerStartupObserver implements ServerStartupObserver {

    private static final Log log = LogFactory.getLog(BPMNEngineServerStartupObserver.class);
    public static final String TRUE = "true";

    @Override
    public void completingServerStartup() {

    }

    @Override
    public void completedServerStartup() {
        if (!CarbonUtils.isChildNode() && TRUE.equalsIgnoreCase(BPMNActivitiConfiguration.getInstance()
                .getBPMNPropertyValue(BPMNConstants.SUBSTITUTION_CONFIG,
                        BPMNConstants.SUBSTITUTION_ENABLED))) {
            String activationIntervalString = BPMNActivitiConfiguration.getInstance()
                    .getBPMNPropertyValue(BPMNConstants.SUBSTITUTION_CONFIG,
                            BPMNConstants.SUBSTITUTION_SCHEDULER_INTERVAL);
            long interval = BPMNConstants.DEFAULT_SUBSTITUTION_INTERVAL_IN_MINUTES * 60 * 1000;
            if (activationIntervalString != null) {
                interval = Long.parseLong(activationIntervalString) * 60 * 1000;
            }
            BPMNServerHolder.getInstance().setSubstitutionScheduler(new SubstitutionScheduler(interval));
            BPMNServerHolder.getInstance().getSubstitutionScheduler().start();
            log.info("BPMN Substitution scheduler started.");
        }
    }


}
