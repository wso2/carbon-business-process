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


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.wso2.carbon.bpmn.core.types.datatypes.json.BPMNJsonException;

import java.util.List;
import java.util.Map;

/**
 * This class contains API to create Json objects from different sources
 */
public class JsonBuilder {

    private ObjectMapper objectMapper;

    public JsonBuilder(ObjectMapper mapper) {
        this.objectMapper = mapper;
    }

    /**
     * Function to create JsonNode from Map<String, Object>
     * @param jsonMap Map that represent json object. Must be Map<String, Object>
     * @return created JsonNode
     * @throws BPMNJsonException
     */
    public JsonNode createJsonNodeFromMap (Map<String, Object> jsonMap) throws BPMNJsonException {
        return createJsonNode(jsonMap, objectMapper);
    }

    /**
     * Function to create JsonArray from List<Object>
     * @param jsonList List that represent json array. Must be List<Object>
     * @return returns created JsonArray
     * @throws BPMNJsonException
     */
    public ArrayNode createJsonArrayFromMap (List<Object> jsonList) throws BPMNJsonException {
        return createJsonNode(jsonList,objectMapper);
    }

    private JsonNode createJsonNode (Object object, ObjectMapper mapper) throws BPMNJsonException {
        if (object == null) {
            return mapper.getNodeFactory().nullNode();
        } else if (object instanceof Map) {
            Map<String, Object> map = (Map<String, Object>) object;
            return createJsonNode(map, mapper);
        } else if (object instanceof List) {
            return createJsonNode((List<Object>)object, mapper);
        } else if (object instanceof String) {
            return createJsonNode((String) object, mapper);
        } else if (object instanceof Integer) {
            return createJsonNode((Integer) object, mapper);
        } else if (object instanceof Boolean) {
            return createJsonNode((Boolean)object, mapper);
        } else if (object instanceof Float) {
            return createJsonNode((Float)object, mapper);
        } else if (object instanceof Long) {
            return createJsonNode((Long)object, mapper);
        } else if (object instanceof Number) {
            return createJsonNode(((Number)object).floatValue(), mapper);
        }  else {
            throw new BPMNJsonException("The object of type : "+ object.getClass().getName() +" cannot convert to JSON");
        }
    }

    private JsonNode createJsonNode (String str, ObjectMapper mapper) {
        return mapper.getNodeFactory().textNode(str);
    }

    private JsonNode createJsonNode (Map<String, Object> map, ObjectMapper mapper) throws BPMNJsonException {
        if (map != null) {
            ObjectNode objNode = mapper.getNodeFactory().objectNode();
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                objNode.put(entry.getKey(), createJsonNode(entry.getValue(), mapper));
            }
            return objNode;
        } else {
            //Create null node
            return mapper.getNodeFactory().nullNode();
        }
    }

    private JsonNode createJsonNode (Integer integer, ObjectMapper mapper) {
        return mapper.getNodeFactory().numberNode(integer);
    }

    private JsonNode createJsonNode (Boolean bool, ObjectMapper mapper) {
        return mapper.getNodeFactory().booleanNode(bool);
    }

    private JsonNode createJsonNode (Float floatValue, ObjectMapper mapper) {
        return mapper.getNodeFactory().numberNode(floatValue);
    }

    private JsonNode createJsonNode (Long longValue, ObjectMapper mapper) {
        return mapper.getNodeFactory().numberNode(longValue);
    }

    private ArrayNode createJsonNode (List<Object> list, ObjectMapper mapper) throws BPMNJsonException {
        if (list != null) {
            ArrayNode arrNode = mapper.getNodeFactory().arrayNode();
            for (Object obj : list) {
                arrNode.add(createJsonNode(obj,mapper));
            }
            return arrNode;
        } else {
            return null;
        }
    }
}
