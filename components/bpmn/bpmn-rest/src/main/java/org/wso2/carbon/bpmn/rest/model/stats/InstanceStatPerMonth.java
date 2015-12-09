package org.wso2.carbon.bpmn.rest.model.stats;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Model object to keep a count of started and completed process and task instances for each month
 */
@XmlRootElement(name = "InstanceVariation")
@XmlAccessorType(XmlAccessType.FIELD)
public class InstanceStatPerMonth {

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    private String month;


    public int getStartedInstances() {
        return startedInstances;
    }

    public void setStartedInstances(int startedInstances) {
        this.startedInstances = startedInstances;
    }

    private int startedInstances;

    private int completedInstances;

    public int getCompletedInstances() {
        return completedInstances;
    }

    public void setCompletedInstances(int completedInstances) {
        this.completedInstances = completedInstances;
    }
}
