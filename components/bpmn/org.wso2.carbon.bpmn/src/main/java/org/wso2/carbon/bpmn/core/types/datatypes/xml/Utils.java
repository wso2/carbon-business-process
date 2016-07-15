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
import org.apache.xerces.impl.Constants;
import org.apache.xerces.util.SecurityManager;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.wso2.carbon.bpmn.core.types.datatypes.xml.api.XML;
import org.wso2.carbon.bpmn.core.types.datatypes.xml.api.XMLDocument;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

/**
 * Util class holding util functions related to XML processing
 */
public class Utils {

    private static final Log log = LogFactory.getLog(XML.class);
    private static final int ENTITY_EXPANSION_LIMIT = 0;

    /**
     * Function to parse string to XMLDocument Object
     *
     * @param str string containing xml to parse
     * @return XMLDocument object which is implementation of org.w3c.dom.Document
     * @throws ParserConfigurationException
     * @throws IOException                  If any IO errors occur.
     * @throws SAXException                 If any parse errors occur.
     */
    public static XMLDocument parse(String str) throws ParserConfigurationException, IOException, SAXException {

        DocumentBuilderFactory docBuilderFactory = getSecuredDocumentBuilder();
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
     * Create DocumentBuilderFactory with the XXE and XEE prevention measurements.
     *
     * @return DocumentBuilderFactory instance
     */
    public static DocumentBuilderFactory getSecuredDocumentBuilder() {

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        dbf.setXIncludeAware(false);
        dbf.setExpandEntityReferences(false);
        try {
            dbf.setFeature(Constants.SAX_FEATURE_PREFIX + Constants.EXTERNAL_GENERAL_ENTITIES_FEATURE, false);
            dbf.setFeature(Constants.SAX_FEATURE_PREFIX + Constants.EXTERNAL_PARAMETER_ENTITIES_FEATURE, false);
            dbf.setFeature(Constants.XERCES_FEATURE_PREFIX + Constants.LOAD_EXTERNAL_DTD_FEATURE, false);
            dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

        } catch (ParserConfigurationException e) {
            log.error("Failed to load XML Processor Feature " + Constants.EXTERNAL_GENERAL_ENTITIES_FEATURE + " or " +
                    Constants.EXTERNAL_PARAMETER_ENTITIES_FEATURE + " or " + Constants.LOAD_EXTERNAL_DTD_FEATURE +
                    " or secure-processing.");
        }

        SecurityManager securityManager = new SecurityManager();
        securityManager.setEntityExpansionLimit(ENTITY_EXPANSION_LIMIT);
        dbf.setAttribute(Constants.XERCES_PROPERTY_PREFIX + Constants.SECURITY_MANAGER_PROPERTY, securityManager);

        return dbf;

    }

    /**
     * Function to convert XMLDocument to String
     *
     * @param xmlDoc XMLDocument object to convert
     * @return xml in string form
     * @throws TransformerException
     */
    public static String stringify(XMLDocument xmlDoc) throws TransformerException {

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

    public static Object evaluateXPath(Document doc, String xpathStr, QName returnType) throws XPathExpressionException {
        XPath xPath = XPathFactory.newInstance().newXPath();
        return xPath.evaluate(xpathStr, doc, returnType);
    }

    /**
     * Function to evaluate xpath. This will resolve the NodeList to Node if the result contains only one node
     * @param doc
     * @param xpathStr
     * @return
     * @throws XPathExpressionException
     */
    public static Object evaluateXPath(Document doc, String xpathStr) throws BPMNXmlException {

        Object result = null;
        NodeList outputObjList = null;
        try {
            outputObjList = (NodeList) evaluateXPath(doc, xpathStr, XPathConstants.NODESET);
            if (outputObjList.getLength() == 1) {
                //If there is only one node
                if (outputObjList.item(0) instanceof Text) {
                    return ((Text)outputObjList.item(0)).getWholeText();
                }
                return outputObjList.item(0);
            }
            return outputObjList;

        } catch (XPathExpressionException eLevel1) {
            //provided xpath cannot be evaluated to NodeList, so it may be evaluated to string
            try {

                if (log.isDebugEnabled()) {
                    log.debug("Since evaluating the xpath: "+ xpathStr+ " to NodeList failed, retrying to evaluate it to a STRING");
                }
                return evaluateXPath(doc, xpathStr, XPathConstants.STRING);

            } catch (XPathExpressionException eLevel2) {
                if (log.isDebugEnabled()) {
                    log.debug("Error occurred while evaluating xpath :"+ xpathStr+ " on xml: "+ doc.toString());
                }
                throw new BPMNXmlException("Error occurred while evaluating xpath :"+ xpathStr+ " due to error in xpath", eLevel2);
            }
        }
    }
}
