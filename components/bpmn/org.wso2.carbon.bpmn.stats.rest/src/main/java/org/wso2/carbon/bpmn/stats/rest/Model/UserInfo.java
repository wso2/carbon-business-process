package org.wso2.carbon.bpmn.stats.rest.Model;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Model object of a user
 */
@XmlRootElement(name = "User")
public class UserInfo {
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    private String userName;

    public long getTaskCount() {
        return taskCount;
    }

    public void setTaskCount(long taskCount) {
        this.taskCount = taskCount;
    }

    private long taskCount;

    public double getAvgTimeDuration() {
        return avgTimeDuration;
    }

    public void setAvgTimeDuration(double avgTimeDuration) {
        this.avgTimeDuration = avgTimeDuration;
    }

    private double avgTimeDuration;


}
