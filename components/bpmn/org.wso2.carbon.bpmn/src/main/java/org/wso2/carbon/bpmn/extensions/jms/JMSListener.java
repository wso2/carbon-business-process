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
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.StringReader;
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

                String expression = text.getText();
                XPath xPath = XPathFactory.newInstance().newXPath();
                if(outputMappings != null){
                    String variables[] = outputMappings.split(";");
                    for (int i = 0; i < variables.length; i++) {
                        String fields[] = variables[i].split("#");
                        //the message body should include a value for messageName which will be used to invoke the process.
                        if("required".equals(fields[2])){
                            if(isValidJsonString(expression)){
                                variableMap.put(fields[0], JsonPath.read(expression, fields[1]).toString());
                            }else if(expression.startsWith("<")){
                                variableMap.put(fields[0], xPath.evaluate(fields[1], new InputSource(new StringReader(expression))));
                            }else{
                                //for a plain text message
                            }
                        }
                    }
                }
                runtimeService.startProcessInstanceByMessageAndTenantId(variableMap.get("messageName").toString(), variableMap, "-1234");

            }
        }catch (JMSException e){
            log.error(e.getMessage(), e);
        } catch (XPathExpressionException e) {
            log.error(e.getMessage(), e);
        }
    }

    private boolean isValidJsonString(String expression){
        try {
            new JSONObject(expression);
        } catch (JSONException e) {
            try {
                new JSONArray(expression);
            } catch (JSONException e1) {
                return false;
            }
        }
        return true;
    }
}
