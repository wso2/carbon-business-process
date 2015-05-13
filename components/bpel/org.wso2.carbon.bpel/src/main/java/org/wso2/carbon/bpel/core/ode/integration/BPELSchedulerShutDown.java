/**
 *
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.apache.ode.bpel.iapi.Scheduler;
import org.wso2.carbon.bpel.core.internal.BPELServiceComponent;
import org.wso2.carbon.utils.WaitBeforeShutdownObserver;

/**
 * ODE scheduler should be shutdown before the Hazelcast instance.
 * This class halts the carbon server shutdown until the scheduler is shut.
 */
public class BPELSchedulerShutDown implements WaitBeforeShutdownObserver{

    private static Log log = LogFactory.getLog(BPELSchedulerShutDown.class);
    private static boolean status = false;

    //triggered before shutting down server and shutdown the scheduler if exists
    @Override
    public void startingShutdown() {
        Scheduler scheduler = ((BPELServerImpl) BPELServiceComponent.getBPELServer()).getScheduler();

        if (scheduler != null) {
            try {
                if (log.isDebugEnabled()) {
                    log.debug("Shutting down quartz scheduler.");
                }
                scheduler.shutdown();
            } catch (Exception e) {
                log.warn("Scheduler couldn't be shut down.", e);
            } finally {
                scheduler = null;
                status = true;
            }
        } else {
            status = true;
        }
    }

    //keep blocking until the status is true
    @Override
    public boolean isTaskComplete() {
        return status;
    }
}
