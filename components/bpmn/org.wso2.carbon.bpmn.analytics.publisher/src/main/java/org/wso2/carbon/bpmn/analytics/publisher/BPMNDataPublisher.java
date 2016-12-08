/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.bpmn.analytics.publisher;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricActivityInstanceQuery;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.persistence.entity.VariableInstance;
import org.activiti.engine.impl.pvm.PvmEvent;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.task.TaskDefinition;
import org.activiti.engine.parse.BpmnParseHandler;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bpmn.analytics.publisher.config.BPSAnalyticsConfiguration;
import org.wso2.carbon.bpmn.analytics.publisher.handlers.ProcessKPIParseHandler;
import org.wso2.carbon.bpmn.analytics.publisher.handlers.ProcessParseHandler;
import org.wso2.carbon.bpmn.analytics.publisher.handlers.TaskParseHandler;
import org.wso2.carbon.bpmn.analytics.publisher.internal.BPMNAnalyticsHolder;
import org.wso2.carbon.bpmn.analytics.publisher.listeners.ProcessTerminationKPIListener;
import org.wso2.carbon.bpmn.analytics.publisher.listeners.ProcessTerminationListener;
import org.wso2.carbon.bpmn.analytics.publisher.listeners.TaskCompletionListener;
import org.wso2.carbon.bpmn.core.BPMNEngineService;
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
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class BPMNDataPublisher {

    private static final Log log = LogFactory.getLog(BPMNDataPublisher.class);
    private static final char PROC_VAR_VALUE_AVAILABLE = '1';
    private static final char PROC_VAR_VALUE_UNAVAILABLE = '0';

    private DataPublisher dataPublisher;

    public void publishProcessEvent(HistoricProcessInstance processInstance) {
        long endTime = System.currentTimeMillis();
        Object[] payload = new Object[]{
                processInstance.getProcessDefinitionId(),
                processInstance.getId(),
                processInstance.getStartActivityId(),
                processInstance.getStartUserId(),
                processInstance.getStartTime().toString(),
                new Date(endTime).toString(),
                (endTime - processInstance.getStartTime().getTime()),
                processInstance.getTenantId()
        };

        if (log.isDebugEnabled()) {
            log.debug("Starting to Publish BPMN process instance event. Process Definition ID:" + processInstance
                    .getProcessDefinitionId() + ", Process Instance ID:" + processInstance.getId());
        }
        if (dataPublisher != null) {
            dataPublisher.tryPublish(getProcessStreamId(), getMeta(), null, payload);
            if (log.isDebugEnabled()) {
                log.debug("Published BPMN process instance event. Process Definition ID:" + processInstance
                        .getProcessDefinitionId() + ", Process Instance ID:" + processInstance.getId());
            }
        } else {
            log.error("Data publisher is not registered. Events will not be published.");
        }
    }

    public void publishTaskEvent(DelegateTask delegateTask) {
        long endTime = System.currentTimeMillis();
        Object[] payload = new Object[]{
                delegateTask.getTaskDefinitionKey(),
                delegateTask.getId(),
                delegateTask.getProcessDefinitionId(),
                delegateTask.getProcessInstanceId(),
                delegateTask.getCreateTime().toString(),
                delegateTask.getCreateTime().toString(),
                new Date(endTime).toString(),
                (endTime - delegateTask.getCreateTime().getTime()),
                delegateTask.getAssignee()
        };

        if (log.isDebugEnabled()) {
            log.debug("Starting to Publish BPMN task instance event. Process Definition ID: " + delegateTask
                    .getProcessDefinitionId() + ", Process Instance ID:" + delegateTask.getProcessInstanceId() +
                    ", Task ID:" + delegateTask.getId());
        }
        if (dataPublisher != null) {
            dataPublisher.tryPublish(getTaskInstanceStreamId(), getMeta(), null, payload);
            if (log.isDebugEnabled()) {
                log.debug("Published BPMN task instance event. Process Definition ID: " + delegateTask
                        .getProcessDefinitionId() + ", Process Instance ID:" + delegateTask.getProcessInstanceId() +
                        ", Task ID:" + delegateTask.getId());
            }
        } else {
            log.error("Data publisher is not registered. Events will not be published.");
        }
    }

    /**
     * @param activityInstanceQuery
     */
    public void publishServiceTaskEvent(HistoricActivityInstanceQuery activityInstanceQuery) {

        if (log.isDebugEnabled()) {
            log.debug("Start to Publish BPMN service task instance event... ");
        }
        List<HistoricActivityInstance> historicActivityInstances = activityInstanceQuery.list();
        for (HistoricActivityInstance instance : historicActivityInstances) {
            if (instance.getActivityType().equals(AnalyticsPublisherConstants.SERVICE_TASK)) {
                Object[] payload = new Object[]{
                        //Service task definition Id
                        instance.getActivityId(),
                        //task instance Id
                        instance.getId(),
                        //process definition id
                        instance.getProcessDefinitionId(),
                        //process instance Id
                        instance.getProcessInstanceId(),
                        //task created time
                        instance.getStartTime().toString(),
                        //task started time
                        instance.getStartTime().toString(),
                        //task end time
                        instance.getEndTime().toString(),
                        //task duration
                        instance.getDurationInMillis(),
                        //task assignee - NA as this is a service task
                        "NA"
                };
                if (dataPublisher != null) {
                    dataPublisher.tryPublish(getServiceTaskInstanceStreamId(), getMeta(), null, payload);
                    if (log.isDebugEnabled()) {
                        log.debug("Published BPMN service task instance event... Service task definition Id:" + instance
                                .getActivityId() + ", task instance Id:" + instance.getId() + ", process definition "
                                + "id:" + instance.getProcessDefinitionId() + ", process instance Id:" + instance
                                .getProcessInstanceId());
                    }
                } else {
                    log.error("Data publisher is not registered. Events will not be published.");
                }
            }
        }
    }

    public void configure() throws IOException, XMLStreamException, DataEndpointAuthenticationException,
            DataEndpointAgentConfigurationException, TransportException, DataEndpointException,
            DataEndpointConfigurationException {
        // Read bps analytics configuration
        BPSAnalyticsConfiguration bpsAnalyticsConfiguration = BPMNAnalyticsHolder.getInstance().getBPSAnalyticsServer
                ().getBPSAnalyticsConfiguration();

        if (bpsAnalyticsConfiguration.isBpmnDataPublishingEnabled() || bpsAnalyticsConfiguration
                .isBpmnKPIDataPublishingEnabled()) {
            configDataPublishing(bpsAnalyticsConfiguration.getAnalyticsReceiverURLSet(),
                    bpsAnalyticsConfiguration.getAnalyticsServerUsername(), bpsAnalyticsConfiguration
                            .getAnalyticsServerPassword(), bpsAnalyticsConfiguration.getAnalyticsAuthURLSet()
                    , bpsAnalyticsConfiguration.getBpmnAnalyticsPublisherType(), bpsAnalyticsConfiguration
                            .isBpmnAsyncDataPublishingEnabled(), bpsAnalyticsConfiguration
                            .isBpmnDataPublishingEnabled(), bpsAnalyticsConfiguration.isBpmnKPIDataPublishingEnabled());
        }
    }

    /**
     * Configure for data publishing to DAS for analytics
     *
     * @param receiverURLSet             Analytics receiver's url
     * @param username                   Analytics server's username
     * @param password                   Analytics server's password
     * @param authURLSet                 Analytics Auth URL set
     * @param type                       Bpmn Analytics Publisher Type
     * @param asyncDataPublishingEnabled is async Data Publishing Enabled
     * @param genericAnalyticsEnabled    is generic Analytics Enabled
     * @param kpiAnalyticsEnabled        is KPI Analytics Enabled
     * @throws DataEndpointAuthenticationException
     * @throws DataEndpointAgentConfigurationException
     * @throws TransportException
     * @throws DataEndpointException
     * @throws DataEndpointConfigurationException
     */
    void configDataPublishing(String receiverURLSet, String username, String password, String authURLSet, String type,
                              boolean asyncDataPublishingEnabled, boolean genericAnalyticsEnabled, boolean
                                      kpiAnalyticsEnabled)
            throws DataEndpointAuthenticationException, DataEndpointAgentConfigurationException, TransportException,
            DataEndpointException, DataEndpointConfigurationException {
        if (receiverURLSet != null && username != null && password != null) {

            // Configure data publisher to be used by all data publishing listeners
            if (log.isDebugEnabled()) {
                log.debug("Creating BPMN analytics data publisher with Receiver URL: " +
                        receiverURLSet + ", Auth URL: " + authURLSet + " and Data publisher type: " + type);
            }
            dataPublisher = new DataPublisher(type, receiverURLSet, authURLSet, username, password);
            BPMNAnalyticsHolder.getInstance().setAsyncDataPublishingEnabled(asyncDataPublishingEnabled);
            BPMNEngineService engineService = BPMNAnalyticsHolder.getInstance().getBpmnEngineService();

            // Attach data publishing listeners to all existing processes
            if (log.isDebugEnabled()) {
                log.debug("Attaching data publishing listeners to already deployed processes...");
            }
            RepositoryService repositoryService = engineService.getProcessEngine().getRepositoryService();
            List<ProcessDefinition> processDefinitions = repositoryService.createProcessDefinitionQuery().list();
            for (ProcessDefinition processDefinition : processDefinitions) {
                // Process definition returned by the query does not contain all details such as task definitions.
                // And it is also not the actual process definition, but a copy of it, so attaching listners to
                // them is useless. Therefore, we have to fetch the complete process definition from the repository
                // again.
                ProcessDefinition completeProcessDefinition = repositoryService.
                        getProcessDefinition(processDefinition.getId());
                if (completeProcessDefinition instanceof ProcessDefinitionEntity) {
                    ProcessDefinitionEntity processDefinitionEntity = (ProcessDefinitionEntity)
                            completeProcessDefinition;
                    if (genericAnalyticsEnabled) {
                        processDefinitionEntity
                                .addExecutionListener(PvmEvent.EVENTNAME_END, new ProcessTerminationListener());
                    }
                    if (kpiAnalyticsEnabled) {
                        processDefinitionEntity
                                .addExecutionListener(PvmEvent.EVENTNAME_END, new ProcessTerminationKPIListener());
                    }
                    Map<String, TaskDefinition> tasks = processDefinitionEntity.getTaskDefinitions();
                    List<ActivityImpl> activities = processDefinitionEntity.getActivities();
                    for (ActivityImpl activity : activities) {
                        if (activity.getProperty("type").toString().equalsIgnoreCase("usertask")) {
                            tasks.get(activity.getId())
                                    .addTaskListener(TaskListener.EVENTNAME_COMPLETE, new TaskCompletionListener());
                        }
                        // We are publishing analytics data of service tasks in process termination ATM.

                        // else if(activity.getProperty("type").toString().equalsIgnoreCase("servicetask")){
                        //       activity.addExecutionListener(PvmEvent.EVENTNAME_END,new
                        // ServiceTaskCompletionListener());
                        // }
                    }
                }
            }

            // Configure parse handlers, which attaches data publishing listeners to new processes
            if (log.isDebugEnabled()) {
                log.debug("Associating parse handlers for processes and tasks, so that data publishing listeners "
                        + "will be attached to new processes.");
            }
            ProcessEngineConfigurationImpl engineConfig = (ProcessEngineConfigurationImpl) engineService.
                    getProcessEngine().getProcessEngineConfiguration();
            if (engineConfig.getPostBpmnParseHandlers() == null) {
                engineConfig.setPostBpmnParseHandlers(new ArrayList<BpmnParseHandler>());
            }
            if (genericAnalyticsEnabled) {
                engineConfig.getPostBpmnParseHandlers().add(new ProcessParseHandler());
                engineConfig.getPostBpmnParseHandlers().add(new TaskParseHandler());
                engineConfig.getBpmnDeployer().getBpmnParser().getBpmnParserHandlers().
                        addHandler(new ProcessParseHandler());
                engineConfig.getBpmnDeployer().getBpmnParser().getBpmnParserHandlers().
                        addHandler(new TaskParseHandler());
            }
            if (kpiAnalyticsEnabled) {
                engineConfig.getPostBpmnParseHandlers().add(new ProcessKPIParseHandler());
                engineConfig.getBpmnDeployer().getBpmnParser().getBpmnParserHandlers()
                        .addHandler(new ProcessKPIParseHandler());
            }
        } else {
            log.warn("Required fields for data publisher are not configured. Receiver URLs, username and password "
                    + "are mandatory. Data publishing will not be enabled.");
        }
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
     * Get StreamId for Service Task instances
     *
     * @return StreamId
     */
    private String getServiceTaskInstanceStreamId() {
        return DataBridgeCommonsUtils.generateStreamId(AnalyticsPublisherConstants.SERVICE_TASK_STREAM_NAME,
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

    /**
     * Publish the separate event to the DAS for the process variables for the called process instance
     *
     * @param processInstance process instance object
     * @throws BPMNDataPublisherException
     * @throws IOException
     */
    public void publishKPIvariableData(ProcessInstance processInstance) throws BPMNDataPublisherException, IOException {
        String processDefinitionId = processInstance.getProcessDefinitionId();
        String processInstanceId = processInstance.getId();
        String eventStreamId;
        Object[] payload = new Object[0];
        try {
            JsonNode kpiConfig = getKPIConfiguration(processDefinitionId);
            // do not publish the KPI event if DAS configurations are not done by the PC
            if (kpiConfig == null) {
                return;
            }

            JsonNode configedProcessVariables = kpiConfig.withArray(AnalyticsPublisherConstants
                    .PROCESS_VARIABLES_JSON_ENTRY_NAME);

            if (log.isDebugEnabled()) {
                log.debug(
                        "Publishing Process Variables (KPI) for the process instance " + processInstanceId + " of the "
                                + "process : " + processDefinitionId);
            }

            /* Keeps configured process variable data as a JSON. These variables are sent as payload data to DAS.
            Example value:
            [{"name":"processInstanceId","type":"string","isAnalyzeData":"false","isDrillDownData":"false"},
            {"name":"valuesAvailability","type":"string","isAnalyzeData":"false","isDrillDownData":"false"},
            {"name":"custid","type":"string","isAnalyzeData":false,"isDrillDownData":false},
            {"name":"amount","type":"long","isAnalyzeData":false,"isDrillDownData":false},
            {"name":"confirm","type":"bool","isAnalyzeData":false,"isDrillDownData":false}]
            */
            JsonNode fieldsConfigedForStreamPayload = kpiConfig
                    .withArray(AnalyticsPublisherConstants.PROCESS_VARIABLES_JSON_ENTRY_NAME);

            eventStreamId = kpiConfig.get("eventStreamId").textValue();
            Map<String, VariableInstance> variableInstances = ((ExecutionEntity) processInstance)
                    .getVariableInstances();
            payload = new Object[fieldsConfigedForStreamPayload.size()];

            //set process instance id as the 1st payload variable value
            payload[0] = processInstanceId;

            //availability of values for each process variable is represented by this char array as '1' (value available)
            // or '0' (not available) in respective array index
            char[] valueAvailabiliy = new char[fieldsConfigedForStreamPayload.size() - 2];

            for (int i = 2; i < configedProcessVariables.size(); i++) {
                String varName = (fieldsConfigedForStreamPayload.get(i)).get("name").textValue();
                String varType = (fieldsConfigedForStreamPayload.get(i)).get("type").textValue();

                Object varValue = variableInstances.get(varName).getValue();

                switch (varType) {
                    case "int":
                        if (varValue == null) {
                            payload[i] = 0;
                            valueAvailabiliy[i - 2] = PROC_VAR_VALUE_UNAVAILABLE;
                        } else {
                            payload[i] = Integer.parseInt(varValue.toString());
                            valueAvailabiliy[i - 2] = PROC_VAR_VALUE_AVAILABLE;
                        }
                        break;
                    case "float":
                        if (varValue == null) {
                            payload[i] = 0;
                            valueAvailabiliy[i - 2] = PROC_VAR_VALUE_UNAVAILABLE;
                        } else {
                            payload[i] = Float.parseFloat(varValue.toString());
                            valueAvailabiliy[i - 2] = PROC_VAR_VALUE_AVAILABLE;
                        }
                        break;
                    case "long":
                        if (varValue == null) {
                            payload[i] = 0;
                            valueAvailabiliy[i - 2] = PROC_VAR_VALUE_UNAVAILABLE;
                        } else {
                            payload[i] = Long.parseLong(varValue.toString());
                            valueAvailabiliy[i - 2] = PROC_VAR_VALUE_AVAILABLE;
                        }
                        break;
                    case "double":
                        if (varValue == null) {
                            payload[i] = 0;
                            valueAvailabiliy[i - 2] = PROC_VAR_VALUE_UNAVAILABLE;
                        } else {
                            payload[i] = Double.parseDouble(varValue.toString());
                            valueAvailabiliy[i - 2] = PROC_VAR_VALUE_AVAILABLE;
                        }
                        break;
                    case "string":
                        if (varValue == null) {
                            payload[i] = "NA";
                            valueAvailabiliy[i - 2] = PROC_VAR_VALUE_UNAVAILABLE;
                        } else {
                            payload[i] = varValue;
                            valueAvailabiliy[i - 2] = PROC_VAR_VALUE_AVAILABLE;
                        }
                        break;
                    case "bool":
                        if (varValue == null) {
                            payload[i] = false;
                            valueAvailabiliy[i - 2] = PROC_VAR_VALUE_UNAVAILABLE;
                        } else {
                            payload[i] = Boolean.parseBoolean(varValue.toString());
                            valueAvailabiliy[i - 2] = PROC_VAR_VALUE_AVAILABLE;
                        }
                        break;
                    default:
                        String errMsg = "Configured process variable type: \"" + varType + "\" of the variable \"" +
                                varName
                                + "\" is not a WSO2 DAS applicable type for the process:" + processDefinitionId;
                        throw new BPMNDataPublisherException(errMsg);
                }
            }

            //set meta data string value representing availability of values for each process variable
            payload[1] = String.valueOf(valueAvailabiliy);

            boolean dataPublishingSuccess = dataPublisher.tryPublish(eventStreamId, getMeta(), null, payload);
            if (dataPublishingSuccess) {
                if (log.isDebugEnabled()) {
                    log.debug("Published BPMN process instance KPI event...  Process Instance Id :" + processInstanceId
                            + ", Process Definition Id:" + processDefinitionId);
                }
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Failed Publishing BPMN process instance KPI event... Process Instance Id :" +
                            processInstanceId + ", Process Definition Id:" + processDefinitionId);
                }
            }
        } catch (RegistryException | RuntimeException | BPMNDataPublisherException e) {
            String strMsg = "Failed Publishing BPMN process instance KPI event... Process Instance Id :" +
                    processInstanceId + ", Process Definition Id:" + processDefinitionId;
            throw new BPMNDataPublisherException(strMsg, e);
        }
    }

    /**
     * Get DAS config details of given certain process which are configured for analytics from the config registry
     *
     * @param processDefinitionId Process definition ID
     * @return KPI configuration details in JSON format. Ex:<p>
     * {"processDefinitionId":"myProcess3:1:32518","eventStreamName":"t_666_process_stream","eventStreamVersion":"1.0.0"
     * ,"eventStreamDescription":"This is the event stream generated to configure process analytics with DAS, for the
     * processt_666","eventStreamNickName":"t_666_process_stream","eventStreamId":"t_666_process_stream:1.0.0",
     * "eventReceiverName":"t_666_process_receiver","pcProcessId":"t:666",
     * "processVariables":[{"name":"processInstanceId","type":"string","isAnalyzeData":"false",
     * "isDrillDownData":"false"}
     * ,{"name":"valuesAvailability","type":"string","isAnalyzeData":"false","isDrillDownData":"false"}
     * ,{"name":"custid","type":"string","isAnalyzeData":false,"isDrillDownData":false}
     * ,{"name":"amount","type":"long","isAnalyzeData":false,"isDrillDownData":false}
     * ,{"name":"confirm","type":"bool","isAnalyzeData":false,"isDrillDownData":false}]}
     * @throws RegistryException
     */
    public JsonNode getKPIConfiguration(String processDefinitionId) throws RegistryException, IOException {
        String resourcePath = AnalyticsPublisherConstants.REG_PATH_BPMN_ANALYTICS + processDefinitionId + "/"
                + AnalyticsPublisherConstants.ANALYTICS_CONFIG_FILE_NAME;
        try {
            RegistryService registryService = BPMNAnalyticsHolder.getInstance().getRegistryService();
            Registry configRegistry = registryService.getConfigSystemRegistry();

            if (configRegistry.resourceExists(resourcePath)) {
                Resource processRegistryResource = configRegistry.get(resourcePath);
                String dasConfigDetailsJSONStr = new String((byte[]) processRegistryResource.getContent(),
                        StandardCharsets.UTF_8);
                ObjectMapper objectMapper = new ObjectMapper();
                return objectMapper.readTree(dasConfigDetailsJSONStr);
            } else {
                return null;
            }

        } catch (RegistryException e) {
            String errMsg = "Error in Getting DAS config details of given process definition id :" + processDefinitionId
                    + " from the BPS Config registry-" + resourcePath;
            throw new RegistryException(errMsg, e);
        }
    }
}