package org.wso2.carbon.bpmn.analytics.publisher;

import org.wso2.carbon.bpmn.analytics.publisher.models.BPMNProcessInstance;
import org.wso2.carbon.bpmn.analytics.publisher.models.BPMNTaskInstance;
import org.wso2.carbon.databridge.agent.thrift.DataPublisher;
import org.wso2.carbon.databridge.agent.thrift.exception.AgentException;
import org.wso2.carbon.databridge.commons.AttributeType;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.databridge.commons.exception.*;

import java.net.MalformedURLException;

/**
 * Created by isuruwi on 6/28/15.
 */
public class BPMNDataPublisher {
    private String processInstanceStreamId;
    private String taskInstanceStreamId;
    private DataPublisher dataPublisher;
    private AnalyticsPublishServiceUtils dasPublishServiceComp;
    private BPMNProcessInstance[] bpmnProcessInstances;
    private BPMNTaskInstance[] bpmnTaskInstances;

    public void initialize() {

        try {
            dataPublisher = createDataPublisher();
            processInstanceStreamId = getBPMNProcessInstanceStreamId();
            taskInstanceStreamId = getBPMNTaskInstanceStreamId();
            dasPublishServiceComp = AnalyticsPublishServiceUtils.getInstance();
            startPollingForProcessInstances();
            startPollingForTaskInstances();

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (AgentException e) {
            e.printStackTrace();
        } catch (AuthenticationException e) {
            e.printStackTrace();
        } catch (TransportException e) {
            e.printStackTrace();
        } catch (DifferentStreamDefinitionAlreadyDefinedException e) {
            e.printStackTrace();
        } catch (StreamDefinitionException e) {
            e.printStackTrace();
        } catch (MalformedStreamDefinitionException e) {
            e.printStackTrace();
        }
    }


    private void startPollingForProcessInstances() {
        //Polling thread goes here
        Thread pollingThread = new Thread() {
            @Override
            public void run() {
                try {
                    while (true) {
                        bpmnProcessInstances = dasPublishServiceComp.getCompletedProcessInstances();
                        if (bpmnProcessInstances != null) {
                            for (BPMNProcessInstance instance : bpmnProcessInstances) {
                                publishBPMNProcessInstanceEvent(instance);
                            }
                        } else {
                           // stopDataPublisher();
                        }
                        // Thread.sleep(5 * 1000);
                    }
                } catch (AgentException e) {
                    e.printStackTrace();
                }
            }
        };
        pollingThread.start();
    }

    private void startPollingForTaskInstances() {
        //Polling thread goes here
        Thread pollingThread = new Thread() {
            @Override
            public void run() {
                try {
                    while (true) {
                        bpmnTaskInstances = dasPublishServiceComp.getCompletedTasks();
                        if (bpmnTaskInstances != null) {
                            for (BPMNTaskInstance instance : bpmnTaskInstances) {
                                publishBPMNTaskInstanceEvent(instance);
                            }
                        } else {
                           // stopDataPublisher();
                        }
                        // Thread.sleep(5 * 1000);
                    }
                } catch (AgentException e) {
                    e.printStackTrace();
                }
            }
        };
        pollingThread.start();
    }

    private void publishBPMNProcessInstanceEvent(BPMNProcessInstance bpmnProcessInstance) throws AgentException {
        Object[] payload = new Object[]{bpmnProcessInstance.getProcessDefinitionId(), bpmnProcessInstance.getInstanceId(), bpmnProcessInstance.getStartTime().toString(), bpmnProcessInstance.getEndTime().toString(), bpmnProcessInstance.getDuration()};
        dataPublisher.publish(processInstanceStreamId, getMeta(), null, payload);
        try {
            Thread.sleep(DASPublisherConstants.DELAY);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void publishBPMNTaskInstanceEvent(BPMNTaskInstance bpmnTaskInstance) throws AgentException {
        Object[] payload = new Object[]{bpmnTaskInstance.getTaskDefinitionKey(), bpmnTaskInstance.getTaskInstanceId(), bpmnTaskInstance.getStartTime().toString(), bpmnTaskInstance.getEndTime().toString(), bpmnTaskInstance.getDurationInMills(), bpmnTaskInstance.getAssignee()};
        dataPublisher.publish(taskInstanceStreamId, getMeta(), null, payload);
        try {
            Thread.sleep(DASPublisherConstants.DELAY);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private String getBPMNProcessInstanceStreamId() throws MalformedStreamDefinitionException, DifferentStreamDefinitionAlreadyDefinedException, StreamDefinitionException, AgentException {
        StreamDefinition streamDefinition = new StreamDefinition(DASPublisherConstants.PROCESS_STREAM_NAME, DASPublisherConstants.STREAM_VERSION);
        streamDefinition.setDescription(DASPublisherConstants.PROCESS_STREAM_DESCRIPTION);
        streamDefinition.addPayloadData("processDefinitionId", AttributeType.STRING);
        streamDefinition.addPayloadData("processInstanceId", AttributeType.STRING);
        streamDefinition.addPayloadData("startTime", AttributeType.STRING);
        streamDefinition.addPayloadData("endTime", AttributeType.STRING);
        streamDefinition.addPayloadData("duration", AttributeType.LONG);
        streamDefinition.addPayloadData("assignee", AttributeType.STRING);
        return dataPublisher.defineStream(streamDefinition);
    }

    private String getBPMNTaskInstanceStreamId() throws MalformedStreamDefinitionException, DifferentStreamDefinitionAlreadyDefinedException, StreamDefinitionException, AgentException {
        StreamDefinition streamDefinition = new StreamDefinition(DASPublisherConstants.TASK_STREAM_NAME, DASPublisherConstants.STREAM_VERSION);
        streamDefinition.setDescription(DASPublisherConstants.TASK_STREAM_DESCRIPTION);
        streamDefinition.addPayloadData("taskDefinitionKey", AttributeType.STRING);
        streamDefinition.addPayloadData("taskInstanceId", AttributeType.STRING);
        streamDefinition.addPayloadData("startTime", AttributeType.STRING);
        streamDefinition.addPayloadData("endTime", AttributeType.STRING);
        streamDefinition.addPayloadData("duration", AttributeType.LONG);
        return dataPublisher.defineStream(streamDefinition);
    }

    private DataPublisher createDataPublisher() throws MalformedURLException, AgentException, AuthenticationException, TransportException {
        DataPublisher dataPublisher = new DataPublisher(getURL(), DASPublisherConstants.USER_NAME, DASPublisherConstants.PASSWORD);
        return dataPublisher;
    }

    private void stopDataPublisher() {
        dataPublisher.stop();
    }

    private String getURL() {
        String url = "tcp://" + DASPublisherConstants.HOST + ":" + DASPublisherConstants.PORT;
        return url;
    }

    private Object[] getMeta() {
        return new Object[]{};
    }

}
