/*
*  Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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


package org.wso2.carbon.humantask.core;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.core.ServerStartupHandler;
import org.wso2.carbon.humantask.core.internal.HumanTaskServiceComponent;

/**
 * Starts the human task scheduler when the task deployment is completed.
 */
public class HumanTaskSchedulerInitializer implements ServerStartupHandler {
    private static final Log log = LogFactory.getLog(HumanTaskSchedulerInitializer.class);

    public void invoke() {
        if(log.isInfoEnabled()) {
            log.info("Starting HumanTasks Scheduler");
        }
        HumanTaskServiceComponent.getHumanTaskServer().getTaskEngine().getScheduler().start();
    }
}
