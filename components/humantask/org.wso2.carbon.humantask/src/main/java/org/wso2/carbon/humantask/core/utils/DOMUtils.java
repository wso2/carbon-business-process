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

package org.wso2.carbon.humantask.core.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xerces.dom.DOMOutputImpl;
import org.apache.xerces.impl.Constants;
import org.apache.xerces.jaxp.DocumentBuilderFactoryImpl;
import org.apache.xml.serialize.DOMSerializerImpl;
import org.w3c.dom.*;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

/**
 * DOM Utility Methods
 */
public final class DOMUtils {

    private static ThreadLocal<DocumentBuilder> builders = new ThreadLocal();
    private static final DocumentBuilderFactory documentBuilderFactory =
            new DocumentBuilderFactoryImpl();
    private static Log log = LogFactory.getLog(DOMUtils.class);

    public static final String NS_URI_XMLNS = "http://www.w3.org/2000/xmlns/";


    static {
        initDocumentBuilderFactory();
    }

    private DOMUtils() {
    }

    /**
     * Initialize the document-builder factory.                 documentBuilderFactory = f;
     */
    private static void initDocumentBuilderFactory() {
//        DocumentBuilderFactory f = XMLParserUtils.getDocumentBuilderFactory();
//        DocumentBuilderFactory f = new DocumentBuilderFactoryImpl();
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
                    builder.setErrorHandler(new SAXLoggingErrorHandler());
                } catch (ParserConfigurationException e) {
                    log.error(e.getMessage(), e);
                    throw new RuntimeException(e);
                }
            }
            builders.set(builder);
        }
        return builder;
    }

    /**
     * Convert a DOM node to a stringified XML representation.
     *
     * @param node DOM Node
     * @return String
     */
    public static String domToString(Node node) {
        if (node == null) {
            throw new IllegalArgumentException("Cannot stringify null Node!");
        }

        String value;
        short nodeType = node.getNodeType();
        if (nodeType == Node.ELEMENT_NODE || nodeType == Node.DOCUMENT_NODE) {
            // serializer doesn't handle Node type well, only Element
            DOMSerializerImpl ser = new DOMSerializerImpl();
            ser.setParameter(Constants.DOM_NAMESPACES, Boolean.TRUE);
            ser.setParameter(Constants.DOM_WELLFORMED, Boolean.FALSE);
            ser.setParameter(Constants.DOM_VALIDATE, Boolean.FALSE);

            // create a proper XML encoding header based on the input document;
            // default to UTF-8 if the parent document's encoding is not accessible
            String usedEncoding = "UTF-8";
            Document parent = node.getOwnerDocument();
            if (parent != null) {
                String parentEncoding = parent.getXmlEncoding();
                if (parentEncoding != null) {
                    usedEncoding = parentEncoding;
                }
            }

            // the receiver of the DOM
            DOMOutputImpl out = new DOMOutputImpl();
            out.setEncoding(usedEncoding);

            // we write into a String
            StringWriter writer = new StringWriter(4096);
            out.setCharacterStream(writer);

            // out, ye characters!
            ser.write(node, out);
            writer.flush();

            // finally get the String
            value = writer.toString();
        } else {
            value = node.getNodeValue();
        }
        return value;
    }

    /**
     * Parse a String into a DOM.
     *
     * @param s DOCUMENTME
     * @return DOCUMENTME
     * @throws org.xml.sax.SAXException DOCUMENTME
     * @throws java.io.IOException      DOCUMENTME
     */
    public static Element stringToDOM(String s) throws SAXException, IOException {
        return parse(new InputSource(new StringReader(s))).getDocumentElement();
    }

    /**
     * Parse an XML document located using an {@link org.xml.sax.InputSource} using the
     * pooled document builder.
     *
     * @param inputSource Input Source to parse
     * @return Parsed document
     * @throws java.io.IOException      If an error occurred while reading from the input source
     * @throws org.xml.sax.SAXException if the content in the input source is invalid
     */
    public static Document parse(InputSource inputSource) throws SAXException, IOException {
        DocumentBuilder db = getBuilder();
        return db.parse(inputSource);
    }

//    public static Element findChildByName(Element parent, QName name) {
//        return findChildByName(parent, name, false);
//    }

//    public static Element findChildByName(Element parent, QName name, boolean recurse) {
//        if (parent == null) {
//            throw new IllegalArgumentException("null parent");
//        }
//        if (name == null) {
//            throw new IllegalArgumentException("null name");
//        }
//
//        NodeList nl = parent.getChildNodes();
//        for (int i = 0; i < nl.getLength(); ++i) {
//            Node c = nl.item(i);
//            if(c.getNodeType() != Node.ELEMENT_NODE) {
//                continue;
//            }
//            // For a reason that I can't fathom, when using in-mem DAO we actually get elements with
//            // no localname.
//            String nodeName = c.getLocalName() != null ? c.getLocalName() : c.getNodeName();
//            if (new QName(c.getNamespaceURI(),nodeName).equals(name)) {
//                return (Element) c;
//            }
//        }
//
//        if(recurse){
//            NodeList cnl = parent.getChildNodes();
//            for (int i = 0; i < cnl.getLength(); ++i) {
//                Node c = cnl.item(i);
//                if(c.getNodeType() != Node.ELEMENT_NODE) {
//                    continue;
//                }
//                Element result = findChildByName((Element)c, name, recurse);
//                if(result != null) {
//                    return result;
//                }
//            }
//        }
//        return null;
//    }

//    public static void copyNSContext(Element source, Element dest) {
//        Map<String, String> sourceNS = getParentNamespaces(source);
//        sourceNS.putAll(getMyNamespaces(source));
//        Map<String, String> destNS = getParentNamespaces(dest);
//        destNS.putAll(getMyNamespaces(dest));
//        // (source - dest) to avoid adding twice the same ns on dest
//        for (String pr : destNS.keySet()) sourceNS.remove(pr);
//
//        for (Map.Entry<String, String> entry : sourceNS.entrySet()) {
//            String prefix = entry.getKey();
//            String uri = entry.getValue();
//            if (prefix == null || "".equals(prefix))
//                dest.setAttributeNS(DOMUtils.NS_URI_XMLNS, "xmlns", uri);
//            else
//                dest.setAttributeNS(DOMUtils.NS_URI_XMLNS, "xmlns:"+ prefix, uri);
//        }
//    }
//
//     /**
//     * This method traverses the DOM and grabs namespace declarations
//     * on parent elements with the intent of preserving them for children.  <em>Note
//     * that the DOM level 3 document method {@link Element#getAttribute(java.lang.String)}
//     * is not desirable in this case, as it does not respect namespace prefix
//     * bindings that may affect attribute values.  (Namespaces in DOM are
//     * uncategorically a mess, especially in the context of XML Schema.)</em>
//     * @param el the starting element
//     * @return a {@link Map} containing prefix bindings.
//     */
//    public static Map<String, String> getParentNamespaces(Element el) {
//        HashMap<String,String> pref = new HashMap<String,String>();
//        Map<String,String> mine = getMyNamespaces(el);
//        Node n = el.getParentNode();
//        while (n != null && n.getNodeType() != Node.DOCUMENT_NODE) {
//            if (n instanceof Element) {
//                Element l = (Element) n;
//                NamedNodeMap nnm = l.getAttributes();
//                int len = nnm.getLength();
//                for (int i = 0; i < len; ++i) {
//                    Attr a = (Attr) nnm.item(i);
//                    if (isNSAttribute(a)) {
//                        String key = getNSPrefixFromNSAttr(a);
//                        String uri = a.getValue();
//                        // prefer prefix bindings that are lower down in the tree.
//                        if (pref.containsKey(key) || mine.containsKey(key)) continue;
//                        pref.put(key, uri);
//                    }
//                }
//            }
//            n = n.getParentNode();
//        }
//        return pref;
//    }
//
//    public static Map<String,String> getMyNamespaces(Element el) {
//        HashMap<String,String> mine = new HashMap<String,String>();
//        NamedNodeMap nnm = el.getAttributes();
//        int len = nnm.getLength();
//        for (int i=0; i < len; ++i) {
//            Attr a = (Attr) nnm.item(i);
//            if (isNSAttribute(a)) {
//                mine.put(getNSPrefixFromNSAttr(a),a.getValue());
//            }
//        }
//        return mine;
//    }
//
//    /**
//     * Test whether an attribute contains a namespace declaration.
//     * @param a an {@link Attr} to test.
//     * @return <code>true</code> if the {@link Attr} is a namespace declaration
//     */
//    public static boolean isNSAttribute(Attr a) {
//        assert a != null;
//        String s = a.getNamespaceURI();
//        return (s != null && s.equals(NS_URI_XMLNS));
//    }
//
//    /**
//     * Fetch the non-null namespace prefix from a {@link Attr} that declares
//     * a namespace.  (The DOM APIs will return <code>null</code> for a non-prefixed
//     * declaration.
//     * @param a the {@link Attr} with the declaration (must be non-<code>null</code).
//     * @return the namespace prefix or <code>&quot;&quot;</code> if none was
//     * declared, e.g., <code>xmlns=&quot;foo&quot;</code>.
//     */
//    public static String getNSPrefixFromNSAttr(Attr a) {
//        assert a != null;
//        assert isNSAttribute(a);
//        if (a.getPrefix() == null) {
//            return "";
//        }
//        return a.getName().substring(a.getPrefix().length()+1);
//    }

//    public static Element findChildElement(Element element) {
//        return (Element)findChildNode(element, Node.ELEMENT_NODE, false);
//    }

//    public static Node findChildNode(Element parent, short nodeType, boolean recurse) {
//        if (parent == null) {
//            throw new IllegalArgumentException("null parent");
//        }
//
//        NodeList nl = parent.getChildNodes();
//        for (int i = 0; i < nl.getLength(); ++i) {
//            Node c = nl.item(i);
//            if(c.getNodeType() != nodeType) {
//                continue;
//            }
//            return c;
//        }
//
//        if(recurse){
//            NodeList cnl = parent.getChildNodes();
//            for (int i = 0; i < cnl.getLength(); ++i) {
//                Node c = cnl.item(i);
//                if(c.getNodeType() != Node.ELEMENT_NODE) {
//                    continue;
//                }
//                Node result = findChildNode((Element)c, nodeType, recurse);
//                if(result != null) {
//                    return result;
//                }
//            }
//        }
//        return null;
//    }

    // We added this method as a dummy because we need to test the functionality.
    // We have an issue right now due to xsd:anyType usage returning Objects.
    //This method creates an element if the object cannot be cast to type Element.
    public static Element getElementFromObject(Object data) {
        Element dataElement = null;

        try {
            dataElement = (Element) data;
        } catch (Exception e) {
            log.warn("Object is not an Element. Trying to create the Element explicitly.", e);
            try {
                //We need a Document
                DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
                DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
                Document doc = docBuilder.newDocument();

                ////////////////////////
                //Creating the XML tree

                //create the root element and add it to the document
                dataElement = doc.createElement("data");
                doc.appendChild(dataElement);


                //add a text element to the child
                Text text = doc.createTextNode("no data provided");
                if (data != null) {
                    text = doc.createTextNode(data.toString());
                }

                dataElement.appendChild(text);
            } catch (Exception ex) {
                log.error("Error while creating the element.", ex);
            }
        }

        return dataElement;
    }
}
