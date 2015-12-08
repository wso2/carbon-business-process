package org.wso2.carbon.bpmn.extensions.jms;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.ws.rs.core.MediaType;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;


/**
 * Created by dilini on 12/2/15.
 */
public class JMSStartTask implements MessageListener {

    private static final Log log = LogFactory.getLog(JMSInvoker.class);

    private Connection connection = null;
    private Context context = null;
    private Session session = null;
    private ConnectionFactory factory = null;
    private MessageConsumer consumer = null;
    private Destination destination = null;
    private Thread idleThread = null;

    public void receiveMessage(String connectionFactory, String provURL, String queueName) {
        Properties properties = new Properties();
        properties.setProperty(Context.INITIAL_CONTEXT_FACTORY, connectionFactory);
        properties.setProperty(Context.PROVIDER_URL, provURL);
        properties.setProperty("queue." + queueName, queueName);

        try {
            context = new InitialContext(properties);

            factory = (ConnectionFactory) context.lookup("ConnectionFactory");

            connection = factory.createConnection();

            connection.start();

            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            destination = (Destination) context.lookup(queueName);

            consumer = session.createConsumer(destination);
            consumer.setMessageListener(this);

            Runnable idleRunnable = new Runnable() {
                @Override
                public void run() {
                    while (true){

                    }
                }
            };

            idleThread = new Thread(idleRunnable);
            idleThread.start();

            if (log.isTraceEnabled()) {
                log.trace("Received a message from " + queueName);
            }

        }catch (NamingException e){
            log.error(e.getMessage());
        }catch (JMSException e){
            log.error(e.getMessage());
        }
    }

    @Override
    public void onMessage(Message message) {
        try{
            if (message instanceof TextMessage) {
                TextMessage text = (TextMessage) message;

                System.out.println("Message received is : " + text.getText());
                ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
                RuntimeService runtimeService = processEngine.getRuntimeService();

                Map<String, Object> variableMap = new HashMap<>();
                runtimeService.startProcessInstanceByMessageAndTenantId(text.getText(), variableMap, "-1234");

            }
        } catch (JMSException e) {
            log.error(e.getMessage());
        }
    }
}
