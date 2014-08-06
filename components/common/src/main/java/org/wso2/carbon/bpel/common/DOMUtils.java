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

package org.wso2.carbon.bpel.common;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xerces.jaxp.DocumentBuilderFactoryImpl;
import org.w3c.dom.*;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * DOM Utility Methods
 */
public final class DOMUtils {

    private static ThreadLocal<DocumentBuilder> builders = new ThreadLocal<DocumentBuilder>();
    private static final DocumentBuilderFactory documentBuilderFactory =
                                                                new DocumentBuilderFactoryImpl();
    private static Log log = LogFactory.getLog(DOMUtils.class);

    public static final String NS_URI_XMLNS = "http://www.w3.org/2000/xmlns/";
    

    static {
        initDocumentBuilderFactory();
    }

    // private constructor in-order to disable instantiation
    private DOMUtils() {}

    /**
     * Initialize the document-builder factory.
     */
    private static void initDocumentBuilderFactory() {
            documentBuilderFactory.setNamespaceAware(true);
    }

    public static Document newDocument() {
        DocumentBuilder db = getBuilder();
        return db.newDocument();
    }

    private static DocumentBuilder getBuilder() {
        DocumentBuilder builder = builders.get();
        if (builder == null) {
            synchronized (documentBuilderFactory) {
                try {
                    builder = documentBuilderFactory.newDocumentBuilder();
                    builder.setErrorHandler(new LoggingErrorHandler());
                } catch (ParserConfigurationException e) {
                    String errMsg = "Error occurred while building the document";
                    log.error(errMsg, e);
                    throw new RuntimeException(errMsg, e);
                }
            }
            builders.set(builder);
        }
        return builder;
    }

    public static void injectNamespaces(Element domElement, NSContext nscontext) {
        for (String uri : nscontext.getUriSet()) {
            String prefix = nscontext.getPrefix(uri);
            if (prefix == null || "".equals(prefix)) {
                domElement.setAttributeNS(DOMUtils.NS_URI_XMLNS, "xmlns", uri);
            }
            else {
                domElement.setAttributeNS(DOMUtils.NS_URI_XMLNS, "xmlns:"+ prefix, uri);
            }
        }
    }

    public static Element findChildByName(Element parent, QName name, boolean recurse) {
        if (parent == null) {
            throw new IllegalArgumentException("null parent");
        }
        if (name == null) {
            throw new IllegalArgumentException("null name");
        }

        NodeList nl = parent.getChildNodes();
        for (int i = 0; i < nl.getLength(); ++i) {
            Node c = nl.item(i);
            if(c.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }
            // For a reason that I can't fathom, when using in-mem DAO we actually get elements with
            // no localname.
            String nodeName = c.getLocalName() != null ? c.getLocalName() : c.getNodeName();
            if (new QName(c.getNamespaceURI(),nodeName).equals(name)) {
                return (Element) c;
            }
        }

        if(recurse) {
            NodeList cnl = parent.getChildNodes();
            for (int i = 0; i < cnl.getLength(); ++i) {
                Node c = cnl.item(i);
                if(c.getNodeType() != Node.ELEMENT_NODE) {
                    continue;
                }
                Element result = findChildByName((Element)c, name, recurse);
                if(result != null) {
                    return result;
                }
            }
        }
        return null;
    }

    public static Node findChildNode(Element parent, short nodeType, boolean recurse) {
        if (parent == null) {
            throw new IllegalArgumentException("null parent");
        }

        NodeList nl = parent.getChildNodes();
        for (int i = 0; i < nl.getLength(); ++i) {
            Node c = nl.item(i);
            if (c.getNodeType() != nodeType) {
                continue;
            }
            return c;
        }

        if (recurse) {
            NodeList cnl = parent.getChildNodes();
            for (int i = 0; i < cnl.getLength(); ++i) {
                Node c = cnl.item(i);
                if (c.getNodeType() != Node.ELEMENT_NODE) {
                    continue;
                }
                Node result = findChildNode((Element)c, nodeType, recurse);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }

}
