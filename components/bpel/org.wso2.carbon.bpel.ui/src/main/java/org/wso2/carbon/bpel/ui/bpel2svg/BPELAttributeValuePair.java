/*
 * Copyright (c) 2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.bpel.ui.bpel2svg;

/**
 * KeyValue pair for a BPEL attribute
 */
public class BPELAttributeValuePair {
    private String attribute = null;
    private String value = null;

    /**
     * @param attribute type of the activity i.e. whether an activity is an instance of ASSIGN, SEQUENCE etc.
     * @param value     name of the activity
     */
    public BPELAttributeValuePair(String attribute, String value) {
        this.attribute = attribute;
        this.value = value;
    }

    /**
     * Gets the type of the activity i.e. whether an activity is an instance of ASSIGN, SEQUENCE etc.
     *
     * @return String with the type of the activity
     */
    public String getAttribute() {
        return attribute;
    }

    /**
     * Gets the name of the activity
     *
     * @return String with the name of the activity
     */
    public String getValue() {
        return value;
    }
}
