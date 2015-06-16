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

import java.util.HashMap;
import java.util.Map;

public class UnifiedEndpointTransport {

    /*i.e: http, nhttp, tcp etc*/
    private String transportType;

    private Map<String, String> transportProperties = new HashMap<String, String>();


    public void addTransportProperty(String propertyName, String propertyValue) {
        if (transportType != null) {
            transportProperties.put(propertyName, propertyValue);
        }
    }

    public Map<String, String> getTransportProperties() {
        return transportProperties;
    }

    public String getTransportType() {
        return transportType;
    }

    public void setTransportType(String transportType) {
        this.transportType = transportType;
    }
}
