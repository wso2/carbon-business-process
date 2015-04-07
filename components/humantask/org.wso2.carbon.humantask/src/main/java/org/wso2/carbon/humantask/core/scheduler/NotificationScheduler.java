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
import org.w3c.dom.Element;

import org.wso2.carbon.event.output.adapter.core.OutputEventAdapterConfiguration;
import org.wso2.carbon.event.output.adapter.core.exception.OutputEventAdapterException;
import org.wso2.carbon.event.output.adapter.email.EmailEventAdapter;
import org.wso2.carbon.event.output.adapter.sms.SMSEventAdapter;
import org.wso2.carbon.humantask.TRendering;
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

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;


public class NotificationScheduler {
    private static Log log = LogFactory.getLog(NotificationScheduler.class);
    private boolean isNotification = false;
    private boolean isEmailNotification = false;
    private boolean isSMSNotification = false;
    private boolean taskCompleted = false;
    private static EmailEventAdapter emailAdapter;
    private static SMSEventAdapter smsAdapter;
    private NotificationScheduler notificationScheduler;
    private ExecutorService exec;
    private NotificationTask notificationTask;
    private Map<String, String> emailDynamicProperties;
    private Map<String, String> smsDynamicProperties;


    private String prefix = "htd";
    String mail_to = null;
    //String ccEmail = null;
    // String bccEmail = null;
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
        try {
            emailAdapter.init();
        } catch (OutputEventAdapterException e) {
             log.error("unable to initialize adapter"+ emailAdapter + e);

        }

        return emailAdapter;
    }

    public static synchronized SMSEventAdapter getSMSEventAdapter(Map<String, String> dynamicProperties) {
        if (smsAdapter == null) {
            //Need staticProperties at init()

            OutputEventAdapterConfiguration configuration = new OutputEventAdapterConfiguration();
            configuration.setName("smsNotification");
            configuration.setType("sms");
            configuration.setStaticProperties(dynamicProperties);
            configuration.setMessageFormat("text");

            smsAdapter = new SMSEventAdapter(configuration, dynamicProperties);
        }

        try {
            smsAdapter.init();
        } catch (OutputEventAdapterException e) {
            log.error("unable to initialize adapter"+ emailAdapter + e);

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
            Object message = getMessageOfEmailNotification(taskConfiguration, context, task);//get email message body
            emailAdapter = getEmailEventAdapter(emailDynamicProperties); //singleton
            notificationTask = new NotificationTask(emailAdapter, message, emailDynamicProperties);//make private prop and add it
            exec.submit(notificationTask);
        }
        if (isSMS) {
            smsDynamicProperties = getDynamicPropertiesOfSmsNotification(taskConfiguration);
            Object message = getMessageOfSmsNotification(taskConfiguration, context, task);//get sms message body
            smsAdapter = getSMSEventAdapter(smsDynamicProperties); // singleton
            notificationTask = new NotificationTask(smsAdapter, message, smsDynamicProperties);
            exec.submit(notificationTask);

        }
    }

    public Map getDynamicPropertiesOfSmsNotification(HumanTaskBaseConfiguration taskConfiguration) {
        Map<String, String> dynamicPropertiesForSms = new HashMap<String, String>();
        //TODO:check for namespace 
        TRendering renderingSMS = taskConfiguration.getRendering(new QName(HumanTaskConstants.RENDERING_NAMESPACE, HumanTaskConstants.RENDERING_TYPE_SMS));

        if (renderingSMS != null) {
            Document document = xmlRead(renderingSMS.toString());
            Element rootSMS = document.getDocumentElement();

            try {
                smsReceiver = rootSMS.getElementsByTagName(prefix + ":" + HumanTaskConstants.SMS_RECEIVER_TAG)
                        .item(0).getTextContent();
                smsBodyTagVal = rootSMS
                        .getElementsByTagName(
                                prefix + ":" + HumanTaskConstants.EMAIL_OR_SMS_BODY_TAG).item(0)
                        .getTextContent();
            } catch (Exception e) {
                log.error("Error while evaluating rendering xpath for sms content. Please review xpath and deploy " +
                        "task again.", e);
            }

        } else {
            log.warn("Rendering type " + renderingSMS + "Not found for task definition.");
        }
        dynamicPropertiesForSms.put("sms.no", smsReceiver);

        return dynamicPropertiesForSms;
    }

    public Map getDynamicPropertiesOfEmailNotification(HumanTaskBaseConfiguration taskConfiguration) {


        Map<String, String> dynamicPropertiesForEmail = new HashMap<String, String>();
        //TODO:check for namespace
        TRendering rendering = taskConfiguration.getRendering(new QName(HumanTaskConstants.RENDERING_NAMESPACE, HumanTaskConstants.RENDERING_TYPE_EMAIL));

        if (rendering != null) {
            Document document = xmlRead(rendering.toString());
            Element root = document.getDocumentElement();

            try {

                mail_to = root.getElementsByTagName(prefix + ":" + HumanTaskConstants.EMAIL_TO_TAG)
                        .item(0).getTextContent();
                mail_subject = root.getElementsByTagName(prefix + ":" + HumanTaskConstants.EMAIL_SUBJECT_TAG).item(0)
                        .getTextContent();
                emailBodyTagVal = root.getElementsByTagName(prefix + ":" + HumanTaskConstants.EMAIL_OR_SMS_BODY_TAG)
                        .item(0).getTextContent();
            } catch (Exception e) {
                log.error("Error while evaluating rendering xpath. Please review xpath and deploy " +
                        "task again.", e);
            }
        } else {
            log.warn("Rendering type " + rendering + "Not found for task definition.");
        }

        dynamicPropertiesForEmail.put("email.address", mail_to);
        dynamicPropertiesForEmail.put("email.subject", mail_subject);


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
