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
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.pvm.PvmEvent;
import org.activiti.engine.impl.task.TaskDefinition;
import org.activiti.engine.parse.BpmnParseHandler;
import org.activiti.engine.repository.ProcessDefinition;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bpmn.analytics.publisher.handlers.ProcessParseHandler;
import org.wso2.carbon.bpmn.analytics.publisher.handlers.TaskParseHandler;
import org.wso2.carbon.bpmn.analytics.publisher.internal.BPMNAnalyticsHolder;
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
import org.wso2.carbon.utils.CarbonUtils;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class BPMNDataPublisher {

    private static final Log log = LogFactory.getLog(BPMNDataPublisher.class);

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
                    }
                }
            }
        }

        if (analyticsEnabled) {
            if (receiverURLSet != null && username != null && password != null) {

                // Configure datapublisher to be used by all data publishing listeners
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
}
