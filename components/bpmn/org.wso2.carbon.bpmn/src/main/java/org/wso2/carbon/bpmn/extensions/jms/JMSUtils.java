package org.wso2.carbon.bpmn.extensions.jms;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import java.util.Properties;

/**
 * Created by dilini on 12/10/15.
 */
public class JMSUtils {
    private static final Log log = LogFactory.getLog(JMSUtils.class);

    /**
     *
     * @param message
     * @param property
     * @return
     */
    public static String getMessageProperty(Message message, String property){
        try {
            return message.getStringProperty(property);
        } catch (JMSException e) {
            return null;
        }
    }

    /**
     *
     * @param session
     * @param destination
     * @return
     * @throws JMSException
     */
    public static MessageConsumer createConsumer(Session session, Destination destination) throws JMSException {
        if(destination instanceof Queue){
            return ((QueueSession)session).createConsumer((Queue)destination);
        }else{
            return ((TopicSession)session).createSubscriber((Topic)destination);
        }
    }

    public static Destination createTemporaryDestinaiton(Session session) throws JMSException {
        if(session instanceof QueueSession){
            return session.createTemporaryQueue();
        }else{
            return session.createTemporaryTopic();
        }
    }

    /**
     *
     * @param connectionFactory
     * @param username
     * @param password
     * @param isQueue
     * @return
     */
    public static Connection createConnection(ConnectionFactory connectionFactory, String username,
                                              String password, Boolean isQueue) throws JMSException {
        Connection connection = null;

        if(isQueue == null){
            if(username == null && password == null){
                connection = connectionFactory.createConnection();
            }else{
                connection = connectionFactory.createConnection(username, password);
            }
        }else{
            QueueConnectionFactory queueConnectionFactory = null;
            TopicConnectionFactory topicConnectionFactory = null;

            if(isQueue){
                queueConnectionFactory = (QueueConnectionFactory)connectionFactory;
            }else{
                topicConnectionFactory = (TopicConnectionFactory)connectionFactory;
            }

            if(queueConnectionFactory != null){
                if(username == null && password == null){
                    connection = queueConnectionFactory.createQueueConnection();
                }else{
                    connection = queueConnectionFactory.createQueueConnection(username, password);
                }
            }else if(topicConnectionFactory != null){
                if(username == null && password == null){
                    connection = topicConnectionFactory.createTopicConnection();
                }else{
                    connection = topicConnectionFactory.createTopicConnection(username, password);
                }
            }
        }
        return connection;
    }

    /**
     *
     * @param connection
     * @param transacted
     * @param ackMode
     * @param isQueue
     * @return
     * @throws JMSException
     */
    public static Session createSession(Connection connection, boolean transacted, int ackMode, Boolean isQueue) throws JMSException {
        if(isQueue == null){
            return connection.createSession(transacted, ackMode);
        }else {
            if (isQueue) {
                return ((QueueConnection) connection).createQueueSession(transacted, ackMode);
            } else {
                return ((TopicConnection) connection).createTopicSession(transacted, ackMode);
            }
        }
    }

    /**
     *
     * @param session
     * @param isQueue
     * @param destination
     * @return
     * @throws JMSException
     */
    public static MessageProducer createProducer(Session session, Boolean isQueue, Destination destination) throws JMSException {
        if(isQueue == null){
            return session.createProducer(destination);
        }else{
            if(isQueue){
                return ((QueueSession)session).createSender((Queue) destination);
            }else{
                return ((TopicSession)session).createPublisher((Topic)destination);
            }
        }
    }


    /**
     * @param destType
     * @return
     */
    public static String getDestinationType(int destType){
        if(destType == JMSConstants.QUEUE){
            return "queue";
        }else{
            return "topic";
        }
    }

    /**
     *
     * @param context
     * @param className
     * @param name
     * @param <T>
     * @return
     * @throws NamingException
     * @throws BPMNJMSException
     */
    public static <T> T lookup(Context context, Class<T> className, String name) throws NamingException{
        Object object = context.lookup(name);

        try{
            return className.cast(object);
        }catch (ClassCastException e){
            String exceptionMsg = "JNDI failed to de-reference Reference with name " + name;
            log.error(exceptionMsg);
            return null;
        }
    }

    public static Destination lookupDestination(Context context, String destinationName, String destinationType)
            throws NamingException {
        if(destinationName == null){
            return null;
        }

        try {
            return JMSUtils.lookup(context,Destination.class, destinationName);
        } catch (NameNotFoundException e) {
            try{
                Properties initialProperties = new Properties();
                if(context.getEnvironment() != null){
                    if(context.getEnvironment().get(JMSConstants.NAMING_FACTOR_INITIAL) != null){
                        initialProperties.put(JMSConstants.NAMING_FACTOR_INITIAL,
                                context.getEnvironment().get(JMSConstants.NAMING_FACTOR_INITIAL));
                    }if(context.getEnvironment().get(JMSConstants.JMS_PROVIDER_URL) != null){
                        initialProperties.put(JMSConstants.JMS_PROVIDER_URL,
                                context.getEnvironment().get(JMSConstants.JMS_PROVIDER_URL));
                    }if(context.getEnvironment().get(JMSConstants.CONNECTION_STRING) != null){
                        initialProperties.put(JMSConstants.CONNECTION_STRING,
                                context.getEnvironment().get(JMSConstants.CONNECTION_STRING));
                    }
                }

                if(JMSConstants.DESTINATION_TYPE_QUEUE.equalsIgnoreCase(destinationType)){
                    initialProperties.put(JMSConstants.QUEUE_PREFIX + destinationName, destinationName);
                }else if(JMSConstants.DESTINATION_TYPE_TOPIC.equalsIgnoreCase(destinationType)){
                    initialProperties.put(JMSConstants.TOPIC_PREFIX + destinationName, destinationName);
                }

                InitialContext initialContext = new InitialContext(initialProperties);
                return JMSUtils.lookup(initialContext, Destination.class, destinationName);

            }catch (NamingException ex){
                log.warn("Cannot locate destination: " + destinationName);
                throw ex;
            }
        }
    }
}
