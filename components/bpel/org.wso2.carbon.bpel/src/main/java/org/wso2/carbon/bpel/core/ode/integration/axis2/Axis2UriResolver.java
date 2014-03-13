/*
 * Copyright (c) 2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.bpel.core.ode.integration.axis2;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.axis2.hooks.ODEAxisService;
import org.xml.sax.InputSource;

import java.io.InputStream;
import java.net.URI;

/**
 * Schema location resolver for the wsdls in the BPEL package
 */
public class Axis2UriResolver implements org.apache.ws.commons.schema.resolver.URIResolver {
    private static final Log log = LogFactory.getLog(ODEAxisService.class);

    public InputSource resolveEntity(String targetNamespace, String schemaLocation, String baseUri) {
        if (log.isDebugEnabled()) {
            log.debug("resolveEntity: targetNamespace=" + targetNamespace + " schemaLocation=" +
                      schemaLocation + " baseUri=" + baseUri);
        }
        InputStream is;
        try {
            URI base = new URI(baseUri);
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
}
