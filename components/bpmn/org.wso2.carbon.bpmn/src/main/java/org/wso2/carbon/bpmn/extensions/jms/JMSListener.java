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

import com.jayway.jsonpath.JsonPath;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RuntimeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.InputSource;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;

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
    private Hashtable<String, String> parameters = null;
    private int connectionIndex = 0;

    /**
     *
     * @param jmsProviderID
     * @param parameters
     *
     * jmsProviderID is given by the user and it refers to the type of the connection factory he wishes to use.
     * parameters is a list of parameters read from the BPMN process file given by the user. If user has not provided
     * those parameters, default parameters will be read from the configuration file in the server.
     */
    public JMSListener(String jmsProviderID, Hashtable<String, String> parameters) {
        this.parameters = parameters;
        if(jmsProviderID != null) {
            try {

                //retrieve destination type and name from the parameters list.
                String destinationType = parameters.get(JMSConstants.PARAM_DESTINATION_TYPE);
                String destinationName = parameters.get(JMSConstants.PARAM_DESTINATION);

                //create a connection factory according to the destination type
                if(JMSConstants.DESTINATION_TYPE_QUEUE.equalsIgnoreCase(destinationType)){

                    //a QueueConnectionFactory for a queue
                    connectionFactory = JMSConnectionFactoryManager.getInstance().getConnectionFactory(JMSConstants.JMS_QUEUE_CONNECTION_FACTORY);
                } else if (JMSConstants.DESTINATION_TYPE_TOPIC.equalsIgnoreCase(destinationType)) {

                    //a TopicConnectionFactory for a topic
                    connectionFactory = JMSConnectionFactoryManager.getInstance().getConnectionFactory(JMSConstants.JMS_TOPIC_CONNECTION_FACTORY);
                }else {
                    log.error("Invalid destination type: " + destinationType);
                }

                //creates a connection object from the connection factory
                connection = connectionFactory.getConnection();

                //start the connection
                connection.start();

                //creates a session object from the connection object
                session = connectionFactory.getSession(connection);

                //creates the destination object
                destination = connectionFactory.getDestination(destinationName, destinationType);

                //creates the consumer based on the destination type
                consumer = JMSUtils.createConsumer(session, destination);

                //if the consumer is not null, set its message listener.
                if (consumer != null) {
                    consumer.setMessageListener(this);
                }

                log.info("JMS MessageListener for destination: " + destinationName + " started to listen");

            }catch (JMSException e) {
                String message = "An error occurred while creating the JMSListener using: " + parameters;
                if(log.isDebugEnabled())
                    log.debug(message, e);
            }
        }

    }

    /**
     *
     * @return the consumer which uses this listener
     */
    public MessageConsumer getConsumer(){
        return consumer;
    }

    /**
     *
     * @param message
     * This method will get executed once the destination receives a message.
     */
    @Override
    public void onMessage(Message message) {
        try {

            //checks whether the message is of type TextMessage
            if (message instanceof TextMessage) {
                TextMessage text = (TextMessage) message;

                Map<String, Object> variableMap = new HashMap<>();

                //gets the list of xpath or json path expressions given by the user in the BPMN process file.
                String outputMappings = parameters.get(JMSConstants.PARAM_OUTPUT_MAPPINGS);
                String contentType = parameters.get(JMSConstants.PARAM_CONTENT_TYPE);

                String expression = text.getText();
                XPath xPath = XPathFactory.newInstance().newXPath();
                if(outputMappings != null){
                    //sample outputMappings string:
                    // name#$.student.name#required;age#$.student.age#required;messageName#$.student.msgName#required
                    //';' is used to separate set of variable names
                    String variables[] = outputMappings.split(";");
                    for (int i = 0; i < variables.length; i++) {
                        //'#' is used to separate attributes of one variable.
                        //these includes the variable name, xpath, json path or text expression, and whether the
                        // variable is required or not.
                        String fields[] = variables[i].split("#");

                        //the message body should include a value for msgName which will be used to invoke the process.
                        if("required".equals(fields[2])){
                            //if the message body content type is json
                            if(JMSConstants.CONTENT_TYPE_JSON.equalsIgnoreCase(contentType)){
                                variableMap.put(fields[0], JsonPath.read(expression, fields[1]).toString());
                            }
                            //if the message body content type is xml
                            else if(JMSConstants.CONTENT_TYPE_XML.equalsIgnoreCase(contentType)){
                                variableMap.put(fields[0], xPath.evaluate(fields[1], new InputSource(new StringReader(expression))));
                            }
                            //if the message body content type is plain text
                            else if(JMSConstants.CONTENT_TYPE_TEXT.equalsIgnoreCase(contentType)){
                                variableMap.put("messageName", text.getText());
                            }
                        }
                    }
                }

                //starts the process instance
                ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
                RuntimeService runtimeService = processEngine.getRuntimeService();
                runtimeService.startProcessInstanceByMessageAndTenantId(variableMap.get("messageName").toString(), variableMap, "-1234");
            }
        }catch (JMSException e){
            String msg = "An error occurred while creating while getting the message from the destination";
            if(log.isDebugEnabled())
                log.debug(msg, e);
        } catch (XPathExpressionException e) {
            String msg = "An error occurred while executing the XPath expression";
            if(log.isDebugEnabled())
                log.debug(msg, e);
        }
    }
}
