package org.wso2.carbon.bpmn.stats.rest.Model;

import org.wso2.carbon.bpmn.core.mgt.model.BPMNVariable;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.Date;

/**
 * Created by natasha on 12/1/15.
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
