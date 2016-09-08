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
package org.wso2.carbon.bpmn.people.substitution.scheduler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bpmn.core.BPMNConstants;
import org.wso2.carbon.bpmn.people.substitution.UserSubstitutionUtils;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class SubstitutionScheduler implements ScheduledTaskRunner {

    private static Log log = LogFactory.getLog(SubstitutionScheduler.class);
    private SchedulerThread _todo;
    private boolean _running;
    ExecutorService _exec;

    /**
     * Jobs scheduled with a time that is between [now, now+immediateInterval] will be assigned to the current node, and placed
     * directly on the todo queue.
     */
    long interval = BPMNConstants.DEFAULT_SUBSTITUTION_INTERVAL_IN_MINUTES * 60 * 1000;

    public SubstitutionScheduler(long interval) {
        _todo = new SchedulerThread(this);
        this.interval = interval;
    }

    @Override
    public void runTask(final ScheduledTask task) {

        Future future =_exec.submit(new Callable<Void>() {
            public Void call() throws Exception {
                try {
                    ((SchedulerScheduledTask) task).run();
                } catch (Exception ex) {
                    log.error("Error during scheduled task execution for substitutes", ex);
                }
                return null;
            }
        });

        if (log.isDebugEnabled()) {
            log.debug("Scheduled task : " + task.toString() +", completed : " + future.isDone());
        }

    }

    public synchronized void start() {
        if (_running)
            return;

        if (_exec == null) {
            _exec = Executors.newCachedThreadPool();
        }

        _todo.enqueue(new LoadImmediateScheduledTask(System.currentTimeMillis()));

        _todo.start();
        _running = true;
    }

    public synchronized void stop() {
        if (!_running)
            return;

        _todo.stop();
        _todo.clearTasks(LoadImmediateScheduledTask.class);
        _running = false;
    }

    private abstract class SchedulerScheduledTask extends ScheduledTask implements Runnable {
        SchedulerScheduledTask(long schedDate) {
            super(schedDate);
        }
    }

    private class LoadImmediateScheduledTask extends SchedulerScheduledTask {
        LoadImmediateScheduledTask(long schedDate) {
            super(schedDate);
        }

        public void run() {
            boolean success = false;
            try {
                log.debug("Executing BPMN User Substitution scheduled event");
                success = UserSubstitutionUtils.handleScheduledEvent();
            } finally {
                if (success) {
                    _todo.enqueue(new LoadImmediateScheduledTask(System.currentTimeMillis() + (long) (interval)));
                } else {
                    log.debug("BPMN User Substitution scheduled event failed. Scheduling next event in 60 seconds");
                    _todo.enqueue(new LoadImmediateScheduledTask(System.currentTimeMillis() + 1000 * 60));
                }

            }
        }

    }

    /**
     * Get the remaining time for the next scheduled event.
     * @return remaining time in milliseconds, very big number of no scheduled event
     */
    public long getNextScheduledTime() {
        return _todo.nextJobTime();
    }
}
