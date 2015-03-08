/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.humantask.core.api.rendering;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import java.util.Iterator;

public class NamespaceResolver implements NamespaceContext{

    private static Log log = LogFactory.getLog(NamespaceResolver.class);
    private Document refDomDocument;

    public NamespaceResolver(Document referenceDoc) {
        this.refDomDocument = referenceDoc;
    }

    public String getNamespaceURI(String prefix) {
        if (prefix.equals(XMLConstants.DEFAULT_NS_PREFIX)) {
            return refDomDocument.lookupNamespaceURI(null);
        } else {
            return refDomDocument.lookupNamespaceURI(prefix);
        }
    }

    public String getPrefix(String namespaceURI) {
        return refDomDocument.lookupPrefix(namespaceURI);
    }

    public Iterator getPrefixes(String namespaceURI) {
        //TODO implement if needed
        return null;
    }
}
