/**
 *  Copyright (c) 2015 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.wso2.carbon.bpmn.rest.model.stats;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Model object which keeps the count of process and task instances with their status i.e.
 * Completed , Active, Suspended, Failed
 */
@XmlRootElement(name = "ProcessTaskCount")
@XmlAccessorType(XmlAccessType.FIELD)
public class ProcessTaskCount {
    private String statusOfProcessOrTask;
    private long count;

    public String getStatusOfProcessOrTask() {
        return statusOfProcessOrTask;
    }

    public void setStatusOfProcessOrTask(String statusOfProcessOrTask) {
        this.statusOfProcessOrTask = statusOfProcessOrTask;
    }

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
