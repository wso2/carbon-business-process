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
import javax.naming.NamingException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by dilini on 12/10/15.
 */
public class JMSConnectionFactory {
    private static final Log log = LogFactory.getLog(JMSConnectionFactory.class);

    /** the list of parameters from activiti.xml definition*/
    private Hashtable<String, String> parameters = new Hashtable<>();

    /** InitialContext reference*/
    private Context context = null;

    /** The JMS ConnectionFactory to create connection objects*/
    private ConnectionFactory connectionFactory = null;

    /** The shared JMS Connection for the JMS connection factory*/
    private Connection sharedConnection = null;

    private Session sharedSession = null;

    /** The shared JMS MessageProducer for the JMS connection factory*/
    private MessageProducer sharedProducer = null;

    /** The shared JMS Destination for the JMS connection factory*/
    private Destination sharedDestination = null;

    private int cacheLevel = JMSConstants.CACHE_CONNECTION;

    private int lastReturnedConnectionIndex = 0;

    private Map<Integer, Connection> sharedConnectionMap = new ConcurrentHashMap<>();

    private int maxSharedConnectionCount = 10;

    private HashMap<Integer, Integer> connectionCount;

    JMSUtils utils = new JMSUtils();

    public JMSConnectionFactory(Hashtable<String, String> parameters){
        this.parameters = parameters;
        this.connectionCount = new HashMap<>();

        digestCacheLevel();

        try {
            context = new InitialContext(parameters);

            if(JMSConstants.DESTINATION_TYPE_QUEUE.equalsIgnoreCase(parameters.get(JMSConstants.PARAM_DESTINATION_TYPE))){
                connectionFactory = utils.lookup(context, QueueConnectionFactory.class,
                        parameters.get(JMSConstants.PARAM_CONNECTION_FACTORY_JNDI_NAME));
            }else{
                connectionFactory = utils.lookup(context, TopicConnectionFactory.class,
                        parameters.get(JMSConstants.PARAM_CONNECTION_FACTORY_JNDI_NAME));
            }

            if(parameters.get(JMSConstants.PARAM_DESTINATION) != null){
                sharedDestination = utils.lookup(context, Destination.class,
                        parameters.get(JMSConstants.PARAM_DESTINATION));
            }

            log.info("JMS ConnectionFactory initialized for: " + parameters.get(JMSConstants.PARAM_CONNECTION_FACTORY_JNDI_NAME));
        }catch (NamingException e){
            String errorMsg = "Cannot acquire JNDI context, JMS Connection factory : " +
                    parameters.get(JMSConstants.PARAM_CONNECTION_FACTORY_JNDI_NAME) + " or default destination : " +
                    parameters.get(JMSConstants.PARAM_DESTINATION) +
                    " using : " + parameters;
            log.error(errorMsg, e);
        }
    }

    private void digestCacheLevel(){
        String value = parameters.get(JMSConstants.PARAM_CACHE_LEVEL);

        if("none".equalsIgnoreCase(value)){
            this.cacheLevel = JMSConstants.CACHE_NONE;
        }else if("connection".equalsIgnoreCase(value)){
            this.cacheLevel = JMSConstants.CACHE_CONNECTION;
        }else if("session".equalsIgnoreCase(value)){
            this.cacheLevel = JMSConstants.CACHE_SESSION;
        }else if("producer".equalsIgnoreCase(value)){
            this.cacheLevel = JMSConstants.CACHE_PRODUCER;
        }else if("consumer".equalsIgnoreCase(value)){
            this.cacheLevel = JMSConstants.CACHE_CONSUMER;
        }else if(value != null){
            String errorMsg = "Invalid cache level: " + value + " for JMS Connection Factory";
            log.error(errorMsg);
        }
    }

    /**
     *
     */
    public synchronized void stop(){
        if(sharedConnection != null){
            try {
                sharedConnection.close();
            } catch (JMSException e) {
                log.warn("Error while shutting down the connection factory", e);
            }
        }

        if(context != null){
            try {
                context.close();
            } catch (NamingException e) {
                log.warn("Error while closing the InitialContext", e);
            }
        }
    }

    /**
     *
     * @return
     */
    public Hashtable<String, String> getParameters(){
        return parameters;
    }

    /**
     *
     * @return
     */
    public Context getContext(){
        return context;
    }

    /**
     *
     * @return
     */
    public Destination getSharedDestination(){
        return sharedDestination;
    }

    private void incrementConnectionCounter(int connectionIndex){
        if(connectionCount.get(connectionIndex) != null)
            connectionCount.put(connectionIndex, connectionCount.get(connectionIndex) + 1);
        else
            connectionCount.put(connectionIndex, 1);
    }
    /**
     *
     * @return
     */
    private synchronized Connection getSharedConnection(){

        Connection connection = sharedConnectionMap.get(lastReturnedConnectionIndex);


        //int connectionIndex = lastReturnedConnectionIndex;
        if(connection == null){
            connection = createConnection();
            try {
                connection.start();
            } catch (JMSException e) {
                log.error(e.getMessage(), e);
            }

            if(connection!= null){
            }
            sharedConnectionMap.put(lastReturnedConnectionIndex, connection);
//            incrementConnectionCounter(lastReturnedConnectionIndex);


        }
//        else{
//            incrementConnectionCounter(lastReturnedConnectionIndex);
//        }
        lastReturnedConnectionIndex++;
        if(lastReturnedConnectionIndex >= maxSharedConnectionCount){
            lastReturnedConnectionIndex = 0;
        }

        return connection;
    }

//    public HashMap<Integer, Integer> getConnectionCount(){
//        return connectionCount;
//    }

    public int getLastReturnedConnectionIndex(){
        return lastReturnedConnectionIndex;
    }

//    public Connection getConnection(int lastReturnedConnectionIndex){
//        return sharedConnectionMap.get(lastReturnedConnectionIndex);
//    }

//    public void decrementCounter(int connectionIndex){
//        connectionCount.put(connectionIndex, connectionCount.get(connectionIndex) - 1);
//    }

    /**
     *
     * @return
     */
    private synchronized Session getSharedSession(int connectionIndex){

        if(sharedSession == null){
            sharedSession = createSession(sharedConnectionMap.get(connectionIndex));
            if(log.isDebugEnabled()){
                log.debug("Creating a shared session from JMS Connection");
            }
        }
        return sharedSession;
    }

    /**
     *
     * @param destinationName
     * @param destinationType
     * @return
     */
    public Destination getDestination(String destinationName, String destinationType){
        try {
            return utils.lookupDestination(context, destinationName, destinationType);
        } catch (NamingException e) {
            log.error("Error looking up the JMS destination with name " + destinationName
                    + " of type " + destinationType, e);
        }

        return null;
    }

    public Boolean isQueue(){
        if(parameters.get(JMSConstants.PARAM_DESTINATION_TYPE) == null){
            return false;
        }else{
            if(JMSConstants.DESTINATION_TYPE_QUEUE.equalsIgnoreCase(parameters.get(JMSConstants.PARAM_DESTINATION_TYPE))){
                return true;
            }else if(JMSConstants.DESTINATION_TYPE_TOPIC.equalsIgnoreCase(parameters.get(JMSConstants.PARAM_DESTINATION_TYPE))){
                return false;
            }else{
                String invalidDestTypeErrorMsg = "Invalid " + JMSConstants.PARAM_DESTINATION_TYPE + ": " +
                        parameters.get(JMSConstants.PARAM_DESTINATION_TYPE);
                log.error(invalidDestTypeErrorMsg);
                return null;
            }
        }
    }

    /**
     *
     * @return
     */
    private Connection createConnection(){
        Connection connection = null;
        try{
            connection = JMSUtils.createConnection(connectionFactory, parameters.get(JMSConstants.PARAM_USERNAME), parameters.get(JMSConstants.PARAM_PASSWORD), isQueue());
            if(log.isDebugEnabled()) {
                log.debug("New JMS Connection was created from the JMS ConnectionFactory");
            }
        }catch (JMSException e){
            log.error("Error creating a connection from the JMS Connection Factory using properties: " + parameters, e);
        }
        return connection;
    }

    /**
     *
     * @param connection
     * @return
     */
    private Session createSession(Connection connection){
        Session session = null;
        try{
            //session is always not transacted
            session = JMSUtils.createSession(connection, false, Session.AUTO_ACKNOWLEDGE, isQueue());
            if(log.isDebugEnabled()){
                log.debug("New JMS Session was created from the JMS Connection");
            }
        }catch (JMSException e){
            log.error("Error creating a session from the JMS Connection using properties: " + parameters, e);
        }
        return session;
    }

    /**
     *
     * @return
     */
    public Connection getConnection(){
        if(cacheLevel > JMSConstants.CACHE_NONE){
            return getSharedConnection();
        }else{
            return createConnection();
        }
    }

    /**
     *
     * @param connection
     * @return
     */
    public Session getSession(Connection connection){
        if(cacheLevel > JMSConstants.CACHE_CONNECTION){
            return getSharedSession(lastReturnedConnectionIndex - 1);
        }else{
            if(connection == null){
                return createSession(getConnection());
            }else{
                return createSession(connection);
            }
        }
    }


    /**
     * Get a new MessageProducer or shared MessageProducer from this JMS CF
     * @param connection the Connection to be used
     * @param session the Session to be used
     * @param destination the Destination to bind MessageProducer to
     * @return new or shared MessageProducer from this JMS CF
     */
    public MessageProducer getMessageProducer(Connection connection, Session session, Destination destination) {
        if (cacheLevel > JMSConstants.CACHE_SESSION) {
            return getSharedProducer(destination);
        } else {
            return createProducer((session == null ? getSession(connection) : session), destination);
        }
    }


    /**
     * Get a shared MessageProducer from this JMS CF
     * @return shared MessageProducer from this JMS CF
     */
    private synchronized MessageProducer getSharedProducer(Destination destination) {
        if (sharedProducer == null) {

            if(sharedDestination != null) {
                sharedProducer = createProducer(getSharedSession(lastReturnedConnectionIndex - 1), sharedDestination);

            }else{
                sharedProducer = createProducer(getSharedSession(lastReturnedConnectionIndex - 1), destination);
                sharedDestination = destination;
            }
            if (log.isDebugEnabled()) {
                log.debug("Created shared JMS MessageConsumer for JMS CF : ");
            }
        }
        return sharedProducer;
    }

    /**
     * Create a new MessageProducer
     * @param session Session to be used
     * @param destination Destination to be used
     * @return a new MessageProducer
     */
    private MessageProducer createProducer(Session session, Destination destination) {
        try {
            if (log.isDebugEnabled()) {
                log.debug("Creating a new JMS MessageProducer from JMS CF");
            }


            return JMSUtils.createProducer(session, destination, isQueue());

        } catch (JMSException e) {
            log.error("Error creating JMS producer from JMS CF : ", e);
        }
        return null;
    }


    /**
     * Cache level applicable for this JMS CF
     * @return applicable cache level
     */
    public int getCacheLevel() {
        return cacheLevel;
    }





}
