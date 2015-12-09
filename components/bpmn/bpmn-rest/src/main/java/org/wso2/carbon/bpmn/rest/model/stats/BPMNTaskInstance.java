package org.wso2.carbon.bpmn.rest.model.stats;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Model object of a task instance
 */
@XmlRootElement(name = "Task")
@XmlAccessorType(XmlAccessType.FIELD)
public class BPMNTaskInstance {

    private String taskDefinitionKey;
    private double averageTimeForCompletion;

    public double getAverageTimeForCompletion() {
        return averageTimeForCompletion;
    }

    public void setAverageTimeForCompletion(double averageTimeForCompletion) {
        this.averageTimeForCompletion = averageTimeForCompletion;
    }

    public String getTaskDefinitionKey() {
        return taskDefinitionKey;
    }

    public void setTaskDefinitionKey(String taskDefinitionKey) {
        this.taskDefinitionKey = taskDefinitionKey;
    }

}
