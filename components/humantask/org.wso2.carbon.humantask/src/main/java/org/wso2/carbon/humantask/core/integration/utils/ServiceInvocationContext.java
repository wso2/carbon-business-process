/**
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
package org.wso2.carbon.humantask.core.integration.utils;

import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.impl.llom.soap12.SOAP12Factory;
import org.apache.axis2.context.MessageContext;
import org.wso2.carbon.unifiedendpoint.core.UnifiedEndpoint;

import javax.wsdl.Binding;
import javax.xml.namespace.QName;

/**
 * Used as a Data Transfer Object. Will be created at the BPEL Message Receiver. Instance of this
 * will use as the container for context information until message receiver invokeBusinessLogic
 * method returns.
 */
public class ServiceInvocationContext {

    private Binding wsdlBindingForCurrentMessageFlow;

    private SOAPFactory soapFactoryForCurrentMessageFlow;

    // Will carry the information about request message for process invocation and
    // will carry information about response message for partner invocations.
    // Can be null when invoking *in only partner services.
    private MessageContext inMessageContext;

    // faultMessageContext
//    private MessageContext faultMessageContext;

//    private boolean isRPCStyleOperation;
//
//    private WSDLAwareMessage requestMessage;

    private UnifiedEndpoint uep;

    private String operationName;

    private boolean isTwoWay = false;

    private QName service;

    private String port;

    private String caller;

    public Binding getWsdlBindingForCurrentMessageFlow() {
        return wsdlBindingForCurrentMessageFlow;
    }

    public void setWsdlBindingForCurrentMessageFlow(Binding wsdlBindingForCurrentMessageFlow) {
        this.wsdlBindingForCurrentMessageFlow = wsdlBindingForCurrentMessageFlow;
    }

//    public SOAPFactory getSoapFactoryForCurrentMessageFlow() {
//        return soapFactoryForCurrentMessageFlow;
//    }
//
//    public void setSoapFactoryForCurrentMessageFlow(SOAPFactory soapFactoryForCurrentMessageFlow) {
//        this.soapFactoryForCurrentMessageFlow = soapFactoryForCurrentMessageFlow;
//    }

    public MessageContext getInMessageContext() {
        return inMessageContext;
    }

    public void setInMessageContext(MessageContext inMessageContext) {
        this.inMessageContext = inMessageContext;
    }

//    public boolean isRPCStyleOperation() {
//        return isRPCStyleOperation;
//    }
//
//    public void setRPCStyleOperation(boolean rpcStyleOperation) {
//        isRPCStyleOperation = rpcStyleOperation;
//    }
//
//    public WSDLAwareMessage getRequestMessage() {
//        return requestMessage;
//    }
//
//    public void setRequestMessage(WSDLAwareMessage requestMessage) {
//        this.requestMessage = requestMessage;
//    }

    public boolean isSoap12() {
        return soapFactoryForCurrentMessageFlow instanceof SOAP12Factory;
    }

//    public MessageContext getFaultMessageContext() {
//        return faultMessageContext;
//    }
//
//    public void setFaultMessageContext(MessageContext faultMessageContext) {
//        this.faultMessageContext = faultMessageContext;
//    }

    public UnifiedEndpoint getUep() {
        return uep;
    }

    public void setUep(UnifiedEndpoint uep) {
        this.uep = uep;
    }

    public String getOperationName() {
        return operationName;
    }

    public void setOperationName(String operationName) {
        this.operationName = operationName;
    }

    public boolean isTwoWay() {
        return isTwoWay;
    }

//    public void setTwoWay(boolean twoWay) {
//        isTwoWay = twoWay;
//    }

    public QName getService() {
        return service;
    }

    public void setService(QName service) {
        this.service = service;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getCaller() {
        return caller;
    }

    public void setCaller(String caller) {
        this.caller = caller;
    }


}
