/**
 *  Copyright (c) 2015 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.bpmn.rest.model.common;

import org.activiti.engine.query.QueryProperty;

import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class CorrelationQueryProperty implements QueryProperty {

    private static final long serialVersionUID = 1L;
    private static final Map<String, CorrelationQueryProperty> properties = new HashMap();
    public static final CorrelationQueryProperty PROCESS_INSTANCE_ID =
            new CorrelationQueryProperty("RES.ID_");

    private String name;

    public CorrelationQueryProperty(String name) {
        this.name = name;
        properties.put(name, this);
    }

    public String getName() {
        return this.name;
    }

    public static CorrelationQueryProperty findByName(String propertyName) {
        return (CorrelationQueryProperty) properties.get(propertyName);
    }
}
