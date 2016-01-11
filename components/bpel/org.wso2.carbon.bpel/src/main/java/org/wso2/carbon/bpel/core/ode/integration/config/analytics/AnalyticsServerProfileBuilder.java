/*
 * Copyright (c) 2005-2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

	// Log factory.
	private static Log log = LogFactory.getLog(AnalyticsServerProfileBuilder.class);
	// Analytic profile location
	private String profileLocation;
	// Tenant ID.
	private Integer tenantId;

	/**
	 * Creates AnalyticsServerProfileBuilder instance for given tenant and Analytic profile location.
	 *
	 * @param profileLocation location of Analytics profile.
	 * @param tenantId        tenant ID.
	 */
	public AnalyticsServerProfileBuilder(String profileLocation, Integer tenantId) {
		this.profileLocation = profileLocation;
		this.tenantId = tenantId;
	}

	/**
	 * Creates AnalyticsServerProfile for given tenant and Analytic profile.
	 *
	 * @return AnalyticsServerProfile instance
	 */
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
	                String errMsg = "Error while loading Analytic profile from config registry.";
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
	                String errMsg = "Error while loading Analytic profile from governance registry.";
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
	                String errMsg = "Error while loading Analytic profile from local registry";
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

	/**
	 * Load Analytics profile from file system.
	 *
	 * @param analyticsServerProfile AnalyticsServerProfile instance
	 * @param location               File system path of the profile file
	 */
	private void loadAnalyticsProfileFromFileSystem(AnalyticsServerProfile analyticsServerProfile, String location) {
		File file = new File(location);
		if (file.exists()) {
			try {
				String profileFileContent = FileUtils.readFileToString(file, "UTF-8");
				parseAnalyticsProfile(profileFileContent, analyticsServerProfile);
			} catch (IOException e) {
				String errMsg = "Error occurred while reading the file from file system: " +
				                location + "to build the Analytics server profile:" + profileLocation;
				handleError(errMsg, e);
			}
		}
	}

	/**
	 * Load Analytics profile from given registry and registry path.
	 *
	 * @param analyticsServerProfile AnalyticsServerProfile instance
	 * @param registry               Registry space where file is located
	 * @param location               Registry path of the profile file
	 */
	private void loadAnalyticsProfileFromRegistry(AnalyticsServerProfile analyticsServerProfile, Registry registry,
	                                              String location) {
		try {
			if (registry.resourceExists(location)) {
				Resource resource = registry.get(location);
				String resourceContent = new String((byte[]) resource.getContent());
				parseAnalyticsProfile(resourceContent, analyticsServerProfile);
			} else {
				String errMsg = "The resource: " + location + " does not exist.";
				handleError(errMsg);
			}
		} catch (RegistryException e) {
			String errMsg = "Error occurred while reading the resource from registry: " +
			                location + " to build the Analytics server profile: " + profileLocation;
			handleError(errMsg, e);
		}
	}

	/**
	 * Parse Analytics Profile file content and adding content to AnalyticsProfile instance.
	 *
	 * @param content                Content of Analytics Profile
	 * @param analyticsServerProfile AnalyticsServerProfile instance
	 */
	private void parseAnalyticsProfile(String content, AnalyticsServerProfile analyticsServerProfile) {
		try {
			OMElement resourceElement =
					new StAXOMBuilder(new ByteArrayInputStream(content.getBytes())).getDocumentElement();
			processAnalyticsServerProfileName(resourceElement, analyticsServerProfile);
			processCredentialElement(resourceElement, analyticsServerProfile);
			processConnectionElement(resourceElement, analyticsServerProfile);
			processStreamsElement(resourceElement, analyticsServerProfile);
		} catch (XMLStreamException e) {
			String errMsg = "Error occurred while creating the OMElement out of Analytics server " +
			                "profile: " + profileLocation;
			handleError(errMsg, e);
		} catch (CryptoException e) {
			String errMsg = "Error occurred while decrypting password in Analytics server profile: " + profileLocation;
			handleError(errMsg, e);
		}
	}

	/**
	 * Reading Analytics Server profile name and Agent type and setting them to given analyticsServerProfile instance.
	 *
	 * @param analyticsServerConfig  OMElement of analyticsServerConfig
	 * @param analyticsServerProfile AnalyticsServerProfile instance
	 */
	private void processAnalyticsServerProfileName(OMElement analyticsServerConfig,
	                                               AnalyticsServerProfile analyticsServerProfile) {
		OMAttribute name = analyticsServerConfig.getAttribute(new QName(AnalyticsConstants.NAME));
		if (name != null && name.getAttributeValue() != null &&
		    !AnalyticsConstants.EMPTY.equals(name.getAttributeValue().trim())) {
			analyticsServerProfile.setName(name.getAttributeValue().trim());
		} else {
			String errMsg =
					AnalyticsConstants.NAME + " attribute not found for Analytics server profile: " + profileLocation;
			handleError(errMsg);
		}
		OMAttribute type = analyticsServerConfig.getAttribute(new QName(AnalyticsConstants.TYPE));
		if (type != null && type.getAttributeValue() != null &&
		    !AnalyticsConstants.EMPTY.equals(type.getAttributeValue().trim())) {
			analyticsServerProfile.setType(type.getAttributeValue().trim());
		} else {
			// This value can be null or empty. Then default agent will get selected.
			analyticsServerProfile.setType(null);
		}
	}

	/**
	 * Reading Analytics Server profile credentials and setting them to given analyticsServerProfile instance.
	 *
	 * @param analyticsServerConfig  OMElement of analyticsServerConfig
	 * @param analyticsServerProfile AnalyticsServerProfile instance
	 * @throws CryptoException
	 */
	private void processCredentialElement(OMElement analyticsServerConfig,
	                                      AnalyticsServerProfile analyticsServerProfile) throws CryptoException {
		OMElement credentialElement = analyticsServerConfig.getFirstChildWithName(
				new QName(BPELConstants.ANALYTICS_SERVER_PROFILE_NS, AnalyticsConstants.CREDENTIAL));
		if (credentialElement != null) {

			OMAttribute userNameAttr =
					credentialElement.getAttribute(new QName(AnalyticsConstants.CREDENTIAL_USER_NAME));
			OMAttribute passwordAttr =
					credentialElement.getAttribute(new QName(AnalyticsConstants.CREDENTIAL_PASSWORD));

			if (userNameAttr != null && passwordAttr != null && userNameAttr.getAttributeValue() != null &&
			    passwordAttr.getAttributeValue() != null &&
			    !AnalyticsConstants.EMPTY.equals(userNameAttr.getAttributeValue().trim()) &&
			    !AnalyticsConstants.EMPTY.equals(passwordAttr.getAttributeValue().trim())) {

				analyticsServerProfile.setUserName(userNameAttr.getAttributeValue().trim());
				analyticsServerProfile.setPassword(new String(CryptoUtil.getDefaultCryptoUtil().
						base64DecodeAndDecrypt(passwordAttr.getAttributeValue())));
			} else {
				String errMsg =
						AnalyticsConstants.CREDENTIAL_USER_NAME + " or " + AnalyticsConstants.CREDENTIAL_PASSWORD +
						" not found for Analytics server profile: " + profileLocation;
				handleError(errMsg);
			}
		} else {
			String errMsg = AnalyticsConstants.CREDENTIAL + " element not found for Analytics server profile: " +
			                profileLocation;
			handleError(errMsg);
		}
	}

	/**
	 * Reading Analytics Server profile connections URLs and setting them to given analyticsServerProfile instance.
	 *
	 * @param analyticsServerConfig  OMElement of analyticsServerConfig
	 * @param analyticsServerProfile AnalyticsServerProfile instance
	 */
	private void processConnectionElement(OMElement analyticsServerConfig,
	                                      AnalyticsServerProfile analyticsServerProfile) {
		OMElement connectionElement = analyticsServerConfig.getFirstChildWithName(
				new QName(BPELConstants.ANALYTICS_SERVER_PROFILE_NS, AnalyticsConstants.CONNECTION));
		if (connectionElement != null) {
			OMAttribute receiverURLsSet =
					connectionElement.getAttribute(new QName(AnalyticsConstants.CONNECTION_RECEIVER_URL_SET));
			OMAttribute authURLsSet =
					connectionElement.getAttribute(new QName(AnalyticsConstants.CONNECTION_AUTH_URL_SET));
			if (receiverURLsSet != null && receiverURLsSet.getAttributeValue() != null &&
			    !AnalyticsConstants.EMPTY.equals(receiverURLsSet.getAttributeValue().trim())) {

				analyticsServerProfile.setReceiverURLSet(receiverURLsSet.getAttributeValue().trim());
			} else {
				String errMsg = "Connection details are missing for Analytics server profile: " + profileLocation;
				handleError(errMsg);
			}
			// AuthURLsSet can be null or empty.
			if (authURLsSet != null && authURLsSet.getAttributeValue() != null &&
			    !AnalyticsConstants.EMPTY.equals(authURLsSet.getAttributeValue().trim())) {
				analyticsServerProfile.setAuthURLSet(authURLsSet.getAttributeValue().trim());
			} else {
				analyticsServerProfile.setAuthURLSet(null);
			}
		} else {
			String errMsg = "Connection details not found for Analytics server profile: " + profileLocation;
			handleError(errMsg);
		}
	}

	/**
	 * Read Steams element and parse subsequent steam elements.
	 *
	 * @param analyticsServerConfigElement OMElement of analyticsServerConfig
	 * @param analyticsServerProfile       AnalyticsServerProfile instance
	 */
	private void processStreamsElement(OMElement analyticsServerConfigElement,
	                                   AnalyticsServerProfile analyticsServerProfile) {
		OMElement streamsElement = analyticsServerConfigElement.getFirstChildWithName(
				new QName(BPELConstants.ANALYTICS_SERVER_PROFILE_NS, AnalyticsConstants.STREAMS));
		if (streamsElement == null) {
			log.warn("No " + AnalyticsConstants.STREAMS + " found for Analytics server profile: " + profileLocation);
		} else {
			Iterator itr = streamsElement.getChildrenWithName(
					new QName(BPELConstants.ANALYTICS_SERVER_PROFILE_NS, AnalyticsConstants.STREAM));
			OMElement streamElement;
			while (itr.hasNext()) {
				streamElement = (OMElement) itr.next();
				processStreamElement(streamElement, analyticsServerProfile);
			}
		}
	}

	/**
	 * Parse Steam element and setting stream data into given analyticsServerProfile instance.
	 *
	 * @param streamElement          OMElement of Steam element.
	 * @param analyticsServerProfile AnalyticsServerProfile instance
	 */
	private void processStreamElement(OMElement streamElement, AnalyticsServerProfile analyticsServerProfile) {
		OMAttribute nameAttr = streamElement.getAttribute(new QName(AnalyticsConstants.STREAM_NAME));
		OMAttribute versionAttr = streamElement.getAttribute(new QName(AnalyticsConstants.STREAM_VERSION));
		OMAttribute nickNameAttr = streamElement.getAttribute(new QName(AnalyticsConstants.STREAM_NICKNAME));
		OMAttribute descriptionAttr = streamElement.getAttribute(new QName(AnalyticsConstants.STREAM_DESCRIPTION));

		if (nameAttr != null && nickNameAttr != null && descriptionAttr != null && versionAttr != null &&
		    nameAttr.getAttributeValue() != null && nickNameAttr.getAttributeValue() != null &&
		    descriptionAttr.getAttributeValue() != null && versionAttr.getAttributeValue() != null &&
		    !AnalyticsConstants.EMPTY.equals(nameAttr.getAttributeValue().trim()) &&
		    !AnalyticsConstants.EMPTY.equals(nickNameAttr.getAttributeValue().trim()) &&
		    //!AnalyticsConstants.EMPTY.equals(descriptionAttr.getAttributeValue().trim()) &&
		    !AnalyticsConstants.EMPTY.equals(versionAttr.getAttributeValue().trim())) {

			AnalyticsStreamConfiguration streamConfiguration =
					new AnalyticsStreamConfiguration(nameAttr.getAttributeValue().trim(),
					                                 nickNameAttr.getAttributeValue().trim(),
					                                 descriptionAttr.getAttributeValue(),
					                                 versionAttr.getAttributeValue().trim());
			OMElement dataElement = streamElement.getFirstChildWithName(
					new QName(BPELConstants.ANALYTICS_SERVER_PROFILE_NS, AnalyticsConstants.STREAM_DATA));
			if (dataElement == null) {
				String errMsg =
						AnalyticsConstants.STREAM_DATA + " element is not available for Analytics server profile: " +
						profileLocation;
				handleError(errMsg);
			}
			Iterator itr = dataElement.getChildrenWithName(
					new QName(BPELConstants.ANALYTICS_SERVER_PROFILE_NS, AnalyticsConstants.STREAM_DATA_KEY));
			OMElement keyElement;
			while (itr.hasNext()) {
				keyElement = (OMElement) itr.next();
				processKeyElement(keyElement, streamConfiguration);
			}
			analyticsServerProfile
					.addAnalyticsStreamConfiguration(streamConfiguration.getName(), streamConfiguration.getVersion(),
					                                 streamConfiguration);
		} else {
			String errMsg =
					"One or many of the attributes of " + AnalyticsConstants.STREAM + " element is not available " +
					"for Analytics server profile: " + profileLocation;
			handleError(errMsg);
		}

	}

	/**
	 * Process Key Element.
	 *
	 * @param keyElement          OMElement of Key element
	 * @param streamConfiguration AnalyticsStreamConfiguration instance
	 */
	private void processKeyElement(OMElement keyElement, AnalyticsStreamConfiguration streamConfiguration) {

		AnalyticsKey analyticsKey;
		String name;
		AnalyticsKey.AnalyticsKeyType type = AnalyticsKey.AnalyticsKeyType.NONE;
		String variable;
		String expression;
		String part = null;
		String query = null;

		OMAttribute nameAttribute = keyElement.getAttribute(new QName(AnalyticsConstants.STREAM_DATA_KEY_NAME));
		if (nameAttribute == null || nameAttribute.getAttributeValue() == null ||
		    AnalyticsConstants.EMPTY.equals(nameAttribute.getAttributeValue().trim())) {
			String errMsg = AnalyticsConstants.STREAM_DATA_KEY_NAME +
			                " attribute of Key element cannot be null for Analytics server " +
			                "profile: " + profileLocation;
			handleError(errMsg);
		}
		name = nameAttribute.getAttributeValue().trim();

		OMAttribute typeAttribute = keyElement.getAttribute(new QName(AnalyticsConstants.STREAM_DATA_KEY_TYPE));
		if (typeAttribute == null || typeAttribute.getAttributeValue() == null ||
		    AnalyticsConstants.EMPTY.equals((typeAttribute.getAttributeValue().trim()))) {
			type = AnalyticsKey.AnalyticsKeyType.PAYLOAD;
			log.debug("type attribute of Key element: " + name + " is not available. " +
			          "Type is default to payload");
		}
		try {
			type = AnalyticsKey.AnalyticsKeyType.valueOf(typeAttribute.getAttributeValue().trim().toUpperCase());
		} catch (IllegalArgumentException e) {
			String errMsg = "Invalid key type specified for key name " + name;
			handleError(errMsg, e);
		}

		OMElement fromElement = keyElement.getFirstChildWithName(
				new QName(BPELConstants.ANALYTICS_SERVER_PROFILE_NS, AnalyticsConstants.STREAM_DATA_KEY_FROM));
		if (fromElement == null) {
			String errMsg = "From element not found for Key element: " + name + " for Analytics server " +
			                "profile: " + profileLocation;
			handleError(errMsg);
		}

		OMAttribute variableAttribute =
				fromElement.getAttribute(new QName(AnalyticsConstants.STREAM_DATA_KEY_FROM_VARIABLE));
		if (variableAttribute != null && variableAttribute.getAttributeValue() != null &&
		    !AnalyticsConstants.EMPTY.equals(variableAttribute.getAttributeValue().trim())) {
			variable = variableAttribute.getAttributeValue().trim();

			OMAttribute partAttribute =
					fromElement.getAttribute(new QName(AnalyticsConstants.STREAM_DATA_KEY_FROM_PART));
			if (partAttribute != null && partAttribute.getAttributeValue() !=null && ! AnalyticsConstants.EMPTY.equals(partAttribute.getAttributeValue().trim())) {
				part = partAttribute.getAttributeValue().trim();
            }

            OMElement queryElement = fromElement.getFirstChildWithName(
                    new QName(BPELConstants.ANALYTICS_SERVER_PROFILE_NS, AnalyticsConstants.STREAM_DATA_KEY_FROM_QUERY));
            if (queryElement != null && ! AnalyticsConstants.EMPTY.equals(queryElement.getText().trim())) {
                query = queryElement.getText();
            }
            analyticsKey = new AnalyticsKey(name, variable, part, query, type);
        } else {
            if (fromElement.getText() == null || AnalyticsConstants.EMPTY.equals(fromElement.getText())) {
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
