/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.unifiedendpoint.core;

import org.apache.axis2.addressing.AddressingConstants;

import javax.xml.namespace.QName;

public final class UnifiedEndpointConstants {

    /*MediaType*/
    public static final String  WSO2_UNIFIED_ENDPOINT_MEDIA_TYPE = "application/vnd+wso2.unifiedendpoint";

    /*Namespace*/
    public static final String WSA_NS = "http://www.w3.org/2005/08/addressing";
    public static final String WSA_NS_PREFIX = "wsa";

    public static final String WSDL11_NS = "http://schemas.xmlsoap.org/wsdl/";
    public static final String WSDL11_NS_PREFIX = "wsdl11";

    public static final String UNIFIED_EPR = "EndpointReference";

    public static final String METADATA = "Metadata";
    public static final String METADATA_ID = "id";
    public static final String METADATA_DISCOVERY = "discovery";
    public static final String METADATA_UUID = "uuid";
    public static final String METADATA_SCOPE = "scope";
    public static final String METADATA_TYPE = "type";

    public static final String WSDL11_DEFINITIONS = "definitions";

    public static final String MESSAGE_OUTPUT = "messageOutput";
    public static final String MESSAGE_OUTPUT_FORMAT = "format";
    public static final String MESSAGE_OUTPUT_OPTIMIZE = "optimize";
    public static final String MESSAGE_OUTPUT_CHARSET = "charset";


    public static final String TRANSPORT = "transport";
    public static final String TRANSPORT_AUTHORIZATION_USERNAME = "authorization-username";
    public static final String TRANSPORT_AUTHORIZATION_PASSWORD = "authorization-password";

    public static final String TRANSPORT_OPTIONS = "transportOptions";
    public static final String TRANSPORT_OPTIONS_TYPE = "type";
    public static final String TRANSPORT_OPTIONS_VALUE = "value";
    public static final String TRANSPORT_OPTIONS_REQUEST_CHUNK = "request-chunk";
    public static final String TRANSPORT_OPTIONS_PROTOCOL_VERSION = "protocol-version";
    public static final String TRANSPORT_OPTIONS_REQUEST_GZIP = "request-gzip";
    public static final String TRANSPORT_OPTIONS_ACCEPT_GZIP = "accept-gzip";
    public static final String TRANSPORT_OPTIONS_CONNECTION_TIMEOUT = "connection-timeout";
    public static final String TRANSPORT_OPTIONS_SOCKET_TIMEOUT = "socket-timeout";
    public static final String TRANSPORT_OPTIONS_PROTOCOL_MAX_REDIRECTS = "protocol-max-redirects";
    public static final String TRANSPORT_OPTIONS_PROXY = "proxy";
    public static final String TRANSPORT_OPTIONS_PROXY_HOST = "host";
    public static final String TRANSPORT_OPTIONS_PROXY_PORT = "port";
    public static final String TRANSPORT_OPTIONS_PROXY_DOMAIN = "domain";
    public static final String TRANSPORT_OPTIONS_PROXY_USER = "user";
    public static final String TRANSPORT_OPTIONS_PROXY_PWD = "password";

    public static final String TIMEOUT = "timeout";
    public static final String TIMEOUT_DURATION = "duration";
    public static final String TIMEOUT_ACTION = "action";

    public static final String MONITORING = "monitoring";
    public static final String MONITORING_STATISTICS = "statistics";
    public static final String MONITORING_TRACE = "trace";

    public static final String ERROR_HANDLING = "errorHandling";
    public static final String ERROR_HANDLING_MARK_SUSPENSION = "markForSuspensionOnFailure";
    public static final String ERROR_HANDLING_MARK_SUSPENSION_ERROR_CODES = "errorCodes";
    public static final String ERROR_HANDLING_MARK_SUSPENSION_RETRIES = "retriesBeforeSuspension";
    public static final String ERROR_HANDLING_MARK_SUSPENSION_RETRY_DELAY = "retryDelay";

    public static final String ERROR_HANDLING_SUSPEND_ON_FAILURE = "suspendOnFailure";
    public static final String ERROR_HANDLING_SUSPEND_ON_FAILURE_ERROR_CODES = "errorCodes";
    public static final String ERROR_HANDLING_SUSPEND_ON_FAILURE_INITIAL_DURATION = "initialDuration";
    public static final String ERROR_HANDLING_SUSPEND_ON_FAILURE_PROGRESSION_FACTOR = "progressionFactor";
    public static final String ERROR_HANDLING_SUSPEND_ON_FAILURE_MAXIMUM_DURATION = "maximumDuration";

    public static final String QOS = "qos";
    public static final String QOS_POLICY = "policy";
    public static final String QOS_VERSION = "version";
    public static final String QOS_ENABLE_RM = "enableRM";
    public static final String QOS_ENABLE_WS_SEC = "enableWsSec";
    public static final String QOS_ENABLE_ADDRESSING = "enableAddressing";
    public static final String QOS_ENABLE_ADDRESSING_SEPARATE_LISTENER = "separateListener";
    public static final String QOS_ENABLE_ADDRESSING_REPLY_TO_ADDRESS = "ReplyTo";
    public static final String QOS_SECURITY = "security";
    public static final String QOS_SECURITY_USER_PWD_PAIR = "userpwdPair";
    public static final String QOS_SECURITY_USER_NAME = "username";
    public static final String QOS_SECURITY_PASSWORD = "password";

    public static final String SESSION = "session";
    public static final String SESSION_TYPE = "type";

    public static final String CLUSTER = "cluster";
    public static final String CLUSTER_MEMBERSHIP = "membership";
    public static final String CLUSTER_MEMBERSHIP_HANDLER = "membershipHandler";
    public static final String CLUSTER_MEMBER = "member";
    public static final String CLUSTER_MEMBER_URL = "url";
    public static final String CLUSTER_PROPERTIES = "properties";
    public static final String CLUSTER_PROPERTY = "property";
    public static final String CLUSTER_PROPERTY_NAME = "name";
    public static final String CLUSTER_PROPERTY_VALUE = "value";
    public static final String CLUSTER_LOAD_BALANCE = "loadBalance";
    public static final String CLUSTER_LOAD_BALANCE_POLICY = "policy";
    public static final String CLUSTER_LOAD_BALANCE_ALGORITHM = "algorithm";
    public static final String CLUSTER_FAIL_OVER = "failover";


    /*QNames*/
    public final static QName METADATA_Q = new QName(AddressingConstants.Final.WSA_NAMESPACE, METADATA);
    public final static QName METADATA_ID_Q = new QName(null, METADATA_ID);
    public final static QName METADATA_UUID_Q = new QName(null, METADATA_UUID);
    public final static QName METADATA_SCOPE_Q = new QName(null, METADATA_SCOPE);
    public final static QName METADATA_TYPE_Q = new QName(null, METADATA_TYPE);

    public final static QName METADATA_DISCOVERY_Q = new QName(null, METADATA_DISCOVERY);
    public final static QName TRANSPORT_Q = new QName(null, TRANSPORT);

    public final static QName METADATA_WSDL11_DEFINITIONS_Q = new QName(WSDL11_NS, WSDL11_DEFINITIONS);
    public final static QName MESSAGE_OUTPUT_Q = new QName(null, MESSAGE_OUTPUT);
    public final static QName MESSAGE_OUTPUT_FORMAT_Q = new QName(null, MESSAGE_OUTPUT_FORMAT);
    public final static QName MESSAGE_OUTPUT_OPTIMIZE_Q = new QName(null, MESSAGE_OUTPUT_OPTIMIZE);
    public final static QName MESSAGE_OUTPUT_CHARSET_Q = new QName(null, MESSAGE_OUTPUT_CHARSET);
    public final static QName TRANSPORT_OPTIONS_TYPE_Q = new QName(null, TRANSPORT_OPTIONS_TYPE);
    public static final QName TRANSPORT_AUTHORIZATION_USERNAME_Q = new QName(null, TRANSPORT_AUTHORIZATION_USERNAME);
    public static final QName TRANSPORT_AUTHORIZATION_PASSWORD_Q = new QName(null, TRANSPORT_AUTHORIZATION_PASSWORD);
    public static final QName TIMEOUT_Q = new QName(null, TIMEOUT);

    public static final String SECURE_VAULT_NS = "http://org.wso2.securevault/configuration";
    public static final String SECRET_ALIAS_ATTR_NAME = "secretAlias";

    public static final QName MONITORING_Q = new QName(null, MONITORING);
    public static final QName MONITORING_STATISTICS_Q = new QName(null, MONITORING_STATISTICS);
    public static final QName MONITORING_TRACE_Q = new QName(null, MONITORING_TRACE);

    public static final QName ERROR_HANDLING_Q = new QName(null, ERROR_HANDLING);
    public static final QName ERROR_HANDLING_MARK_SUSPENSION_Q = new QName(null, ERROR_HANDLING_MARK_SUSPENSION);


    public static final QName ERROR_HANDLING_MARK_SUSPENSION_ERROR_CODES_Q = new QName(null, ERROR_HANDLING_MARK_SUSPENSION_ERROR_CODES);
    public static final QName ERROR_HANDLING_MARK_SUSPENSION_RETRIES_Q = new QName(null, ERROR_HANDLING_MARK_SUSPENSION_RETRIES);
    public static final QName ERROR_HANDLING_MARK_SUSPENSION_RETRY_DELAY_Q = new QName(null, ERROR_HANDLING_MARK_SUSPENSION_RETRY_DELAY);
    public static final QName ERROR_HANDLING_SUSPEND_ON_FAILURE_Q = new QName(null, ERROR_HANDLING_SUSPEND_ON_FAILURE);
    public static final QName ERROR_HANDLING_SUSPEND_ON_FAILURE_ERROR_CODES_Q = new QName(null, ERROR_HANDLING_SUSPEND_ON_FAILURE_ERROR_CODES);
    public static final QName ERROR_HANDLING_SUSPEND_ON_FAILURE_INITIAL_DURATION_Q = new QName(null, ERROR_HANDLING_SUSPEND_ON_FAILURE_INITIAL_DURATION);
    public static final QName ERROR_HANDLING_SUSPEND_ON_FAILURE_PROGRESSION_FACTOR_Q = new QName(null, ERROR_HANDLING_SUSPEND_ON_FAILURE_PROGRESSION_FACTOR);
    public static final QName ERROR_HANDLING_SUSPEND_ON_FAILURE_MAXIMUM_DURATION_Q = new QName(null, ERROR_HANDLING_SUSPEND_ON_FAILURE_MAXIMUM_DURATION);

    public static final QName QOS_Q = new QName(null, QOS);
    public static final QName QOS_ENABLE_RM_Q = new QName(null, QOS_ENABLE_RM);
    public static final QName QOS_POLICY_Q = new QName(null, QOS_POLICY);    
    public static final QName QOS_ENABLE_WS_SEC_Q = new QName(null, QOS_ENABLE_WS_SEC);
    public static final QName QOS_ENABLE_ADDRESSING_Q = new QName(null, QOS_ENABLE_ADDRESSING);
    public static final QName QOS_VERSION_Q = new QName(null, QOS_VERSION);
    public static final QName QOS_ENABLE_ADDRESSING_SEPARATE_LISTENER_Q = new QName(null, QOS_ENABLE_ADDRESSING_SEPARATE_LISTENER);
    public static final QName QOS_ADDRESSING_REPLY_TO_Q = new QName(null, QOS_ENABLE_ADDRESSING_REPLY_TO_ADDRESS);
    public static final QName QOS_SECURITY_Q = new QName(null, QOS_SECURITY);
    public static final QName QOS_SECURITY_USER_PWD_PAIR_Q = new QName(null, QOS_SECURITY_USER_PWD_PAIR);
    public static final QName QOS_SECURITY_USER_NAME_Q = new QName(null, QOS_SECURITY_USER_NAME);
    public static final QName QOS_SECURITY_PASSWORD_Q = new QName(null, QOS_SECURITY_PASSWORD);
    

    public static final QName SESSION_Q = new QName(null, SESSION);
    public static final QName SESSION_TYPE_Q = new QName(null, SESSION_TYPE);

    public static final QName CLUSTER_Q = new QName(null, CLUSTER);
    public static final QName CLUSTER_MEMBERSHIP_Q = new QName(null, CLUSTER_MEMBERSHIP);
    public static final QName CLUSTER_MEMBERSHIP_HANDLER_Q = new QName(null, CLUSTER_MEMBERSHIP_HANDLER);
    public static final QName CLUSTER_MEMBER_Q = new QName(null, CLUSTER_MEMBER);
    public static final QName CLUSTER_MEMBER_URL_Q = new QName(null, CLUSTER_MEMBER_URL);
    public static final QName CLUSTER_LOAD_BALANCE_Q = new QName(null, CLUSTER_LOAD_BALANCE);
    public static final QName CLUSTER_LOAD_BALANCE_ALGORITHM_Q = new QName(null, CLUSTER_LOAD_BALANCE_ALGORITHM);
    public static final QName CLUSTER_LOAD_BALANCE_POLICY_Q = new QName(null, CLUSTER_LOAD_BALANCE_POLICY);

    public static final QName CLUSTER_FAIL_OVER_Q = new QName(null, CLUSTER_FAIL_OVER);
    public static final QName UNIFIED_EPR_Q = new QName(WSA_NS, UNIFIED_EPR);


    /*Axis2 MsgCtx related constants */
    public static final String FORMAT_POX = "pox";
    public static final String FORMAT_GET = "get";
    public static final String FORMAT_SOAP11 = "soap11";
    public static final String FORMAT_SOAP12 = "soap12";
    public static final String FORMAT_REST = "rest";

    public static final String OPTIMIZE_MTOM = "mtom";
    public static final String OPTIMIZE_SWA = "swa";

    public static final String ADDRESSING_VERSION_SUBMISSION = "submission";
    public static final String ADDRESSING_VERSION_FINAL = "final";

    public static final String TRANSPORT_TYPE_HTTP = "http";

    public static final String VIRTUAL_FILE = "file:";
    public static final String VIRTUAL_REG = "reg:";    
    public static final String VIRTUAL_GOV_REG = "gov:";
    public static final String VIRTUAL_CONF_REG = "conf:";
}
