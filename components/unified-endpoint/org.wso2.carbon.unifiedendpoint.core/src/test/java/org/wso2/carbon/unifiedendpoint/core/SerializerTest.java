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

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.stream.XMLStreamException;
import java.io.*;

public class SerializerTest {
    private static Log log = LogFactory.getLog(SerializerTest.class);
    public static void main(String[] args) {


        uepSerializerTests();
        //virtualTest();


    }


    public static void uepSerializerTests() {
        UnifiedEndpoint uep = new UnifiedEndpoint();

        uep.setAddress("http://localhost:9000/services");
        uep.setUepId("UEP_ID_R_9299");
        uep.setDiscoveryUuid("UUID_99");
        uep.setDiscoveryScope("axis2");
        uep.setDiscoveryType("test");

        UnifiedEndpointMessageOutput uepMessageOutput = new UnifiedEndpointMessageOutput();
        uepMessageOutput.setCharSetEncoding("ca");
        uepMessageOutput.setFormat("soap");
        uepMessageOutput.setOptimize("test1");

        uep.setMessageOutput(uepMessageOutput);

        /*Transport*/
        UnifiedEndpointTransport unifiedEndpointTransport = new UnifiedEndpointTransport();
        unifiedEndpointTransport.setTransportType("http");
        unifiedEndpointTransport.addTransportProperty("PROP_1", "sdf");
        unifiedEndpointTransport.addTransportProperty("PROP_2", "sds");
        unifiedEndpointTransport.addTransportProperty("PROP_3", "sdg");
        unifiedEndpointTransport.addTransportProperty("PROP_4", "sdt");

        uep.setTransport(unifiedEndpointTransport);

        /*TimeOut*/
        UnifiedEndpointTimeout unifiedEndpointTimeout = new UnifiedEndpointTimeout();
        unifiedEndpointTimeout.addTimeOutProperty("SEND_TIMEOUT", "9000");
        unifiedEndpointTimeout.addTimeOutProperty("SEND_TIMEOUT_ACK", "1000");

        uep.setTimeout(unifiedEndpointTimeout);

        /*Cluster*/
        UnifiedEndpointCluster cluster = new UnifiedEndpointCluster();
        UnifiedEndpoint temp = new UnifiedEndpoint();
        temp.setUepId("dfd");
        temp.setAddress("dfsfjsdfjsjf");
        cluster.addClusteredUnifiedEndpoint(temp);

        System.out.println("our cluster " + cluster.getClusteredUnifiedEndpointList().get(0));
        uep.setUnifiedEndpointCluster(cluster);


        /*Serialize*/
        UnifiedEndpointSerializer uepSerializer = new UnifiedEndpointSerializer();
        OMElement elem = uepSerializer.serializeUnifiedEndpoint(uep);
        //System.out.println("OMSTR ... " + elem.toString());

/*
        try {
            FileWriter fileWriter = new FileWriter("/home/kasun/development/wso2/wso2svn/trunk/components/unified-endpoint/org.wso2.carbon.unifiedendpoint.core/src/main/resources/uep-test.xml");
            fileWriter.write(elem.toString());
            fileWriter.close();
        } catch (IOException e) {

        }

        System.out.println("Serialized Elem : " + elem.toString());*/


        /*Factory Test*/
        UnifiedEndpointFactory unifiedEndpointFactory = new UnifiedEndpointFactory();
        try {
            UnifiedEndpoint myEP = unifiedEndpointFactory.createEndpoint(elem);
            System.out.println("NEW EP : " + myEP.getTransport().getTransportProperties());

        } catch (AxisFault f) {

        }

    }

    public static void virtualTest() {
        String s = readString("/home/kasun/development/wso2/wso2svn/trunk/components/unified-endpoint/org.wso2.carbon.unifiedendpoint.core/src/main/resources/uep-test.xml");

        try {
            System.out.println("OM Str -" + AXIOMUtil.stringToOM(s));

            UnifiedEndpointFactory factory = new UnifiedEndpointFactory();
            UnifiedEndpoint uep = factory.createEndpoint(AXIOMUtil.stringToOM(s));

            System.out.println("uep add " + uep.getAddress());
        } catch (AxisFault axisFault) {
            log.error("Failed to process the uep-test.xml. Caused by axis2 fault", axisFault);
        } catch (XMLStreamException e) {
            log.error("Failed to process the uep-test.xml. Caused by xml streaming exception", e);
        }

    }


    public static String readString(String filePath) {
        String s = "";
        try {
            FileReader fr = new FileReader(filePath);
            BufferedReader br = new BufferedReader(fr);

            StringBuffer sb = new StringBuffer();
            String eachLine = br.readLine();

            while (eachLine != null) {
                sb.append(eachLine);
                sb.append("\n");
                eachLine = br.readLine();
            }
            s = sb.toString();

        } catch (FileNotFoundException e) {
            String errMsg = "File not found in the path "+ filePath;
            log.error(errMsg, e);
        } catch (IOException e) {
            String errMsg = "IO operation failed for "+ filePath;
            log.error(errMsg, e);
        }
        return s;
    }


    public static void omTest() {


        
    }
}
