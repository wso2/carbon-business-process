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
package org.wso2.carbon.bpmn.extensions;

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
import org.wso2.carbon.bpmn.extensions.soap.impl.HTTPTransportHeaders;
import org.wso2.carbon.bpmn.extensions.soap.impl.SOAPException;

import javax.xml.stream.XMLStreamException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Provides SOAP service invocation support within BPMN processes.
 */
public class SOAPTask implements JavaDelegate {
    private static final Log log = LogFactory.getLog(SOAPTask.class);
    private static final String HTTP_SCHEMA = "http";
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
        String host = null;
        int port = 0;
        String connection = null;
        String transferEncoding = null;
        String transportHeaderList[] = null;
        String action = null;
        String soapVersionURI = SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI;
        try {
            if (serviceURL != null) {
                endpointURL = serviceURL.getValue(execution).toString();
                URL url = new URL(endpointURL);
                host = url.getHost();
                port = url.getPort();

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

            // Create an instance of org.apache.commons.httpclient.Header
            Header contentTypeHeader = new Header();


            //Adding the connection
            Header transferEncodingHeader = new Header();
            if (httpConnection != null) {
                connection = httpConnection.getValue(execution).toString();
            } else {
                connection = "keep-alive";
            }
            transferEncodingHeader.setName("Connection");
            transferEncodingHeader.setValue(connection);
            list.add(transferEncodingHeader);

            //Adding the soap action
            if (soapAction != null) {
                action = soapAction.getValue(execution).toString();
            } else {
                action = "";
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

            //Adding the transfer encoding
            if (httpTransferEncoding != null) {
                transferEncoding = httpTransferEncoding.getValue(execution).toString();
                if (transferEncoding.equalsIgnoreCase("chunked")) {
                    options.setProperty(org.apache.axis2.transport.http.HTTPConstants.CHUNKED, Boolean.TRUE);
                } else {
                    options.setProperty(org.apache.axis2.transport.http.HTTPConstants.CHUNKED, Boolean.FALSE);

                }
            } else {
                options.setProperty(org.apache.axis2.transport.http.HTTPConstants.CHUNKED, Boolean.TRUE);

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

           /* System.out.println("Response Message :" + response);
            System.out.println("Str --> " + responseStr);*/
        } catch (SOAPException e) {
            log.error("Exception when generating the envelope", e);
            throw new BpmnError("Exception when generating the envelope");
        } catch (AxisFault axisFault) {
            System.out.println(axisFault.getMessage());
            throw new BpmnError("AxisFault while getting response :" + axisFault.getMessage());
        } catch (XMLStreamException e) {
            System.out.println(e.getMessage());
            throw new BpmnError("XMLStreamException  :" + e.getMessage());
        } catch (MalformedURLException e) {
            //  log.error("Exception when creating the URL");
            throw new BpmnError("Exception when creating the URL");
        }


    }

}
