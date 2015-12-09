package org.wso2.carbon.bpmn.rest.model.stats;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Model object of a user
 */
@XmlRootElement(name = "User")
@XmlAccessorType(XmlAccessType.FIELD)
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
