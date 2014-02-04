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

import org.wso2.carbon.humantask.core.dao.PresentationNameDAO;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

/**
 * Domain Object for presentation name.
 */
@Entity
@DiscriminatorValue("PNAME")
@NamedQueries(
        @NamedQuery(name=PresentationName.DELETE_PRESENTATION_NAME_BY_TASK, query = "delete from org.wso2.carbon.humantask.core.dao.jpa.openjpa.model.PresentationName as pn where pn.task = :task")
)
public final class PresentationName extends PresentationElement implements PresentationNameDAO {

    public static final String DELETE_PRESENTATION_NAME_BY_TASK ="DELETE_PRESENTATION_NAME_BY_TASK";

    @Override
    public void setValue(String value) {
        super.setValue(value);
    }
}
