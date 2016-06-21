/*
* Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.wso2.carbon.bpmn.core.types.datatypes.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jdk.nashorn.api.scripting.ScriptObjectMirror;
import jdk.nashorn.api.scripting.ScriptUtils;
import jdk.nashorn.internal.runtime.ScriptObject;
import org.activiti.engine.impl.util.JvmUtil;
import org.activiti.engine.impl.variable.JsonType;
import org.activiti.engine.impl.variable.ValueFields;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.wso2.carbon.bpmn.core.types.datatypes.BPMNDataTypeException;


public class ExtendedJsonType extends JsonType {

    private static final Log log = LogFactory.getLog(ExtendedJsonType.class);

    public ExtendedJsonType(int maxLength, ObjectMapper objectMapper) {
        super(maxLength, objectMapper);
    }

    @Override
    public boolean isCachable() {
        return false;
    }

    @Override
    public String getTypeName() {
        return "extendedJson";
    }

    @Override
    public boolean isAbleToStore(Object value) {
        if (value == null) {
            return true;
            //TODO NEED TO DISCUSS WHETHER TO CHECK FOR JAVA7, JAVA8 OR >JAVA7, >JAVA8
            //TODO WE CANNOT PROVIDE NATIVE JS JSON support of JAVA7
            //TODO ScriptObjectMirror not found in Java7
        } else if (JvmUtil.isJDK8() && value instanceof ScriptObjectMirror) {
            ScriptObjectMirror scriptObjectMirror = (ScriptObjectMirror) value;

            Object unwrappedObj = ScriptUtils.unwrap(scriptObjectMirror);
            if (unwrappedObj.getClass().getName().startsWith("jdk.nashorn.internal.scripts.JO")) {
                //The wrapped object may be an JS Json object
                //try to convert to JSON object
                try {
                    JSONObject convertedJObject = convertToJsonObject(scriptObjectMirror);
                    if (convertedJObject != null) {
                        return true;
                    }
                } catch (BPMNDataTypeException e) {
                    return false;
                }
            }
        } else if (JsonNode.class.isAssignableFrom(value.getClass())) {
            JsonNode jsonValue = (JsonNode)value;
            return jsonValue.toString().length() <= this.maxLength;
        } else {
            return false;
        }

        return false;
    }

    @Override
    public void setValue(Object obj, ValueFields valueFields) {
        try {
            if (obj != null) {
                if (obj instanceof ScriptObjectMirror) {
                    valueFields.setTextValue(convertToJsonObject((ScriptObjectMirror) obj).toString());
                } else {
                    valueFields.setTextValue(obj.toString());
                }
            } else {
                valueFields.setTextValue(null);
            }
        } catch (BPMNDataTypeException e) {
            log.error("Error occurred while setting scriptJson object", e);
        }
    }


    /**
     *  Function to convert ScriptObjectMirror object to JSONObject
     * @param jsObj target ScriptObjectMirror object
     * @return resulting JSONObject
     * @throws BPMNDataTypeException If the jsObj is not a JSON object
     */
    private JSONObject convertToJsonObject (ScriptObjectMirror jsObj) throws BPMNDataTypeException {

        return Utils.jsObjectToJsonObject((ScriptObject) ScriptUtils.unwrap(jsObj));

    }

}
