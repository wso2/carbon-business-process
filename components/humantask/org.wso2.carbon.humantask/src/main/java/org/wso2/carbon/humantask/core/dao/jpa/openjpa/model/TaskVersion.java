package org.wso2.carbon.humantask.core.dao.jpa.openjpa.model;


import org.wso2.carbon.humantask.core.dao.TaskVersionDAO;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "HT_VERSIONS")
public class TaskVersion implements TaskVersionDAO{

    @Column(name="TASK_VERSION", nullable = false)
    private long taskVersion;


    public long getTaskVersion() {
        return taskVersion;
    }
    public void setTaskVersion(long version) {
        this.taskVersion = version;
    }
}