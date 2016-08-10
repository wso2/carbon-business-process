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

package org.wso2.carbon.bpel.analytics.publisher;

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
import org.wso2.carbon.bpel.analytics.publisher.internal.AnalyticsPublisherServiceComponent;
import org.wso2.carbon.bpel.core.ode.integration.config.analytics.AnalyticsKey;
import org.wso2.carbon.bpel.core.ode.integration.config.analytics.AnalyticsServerProfile;
import org.wso2.carbon.bpel.core.ode.integration.config.analytics.AnalyticsStreamConfiguration;
import org.wso2.carbon.bpel.core.ode.integration.store.TenantProcessStore;
import org.wso2.carbon.databridge.agent.DataPublisher;
import org.wso2.carbon.databridge.agent.exception.DataEndpointAgentConfigurationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointAuthenticationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointConfigurationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointException;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.databridge.commons.exception.*;
import org.wso2.carbon.databridge.commons.utils.DataBridgeCommonsUtils;

import javax.xml.namespace.QName;
import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class AnalyticsPublisherExtensionOperation extends AbstractSyncExtensionOperation {
    private static final Log log = LogFactory.getLog(AnalyticsPublisherExtensionOperation.class);

    @Override
    protected void runSync(ExtensionContext extensionContext, Element element)
            throws FaultException {
        String analyticsServerProfileName = element.getAttribute("analyticsServerProfile");
        String streamName = element.getAttribute(AnalyticsPublisherConstants.STREAM_NAME_ATTR);
        String streamVersion = element.getAttribute(AnalyticsPublisherConstants.STREAM_VERSION);
        Integer tenantId = getTenantId(extensionContext);
        AnalyticsStreamConfiguration stream = getEventStream(tenantId, analyticsServerProfileName, streamName, streamVersion);

        if(stream == null) {
            log.debug("Stream configuration is invalid");
            return;
        }
	    DataPublisher dataPublisher = getDataPublisher(extensionContext, tenantId, analyticsServerProfileName);
	    if (dataPublisher == null) {
		    String msg = "Error while creating data publisher";
		    handleException(msg);
	    }
	    String streamId = DataBridgeCommonsUtils.generateStreamId(stream.getName(), stream.getVersion());

	    dataPublisher.tryPublish(streamId, createMetadata(stream, extensionContext, element),
	                             createCorrelationData(stream, extensionContext, element),
	                             createPayloadData(stream, extensionContext, element));
    }

    private Integer getTenantId(ExtensionContext context) {
        DeploymentUnitDir du = new DeploymentUnitDir(new File(context.getDUDir()));
        QName processIdQname = new QName(context.getProcessModel().getQName().getNamespaceURI(),
                context.getProcessModel().getQName().getLocalPart() + "-"
                        + du.getStaticVersion());
        return AnalyticsPublisherServiceComponent.getBPELServer().
                getMultiTenantProcessStore().getTenantId(processIdQname);
    }


    private AnalyticsServerProfile getAnalyticsServerProfile(int tenantId, String analyticsServerProfileName) {
        TenantProcessStore tenantsProcessStore = AnalyticsPublisherServiceComponent.getBPELServer().
                getMultiTenantProcessStore().getTenantsProcessStore(tenantId);
        return tenantsProcessStore.getAnalyticsServerProfile(analyticsServerProfileName);
    }

    private AnalyticsStreamConfiguration getEventStream(int tenantId, String analyticsServerProfileName,
                                                  String streamName, String streamVersion) {
        AnalyticsServerProfile analyticsServerProfile = getAnalyticsServerProfile(tenantId, analyticsServerProfileName);
        if(null == analyticsServerProfile) {
            String errMsg = "AnalyticsServerProfile not found for stream name and version " + streamName + " " + streamVersion;
            log.error(errMsg);
            return null;
        }
        return analyticsServerProfile.getAnalyticsStreamConfiguration(streamName, streamVersion);
    }

    private void handleException(String errMsg, Throwable t) throws FaultException {
        log.error(errMsg, t);
        throw new FaultException(AnalyticsPublisherConstants.ANALYTICS_FAULT, errMsg, t);
    }

    private void handleException(String errMsg) throws FaultException {
        log.error(errMsg);
        throw new FaultException(AnalyticsPublisherConstants.ANALYTICS_FAULT, errMsg);
    }

    private Object[] createCorrelationData(AnalyticsStreamConfiguration stream, ExtensionContext context, Element element)
            throws FaultException {
        List<AnalyticsKey> correlationAnalyticsKeyList = stream.getCorrelationAnalyticsKeyList();
        int objectListSize = correlationAnalyticsKeyList.size();
        Object[] dataArray = new Object[objectListSize];
//        dataArray[0] = context.getInternalInstance().getPid().toString();
        int startIndex = 0;
        fillDataArray(dataArray, correlationAnalyticsKeyList, startIndex, context, element);
        return dataArray;
    }

    private Object[] createMetadata(AnalyticsStreamConfiguration stream, ExtensionContext context, Element element)
            throws FaultException {
        List<AnalyticsKey> metaAnalyticsKeyList = stream.getMetaAnalyticsKeyList();

        int objectListSize = metaAnalyticsKeyList.size();
        Object[] dataArray = new Object[objectListSize];
//        dataArray[0] = getTenantId(context);
//        dataArray[1] = context.getProcessModel().getQName().toString();
        int startIndex = 0;
        fillDataArray(dataArray, metaAnalyticsKeyList, startIndex, context, element);
        return dataArray;
    }

    private Object[] createPayloadData(AnalyticsStreamConfiguration stream, ExtensionContext context, Element element)
            throws FaultException {
        List<AnalyticsKey> payloadAnalyticsKeyList = stream.getPayloadAnalyticsKeyList();
        int objectListSize = payloadAnalyticsKeyList.size();
        Object[] dataArray = new Object[objectListSize];
        int startIndex = 0;
        fillDataArray(dataArray, payloadAnalyticsKeyList, startIndex, context, element);
        return dataArray;
    }

    private void fillDataArray(Object[] dataArray, List<AnalyticsKey> payloadAnalyticsKeyList, int startIndex,
                               ExtensionContext context, Element element) throws FaultException {
        for (int i = 0; i < payloadAnalyticsKeyList.size(); i++) {
            AnalyticsKey analyticsKey = payloadAnalyticsKeyList.get(i);
            if (analyticsKey.getExpression() != null) {
                String expression = evaluateXPathExpression(context, analyticsKey.getExpression(), element);
                convertDataType(dataArray, (i + startIndex) , analyticsKey, expression);

            } else if (analyticsKey.getVariable() != null && analyticsKey.getPart() == null) {
                if (analyticsKey.getQuery() == null) {
                    String variable = context.readVariable(analyticsKey.getVariable()).getTextContent();

                    convertDataType(dataArray, ( i + startIndex) , analyticsKey, variable);
                    /* simple types should be specified for here */

                } else {
                    String errMsg = "This functionality is currently not supported";
                    log.error(errMsg);
                    handleException(errMsg);
                }
            } else if (analyticsKey.getVariable() != null && analyticsKey.getPart() != null) {
                NodeList childNodes = context.readVariable(analyticsKey.getVariable()).getChildNodes();
                String result = null;
                String part = analyticsKey.getPart();
                for(int j=0; j < childNodes.getLength(); j++) {
                    Node item = childNodes.item(j);
                    if(item != null && item.getNodeType() == Node.ELEMENT_NODE && item.getLocalName().equals(part)) {
                        /* remove the payload part */
                        result = DOMUtils.domToString(DOMUtils.getFirstChildElement(item));
                    }
                }
                convertDataType(dataArray, ( i + startIndex) , analyticsKey, result);
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
                /** for analytics publishing to work, there should only be a single node here */
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

	private DataPublisher getDataPublisher(ExtensionContext context, int tenantId, String analyticsServerProfileName)
			throws FaultException {

        DataPublisher dataPublisher = null;
        TenantProcessStore tenantsProcessStore =
                AnalyticsPublisherServiceComponent.getBPELServer().getMultiTenantProcessStore().getTenantsProcessStore(tenantId);

        String processName = context.getProcessModel().getName().toString();

		dataPublisher = (DataPublisher) tenantsProcessStore.getDataPublisher(processName);

		// Create new DataPublisher if not created already.
		if(dataPublisher == null) {
            AnalyticsServerProfile analyticsServerProfile = getAnalyticsServerProfile(tenantId, analyticsServerProfileName);
            try {
	            dataPublisher =
			            new DataPublisher(analyticsServerProfile.getType(), analyticsServerProfile.getReceiverURLSet(),
			                              analyticsServerProfile.getAuthURLSet(), analyticsServerProfile.getUserName(),
			                              analyticsServerProfile.getPassword());
            } catch (TransportException e) {
                String errorMsg = "Transport layer problem.";
                handleException(errorMsg, e);
            } catch (DataEndpointAuthenticationException e) {
	            String errorMsg = "Data endpoint authentication problem.";
	            handleException(errorMsg, e);
            } catch (DataEndpointAgentConfigurationException e) {
	            String errorMsg = "Data endpoint agent configuration problem.";
	            handleException(errorMsg, e);
            } catch (DataEndpointException e) {
	            String errorMsg = "Data endpoint problem.";
	            handleException(errorMsg, e);
            } catch (DataEndpointConfigurationException e) {
	            String errorMsg = "Data endpoint configuration problem.";
	            handleException(errorMsg, e);
            }
	        if (log.isDebugEnabled()) {
		        log.debug("Data Publisher Created : " + analyticsServerProfile.toString());
	        }
            if(dataPublisher != null) {
                tenantsProcessStore.addDataPublisher(processName, dataPublisher);
            }
        }
        return dataPublisher;
    }

    public void convertDataType(Object[] dataArray, int index, AnalyticsKey key, String value) {
        AnalyticsKey.AnalyticsKeyDataType dataType = key.getDataType();
        switch (dataType) {
            case  INTEGER :
                dataArray[index] = Integer.valueOf(value);
                break;
            case LONG:
                dataArray[index] = Long.valueOf(value);
                break;
            case DOUBLE:
                dataArray[index] = Double.valueOf(value);
                break;
            case FLOAT:
                dataArray[index] = Float.valueOf(value);
                break;
            case BOOL:
                dataArray[index] = Boolean.valueOf(value);
                break;
            case STRING:
                dataArray[index] = String.valueOf(value);
                break;
            default:
                dataArray[index] = String.valueOf(value);
                break;
        }
    }
}



