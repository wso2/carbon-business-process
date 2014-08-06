/*
 * Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.humantask.core.engine.event.processor;

import org.wso2.carbon.humantask.core.api.event.HumanTaskEventListener;
import org.wso2.carbon.humantask.core.api.event.TaskEventInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is responsible for the event processing logic of task events.
 * Currently we'd only call the registered event listeners.
 */
public class EventProcessor {


    /**
     * The list of task event listeners registered with the event processor.
     */
    private List<HumanTaskEventListener> eventListeners = new ArrayList<HumanTaskEventListener>();


    /**
     * Process the task event.
     *
     * Currently we'd only call the registered event listeners.
     *
     * @param taskEventInfo : The task event information.
     */
    public void processEvent(final TaskEventInfo taskEventInfo) {
        for (HumanTaskEventListener eventListener : eventListeners) {
            eventListener.onEvent(taskEventInfo);
        }
    }

    /**
     * @param eventListener : Add a new event listener class to the event processor.
     */
    public void addEventListener(HumanTaskEventListener eventListener) {
        this.eventListeners.add(eventListener);
    }
}
