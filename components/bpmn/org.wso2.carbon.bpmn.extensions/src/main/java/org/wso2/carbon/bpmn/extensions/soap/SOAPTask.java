/**
 *  Copyright (c) 2015 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.wso2.carbon.bpmn.extensions.soap;

import org.activiti.engine.delegate.BpmnError;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.delegate.JavaDelegate;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.commons.httpclient.Header;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bpmn.extensions.internal.ServiceComponent;
import org.wso2.carbon.bpmn.extensions.soap.constants.Constants;
import org.wso2.carbon.bpmn.extensions.soap.constants.SOAP11Constants;
import org.wso2.carbon.bpmn.extensions.soap.constants.SOAP12Constants;
import javax.xml.stream.XMLStreamException;
import java.util.ArrayList;
import java.util.List;

/**
 * Provides SOAP service invocation support within BPMN processes.
 * Following fields is required when using the SOAP task : (Both string values and expressions can be used)
     serviceURL : Endpoint url of the partner service
     payload :  Request payload which is attached to the SOAP body when creating the SOAP request
     outputVariable : Name of the variable to save response
     soapVersion :  Soap version to be used when creating the SOAP request i.e. soap11 or soap12
     headers : SOAP header block which is attached to the SOAP header when creating the SOAP request
     transportHeaders : additional transport header values in the format "headerName1:headerVal ue1,headerName2:headerValue2"
     httpConnection : Control options for the current connection. Connection: keep-alive (set as the default value)
     soapAction : Indicate the intent of the SOAP HTTP request
     httpTransferEncoding : The form of encoding used to safely transfer the entity to the user. Transfer-Encoding: chunked (set as the default value)
 *  Example :
     <serviceTask id="servicetask3" name="SOAP Task" activiti:class="org.wso2.carbon.bpmn.extensions.soap.SOAPTask" activiti:extensionId="org.wso2.bps.tooling.bpmn.extensions.soapTask.SOAPTask">
     <extensionElements>
     <activiti:field name="serviceURL">
     <activiti:expression>${serviceURL eg: http://10.100.4.192:9764/services/HelloService }</activiti:expression>
     </activiti:field>
     <activiti:field name="payload">
     <activiti:expression>${input-payload}</activiti:expression>
     </activiti:field>
     <activiti:field name="soapVersion">
     <activiti:string>soap11</activiti:string>
     </activiti:field>
     <activiti:field name="httpTransferEncoding">
     <activiti:expression>${httpTransferEncoding}</activiti:expression>
     </activiti:field>
     <activiti:field name="outputVariable">
     <activiti:string>output</activiti:string>
     </activiti:field>
     <activiti:field name="headers">
     <activiti:string>&lt;ns1:hello xmlns:ns1='http://ode/bpel/unit-test.wsdl'&gt; &lt;TestPart&gt;HEADER11&lt;/TestPart&gt;&lt;/ns1:hello&gt;</activiti:string>
     </activiti:field>
     <activiti:field name="httpConnection">
     <activiti:string>keep-alive</activiti:string>
     </activiti:field>
     <activiti:field name="transportHeaders">
     <activiti:string>Pragma: no-cache,Cache-Control: no-cache</activiti:string>
     </activiti:field>
     </extensionElements>
     </serviceTask>
 */
public class SOAPTask implements JavaDelegate {
    private static final Log log = LogFactory.getLog(SOAPTask.class);
    private Expression serviceURL;
    private Expression payload;
    private Expression headers;
    private Expression soapVersion;
    private Expression httpConnection;
    private Expression httpTransferEncoding;
    private Expression outputVariable;
    private Expression transportHeaders;
    private Expression soapAction;

    public SOAPTask() {
    }

    @Override
    public void execute(DelegateExecution execution) {

        String endpointURL = null;
        String payloadRequest = null;
        String version = null;
        String headerList = null;
        String connection = null;
        String transferEncoding = null;
        String transportHeaderList[] = null;
        String action = "";
        String soapVersionURI = SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI;
        try {
            if (serviceURL != null) {
                endpointURL = serviceURL.getValue(execution).toString();

            } else {
                String urlNotFoundErrorMsg = "Service URL is not provided. serviceURL must be provided.";
                throw new SOAPException(urlNotFoundErrorMsg);
            }
            if (payload != null) {
                payloadRequest = payload.getValue(execution).toString();
            } else {
                String payloadNotFoundErrorMsg = "Payload request is not provided. Payload must be provided.";
                throw new SOAPException(payloadNotFoundErrorMsg);
            }
            if (soapVersion != null) {
                version = soapVersion.getValue(execution).toString();
                if (version.equalsIgnoreCase(Constants.SOAP12_VERSION)) {
                    soapVersionURI = SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI;
                }
            }

            List list = new ArrayList();

            //Adding the connection
            Header connectionHeader = new Header();
            if (httpConnection != null) {
                connection = httpConnection.getValue(execution).toString();
            } else {
                connection = "keep-alive";
            }
            connectionHeader.setName("Connection");
            connectionHeader.setValue(connection);
            list.add(connectionHeader);

            //Adding the soap action
            if (soapAction != null) {
                action = soapAction.getValue(execution).toString();
            }
            if (transportHeaders != null) {
                String headerContent = transportHeaders.getValue(execution).toString();
                transportHeaderList = headerContent.split(",");
                for (String transportHeader : transportHeaderList) {
                    String pair[] = transportHeader.split(":");
                    Header additionalHeader = new Header();
                    if (pair.length == 1) {
                        additionalHeader.setName(pair[0]);
                        additionalHeader.setValue("");
                    } else {
                        additionalHeader.setName(pair[0]);
                        additionalHeader.setValue(pair[1]);
                    }
                    list.add(additionalHeader);
                }
            }

            OMElement payLoad = AXIOMUtil.stringToOM(payloadRequest);
            ServiceClient sender;
            OMElement response = null;
            sender = new ServiceClient(ServiceComponent.getConfigurationContext(), null);
            Options options = new Options();
            options.setTo(new EndpointReference(endpointURL));
            options.setProperty(org.apache.axis2.transport.http.HTTPConstants.HTTP_HEADERS, list);
            options.setAction(action);
            options.setSoapVersionURI(soapVersionURI);
            options.setProperty(org.apache.axis2.transport.http.HTTPConstants.CHUNKED, Boolean.TRUE);

            //Adding the transfer encoding
            if (httpTransferEncoding != null) {
                transferEncoding = httpTransferEncoding.getValue(execution).toString();
                if (!transferEncoding.equalsIgnoreCase("chunked")) {
                    options.setProperty(org.apache.axis2.transport.http.HTTPConstants.CHUNKED, Boolean.FALSE);
                }
            }

            sender.setOptions(options);

            if (headers != null) {
                headerList = headers.getValue(execution).toString();
                OMElement headerElement = AXIOMUtil.stringToOM(headerList);
                sender.addHeader(headerElement);

            }
            response = sender.sendReceive(payLoad);
            String responseStr = response.toStringWithConsume();
            if (outputVariable != null) {
                String outVarName = outputVariable.getValue(execution).toString();
                execution.setVariable(outVarName, responseStr);
            } else {
                String outputNotFoundErrorMsg = "Output variable is not provided. " +
                        "outputVariable must be provided to save " +
                        "the response.";
                throw new SOAPException(outputNotFoundErrorMsg);

            }
            log.info("Response Message :" + execution.getVariable(outputVariable.getValue(execution).toString()));


        } catch (SOAPException e) {
            log.error("Exception when generating the envelope", e);
            throw new BpmnError("Exception when generating the envelope");
        } catch (AxisFault axisFault) {
            log.error("Axis2 Fault" + axisFault.getMessage());
            throw new BpmnError("AxisFault while getting response :" + axisFault.getMessage());
        } catch (XMLStreamException e) {
            log.error("XML Stream Exception");
            throw new BpmnError("XMLStreamException  :" + e.getMessage());
        }

    }

}
