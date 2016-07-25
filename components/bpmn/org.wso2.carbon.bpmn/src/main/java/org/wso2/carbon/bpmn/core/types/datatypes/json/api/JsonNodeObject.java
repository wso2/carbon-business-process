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
import com.jayway.jsonpath.JsonPath;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bpmn.core.types.datatypes.json.BPMNJsonException;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * This is wrapper class for com.fasterxml.jackson.databind.JsonNode
 * this is to introduce WSO2 BPS specific functions : evalJsonPath()
 */
public class JsonNodeObject {

    private static final Log log = LogFactory.getLog(JsonNodeObject.class);
    private JsonNode jsonNode;

    /**
     * Constructor
     *
     * @param jsonNode JsonNode object that wrapped by JsonNodeObject
     */
    public JsonNodeObject(JsonNode jsonNode) {
        this.jsonNode = jsonNode;
    }

    /**
     * Function to evaluate jsonpath over JsonNodeObject
     * @param jsonPathStr jsonpath
     * @return returns the evaluation result. The returned Object can be a
     *                      com.fasterxml.jackson.databind.JsonNode (in case the result is json object)
     *                      com.fasterxml.jackson.databind.node.ArrayNode (in case the result is json array)
     *                      Or main primitive data types (String, Integer, Byte, Character, Short, Long, Float, Double, Boolean)
     *                 This function returns new Object representing the evaluation results, no a reference to a node
     * @throws IOException
     * @throws BPMNJsonException is thrown if the the resulting data type cannot be identified
     */
    public Object jsonPath(String jsonPathStr) throws IOException, BPMNJsonException {

        ObjectMapper mapper = new ObjectMapper();
        Map map = mapper.convertValue(jsonNode, Map.class);
        Object result = JsonPath.read(map, jsonPathStr);

        JsonBuilder builder = new JsonBuilder(mapper);
        if (result instanceof Map) {
            //If the result is a Map, then it should be a json object
            return builder.createJsonNodeFromMap((Map<String, Object>)result);

        } else if (result instanceof List) {
            //when result is a list, then it should be a json array
            return builder.createJsonArrayFromMap((List<Object>)result);

        } else if (result instanceof String || result instanceof Integer || result instanceof Byte ||
                        result instanceof Character || result instanceof Short || result instanceof Long ||
                        result instanceof Float || result instanceof Double || result instanceof Boolean) {

            //If result is primitive data type, then return it as it is
            return result;
        } else {

            //Un-filtered data type, considered as unknown types
            throw new BPMNJsonException("Unknown type data type: "+ result.getClass().getName()+ " resulted while evaluating json path");

        }

    }

    /**
     * Function to unwrap and return wrapped JsonNode
     * @return wrapped JsonNode
     */
    public JsonNode unwrap() {
        return jsonNode;
    }



    /**
    Set of useful (frequently used) function available JsonNode is wrapped below, to make ease of using wrapped Json type
    If required other functionality available in we can unwrap using
     org.wso2.carbon.bpmn.core.types.datatypes.json.api.JsonNodeObject.unwrap() and use
    **/
    public JsonNode findValue(String fieldName) {
        return jsonNode.findValue(fieldName);
    }

    public JsonNode findPath(String fieldName) {
        return jsonNode.findPath(fieldName);
    }

    public JsonNode findParent(String fieldName) {
        return jsonNode.findParent(fieldName);
    }

    public List<JsonNode> findValues(String fieldName, List<JsonNode> foundSoFar) {
        return jsonNode.findValues(fieldName, foundSoFar);
    }

    public List<String> findValuesAsText(String fieldName, List<String> foundSoFar) {
        return jsonNode.findValuesAsText(fieldName, foundSoFar);
    }

    public List<JsonNode> findParents(String fieldName, List<JsonNode> foundSoFar) {
        return jsonNode.findParents(fieldName, foundSoFar);
    }

    public JsonNode path(String fieldName) {
        return jsonNode.path(fieldName);
    }

    public JsonNode path(int index) {
        return jsonNode.path(index);
    }

    public String toString() {
        if (jsonNode != null) {
            return jsonNode.toString();
        }
        return "";
    }

    public boolean equals(Object obj) {
        return (obj instanceof JsonNode && jsonNode.equals(obj));
    }

    public JsonNode get(int index) {
        return jsonNode.get(index);
    }

    public JsonNode get(String fieldName) {
        return jsonNode.get(fieldName);
    }

    public boolean has(String fieldName) {
        return jsonNode.has(fieldName);
    }

    public boolean has(int index) {
        return jsonNode.has(index);
    }

    public int size() {
        return jsonNode.size();
    }

    public Iterator<JsonNode> elements() {
        return jsonNode.elements();
    }

    public Iterator<String> fieldNames() {
        return jsonNode.fieldNames();
    }

    public Iterator<Map.Entry<String, JsonNode>> fields() {
        return jsonNode.fields();
    }
}

