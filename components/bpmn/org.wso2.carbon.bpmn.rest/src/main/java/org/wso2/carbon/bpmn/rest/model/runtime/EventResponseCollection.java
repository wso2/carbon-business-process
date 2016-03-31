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

package org.wso2.carbon.bpmn.rest.model.runtime;

import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 */
@XmlRootElement(name = "EventResponseCollection")
@XmlAccessorType(XmlAccessType.FIELD)
public class EventResponseCollection {

    public List<EventResponse> getEventResponseList() {
        return eventResponseList;
    }

    public EventResponseCollection() {
    }

    public void setEventResponseList(List<EventResponse> eventResponseList) {
        this.eventResponseList = eventResponseList;
    }

    @XmlElementWrapper(name = "EventResponses")
    @XmlElement(name = "EventResponse", type = EventResponse.class)
    private List<EventResponse> eventResponseList;

}
