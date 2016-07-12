/*
 * Copyright 2005-2015 WSO2, Inc. (http://wso2.com)
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

import org.activiti.engine.RepositoryService;
import org.activiti.engine.delegate.TaskListener;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.persistence.entity.VariableInstance;
import org.activiti.engine.impl.persistence.entity.VariableInstanceEntity;
import org.activiti.engine.impl.pvm.PvmEvent;
import org.activiti.engine.impl.task.TaskDefinition;
import org.activiti.engine.parse.BpmnParseHandler;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONObject;
import org.wso2.carbon.bpmn.analytics.publisher.handlers.ProcessKPIParseHandler;
import org.wso2.carbon.bpmn.analytics.publisher.handlers.ProcessParseHandler;
import org.wso2.carbon.bpmn.analytics.publisher.handlers.TaskParseHandler;
import org.wso2.carbon.bpmn.analytics.publisher.internal.BPMNAnalyticsHolder;
import org.wso2.carbon.bpmn.analytics.publisher.listeners.ProcessTerminationKPIListener;
import org.wso2.carbon.bpmn.analytics.publisher.listeners.ProcessTerminationListener;
import org.wso2.carbon.bpmn.analytics.publisher.listeners.TaskCompletionListener;
import org.wso2.carbon.bpmn.core.BPMNConstants;
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
import org.wso2.carbon.utils.CarbonUtils;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class BPMNDataPublisher {

    private static final Log log = LogFactory.getLog(BPMNDataPublisher.class);

    private DataPublisher dataPublisher;
    private Map<String, String[][]> processVariablesMap = new HashMap<String, String[][]>();
    private Map<String, String> kpiStreamIdMap =  new HashMap<String, String>();

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
            log.debug("Start to Publish BPMN process instance event... " + payload.toString());
        }
        if (dataPublisher != null) {
            dataPublisher.tryPublish(getProcessStreamId(), getMeta(), null, payload);
            if (log.isDebugEnabled()) {
                log.debug("Published BPMN process instance event... " + payload.toString());
            }
        } else {
            log.error("Data publisher is not registered. Events will not be published.");
        }
    }

    public void publishTaskEvent(HistoricTaskInstance taskInstance) {
        long endTime = System.currentTimeMillis();
        Object[] payload = new Object[]{
                taskInstance.getTaskDefinitionKey(),
                taskInstance.getId(),
                taskInstance.getProcessInstanceId(),
                taskInstance.getCreateTime().toString(),
                taskInstance.getStartTime().toString(),
                new Date(endTime).toString(),
                (endTime - taskInstance.getCreateTime().getTime()),
                taskInstance.getAssignee()
        };

        if (log.isDebugEnabled()) {
            log.debug("Start to Publish BPMN task instance event... " + payload.toString());
        }
        if (dataPublisher != null) {
            dataPublisher.tryPublish(getTaskInstanceStreamId(), getMeta(), null, payload);
            if (log.isDebugEnabled()) {
                log.debug("Published BPMN task instance event... " + payload.toString());
            }
        } else {
            log.error("Data publisher is not registered. Events will not be published.");
        }
    }

    public void configure() throws IOException, XMLStreamException, DataEndpointAuthenticationException,
            DataEndpointAgentConfigurationException, TransportException, DataEndpointException, DataEndpointConfigurationException {

        boolean analyticsEnabled = false;
        boolean kpiAnalyticsEnabled = false;
        String receiverURLSet = "";
        String authURLSet = null;
        String type = null;
        String username = "";
        String password = "";

        // Read analytics configuration from activiti.xml file
        String carbonConfigDirPath = CarbonUtils.getCarbonConfigDirPath();
        String activitiConfigPath = carbonConfigDirPath + File.separator + BPMNConstants.ACTIVITI_CONFIGURATION_FILE_NAME;
        if (log.isDebugEnabled()) {
            log.debug("Reading BPMN analytics configuration from " + activitiConfigPath);
        }
        File configFile = new File(activitiConfigPath);
        String configContent = FileUtils.readFileToString(configFile);
        OMElement configElement = AXIOMUtil.stringToOM(configContent);
        Iterator beans = configElement.getChildrenWithName(new QName("http://www.springframework.org/schema/beans", "bean"));
        while (beans.hasNext()) {
            OMElement bean = (OMElement) beans.next();
            String beanId = bean.getAttributeValue(new QName(null, "id"));
            if (beanId.equals(AnalyticsPublisherConstants.ANALYTICS_CONFIG_ELEMENT)) {
                Iterator beanProps = bean.getChildrenWithName(new QName("http://www.springframework.org/schema/beans", "property"));
                while (beanProps.hasNext()) {
                    OMElement beanProp = (OMElement) beanProps.next();
                    if (beanProp.getAttributeValue(new QName(null, "name")).equals(AnalyticsPublisherConstants.PUBLISHER_RECEIVER_URL_SET_PROPERTY)) {
                        receiverURLSet = beanProp.getAttributeValue(new QName(null, "value"));
                    } else if (beanProp.getAttributeValue(new QName(null, "name")).equals(AnalyticsPublisherConstants.PUBLISHER_USER_NAME_PROPERTY)) {
                        username = beanProp.getAttributeValue(new QName(null, "value"));
                    } else if (beanProp.getAttributeValue(new QName(null, "name")).equals(AnalyticsPublisherConstants.PUBLISHER_PASSWORD_PROPERTY)) {
                        password = beanProp.getAttributeValue(new QName(null, "value"));
                    } else if (beanProp.getAttributeValue(new QName(null, "name")).equals(AnalyticsPublisherConstants.PUBLISHER_AUTH_URL_SET_PROPERTY)) {
                        authURLSet = beanProp.getAttributeValue(new QName(null, "value"));
                    } else if (beanProp.getAttributeValue(new QName(null, "name")).equals(AnalyticsPublisherConstants.PUBLISHER_TYPE_PROPERTY)) {
                        type = beanProp.getAttributeValue(new QName(null, "value"));
                    } else if (beanProp.getAttributeValue(new QName(null, "name")).equals(AnalyticsPublisherConstants.PUBLISHER_ENABLED_PROPERTY)) {
                        String analyticsEnabledValue = beanProp.getAttributeValue(new QName(null, "value"));
                        if ("true".equalsIgnoreCase(analyticsEnabledValue)) {
                            analyticsEnabled = true;
                        }
                    } else if (beanProp.getAttributeValue(new QName(null, "name")).equals(AnalyticsPublisherConstants.KPI_PUBLISHER_ENABLED_PROPERTY)) {
                        String kpiAalyticsEnabledValue = beanProp.getAttributeValue(new QName(null, "value"));
                        if ("true".equalsIgnoreCase(kpiAalyticsEnabledValue)) {
                            kpiAnalyticsEnabled = true;
                        }
                    }
                }
            }
        }

        if (analyticsEnabled) {
            configGenericVariablesPublishing(receiverURLSet, username, password, authURLSet, type);
        }

        if (kpiAnalyticsEnabled) {
            configProcessVariablesPublishing(receiverURLSet, username, password, authURLSet, type);
        }
    }

    /**
     *
     * @param receiverURLSet
     * @param username
     * @param password
     * @param authURLSet
     * @param type
     * @throws DataEndpointAuthenticationException
     * @throws DataEndpointAgentConfigurationException
     * @throws TransportException
     * @throws DataEndpointException
     * @throws DataEndpointConfigurationException
     */
    private void configGenericVariablesPublishing(String receiverURLSet, String username, String password,
            String authURLSet, String type)
            throws DataEndpointAuthenticationException, DataEndpointAgentConfigurationException, TransportException,
            DataEndpointException, DataEndpointConfigurationException {
            if (receiverURLSet != null && username != null && password != null) {

                // Configure data publisher to be used by all (generic variables) data publishing listeners
                if (log.isDebugEnabled()) {
                    log.debug("Creating BPMN analytics data publisher with Receiver URL: " +
                            receiverURLSet + ", Auth URL: " + authURLSet + " and Data publisher type: " + type);
                }
                dataPublisher = new DataPublisher(type, receiverURLSet, authURLSet, username, password);
                BPMNEngineService engineService = BPMNAnalyticsHolder.getInstance().getBpmnEngineService();

                // Attach data publishing listeners to all existing processes
                if (log.isDebugEnabled()) {
                    log.debug("Attaching data publishing listeners to already deployed processes...");
                }
                RepositoryService repositoryService = engineService.getProcessEngine().getRepositoryService();
                List<ProcessDefinition> processDefinitions = repositoryService.createProcessDefinitionQuery().list();
                for (ProcessDefinition processDefinition : processDefinitions) {
                    // Process definition returned by the query does not contain all details such as task definitions. Therefore, we have to fetch the complete process definition
                    // from the repository again.
                    ProcessDefinition completeProcessDefinition = repositoryService.getProcessDefinition(processDefinition.getId());
                    if (completeProcessDefinition instanceof ProcessDefinitionEntity) {
                        ProcessDefinitionEntity processDefinitionEntity = (ProcessDefinitionEntity) completeProcessDefinition;
                        processDefinitionEntity.addExecutionListener(PvmEvent.EVENTNAME_END, new ProcessTerminationListener());

                        Map<String, TaskDefinition> tasks = processDefinitionEntity.getTaskDefinitions();
                        for (TaskDefinition task : tasks.values()) {
                            task.addTaskListener(TaskListener.EVENTNAME_COMPLETE, new TaskCompletionListener());
                        }
                    }
                }

                // Configure parse handlers, which attaches data publishing listeners to new processes
                if (log.isDebugEnabled()) {
                    log.debug("Associating parse handlers for processes and tasks, so that data publishing listeners will be attached to new processes.");
                }
                ProcessEngineConfigurationImpl engineConfig = (ProcessEngineConfigurationImpl) engineService.getProcessEngine().getProcessEngineConfiguration();
                if (engineConfig.getPostBpmnParseHandlers() == null) {
                    engineConfig.setPostBpmnParseHandlers(new ArrayList<BpmnParseHandler>());
                }
                engineConfig.getPostBpmnParseHandlers().add(new ProcessParseHandler());
                engineConfig.getPostBpmnParseHandlers().add(new TaskParseHandler());
                engineConfig.getBpmnDeployer().getBpmnParser().getBpmnParserHandlers().addHandler(new ProcessParseHandler());
                engineConfig.getBpmnDeployer().getBpmnParser().getBpmnParserHandlers().addHandler(new TaskParseHandler());

            } else {
                log.warn("Required fields for data publisher are not configured. Receiver URLs, username and password are mandatory. Data publishing will not be enabled.");
            }
    }

    /**
     * @param receiverURLSet
     * @param username
     * @param password
     * @param authURLSet
     * @param type
     * @throws DataEndpointAuthenticationException
     * @throws DataEndpointAgentConfigurationException
     * @throws TransportException
     * @throws DataEndpointException
     * @throws DataEndpointConfigurationException
     */
    private void configProcessVariablesPublishing(String receiverURLSet, String username, String password,
            String authURLSet, String type)
            throws DataEndpointAuthenticationException, DataEndpointAgentConfigurationException, TransportException,
            DataEndpointException, DataEndpointConfigurationException {
        if (receiverURLSet != null && username != null && password != null) {
            // Configure datapublisher to be used by all KPI data publishing listeners
            if (log.isDebugEnabled()) {
                log.debug("Creating BPMN analytics data publisher (KPI) with Receiver URL: " +
                        receiverURLSet + ", Auth URL: " + authURLSet + " and Data publisher type: " + type);
            }
            dataPublisher = new DataPublisher(type, receiverURLSet, authURLSet, username, password);
            BPMNEngineService engineService = BPMNAnalyticsHolder.getInstance().getBpmnEngineService();

            // Attach data publishing listeners to all existing processes
            if (log.isDebugEnabled()) {
                log.debug("Attaching data publishing (KPI) listeners to already deployed processes...");
            }
            RepositoryService repositoryService = engineService.getProcessEngine().getRepositoryService();
            List<ProcessDefinition> processDefinitions = repositoryService.createProcessDefinitionQuery().list();
            for (ProcessDefinition processDefinition : processDefinitions) {
                if (processDefinition instanceof ProcessDefinitionEntity) {
                    ProcessDefinitionEntity processDefinitionEntity = (ProcessDefinitionEntity) processDefinition;
                    processDefinitionEntity
                            .addExecutionListener(PvmEvent.EVENTNAME_END, new ProcessTerminationKPIListener());
                }
            }

            // Configure parse handlers, which attaches KPI data publishing listeners to new processes
            if (log.isDebugEnabled()) {
                log.debug("Associating parse handlers for processes and tasks, so that KPI data publishing "
                        + "listeners will be attached to new processes.");
            }
            ProcessEngineConfigurationImpl engineConfig = (ProcessEngineConfigurationImpl) engineService
                    .getProcessEngine().getProcessEngineConfiguration();
            if (engineConfig.getPostBpmnParseHandlers() == null) {
                engineConfig.setPostBpmnParseHandlers(new ArrayList<BpmnParseHandler>());
            }
            engineConfig.getPostBpmnParseHandlers().add(new ProcessKPIParseHandler());
            engineConfig.getBpmnDeployer().getBpmnParser().getBpmnParserHandlers()
                    .addHandler(new ProcessKPIParseHandler());
        } else {
            log.warn("Required fields for data publisher are not configured. Receiver URLs, username and password are "
                    + "mandatory. Data publishing will not be enabled.");
        }
    }

    /**
     * Get meta data of the instances to publish them.
     *
     * @return new object array
     */
    private Object[] getMeta() {
        return new Object[]{};
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

    /**
     * Publish the separate event to the DAS for the process variables for the called process instance
     *
     * @param processInstance
     */
    public void publishKPIvariableData(ProcessInstance processInstance) throws BPMNDataPublisherException {
        String processDefinitionId = processInstance.getProcessDefinitionId();
        String processInstanceId = processInstance.getId();
        String eventStreamId;
        Object[] payload = new Object[0];
        try {
            //get a list of names of variables which are configured for analytics from registry for that process, if
            // not already taken from registry
            String[][] configedProcessVariables;
            if (processVariablesMap.get(processDefinitionId) == null) {
                JSONObject kpiConfig = getKPIConfiguration(processDefinitionId);
                //do not publish the KPI event if DAS configurations are not done by the PC
                if (kpiConfig == null) {
                    return;
                }

                /* Keeps configed process variabe data as a JSON. Example value:
                [{"isAnalyzeData":false,"name":"size","isDrillDownData":false,"type":"int"},
                {"isAnalyzeData":false,"name":"status","isDrillDownData":false,"type":"string"},
                {"isAnalyzeData":false,"name":"pizzaTopping","isDrillDownData":false,"type":"string"},
                {"isAnalyzeData":false,"name":"amount","isDrillDownData":false,"type":"int"},
                {"isAnalyzeData":"false","name":"processInstanceId","isDrillDownData":"false","type":"string"}]
                 */
                JSONArray configedProcVarsJson = kpiConfig
                        .getJSONArray(AnalyticsPublisherConstants.PROCESS_VARIABLES_JSON_ENTRY_NAME);

                int variableCount = configedProcVarsJson.length();
                configedProcessVariables = new String[variableCount][2];

                for (int i = 0; i < variableCount; i++) {
                    configedProcessVariables[i][0] = configedProcVarsJson.getJSONObject(i).getString("name");
                    configedProcessVariables[i][1] = configedProcVarsJson.getJSONObject(i).getString("type");
                }

                processVariablesMap.put(processDefinitionId, configedProcessVariables);
                eventStreamId = kpiConfig.getString("eventStreamId");
                kpiStreamIdMap.put(processDefinitionId, eventStreamId);

            } else { //if the process variables are already taken, get them from the Map
                configedProcessVariables = processVariablesMap.get(processDefinitionId);
                eventStreamId = kpiStreamIdMap.get(processDefinitionId);
            }

            Map<String, VariableInstance> variableInstances = ((ExecutionEntity) processInstance)
                    .getVariableInstances();
            payload = new Object[configedProcessVariables.length];

            for (int i = 0; i < configedProcessVariables.length - 1; i++) {
                String varName = configedProcessVariables[i][0];
                String varType = configedProcessVariables[i][1];

                Object varValue = variableInstances.get(varName).getValue();

                switch (varType) {
                case "int":
                    if (varValue == null) {
                        payload[i] = -1;
                    } else {
                        payload[i] = Integer.parseInt((String) varValue);
                    }
                    break;
                case "float":
                    if (varValue == null) {
                        payload[i] = -1;
                    } else {
                        payload[i] = Float.parseFloat((String) varValue);
                    }
                    break;
                case "long":
                    if (varValue == null) {
                        payload[i] = -1;
                    } else {
                        payload[i] = Long.parseLong((String) varValue);
                    }
                    break;
                case "double":
                    if (varValue == null) {
                        payload[i] = -1;
                    } else {
                        payload[i] = Double.parseDouble((String) varValue);
                    }
                    break;
                case "string":
                    if (varValue == null) {
                        payload[i] = "";
                    } else {
                        payload[i] = varValue;
                    }
                    break;
                case "bool":
                    if (varValue == null) {
                        payload[i] = false;
                    } else {
                        payload[i] = Boolean.parseBoolean((String) varValue);
                    }
                    break;
                default:
                    log.warn("Configured process variable type is not a WSO2 DAS applicable type for the process:"
                            + processDefinitionId);
                    if (varValue == null) {
                        payload[i] = "";
                    } else {
                        payload[i] = varValue;
                    }
                    break;
                }
            }

            //set process instance id as the last payload variable value
            payload[configedProcessVariables.length-1] = processInstanceId;

            boolean dataPublishingSuccess = dataPublisher.tryPublish(eventStreamId, getMeta(), null, payload);
            if (dataPublishingSuccess) {
                if (log.isDebugEnabled()) {
                    log.debug("Published BPMN process instance KPI event...  Process Instance Id :" + processInstanceId
                            + ", Process Definition Id:" + processDefinitionId + ", Published Event's Payload Data :"
                            + payload.toString());
                }
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Failed Publishing BPMN process instance KPI event... Process Instance Id :" +
                            processInstanceId + ", Process Definition Id:" + processDefinitionId
                            + ", Published Event's Payload Data :" + payload.toString());
                }
            }
        } catch (RegistryException | RuntimeException e) {
            String strMsg = "Failed Publishing BPMN process instance KPI event... Process Instance Id :" +
                    processInstanceId + ", Process Definition Id:" + processDefinitionId
                    + ", Published Event's Payload Data :" + payload.toString();
            throw new BPMNDataPublisherException(strMsg, e);
        }
    }

    /**
     * Get DAS config details of given certain process which are configured for analytics from the config registry
     *
     * @param processDefinitionId
     * @return KPI configuration details in JSON format. Ex:<p>
     * {"eventReceiverName":"process1_77_process_receiver",
     * "processDefinitionId":"manualTaskProcess111:1:22509","pcProcessId":"process1:77",
     * "eventStreamNickName":"process1_77_process_stream","eventStreamDescription":"This is the event stream
     * generated to configure process analytics with DAS, for the processprocess1_77","eventStreamVersion":"1.0.0",
     * "eventStreamId":"process1_77_process_stream:1.0.0","processVariables":[{"isAnalyzeData":false,"name":"size",
     * "isDrillDownData":false,"type":"int"},{"isAnalyzeData":false,"name":"pizzaTopping","isDrillDownData":false,
     * "type":"string"},{"isAnalyzeData":false,"name":"amount","isDrillDownData":false,"type":"int"},
     * "eventStreamName":"process1_77_process_stream"}
     * @throws RegistryException
     */
    public JSONObject getKPIConfiguration(String processDefinitionId) throws RegistryException {
        String resourcePath = AnalyticsPublisherConstants.REG_PATH_BPMN_ANALYTICS + processDefinitionId + "/"
                + AnalyticsPublisherConstants.ANALYTICS_CONFIG_FILE_NAME;
        try {
            RegistryService registryService = BPMNAnalyticsHolder.getInstance().getRegistryService();
            Registry configRegistry = registryService.getConfigSystemRegistry();

            if (configRegistry.resourceExists(resourcePath)) {
                Resource processRegistryResource = configRegistry.get(resourcePath);
                String dasConfigDetailsJSONStr = new String((byte[]) processRegistryResource.getContent(),
                        StandardCharsets.UTF_8);
                return new JSONObject(dasConfigDetailsJSONStr);
            } else {
                return null;
            }

        } catch (RegistryException e) {
            String errMsg =
                    "Error in Getting DAS config details of given process definition id :" + processDefinitionId
                            + " from the BPS Config registry-" + resourcePath;
            throw new RegistryException(errMsg, e);
        }
    }
}