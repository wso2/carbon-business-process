/*
 * Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.bpel.bam.publisher;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.common.FaultException;
import org.apache.ode.bpel.elang.xpath20.o.OXPath20ExpressionBPEL20;
import org.apache.ode.bpel.explang.ConfigurationException;
import org.apache.ode.bpel.explang.EvaluationException;
import org.apache.ode.bpel.o.OExpressionLanguage;
import org.apache.ode.bpel.runtime.ExprEvaluationContextImpl;
import org.apache.ode.bpel.runtime.ExtensionContextImpl;
import org.apache.ode.bpel.runtime.ScopeFrame;
import org.apache.ode.bpel.runtime.extension.AbstractSyncExtensionOperation;
import org.apache.ode.bpel.runtime.extension.ExtensionContext;
import org.apache.ode.store.DeploymentUnitDir;
import org.apache.ode.utils.DOMUtils;
import org.apache.ode.utils.Namespaces;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.wso2.carbon.bpel.bam.publisher.internal.BAMPublisherServiceComponent;
import org.wso2.carbon.bpel.core.ode.integration.config.bam.BAMKey;
import org.wso2.carbon.bpel.core.ode.integration.config.bam.BAMServerProfile;
import org.wso2.carbon.bpel.core.ode.integration.config.bam.BAMStreamConfiguration;
import org.wso2.carbon.bpel.core.ode.integration.store.TenantProcessStore;
import org.wso2.carbon.databridge.agent.thrift.Agent;
import org.wso2.carbon.databridge.agent.thrift.AsyncDataPublisher;
import org.wso2.carbon.databridge.agent.thrift.DataPublisher;
import org.wso2.carbon.databridge.agent.thrift.conf.AgentConfiguration;
import org.wso2.carbon.databridge.agent.thrift.exception.AgentException;
import org.wso2.carbon.databridge.agent.thrift.lb.DataPublisherHolder;
import org.wso2.carbon.databridge.agent.thrift.lb.LoadBalancingDataPublisher;
import org.wso2.carbon.databridge.agent.thrift.lb.ReceiverGroup;
import org.wso2.carbon.databridge.agent.thrift.util.DataPublisherUtil;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.databridge.commons.exception.*;

import javax.xml.namespace.QName;
import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class BAMPublisherExtensionOperation extends AbstractSyncExtensionOperation {
    private static final Log log = LogFactory.getLog(BAMPublisherExtensionOperation.class);

    @Override
    protected void runSync(ExtensionContext extensionContext, Element element)
            throws FaultException {
        String bamServerProfileName = element.getAttribute("bamServerProfile");
        String streamName = element.getAttribute(BAMPublisherConstants.STREAM_NAME_ATTR);
        String streamVersion = element.getAttribute(BAMPublisherConstants.STREAM_VERSION);
        Integer tenantId = getTenantId(extensionContext);
        BAMServerProfile bamServerProfile = getBAMServerProfile(tenantId, bamServerProfileName);
        BAMStreamConfiguration stream = getEventStream(tenantId, bamServerProfileName, streamName, streamVersion);

        if(stream == null) {
            log.debug("Stream configuration is invalid");
            return;
        }

        if (!bamServerProfile.isLoadBalanced()) {
            AsyncDataPublisher dataPublisher = createAsyncDataPublisher(extensionContext, tenantId, bamServerProfileName,
                    stream);
            if (dataPublisher == null) {
                String msg = "Error while creating data publisher";
                handleException(msg);
            }

            try {
                if (!dataPublisher.canPublish()) {
                    dataPublisher.reconnect();
                }
                dataPublisher.publish(stream.getName(), stream.getVersion(),
                        createMetadata(stream, extensionContext, element),
                        createCorrelationData(stream, extensionContext, element),
                        createPayloadData(stream, extensionContext, element));

            } catch (AgentException e) {
                String errMsg = "Problem with Agent while publishing.";
                handleException(errMsg);
            }
        } else {
           LoadBalancingDataPublisher loadBalancingDataPublisher =  createLoadBalancingDataPublisher(extensionContext, tenantId, bamServerProfileName, stream, element);
           String streamId = element.getAttribute(BAMPublisherConstants.STREAM_ID);


            if (loadBalancingDataPublisher == null)  {
               String msg = "Error while creating data publisher";
               handleException(msg);
           }

           try {
               loadBalancingDataPublisher.publish(streamName, streamVersion, createEvent(streamId, stream, extensionContext, element));
           } catch (AgentException e) {
                String errMsg = "Problem with Agent while publishing.";
                handleException(errMsg);
           }

        }
    }

    private Integer getTenantId(ExtensionContext context) {
        DeploymentUnitDir du = new DeploymentUnitDir(new File(context.getDUDir()));
        QName processIdQname = new QName(context.getProcessModel().getQName().getNamespaceURI(),
                context.getProcessModel().getQName().getLocalPart() + "-"
                        + du.getStaticVersion());
        return BAMPublisherServiceComponent.getBPELServer().
                getMultiTenantProcessStore().getTenantId(processIdQname);
    }


    private AsyncDataPublisher createAsyncDataPublisher(ExtensionContext context, int tenantId,
                                              String bamServerProfileName, BAMStreamConfiguration stream) throws FaultException {
        AsyncDataPublisher dataPublisher = null;
        EventPublisherConfig config = null;
        TenantProcessStore tenantsProcessStore =
                BAMPublisherServiceComponent.getBPELServer().getMultiTenantProcessStore().getTenantsProcessStore(tenantId);
        String processName = context.getProcessModel().getName().toString();
        config = (EventPublisherConfig)tenantsProcessStore.getDataPublisher(processName);

        if(config == null) {
            BAMServerProfile bamServerProfile = getBAMServerProfile(tenantId, bamServerProfileName);
            dataPublisher = new AsyncDataPublisher(bamServerProfile.getUrl(), bamServerProfile.getUserName(), bamServerProfile.getPassword());

            log.info("BPEL BAM data publisher created");
                addEventStream(dataPublisher, stream);
                config = new EventPublisherConfig(dataPublisher);
                config.addEventStream(stream.getName(), stream.getVersion());
                tenantsProcessStore.addDataPublisher(processName, config);
        } else {
            dataPublisher = config.getDataPublisher();
            if(!config.eventStreamAlreadyDefined(stream.getName(), stream.getVersion())) {
                addEventStream(config.getDataPublisher(), stream);
                config.addEventStream(stream.getName(), stream.getVersion());
            }
        }

        return dataPublisher;
    }

    private LoadBalancingDataPublisher createLoadBalancingDataPublisher(ExtensionContext context, int tenantId, String bamServerProfileName, BAMStreamConfiguration stream, Element element) throws FaultException {
        EventPublisherConfig config = null;
        LoadBalancingDataPublisher loadBalancingDataPublisher = null;
        TenantProcessStore tenantsProcessStore =
                BAMPublisherServiceComponent.getBPELServer().getMultiTenantProcessStore().getTenantsProcessStore(tenantId);
        String processName = context.getProcessModel().getName();
        config = (EventPublisherConfig) tenantsProcessStore.getDataPublisher(processName);

        if (config == null) {
            BAMServerProfile bamServerProfile = getBAMServerProfile(tenantId, bamServerProfileName);

            ArrayList<ReceiverGroup> allReceiverGroups = new ArrayList<ReceiverGroup>();
            ArrayList<String> receiverGroupUrls = DataPublisherUtil.getReceiverGroups(bamServerProfile.getUrl());

            for (String aReceiverGroupURL : receiverGroupUrls) {
                ArrayList<DataPublisherHolder> dataPublisherHolders = new ArrayList<DataPublisherHolder>();
                String[] urls = aReceiverGroupURL.split(",");
                for (String aUrl : urls) {
                    DataPublisherHolder aNode = new DataPublisherHolder(null, aUrl.trim(), bamServerProfile.getUserName(),
                            bamServerProfile.getPassword());
                    dataPublisherHolders.add(aNode);
                }
                ReceiverGroup group = new ReceiverGroup(dataPublisherHolders);
                allReceiverGroups.add(group);
            }

            loadBalancingDataPublisher = new LoadBalancingDataPublisher(allReceiverGroups);
            addEventStream(loadBalancingDataPublisher, stream);
            config = new EventPublisherConfig(loadBalancingDataPublisher);
            config.addEventStream(stream.getName(), stream.getVersion());
            tenantsProcessStore.addDataPublisher(processName, config);

        } else {

            loadBalancingDataPublisher = config.getLoadBalancingDataPublisher();
            if (!config.eventStreamAlreadyDefined(stream.getName(), stream.getVersion())) {
                addEventStream(config.getDataPublisher(), stream);
                config.addEventStream(stream.getName(), stream.getVersion());
            }
        }
        return loadBalancingDataPublisher;
    }

    private BAMServerProfile getBAMServerProfile(int tenantId, String bamServerProfileName) {
        TenantProcessStore tenantsProcessStore = BAMPublisherServiceComponent.getBPELServer().
                getMultiTenantProcessStore().getTenantsProcessStore(tenantId);
        return tenantsProcessStore.getBAMServerProfile(bamServerProfileName);
    }

    private String defineEventStream(DataPublisher dataPublisher, BAMStreamConfiguration stream)
            throws FaultException {
        String streamDefinition = "{" +
                "  'name':'" + stream.getName() + "'," +
                "  '" + BAMPublisherConstants.STREAM_VERSION + "':'" + stream.getVersion() + "'," +
                "  '" + BAMPublisherConstants.STREAM_NICK_NAME + "': '" + stream.getNickName() + "'," +
                "  '" + BAMPublisherConstants.STREAM_DESCRIPTION + "': '" + stream.getDescription() + "'," +
                "  'metaData':[" +
                    "{'name':'" + BAMPublisherConstants.TENANT_ID + "','type':'INT'}" +
                    ", {'name':'" + BAMPublisherConstants.PROCESS_ID + "','type':'STRING'}" +
                    getStreamDefinitionString(BAMKey.BAMKeyType.META, stream) +
                "  ]," +
                "  'payloadData':[" +
                    getStreamDefinitionString(BAMKey.BAMKeyType.PAYLOAD, stream) +
                "  ]," +
                "  'correlationData':[" +
                "{'name':'" + BAMPublisherConstants.INSTANCE_ID + "','type':'STRING'}" +
                    getStreamDefinitionString(BAMKey.BAMKeyType.CORRELATION, stream) +
                "  ]" +
                "}";
        try {
            return dataPublisher.defineStream(streamDefinition);
        } catch (AgentException e) {
            String errorMsg = "Problem using creating the Agent.";
            handleException(errorMsg, e);
        } catch (MalformedStreamDefinitionException e) {
            String errorMsg = "Invalid Stream definition: " + streamDefinition;
            handleException(errorMsg, e);
        } catch (StreamDefinitionException e) {
            String errorMsg = "Problem with Stream Definition: " + streamDefinition;
            handleException(errorMsg, e);
        } catch (DifferentStreamDefinitionAlreadyDefinedException ignore) {
            //TODO If the stream is already defined, just ignore and continue.
            // Also check whether streams are defined while deploying the process and keep the status,
            // so that we can check it before call define stream method.
            // String errorMsg = "Already there is a different Stream Definition exists
            // for the Name and Version. " + e.getMessage();
        }
        handleException("Error occurred while defining the stream: " + stream.getName());
        return null;
    }

    private void addEventStream(LoadBalancingDataPublisher dataPublisher, BAMStreamConfiguration stream)
            throws FaultException {
        String streamDefinition = "{" +
                "  'name':'" + stream.getName() + "'," +
                "  '" + BAMPublisherConstants.STREAM_VERSION + "':'" + stream.getVersion() + "'," +
                "  '" + BAMPublisherConstants.STREAM_NICK_NAME + "': '" + stream.getNickName() + "'," +
                "  '" + BAMPublisherConstants.STREAM_DESCRIPTION + "': '" + stream.getDescription() + "'," +
                "  'metaData':[" +
                "{'name':'" + BAMPublisherConstants.TENANT_ID + "','type':'INT'}" +
                ", {'name':'" + BAMPublisherConstants.PROCESS_ID + "','type':'STRING'}" +
                getStreamDefinitionString(BAMKey.BAMKeyType.META, stream) +
                "  ]," +
                "  'payloadData':[" +
                getStreamDefinitionString(BAMKey.BAMKeyType.PAYLOAD, stream) +
                "  ]," +
                "  'correlationData':[" +
                "{'name':'" + BAMPublisherConstants.INSTANCE_ID + "','type':'STRING'}" +
                getStreamDefinitionString(BAMKey.BAMKeyType.CORRELATION, stream) +
                "  ]" +
                "}";

        if (!dataPublisher.isStreamDefinitionAdded(stream.getName(), stream.getVersion())) {
            dataPublisher.addStreamDefinition(streamDefinition, stream.getName(), stream.getVersion());
        }
    }

    private void addEventStream(AsyncDataPublisher dataPublisher, BAMStreamConfiguration stream)
            throws FaultException {
        String streamDefinition = "{" +
                "  'name':'" + stream.getName() + "'," +
                "  '" + BAMPublisherConstants.STREAM_VERSION + "':'" + stream.getVersion() + "'," +
                "  '" + BAMPublisherConstants.STREAM_NICK_NAME + "': '" + stream.getNickName() + "'," +
                "  '" + BAMPublisherConstants.STREAM_DESCRIPTION + "': '" + stream.getDescription() + "'," +
                "  'metaData':[" +
                "{'name':'" + BAMPublisherConstants.TENANT_ID + "','type':'INT'}" +
                ", {'name':'" + BAMPublisherConstants.PROCESS_ID + "','type':'STRING'}" +
                getStreamDefinitionString(BAMKey.BAMKeyType.META, stream) +
                "  ]," +
                "  'payloadData':[" +
                getStreamDefinitionString(BAMKey.BAMKeyType.PAYLOAD, stream) +
                "  ]," +
                "  'correlationData':[" +
                "{'name':'" + BAMPublisherConstants.INSTANCE_ID + "','type':'STRING'}" +
                getStreamDefinitionString(BAMKey.BAMKeyType.CORRELATION, stream) +
                "  ]" +
                "}";

            dataPublisher.addStreamDefinition(streamDefinition, stream.getName(), stream.getVersion());
    }

    private String getStreamDefinitionString(BAMKey.BAMKeyType type, BAMStreamConfiguration stream) throws FaultException {
        String keyString = "";
        List<BAMKey> keys = null;
        switch (type) {
            case PAYLOAD:
                keys = stream.getPayloadBAMKeyList();
                break;
            case META:
                keys = stream.getMetaBAMKeyList();
                break;
            case CORRELATION:
                keys = stream.getCorrelationBAMKeyList();
                break;
            default:
                String errMsg = "Unknown BAM key type: " + type;
                handleException(errMsg);
        }
        for(int i = 0 ; i < keys.size(); i++)  {
            BAMKey key = keys.get(i);
            if(type == BAMKey.BAMKeyType.CORRELATION || type == BAMKey.BAMKeyType.META) {
                keyString = "," ;
            }
            if(i == 0) {
                keyString = keyString + "{'name':'" + key.getName() + "','type':'STRING'}";
            } else {
                keyString = keyString + ", {'name':'" + key.getName() + "','type':'STRING'}";
            }
        }

        return keyString;
    }

    private BAMStreamConfiguration getEventStream(int tenantId, String bamServerProfileName,
                                                  String streamName, String streamVersion) {
        BAMServerProfile bamServerProfile = getBAMServerProfile(tenantId, bamServerProfileName);
        if(null == bamServerProfile) {
            String errMsg = "BAMServerProfile not found for stream name and version " + streamName + " " + streamVersion;
            log.error(errMsg);
            return null;
        }
        return bamServerProfile.getBAMStreamConfiguration(streamName, streamVersion);
    }

    private void handleException(String errMsg, Throwable t) throws FaultException {
        log.error(errMsg, t);
        throw new FaultException(BAMPublisherConstants.BAM_FAULT, errMsg, t);
    }

    private void handleException(String errMsg) throws FaultException {
        log.error(errMsg);
        throw new FaultException(BAMPublisherConstants.BAM_FAULT, errMsg);
    }

    private Event createEvent(String streamId, BAMStreamConfiguration stream,
                              ExtensionContext context, Element element)
            throws FaultException {
        Event e = new Event();
        e.setStreamId(streamId);
        e.setTimeStamp(System.currentTimeMillis());
        e.setCorrelationData(createCorrelationData(stream, context, element));
        e.setMetaData(createMetadata(stream, context, element));
        e.setPayloadData(createPayloadData(stream, context, element));
        return e;
    }

    private Object[] createCorrelationData(BAMStreamConfiguration stream, ExtensionContext context, Element element)
            throws FaultException {
        List<BAMKey> correlationBAMKeyList = stream.getCorrelationBAMKeyList();
        int objectListSize = correlationBAMKeyList.size() + 1;
        Object[] dataArray = new Object[objectListSize];
        dataArray[0] = context.getInternalInstance().getPid().toString();
        int startIndex = 1;
        fillDataArray(dataArray, correlationBAMKeyList, startIndex, context, element);
        return dataArray;
    }

    private Object[] createMetadata(BAMStreamConfiguration stream, ExtensionContext context, Element element)
            throws FaultException {
        List<BAMKey> metaBAMKeyList = stream.getMetaBAMKeyList();

        int objectListSize = metaBAMKeyList.size() + 2;
        Object[] dataArray = new Object[objectListSize];
        dataArray[0] = getTenantId(context);
        dataArray[1] = context.getProcessModel().getQName().toString();
        int startIndex = 2;
        fillDataArray(dataArray, metaBAMKeyList, startIndex, context, element);
        return dataArray;
    }

    private Object[] createPayloadData(BAMStreamConfiguration stream, ExtensionContext context, Element element)
            throws FaultException {
        List<BAMKey> payloadBAMKeyList = stream.getPayloadBAMKeyList();
        int objectListSize = payloadBAMKeyList.size();
        Object[] dataArray = new Object[objectListSize];
        int startIndex = 0;
        fillDataArray(dataArray, payloadBAMKeyList, startIndex, context, element);
        return dataArray;
    }

    private void fillDataArray(Object[] dataArray, List<BAMKey> payloadBAMKeyList, int startIndex,
                               ExtensionContext context, Element element) throws FaultException {
        for (int i = 0; i < payloadBAMKeyList.size(); i++) {
            BAMKey bamKey = payloadBAMKeyList.get(i);
            if (bamKey.getExpression() != null) {
                dataArray[i + startIndex] = evaluateXPathExpression(context, bamKey.getExpression(), element);
            } else if (bamKey.getVariable() != null && bamKey.getPart() == null) {
                if (bamKey.getQuery() == null) {
                    /* simple types should be specified for here */
                    dataArray[i + startIndex] = context.readVariable(bamKey.getVariable()).getTextContent();
                } else {
                    String errMsg = "This functionality is currently not supported";
                    log.error(errMsg);
                    handleException(errMsg);
                }
            } else if (bamKey.getVariable() != null && bamKey.getPart() != null) {
                NodeList childNodes = context.readVariable(bamKey.getVariable()).getChildNodes();
                String result = null;
                String part = bamKey.getPart();
                for(int j=0; j < childNodes.getLength(); j++) {
                    Node item = childNodes.item(j);
                    if(item != null && item.getNodeType() == Node.ELEMENT_NODE && item.getLocalName().equals(part)) {
                        /* remove the payload part */
                        result = DOMUtils.domToString(DOMUtils.getFirstChildElement(item));
                    }
                }
                dataArray[i + startIndex] = result;
            }
        }
    }

    private String evaluateXPathExpression(ExtensionContext context, String xpath, Element element)
            throws FaultException{
        String result = "";
        QName qnVariableData = new QName(Namespaces.BPEL11_NS, "getVariableData");
        QName qnGetVariableProperty = new QName(Namespaces.BPEL11_NS, "getVariableProperty");
        QName qnGetLinkStatus = new QName(Namespaces.BPEL11_NS, "getLinkStatus");
        QName qnDoXslTransform = new QName(Namespaces.BPEL11_NS,"getDoXslTransform");

        OXPath20ExpressionBPEL20 oexpr = new OXPath20ExpressionBPEL20(
                context.getInternalInstance().getProcessModel().getOwner(),
                qnVariableData, qnGetVariableProperty, qnGetLinkStatus, qnDoXslTransform, false);

        OExpressionLanguage oExpressionLanguage= new OExpressionLanguage(context.getProcessModel().getOwner(),null);
        oExpressionLanguage.expressionLanguageUri="urn:oasis:names:tc:wsbpel:2.0:sublang:xpath2.0";
        oexpr.expressionLanguage= oExpressionLanguage;

        oExpressionLanguage.properties.put("runtime-class","org.apache.ode.bpel.elang.xpath20.runtime.XPath20ExpressionRuntime");

        try{
            context.getInternalInstance().getExpLangRuntime().registerRuntime(oExpressionLanguage);
        } catch (ConfigurationException ex) {
            String errMsg = "Error when trying to register xpath runtime";
            log.error(errMsg, ex);
            handleException(errMsg, ex);
        }
        oexpr.insertMissingData=true;
        ScopeFrame scopeFrame=((ExtensionContextImpl)context).getScopeFrame();
        ExprEvaluationContextImpl exprEvaluationContext= new ExprEvaluationContextImpl(
                scopeFrame,context.getInternalInstance());

        oexpr.vars = (HashMap)context.getVisibleVariables();

        oexpr.namespaceCtx = context.getProcessModel().namespaceContext;

        try {
            oexpr.xpath = xpath;
            List resultList= context.getInternalInstance().getExpLangRuntime().evaluate(
                    oexpr, exprEvaluationContext);
            if(result != null) {
                Iterator iterator = resultList.iterator();
                /** for bam publishing to work, there should only be a single node here */
                while (iterator.hasNext()){
                    Node node = ((Node)iterator.next());
                    if(node.getNodeType() == Node.ELEMENT_NODE) {
                        result += node.getTextContent();
                    } else if(node.getNodeType() == Node.ATTRIBUTE_NODE) {
                        result += node.getNodeValue();
                    }
                }
            }
        } catch (EvaluationException e) {
            String errMsg = "Xpath evaluation failed";
            log.error(errMsg);
            handleException(errMsg, e);
        }
        return result;
    }

    private Agent getBamAgent(int tenantId, String bamServerProfileName) throws FaultException{
        Agent agent = TenantBamAgentHolder.getInstance().getAgent(tenantId);
        if(agent == null) {
            // Check whether setting security properties is necessary
            BAMServerProfile bamServerProfile = getBAMServerProfile(tenantId, bamServerProfileName);
            AgentConfiguration agentConfiguration = new AgentConfiguration();
            if(bamServerProfile.getKeyStoreLocation() != null && bamServerProfile.getKeyStorePassword() != null) {
                agentConfiguration.setTrustStore(bamServerProfile.getKeyStoreLocation());
                agentConfiguration.setTrustStorePassword(bamServerProfile.getKeyStorePassword());
                System.setProperty("javax.net.ssl.trustStore", bamServerProfile.getKeyStoreLocation());
                System.setProperty("javax.net.ssl.trustStorePassword", bamServerProfile.getKeyStorePassword());
            } else {
                String errMsg = "Key store location not found";
                handleException(errMsg);
            }
            agent = TenantBamAgentHolder.getInstance().createAgent(agentConfiguration);
        }
        return agent;
    }


    private String handleEventStream(DataPublisher publisher, BAMStreamConfiguration stream) throws FaultException{
        String streamName = stream.getName();
        String streamVersion = stream.getVersion();

        try {
            return publisher.findStream(streamName, streamVersion);
        } catch (NoStreamDefinitionExistException e) {
            return defineEventStream(publisher, stream);
        } catch (StreamDefinitionException e) {
            String errorMsg = "Problem with Stream Definition";
            handleException(errorMsg, e);
        } catch (AgentException e) {
            String errorMsg = "Problem using the Agent with data publisher.";
            handleException(errorMsg, e);
        }
        return null;
    }



    private Agent createAgent(int tenantId, String bamServerProfileName) {
        BAMServerProfile bamServerProfile = getBAMServerProfile(tenantId, bamServerProfileName);
        AgentConfiguration agentConfiguration = new AgentConfiguration();
        agentConfiguration.setTrustStore(bamServerProfile.getKeyStoreLocation());
        agentConfiguration.setTrustStorePassword(bamServerProfile.getKeyStorePassword());
        System.setProperty("javax.net.ssl.trustStore", bamServerProfile.getKeyStoreLocation());
        System.setProperty("javax.net.ssl.trustStorePassword", bamServerProfile.getKeyStorePassword());
        return new Agent(agentConfiguration);
    }

    private DataPublisher createDataPublisher(ExtensionContext context, int tenantId,
                                              String bamServerProfileName, Agent agent) throws FaultException {

        DataPublisher dataPublisher = null;
        TenantProcessStore tenantsProcessStore =
                BAMPublisherServiceComponent.getBPELServer().getMultiTenantProcessStore().getTenantsProcessStore(tenantId);

        String processName = context.getProcessModel().getName().toString();

        dataPublisher = (DataPublisher)tenantsProcessStore.getDataPublisher(processName);

        if(dataPublisher == null) {
            BAMServerProfile bamServerProfile = getBAMServerProfile(tenantId, bamServerProfileName);
            try {
                dataPublisher = new DataPublisher(bamServerProfile.getUrl(), bamServerProfile.getUserName(), bamServerProfile.getPassword(), agent);
            } catch (MalformedURLException e) {
                String errorMsg = "Given URLs are incorrect.";
                handleException(errorMsg, e);
            } catch (AgentException e) {
                String errorMsg = "Problem while using the Agent.";
                handleException(errorMsg, e);
            } catch (AuthenticationException e) {
                String errorMsg = "Authentication failed.";
                handleException(errorMsg, e);
            } catch (TransportException e) {
                String errorMsg = "Transport layer problem.";
                handleException(errorMsg, e);
            }
            log.info("Data Publisher Created.");
            if(dataPublisher != null) {
                tenantsProcessStore.addDataPublisher(processName, dataPublisher);
            }
        }

        return dataPublisher;
    }
}



