/*
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.bpel.common.config;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bpel.common.internal.BPELCommonServiceComponent;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.registry.api.Resource;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.unifiedendpoint.core.UnifiedEndpoint;
import org.wso2.carbon.unifiedendpoint.core.UnifiedEndpointConstants;
import org.wso2.carbon.unifiedendpoint.core.UnifiedEndpointFactory;

import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayInputStream;
import java.io.File;

/**
 * The configuration for endpoints used by BPEL processes.
 * The main usage is to create/configure operation clients to invoke external services
 *
 * @link EndpointConfiguration
 */
public class EndpointConfiguration {

    private static Log log = LogFactory.getLog(EndpointConfiguration.class);

    private String serviceName;

    private String servicePort;

    private String serviceNS;

    private String mexTimeout;

    private String basePath;

    private String unifiedEndPointReference;

    /* Keeps the inline definition of a unified endpoint */
    private OMElement uepOM;

    //Cache the unified endpoint
    private UnifiedEndpoint unifiedEndpoint = null;

    private boolean serviceDescriptionAvailable;

    private String serviceDescriptionLocation;

    public void setUnifiedEndPointReference(String unifiedEndPointReference) {
        this.unifiedEndPointReference = unifiedEndPointReference;
    }

    public void setUepOM(OMElement uepOM) {
        this.uepOM = uepOM;
    }

    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getServicePort() {
        return servicePort;
    }

    public void setServicePort(String servicePort) {
        this.servicePort = servicePort;
    }

    public String getServiceNS() {
        return serviceNS;
    }

    public void setServiceNS(String serviceNS) {
        this.serviceNS = serviceNS;
    }

    public String getMexTimeout() {
        return mexTimeout;
    }

    public void setMexTimeout(String mexTimeout) {
        this.mexTimeout = mexTimeout;
    }

    public String getBasePath() {
        return basePath;
    }

    public UnifiedEndpoint getUnifiedEndpoint() throws AxisFault {
        if (unifiedEndpoint == null) {
            UnifiedEndpointFactory uepFactory = new UnifiedEndpointFactory();
            if ((uepOM != null ||
                 unifiedEndPointReference != null)) {
                if (uepOM != null) {
                    unifiedEndpoint = uepFactory.createEndpoint(uepOM);
                } else {
                    String uepConfPath = unifiedEndPointReference;

                    if (!uepConfPath.startsWith(UnifiedEndpointConstants.VIRTUAL_GOV_REG) &&
                        !uepConfPath.startsWith(UnifiedEndpointConstants.VIRTUAL_CONF_REG) &&
                        !uepConfPath.startsWith(UnifiedEndpointConstants.VIRTUAL_REG)) {

                        if (uepConfPath.startsWith(UnifiedEndpointConstants.VIRTUAL_FILE)) {
                            uepConfPath = uepConfPath.substring(UnifiedEndpointConstants.VIRTUAL_FILE.
                                    length());
                        }
                        if (isAbsolutePath(uepConfPath)) {
                            uepConfPath = UnifiedEndpointConstants.VIRTUAL_FILE + uepConfPath;
                        } else {
                            uepConfPath = getAbsolutePath(basePath, uepConfPath);
                        }
                        unifiedEndpoint = uepFactory.createVirtualEndpointFromFilePath(uepConfPath);
                    } else {
                        OMElement endpointElement = getEndpointElementFromRegistry(uepConfPath);
                        unifiedEndpoint = uepFactory.createEndpoint(endpointElement);
                    }
                }
            } else {
                unifiedEndpoint = new UnifiedEndpoint();
                unifiedEndpoint.setUepId(serviceName);
                unifiedEndpoint.setAddressingEnabled(true);
                unifiedEndpoint.setAddressingVersion(UnifiedEndpointConstants.
                                                             ADDRESSING_VERSION_FINAL);
            }

            if (unifiedEndpoint.isSecurityEnabled()) {
                String secPolicyKey = unifiedEndpoint.getWsSecPolicyKey();
                if (secPolicyKey.startsWith(UnifiedEndpointConstants.VIRTUAL_FILE)) {
                    String secPolicyLocation = secPolicyKey.substring(
                            UnifiedEndpointConstants.VIRTUAL_FILE.length());
                    if (!isAbsolutePath(secPolicyLocation)) {
                        secPolicyKey = getAbsolutePath(basePath,
                                                       secPolicyLocation);
                    } else {
                        secPolicyKey = UnifiedEndpointConstants.VIRTUAL_FILE + secPolicyLocation;
                    }
                    unifiedEndpoint.setWsSecPolicyKey(secPolicyKey);
                }
            }
        }

        return unifiedEndpoint;
    }

    private OMElement getEndpointElementFromRegistry(String uepConfPath) throws AxisFault {
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        RegistryService registryService = BPELCommonServiceComponent.getRegistryService();
        Registry registry = null;
        OMElement uepOMContent = null;
        String location;

        try {
            if (uepConfPath.startsWith(UnifiedEndpointConstants.VIRTUAL_CONF_REG)) {
                registry =
                        registryService.getConfigSystemRegistry(tenantId);
                location = uepConfPath.substring(uepConfPath.indexOf(
                        UnifiedEndpointConstants.VIRTUAL_CONF_REG) +
                                                 UnifiedEndpointConstants.VIRTUAL_CONF_REG.length());
                uepOMContent = loadUEPOMFromRegistry(registry, location);
            } else if (uepConfPath.startsWith(UnifiedEndpointConstants.VIRTUAL_GOV_REG)) {
                registry =
                        registryService.getGovernanceSystemRegistry(tenantId);
                location = uepConfPath.substring(uepConfPath.indexOf(
                        UnifiedEndpointConstants.VIRTUAL_GOV_REG) +
                                                 UnifiedEndpointConstants.VIRTUAL_GOV_REG.length());
                uepOMContent = loadUEPOMFromRegistry(registry, location);
            } else if (uepConfPath.startsWith(UnifiedEndpointConstants.VIRTUAL_REG)) {
                registry =
                        registryService.getLocalRepository(tenantId);
                location = uepConfPath.substring(uepConfPath.indexOf(
                        UnifiedEndpointConstants.VIRTUAL_REG) +
                                                 UnifiedEndpointConstants.VIRTUAL_REG.length());
                uepOMContent = loadUEPOMFromRegistry(registry, location);
            }
        } catch (RegistryException ex) {
            String error = "Error occurred while getting registry service" + ex.getLocalizedMessage();
            handleError(error);
        }

        return uepOMContent;
    }

    private OMElement loadUEPOMFromRegistry(Registry registry, String location) throws AxisFault {
        OMElement uepOMElement = null;
        try {
            if (registry.resourceExists(location)) {
                Resource resource = registry.get(location);
                String resourceContent = new String((byte[]) resource.getContent());
                uepOMElement = new StAXOMBuilder(new ByteArrayInputStream(resourceContent.getBytes())).getDocumentElement();

            } else {
                String errMsg = "The resource: " + location + " does not exist.";
                handleError(errMsg);
            }
        } catch (org.wso2.carbon.registry.api.RegistryException e) {
            String errMsg = "Error occurred while reading the resource from registry: " +
                            location + " to build the Unified End Point: " + location;
            handleError(errMsg, e);
        } catch (XMLStreamException e) {
            String errMsg = "Error occurred while creating the OMElement out of Unified End Point " +
                            "profile: " + location;
            handleError(errMsg, e);
        }
        return uepOMElement;
    }

    private void handleError(String errMsg) throws AxisFault {
        log.error(errMsg);
        throw new AxisFault(errMsg);
    }

    private void handleError(String errMsg, Exception ex) throws AxisFault {
        log.error(errMsg);
        throw new AxisFault(errMsg, ex);
    }

    public static String getAbsolutePath(String basePath, String filePath) {
        return UnifiedEndpointConstants.VIRTUAL_FILE + basePath + File.separator + filePath;
    }

    public static boolean isAbsolutePath(String filePath) {
        return filePath.startsWith("/") ||
               (filePath.length() > 1 && filePath.charAt(1) == ':');
    }

    public boolean isServiceDescriptionAvailable() {
        return serviceDescriptionAvailable;
    }

    public void setServiceDescriptionAvailable(boolean serviceDescriptionAvailable) {
        this.serviceDescriptionAvailable = serviceDescriptionAvailable;
    }

    public String getServiceDescriptionLocation() {
        return serviceDescriptionLocation;
    }

    public void setServiceDescriptionLocation(String serviceDescriptionLocation) {
        this.serviceDescriptionLocation = serviceDescriptionLocation;
    }
}
