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
 *//*



package org.wso2.carbon.bpmn.rest.engine.variable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
//import org.activiti.engine.ActivitiIllegalArgumentException;

import javax.xml.bind.annotation.*;

@XmlRootElement (name = "RestVariable")
@XmlAccessorType(XmlAccessType.FIELD)
public class RestVariable {


    public RestVariable(){}
    @XmlType
    @XmlEnum(String.class)
    public enum RestVariableScope {
        @XmlEnumValue("LOCAL") LOCAL,
        @XmlEnumValue("GLOBAL") GLOBAL
    }

    private String name;
    private String type;
    private RestVariableScope variableScope;
    private Object value;
    private String valueUrl;

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    @JsonSerialize(include= JsonSerialize.Inclusion.NON_NULL)
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }
    @JsonIgnore
    public RestVariableScope getVariableScope() {
        return variableScope;
    }
    public void setVariableScope(RestVariableScope variableScope) {
        this.variableScope = variableScope;
    }
    public Object getValue() {
        return value;
    }
    public void setValue(Object value) {
        this.value = value;
    }
    public String getScope() {
        String scope = null;
        if (variableScope != null) {
            scope = variableScope.name().toLowerCase();
        }
        return scope;
    }
    public void setScope(String scope) {
        setVariableScope(getScopeFromString(scope));
    }
    public void setValueUrl(String valueUrl) {
        this.valueUrl = valueUrl;
    }
    @JsonSerialize(include= JsonSerialize.Inclusion.NON_NULL)
    public String getValueUrl() {
        return valueUrl;
    }

    public static RestVariableScope getScopeFromString(String scope) {
        if (scope != null) {
            for (RestVariableScope s : RestVariableScope.values()) {
                if (s.name().equalsIgnoreCase(scope)) {
                    return s;
                }
            }
          //  throw new ActivitiIllegalArgumentException("Invalid variable scope: '" + scope + "'");
        } else {
            return null;
        }
    }
}
*/
