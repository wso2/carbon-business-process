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

import org.activiti.engine.delegate.BpmnError;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.activiti.engine.impl.el.JuelExpression;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bpmn.core.BPMNConstants;
import org.wso2.carbon.utils.CarbonUtils;

import javax.naming.Context;
import javax.xml.namespace.QName;

import java.io.File;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Properties;

/**
 * Created by dilini on 11/30/15.
 */
public class JMSSender implements JavaDelegate{

    private static final Log log = LogFactory.getLog(JMSSender.class);

    private static final String JMS_INVOKE_ERROR = "JMSInvokeError";

    private JMSInvoker jmsInvoker;

    private JuelExpression inputMessage;
    private JuelExpression destinationType;
    private JuelExpression destination;
    private JuelExpression jmsProvider;

    public JMSSender(){   jmsInvoker = BPMNJMSExtensionHolder.getInstance().getJMSInvoker();  }


    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {
        if(log.isDebugEnabled()){
            log.debug("Executing JMSInvokerTask ");
        }

        String destType = null;
        String dest = null;
        String initContextFactory = null;
        String provURL = null;
        String jmsProviderID = null;

        if(jmsProvider == null) {
            String jmsProviderNotFoundError = "JMS Provider is not provided for " +
                    getTaskDetails(delegateExecution) + ". jmsProvider must be provided.";
            throw new BPMNJMSException(jmsProviderNotFoundError);

        }else{
            jmsProviderID = jmsProvider.getValue(delegateExecution).toString();
            Hashtable<String, String> propertyList = ActiviitiFileReader.readJMSProviderInformation(jmsProviderID);
            initContextFactory = propertyList.get(BPMNConstants.JMS_INITIAL_CONTEXT_FACTORY);
            provURL = propertyList.get(BPMNConstants.JMS_PROVIDER_URL);
        }

        try {

            if(destinationType != null){
                destType = destinationType.getValue(delegateExecution).toString();
            }else{
                String destTypeNotFoundErrorMsg = "Destination Type is not provided for " +
                        getTaskDetails(delegateExecution) + ". destinationType must be provided.";
                throw new BPMNJMSException(destTypeNotFoundErrorMsg);
            }

            if(destination != null){
                dest = destination.getValue(delegateExecution).toString();
            }else{
                String destNotFoundErrorMsg = "Destination is not provided for " +
                        getTaskDetails(delegateExecution) + ". destination must be provided.";
                throw new BPMNJMSException(destNotFoundErrorMsg);
            }

            String inputContent = inputMessage.getValue(delegateExecution).toString();

            Properties properties = new Properties();
            properties.setProperty(Context.INITIAL_CONTEXT_FACTORY, initContextFactory);
            properties.setProperty(Context.PROVIDER_URL, provURL);

            if(destType != null && destType.toLowerCase().equals("queue")){
                properties.setProperty("queue." + dest, dest);
                jmsInvoker.sendMessage(inputContent, properties, dest);

            }else if (destType != null && destType.toLowerCase().equals("topic")){
                properties.setProperty("topic." + dest, dest);
                jmsInvoker.publishMessage(inputContent, properties, dest);
            }
        }catch (Exception e){
            String errorMessage = "Failed to execute "+ getTaskDetails(delegateExecution);
            log.error(errorMessage, e);
            throw new BpmnError(JMS_INVOKE_ERROR, errorMessage);
        }
    }

    private String getTaskDetails(DelegateExecution execution) {
        String task = execution.getCurrentActivityId() + ":" + execution.getCurrentActivityName() + " in process instance " + execution.getProcessInstanceId();
        return task;
    }

    public void setJmsInvoker(JMSInvoker jmsInvoker) {
        this.jmsInvoker = jmsInvoker;
    }

    public void setInputMessage(JuelExpression inputMessage) {
        this.inputMessage = inputMessage;
    }

    public void setDestinationType(JuelExpression destinationType) {
        this.destinationType = destinationType;
    }

    public void setDestination(JuelExpression destination) {
        this.destination = destination;
    }

    public void setJmsProvider(JuelExpression jmsProvider){ this.jmsProvider = jmsProvider; }
}
