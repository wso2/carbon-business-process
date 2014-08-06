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

import org.wso2.carbon.humantask.core.dao.TaskDAO;

import javax.persistence.*;

/**
 * Domain Object for presentation element.
 */
@Entity
@Table(name = "HT_PRESENTATION_ELEMENT")
@Inheritance
@DiscriminatorColumn(name = "PE_TYPE")
@NamedNativeQueries(
        @NamedNativeQuery(name=PresentationElement.DELETE_PRESENTATION_ELEMENTS_FOR_TASK, query = "delete from PresentationElement as e where e.task=:task")
)
public class PresentationElement {

    public static final String DELETE_PRESENTATION_ELEMENTS_FOR_TASK="DELETE_PRESENTATION_ELEMENTS_FOR_TASK";

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "XML_LANG")
    private String xmlLang;

    @Column(name = "PE_CONTENT", length = 2000)
    private String value;

    @ManyToOne
    private Task task;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getXmlLang() {
        return xmlLang;
    }

    public void setXmlLang(String xmlLang) {
        this.xmlLang = xmlLang;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public TaskDAO getTask() {
        return task;
    }

    public void setTask(TaskDAO task) {
        this.task = (Task) task;
    }
}
