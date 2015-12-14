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

    public JMSListener(String jmsProviderID, Hashtable<String, String> parameters) {

        /*Remember to initialize JMSConnectionFactoryManager at the beginning of deploy and fixedDeploy methods
        * in TenantRepository and then call the initConnectionFactories method.
        */
        connectionFactory = JMSConnectionFactoryManager.getInstance().getConnectionFactory(jmsProviderID);
        connection = connectionFactory.getConnection();
        session = connectionFactory.getSession(connection);

        String destinationType = parameters.get(JMSConstants.PARAM_DESTINATION_TYPE);
        String destinationName = parameters.get(JMSConstants.PARAM_DESTINATION);
        destination = connectionFactory.getDestination(destinationName, destinationType);

        try {
            if ("queue".equalsIgnoreCase(destinationType)) {
                consumer = ((QueueSession) session).createReceiver((Queue) destination);
            } else if ("topic".equalsIgnoreCase(destinationType)) {
                consumer = ((TopicSession) session).createSubscriber((Topic) destination);
            } else {
                log.error("Invalid destination type: " + destinationType);
            }

            if(consumer != null){
                consumer.setMessageListener(this);
            }

            connection.start();

            Runnable idleRunnable = new Runnable() {
                @Override
                public void run() {
                    while (true){

                    }
                }
            };

            idleThread = new Thread(idleRunnable);
            idleThread.start();
            log.info("JMS MessageListener for destination: " + destinationName + " started to listen" );
        } catch (JMSException e) {
            log.error("Error creating a JMS Consumer from this JMS Session", e);
        }
    }

    @Override
    public void onMessage(Message message) {
        try {
            if (message instanceof TextMessage) {
                TextMessage text = (TextMessage) message;

                log.info("Message received is : " + text.getText());
                ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
                RuntimeService runtimeService = processEngine.getRuntimeService();

                Map<String, Object> variableMap = new HashMap<>();
                runtimeService.startProcessInstanceByMessageAndTenantId(text.getText(), variableMap, "-1234");

            }
        }catch (JMSException e){
            log.error(e.getMessage());
        }
    }
}
