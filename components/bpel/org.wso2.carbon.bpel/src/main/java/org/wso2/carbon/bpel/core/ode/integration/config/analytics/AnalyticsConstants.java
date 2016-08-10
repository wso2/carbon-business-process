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

    String EMPTY = "";

    // Root level attributes.
    String NAME = "name";
    String TYPE = "type";

    // Connection element and its attributes.
    String CONNECTION = "Connection";
    String CONNECTION_RECEIVER_URL_SET = "receiverURLSet";
    String CONNECTION_AUTH_URL_SET = "authURLSet";

    // Credential element and its attributes.
    String CREDENTIAL = "Credential";
    String CREDENTIAL_USER_NAME = "userName";
    String CREDENTIAL_PASSWORD = "password";

    // Streams element and its sub elements.
    String STREAMS = "Streams";
    String STREAM = "Stream";

    String STREAM_NAME = "name";
    String STREAM_VERSION = "version";
    String STREAM_NICKNAME = "nickName";
    String STREAM_DESCRIPTION = "description";
    String STREAM_DATA = "Data";
    String STREAM_DATA_KEY = "Key";
    String STREAM_DATA_KEY_NAME = "name";
    String STREAM_DATA_KEY_TYPE = "type";
    String STREAM_DATA_KEY_DATA_TYPE = "dataType";
    String STREAM_DATA_KEY_FROM = "From";
    String STREAM_DATA_KEY_FROM_VARIABLE = "variable";
    String STREAM_DATA_KEY_FROM_PART = "part";
    String STREAM_DATA_KEY_FROM_QUERY = "Query";


}
