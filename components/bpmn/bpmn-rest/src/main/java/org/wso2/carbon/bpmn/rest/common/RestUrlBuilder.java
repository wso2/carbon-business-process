/**
 *  Copyright (c) 2015 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

//import org.activiti.engine.ActivitiIllegalArgumentException;
import org.apache.commons.lang3.StringUtils;

import java.text.MessageFormat;

public class RestUrlBuilder {

    protected String baseUrl = "";

    protected RestUrlBuilder(){
    }

    protected RestUrlBuilder(String baseUrl){
        this.baseUrl = baseUrl;
    }

    /** Extracts the base URL from current request */
    public static RestUrlBuilder fromCurrentRequest(String baseUri){
        return usingBaseUrl(baseUri);
    }

    /** Uses baseUrl as the base URL */
    public static RestUrlBuilder usingBaseUrl(String baseUrl){
      //  if(baseUrl == null) throw new ActivitiIllegalArgumentException("baseUrl can not be null");
        if(baseUrl.endsWith("/")) baseUrl = baseUrl.substring(0, baseUrl.length()-1);
        return new RestUrlBuilder(baseUrl);
    }

    public String buildUrl(String[] fragments, Object ... arguments) {
        return new StringBuilder(baseUrl)
                .append("/")
                .append(MessageFormat.format(StringUtils.join(fragments, '/'), arguments))
                .toString();
    }
}
