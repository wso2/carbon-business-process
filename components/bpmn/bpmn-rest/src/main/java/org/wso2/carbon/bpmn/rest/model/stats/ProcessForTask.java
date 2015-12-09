package org.wso2.carbon.bpmn.rest.model.stats;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * Model object to keep a list of tasks in a process
 */
@XmlRootElement(name = "TaskListForProcess")
@XmlAccessorType(XmlAccessType.FIELD)
public class ProcessForTask {

    public List<BPMNTaskInstance> getTaskList() {
        return taskList;
    }

    public void setTaskList(List<BPMNTaskInstance> taskList) {
        this.taskList = taskList;
    }

    private List<BPMNTaskInstance> taskList;

}
