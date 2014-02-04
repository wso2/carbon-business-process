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
package org.wso2.carbon.bpel.core.ode.integration;

import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.impl.llom.soap12.SOAP12Factory;
import org.apache.axis2.context.MessageContext;
import org.wso2.carbon.bpel.core.ode.integration.axis2.WSDLAwareMessage;
import org.wso2.carbon.unifiedendpoint.core.UnifiedEndpoint;

import javax.wsdl.Binding;
import javax.wsdl.Definition;
import javax.xml.namespace.QName;
import java.util.List;

/**
 * Used as a Data Transfer Object. Will be created at the BPEL Message Receiver. Instance of this
 * will use as the container for context information until message receiver invokeBusinessLogic
 * method returns.
 */
public class BPELMessageContext {
    private Definition bpelServiceWSDLDefinition;

    private Binding wsdlBindingForCurrentMessageFlow;

    private SOAPFactory soapFactoryForCurrentMessageFlow;

    // Will carry the information about request message for process invocation and
    // will carry information about response message for partner invocations.
    // Can be null when invoking *in only partner services.
    private MessageContext inMessageContext;

    // Can be null for in only operations.
    // *We are not going to store out going message information here for partner invocations.
    private MessageContext outMessageContext;

    // faultMessageContext
    private MessageContext faultMessageContext;

    private boolean isRPCStyleOperation;

    private WSDLAwareMessage requestMessage;

    private UnifiedEndpoint uep;

    private String operationName;

    private boolean isTwoWay;

    private QName service;

    private String port;

    private String caller;

    /**
     * Associated list of attachment ids for the BPEL message context
     */
    private List attachmentIDs;

    public BPELMessageContext(Definition bpelServiceWSDLDefinition){
        this.bpelServiceWSDLDefinition = bpelServiceWSDLDefinition;
    }

    public Definition getBpelServiceWSDLDefinition() {
        return bpelServiceWSDLDefinition;
    }

//    public void setBpelServiceWSDLDefinition(Definition bpelServiceWSDLDefinition) {
//        this.bpelServiceWSDLDefinition = bpelServiceWSDLDefinition;
//    }

    public Binding getWsdlBindingForCurrentMessageFlow() {
        return wsdlBindingForCurrentMessageFlow;
    }

    public void setWsdlBindingForCurrentMessageFlow(Binding wsdlBindingForCurrentMessageFlow) {
        this.wsdlBindingForCurrentMessageFlow = wsdlBindingForCurrentMessageFlow;
    }

    public SOAPFactory getSoapFactoryForCurrentMessageFlow() {
        return soapFactoryForCurrentMessageFlow;
    }

    public void setSoapFactoryForCurrentMessageFlow(SOAPFactory soapFactoryForCurrentMessageFlow) {
        this.soapFactoryForCurrentMessageFlow = soapFactoryForCurrentMessageFlow;
    }

    public MessageContext getInMessageContext() {
        return inMessageContext;
    }

    public void setInMessageContext(MessageContext inMessageContext) {
        this.inMessageContext = inMessageContext;
    }

    public MessageContext getOutMessageContext() {
        return outMessageContext;
    }

    public void setOutMessageContext(MessageContext outMessageContext) {
        this.outMessageContext = outMessageContext;
    }

    public boolean isRPCStyleOperation() {
        return isRPCStyleOperation;
    }

    public void setRPCStyleOperation(boolean rpcStyleOperation) {
        isRPCStyleOperation = rpcStyleOperation;
    }

    public WSDLAwareMessage getRequestMessage() {
        return requestMessage;
    }

    public void setRequestMessage(WSDLAwareMessage requestMessage) {
        this.requestMessage = requestMessage;
    }

    public boolean isSoap12() {
        return soapFactoryForCurrentMessageFlow instanceof SOAP12Factory;
    }

    public MessageContext getFaultMessageContext() {
        return faultMessageContext;
    }

    public void setFaultMessageContext(MessageContext faultMessageContext) {
        this.faultMessageContext = faultMessageContext;
    }

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

    public void setTwoWay(boolean twoWay) {
        isTwoWay = twoWay;
    }

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

    /**
     * Assign a list of attachments to be associated with the BPEL Message Context
     *
     * @param attachmentIDs a list of attachments to be associated with the BPEL Message Context
     */
    public void setAttachmentIDList(List attachmentIDs) {
        this.attachmentIDs = attachmentIDs;
    }

    /**
     * Returns the list of attachments associated with the BPEL Message Context
     *
     * @return the list of attachments associated with the BPEL Message Context
     */
    public List getAttachmentIDList() {
        return attachmentIDs;
    }
}
