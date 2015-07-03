/*
 * Copyright (c) , WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.bpmn.analytics.publisher;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bpmn.analytics.publisher.models.BPMNProcessInstance;
import org.wso2.carbon.bpmn.analytics.publisher.models.BPMNTaskInstance;
import org.wso2.carbon.bpmn.analytics.publisher.utils.BPMNAdminConfig;
import org.wso2.carbon.bpmn.core.BPMNServerHolder;
import org.wso2.carbon.databridge.agent.thrift.DataPublisher;
import org.wso2.carbon.databridge.agent.thrift.exception.AgentException;
import org.wso2.carbon.databridge.commons.AttributeType;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.databridge.commons.exception.*;
import org.wso2.carbon.registry.api.RegistryException;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.utils.NetworkUtils;

import java.io.File;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.util.concurrent.Executors;

/**
 * AnalyticsPublisher uses to publish events to the data receiver in data-bridge
 */
public class AnalyticsPublisher {
	private static Log log = LogFactory.getLog(AnalyticsPublisher.class);

	private String processInstanceStreamId;
	private String taskInstanceStreamId;
	private DataPublisher dataPublisher;
	private AnalyticsPublishServiceUtils analyticsPublishServiceUtils;

	/**
	 * Initialize the objects for AnalyticsPublisher
	 */
	public void initialize() {
		try {
			RegistryUtils.setTrustStoreSystemProperties();
			dataPublisher = createDataPublisher();
			processInstanceStreamId = getBPMNProcessInstanceStreamId();
			taskInstanceStreamId = getBPMNTaskInstanceStreamId();
			analyticsPublishServiceUtils = new AnalyticsPublishServiceUtils();
			startPollingForProcessInstances();
			startPollingForTaskInstances();
		} catch (MalformedURLException | AgentException | AuthenticationException | TransportException |
				DifferentStreamDefinitionAlreadyDefinedException | StreamDefinitionException |
				MalformedStreamDefinitionException | UserStoreException | RegistryException | SocketException e) {
			String errMsg = "Data publisher objects initialization error.";
			log.error(errMsg, e);
		}
	}

	/**
	 * Polling for process instances
	 */
	private void startPollingForProcessInstances() {

		BPMNServerHolder.getInstance().getExecutorService().execute(new Runnable() {
			@Override public void run() {
				try {
					while (true) {
						BPMNProcessInstance[] bpmnProcessInstances =
								analyticsPublishServiceUtils.getCompletedProcessInstances();
						if (bpmnProcessInstances != null) {
							for (BPMNProcessInstance instance : bpmnProcessInstances) {
								long startTime = System.currentTimeMillis();
								publishBPMNProcessInstanceEvent(instance);
								long elapsedTime = System.currentTimeMillis() - startTime;
								try {
									Thread.sleep(elapsedTime);
								} catch (InterruptedException e) {
									String errMsg =
											"Interrupted exception in polling thread for BPMN process instances.";
									log.error(errMsg, e);
								}
							}
						}
					}
				} catch (AgentException e) {
					String errMsg = "Agent exception in polling thread for BPMN process instances.";
					log.error(errMsg, e);
				}
			}
		});
	}

	/**
	 * Polling for task instances
	 */
	private void startPollingForTaskInstances() {
		BPMNServerHolder.getInstance().getExecutorService().execute(new Runnable() {
			@Override public void run() {
				try {
					while (true) {
						BPMNTaskInstance[] bpmnTaskInstances =
								analyticsPublishServiceUtils.getCompletedTasks();
						if (bpmnTaskInstances != null) {
							for (BPMNTaskInstance instance : bpmnTaskInstances) {
								long startTime = System.currentTimeMillis();
								publishBPMNTaskInstanceEvent(instance);
								long elapsedTime = System.currentTimeMillis() - startTime;
								try {
									Thread.sleep(elapsedTime);
								} catch (InterruptedException e) {
									String errMsg =
											"Interrupted exception in polling thread for BPMN task instances.";
									log.error(errMsg, e);
								}
							}
						}
					}
				} catch (AgentException e) {
					String errMsg = "Agent exception in polling thread for BPMN task instances.";
					log.error(errMsg, e);
				}
			}
		});
	}

	/**
	 * Publish process instance as events to the data receiver
	 *
	 * @param bpmnProcessInstance BPMN process instance to retrieve the data for payload param of data publisher's publish method
	 * @throws AgentException
	 */
	private void publishBPMNProcessInstanceEvent(BPMNProcessInstance bpmnProcessInstance)
			throws AgentException {
		Object[] payload = new Object[] { bpmnProcessInstance.getProcessDefinitionId(),
		                                  bpmnProcessInstance.getInstanceId(),
		                                  bpmnProcessInstance.getStartTime().toString(),
		                                  bpmnProcessInstance.getEndTime().toString(),
		                                  bpmnProcessInstance.getDuration() };
		dataPublisher.publish(processInstanceStreamId, getMeta(), null, payload);
	}

	/**
	 * Publish task instance as events to the data receiver
	 *
	 * @param bpmnTaskInstance BPMN task instance to retrieve the data for payload param of data publisher's publish method
	 * @throws AgentException
	 */
	private void publishBPMNTaskInstanceEvent(BPMNTaskInstance bpmnTaskInstance)
			throws AgentException {
		Object[] payload = new Object[] { bpmnTaskInstance.getTaskDefinitionKey(),
		                                  bpmnTaskInstance.getTaskInstanceId(),
		                                  bpmnTaskInstance.getStartTime().toString(),
		                                  bpmnTaskInstance.getEndTime().toString(),
		                                  bpmnTaskInstance.getDurationInMills(),
		                                  bpmnTaskInstance.getAssignee() };
		dataPublisher.publish(taskInstanceStreamId, getMeta(), null, payload);
	}

	/**
	 * Retrieve stream id to uniquely identify the stream for process instances
	 *
	 * @return the stream id of process instance stream
	 * @throws MalformedStreamDefinitionException
	 * @throws DifferentStreamDefinitionAlreadyDefinedException
	 * @throws StreamDefinitionException
	 * @throws AgentException
	 */
	private String getBPMNProcessInstanceStreamId() throws MalformedStreamDefinitionException,
	                                                       DifferentStreamDefinitionAlreadyDefinedException,
	                                                       StreamDefinitionException,
	                                                       AgentException {
		StreamDefinition streamDefinition =
				new StreamDefinition(AnalyticsPublisherConstants.PROCESS_STREAM_NAME,
				                     AnalyticsPublisherConstants.STREAM_VERSION);
		streamDefinition.setDescription(AnalyticsPublisherConstants.PROCESS_STREAM_DESCRIPTION);
		streamDefinition.addPayloadData(AnalyticsPublisherConstants.PROCESS_DEFINITION_ID,
		                                AttributeType.STRING);
		streamDefinition.addPayloadData(AnalyticsPublisherConstants.PROCESS_INSTANCE_ID,
		                                AttributeType.STRING);
		streamDefinition
				.addPayloadData(AnalyticsPublisherConstants.START_TIME, AttributeType.STRING);
		streamDefinition.addPayloadData(AnalyticsPublisherConstants.END_TIME, AttributeType.STRING);
		streamDefinition.addPayloadData(AnalyticsPublisherConstants.DURATION, AttributeType.LONG);
		streamDefinition.addPayloadData(AnalyticsPublisherConstants.ASSIGNEE, AttributeType.STRING);
		return dataPublisher.defineStream(streamDefinition);
	}

	/**
	 * Retrieve stream id to uniquely identify the stream for task instances
	 *
	 * @return the stream id of task instance stream
	 * @throws MalformedStreamDefinitionException
	 * @throws DifferentStreamDefinitionAlreadyDefinedException
	 * @throws StreamDefinitionException
	 * @throws AgentException
	 */
	private String getBPMNTaskInstanceStreamId() throws MalformedStreamDefinitionException,
	                                                    DifferentStreamDefinitionAlreadyDefinedException,
	                                                    StreamDefinitionException, AgentException {
		StreamDefinition streamDefinition =
				new StreamDefinition(AnalyticsPublisherConstants.TASK_STREAM_NAME,
				                     AnalyticsPublisherConstants.STREAM_VERSION);
		streamDefinition.setDescription(AnalyticsPublisherConstants.TASK_STREAM_DESCRIPTION);
		streamDefinition.addPayloadData(AnalyticsPublisherConstants.TASK_DEFINITION_ID,
		                                AttributeType.STRING);
		streamDefinition
				.addPayloadData(AnalyticsPublisherConstants.TASK_INSTANCE_ID, AttributeType.STRING);
		streamDefinition
				.addPayloadData(AnalyticsPublisherConstants.START_TIME, AttributeType.STRING);
		streamDefinition.addPayloadData(AnalyticsPublisherConstants.END_TIME, AttributeType.STRING);
		streamDefinition.addPayloadData(AnalyticsPublisherConstants.DURATION, AttributeType.LONG);
		return dataPublisher.defineStream(streamDefinition);
	}

	/**
	 * Create a data publisher to publish the data as events
	 *
	 * @return DataPublisher object
	 * @throws MalformedURLException
	 * @throws AgentException
	 * @throws AuthenticationException
	 * @throws TransportException
	 */
	private DataPublisher createDataPublisher()
			throws MalformedURLException, AgentException, AuthenticationException,
			       TransportException, UserStoreException, RegistryException, SocketException {
		System.out.println("Start data publisher");
		DataPublisher dataPublisher =
				new DataPublisher(getURL(), BPMNAdminConfig.getUserName(),
				                  BPMNAdminConfig.getPassword());
		System.out.println("End data publisher");
		return dataPublisher;
	}

	/**
	 * Stop the data publisher
	 */
	private void stopDataPublisher() {
		dataPublisher.stop();
	}

	/**
	 * Get the url of data receiver to publish the data
	 *
	 * @return the url of data receiver in the data-bridge
	 */
	private String getURL() throws SocketException {
		String url = "tcp://" + NetworkUtils.getLocalHostname() + ":" +
		             AnalyticsPublisherConstants.PORT;
		return url;
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
	 * Set trust store parameters as the system property for encryption
	 */
	private void setTrustStoreParams() {
		File filePath = new File("src/main/resources");
		if (!filePath.exists()) {
			filePath = new File("resources");
		}
		String trustStore = filePath.getAbsolutePath();
		System.setProperty("javax.net.ssl.trustStore", trustStore + "/client-truststore.jks");
		System.setProperty("javax.net.ssl.trustStorePassword", "wso2carbon");
	}
}

