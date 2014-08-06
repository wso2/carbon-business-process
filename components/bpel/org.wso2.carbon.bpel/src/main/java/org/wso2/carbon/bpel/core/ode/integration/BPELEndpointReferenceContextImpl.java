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
package org.wso2.carbon.bpel.core.ode.integration;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.epr.EndpointFactory;
import org.apache.ode.bpel.epr.MutableEndpoint;
import org.apache.ode.bpel.iapi.EndpointReference;
import org.apache.ode.bpel.iapi.EndpointReferenceContext;
import org.apache.ode.utils.DOMUtils;
import org.w3c.dom.Element;

import javax.xml.namespace.QName;
import java.util.Map;

/**
 * Endpoint reference context: facililates the creation of
 * {@link EndpointReference} objects.
 *
 * In ODE, there are 4 types of {@link EndpointReference}s
 * <ul>
 *  <li>{@link org.apache.ode.bpel.epr.URLEndpoint}</li>
 *  <li>{@link org.apache.ode.bpel.epr.WSAEndpoint}</li>
 *  <li>{@link org.apache.ode.bpel.epr.WSDL11Endpoint}</li>
 *  <li>{@link org.apache.ode.bpel.epr.WSDL20Endpoint}</li>
 * </ul>
 */
public class BPELEndpointReferenceContextImpl implements EndpointReferenceContext {
    private static final Log log = LogFactory.getLog(BPELEndpointReferenceContextImpl.class);

    public EndpointReference resolveEndpointReference(Element element) {
        if(log.isDebugEnabled()){
            log.debug("Resolving endpoint reference "+ DOMUtils.domToString(element));
        }
        return EndpointFactory.createEndpoint(element);
    }

    public EndpointReference convertEndpoint(QName qName, Element element) {
        return EndpointFactory.convert(qName, element);
    }

    public Map getConfigLookup(EndpointReference endpointReference) {
        return ((MutableEndpoint)endpointReference).toMap();
    }
}