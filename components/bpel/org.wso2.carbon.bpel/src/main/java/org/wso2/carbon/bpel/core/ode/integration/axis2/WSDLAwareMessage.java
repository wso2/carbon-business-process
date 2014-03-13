/*
 * Copyright (c) 2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.bpel.core.ode.integration.axis2;

import org.apache.axiom.om.OMElement;

import javax.wsdl.Binding;
import java.util.HashMap;
import java.util.Map;

public class WSDLAwareMessage {
    private Map<String, OMElement> headerParts = new HashMap<String, OMElement>();
    private Map<String, OMElement> bodyParts = new HashMap<String, OMElement>();

    private boolean isRPC = false;
//    private String serviceName;
//    private String portName;
    private Binding binding;

    public void addBodyPart(String partName, OMElement partElement){
        bodyParts.put(partName, partElement);
    }

    public void addHeaderPart(String partName, OMElement partElement){
        headerParts.put(partName, partElement);
    }

//    public Element getBodyPart(String partName){
//        return OMUtils.toDOM(bodyParts.get(partName));
//    }
//
//    public Element getHeaderPart(String partName){
//        return OMUtils.toDOM(headerParts.get(partName));
//    }

    public Map<String, OMElement> getBodyParts(){
        return bodyParts;
    }

    public Map<String, OMElement> getHeaderParts(){
        return headerParts;
    }

    public boolean isRPC() {
        return isRPC;
    }

    public void setRPC(boolean rpc) {
        isRPC = rpc;
    }

//    public String getServiceName() {
//        return serviceName;
//    }
//
//    public void setServiceName(String serviceName) {
//        this.serviceName = serviceName;
//    }
//
//    public String getPortName() {
//        return portName;
//    }
//
//    public void setPortName(String portName) {
//        this.portName = portName;
//    }
//
//    public Binding getBinding() {
//        return binding;
//    }

    public void setBinding(Binding binding) {
        this.binding = binding;
    }
}
