package org.wso2.carbon.humantask.core.dao.jpa.openjpa.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.humantask.core.api.client.TransformerUtils;
import org.wso2.carbon.humantask.core.dao.MessageDAO;
import org.wso2.carbon.humantask.core.dao.TaskCreationContext;
import org.wso2.carbon.humantask.core.dao.TaskDAO;
import org.wso2.carbon.humantask.core.dao.TaskStatus;
import org.wso2.carbon.humantask.core.dao.TaskType;
import org.wso2.carbon.humantask.core.dao.jpa.openjpa.model.Task;
import org.wso2.carbon.humantask.core.engine.HumanTaskException;
import org.wso2.carbon.humantask.core.engine.runtime.api.HumanTaskRuntimeException;
import org.wso2.carbon.humantask.core.store.HumanTaskBaseConfiguration;

import java.util.Date;

/**
 * The builder class for creating Task Objects.
 */
public class HumanTaskBuilderImpl {
    /**
     * Class Logger
     */
    private static Log log = LogFactory.getLog(HumanTaskBuilderImpl.class);

    /**
     * The object holding information about task creation
     */
    private TaskCreationContext creationContext;

    /**
     * The inputMessage of the task.
     */
    private MessageDAO inputMessage;

    /**
     * Add the TaskCreationContext to the builder.
     *
     * @param creationContext : The task creation context.
     * @return : The HumanTaskBuilderImpl object.
     */
    public HumanTaskBuilderImpl addTaskCreationContext(TaskCreationContext creationContext) {
        this.creationContext = creationContext;
        return this;
    }

    /**
     * Add the TaskCreationContext to the builder.
     *
     * @param inputMessage : The task creation context.
     * @return : The HumanTaskBuilderImpl object.
     */
    public HumanTaskBuilderImpl addInputMessage(MessageDAO inputMessage) {
        this.inputMessage = inputMessage;
        return this;
    }

    /**
     * Builds the Task from the given input.
     *
     * @return : The created task object.
     */
    public TaskDAO build() {
        validateParams();
        TaskDAO task;
        HumanTaskBaseConfiguration taskConfiguration = creationContext.getTaskConfiguration();
        int tenantId = creationContext.getTenantId();

        if (creationContext.getTaskConfiguration().isTask()) {
            task = new Task(taskConfiguration.getName(), TaskType.TASK, tenantId);
        } else {
            task = new Task(taskConfiguration.getName(), TaskType.NOTIFICATION, tenantId);
        }

        task.setInputMessage(this.inputMessage);
        task.setSkipable(false);
        task.setEscalated(false);
        task.setStatus(TaskStatus.CREATED);
        task.setActivationTime(new Date());
        task.setTaskVersion(taskConfiguration.getVersion());
        task.setTaskPackageName(taskConfiguration.getPackageName());
        task.setDefinitionName(taskConfiguration.getDefinitionName());

        if (creationContext.getTaskConfiguration().isTask()) { // Attachments are only valid for Tasks.
            //Setting the attachments to the task
            try {
                task.setAttachments(TransformerUtils.generateAttachmentDAOListFromIDs(task,
                        creationContext.getAttachmentIDs()));
            } catch (HumanTaskException e) {
                log.error(e.getLocalizedMessage(), e);
            }
        }
        //Setting the attachments to the task
        try {
            task.setAttachments(TransformerUtils.generateAttachmentDAOListFromIDs(task,
                                                                                  creationContext.getAttachmentIDs()));
        } catch (HumanTaskException e) {
            log.error(e.getLocalizedMessage(), e);
        }

        return task;
    }

    // validates the required builder parameters are there.
    private void validateParams() {
        if (inputMessage == null) {
            throw new HumanTaskRuntimeException("the input message cannot be null");
        }

        if (creationContext == null) {
            throw new HumanTaskRuntimeException("the task creation context cannot be null");
        }
    }
}
