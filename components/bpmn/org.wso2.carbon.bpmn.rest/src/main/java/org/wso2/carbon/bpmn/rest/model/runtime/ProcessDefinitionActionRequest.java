/*
 *   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 *
 */
package org.wso2.carbon.bpmn.rest.model.runtime;

import java.util.Date;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "ProcessDefinitionActionRequest")
@XmlAccessorType(XmlAccessType.FIELD)
public class ProcessDefinitionActionRequest extends RestActionRequest {

    public static final String ACTION_SUSPEND = "suspend";
    public static final String ACTION_ACTIVATE = "activate";

    private boolean includeProcessInstances;
    private Date date;

    public boolean isIncludeProcessInstances() {
        return includeProcessInstances;
    }

    public void setIncludeProcessInstances(boolean includeProcessInstances) {
        this.includeProcessInstances = includeProcessInstances;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}

