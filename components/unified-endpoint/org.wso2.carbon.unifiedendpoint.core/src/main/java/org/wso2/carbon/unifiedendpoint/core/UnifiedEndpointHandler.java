/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.unifiedendpoint.core;

import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.AddressingConstants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.WSDL2Constants;
import org.apache.axis2.handlers.AbstractHandler;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.neethi.Policy;
import org.apache.neethi.PolicyEngine;
import org.apache.synapse.core.axis2.SOAPUtils;
import org.apache.synapse.transport.nhttp.NhttpConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.RegistryType;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.registry.api.RegistryException;
import org.wso2.carbon.registry.api.Resource;
import org.wso2.carbon.registry.api.Registry;
import org.wso2.carbon.utils.CarbonUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * Axis2 Handler that should be invoked when using Unified EP. This handler shoule be included in
 * UEPModule
 */
public class UnifiedEndpointHandler extends AbstractHandler {
    static private Log log = LogFactory.getLog(UnifiedEndpointHandler.class);

    public InvocationResponse invoke(MessageContext msgContext) throws AxisFault {
        if (log.isDebugEnabled()) {
            log.debug("Invoking UnifiedEndpointHandler.");
        }
        UnifiedEndpoint unifiedEndpoint;
        if (msgContext.getTo() instanceof UnifiedEndpoint) {
            unifiedEndpoint = (UnifiedEndpoint) msgContext.getTo();
            handleMessageOutput(unifiedEndpoint, msgContext);
            handleAddressing(unifiedEndpoint, msgContext);
            handleSecurity(unifiedEndpoint, msgContext);
            handleRM(unifiedEndpoint, msgContext);
            handleTransportProperties(unifiedEndpoint, msgContext);
        }
        return InvocationResponse.CONTINUE;
    }

    private void handleMessageOutput(UnifiedEndpoint uep, MessageContext msgContext) throws AxisFault {
        /*UEP MessageOutput*/
        UnifiedEndpointMessageOutput uepMessageOutput = uep.getMessageOutput();
        if (uepMessageOutput != null) {
            /*Format*/
            String uepMessageOutputFormat = uepMessageOutput.getFormat();
            if (uepMessageOutputFormat.equals(UnifiedEndpointConstants.FORMAT_POX)) {
                msgContext.setDoingREST(true);
                msgContext.setProperty(org.apache.axis2.Constants.Configuration.MESSAGE_TYPE,
                        org.apache.axis2.transport.http.HTTPConstants.MEDIA_TYPE_APPLICATION_XML);
            } else if (uepMessageOutputFormat.equals(UnifiedEndpointConstants.FORMAT_GET)) {
                msgContext.setDoingREST(true);
                msgContext.setProperty(Constants.Configuration.HTTP_METHOD,
                        Constants.Configuration.HTTP_METHOD_GET);
                msgContext.setProperty(org.apache.axis2.Constants.Configuration.MESSAGE_TYPE,
                        org.apache.axis2.transport.http.HTTPConstants.MEDIA_TYPE_X_WWW_FORM);
            } else if (uepMessageOutputFormat.equals(UnifiedEndpointConstants.FORMAT_SOAP11)) {
                msgContext.setDoingREST(false);
                msgContext.removeProperty(org.apache.axis2.Constants.Configuration.MESSAGE_TYPE);
                msgContext.setProperty(Constants.Configuration.HTTP_METHOD,
                        Constants.Configuration.HTTP_METHOD_POST);

                if (msgContext.getSoapAction() == null && msgContext.getWSAAction() != null) {
                    msgContext.setSoapAction(msgContext.getWSAAction());
                }
                if (!msgContext.isSOAP11()) {
                    SOAPUtils.convertSOAP12toSOAP11(msgContext);
                }
            } else if (uepMessageOutputFormat.equals(UnifiedEndpointConstants.FORMAT_SOAP12)) {
                msgContext.setDoingREST(false);
                msgContext.removeProperty(org.apache.axis2.Constants.Configuration.MESSAGE_TYPE);
                msgContext.setProperty(Constants.Configuration.HTTP_METHOD,
                        Constants.Configuration.HTTP_METHOD_POST);
                if (msgContext.getSoapAction() == null && msgContext.getWSAAction() != null) {
                    msgContext.setSoapAction(msgContext.getWSAAction());
                }
                if (msgContext.isSOAP11()) {
                    SOAPUtils.convertSOAP11toSOAP12(msgContext);
                }
            } else if (uepMessageOutputFormat.equals(UnifiedEndpointConstants.FORMAT_REST)) {
                msgContext.removeProperty(org.apache.axis2.Constants.Configuration.MESSAGE_TYPE);
                msgContext.setDoingREST(true);
            } else {
                /*ToDo*/
                processHttpGetMethod(msgContext, msgContext);
            }

            /*Optimize*/
            if (uepMessageOutput.getOptimize().equals(UnifiedEndpointConstants.OPTIMIZE_MTOM)) {
                msgContext.setDoingMTOM(true);
                msgContext.setProperty(org.apache.axis2.Constants.Configuration.ENABLE_MTOM,
                        org.apache.axis2.Constants.VALUE_TRUE);
            } else if (uepMessageOutput.getOptimize().equals(UnifiedEndpointConstants.OPTIMIZE_SWA)) {
                msgContext.setDoingSwA(true);
                msgContext.setProperty(org.apache.axis2.Constants.Configuration.ENABLE_SWA,
                        org.apache.axis2.Constants.VALUE_TRUE);
            }

            /*Charset*/
            if (uepMessageOutput.getCharSetEncoding() != null) {
                msgContext.setProperty(Constants.Configuration.CHARACTER_SET_ENCODING,
                        uepMessageOutput.getCharSetEncoding());
            }

             /**/
            if (uep.getAddress() != null) {
                if (uepMessageOutputFormat.equals(UnifiedEndpointConstants.FORMAT_REST)
                        && msgContext.getProperty(NhttpConstants.REST_URL_POSTFIX) != null) {
                    msgContext.setTo(new EndpointReference(uep.getAddress() +
                            msgContext.getProperty(NhttpConstants.REST_URL_POSTFIX)));
                } else {
                    msgContext.setTo(new EndpointReference(uep.getAddress()));
                }
                msgContext.setProperty(NhttpConstants.ENDPOINT_PREFIX, uep.getAddress());
            }

            if (uep.isSeparateListener()) {
                msgContext.getOptions().setUseSeparateListener(true);
            }
        } else {
            /*ToDo*/
            processHttpGetMethod(msgContext, msgContext);
        }

        if (msgContext.isDoingREST() && HTTPConstants.MEDIA_TYPE_X_WWW_FORM.equals(
                msgContext.getProperty(Constants.Configuration.MESSAGE_TYPE))) {
            if (msgContext.getProperty(WSDL2Constants.ATTR_WHTTP_LOCATION) == null
                    && msgContext.getEnvelope().getBody().getFirstElement() != null) {
                msgContext.setProperty(WSDL2Constants.ATTR_WHTTP_LOCATION,
                        msgContext.getEnvelope().getBody().getFirstElement()
                                .getQName().getLocalPart());
            }
        }
    }

    private static void processHttpGetMethod(MessageContext originalInMsgCtx,
                                             MessageContext axisOutMsgCtx) {
          /*ToDO*/
    }

    private void handleAddressing(UnifiedEndpoint uep, MessageContext msgContext) {
        String addressingVersion = uep.getAddressingVersion();
        if (uep.isAddressingEnabled()) {
            if (addressingVersion != null
                    && UnifiedEndpointConstants.ADDRESSING_VERSION_SUBMISSION.equals(addressingVersion)) {
                msgContext.setProperty(AddressingConstants.WS_ADDRESSING_VERSION,
                        AddressingConstants.Submission.WSA_NAMESPACE);
            } else if (addressingVersion != null
                    && UnifiedEndpointConstants.ADDRESSING_VERSION_FINAL.equals(addressingVersion)) {
                msgContext.setProperty(AddressingConstants.WS_ADDRESSING_VERSION,
                        AddressingConstants.Final.WSA_NAMESPACE);
            }

            msgContext.setProperty(AddressingConstants.DISABLE_ADDRESSING_FOR_OUT_MESSAGES, Boolean.FALSE);

            //Adding ReplyTo address
	    if (uep.getReplyToAddress() != null) {
            	msgContext.getOptions().setReplyTo(new EndpointReference(uep.getReplyToAddress().toString()));
	    }
        } else {
            msgContext.setProperty(AddressingConstants.DISABLE_ADDRESSING_FOR_OUT_MESSAGES, Boolean.TRUE);
        }
    }

    private void handleTransportProperties(UnifiedEndpoint uep, MessageContext msgContext) {
        UnifiedEndpointTransport uepTransport = uep.getTransport();
        if (uepTransport == null) {
            if (log.isDebugEnabled()) {
                log.debug("Transport specific properties not defined in the EPR");
            }
            return;
        }
        /** HTTP Transport */
        if (uepTransport.getTransportType().equals(UnifiedEndpointConstants.TRANSPORT_TYPE_HTTP)) {
            if (uepTransport.getTransportProperties() != null && (!uepTransport.getTransportProperties().isEmpty())) {
                //Here some-properties like (disable chunking has to be fixed case by case. So
                // just adding a property map for HTTPConstants.HTTP_HEADERS may not work. So the
                // following line is commented out for that reason.
                //msgContext.setProperty(HTTPConstants.HTTP_HEADERS,uepTransport.getTransportProperties());

                for(String key : uepTransport.getTransportProperties().keySet()) {
                    String trimmedKey = key.trim();
                    String trimmedValue = uepTransport.getTransportProperties().get(trimmedKey)
                            .trim();
                    if (trimmedKey.equals("FORCE_HTTP_1.0") || trimmedKey.equals("authorization-username") || trimmedKey.equals("authorization-password")) {
                        if (trimmedKey.equals("FORCE_HTTP_1.0")) {
                            if (trimmedValue.equals("true")) {
                                //If the "FORCE_HTTP_1.0" key value is "true" -> "HTTPConstants.CHUNKED" key value is "false"
                                msgContext.getOptions().setProperty(HTTPConstants.CHUNKED, false);
                            } else {
                                log.warn("Wrong parameter value: \"" + trimmedValue + "\" for the key: " +
                                        trimmedKey);
                            }
                        }
                    } else {
                        log.info("The property key: \"" + trimmedKey + "\" is not supported.");
                    }
                }

                try {
                    CarbonUtils.setBasicAccessSecurityHeaders(uep.getAuthorizationUserName(), uep.getAuthorizationPassword(), false, msgContext);
                } catch (AxisFault e) {
                    log.error("Error while authorizing the user ", e);
                }
            }
        }
        /*TODO Add other transports*/
        /*else if () {
        }*/
    }

    private void handleTimeout(UnifiedEndpoint uep, MessageContext msgContext) {
        UnifiedEndpointTimeout unifiedEndpointTimeout = uep.getTimeout();
        if (unifiedEndpointTimeout.getTimeOutProperties() != null && (!unifiedEndpointTimeout.getTimeOutProperties().isEmpty())) {
            for (Map.Entry<String, String> entry : unifiedEndpointTimeout.getTimeOutProperties().entrySet()) {
                if (entry.getKey() != null && entry.getValue() != null) {
                    msgContext.setProperty(entry.getKey(), entry.getValue());
                }
            }
        }
    }

    private void handleSecurity(UnifiedEndpoint uep, MessageContext msgContext) throws AxisFault {
        if (uep.isSecurityEnabled()) {
            msgContext.getAxisService().engageModule(msgContext.getAxisService().
                                                getAxisConfiguration().getModule("rampart"));
            msgContext.getAxisService().getPolicySubject().attachPolicy(
                                                loadPolicy(uep.getWsSecPolicyKey(),msgContext));
        }
    }


    private void handleRM(UnifiedEndpoint uep, MessageContext msgContext) {
        /*ToDO*/
    }

    private Policy loadPolicy(String path, MessageContext msgContext) throws AxisFault {
        Policy policyDoc = null;
        if (path.startsWith(UnifiedEndpointConstants.VIRTUAL_FILE))  {
            path = path.replaceFirst(UnifiedEndpointConstants.VIRTUAL_FILE, "");
            try {
                InputStream policyStream = UnifiedEndpointUtils.getFileInputStream(path);
                try {
                    policyDoc = PolicyEngine.getPolicy(policyStream);
                } finally {
                    policyStream.close();
                }
            } catch (IOException e) {
                String errMsg = "Exception while parsing policy: " + path;
                log.error(errMsg, e);
                throw new AxisFault(errMsg, e);
            }
        } else if (path.startsWith(UnifiedEndpointConstants.VIRTUAL_GOV_REG)) {
            Registry reg = PrivilegedCarbonContext.getCurrentContext(
                    msgContext.getConfigurationContext()).
                    getRegistry(RegistryType.SYSTEM_GOVERNANCE);
            path = path.substring(UnifiedEndpointConstants.VIRTUAL_GOV_REG.length());
            try {
                if (reg.resourceExists(path)) {
                    Resource policyResource = reg.get(path);
                    policyDoc = PolicyEngine.getPolicy(policyResource.getContentStream());
                }
            } catch (RegistryException e) {
                String errMsg = "Exception while loading policy from Governance registry: " + path;
                log.error(errMsg, e);
                throw new AxisFault(errMsg, e);
            }
        } else if (path.startsWith(UnifiedEndpointConstants.VIRTUAL_CONF_REG)) {
            Registry reg = PrivilegedCarbonContext.getCurrentContext(
                    msgContext.getConfigurationContext()).
                    getRegistry(RegistryType.SYSTEM_CONFIGURATION);
            path = path.substring(UnifiedEndpointConstants.VIRTUAL_CONF_REG.length());
            try {
                if (reg.resourceExists(path)) {
                    Resource policyResource = reg.get(path);
                    policyDoc = PolicyEngine.getPolicy(policyResource.getContentStream());
                }
            } catch (RegistryException e) {
                String errMsg = "Exception while loading policy from Configuration registry: " + path;
                log.error(errMsg, e);
                throw new AxisFault(errMsg, e);
            }
        } else {
            String errMsg = "Invalid policy path: " + path;
            log.error(errMsg);
            throw new AxisFault(errMsg);
        }

        return policyDoc;
    }
}
