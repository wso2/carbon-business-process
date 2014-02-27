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

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.om.OMText;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Iterator;

/**
 * OM Utility Methods
 */
public final class OMUtils {

    // private constructor in-order to disable instantiation
    private OMUtils() {}

    public static Element toDOM(OMElement element) {
        return toDOM(element, DOMUtils.newDocument());
    }

    public static Element toDOM(OMElement element, Document doc) {
        return toDOM(element,doc,true);
    }

    public static Element toDOM(OMElement element, Document doc, boolean deepNS) {
        final Element domElement = doc.createElementNS(element.getQName().getNamespaceURI(),
                                                       element.getQName().getLocalPart());

        if (deepNS) {
            NSContext nscontext = new NSContext();
            buildNScontext(nscontext, element);
            DOMUtils.injectNamespaces(domElement,nscontext);
        } else {
            if (element.getAllDeclaredNamespaces() != null) {
                for (Iterator i = element.getAllDeclaredNamespaces(); i.hasNext(); ) {
                    OMNamespace omns = (OMNamespace)i.next();
                    if (omns.getPrefix().equals("")) {
                        domElement.setAttributeNS(DOMUtils.NS_URI_XMLNS, "xmlns",
                                                  omns.getNamespaceURI() == null ? "" :
                                                                            omns.getNamespaceURI());
                    }
                    else {
                        domElement.setAttributeNS(DOMUtils.NS_URI_XMLNS, "xmlns:"+ omns.getPrefix(),
                                                  omns.getNamespaceURI());
                    }
                }

            }
        }

        for (Iterator i = element.getAllAttributes(); i.hasNext();) {
            final OMAttribute attr = (OMAttribute) i.next();
            Attr newAttr;
            if (attr.getNamespace() != null) {
                newAttr = doc.createAttributeNS(attr.getNamespace().getNamespaceURI(),
                                                attr.getLocalName());
            } else {
                newAttr = doc.createAttributeNS(null,attr.getLocalName());
            }

            newAttr.appendChild(doc.createTextNode(attr.getAttributeValue()));
            domElement.setAttributeNodeNS(newAttr);

            // Case of qualified attribute values, we're forced to add corresponding namespace declaration manually...
            int colonIdx = attr.getAttributeValue().indexOf(":");
            if (colonIdx > 0) {
                OMNamespace attrValNs = element.findNamespaceURI(attr.getAttributeValue().
                                                                        substring(0, colonIdx));
                if(attrValNs!=null) {
                    domElement.setAttributeNS(DOMUtils.NS_URI_XMLNS, "xmlns:"+ attrValNs.getPrefix(),
                                              attrValNs.getNamespaceURI());
                }
            }
        }

        for (Iterator i = element.getChildren(); i.hasNext();) {
            OMNode omn = (OMNode) i.next();

            switch (omn.getType()) {
            case OMNode.CDATA_SECTION_NODE:
                domElement.appendChild(doc.createCDATASection(((OMText)omn).getText()));
                break;
            case OMNode.TEXT_NODE:
                domElement.appendChild(doc.createTextNode(((OMText)omn).getText()));
                break;
            case OMNode.ELEMENT_NODE:
                domElement.appendChild(toDOM((OMElement)omn,doc, false));
                break;
            }

        }

        return domElement;

    }

    private static void buildNScontext(NSContext nscontext, OMElement element) {
        if (element == null) {
            return;
        }

        if (element.getParent() instanceof OMElement) {
            buildNScontext(nscontext, (OMElement) element.getParent());
        }

        if (element.getAllDeclaredNamespaces() != null) {
            for (Iterator i=element.getAllDeclaredNamespaces(); i.hasNext(); ){
                OMNamespace omn = (OMNamespace) i.next();
                nscontext.register(omn.getPrefix(), omn.getNamespaceURI());
            }
        }

        if (element.getDefaultNamespace() != null) {
            nscontext.register("", element.getDefaultNamespace().getNamespaceURI());
        }
    }
}
