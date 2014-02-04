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

import org.wso2.carbon.humantask.core.dao.PresentationSubjectDAO;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

/**
 * Domain Object for Presentation Subject.
 */
@Entity
@DiscriminatorValue("PSUB")
@NamedQueries(
        @NamedQuery(name = PresentationSubject.DELETE_PRESENTATION_SUBJECT_BY_TASK , query = "delete from org.wso2.carbon.humantask.core.dao.jpa.openjpa.model as s where s.task = :task")
)
public final class PresentationSubject extends PresentationElement implements PresentationSubjectDAO {

    public static final String DELETE_PRESENTATION_SUBJECT_BY_TASK = "DELETE_PRESENTATION_SUBJECT_BY_TASK";

    @Override
    public void setValue(String value) {
        super.setValue(value);
    }
}
