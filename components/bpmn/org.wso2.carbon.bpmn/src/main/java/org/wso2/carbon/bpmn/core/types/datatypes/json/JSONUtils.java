/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.wso2.carbon.bpmn.core.types.datatypes.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.wso2.carbon.bpmn.core.types.datatypes.json.api.JsonNodeObject;
import org.wso2.carbon.bpmn.core.types.datatypes.xml.api.XMLDocument;

import java.io.IOException;

/**
 * JSONUtils class holding util functions related to JSON processing
 */
public class JSONUtils {
    private static ObjectMapper objectMapper = null;

    /**
     * Function to parse string to JsonNodeObject
     *
     * @param jsonStr string containing json to parse
     * @return JsonNodeObject which is implementation of com.fasterxml.jackson.databind.JsonNode
     * @throws IOException If any IO errors occur.
     */
    public static JsonNodeObject parse(String jsonStr) throws IOException {

        if (objectMapper == null) {
            objectMapper = new ObjectMapper();
        }
        return new JsonNodeObject(objectMapper.readTree(jsonStr));
    }

    /**
     * Function to convert JsonNode to representing string
     * @param jsonObj JsonNode to convert to representing string
     * @return serialized jsonNode
     */
    public static String stringify (JsonNodeObject jsonObj) {
        if (jsonObj != null) {
            return jsonObj.toString();
        }
        return null;
    }
}
