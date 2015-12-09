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

    private String userName;
    private long taskCount;
    private double avgTimeDuration;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public long getTaskCount() {
        return taskCount;
    }

    public void setTaskCount(long taskCount) {
        this.taskCount = taskCount;
    }

    public double getAvgTimeDuration() {
        return avgTimeDuration;
    }

    public void setAvgTimeDuration(double avgTimeDuration) {
        this.avgTimeDuration = avgTimeDuration;
    }

}
