/*
 * Copyright (c) 2015. WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * /
 */

package org.wso2.carbon.bpmn.rest.model.runtime;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.wso2.carbon.bpmn.rest.engine.variable.RestVariable;

import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 */
@XmlRootElement(name = "ExecutionActionRequest")
@XmlAccessorType(XmlAccessType.FIELD)
public class ExecutionActionRequest extends RestActionRequest {

    public static final String ACTION_SIGNAL = "signal";
    public static final String ACTION_SIGNAL_EVENT_RECEIVED = "signalEventReceived";
    public static final String ACTION_MESSAGE_EVENT_RECEIVED = "messageEventReceived";

    protected String signalName;
    protected String messageName;
    @XmlElementWrapper(name = "RestVariables")
    @XmlElement(name = "RestVariable", type = RestVariable.class)
    protected List<RestVariable> variables;

    public void setVariables(List<RestVariable> variables) {
        this.variables = variables;
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, defaultImpl = RestVariable.class)
    public List<RestVariable> getVariables() {
        return variables;
    }

    public String getSignalName() {
        return signalName;
    }

    public void setSignalName(String signalName) {
        this.signalName = signalName;
    }

    public String getMessageName() {
        return messageName;
    }

    public void setMessageName(String messageName) {
        this.messageName = messageName;
    }

}
