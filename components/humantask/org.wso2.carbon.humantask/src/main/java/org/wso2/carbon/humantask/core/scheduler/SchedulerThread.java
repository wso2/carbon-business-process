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

import org.wso2.carbon.humantask.core.utils.CollectionsX;
import org.wso2.carbon.humantask.core.utils.MemberOfFunction;

import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Implements the "todo" queue and prioritized scheduling mechanism.
 */
class SchedulerThread implements Runnable {

    private static final int TODO_QUEUE_INITIAL_CAPACITY = 200;

    /**
     * Jobs ready for immediate execution.
     */
    private PriorityBlockingQueue<Task> todo;

    /**
     * Lock for managing the queue
     */
    private ReentrantLock lock = new ReentrantLock();

    private Condition activity = lock.newCondition();

    private volatile boolean done;

    private TaskRunner taskrunner;

    private Thread thread;

    SchedulerThread(TaskRunner runner) {
        todo = new PriorityBlockingQueue<Task>(TODO_QUEUE_INITIAL_CAPACITY,
                new JobComparatorByDate());
        taskrunner = runner;
    }

    void start() {
        if (thread != null) {
            return;
        }

        done = false;
        thread = new Thread(this, "HTScheduler");
        thread.start();
    }

    /**
     * Shutdown the thread.
     */
    void stop() {
        if (thread == null) {
            return;
        }

        done = true;
        lock.lock();
        try {
            activity.signal();
        } finally {
            lock.unlock();

        }

        while (thread != null) {
            try {
                thread.join();
                thread = null;
            } catch (InterruptedException ignored) {
            }
        }

    }

    /**
     * Add a job to the todo queue.
     *
     * @param task Task/Job
     */
    void enqueue(Task task) {
        lock.lock();
        try {
            todo.add(task);
            activity.signal();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Remove a job from the todo queue.
     *
     * @param task Task/Job
     */
    void dequeue(Task task) {
        lock.lock();
        try {
            todo.remove(task);
            activity.signal();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Get the size of the todo queue.
     *
     * @return Size of the todo queue
     */
    public int size() {
        return todo.size();
    }

    /**
     * Pop items off the todo queue, and send them to the task runner for processing.
     */
    public void run() {
        while (!done) {
            lock.lock();
            try {
                long nextjob = nextJobTime();
                while ((nextjob) > 0 && !done) {
                    activity.await(nextjob, TimeUnit.MILLISECONDS);
                    nextjob = nextJobTime();
                }

                if (!done && nextjob == 0) {
                    Task task = todo.take();
                    taskrunner.runTask(task);
                }
            } catch (InterruptedException ignore) {
            } finally {
                lock.unlock();
            }
        }
    }

    /**
     * Calculate the time until the next available job.
     *
     * @return time until next job, 0 if one is one is scheduled to go, and some
     *         really large number if there are no jobs to speak of
     */
    private long nextJobTime() {
        assert lock.isLocked();

        Task job = todo.peek();
        if (job == null) {
            return Long.MAX_VALUE;
        }

        return Math.max(0, job.schedDate - System.currentTimeMillis());
    }

    /**
     * Remove the tasks of a given type from the list.
     *
     * @param tasktype type of task
     */
    public void clearTasks(final Class<? extends Task> tasktype) {
        lock.lock();
        try {
            CollectionsX.removeIf(todo, new MemberOfFunction<Task>() {
                @Override
                public boolean isMember(Task o) {
                    return tasktype.isAssignableFrom(o.getClass());
                }

            });
        } finally {
            lock.unlock();
        }
    }
}
