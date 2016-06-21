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

package org.wso2.carbon.bpmn.core.types.datatypes.xml;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.wso2.carbon.bpmn.core.types.datatypes.xml.api.XML;
import org.wso2.carbon.bpmn.core.types.datatypes.xml.api.XMLDocument;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

public class Utils {

    private static final Log log = LogFactory.getLog(XML.class);

    /**
     * Function to parse string to XMLDocument Object
     * @param str string containing xml to parse
     * @return XMLDocument object which is implementation of org.w3c.dom.Document
     * @throws ParserConfigurationException
     * @throws IOException If any IO errors occur.
     * @throws SAXException If any parse errors occur.
     */
    public static XMLDocument parse (String str) throws ParserConfigurationException, IOException, SAXException {

        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = docBuilderFactory.newDocumentBuilder();

        InputStream inputStream = new ByteArrayInputStream(str.getBytes(StandardCharsets.UTF_8));
        Document doc = builder.parse(inputStream);
        if (doc != null) {
            if (log.isDebugEnabled()) {
                log.debug("Parsing to XMLDocument Success. Src string: " + str);
            }
            return new XMLDocument(doc);
        }
        return null;
    }

    /**
     * Function to convert XMLDocument to String
     * @param xmlDoc XMLDocument object to convert
     * @return xml in string form
     * @throws TransformerException
     */
    public static String stringify (XMLDocument xmlDoc) throws TransformerException {

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        StringWriter strWriter = new StringWriter();

        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        transformer.transform(new DOMSource(xmlDoc), new StreamResult(strWriter));

        if (log.isDebugEnabled()) {
            log.debug("XMLDocument to String : " + strWriter.getBuffer().toString());
        }

        return strWriter.getBuffer().toString();
    }
}
