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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.helpers.AbsoluteTimeDateFormat;
import org.wso2.carbon.humantask.core.api.scheduler.InvalidJobsInDbException;
import org.wso2.carbon.humantask.core.api.scheduler.InvalidUpdateRequestException;
import org.wso2.carbon.humantask.core.api.scheduler.Scheduler;
import org.wso2.carbon.humantask.core.dao.HumanTaskDAOConnection;
import org.wso2.carbon.humantask.core.dao.HumanTaskJobDAO;
import org.wso2.carbon.humantask.core.engine.HumanTaskException;
import org.wso2.carbon.humantask.core.internal.HumanTaskServiceComponent;

import javax.persistence.EntityManager;
import javax.transaction.TransactionManager;
import java.lang.reflect.ParameterizedType;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Responsible for scheduling tasks and run/start scheduled tasks
 */

public class SimpleScheduler implements Scheduler, TaskRunner {

    private static Log log = LogFactory.getLog(SimpleScheduler.class);

    //private static final int DEFAULT_TRANSACTION_TIMEOUT = 60 * 1000;

    /**
     * Jobs scheduled with a time that is between [now, now+immediateInterval] will be assigned to the current node, and placed
     * directly on the todo queue.
     */
    private static final long immediateInterval = 30000;

    /**
     * Jobs scheduled with a time that is between (now+immediateInterval,now+nearFutureInterval) will be assigned to the current
     * node, but will not be placed on the todo queue (the promoter will pick them up).
     */
    private final long nearFutureInterval = 60 * 1000;
//    long _nearFutureInterval = 10 * 60 * 1000;

//    /**
//     * 10s of no communication and you are deemed dead.
//     */
//    private long staleInterval = 10000;

    //    long _warningDelay = 5*60*1000;

    private ExecutorService exec;

    private String nodeId;

    /**
     * Maximum number of jobs in the "near future" / todo queue.
     */
    private static final int todoLimit = 10000;

    /**
     * The object that actually handles the jobs.
     */
    private volatile JobProcessor jobProcessor;

//    volatile JobProcessor _polledRunnableProcessor;

    private SchedulerThread todo;

    /**
     * All the nodes we know about
     */
    private CopyOnWriteArraySet<String> knownNodes = new CopyOnWriteArraySet<String>();

    /**
     * When we last heard from our nodes.
     */
    private ConcurrentHashMap<String, Long> lastHeartBeat = new ConcurrentHashMap<String, Long>();

    /**
     * Set of outstanding jobs, i.e., jobs that have been enqueued but not dequeued or dispatched yet.
     * Used to avoid cases where a job would be dispatched twice if the server is under high load and
     * does not fully process a job before it is reloaded from the database.
     */
    private ConcurrentHashMap<Long, Long> outstandingJobs = new ConcurrentHashMap<Long, Long>();
    /**
     * Set of Jobs processed since the last LoadImmediate task.
     * This prevents a race condition where a job is processed twice. This could happen if a LoadImediate tasks loads a job
     * from the db before the job is processed but puts it in the _outstandingJobs map after the job was processed .
     * In such a case the job is no longer in the _outstandingJobs map, and so it's queued again.
     */
    private ConcurrentHashMap<Long, Long> processedSinceLastLoadTask = new ConcurrentHashMap<Long, Long>();

    private boolean running;

    /**
     * Time for next upgrade.
     */
    private AtomicLong nextUpgrade = new AtomicLong();

//    private Random random = new Random();

//    private long pollIntervalForPolledRunnable = Long.getLong("org.apache.ode.polledRunnable.pollInterval", 10 * 60 * 1000);

//    /**
//     * Number of immediate retries when the transaction fails *
//     */
//    private int immediateTransactionRetryLimit = 3;
//
//    /**
//     * Interval between immediate retries when the transaction fails *
//     */
//    private long immediateTransactionRetryInterval = 1000;

    private TransactionManager transactionManager;

    public SimpleScheduler(String nodeId) {
        this.nodeId = nodeId;
        todo = new SchedulerThread(this);
    }

    public void setTransactionManager(TransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    public void runTask(final org.wso2.carbon.humantask.core.scheduler.Task task) {
        if (task instanceof Job) {
            Job job = (Job) task;
            runJob(job);
        } else if (task instanceof SchedulerTask) {
            exec.submit(new Callable<Void>() {
                public Void call() throws Exception {
                    try {
                        ((SchedulerTask) task).run();
                    } catch (Exception ex) {
                        log.error("Error during SchedulerTask execution", ex);
                    }
                    return null;
                }
            });
        }
    }

    /**
     * Run a job in the current thread.
     *
     * @param job job to run.
     */
    protected void runJob(final Job job) {
        exec.submit(new RunJob(job, jobProcessor));
    }

    class RunJob implements Callable<Void> {
        private final Job job;
        private final JobProcessor processor;

        RunJob(Job job, JobProcessor processor) {
            this.job = job;
            this.processor = processor;
        }

        public Void call() throws Exception {
            try {
                final Scheduler.JobInfo jobInfo = new Scheduler.JobInfo(job.getJobID(),
                        job.jobDAO.getTime(), job.jobDAO.getTaskId(), job.jobDAO.getName(),
                        job.jobDAO.getType());

                try {
                    execTransaction(new Callable<Void>() {
                        public Void call() throws Exception {
                            job.jobDAO = getConnection().getEntityManager().find(job.jobDAO.getClass(), job.jobDAO.getId());
                            getConnection().getEntityManager().remove(job.jobDAO);
//                            job.jobDAO.delete();
//                                try {
                            processor.onScheduledJob(jobInfo);
                            // If the job is a "runnable" job, schedule the next job occurence
//                                    if (job.detail.getDetailsExt().get("runnable") != null && !"COMPLETED".equals(String.valueOf(jobInfo.jobDetail.getDetailsExt().get("runnable_status")))) {
//                                        // the runnable is still in progress, schedule checker to 10 mins later
//                                        if (_pollIntervalForPolledRunnable < 0) {
//                                            if (__log.isWarnEnabled())
//                                                __log.warn("The poll interval for polled runnables is negative; setting it to 1000ms");
//                                            _pollIntervalForPolledRunnable = 1000;
//                                        }
//                                        job.schedDate = System.currentTimeMillis() + _pollIntervalForPolledRunnable;
//                                        _db.insertJob(job, _nodeId, false);
//                                    }
//                                } catch (JobProcessorException jpe) {
////                                    if (!jpe.retry) {
////                                        needRetry[0] = false;
////                                    }
//                                    // Let execTransaction know that shit happened.
//                                    throw jpe;
//                                }
                            return null;
                        }
                    });
//                    } catch (JobNoLongerInDbException jde) {
//                        // This may happen if two node try to do the same job... we try to avoid
//                        // it the synchronization is a best-effort but not perfect.
//                        __log.debug("job no longer in db forced rollback: "+job);
                } catch (final Exception ex) {
                    log.error("Error while processing a persisted job" + job, ex);
//                        log.error("Error while processing a "+(job.persisted?"":"non-")+"persisted job"+(needRetry[0] && job.persisted?": ":", no retry: ")+job, ex);

                    // We only get here if the above execTransaction fails, so that transaction got
                    // rollbacked already
//                        if (job.persisted) {
//                            execTransaction(new Callable<Void>() {
//                                public Void call() throws Exception {
//                                    if (needRetry[0]) {
//                                        int retry = job.detail.getRetryCount() + 1;
//                                        if (retry <= 10) {
//                                            job.detail.setRetryCount(retry);
//                                            long delay = (long)(Math.pow(5, retry));
//                                            job.schedDate = System.currentTimeMillis() + delay*1000;
//                                            _db.updateJob(job);
//                                            __log.error("Error while processing job, retrying in " + delay + "s");
//                                        } else {
//                                            _db.deleteJob(job.jobId, _nodeId);
//                                            __log.error("Error while processing job after 10 retries, no more retries:" + job);
//                                        }
//                                    } else {
//                                        _db.deleteJob(job.jobId, _nodeId);
//                                    }
//                                    return null;
//                                }
//                            });
//                        }
                }
                return null;
            } finally {
                processedSinceLastLoadTask.put(job.getJobID(), job.schedDate);
                outstandingJobs.remove(job.getJobID());
            }


//            try {
//                log.info("RunJob - call - ");
//                final Scheduler.JobInfo jobInfo = new Scheduler.JobInfo(job.getJobID(),
//                        job.jobDAO.getTime(), job.jobDAO.getTaskId(), job.jobDAO.getName(),
//                        job.jobDAO.getType());
//                log.info("RunJob - delete - ");
//
//                job.jobDAO.delete();
//                log.info("RunJob - onSchedule - ");
//
//                processor.onScheduledJob(jobInfo);
//                return null;
//            } catch (Exception e) {
//                log.error("Runjob - Exception", e);
//                return null;
//            } finally {
//                // the order of these 2 actions is crucial to avoid a race condition.
//                _processedSinceLastLoadTask.put(job.getJobID(), job.schedDate);
//                outstandingJobs.remove(job.getJobID());
//            }
        }
    }

    /**
     * @return true if the current thread is associated with a transaction.
     */
    public boolean isTransacted() {
        return false;
    }

    private abstract class SchedulerTask extends Task implements Runnable {
        SchedulerTask(long schedDate) {
            super(schedDate);
        }
    }

    private class LoadImmediateTask extends SchedulerTask {
        LoadImmediateTask(long schedDate) {
            super(schedDate);
        }

        public void run() {
            boolean success = false;
            try {
                success = doLoadImmediate();
            } finally {
                if (success) {
                    todo.enqueue(new LoadImmediateTask(System.currentTimeMillis() +
                            (long) (immediateInterval * .90)));
                } else {
                    todo.enqueue(new LoadImmediateTask(System.currentTimeMillis() + 1000));
                }
            }
        }

    }

    boolean doLoadImmediate() {
        if (log.isDebugEnabled()) {
            log.debug("LOAD IMMEDIATE started");
        }

        // don't load anything if we're already half-full;  we've got plenty to do already
        if (outstandingJobs.size() > todoLimit / 2) {
            return true;
        }

        List<Job> jobs = new ArrayList<Job>();
        try {
            // don't load more than we can chew
            int tps = 100;
            final int batch = Math.min((int) (immediateInterval * tps / 1000),
                    todoLimit - outstandingJobs.size());

            // jobs might have been enqueued by #addTodoList meanwhile
            if (batch <= 0) {
                if (log.isDebugEnabled()) {
                    log.debug("Max capacity reached: " + outstandingJobs.size() +
                            " jobs dispacthed i.e. queued or being executed");
                }
                return true;
            }

            if (log.isDebugEnabled()) {
                log.debug("Started loading " + batch + " jobs from db");
            }

            //jobs = _db.dequeueImmediate(_nodeId, System.currentTimeMillis() + _immediateInterval, batch);

            List<HumanTaskJobDAO> htJobs = execTransaction(new Callable<List<HumanTaskJobDAO>>() {
                public List<HumanTaskJobDAO> call() throws Exception {
                    return getConnection().dequeueImmediate(nodeId,
                            System.currentTimeMillis() + immediateInterval, batch);
                }
            });

            for (HumanTaskJobDAO htJob : htJobs) {
                jobs.add(new Job(htJob));
            }

            if (log.isDebugEnabled()) {
                log.debug("loaded " + jobs.size() + " jobs from db");
            }

            long warningDelay = 0;
            long delayedTime = System.currentTimeMillis() - warningDelay;
            int delayedCount = 0;
            boolean runningLate;
            AbsoluteTimeDateFormat f = new AbsoluteTimeDateFormat();
            for (Job j : jobs) {
                // jobs might have been enqueued by #addTodoList meanwhile
                if (outstandingJobs.size() >= todoLimit) {
                    if (log.isDebugEnabled()) {
                        log.debug("Max capacity reached: " + outstandingJobs.size() +
                                " jobs dispacthed i.e. queued or being executed");
                    }
                    break;
                }
                runningLate = j.schedDate <= delayedTime;
                if (runningLate) {
                    //TODO run the job here
                    delayedCount++;
                }
                if (log.isDebugEnabled()) {
                    log.debug("todo.enqueue job from db: " + j.getJobID() + " for " + j.schedDate +
                            "(" + f.format(j.schedDate) + ") " + (runningLate ? " delayed=true" : ""));
                }
                enqueue(j);
            }
            if (delayedCount > 0) {
                log.warn("Dispatching jobs with more than " + (warningDelay / 60000) +
                        " minutes delay. Either the server was down for some time or the job " +
                        "load is greater than available capacity");
            }

            // clear only if the batch succeeded
            processedSinceLastLoadTask.clear();
            return true;
        } catch (Exception ex) {
            log.error("Error loading immediate jobs from database.", ex);
            return false;
        } finally {
            if (log.isDebugEnabled()) {
                log.debug("LOAD IMMEDIATE complete");
            }
        }
    }

    void enqueue(Job job) {
        if (processedSinceLastLoadTask.get(job.getJobID()) == null) {
            if (outstandingJobs.putIfAbsent(job.getJobID(), job.schedDate) == null) {
                if (job.schedDate <= System.currentTimeMillis()) {
                    runTask(job);
                } else {
                    todo.enqueue(job);
                }
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Job " + job.getJobID() + " is being processed (outstanding job)");
                }
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Job " + job.getJobID() + " is being processed (processed since last load)");
            }
        }
    }

    /**
     * Upgrade jobs from far future to immediate future (basically, assign them to a node).
     *
     * @author mszefler
     */
    private class UpgradeJobsTask extends SchedulerTask {
        UpgradeJobsTask(long schedDate) {
            super(schedDate);
        }

        public void run() {
            long ctime = System.currentTimeMillis();
            long ntime = nextUpgrade.get();
            if (log.isDebugEnabled()) {
                log.debug("UPGRADE task for time: " + schedDate + " fired at " + ctime);
            }

            // We could be too early, this can happen if upgrade gets delayed due to another
            // node
            if (nextUpgrade.get() > System.currentTimeMillis()) {
                if (log.isDebugEnabled()) {
                    log.debug("UPGRADE skipped -- wait another " + (ntime - ctime) + "ms");
                }
                todo.enqueue(new UpgradeJobsTask(ntime));
                return;
            }

            boolean success = false;
            try {
                success = doUpgrade();
            } finally {
                long future = System.currentTimeMillis() +
                        (success ? (long) (nearFutureInterval * .50) : 1000);
                nextUpgrade.set(future);
                todo.enqueue(new UpgradeJobsTask(future));
                log.debug("UPGRADE completed, success = " + success + "; next time in " +
                        (future - ctime) + "ms");
            }
        }
    }

    boolean doUpgrade() {
        if (log.isDebugEnabled()) {
            log.debug("UPGRADE started");
        }
        final ArrayList<String> nodes = new ArrayList<String>(this.knownNodes);
        // Don't forget about self.
        nodes.add(nodeId);
        Collections.sort(nodes);

        // We're going to try to upgrade near future jobs using the db only.
        // We assume that the distribution of the trailing digits in the
        // scheduled time are uniformly distributed, and use modular division
        // of the time by the number of nodes to create the node assignment.
        // This can be done in a single update statement.
        final long maxtime = System.currentTimeMillis() + nearFutureInterval;
        try {
            final int numNodes = nodes.size();

            return execTransaction(new Callable<Boolean>() {
                public Boolean call() throws Exception {
                    for (int i = 0; i < numNodes; ++i) {
                        String node = nodes.get(i);

                        getConnection().updateAssignToNode(node, i, numNodes, maxtime);
                        //_db.updateAssignToNode(node, i, numNodes, maxtime);
                    }
                    return true;
                }
            });
        } catch (Exception ex) {
            log.error("Database error upgrading jobs.", ex);
            return false;
        } finally {
            if (log.isDebugEnabled()) {
                log.debug("UPGRADE complete");
            }
        }

    }


//    /**
//     * Check if any of the nodes in our cluster are stale.
//     */
//    private class CheckStaleNodes extends SchedulerTask {
//        CheckStaleNodes(long schedDate) {
//            super(schedDate);
//        }
//
//        public void run() {
//            _todo.enqueue(new CheckStaleNodes(System.currentTimeMillis() + _staleInterval));
//            if (log.isDebugEnabled()) {
//                log.debug("CHECK STALE NODES started");
//            }
//            for (String nodeId : _knownNodes) {
//                Long lastSeen = _lastHeartBeat.get(nodeId);
//                if ((lastSeen == null || (System.currentTimeMillis() - lastSeen) > _staleInterval)
//                    && !_nodeId.equals(nodeId))
//                {
//                    recoverStaleNode(nodeId);
//                }
//            }
//        }
//    }
//
//    /**
//     * Re-assign stale node's jobs to self.
//     * @param nodeId NodeId
//     */
//    void recoverStaleNode(final String nodeId) {
//        if (log.isDebugEnabled()) {
//            log.debug("recovering stale node " + nodeId);
//        }
//        try {
//            int numrows;
//            //numrows = _db.updateReassign(nodeId, _nodeId);
//            numrows = schedulerDAO.updateReassign(nodeId, _nodeId);
//            if (log.isDebugEnabled()) {
//                log.debug("reassigned " + numrows + " jobs to self. ");
//            }
//
//            // We can now forget about this node, if we see it again, it will be
//            // "new to us"
//            _knownNodes.remove(nodeId);
//            _lastHeartBeat.remove(nodeId);
//
//            // Force a load-immediate to catch anything new from the recovered node.
//            doLoadImmediate();
//
//        } catch (Exception ex) {
//            log.error("Database error reassigning node.", ex);
//        } finally {
//            if (log.isDebugEnabled()) {
//                log.debug("node recovery complete");
//            }
//        }
//    }


    public void setJobProcessor(JobProcessor processor) {
        jobProcessor = processor;
    }

    private void addTodoList(final Job job) {
        enqueue(job);
    }

    public void start() {
        if (running) {
            return;
        }

        if (Boolean.parseBoolean(System.
                getProperty("org.wso2.carbon.humantask.scheduler.deleteJobsOnStart", "false"))) {
            if (log.isDebugEnabled()) {
                log.debug("DeleteJobsOnStart");
            }
            try {
//                 _db.deleteAllJobs();
                execTransaction(new Callable<Integer>() {
                    public Integer call() throws Exception {
                        return getConnection().deleteAllJobs();
                    }
                });
            } catch (Exception ex) {
                log.error("", ex);
                throw new RuntimeException("", ex);
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("no DeleteJobsOnStart");
            }
        }

        if (exec == null) {
            exec = Executors.newCachedThreadPool();
        }

        todo.clearTasks(UpgradeJobsTask.class);
        todo.clearTasks(LoadImmediateTask.class);
//        _todo.clearTasks(CheckStaleNodes.class);
        processedSinceLastLoadTask.clear();
        outstandingJobs.clear();

        knownNodes.clear();

        try {
            List<String> nodeList = execTransaction(new Callable<List<String>>() {
                public List<String> call() throws Exception {
                    return getConnection().getNodeIds();
                }
            });
            knownNodes.addAll(nodeList);
        } catch (Exception ex) {
            log.error("Error retrieving node list.", ex);
            throw new RuntimeException("Error retrieving node list.", ex);
        }

        long now = System.currentTimeMillis();

        // Pretend we got a heartbeat...
        for (String s : knownNodes) {
            lastHeartBeat.put(s, now);
        }

        // schedule immediate job loading for now!
        todo.enqueue(new UpgradeJobsTask(now));
        todo.enqueue(new LoadImmediateTask(now + 1000));

        // schedule check for stale nodes, make it random so that the nodes don't overlap.
//        _todo.enqueue(new CheckStaleNodes(now + randomMean(_staleInterval)));

        // do the upgrade sometime (random) in the immediate interval.
//        _todo.enqueue(new UpgradeJobsTask(now + randomMean(_immediateInterval)));

        todo.start();
        running = true;
    }

//    private long randomMean(long mean) {
//        return (long) random.nextDouble() * mean + (mean / 2);
//    }

    public void stop() {
        if (!running) {
            return;
        }

        todo.stop();
        todo.clearTasks(UpgradeJobsTask.class);
        todo.clearTasks(LoadImmediateTask.class);
//        _todo.clearTasks(CheckStaleNodes.class);
        processedSinceLastLoadTask.clear();
        outstandingJobs.clear();

        // disable because this is not the right way to do it
        // will be fixed by ODE-595
        // graceful shutdown; any new submits will throw RejectedExecutionExceptions
//        _exec.shutdown();
        running = false;
    }

    public void shutdown() {
        stop();
        jobProcessor = null;
        todo = null;
    }

//    public void setNodeId(String nodeId) {
//        this.nodeId = nodeId;
//    }

//    public void setStaleInterval(long staleInterval) {
//        this.staleInterval = staleInterval;
//    }

//    public void setImmediateInterval(long immediateInterval) {
//        this.immediateInterval = immediateInterval;
//    }
//
//    public void setNearFutureInterval(long nearFutureInterval) {
//        this.nearFutureInterval = nearFutureInterval;
//    }
//
//    public void setTransactionsPerSecond(int tps) {
//        this.tps = tps;
//    }

//    public void setDatabaseDelegate(DatabaseDelegate dbd) {
//        _db = dbd;
//    }

    public void setExecutorService(ExecutorService executorService) {
        exec = executorService;
    }

    public long scheduleJob(long now, long scheduledTime, JobType type, String details,
                            long taskId, String name) {
        boolean immediate = scheduledTime <= now + immediateInterval;
        boolean nearfuture = !immediate && scheduledTime <= now + nearFutureInterval;
        HumanTaskJobDAO tempJob = HumanTaskServiceComponent.getHumanTaskServer().
                getDaoConnectionFactory().getConnection().createHumanTaskJobDao();
        tempJob.setTime(scheduledTime);
        tempJob.setTransacted(false);
        tempJob.setDetails(details);
        tempJob.setTaskId(taskId);
        tempJob.setName(name);
        tempJob.setType(type.toString());

        if (immediate) {
            // Immediate scheduling means we put it in the DB for safe keeping
            //_db.insertJob(job, _nodeId, true);

            tempJob.setNodeId(nodeId);
            tempJob.setScheduled(true);

            getEntityManager().persist(tempJob);

            // And add it to our todo list .
            if (outstandingJobs.size() < todoLimit) {
                addTodoList(new Job(tempJob));
            }
            if (log.isDebugEnabled()) {
                log.debug("scheduled immediate job: " + tempJob.getId());
            }
        } else if (nearfuture) {
            // Near future, assign the job to ourselves (why? -- this makes it very unlikely that we
            // would get two nodes trying to process the same instance, which causes unsightly rollbacks).
//                _db.insertJob(job, _nodeId, false);
            tempJob.setNodeId(nodeId);
            tempJob.setScheduled(false);

            getEntityManager().persist(tempJob);

            if (log.isDebugEnabled()) {
                log.debug("scheduled near-future job: " + tempJob.getId());
            }
        } else /* far future */ {
            // Not the near future, we don't assign a node-id, we'll assign it later.
            //_db.insertJob(job, null, false);
            tempJob.setNodeId(null);
            tempJob.setScheduled(false);

            getEntityManager().persist(tempJob);
            if (log.isDebugEnabled()) {
                log.debug("scheduled far-future job: " + tempJob.getId());
            }
        }
        return tempJob.getId();
    }

    public void cancelJob(long jobId) {

        todo.dequeue(new Job(jobId));
        outstandingJobs.remove(jobId);
    }

    public void cancelJobsForTask(long taskId) {
        if (log.isDebugEnabled()) {
            log.debug("Cancelling jobs for task: " + taskId);
        }
        List<Long> jobIds = getConnection().deleteJobsForTask(taskId);
        for (Long jobId : jobIds) {
            cancelJob(jobId);
        }
    }

    /**
     * Update the schedule time for a job
     *
     * @param taskId        Task ID
     * @param scheduledTime Time to be updated
     * @param name          Name of the task
     */
    public void updateJob(Long taskId, String name, Long scheduledTime)
            throws InvalidJobsInDbException, InvalidUpdateRequestException {
        long now = System.currentTimeMillis();
        if (now > scheduledTime) {
            String errMessage = "Current time: " + now + " > request time: " + scheduledTime;
            throw new InvalidUpdateRequestException(errMessage);
        }

        boolean immediate = scheduledTime <= now + immediateInterval;
        boolean nearfuture = !immediate && scheduledTime <= now + nearFutureInterval;
        Long jobId = getConnection().updateJob(taskId, name, immediate, nearfuture, nodeId,
                scheduledTime);
        if (jobId > -1) { //one job is found
            todo.dequeue(new Job(jobId));
            //We ignore if the job is not in the Map outstandingJobs
            outstandingJobs.remove(jobId);

            //Loading/Refresh the job here, in-order to update the job for the latest changes.
            //Otherwise when the next immediate load task runs, it still fetch the job with the
            // old updates.
            ParameterizedType genericSuperClass =
                    (ParameterizedType) getConnection().getClass().getGenericSuperclass();
            Class entityClass = (Class) genericSuperClass.getActualTypeArguments()[0];
            HumanTaskJobDAO updatedJob = (HumanTaskJobDAO) getEntityManager().find(entityClass, jobId);
            getEntityManager().refresh(updatedJob);

            if (immediate) {
                // Immediate scheduling means we add the job immediately to the todo list and
                // we put it in the DB for safe keeping
                addTodoList(new Job(updatedJob));
            } else if (nearfuture) {
                //Re-schedule load-immediate job task
                todo.clearTasks(LoadImmediateTask.class);
                todo.dequeue(new Job(jobId));
                //We ignore if the job is not in the Map outstandingJobs
                outstandingJobs.remove(jobId);
                todo.enqueue(new LoadImmediateTask(System.currentTimeMillis() + 1000));
            } else {
                todo.clearTasks(UpgradeJobsTask.class);
                todo.enqueue(new UpgradeJobsTask(System.currentTimeMillis() + 1000));
            }
        }
    }

    private EntityManager getEntityManager() {
        return getConnection().getEntityManager();
    }

    private HumanTaskDAOConnection getConnection() {
        return HumanTaskServiceComponent.getHumanTaskServer().getDaoConnectionFactory().
                getConnection();
    }

    public <T> T execTransaction(Callable<T> transaction) throws Exception {
        return execTransaction(transaction, 0);
    }

    public <T> T execTransaction(Callable<T> transaction, int timeout) throws Exception {
        TransactionManager txm = transactionManager;
        if (txm == null) {
            throw new HumanTaskException("Cannot locate the transaction manager; " +
                    "the server might be shutting down.");
        }

        // The value of the timeout is in seconds. If the value is zero, 
        // the transaction service restores the default value.
        if (timeout < 0) {
            throw new IllegalArgumentException("Timeout must be positive, received: " + timeout);
        }

        boolean existingTransaction;
        try {
            existingTransaction = txm.getTransaction() != null;
        } catch (Exception ex) {
            String errMsg = "Internal Error, could not get current transaction.";
            throw new HumanTaskException(errMsg, ex);
        }

        // already in transaction, execute and return directly
        if (existingTransaction) {
            return transaction.call();
        }

        // run in new transaction
        Exception ex = null;
//        int immediateRetryCount = _immediateTransactionRetryLimit;

        transactionManager.setTransactionTimeout(timeout);
        if (log.isDebugEnabled() && timeout != 0) {
            log.debug("Custom transaction timeout: " + timeout);
        }

        try {
            try {
                if (log.isDebugEnabled()) {
                    log.debug("Beginning a new transaction");
                }
                txm.begin();
            } catch (Exception e) {
                String errMsg = "Internal Error, could not begin transaction.";
                throw new HumanTaskException(errMsg, e);
            }

            try {
                ex = null;
                return transaction.call();
            } catch (Exception e) {
                ex = e;
            } finally {
                if (ex == null) {
                    if (log.isDebugEnabled()) {
                        log.debug("Committing on " + txm + "...");
                    }
                    try {
                        txm.commit();
                    } catch (Exception e2) {
                        ex = e2;
                    }
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug("Rollbacking on " + txm + "...");
                    }
                    txm.rollback();
                }

//                    if (ex != null && immediateRetryCount > 0) {
//                        if (log.isDebugEnabled()) {
//                            log.debug("Will retry the transaction in " +
//                                    _immediateTransactionRetryInterval + " msecs on " +
//                                    transactionManager + " for error: ", ex);
//                        }
//                        Thread.sleep(_immediateTransactionRetryInterval);
//                    }
            }
        } finally {
            // 0 restores the default value
            transactionManager.setTransactionTimeout(0);
        }

        throw ex;
    }
}
