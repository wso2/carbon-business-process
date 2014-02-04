package org.wso2.carbon.humantask.core.engine.commands;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.humantask.core.dao.*;
import org.wso2.carbon.humantask.core.engine.runtime.api.HumanTaskIllegalStateException;
import org.wso2.carbon.humantask.core.engine.runtime.api.HumanTaskRuntimeException;

import java.util.ArrayList;
import java.util.List;

/**
 * Task suspend
 */
public class Suspend extends AbstractHumanTaskCommand {
    private static final Log log = LogFactory.getLog(Start.class);

    public Suspend(String callerId, Long taskId) {
        super(callerId, taskId);
    }

    /**
     * Checks the Pre-conditions before executing the task operation.
     */
    @Override
    protected void checkPreConditions() {
        checkForValidTask();
    }

    /**
     * Perform the authorization checks before executing the task operation.
     */
    @Override
    protected void authorise() {
        List<GenericHumanRoleDAO.GenericHumanRoleType> allowedRoles =
                new ArrayList<GenericHumanRoleDAO.GenericHumanRoleType>();
        allowedRoles.add(GenericHumanRoleDAO.GenericHumanRoleType.ACTUAL_OWNER);
        allowedRoles.add(GenericHumanRoleDAO.GenericHumanRoleType.BUSINESS_ADMINISTRATORS);
        allowedRoles.add(GenericHumanRoleDAO.GenericHumanRoleType.STAKEHOLDERS);

        authoriseRoles(allowedRoles);
    }

    /**
     * Perform the state checks before executing the task operation.
     */
    @Override
    protected void checkState() {
        TaskDAO task = getTask();
        boolean isInSuspendableState = false;
        if (TaskStatus.IN_PROGRESS.equals(task.getStatus()) ||
                TaskStatus.READY.equals(task.getStatus()) ||
                TaskStatus.RESERVED.equals(task.getStatus())) {
            isInSuspendableState = true;
        }
        if (!isInSuspendableState) {
            String errMsg = String.format("User[%s] cannot perform [%s] operation on task[%d] as the task is in state[%s]. " +
                    "[%s] operation can be performed only on tasks in states[%s,%s,%s]",
                    getOperationInvoker().getName(), Suspend.class, task.getId(),
                    task.getStatus(), Suspend.class, TaskStatus.RESERVED,
                    TaskStatus.READY, TaskStatus.IN_PROGRESS);
            log.error(errMsg);
            throw new HumanTaskIllegalStateException(errMsg);
        }
    }

    /**
     * Checks the post-conditions after executing the task operation.
     */
    @Override
    protected void checkPostConditions() {
        checkPostState(TaskStatus.SUSPENDED);
    }

    @Override
    protected EventDAO createTaskEvent() {
        EventDAO taskEvent = super.createTaskEvent();
        taskEvent.setDetails("");
        return taskEvent;
    }

    @Override
    public void execute() {
        authorise();
        TaskDAO task = getTask();
        checkPreConditions();
        checkState();
        task.suspend();
        processTaskEvent();
        checkPostConditions();
    }
}
