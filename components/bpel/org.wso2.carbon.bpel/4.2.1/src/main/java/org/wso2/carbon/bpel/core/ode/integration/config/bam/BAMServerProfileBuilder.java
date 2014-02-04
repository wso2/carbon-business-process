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

package org.wso2.carbon.bpel.core.ode.integration.config.bam;

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
 * Builds the BAM Server Profile {@link BAMServerProfile}
 */
public class BAMServerProfileBuilder {
    private String profileLocation;
    private Integer tenantId;
    private static Log log = LogFactory.getLog(BAMServerProfileBuilder.class);

    public BAMServerProfileBuilder(String profileLocation, Integer tenantId) {
        this.profileLocation = profileLocation;
        this.tenantId = tenantId;
    }

    public BAMServerProfile build() {
        BAMServerProfile bamServerProfile = new BAMServerProfile();
        Registry registry;
        String location;

            if (profileLocation.startsWith(UnifiedEndpointConstants.VIRTUAL_CONF_REG)) {
                try {
                    registry =
                        BPELServiceComponent.getRegistryService().getConfigSystemRegistry(tenantId);
                    location = profileLocation.substring(profileLocation.indexOf(
                            UnifiedEndpointConstants.VIRTUAL_CONF_REG) +
                            UnifiedEndpointConstants.VIRTUAL_CONF_REG.length());
                    loadBAMProfileFromRegistry(bamServerProfile, registry, location);
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
                    loadBAMProfileFromRegistry(bamServerProfile, registry, location);
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
                    loadBAMProfileFromRegistry(bamServerProfile, registry, location);
                } catch (RegistryException re) {
                    String errMsg ="Error while loading local registry";
                    handleError(errMsg, re);
                }
            } else if (profileLocation.startsWith(UnifiedEndpointConstants.VIRTUAL_FILE)) {
                location = profileLocation.substring(profileLocation.indexOf(
                        UnifiedEndpointConstants.VIRTUAL_FILE) +
                        UnifiedEndpointConstants.VIRTUAL_FILE.length());
                loadBAMProfileFromFileSystem(bamServerProfile, location);
            } else {
                String errMsg = "Invalid bam profile location: " + profileLocation;
                handleError(errMsg);
            }

            return bamServerProfile;
    }

    private void loadBAMProfileFromFileSystem(BAMServerProfile bamServerProfile, String location) {
        File file = new File(location);
        if(file.exists()) {
            try {
                String profileFileContent = FileUtils.readFileToString(file);
                OMElement resourceElement = new StAXOMBuilder(new ByteArrayInputStream(profileFileContent.getBytes())).getDocumentElement();
                processBAMServerProfileName(resourceElement, bamServerProfile);
                processCredentialElement(resourceElement, bamServerProfile);
                processConnectionElement(resourceElement, bamServerProfile);
                processKeyStoreElement(resourceElement, bamServerProfile);
                processStreamsElement(resourceElement, bamServerProfile);
            } catch (IOException e) {
                String errMsg = "Error occurred while reading the file from file system: " +
                        location + "to build the BAM server profile:" + profileLocation;
                handleError(errMsg, e);
            } catch (XMLStreamException e) {
                String errMsg = "Error occurred while creating the OMElement out of BAM server " +
                        "profile: " + profileLocation;
                handleError(errMsg, e);
            } catch (CryptoException e) {
                String errMsg = "Error occurred while decrypting password in BAM server profile: " +
                        profileLocation;
                handleError(errMsg, e);
            }
        }
    }

    private void loadBAMProfileFromRegistry(BAMServerProfile bamServerProfile, Registry registry, String location) {
        try {
            if (registry.resourceExists(location)) {
                Resource resource = registry.get(location);
                String resourceContent = new String((byte[]) resource.getContent());
                OMElement resourceElement = new StAXOMBuilder(new ByteArrayInputStream(resourceContent.getBytes())).getDocumentElement();
                processBAMServerProfileName(resourceElement, bamServerProfile);
                processCredentialElement(resourceElement, bamServerProfile);
                processConnectionElement(resourceElement, bamServerProfile);
                processKeyStoreElement(resourceElement, bamServerProfile);
                processStreamsElement(resourceElement, bamServerProfile);
            } else {
                String errMsg = "The resource: " + location + " does not exist.";
                handleError(errMsg);
            }
        } catch (RegistryException e) {
            String errMsg = "Error occurred while reading the resource from registry: " +
                    location + " to build the BAM server profile: " + profileLocation;
            handleError(errMsg, e);
        } catch (XMLStreamException e) {
            String errMsg = "Error occurred while creating the OMElement out of BAM server " +
                    "profile: " + profileLocation;
            handleError(errMsg, e);
        } catch (CryptoException e) {
            String errMsg = "Error occurred while decrypting password in BAM server profile: " +
                    profileLocation;
            handleError(errMsg, e);
        }
    }

    private void processBAMServerProfileName(OMElement bamServerConfig,
                                             BAMServerProfile bamServerProfile) {
        OMAttribute name = bamServerConfig.getAttribute(new QName("name"));
        if (name != null) {
            bamServerProfile.setName(name.getAttributeValue());
        } else {
            String errMsg = "Name attribute not found for BAM server profile: " + profileLocation;
            handleError(errMsg);
        }
    }

    private void processCredentialElement(OMElement bamServerConfig,
                                          BAMServerProfile bamServerProfile) throws CryptoException {
        OMElement credentialElement = bamServerConfig.getFirstChildWithName(
                new QName(BPELConstants.BAM_SERVER_PROFILE_NS, "Credential"));
        if (credentialElement != null) {
            OMAttribute userNameAttr = credentialElement.getAttribute(new QName("userName"));
            OMAttribute passwordAttr = credentialElement.getAttribute(new QName("password"));
            //OMAttribute secureAttr = credentialElement.getAttribute(new QName("secure"));
            if (userNameAttr != null && passwordAttr != null && /*secureAttr != null &&*/
                    !userNameAttr.getAttributeValue().equals("") &&
                    !passwordAttr.getAttributeValue().equals("")
                /*&& !secureAttr.getAttributeValue().equals("")*/) {
                bamServerProfile.setUserName(userNameAttr.getAttributeValue());
                bamServerProfile.setPassword(new String(CryptoUtil.getDefaultCryptoUtil().
                        base64DecodeAndDecrypt(passwordAttr.getAttributeValue())));
            } else {
                String errMsg = "Username or Password not found for BAM server profile: " +
                        profileLocation;
                handleError(errMsg);
            }
        } else {
            String errMsg = "Credentials not found for BAM server profile: " + profileLocation;
            handleError(errMsg);
        }
    }

    private void processConnectionElement(OMElement bamServerConfig,
                                          BAMServerProfile bamServerProfile) {
        OMElement connectionElement = bamServerConfig.getFirstChildWithName(
                new QName(BPELConstants.BAM_SERVER_PROFILE_NS, "Connection"));
        if (connectionElement != null) {
            OMAttribute loadBalanceAttr = connectionElement.getAttribute(new QName("enableLoadBalancing"));
            OMAttribute urlAttr = connectionElement.getAttribute(new QName("url"));
            if (urlAttr != null && loadBalanceAttr != null &&
                    !urlAttr.getAttributeValue().equals("") && !loadBalanceAttr.getAttributeValue().equals("")) {
                bamServerProfile.setUrl(urlAttr.getAttributeValue());
                if ("true".equals(loadBalanceAttr.getAttributeValue())) {
                    bamServerProfile.setLoadBalanced(true);
                } else if ("false".equals(loadBalanceAttr.getAttributeValue())) {
                    bamServerProfile.setLoadBalanced(false);
                } else {
                    String errMsg = "Invalid value found for loadBalancing element in BAM server profile: " +
                            profileLocation;
                    handleError(errMsg);
                }
            } else {
                String errMsg = "Connection details are missing for BAM server profile: " +
                        profileLocation;
                handleError(errMsg);
            }
        } else {
            String errMsg = "Connection details not found for BAM server profile: " + profileLocation;
            handleError(errMsg);
        }
    }

    private void processKeyStoreElement(OMElement bamServerConfig,
                                        BAMServerProfile bamServerProfile) {
        OMElement keyStoreElement = bamServerConfig.getFirstChildWithName(
                new QName(BPELConstants.BAM_SERVER_PROFILE_NS, "KeyStore"));
        if (keyStoreElement != null) {
            OMAttribute locationAttr = keyStoreElement.getAttribute(new QName("location"));
            OMAttribute passwordAttr = keyStoreElement.getAttribute(new QName("password"));
            if (locationAttr != null && passwordAttr != null &&
                    !locationAttr.getAttributeValue().equals("") &&
                    !passwordAttr.getAttributeValue().equals("")) {
                bamServerProfile.setKeyStoreLocation(locationAttr.getAttributeValue());

                try{
                    bamServerProfile.setKeyStorePassword(
                        new String(CryptoUtil.getDefaultCryptoUtil().base64DecodeAndDecrypt(
                                passwordAttr.getAttributeValue())));

                } catch (CryptoException e) {
                    String errMsg = "Error occurred while decrypting password in BAM server profile: " +
                            profileLocation;
                    handleError(errMsg, e);
                }
            } else {
                String errMsg = "Key store details location or password not found for BAM " +
                        "server profile: " + profileLocation;
                handleError(errMsg);
            }
        } else {
            String errMsg = "Key store element not found for BAM server profile: " + profileLocation;
            /** handle this with new api
            log.warn("Key store element not specified");
             **/
        }
    }

    private void processStreamsElement(OMElement bamServerConfigElement,
                                       BAMServerProfile bamServerProfile) {
        OMElement streamsElement = bamServerConfigElement.getFirstChildWithName(
                new QName(BPELConstants.BAM_SERVER_PROFILE_NS, "Streams"));
        if (streamsElement == null) {
            log.warn("No Streams found for BAM server profile: " + profileLocation);
        } else {
            Iterator itr = streamsElement.getChildrenWithName(
                    new QName(BPELConstants.BAM_SERVER_PROFILE_NS, "Stream"));
            OMElement streamElement;
            while (itr.hasNext()) {
                streamElement = (OMElement) itr.next();
                processStreamElement(streamElement, bamServerProfile);
            }
        }
    }

    private void processStreamElement(OMElement streamElement,
                                      BAMServerProfile bamServerProfile) {
        OMAttribute nameAttr = streamElement.getAttribute(new QName("name"));
        OMAttribute versionAttr = streamElement.getAttribute(new QName("version"));
        OMAttribute nickNameAttr = streamElement.getAttribute(new QName("nickName"));
        OMAttribute descriptionAttr = streamElement.getAttribute(new QName("description"));
        if (nameAttr != null && nickNameAttr != null && descriptionAttr != null &&
                !nameAttr.getAttributeValue().equals("") &&
                !nickNameAttr.getAttributeValue().equals("") &&
                !descriptionAttr.getAttributeValue().equals("")) {
            BAMStreamConfiguration streamConfiguration =
                    new BAMStreamConfiguration(nameAttr.getAttributeValue(),
                            nickNameAttr.getAttributeValue(), descriptionAttr.getAttributeValue(),
                            versionAttr.getAttributeValue());
            OMElement dataElement = streamElement.getFirstChildWithName(
                    new QName(BPELConstants.BAM_SERVER_PROFILE_NS, "Data"));
            if (dataElement == null) {
                String errMsg = "Data element is not available for BAM server profile: " +
                        profileLocation;
                handleError(errMsg);
            }
            Iterator itr = dataElement.getChildrenWithName(
                    new QName(BPELConstants.BAM_SERVER_PROFILE_NS, "Key"));
            OMElement keyElement;
            while (itr.hasNext()) {
                keyElement = (OMElement) itr.next();
                processKeyElement(keyElement, streamConfiguration);
            }
            bamServerProfile.addBAMStreamConfiguration(streamConfiguration.getName(), streamConfiguration.getVersion(),
                    streamConfiguration);
        } else {
            String errMsg = "One or many of the attributes of Stream element is not available " +
                    "for BAM server profile: " + profileLocation;
            handleError(errMsg);
        }

    }

    private void processKeyElement(OMElement keyElement,
                                   BAMStreamConfiguration streamConfiguration) {

        BAMKey bamKey;
        String name;
        BAMKey.BAMKeyType type = BAMKey.BAMKeyType.NONE;
        String variable;
        String expression;
        String part = null;
        String query = null;

        OMAttribute nameAttribute = keyElement.getAttribute(new QName("name"));
        if (nameAttribute == null || "".equals(nameAttribute.getAttributeValue())) {
            String errMsg = "name attribute of Key element cannot be null for BAM server " +
                    "profile: " + profileLocation;
            handleError(errMsg);
        }
        name = nameAttribute.getAttributeValue();

        OMAttribute typeAttribute = keyElement.getAttribute(new QName("type"));
        if (typeAttribute == null || "".equals(typeAttribute.getAttributeValue())) {
            type = BAMKey.BAMKeyType.PAYLOAD;
            log.debug("type attribute of Key element: " + name + " is not available. " +
                    "Type is default to payload");
        }
        try {
            type = BAMKey.BAMKeyType.valueOf(typeAttribute.getAttributeValue().toUpperCase());
        } catch (IllegalArgumentException e) {
            String errMsg = "Invalid key type specified for key name " + name;
            handleError(errMsg, e);
        }


        OMElement fromElement = keyElement.getFirstChildWithName(
                new QName(BPELConstants.BAM_SERVER_PROFILE_NS, "From"));
        if (fromElement == null) {
            String errMsg = "From element not found for Key element: " + name + " for BAM server " +
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
                    new QName(BPELConstants.BAM_SERVER_PROFILE_NS, "Query"));
            if (queryElement != null && ( "".equals(queryElement.getText()) == false)) {
                query = queryElement.getText();
            }
            bamKey = new BAMKey(name, variable, part, query, type);
        } else {
            if (fromElement.getText() == null || "".equals(fromElement.getText())) {
                String errMsg = "Variable name or XPath expression not found for From of Key: " +
                        name + " for BAM server profile: " + profileLocation;
                handleError(errMsg);
            }
            expression = fromElement.getText().trim();
            bamKey = new BAMKey(name, type);
            bamKey.setExpression(expression);
        }

        switch (bamKey.getType()) {
            case PAYLOAD:
                streamConfiguration.addPayloadBAMKey(bamKey);
                break;
            case CORRELATION:
                streamConfiguration.addCorrelationBAMKey(bamKey);
                break;
            case META:
                streamConfiguration.addMetaBAMKey(bamKey);
                break;
            default:
                String errMsg = "Unknown BAM key type: " + type + " with BAM key name: " +
                        bamKey.getName() +" in stream: " + streamConfiguration.getName();
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
