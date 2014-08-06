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

package org.wso2.carbon.humantask.core.api.scheduler;

import java.io.Serializable;
import java.util.concurrent.Callable;

/**
 *
 * The scheduler API for deadlines.
 *
 */
public interface Scheduler {
    /**
     * Interface implemented by the object responsible for job execution.
     */
    public interface JobProcessor {
         /**
          * Implements execution of the job.
          * @param jobInfo the job information
          * @throws JobProcessorException
          */
         //TODO implementation
        void onScheduledJob(JobInfo jobInfo) throws JobProcessorException;
    }

    /**
     * Wrapper containing information about a scheduled job.
     */
    public static class JobInfo implements Serializable {
        private static final long serialVersionUID = 1L;
        private final long jobId;
        private final long scheduledTime;
        private final long taskId;
        private final String name;
        private final JobType type;

        public long getJobId() {
            return jobId;
        }

        public long getScheduledTime() {
            return scheduledTime;
        }

        public long getTaskId() {
            return taskId;
        }

        public String getName() {
            return name;
        }

        public JobType getType() {
            return type;
        }

        public JobInfo(long jobId, long scheduledTime, long taskId, String name, String type) {
            this.jobId = jobId;
            this.scheduledTime = scheduledTime;
            this.taskId = taskId;
            this.name = name;
            this.type = JobType.valueOf(type);
        }

        public String toString() {
            return Long.toString(jobId) + " : time: " + Long.toString(scheduledTime) + " : taskID: "
                    + Long.toString(taskId) + " : name: " + name + " : Type: " + type;
        }
    }

    public enum JobType {
        TIMER_DEADLINE,
        TIMER_SUSPEND
    }

    /**
     * Exception thrown by the {@link JobProcessor} to indicate failure in job
     * processing.
     */
    public class JobProcessorException extends RuntimeException {
        //TODO

        private static final long serialVersionUID = 1L;

        public JobProcessorException(Throwable cause) {
            super(cause);
        }

        public JobProcessorException(String msg, Throwable cause) {
            super(msg, cause);
        }

        public JobProcessorException(String msg) {
            super(msg);
        }
    }

    void setJobProcessor(JobProcessor processor);

    /**
     * @return true if the current thread is associated with a transaction.
     */
    boolean isTransacted();

    void start();

    void stop();

    void shutdown();

    long scheduleJob(long now, long scheduledTime, JobType type, String details, long taskId,
                     String deadlineName);

    /**
     * When the task reaches a final state (Completed, Failed, Error, Exited, Obsolete) all deadlines should be deleted
     * @param taskId Task ID
     */
    void cancelJobsForTask(long taskId);

    /**
     * Update the schedule time for a job
     * @param taskId Task ID
     * @param time Time to be updated
     * @param name Name of the task
     * @throws InvalidJobsInDbException If there are more than one
     * jobs found for Tadk Id and name
     * @throws InvalidUpdateRequestException If the requested time
     * has already passed.
     */
    void updateJob(Long taskId, String name, Long time)
            throws InvalidJobsInDbException, InvalidUpdateRequestException;

/**
     * Execute a {@link java.util.concurrent.Callable} in a transactional context. If the callable
     * throws an exception, then the transaction will be rolled back, otherwise
     * the transaction will commit.
     *
     * @param <T> return type
     * @param transaction transaction to execute
     * @return result
     * @throws Exception
     */
    <T> T execTransaction(Callable<T> transaction)
            throws Exception;

    /**
     * Execute a {@link Callable} in a transactional context. If the callable
     * throws an exception, then the transaction will be rolled back, otherwise
     * the transaction will commit. Also, modify the value of the timeout value
     * that is associated with the transactions started by the current thread.
     *
     * @param <T> return type
     * @param transaction transaction to execute
     * @param timeout, The value of the timeout in seconds. If the value is zero, the transaction service uses the default value.
     * @return result
     * @throws Exception
     */
    <T> T execTransaction(Callable<T> transaction, int timeout)
            throws Exception;


}
