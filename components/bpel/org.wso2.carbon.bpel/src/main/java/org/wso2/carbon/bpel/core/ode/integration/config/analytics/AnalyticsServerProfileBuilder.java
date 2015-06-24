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

package org.wso2.carbon.bpel.core.ode.integration.config.analytics;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bpel.core.BPELConstants;
import org.wso2.carbon.bpel.core.internal.BPELServiceComponent;
import org.wso2.carbon.bpel.core.ode.integration.store.BPELDeploymentException;
import org.wso2.carbon.core.util.CryptoException;
import org.wso2.carbon.core.util.CryptoUtil;
import org.wso2.carbon.registry.api.Registry;
import org.wso2.carbon.registry.api.RegistryException;
import org.wso2.carbon.registry.api.Resource;
import org.wso2.carbon.unifiedendpoint.core.UnifiedEndpointConstants;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;

/**
 * Builds the Analytics Server Profile {@link AnalyticsServerProfile}
 */
public class AnalyticsServerProfileBuilder {
    private String profileLocation;
    private Integer tenantId;
    private static Log log = LogFactory.getLog(AnalyticsServerProfileBuilder.class);

    public AnalyticsServerProfileBuilder(String profileLocation, Integer tenantId) {
        this.profileLocation = profileLocation;
        this.tenantId = tenantId;
    }

    public AnalyticsServerProfile build() {
        AnalyticsServerProfile analyticsServerProfile = new AnalyticsServerProfile();
        Registry registry;
        String location;

            if (profileLocation.startsWith(UnifiedEndpointConstants.VIRTUAL_CONF_REG)) {
                try {
                    registry =
                        BPELServiceComponent.getRegistryService().getConfigSystemRegistry(tenantId);
                    location = profileLocation.substring(profileLocation.indexOf(
                            UnifiedEndpointConstants.VIRTUAL_CONF_REG) +
                            UnifiedEndpointConstants.VIRTUAL_CONF_REG.length());
                    loadAnalyticsProfileFromRegistry(analyticsServerProfile, registry, location);
                }catch (RegistryException re) {
                    String errMsg ="Error while loading config registry";
                    handleError(errMsg, re);
                }

            } else if (profileLocation.startsWith(UnifiedEndpointConstants.VIRTUAL_GOV_REG)) {
                try {
                    registry =  BPELServiceComponent.getRegistryService().getGovernanceSystemRegistry(tenantId);
                    location = profileLocation.substring(profileLocation.indexOf(
                            UnifiedEndpointConstants.VIRTUAL_GOV_REG) +
                            UnifiedEndpointConstants.VIRTUAL_GOV_REG.length());
                    loadAnalyticsProfileFromRegistry(analyticsServerProfile, registry, location);
                } catch (RegistryException re) {
                    String errMsg ="Error while loading governance registry";
                    handleError(errMsg, re);
                }
            } else if (profileLocation.startsWith(UnifiedEndpointConstants.VIRTUAL_REG)) {
                try {
                    registry = BPELServiceComponent.getRegistryService().getLocalRepository(tenantId);
                    location = profileLocation.substring(profileLocation.indexOf(
                            UnifiedEndpointConstants.VIRTUAL_REG) +
                            UnifiedEndpointConstants.VIRTUAL_REG.length());
                    loadAnalyticsProfileFromRegistry(analyticsServerProfile, registry, location);
                } catch (RegistryException re) {
                    String errMsg ="Error while loading local registry";
                    handleError(errMsg, re);
                }
            } else if (profileLocation.startsWith(UnifiedEndpointConstants.VIRTUAL_FILE)) {
                location = profileLocation.substring(profileLocation.indexOf(
                        UnifiedEndpointConstants.VIRTUAL_FILE) +
                        UnifiedEndpointConstants.VIRTUAL_FILE.length());
                loadAnalyticsProfileFromFileSystem(analyticsServerProfile, location);
            } else {
                String errMsg = "Invalid analytics profile location: " + profileLocation;
                handleError(errMsg);
            }

            return analyticsServerProfile;
    }

    private void loadAnalyticsProfileFromFileSystem(AnalyticsServerProfile analyticsServerProfile, String location) {
        File file = new File(location);
        if(file.exists()) {
            try {
                String profileFileContent = FileUtils.readFileToString(file , "UTF-8");
                OMElement resourceElement = new StAXOMBuilder(new ByteArrayInputStream(profileFileContent.getBytes())).getDocumentElement();
                processAnalyticsServerProfileName(resourceElement, analyticsServerProfile);
                processCredentialElement(resourceElement, analyticsServerProfile);
                processConnectionElement(resourceElement, analyticsServerProfile);
                processKeyStoreElement(resourceElement, analyticsServerProfile);
                processStreamsElement(resourceElement, analyticsServerProfile);
            } catch (IOException e) {
                String errMsg = "Error occurred while reading the file from file system: " +
                        location + "to build the Analytics server profile:" + profileLocation;
                handleError(errMsg, e);
            } catch (XMLStreamException e) {
                String errMsg = "Error occurred while creating the OMElement out of Analytics server " +
                        "profile: " + profileLocation;
                handleError(errMsg, e);
            } catch (CryptoException e) {
                String errMsg = "Error occurred while decrypting password in Analytics server profile: " +
                        profileLocation;
                handleError(errMsg, e);
            }
        }
    }

    private void loadAnalyticsProfileFromRegistry(AnalyticsServerProfile analyticsServerProfile, Registry registry,
            String location) {
        try {
            if (registry.resourceExists(location)) {
                Resource resource = registry.get(location);
                String resourceContent = new String((byte[]) resource.getContent());
                OMElement resourceElement = new StAXOMBuilder(new ByteArrayInputStream(resourceContent.getBytes())).getDocumentElement();
                processAnalyticsServerProfileName(resourceElement, analyticsServerProfile);
                processCredentialElement(resourceElement, analyticsServerProfile);
                processConnectionElement(resourceElement, analyticsServerProfile);
                processKeyStoreElement(resourceElement, analyticsServerProfile);
                processStreamsElement(resourceElement, analyticsServerProfile);
            } else {
                String errMsg = "The resource: " + location + " does not exist.";
                handleError(errMsg);
            }
        } catch (RegistryException e) {
            String errMsg = "Error occurred while reading the resource from registry: " +
                    location + " to build the Analytics server profile: " + profileLocation;
            handleError(errMsg, e);
        } catch (XMLStreamException e) {
            String errMsg = "Error occurred while creating the OMElement out of Analytics server " +
                    "profile: " + profileLocation;
            handleError(errMsg, e);
        } catch (CryptoException e) {
            String errMsg = "Error occurred while decrypting password in Analytics server profile: " +
                    profileLocation;
            handleError(errMsg, e);
        }
    }

    private void processAnalyticsServerProfileName(OMElement analyticsServerConfig,
            AnalyticsServerProfile analyticsServerProfile) {
        OMAttribute name = analyticsServerConfig.getAttribute(new QName("name"));
        if (name != null) {
            analyticsServerProfile.setName(name.getAttributeValue());
        } else {
            String errMsg = "Name attribute not found for Analytics server profile: " + profileLocation;
            handleError(errMsg);
        }
    }

    private void processCredentialElement(OMElement analyticsServerConfig,
                                          AnalyticsServerProfile analyticsServerProfile) throws CryptoException {
        OMElement credentialElement = analyticsServerConfig.getFirstChildWithName(
                new QName(BPELConstants.ANALYTICS_SERVER_PROFILE_NS, "Credential"));
        if (credentialElement != null) {
            OMAttribute userNameAttr = credentialElement.getAttribute(new QName("userName"));
            OMAttribute passwordAttr = credentialElement.getAttribute(new QName("password"));
            //OMAttribute secureAttr = credentialElement.getAttribute(new QName("secure"));
            if (userNameAttr != null && passwordAttr != null && /*secureAttr != null &&*/
                    !userNameAttr.getAttributeValue().equals("") &&
                    !passwordAttr.getAttributeValue().equals("")
                /*&& !secureAttr.getAttributeValue().equals("")*/) {
                analyticsServerProfile.setUserName(userNameAttr.getAttributeValue());
                analyticsServerProfile.setPassword(new String(CryptoUtil.getDefaultCryptoUtil().
                        base64DecodeAndDecrypt(passwordAttr.getAttributeValue())));
            } else {
                String errMsg = "Username or Password not found for Analytics server profile: " +
                        profileLocation;
                handleError(errMsg);
            }
        } else {
            String errMsg = "Credentials not found for Analytics server profile: " + profileLocation;
            handleError(errMsg);
        }
    }

    private void processConnectionElement(OMElement analyticsServerConfig,
                                          AnalyticsServerProfile analyticsServerProfile) {
        OMElement connectionElement = analyticsServerConfig.getFirstChildWithName(
                new QName(BPELConstants.ANALYTICS_SERVER_PROFILE_NS, "Connection"));
        if (connectionElement != null) {
            OMAttribute loadBalanceAttr = connectionElement.getAttribute(new QName("enableLoadBalancing"));
            OMAttribute urlAttr = connectionElement.getAttribute(new QName("url"));
            if (urlAttr != null && loadBalanceAttr != null &&
                    !urlAttr.getAttributeValue().equals("") && !loadBalanceAttr.getAttributeValue().equals("")) {
                analyticsServerProfile.setUrl(urlAttr.getAttributeValue());
                if ("true".equals(loadBalanceAttr.getAttributeValue())) {
                    analyticsServerProfile.setLoadBalanced(true);
                } else if ("false".equals(loadBalanceAttr.getAttributeValue())) {
                    analyticsServerProfile.setLoadBalanced(false);
                } else {
                    String errMsg = "Invalid value found for loadBalancing element in analytics server profile: " +
                            profileLocation;
                    handleError(errMsg);
                }
            } else {
                String errMsg = "Connection details are missing for Analytics server profile: " +
                        profileLocation;
                handleError(errMsg);
            }
        } else {
            String errMsg = "Connection details not found for Analytics server profile: " + profileLocation;
            handleError(errMsg);
        }
    }

    private void processKeyStoreElement(OMElement analyticsServerConfig,
                                        AnalyticsServerProfile analyticsServerProfile) {
        OMElement keyStoreElement = analyticsServerConfig.getFirstChildWithName(
                new QName(BPELConstants.ANALYTICS_SERVER_PROFILE_NS, "KeyStore"));
        if (keyStoreElement != null) {
            OMAttribute locationAttr = keyStoreElement.getAttribute(new QName("location"));
            OMAttribute passwordAttr = keyStoreElement.getAttribute(new QName("password"));
            if (locationAttr != null && passwordAttr != null &&
                    !locationAttr.getAttributeValue().equals("") &&
                    !passwordAttr.getAttributeValue().equals("")) {
                analyticsServerProfile.setKeyStoreLocation(locationAttr.getAttributeValue());

                try{
                    analyticsServerProfile.setKeyStorePassword(
                        new String(CryptoUtil.getDefaultCryptoUtil().base64DecodeAndDecrypt(
                                passwordAttr.getAttributeValue())));

                } catch (CryptoException e) {
                    String errMsg = "Error occurred while decrypting password in Analytics server profile: " +
                            profileLocation;
                    handleError(errMsg, e);
                }
            } else {
                String errMsg = "Key store details location or password not found for Analytics server " +
                        "server profile: " + profileLocation;
                handleError(errMsg);
            }
        } else {
            String errMsg = "Key store element not found for Analytics server profile: " + profileLocation;
            /** handle this with new api
            log.warn("Key store element not specified");
             **/
        }
    }

    private void processStreamsElement(OMElement analyticsServerConfigElement,
                                       AnalyticsServerProfile analyticsServerProfile) {
        OMElement streamsElement = analyticsServerConfigElement.getFirstChildWithName(
                new QName(BPELConstants.ANALYTICS_SERVER_PROFILE_NS, "Streams"));
        if (streamsElement == null) {
            log.warn("No Streams found for Analytics server profile: " + profileLocation);
        } else {
            Iterator itr = streamsElement.getChildrenWithName(
                    new QName(BPELConstants.ANALYTICS_SERVER_PROFILE_NS, "Stream"));
            OMElement streamElement;
            while (itr.hasNext()) {
                streamElement = (OMElement) itr.next();
                processStreamElement(streamElement, analyticsServerProfile);
            }
        }
    }

    private void processStreamElement(OMElement streamElement,
                                      AnalyticsServerProfile analyticsServerProfile) {
        OMAttribute nameAttr = streamElement.getAttribute(new QName("name"));
        OMAttribute versionAttr = streamElement.getAttribute(new QName("version"));
        OMAttribute nickNameAttr = streamElement.getAttribute(new QName("nickName"));
        OMAttribute descriptionAttr = streamElement.getAttribute(new QName("description"));
        if (nameAttr != null && nickNameAttr != null && descriptionAttr != null &&
                !nameAttr.getAttributeValue().equals("") &&
                !nickNameAttr.getAttributeValue().equals("") &&
                !descriptionAttr.getAttributeValue().equals("")) {
            AnalyticsStreamConfiguration streamConfiguration =
                    new AnalyticsStreamConfiguration(nameAttr.getAttributeValue(),
                            nickNameAttr.getAttributeValue(), descriptionAttr.getAttributeValue(),
                            versionAttr.getAttributeValue());
            OMElement dataElement = streamElement.getFirstChildWithName(
                    new QName(BPELConstants.ANALYTICS_SERVER_PROFILE_NS, "Data"));
            if (dataElement == null) {
                String errMsg = "Data element is not available for Analytics server profile: " +
                        profileLocation;
                handleError(errMsg);
            }
            Iterator itr = dataElement.getChildrenWithName(
                    new QName(BPELConstants.ANALYTICS_SERVER_PROFILE_NS, "Key"));
            OMElement keyElement;
            while (itr.hasNext()) {
                keyElement = (OMElement) itr.next();
                processKeyElement(keyElement, streamConfiguration);
            }
            analyticsServerProfile.addAnalyticsStreamConfiguration(streamConfiguration.getName(),
                    streamConfiguration.getVersion(), streamConfiguration);
        } else {
            String errMsg = "One or many of the attributes of Stream element is not available " +
                    "for Analytics server profile: " + profileLocation;
            handleError(errMsg);
        }

    }

    private void processKeyElement(OMElement keyElement,
                                   AnalyticsStreamConfiguration streamConfiguration) {

        AnalyticsKey analyticsKey;
        String name;
        AnalyticsKey.AnalyticsKeyType type = AnalyticsKey.AnalyticsKeyType.NONE;
        String variable;
        String expression;
        String part = null;
        String query = null;

        OMAttribute nameAttribute = keyElement.getAttribute(new QName("name"));
        if (nameAttribute == null || "".equals(nameAttribute.getAttributeValue())) {
            String errMsg = "name attribute of Key element cannot be null for Analytics server " +
                    "profile: " + profileLocation;
            handleError(errMsg);
        }
        name = nameAttribute.getAttributeValue();

        OMAttribute typeAttribute = keyElement.getAttribute(new QName("type"));
        if (typeAttribute == null || "".equals(typeAttribute.getAttributeValue())) {
            type = AnalyticsKey.AnalyticsKeyType.PAYLOAD;
            log.debug("type attribute of Key element: " + name + " is not available. " +
                    "Type is default to payload");
        }
        try {
            type = AnalyticsKey.AnalyticsKeyType.valueOf(typeAttribute.getAttributeValue().toUpperCase());
        } catch (IllegalArgumentException e) {
            String errMsg = "Invalid key type specified for key name " + name;
            handleError(errMsg, e);
        }


        OMElement fromElement = keyElement.getFirstChildWithName(
                new QName(BPELConstants.ANALYTICS_SERVER_PROFILE_NS, "From"));
        if (fromElement == null) {
            String errMsg = "From element not found for Key element: " + name + " for Analytics server " +
                    "profile: " + profileLocation;
            handleError(errMsg);
        }

        OMAttribute variableAttribute = fromElement.getAttribute(new QName("variable"));
        if (variableAttribute != null && !"".equals(variableAttribute.getAttributeValue())) {
            variable = variableAttribute.getAttributeValue();

            OMAttribute partAttribute = fromElement.getAttribute(new QName("part"));
            if (partAttribute != null) {
                part = partAttribute.getAttributeValue();
            }

            OMElement queryElement = fromElement.getFirstChildWithName(
                    new QName(BPELConstants.ANALYTICS_SERVER_PROFILE_NS, "Query"));
            if (queryElement != null && ( "".equals(queryElement.getText()) == false)) {
                query = queryElement.getText();
            }
            analyticsKey = new AnalyticsKey(name, variable, part, query, type);
        } else {
            if (fromElement.getText() == null || "".equals(fromElement.getText())) {
                String errMsg = "Variable name or XPath expression not found for From of Key: " +
                        name + " for Analytics server profile: " + profileLocation;
                handleError(errMsg);
            }
            expression = fromElement.getText().trim();
            analyticsKey = new AnalyticsKey(name, type);
            analyticsKey.setExpression(expression);
        }

        switch (analyticsKey.getType()) {
            case PAYLOAD:
                streamConfiguration.addPayloadAnalyticsKey(analyticsKey);
                break;
            case CORRELATION:
                streamConfiguration.addCorrelationAnalyticsKey(analyticsKey);
                break;
            case META:
                streamConfiguration.addMetaAnalyticsKey(analyticsKey);
                break;
            default:
                String errMsg = "Unknown Analytics key type: " + type + " with Analytics key name: " +
                        analyticsKey.getName() +" in stream: " + streamConfiguration.getName();
                handleError(errMsg);
        }
    }

    private void handleError(String errMsg) {
        log.error(errMsg);
        throw new BPELDeploymentException(errMsg);
    }

    private void handleError(String errMsg, Exception e) {
        log.error(errMsg, e);
        throw new BPELDeploymentException(errMsg, e);
    }
}
