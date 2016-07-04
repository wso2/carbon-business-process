/**
 * Copyright (c) 2015 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.bpmn.extensions.soap;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.delegate.JavaDelegate;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axiom.soap.SOAP11Constants;
import org.apache.axiom.soap.SOAP12Constants;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.commons.httpclient.Header;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.stream.XMLStreamException;
import java.util.ArrayList;
import java.util.List;

/**
 * Provides SOAP service invocation support within BPMN processes.
 * Following fields is required when using the SOAP task : (Both string values and expressions can be used)
 * serviceURL : Endpoint url of the partner service
 * payload :  Request payload which is attached to the SOAP body when creating the SOAP request
 * outputVariable : Name of the variable to save response
 * soapVersion :  Soap version to be used when creating the SOAP request i.e. soap11 or soap12
 * headers : SOAP header block which is attached to the SOAP header when creating the SOAP request
 * transportHeaders : additional transport header values in the format "headerName1:headerValue1,headerName2:headerValue2"
 * httpConnection : Control options for the current connection. Connection: keep-alive (set as the default value)
 * soapAction : Indicate the intent of the SOAP HTTP request
 * httpTransferEncoding : The form of encoding used to safely transfer the entity to the user.
 * Transfer-Encoding: chunked (set as the default value)
 * Example :
 * <serviceTask id="servicetask3" name="SOAP Task"
 *  activiti:class="org.wso2.carbon.bpmn.extensions.soap.SOAPTask"
 *  activiti:extensionId="org.wso2.bps.tooling.bpmn.extensions.soapTask.SOAPTask">
 * <extensionElements>
 * <activiti:field name="serviceURL">
 * <activiti:expression>${serviceURL eg: http://localhost:9763/services/HelloService }</activiti:expression>
 * </activiti:field>
 * <activiti:field name="payload">
 * <activiti:expression>${input-payload}</activiti:expression>
 * </activiti:field>
 * <activiti:field name="soapVersion">
 * <activiti:string>soap11</activiti:string>
 * </activiti:field>
 * <activiti:field name="httpTransferEncoding">
 * <activiti:expression>${httpTransferEncoding}</activiti:expression>
 * </activiti:field>
 * <activiti:field name="outputVariable">
 * <activiti:string>output</activiti:string>
 * </activiti:field>
 * <activiti:field name="headers">
 * <activiti:string>&lt;ns1:hello xmlns:ns1='http://ode/bpel/unit-test.wsdl'&gt; &lt;TestPart&gt;HEADER11&lt;/TestPart&gt;&lt;/ns1:hello&gt;</activiti:string>
 * </activiti:field>
 * <activiti:field name="httpConnection">
 * <activiti:string>keep-alive</activiti:string>
 * </activiti:field>
 * <activiti:field name="transportHeaders">
 * <activiti:string>Pragma: no-cache,Cache-Control: no-cache</activiti:string>
 * </activiti:field>
 * <activiti:field name="soapAction">
 * <activiti:string>urn:sayHello</activiti:string>
 * </activiti:field>
 * </extensionElements>
 * </serviceTask>
 */
public class SOAPTask implements JavaDelegate {

    private static final Log log = LogFactory.getLog(SOAPTask.class);

    /**
     * Soap version numbers
     */
    private static final String SOAP12_VERSION = "soap12";
    private static final String SOAP11_VERSION = "soap11";
    private static final String SOAP_INVOKE_ERROR_CODE = "SoapClientInvokeError";

    private Expression serviceURL;
    private Expression payload;
    private Expression headers;
    private Expression soapVersion;
    private Expression httpConnection;
    private Expression httpTransferEncoding;
    private Expression outputVariable;
    private Expression transportHeaders;
    private Expression soapAction;

    @Override
    public void execute(DelegateExecution execution) {


        String endpointURL = null;
        String payloadRequest = null;
        String version = null;
        String headerStr = null;
        String connection = null;
        String transferEncoding = null;
        String transportHeaderList[] = null;
        String action = "";
        String soapVersionURI = SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI;
        List<Header> headerList = new ArrayList<Header>();

        try {
            if (serviceURL != null) {
                endpointURL = serviceURL.getValue(execution).toString();
            } else {
                String urlNotFoundErrorMsg = "Service URL is not provided. serviceURL must be provided.";
                throw new SOAPException(SOAP_INVOKE_ERROR_CODE, urlNotFoundErrorMsg);
            }
            if (payload != null) {
                payloadRequest = payload.getValue(execution).toString();
            } else {
                String payloadNotFoundErrorMsg = "Payload request is not provided. Payload must be provided.";
                throw new SOAPException(SOAP_INVOKE_ERROR_CODE, payloadNotFoundErrorMsg);
            }
            if (soapVersion != null) {
                version = soapVersion.getValue(execution).toString();
                if (version.equalsIgnoreCase(SOAP12_VERSION)) {
                    soapVersionURI = SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI;
                } else if (version.equalsIgnoreCase(SOAP11_VERSION)) {
                    soapVersionURI = SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI;
                } else {
                    String invalidVersionErrorMsg = "Invalid soap version string specified";
                    throw new SOAPException(SOAP_INVOKE_ERROR_CODE, invalidVersionErrorMsg);
                }
            }
            //Adding the connection
            Header connectionHeader = new Header();
            if (httpConnection != null) {
                connection = httpConnection.getValue(execution).toString();
                if(connection != null && !connection.trim().equals("Keep-Alive")) {
                    log.debug("Setting Keep-Alive header ");
                    connectionHeader.setName("Connection");
                    connectionHeader.setValue(connection);
                    headerList.add(connectionHeader);

                }
            }

            //Adding the additional transport headers
            if (transportHeaders != null) {
                String headerContent = transportHeaders.getValue(execution).toString();
                if (headerContent != null) {
                    transportHeaderList = headerContent.split(",");
                    for (String transportHeader : transportHeaderList) {
                        String pair[] = transportHeader.split(":");
                        Header additionalHeader = new Header();

                        if (pair.length == 1) {
                            additionalHeader.setName(pair[0]);
                            additionalHeader.setValue("");
                            if(log.isDebugEnabled()) {
                                log.debug("Adding transport headers " + pair[0]);
                            }
                        } else {
                            additionalHeader.setName(pair[0]);
                            additionalHeader.setValue(pair[1]);
                            if(log.isDebugEnabled()) {
                            }
                        }
                        headerList.add(additionalHeader);
                    }
                }

            }

            //Adding the soap action
            if (soapAction != null) {
                action = soapAction.getValue(execution).toString();
                if(log.isDebugEnabled()) {
                    log.debug("Setting soap action " + soapAction);
                }
            }
            //Converting the payload to an OMElement
            OMElement payLoad = AXIOMUtil.stringToOM(payloadRequest);
            //Creating the Service client
            ServiceClient sender = new ServiceClient();
            OMElement response = null;
            //Creating options to set the headers
            Options options = new Options();
            options.setTo(new EndpointReference(endpointURL));
            options.setAction(action);
            options.setSoapVersionURI(soapVersionURI);

            options.setProperty(org.apache.axis2.transport.http.HTTPConstants.HTTP_HEADERS, headerList);

            //Adding the soap header block to the SOAP Header block when creating the SOAP Envelope
            if (headers != null) {
                String headerContent = headers.getValue(execution).toString();
                OMElement headerElement = AXIOMUtil.stringToOM(headerContent);
                sender.addHeader(headerElement);
            }
            //Adding the transfer encoding
            if (httpTransferEncoding != null) {
                transferEncoding = httpTransferEncoding.getValue(execution).toString();
                if (!transferEncoding.equalsIgnoreCase("chunked")) {
                    options.setProperty(org.apache.axis2.transport.http.HTTPConstants.CHUNKED, Boolean.FALSE);
                }
            }
            sender.setOptions(options);
            //Invoking the endpoint
            response = sender.sendReceive(payLoad);
            //Getting the response as a string
            String responseStr = response.toStringWithConsume();
            if (outputVariable != null) {
                String outVarName = outputVariable.getValue(execution).toString();
                execution.setVariable(outVarName, responseStr);
            } else {
                String outputNotFoundErrorMsg = "Output variable is not provided. " +
                        "outputVariable must be provided to save " +
                        "the response.";
                throw new SOAPException(SOAP_INVOKE_ERROR_CODE, outputNotFoundErrorMsg);
            }
        } catch (AxisFault axisFault) {
            log.error("Axis2 Fault" , axisFault);
            throw new SOAPException(SOAP_INVOKE_ERROR_CODE ,"Exception while getting response :" + axisFault.getMessage());
        } catch (XMLStreamException e) {
            log.error("XML Stream Exception" , e);
            throw new SOAPException(SOAP_INVOKE_ERROR_CODE, "XMLStreamException  :" + e.getMessage());
        }
    }
}