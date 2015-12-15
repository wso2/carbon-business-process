package org.wso2.carbon.bpmn.extensions.jms;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RuntimeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.jms.*;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

/**
 * Created by dilini on 12/11/15.
 */
public class JMSListener implements MessageListener{

    private static final Log log = LogFactory.getLog(JMSListener.class);

    private JMSConnectionFactory connectionFactory;
    private Connection connection = null;
    private Session session = null;
    private MessageConsumer consumer = null;
    private Destination destination = null;
    private Thread idleThread = null;
    private Hashtable<String, String> parameters = null;

    public JMSListener(String jmsProviderID, Hashtable<String, String> parameters) {
        this.parameters = parameters;
        if(jmsProviderID != null) {
            String destinationType = parameters.get(JMSConstants.PARAM_DESTINATION_TYPE);
            String destinationName = parameters.get(JMSConstants.PARAM_DESTINATION);

            connectionFactory = JMSConnectionFactoryManager.getInstance().getConnectionFactory(jmsProviderID);
            connection = connectionFactory.getConnection();
            session = connectionFactory.getSession(connection);
            destination = connectionFactory.getDestination(destinationName, destinationType);

            try {
                if (JMSConstants.DESTINATION_TYPE_QUEUE.equalsIgnoreCase(destinationType)) {
                    consumer = ((QueueSession) session).createReceiver((Queue) destination);
                } else if (JMSConstants.DESTINATION_TYPE_TOPIC.equalsIgnoreCase(destinationType)) {
                    consumer = ((TopicSession) session).createSubscriber((Topic) destination);
                } else {
                    log.error("Invalid destination type: " + destinationType);
                }

                if (consumer != null) {
                    consumer.setMessageListener(this);
                }

                connection.start();

                Runnable idleRunnable = new Runnable() {
                    @Override
                    public void run() {
                        while (true) {

                        }
                    }
                };

                idleThread = new Thread(idleRunnable);
                idleThread.start();
                log.info("JMS MessageListener for destination: " + destinationName + " started to listen");
            } catch (JMSException e) {
                log.error("Error creating a JMS Consumer from this JMS Session", e);
            }
        }
    }

    @Override
    public void onMessage(Message message) {
        try {
            if (message instanceof TextMessage) {
                TextMessage text = (TextMessage) message;

                ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
                RuntimeService runtimeService = processEngine.getRuntimeService();

                Map<String, Object> variableMap = new HashMap<>();
                String outputMappings = parameters.get(JMSConstants.PARAM_OUTPUT_MAPPINGS);

                if(outputMappings != null){
                    String variables[] = outputMappings.split(";");
                    for (int i = 0; i < variables.length; i++) {
                        String fields[] = variables[i].split("#");
                        if("required".equals(fields[2])){

                            //Have to read the value from the message and assign it here instead of fields[1]
                            variableMap.put(fields[0], fields[1]);
                        }
                    }
                }

                runtimeService.startProcessInstanceByMessageAndTenantId(text.getText(), variableMap, "-1234");

            }
        }catch (JMSException e){
            log.error(e.getMessage());
        }
    }
}
