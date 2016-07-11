/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.bpmn.core.types.datatypes.xml;


import org.activiti.engine.impl.scripting.Resolver;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bpmn.core.types.datatypes.xml.api.XML;

/**
 * Resolver to resolve "XML" api usage in JS scripts in script tasks
 */
public class XmlAPIResolver implements Resolver {

    private static final Log log = LogFactory.getLog(XmlAPIResolver.class);

    private static final String APIkey = "XML";
    private static final XML xmlAPIObj = new XML();


    @Override
    public boolean containsKey(Object key) {
        return APIkey.equals(key);
    }

    @Override
    public Object get(Object key) {

        if (APIkey.equals(key)) {
            return xmlAPIObj;
        }
        return null;
    }
}
