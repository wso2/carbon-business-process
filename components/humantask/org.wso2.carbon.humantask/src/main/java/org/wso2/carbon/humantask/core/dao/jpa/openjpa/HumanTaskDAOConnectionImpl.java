package org.wso2.carbon.humantask.core.dao.jpa.openjpa;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.humantask.core.HumanTaskConstants;
import org.wso2.carbon.humantask.core.api.scheduler.InvalidJobsInDbException;
import org.wso2.carbon.humantask.core.dao.*;
import org.wso2.carbon.humantask.core.dao.jpa.openjpa.model.*;
import org.wso2.carbon.humantask.core.dao.jpa.openjpa.util.HumanTaskBuilderImpl;
import org.wso2.carbon.humantask.core.dao.sql.HumanTaskJPQLQueryBuilder;
import org.wso2.carbon.humantask.core.deployment.HumanTaskDeploymentUnit;
import org.wso2.carbon.humantask.core.engine.HumanTaskException;
import org.wso2.carbon.humantask.core.engine.runtime.api.HumanTaskIllegalArgumentException;
import org.wso2.carbon.humantask.core.engine.runtime.api.HumanTaskRuntimeException;
import org.wso2.carbon.humantask.core.engine.util.CommonTaskUtil;
import org.wso2.carbon.humantask.core.internal.HumanTaskServiceComponent;
import org.wso2.carbon.humantask.core.store.HumanTaskBaseConfiguration;
import org.wso2.carbon.humantask.core.store.TaskConfiguration;
import org.wso2.carbon.utils.CarbonUtils;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.io.File;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * The Open JPA based implementation of HumanTaskDAOConnection interface.
 */
public class HumanTaskDAOConnectionImpl implements HumanTaskDAOConnection {

    private static final Log log = LogFactory.getLog(HumanTaskDAOConnectionImpl.class);

    /** The entity manager handling object persistence  */
    private EntityManager entityManager;

    /**
     * @param entityManager : The entity manager handling object persistence.
     */
    public HumanTaskDAOConnectionImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }


    
    public TaskDAO createTask(TaskCreationContext creationContext)
            throws HumanTaskException {
        HumanTaskBuilderImpl taskBuilder = new HumanTaskBuilderImpl();

        if(log.isDebugEnabled()) {
            log.debug("Creating task instance for task " + creationContext.getTaskConfiguration().getName());
        }

        MessageDAO inputMessage = createMessage(creationContext);
        inputMessage.setMessageType(MessageDAO.MessageType.INPUT);
        taskBuilder.addTaskCreationContext(creationContext)
                .addInputMessage(inputMessage);
        TaskDAO task = taskBuilder.build();

        entityManager.persist(task);
        try {
            creationContext.injectExpressionEvaluationContext(task);
            JPATaskUtil.processGenericHumanRoles(task, creationContext.getTaskConfiguration(),
                    creationContext.getPeopleQueryEvaluator(), creationContext.getEvalContext());
            JPATaskUtil.processPresentationElements(task, creationContext.getTaskConfiguration(),
                    creationContext);
            if (task.getType().equals(TaskType.TASK)) {
                CommonTaskUtil.nominate(task, creationContext.getPeopleQueryEvaluator());
            } else if(task.getType().equals(TaskType.NOTIFICATION)){
                task.setStatus(TaskStatus.READY);
            }
            task.setPriority(CommonTaskUtil.calculateTaskPriority(creationContext.getTaskConfiguration(),
                    creationContext.getEvalContext()));

            CommonTaskUtil.setTaskToMessage(task);

            if (TaskType.TASK.equals(task.getType())) {
                CommonTaskUtil.processDeadlines(task,
                        (TaskConfiguration) creationContext.getTaskConfiguration(),
                        creationContext.getEvalContext());
                CommonTaskUtil.scheduleDeadlines(task);
            }

            //Setting HumanTask context override attributes
            CommonTaskUtil.setTaskOverrideContextAttributes(task, creationContext.getMessageHeaderParts());

            HumanTaskServiceComponent.getHumanTaskServer().getTaskEngine().getEventProcessor().processEvent(CommonTaskUtil.createNewTaskEvent(task));
        } catch (HumanTaskRuntimeException ex) {
            String errorMsg = "Error occurred after task creation for Task ID: " + task.getId() + ". Cause: " + ex.getMessage();
            log.error(errorMsg);
            task.setStatus(TaskStatus.ERROR);
            throw ex;
        }

        return task;
    }

    
    public TaskDAO getTask(Long taskId) {
        TaskDAO matchingTask = entityManager.find(Task.class, taskId);
        if(matchingTask != null) {
            //lazy loading items.
            matchingTask.getHumanRoles();
            matchingTask.getParentTask();
            matchingTask.getInputMessage();
            matchingTask.getOutputMessage();
            matchingTask.getFailureMessage();
            matchingTask.getComments();
            matchingTask.getAttachments();
            matchingTask.getPresentationDescriptions();
            matchingTask.getPresentationNames();
            matchingTask.getPresentationSubjects();
            matchingTask.getPresentationParameters();
            matchingTask.getEvents();
        } else {
            String errorMsg = "Task lookup failed for task id " + taskId + " invalid task id provide" ;
            log.error(errorMsg);
            throw new HumanTaskIllegalArgumentException(errorMsg);
        }
        return matchingTask;
    }

    
    public List<TaskDAO> getTasksForUser(String userName) {
        return null;
    }

    
    public MessageDAO createMessage(TaskCreationContext taskCreationContext) {
        Message message = new Message();
        MessageHelper msgHelper = new MessageHelper(message);
        return msgHelper.createMessage(taskCreationContext);
    }


    
    public MessageDAO createMessage() {
        return new Message();
    }

    
    public OrganizationalEntityDAO createNewOrgEntityObject(String userName,
                                                            OrganizationalEntityDAO.OrganizationalEntityType type) {
        OrganizationalEntityDAO orgEntity = new OrganizationalEntity();
        orgEntity.setName(userName);
        orgEntity.setOrgEntityType(type);
        return orgEntity;
    }

    
    public GenericHumanRoleDAO createNewGHRObject(GenericHumanRoleDAO.GenericHumanRoleType type) {
        GenericHumanRoleDAO ghr = new GenericHumanRole();
        ghr.setType(type);
        return ghr;
    }

    
    public void obsoleteTasks(String taskName, Integer tenantId) {
        entityManager.createQuery("UPDATE org.wso2.carbon.humantask.core.dao.jpa.openjpa.model.Task t SET t.status = :statusToSet WHERE t.name = :taskName AND t.tenantId = :tenantId").
                //entityManager.createNamedQuery(Task.OBSOLETE_TASKS).
                        setParameter("taskName", taskName).
                setParameter("tenantId", tenantId).
                setParameter("statusToSet", TaskStatus.OBSOLETE).executeUpdate();
    }

    
    public List<TaskDAO> simpleTaskQuery(SimpleQueryCriteria simpleQueryCriteria) {
        Query taskQuery = entityManager.createQuery("SELECT t FROM org.wso2.carbon.humantask.core.dao.jpa.openjpa.model.Task t WHERE t.status <> :obsoleteStatus ").
                setParameter("obsoleteStatus", TaskStatus.OBSOLETE);
        return taskQuery.getResultList();
    }

    
    public List<TaskDAO> searchTasks(SimpleQueryCriteria queryCriteria) {
        HumanTaskJPQLQueryBuilder queryBuilder = new HumanTaskJPQLQueryBuilder(queryCriteria, entityManager);
        Query taskQuery =queryBuilder.build();
        return taskQuery.getResultList();
    }
    
    public int getTasksCount(SimpleQueryCriteria queryCriteria) {
        HumanTaskJPQLQueryBuilder queryBuilder = new HumanTaskJPQLQueryBuilder(queryCriteria, entityManager);
        Query countQuery = queryBuilder.buildCount();
        int firstResult = Integer.parseInt(countQuery.getSingleResult().toString());
        return firstResult;
    }

    public void removeTasks(SimpleQueryCriteria queryCriteria) {
        HumanTaskJPQLQueryBuilder queryBuilder = new HumanTaskJPQLQueryBuilder(queryCriteria, entityManager);
        Query taskQuery = queryBuilder.build();
        taskQuery.executeUpdate();
    }

    
    public CommentDAO getCommentDAO(String commentString, String commentedByUserName) {
        return new Comment(commentString, commentedByUserName);
    }

    /**
     * Create a Deadline
     *
     * @return DeadLine
     */
    public DeadlineDAO createDeadline() {
        return new Deadline();
    }

    /**
     * Create a new job
     *
     * @return HumanTaskJobDAO
     */
    public HumanTaskJobDAO createHumanTaskJobDao() {
        //        entityManager.merge(humanTaskJob);
        return new HumanTaskJob();
    }

    /**
     * Return a list of unique nodes identifiers found in the database. This is used
     * to initialize the list of known nodes when a new node starts up.
     *
     * @return list of unique node identfiers found in the databaseuniqu
     */
    public List<String> getNodeIds() {
        Query q = entityManager.createQuery("SELECT DISTINCT t.nodeId FROM org.wso2.carbon.humantask.core.dao.jpa.openjpa.model.HumanTaskJob t WHERE t.nodeId IS NOT NULL");
        return (List<String>) q.getResultList();
    }

    /**
     * "Dequeue" jobs from the database that are ready for immediate execution; this basically
     * is a select/delete operation with constraints on the nodeId and scheduled time.
     *
     * @param nodeId  node identifier of the jobs
     * @param maxtime only jobs with scheduled time earlier than this will be dequeued
     * @param maxjobs maximum number of jobs to deqeue
     * @return list of jobs that met the criteria and were deleted from the database
     */
    public List<HumanTaskJobDAO> dequeueImmediate(String nodeId, long maxtime, int maxjobs) {
        Query q = entityManager.createQuery("SELECT DISTINCT t FROM org.wso2.carbon.humantask.core.dao.jpa.openjpa.model.HumanTaskJob t WHERE t.nodeId = ?1 AND t.time < ?2 order by t.time");
        q.setParameter(1, nodeId);
        q.setParameter(2, maxtime);
        q.setMaxResults(maxjobs);
        return (List<HumanTaskJobDAO>) q.getResultList();
    }

    /**
     * Assign a particular node identifier to a fraction of jobs in the database that do not have one,
     * and are up for execution within a certain time. Only a fraction of the jobs found are assigned
     * the node identifier. This fraction is determined by the "y" parameter, while membership in the
     * group (of jobs that get the nodeId) is determined by the "x" parameter. Essentially the logic is:
     * <code>
     * UPDATE jobs AS job
     * WHERE job.scheduledTime before :maxtime
     * AND job.nodeId is null
     * AND job.scheduledTime MOD :y == :x
     * SET job.nodeId = :nodeId
     * </code>
     *
     * @param nodeId  node identifier to assign to jobs
     * @param x       the result of the mod-division
     * @param y       the dividend of the mod-division
     * @param maxtime only jobs with scheduled time earlier than this will be updated
     * @return number of jobs updated
     */
    public int updateAssignToNode(String nodeId, int x, int y, long maxtime) {
        Query q = entityManager.createQuery("UPDATE org.wso2.carbon.humantask.core.dao.jpa.openjpa.model.HumanTaskJob t SET t.nodeId = ?1 WHERE t.nodeId IS NULL AND MOD(t.time, ?2) = ?3 and t.time < ?4");
        q.setParameter(1, nodeId);
        q.setParameter(2, y);
        q.setParameter(3, x);
        q.setParameter(4, maxtime);

        return q.executeUpdate();
    }

    /**
     * Reassign jobs from one node to another.
     *
     * @param oldnode node assigning from
     * @param newnode new node asssigning to
     * @return number of rows changed
     */
    public int updateReassign(String oldnode, String newnode) {
        Query q = entityManager.createQuery("UPDATE org.wso2.carbon.humantask.core.dao.jpa.openjpa.model.HumanTaskJob t SET t.nodeId = ?1, t.scheduled = 0 WHERE t.nodeId = ?2");
        q.setParameter(1, newnode);
        q.setParameter(2, oldnode);

        return q.executeUpdate();
    }

    public int deleteAllJobs() {
        Query q = entityManager.createQuery("DELETE FROM org.wso2.carbon.humantask.core.dao.jpa.openjpa.model.HumanTaskJob");

        return q.executeUpdate();
    }

    public boolean delete(String jobId, String nodeId) {
        Query q = entityManager.createQuery("DELETE FROM org.wso2.carbon.humantask.core.dao.jpa.openjpa.model.HumanTaskJob t WHERE t.id = ?1 AND t.nodeId = ?2");
        q.setParameter(1, jobId);
        q.setParameter(2, nodeId);

        return q.executeUpdate() == 1;
    }

    /**
     * Delete jobs for the specified task and returns the list of jobIds
     *
     * @param taskId Task Id
     * @return List of job ids corresponding to the deleted jobs
     */
    public List<Long> deleteJobsForTask(Long taskId) {
        Query q = entityManager.createQuery("SELECT t.id FROM org.wso2.carbon.humantask.core.dao.jpa.openjpa.model.HumanTaskJob t WHERE t.taskId = ?1");
        q.setParameter(1, taskId);
        List<Long> jobIds = (List<Long>) q.getResultList();

        q = entityManager.createQuery("DELETE FROM org.wso2.carbon.humantask.core.dao.jpa.openjpa.model.HumanTaskJob t WHERE t.taskId = ?1");
        q.setParameter(1, taskId);

        q.executeUpdate();
        return jobIds;
    }

    /**
     * Update the schedule time for a job
     *
     * @param taskId     Task ID
     * @param immediate  Whether the job should be executed immediately
     * @param nearFuture Whether the job should be executed in the near future
     * @param nodeId     Node ID
     * @param time       Time to be updated
     * @param name       Name of the task
     * @return If there is a job, corresponding to taskId and name, returns the job id. If the are
     *         no jobs then returns -1.
     * @throws org.wso2.carbon.humantask.core.api.scheduler.InvalidJobsInDbException
     *          If there are two or more
     *          jobs are selected for the Task Id
     */
    public Long updateJob(Long taskId, String name, boolean immediate, boolean nearFuture,
                          String nodeId, Long time) throws InvalidJobsInDbException {
        Query q = entityManager.createQuery("SELECT t.id FROM org.wso2.carbon.humantask.core.dao.jpa.openjpa.model.HumanTaskJob t WHERE t.taskId = ?1 AND t.name = ?2");
        q.setParameter(1, taskId);
        q.setParameter(2, name);

        List<Long> jobIds = (List<Long>) q.getResultList();
        if (jobIds.size() != 1) {
            return handleException(taskId, name, jobIds.size());
        }

        int size;

        if (immediate) {
            log.info("Immediate");
            // Immediate scheduling means we add the job immediately to the todo list and
            // we put it in the DB for safe keeping
            q = entityManager.createQuery("UPDATE org.wso2.carbon.humantask.core.dao.jpa.openjpa.model.HumanTaskJob t SET t.time = ?1, t.nodeId = ?2, t.scheduled = true WHERE t.taskId = ?3 AND t.name = ?4");
            q.setParameter(1, time);
            q.setParameter(2, nodeId);
            q.setParameter(3, taskId);
            q.setParameter(4, name);
        } else if (nearFuture) {
            log.info("near");
            // Near future, assign the job to ourselves (why? -- this makes it very unlikely that we
            // would get two nodes trying to process the same instance
            q = entityManager.createQuery("UPDATE org.wso2.carbon.humantask.core.dao.jpa.openjpa.model.HumanTaskJob t SET t.time = ?1, t.nodeId = ?2, t.scheduled = false WHERE t.taskId = ?3 AND t.name = ?4");
            q.setParameter(1, time);
            q.setParameter(2, nodeId);
            q.setParameter(3, taskId);
            q.setParameter(4, name);
        } else {
            log.info("far");
            // Not the near future, we don't assign a node-id, we'll assign it later.
            q = entityManager.createQuery("UPDATE org.wso2.carbon.humantask.core.dao.jpa.openjpa.model.HumanTaskJob t SET t.time = ?1,t.nodeId = null, t.scheduled = false WHERE t.taskId = ?2 AND t.name = ?3");
            q.setParameter(1, time);
            q.setParameter(2, taskId);
            q.setParameter(3, name);
        }

        size = q.executeUpdate();

        if (size != 1) {
            return handleException(taskId, name, size);
        }

        return jobIds.get(0);
    }

    private Long handleException(Long taskId, String name, int size)
            throws InvalidJobsInDbException {
        String errMsg;
        if (size == 0) {
            errMsg = "There are no any jobs, corresponding to Task ID: " + taskId +
                     " and Name: " + name;
            log.warn(errMsg);
            return -1L;
        } else {
            errMsg = "More than 1 (" + size + ") jobs are selected to Task ID: " +
                     taskId + " and Name: " + name;
            log.error(errMsg);
            throw new InvalidJobsInDbException(errMsg);
        }
    }

    public EntityManager getEntityManager() {
        return entityManager;
    }

    /**
     * Creates a new EventDAO object.
     *
     * @return :
     */
    
    public EventDAO createNewEventObject(TaskDAO task) {
        EventDAO eventDAO = new Event();
        eventDAO.setTimeStamp(new Date());
        eventDAO.setOldState(task.getStatus());
        return eventDAO;
    }

    
    public AttachmentDAO createAttachment() {
        return new Attachment();
    }

    public DeploymentUnitDAO createDeploymentUnit() {
        return null;
    }

    public long getNextVersion() {
        List<TaskVersionDAO> resultList = entityManager.createQuery("select v from TaskVersion v")
                .getResultList();
        if(resultList.size() == 0){
            return 1;
        } else {
            TaskVersionDAO versionDAO = resultList.get(0);
            return versionDAO.getTaskVersion() + 1;
        }
    }

    public long setNextVersion(long version) {
        List<TaskVersionDAO> resultList = entityManager.createQuery("select v from TaskVersion v").getResultList();
        TaskVersionDAO versionDAO;
        if(resultList.size() == 0) {
            versionDAO = new TaskVersion();
        } else {
            versionDAO = resultList.get(0);
        }
        versionDAO.setTaskVersion(version);
        entityManager.persist(versionDAO);
        return version;
    }



    public DeploymentUnitDAO createDeploymentUnitDAO(HumanTaskDeploymentUnit deploymentUnit, int tenantId){
        String taskRelativePath =  "repository" + File.separator + HumanTaskConstants.HUMANTASK_REPO_DIRECTORY +
                File.separator + tenantId + File.separator + deploymentUnit.getName();

        DeploymentUnitDAO deploymentUnitDAO = new DeploymentUnit();
        deploymentUnitDAO.setVersion(deploymentUnit.getVersion());
        deploymentUnitDAO.setName(deploymentUnit.getName());
        deploymentUnitDAO.setPackageName(deploymentUnit.getPackageName());
        deploymentUnitDAO.setStatus(TaskPackageStatus.ACTIVE);
        deploymentUnitDAO.setChecksum(deploymentUnit.getMd5sum());
        deploymentUnitDAO.setDeploymentUnitDir(taskRelativePath);
        deploymentUnitDAO.setDeployDate(new Date());
        deploymentUnitDAO.setTenantId(tenantId);
        entityManager.persist(deploymentUnitDAO);
        return deploymentUnitDAO;
    }

    public DeploymentUnitDAO getDeploymentUnit(int tenantId, String md5sum) {
        Query q = entityManager.createQuery("select hu from org.wso2.carbon.humantask.core.dao.jpa.openjpa.model" +
                ".DeploymentUnit hu WHERE hu.status=?1 and hu.tenantId=?2 and hu.checksum=?3");

        q.setParameter(1, TaskPackageStatus.ACTIVE);
        q.setParameter(2, tenantId);
        q.setParameter(3, md5sum);
        List<DeploymentUnit> resultList = q.getResultList();
        DeploymentUnit deploymentUnit = null;
        if (resultList.size() != 0) {
            deploymentUnit = resultList.get(0);
        }
        return deploymentUnit;
    }

    /**
     * Obtain the list of deployment units matching the given package name. Name of the deployment unit will be
     * appended with the version while package name will remain the same. Hence if multiple version of the same package exists
     * (At least one version of the human task package is already deployed, there should be a maching deployment unit )
     * @param tenantId
     * @param packageName
     * @return
     */
   public List<DeploymentUnitDAO> getDeploymentUnitsForPackageName(int tenantId, String packageName) {
        Query query = entityManager.createQuery("SELECT hdu from org.wso2.carbon.humantask.core.dao.jpa.openjpa.model.DeploymentUnit" +
                " hdu WHERE hdu.packageName=?1 and hdu.tenantId=?2 ");
        query.setParameter(1, packageName);
        query.setParameter(2, tenantId);
        return query.getResultList();
   }

    public DeploymentUnitDAO getDeploymentUnitByName(int tenantId, String packageName) {

        Query q = entityManager.createQuery("select hu from org.wso2.carbon.humantask.core.dao.jpa.openjpa.model" +
                ".DeploymentUnit hu WHERE hu.status=?1 and hu.tenantId=?2 and hu.packageName=?3");

        q.setParameter(1, TaskPackageStatus.ACTIVE);
        q.setParameter(2, tenantId);
        q.setParameter(3, packageName);
        List<DeploymentUnit> resultList = q.getResultList();
        DeploymentUnit deploymentUnit = null;
        if (resultList.size() != 0) {
            deploymentUnit = resultList.get(0);
        }
        return deploymentUnit;
    }


    public List<TaskDAO> getMatchingTaskInstances(String packageName, int tenantId){
        Query q = entityManager.createQuery("select t from org.wso2.carbon.humantask.core.dao.jpa.openjpa.model.Task t where t.packageName = ?1 and t.tenantId = ?2");
        q.setParameter(1, packageName);
        q.setParameter(2, tenantId);
        return q.getResultList();
    }

    public void deleteDeploymentUnits(String packageName, int tenantId){
        Query query = entityManager.createQuery("delete from org.wso2.carbon.humantask.core.dao.jpa.openjpa.model.DeploymentUnit  d where d.packageName = ?1 and d.tenantId = ?2");
        query.setParameter(1, packageName);
        query.setParameter(2, tenantId);
        query.executeUpdate();
    }

}
