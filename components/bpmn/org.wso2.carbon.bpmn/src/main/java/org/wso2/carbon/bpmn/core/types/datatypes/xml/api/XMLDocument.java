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


import org.w3c.dom.Attr;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Comment;
import org.w3c.dom.DOMConfiguration;
import org.w3c.dom.DOMException;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.EntityReference;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;
import org.w3c.dom.UserDataHandler;
import org.wso2.carbon.bpmn.core.types.datatypes.BPMNDataTypeException;
import org.wso2.carbon.bpmn.core.types.datatypes.xml.BPMNXmlException;
import org.wso2.carbon.bpmn.core.types.datatypes.xml.Utils;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;

/**
 * Wrap class wrapping Document object implementing org.w3c.dom.Document as WSO2 BPMN XML datatype adding new functions
 */
public class XMLDocument implements Document {

    private Document doc;

    public XMLDocument(Document document) {
        this.doc = document;
    }

    //introduced functions for BPMN START

    /**
     * Function to evaluate xPath query and retrieve relevant element
     * @param xpathStr
     * @return Returns org.w3c.dom.NodeList if there are more than one elements in the result, Otherwise org.w3c.dom.Node Object is returned
     * @throws XPathExpressionException If expression cannot be evaluated
     */
    public Object xPath (String xpathStr) throws XPathExpressionException {

        XPath xPath = XPathFactory.newInstance().newXPath();
        //String output = xPath.evaluate(xpathStr, this.doc);
        NodeList outputObjList = (NodeList) xPath.evaluate(xpathStr, this.doc, XPathConstants.NODESET);

        if (outputObjList.getLength() == 1) {
            //If there is only one node, return the node instead of Node List
            //TODO : If there are any situation where Boolean, String, Double Object types, we have to cast them to relevant
            // type here and return.
            return (Node)outputObjList.item(0);
        }

        return outputObjList;
    }

    /**
     * Function to set/replace/update an object (String / Element) to matching the xPath provided. (In case new element
     * is added, this api will clone it and merge the new node to the target location pointed by xPath and return the new cloned node)
     * @param xPathStr xPath to the location object need to set
     * @param obj String or Node
     * @return returns the node get updated when the set object is String, or returns newly added Node in case object is Element
     * @throws XPathExpressionException If expression cannot be evaluated
     * @throws BPMNXmlException is thrown due to : Provided XPath and object does not match, provided object is not a Node or String
     *                                              result is NodeList, not a Text node or Element
     */
    public Node set (String xPathStr, Object obj) throws XPathExpressionException, BPMNXmlException {

        Object evalResult = xPath(xPathStr);
        if (evalResult instanceof Node) {
            Node targetNode = (Node) evalResult;

            if (obj instanceof String && targetNode instanceof Text) { //if string is provided, assume that user
                                                                        //need to replace the node value
                targetNode.setNodeValue((String)obj);
                //return updated Text Node
                return targetNode;
            } else if (obj instanceof Element && evalResult instanceof Element && targetNode.getParentNode() != null) { //if the user provides Node object,
                                                                        // assume that need to replace the target node
                Element targetParent = (Element) targetNode.getParentNode();
                //remove the target node
                targetParent.removeChild(targetNode);
                //add new node
                Node newNode = doc.importNode((Node)obj, true);
                targetParent.appendChild(newNode);
                //return new node
                return newNode;

            } else { //provided XPath and object to set does not match
                throw new BPMNXmlException("Provided XPath and object does not match");
            }

        } else if (evalResult instanceof NodeList) {

            /**/

            throw new BPMNXmlException("Error in provided xPath. Evaluation result is NodeList, not a Text node or Element");

        } else {
            throw new BPMNXmlException("Error in provided xPath. Evaluation result is not a Text node or Element");
        }

    }

    /**
     * Function to append child element to target element
     * @param xPathToParent xPath to parent node
     * @param element element to append
     * @return returns the node get appended or returns newly added Node in case object is Element
     * @throws XPathExpressionException If expression cannot be evaluated
     * @throws BPMNXmlException If no parent node found, the resulting NodeList empty,
     *                              Error in provided xPath. Evaluation result is not a Node or NodeList
     */
    public Element append (String xPathToParent, Element element) throws XPathExpressionException, BPMNXmlException {
        Object evalResult = xPath(xPathToParent);
        if (evalResult instanceof Element) {

            Node newNode = doc.importNode((Node)element, true);
            Node temp = ((Node) evalResult).appendChild(newNode);
            return (Element) newNode;

        } else if (evalResult instanceof NodeList) {

            NodeList resuldNodeList = (NodeList) evalResult;
            //if the result is node list, then that element is added as child of the first nodes parent
            if (resuldNodeList.getLength() > 0 && resuldNodeList.item(0).getParentNode() != null) {

                Node newNode = doc.importNode((Node)element, true);
                return (Element) resuldNodeList.item(0).getParentNode().appendChild((Node) newNode);

            } else {

                String exceptionMessage = "XML append failed due to : ";
                exceptionMessage = exceptionMessage + (resuldNodeList.getLength() > 0 ?
                                    (resuldNodeList.item(0).getParentNode() != null ? "Unknown Error" : "no parent node") :
                                    "the resulting NodeList empty");
                throw new BPMNXmlException(exceptionMessage);
            }
        } else {
            throw new BPMNXmlException("Error in provided xPath. Evaluation result is not a Node or NodeList");
        }
    }

    public Node createNode (String nodeStr) throws IOException, SAXException, ParserConfigurationException {
        XMLDocument document = Utils.parse(nodeStr);
        return document.getDocumentElement();
    }

    //introduced functions for BPMN END



    //Implemented functions
    @Override
    public DocumentType getDoctype() {
        return doc.getDoctype();
    }

    @Override
    public DOMImplementation getImplementation() {
        return doc.getImplementation();
    }

    @Override
    public Element getDocumentElement() {
        return doc.getDocumentElement();
    }

    @Override
    public Element createElement(String tagName) throws DOMException {
        return doc.createElement(tagName);
    }

    @Override
    public DocumentFragment createDocumentFragment() {
        return doc.createDocumentFragment();
    }

    @Override
    public Text createTextNode(String data) {
        return doc.createTextNode(data);
    }

    @Override
    public Comment createComment(String data) {
        return doc.createComment(data);
    }

    @Override
    public CDATASection createCDATASection(String data) throws DOMException {
        return doc.createCDATASection(data);
    }

    @Override
    public ProcessingInstruction createProcessingInstruction(String target, String data) throws DOMException {
        return doc.createProcessingInstruction(target, data);
    }

    @Override
    public Attr createAttribute(String name) throws DOMException {
        return doc.createAttribute(name);
    }

    @Override
    public EntityReference createEntityReference(String name) throws DOMException {
        return doc.createEntityReference(name);
    }

    @Override
    public NodeList getElementsByTagName(String tagname) {
        return doc.getElementsByTagName(tagname);
    }

    @Override
    public Node importNode(Node importedNode, boolean deep) throws DOMException {
        return doc.importNode(importedNode, deep);
    }

    @Override
    public Element createElementNS(String namespaceURI, String qualifiedName) throws DOMException {
        return doc.createElementNS(namespaceURI, qualifiedName);
    }

    @Override
    public Attr createAttributeNS(String namespaceURI, String qualifiedName) throws DOMException {
        return doc.createAttributeNS(namespaceURI, qualifiedName);
    }

    @Override
    public NodeList getElementsByTagNameNS(String namespaceURI, String localName) {
        return doc.getElementsByTagNameNS(namespaceURI, localName);
    }

    @Override
    public Element getElementById(String elementId) {
        return doc.getElementById(elementId);
    }

    @Override
    public String getInputEncoding() {
        return doc.getInputEncoding();
    }

    @Override
    public String getXmlEncoding() {
        return doc.getXmlEncoding();
    }

    @Override
    public boolean getXmlStandalone() {
        return doc.getXmlStandalone();
    }

    @Override
    public void setXmlStandalone(boolean xmlStandalone) throws DOMException {
        doc.setXmlStandalone(xmlStandalone);
    }

    @Override
    public String getXmlVersion() {
        return doc.getXmlVersion();
    }

    @Override
    public void setXmlVersion(String xmlVersion) throws DOMException {
        doc.setXmlVersion(xmlVersion);
    }

    @Override
    public boolean getStrictErrorChecking() {
        return doc.getStrictErrorChecking();
    }

    @Override
    public void setStrictErrorChecking(boolean strictErrorChecking) {
        doc.setStrictErrorChecking(strictErrorChecking);
    }

    @Override
    public String getDocumentURI() {
        return doc.getDocumentURI();
    }

    @Override
    public void setDocumentURI(String documentURI) {
        doc.getDocumentURI();
    }

    @Override
    public Node adoptNode(Node source) throws DOMException {
        return doc.adoptNode(source);
    }

    @Override
    public DOMConfiguration getDomConfig() {
        return doc.getDomConfig();
    }

    @Override
    public void normalizeDocument() {
        doc.normalizeDocument();
    }

    @Override
    public Node renameNode(Node n, String namespaceURI, String qualifiedName) throws DOMException {
        return doc.renameNode(n, namespaceURI, qualifiedName);
    }

    @Override
    public String getNodeName() {
        return doc.getNodeName();
    }

    @Override
    public String getNodeValue() throws DOMException {
        return doc.getNodeValue();
    }

    @Override
    public void setNodeValue(String nodeValue) throws DOMException {
        doc.setNodeValue(nodeValue);
    }

    @Override
    public short getNodeType() {
        return doc.getNodeType();
    }

    @Override
    public Node getParentNode() {
        return doc.getParentNode();
    }

    @Override
    public NodeList getChildNodes() {
        return doc.getChildNodes();
    }

    @Override
    public Node getFirstChild() {
        return doc.getFirstChild();
    }

    @Override
    public Node getLastChild() {
        return doc.getLastChild();
    }

    @Override
    public Node getPreviousSibling() {
        return doc.getPreviousSibling();
    }

    @Override
    public Node getNextSibling() {
        return doc.getNextSibling();
    }

    @Override
    public NamedNodeMap getAttributes() {
        return doc.getAttributes();
    }

    @Override
    public Document getOwnerDocument() {
        return doc.getOwnerDocument();
    }

    @Override
    public Node insertBefore(Node newChild, Node refChild) throws DOMException {
        return doc.insertBefore(newChild, refChild);
    }

    @Override
    public Node replaceChild(Node newChild, Node oldChild) throws DOMException {
        return doc.replaceChild(newChild, oldChild);
    }

    @Override
    public Node removeChild(Node oldChild) throws DOMException {
        return doc.removeChild(oldChild);
    }

    @Override
    public Node appendChild(Node newChild) throws DOMException {
        return doc.appendChild(newChild);
    }

    @Override
    public boolean hasChildNodes() {
        return doc.hasChildNodes();
    }

    @Override
    public Node cloneNode(boolean deep) {
        return doc.cloneNode(deep);
    }

    @Override
    public void normalize() {
        doc.normalize();
    }

    @Override
    public boolean isSupported(String feature, String version) {
        return doc.isSupported(feature, version);
    }

    @Override
    public String getNamespaceURI() {
        return doc.getNamespaceURI();
    }

    @Override
    public String getPrefix() {
        return doc.getPrefix();
    }

    @Override
    public void setPrefix(String prefix) throws DOMException {
        doc.setPrefix(prefix);
    }

    @Override
    public String getLocalName() {
        return doc.getLocalName();
    }

    @Override
    public boolean hasAttributes() {
        return doc.hasAttributes();
    }

    @Override
    public String getBaseURI() {
        return doc.getBaseURI();
    }

    @Override
    public short compareDocumentPosition(Node other) throws DOMException {
        return doc.compareDocumentPosition(other);
    }

    @Override
    public String getTextContent() throws DOMException {
        return doc.getTextContent();
    }

    @Override
    public void setTextContent(String textContent) throws DOMException {
        doc.setTextContent(textContent);
    }

    @Override
    public boolean isSameNode(Node other) {
        return doc.isSameNode(other);
    }

    @Override
    public String lookupPrefix(String namespaceURI) {
        return doc.lookupPrefix(namespaceURI);
    }

    @Override
    public boolean isDefaultNamespace(String namespaceURI) {
        return doc.isDefaultNamespace(namespaceURI);
    }

    @Override
    public String lookupNamespaceURI(String prefix) {
        return doc.lookupNamespaceURI(prefix);
    }

    @Override
    public boolean isEqualNode(Node arg) {
        return doc.isEqualNode(arg);
    }

    @Override
    public Object getFeature(String feature, String version) {
        return doc.getFeature(feature, version);
    }

    @Override
    public Object setUserData(String key, Object data, UserDataHandler handler) {
        return doc.setUserData(key, data, handler);
    }

    @Override
    public Object getUserData(String key) {
        return doc.getUserData(key);
    }
}
