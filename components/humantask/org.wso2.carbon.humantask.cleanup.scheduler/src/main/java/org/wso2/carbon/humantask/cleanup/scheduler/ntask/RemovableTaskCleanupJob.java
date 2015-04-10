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

package org.wso2.carbon.humantask.cleanup.scheduler.ntask;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.humantask.cleanup.scheduler.internal.HumanTaskCleanupSchedulerServiceComponent;
import org.wso2.carbon.humantask.core.configuration.HumanTaskServerConfiguration;
import org.wso2.carbon.humantask.core.dao.HumanTaskDAOConnection;
import org.wso2.carbon.humantask.core.dao.SimpleQueryCriteria;
import org.wso2.carbon.humantask.core.engine.runtime.api.HumanTaskRuntimeException;
import org.wso2.carbon.ntask.core.AbstractTask;

import java.util.concurrent.Callable;

/**
 * The task execution logic for cleaning removable tasks.
 */
public class RemovableTaskCleanupJob extends AbstractTask {

    private static Log log = LogFactory.getLog(RemovableTaskCleanupJob.class);

    /**
     * The task clean up execution logic.
     */
    public void execute() {

        HumanTaskServerConfiguration serverConfiguration =
                HumanTaskCleanupSchedulerServiceComponent.getHumanTaskServer().getServerConfig();

        final SimpleQueryCriteria queryCriteria = createQueryCriteria(serverConfiguration);
        log.info("Running the task cleanup service.....");
        try {
            HumanTaskCleanupSchedulerServiceComponent.getHumanTaskServer().getTaskEngine().getScheduler().
                    execTransaction(new Callable<Object>() {
                        public Object call() throws Exception {
                            HumanTaskDAOConnection daoConnection =
                                    HumanTaskCleanupSchedulerServiceComponent.getHumanTaskServer().getDaoConnectionFactory().getConnection();

                            daoConnection.removeTasks(queryCriteria);
                            return null;
                        }
                    });
        } catch (Exception ex) {
            String errMsg = "Task Cleanup operation failed! :";
            log.error(errMsg, ex);
            throw new HumanTaskRuntimeException(errMsg, ex);
        }
    }

    /**
     * Create task removal query criteria.
     *
     * @param serverConfiguration : The server config.
     * @return : The SimpleQueryCriteria.
     */
    private SimpleQueryCriteria createQueryCriteria(
            HumanTaskServerConfiguration serverConfiguration) {

        SimpleQueryCriteria queryCriteria = new SimpleQueryCriteria();
        queryCriteria.setSimpleQueryType(SimpleQueryCriteria.QueryType.REMOVE_TASKS);
        queryCriteria.setStatuses(serverConfiguration.getRemovableTaskStatuses());
        return queryCriteria;

    }
}
