package org.wso2.carbon.bpmn.stats.rest.Model;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by natasha on 11/30/15.
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
    public String toString(){
        return statusOfProcessOrTask+" "+count;
    }
}
