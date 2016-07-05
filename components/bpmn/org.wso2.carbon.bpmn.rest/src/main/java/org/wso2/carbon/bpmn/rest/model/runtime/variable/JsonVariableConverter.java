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

package org.wso2.carbon.bpmn.rest.model.runtime.variable;


import com.fasterxml.jackson.core.JsonProcessingException;
import org.activiti.engine.ActivitiIllegalArgumentException;
import org.wso2.carbon.bpmn.core.types.datatypes.json.api.JSON;
import org.wso2.carbon.bpmn.core.types.datatypes.json.api.JsonNodeObject;
import org.wso2.carbon.bpmn.rest.engine.variable.RestVariable;

import java.io.IOException;

/**
 * WSO2 JSON variable converter
 */
public class JsonVariableConverter implements RestVariableConverter {

    private static final String typeName = "json";
    private JSON json = new JSON();

    @Override
    public String getRestTypeName() {
        return typeName;
    }

    @Override
    public Class<?> getVariableType() {
        return JsonNodeObject.class;
    }

    @Override
    public Object getVariableValue(RestVariable result) {
        if (result.getValue() != null) {
            if (result.getType().equals(typeName) && result.getValue() instanceof String) {
                try {
                    return json.parse((String) result.getValue());
                } catch (IOException exception) {
                    //find specific reason for exception
                    if (exception instanceof JsonProcessingException) {
                        throw new ActivitiIllegalArgumentException("Converter cannot convert json variable content", exception);
                    } else {
                        throw new ActivitiIllegalArgumentException("Error occurred while parsing the json variable content", exception);
                    }
                }
            } else {
                throw new ActivitiIllegalArgumentException("Converter cannot convert " + result.getValue().getClass().getName()
                        + " type to JSON");
            }
        }
        return null;
    }

    @Override
    public void convertVariableValue(Object variableValue, RestVariable result) {
        if (variableValue != null) {
            if (!(variableValue instanceof JsonNodeObject)) {
                throw new ActivitiIllegalArgumentException("Converter can only convert json variables");
            }
            result.setValue(variableValue.toString());
        } else {
            result.setValue(null);
        }
    }
}
