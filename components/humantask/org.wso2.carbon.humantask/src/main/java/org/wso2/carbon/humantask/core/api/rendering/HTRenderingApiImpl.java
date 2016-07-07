/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.humantask.core.api.rendering;

import com.predic8.wsdl.Definitions;
import com.predic8.wsdl.WSDLParser;
import com.predic8.wstool.creator.RequestTemplateCreator;
import com.predic8.wstool.creator.SOARequestCreator;
import groovy.xml.MarkupBuilder;
import org.apache.axis2.databinding.types.NCName;
import org.apache.axis2.databinding.types.URI;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.humantask.client.api.IllegalAccessFault;
import org.wso2.carbon.humantask.client.api.IllegalArgumentFault;
import org.wso2.carbon.humantask.client.api.IllegalOperationFault;
import org.wso2.carbon.humantask.client.api.IllegalStateFault;
import org.wso2.carbon.humantask.core.HumanTaskConstants;
import org.wso2.carbon.humantask.core.api.client.TaskOperationsImpl;
import org.wso2.carbon.humantask.core.dao.HumanTaskDAOConnection;
import org.wso2.carbon.humantask.core.dao.TaskDAO;
import org.wso2.carbon.humantask.core.engine.HumanTaskEngine;
import org.wso2.carbon.humantask.core.engine.runtime.api.HumanTaskIllegalAccessException;
import org.wso2.carbon.humantask.core.internal.HumanTaskServiceComponent;
import org.wso2.carbon.humantask.core.store.HumanTaskBaseConfiguration;
import org.wso2.carbon.humantask.core.store.TaskConfiguration;
import org.wso2.carbon.humantask.core.utils.DOMUtils;
import org.wso2.carbon.humantask.rendering.api.CompleteTaskFaultException;
import org.wso2.carbon.humantask.rendering.api.GetRenderingsFaultException;
import org.wso2.carbon.humantask.rendering.api.GetRenderingsResponse;
import org.wso2.carbon.humantask.rendering.api.HumanTaskRenderingAPISkeletonInterface;
import org.wso2.carbon.humantask.rendering.api.InputElementType;
import org.wso2.carbon.humantask.rendering.api.InputType;
import org.wso2.carbon.humantask.rendering.api.OutputElementType;
import org.wso2.carbon.humantask.rendering.api.OutputType;
import org.wso2.carbon.humantask.rendering.api.SetOutputValuesType;
import org.wso2.carbon.humantask.rendering.api.SetOutputvalueType;
import org.wso2.carbon.humantask.rendering.api.SetTaskOutputFaultException;
import org.wso2.carbon.humantask.rendering.api.SetTaskOutputResponse;
import org.wso2.carbon.humantask.rendering.api.ValueType;
import org.wso2.carbon.humantask.rendering.api.Value_tType;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.concurrent.Callable;
import javax.wsdl.Port;
import javax.wsdl.Service;
import javax.xml.namespace.QName;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

/**
 * The implementation of the WS Human Task Rendering API Operations.
 */
public class HTRenderingApiImpl implements HumanTaskRenderingAPISkeletonInterface {

    private static Log log = LogFactory.getLog(HTRenderingApiImpl.class);
    private static String htRenderingNS = "http://wso2.org/ht/schema/renderings/";
    private static TaskOperationsImpl taskOps = new TaskOperationsImpl();

    //HashMap to store output response template against the task Name (QName Task name : <TaskName>-<version>)
    private static HashMap<QName, Element> outputTemplates = new HashMap<QName, Element>();


    public GetRenderingsResponse getRenderings(URI taskIdentifier) throws
                                                                   GetRenderingsFaultException {

        GetRenderingsResponse response = new GetRenderingsResponse();

        //Retrieve input renderings
        try {

            InputType inputRenderings = getRenderingInputElements(taskIdentifier);
            if (inputRenderings != null) {
                response.setInput(inputRenderings);
            }

        } catch (IllegalArgumentFault illegalArgumentFault) {
            throw new GetRenderingsFaultException(illegalArgumentFault);
        } catch (IOException e) {
            log.error("Error occurred while retrieving input renderings", e);
            throw new GetRenderingsFaultException("Internal Error Occurred");
        } catch (SAXException e) {
            log.error("Error occured while retrieving input renderings", e);
            throw new GetRenderingsFaultException("Error occured while retrieving input renderings");
        } catch (XPathExpressionException e) {
            log.error("Error occurred while evaluating xpath expression over user input message", e);
            throw new GetRenderingsFaultException("Error occurred while evaluating xpath expression over user input message");
        } catch (Exception e) {
            log.error("Internal Error Occurred", e);
            throw new GetRenderingsFaultException(e.getMessage());
        }

        //retrieve output renderings
        try {

            OutputType outputRenderings = getRenderingOutputElements(taskIdentifier);
            if (outputRenderings != null) {
                response.setOutput(outputRenderings);
            }

        } catch (IllegalArgumentFault illegalArgumentFault) {
            throw new GetRenderingsFaultException(illegalArgumentFault);
        } catch (IOException e) {
            log.error("Error occurred while retrieving output renderings", e);
            throw new GetRenderingsFaultException("Internal Error Occurred");
        } catch (SAXException e) {
            log.error("Error occurred while retrieving output renderings", e);
            throw new GetRenderingsFaultException("Error occurred while retrieving output renderings");
        } catch (IllegalAccessFault illegalAccessFault) {
            log.error("Error occurred while retrieving output renderings", illegalAccessFault);
            throw new GetRenderingsFaultException("illegal access attempt");
        } catch (IllegalStateFault illegalStateFault) {
            log.error("Error occurred while retrieving output renderings", illegalStateFault);
            throw new GetRenderingsFaultException("illegal state");
        } catch (IllegalOperationFault illegalOperationFault) {
            log.error("Error occurred while retrieving output renderings", illegalOperationFault);
            throw new GetRenderingsFaultException("illegal operation");
        } catch (XPathExpressionException e) {
            log.error("XPath evaluation failed", e);
            throw new GetRenderingsFaultException("Internal Error Occurred");
        }

        return response;
    }

    public SetTaskOutputResponse setTaskOutput(URI taskIdentifier, SetOutputValuesType values)
            throws SetTaskOutputFaultException {

        //Retrieve task information
        TaskDAO htTaskDAO;
        try {
            htTaskDAO = getTaskDAO(taskIdentifier);
        } catch (Exception e) {
            log.error("Error occurred while retrieving task data", e);
            throw new SetTaskOutputFaultException(e);
        }

        QName taskName = QName.valueOf(htTaskDAO.getName());

        //Check hash map for output message template
        Element outputMsgTemplate = outputTemplates.get(taskName);

        if (outputMsgTemplate == null) {
            //Output message template not available

            try {
                //generate output message template
                int tenantID = CarbonContext.getThreadLocalCarbonContext().getTenantId();
                HumanTaskBaseConfiguration htConf = HumanTaskServiceComponent.getHumanTaskServer().getTaskStoreManager().
                        getHumanTaskStore(tenantID).getTaskConfiguration(taskName);
                TaskConfiguration taskConf = (TaskConfiguration) htConf;

                //retrieve response binding
                Service callbackService = (Service) taskConf.getResponseWSDL().getServices().get(taskConf.getCallbackServiceName());
                Port callbackPort = (Port) callbackService.getPorts().get(taskConf.getCallbackPortName());
                String callbackBinding = callbackPort.getBinding().getQName().getLocalPart();

                outputMsgTemplate = createSoapTemplate(taskConf.getResponseWSDL().getDocumentBaseURI(),
                                                       taskConf.getResponsePortType().getLocalPart(),
                                                       taskConf.getResponseOperation(), callbackBinding);
            } catch (Exception e) {
                log.error("Error occurred while output message template generation", e);
                throw new SetTaskOutputFaultException("Unable to generate output message", e);
            }

            //add to the template HashMap
            if (outputMsgTemplate != null) {
                outputTemplates.put(taskName, outputMsgTemplate);

            } else {
                log.error("Unable to create output message template");
                throw new SetTaskOutputFaultException("Unable to generate output message");
            }
        }

        //update template with new values
        try {
            //TODO improve this section with caching
            QName renderingType = new QName(htRenderingNS, "output", "wso2");
            String outputRenderings = (String) taskOps.getRendering(taskIdentifier, renderingType);
            SetOutputvalueType[] valueSet = values.getValue();

            if (outputRenderings != null && valueSet.length > 0) {
                Element outputRenderingsElement = DOMUtils.stringToDOM(outputRenderings);
                //update elements in the template to create output xml
                for (int i = 0; i < valueSet.length; i++) {
                    Element outElement = getOutputElementById(valueSet[i].getId(), outputRenderingsElement);
                    outputMsgTemplate = updateXmlByXpath(outputMsgTemplate, outElement.
                                                                 getElementsByTagNameNS(htRenderingNS, "xpath").item(0).getTextContent(),
                                                         valueSet[i].getString(), outputRenderingsElement.getOwnerDocument());
                }
            } else {
                log.error("Retrieving output renderings failed");
                throw new SetTaskOutputFaultException("Retrieving output renderings failed");
            }

            //TODO what is this NCName?
            taskOps.setOutput(taskIdentifier, new NCName("message"), DOMUtils.domToString(outputMsgTemplate));

        } catch (IllegalArgumentFault illegalArgumentFault) {
            //Error occurred while retrieving HT renderings and set output message
            throw new SetTaskOutputFaultException(illegalArgumentFault);
        } catch (SAXException e) {
            log.error("Error occured while parsing output renderings", e);
            throw new SetTaskOutputFaultException("Response message generation failed");
        } catch (XPathExpressionException e) {
            //Error occured while updating elements in the template to create output xml
            log.error("XPath evaluation failed", e);
            throw new SetTaskOutputFaultException("Internal Error Occurred");
        } catch (Exception e) {
            //Error occurred while updating template with new values
            log.error("Error occurred while updating template with new values", e);
            throw new SetTaskOutputFaultException("Internal Error Occurred");
        }

        SetTaskOutputResponse response = new SetTaskOutputResponse();
        response.setSuccess(true);

        return response;
    }

    public boolean completeTask(URI taskIdentifier0, SetOutputValuesType values1)
            throws CompleteTaskFaultException {

        try {
            setTaskOutput(taskIdentifier0, values1);
        } catch (SetTaskOutputFaultException e) {
            log.error("Error occured while setting task output message", e);
            throw new CompleteTaskFaultException("Error occured while setting task output message", e);
        }

        try {

            String savedOutputMsg = (String) taskOps.getOutput(taskIdentifier0, null);
            if (savedOutputMsg != null && savedOutputMsg.length() > 0) {
                taskOps.complete(taskIdentifier0, savedOutputMsg);
                return true;
            } else {
                return false;
            }

        } catch (Exception e) {
            log.error("Error occured while completing the task", e);
            throw new CompleteTaskFaultException("Error occured while completing the task", e);
        }

    }


    /**
     * @param taskIdentifier : interested task identifier
     * @return set of input renderings wrapped within InputType
     * @throws IllegalArgumentFault : error occured while retrieving renderings from task definition
     * @throws IOException          If an error occurred while reading from the input source
     * @throws SAXException         If the content in the input source is invalid
     */
    private InputType getRenderingInputElements(URI taskIdentifier)
            throws IllegalArgumentFault, IOException, SAXException, IllegalOperationFault,
                   IllegalAccessFault, IllegalStateFault, XPathExpressionException {

        //TODO Chaching : check cache against task id for input renderings
        QName renderingType = new QName(htRenderingNS, "input", "wso2");
        String inputRenderings = (String) taskOps.getRendering(taskIdentifier, renderingType);

        //Create input element
        InputType renderingInputs = null;

        //check availability of input renderings
        if (inputRenderings != null && inputRenderings.length() > 0) {

            //parse input renderings
            Element inputRenderingsElement = DOMUtils.stringToDOM(inputRenderings);

            //retrieve input elements
            NodeList inputElementList = inputRenderingsElement.getElementsByTagNameNS(htRenderingNS, "element");

            Element taskInputMsgElement = DOMUtils.stringToDOM((String) taskOps.getInput(taskIdentifier, null));
            if (inputElementList != null && inputElementList.getLength() > 0) {

                int inputElementNum = inputElementList.getLength();//hold number of input element
                InputElementType[] inputElements = new InputElementType[inputElementNum];

                for (int i = 0; i < inputElementNum; i++) {
                    Element tempElement = (Element) inputElementList.item(i);
                    String label = tempElement.getElementsByTagNameNS(htRenderingNS, "label").item(0).getTextContent();
                    String value = tempElement.getElementsByTagNameNS(htRenderingNS, "value").item(0).getTextContent();

                    //check if the value is xpath or not
                    //if the value starts with '/' then considered as an xpath and evaluate over received Input Message
                    if (value.startsWith("/") && taskInputMsgElement != null) {
                        //value represents xpath. evaluate against the input message
                        String xpathValue = evaluateXPath(value, taskInputMsgElement, inputRenderingsElement.getOwnerDocument());
                        if (xpathValue != null) {
                            value = xpathValue;
                        }
                    }
                    inputElements[i] = new InputElementType();
                    inputElements[i].setLabel(label);
                    inputElements[i].setValue(value);

                }

                renderingInputs = new InputType();
                renderingInputs.setElement(inputElements);
                //TODO cache renderingInputs against task instance id
            }
        }
        //NOTE :
        //HT without input renderings is valid scennario in such scennario return zero length input renderings will be returned

        return renderingInputs;
    }

    /**
     * Function to retrieve output rendering elements
     *
     * @param taskIdentifier interested task identifier
     * @return set of output renderings wrapped within OutputType
     * @throws IllegalArgumentFault        error occured while retrieving renderings from task definition
     * @throws IOException                 If an error occurred while reading from the input source
     * @throws SAXException                If the xml content in the input source is invalid
     * @throws IllegalOperationFault
     * @throws IllegalAccessFault
     * @throws IllegalStateFault
     * @throws XPathExpressionException    If error occurred while xpath evaluation
     * @throws GetRenderingsFaultException If unable to find unique id for the wso2:output rendering element
     */
    private OutputType getRenderingOutputElements(URI taskIdentifier)
            throws IllegalArgumentFault, IOException, SAXException, IllegalOperationFault,
                   IllegalAccessFault, IllegalStateFault, XPathExpressionException,
                   GetRenderingsFaultException {

        QName renderingType = new QName(htRenderingNS, "output", "wso2");
        String outputRenderings = (String) taskOps.getRendering(taskIdentifier, renderingType);

        //create output element
        OutputType renderingOutputs = null;

        //HT without output renderings is valid scenario
        //check availability of output renderings
        if (outputRenderings != null && outputRenderings.length() > 0) {
            //parse output renderings
            Element outputRenderingsElement = DOMUtils.stringToDOM(outputRenderings);
            //retrieve output rendering elements
            NodeList outputElementList = outputRenderingsElement.getElementsByTagNameNS(htRenderingNS, "element");

            if (outputElementList != null && outputElementList.getLength() > 0) {

                int outputElementNum = outputElementList.getLength();
                OutputElementType[] outputElements = new OutputElementType[outputElementNum];

                //TODO get task output message from the cache
                // (if not in the cache) retrieve saved output using HumanTaskClientAPI
                String savedOutputMsg = (String) taskOps.getOutput(taskIdentifier, null);

                //Element to hold parsed saved output message
                Element savedOutputElement = null;
                if (savedOutputMsg != null && savedOutputMsg.length() > 0) {
                    savedOutputElement = DOMUtils.stringToDOM(savedOutputMsg);
                }

                for (int i = 0; i < outputElementNum; i++) {

                    Element tempElement = (Element) outputElementList.item(i);

                    if (tempElement.hasAttribute("id")) {

                        //Retrieve element data
                        String elementID = tempElement.getAttribute("id");
                        String label = tempElement.getElementsByTagNameNS(htRenderingNS, "label").item(0).getTextContent();
                        String xpath = tempElement.getElementsByTagNameNS(htRenderingNS, "xpath").item(0).getTextContent();
                        String defaultValue = tempElement.getElementsByTagNameNS(htRenderingNS, "default").item(0).getTextContent();

                        //set the readOnly attribute if Exists, default false
                        String readOnly = "false";
                        if (tempElement.hasAttribute("readOnly")) {
                            readOnly = tempElement.getAttribute("readOnly");
                        }

                        //set element data in the response message
                        outputElements[i] = new OutputElementType();
                        //set ID
                        outputElements[i].setId(elementID);
                        //set label
                        outputElements[i].setLabel(label);
                        //set xpath
                        outputElements[i].setXpath(xpath);
                        //set value
                        Element valueElement = (Element) tempElement.getElementsByTagNameNS(htRenderingNS, "value").item(0);
                        outputElements[i].setValue(createOutRenderElementValue(valueElement));
                        if (readOnly.equals("true")) {
                            outputElements[i].setReadOnly(true);
                        } else {
                            outputElements[i].setReadOnly(false);
                        }

                        if (savedOutputElement != null) {
                            //resolve default value

                            String savedOutMessageValue = evaluateXPath(xpath, savedOutputElement, outputRenderingsElement.getOwnerDocument());
                            if (savedOutMessageValue == null) {
                                outputElements[i].set_default(defaultValue);
                            } else {
                                outputElements[i].set_default(savedOutMessageValue);
                            }

                        } else {
                            //add default value specified in the HT rendering definition
                            outputElements[i].set_default(defaultValue);
                        }

                    } else {
                        //no unique id for the element
                        log.error("Unable to find unique id for the wso2:output rendering element");
                        throw new GetRenderingsFaultException("Unable to find unique id for the wso2:output rendering element");
                    }
                }
                renderingOutputs = new OutputType();
                renderingOutputs.setElement(outputElements);
            }
        }

        //NOTE :
        //HT without output renderings is valid scennario in such scennario return zero length output renderings will be returned

        return renderingOutputs;
    }


    /**
     * Function to create output rendering value type base on the value of type attribute of the output rendering element
     *
     * @param valueElement DOMElement representing output rendering element
     * @return ValueType object updated with output rendering element type
     */
    private ValueType createOutRenderElementValue(Element valueElement) {

        ValueType outRenderElementValue = new ValueType();

        String value = valueElement.getTextContent();
        String valueTypeAttr = valueElement.getAttribute("type");

        outRenderElementValue.setString(value);

        if (valueTypeAttr.equals(Value_tType._value1)) {
            //String value
            outRenderElementValue.setType(Value_tType.value1);

        } else if (valueTypeAttr.equals(Value_tType._value2)) {

            //Int value
            outRenderElementValue.setType(Value_tType.value2);

        } else if (valueTypeAttr.equals(Value_tType._value3)) {

            //double value
            outRenderElementValue.setType(Value_tType.value3);

        } else if (valueTypeAttr.equals(Value_tType._value4)) {

            //float value
            outRenderElementValue.setType(Value_tType.value4);

        } else if (valueTypeAttr.equals(Value_tType._value5)) {

            //boolean value
            outRenderElementValue.setType(Value_tType.value5);

        } else if (valueTypeAttr.equals(Value_tType._value6)) {

            //list value
            outRenderElementValue.setType(Value_tType.value6);

        } else {
            //TODO Assume String value as default type
            outRenderElementValue.setType(Value_tType.value1);
        }

        return outRenderElementValue;
    }

    /**
     * Function to retrieve output rendering element with matching id
     *
     * @param id output rendering element id
     * @param outputRendering DOMElement representing output renderings in the HT definition
     * @return DOM Element if matching element found, otherwise return null
     */
    private static Element getOutputElementById(String id, Element outputRendering) {

        NodeList nodes = outputRendering.getElementsByTagNameNS(htRenderingNS, "element");

        for (int i = 0; i < nodes.getLength(); i++) {
            Element tempElement = (Element) nodes.item(i);
            if (tempElement.getAttribute("id").equals(id)) {
                return tempElement;
            }
        }

        return null;
    }

    /**
     * Function to create response message template
     *
     * @param SrcWsdl   source wsld : wsdl file path or url
     * @param portType  callback port type
     * @param operation callback operation name
     * @param binding   callback binding
     * @return DOM element of response message template
     * @throws IOException  If error occurred while parsing string xml to Dom element
     * @throws SAXException If error occurred while parsing string xml to Dom element
     */
    private static Element createSoapTemplate(String SrcWsdl, String portType, String operation,
                                              String binding)
            throws IOException, SAXException {
        WSDLParser parser = new WSDLParser();

        //BPS-677
        int fileLocationPrefixIndex = SrcWsdl.indexOf(HumanTaskConstants.FILE_LOCATION_FILE_PREFIX);
        if( SrcWsdl.indexOf(HumanTaskConstants.FILE_LOCATION_FILE_PREFIX) != -1 ){
            SrcWsdl = SrcWsdl.substring( fileLocationPrefixIndex + HumanTaskConstants.FILE_LOCATION_FILE_PREFIX
                    .length());
        }

        Definitions wsdl = parser.parse(SrcWsdl);

        StringWriter writer = new StringWriter();

        //SOAPRequestCreator constructor: SOARequestCreator(Definitions, Creator, MarkupBuilder)
        SOARequestCreator creator = new SOARequestCreator(wsdl, new RequestTemplateCreator(), new MarkupBuilder(writer));

        //creator.createRequest(PortType name, Operation name, Binding name);
        creator.createRequest(portType, operation, binding);

        Element outGenMessageDom = DOMUtils.stringToDOM(writer.toString());

        Element outMsgElement = null;
        NodeList nodes = outGenMessageDom.getElementsByTagNameNS(outGenMessageDom.getNamespaceURI(), "Body").item(0).getChildNodes();

        if (nodes != null) {
            for (int i = 0; i < nodes.getLength(); i++) {
                if (nodes.item(i).getNodeType() == Node.ELEMENT_NODE) {
                    outMsgElement = (Element) nodes.item(i);
                    break;
                }
            }
        }

        if (outMsgElement != null) {
            //convert element to string and back to element to remove Owner Document
            return DOMUtils.stringToDOM(DOMUtils.domToString(outMsgElement));
        }

        return null;
    }

    /**
     * Function to evaluate xpath and retrieve String of the result
     *
     * @param xPathExpression
     * @param xPathExpression
     * @param nsReferenceDoc
     * @return result of the XPath expression if evaluation success, Otherwise return null
     * @throws IOException
     * @throws SAXException
     * @throws IllegalAccessFault
     * @throws IllegalArgumentFault
     * @throws IllegalStateFault
     * @throws IllegalOperationFault
     * @throws XPathExpressionException
     */
    private String evaluateXPath(String xPathExpression,
                                           Element targetXmlElement, Document nsReferenceDoc)
            throws IOException, SAXException, IllegalAccessFault, IllegalArgumentFault,
                   IllegalStateFault, IllegalOperationFault, XPathExpressionException {

        if (xPathExpression.length() > 0) {
            //Evaluate XPath
            XPath xPath = XPathFactory.newInstance().newXPath();
            NamespaceResolver nsResolver = new NamespaceResolver(nsReferenceDoc);
            xPath.setNamespaceContext(nsResolver);
            Node result = (Node) xPath.evaluate(xPathExpression, targetXmlElement, XPathConstants.NODE);

            if (result != null && result.getFirstChild().hasChildNodes() == false) {
                return result.getTextContent();
            }
        }

        return null;
    }

    /**
     * Function to update xml DOM element according to the xpath expression with given string value
     *
     * @param xmlElement DOM element representing the xml
     * @param xPathExpression xpath expression
     * @param value string that need to replace in the xml
     * @param referanceDoc reference Document which represents original ht rendering element, which used to resolve
     *                     namespaces mentioned in the xpath expression
     * @return
     * @throws XPathExpressionException
     */
    private Element updateXmlByXpath(Element xmlElement, String xPathExpression, String value,
                                     Document referanceDoc)
            throws XPathExpressionException {
        if (xPathExpression.length() > 0) {
            //Evaluate XPath
            XPath xPath = XPathFactory.newInstance().newXPath();
            NamespaceResolver nsResolver = new NamespaceResolver(referanceDoc);
            xPath.setNamespaceContext(nsResolver);

            Node result = (Node) xPath.evaluate(xPathExpression, xmlElement, XPathConstants.NODE);
            result.setTextContent(value);
        }

        return xmlElement;
    }

    /**
     * Function to retrieve task DAO
     *
     * @param taskIdURI task ID
     * @return task DAO
     * @throws Exception
     * @throws IllegalArgumentException
     */
    private TaskDAO getTaskDAO(URI taskIdURI) throws IllegalArgumentException, HumanTaskIllegalAccessException, Exception {

        final Long taskId = validateTaskId(taskIdURI);
        TaskDAO task = HumanTaskServiceComponent.getHumanTaskServer().getTaskEngine().getScheduler().
                execTransaction(new Callable<TaskDAO>() {
                    public TaskDAO call() throws Exception {
                        HumanTaskEngine engine = HumanTaskServiceComponent.getHumanTaskServer().getTaskEngine();
                        HumanTaskDAOConnection daoConn = engine.getDaoConnectionFactory().getConnection();
                        TaskDAO task = daoConn.getTask(taskId);
                        validateTaskTenant(task);
                        return task;
                    }
                });
        return task;

    }



    /**
     * Validates the provided task ID URI and returns the id as Long
     *
     * @param taskId task ID
     * @return returns validated task id converted to Long type
     * @throws IllegalArgumentException If taskId is null or empty or not a number
     */
    private Long validateTaskId(URI taskId) throws IllegalArgumentException {
        if (taskId == null || StringUtils.isEmpty(taskId.toString())) {
            throw new IllegalArgumentException("The task id cannot be null or empty");
        }

        try {
            return Long.valueOf(taskId.toString());
        } catch (NumberFormatException e) {
            log.error("The task id must be a number", e);
            throw new IllegalArgumentException("The task id must be a number", e);
        }
    }


    /**
     * Function to validate tenant of the task with the user tenant
     *
     * @param task task DAO
     */
    private void validateTaskTenant(TaskDAO task) throws HumanTaskIllegalAccessException{
        final int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        if (tenantId != task.getTenantId()) {
            log.error("Access Denied : Users in " + task.getTenantId() + " can't perform other tenant's task");
            throw new HumanTaskIllegalAccessException("Access Denied. You are not authorized to perform this task");
        }
    }

}
