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
package org.wso2.carbon.bpel.core.ode.integration.utils;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.*;
import org.apache.axis2.util.MessageContextBuilder;
import org.apache.axis2.wsdl.WSDLConstants;
import org.wso2.carbon.bpel.core.BPELConstants;
import org.wso2.carbon.bpel.core.ode.integration.BPELMessageContext;
import org.wso2.carbon.bpel.core.ode.integration.BPELProcessProxy;
import org.wso2.carbon.bpel.core.ode.integration.PartnerService;
import org.wso2.carbon.bpel.core.ode.integration.axis2.WSDLAwareMessage;
import org.wso2.carbon.bpel.core.ode.integration.axis2.WSDLAwareSOAPProcessor;

import javax.wsdl.Binding;
import javax.wsdl.Definition;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.soap.SOAPBinding;
import javax.wsdl.extensions.soap12.SOAP12Binding;
import javax.xml.namespace.QName;

/**
 * Utility methods for creating and populating BPELMessageContext DTO.
 */
public final class BPELMessageContextFactory {

    // Utility classes should not have a public or default constructor.

    private BPELMessageContextFactory() {
    }

    /**
     * Create BPELMessageContext object using information from in message context.
     *
     * @param inMessageContext in message context
     * @param processProxy     BPEL process proxy object
     * @param soapFactory      SOAPFactory instance
     * @return BPELMessageContext instance
     * @throws AxisFault in case of a error(most of the times AxisFault will be thrown from methods
     *                   used inside the implementation).
     */
    public static BPELMessageContext createBPELMessageContext(final MessageContext inMessageContext,
                                                              final BPELProcessProxy processProxy,
                                                              final SOAPFactory soapFactory)
            throws AxisFault {
        BPELMessageContext bpelMessageContext =
                new BPELMessageContext(processProxy.getWsdlDefinition());

        bpelMessageContext.setInMessageContext(inMessageContext);
        bpelMessageContext.setSoapFactoryForCurrentMessageFlow(soapFactory);

        if (hasResponse(inMessageContext.getAxisOperation())) {
            setOutMessageContextToBPELMessageContext(bpelMessageContext);
        }

        fillBindingAndRelatedInformation(bpelMessageContext);

        bpelMessageContext.setRequestMessage(
                extractRequestMessageFromInMessageContext(inMessageContext));

        return bpelMessageContext;
    }

    public static BPELMessageContext createBPELMessageContext(
            final MessageContext inMessageContext,
            final PartnerService partnerService) {
        BPELMessageContext bpelMessageContext =
                new BPELMessageContext(partnerService.getWsdlDefinition());

        bpelMessageContext.setInMessageContext(inMessageContext);
        bpelMessageContext.setWsdlBindingForCurrentMessageFlow(partnerService.getBinding());
        setSOAPFactoryAndBindingStyle(bpelMessageContext);

        return bpelMessageContext;
    }

    private static void setOutMessageContextToBPELMessageContext(
            final BPELMessageContext bpelMessageContext)
            throws AxisFault {
        MessageContext outMessageContext = MessageContextBuilder
                .createOutMessageContext(bpelMessageContext.getInMessageContext());
        outMessageContext.getOperationContext().addMessageContext(outMessageContext);
        bpelMessageContext.setOutMessageContext(outMessageContext);
    }

    private static void fillBindingAndRelatedInformation(
            final BPELMessageContext bpelMessageContext) throws AxisFault {
        Binding wsdlBinding = getWSDLBindingOfCurrentMessageFlow(
                bpelMessageContext.getInMessageContext().getAxisService(),
                bpelMessageContext.getInMessageContext());
        if (wsdlBinding == null) {
            throw new NullPointerException("WSDL Binding null for incoming message.");
        }

        bpelMessageContext.setWsdlBindingForCurrentMessageFlow(wsdlBinding);
        setSOAPFactoryAndBindingStyle(bpelMessageContext);
    }

    private static void setSOAPFactoryAndBindingStyle(final BPELMessageContext bpelMessageContext) {
        ExtensibilityElement bindingType = WSDLAwareSOAPProcessor.getBindingExtension(
                bpelMessageContext.getWsdlBindingForCurrentMessageFlow());

        if (bpelMessageContext.getSoapFactoryForCurrentMessageFlow() == null) {
            if (bindingType instanceof SOAPBinding) {
                bpelMessageContext.setSoapFactoryForCurrentMessageFlow(
                        OMAbstractFactory.getSOAP11Factory());
            } else {
                bpelMessageContext.setSoapFactoryForCurrentMessageFlow(
                        OMAbstractFactory.getSOAP12Factory());
            }
        }

        deriveAndSetBindingStyle(bindingType, bpelMessageContext);
    }

    private static void deriveAndSetBindingStyle(final ExtensibilityElement bindingType,
                                                 final BPELMessageContext bpelMessageContext) {
        boolean isRPC = false;
        if (bindingType instanceof SOAPBinding) {
            isRPC = ((SOAPBinding) bindingType).getStyle() != null
                    && ((SOAPBinding) bindingType).getStyle().equalsIgnoreCase("rpc");
        } else if (bindingType instanceof SOAP12Binding) {
            isRPC = ((SOAP12Binding) bindingType).getStyle() != null
                    && ((SOAP12Binding) bindingType).getStyle().equalsIgnoreCase("rpc");
        }

        bpelMessageContext.setRPCStyleOperation(isRPC);
    }

    private static Binding getWSDLBindingOfCurrentMessageFlow(
            final AxisService bpelProcessService,
            final MessageContext inMessageContext) {
        Definition wsdl = getWSDLDefinitionFromAxisService(bpelProcessService);
        return extractBindingInformation(bpelProcessService, wsdl, inMessageContext);
    }

    private static Definition getWSDLDefinitionFromAxisService(final AxisService service) {
        Definition wsdlDefinition = (Definition) service
                .getParameter(BPELConstants.WSDL_4_J_DEFINITION).getValue();

        QName serviceName = new QName(service.getTargetNamespace(),
                service.getName());

        if (wsdlDefinition == null) {
            throw new NullPointerException("No WSDL Definition was found for service "
                    + serviceName.getLocalPart() + ".");
        }

        checkWhetherServiceDefinitionIsAvailable(wsdlDefinition, serviceName);

        return wsdlDefinition;
    }

    private static void checkWhetherServiceDefinitionIsAvailable(final Definition wsdlOfTheService,
                                                                 final QName serviceName) {
        if (wsdlOfTheService.getService(serviceName) == null) {
            throw new NullPointerException("WSDL Service Definition not found for service "
                    + serviceName.getLocalPart());
        }
    }

    private static Binding extractBindingInformation(final AxisService service,
                                                     final Definition wsdlOfService,
                                                     final MessageContext inMessageContext) {
        AxisEndpoint currentEndpoint = (AxisEndpoint) inMessageContext
                .getProperty(WSDL2Constants.ENDPOINT_LOCAL_NAME);
        if (currentEndpoint == null) {
            String defaultEndpointName = service.getEndpointName();
            currentEndpoint = service.getEndpoints().get(defaultEndpointName);
            if (currentEndpoint == null) {
                throw new NullPointerException("AxisEndpoint cannot be null.");
            }
        }

        AxisBinding currentAxisBinding = currentEndpoint.getBinding();
        QName bindingQName = currentAxisBinding.getName();

        return wsdlOfService.getBinding(bindingQName);
    }


    private static WSDLAwareMessage extractRequestMessageFromInMessageContext(
            MessageContext inMessageContext) throws AxisFault {
        WSDLAwareSOAPProcessor soapProcessor = new WSDLAwareSOAPProcessor(inMessageContext);
        return soapProcessor.parseRequest();
    }

    /**
     * Returns the web service operation style(in only/in-out).
     * // TODO: Move to a separate class
     *
     * @param op Interested Axis Operation
     * @return true if the operation is in-out, false otherwise.
     */
    public static boolean hasResponse(final AxisOperation op) {
        switch (op.getAxisSpecificMEPConstant()) {
            case WSDLConstants.MEP_CONSTANT_IN_OUT:
                return true;
            case WSDLConstants.MEP_CONSTANT_OUT_ONLY:
                return true;
            case WSDLConstants.MEP_CONSTANT_OUT_OPTIONAL_IN:
                return true;
            case WSDLConstants.MEP_CONSTANT_ROBUST_OUT_ONLY:
                return true;
            default:
                return false;
        }
    }

}
