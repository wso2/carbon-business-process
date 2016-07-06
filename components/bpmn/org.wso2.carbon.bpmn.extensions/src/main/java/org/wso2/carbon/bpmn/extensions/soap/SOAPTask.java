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
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.httpclient.Header;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bpmn.extensions.internal.BPMNExtensionsComponent;
import org.wso2.carbon.registry.api.Registry;
import org.wso2.carbon.registry.api.RegistryException;
import org.wso2.carbon.registry.api.Resource;
import org.wso2.carbon.unifiedendpoint.core.UnifiedEndpoint;
import org.wso2.carbon.unifiedendpoint.core.UnifiedEndpointFactory;

import javax.xml.stream.XMLStreamException;
import java.nio.charset.Charset;
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
 * activiti:class="org.wso2.carbon.bpmn.extensions.soap.SOAPTask"
 * activiti:extensionId="org.wso2.bps.tooling.bpmn.extensions.soapTask.SOAPTask">
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
    private static final String GOVERNANCE_REGISTRY_PREFIX = "gov:/";
    private static final String CONFIGURATION_REGISTRY_PREFIX = "conf:/";
    private static final String REST_INVOKE_ERROR = "REST_CLIENT_INVOKE_ERROR";
    private static final String SOAP12_VERSION = "soap12";
    private static final String SOAP11_VERSION = "soap11";
    private static final String SOAP_INVOKE_ERROR_CODE = "SOAP_CLIENT_INVOKE_ERROR";



    private Expression serviceURL;
    private Expression serviceRef;
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


        String endpointURL;
        String payloadRequest;
        String version;
        String connection;
        String transferEncoding;
        String transportHeaderList[];
        String action = "";
        String soapVersionURI = SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI;
        List<Header> headerList = new ArrayList<Header>();

        try {
            if (serviceURL != null) {
                endpointURL = serviceURL.getValue(execution).toString();
            } else if (serviceRef != null) {
                String resourcePath = serviceRef.getValue(execution).toString();
                String registryPath;
                String tenantId = execution.getTenantId();
                Registry registry;
                if (resourcePath.startsWith(GOVERNANCE_REGISTRY_PREFIX)) {
                    registryPath = resourcePath.substring(GOVERNANCE_REGISTRY_PREFIX.length());
                    registry = BPMNExtensionsComponent.getRegistryService().getGovernanceSystemRegistry(
                            Integer.parseInt(tenantId));
                } else if (resourcePath.startsWith(CONFIGURATION_REGISTRY_PREFIX)) {
                    registryPath = resourcePath.substring(CONFIGURATION_REGISTRY_PREFIX.length());
                    registry = BPMNExtensionsComponent.getRegistryService().getConfigSystemRegistry(
                            Integer.parseInt(tenantId));
                } else {
                    String msg = "Registry type is not specified for service reference in " +
                            " serviceRef should begin with gov:/ or conf:/ to indicate the registry type.";
                    throw new SOAPException(SOAP_INVOKE_ERROR_CODE , msg);
                }

                if (log.isDebugEnabled()) {
                    log.debug("Reading endpoint from registry location: " + registryPath + " for task " +
                            execution.getCurrentActivityName());
                }
                Resource urlResource = registry.get(registryPath);
                if (urlResource != null) {
                    String uepContent = new String((byte[]) urlResource.getContent(), Charset.defaultCharset());

                    UnifiedEndpointFactory uepFactory = new UnifiedEndpointFactory();
                    OMElement uepElement = AXIOMUtil.stringToOM(uepContent);
                    UnifiedEndpoint uep = uepFactory.createEndpoint(uepElement);
                    endpointURL = uep.getAddress();

                } else {
                    String errorMsg = "Endpoint resource " + registryPath +
                            " is not found. Failed to execute REST invocation in task " +
                            execution.getCurrentActivityName();
                    throw new SOAPException(SOAP_INVOKE_ERROR_CODE, errorMsg);
                }
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
                if (connection != null && !connection.trim().equals("Keep-Alive")) {
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
                            if (log.isDebugEnabled()) {
                                log.debug("Adding transport headers " + pair[0]);
                            }
                        } else {
                            additionalHeader.setName(pair[0]);
                            additionalHeader.setValue(pair[1]);
                            if (log.isDebugEnabled()) {
                                log.debug("Adding transport headers " + pair[0] + " " + pair[1] );
                            }
                        }
                        headerList.add(additionalHeader);
                    }
                }

            }

            //Adding the soap action
            if (soapAction != null) {
                action = soapAction.getValue(execution).toString();
                if (log.isDebugEnabled()) {
                    log.debug("Setting soap action " + soapAction);
                }
            }
            //Converting the payload to an OMElement
            OMElement payLoad = AXIOMUtil.stringToOM(payloadRequest);
            //Creating the Service client
            ServiceClient sender = new ServiceClient();
            OMElement response;
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
                if (log.isDebugEnabled()) {
                    log.debug("Adding soap header " + headerContent);
                }
            }
            //Adding the transfer encoding
            if (httpTransferEncoding != null) {
                transferEncoding = httpTransferEncoding.getValue(execution).toString();
                if (transferEncoding.equalsIgnoreCase("chunked")) {
                    options.setProperty(HTTPConstants.CHUNKED, Boolean.TRUE);
                    if (log.isDebugEnabled()) {
                        log.debug("Enabling transfer encoding chunked ");
                    }
                } else {
                    options.setProperty(HTTPConstants.CHUNKED, Boolean.FALSE);
                    if (log.isDebugEnabled()) {
                        log.debug("Disabling transfer encoding chunked ");
                    }
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
            log.error("Axis2 Fault", axisFault);
            throw new SOAPException(SOAP_INVOKE_ERROR_CODE, "Exception while getting response :" +
                    axisFault.getMessage());
        } catch (XMLStreamException | RegistryException e) {
            log.error("Exception in processing", e);
            throw new SOAPException(SOAP_INVOKE_ERROR_CODE, "Exception in processing  :" + e.getMessage());
        }
    }

    public void setServiceURL(Expression serviceURL) {
        this.serviceURL = serviceURL;
    }

    public void setServiceRef(Expression serviceRef) {
        this.serviceRef = serviceRef;
    }

    public void setPayload(Expression payload) {
        this.payload = payload;
    }

    public void setHeaders(Expression headers) {
        this.headers = headers;
    }

    public void setSoapVersion(Expression soapVersion) {
        this.soapVersion = soapVersion;
    }

    public void setHttpConnection(Expression httpConnection) {
        this.httpConnection = httpConnection;
    }

    public void setOutputVariable(Expression outputVariable) {
        this.outputVariable = outputVariable;
    }

    public void setHttpTransferEncoding(Expression httpTransferEncoding) {
        this.httpTransferEncoding = httpTransferEncoding;
    }

    public void setSoapAction(Expression soapAction) {
        this.soapAction = soapAction;
    }

    public void setTransportHeaders(Expression transportHeaders) {
        this.transportHeaders = transportHeaders;
    }
}