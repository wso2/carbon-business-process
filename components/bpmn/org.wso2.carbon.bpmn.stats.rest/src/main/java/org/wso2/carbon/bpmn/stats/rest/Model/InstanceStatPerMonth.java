package org.wso2.carbon.bpmn.stats.rest.Model;

/**
 * Created by natasha on 12/2/15.
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
