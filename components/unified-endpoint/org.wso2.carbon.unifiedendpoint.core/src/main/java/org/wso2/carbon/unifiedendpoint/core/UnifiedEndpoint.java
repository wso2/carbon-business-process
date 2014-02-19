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
import org.apache.axis2.addressing.EndpointReference;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * This is the class to represent an UnifiedEndpoint. To be compatible with existing endpoints,
 * this extends from EndpointReference.
 */
public class UnifiedEndpoint extends EndpointReference {

    /** UEP ID */
    private String uepId;

    /** WS-Discovery */
    private String discoveryUuid;
    private String discoveryScope;
    private String discoveryType;

    /** WSDL based EP */
    private OMElement wsdl11Definitions;

    /** MessageOutput */
    private UnifiedEndpointMessageOutput messageOutput;

    /** Transport */
    private UnifiedEndpointTransport transport;
    private String authorizationUserName;
    private String authorizationPassword;

    /** Timeout */
    UnifiedEndpointTimeout timeout;

    /** Monitoring */
    private boolean isStatisticEnabled;
    private boolean isTraceEnabled;
    private String monitoringLogStatement;

    /** Error Handling */
    /** Mark for suspension on  Failure */
    private final List<Integer> timeoutErrorCodes = new ArrayList<Integer>();
    private int retriesBeforeSuspension = -1;
    private long retryDelay = -1;

    /** Suspend on Failure */
    private final List<Integer> suspendErrorCodes = new ArrayList<Integer>();
    private long initialDuration = -1;
    private double progressionFactor = 0.0;
    private long maximumDuration = -1;


    /** QoS */
    private boolean isRMEnabled = false;
    private String wsRMPolicyKey = null;

    private boolean isSecurityEnabled = false;
    private String wsSecPolicyKey = null;

    private boolean isAddressingEnabled = false;
    private String addressingVersion;
    private boolean isSeparateListener;
    private URI replyToAddress;

    /** Security */
    private String secUserName;
    private String secPwd;
    private OMElement secPolicy;

    /** Session */
    private String sessionType;

    /** Load Balancing or Fail Over */
    private UnifiedEndpointCluster unifiedEndpointCluster;


    /** Metadata Serializer status */
    private boolean isUepMetadataSerialized = false;

    public String getUepId() {
        return uepId;
    }

    public void setUepId(String uepId) {
        this.uepId = uepId;
    }

    public String getDiscoveryUuid() {
        return discoveryUuid;
    }

    public void setDiscoveryUuid(String discoveryUuid) {
        this.discoveryUuid = discoveryUuid;
    }

    public String getDiscoveryScope() {
        return discoveryScope;
    }

    public void setDiscoveryScope(String discoveryScope) {
        this.discoveryScope = discoveryScope;
    }

    public String getDiscoveryType() {
        return discoveryType;
    }

    public void setDiscoveryType(String discoveryType) {
        this.discoveryType = discoveryType;
    }

    public boolean isUepMetadataSerialized() {
        return isUepMetadataSerialized;
    }

    public void setUepMetadataSerialized(boolean uepMetadataSerialized) {
        isUepMetadataSerialized = uepMetadataSerialized;
    }

    public boolean isAddressingEnabled() {
        return isAddressingEnabled;
    }

    public void setAddressingEnabled(boolean addressingEnabled) {
        isAddressingEnabled = addressingEnabled;
    }

    public OMElement getWsdl11Definitions() {
        return wsdl11Definitions;
    }

    public void setWsdl11Definitions(OMElement wsdl11Definitions) {
        this.wsdl11Definitions = wsdl11Definitions;
    }

    public UnifiedEndpointMessageOutput getMessageOutput() {
        return messageOutput;
    }

    public void setMessageOutput(UnifiedEndpointMessageOutput messageOutput) {
        this.messageOutput = messageOutput;
    }

    public UnifiedEndpointTransport getTransport() {
        return transport;
    }

    public void setTransport(UnifiedEndpointTransport transport) {
        this.transport = transport;
    }

    public UnifiedEndpointTimeout getTimeout() {
        return timeout;
    }

    public void setTimeout(UnifiedEndpointTimeout timeout) {
        this.timeout = timeout;
    }

    public boolean isStatisticEnabled() {
        return isStatisticEnabled;
    }

    public void setStatisticEnabled(boolean statisticEnabled) {
        isStatisticEnabled = statisticEnabled;
    }

    public boolean isTraceEnabled() {
        return isTraceEnabled;
    }

    public void setTraceEnabled(boolean traceEnabled) {
        isTraceEnabled = traceEnabled;
    }

    public String getMonitoringLogStatement() {
        return monitoringLogStatement;
    }

    public void setMonitoringLogStatement(String monitoringLogStatement) {
        this.monitoringLogStatement = monitoringLogStatement;
    }

    public int getRetriesBeforeSuspension() {
        return retriesBeforeSuspension;
    }

    public void setRetriesBeforeSuspension(int retriesBeforeSuspension) {
        this.retriesBeforeSuspension = retriesBeforeSuspension;
    }

    public long getRetryDelay() {
        return retryDelay;
    }

    public void setRetryDelay(long retryDelay) {
        this.retryDelay = retryDelay;
    }

    public long getInitialDuration() {
        return initialDuration;
    }

    public void setInitialDuration(long initialDuration) {
        this.initialDuration = initialDuration;
    }

    public double getProgressionFactor() {
        return progressionFactor;
    }

    public void setProgressionFactor(double progressionFactor) {
        this.progressionFactor = progressionFactor;
    }

    public long getMaximumDuration() {
        return maximumDuration;
    }

    public void setMaximumDuration(long maximumDuration) {
        this.maximumDuration = maximumDuration;
    }

    public boolean isRMEnabled() {
        return isRMEnabled;
    }

    public void setRMEnabled(boolean RMEnabled) {
        isRMEnabled = RMEnabled;
    }

    public String getWsRMPolicyKey() {
        return wsRMPolicyKey;
    }

    public void setWsRMPolicyKey(String wsRMPolicyKey) {
        this.wsRMPolicyKey = wsRMPolicyKey;
    }

    public boolean isSecurityEnabled() {
        return isSecurityEnabled;
    }

    public void setSecurityEnabled(boolean securityEnabled) {
        isSecurityEnabled = securityEnabled;
    }

    public String getWsSecPolicyKey() {
        return wsSecPolicyKey;
    }

    public void setWsSecPolicyKey(String wsSecPolicyKey) {
        this.wsSecPolicyKey = wsSecPolicyKey;
    }

    public String getAddressingVersion() {
        return addressingVersion;
    }

    public void setAddressingVersion(String addressingVersion) {
        this.addressingVersion = addressingVersion;
    }

    public boolean isSeparateListener() {
        return isSeparateListener;
    }

    public void setSeparateListener(boolean separateListener) {
        isSeparateListener = separateListener;
    }

    public URI getReplyToAddress() {
        return replyToAddress;
    }

    public void setReplyToAddress(URI replyToAddress) {
        this.replyToAddress = replyToAddress;
    }

    public String getSecUserName() {
        return secUserName;
    }

    public void setSecUserName(String secUserName) {
        this.secUserName = secUserName;
    }

    public String getSecPwd() {
        return secPwd;
    }

    public void setSecPwd(String secPwd) {
        this.secPwd = secPwd;
    }

    public OMElement getSecPolicy() {
        return secPolicy;
    }

    public void setSecPolicy(OMElement secPolicy) {
        this.secPolicy = secPolicy;
    }

    public String getSessionType() {
        return sessionType;
    }

    public void setSessionType(String sessionType) {
        this.sessionType = sessionType;
    }

    public UnifiedEndpointCluster getUnifiedEndpointCluster() {
        return unifiedEndpointCluster;
    }

    public void setUnifiedEndpointCluster(UnifiedEndpointCluster unifiedEndpointCluster) {
        this.unifiedEndpointCluster = unifiedEndpointCluster;
    }

    public List<Integer> getTimeoutErrorCodes() {
        return timeoutErrorCodes;
    }

    public void addTimeOutErrorCode(int timeOutErrorCode) {
        timeoutErrorCodes.add(timeOutErrorCode);

    }

    public List<Integer> getSuspendErrorCodes() {
        return suspendErrorCodes;
    }

    public void addSuspendErrorCode(int suspendErrorCode) {
        suspendErrorCodes.add(suspendErrorCode);

    }

    public String getAuthorizationUserName() {
        return authorizationUserName;
    }

    public void setAuthorizationUserName(String authorizationUserName) {
        this.authorizationUserName = authorizationUserName;
    }

    public String getAuthorizationPassword() {
        return authorizationPassword;
    }

    public void setAuthorizationPassword(String authorizationPassword) {
        this.authorizationPassword = authorizationPassword;
    }

}
