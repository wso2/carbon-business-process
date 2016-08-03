/*
 *     Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *     WSO2 Inc. licenses this file to you under the Apache License,
 *     Version 2.0 (the "License"); you may not use this file except
 *     in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing,
 *    software distributed under the License is distributed on an
 *    "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *    KIND, either express or implied.  See the License for the
 *    specific language governing permissions and limitations
 *    under the License.
 */

package org.wso2.carbon.bpmn.core.types.datatypes.json;


import org.activiti.engine.impl.scripting.Resolver;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bpmn.core.types.datatypes.json.api.JSON;
import org.wso2.carbon.bpmn.core.types.datatypes.xml.XmlAPIResolver;

/**
 * The JsonAPIResolver resolves "BPSJSON" used in JS script
 */
public class JsonAPIResolver implements Resolver{

    private static final Log log = LogFactory.getLog(JsonAPIResolver.class);

    private static final String APIkey = "JSONUtil";
    private static final JSON jsonAPIObject = new JSON();

    @Override
    public boolean containsKey(Object key) {
        return APIkey.equals(key);
    }

    @Override
    public Object get(Object obj) {
        if (APIkey.equals(obj)) {
            return jsonAPIObject;
        }
        return null;
    }

}
