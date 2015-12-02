package org.wso2.carbon.bpmn.stats.rest.Model;

/**
 * Model object to keep a count of started and completed process and task instances for each month
 */
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
