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

package org.wso2.carbon.bpel.core.ode.integration.utils;

import org.apache.ode.bpel.evt.ActivityEvent;
import org.apache.ode.bpel.evt.BpelEvent;
import org.apache.ode.bpel.iapi.BpelEventListener;
import org.apache.ode.bpel.pmapi.EventInfoListDocument;
import org.apache.ode.bpel.pmapi.TEventInfo;
import org.apache.ode.bpel.pmapi.TEventInfoList;

import java.util.Calendar;
import java.util.Date;
import java.util.Properties;

public class ActivityLifeCycleEventsDocumentBuilder implements BpelEventListener {
    public EventInfoListDocument getActivityLifeCycleEvents() {
        return activityLifeCycleEvents;
    }

    private EventInfoListDocument activityLifeCycleEvents = EventInfoListDocument.Factory.newInstance();

    public void onEvent(BpelEvent bpelEvent) {
        if (bpelEvent instanceof ActivityEvent) {
            final ActivityEvent event = (ActivityEvent) bpelEvent;
            fillEventInfo(event);
        }
    }

    private void fillEventInfo(ActivityEvent event) {
        TEventInfoList aEventList = activityLifeCycleEvents.getEventInfoList();
        if (aEventList == null) {
            aEventList = TEventInfoList.Factory.newInstance();
            activityLifeCycleEvents.setEventInfoList(aEventList);
            aEventList = activityLifeCycleEvents.getEventInfoList();
        }

        TEventInfo eventInfo = aEventList.addNewEventInfo();
        eventInfo.setName(getClassName(BpelEvent.eventName(event)));
        eventInfo.setLineNumber(event.getLineNo());
        eventInfo.setTimestamp(convertDatetoCalendar(event.getTimestamp()));
        eventInfo.setType(event.getType().toString());
        eventInfo.setActivityId(event.getActivityId());
        eventInfo.setActivityName(event.getActivityName());
        eventInfo.setActivityType(event.getActivityType());
        eventInfo.setScopeId(event.getScopeId());
        eventInfo.setScopeName(event.getScopeName());
        //activityLifeCycleEvents.
    }

    /**
     * used to get the class name for a output of BpelEvent.eventName()
     * this is not a standard way of getting the class name, so mey be need to re-impl
     * @param reflectionName Reflection name
     * @return Class name
     */
    private String getClassName(String reflectionName) {
       if (reflectionName.contains("$"))
        {
        String[] splitter = reflectionName.split("\\$");
        return splitter[splitter.length-3];
        }
        else return reflectionName; 
    }

    private Calendar convertDatetoCalendar(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);

        return cal;
    }

    public void startup(Properties properties) {
        //do nothing
    }

    public void shutdown() {
        //do nothing
    }
}
