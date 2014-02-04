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

import org.apache.ode.bpel.pmapi.ActivityInfoDocument;
import org.apache.ode.bpel.pmapi.EventInfoListDocument;

/**
 * Handles the activityinfo with event details for particular activities
 * This is used as a data structure at the FE to log the process state
 */
public class ActivityInfoWithEventsDocument {
    private EventInfoListDocument eventInfoList;
    private ActivityInfoDocument activityInfoDoc;

    public EventInfoListDocument getEventInfoList() {
        return eventInfoList;
    }

    public ActivityInfoDocument getActivityInfoDoc() {
        return activityInfoDoc;
    }

    public ActivityInfoWithEventsDocument(ActivityInfoDocument activityInfoDoc) {
        this.activityInfoDoc = activityInfoDoc;
        this.eventInfoList = null;
    }

    public ActivityInfoWithEventsDocument(EventInfoListDocument eventInfoList) {
        this.eventInfoList = eventInfoList;
        this.activityInfoDoc = null;
    }

    public ActivityInfoWithEventsDocument(ActivityInfoDocument activityInfoDoc,
                                          EventInfoListDocument eventInfoList) {
        this.activityInfoDoc = activityInfoDoc;
        this.eventInfoList = eventInfoList;
    }

    public void setEventInfoList(EventInfoListDocument eventInfoList) {
        this.eventInfoList = eventInfoList;
    }
}
