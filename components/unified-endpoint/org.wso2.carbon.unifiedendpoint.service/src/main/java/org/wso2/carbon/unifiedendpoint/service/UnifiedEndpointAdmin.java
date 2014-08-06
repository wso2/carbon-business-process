/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.unifiedendpoint.service;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.synapse.config.xml.XMLConfigConstants;
import org.apache.synapse.registry.Registry;
import org.wso2.carbon.mediation.initializer.AbstractServiceBusAdmin;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.unifiedendpoint.core.UnifiedEndpointConstants;

import javax.xml.stream.XMLStreamException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;


public class UnifiedEndpointAdmin extends AbstractServiceBusAdmin {

    public String saveUnifiedEP(String key, String ele) {

        System.out.println("Key : " + key);
        try {
            org.wso2.carbon.registry.core.Registry registry;
            if(key.startsWith("conf:")) {
                registry = getConfigSystemRegistry();
                key = key.replace("conf:","");
            } else {
                registry = getGovernanceRegistry();
                key = key.replace("gov:","");
            }
            
            if (!registry.resourceExists(key)) {
                Resource resource = registry.newResource();
                resource.setMediaType(UnifiedEndpointConstants.WSO2_UNIFIED_ENDPOINT_MEDIA_TYPE);
                resource.setContent(ele);
                registry.put(key, resource);
            } else {

                registry.delete(key);

                Resource resource = registry.newResource();
                resource.setMediaType(UnifiedEndpointConstants.WSO2_UNIFIED_ENDPOINT_MEDIA_TYPE);
                resource.setContent(ele);
                registry.put(key, resource);

                /*Resource r = registry.get(key);
                r.setContent(ele);*/
                System.out.println("Resource already exists");
            }
        } catch (RegistryException e) {

        }


        /*if (unifiedEPElement.getQName().getLocalPart()
                .equals(XMLConfigConstants.ENDPOINT_ELT.getLocalPart())) {


        } else {
            System.out.println("invalid XML");
        }*/

        return "STATUS";
    }

    


    public String[] getDynamicEndpoints(int pageNumber, int endpointsPerPage) throws Exception {
        org.wso2.carbon.registry.core.Registry registry;
        System.out.println("AdminService - Retrieved content.....");
        try {
            registry = getConfigSystemRegistry();
            if (registry.getRegistryContext().isReadOnly()) {
                return null;
            }

            String[] configInfo = getMimeTypeResult(getConfigSystemRegistry());
            String[] govInfo = getMimeTypeResult(getGovernanceRegistry());
            String[] info = new String[configInfo.length + govInfo.length];

            int ptr = 0;
            for (String aConfigInfo : configInfo) {
                info[ptr] = "conf:" + aConfigInfo;
                ++ptr;
            }
            for (String aGovInfo : govInfo) {
                info[ptr] = "gov:" + aGovInfo;
                ++ptr;
            }

            Arrays.sort(info);
            
            String[] uepStrArray = new String[info.length];

            int uepPtr = 0;
            for (String key : info) {
                if (key.startsWith("conf:/")) {
                    String resourceKey = key.replaceFirst("conf:/", "");
                    if (resourceKey != null) {
                        Resource resource = registry.get(resourceKey);
                        if (resource != null) {
                            String strContent = new String((byte[]) resource.getContent());
                            uepStrArray[uepPtr++] = strContent;
                        }
                    }
                }
            }


            
            for (String key : info) {
                if (key.startsWith("gov:/")) {
                    org.wso2.carbon.registry.core.Registry govReg = getGovernanceRegistry();
                    String resourceKey = key.replaceFirst("gov:/", "");
                    if (resourceKey != null) {
                        Resource resource = govReg.get(resourceKey);
                        if (resource != null) {
                            String strContent = new String((byte[]) resource.getContent());
                            uepStrArray[uepPtr++] = strContent;
                        }
                    }
                }
            }

            /*Arrays.sort(info);

            String[] ret;
            if (info.length >= (endpointsPerPage * pageNumber + endpointsPerPage)) {
                ret = new String[endpointsPerPage];
            } else {
                ret = new String[info.length - (endpointsPerPage * pageNumber)];
            }
            for (int i = 0; i < endpointsPerPage; ++i) {
                if (ret.length > i)
                    ret[i] = info[endpointsPerPage * pageNumber + i];
            }*/

            return uepStrArray;
        } catch (RegistryException e) {
            return null;
        }
    }


    @SuppressWarnings({"unchecked"})
    private String[] getMimeTypeResult(org.wso2.carbon.registry.core.Registry targetRegistry) throws RegistryException {
        String sql = "SELECT REG_PATH_ID, REG_NAME FROM REG_RESOURCE WHERE REG_MEDIA_TYPE = ?";
        Map parameters = new HashMap();
        parameters.put("query",sql);
        parameters.put("1", UnifiedEndpointConstants.WSO2_UNIFIED_ENDPOINT_MEDIA_TYPE);
        Resource result = targetRegistry.executeQuery(null, parameters);
        return (String[]) result.getContent();
    }




}
