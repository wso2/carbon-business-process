/**
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.bpmn.analytics.publisher;

import org.activiti.engine.HistoryService;
import org.activiti.engine.history.HistoricVariableInstance;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.wso2.carbon.bpmn.analytics.publisher.internal.BPMNAnalyticsHolder;
import org.wso2.carbon.bpmn.analytics.publisher.models.BPMNProcessInstance;
import org.wso2.carbon.bpmn.analytics.publisher.models.BPMNTaskInstance;
import org.wso2.carbon.bpmn.analytics.publisher.utils.AnalyticsPublishServiceUtils;
import org.wso2.carbon.bpmn.analytics.publisher.utils.BPMNDataReceiverConfig;
import org.wso2.carbon.bpmn.core.BPMNServerHolder;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.context.RegistryType;
import org.wso2.carbon.databridge.agent.DataPublisher;
import org.wso2.carbon.databridge.agent.exception.DataEndpointAgentConfigurationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointAuthenticationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointConfigurationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointException;
import org.wso2.carbon.databridge.commons.exception.TransportException;
import org.wso2.carbon.databridge.commons.utils.DataBridgeCommonsUtils;
import org.wso2.carbon.registry.api.Registry;
import org.wso2.carbon.registry.api.RegistryException;
import org.wso2.carbon.registry.api.Resource;
import org.wso2.carbon.registry.core.service.RegistryService;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * AnalyticsPublisher uses to publish events to the data receiver in the data-bridge
 * Two streams are defined. One is for the processes and the other is for the tasks.
 * process_stream(processDefinitionId, processInstanceId, startActivityId, startUserId, startTime, endTime, duration, tenantId)
 * task_stream(taskDefinitionKey, taskInstanceId, processInstanceId, createTime, startTime, endTime, duration, assignee)
 */
public class AnalyticsPublisher {
    private static final Log log = LogFactory.getLog(AnalyticsPublisher.class);

    private String processInstanceStreamId;
    private String taskInstanceStreamId;
    private DataPublisher dataPublisher;
    private AnalyticsPublishServiceUtils analyticsPublishServiceUtils;

    private ExecutorService analyticsExecutorService;
    private Map<String, JSONArray> mapOfProcessVariablesLists;
    private Map<String, String> mapOfKPIstreamIdsOFProcesses;

    /**
     * Initialize the objects for AnalyticsPublisher
     */
    public boolean initialize(BPMNDataReceiverConfig config) throws Exception {
        try {
            if (BPMNDataReceiverConfig.isDASPublisherActivated()) {

                analyticsExecutorService = Executors.newSingleThreadExecutor();

                //RegistryUtils.setTrustStoreSystemProperties();
                //dataPublisher = createDataPublisher(config);
                processInstanceStreamId = getProcessStreamId();
                taskInstanceStreamId = getTaskInstanceStreamId();
                analyticsPublishServiceUtils = new AnalyticsPublishServiceUtils();
                mapOfProcessVariablesLists = new HashMap<String, JSONArray>();
                mapOfKPIstreamIdsOFProcesses = new HashMap<String, String>();
                int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
                String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
                Registry registry = BPMNAnalyticsHolder.getInstance().getRegistryService()
                        .getGovernanceSystemRegistry();
                startPollingForInstances(tenantId, tenantDomain, registry, config);

            } else {
                if (log.isDebugEnabled()) {
                    log.debug("BPMN Data Publisher is not activated for server.");
                }
                // Nothing to here.
                return true;
            }
        } catch (IOException | RegistryException | XMLStreamException e) {
            throw new Exception("Data publisher objects initialization error.", e);
        }
        return false;
    }

    /**
     * Set thread local privileges to polling thread
     */
    private void startPollingForInstances(final int tenantId, final String tenantDomain, final Registry registry,
            final BPMNDataReceiverConfig config) {
        if (log.isDebugEnabled()) {
            log.debug("Run startPollingForInstances method... " + tenantId + ", " + tenantDomain + ", " + registry);
        }
        analyticsExecutorService.execute(new Runnable() {
            @Override public void run() {
                try {
                    PrivilegedCarbonContext.startTenantFlow();
                    PrivilegedCarbonContext privilegedCarbonContext = PrivilegedCarbonContext
                            .getThreadLocalCarbonContext();
                    privilegedCarbonContext.setTenantId(tenantId, true);
                    privilegedCarbonContext.setTenantDomain(tenantDomain, true);
                    privilegedCarbonContext.setRegistry(RegistryType.SYSTEM_GOVERNANCE, registry);
                    doPollingForInstances(config);
                } finally {
                    PrivilegedCarbonContext.endTenantFlow();
                }
            }
        });
    }

    /**
     * Polling for Process instances
     */
    private void doPollingForInstances(BPMNDataReceiverConfig config) {
        if (log.isDebugEnabled()) {
            log.debug("Start polling for process instances...");
        }
        try {
            Thread.sleep(AnalyticsPublisherConstants.DELAY);
            while (true) {
                if (config.isDataPublisherEnabled()) {
                    if (dataPublisher == null) {
                        log.info("Re-initializing data publisher for tenant ID : " + config.getTenantID());
                        dataPublisher = createDataPublisher(config);
                    }
                    // Still data publisher can be null, due to miss configuration.
                    if (dataPublisher != null) {
                        BPMNProcessInstance[] bpmnProcessInstances = analyticsPublishServiceUtils
                                .getCompletedProcessInstances();
                        if (log.isDebugEnabled()) {
                            log.debug("publishing data to the receiver urlset:" + config.getReceiverURLsSet());
                        }
                        if (bpmnProcessInstances != null && bpmnProcessInstances.length > 0) {
                            for (BPMNProcessInstance instance : bpmnProcessInstances) {
                                publishBPMNProcessInstanceGenericEvent(instance);
                                publishBPMNProcessInstanceProcVariablesEvent(instance);
                            }
                        }
                        //        String processDefinitionId = instance.getProcessDefinitionId();

                        BPMNTaskInstance[] bpmnTaskInstances = analyticsPublishServiceUtils.getCompletedTaskInstances();
                        if (bpmnTaskInstances != null && bpmnTaskInstances.length > 0) {
                            for (BPMNTaskInstance instance : bpmnTaskInstances) {
                                publishBPMNTaskInstanceEvent(instance);
                            }
                        }
                        Thread.sleep(AnalyticsPublisherConstants.REPEATEDLY_DELAY);
                    } else {
                        log.warn("Can't initializing data publisher for tenant ID : " + config.getTenantID()
                                + "Next attempt will be in " +
                                AnalyticsPublisherConstants.NEXT_CHECK_DELAY
                                + " ms. Or Disable data publisher for this tenant.");
                        Thread.sleep(AnalyticsPublisherConstants.NEXT_CHECK_DELAY);
                    }
                } else {
                    dataPublisher = null;
                    if (log.isDebugEnabled()) {
                        log.debug("Analytics publisher is disabled for this tenant. Next check will be in " +
                                AnalyticsPublisherConstants.NEXT_CHECK_DELAY + " ms.");
                    }
                    Thread.sleep(AnalyticsPublisherConstants.NEXT_CHECK_DELAY);
                }
            }

        } catch (InterruptedException e) {
            //nothing to do
        }
    }

    /**
     * Publish process instance as events to the data receiver
     *
     * @param bpmnProcessInstance BPMN process instance to retrieve the data for payload param of data publisher's publish method
     */
    private void publishBPMNProcessInstanceGenericEvent(BPMNProcessInstance bpmnProcessInstance) {

        Object[] payload = new Object[] { bpmnProcessInstance.getProcessDefinitionId(),
                bpmnProcessInstance.getInstanceId(), bpmnProcessInstance.getStartActivityId(),
                bpmnProcessInstance.getStartUserId(), bpmnProcessInstance.getStartTime().toString(),
                bpmnProcessInstance.getEndTime().toString(), bpmnProcessInstance.getDuration(),
                bpmnProcessInstance.getTenantId() };
        if (log.isDebugEnabled()) {
            log.debug("Start to Publish BPMN process instance event... " + payload.toString());
        }
        dataPublisher.tryPublish(getProcessStreamId(), getMeta(), null, payload);
        if (log.isDebugEnabled()) {
            log.debug("Published BPMN process instance event... " + payload.toString());
        }
    }

    private void publishBPMNProcessInstanceProcVariablesEvent(BPMNProcessInstance bpmnProcessInstance) {
        String processDefinitionId = bpmnProcessInstance.getProcessDefinitionId();
        String processInstanceId = bpmnProcessInstance.getInstanceId();
        String eventStreamId;
        Object[] payload = new Object[0];//initialize with a 0 length array just to avoid possible null pointer exception in the catch block
        try {
            //get a list of names of variables which are configured for analytics from registry for that process, if not already taken
            JSONArray configedProcessVariablesListJsonArray = null;
            Map<String, ?> variableVals = new HashMap();
            if (mapOfProcessVariablesLists.get(processDefinitionId) == null) {
                JSONObject dasConfigDetailsSavedInBPSregJOb = null;

                dasConfigDetailsSavedInBPSregJOb = getDasConfigDetailsSavedInBPSreg(processDefinitionId);

                configedProcessVariablesListJsonArray = dasConfigDetailsSavedInBPSregJOb
                        .getJSONArray(AnalyticsPublisherConstants.PROCESS_VARIABLES_JSON_ENTRY_NAME);
                //configedProcessVariablesListJsonArray = getProcessVariablesList(dasConfigDetailsSavedInBPSregJOb); // >> [{"name":"status","type":"string"},{"name":"amount","type":"int"},{"name":"pizzaTopping","type":"string"},{"name":"size","type":"int"},{"name":"processInstanceId","type":"string"}]}

                mapOfProcessVariablesLists.put(processDefinitionId, configedProcessVariablesListJsonArray);

                String streamId = dasConfigDetailsSavedInBPSregJOb.getString("eventStreamId");
                mapOfKPIstreamIdsOFProcesses.put(processDefinitionId, streamId);
            }

            Map<String, Object> hitoricVariablesMap = new HashMap();
            eventStreamId = mapOfKPIstreamIdsOFProcesses.get(processDefinitionId);

            HistoryService historyService = BPMNServerHolder.getInstance().getEngine().getHistoryService();
            final List<HistoricVariableInstance> historicVariableInstancesList = historyService
                    .createHistoricVariableInstanceQuery().processInstanceId(processInstanceId).orderByVariableName()
                    .desc().list();
            for (HistoricVariableInstance historicVariableInstance : historicVariableInstancesList) {
                hitoricVariablesMap
                        .put(historicVariableInstance.getVariableName(), historicVariableInstance.getValue());
            }

            payload = new Object[configedProcessVariablesListJsonArray.length()];

            for (int i = 0; i < configedProcessVariablesListJsonArray.length() - 1; i++) {
                JSONObject job = configedProcessVariablesListJsonArray.getJSONObject(i);
                String varName = job.getString("name");
                String varType = job.getString("type");

                Object varValue = hitoricVariablesMap.get(varName);
                if (varValue != null) {
                    switch (varType) {
                    case "string":
                        payload[i] = (String) varValue;
                        break;
                    case "int":
                        payload[i] = Integer.parseInt((String) varValue);
                        break;
                    case "float":
                        payload[i] = Float.parseFloat((String) varValue);
                        break;
                    case "boolean":
                        payload[i] = Boolean.parseBoolean((String) varValue);
                        break;
                    }
                }
            }

            //set process instance id as the final payload variable value
            payload[payload.length - 1] = processInstanceId;

            boolean dataPublishingSuccess = dataPublisher.tryPublish(eventStreamId, getMeta(), null, payload);
            if (dataPublishingSuccess) {
                if (log.isDebugEnabled()) {
                    log.debug("Published BPMN process instance event...  Process Instance Id :" + processInstanceId
                            + ", Process Definition Id:" + processDefinitionId + ", Published Event's Payload Data :"
                            + payload.toString());
                }
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Failed Publishing BPMN process instance event... Process Instance Id :" + processInstanceId
                            + ", Process Definition Id:" + processDefinitionId + ", Published Event's Payload Data :"
                            + payload.toString());
                }
            }
        } catch (RegistryException e) {
            String strMsg="Failed Publishing BPMN process instance event... Process Instance Id :" + processInstanceId
                    + ", Process Definition Id:" + processDefinitionId + ", Published Event's Payload Data :"
                    + payload.toString();
            log.error(strMsg,e);;
        }
    }

    /**
     * Publish task instance as events to the data receiver
     *
     * @param bpmnTaskInstance BPMN task instance to retrieve the data for payload param of data publisher's publish method
     */
    private void publishBPMNTaskInstanceEvent(BPMNTaskInstance bpmnTaskInstance) {
        Object[] payload = new Object[] { bpmnTaskInstance.getTaskDefinitionKey(), bpmnTaskInstance.getTaskInstanceId(),
                bpmnTaskInstance.getProcessInstanceId(), bpmnTaskInstance.getCreateTime().toString(),
                bpmnTaskInstance.getStartTime().toString(), bpmnTaskInstance.getEndTime().toString(),
                bpmnTaskInstance.getDurationInMills(), bpmnTaskInstance.getAssignee()

        };
        if (log.isDebugEnabled()) {
            log.debug("Start to Publish BPMN task instance event... " + payload.toString());
        }
        dataPublisher.tryPublish(taskInstanceStreamId, getMeta(), null, payload);
        if (log.isDebugEnabled()) {
            log.debug("Published BPMN task instance event... " + payload.toString());
        }
    }

    // Note: From DAS 3.0.0 onwards, it is no longer supporting on fly stream definition creation. User must create stream definition before publishing it.
    //    /**
    //     * Retrieve stream id to uniquely identify the stream for process instances
    //     *
    //     * @return the stream id of process instance stream
    //     * @throws MalformedStreamDefinitionException
    //     * @throws DifferentStreamDefinitionAlreadyDefinedException
    //     * @throws StreamDefinitionException
    //     * @throws AgentException
    //     */
    //    private String getBPMNProcessInstanceStreamId() throws MalformedStreamDefinitionException,
    //            DifferentStreamDefinitionAlreadyDefinedException,
    //            StreamDefinitionException,
    //            AgentException {
    //        StreamDefinition streamDefinition =
    //                new StreamDefinition(AnalyticsPublisherConstants.PROCESS_STREAM_NAME,
    //                        AnalyticsPublisherConstants.STREAM_VERSION);
    //        streamDefinition.setDescription(AnalyticsPublisherConstants.PROCESS_STREAM_DESCRIPTION);
    //        streamDefinition.addPayloadData(AnalyticsPublisherConstants.PROCESS_DEFINITION_ID,
    //                AttributeType.STRING);
    //        streamDefinition.addPayloadData(AnalyticsPublisherConstants.PROCESS_INSTANCE_ID,
    //                AttributeType.STRING);
    //        streamDefinition.addPayloadData(AnalyticsPublisherConstants.START_ACTIVITY_ID,
    //                AttributeType.STRING);
    //        streamDefinition.addPayloadData(AnalyticsPublisherConstants.START_USER_ID, AttributeType.STRING);
    //        streamDefinition
    //                .addPayloadData(AnalyticsPublisherConstants.START_TIME, AttributeType.STRING);
    //        streamDefinition.addPayloadData(AnalyticsPublisherConstants.END_TIME, AttributeType.STRING);
    //        streamDefinition.addPayloadData(AnalyticsPublisherConstants.DURATION, AttributeType.LONG);
    //        streamDefinition.addPayloadData(AnalyticsPublisherConstants.TENANT_ID, AttributeType.STRING);
    //        return dataPublisher.defineStream(streamDefinition);
    //    }
    //
    //    /**
    //     * Retrieve stream id to uniquely identify the stream for task instances
    //     *
    //     * @return the stream id of task instance stream
    //     * @throws MalformedStreamDefinitionException
    //     * @throws DifferentStreamDefinitionAlreadyDefinedException
    //     * @throws StreamDefinitionException
    //     * @throws AgentException
    //     */
    //    private String getBPMNTaskInstanceStreamId() throws MalformedStreamDefinitionException,
    //            DifferentStreamDefinitionAlreadyDefinedException,
    //            StreamDefinitionException, AgentException {
    //        StreamDefinition streamDefinition =
    //                new StreamDefinition(AnalyticsPublisherConstants.TASK_STREAM_NAME,
    //                        AnalyticsPublisherConstants.STREAM_VERSION);
    //        streamDefinition.setDescription(AnalyticsPublisherConstants.TASK_STREAM_DESCRIPTION);
    //        streamDefinition.addPayloadData(AnalyticsPublisherConstants.TASK_DEFINITION_ID,
    //                AttributeType.STRING);
    //        streamDefinition
    //                .addPayloadData(AnalyticsPublisherConstants.TASK_INSTANCE_ID, AttributeType.STRING);
    //        streamDefinition
    //                .addPayloadData(AnalyticsPublisherConstants.PROCESS_INSTANCE_ID, AttributeType.STRING);
    //        streamDefinition.addPayloadData(AnalyticsPublisherConstants.CREATE_TIME, AttributeType.STRING);
    //        streamDefinition
    //                .addPayloadData(AnalyticsPublisherConstants.START_TIME, AttributeType.STRING);
    //        streamDefinition.addPayloadData(AnalyticsPublisherConstants.END_TIME, AttributeType.STRING);
    //        streamDefinition.addPayloadData(AnalyticsPublisherConstants.DURATION, AttributeType.LONG);
    //        streamDefinition.addPayloadData(AnalyticsPublisherConstants.ASSIGNEE, AttributeType.STRING);
    //        return dataPublisher.defineStream(streamDefinition);
    //    }

    /**
     * Create a data publisher to publish the data as events
     *
     * @return DataPublisher object
     * @throws MalformedURLException
     */
    private DataPublisher createDataPublisher(BPMNDataReceiverConfig config) {
        DataPublisher dataPublisher = null;
        if (config != null) {
            String type = config.getType();
            String receiverURLsSet = config.getReceiverURLsSet();
            String authURLsSet = config.getAuthURLsSet();
            String userName = config.getUserName();
            String password = config.getPassword();
            if (log.isDebugEnabled()) {
                log.debug("BPMNDataReceiverConfig { type :" + type + " , username " + userName + " , receiverURLsSet " +
                        receiverURLsSet + " , authURLsSet " + authURLsSet + " }");
            }

            if (receiverURLsSet != null && userName != null && password != null) {
                try {
                    dataPublisher = new DataPublisher(type, receiverURLsSet, authURLsSet, userName, password);
                } catch (DataEndpointAgentConfigurationException | DataEndpointException | DataEndpointConfigurationException | DataEndpointAuthenticationException | TransportException e) {
                    log.error("Error while creating data publisher. ", e);
                    return null;
                }
            } else {
                log.warn(
                        "Unable to create data publisher as one or more required BPMNDataReceiverConfig are not configured properly. "
                                + "Check receiverURLsSet, userName, password fields.");
            }
        } else {
            log.warn("BPMNDataReceiverConfig instance is null. Could not create Data publisher.");
        }
        return dataPublisher;
    }

    /**
     * Stop the data publisher
     */
    public boolean stopDataPublisher() {
        Integer tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        if (analyticsExecutorService != null && !analyticsExecutorService.isShutdown()) {

            log.info("Shutting down analytics executor service for tenant : " + tenantId);
            analyticsExecutorService.shutdownNow();

            for (int i = 0; i < 5; i++) {
                if (analyticsExecutorService.isShutdown()) {
                    log.info("analytics executor service shutdowned for tenant : " + tenantId);
                    return true;
                } else {
                    try {
                        Thread.sleep(AnalyticsPublisherConstants.REPEATEDLY_DELAY);
                    } catch (InterruptedException e) {
                        // Nothing to do.
                    }
                }
            }
        } else {
            log.info("analytics executor service not running for tenant : " + tenantId);
            return true;
        }
        log.warn("Unable to shutdown analytics executor service for tenant : " + tenantId);
        return false;
    }

    /**
     * Get meta data of the instances to publish them.
     *
     * @return new object array
     */
    private Object[] getMeta() {
        return new Object[] {};
    }

    /**
     * Get StreamId for Task Instances.
     *
     * @return StreamId
     */
    private String getTaskInstanceStreamId() {
        return DataBridgeCommonsUtils.generateStreamId(AnalyticsPublisherConstants.TASK_STREAM_NAME,
                AnalyticsPublisherConstants.STREAM_VERSION);
    }

    /**
     * Get StreamId for processes.
     *
     * @return StreamId
     */
    private String getProcessStreamId() {
        return DataBridgeCommonsUtils.generateStreamId(AnalyticsPublisherConstants.PROCESS_STREAM_NAME,
                AnalyticsPublisherConstants.STREAM_VERSION);
    }

/*//get also the process stream id and set it to a instance variable in this class
    //set proc variables too to a instance variable Map
    public JSONArray getProcessVariablesList(JSONObject dasConfigDetailsJObj) throws JSONException{
            JSONArray processVarJObArray = dasConfigDetailsJObj.getJSONArray(AnalyticsPublisherConstants.PROCESS_VARIABLES_JSON_ENTRY_NAME);
            //JSONArray variableArray = new JSONArray(processVariablesElement.getTextContent());
            return processVarJObArray;

    }

    public String getConfigedStreamId(JSONObject dasConfigDetailsJObj){
        return dasConfigDetailsJObj.getString("eventStreamId");
    }*/

    public JSONObject getDasConfigDetailsSavedInBPSreg(String processDefinitionId) throws RegistryException {
        String resourcePath = AnalyticsPublisherConstants.REG_PATH_BPMN_ANALYTICS + processDefinitionId + "/"
                + AnalyticsPublisherConstants.ANALYTICS_CONFIG_FILE_NAME;
        try {
            Integer tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
            RegistryService registryService = BPMNAnalyticsHolder.getInstance().getRegistryService();
            Registry configRegistry = registryService.getConfigSystemRegistry(tenantId);

            Resource processRegistryResource = configRegistry.get(resourcePath);
            log.info("content:" + processRegistryResource.getContent().toString());
            String dasConfigDetailsJSONStr = new String((byte[]) processRegistryResource.getContent());
            return new JSONObject(dasConfigDetailsJSONStr);

        } catch (RegistryException e) {
            String errMsg =
                    "Error in Getting das config details of given certain process definition id :" + processDefinitionId
                            + " from the BPS Config registry-" + resourcePath;
            throw new RegistryException(errMsg, e);
        }
    }
}