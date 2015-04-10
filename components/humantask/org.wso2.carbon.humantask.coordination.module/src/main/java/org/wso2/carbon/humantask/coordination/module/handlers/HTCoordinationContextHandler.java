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

package org.wso2.carbon.humantask.coordination.module.handlers;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMException;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAPHeader;
import org.apache.axiom.soap.SOAPHeaderBlock;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.engine.Handler;
import org.apache.axis2.handlers.AbstractHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.humantask.coordination.module.HumanTaskCoordinationException;
import org.wso2.carbon.humantask.coordination.module.internal.HTCoordinationModuleContentHolder;
import org.wso2.carbon.humantask.coordination.module.utils.Constants;
import org.wso2.carbon.humantask.coordination.module.utils.SOAPUtils;
import org.wso2.carbon.humantask.coordination.module.utils.ServiceUtils;
import org.wso2.carbon.humantask.core.configuration.HumanTaskServerConfiguration;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.util.Iterator;

/**
 * Axis2 Handler Class for handle HumanTaskCoordination Context
 */
public class HTCoordinationContextHandler extends AbstractHandler implements Handler {

    private static Log log = LogFactory.getLog(HTCoordinationContextHandler.class);

    private static HumanTaskServerConfiguration serverConfig = null;

    static {
        if (HTCoordinationModuleContentHolder.getInstance().getHtServer() != null) {
            serverConfig = HTCoordinationModuleContentHolder.getInstance().getHtServer().getServerConfig();
        }
    }


    public HTCoordinationContextHandler() throws HumanTaskCoordinationException {
        super();
    }

    @Override
    public InvocationResponse invoke(MessageContext messageContext) throws AxisFault {

        if (serverConfig == null || !serverConfig.isTaskRegistrationEnabled()) {
            return InvocationResponse.CONTINUE;
        }

        SOAPHeader soapHeader;
        try {
            soapHeader = messageContext.getEnvelope().getHeader();
        } catch (OMException ex) {
            throw new AxisFault("Error while extracting SOAP header", ex);
        }

        if (soapHeader == null) {
            if (log.isDebugEnabled()) {
                log.debug("No SOAP Header received. Continuing as an uncoordinated HumanTask.");
            }
            return InvocationResponse.CONTINUE;
        }

        Iterator headers = soapHeader.getChildElements();
        SOAPHeaderBlock coordinationHeaderBlock = null;
        // Searching for WS-Coor Coordination Context
        while (headers.hasNext()) {
            SOAPHeaderBlock hb = (SOAPHeaderBlock) headers.next();
            if (hb.getLocalName().equals(Constants.WS_COOR_COORDINATION_CONTEXT) &&
                    hb.getNamespace().getNamespaceURI().equals(Constants.WS_COOR_NAMESPACE)) {
                coordinationHeaderBlock = hb;
                break;
            }
        }

        if (coordinationHeaderBlock == null) {
            if (log.isDebugEnabled()) {
                log.debug("No coordination context received. Processing as an uncoordinated HumanTask.");
            }
            return InvocationResponse.CONTINUE;
        }

        //We have received a ws-coordination context. Now validate it for HT coordination type
        String coordinationType = SOAPUtils.getCoordinationType(coordinationHeaderBlock);
        if (!Constants.WS_HT_COORDINATION_TYPE.equals(coordinationType)) {
            // Found wrong coordination Type. We Only support http://docs.oasis-open.org/ns/bpel4people/ws-humantask/protocol/200803.
            // So we cannot allow message to go forward.
            String errorMsg = "Message aborted ! Invalid coordination type" + coordinationType +
                    " . Support only " + Constants.WS_HT_COORDINATION_TYPE;
            log.error(errorMsg);
            return InvocationResponse.ABORT;
        }

        if (log.isDebugEnabled()) {
            log.debug("HT coordination context received.");
        }

        String identifier = SOAPUtils.getCoordinationIdentifier(coordinationHeaderBlock);
        String registrationService = SOAPUtils.getRegistrationService(coordinationHeaderBlock);

        //validating values. These values cannot be empty
        if (identifier == null || identifier.isEmpty() || registrationService == null || registrationService.isEmpty()) {
            String errorMsg = "Message aborted ! Invalid coordination context parameters.";
            log.error(errorMsg);
            return InvocationResponse.ABORT;
        }

        //Service URL of the HumanTask Coordination Protocol Handler AdminService
        String humanTaskProtocolHandlerServiceURL;
        try {
            humanTaskProtocolHandlerServiceURL = ServiceUtils.getTaskProtocolHandlerURL(messageContext.getConfigurationContext());
        } catch (HumanTaskCoordinationException e) {
            String errorMsg = "Error while generating HumanTask engine's protocol Handler Service URL.";
            log.error(errorMsg);
            throw new AxisFault(e.getLocalizedMessage(), e);
        }

        // We are OK to invokeRegistrationService Registration service
        try {
            OMElement response = invokeRegistrationServiceUsingServiceClient(identifier, humanTaskProtocolHandlerServiceURL, registrationService);
            // We just discard registration response, since we are using CallBack service as TaskParent's Protocol Handler.
            // But we are validating it for successful completion.
            if (!SOAPUtils.validateResponse(response, identifier)) {
                String errorMsg = "Message aborted ! registration response validation failed.";
                log.error(errorMsg);
                return InvocationResponse.ABORT;
            }

            //successful coordination
            if (log.isDebugEnabled()) {
                log.debug("RegistrationResponse received. Task is successfully coordinated with Task parent.");
            }
        } catch (AxisFault e) {
            String errorMsg = "Error while invoking registration service";
            log.error(errorMsg);
            throw new AxisFault(e.getLocalizedMessage(), e);
        }
        return InvocationResponse.CONTINUE;
    }

    @Override
    public String getName() {
        return "HumanTask Coordination Protocol Handler";
    }


    private OMElement invokeRegistrationServiceUsingServiceClient(String identifier, String taskProtocolHandlerServiceURL, String registrationService) throws AxisFault {
        OMElement payload = SOAPUtils.getRegistrationPayload(identifier, taskProtocolHandlerServiceURL);
        Options options = new Options();
        options.setTo(new EndpointReference(registrationService)); // this sets the location of registration service
        options.setAction(Constants.WS_COOR_REGISTERATION_ACTION);
        options.setTransportInProtocol(org.apache.axis2.Constants.TRANSPORT_HTTPS);
        ServiceClient serviceClient = new ServiceClient();
        serviceClient.setOptions(options);

        //Setting basic auth headers. Reading those information using HT server config.

        String tenantDomain = MultitenantUtils.getTenantDomainFromUrl(registrationService);
        if (registrationService.equals(tenantDomain)) {
            //this is a Super tenant registration service
            CarbonUtils.setBasicAccessSecurityHeaders(serverConfig.getRegistrationServiceAuthUsername(), serverConfig.getRegistrationServiceAuthPassword(), serviceClient);
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Sending ws-coor Registration request to tenant domain: " + tenantDomain);
            }
            // Tenant's registration service
            CarbonUtils.setBasicAccessSecurityHeaders(
                    serverConfig.getRegistrationServiceAuthUsername() + "@" + tenantDomain,
                    serverConfig.getRegistrationServiceAuthPassword(),
                    serviceClient);
        }

        return serviceClient.sendReceive(payload);
    }

    private OMElement invokeRegistrationService(String identifier, String taskProtocolHandlerServiceURL, String registrationService) throws AxisFault {
        OMElement payload = SOAPUtils.getRegistrationPayload(identifier, taskProtocolHandlerServiceURL);

        SOAPFactory soap11Factory = OMAbstractFactory.getSOAP11Factory();
        MessageContext mctx = new MessageContext();

        if (mctx.getEnvelope() == null) {
            mctx.setEnvelope(soap11Factory.createSOAPEnvelope());
        }

        if (mctx.getEnvelope().getBody() == null) {
            soap11Factory.createSOAPBody(mctx.getEnvelope());
        }

        if (mctx.getEnvelope().getHeader() == null) {
            soap11Factory.createSOAPHeader(mctx.getEnvelope());
        }

        mctx.getEnvelope().getBody().addChild(payload);

//        //Setting basic auth headers. Reading those information using HT server config.
//        HumanTaskServerConfiguration serverConfig = HTCoordinationModuleContentHolder.getInstance().getHtServer().getServerConfig();
//        String tenantDomain = MultitenantUtils.getTenantDomainFromUrl(registrationService);
//        if (registrationService.equals(tenantDomain)) {
//            //this is a Super tenant registration service
//            if (log.isDebugEnabled()) {
//                log.debug("Sending Username" + serverConfig.getRegistrationServiceAuthUsername() + " - " + serverConfig.getRegistrationServiceAuthPassword());  //TODO REMOVE this
//            }
//            CarbonUtils.setBasicAccessSecurityHeaders(serverConfig.getRegistrationServiceAuthUsername(), serverConfig.getRegistrationServiceAuthPassword(), true, mctx);
//        } else {
//            if (log.isDebugEnabled()) {
//                log.debug("Sending ws-coor Registration request to tenant domain: " + tenantDomain);
//            }
//            // Tenant's registration service
//
//            String username = serverConfig.getRegistrationServiceAuthUsername() + "@" + tenantDomain;
//            String pass = serverConfig.getRegistrationServiceAuthPassword();
//            if (log.isDebugEnabled()) {
//                log.debug("Sending Username" + username + " - " + pass);  //TODO REMOVE this
//            }
//            CarbonUtils.setBasicAccessSecurityHeaders(
//                    username,
//                    pass,
//                    true,
//                    mctx);
//        }

        MessageContext responseMsgContext = ServiceUtils.invokeRegistrationService(mctx, registrationService);
        if (responseMsgContext.getEnvelope() != null) {
            if (responseMsgContext.getEnvelope().getBody() != null) {
                return responseMsgContext.getEnvelope().getBody();
            }
        }
        return null;
    }

}
