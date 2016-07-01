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
                queueConnectionFactory = QueueConnectionFactory.class.cast(connectionFactory);
            }else{
                topicConnectionFactory = TopicConnectionFactory.class.cast(connectionFactory);
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
     * This is a JMS spec independent method to create a MessageProducer. Please be cautious when
     * making any changes
     *
     * @param session JMS session
     * @param destination the Destination
     * @param isQueue is the Destination a queue?
     * @return a MessageProducer to send messages to the given Destination
     * @throws JMSException on errors, to be handled and logged by the caller
     */
    public static MessageProducer createProducer(
            Session session, Destination destination, Boolean isQueue) throws JMSException {

        if (isQueue == null) {
            return session.createProducer(destination);
        } else {
            if (isQueue) {
                return ((QueueSession) session).createSender((Queue) destination);
            } else {
                return ((TopicSession) session).createPublisher((Topic) destination);
            }
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
    public <T> T lookup(Context context, Class<T> className, String name) throws NamingException{
        Object object;
        object = context.lookup(name);

        try {
            return className.cast(object);
        } catch (ClassCastException e) {
            log.error(e.getMessage());
            return null;
        }
    }

    /**
     *
     * @param context
     * @param destinationName
     * @param destinationType
     * @return
     * @throws NamingException
     */
    public Destination lookupDestination(Context context, String destinationName, String destinationType)
            throws NamingException {
        if(destinationName == null){
            return null;
        }

        try {
            return lookup(context,Destination.class, destinationName);
        } catch (NameNotFoundException e) {
            try{
                Properties initialProperties = new Properties();
                if(context.getEnvironment() != null){
                    if(context.getEnvironment().get(JMSConstants.NAMING_FACTORY_INITIAL) != null){
                        initialProperties.put(JMSConstants.NAMING_FACTORY_INITIAL,
                                context.getEnvironment().get(JMSConstants.NAMING_FACTORY_INITIAL));
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
                return lookup(initialContext, Destination.class, destinationName);

            }catch (NamingException ex){
                log.warn("Cannot locate destination: " + destinationName);
                throw ex;
            }
        }
    }
}
