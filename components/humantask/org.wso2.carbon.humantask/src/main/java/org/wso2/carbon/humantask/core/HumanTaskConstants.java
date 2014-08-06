/*
 * Copyright (c) 2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.humantask.core;

import javax.xml.namespace.QName;
import java.io.File;

/**
 * A placeholder class for human task constants used throughout the module.
 */
public final class HumanTaskConstants {
    private HumanTaskConstants() {
    }

    /** HumanTask file extension */
    public static final String HUMANTASK_FILE_EXT = ".ht";

    /** HumanTask config file name */
    public static final String HUMANTASK_CONFIG_FILE = "humantask.xml";

    /** XPath 2  */
    public static final String WSHT_EXP_LANG_XPATH20 = "urn:wsht:sublang:xpath2.0";

    /** HumanTask Repo directory name */
    public static final String HUMANTASK_REPO_DIRECTORY = "humantasks";

    /** HumanTask package temporary location */
    public static final String HUMANTASK_PACKAGE_TEMP_DIRECTORY = "tmp" +
            File.separator + "humantaskuploads";

    /** HumanTask package file extension */
    public static final String HUMANTASK_PACKAGE_EXTENSION = "zip";

    /** Registry repository root location for storing human task deployment units */
    public static final String HT_DEP_UNITS_REPO_LOCATION = "/humantask/deploymentunits/";

    /** BPEL4People correlation header  */
    public static final String B4P_CORRELATION_HEADER = "correlation";

    /** Bpel4People correlation header attribute */
    public static final String B4P_CORRELATION_HEADER_ATTRIBUTE = "taskid";

    /** BPEL4People namespace */
    public static final String B4P_NAMESPACE = "http://docs.oasis-open.org/ns/bpel4people/bpel4people/200803";

    /** Default pagination size */
	public static final int ITEMS_PER_PAGE = 20;

    /** */
    public static final String HUMANTASK_TASK_TYPE =  "humantaskType";

    /** HumanTask Cleanup job name */
    public static final String HUMANTASK_CLEANUP_JOB = "humantaskCleanupJob";

    /** The port off set identifier */
    public static final String CARBON_CONFIG_PORT_OFFSET_NODE = "Ports.Offset";

    /** The specification defines the default task priority to be set as 5 */
    public static final int DEFAULT_TASK_PRIORITY = 5;

    /** The default access type value for an attachment*/
    public static final String DEFAULT_ATTACHMENT_ACCESS_TYPE = "AnonymousAccessType";

    /** The default content category value for an attachment*/
    public static final String DEFAULT_ATTACHMENT_CONTENT_CATEGORY = "AnonymousContentCategory";

    public static final String ATTACHMENT_CONTENT_CATEGORY_MIME ="http://www.iana.org/assignments/media-types/";

    public static final String ATTACHMENT_CONTENT_CATEGORY_XML_SCHEMA = "http://www.w3.org/2001/XMLSchema";


    /** The default content type of the presentation desc. */
    public static final String PRESENTATION_DESC_CONTENT_TYPE = "text/plain";

    /** The log name to enable message tracing for humantask component */
    public static final String MESSAGE_TRACE = "org.wso2.carbon.humantask.messagetrace";

    public static final String SOAP_ENV_NS = "http://schemas.xmlsoap.org/soap/envelope/";

    public static final String JAVAX_WSDL_VERBOSE_MODE_KEY = "javax.wsdl.verbose";

    /** HumanTask Protocol and HumanTask Context related constants. See HT spec 8 and  8.4.1 sections*/
    public static final String HT_CONTEXT_NAMESPACE = "http://docs.oasis-open.org/ns/bpel4people/ws-humantask/context/200803";
    public static final String HT_CONTEXT_DEFAULT_PREFIX = "htc";
    public static final String HT_CONTEXT_REQUEST = "humanTaskRequestContext";
    public static final String HT_CONTEXT_IS_SKIPABLE = "isSkipable";
    public static final String HT_CONTEXT_PRIORITY = "priority";

    public static final String HT_PROTOCOL_NAMESPACE = "http://docs.oasis-open.org/ns/bpel4people/ws-humantask/protocol/200803";
    public static final String HT_PROTOCOL_DEFAULT_PREFIX = "htcp";
    public static final String HT_PROTOCOL_SKIPPED = "skipped";
    public static final String HT_PROTOCOL_FAULT = "fault";

    public static final String B4P_REGISTRATIONS_USERNAME_ALIAS = "B4P.RegistrationService.Username";
    public static final String B4P_REGISTRATIONS_PASSWORD_ALIAS = "B4P.RegistrationService.Password";
	

    public static final QName organizationalEntityQname = new QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803",
            "organizationalEntity");


    public static final QName groupQname = new QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803",
            "group");

    public static final QName userQname = new QName("http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803",
            "user");


    /*HumanTask Caching related constants*/
    public static final String HT_CACHE_MANAGER = "htCacheManager";
    public static final String HT_CACHE_USER_NAME_LIST = "hUserListCache";
    public static final String HT_CACHE_ROLE_NAME_LIST = "htRoleListCache";
    public static final String HT_CACHE_ROLE_NAME_LIST_FOR_USER = "RoleNameListForUser";
    public static final String HT_CACHE_USER_NAME_LIST_FOR_ROLE = "UserNameListForRole";

    //Default Cache expiry duration in seconds
    public static final int DEFAULT_CACHE_EXPIRY_DURATION = 30;
    
    /*Human task notification related constants*/
    public static final String RENDERING_TYPE_EMAIL="email";
    public static final String RENDERING_TYPE_SMS="sms";
    public static final String EMAIL_TO_TAG="to";
    public static final String EMAIL_CC_TAG="cc";
    public static final String EMAIL_BCC_TAG="bcc";
    public static final String EMAIL_SUBJECT_TAG="mailSubject";
    public static final String EMAIL_OR_SMS_BODY_TAG="body";
    public static final String SMS_RECIEVER_TAG="reciever";
    public static final String RENDERING_TAG="rendering";
    public static final String TRANSPORT_SMS="sms";
    
    public static final String SMS_IMPLCLASS="smsImplClass";
    public static final String SMS_COM_PORT="com_port";
    public static final String SMS_GATEWAY_ID="gateway_id";
    public static final String SMS_BAUD_RATE="baud_rate";
    public static final String SMS_DONGLE_MANUFATURER="manufacturer";
    public static final String SMS_DONGLE_MODEL="model";
    public static final String HTD_NAMESPACE="http://docs.oasis-open.org/ns/bpel4people/ws-humantask/200803";
    
    
    
}
