package org.wso2.carbon.bpmn.stats.rest.Model;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Model object of a task instance
 */
@XmlRootElement(name = "Task")
public class BPMNTaskInstance {

    public String getTaskDefinitionKey() {
        return taskDefinitionKey;
    }

    public void setTaskDefinitionKey(String taskDefinitionKey) {
        this.taskDefinitionKey = taskDefinitionKey;
    }

    private String taskDefinitionKey;

    public double getAverageTimeForCompletion() {
        return averageTimeForCompletion;
    }

    public void setAverageTimeForCompletion(double averageTimeForCompletion) {
        this.averageTimeForCompletion = averageTimeForCompletion;
    }

    private double averageTimeForCompletion;

}
