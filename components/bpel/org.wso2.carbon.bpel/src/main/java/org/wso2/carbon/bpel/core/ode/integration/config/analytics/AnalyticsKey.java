/*
 * Copyright (c) 2005-2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.bpel.core.ode.integration.config.analytics;

/**
 * Represents the From elements in the analytics publisher for BPEL
 * <p/>
 * <Data>
 * <Key name="NCName1" type="meta|correlation|payload" dataType="integer,long,double,float,string,bool">
 * <From variable="myVar" part="partName"/>
 * </Key>
 * <Key name="NCName2" type="meta|correlation|payload" dataType="integer,long,double,float,string,bool">
 * <From variable="myVar2"/>
 * </Key>
 * <Key name="NCName3" type="meta|correlation|payload" dataType="integer,long,double,float,string,bool">
 * <From variable="myVar3">
 * <Query>XPath expression</Query>
 * </From>
 * </Key>
 * <Key name="NCName4" type="meta|correlation|payload" dataType="integer,long,double,float,string,bool">
 * <From variable="myVar4" part="partName">
 * <Query>XPath expression</Query>
 * </From>
 * </Key>
 * <Key name="NCName5" type="meta|correlation|payload" dataType="integer,long,double,float,string,bool">
 * <From>XPath expression</From>
 * </Key>
 * </Data>
 */
public class AnalyticsKey {
    private String name;
    private AnalyticsKeyType type;
    private AnalyticsKeyDataType dataType;
    private String variable;
    private String part;
    private String query;
    private String expression;

    public AnalyticsKey(String name, AnalyticsKeyType type, AnalyticsKeyDataType dataType) {
        this.name = name;
        this.type = type;
        this.dataType = dataType;
    }

    public AnalyticsKey(String name, String variable, AnalyticsKeyType type, AnalyticsKeyDataType dataType) {
        this.name = name;
        this.variable = variable;
        this.type = type;
        this.dataType = dataType;
    }

    public AnalyticsKey(String name, String variable, String part, AnalyticsKeyType type, AnalyticsKeyDataType dataType) {
        this.name = name;
        this.variable = variable;
        this.part = part;
        this.type = type;
        this.dataType = dataType;
    }

    public AnalyticsKey(String name, String variable, String part, String query, AnalyticsKeyType type,
                        AnalyticsKeyDataType dataType) {
        this.name = name;
        this.variable = variable;
        this.part = part;
        this.query = query;
        this.type = type;
        this.dataType = dataType;
    }

    public String getVariable() {
        return variable;
    }

    public String getPart() {
        return part;
    }

    public String getQuery() {
        return query;
    }

    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

    public String getName() {
        return name;
    }

    public AnalyticsKeyType getType() {
        return type;
    }

    public AnalyticsKeyDataType getDataType() {
        return dataType;
    }

    /**
     * Analytics Key Types.
     */
    public enum AnalyticsKeyType {
        META, CORRELATION, PAYLOAD, NONE
    }

    public enum AnalyticsKeyDataType {
        INTEGER, LONG, DOUBLE, FLOAT, STRING, BOOL
    }
}
