/*
 * Copyright (c) 2005-2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.bpel.core.ode.integration.config.analytics;

/**
 * Constants of Analytics
 */
public interface AnalyticsConstants {

    public static final String EMPTY = "";

    // Root level attributes.
    public static final String NAME = "name";
    public static final String TYPE = "type";

    // Connection element and its attributes.
    public static final String CONNECTION = "Connection";
    public static final String CONNECTION_RECEIVER_URL_SET = "receiverURLSet";
    public static final String CONNECTION_AUTH_URL_SET = "authURLSet";

    // Credential element and its attributes.
    public static final String CREDENTIAL = "Credential";
    public static final String CREDENTIAL_USER_NAME = "userName";
    public static final String CREDENTIAL_PASSWORD = "password";

    // Streams element and its sub elements.
    public static final String STREAMS = "Streams";
    public static final String STREAM = "Stream";

    public static final String STREAM_NAME = "name";
    public static final String STREAM_VERSION = "version";
    public static final String STREAM_NICKNAME = "nickName";
    public static final String STREAM_DESCRIPTION = "description";
    public static final String STREAM_DATA = "Data";
    public static final String STREAM_DATA_KEY = "Key";
    public static final String STREAM_DATA_KEY_NAME = "name";
    public static final String STREAM_DATA_KEY_TYPE = "type";
    public static final String STREAM_DATA_KEY_FROM = "From";
    public static final String STREAM_DATA_KEY_FROM_VARIABLE = "variable";
    public static final String STREAM_DATA_KEY_FROM_PART = "part";
    public static final String STREAM_DATA_KEY_FROM_QUERY = "Query";


}
