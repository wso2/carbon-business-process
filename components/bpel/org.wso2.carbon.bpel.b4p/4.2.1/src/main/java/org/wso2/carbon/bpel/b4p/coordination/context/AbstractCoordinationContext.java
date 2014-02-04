/*
 * Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.bpel.b4p.coordination.context;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axis2.addressing.EndpointReference;
import org.wso2.carbon.bpel.b4p.coordination.context.CoordinationContext;
import org.wso2.carbon.bpel.b4p.coordination.context.WSConstants;
import org.wso2.carbon.bpel.b4p.coordination.context.utils.EndPointReferenceFactory;

public abstract class AbstractCoordinationContext implements CoordinationContext {

    private String identifier;
    private String coordinationType;
    private long expires;
    private EndpointReference registrationService;
    private OMElement contextElement = null;

    public AbstractCoordinationContext(OMElement context) {
        this.contextElement = context;
        this.identifier = context.getFirstChildWithName(
                new QName(WSConstants.WS_COOR_NAMESPACE, WSConstants.WS_COOR_IDENTIFIER)).getText();
        this.coordinationType = context.getFirstChildWithName(
                new QName(WSConstants.WS_COOR_NAMESPACE, WSConstants.WS_COOR_COORDINATION_TYPE)).getText();
        OMElement registrationServiceElement = context.getFirstChildWithName(
                new QName(WSConstants.WS_COOR_NAMESPACE, WSConstants.WS_COOR_REGISTRATION_SERVICE));
        this.registrationService = EndPointReferenceFactory.getInstance().eprFromOMElement(registrationServiceElement);
    }

    public AbstractCoordinationContext(String identifier, String coordinationType, long expires, EndpointReference registrationService) {
        this.identifier = identifier;
        this.coordinationType = coordinationType;
        this.expires = expires;
        this.registrationService = registrationService;
    }

    public AbstractCoordinationContext(String identifier, String coordinationType, EndpointReference registrationService) {
        this.identifier = identifier;
        this.coordinationType = coordinationType;
        this.registrationService = registrationService;
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }

    @Override
    public String getCoordinationType() {
        return coordinationType;
    }

    @Override
    public long getExpires() {
        return expires;
    }

    @Override
    public EndpointReference getRegistrationService() {
        return registrationService;
    }

    @Override
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    @Override
    public void setCoordinationType(String coordinationType) {
        this.coordinationType = coordinationType;
    }

    @Override
    public void setExpires(long expires) {
        this.expires = expires;
    }

    @Override
    public void getRegistrationService(EndpointReference endpointReference) {
        this.registrationService = endpointReference;
    }

    @Override
    public OMElement toOM() {
        if (contextElement != null) {
            return contextElement;
        }
        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMNamespace wsCoordinationNS = factory.createOMNamespace(WSConstants.WS_COOR_NAMESPACE, WSConstants.WS_COOR_DEFAULT_PREFIX);
        OMElement contextElement = factory.createOMElement(WSConstants.WS_COOR_COORDINATION_CONTEXT, wsCoordinationNS);

        // Expires Element
        if (this.expires != 0) {
            OMElement expiresElement = factory.createOMElement(WSConstants.WS_COOR_EXPIRES, wsCoordinationNS);
            expiresElement.setText(Long.toString(this.expires));
            contextElement.addChild(expiresElement);
        }

        // Identifier Element
        OMElement identifierElement = factory.createOMElement(WSConstants.WS_COOR_IDENTIFIER, wsCoordinationNS);
        identifierElement.setText(this.identifier);
        contextElement.addChild(identifierElement);

        // CoordinationType Element
        OMElement coorTypeElement = factory.createOMElement(WSConstants.WS_COOR_COORDINATION_TYPE, wsCoordinationNS);
        coorTypeElement.setText(this.coordinationType);
        contextElement.addChild(coorTypeElement);

        //RegistrationJaxWSService Element
        OMElement registrationServiceElement = factory.createOMElement(WSConstants.WS_COOR_REGISTRATION_SERVICE, wsCoordinationNS);
        EndPointReferenceFactory.getInstance().omElementFromEPR(registrationService, registrationServiceElement);
        contextElement.addChild(registrationServiceElement);

        this.contextElement = contextElement;
        return contextElement;
    }
}
