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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.bpmn.extensions.soap.impl.CallbackSOAPMessage;
import org.wso2.carbon.bpmn.extensions.soap.impl.CarbonSOAPMessage;
import org.wso2.carbon.bpmn.extensions.soap.impl.HTTPTransportHeaders;
import org.wso2.carbon.bpmn.extensions.soap.impl.SOAPCallBackResponseImpl;
import org.wso2.carbon.bpmn.extensions.soap.impl.SOAPEnvelope;
import org.wso2.carbon.bpmn.extensions.soap.impl.SOAPException;
import org.wso2.carbon.bpmn.extensions.soap.impl.SOAPFactory;
import org.wso2.carbon.bpmn.extensions.soap.impl.SOAPModel;
import org.wso2.carbon.messaging.Constants;
import org.wso2.carbon.messaging.MessageProcessorException;
import org.wso2.carbon.transport.http.netty.config.SenderConfiguration;
import org.wso2.carbon.transport.http.netty.config.TransportsConfiguration;
import org.wso2.carbon.transport.http.netty.config.YAMLTransportConfigurationBuilder;
import org.wso2.carbon.transport.http.netty.sender.NettySender;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;
import java.util.Set;

/**
 * Provides SOAP service invocation support within BPMN processes.
 */
public class SOAPTask implements JavaDelegate {
    private static final Logger log = LoggerFactory.getLogger(SOAPTask.class);
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

    @Override
    public void execute(DelegateExecution execution) {
        if (log.isDebugEnabled()) {
            log.debug("Executing SOAP Task " + serviceURL.getValue(execution).toString());
        }
        String senderConfig = null;
        TransportsConfiguration trpConfig = YAMLTransportConfigurationBuilder.build();
        Set<SenderConfiguration> configs = trpConfig.getSenderConfigurations();
        if (!(configs.isEmpty())) {
            for (SenderConfiguration config : configs) {
                if (config.getScheme() != null && config.getScheme().toLowerCase(Locale.ENGLISH).equals(HTTP_SCHEMA)) {
                    senderConfig = config.getId();
                    break;
                }
            }
        } else {
            log.error("Sender Configuration is id is not specified in netty-transports.yml");
        }

        SenderConfiguration senderConfiguration = new SenderConfiguration(senderConfig);
        NettySender nettySender = new NettySender(senderConfiguration);

        String endpointURL = null;
        String payloadRequest = null;
        String version = null;
        String headerList = null;
        String host = null;
        int port = 0;
        String service = null;
        String connection = null;
        String transferEncoding = null;
        String transportHeaderList[] = null;
        String action = null;
        try {
            if (serviceURL != null) {
                endpointURL = serviceURL.getValue(execution).toString();
                URL url = new URL(endpointURL);
                host = url.getHost();
                port = url.getPort();
                service = url.getPath();

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
            } else {
                version = org.wso2.carbon.bpmn.extensions.soap.constants.Constants.SOAP11_VERSION;
            }

        } catch (SOAPException e) {
            log.error("SOAP Exception");
            throw new BpmnError("SOAP Exception");
        } catch (MalformedURLException e) {
            log.error("Exception when creating the URL");
            throw new BpmnError("Exception when creating the URL");
        }
        // Setting the properties
        CarbonSOAPMessage carbonSOAPMessage = new CarbonSOAPMessage();
        carbonSOAPMessage.setProperty(Constants.HOST, host);
        carbonSOAPMessage.setProperty(Constants.PORT, port);
        carbonSOAPMessage.setProperty(Constants.TO, service);
        carbonSOAPMessage.setProperty(org.wso2.carbon.transport.http.netty.common.Constants.IS_DISRUPTOR_ENABLE,
                "true");

        //Creating the soap envelope
        SOAPFactory soapFactory = new SOAPFactory();
        SOAPModel soapModel = null;
        try {
            soapModel = soapFactory.getSoapModel(version);
            soapModel.createSOAPEnvelope();
            if (headers != null) {
                headerList = headers.getValue(execution).toString();
                soapModel.createSOAPHeader(soapModel.createNode(headerList));
            } else {
                soapModel.createSOAPHeader();
            }
            if (payloadRequest.length() != 0) {
                soapModel.createSOAPBody(soapModel.createNode(payloadRequest));
            } else {
                soapModel.createSOAPBody();
            }


            SOAPEnvelope soapEnvelope = soapModel.generateSOAPEnvelope();

            // Setting the SOAP Envelope
            carbonSOAPMessage.setSOAPMessage(soapEnvelope);

            HTTPTransportHeaders httpTransportHeaders = new HTTPTransportHeaders(soapModel);
            if (httpConnection != null) {
                connection = httpConnection.getValue(execution).toString();
            } else {
                connection = Constants.KEEP_ALIVE;
            }
            if (httpTransferEncoding != null) {
                transferEncoding = httpTransferEncoding.getValue(execution).toString();
            } else {
                transferEncoding = "chunked";
            }
            if (soapAction != null) {
                action = soapAction.getValue(execution).toString();
                httpTransportHeaders.addSOAPAction(action);
            }

            httpTransportHeaders.addHeader(Constants.HTTP_CONNECTION, connection);
            httpTransportHeaders.addHeader(Constants.HTTP_HOST, host.concat(":" + port));
            httpTransportHeaders.addHeader(Constants.HTTP_TRANSFER_ENCODING, transferEncoding);

            if (transportHeaders != null) {
                String headerContent = transportHeaders.getValue(execution).toString();
                transportHeaderList = headerContent.split(",");
                for (String header : transportHeaderList) {
                    String pair[] = header.split(":");
                    if (pair.length == 1) {
                        httpTransportHeaders.addHeader(pair[0], "");
                    } else {
                        httpTransportHeaders.addHeader(pair[0], pair[1]);
                    }
                }
            }

            carbonSOAPMessage.setHeaderProperties(httpTransportHeaders);

        } catch (SOAPException e) {
            log.error("Exception when generating the envelope", e);
            throw new BpmnError("Exception when generating the envelope");
        }

        try {
            SOAPCallBackResponseImpl callBackResponse = new SOAPCallBackResponseImpl();
            CallbackSOAPMessage callbackSOAPMessage = new CallbackSOAPMessage(callBackResponse);
            nettySender.send(carbonSOAPMessage, callbackSOAPMessage);
            while (!callBackResponse.isSuccess()) {
                Thread.sleep(500);
            }
            if (outputVariable != null) {
                String outVarName = outputVariable.getValue(execution).toString();
                execution.setVariable(outVarName, callBackResponse.getResponseMessage());
            } else {
                String outputNotFoundErrorMsg = "Output variable is not provided. " +
                        "outputVariable must be provided to save " +
                        "the response.";
                throw new SOAPException(outputNotFoundErrorMsg);

            }

        } catch (MessageProcessorException e) {
            log.error("Message Processor Exception");
            throw new BpmnError("Message Processor Exception");
        } catch (InterruptedException e) {
            log.error("Thread Interrupted");
            throw new BpmnError("Thread Interrupted");
        } catch (SOAPException e) {
            log.error("SOAP Exception");
            throw new BpmnError("SOAP Exception");
        }

    }

}
