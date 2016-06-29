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

package org.wso2.carbon.bpel.b4p.coordination.service;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axis2.databinding.types.URI;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bpel.b4p.coordination.configuration.CoordinationConfiguration;
import org.wso2.carbon.bpel.b4p.coordination.dao.HTCoordinationDAOConnection;
import org.wso2.carbon.bpel.b4p.coordination.dao.HTProtocolHandlerDAO;
import org.wso2.carbon.bpel.b4p.internal.B4PContentHolder;
import org.wso2.carbon.bpel.core.ode.integration.BPELServerImpl;
import org.wso2.carbon.bpel.skeleton.b4p.coordination.RegisterResponseType;
import org.wso2.carbon.bpel.skeleton.b4p.coordination.RegistrationServiceSkeletonInterface;
import org.wso2.carbon.bpel.skeleton.b4p.coordination.addressing.AttributedURI;
import org.wso2.carbon.bpel.skeleton.b4p.coordination.addressing.EndpointReferenceType;
import org.wso2.carbon.core.AbstractAdmin;

import java.util.concurrent.Callable;

/**
 * WS-Coordination Registration service. (Admin Service).
 */
public class RegistrationService extends AbstractAdmin implements RegistrationServiceSkeletonInterface {


    private static final Log log = LogFactory.getLog(RegistrationService.class);

    private static final String B4P_IDENTIFIER = "Identifier";
    private static final String B4P_NAMESPACE = "http://wso2.org/bps/b4p/coordination/";
    private static final String B4P_PREFIX = "b4p";

    private static final String COORDINATION_FAULT_MESSAGE = "Participant could not be registered.";
    private static final String COORDINATION_FAULT_SUB_CODE = "wscoor:CannotRegisterParticipant";

    private static final String HT_COORDINATION_PROTOCOL = "http://docs.oasis-open" +
            ".org/ns/bpel4people/ws-humantask/protocol/200803";

    @Override
    public RegisterResponseType registerOperation(URI uri, EndpointReferenceType endpointReferenceType, OMElement[]
            omElements) {
        if (!CoordinationConfiguration.getInstance().isRegistrationServiceEnabled()) {
            log.warn("Registration request is discarded. Registration service is disabled in this server");
            return null;
        }
        if (log.isDebugEnabled()) {
            log.debug("Registration request received.");
        }

        URI htIdentifierURI = null;
        try {
            htIdentifierURI = new URI(HT_COORDINATION_PROTOCOL);
        } catch (URI.MalformedURIException e) {
            log.error(e);
        }
        if (!htIdentifierURI.equals(uri)) {
            String errorMsg = "Received an invalid Protocol identifier: " + uri.toString();
            log.error(errorMsg);
            return null;
        }

        String participantProtocolService = "";
        if (endpointReferenceType != null && endpointReferenceType.getAddress() != null) {
            participantProtocolService = endpointReferenceType.getAddress().toString();
        } else {
            String errorMsg = "Received an invalid Participant Protocol Service";
            log.error(errorMsg);
        }

        String messageID = "";
        boolean foundB4PMessageID = false;
        if (omElements.length > 0) {
            for (OMElement omElement : omElements) {
                if (B4P_NAMESPACE.equals(omElement.getNamespace().getNamespaceURI()) && B4P_IDENTIFIER.equals
                        (omElement.getLocalName())) {
                    messageID = omElement.getText();
                    foundB4PMessageID = true;
                    break;
                }
            }
        }

        if (!foundB4PMessageID) {
            String errorMsg = "no B4P messageID received";
            log.error(errorMsg);
            return null;
        }

        if (log.isDebugEnabled()) {
            log.debug("Adding message ID: " + messageID + "-> " + participantProtocolService);
        }

        //Persisting data.
        try {
            persistData(messageID, participantProtocolService);
        } catch (Exception e) {
            log.error("Error occurred during persisting data", e);
            return null;
        }

        //Sending Dummy Response.
        RegisterResponseType responseType = new RegisterResponseType();
        EndpointReferenceType epr = new EndpointReferenceType();
        AttributedURI attributedURI = new AttributedURI();
        URI b4pProtocolHandlerURI;
        try {
            b4pProtocolHandlerURI = new URI(getB4PProtocolHandlerURI()); // Setting Dummy Address here.
            attributedURI.setAnyURI(b4pProtocolHandlerURI);
        } catch (URI.MalformedURIException e) {
            log.error("Error occurred while generating b4p protocol handler uri", e);
            return null;
        }
        epr.setAddress(attributedURI);

        OMFactory omFactory = OMAbstractFactory.getOMFactory();
        OMNamespace b4pOMNamespace = omFactory.createOMNamespace(B4P_NAMESPACE, B4P_PREFIX);

        responseType.setCoordinatorProtocolService(epr); //Dummy Endpoint
        OMElement identifierElement = omFactory.createOMElement(B4P_IDENTIFIER, b4pOMNamespace);
        identifierElement.addChild(omFactory.createOMText(identifierElement, messageID));
        responseType.addExtraElement(identifierElement);
        return responseType;
    }

    private HTProtocolHandlerDAO persistData(final String messageID, final String participantProtocolService) throws
            Exception {
        HTProtocolHandlerDAO htProtocolHandlerDAO = ((BPELServerImpl) B4PContentHolder.getInstance().getBpelServer())
                .getScheduler().execTransaction(new Callable<HTProtocolHandlerDAO>() {
                    @Override
                    public HTProtocolHandlerDAO call() throws Exception {
                        HTCoordinationDAOConnection daoConnection = B4PContentHolder.getInstance()
                                .getCoordinationController
                                ().getDaoConnectionFactory().getConnection();
                        return daoConnection.createCoordinatedTask(messageID, participantProtocolService);
                    }
                });
        return htProtocolHandlerDAO;
    }

    private String getB4PProtocolHandlerURI() {   //TODO implement this logic.
        // this is hardcoded, since we are not using B4P protocol handler at HumanTask Engine.
        return "http://example.com";
    }
}
