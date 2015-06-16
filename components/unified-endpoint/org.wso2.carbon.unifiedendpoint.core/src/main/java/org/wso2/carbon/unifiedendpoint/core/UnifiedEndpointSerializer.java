/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.unifiedendpoint.core;

import org.apache.axiom.om.*;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReferenceHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.QName;
import java.util.Map;

/**
 * Serialize unified EP.
 */
public class UnifiedEndpointSerializer {

    private static Log log = LogFactory.getLog(UnifiedEndpointSerializer.class);
    private OMFactory omFactory;

    public UnifiedEndpointSerializer() {
        omFactory = OMAbstractFactory.getOMFactory();
    }

    public OMElement serializeUnifiedEndpoint(UnifiedEndpoint unifiedEndpoint) {

        if (!unifiedEndpoint.isUepMetadataSerialized()) {
            unifiedEndpoint.addMetaData(serializeUnifiedEndpointMetadataId(unifiedEndpoint));
            unifiedEndpoint.addMetaData(serializeUnifiedEndpointDiscovery(unifiedEndpoint));

            if (unifiedEndpoint.getWsdl11Definitions() != null) {
                unifiedEndpoint.addMetaData(unifiedEndpoint.getWsdl11Definitions());
            }

            /*MessageOutput*/
            if (unifiedEndpoint.getMessageOutput() != null) {
                unifiedEndpoint.addMetaData(serializeUnifiedEndpointMessageOutput(unifiedEndpoint.getMessageOutput()));
            }

            /*Transport Properties*/
            if (unifiedEndpoint.getTransport() != null) {
                unifiedEndpoint.addMetaData(serializeUnifiedEndpointTransport(unifiedEndpoint));
            }

            /*Timeout*/
            unifiedEndpoint.addMetaData(serializeTimeOut(unifiedEndpoint));

            /*Monitoring*/
            unifiedEndpoint.addMetaData(serializeUnifiedEndpointMonitoring(unifiedEndpoint));


            /*QoS*/
            unifiedEndpoint.addMetaData(serializeQoS(unifiedEndpoint));

            /*Session*/
            if (unifiedEndpoint.getSessionType() != null) {
                OMElement sessionElem = omFactory.createOMElement(UnifiedEndpointConstants.SESSION, null);
                sessionElem.addAttribute(UnifiedEndpointConstants.SESSION_TYPE, unifiedEndpoint.getSessionType(), null);
                unifiedEndpoint.addMetaData(sessionElem);
            }

            unifiedEndpoint.setUepMetadataSerialized(true); /*UEP Metadata is serialized now*/
        }

        OMElement unifiedEPRElement = null;
        try {
            /*Creating OM Element for the given  Unified EPR instance */
            unifiedEPRElement = EndpointReferenceHelper.toOM(omFactory, unifiedEndpoint,
                                                             new QName(UnifiedEndpointConstants.WSA_NS,
                                                                       UnifiedEndpointConstants.UNIFIED_EPR,
                                                                       UnifiedEndpointConstants.WSA_NS_PREFIX),
                                                             UnifiedEndpointConstants.WSA_NS);

        } catch (AxisFault e) {
            log.error("Invalid UEP Instance.");

        }
        return unifiedEPRElement;
    }


    public OMElement serializeUnifiedEndpointMetadataId(UnifiedEndpoint unifiedEndpoint) {
        OMElement metadataIDElem = null;

        if (unifiedEndpoint.getUepId() != null) {
            metadataIDElem = omFactory.createOMElement(UnifiedEndpointConstants.METADATA_ID, null);
            OMText nameText = omFactory.createOMText(unifiedEndpoint.getUepId());
            metadataIDElem.addChild(nameText);
        }
        return metadataIDElem;
    }


    public OMElement serializeUnifiedEndpointDiscovery(UnifiedEndpoint unifiedEndpoint) {
        OMElement discoveryElem = null;

        if (unifiedEndpoint.getDiscoveryUuid() != null) {

            discoveryElem = omFactory.createOMElement(UnifiedEndpointConstants.METADATA_DISCOVERY, null);

            OMElement uuidElem = omFactory.createOMElement(UnifiedEndpointConstants.METADATA_UUID, null);
            OMText uuidText = omFactory.createOMText(unifiedEndpoint.getDiscoveryUuid());
            uuidElem.addChild(uuidText);
            discoveryElem.addChild(uuidElem);

            if (unifiedEndpoint.getDiscoveryScope() != null) {
                OMElement scopeElem = omFactory.createOMElement(UnifiedEndpointConstants.METADATA_SCOPE, null);
                OMText scopeText = omFactory.createOMText(unifiedEndpoint.getDiscoveryScope());
                scopeElem.addChild(scopeText);
                discoveryElem.addChild(scopeElem);
            }

            if (unifiedEndpoint.getDiscoveryType() != null) {
                OMElement typeElem = omFactory.createOMElement(UnifiedEndpointConstants.METADATA_TYPE, null);
                OMText typeText = omFactory.createOMText(unifiedEndpoint.getDiscoveryType());
                typeElem.addChild(typeText);
                discoveryElem.addChild(typeElem);
            }
        }
        return discoveryElem;
    }

    public OMElement serializeUnifiedEndpointMessageOutput(
            UnifiedEndpointMessageOutput unifiedEndpointMessageOutput) {

        OMElement messageOutputElem = omFactory.createOMElement(UnifiedEndpointConstants.MESSAGE_OUTPUT, null);

        if (unifiedEndpointMessageOutput.getFormat() != null) {
            messageOutputElem.addAttribute(UnifiedEndpointConstants.MESSAGE_OUTPUT_FORMAT,
                                           unifiedEndpointMessageOutput.getFormat(), null);
        }

        if (unifiedEndpointMessageOutput.getOptimize() != null) {
            messageOutputElem.addAttribute(UnifiedEndpointConstants.MESSAGE_OUTPUT_OPTIMIZE,
                                           unifiedEndpointMessageOutput.getOptimize(), null);
        }

        if (unifiedEndpointMessageOutput.getCharSetEncoding() != null) {
            messageOutputElem.addAttribute(UnifiedEndpointConstants.MESSAGE_OUTPUT_CHARSET,
                                           unifiedEndpointMessageOutput.getCharSetEncoding(), null);
        }

        return messageOutputElem;
    }

    public OMElement serializeUnifiedEndpointTransport(UnifiedEndpoint unifiedEndpoint) {
        OMElement transportsElem = null;

        if (unifiedEndpoint.getTransport() != null) {
            if (unifiedEndpoint.getTransport().getTransportType() != null) {
                Map<String, String> transportProperties = unifiedEndpoint.getTransport().getTransportProperties();
                transportsElem = omFactory.createOMElement(UnifiedEndpointConstants.TRANSPORT, null);
                transportsElem.addAttribute(UnifiedEndpointConstants.TRANSPORT_OPTIONS_TYPE,
                                            unifiedEndpoint.getTransport().getTransportType(), null);

                for (Map.Entry<String, String> entry : transportProperties.entrySet()) {
                    OMElement transportPropertyElem = omFactory.createOMElement(entry.getKey(), null);
                    OMText transportPropertyText = omFactory.createOMText(entry.getValue());
                    transportPropertyElem.addChild(transportPropertyText);
                    transportsElem.addChild(transportPropertyElem);
                }
            }
        }

        return transportsElem;
    }

    public OMElement serializeTimeOut(UnifiedEndpoint unifiedEndpoint) {
        OMElement timeOutElem = null;

        if (unifiedEndpoint.getTimeout() != null) {

            Map<String, String> timeoutProperties = unifiedEndpoint.getTimeout().getTimeOutProperties();
            timeOutElem = omFactory.createOMElement(UnifiedEndpointConstants.TIMEOUT, null);

            for (Map.Entry<String, String> entry : timeoutProperties.entrySet()) {
                OMElement timeoutPropertyElem = omFactory.createOMElement(entry.getKey(), null);
                OMText timeoutPropertyText = omFactory.createOMText(entry.getValue());
                timeoutPropertyElem.addChild(timeoutPropertyText);
                timeOutElem.addChild(timeoutPropertyElem);
            }
        }
        return timeOutElem;
    }


    public OMElement serializeUnifiedEndpointMonitoring(UnifiedEndpoint unifiedEndpoint) {
        OMElement monitoringElem = omFactory.createOMElement(UnifiedEndpointConstants.MONITORING, null);
        String statStatus = unifiedEndpoint.isStatisticEnabled() ? "enable" : "disable";
        String traceStatus = unifiedEndpoint.isTraceEnabled() ? "enable" : "disable";

        monitoringElem.addAttribute(UnifiedEndpointConstants.MONITORING_STATISTICS, statStatus, null);
        monitoringElem.addAttribute(UnifiedEndpointConstants.MONITORING_TRACE, traceStatus, null);

        if (unifiedEndpoint.getMonitoringLogStatement() != null) {
            monitoringElem.addChild(omFactory.createOMText(unifiedEndpoint.getMonitoringLogStatement()));
        }

        return monitoringElem;
    }

    public OMElement serializeQoS(UnifiedEndpoint unifiedEndpoint) {
        OMElement qosElem = omFactory.createOMElement(UnifiedEndpointConstants.QOS, null);
        if (unifiedEndpoint.isRMEnabled()) {
            OMElement rmEnabledElem = omFactory.createOMElement(UnifiedEndpointConstants.QOS_ENABLE_RM, null);
            if (unifiedEndpoint.getWsRMPolicyKey() != null) {
                rmEnabledElem.addAttribute(UnifiedEndpointConstants.QOS_POLICY, unifiedEndpoint.getWsRMPolicyKey(), null);
            }
            qosElem.addChild(rmEnabledElem);
        }

        if (unifiedEndpoint.isSecurityEnabled()) {
            OMElement secEnabledElem = omFactory.createOMElement(UnifiedEndpointConstants.QOS_ENABLE_WS_SEC, null);
            if (unifiedEndpoint.getWsSecPolicyKey() != null) {
                secEnabledElem.addAttribute(UnifiedEndpointConstants.QOS_POLICY, unifiedEndpoint.getWsSecPolicyKey(), null);
            }
            qosElem.addChild(secEnabledElem);
        }

        if (unifiedEndpoint.isAddressingEnabled()) {
            OMElement addressingEnabledElem = omFactory.createOMElement(UnifiedEndpointConstants.QOS_ENABLE_ADDRESSING, null);
            if (unifiedEndpoint.getAddressingVersion() != null) {
                addressingEnabledElem.addAttribute(UnifiedEndpointConstants.QOS_VERSION, unifiedEndpoint.getAddressingVersion(), null);
            }

            /*isSeparate Listener*/
            addressingEnabledElem.addAttribute(UnifiedEndpointConstants.QOS_ENABLE_ADDRESSING_SEPARATE_LISTENER,
                                               Boolean.toString(unifiedEndpoint.isSeparateListener()), null);
            qosElem.addChild(addressingEnabledElem);

            //replyTo address
            if (unifiedEndpoint.getReplyToAddress() != null) {
                OMElement replyToElem = omFactory.createOMElement(UnifiedEndpointConstants.QOS_ENABLE_ADDRESSING_REPLY_TO_ADDRESS, null);
                OMText replyToAddressText = omFactory.createOMText(unifiedEndpoint.getReplyToAddress().toString());
                replyToElem.addChild(replyToAddressText);
                addressingEnabledElem.addChild(replyToElem);
            }
        }

        /*Security Element mandatory ?*/
        OMElement secElem = omFactory.createOMElement(UnifiedEndpointConstants.QOS_SECURITY, null);
        if (unifiedEndpoint.getSecUserName() != null && unifiedEndpoint.getSecPwd() != null) {
            OMElement userPwdPairElem = omFactory.createOMElement(UnifiedEndpointConstants.QOS_SECURITY_USER_PWD_PAIR, null);
            userPwdPairElem.addAttribute(UnifiedEndpointConstants.QOS_SECURITY_USER_NAME, unifiedEndpoint.getSecUserName(), null);
            userPwdPairElem.addAttribute(UnifiedEndpointConstants.QOS_SECURITY_PASSWORD, unifiedEndpoint.getSecPwd(), null);
            if (unifiedEndpoint.getSecPolicy() != null) {
                userPwdPairElem.addChild(unifiedEndpoint.getSecPolicy());
            }
            secElem.addChild(userPwdPairElem);
        }
        qosElem.addChild(secElem);
        return qosElem;
    }
}
