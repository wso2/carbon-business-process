package org.wso2.carbon.bpmn.core.mgt.services;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.TaskService;
import org.activiti.engine.task.Task;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bpmn.core.BPMNServerHolder;
import org.wso2.carbon.bpmn.core.BPSException;
import org.wso2.carbon.bpmn.core.mgt.model.BPMNTask;
import org.wso2.carbon.context.CarbonContext;

import java.util.List;

public class BPMNHumanTasksService {

    private static Log log = LogFactory.getLog(BPMNHumanTasksService.class);

    public BPMNTask[] getTasksOfUser(String username) throws BPSException {

        Integer tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            ProcessEngine engine = BPMNServerHolder.getInstance().getEngine();
            TaskService taskService = engine.getTaskService();
            List<Task> tasks = taskService.createTaskQuery().taskTenantId(tenantId.toString()).taskAssignee(username).list();

            BPMNTask[] bpmnTasks = new BPMNTask[tasks.size() + 1];
            int i = 0;
            for (Task t : tasks) {
                BPMNTask bpmnTask = new BPMNTask();
                bpmnTask.setId(t.getId());
                bpmnTask.setName(t.getName());
                bpmnTask.setProcessInstanceId(t.getProcessInstanceId());
                bpmnTasks[i] = bpmnTask;
                i++;
            }
            return bpmnTasks;
        } catch (Exception e) {
            String msg = "Failed to get tasks of the user: " + username;
            log.error(msg, e);
            throw new BPSException(msg, e);
        }
    }

    public void completeTask(String taskId) throws BPSException {
        try {
            ProcessEngine engine = BPMNServerHolder.getInstance().getEngine();
            engine.getTaskService().complete(taskId);
        } catch (Exception e) {
            String msg = "Failed to complete the task: " + taskId;
            log.error(msg, e);
            throw new BPSException(msg, e);
        }
    }

}
