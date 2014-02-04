/*
 * Copyright (c) 2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.humantask.core.utils;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bpel.common.BusinessProcessConstants;
import org.wso2.carbon.bpel.common.config.EndpointConfiguration;
import org.wso2.carbon.humantask.core.deployment.HumanTaskDeploymentException;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Utility methods for Human Task Store classes
 */
public final class HumanTaskStoreUtils {
    private static Log log = LogFactory.getLog(HumanTaskStoreUtils.class);
    private static final String HEXES = "0123456789ABCDEF";

    public static OMElement getOMElement(String elementString) throws HumanTaskDeploymentException {
        OMElement serviceEle;
        try {
            serviceEle = AXIOMUtil.stringToOM(elementString);
        } catch (XMLStreamException e) {
            String errMsg = "Error occurred while converting string: " + elementString +
                    " to OMElement";
            log.error(errMsg, e);
            throw new HumanTaskDeploymentException(errMsg, e);
        }
        return serviceEle;
    }

    public static EndpointConfiguration getEndpointConfig(OMElement serviceEle) {
        OMElement endpointEle = serviceEle.getFirstElement();
        if (endpointEle == null || !endpointEle.getQName().equals(
                new QName(BusinessProcessConstants.BPEL_PKG_ENDPOINT_CONFIG_NS,
                        BusinessProcessConstants.ENDPOINT))) {
            return null;
        }

        String serviceDescLocation = endpointEle.getAttributeValue(new QName(null,
                BusinessProcessConstants.SERVICE_DESC_LOCATION));

        if (StringUtils.isNotEmpty(serviceDescLocation)) {
            EndpointConfiguration endpointConfig = new EndpointConfiguration();
            endpointConfig.setServiceDescriptionAvailable(true);
            endpointConfig.setServiceDescriptionLocation(serviceDescLocation.trim());
            return endpointConfig;
        }

        String endpointRef = endpointEle.getAttributeValue(new QName(null,
                BusinessProcessConstants.ENDPOINTREF));
        if (StringUtils.isNotEmpty(endpointRef)) {
            EndpointConfiguration endpointConfig = new EndpointConfiguration();
            endpointConfig.setUnifiedEndPointReference(endpointRef.trim());
            return endpointConfig;
        }

        return null;
    }

    public static String getHex(byte[] raw) {
        if (raw == null) {
            return null;
        }
        final StringBuilder hex = new StringBuilder(2 * raw.length);
        for (final byte b : raw) {
            hex.append(HEXES.charAt((b & 0xF0) >> 4))
                    .append(HEXES.charAt((b & 0x0F)));
        }
        return hex.toString();
    }

    public static byte[] createChecksum(File fileToCalculateMD5)
            throws IOException, NoSuchAlgorithmException {
        InputStream fis = new FileInputStream(fileToCalculateMD5);
        try {
            byte[] buffer = new byte[1024];
            MessageDigest complete = MessageDigest.getInstance("MD5");
            int numRead;
            do {
                numRead = fis.read(buffer);
                if (numRead > 0) {
                    complete.update(buffer, 0, numRead);
                }
            } while (numRead != -1);

            return complete.digest();
        } finally {
            fis.close();
        }
    }

    // see this How-to for a faster way to convert
    // a byte array to a HEX string

    public static String getMD5Checksum(File file) throws IOException, NoSuchAlgorithmException {
        byte[] b = createChecksum(file);
        return getHex(b);
    }
}
