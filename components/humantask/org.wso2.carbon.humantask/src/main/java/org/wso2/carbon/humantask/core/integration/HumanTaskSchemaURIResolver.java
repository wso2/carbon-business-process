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

package org.wso2.carbon.humantask.core.integration;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.InputSource;

import java.io.InputStream;
import java.net.URI;

/**
 * TODO move to a common bundle to share with bpel
 * URI resolver for the schemas in the human task deployment unit.
 */
public class HumanTaskSchemaURIResolver implements org.apache.ws.commons.schema.resolver.URIResolver {
    private static final Log log = LogFactory.getLog(HumanTaskSchemaURIResolver.class);

    //From the file system
    public InputSource resolveEntity(String targetNamespace, String schemaLocation, String baseUri) {
        if (log.isDebugEnabled()) {
            log.debug("resolveEntity: targetNamespace=" + targetNamespace + " schemaLocation=" +
                      schemaLocation + " baseUri=" + baseUri);
        }
        InputStream is;
        try {
            URI base = new URI("file:"+baseUri);
            URI uri = base.resolve(schemaLocation);
            is = uri.toURL().openStream();
            if (is == null) {
                log.error("Exception resolving entity: schemaLocation=" + schemaLocation +
                          " baseUri=" + baseUri);
                return null;
            }
            InputSource source = new InputSource(is);
            source.setSystemId(uri.toString());
            source.setPublicId(schemaLocation);
            return new InputSource(is);
        } catch (Exception e) {
            log.error("Exception resolving entity: schemaLocation=" + schemaLocation + " baseUri=" +
                      baseUri, e);
            return null;
        }
    }

    //With registry handler
//    public InputSource resolveEntity(String targetNamespace, String schmLocation, String baseUri) {
//        int begin = schmLocation.indexOf(HIConstants.HUMAN_INTERACTION_SCHEMA_LOCATION);
//        String schemaLocaion = schmLocation;
//
//        if (begin < 0) {
//            begin = schmLocation.indexOf(HIConstants.HUMAN_INTERACTION_WSDL_LOCATION);
//        }
//
//        if (begin < 0) {
//            String errMsg = "Invalid schema/wsdl location: " + schmLocation;
//            log.error(errMsg);
//            return null;
//        }
//
//        if (begin > 0) {
//            schmLocation = schmLocation.substring(begin);
//        }
//
//        if (log.isDebugEnabled()) {
//            log.debug("resolveEntity: targetNamespace=" + targetNamespace + " schmLocation=" + schmLocation
//                      + " baseUri=" + baseUri);
//        }
//        InputStream is;
//        try {
//            Registry governanceRegistry = HumanTaskServiceComponent.getRegistryService().getGovernanceSystemRegistry();
//            if (!governanceRegistry.resourceExists(schmLocation)) {
//                String errMsg = "Schema does not exist in: " + schmLocation;
//                log.error(errMsg);
//                return null;
//            }
//            is = governanceRegistry.get(schmLocation).getContentStream();
//            if (is == null) {
//                log.error("Exception resolving entity: schmLocation=" + schmLocation + " baseUri=" + baseUri);
//                return null;
//            }
//            InputSource source = new InputSource(is);
//            source.setSystemId(schemaLocaion);
//            source.setPublicId(schemaLocaion);
//            return new InputSource(is);
//        } catch (Exception e) {
//            log.error("Exception resolving entity: schmLocation=" + schmLocation + " baseUri=" + baseUri, e);
//            return null;
//        }
//    }

    //Without registry handler But from registry
//    public InputSource resolveEntity(String targetNamespace, String schemaLocation, String baseUri) {
//        if (log.isDebugEnabled()) {
//            log.debug("resolveEntity: targetNamespace=" + targetNamespace +
//                    " schemaLocation=" + schemaLocation + " baseUri=" + baseUri);
//        }
//        String location = baseUri.substring(0, baseUri.lastIndexOf("/") + 1) + schemaLocation;
//        InputStream is;
//        try {
//            Registry configRegistry = HumanTaskServiceComponent.getRegistryService().
//                    getConfigSystemRegistry();
//            if (!configRegistry.resourceExists(location)) {
//                String errMsg = "Schema does not exist in: " + location;
//                log.error(errMsg);
//                return null;
//            }
//            is = configRegistry.get(location).getContentStream();
//            if (is == null) {
//                log.error("Exception resolving entity: schemaLocation=" + location +
//                        " baseUri=" + baseUri);
//                return null;
//            }
//            InputSource source = new InputSource(is);
//            source.setSystemId(location);
//            source.setPublicId(location);
//            return new InputSource(is);
//        } catch (Exception e) {
//            log.error("Exception resolving entity: schemaLocation=" + location +
//                    " baseUri=" + baseUri, e);
//            return null;
//        }
//    }
}
