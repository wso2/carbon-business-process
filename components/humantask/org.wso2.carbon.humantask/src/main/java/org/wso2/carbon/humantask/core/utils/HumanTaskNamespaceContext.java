/*
 * Copyright (c) 2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import java.util.*;

public class HumanTaskNamespaceContext implements javax.xml.namespace.NamespaceContext {
    private static final Log log = LogFactory.getLog(HumanTaskNamespaceContext.class);

    private Map<String, String> prefixToUriMap = new HashMap<String, String>();

    public HumanTaskNamespaceContext() {
    }

    public String getNamespaceURI(String prefix) {
        return prefixToUriMap.get(prefix == null ? "" : prefix);
    }

    public String getPrefix(String namespaceURI) {
        for (String s : prefixToUriMap.keySet()) {
            if (prefixToUriMap.get(s).equals(namespaceURI)) {
                return s;
            }
        }
        return null;
    }

    public Iterator getPrefixes(String namespaceURI) {
        if (namespaceURI == null) {
            throw new IllegalArgumentException("Namespace URI null.");
        }
        ArrayList<String> matchedObjs = new ArrayList<String>();
        for (String s : prefixToUriMap.keySet()) {
            if (prefixToUriMap.get(s).equals(namespaceURI)) {
                matchedObjs.add(s);
            }
        }

        return matchedObjs.iterator();
    }

    public void register(String prefix, String nsUri) {
        String tPrefix = prefix;
        String tNsUri = nsUri;
        if (tPrefix == null) {
            tPrefix = "";
        }

        if (tNsUri == null) {
            tNsUri = "";
        }

        if (log.isTraceEnabled()) {
            log.trace("Registering Namespace: Prefix pair - " + tNsUri + ":" + tPrefix);
        }

        prefixToUriMap.put(tPrefix, tNsUri);
    }
}
