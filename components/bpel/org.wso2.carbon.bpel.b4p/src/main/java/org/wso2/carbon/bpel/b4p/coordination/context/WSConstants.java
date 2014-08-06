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

package org.wso2.carbon.bpel.b4p.coordination.context;

public final class WSConstants {

    /** ws-addressing related constants */
    public static final String WS_ADDRESSING_NAMESPACE = "http://schemas.xmlsoap.org/ws/2004/08/addressing";
    public static final String WS_ADDRESSING_ADDRESS = "Address";
    public static final String WS_ADDRESSING_REFERENCE_PARAMETERS = "ReferenceParameters";

    /** ws-coordination related constants. */
    public static final String WS_COOR_NAMESPACE = "http://docs.oasis-open.org/ws-tx/wscoor/2006/06";
    public static final String WS_COOR_COORDINATION_CONTEXT = "CoordinationContext";
    public static final String WS_COOR_REGISTRATION_SERVICE = "RegistrationService";
    public static final String WS_COOR_COORDINATION_TYPE = "CoordinationType";
    public static final String WS_COOR_EXPIRES = "Expires";
    public static final String WS_COOR_IDENTIFIER = "Identifier";
    public static final String WS_COOR_DEFAULT_PREFIX ="wscoor";

    /** WS-HumanTask coordination related constants */
    public static final String WS_HT_COORDINATION_TYPE = "http://docs.oasis-open.org/ns/bpel4people/ws-humantask/protocol/200803";

    public static final String WS_HT_COORDINATION_PROTOCOL_NAMESPACE = "http://docs.oasis-open.org/ns/bpel4people/ws-humantask/protocol/200803";
    public static final String WS_HT_COORDINATION_PROTOCOL_DEFAULT_PREFIX = "htc";
    public static final String WS_HT_COORDINATION_PROTOCOL_EXIT = "exit";
    public static final String WS_HT_COORDINATION_PROTOCOL_EXIT_ACTION = "http://docs.oasis-open.org/ns/bpel4people/ws-humantask/protocol/200803/exit";
    public static final String WS_HT_COORDINATION_PROTOCOL_SKIPPED = "skipped";
    public static final String WS_HT_COORDINATION_PROTOCOL_FAULT = "fault";
    public static final String WS_HT_COORDINATION_PROTOCOL_EXIT_TASK_ID = "taskID";
}
