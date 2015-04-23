/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.wso2.carbon.event.output.adapter.core.exception.OutputEventAdapterException;
import org.wso2.carbon.event.output.adapter.email.EmailEventAdapter;
import org.wso2.carbon.event.output.adapter.sms.SMSEventAdapter;
import org.wso2.carbon.humantask.core.HumanTaskConstants;
import org.wso2.carbon.humantask.core.dao.TaskCreationContext;
import org.wso2.carbon.humantask.core.dao.TaskDAO;
import org.wso2.carbon.humantask.core.engine.util.CommonTaskUtil;
import org.wso2.carbon.humantask.core.internal.HumanTaskServiceComponent;
import org.wso2.carbon.humantask.core.store.HumanTaskBaseConfiguration;
import org.xml.sax.InputSource;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

/**
 * Retrieve notification type from human task to send related email/sms notifications
 */
public class NotificationScheduler {
    private static Log log = LogFactory.getLog(NotificationScheduler.class);
    private boolean isNotification = false;
    private boolean isEmailNotification = false;
    private boolean isSMSNotification = false;
    private static EmailEventAdapter emailAdapter;
    private static SMSEventAdapter smsAdapter;
    private Map<String, String> emailDynamicProperties;
    private Map<String, String> smsDynamicProperties;
    private String message;

    /**
     * Initialize EmailEventAdapter object
     *
     * @param dynamicProperties
     * @return
     */
    public static synchronized EmailEventAdapter getEmailEventAdapter(Map<String, String> dynamicProperties) {
        if (emailAdapter == null) {
            emailAdapter = new EmailEventAdapter(null, dynamicProperties);
        }
        try {
            emailAdapter.init();
        } catch (OutputEventAdapterException e) {
            log.error("Unable to initialize email adapter.", e);
        }
        return emailAdapter;
    }

    /**
     * Set email/sms message content
     *
     * @param messageBody
     * @return
     */
    public void setMessageForNotification(String messageBody) {
        this.message = messageBody;
    }

    /**
     * Get email/sms message content
     *
     * @return
     */
    public Object getMessageForNotification() {
        return message;
    }

    /**
     * Initialize SMSEventAdapter object
     *
     * @param dynamicProperties
     * @return
     */

    public static synchronized SMSEventAdapter getSMSEventAdapter(Map<String, String> dynamicProperties) {
        if (smsAdapter == null) {
            smsAdapter = new SMSEventAdapter(null, dynamicProperties);
        }
        try {
            smsAdapter.init();
        } catch (OutputEventAdapterException e) {
            log.error("Unable to initialize sms adapter.", e);
        }
        return smsAdapter;
    }

    /**
     * Check  availability of type email/sms notifications
     *
     * @param taskConfiguration
     * @param creationContext
     * @param task
     */
    public void checkForNotificationTasks(HumanTaskBaseConfiguration taskConfiguration,
                                          TaskCreationContext creationContext, TaskDAO task) {
        isNotification = taskConfiguration.getConfigurationType().toString().equalsIgnoreCase("notification");

        if (isNotification) {
            isEmailNotification = HumanTaskServiceComponent.getHumanTaskServer()
                    .getServerConfig().getEnableEMailNotification();
            isSMSNotification = HumanTaskServiceComponent.getHumanTaskServer()
                    .getServerConfig().getEnableSMSNotification();

            executeNotifications(taskConfiguration, creationContext, task, isEmailNotification, isSMSNotification);
        } else {
            if(log.isDebugEnabled()) {
                log.debug("Notification type tasks are not available in " + taskConfiguration.getName());
            }
        }
    }

    /**
     * Publish notifications of type email/sms
     *
     * @param taskConfiguration
     * @param context
     * @param task
     * @param isEmail
     * @param isSMS
     */
    public void executeNotifications(HumanTaskBaseConfiguration taskConfiguration, TaskCreationContext context,
                                     TaskDAO task, boolean isEmail, boolean isSMS) {
        if (isEmail) {
            emailDynamicProperties = getDynamicPropertiesOfEmailNotification(task, taskConfiguration); //get dynamic properties
            Object message = getMessageForNotification();//get email message body
            if (emailAdapter == null) {
                emailAdapter = getEmailEventAdapter(emailDynamicProperties); //singleton
            }
            emailAdapter.publish(message, emailDynamicProperties);
            if (log.isDebugEnabled()) {
                log.debug("sent email notifications of task" + task.getName());
            }
        }
        if (isSMS) {
            smsDynamicProperties = getDynamicPropertiesOfSmsNotification(task, taskConfiguration);
            Object message = getMessageForNotification(); //get sms message body
            if (smsAdapter == null) {
                smsAdapter = getSMSEventAdapter(smsDynamicProperties); // singleton
            }
            smsAdapter.publish(message, smsDynamicProperties);
            if (log.isDebugEnabled()) {
                log.debug("sent sms notifications of task" + task.getName());
            }
        }
    }

    /**
     * Get dynamic properties(smsReceiver,sms body) of a sms notification
     *
     * @param taskConfiguration
     * @return dynamicPropertiesForSms
     */
    public Map getDynamicPropertiesOfSmsNotification(TaskDAO task, HumanTaskBaseConfiguration taskConfiguration) {
        Map<String, String> dynamicPropertiesForSms = new HashMap<String, String>();
        String smsReceiver = null;
        String smsBody = null;
        String renderingSMS = CommonTaskUtil.getRendering(task, taskConfiguration,
                new QName(HumanTaskConstants.RENDERING_NAMESPACE, HumanTaskConstants.RENDERING_TYPE_SMS));

        if (renderingSMS != null) {
            Document document = xmlRead(renderingSMS);
            Element rootSMS = document.getDocumentElement();
            if (rootSMS != null) {
                try {
                    smsReceiver = rootSMS.getElementsByTagName(HumanTaskConstants.PREFIX + ":"
                            + HumanTaskConstants.SMS_RECEIVER_TAG)
                            .item(0).getTextContent();
                    smsBody = rootSMS.getElementsByTagName(HumanTaskConstants.PREFIX + ":"
                            + HumanTaskConstants.EMAIL_OR_SMS_BODY_TAG).item(0) .getTextContent();
                } catch (DOMException e) {
                    log.error("Error while evaluating rendering xpath for sms content. Please review xpath and deploy "
                            + "task again.", e);
                }
                setMessageForNotification(smsBody);
            }

        } else {
            log.warn("Rendering type " + renderingSMS + "Not found for task definition.");
        }
        dynamicPropertiesForSms.put(HumanTaskConstants.ARRAY_SMS_NO, smsReceiver);
        return dynamicPropertiesForSms;
    }

    /**
     * Get dynamic properties(receiver,subject,email body) of an email notification
     *
     * @param taskConfiguration
     * @return dynamicPropertiesForEmail
     */
    public Map getDynamicPropertiesOfEmailNotification(TaskDAO task, HumanTaskBaseConfiguration taskConfiguration) {

        Map<String, String> dynamicPropertiesForEmail = new HashMap<String, String>();
        String emailBody = null;
        String mailSubject = null;
        String mailTo = null;

        String rendering = CommonTaskUtil.getRendering(task, taskConfiguration,
                new QName(HumanTaskConstants.RENDERING_NAMESPACE, HumanTaskConstants.RENDERING_TYPE_EMAIL));

        if (rendering != null) {
            Document document = xmlRead(rendering);
            Element root = document.getDocumentElement();
            if (root != null) {
                try {
                    mailTo = root.getElementsByTagName(HumanTaskConstants.PREFIX + ":"
                            + HumanTaskConstants.EMAIL_TO_TAG)
                            .item(0).getTextContent();
                    mailSubject = root.getElementsByTagName(HumanTaskConstants.PREFIX + ":"
                            + HumanTaskConstants.EMAIL_SUBJECT_TAG).item(0)
                            .getTextContent();
                    emailBody = root.getElementsByTagName(HumanTaskConstants.PREFIX + ":"
                            + HumanTaskConstants.EMAIL_OR_SMS_BODY_TAG)
                            .item(0).getTextContent();

                } catch (DOMException e) {
                    log.error("Error while evaluating rendering xpath. Please review xpath and deploy " +
                            "task again.", e);
                }
                setMessageForNotification(emailBody);
            }
        } else {
            log.warn("Rendering type " + rendering + "Not found for task definition.");
        }
        dynamicPropertiesForEmail.put(HumanTaskConstants.ARRAY_EMAIL_ADDRESS, mailTo);
        dynamicPropertiesForEmail.put(HumanTaskConstants.ARRAY_EMAIL_SUBJECT, mailSubject);
        return dynamicPropertiesForEmail;
    }

    /**
     * Creating Document object(xml parsing) from string content
     *
     * @param xmlString
     * @return
     */
    public Document xmlRead(String xmlString) {
        Document doc = null;
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            doc = builder.parse(new InputSource(new StringReader(xmlString)));
            doc.getDocumentElement().normalize();

        } catch (ParserConfigurationException e) {
            log.error("unable to parse content" + xmlString, e);

        } catch (SAXException e) {
            log.error("unable to parse content" + xmlString, e);

        } catch (IOException e) {
            log.error("unable to parse content" + xmlString, e);
        }
        return doc;
    }
}
