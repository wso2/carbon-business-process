/*
 * Copyright (c) 2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 */

package org.wso2.carbon.bpel.b4p.extension;

import javax.xml.namespace.QName;

/**
 * Holds the constants for BPEL4People component
 */
public final class BPEL4PeopleConstants {
    private BPEL4PeopleConstants() {}

    public static final String B4P_NAMESPACE = "http://docs.oasis-open.org/ns/bpel4people/bpel4people/200803";
    public static final String PEOPLE_ACTIVITY = "peopleActivity";
    public static final String PEOPLE_ACTIVITY_FILTER_NAME = "b4pFilter";
    public static final String B4P_CORRELATION_HEADER = "correlation";
    public static final String B4P_CORRELATION_HEADER_ATTRIBUTE = "taskid";

    //Elements and Attribute names of peopleActivity
    public static final String PEOPLE_ACTIVITY_NAME = "name";
    public static final String PEOPLE_ACTIVITY_INPUT_VARIABLE = "inputVariable";
    public static final String PEOPLE_ACTIVITY_OUTPUT_VARIABLE = "outputVariable";
    public static final String PEOPLE_ACTIVITY_IS_SKIPABLE = "isSkipable";
    public static final String PEOPLE_ACTIVITY_REMOTE_TASK = "remoteTask";
    public static final String PEOPLE_ACTIVITY_PARTNER_LINK = "partnerLink";
    public static final String PEOPLE_ACTIVITY_OPERATION = "operation";
    public static final String PEOPLE_ACTIVITY_RESPONSE_OPERATION = "responseOperation";
    public static final String PEOPLE_ACTIVITY_REMOTE_NOTIFICATION = "remoteNotification";
    public static final String PEOPLE_ACTIVITY_LOCAL_NOTIFICATION = "localNotification";
    public static final String PEOPLE_ACTIVITY_LOCAL_TASK = "localTask";

    public static final String ATTACHMENT_PROPAGATION_ACTIVITY = "attachmentPropagation";
    public static final String ATTACHMENT_PROPAGATION_ACTIVITY_FROM_PROCESS = "fromProcess";
    public static final String ATTACHMENT_PROPAGATION_ACTIVITY_TO_PROCESS = "toProcess";

    public static final String NON_RECOVERABLE_ERROR = "nonRecoverableError";
    public static final QName B4P_FAULT = new QName(BPEL4PeopleConstants.B4P_NAMESPACE,
            BPEL4PeopleConstants.NON_RECOVERABLE_ERROR);

    public static final String MESSAGE_TRACE = "org.wso2.carbon.bpel.messagetrace";

    /** Carbon service related constants */
    public static final String B4P_REGISTRATION_SERVICE = "RegistrationService";
    public static final String HT_ENGINE_COORDINATION_PROTOCOL_HANDLER_SERVICE = "HumanTaskProtocolHandler";
    public static final String CARBON_ADMIN_SERVICE_CONTEXT_ROOT = "/services";

    /** HumanTask Context related constants. See HT spec 8.4.1*/
    public static final String HT_CONTEXT_NAMESPACE = "http://docs.oasis-open.org/ns/bpel4people/ws-humantask/context/200803";
    public static final String HT_CONTEXT_DEFAULT_PREFIX = "htc";
    public static final String HT_CONTEXT_REQUEST = "humanTaskRequestContext";
    public static final String HT_CONTEXT_IS_SKIPABLE = "isSkipable";
}
