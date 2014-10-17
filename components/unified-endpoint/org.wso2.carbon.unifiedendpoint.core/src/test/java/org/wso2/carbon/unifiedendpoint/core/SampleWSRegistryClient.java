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

package org.wso2.carbon.unifiedendpoint.core;


import java.io.File;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.registry.core.Comment;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;

public class SampleWSRegistryClient {

    private String cookie = null;
    private static ConfigurationContext configContext = null;
    private static Log log = LogFactory.getLog(SampleWSRegistryClient.class);
    //private static final String CARBON_HOME = ".." + File.separator + ".." + File.separator;

    //private static final String CARBON_HOME = "/home/kasun/development/wso2/wso2-distrns/wso2esb-4.0.0-SNAPSHOT/" ;
    private static final String CARBON_HOME = "/home/kasun/development/wso2/wso2-distrns/wso2greg-3.5.1" ;

    private static String axis2Repo = CARBON_HOME + File.separator +"repository" + File.separator + "deployment" + File.separator + "client";
    private static String axis2Conf = CARBON_HOME + File.separator + "repository" + File.separator + "conf" + File.separator + "axis2_client.xml";
    String username = "admin";
    String password = "admin";
    private static String serverURL = "https://localhost:9443/services/";
    String policyPath = "META-INF/policy.xml";
    static WSRegistryServiceClient registry = null;

    private static WSRegistryServiceClient initialize() {

        System.setProperty("javax.net.ssl.trustStore", CARBON_HOME + File.separator + "resources" + File.separator + "security" + File.separator + "wso2carbon.jks");
        System.setProperty("javax.net.ssl.trustStorePassword", "wso2carbon");
        System.setProperty("javax.net.ssl.trustStoreType","JKS");
        try {
            configContext = ConfigurationContextFactory.createConfigurationContextFromFileSystem(axis2Repo, axis2Conf);

            registry =  new WSRegistryServiceClient(serverURL, "admin", "admin", configContext);
//            registry.addSecurityOptions(policyPath, CARBON_HOME + "/resources/security/wso2carbon.jks");

        } catch (AxisFault axisFault) {
            log.error("Failed to authenticate the client. Caused by axis2 fault", axisFault);
        } catch (RegistryException e) {
            log.error("Failed to authenticate the client. Caused by RegistryException", e);
        }
        return registry;
    }

	public static void main(String[] args) throws Exception {
		WSRegistryServiceClient client;
		client = initialize();
		Resource resource = client.newResource();
		resource.setContent("Hello Out there!");

		String resourcePath = "/abc";
		registry.put(resourcePath, resource);

		System.out.println("A resource added to" + resourcePath);

		registry.rateResource(resourcePath, 3);

		System.out.println("Resource rated with 3 stars!");
		Comment comment = new Comment();
		comment.setText("Wow! A comment out there");
		registry.addComment(resourcePath, comment);
		System.out.println("Comment added to resource");

		Resource getResource = registry.get("/abc");
		System.out.println("Resource retrived");
		System.out.println("Printing retrieved resource content: " + new String((byte[])getResource.getContent()));

	}
}
