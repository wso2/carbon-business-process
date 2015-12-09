package org.wso2.carbon.bpmn.rest.model.stats;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Model object which keeps the count of process and task instances with their status i.e.
 * Completed , Active, Suspended, Failed
 */
@XmlRootElement(name = "ProcessTaskCount")
public class ProcessTaskCount {

    public String getStatusOfProcessOrTask() {
        return statusOfProcessOrTask;
    }

    public void setStatusOfProcessOrTask(String statusOfProcessOrTask) {
        this.statusOfProcessOrTask = statusOfProcessOrTask;
    }

    private String statusOfProcessOrTask;
    private long count;

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    @Override
    public String toString() {
        return statusOfProcessOrTask + " " + count;
    }
}
