/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.bpel.core.ode.integration;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bpel.core.internal.BPELServiceComponent;
import org.wso2.carbon.core.ServerStartupObserver;

/**
 * This class starts up the ode scheduler when the startup finalizer calls the invoke method
 */
public class BPELSchedulerInitializer implements ServerStartupObserver {

    private static Log log = LogFactory.getLog(BPELSchedulerInitializer.class);

    @Override
    public void completingServerStartup() {
        if (BPELServiceComponent.getBPELServer().getBpelServerConfiguration().getUseDistributedLock()) {
            if (BPELServiceComponent.getHazelcastInstance() != null) {
                log.info("HazelCast instance available and configured");
            } else {
                log.error("HazelCast instance not available, but distributed lock enabled");
            }
        }
    }

    @Override
    public void completedServerStartup() {
        if (log.isDebugEnabled()) {
            log.debug("Competed server startup");
        }

        /**Need to start the scheduler after all BPEL processes get deployed to resume currently active process instances.
         Hence start scheduler after completing server startup. Users should configure Node ID in the bps.xml*/
        log.info("Starting BPS Scheduler");
        ((BPELServerImpl) BPELServiceComponent.getBPELServer()).getScheduler().start();
    }
}
