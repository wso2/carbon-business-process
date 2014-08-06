/*
 * Copyright (c), WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.humantask.core.scheduler;

import org.wso2.carbon.humantask.core.dao.HumanTaskJobDAO;
import org.wso2.carbon.humantask.core.internal.HumanTaskServiceComponent;

import java.text.SimpleDateFormat;

/**
 * Like a task, but a little bit better.
 *
 */
class Job extends Task {
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");

    HumanTaskJobDAO jobDAO;

    public Job(long jobId) {
        super(0);
        jobDAO = HumanTaskServiceComponent.getHumanTaskServer().getDaoConnectionFactory().
                getConnection().createHumanTaskJobDao();
        jobDAO.setId(jobId);
    }

    public Job(HumanTaskJobDAO job) {
        this(job.getTime(), job);
    }

    public Job(long when, HumanTaskJobDAO job) {
        super(when);
        jobDAO = job;
    }

    @Override
    public int hashCode() {
        return jobDAO.getId().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Job && jobDAO.getId().equals(((Job) obj).jobDAO.getId());
    }

    @Override
    public String toString() {
        SimpleDateFormat f = (SimpleDateFormat) DATE_FORMAT.clone();
        return "Job "+jobDAO.getId()+" time: "+f.format(schedDate)+" transacted: "+
                jobDAO.isTransacted()+" details: "+jobDAO.getDetails();
    }

    public long getJobID() {
        return jobDAO.getId();
    }
}
