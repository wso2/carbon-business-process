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
import org.activiti.engine.impl.variable.JsonType;
import org.activiti.engine.impl.variable.ValueFields;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bpmn.core.types.datatypes.json.api.JsonNodeObject;

/**
 * WSO2 JSON type implemented extending Activiti JsonType
 */
public class ExtendedJsonType extends JsonType {

    private static final Log log = LogFactory.getLog(ExtendedJsonType.class);

    public ExtendedJsonType(int maxLength, ObjectMapper objectMapper) {
        super(maxLength, objectMapper);
    }

    @Override
    public boolean isCachable() {
        return true;
    }

    @Override
    public String getTypeName() {
        return "WSO2Json";
    }

    @Override
    public boolean isAbleToStore(Object value) {
        if (value == null) {
            return true;
        }
        if (value instanceof JsonNodeObject && ((JsonNodeObject)value).unwrap() != null) {
            return value.toString().length() <= maxLength;
        }
        return false;
    }

    @Override
    public void setValue(Object obj, ValueFields valueFields) {
        valueFields.setTextValue((obj != null && obj instanceof JsonNodeObject &&
                                            ((JsonNodeObject)obj).unwrap() != null) ? obj.toString() : null);
    }

    @Override
    public Object getValue(ValueFields valueFields) {
        JsonNode jsonValue = null;
        if (valueFields.getTextValue() != null && valueFields.getTextValue().length() > 0) {
            try {
                jsonValue = objectMapper.readTree(valueFields.getTextValue());
                return new JsonNodeObject(jsonValue);
            } catch (Exception e) {
                //Since we cannot throw exception here, simply log the error and return null
                log.error("Error reading json variable " + valueFields.getName(), e);
            }
        }
        return jsonValue;
    }
}
