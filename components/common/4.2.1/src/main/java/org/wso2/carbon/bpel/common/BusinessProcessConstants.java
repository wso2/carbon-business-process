/*
 * Copyright (c) 2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.bpel.common;

/**
 * Constants shared among business-process components
 */
public final class BusinessProcessConstants {
    private BusinessProcessConstants() {
    }

    public static final String BPEL_PKG_ENDPOINT_CONFIG_NS = "http://wso2.org/bps/bpel/endpoint/config";

    public static final String ENDPOINT = "endpoint";

    public static final String ENDPOINTREF = "endpointReference";

    public static final String CONFIGURED_USING_BPEL_PKG_CONFIG_FILES = "confgiuredUsingBpelPkgFiles";

    public static final String SERVICE_DESC_LOCATION = "serviceDescriptionReference";
}
