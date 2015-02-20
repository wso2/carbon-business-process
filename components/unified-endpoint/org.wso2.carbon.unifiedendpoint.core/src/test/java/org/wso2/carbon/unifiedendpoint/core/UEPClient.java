/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.unifiedendpoint.core;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.client.Options;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;


public class UEPClient {
    public static final String AXIS2_REPO = "/home/kasun/development/apache/axis2/core/trunk/modules/distribution/target/axis2-SNAPSHOT/repository";
    public static final String AXIS2_XML_PATH = "/home/kasun/development/wso2/wso2svn/trunk/components/unified-endpoint/org.wso2.carbon.unifiedendpoint.core/src/main/resources/axis2.xml";

    public static void main(String[] args) throws Exception {

//        ConfigurationContext myConfigContext = ConfigurationContextFactory.createConfigurationContextFromFileSystem(AXIS2_REPO, AXIS2_XML_PATH);
//
//        myConfigContext.getAxisConfiguration().getModules();
//
//        ServiceClient serviceClient = new ServiceClient(myConfigContext, null);
//        Options opts = new Options();
//        serviceClient.engageModule("addressing");
//
//
//
//
//        UnifiedEndpointFactory uepFactory = new UnifiedEndpointFactory();
//
//        /*UnifiedEndpoint myUniEP = uepFactory.createEndpoint(SerializerTest.readString("/home/kasun/development/wso2/wso2svn/trunk/components/unified-endpoint/org.wso2.carbon.unifiedendpoint.core/src/main/resources/uep-test.xml"));*/
//        //UnifiedEndpoint myUniEP = uepFactory.createEndpoint(SerializerTest.readString("/home/kasun/development/wso2/wso2svn/trunk/components/unified-endpoint/org.wso2.carbon.unifiedendpoint.core/src/main/resources/uep-test.xml"));
//
//        //UnifiedEndpoint myUniEP = uepFactory.createVirtualEndpoint("file:/home/kasun/development/wso2/wso2svn/trunk/components/unified-endpoint/org.wso2.carbon.unifiedendpoint.core/src/main/resources/uep-test.xml");
//
//        UnifiedEndpoint myUniEP = uepFactory.createVirtualEndpoint("gov:uep1");
//
//        opts.setTo(myUniEP);
//        opts.setAction("urn:getQuote");
//        serviceClient.setOptions(opts);
//
//
//        OMElement res = serviceClient.sendReceive(createPayLoad());
//        System.out.println(res);
    }

    public static OMElement createPayLoad() {
        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace omNs = fac.createOMNamespace("http://services.samples", "m0");
        OMElement getQuoteOm = fac.createOMElement("getQuote", omNs);
        OMElement requestOm = fac.createOMElement("request", omNs);
        OMElement symbolOm = fac.createOMElement("symbol", omNs);
        symbolOm.setText("IBM");

        requestOm.addChild(symbolOm);
        getQuoteOm.addChild(requestOm);

        return getQuoteOm;

    }
}
