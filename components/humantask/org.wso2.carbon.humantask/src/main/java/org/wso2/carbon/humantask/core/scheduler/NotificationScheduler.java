/*
 * Copyright (c) 2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.humantask.core.scheduler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.NodeList;
import org.wso2.carbon.event.output.adapter.email.EmailEventAdapter;
import org.wso2.carbon.event.output.adapter.sms.SMSEventAdapter;
import org.wso2.carbon.humantask.core.HumanTaskConstants;
import org.wso2.carbon.humantask.core.dao.TaskCreationContext;
import org.wso2.carbon.humantask.core.dao.TaskDAO;
import org.wso2.carbon.humantask.core.engine.runtime.api.EvaluationContext;
import org.wso2.carbon.humantask.core.engine.runtime.api.ExpressionLanguageRuntime;
import org.wso2.carbon.humantask.core.engine.util.CommonTaskUtil;
import org.wso2.carbon.humantask.core.internal.HumanTaskServiceComponent;
import org.wso2.carbon.humantask.core.store.HumanTaskBaseConfiguration;
import org.xml.sax.InputSource;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public class NotificationScheduler {
    private static Log log = LogFactory.getLog(NotificationScheduler.class);
    private boolean isNotification = false;
    private boolean isEmailNotification = false;
    private boolean isSMSNotification = false;
    private boolean taskCompleted = false;
    private static EmailEventAdapter emailAdapter;
    private static SMSEventAdapter smsAdapter;
    private ExecutorService exec;
    private NotificationTask notificationTask;
    private Map<String, String> emailDynamicProperties;
    private Map<String, String> smsDynamicProperties;


    private String prefix = "";
    String mail_to = null;
    String ccEmail = null;
    String bccEmail = null;
    String mail_subject = null;
    String emailBody = null;
    String emailBodyTagVal = null;
    String smsReceiver = null;
    String smsBodyTagVal = null;
    String smsBody = null;


    public void setExecutorService(ExecutorService executorService) {
        exec = executorService;
    }

    public static synchronized EmailEventAdapter getEmailEventAdapter(Map<String, String> dynamicProperties) {
        if (emailAdapter == null) {
            emailAdapter = new EmailEventAdapter(null, dynamicProperties);

        }
        return emailAdapter;
    }

    public static synchronized SMSEventAdapter getSMSEventAdapter(Map<String, String> dynamicProperties) {
        if (smsAdapter == null) {
            smsAdapter = new SMSEventAdapter(null, dynamicProperties);

        }
        return smsAdapter;
    }

    public void checkForNotificationTasks(HumanTaskBaseConfiguration taskConfiguration, TaskCreationContext creationContext, TaskDAO task) {
        isNotification = taskConfiguration.getConfigurationType().toString().equalsIgnoreCase("notification");

        if (isNotification) {
            isEmailNotification = HumanTaskServiceComponent.getHumanTaskServer()
                    .getServerConfig().getEnableEMailNotification();
            isSMSNotification = HumanTaskServiceComponent.getHumanTaskServer()
                    .getServerConfig().getEnableSMSNotification();

            executeNotifications(taskConfiguration, creationContext, task, isEmailNotification, isSMSNotification);

        } else {
            log.info("There are no type notification tasks");
        }
    }

    public void executeNotifications(HumanTaskBaseConfiguration taskConfiguration, TaskCreationContext context,
                                     TaskDAO task, boolean isEmail, boolean isSMS) {

        if (isEmail) {
            emailDynamicProperties = getDynamicPropertiesOfEmailNotification(taskConfiguration); //get dynamic properties
            Object message = getMessageOfEmailNotification(taskConfiguration, context, task);
            //emailAdapter = getEmailEventAdapter(emailDynamicProperties); //singleton
            notificationTask = new NotificationTask(getEmailEventAdapter(emailDynamicProperties), message, emailDynamicProperties);//make private prop and add it
            exec.submit(notificationTask);


            exec.shutdown();
            try {
                taskCompleted = exec.awaitTermination(1, TimeUnit.MINUTES);
                log.info("All e-mails were sent so far? {}" + taskCompleted);
            } catch (InterruptedException e) {
                log.error("Error during Email notification task execution", e);
            }
        } else if (isSMS) {
            smsDynamicProperties = getDynamicPropertiesOfSmsNotification(taskConfiguration);
            Object message = getMessageOfSmsNotification(taskConfiguration, context, task);
            //smsAdapter = getSMSEventAdapter(smsDynamicProperties); // singleton
            notificationTask = new NotificationTask(getSMSEventAdapter(smsDynamicProperties), message, smsDynamicProperties);
            exec.submit(notificationTask);

            exec.shutdown();
            try {
                taskCompleted = exec.awaitTermination(1, TimeUnit.MINUTES);
                log.info("All sms were sent so far? {}" + taskCompleted);
            } catch (InterruptedException e) {

                log.error("Error during sms notification task execution", e);
            }
        }
    }

    public Map getDynamicPropertiesOfSmsNotification(HumanTaskBaseConfiguration taskConfiguration) {
        Map<String, String> dynamicPropertiesForSms = new HashMap<String, String>();

        Document document = xmlRead(taskConfiguration.getRenderings().toString());
        NodeList node = document.getElementsByTagName(prefix + ":"
                + HumanTaskConstants.RENDERING_TAG);
        for (int i = 0; i < node.getLength(); i++) {
            if ((node.item(i).getAttributes().getNamedItem("type").getTextContent())
                    .equals(HumanTaskConstants.RENDERING_TYPE_SMS)) {
                Document doc = xmlRead(taskConfiguration.getRenderings().getRenderingArray(i)
                        .toString());
                smsReceiver = doc
                        .getElementsByTagName(prefix + ":" + HumanTaskConstants.SMS_RECEIVER_TAG)
                        .item(0).getTextContent();
                smsBodyTagVal = doc
                        .getElementsByTagName(
                                prefix + ":" + HumanTaskConstants.EMAIL_OR_SMS_BODY_TAG).item(0)
                        .getTextContent();
            }
        }
        dynamicPropertiesForSms.put("sms.no", smsReceiver);
        // dynamicPropertiesForSms.put("smsBodyTagVal", smsBodyTagVal);

        return dynamicPropertiesForSms;
    }

    public Map getDynamicPropertiesOfEmailNotification(HumanTaskBaseConfiguration taskConfiguration) {


        Map<String, String> dynamicPropertiesForEmail = new HashMap<String, String>();

        Document document = xmlRead(taskConfiguration.getRenderings().toString());
        NodeList node = document.getElementsByTagName(prefix + ":"
                + HumanTaskConstants.RENDERING_TAG);
        for (int i = 0; i < node.getLength(); i++) {
            if ((node.item(i).getAttributes().getNamedItem("type").getTextContent())
                    .equals(HumanTaskConstants.RENDERING_TYPE_EMAIL)) {
                Document doc = xmlRead(taskConfiguration.getRenderings().getRenderingArray(i)
                        .toString());
                mail_to = doc
                        .getElementsByTagName(prefix + ":" + HumanTaskConstants.EMAIL_TO_TAG)
                        .item(0).getTextContent();
                ccEmail = doc
                        .getElementsByTagName(prefix + ":" + HumanTaskConstants.EMAIL_CC_TAG)
                        .item(0).getTextContent();
                bccEmail = doc
                        .getElementsByTagName(prefix + ":" + HumanTaskConstants.EMAIL_BCC_TAG)
                        .item(0).getTextContent();
                mail_subject = doc
                        .getElementsByTagName(
                                prefix + ":" + HumanTaskConstants.EMAIL_SUBJECT_TAG).item(0)
                        .getTextContent();
                emailBodyTagVal = doc
                        .getElementsByTagName(
                                prefix + ":" + HumanTaskConstants.EMAIL_OR_SMS_BODY_TAG)
                        .item(0).getTextContent();
            }

        }
        dynamicPropertiesForEmail.put("email.address", mail_to);
        dynamicPropertiesForEmail.put("email.subject", mail_subject);
        dynamicPropertiesForEmail.put("ccEmail", ccEmail);
        dynamicPropertiesForEmail.put("bccEmail", bccEmail);

        return dynamicPropertiesForEmail;
    }

    public String getMessageOfSmsNotification(HumanTaskBaseConfiguration taskConfiguration, TaskCreationContext context,
                                              TaskDAO task) {

        smsBody = evaluateTextForXPath(smsBodyTagVal, taskConfiguration, context, task);
        return smsBody;
    }

    public String getMessageOfEmailNotification(HumanTaskBaseConfiguration taskConfiguration, TaskCreationContext context,
                                                TaskDAO task) {
        emailBody = evaluateTextForXPath(emailBodyTagVal, taskConfiguration, context,
                task);
        return emailBody;
    }

    public String evaluateTextForXPath(String xmlString,
                                       HumanTaskBaseConfiguration taskConfiguration, TaskCreationContext creationContext,
                                       TaskDAO task) {
        String expressionLanguage = taskConfiguration.getExpressionLanguage();
        ExpressionLanguageRuntime expLangRuntime = HumanTaskServiceComponent.getHumanTaskServer()
                .getTaskEngine().getExpressionLanguageRuntime(expressionLanguage);
        EvaluationContext evalContext = creationContext.getEvalContext();
        String processedString = resolveJAXP(xmlString, task);
        String[] split = processedString.split("\\s+");
        StringBuilder builder = new StringBuilder();
        for (String element : split) {
            if (element.startsWith(prefix + ":")) {
                builder.append(" " + expLangRuntime.evaluateAsString(element, evalContext));
            } else {
                builder.append(" " + element);
            }
        }

        return builder.toString();
    }

    public String resolveJAXP(String xmlString, TaskDAO task) {
        String replacedXmlString = CommonTaskUtil.replaceUsingPresentationParams(
                task.getPresentationParameters(), xmlString);
        return replacedXmlString;
    }

    public Document xmlRead(String xmlString) {
        Document doc = null;
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            doc = builder.parse(new InputSource(new StringReader(xmlString)));
            doc.getDocumentElement().normalize();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return doc;
    }

}
