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
 * Model object to keep a count of started and completed process and task instances for each month
 */
@XmlRootElement(name = "InstanceVariation")
@XmlAccessorType(XmlAccessType.FIELD)
public class ProcessInstanceStatInfo {

    private String month;
    private int instancesStarted;
    private int instancesCompleted;

    public ProcessInstanceStatInfo(String  month, int instancesStarted, int instancesCompleted) {
        this.month = month;
        this.instancesStarted = instancesStarted;
        this.instancesCompleted = instancesCompleted;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public int getInstancesStarted() {
        return instancesStarted;
    }

    public void setInstancesStarted(int instancesStarted) {
        this.instancesStarted = instancesStarted;
    }

    public int getInstancesCompleted() {
        return instancesCompleted;
    }

    public void setInstancesCompleted(int instancesCompleted) {
        this.instancesCompleted = instancesCompleted;
    }
}
