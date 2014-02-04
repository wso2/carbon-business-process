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

package org.wso2.carbon.humantask.coordination.module.utils;

import javax.xml.namespace.QName;

/**
 * HumanTask Coordination protocol module's constants.
 */
public final class Constants {

    /**
     * Module name
     */
    public static final String HUMANTASK_COORDINATION_MODULE_NAME = "htcoordination";

    /**
     * Service name of the HumanTask engine's Protocol handler
     */
    public static final String HUMANTASK_ENGINE_COORDINATION_PROTOCOL_HANDLER_SERVICE = "HumanTaskProtocolHandler";
    public static final String CARBON_ADMIN_SERVICE_CONTEXT_ROOT = "/services";

    /** ws-coordination related constants. */
    public static final String WS_COOR_NAMESPACE = "http://docs.oasis-open.org/ws-tx/wscoor/2006/06";
    public static final String WS_COOR_PREFIX = "wscoor";

    public static final String WS_COOR_COORDINATION_CONTEXT = "CoordinationContext";
    public static final String WS_COOR_COORDINATION_CONTEXT_REGISTRATION_SERVICE = "RegistrationService";
    public static final String WS_COOR_COORDINATION_CONTEXT_COORDINATION_TYPE = "CoordinationType";
    public static final String WS_COOR_COORDINATION_CONTEXT_EXPIRES = "Expires";
    public static final String WS_COOR_COORDINATION_CONTEXT_IDENTIFIER = "Identifier";

    public static final String WS_COOR_REGISTER = "Register";
    public static final String WS_COOR_REGISTER_PROTOCOL_IDENTIFIER = "ProtocolIdentifier";
    public static final String WS_COOR_REGISTER_PARTICIPANT_PROTOCOL_SERVICE = "ParticipantProtocolService";
    public static final String WS_COOR_REGISTERATION_ACTION = "http://docs.oasis-open.org/ws-tx/wscoor/2006/06/Register";

    /** b4p related constants */
    public static final String B4P_IDENTIFIER = "Identifier";
    public static final String B4P_NAMESPACE = "http://wso2.org/bps/b4p/coordination/";
    public static final String B4P_PREFIX = "b4p";

    /** ws-humantask related constants. */
    public static final String WS_HT_COORDINATION_TYPE = "http://docs.oasis-open.org/ns/bpel4people/ws-humantask/protocol/200803";

    /** ws-addressing related constants */
    public static final String WS_A_NAMESPACE = "http://schemas.xmlsoap.org/ws/2004/08/addressing";
    public static final String WS_A_PREFIX = "wsa";
    public static final String WS_A_ADDRESS = "Address";
    public static final String WS_A_REFERENCE_PARAMETER = "ReferenceParameters";

    public static QName registrationService = new QName(WS_COOR_NAMESPACE,WS_COOR_COORDINATION_CONTEXT_REGISTRATION_SERVICE);
    public static final String REGISTRATION_PORT = "RegistrationPort";
}
