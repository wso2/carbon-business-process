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

package org.wso2.carbon.humantask.coordination.module.utils;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.soap.SOAPHeaderBlock;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.QName;


public class SOAPUtils {

    private static final Log log = LogFactory.getLog(SOAPUtils.class);

    private static final OMFactory omFactory = OMAbstractFactory.getOMFactory();

    /**
     * Namespaces for WS-Coordination, WS-Addressing, and BPS b4p
     */
    private static final OMNamespace wsCoorOMNamespace = omFactory.createOMNamespace(Constants.WS_COOR_NAMESPACE, Constants.WS_COOR_PREFIX);
    private static final OMNamespace wsaOMNamespace = omFactory.createOMNamespace(Constants.WS_A_NAMESPACE, Constants.WS_A_PREFIX);
    private static final OMNamespace b4pOMNamespace = omFactory.createOMNamespace(Constants.B4P_NAMESPACE, Constants.B4P_PREFIX);

    private static final QName coordinationIdentifierQName = new QName(Constants.WS_COOR_NAMESPACE, Constants.WS_COOR_COORDINATION_CONTEXT_IDENTIFIER);
    private static final QName coordinationTypeQName = new QName(Constants.WS_COOR_NAMESPACE, Constants.WS_COOR_COORDINATION_CONTEXT_COORDINATION_TYPE);
    private static final QName registrationServiceQName = new QName(Constants.WS_COOR_NAMESPACE, Constants.WS_COOR_COORDINATION_CONTEXT_REGISTRATION_SERVICE);
    private static final QName addressQName = new QName(Constants.WS_A_NAMESPACE, Constants.WS_A_ADDRESS);

    private static final QName b4pIdentifierQName = new QName(Constants.B4P_NAMESPACE, Constants.WS_COOR_COORDINATION_CONTEXT_IDENTIFIER);

    public static String getCoordinationIdentifier(SOAPHeaderBlock headerBlock) {
        return headerBlock.getFirstChildWithName(coordinationIdentifierQName).getText();
    }

    public static String getCoordinationType(SOAPHeaderBlock headerBlock) {
        return headerBlock.getFirstChildWithName(coordinationTypeQName).getText();
    }

    public static String getRegistrationService(SOAPHeaderBlock headerBlock) {
        OMElement registrationElement = headerBlock.getFirstChildWithName(registrationServiceQName);

        return registrationElement.getFirstChildWithName(addressQName).getText();
    }

    /**
     * Generating Registration Service request. e.g
     * <ns:Register  xmlns:ns="http://docs.oasis-open.org/ws-tx/wscoor/2006/06"
     * xmlns:add="http://schemas.xmlsoap.org/ws/2004/08/addressing"
     * xmlns:b4p="http://wso2.org/bps/b4p/coordination/">
     * <ns:ProtocolIdentifier>http://docs.oasis-open.org/ns/bpel4people/ws-humantask/protocol/200803</ns:ProtocolIdentifier>
     * <ns:ParticipantProtocolService>
     * <add:Address>http://localhost:8080/service/registratoin</add:Address>
     * </ns:ParticipantProtocolService>
     * <b4p:Identifier>f24b2f36-03dd-11e3-a03f-f23c91aec05e</b4p:Identifier>
     * </ns:Register>
     *
     * @param identifier
     * @return
     */
    public static OMElement getRegistrationPayload(String identifier, String protocolHandler) {
        OMElement registerElement = omFactory.createOMElement(Constants.WS_COOR_REGISTER, wsCoorOMNamespace);

        //Adding ProtocolIdentifier
        OMElement protocolIdentifierElement = omFactory.createOMElement(Constants.WS_COOR_REGISTER_PROTOCOL_IDENTIFIER, wsCoorOMNamespace);
        protocolIdentifierElement.addChild(omFactory.createOMText(protocolIdentifierElement, Constants.WS_HT_COORDINATION_TYPE));
        registerElement.addChild(protocolIdentifierElement);

        // Adding ParticipantProtocolService/Address
        OMElement participantProtocolServiceElement = omFactory.createOMElement(Constants.WS_COOR_REGISTER_PARTICIPANT_PROTOCOL_SERVICE, wsCoorOMNamespace);
        OMElement addressElement = omFactory.createOMElement(Constants.WS_A_ADDRESS, wsaOMNamespace);
        addressElement.addChild(omFactory.createOMText(addressElement, protocolHandler));
        participantProtocolServiceElement.addChild(addressElement);
        registerElement.addChild(participantProtocolServiceElement);

        // Adding Identifier
        OMElement identifierElement = omFactory.createOMElement(Constants.B4P_IDENTIFIER, b4pOMNamespace);
        identifierElement.addChild(omFactory.createOMText(identifierElement, identifier));
        registerElement.addChild(identifierElement);

        return registerElement;
    }

    public static boolean validateResponse(OMElement response, String originalID) {
        boolean foundMessageID = false;
        try {
            OMElement id = response.getFirstChildWithName(b4pIdentifierQName);
            if (originalID.equals(id.getText())) {
                foundMessageID = true;
            }
        } catch (Exception ex) {
            return false;
        }

        return foundMessageID;
    }
}
