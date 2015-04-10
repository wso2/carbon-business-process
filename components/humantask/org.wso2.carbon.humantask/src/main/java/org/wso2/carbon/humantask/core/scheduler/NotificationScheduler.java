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
import org.w3c.dom.Element;

import org.wso2.carbon.event.output.adapter.core.OutputEventAdapterConfiguration;
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

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;


public class NotificationScheduler {
    private static Log log = LogFactory.getLog(NotificationScheduler.class);
    private boolean isNotification = false;
    private boolean isEmailNotification = false;
    private boolean isSMSNotification = false;
    private static EmailEventAdapter emailAdapter;
    private static SMSEventAdapter smsAdapter;
    private Map<String, String> emailDynamicProperties;
    private Map<String, String> smsDynamicProperties;

    private String prefix = "htd";
    String mailTo = null;
    String mailSubject = null;
    String emailBody = null;
    String smsReceiver = null;
    String smsBody = null;


    public NotificationScheduler() {
        emailAdapter = new EmailEventAdapter(null, null);
        try {
            emailAdapter.init();
        } catch (OutputEventAdapterException e) {
            log.error("unable to initialize adapter" + emailAdapter, e);

        }
        smsAdapter = new SMSEventAdapter(null, null);
        try {
            smsAdapter.init();
        } catch (OutputEventAdapterException e) {
            log.error("unable to initialize adapter" + emailAdapter, e);

        }
    }

    /**
     * Initialize EmailEventAdapter object
     *
     * @param dynamicProperties
     * @return
     */
    public EmailEventAdapter getEmailEventAdapter(Map<String, String> dynamicProperties) {
        emailAdapter = new EmailEventAdapter(null, dynamicProperties);

        try {
            emailAdapter.init();
        } catch (OutputEventAdapterException e) {
            log.error("unable to initialize adapter" + emailAdapter, e);

        }

        return emailAdapter;
    }

    /**
     * Initialize SMSEventAdapter object
     *
     * @param dynamicProperties
     * @return
     */
    public SMSEventAdapter getSMSEventAdapter(Map<String, String> dynamicProperties) {

        //Need staticProperties at init()

        OutputEventAdapterConfiguration configuration = new OutputEventAdapterConfiguration();
        configuration.setName(HumanTaskConstants.SMS_CONFIG_NAME);
        configuration.setType(HumanTaskConstants.SMS_CONFIG_TYPE);
        configuration.setStaticProperties(dynamicProperties);
        configuration.setMessageFormat(HumanTaskConstants.SMS_CONFIG_FORMAT);

        smsAdapter = new SMSEventAdapter(configuration, dynamicProperties);


        try {
            smsAdapter.init();
        } catch (OutputEventAdapterException e) {
            log.error("unable to initialize adapter" + emailAdapter, e);

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
    public void checkForNotificationTasks(HumanTaskBaseConfiguration taskConfiguration, TaskCreationContext creationContext, TaskDAO task) {

        isNotification = taskConfiguration.getConfigurationType().toString().equalsIgnoreCase("notification");

        if (isNotification) {
            isEmailNotification = HumanTaskServiceComponent.getHumanTaskServer()
                    .getServerConfig().getEnableEMailNotification();
            isSMSNotification = HumanTaskServiceComponent.getHumanTaskServer()
                    .getServerConfig().getEnableSMSNotification();

            publishNotifications(taskConfiguration, creationContext, task, isEmailNotification, isSMSNotification);

        } else {
            log.info("Notification type tasks are not available in " + taskConfiguration);
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
    public void publishNotifications(HumanTaskBaseConfiguration taskConfiguration, TaskCreationContext context,
                                     TaskDAO task, boolean isEmail, boolean isSMS) {

        if (isEmail) {
            emailDynamicProperties = getDynamicPropertiesOfEmailNotification(task, taskConfiguration); //get dynamic properties
            Object message = emailBody;//get email message body
            emailAdapter = getEmailEventAdapter(emailDynamicProperties);
            emailAdapter.publish(message, emailDynamicProperties);
            log.info("sent email notifications");
        }
        if (isSMS) {
            smsDynamicProperties = getDynamicPropertiesOfSmsNotification(task, taskConfiguration);
            Object message = smsBody; //get sms message body
            smsAdapter = getSMSEventAdapter(smsDynamicProperties);
            smsAdapter.publish(message, smsDynamicProperties);
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
        String renderingSMS = CommonTaskUtil.getRendering(task, taskConfiguration, new QName(HumanTaskConstants.RENDERING_NAMESPACE, HumanTaskConstants.RENDERING_TYPE_SMS));


        if (renderingSMS != null) {
            Document document = xmlRead(renderingSMS);
            Element rootSMS = document.getDocumentElement();

            try {
                smsReceiver = rootSMS.getElementsByTagName(prefix + ":" + HumanTaskConstants.SMS_RECEIVER_TAG)
                        .item(0).getTextContent();
                smsBody = rootSMS
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

    /**
     * Get dynamic properties(receiver,subject,email body) of an email notification
     *
     * @param taskConfiguration
     * @return dynamicPropertiesForEmail
     */
    public Map getDynamicPropertiesOfEmailNotification(TaskDAO task, HumanTaskBaseConfiguration taskConfiguration) {


        Map<String, String> dynamicPropertiesForEmail = new HashMap<String, String>();
        String rendering = CommonTaskUtil.getRendering(task, taskConfiguration, new QName(HumanTaskConstants.RENDERING_NAMESPACE, HumanTaskConstants.RENDERING_TYPE_EMAIL));


        if (rendering != null) {
            Document document = xmlRead(rendering);
            Element root = document.getDocumentElement();


            try {

                mailTo = root.getElementsByTagName(prefix + ":" + HumanTaskConstants.EMAIL_TO_TAG)
                        .item(0).getTextContent();
                mailSubject = root.getElementsByTagName(prefix + ":" + HumanTaskConstants.EMAIL_SUBJECT_TAG).item(0)
                        .getTextContent();
                emailBody = root.getElementsByTagName(prefix + ":" + HumanTaskConstants.EMAIL_OR_SMS_BODY_TAG)
                        .item(0).getTextContent();


            } catch (Exception e) {
                log.error("Error while evaluating rendering xpath. Please review xpath and deploy " +
                        "task again.", e);
            }
        } else {
            log.warn("Rendering type " + rendering + "Not found for task definition.");
        }

        dynamicPropertiesForEmail.put("email.address", mailTo);
        dynamicPropertiesForEmail.put("email.subject", mailSubject);


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

        } catch (Exception e) {
            log.error("unable to parse content" + xmlString, e);
        }
        return doc;
    }

}
