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


import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.AddressingConstants;
import org.apache.axis2.addressing.EndpointReferenceHelper;
import org.apache.axis2.deployment.DeploymentException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.securevault.SecretResolver;
import org.wso2.securevault.SecretResolverFactory;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Iterator;

/**
 * Factory to create Unified Endpoints
 */

public class UnifiedEndpointFactory {

    private final Log log = LogFactory.getLog(UnifiedEndpointFactory.class);

    public UnifiedEndpointFactory() {
    }

    public UnifiedEndpoint createVirtualEndpointFromFilePath(String logicalName) throws AxisFault {
        UnifiedEndpoint realUEP = null;
        if (logicalName.startsWith(UnifiedEndpointConstants.VIRTUAL_FILE)) {
            String filePath = logicalName.replaceFirst(UnifiedEndpointConstants.VIRTUAL_FILE, "");

            try {
                OMElement omElem = getOmFromFile(filePath);
                realUEP = createEndpoint(omElem);

            } catch (XMLStreamException e) {
                throw new AxisFault("Could not create endpoint from file path", e);
            } catch (IOException e) {
                throw new AxisFault("Could not create endpoint from file path", e);
            }
        }
        return realUEP;
    }

    private OMElement getOmFromFile(String filePath) throws IOException, XMLStreamException {
        if (!new File(filePath).isFile()) {
            throw new DeploymentException("EPR file: " + filePath + " not found.");
        }
        String xmlString = FileUtils.readFileToString(new File(filePath), "UTF-8");
        return AXIOMUtil.stringToOM(xmlString);
    }

    public UnifiedEndpoint createEndpoint(OMElement uEPConfigEle) throws AxisFault {
        UnifiedEndpoint unifiedEndpoint = new UnifiedEndpoint();
        EndpointReferenceHelper.fromOM(unifiedEndpoint, uEPConfigEle, AddressingConstants.Final.WSA_NAMESPACE);
        OMElement metadataElem = uEPConfigEle.getFirstChildWithName(UnifiedEndpointConstants.METADATA_Q);

        if (metadataElem != null) {
            OMElement idElem = metadataElem.getFirstChildWithName(UnifiedEndpointConstants.METADATA_ID_Q);

            if (idElem != null) {
                unifiedEndpoint.setUepId(idElem.getText());
            } else {
                log.error("UEP Configuration violation: " + UnifiedEndpointConstants.METADATA_ID_Q + " not found");
            }

            /** Discovery */
            if (metadataElem.getFirstChildWithName(UnifiedEndpointConstants.METADATA_DISCOVERY_Q) != null) {
                extractDiscoveryConfig(unifiedEndpoint, metadataElem.getFirstChildWithName(
                        UnifiedEndpointConstants.METADATA_DISCOVERY_Q));
            }

            /** Timeout */
            if (metadataElem.getFirstChildWithName(UnifiedEndpointConstants.TIMEOUT_Q) != null) {
                extractTimeoutConfig(unifiedEndpoint, metadataElem.getFirstChildWithName(
                        UnifiedEndpointConstants.TIMEOUT_Q));
            }

            /** WSDL Definitions */
            if (metadataElem.getFirstChildWithName(
                    UnifiedEndpointConstants.METADATA_WSDL11_DEFINITIONS_Q) != null) {
                unifiedEndpoint.setWsdl11Definitions(
                        metadataElem.getFirstChildWithName(
                                UnifiedEndpointConstants.METADATA_WSDL11_DEFINITIONS_Q));
            }

            /** MessageOutput */
            if (metadataElem.getFirstChildWithName(
                    UnifiedEndpointConstants.MESSAGE_OUTPUT_Q) != null) {
                extractMessageOutPutConfig(unifiedEndpoint,
                                           metadataElem.getFirstChildWithName(UnifiedEndpointConstants.MESSAGE_OUTPUT_Q));
            }

            /** Transport */
            if (metadataElem.getFirstChildWithName(UnifiedEndpointConstants.TRANSPORT_Q) != null) {
                extractTransportConfig(unifiedEndpoint, metadataElem.getFirstChildWithName(
                        UnifiedEndpointConstants.TRANSPORT_Q));

                if (metadataElem.getFirstChildWithName(UnifiedEndpointConstants.TRANSPORT_Q).getFirstChildWithName(
                        UnifiedEndpointConstants.TRANSPORT_AUTHORIZATION_USERNAME_Q) != null) {

                    unifiedEndpoint.setAuthorizationUserName(metadataElem.getFirstChildWithName(
                            UnifiedEndpointConstants.TRANSPORT_Q).getFirstChildWithName(
                            UnifiedEndpointConstants.TRANSPORT_AUTHORIZATION_USERNAME_Q).getText());

                }

                if (metadataElem.getFirstChildWithName(UnifiedEndpointConstants.TRANSPORT_Q).getFirstChildWithName(
                        UnifiedEndpointConstants.TRANSPORT_AUTHORIZATION_PASSWORD_Q) != null) {

                    OMElement transport_auth_password = metadataElem.getFirstChildWithName(
                            UnifiedEndpointConstants.TRANSPORT_Q).getFirstChildWithName(
                            UnifiedEndpointConstants.TRANSPORT_AUTHORIZATION_PASSWORD_Q);
                    String secretAlias = transport_auth_password.getAttributeValue(
                            new QName(UnifiedEndpointConstants.SECURE_VAULT_NS,
                                      UnifiedEndpointConstants.SECRET_ALIAS_ATTR_NAME));

                    if (secretAlias != null && secretAlias.trim().length() > 0) {
                        secretAlias = secretAlias.trim();
                        SecretResolver secretResolver = SecretResolverFactory.create(metadataElem.getFirstChildWithName(
                                UnifiedEndpointConstants.TRANSPORT_Q), false);
                        /* Setting the secured password */
                        if (secretResolver != null && secretResolver.isInitialized() &&
                            secretResolver.isTokenProtected(secretAlias)) {

                            String adminPassword = secretResolver.resolve(secretAlias);
                            unifiedEndpoint.setAuthorizationPassword(adminPassword);

                        } else {
                            /* If secure vault is not configured properly, Reading plain text password */
                            unifiedEndpoint.setAuthorizationPassword(metadataElem.getFirstChildWithName(
                                    UnifiedEndpointConstants.TRANSPORT_Q).getFirstChildWithName(
                                    UnifiedEndpointConstants.TRANSPORT_AUTHORIZATION_PASSWORD_Q).getText());
                        }
                    } else {
                        unifiedEndpoint.setAuthorizationPassword(
                                metadataElem.getFirstChildWithName(
                                        UnifiedEndpointConstants.TRANSPORT_Q).getFirstChildWithName(
                                        UnifiedEndpointConstants.TRANSPORT_AUTHORIZATION_PASSWORD_Q).getText());
                    }
                }
            }

            /** Monitoring */
            if (metadataElem.getFirstChildWithName(UnifiedEndpointConstants.MONITORING_Q) != null) {
                extractMetadataMonitoringConfig(unifiedEndpoint,
                                                metadataElem.getFirstChildWithName(UnifiedEndpointConstants.MONITORING_Q));
            }

            /** ErrorHandling */
            if (metadataElem.getFirstChildWithName(UnifiedEndpointConstants.ERROR_HANDLING_Q) != null) {
                extractErrorHandlingConfig(unifiedEndpoint, metadataElem.getFirstChildWithName(
                        UnifiedEndpointConstants.ERROR_HANDLING_Q));
            }

            /** QoS */
            if (metadataElem.getFirstChildWithName(UnifiedEndpointConstants.QOS_Q) != null) {
                extractQoSConfig(unifiedEndpoint, metadataElem.getFirstChildWithName(
                        UnifiedEndpointConstants.QOS_Q));
            }

            /** Session */
            if (metadataElem.getFirstChildWithName(UnifiedEndpointConstants.SESSION_Q) != null) {
                if (metadataElem.getFirstChildWithName(
                        UnifiedEndpointConstants.SESSION_Q).getAttributeValue(
                        UnifiedEndpointConstants.SESSION_TYPE_Q) != null) {
                    unifiedEndpoint.setSessionType(metadataElem.getFirstChildWithName(
                            UnifiedEndpointConstants.SESSION_Q).getAttributeValue(
                            UnifiedEndpointConstants.SESSION_TYPE_Q));
                }
            }

            /** Cluster */
            if (metadataElem.getFirstChildWithName(UnifiedEndpointConstants.CLUSTER_Q) != null) {
                extractClusterConfig(unifiedEndpoint, metadataElem.getFirstChildWithName(
                        UnifiedEndpointConstants.CLUSTER_Q));
            }
        }
        return unifiedEndpoint;
    }

    /**
     * Discovery
     *
     * @param unifiedEndpoint
     * @param discoveryElem
     */
    public void extractDiscoveryConfig(UnifiedEndpoint unifiedEndpoint, OMElement discoveryElem) {
        OMElement uuidElem = discoveryElem.getFirstChildWithName(UnifiedEndpointConstants.METADATA_UUID_Q);
        if (uuidElem != null) {
            unifiedEndpoint.setDiscoveryUuid(uuidElem.getText());
        }

        OMElement scopeElem = discoveryElem.getFirstChildWithName(UnifiedEndpointConstants.METADATA_SCOPE_Q);
        if (scopeElem != null) {
            unifiedEndpoint.setDiscoveryScope(scopeElem.getText());
        }

        OMElement typeElem = discoveryElem.getFirstChildWithName(UnifiedEndpointConstants.METADATA_TYPE_Q);
        if (typeElem != null) {
            unifiedEndpoint.setDiscoveryType(typeElem.getText());
        }
    }

    /**
     * Timeout
     *
     * @param unifiedEndpoint
     * @param timeoutElem
     */
    public void extractTimeoutConfig(UnifiedEndpoint unifiedEndpoint, OMElement timeoutElem) {
        UnifiedEndpointTimeout unifiedEndpointTimeout = new UnifiedEndpointTimeout();

        Iterator timeoutPropertiesIterator = timeoutElem.getChildren();
        while (timeoutPropertiesIterator.hasNext()) {
            OMElement timeoutPropertyElem = (OMElement) timeoutPropertiesIterator.next();
            unifiedEndpointTimeout.addTimeOutProperty(timeoutPropertyElem.getLocalName(),
                                                      timeoutPropertyElem.getText());
        }
        unifiedEndpoint.setTimeout(unifiedEndpointTimeout);
    }

    /**
     * Transport
     *
     * @param unifiedEndpoint
     * @param transportElem
     */
    public void extractTransportConfig(UnifiedEndpoint unifiedEndpoint, OMElement transportElem) {
        UnifiedEndpointTransport unifiedEndpointTransport = new UnifiedEndpointTransport();

        if (transportElem.getAttributeValue(UnifiedEndpointConstants.TRANSPORT_OPTIONS_TYPE_Q) != null) {
            unifiedEndpointTransport.setTransportType(transportElem.getAttributeValue(
                    UnifiedEndpointConstants.TRANSPORT_OPTIONS_TYPE_Q));
            Iterator transportPropertiesIterator = transportElem.getChildElements();
            while (transportPropertiesIterator.hasNext()) {
                OMElement transportPropElement = (OMElement) transportPropertiesIterator.next();
                unifiedEndpointTransport.addTransportProperty(
                        transportPropElement.getLocalName().trim(), transportPropElement.getText
                                ().trim());
            }
            unifiedEndpoint.setTransport(unifiedEndpointTransport);
        }
    }

    /**
     * Message Output
     *
     * @param unifiedEndpoint
     * @param messageOutputElem
     */
    public void extractMessageOutPutConfig(UnifiedEndpoint unifiedEndpoint, OMElement messageOutputElem) {
        UnifiedEndpointMessageOutput unifiedEndpointMessageOutput = new UnifiedEndpointMessageOutput();
        if (messageOutputElem.getAttributeValue(UnifiedEndpointConstants.MESSAGE_OUTPUT_FORMAT_Q) != null) {
            unifiedEndpointMessageOutput.setFormat(messageOutputElem.getAttributeValue(
                    UnifiedEndpointConstants.MESSAGE_OUTPUT_FORMAT_Q));
        }
        if (messageOutputElem.getAttributeValue(UnifiedEndpointConstants.MESSAGE_OUTPUT_OPTIMIZE_Q) != null) {
            unifiedEndpointMessageOutput.setOptimize(messageOutputElem.getAttributeValue(
                    UnifiedEndpointConstants.MESSAGE_OUTPUT_OPTIMIZE_Q));
        }
        if (messageOutputElem.getAttributeValue(UnifiedEndpointConstants.MESSAGE_OUTPUT_CHARSET_Q) != null) {
            unifiedEndpointMessageOutput.setCharSetEncoding(messageOutputElem.getAttributeValue(
                    UnifiedEndpointConstants.MESSAGE_OUTPUT_CHARSET_Q));
        }
        unifiedEndpoint.setMessageOutput(unifiedEndpointMessageOutput);
    }

    /**
     * Monitoring
     *
     * @param unifiedEndpoint
     * @param monitoringElem
     */
    public void extractMetadataMonitoringConfig(UnifiedEndpoint unifiedEndpoint, OMElement monitoringElem) {
        if (monitoringElem.getAttributeValue(UnifiedEndpointConstants.MONITORING_STATISTICS_Q) != null) {
            unifiedEndpoint.setStatisticEnabled(monitoringElem.getAttributeValue(
                    UnifiedEndpointConstants.MONITORING_STATISTICS_Q).equals("enable"));
        }
        if (monitoringElem.getAttributeValue(UnifiedEndpointConstants.MONITORING_TRACE_Q) != null) {
            unifiedEndpoint.setTraceEnabled(monitoringElem.getAttributeValue(
                    UnifiedEndpointConstants.MONITORING_TRACE_Q).equals("enable"));
        }
        if (monitoringElem.getText() != null) {
            unifiedEndpoint.setMonitoringLogStatement(monitoringElem.getText());
        }
    }

    /**
     * Error Handling
     *
     * @param unifiedEndpoint
     * @param errorHandlingElem
     */
    public void extractErrorHandlingConfig(UnifiedEndpoint unifiedEndpoint, OMElement errorHandlingElem) {

        OMElement markForSuspensionElem = errorHandlingElem.getFirstChildWithName(
                UnifiedEndpointConstants.ERROR_HANDLING_MARK_SUSPENSION_Q);
        if (markForSuspensionElem != null) {
            if (markForSuspensionElem.getFirstChildWithName(
                    UnifiedEndpointConstants.ERROR_HANDLING_MARK_SUSPENSION_RETRIES_Q) != null) {
                unifiedEndpoint.setRetriesBeforeSuspension(Integer.parseInt(
                        markForSuspensionElem.getFirstChildWithName(
                                UnifiedEndpointConstants.ERROR_HANDLING_MARK_SUSPENSION_RETRIES_Q).getText()));
            }
            if (markForSuspensionElem.getFirstChildWithName(
                    UnifiedEndpointConstants.ERROR_HANDLING_MARK_SUSPENSION_RETRY_DELAY_Q) != null) {
                unifiedEndpoint.setRetryDelay(Long.parseLong(
                        markForSuspensionElem.getFirstChildWithName(
                                UnifiedEndpointConstants.ERROR_HANDLING_MARK_SUSPENSION_RETRY_DELAY_Q).getText()));
            }
            if (markForSuspensionElem.getFirstChildWithName(
                    UnifiedEndpointConstants.ERROR_HANDLING_MARK_SUSPENSION_ERROR_CODES_Q) != null) {
                String errorCodesStr = markForSuspensionElem.getFirstChildWithName(
                        UnifiedEndpointConstants.ERROR_HANDLING_MARK_SUSPENSION_ERROR_CODES_Q).getText();
                String[] errorCodes = errorCodesStr.split(",");
                if (errorCodes != null) {
                    for (String errorCode : errorCodes) {
                        if (errorCode != null && !errorCode.equals("")) {
                            unifiedEndpoint.addTimeOutErrorCode(Integer.parseInt(errorCode));
                        }
                    }
                }
            }
        }

        OMElement suspendOnFailureElem = errorHandlingElem.getFirstChildWithName(
                UnifiedEndpointConstants.ERROR_HANDLING_SUSPEND_ON_FAILURE_Q);
        if (suspendOnFailureElem != null) {
            if (suspendOnFailureElem.getFirstChildWithName(
                    UnifiedEndpointConstants.ERROR_HANDLING_SUSPEND_ON_FAILURE_INITIAL_DURATION_Q) != null) {
                unifiedEndpoint.setInitialDuration(Long.parseLong(
                        suspendOnFailureElem.getFirstChildWithName(
                                UnifiedEndpointConstants.ERROR_HANDLING_SUSPEND_ON_FAILURE_INITIAL_DURATION_Q).getText()));
            }
            if (suspendOnFailureElem.getFirstChildWithName(
                    UnifiedEndpointConstants.ERROR_HANDLING_SUSPEND_ON_FAILURE_PROGRESSION_FACTOR_Q) != null) {
                unifiedEndpoint.setProgressionFactor(Double.parseDouble(
                        suspendOnFailureElem.getFirstChildWithName(
                                UnifiedEndpointConstants.ERROR_HANDLING_SUSPEND_ON_FAILURE_PROGRESSION_FACTOR_Q).
                                getText()));
            }
            if (suspendOnFailureElem.getFirstChildWithName(
                    UnifiedEndpointConstants.ERROR_HANDLING_SUSPEND_ON_FAILURE_MAXIMUM_DURATION_Q) != null) {
                unifiedEndpoint.setMaximumDuration(Long.parseLong(
                        suspendOnFailureElem.getFirstChildWithName(
                                UnifiedEndpointConstants.ERROR_HANDLING_SUSPEND_ON_FAILURE_MAXIMUM_DURATION_Q).getText()));
            }

            if (suspendOnFailureElem.getFirstChildWithName(
                    UnifiedEndpointConstants.ERROR_HANDLING_SUSPEND_ON_FAILURE_ERROR_CODES_Q) != null) {
                String errorCodesStr = suspendOnFailureElem.getFirstChildWithName(
                        UnifiedEndpointConstants.ERROR_HANDLING_SUSPEND_ON_FAILURE_ERROR_CODES_Q).getText();
                String[] errorCodes = errorCodesStr.split(",");
                if (errorCodes != null) {
                    for (String errorCode : errorCodes) {
                        if (errorCode != null && !errorCode.equals("")) {
                            unifiedEndpoint.addSuspendErrorCode(Integer.parseInt(errorCode));
                        }
                    }
                }
            }
        }
    }

    /**
     * QoS
     *
     * @param unifiedEndpoint
     * @param qosElem
     */
    public void extractQoSConfig(UnifiedEndpoint unifiedEndpoint, OMElement qosElem) {

        OMElement enableRmElem = qosElem.getFirstChildWithName(UnifiedEndpointConstants.QOS_ENABLE_RM_Q);
        if (enableRmElem != null) {
            unifiedEndpoint.setRMEnabled(true);
            if (enableRmElem.getAttributeValue(UnifiedEndpointConstants.QOS_POLICY_Q) != null) {
                unifiedEndpoint.setWsRMPolicyKey(enableRmElem.getAttributeValue(UnifiedEndpointConstants.QOS_POLICY_Q));
            }
        }

        OMElement enableWsSecElem = qosElem.getFirstChildWithName(UnifiedEndpointConstants.QOS_ENABLE_WS_SEC_Q);
        if (enableWsSecElem != null) {
            unifiedEndpoint.setSecurityEnabled(true);
            if (enableWsSecElem.getAttributeValue(UnifiedEndpointConstants.QOS_POLICY_Q) != null) {
                unifiedEndpoint.setWsSecPolicyKey(enableWsSecElem.getAttributeValue(UnifiedEndpointConstants.QOS_POLICY_Q));
            }
        }

        OMElement enableAddressingElem = qosElem.getFirstChildWithName(UnifiedEndpointConstants.QOS_ENABLE_ADDRESSING_Q);
        if (enableAddressingElem != null) {
            unifiedEndpoint.setAddressingEnabled(true);
            if (enableAddressingElem.getAttributeValue(UnifiedEndpointConstants.QOS_VERSION_Q) != null) {
                unifiedEndpoint.setAddressingVersion(enableAddressingElem.getAttributeValue(
                        UnifiedEndpointConstants.QOS_VERSION_Q));
            }
            if (enableAddressingElem.getAttributeValue(
                    UnifiedEndpointConstants.QOS_ENABLE_ADDRESSING_SEPARATE_LISTENER_Q) != null) {
                unifiedEndpoint.setSeparateListener(Boolean.parseBoolean(
                        enableAddressingElem.getAttributeValue(
                                UnifiedEndpointConstants.QOS_ENABLE_ADDRESSING_SEPARATE_LISTENER_Q)));
            }
            if (enableAddressingElem.getFirstChildWithName(UnifiedEndpointConstants.QOS_ADDRESSING_REPLY_TO_Q) != null) {
                URI replyToAddress = URI.create(enableAddressingElem.getFirstChildWithName(
                        UnifiedEndpointConstants.QOS_ADDRESSING_REPLY_TO_Q).getText().trim());
                unifiedEndpoint.setReplyToAddress(replyToAddress);
            }
        }

        OMElement securityElem = qosElem.getFirstChildWithName(UnifiedEndpointConstants.QOS_SECURITY_Q);
        if (securityElem != null) {
            OMElement userPwdPairElem = securityElem.getFirstChildWithName(
                    UnifiedEndpointConstants.QOS_SECURITY_USER_PWD_PAIR_Q);
            if (userPwdPairElem != null) {
                if (userPwdPairElem.getAttributeValue(UnifiedEndpointConstants.QOS_SECURITY_USER_NAME_Q) != null) {
                    unifiedEndpoint.setSecUserName(userPwdPairElem.getAttributeValue(
                            UnifiedEndpointConstants.QOS_SECURITY_USER_NAME_Q));
                }
                if (userPwdPairElem.getAttributeValue(UnifiedEndpointConstants.QOS_SECURITY_PASSWORD_Q) != null) {
                    unifiedEndpoint.setSecPwd(userPwdPairElem.getAttributeValue(
                            UnifiedEndpointConstants.QOS_SECURITY_PASSWORD_Q));
                }

                if (userPwdPairElem.getFirstElement() != null) {
                    unifiedEndpoint.setSecPolicy(userPwdPairElem.getFirstElement());
                }
            }
        }
    }

    /**
     * Cluster : LB/FO
     *
     * @param unifiedEndpoint
     * @param clusterElem
     * @throws AxisFault
     */
    public void extractClusterConfig(UnifiedEndpoint unifiedEndpoint, OMElement clusterElem) throws AxisFault {

        UnifiedEndpointCluster uEPCluster = new UnifiedEndpointCluster();
        OMElement membershipOmElem = clusterElem.getFirstChildWithName(UnifiedEndpointConstants.CLUSTER_MEMBERSHIP_Q);
        if (membershipOmElem != null) {
            if (membershipOmElem.getAttributeValue(UnifiedEndpointConstants.CLUSTER_MEMBERSHIP_HANDLER_Q) != null) {
                uEPCluster.setMembershipHandler(membershipOmElem.getAttributeValue(
                        UnifiedEndpointConstants.CLUSTER_MEMBERSHIP_HANDLER_Q));
            }

            /*UEP Group*/
            Iterator clusterUepIterator = membershipOmElem.getChildrenWithName(UnifiedEndpointConstants.UNIFIED_EPR_Q);
            while (clusterUepIterator.hasNext()) {
                OMElement clusterElement = (OMElement) clusterUepIterator.next();
                /*Recursive call to create UEP*/
                uEPCluster.addClusteredUnifiedEndpoint(createEndpoint(clusterElement));
            }

            /*EP Group with members url*/
            Iterator memberIterator = membershipOmElem.getChildrenWithName(UnifiedEndpointConstants.CLUSTER_MEMBER_Q);
            while (memberIterator.hasNext()) {
                OMElement memberElem = (OMElement) memberIterator.next();
                if (memberElem != null
                    && memberElem.getAttributeValue(UnifiedEndpointConstants.CLUSTER_MEMBER_URL_Q) != null) {
                    uEPCluster.addClusteredEndpointUrlList(memberElem.getAttributeValue(
                            UnifiedEndpointConstants.CLUSTER_MEMBER_URL_Q));
                }
            }
        }

        /*LB*/
        OMElement lbElem = clusterElem.getFirstChildWithName(UnifiedEndpointConstants.CLUSTER_LOAD_BALANCE_Q);
        if (lbElem != null) {
            uEPCluster.setLoadBalancing(true);
            if (lbElem.getAttributeValue(UnifiedEndpointConstants.CLUSTER_LOAD_BALANCE_POLICY_Q) != null) {
                uEPCluster.setLoadBalancingPolicy(lbElem.getAttributeValue(
                        UnifiedEndpointConstants.CLUSTER_LOAD_BALANCE_POLICY_Q));
            }
            if (lbElem.getAttributeValue(UnifiedEndpointConstants.CLUSTER_LOAD_BALANCE_ALGORITHM_Q) != null) {
                uEPCluster.setLoadBalancingAlgorithm(lbElem.getAttributeValue(
                        UnifiedEndpointConstants.CLUSTER_LOAD_BALANCE_ALGORITHM_Q));
            }
        }

        /*FO*/
        if (clusterElem.getFirstChildWithName(UnifiedEndpointConstants.CLUSTER_FAIL_OVER_Q) != null) {
            uEPCluster.setFailOver(true);
        }
        unifiedEndpoint.setUnifiedEndpointCluster(uEPCluster);
    }
}
