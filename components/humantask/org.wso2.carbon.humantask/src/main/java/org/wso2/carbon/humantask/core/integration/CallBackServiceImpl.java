/*
 * Copyright (c) 2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.humantask.core.integration;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAPFault;
import org.apache.axiom.soap.SOAPHeaderBlock;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bpel.common.SOAPHelper;
import org.wso2.carbon.bpel.common.config.EndpointConfiguration;
import org.wso2.carbon.humantask.core.CallBackService;
import org.wso2.carbon.humantask.core.HumanTaskConstants;
import org.wso2.carbon.humantask.core.deployment.HumanTaskDeploymentException;
import org.wso2.carbon.humantask.core.integration.utils.AxisServiceUtils;
import org.wso2.carbon.humantask.core.integration.utils.SOAPUtils;
import org.wso2.carbon.humantask.core.integration.utils.ServiceInvocationContext;
import org.wso2.carbon.humantask.core.internal.HumanTaskServiceComponent;
import org.wso2.carbon.unifiedendpoint.core.UnifiedEndpoint;
import org.wso2.carbon.unifiedendpoint.core.UnifiedEndpointConstants;

import javax.wsdl.*;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.http.HTTPBinding;
import javax.wsdl.extensions.soap.SOAPBinding;
import javax.wsdl.extensions.soap12.SOAP12Binding;
import javax.xml.namespace.QName;
import org.wso2.carbon.utils.CarbonUtils;

import java.util.List;

/**
 * Axis based implementation of CallBackService interface
 */
public class CallBackServiceImpl implements CallBackService {
    private static Log log = LogFactory.getLog(CallBackServiceImpl.class);

    private UnifiedEndpoint uep;

    private int tenantId;

    private QName serviceName;

    private String portName;

    private QName taskName;

    private Binding binding;

    private String operation;

    public CallBackServiceImpl(int tenantId, QName serviceName, String portName, QName taskName,
                               Definition wsdl, String operation,
                               EndpointConfiguration endpointConfig)
            throws HumanTaskDeploymentException {
        this.tenantId = tenantId;
        this.serviceName = serviceName;
        this.portName = portName;
        this.taskName = taskName;
        this.operation = operation;

        inferBindingInformation(wsdl);

        if (endpointConfig != null) {
            try {
                uep = endpointConfig.getUnifiedEndpoint();
            } catch (AxisFault axisFault) {
                String errMsg = "Error occurred while reading unified endpoint for callback " +
                        "service: " + serviceName + " of port: " + portName;
                log.error(errMsg, axisFault);
                throw new HumanTaskDeploymentException(errMsg, axisFault);
            }
        } else {
            uep = new UnifiedEndpoint();
            uep.setUepId(this.serviceName.getLocalPart());
            uep.setAddressingEnabled(true);
            uep.setAddressingVersion(UnifiedEndpointConstants.ADDRESSING_VERSION_FINAL);
            uep.setAddress(CarbonUtils.resolveSystemProperty(
                    AxisServiceUtils.getEPRfromWSDL(wsdl, serviceName, portName)));
        }
    }

    @Override
    public void invoke(OMElement payload, long taskId) throws AxisFault {
        final MessageContext mctx = new MessageContext();

        ServiceInvocationContext invocationContext = new ServiceInvocationContext();
        invocationContext.setInMessageContext(mctx);
        invocationContext.setUep(uep);
        invocationContext.setService(serviceName);
        invocationContext.setPort(portName);
        invocationContext.setCaller(taskName.getLocalPart());
        invocationContext.setWsdlBindingForCurrentMessageFlow(binding);
        invocationContext.setOperationName(operation);

        if (mctx.getEnvelope() == null) {
            mctx.setEnvelope(getSoapFactory().createSOAPEnvelope());
        }

        if (mctx.getEnvelope().getBody() == null) {
            getSoapFactory().createSOAPBody(mctx.getEnvelope());
        }

        if (mctx.getEnvelope().getHeader() == null) {
            getSoapFactory().createSOAPHeader(mctx.getEnvelope());
        }

        mctx.getEnvelope().getBody().addChild(payload);

        OMNamespace ns = OMAbstractFactory.getSOAP11Factory().createOMNamespace(HumanTaskConstants.B4P_NAMESPACE, "b4p");
        SOAPHeaderBlock header = mctx.getEnvelope().getHeader().addHeaderBlock(HumanTaskConstants.B4P_CORRELATION_HEADER, ns);
        header.addAttribute(HumanTaskConstants.B4P_CORRELATION_HEADER_ATTRIBUTE, Long.toString(taskId), ns);

        AxisServiceUtils.invokeService(invocationContext,
                HumanTaskServiceComponent.getHumanTaskServer().getTaskStoreManager().
                        getHumanTaskStore(tenantId).getConfigContext());
    }

    @Override
    public void invokeSkip(long taskID) throws AxisFault {
        sendProtocolMessage( taskID, HumanTaskConstants.HT_PROTOCOL_SKIPPED, String.valueOf(taskID));
    }

    @Override
    public void invokeFault(long taskID, String faultMessage) throws AxisFault {
       sendProtocolMessage( taskID, HumanTaskConstants.HT_PROTOCOL_FAULT,faultMessage);
    }

    private void sendProtocolMessage(long taskID, String headerValue,String value) throws AxisFault
    {
        final MessageContext mctx = new MessageContext();

        ServiceInvocationContext invocationContext = new ServiceInvocationContext();
        invocationContext.setInMessageContext(mctx);
        invocationContext.setUep(uep);
        invocationContext.setService(serviceName);
        invocationContext.setPort(portName);
        invocationContext.setCaller(taskName.getLocalPart());
        invocationContext.setWsdlBindingForCurrentMessageFlow(binding);
        invocationContext.setOperationName(operation);

        if (mctx.getEnvelope() == null) {
            mctx.setEnvelope(getSoapFactory().createSOAPEnvelope());
        }

        if (mctx.getEnvelope().getBody() == null) {
            getSoapFactory().createSOAPBody(mctx.getEnvelope());
        }

        if (mctx.getEnvelope().getHeader() == null) {
            getSoapFactory().createSOAPHeader(mctx.getEnvelope());
        }

        //Creating Dummy Element
        // Extracting MessageName
        List bindingOperations = binding.getBindingOperations();
        String messageName = "";
        OMNamespace serviceNS = null;
        BindingOperation oper;
        for (int i = 0; i < bindingOperations.size(); i++) {
            oper = (BindingOperation) bindingOperations.get(i);
            if (operation.equals(oper.getName())) {
                Message message = oper.getOperation().getInput().getMessage();
                messageName = message.getQName().getLocalPart();
                for (Object ob : message.getParts().keySet()) {   // Here we don't support RPC messages.
                    Part part = (Part) message.getParts().get(ob);
                    serviceNS = OMAbstractFactory.getSOAP11Factory().createOMNamespace(
                            part.getElementName().getNamespaceURI(), part.getElementName().getPrefix());
                    break;
                }
                break;
            }
        }


        OMElement payload = OMAbstractFactory.getOMFactory().createOMElement(messageName,serviceNS);
        mctx.getEnvelope().getBody().addChild(payload);

        OMNamespace htpNS = OMAbstractFactory.getSOAP11Factory().createOMNamespace(HumanTaskConstants.HT_PROTOCOL_NAMESPACE, HumanTaskConstants.HT_PROTOCOL_DEFAULT_PREFIX );
        SOAPHeaderBlock protocolHeader = mctx.getEnvelope().getHeader().addHeaderBlock(headerValue, htpNS);
        protocolHeader.setText(value);
        protocolHeader.addAttribute(HumanTaskConstants.B4P_CORRELATION_HEADER_ATTRIBUTE, Long.toString(taskID), htpNS);

        OMNamespace b4pNS = OMAbstractFactory.getSOAP11Factory().createOMNamespace(HumanTaskConstants.B4P_NAMESPACE, "b4p");
        SOAPHeaderBlock header = mctx.getEnvelope().getHeader().addHeaderBlock(HumanTaskConstants.B4P_CORRELATION_HEADER, b4pNS);
        header.addAttribute(HumanTaskConstants.B4P_CORRELATION_HEADER_ATTRIBUTE, Long.toString(taskID), b4pNS);

        AxisServiceUtils.invokeService(invocationContext,
                HumanTaskServiceComponent.getHumanTaskServer().getTaskStoreManager().
                        getHumanTaskStore(tenantId).getConfigContext());
    }

    private void inferBindingInformation(Definition wsdlDefinition)
            throws HumanTaskDeploymentException {
        Service serviceDef = wsdlDefinition.getService(serviceName);
        if (serviceDef == null) {
            throw new HumanTaskDeploymentException("Service element not found for callback service wsdl: " +
                    serviceName);
        }
        Port port = serviceDef.getPort(portName);
        if (port == null) {
            throw new HumanTaskDeploymentException("Port: " + portName + " not found for Service: " +
                    serviceName + " in the callback service wsdl");
        }

        binding = port.getBinding();
        if (binding == null) {
            throw new HumanTaskDeploymentException("Binding not found for port: " + portName +
                    " and Service: " + serviceName + " in the callback service wsdl");
        }

    }

    public SOAPFactory getSoapFactory() throws AxisFault {
        ExtensibilityElement bindingType = SOAPHelper.getBindingExtension(binding);

        if (!(bindingType instanceof SOAPBinding || bindingType instanceof SOAP12Binding ||
                bindingType instanceof HTTPBinding)) {
            throw new AxisFault("Service binding is not supported for service " + serviceName);
        }

        if (bindingType instanceof SOAPBinding) {
            return OMAbstractFactory.getSOAP11Factory();
        } else {
            return OMAbstractFactory.getSOAP12Factory();
        }
    }

}
