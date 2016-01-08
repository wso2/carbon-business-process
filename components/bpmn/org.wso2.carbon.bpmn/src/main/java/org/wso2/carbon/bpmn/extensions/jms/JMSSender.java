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

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.activiti.engine.impl.el.JuelExpression;
import org.apache.axiom.om.OMElement;
import org.apache.axis2.transport.base.BaseConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.axis2.context.MessageContext;
import javax.jms.*;



public class JMSSender implements JavaDelegate {

    private JuelExpression destinationName;
    private JuelExpression input;
    private JuelExpression destinationType;
    private JuelExpression serviceProviderID;
    private JuelExpression cacheLevel;

    Log log = LogFactory.getLog(JMSSender.class);

    /**
     *
     * @param execution
     */
    @Override
    public void execute(DelegateExecution execution) {

        String providerID;
        String destType;
        String destName;
        String text;
        //String cache;

        JMSConnectionFactory conFac = null;


        providerID = serviceProviderID.getValue(execution).toString();
        destType = destinationType.getValue(execution).toString();
        destName = destinationName.getValue(execution).toString();
        text = input.getValue(execution).toString();
        // = cacheLevel.getValue(execution).toString();


        if(JMSConstants.DESTINATION_TYPE_QUEUE.equalsIgnoreCase(destType)){
            conFac = JMSConnectionFactoryManager.getInstance().getConnectionFactory(JMSConstants.JMS_QUEUE_CONNECTION_FACTORY);
        } else if (JMSConstants.DESTINATION_TYPE_TOPIC.equalsIgnoreCase(destType)) {
            conFac = JMSConnectionFactoryManager.getInstance().getConnectionFactory(JMSConstants.JMS_TOPIC_CONNECTION_FACTORY);
        }else {
            log.error("Invalid destination type: " + destinationType);
        }


        sendMessage(conFac, conFac.getCacheLevel(), destName, text);

    }




    /**
     * Sends a message to a queue/topic
     * @param connectionFactory
     * @param text
     */

    public void sendMessage(JMSConnectionFactory connectionFactory, int cacheLevel, String target, String text )
    {

        try {

        Connection connection = connectionFactory.getConnection();

        Session session = connectionFactory.getSession(connection);
        Destination destination;

            if(connectionFactory.isQueue()) {
                destination = session.createQueue(target);
                if(destination == null){
                }
            }
            else{
                destination = session.createTopic(target);
            }

            if(destination != null) {

                log.info("initializing jms message producer..");
                MessageProducer producer = connectionFactory.getMessageProducer(connection, session, destination);

                JMSMessageSender jmsMessageSender = new JMSMessageSender(connection, session, producer, destination,
                        cacheLevel, connectionFactory.isQueue());

                TextMessage message = session.createTextMessage(text);

                jmsMessageSender.send(message, null);

            }
            else{
                log.error("Destination not specified");
            }
        }catch (JMSException ex){
            log.error(ex.toString(), ex);
        }
    }



    /**
     * Guess the message type to use for JMS looking at the message contexts' envelope
     * @param msgContext the message context
     * @return JMSConstants.JMS_BYTE_MESSAGE or JMSConstants.JMS_TEXT_MESSAGE or null
     */
    private String guessMessageType(MessageContext msgContext) {
        OMElement firstChild = msgContext.getEnvelope().getBody().getFirstElement();
        if (firstChild != null) {
            if (BaseConstants.DEFAULT_BINARY_WRAPPER.equals(firstChild.getQName())) {
                return JMSConstants.JMS_BYTE_MESSAGE;
            } else if (BaseConstants.DEFAULT_TEXT_WRAPPER.equals(firstChild.getQName())) {
                return JMSConstants.JMS_TEXT_MESSAGE;
            }
        }
        return null;
    }





}
