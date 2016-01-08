/**
 *  Copyright (c) 2014 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.bpmn.extensions.jms;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.transport.base.BaseConstants;

import javax.jms.*;
import javax.transaction.*;

/**
 * Performs the actual sending of a JMS message, and the subsequent committing of a JTA transaction
 * (if requested) or the local session transaction, if used. An instance of this class is unique
 * to a single message send out operation and will not be shared.
 */
public class JMSMessageSender {

    private static final Log log = LogFactory.getLog(JMSMessageSender.class);

    /** The Connection to be used to send out */
    private Connection connection = null;
    /** The Session to be used to send out */
    private Session session = null;
    /** The MessageProducer used */
    private MessageProducer producer = null;
    /** Target Destination */
    private Destination destination = null;
    /** The level of cachability for resources */
    private int cacheLevel = JMSConstants.CACHE_CONNECTION;

    private Boolean isQueue = null;

    /**
     *
     * @param connection the JMS Connection
     * @param session JMS Session
     * @param producer the MessageProducer
     * @param destination the JMS Destination
     * @param cacheLevel cacheLevel - None | Connection | Session | Producer
     * @param isQueue posting to a Queue?
     */
    public JMSMessageSender(Connection connection, Session session, MessageProducer producer,
                            Destination destination, int cacheLevel, Boolean isQueue) {

        this.connection = connection;
        this.session = session;
        this.producer = producer;
        this.destination = destination;
        this.cacheLevel = cacheLevel;
        this.isQueue = isQueue;
    }



    /**
     * Perform actual send of JMS message to the Destination selected
     *
     * @param message the JMS message
     */
    public void send(Message message, MessageContext msgCtx) {

        Boolean jtaCommit = null;
        Boolean rollbackOnly = null;
        Boolean persistent = null;
        Integer priority = null;
        Integer timeToLive = null;
        if(msgCtx != null) {

            jtaCommit = getBooleanProperty(msgCtx, BaseConstants.JTA_COMMIT_AFTER_SEND);
            rollbackOnly = getBooleanProperty(msgCtx, BaseConstants.SET_ROLLBACK_ONLY);
            persistent = getBooleanProperty(msgCtx, JMSConstants.JMS_DELIVERY_MODE);
            priority = getIntegerProperty(msgCtx, JMSConstants.JMS_PRIORITY);
            timeToLive = getIntegerProperty(msgCtx, JMSConstants.JMS_TIME_TO_LIVE);

        }
        // Do not commit, if message is marked for rollback
        if (rollbackOnly != null && rollbackOnly) {
            jtaCommit = Boolean.FALSE;
        }

        if (persistent != null) {
            try {
                producer.setDeliveryMode(DeliveryMode.PERSISTENT);
            } catch (JMSException e) {
                log.error("Error setting JMS Producer for PERSISTENT delivery", e);
            }
        }
        if (priority != null) {
            try {
                producer.setPriority(priority);
            } catch (JMSException e) {
                log.error("Error setting JMS Producer priority to : " + priority, e);
            }
        }
        if (timeToLive != null) {
            try {
                producer.setTimeToLive(timeToLive);
            } catch (JMSException e) {
                log.error("Error setting JMS Producer TTL to : " + timeToLive, e);
            }
        }


        boolean sendingSuccessful = false;
        // perform actual message sending
        try {
            if (isQueue == null) {
                producer.send(message);

            } else {
                if (isQueue) {
                    try {
                        ((QueueSender) producer).send(message);
                    } catch (JMSException e) {
                        createTempQueueConsumer();
                        ((QueueSender) producer).send(message);
                    }

                } else {
                    try {
                        ((TopicPublisher) producer).publish(message);
                    } catch (JMSException e) {
                        createTempTopicSubscriber();
                        ((TopicPublisher) producer).publish(message);
                    }
                }
            }

            // set the actual MessageID to the message context for use by any others down the line
            //String msgId = null;
//            try {
//                msgId = message.getJMSMessageID();
//                if (msgId != null) {
//                    msgCtx.setProperty(JMSConstants.JMS_MESSAGE_ID, msgId);
//                }
//            } catch (JMSException ignore) {}

            sendingSuccessful = true;
            log.info("Message sent");

//            if (log.isDebugEnabled()) {
//                log.debug("Sent Message Context ID : " + msgCtx.getMessageID() +
//                        " with JMS Message ID : " + msgId +
//                        " to destination : " + producer.getDestination());
//            }

        } catch (JMSException e) {
            log.error("Error sending message with MessageContext ID : " +
                    msgCtx.getMessageID() + " to destination : " + destination, e);

        } finally {

            if (jtaCommit != null) {

                UserTransaction ut = (UserTransaction) msgCtx.getProperty(BaseConstants.USER_TRANSACTION);
                if (ut != null) {

                    try {
                        if (sendingSuccessful && jtaCommit) {
                            ut.commit();
                        } else {
                            ut.rollback();
                        }
                        msgCtx.removeProperty(BaseConstants.USER_TRANSACTION);

                        if (log.isDebugEnabled()) {
                            log.debug((sendingSuccessful ? "Committed" : "Rolled back") +
                                    " JTA Transaction");
                        }

                    } catch (Exception e) {
                        log.error("Error committing/rolling back JTA transaction after " +
                                "sending of message with MessageContext ID : " + msgCtx.getMessageID() +
                                " to destination : " + destination, e);
                    }
                }

            } else {
                try {
                    if (session.getTransacted()) {
                        if (sendingSuccessful && (rollbackOnly == null || !rollbackOnly)) {
                            session.commit();
                        } else {
                            session.rollback();
                        }
                    }

                    if (log.isDebugEnabled()) {
                        log.debug((sendingSuccessful ? "Committed" : "Rolled back") +
                                " local (JMS Session) Transaction");
                    }

                } catch (JMSException e) {
                    log.error("Error committing/rolling back local (i.e. session) " +
                            "transaction after sending of message with MessageContext ID : " +
                            msgCtx.getMessageID() + " to destination : " + destination, e);
                }
            }
        }
    }
    /**
     * Creating a temporary  Queue Consumer; The objective of this is to make
     * a binding for this destination in the message broker. If there is no
     * bindings created in the server before sending messages, messages will not
     * be stored in the server. So we create a consumer and close it, if there
     * are not any bindings already created in the server
     *
     * */
    public void createTempQueueConsumer() throws JMSException {
        MessageConsumer consumer = ((QueueSession) session).createConsumer((Destination) destination);
        consumer.close();
    }

    public void createTempTopicSubscriber() throws JMSException {
        TopicSubscriber subscriber = ((TopicSession) session).createSubscriber((Topic) destination);
        subscriber.close();
    }
    /**
     * Close non-shared producer, session and connection if any
     */
    public void close() {
        if (producer != null && cacheLevel < JMSConstants.CACHE_PRODUCER) {
            try {
                producer.close();
            } catch (JMSException e) {
                log.error("Error closing JMS MessageProducer after send", e);
            } finally {
                producer = null;
            }
        }

        if (session != null && cacheLevel < JMSConstants.CACHE_SESSION) {
            try {
                session.close();
            } catch (JMSException e) {
                log.error("Error closing JMS Session after send", e);
            } finally {
                session = null;
            }
        }

        if (connection != null && cacheLevel < JMSConstants.CACHE_CONNECTION) {
            try {
                connection.close();
            } catch (JMSException e) {
                log.error("Error closing JMS Connection after send", e);
            } finally {
                connection = null;
            }
        }
    }



    private Boolean getBooleanProperty(MessageContext msgCtx, String name) {
        Object o = msgCtx.getProperty(name);
        if (o != null) {
            if (o instanceof Boolean) {
                return (Boolean) o;
            } else if (o instanceof String) {
                return Boolean.valueOf((String) o);
            }
        }
        return null;
    }

    private Integer getIntegerProperty(MessageContext msgCtx, String name) {
        Object o = msgCtx.getProperty(name);
        if (o != null) {
            if (o instanceof Integer) {
                return (Integer) o;
            } else if (o instanceof String) {
                return Integer.parseInt((String) o);
            }
        }
        return null;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    public void setSession(Session session) {
        this.session = session;
    }

    public void setProducer(MessageProducer producer) {
        this.producer = producer;
    }

    public void setCacheLevel(int cacheLevel) {
        this.cacheLevel = cacheLevel;
    }

    public int getCacheLevel() {
        return cacheLevel;
    }

    public Connection getConnection() {
        return connection;
    }

    public MessageProducer getProducer() {
        return producer;
    }

    public Session getSession() {
        return session;
    }
}
