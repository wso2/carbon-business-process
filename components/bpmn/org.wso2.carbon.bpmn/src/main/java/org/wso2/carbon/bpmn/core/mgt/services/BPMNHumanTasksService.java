package org.wso2.carbon.bpmn.core.mgt.services;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.TaskService;
import org.activiti.engine.task.Task;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bpmn.core.BPSException;
import org.wso2.carbon.bpmn.core.deployment.TenantRepository;
import org.wso2.carbon.bpmn.core.BPMNServerHolder;
import org.wso2.carbon.bpmn.core.mgt.model.BPMNTask;
import org.wso2.carbon.context.CarbonContext;

import java.util.ArrayList;
import java.util.List;

public class BPMNHumanTasksService {

    private static Log log = LogFactory.getLog(BPMNHumanTasksService.class);

    public BPMNTask[] getTasksOfUser(String username) throws BPSException {
        try {
            ProcessEngine engine = BPMNServerHolder.getInstance().getEngine();
            TaskService taskService = engine.getTaskService();
            List<Task> tasks = taskService.createTaskQuery().taskAssignee(username).list();
            List<Task> tenantTasks = filterTenantTasks(tasks);

            BPMNTask[] bpmnTasks = new BPMNTask[tenantTasks.size() + 1];
            int i = 0;
            for (Task t : tenantTasks) {
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
            Integer tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
            TenantRepository tenantRepository = BPMNServerHolder.getInstance().getTenantManager().getTenantRepository(tenantId);

            ProcessEngine engine = BPMNServerHolder.getInstance().getEngine();
            TaskService taskService = engine.getTaskService();
            Task task = taskService.createTaskQuery().taskId(taskId).list().get(0);
            if (!tenantRepository.isTenantTask(task)) {
                String msg = "Operation not permitted for tenant: " + tenantId;
                log.error(msg);
                throw new BPSException(msg);
            }

            taskService.complete(taskId);
        } catch (Exception e) {
            String msg = "Failed to complete the task: " + taskId;
            log.error(msg, e);
            throw new BPSException(msg, e);
        }
    }

    private List<Task> filterTenantTasks(List<Task> tasks) throws BPSException {
        Integer tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        TenantRepository tenantRepository = BPMNServerHolder.getInstance().getTenantManager().getTenantRepository(tenantId);

        List<Task> tenantTasks = new ArrayList<Task>();
        for (Task task : tasks) {
            if (tenantRepository.isTenantTask(task)) {
                tenantTasks.add(task);
            }
        }
        return tenantTasks;
    }
}
