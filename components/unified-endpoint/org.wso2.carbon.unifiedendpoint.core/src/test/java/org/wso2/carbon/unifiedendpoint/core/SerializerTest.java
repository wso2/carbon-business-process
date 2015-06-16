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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class SerializerTest {

    public static void main(String[] args) {
        uepSerializerTests();
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

        /*Serialize*/
        UnifiedEndpointSerializer uepSerializer = new UnifiedEndpointSerializer();
        OMElement elem = uepSerializer.serializeUnifiedEndpoint(uep);

    }
}
