/*
 * Copyright (c), WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.bpel.analytics.publisher;


import javax.xml.namespace.QName;
import java.lang.String;

public final class AnalyticsPublisherConstants {
    public static final String ANALYTICS_PUBLISHER_NS = "http://wso2.org/bps/monitoring/publisher";

    public static final String SERVER = "Server";
    public static final String DEFAULT_SERVER_NAME = "WSO2BPS";

    public static final String ANALYTICS_KEY = "key";
    public static final String ANALYTICS_FROM = "from";
    public static final String ANALYTICS_CATEGORY = "category";
    public static final String NAME_ATTR = "name";
    public static final String TYPE_ATTR = "type";
    public static final String VARIABLE_ATTR = "variable";
    public static final String PART_ATTR = "part";

    public static final String EVENT_VALUE_TYPE_PAYLOAD = "payloadData";
    public static final String EVENT_VALUE_TYPE_META = "metaData";
    public static final String EVENT_VALUE_TYPE_CORRELATION ="correlationData";

    public static final String STREAM_NAME_ATTR = "streamName";
    public static final String STREAM_ID = "id";
    public static final String STREAM_VERSION = "version";
    public static final String STREAM_NICK_NAME = "nickName";
    public static final String STREAM_DESCRIPTION = "description";

    public static final String NON_RECOVERABLE_ERROR = "nonRecoverableError";
    public static final QName ANALYTICS_FAULT = new QName(AnalyticsPublisherConstants.ANALYTICS_PUBLISHER_NS,
            AnalyticsPublisherConstants.NON_RECOVERABLE_ERROR);



    public static final String CONFIG_RESOURCE_PATH =
            "repository/components/org.wso2.carbon.bpel.analytics.publisher/analytics_publisher_info";

    public static final String ANALYTICS_SERVER_URL = "ANALYTICS_SERVER_URL";
    public static final String ANALYTICS_SERVER_PORT = "ANALYTICS_SERVER_PORT";
    public static final String ANALYTICS_SERVER_USERNAME = "ANALYTICS_SERVER_USERNAME";
    public static final String ANALYTICS_SERVER_PASSWORD = "ANALYTICS_SERVER_PASSWORD";
    public static final String ANALYTICS_SERVER_ENABLE_SOCKET_TRANSPORT = "ENABLE_SOCKET_TRANSPORT";
    public static final String ANALYTICS_SERVER_ENABLE_HTTP_TRANSPORT = "ENABLE_HTTP_TRANSPORT";

    public static final String INSTANCE_ID = "instanceId";
    public static final String TENANT_ID = "tenantId";
    public static final String PROCESS_ID = "processId";
}
