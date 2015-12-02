package org.wso2.carbon.bpmn.stats.rest.Model;

import org.wso2.carbon.bpmn.core.mgt.model.BPMNVariable;

import java.util.Date;
import java.util.List;

/**
 * Model object to keep a list of tasks in a process
 */
public class ProcessForTask {

    public List<BPMNTaskInstance> getTaskList() {
        return taskList;
    }

    public void setTaskList(List<BPMNTaskInstance> taskList) {
        this.taskList = taskList;
    }

    private List<BPMNTaskInstance> taskList;

}
