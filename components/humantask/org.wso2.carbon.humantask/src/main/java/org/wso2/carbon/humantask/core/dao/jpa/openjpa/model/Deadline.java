/*
 * Copyright (c) 2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.humantask.core.dao.jpa.openjpa.model;

import org.wso2.carbon.humantask.core.dao.DeadlineDAO;
import org.wso2.carbon.humantask.core.dao.TaskDAO;
import org.wso2.carbon.humantask.core.dao.TaskStatus;

import javax.persistence.*;
import java.util.Date;

/**
 * Task Deadline.
 */
@Entity
@Table(name = "HT_DEADLINE")
@NamedQueries(
        @NamedQuery(name = Deadline.DELETE_DEADLINES_BY_TASK, query = "delete from org.wso2.carbon.humantask.core.dao.jpa.openjpa.model.Deadline as d where d.task = :task" )
)
public class Deadline implements DeadlineDAO {

    public static final String DELETE_DEADLINES_BY_TASK = "DELETE_DEADLINES_BY_TASK";

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "DEADLINE_NAME", nullable = false)
    private String name;

    @Column(name = "DEADLINE_DATE", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date deadlineDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS_TOBE_ACHIEVED", nullable = false)
    private TaskStatus status;

    @ManyToOne
    private Task task;

//    @OneToMany(mappedBy = "deadline", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
//    private List<Escalation> escalations = new ArrayList<Escalation>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getDeadlineDate() {
        return new Date(deadlineDate.getTime());
    }

    public void setDeadlineDate(Date deadlineDate) {
        this.deadlineDate = new Date(deadlineDate.getTime());
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public TaskDAO getTask() {
        return task;
    }

    public void setTask(TaskDAO task) {
        this.task = (Task) task;
    }
}
