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

import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricTaskInstance;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bpmn.analytics.publisher.internal.BPMNAnalyticsHolder;
import org.wso2.carbon.bpmn.analytics.publisher.utils.BPMNDataReceiverConfig;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.databridge.agent.DataPublisher;
import org.wso2.carbon.databridge.agent.exception.DataEndpointAgentConfigurationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointAuthenticationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointConfigurationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointException;
import org.wso2.carbon.databridge.commons.exception.TransportException;
import org.wso2.carbon.databridge.commons.utils.DataBridgeCommonsUtils;

import java.util.Date;

public class BPSDataPublisher {

    private static final Log log = LogFactory.getLog(BPSDataPublisher.class);

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
        Integer tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        DataPublisher dataPublisher = BPMNAnalyticsHolder.getInstance().getDataPublisher(tenantId);
        if (dataPublisher != null) {
            dataPublisher.tryPublish(getProcessStreamId(), getMeta(), null, payload);
            if (log.isDebugEnabled()) {
                log.debug("Published BPMN process instance event... " + payload.toString());
            }
        } else {
            log.error("Data publisher is not registered for tenant " + tenantId + ". Events will not be published.");
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
        Integer tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        DataPublisher dataPublisher = BPMNAnalyticsHolder.getInstance().getDataPublisher(tenantId);
        if (dataPublisher != null) {
            dataPublisher.tryPublish(getTaskInstanceStreamId(), getMeta(), null, payload);
            if (log.isDebugEnabled()) {
                log.debug("Published BPMN task instance event... " + payload.toString());
            }
        } else {
            log.error("Data publisher is not registered for tenant " + tenantId + ". Events will not be published.");
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

    public DataPublisher createDataPublisher(BPMNDataReceiverConfig config) {
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
                        "Unable to create data publisher as one or more required BPMNDataReceiverConfig are not configured properly. " +
                                "Check receiverURLsSet, userName, password fields.");
            }
        } else {
            log.warn("BPMNDataReceiverConfig instance is null. Could not create Data publisher.");
        }
        return dataPublisher;
    }
}
