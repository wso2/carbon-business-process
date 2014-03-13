/*
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.bpel.b4p.extension;

import com.ibm.wsdl.extensions.soap.SOAPAddressImpl;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.common.FaultException;
import org.apache.ode.bpel.dao.AttachmentDAO;
import org.apache.ode.bpel.dd.DeployDocument;
import org.apache.ode.bpel.dd.TDeployment;
import org.apache.ode.bpel.dd.TInvoke;
import org.apache.ode.bpel.dd.TProvide;
import org.apache.ode.bpel.engine.MessageExchangeImpl;
import org.apache.ode.bpel.epr.WSDL11Endpoint;
import org.apache.ode.bpel.iapi.MessageExchange;
import org.apache.ode.bpel.o.OPartnerLink;
import org.apache.ode.bpel.o.OProcess;
import org.apache.ode.bpel.runtime.BpelRuntimeContext;
import org.apache.ode.bpel.runtime.PartnerLinkInstance;
import org.apache.ode.bpel.runtime.extension.ExtensionContext;
import org.apache.ode.store.DeploymentUnitDir;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.bpel.b4p.coordination.B4PCoordinationException;
import org.wso2.carbon.bpel.b4p.coordination.configuration.CoordinationConfiguration;
import org.wso2.carbon.bpel.b4p.coordination.dao.HTCoordinationDAOConnection;
import org.wso2.carbon.bpel.b4p.coordination.dao.HTProtocolHandlerDAO;
import org.wso2.carbon.bpel.b4p.internal.B4PContentHolder;
import org.wso2.carbon.bpel.b4p.internal.B4PServiceComponent;
import org.wso2.carbon.bpel.b4p.utils.SOAPHelper;
import org.wso2.carbon.bpel.common.config.EndpointConfiguration;
import org.wso2.carbon.bpel.core.ode.integration.BPELMessageContext;
import org.wso2.carbon.bpel.core.ode.integration.BPELServerImpl;
import org.wso2.carbon.bpel.core.ode.integration.store.ProcessConfigurationImpl;
import org.wso2.carbon.bpel.core.ode.integration.utils.AxisServiceUtils;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.unifiedendpoint.core.UnifiedEndpoint;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.NetworkUtils;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import javax.wsdl.*;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.http.HTTPBinding;
import javax.wsdl.extensions.soap.SOAPBinding;
import javax.wsdl.extensions.soap12.SOAP12Binding;
import javax.xml.namespace.QName;
import java.io.File;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;

public class PeopleActivity {
    private final Log log = LogFactory.getLog(PeopleActivity.class);

    private String name;
    private String inputVarName;
    private String outputVarName;
    private boolean isSkipable = false;

    private String partnerLinkName;
    private String operation;
    private String callbackOperationName;

    private String serviceURI;
    private String servicePort;
    private String callbackServicePort;

    private InteractionType activityType;

    private QName processId;

    private boolean isRPC = false;
    private boolean isTwoWay = true;

    private QName serviceName;
    private QName callbackServiceName;
    private Definition hiWSDL;

    private AttachmentPropagation attachmentPropagation;


    private static final long serialVersionUID = -89894857418738012L;

//    public InteractionType getActivityType() {
//        return activityType;
//    }
//
//    public QName getCallbackServiceName() {
//        return callbackServiceName;
//    }
//
//    public String getCallbackServicePort() {
//        return callbackServicePort;
//    }

    public PeopleActivity(ExtensionContext extensionContext, Element element) throws FaultException {
        init(extensionContext, element);
    }

//    public Element getInputMessage(ExtensionContext extensionContext) throws FaultException {
//        Node inputNode = extensionContext.readVariable(inputVarName);
//
//        if (inputNode.getNodeType() == Node.ELEMENT_NODE) {
//            return (Element) inputNode;
//        } else {
//            log.error("The node type of the variable is not ELEMENT");
//            throw new FaultException(new QName(BPEL4PeopleConstants.B4P_NAMESPACE, "Unsupported variable type"));
//        }
//    }

    public Operation getOperation(ExtensionContext extensionContext) {
        BpelRuntimeContext runTimeContext = extensionContext.getInternalInstance();

        OProcess process = runTimeContext.getProcessModel();

        OPartnerLink partnerLink = process.getPartnerLink(partnerLinkName);

        return partnerLink.getPartnerRoleOperation(operation);
    }

//    public Operation getCallbackOperation(ExtensionContext extensionContext) {
//        BpelRuntimeContext runTimeContext = extensionContext.getInternalInstance();
//
//        OProcess process = runTimeContext.getProcessModel();
//
//        OPartnerLink partnerLink = process.getPartnerLink(partnerLinkName);
//
//        return partnerLink.getMyRoleOperation(callbackOperationName);
//
//    }

    public String getOperationName() {
        return operation;
    }

//    public String getEPRURL() {
//        return serviceURI;
//    }

    public String getServicePort() {
        return servicePort;
    }

    public String getOutputVarName() {
        return outputVarName;
    }

//    public DeploymentUnitDir getDu() {
//        return du;
//    }

    public QName getServiceName() {
        return serviceName;
    }

//    public Definition getHiWSDL() {
//        return hiWSDL;
//    }

//    public Definition getCallbackWSDL() {
//        return du.getDefinitionForService(callbackServiceName);
//    }

//    public String getCallbackOperationName() {
//        return callbackOperationName;
//    }
//
//    public String getPartnerLinkName() {
//        return partnerLinkName;
//    }

    /**
     * Parse a localTask node and extract the information
     *
     * @param localTaskNode Node reference of the localTask
     */
    private void parseLocalTask(Node localTaskNode) {
        activityType = InteractionType.NOTIFICATION;
        String warnMsg = localTaskNode.getLocalName() + " is not supported yet!";
        log.warn(warnMsg);
        throw new RuntimeException(warnMsg);
    }

    /**
     * Parse a localNotification node and extract the information
     *
     * @param localNotificationNode Node reference of the localNotification
     */
    private void parseLocalNotification(Node localNotificationNode) {
        activityType = InteractionType.NOTIFICATION;
        String warnMsg = localNotificationNode.getLocalName() + " is not supported yet!";
        log.warn(warnMsg);
        throw new RuntimeException(warnMsg);
    }

    /**
     * Parse a remoteNotification node and extract the information
     * eg -
     * <b4p:remoteNotification partnerLink="b4pNPartnerLink" operation="notify"></b4p:remoteNotification>
     *
     * @param remoteNotificationNode Node reference of the remoteNotification
     */
    private void parseRemoteNotification(Node remoteNotificationNode) {
        activityType = InteractionType.NOTIFICATION;
        if (remoteNotificationNode.getNodeType() == Node.ELEMENT_NODE) {
            Element remoteTaskEle = (Element) remoteNotificationNode;
            partnerLinkName = remoteTaskEle.
                    getAttribute(BPEL4PeopleConstants.PEOPLE_ACTIVITY_PARTNER_LINK);
            operation = remoteTaskEle.
                    getAttribute(BPEL4PeopleConstants.PEOPLE_ACTIVITY_OPERATION);
            if (log.isDebugEnabled()) {
                log.debug("name: " + name + " inputVarName: " + inputVarName + " partnerLinkName: " +
                        partnerLinkName + " operation: " + operation);
            }
        } //TODO what if NODE type is not ELEMENT_NODE
    }

    /**
     * Parse a remoteTask node and extract the information
     * eg -
     * <b4p:remoteTask partnerLink="b4pPartnerLink" operation="approve" responseOperation="approvalResponse">
     * </b4p:remoteTask>
     *
     * @param remoteTaskNode Node reference of the remoteTask
     */
    private void parseRemoteTask(Node remoteTaskNode) {
        activityType = InteractionType.TASK;
        if (remoteTaskNode.getNodeType() == Node.ELEMENT_NODE) {
            Element remoteTaskEle = (Element) remoteTaskNode;
            partnerLinkName =
                    remoteTaskEle.getAttribute(BPEL4PeopleConstants.PEOPLE_ACTIVITY_PARTNER_LINK);
            operation = remoteTaskEle.getAttribute(BPEL4PeopleConstants.PEOPLE_ACTIVITY_OPERATION);
            callbackOperationName = remoteTaskEle.getAttribute(BPEL4PeopleConstants.PEOPLE_ACTIVITY_RESPONSE_OPERATION);
            if (log.isDebugEnabled()) {
                log.debug("name: " + name + " inputVarName: " + inputVarName +
                        " outPutVarName: " + outputVarName + " isSkipable: " +
                        isSkipable + " partnerLinkName: " + partnerLinkName + " operation: " +
                        operation + " responseOperation: " + callbackOperationName);
            }
        } //TODO what if NODE type is not ELEMENT_NODE
    }

    /**
     * Determine the type of the standard element
     *
     * @param element element to be processed.
     * @return the type of Standard Element
     * @throws FaultException if the given element is not a standard element
     */
    private String getTypeOfStandardElement(Node element) throws FaultException {
        if (element.getLocalName().equals(BPEL4PeopleConstants.PEOPLE_ACTIVITY_REMOTE_TASK)) {
            return BPEL4PeopleConstants.PEOPLE_ACTIVITY_REMOTE_TASK;
        } else if (element.getLocalName().equals(BPEL4PeopleConstants.PEOPLE_ACTIVITY_REMOTE_NOTIFICATION)) {
            return BPEL4PeopleConstants.PEOPLE_ACTIVITY_REMOTE_NOTIFICATION;
        } else if (element.getLocalName().equals(BPEL4PeopleConstants.PEOPLE_ACTIVITY_LOCAL_NOTIFICATION)) {
            return BPEL4PeopleConstants.PEOPLE_ACTIVITY_LOCAL_NOTIFICATION;
        } else if (element.getLocalName().equals(BPEL4PeopleConstants.PEOPLE_ACTIVITY_LOCAL_TASK)) {
            return BPEL4PeopleConstants.PEOPLE_ACTIVITY_LOCAL_TASK;
        } else {
            throw new FaultException(BPEL4PeopleConstants.B4P_FAULT,
                    "The given element:" + element.getLocalName() +
                            " is not a standard Element.");
        }
    }

    /**
     * In a peopleActivity element there can be exist only one standard element.
     * eg - Standard Elements are
     * 1. task
     * 2. localTask
     * 3. remoteTask
     * 4. remoteNotification etc.
     * <p/>
     * This method verify there's only one standard element exist and return the type of that standard element
     *
     * @return
     */
    private String extractStandardElementType(Element parentElement) throws FaultException {
        NodeList taskList = parentElement.getChildNodes();
        String elementType = null;
        int standardElementCounter = 0;
        for (int i = 0; i < taskList.getLength(); i++) {
            if (taskList.item(i).getNodeType() == Node.ELEMENT_NODE) {
                try {
                    elementType = getTypeOfStandardElement(taskList.item(i));

                    standardElementCounter++;
                    if (standardElementCounter > 1) {
                        throw new FaultException(BPEL4PeopleConstants.B4P_FAULT,
                                "There is exist more than one standard child elements in " +
                                        BPEL4PeopleConstants.PEOPLE_ACTIVITY);
                    }

                } catch (FaultException e) {
                    //Do nothing
                }
            }
        }
        if (elementType != null) {
            return elementType;
        } else {
            throw new FaultException(BPEL4PeopleConstants.B4P_FAULT,
                    "There is no standard child elements defined in " +
                            BPEL4PeopleConstants.PEOPLE_ACTIVITY);
        }
    }

    /**
     * Determine what the standard element is and process it
     *
     * @param peopleActivityElement
     * @throws FaultException
     */
    private void processStandardElement(Element peopleActivityElement) throws FaultException {
        try {
            String elementType = extractStandardElementType(peopleActivityElement);

            if (elementType.equals(BPEL4PeopleConstants.PEOPLE_ACTIVITY_REMOTE_TASK)) {
                Node node = peopleActivityElement.getElementsByTagNameNS(BPEL4PeopleConstants.B4P_NAMESPACE,
                        BPEL4PeopleConstants
                                .PEOPLE_ACTIVITY_REMOTE_TASK).item(0);
                if (node == null) {
                    throw new FaultException(BPEL4PeopleConstants.B4P_FAULT,
                            "Namespace for element:" + elementType + " is not " +
                                    BPEL4PeopleConstants.B4P_NAMESPACE);
                }
                parseRemoteTask(node);
            } else if (elementType.equals(BPEL4PeopleConstants.PEOPLE_ACTIVITY_REMOTE_NOTIFICATION)) {
                Node node = peopleActivityElement.getElementsByTagNameNS(BPEL4PeopleConstants
                        .B4P_NAMESPACE, BPEL4PeopleConstants
                        .PEOPLE_ACTIVITY_REMOTE_NOTIFICATION).item(0);
                if (node == null) {
                    throw new FaultException(BPEL4PeopleConstants.B4P_FAULT,
                            "Namespace for element:" + elementType + " is not " +
                                    BPEL4PeopleConstants.B4P_NAMESPACE);
                }
                parseRemoteNotification(node);
            } else if (elementType.equals(BPEL4PeopleConstants.PEOPLE_ACTIVITY_LOCAL_NOTIFICATION)) {
                Node node = peopleActivityElement.getElementsByTagNameNS(BPEL4PeopleConstants
                        .B4P_NAMESPACE, BPEL4PeopleConstants
                        .PEOPLE_ACTIVITY_LOCAL_NOTIFICATION).item(0);

                if (node == null) {
                    throw new FaultException(BPEL4PeopleConstants.B4P_FAULT,
                            "Namespace for element:" + elementType + " is not " +
                                    BPEL4PeopleConstants.B4P_NAMESPACE);
                }
                parseLocalNotification(node);
            } else if (elementType.equals(BPEL4PeopleConstants.PEOPLE_ACTIVITY_LOCAL_TASK)) {
                Node node = peopleActivityElement.getElementsByTagNameNS(BPEL4PeopleConstants.B4P_NAMESPACE,
                        BPEL4PeopleConstants
                                .PEOPLE_ACTIVITY_LOCAL_TASK).item(0);

                if (node == null) {
                    throw new FaultException(BPEL4PeopleConstants.B4P_FAULT,
                            "Namespace for element:" + elementType + " is not " +
                                    BPEL4PeopleConstants.B4P_NAMESPACE);
                }
                parseLocalTask(node);
            }
        } catch (FaultException fex) {
            throw fex;
        }
    }

    /**
     * Process the &lt;attachmentPropagation/&gt; element defined under peopleActivity
     *
     * @param peopleActivityElement peopleActivity (parent element) where the &lt;attachmentPropagation/&gt; resides
     * @throws FaultException can be raised if there are more than one &lt;attachmentPropagation/&gt; elements
     *                        defined or the namespace for the &lt;attachmentPropagation/&gt; element is not defined correctly
     */
    private void processAttachmentPropagationElement(Element peopleActivityElement)
            throws FaultException {
        NodeList attachmentElementList =
                peopleActivityElement.getElementsByTagNameNS(BPEL4PeopleConstants.B4P_NAMESPACE,
                        BPEL4PeopleConstants
                                .ATTACHMENT_PROPAGATION_ACTIVITY);
        if (attachmentElementList.getLength() > 1) {
            throw new FaultException(BPEL4PeopleConstants.B4P_FAULT,
                    "More than one elements defined for:" + BPEL4PeopleConstants
                            .ATTACHMENT_PROPAGATION_ACTIVITY + " inside " +
                            BPEL4PeopleConstants.PEOPLE_ACTIVITY);
        } else if (attachmentElementList.getLength() == 1) {
            // <attachmentPropagation/> element processing logic
            Node attachmentPropagationElement = attachmentElementList.item(0);
            this.attachmentPropagation =
                    new AttachmentPropagation((Element) attachmentPropagationElement);
        } else if (attachmentElementList.getLength() == 0) {
            //As the BPEL4PeopleConstants.ATTACHMENT_PROPAGATION_ACTIVITY is not declared. So handling the default behavior
            if (log.isDebugEnabled()) {
                log.debug("No " + BPEL4PeopleConstants.ATTACHMENT_PROPAGATION_ACTIVITY + " " +
                        "activities found. Hence " +
                        "assuming the default values defined by specification.");
            }
            this.attachmentPropagation = new AttachmentPropagation();
        } else {
            if (peopleActivityElement.
                    getElementsByTagName(BPEL4PeopleConstants.ATTACHMENT_PROPAGATION_ACTIVITY)
                    .getLength() > 0) {
                throw new FaultException(BPEL4PeopleConstants.B4P_FAULT,
                        "Namespace defined for :" + BPEL4PeopleConstants
                                .ATTACHMENT_PROPAGATION_ACTIVITY + " inside " +
                                BPEL4PeopleConstants.PEOPLE_ACTIVITY + " is wrong.");
            }
        }
    }

    private void init(ExtensionContext extensionContext, Element element) throws FaultException {
        if (!element.getLocalName().equals(BPEL4PeopleConstants.PEOPLE_ACTIVITY) ||
                !element.getNamespaceURI().equals(BPEL4PeopleConstants.B4P_NAMESPACE)) {
            throw new FaultException(BPEL4PeopleConstants.B4P_FAULT,
                    "No " + BPEL4PeopleConstants.PEOPLE_ACTIVITY + " activity found");
        }

        name = element.getAttribute(BPEL4PeopleConstants.PEOPLE_ACTIVITY_NAME);
        inputVarName = element.getAttribute(BPEL4PeopleConstants.PEOPLE_ACTIVITY_INPUT_VARIABLE);
        outputVarName = element.getAttribute(BPEL4PeopleConstants.PEOPLE_ACTIVITY_OUTPUT_VARIABLE);
        isSkipable = "yes".equalsIgnoreCase(
                element.getAttribute(BPEL4PeopleConstants.PEOPLE_ACTIVITY_IS_SKIPABLE));

        processStandardElement(element);

        processAttachmentPropagationElement(element);

        DeploymentUnitDir du = new DeploymentUnitDir(new File(extensionContext.getDUDir()));
        processId = new QName(extensionContext.getProcessModel().getQName().getNamespaceURI(),
                extensionContext.getProcessModel().getQName().getLocalPart() + "-" +
                        du.getStaticVersion());


        isTwoWay = activityType.equals(InteractionType.TASK);

        deriveServiceEPR(du, extensionContext);
    }

    private void deriveServiceEPR(DeploymentUnitDir du, ExtensionContext extensionContext)
            throws FaultException {
        DeployDocument deployDocument = du.getDeploymentDescriptor();
        BpelRuntimeContext runTimeContext = extensionContext.getInternalInstance();

        //TODO neeed to extend ExtentionContext
        OProcess oProcess = runTimeContext.getProcessModel();

        TDeployment.Process hiProcess = null;
        List<TDeployment.Process> processList = deployDocument.getDeploy().getProcessList();
        for (TDeployment.Process process : processList) {
            if (process.getName().equals(oProcess.getQName())) {
                hiProcess = process;
                break;
            }
        }

        if (hiProcess == null) {
            throw new FaultException(BPEL4PeopleConstants.B4P_FAULT, "Related process: " +
                    oProcess.getQName() + " not found");
        }

        List<TInvoke> tInvokeList = hiProcess.getInvokeList();
        for (TInvoke tInvoke : tInvokeList) {
            if (tInvoke.getPartnerLink().equals(partnerLinkName)) {
                serviceName = tInvoke.getService().getName();
                servicePort = tInvoke.getService().getPort();
                break;
            }
        }

        if (serviceName == null || servicePort == null) {
            log.error("service and port for human interaction is not found in the deploy.xml");
            throw new FaultException(BPEL4PeopleConstants.B4P_FAULT,
                    "Service or port for human interaction is not found in the deploy.xml");
        }

        //get the callback information for the TASK
        if (activityType.equals(InteractionType.TASK)) {
            List<TProvide> tProvideList = hiProcess.getProvideList();
            for (TProvide tProvide : tProvideList) {
                if (tProvide.getPartnerLink().equals(partnerLinkName)) {
                    callbackServiceName = tProvide.getService().getName();
                    callbackServicePort = tProvide.getService().getPort();
                    break;
                }
            }
            if (callbackServiceName == null || callbackServicePort == null) {
                throw new FaultException(BPEL4PeopleConstants.B4P_FAULT,
                        "Service or port for human task callback is not found in the deploy.xml");
            }
        }

        hiWSDL = du.getDefinitionForService(serviceName);

        Service service = hiWSDL.getService(serviceName);
        Port port = service.getPort(servicePort);
        List extList = port.getExtensibilityElements();
        for (Object extEle : extList) {
            if (extEle instanceof SOAPAddressImpl) {
                SOAPAddressImpl soapAddress = (SOAPAddressImpl) extEle;
                serviceURI = soapAddress.getLocationURI();
                break;
            }
        }

        if (serviceURI == null) {
            throw new FaultException(BPEL4PeopleConstants.B4P_FAULT, "Service URI is not available");
        }
    }

    public Binding getBinding() throws FaultException {
        Service serviceDef = hiWSDL.getService(serviceName);
        if (serviceDef == null) {
            throw new FaultException(BPEL4PeopleConstants.B4P_FAULT,
                    "Service definition is not available for service " + serviceName);
        }
        Port port = serviceDef.getPort(getServicePort());
        if (port == null) {
            throw new FaultException(BPEL4PeopleConstants.B4P_FAULT,
                    "Service port is not available for service " + serviceName + " and port " +
                            getServicePort());
        }

        Binding binding = port.getBinding();

        if (binding == null) {
            throw new FaultException(BPEL4PeopleConstants.B4P_FAULT,
                    "Service binding is not available for service " + serviceName + " and port " +
                            getServicePort());
        }

        return binding;
    }

    public SOAPFactory getSoapFactory() throws FaultException {
        Binding binding = getBinding();
        ExtensibilityElement bindingType = SOAPHelper.getBindingExtension(binding);

        if (!(bindingType instanceof SOAPBinding || bindingType instanceof SOAP12Binding ||
                bindingType instanceof HTTPBinding)) {
            throw new FaultException(BPEL4PeopleConstants.B4P_FAULT,
                    "Service binding is not supported for service " + serviceName + " and port " +
                            getServicePort());
        }

        if (bindingType instanceof SOAPBinding) {
            return OMAbstractFactory.getSOAP11Factory();
        } else {
            return OMAbstractFactory.getSOAP12Factory();
        }
    }

    public UnifiedEndpoint getUnifiedEndpoint() throws FaultException {
        int tenantId = B4PServiceComponent.getBPELServer().getMultiTenantProcessStore().
                getTenantId(processId);
        ProcessConfigurationImpl processConf = (ProcessConfigurationImpl) B4PServiceComponent.
                getBPELServer().getMultiTenantProcessStore().getTenantsProcessStore(tenantId).
                getProcessConfiguration(processId);
        EndpointConfiguration epConf = processConf.
                getEndpointConfiguration(new WSDL11Endpoint(serviceName, servicePort));
        try {
            return epConf.getUnifiedEndpoint();
        } catch (AxisFault axisFault) {
            throw new FaultException(BPEL4PeopleConstants.B4P_FAULT,
                    "Error occurred while reading UnifiedEndpoint for service " + serviceName,
                    axisFault);
        }
    }

    public ConfigurationContext getConfigurationContext() throws FaultException {
        int tenantId = B4PServiceComponent.getBPELServer().getMultiTenantProcessStore().
                getTenantId(processId);
        ProcessConfigurationImpl processConf = (ProcessConfigurationImpl) B4PServiceComponent.
                getBPELServer().getMultiTenantProcessStore().getTenantsProcessStore(tenantId).
                getProcessConfiguration(processId);
        return processConf.getTenantConfigurationContext();
    }

//    public WSDLAwareMessage getWSDLAwareMessage() throws FaultException {
//        Element request = getInputMessage();
//        WSDLAwareMessage message = new WSDLAwareMessage();
//        message.setBinding(getBinding());
//        message.setPortName(servicePort);
//        message.setRPC(isRPC);
//        message.setServiceName(serviceName.getLocalPart());
//        message.addBodyPart();
//    }

    /**
     * Process the current runtime context and generate a list of attachment ids bind to the current runtime context.
     *
     * @return a list of attachment ids
     */
    private Collection<Long> getAttachmentIDs(ExtensionContext extensionContext) {
        Collection<Long> attachmentIDs = new ArrayList<Long>();
        Collection<MessageExchange> mexList =
                extensionContext.getInternalInstance().getMessageExchangeDAOs();

        for (MessageExchange mex : mexList) {
            //extract the available mex references

            MessageExchangeImpl mexImpl = (MessageExchangeImpl) mex;
            Collection<AttachmentDAO> attachmentDAOList = mexImpl.getDAO().getAttachments();

            for (AttachmentDAO dao : attachmentDAOList) {
                attachmentIDs.add(dao.getId());
            }
        }

        //TODO log.warn("Here we return a one level list, so the client doesn't knows which " +
        //        "attachment ids are bind to which message exchanges");
        return attachmentIDs;
    }

    /**
     * Generate an attachment id list to be sent to the human-task based on the fromProcess attribute defined inside
     * &lt;attachmentPropagation/&gt; element
     *
     * @param extensionContext
     * @param taskMessageContext
     * @return a list of attachment ids. The list can be empty if attachment propagation is not allowed from process
     *         to human task
     */
    private List<Long> extractAttachmentIDsToBeSentToHumanTask(ExtensionContext extensionContext,
                                                               BPELMessageContext taskMessageContext) {
        List<Long> attachmentIDList = new ArrayList<Long>();
        if (attachmentPropagation != null && attachmentPropagation.isInitialized) {
            if (FromProcessSpec.all.toString().equals(attachmentPropagation.getFromProcess())) {
                attachmentIDList = (List<Long>) getAttachmentIDs(extensionContext);
                taskMessageContext.setAttachmentIDList(attachmentIDList);
            } else if (FromProcessSpec.none.toString().equals(attachmentPropagation.getFromProcess())) {
                if (log.isDebugEnabled()) {
                    log.debug("No attachments will be propagated to the human-task as attribute value of " +
                            BPEL4PeopleConstants.ATTACHMENT_PROPAGATION_ACTIVITY_FROM_PROCESS +
                            " is " + FromProcessSpec.none);
                }
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("AttachmentPropagation element is not initialized yet." +
                        " So attachments are ignored by the BPEL4People extension runtime.");
            }
        }
        return attachmentIDList;
    }

    public String invoke(ExtensionContext extensionContext) throws FaultException {
        BPELMessageContext taskMessageContext = new BPELMessageContext(hiWSDL);
        UUID messageID = null;

        int tenantId = B4PServiceComponent.getBPELServer().getMultiTenantProcessStore().
                getTenantId(processId);
        String tenantDomain = null;
        try {
            tenantDomain = B4PContentHolder.getInstance().getRealmService().getTenantManager().getDomain(tenantId);
        } catch (UserStoreException e) {
            log.error(" Cannot find the tenant domain " + e.toString());
        }

        if(tenantDomain == null) {
            tenantDomain = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
        }

        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain);

        try {
            //Setting the attachment id attachmentIDList
            List<Long> attachmentIDList = extractAttachmentIDsToBeSentToHumanTask(extensionContext,
                    taskMessageContext);
            taskMessageContext.setOperationName(getOperationName());

            SOAPHelper soapHelper = new SOAPHelper(getBinding(), getSoapFactory(), isRPC);
            MessageContext messageContext = new MessageContext();

            /*
            Adding attachment ID list as a method input to createSoapRequest makes no sense.
            Have to fix. Here we can't embed attachments in MessageContext, as we have only a
            list of attachment ids.
            */


            soapHelper.createSoapRequest(messageContext,
                    (Element) extensionContext.readVariable(inputVarName),
                    getOperation(extensionContext), attachmentIDList);

            // Coordination Context and skipable attribute is only valid for a Task.
            if (InteractionType.TASK.equals(activityType)) {

                //Adding HumanTask Coordination context.
                //Note: If registration service is not enabled, we don't need to send coor-context.
                if (CoordinationConfiguration.getInstance().isHumantaskCoordinationEnabled() && CoordinationConfiguration.getInstance().isRegistrationServiceEnabled()) {
                    messageID = UUID.randomUUID();
                    soapHelper.addCoordinationContext(messageContext, messageID.toString(), getRegistrationServiceURL());
                }

                //  Adding HumanTask Context overriding attributes.
                soapHelper.addOverridingHumanTaskAttributes(messageContext, isSkipable);
            }

            taskMessageContext.setInMessageContext(messageContext);
            taskMessageContext.setPort(getServicePort());
            taskMessageContext.setService(getServiceName());
            taskMessageContext.setRPCStyleOperation(isRPC);
            taskMessageContext.setTwoWay(isTwoWay);
            taskMessageContext.setSoapFactoryForCurrentMessageFlow(getSoapFactory());
            taskMessageContext.setWsdlBindingForCurrentMessageFlow(getBinding());
            taskMessageContext.setUep(getUnifiedEndpoint());
            taskMessageContext.setCaller(processId.getLocalPart());
            AxisServiceUtils.invokeService(taskMessageContext, getConfigurationContext());
        } catch (AxisFault axisFault) {
            log.error(axisFault, axisFault);
            throw new FaultException(BPEL4PeopleConstants.B4P_FAULT,
                    "Error occurred while invoking service " + serviceName,
                    axisFault);
        } catch (B4PCoordinationException coordinationFault) {
            throw new FaultException(BPEL4PeopleConstants.B4P_FAULT,
                    "Error occurred while generating Registration Service URL" + serviceName,
                    coordinationFault);
        }
// it seems the WSDLAwareMessage is not required.
//        taskMessageContext.setRequestMessage();

        if (taskMessageContext.getFaultMessageContext() != null ||
                taskMessageContext.getOutMessageContext().isFault()) {
            MessageContext faultContext = taskMessageContext.getFaultMessageContext() != null ?
                    taskMessageContext.getFaultMessageContext() :
                    taskMessageContext.getOutMessageContext();
            log.warn("SOAP Fault: " + faultContext.getEnvelope().toString());
            throw new FaultException(BPEL4PeopleConstants.B4P_FAULT,
                    faultContext.getEnvelope().toString());
        }

        String taskID = SOAPHelper.parseResponseFeedback(
                taskMessageContext.getOutMessageContext().getEnvelope().getBody());

        // HT-Coordination - Persisting taskID against message ID
        // Ignore Notifications, since we are ignore coordination context for notification.
        if (CoordinationConfiguration.getInstance().isHumantaskCoordinationEnabled() && InteractionType.TASK.equals(activityType)) {
            Long instanceID = extensionContext.getProcessId();
            if (CoordinationConfiguration.getInstance().isRegistrationServiceEnabled()) {
                try { // Already coordinated with Registration service.
                    updateCoordinationData(messageID.toString(), Long.toString(instanceID), taskID);
                } catch (Exception e) {
                    log.error("Error occurred while updating humantask coordination data.", e);
                }
            } else {
                // This is an special case. Registration service is disabled. So we are calculating Task engine protocol
                // Handler URL by manually.
                try {
                    messageID = UUID.randomUUID();
                    String protocolHandlerURL = generateTaskProtocolHandlerURL(taskMessageContext);
                    if(log.isDebugEnabled())
                    {
                        log.debug("Generated Protocol Handler URL : " + protocolHandlerURL);
                    }
                    createCoordinationData(messageID.toString(), protocolHandlerURL, Long.toString(instanceID), taskID);
                } catch (Exception e) {
                    log.error("Error occurred while creating humantask coordination data for coordinated task.", e);
                }
            }
        }
        return taskID;
    }

    public String inferCorrelatorId(ExtensionContext extensionContext) throws FaultException {
        PartnerLinkInstance plink;
        plink = extensionContext.getPartnerLinkInstance(partnerLinkName);
        return plink.partnerLink.getName() + "." + callbackOperationName;
    }

    private void updateCoordinationData(final String messageID, final String instanceID, final String taskID) throws Exception {
        ((BPELServerImpl) B4PContentHolder.getInstance().getBpelServer()).getScheduler().execTransaction(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                HTCoordinationDAOConnection daoConnection = B4PContentHolder.getInstance().getCoordinationController().getDaoConnectionFactory().getConnection();
                daoConnection.updateProtocolHandler(messageID, instanceID, taskID);
                return null;
            }
        });
    }

    private HTProtocolHandlerDAO createCoordinationData(final String messageID, final String participantProtocolService,
                                        final String instanceID, final String taskID) throws Exception {
        HTProtocolHandlerDAO htProtocolHandlerDAO = ((BPELServerImpl) B4PContentHolder.getInstance().getBpelServer()).getScheduler().execTransaction(new Callable<HTProtocolHandlerDAO>() {
            @Override
            public HTProtocolHandlerDAO call() throws Exception {
                HTCoordinationDAOConnection daoConnection = B4PContentHolder.getInstance().getCoordinationController().getDaoConnectionFactory().getConnection();
                return daoConnection.createCoordinatedTask(messageID, participantProtocolService,instanceID,taskID);
            }
        });
        return htProtocolHandlerDAO;
    }

    private String generateServiceURLUpToWebContext() throws B4PCoordinationException, FaultException {
        String baseURL = "";
        if (CoordinationConfiguration.getInstance().isClusteredTaskEngines()) {
            baseURL = CoordinationConfiguration.getInstance().getLoadBalancerURL();
        } else {
            ConfigurationContext serverConfigurationContext = getConfigurationContext();

            String scheme = CarbonConstants.HTTPS_TRANSPORT;
            String host;
            try {
                host = NetworkUtils.getLocalHostname();
            } catch (SocketException e) {
                log.error(e.getMessage(), e);
                throw new B4PCoordinationException(e.getLocalizedMessage(), e);
            }

            int port = 9443;
            port = CarbonUtils.getTransportProxyPort(serverConfigurationContext, scheme);
            if (port == -1) {
                port = CarbonUtils.getTransportPort(serverConfigurationContext, scheme);
            }
            baseURL = scheme + "://" + host + ":" + port;
        }

        String webContext = ServerConfiguration.getInstance().getFirstProperty("WebContextRoot");
        if (webContext == null || webContext.equals("/")) {
            webContext = "";
        }

        return baseURL + webContext;
    }

    private String getRegistrationServiceURL() throws B4PCoordinationException, FaultException {
        String tenantDomain = "";
        try {
            tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain(true);
        } catch (Throwable e) {
            tenantDomain = null;
        }
        String registrationServiceURL = generateServiceURLUpToWebContext() + ((tenantDomain != null &&
                !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) ?
                "/" + MultitenantConstants.TENANT_AWARE_URL_PREFIX + "/" + tenantDomain : "") +
                BPEL4PeopleConstants.CARBON_ADMIN_SERVICE_CONTEXT_ROOT + "/"
                + BPEL4PeopleConstants.B4P_REGISTRATION_SERVICE + "/";
        return registrationServiceURL;
    }

    private String generateTaskProtocolHandlerURL(BPELMessageContext taskMessageContext) throws FaultException, B4PCoordinationException {
        String tenantTaskService = taskMessageContext.getUep().getAddress();
        int tenantDelimiterIndex = tenantTaskService.indexOf("/t/");
        String tenantIdentifier = "";
        if (tenantDelimiterIndex != -1) {
            String temp = tenantTaskService.substring(tenantDelimiterIndex + 3);  // 3 = length("/t/")
            int indexOfSlash = temp.indexOf('/');
            tenantIdentifier = "/t/" + ((indexOfSlash != -1) ? temp.substring(0, indexOfSlash) : temp);
        }
        //Else super tenant. -> tenantIdentifier = ""
        String protocolHandlerURL = generateServiceURLUpToWebContext() + tenantIdentifier +
                BPEL4PeopleConstants.CARBON_ADMIN_SERVICE_CONTEXT_ROOT + "/"
                + BPEL4PeopleConstants.HT_ENGINE_COORDINATION_PROTOCOL_HANDLER_SERVICE;
        return protocolHandlerURL;
    }

    /**
     * Manage the child element of PeopleActivity
     * eg -
     * <b4p:attachmentPropagation fromProcess="all|none" toProcess="all|newOnly|none" />
     */
    class AttachmentPropagation {
        private String fromProcess;
        private String toProcess;
        private boolean isInitialized;

        public boolean isInitialized() {
            return isInitialized;
        }

        public String getFromProcess() {
            return fromProcess;
        }

        public String getToProcess() {
            return toProcess;
        }

        /**
         * Default constructor : specifies the default values for the &lt;attachmentPropagation/&gt; child element
         */
        public AttachmentPropagation() throws FaultException {
            init();
        }

        private AttachmentPropagation(Element element) throws FaultException {
            init(element);
        }

        /**
         * Initializes the default values for the &lt;attachmentPropagation/&gt; child element
         */
        private void init() throws FaultException {
            this.fromProcess = extractFromProcessValue(FromProcessSpec.all.toString());    //Default value is : FromProcessSpec.all
            this.toProcess = extractToProcessValue(ToProcessSpec.newOnly.toString());      //Default value is : ToProcessSpec.newOnly

            isInitialized = true;
        }

        private void init(Element element) throws FaultException {
            if (!element.getLocalName().equals(BPEL4PeopleConstants.ATTACHMENT_PROPAGATION_ACTIVITY) ||
                    !element.getNamespaceURI().equals(BPEL4PeopleConstants.B4P_NAMESPACE)) {
                throw new FaultException(BPEL4PeopleConstants.B4P_FAULT,
                        "No " + BPEL4PeopleConstants.ATTACHMENT_PROPAGATION_ACTIVITY + " found");
            }

            this.fromProcess = extractFromProcessValue(element.getAttribute(BPEL4PeopleConstants
                    .ATTACHMENT_PROPAGATION_ACTIVITY_FROM_PROCESS));
            this.toProcess = extractToProcessValue(element.getAttribute(BPEL4PeopleConstants
                    .ATTACHMENT_PROPAGATION_ACTIVITY_TO_PROCESS));

            isInitialized = true;
        }

        private String extractFromProcessValue(String fromProcessValue) throws FaultException {
            if (FromProcessSpec.all.toString().equals(fromProcessValue) ||
                    FromProcessSpec.none.toString().equals(fromProcessValue)) {
                return fromProcessValue;
            } else {
                throw new FaultException(BPEL4PeopleConstants.B4P_FAULT,
                        "Assigned value for " +
                                BPEL4PeopleConstants.ATTACHMENT_PROPAGATION_ACTIVITY_FROM_PROCESS +
                                " attribute is not compliant with the specification.");
            }
        }

        private String extractToProcessValue(String toProcessValue) throws FaultException {
            if (ToProcessSpec.all.toString().equals(toProcessValue) ||
                    ToProcessSpec.none.toString().equals(toProcessValue) ||
                    ToProcessSpec.newOnly.toString().equals(toProcessValue)) {
                return toProcessValue;
            } else {
                throw new FaultException(BPEL4PeopleConstants.B4P_FAULT,
                        "Assigned value for " +
                                BPEL4PeopleConstants.ATTACHMENT_PROPAGATION_ACTIVITY_TO_PROCESS +
                                " attribute is not compliant with the specification.");
            }
        }
    }

    enum FromProcessSpec {
        all,
        none
    }

    enum ToProcessSpec {
        all,
        newOnly,
        none
    }
}

