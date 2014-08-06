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
import org.apache.ode.bpel.evtproc.ActivityStateDocumentBuilder;
import org.apache.ode.bpel.iapi.BpelEventListener;
import org.apache.ode.bpel.pmapi.ActivityInfoDocument;
import org.apache.ode.bpel.pmapi.EventInfoListDocument;
import org.apache.ode.bpel.pmapi.TEventInfo;
import org.apache.ode.bpel.pmapi.TEventInfoList;

import java.util.*;

/**
 * Initiate, update the ActivityInfoWithEventsDocument objects so,
 */
public class ActivityStateAndEventDocumentBuilder extends ActivityStateDocumentBuilder
        implements BpelEventListener {
    /**
     * Keep a list of the activity info with events objects, so we can return
     * them in the order of creation.
     */
    private List<ActivityInfoWithEventsDocument> activitiesWithEventsOrdered = new ArrayList<ActivityInfoWithEventsDocument>();
    private Map<Long, ActivityInfoWithEventsDocument> activitiesWithEvents = new HashMap<Long, ActivityInfoWithEventsDocument>();

//    private boolean removeCompleted = false;

    /**
     * Update activitiesWithEventsOrdered and activitiesWithEvents based on the event
     *
     * @param be BPEL Event
     */
    public void onEvent(BpelEvent be) {
        super.onEvent(be);
        List<ActivityInfoDocument> infoDocList = super.getActivities();


        //Adding the event information
        if (be instanceof ActivityEvent) {
            final ActivityEvent event = (ActivityEvent) be;
            fillActivityInfo(event, infoDocList);       //Fill the activity info to activitiesWithEvents & activitiesWithEventsOrdered

            ActivityInfoWithEventsDocument actinf = lookup(event);
            assert actinf != null;
            //TODO: Denis please fill the gaps
//            if (event instanceof ActivityEnabledEvent) {
//
//            }
//            if (event instanceof ActivityExecStartEvent) {
//
//            } else if (event instanceof ActivityExecEndEvent) {
//
//            }

        }

    }

    /**
     * fill the activitiesWithEvents and activitiesWithEventsOrdered using the infoDocList
     * Need to be done as event-info can be stored only in activitiesWithEvents and activitiesWithEventsOrdered
     * (parent class doesn't support for event-info management)
     *
     * @param be          Activity Event
     * @param infoDocList Activity Information Document
     */
    private void fillActivityInfo(ActivityEvent be, List<ActivityInfoDocument> infoDocList) {
        ActivityInfoDocument infoDoc = null;
        for (ActivityInfoDocument inf : infoDocList) {
            if (inf.getActivityInfo().getAiid().equals(String.valueOf(be.getActivityId()))) {
                infoDoc = inf;
            }
        }
        if (activitiesWithEvents.get(be.getActivityId()) == null) {
            activitiesWithEvents.put(be.getActivityId(), new ActivityInfoWithEventsDocument(infoDoc));
        }

        addActivitiesWithEventOrdered(infoDoc);
    }

//    private void completed(ActivityInfoWithEventsDocument ainf) {
//        if (removeCompleted) {
//            activitiesWithEventsOrdered.remove(ainf);
//            activitiesWithEvents.values().remove(ainf);
//        }
//    }

    /**
     * return the ActivityInfoWithEventsDocument object for a particular event
     * Then take it and fill it with particular event info
     * Note - this must be used after fillActivityInfo method, else aInfo will return a null ref.
     *
     * @param event Activity Event
     * @return updated element due to event input
     */
    private ActivityInfoWithEventsDocument lookup(ActivityEvent event) {
        ActivityInfoWithEventsDocument actEvtInfoDoc = activitiesWithEvents.get(event.getActivityId());
        ActivityInfoDocument aInfo = actEvtInfoDoc.getActivityInfoDoc();
        EventInfoListDocument aEventList = actEvtInfoDoc.getEventInfoList();

        if (aEventList == null) {
            aEventList = EventInfoListDocument.Factory.newInstance();
            actEvtInfoDoc.setEventInfoList(aEventList);
            aEventList = actEvtInfoDoc.getEventInfoList();
        }
        TEventInfo eventInfo;
        if (aEventList.getEventInfoList() == null) {
            TEventInfoList eventInfoList = aEventList.addNewEventInfoList();
            eventInfo = eventInfoList.addNewEventInfo();
        } else {
            eventInfo = aEventList.getEventInfoList().addNewEventInfo();
        }
        fillEventInfo(eventInfo, event);

        addActivitiesWithEventOrdered(event, new ActivityInfoWithEventsDocument(aInfo, aEventList));

        return activitiesWithEvents.get(event.getActivityId());
    }


    /**
     * Use to verify the new addition to activitiesWithEventsOrdered
     *
     * @param infoDoc Activity Information Document
     */
    private void addActivitiesWithEventOrdered(ActivityInfoDocument infoDoc) {
        boolean isExist = false;
        for (ActivityInfoWithEventsDocument anActivitiesWithEventsOrdered : activitiesWithEventsOrdered) {
            if (anActivitiesWithEventsOrdered.getActivityInfoDoc().getActivityInfo().
                    getAiid().equals(infoDoc.getActivityInfo().getAiid())) {
                isExist = true;
                break;
            }
        }
        if (!isExist) {
            activitiesWithEventsOrdered.add(new ActivityInfoWithEventsDocument(infoDoc));
        }
    }

    /**
     * Update activitiesWithEventsOrdered using infoWithEventsDoc
     *
     * @param event             Activity Event
     * @param infoWithEventsDoc ActivityInfoWithEventsDocument
     */
    private void addActivitiesWithEventOrdered(ActivityEvent event,
                                               ActivityInfoWithEventsDocument infoWithEventsDoc) {
        boolean isExist = false;
        for (ActivityInfoWithEventsDocument anActivitiesWithEventsOrdered :
                activitiesWithEventsOrdered) {
            if (anActivitiesWithEventsOrdered.getActivityInfoDoc().getActivityInfo().getAiid().
                    equals(infoWithEventsDoc.getActivityInfoDoc().getActivityInfo().getAiid())) {
                isExist = true;

                EventInfoListDocument aEventList = anActivitiesWithEventsOrdered.getEventInfoList();

                if (aEventList == null) {
                    aEventList = EventInfoListDocument.Factory.newInstance();
                    anActivitiesWithEventsOrdered.setEventInfoList(aEventList);
                    aEventList = anActivitiesWithEventsOrdered.getEventInfoList();
                }
                TEventInfo eventInfo;
                if (aEventList.getEventInfoList() == null) {
                    TEventInfoList eventInfoList = aEventList.addNewEventInfoList();
                    eventInfo = eventInfoList.addNewEventInfo();
                } else {
                    eventInfo = aEventList.getEventInfoList().addNewEventInfo();
                }
                fillEventInfo(eventInfo, event);
                break;
            }
        }
        if (!isExist) {
            activitiesWithEventsOrdered.add(infoWithEventsDoc);
        }
    }

    /**
     * Fill the event info from an event.
     *
     * @param info  Event Info
     * @param event ActivityEvent
     */
    private void fillEventInfo(TEventInfo info, ActivityEvent event) {

        info.setActivityType(event.getActivityType());
        info.setActivityDefinitionId(event.getActivityDeclarationId());  //TODO: verify this
        info.setActivityId(event.getActivityId());
        //info.setActivityFailureReason(event.getActivityF);
        info.setActivityName(event.getActivityName());
        //info.setActivityRecoveryAction(event.getA);
        //info.setCorrelationKey(event.get);
        //info.setCorrelationSet(event.get);
        //info.setExplanation(event.get);
        //info.setExpression(event.get);
        //info.setFault(event.get);
        //info.setFaultLineNumber(event.get);
        //info.setInstanceId(event.get);
        info.setLineNumber(event.getLineNo());
        //info.setMexId(event.get);
        info.setName(BpelEvent.eventName(event));
        //info.setNewState(event.get);
        //info.setNewValue(event.get);
        //info.setOldState(event.get);
        //info.setOperation(event.get);
        //info.setParentScopeId(event.getParentScopeId()); //TODO: this is not compatible
        //info.setPartnerLinkName(event.get);
        //info.setPortType(event.get);
        info.setProcessId(event.getProcessId());
        //info.setProcessType(event.get);
        //info.setResult(event.get);
        //info.setRootScopeDeclarationId(event.get);
        //info.setRootScopeId(event.get);
        //info.setScopeDefinitionId(event.get);
        info.setScopeId(event.getScopeId());
        info.setScopeName(event.getScopeName());
        //info.setSuccess(event.get);
        info.setTimestamp(convertDatetoCalendar(event.getTimestamp()));
        info.setType(event.getType().toString());
        //info.setVariableName(event.get);
    }

    private Calendar convertDatetoCalendar(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);

        return cal;
    }

    public List<ActivityInfoWithEventsDocument> getActivitiesWithEvents() {
        return activitiesWithEventsOrdered;
    }

    public void startup(Properties properties) {
        //Do nothing
    }

    public void shutdown() {
        //Do nothing
    }
}
