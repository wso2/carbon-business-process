package org.wso2.carbon.bpmn.stats.rest.Model;

import org.wso2.carbon.bpmn.core.mgt.model.BPMNVariable;

import java.util.Date;
import java.util.List;

/**
 * Created by natasha on 12/1/15.
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
