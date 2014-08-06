/*
 * Copyright (c) 2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.attachment.mgt.util;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.attachment.mgt.configuration.AttachmentMgtConfigurationConstants;
import org.wso2.carbon.attachment.mgt.core.exceptions.AttachmentMgtException;
import org.wso2.carbon.attachment.mgt.server.internal.AttachmentServerHolder;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.NetworkUtils;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.URI;
import java.net.URL;
import java.security.SecureRandom;

/**
 * Logic relevant to URL generation. This URL will be used by outsiders to access the attachment.
 */
public class URLGeneratorUtil extends AbstractAdmin {
    /**
     * Logger class
     */
    private static Log log = LogFactory.getLog(URLGeneratorUtil.class);

    private static final SecureRandom random = new SecureRandom();

    /**
     * URL generation logic
     *
     * @return string value of URL
     */
    public static String generateURL() throws AttachmentMgtException {
        return generateUniqueID();
    }

    /**
     * Generate a unique string required for URL generation
     *
     * @return a unique string
     */
    private static String generateUniqueID() {
        return new BigInteger(130, random).toString(32);
    }

    /**
     * Generate the permanent link for the given attachment uri based on current system configurations like host, port
     * eg - if {@code uniqueID} is abc123, then the resultant permanent link would {@code https://127.0.0.1:9443/context/abc123}
     * So this url can be used to download the attachment
     *
     * @param uniqueID uri for the attachment
     * @return downloadable url of the attachment
     * @throws AttachmentMgtException
     */
    public static URL getPermanentLink(URI uniqueID) throws AttachmentMgtException {
        String scheme = CarbonConstants.HTTPS_TRANSPORT;
        String host;
        try {
            host = NetworkUtils.getLocalHostname();
        } catch (SocketException e) {
            log.error(e.getMessage(), e);
            throw new AttachmentMgtException(e.getLocalizedMessage(), e);
        }

        int port = 9443;

        try {
            ConfigurationContext serverConfigContext =
                    AttachmentServerHolder.getInstance().getConfigurationContextService().getServerConfigContext();
            port = CarbonUtils.getTransportProxyPort(serverConfigContext, scheme);
            if (port == -1) {
                port = CarbonUtils.getTransportPort(serverConfigContext, scheme);
            }
        } catch (Exception ex) {
            log.warn("Using default port settings");
        }

        String webContext = ServerConfiguration.getInstance().getFirstProperty("WebContextRoot");
        if (webContext == null || webContext.equals("/")) {
            webContext = "";
        }

        String tenantDomain = String.valueOf(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
        try {
            tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        } catch (Throwable e) {
            tenantDomain = null;
        }

        String url = null;
        try {
            String link = scheme + "://" + host + ":" + port + webContext + ((tenantDomain != null &&
                                                                              !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) ?
                                                                             "/" + MultitenantConstants.TENANT_AWARE_URL_PREFIX + "/" + tenantDomain : "") +
                          AttachmentMgtConfigurationConstants.ATTACHMENT_DOWNLOAD_SERVELET_URL_PATTERN + "/" + uniqueID.toString();

            return new URL(link);
        } catch (MalformedURLException e) {
            log.error(e.getMessage(), e);
            throw new AttachmentMgtException(e.getLocalizedMessage(), e);
        }
    }
}