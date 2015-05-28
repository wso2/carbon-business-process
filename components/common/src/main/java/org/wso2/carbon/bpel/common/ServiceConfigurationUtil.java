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

package org.wso2.carbon.bpel.common;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.deployment.ServiceBuilder;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.Parameter;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bpel.common.config.EndpointConfiguration;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.RegistryType;
import org.wso2.carbon.registry.api.Registry;
import org.wso2.carbon.registry.api.RegistryException;
import org.wso2.carbon.registry.api.Resource;
import org.wso2.carbon.unifiedendpoint.core.UnifiedEndpointConstants;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Iterator;

/**
 * Configure the service based on the services.xml file provided with the bpel archive.
 */
public class ServiceConfigurationUtil {

    private static final Log log = LogFactory.getLog(ServiceConfigurationUtil.class);

    public static void configureService(AxisService axisService, EndpointConfiguration endpointConf,
                                        ConfigurationContext configCtx) throws AxisFault {

        if (endpointConf != null && endpointConf.isServiceDescriptionAvailable() &&
            StringUtils.isNotEmpty(endpointConf.getServiceDescriptionLocation())) {

            OMElement documentEle = getServiceElement(endpointConf);
            if (documentEle != null) {
                if (log.isDebugEnabled()) {
                    log.debug("Configuring service " + axisService.getName() + " using: " +
                              endpointConf.getServiceDescriptionLocation());
                }
//                if (configCtx == null) {
//                    configCtx = new ConfigurationContext(axisService.getAxisConfiguration());
//                }
                ServiceBuilder builder = new ServiceBuilder(configCtx, axisService);
                Iterator itr = documentEle.getChildElements();

                while (itr.hasNext()) {
                    OMElement serviceEle = (OMElement) itr.next();
                    if (serviceEle.getLocalName().toLowerCase().equals("service")) {
                        if (serviceEle.getAttribute(new QName("name")) != null &&
                            serviceEle.getAttribute(new QName("name")).getAttributeValue().equals(axisService.getName())) {

                            builder.populateService(serviceEle);
                            // This is a hack to avoid security configurations get persisted when we configure using
                            // services.xml file or policy.xml file BPEL package. But this should be fix at the
                            // Carbon Persistence manager.
                            Parameter param = new Parameter(
                                    BusinessProcessConstants.CONFIGURED_USING_BPEL_PKG_CONFIG_FILES,
                                    "true");
                            axisService.addParameter(param);
                        }
                    }
                }
            }
        }
    }

    private static OMElement getServiceElement(EndpointConfiguration endpointConfig) {

        OMElement serviceElement;

        String serviceDescriptionLocation = endpointConfig.getServiceDescriptionLocation();

        if (!serviceDescriptionLocation.startsWith(UnifiedEndpointConstants.VIRTUAL_GOV_REG) &&
            !serviceDescriptionLocation.startsWith(UnifiedEndpointConstants.VIRTUAL_CONF_REG) &&
            !serviceDescriptionLocation.startsWith(UnifiedEndpointConstants.VIRTUAL_REG)) {
            if (serviceDescriptionLocation.startsWith(UnifiedEndpointConstants.VIRTUAL_FILE)) {
                serviceDescriptionLocation = serviceDescriptionLocation.substring(UnifiedEndpointConstants.VIRTUAL_FILE.
                        length());
            }
            if (EndpointConfiguration.isAbsolutePath(serviceDescriptionLocation)) {
                serviceDescriptionLocation = UnifiedEndpointConstants.VIRTUAL_FILE + serviceDescriptionLocation;
            } else {
                serviceDescriptionLocation =
                        EndpointConfiguration.getAbsolutePath(endpointConfig.getBasePath(),
                                                              serviceDescriptionLocation);
            }
            serviceElement = readServiceElementFromFile(serviceDescriptionLocation);
        } else {
            serviceElement = readServiceElementFromRegistry(serviceDescriptionLocation);
        }

        return serviceElement;
    }

    //If the service file is located in the file system, read if from the file.
    private static OMElement readServiceElementFromFile(String fileLocation) {
        OMElement serviceElement = null;
        File serviceDescFile =
                new File(fileLocation.substring(UnifiedEndpointConstants.VIRTUAL_FILE.length()));
        if (serviceDescFile.exists()) {
            InputStream fis = null;
            try {
                fis = new FileInputStream(serviceDescFile);
                StAXOMBuilder omBuilder = new StAXOMBuilder(fis);
                serviceElement = omBuilder.getDocumentElement();
                serviceElement.build();
            } catch (FileNotFoundException | XMLStreamException ex) {
                log.error("Error while processing the services file : " + fileLocation , ex);
            } finally {
                IOUtils.closeQuietly(fis);
            }
        }
        return serviceElement;
    }

    //If the service file is located in the registry, read if from the registry.
    private static OMElement readServiceElementFromRegistry(String registryLocation) {
        OMElement serviceElement = null;
        Registry registry;
        String location;

        if (registryLocation.startsWith(UnifiedEndpointConstants.VIRTUAL_CONF_REG)) {
            registry = CarbonContext.getThreadLocalCarbonContext().getRegistry(RegistryType.SYSTEM_CONFIGURATION);
            location = registryLocation.substring(registryLocation.indexOf(
                    UnifiedEndpointConstants.VIRTUAL_CONF_REG) +
                                                  UnifiedEndpointConstants.VIRTUAL_CONF_REG.length());
            serviceElement = loadServiceElement(registry, location);
        } else if (registryLocation.startsWith(UnifiedEndpointConstants.VIRTUAL_GOV_REG)) {
            registry = CarbonContext.getThreadLocalCarbonContext().getRegistry(RegistryType.SYSTEM_GOVERNANCE);
            location = registryLocation.substring(registryLocation.indexOf(
                    UnifiedEndpointConstants.VIRTUAL_GOV_REG) +
                                                  UnifiedEndpointConstants.VIRTUAL_GOV_REG.length());
            serviceElement = loadServiceElement(registry, location);
        } else if (registryLocation.startsWith(UnifiedEndpointConstants.VIRTUAL_REG)) {
            registry = CarbonContext.getThreadLocalCarbonContext().getRegistry(RegistryType.LOCAL_REPOSITORY);
            location = registryLocation.substring(registryLocation.indexOf(
                    UnifiedEndpointConstants.VIRTUAL_REG) +
                                                  UnifiedEndpointConstants.VIRTUAL_REG.length());
            serviceElement = loadServiceElement(registry, location);
        } else {
            String errMsg = "Invalid service.xml file location: " + registryLocation;
            log.warn(errMsg);
        }


        return serviceElement;
    }

    private static OMElement loadServiceElement(Registry registry, String location) {
        OMElement serviceElement = null;
        try {
            if (registry.resourceExists(location)) {
                Resource resource = registry.get(location);
                String resourceContent = new String((byte[]) resource.getContent());
                serviceElement = new StAXOMBuilder
                        (new ByteArrayInputStream(resourceContent.getBytes())).getDocumentElement();
            } else {
                String errMsg = "The resource: " + location + " does not exist.";
                log.warn(errMsg);
            }
        } catch (RegistryException e) {
            String errMsg = "Error occurred while creating the OMElement out of service.xml " +
                            location + " to build the BAM server profile: " + location;
            log.warn(errMsg, e);
        } catch (XMLStreamException e) {
            String errMsg = "Error occurred while creating the OMElement out of service.xml " +
                            "location: " + location;
            log.warn(errMsg, e);
        }
        return serviceElement;

    }
}
