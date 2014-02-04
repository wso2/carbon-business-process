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

package org.wso2.carbon.humantask.core.dao;

import org.wso2.carbon.humantask.core.api.scheduler.InvalidJobsInDbException;
import org.wso2.carbon.humantask.core.deployment.HumanTaskDeploymentUnit;
import org.wso2.carbon.humantask.core.engine.HumanTaskException;
import org.wso2.carbon.humantask.core.store.HumanTaskBaseConfiguration;

import javax.persistence.EntityManager;
import java.util.List;

/**
 * This interface will be used through out the human task engine implementation to make the logic
 * neutral of the underlying database ORM model.
 */
public interface HumanTaskDAOConnection {


    /**
     * Creates the task object and persists in the database according the the context data provided.
     *
     * @param creationContext : The context data required to create the task.
     * @return : The created task.
     * @throws org.wso2.carbon.humantask.core.engine.HumanTaskException :
     */
    TaskDAO createTask(TaskCreationContext creationContext) throws HumanTaskException;

    /**
     * Retrieves the given task with it's children ( comments, events, etc).
     * Note: The returned task object contains all the lazy relationships.
     * Hence call this only when all the relations need to be loaded.
     *
     * @param taskId : The id of the task to be retrieved.
     * @return : The retrieved task.
     */
    TaskDAO getTask(Long taskId);

    /**
     * Retrieves the list of tasks for the given user name.
     *
     * @param userName : The user name for whom the tasks should be retrieved.
     * @return : The list of matching tasks for the user.
     */
    List<TaskDAO> getTasksForUser(String userName);

    /**
     * Creates the MessageDAO from the given WSDLAwareMessage.
     *
     * @param taskCreationContext : The input message context.
     * @return : The constructed MessageDAO.
     */
    MessageDAO createMessage(TaskCreationContext taskCreationContext);

    /**
     * Creates an empty MessageDAO object of the specific impl.
     *
     * @return : The constructed MessageDAO.
     */
    MessageDAO createMessage();

    /**
     * Creates a new OrganizationalEntityDAO for the given username and the type.
     * NOTE : This does not do any persistent operation. Only creates the dao implementation object.
     * Persisting and adding generic human roles are the responsibility of the caller.
     *
     * @param userName : The user name of the org entity.
     * @param type     : The type of the org entity.
     * @return : The created org entity.
     */
    OrganizationalEntityDAO createNewOrgEntityObject(String userName,
                                                            OrganizationalEntityDAO.OrganizationalEntityType type);

    /**
     * Creates a new GenericHumanRoleDAO object.
     *
     * @param type : The role type.
     * @return : The created role object.
     */
    GenericHumanRoleDAO createNewGHRObject(GenericHumanRoleDAO.GenericHumanRoleType type);

    /**
     * Create a new CommentDAO object.
     * @param commentString : The comment string.
     * @param commentedByUserName : The commented user name.
     * @return : The created comment object.
     */
    CommentDAO getCommentDAO(String commentString, String commentedByUserName);

    /**
     * Make the tast status OBSOLETE for the tasks with the give name and the tenant Id.
     *
     * @param taskName : The task name for whose instances are to be made obsolete.
     * @param tenantId : The tenant id of the task.
     */
    void obsoleteTasks(String taskName, Integer tenantId);


    /**
     * Remove the tasks matching the provided criteria.
     *
     * @param queryCriteria : The query criteria for which the matching tasks should be deleted.
     */
    void removeTasks(SimpleQueryCriteria queryCriteria);

    /**
     * Create a Deadline
     * @return DeadLine
     */
    DeadlineDAO createDeadline();

    /**
     * Performs a simple task query.
     * @param simpleQueryCriteria : The query criteria.
     * @return : The matching task list.
     */
    List<TaskDAO> simpleTaskQuery(SimpleQueryCriteria simpleQueryCriteria);

    /**
     * Search the matching tasks for a given set of criteria.
     *
     * @param queryCriteria : The Query criteria.
     * @return : The matching tasks.
     */
    List<TaskDAO> searchTasks(SimpleQueryCriteria queryCriteria);
    /**
     * Get the count of the matching tasks for a given set of criteria.
     *
     * @param queryCriteria : The Query criteria.
     * @return : The count of matching tasks.
     */
    int getTasksCount(SimpleQueryCriteria queryCriteria);
    /**
     * Create a new job
     * @return HumanTaskJobDAO
     */
    HumanTaskJobDAO createHumanTaskJobDao();


/**
     * Return a list of unique nodes identifiers found in the database. This is used
     * to initialize the list of known nodes when a new node starts up.
     * @return list of unique node identfiers found in the databaseuniqu
     */
    List<String> getNodeIds();

    /**
     * "Dequeue" jobs from the database that are ready for immediate execution; this basically
     * is a select/delete operation with constraints on the nodeId and scheduled time.
     *
     * @param nodeId node identifier of the jobs
     * @param maxtime only jobs with scheduled time earlier than this will be dequeued
     * @param maxjobs maximum number of jobs to deqeue
     * @return list of jobs that met the criteria and were deleted from the database
     */
    List<HumanTaskJobDAO> dequeueImmediate(String nodeId, long maxtime, int maxjobs);

    /**
     * Assign a particular node identifier to a fraction of jobs in the database that do not have one,
     * and are up for execution within a certain time. Only a fraction of the jobs found are assigned
     * the node identifier. This fraction is determined by the "y" parameter, while membership in the
     * group (of jobs that get the nodeId) is determined by the "x" parameter. Essentially the logic is:
     * <code>
     *  UPDATE jobs AS job
     *      WHERE job.scheduledTime before :maxtime
     *            AND job.nodeId is null
     *            AND job.scheduledTime MOD :y == :x
     *      SET job.nodeId = :nodeId
     * </code>
     *
     * @param nodeId node identifier to assign to jobs
     * @param x the result of the mod-division
     * @param y the dividend of the mod-division
     * @param maxtime only jobs with scheduled time earlier than this will be updated
     * @return number of jobs updated
     */
    int updateAssignToNode(String nodeId, int x, int y, long maxtime);

    /**
     * Reassign jobs from one node to another.
     *
     * @param oldnode node assigning from
     * @param newnode new node asssigning to
     * @return number of rows changed
     */
    int updateReassign(String oldnode, String newnode);

   // public void acquireTransactionLocks();

    int deleteAllJobs();

    boolean delete(String jobId, String nodeId);

    /**
     * Delete jobs for the specified task and returns the list of jobIds
     * @param taskId Task Id
     * @return List of job ids corresponding to the deleted jobs
     */
    List<Long> deleteJobsForTask(Long taskId);

    /**
     * Update the schedule time for a job
     * @param taskId Task ID
     * @param immediate Whether the job should be executed immediately
     * @param nearFuture Whether the job should be executed in the near future
     * @param nodeId Node ID
     * @param time Time to be updated
     * @param name Name of the task
     * @return If there is a job, corresponding to taskId and name, returns the job id. If the are
     *         no jobs then returns -1.
     * @throws org.wso2.carbon.humantask.core.api.scheduler.InvalidJobsInDbException If there are two or more
     *          jobs are selected for the Task Id
     */
    Long updateJob(Long taskId, String name, boolean immediate, boolean nearFuture,
                          String nodeId, Long time)
            throws InvalidJobsInDbException;

    /**
     * Return the Entity Manager
     * @return EntityManager
     */
    EntityManager getEntityManager();


    /**
     * Creates a new EventDAO object from the underlying dao implementation.
     * @param task TaskDAO
     * @return EventDAO
     */
    EventDAO createNewEventObject(TaskDAO task);

    /**
     * Create an Attachment object from the underlying dao implementation.
     *
     * @return AttachmentDAO
     */
    AttachmentDAO createAttachment();

    /**
     * Create a DeploymentUnitDao
     */
    DeploymentUnitDAO createDeploymentUnit();

    /**
     *
     * @param tenantId
     * @param md5sum
     * @return
     */
    DeploymentUnitDAO getDeploymentUnit(int tenantId,String md5sum);

    /**
     *
     * @param deploymentUnit
     * @param taskConfigurations
     * @param tenantId
     * @return
     */

    DeploymentUnitDAO createDeploymentUnitDAO(HumanTaskDeploymentUnit deploymentUnit,
                                              int tenantId);

    /**
     *  Versions of the human tasks follow share the same sequence id. This sequence is shared between
     *  all the human tasks and all the tenants.
     * Returns the next  human task version id
     * @return  next number in the sequence
     */

    long getNextVersion();

    /**
     * Set the task version
     */

    long setNextVersion(long version);

    /**
     *
     * @param tenantId
     * @param packageName
     * @return
     */
    public List<DeploymentUnitDAO> getDeploymentUnitsForPackageName(int tenantId, String packageName);

    /**
     *
     * @param packageName
     * @param tenantId
     * @return
     */
    public List<TaskDAO> getMatchingTaskInstances(String packageName, int tenantId);

    /**
     *
     * @param packageName
     * @param tenantId
     */
    public void deleteDeploymentUnits(String packageName, int tenantId);


}
