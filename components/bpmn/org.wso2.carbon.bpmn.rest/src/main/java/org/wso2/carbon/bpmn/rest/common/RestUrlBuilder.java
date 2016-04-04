/**
 *  Copyright (c) 2015-2016 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.bpmn.rest.common;

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.apache.commons.lang3.StringUtils;
//import org.wso2.carbon.kernel.utils.Utils;
import org.wso2.carbon.transport.http.netty.internal.config.ListenerConfiguration;
import org.wso2.carbon.transport.http.netty.internal.config.TransportsConfiguration;
import org.wso2.carbon.transport.http.netty.internal.config.YAMLTransportConfigurationBuilder;

import java.net.URI;
import java.text.MessageFormat;
import java.util.Set;

/**
 *
 */
public class RestUrlBuilder {

    protected String baseUrl = "";
    protected String createdUrl = "";
    protected String urlContext = "";

    public RestUrlBuilder(String baseContext) {
        //TEST - get netty-transports config file and read host:port
        if (baseContext.endsWith("/")) {
            urlContext = baseContext;
        } else {
            int index = baseContext.lastIndexOf("/");
            urlContext = baseContext.substring(0, index);
        }
        TransportsConfiguration trpConfig = YAMLTransportConfigurationBuilder.build();
        Set<ListenerConfiguration> configs = trpConfig.getListenerConfigurations();
        for (ListenerConfiguration config : configs) {
            String hostname = config.getHost();
            String port = Integer.toString(config.getPort());
            // TODO: add full base url http://host:port/bps/bpmn/v.4.0.0/context
            String createdUrl =
                    URI.create(String.format("http://%s:%d%c", hostname, port, urlContext))
                       .toASCIIString();
            setBaseUrl(createdUrl);
        }

    }

    protected void setBaseUrl(String createdUrl) {
        this.baseUrl = createdUrl;
    }

    //    protected RestUrlBuilder(String baseUrl) {
    //        this.baseUrl = baseUrl;
    //    }

    /**
     * Extracts the base URL from current request
     */
    public static RestUrlBuilder fromCurrentRequest(String baseUri) {
        return usingBaseUrl(baseUri);

    }

    /**
     * Uses baseUrl as the base URL
     */
    public static RestUrlBuilder usingBaseUrl(String baseUrl) {
        if (baseUrl == null) {
            throw new ActivitiIllegalArgumentException("baseUrl can not be null");
        }
        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }
        return new RestUrlBuilder(baseUrl);
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    //USED
    public String buildUrl(String[] fragments, Object... arguments) {
        if (baseUrl == null) {
            throw new ActivitiIllegalArgumentException("baseUrl can not be null");
        }
        return new StringBuilder(getBaseUrl()).append("/").append(MessageFormat.format
                (StringUtils.join(fragments, '/'), arguments)).toString();
    }
}
