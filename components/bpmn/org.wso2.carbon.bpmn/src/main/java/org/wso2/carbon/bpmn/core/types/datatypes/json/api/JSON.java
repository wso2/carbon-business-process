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

package org.wso2.carbon.bpmn.core.types.datatypes.json.api;


import org.wso2.carbon.bpmn.core.types.datatypes.json.JSONUtils;

import java.io.IOException;

/**
 * Json API for BPMN JSON data type support
 */
public class JSON {

    /**
     * Function to parse string to JsonNode
     * @param jsonStr string which needed to parse to JsonNode
     * @return parsed JsonNode
     * @throws IOException thrown when error occurred during parsing the string
     */
    public JsonNodeObject parse (String jsonStr) throws IOException {
        return JSONUtils.parse(jsonStr);
    }

    /**
     * Function to convert JsonNode to representing string
     * @param jsonObj JsonNode to convert to representing string
     * @return serialized jsonNode
     */
    public String stringify (JsonNodeObject jsonObj) {
        return JSONUtils.stringify(jsonObj);
    }
}
