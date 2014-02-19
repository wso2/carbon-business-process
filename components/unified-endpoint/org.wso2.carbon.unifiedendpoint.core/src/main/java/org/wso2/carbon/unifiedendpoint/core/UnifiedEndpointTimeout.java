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


public class UnifiedEndpointTimeout {
    private Map<String, String> timeOutProperties = new HashMap<String, String>();

    public void addTimeOutProperty(String key, String value) {
        timeOutProperties.put(key, value);
    }

    public String getTimeOutPropertyValue(String key) {
        return timeOutProperties.get(key);
    }

    public void removeTimeOutProperty(String key) {
        timeOutProperties.remove(key);
    }

    public Map<String, String> getTimeOutProperties() {
        return timeOutProperties;
    }

    public void setTimeOutProperties(Map<String, String> timeOutProperties) {
        this.timeOutProperties = timeOutProperties;
    }
}
