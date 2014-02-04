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

import org.wso2.carbon.humantask.core.dao.PresentationParameterDAO;
import org.wso2.carbon.humantask.core.dao.TaskDAO;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "HT_PRESENTATION_PARAM")
@NamedQueries(
        @NamedQuery(name = PresentationParameter.DELETE_PRESENTATION_PARAMETERS_BY_TASK, query = "delete from org.wso2.carbon.humantask.core.dao.jpa.openjpa.model.PresentationParameter as p where p.task=:task")
)
public class PresentationParameter implements PresentationParameterDAO, Serializable {

    public static final String DELETE_PRESENTATION_PARAMETERS_BY_TASK = "DELETE_PRESENTATION_PARAMETERS_BY_TASK";

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "PARAM_NAME")
    private String name;

    /**
     * Value will be stored as a string. Also type will be persisted. When getting back the
     * value caller must convert it back to correct type with correct value.
     */
    @Column(name = "PARAM_VALUE", length = 2000)
    private String value;

    @Enumerated(EnumType.STRING)
    @Column(name = "PARAM_TYPE")
    private Type type;

    @ManyToOne
    private Task task;

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

    public void setTask(TaskDAO task) {
        this.task = (Task)task;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public Type getType() {
        return type;
    }
}
