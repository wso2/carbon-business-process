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
 *
 *
 */

package org.wso2.carbon.bpmn.core.types.datatypes.xml.api;


import org.wso2.carbon.bpmn.core.types.datatypes.xml.Utils;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;

/**
 * This class provide API to parse and stringify XML similar to JSON.parse, JSON.stringify in JS
 */
public class XML {

    /**
     * Function to parse string to XMLDocument Object
     * @param str string containing xml to parse
     * @return XMLDocument object which is implementation of org.w3c.dom.Document
     * @throws ParserConfigurationException
     * @throws IOException
     * @throws SAXException
     */
    public XMLDocument parse(String str) throws ParserConfigurationException, IOException, SAXException {

        return Utils.parse(str);

    }

    /**
     * Function to convert XMLDocument to String
     * @param xmlDoc XMLDocument object to convert
     * @return xml in string form
     * @throws TransformerException
     */
    public String stringify(XMLDocument xmlDoc) throws TransformerException {

        return Utils.stringify(xmlDoc);

    }

}
