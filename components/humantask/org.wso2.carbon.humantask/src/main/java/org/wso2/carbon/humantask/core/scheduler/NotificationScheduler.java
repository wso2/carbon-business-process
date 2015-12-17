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
import org.w3c.dom.NodeList;
import org.wso2.carbon.event.output.adapter.core.OutputEventAdapterConfiguration;
import org.wso2.carbon.event.output.adapter.core.exception.OutputEventAdapterException;
import org.wso2.carbon.humantask.core.HumanTaskConstants;
import org.wso2.carbon.humantask.core.dao.TaskCreationContext;
import org.wso2.carbon.humantask.core.dao.TaskDAO;
import org.wso2.carbon.humantask.core.engine.util.CommonTaskUtil;
import org.wso2.carbon.humantask.core.internal.HumanTaskServiceComponent;
import org.wso2.carbon.humantask.core.store.HumanTaskBaseConfiguration;
import org.wso2.carbon.humantask.core.utils.DOMUtils;
import org.xml.sax.SAXException;

import javax.xml.namespace.QName;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Retrieve notification type from human task to send related email/sms notifications
 */
public class NotificationScheduler {
    private static final Log log = LogFactory.getLog(NotificationScheduler.class);
    private boolean isEmailNotificationEnabled = false;
    private boolean isSMSNotificationEnabled = false;

    private ArrayList<String> emailAdapterNames;
    private ArrayList<String> smsAdapterNames;

    /**
     * Create and initialize emailAdaptor instance and SMS Adaptor instance that will be used to publish
     * email and sms notifications. Even though the method signature specifies that the init method throws an
     * exception, actual implementation does not throw such an exception and hence this exception would not occur.
     */
    public void init() {
        isEmailNotificationEnabled = HumanTaskServiceComponent.getHumanTaskServer()
                .getServerConfig().getEnableEMailNotification();
        isSMSNotificationEnabled = HumanTaskServiceComponent.getHumanTaskServer()
                .getServerConfig().getEnableSMSNotification();

        if(isEmailNotificationEnabled) {
            emailAdapterNames = new ArrayList<>();
        }

        if(isSMSNotificationEnabled) {
            smsAdapterNames = new ArrayList<>();
        }
    }

    /**
     * Check  availability of type email/sms notifications and publish notifications
     *
     * @param taskConfiguration
     * @param creationContext
     * @param task
     */
    public void checkForNotificationTasks(HumanTaskBaseConfiguration taskConfiguration,
                                          TaskCreationContext creationContext, TaskDAO task) {
        try {
            if (isEmailNotificationEnabled) {
                publishEmailNotifications(task, taskConfiguration);
            }
            if(isSMSNotificationEnabled) {
                publishSMSNotifications(task, taskConfiguration);
            }
        } catch (IOException | SAXException | ParserConfigurationException e) {
            log.error("Error publishing notifications via sms/email. But Notification creation will continue. ", e);
        }
    }

    /**
     * Publish SMS notifications by extracting the information from the incoming message rendering tags
     * <htd:renderings>
     *  <htd:rendering type="wso2:email" xmlns:wso2="http://wso2.org/ht/schema/renderings/">
     *     <wso2:to name="to" type="xsd:string">wso2bpsemail@wso2.com</wso2:to>
     *     <wso2:subject name="subject" type="xsd:string">email subject to user</wso2:subject>
     *     <wso2:body name="body" type="xsd:string">Hi email notifications</wso2:body>
     *  </htd:rendering>
     *  <htd:rendering type="wso2:sms" xmlns:wso2="http://wso2.org/ht/schema/renderings/">
     *      <wso2:receiver name="receiver" type="xsd:string">94777459299</wso2:receiver>
     *      <wso2:body name="body" type="xsd:string">Hi $firstname$</wso2:body>
     *  </htd:rendering>
     * </htd:renderings>
     * @param task Task Dao Object for this notification task
     * @param taskConfiguration task Configuration for this notification task instance
     */
    public void publishSMSNotifications(TaskDAO task, HumanTaskBaseConfiguration taskConfiguration)
            throws IOException, SAXException, ParserConfigurationException {

        String renderingSMS = CommonTaskUtil.getRendering(task, taskConfiguration,
                                          new QName(HumanTaskConstants.RENDERING_NAMESPACE,
                                                    HumanTaskConstants.RENDERING_TYPE_SMS));

        if (renderingSMS != null) {
            Map<String, String> dynamicPropertiesForSms = new HashMap<String, String>();
            Element rootSMS = DOMUtils.stringToDOM(renderingSMS);
            if (rootSMS != null) {
                String smsReceiver = null;
                String smsBody = null;
                if(log.isDebugEnabled()) {
                    log.debug("Parsing SMS notification rendering element 'receiver' for notification id " +
                                                task.getId());
                }
                NodeList smsReceiverList = rootSMS.getElementsByTagNameNS(HumanTaskConstants.RENDERING_NAMESPACE,
                                                                          HumanTaskConstants.SMS_RECEIVER_TAG);
                if(smsReceiverList != null && smsReceiverList.getLength() > 0) {
                    smsReceiver = smsReceiverList.item(0).getTextContent();
                } else {
                    log.warn("SMS notification rendering element 'receiver' not specified for notification with id " +
                            task.getId());
                }

                NodeList smsBodyList = rootSMS.getElementsByTagNameNS(HumanTaskConstants.RENDERING_NAMESPACE,
                                                                      HumanTaskConstants.EMAIL_OR_SMS_BODY_TAG);
                if(log.isDebugEnabled()) {
                    log.debug("Parsing SMS notification rendering element 'body' for notification id " +
                              task.getId());
                }

                if(smsBodyList != null && smsBodyList.getLength() > 0) {
                    smsBody = smsBodyList.item(0).getTextContent();
                } else {
                    log.warn("SMS notification rendering element 'body' not specified for notification with id " +
                              task.getId());
                }
                dynamicPropertiesForSms.put(HumanTaskConstants.ARRAY_SMS_NO, smsReceiver);
                String adaptorName = getAdaptorName(task.getName(), HumanTaskConstants.RENDERING_TYPE_SMS);
                if (!smsAdapterNames.contains(adaptorName)) {
                    OutputEventAdapterConfiguration outputEventAdapterConfiguration =
                            createOutputEventAdapterConfiguration(adaptorName, HumanTaskConstants.RENDERING_TYPE_SMS,
                                    HumanTaskConstants.SMS_MESSAGE_FORMAT);
                    try {
                        HumanTaskServiceComponent.getOutputEventAdapterService().create(outputEventAdapterConfiguration);
                        smsAdapterNames.add(adaptorName);
                    } catch (OutputEventAdapterException e) {
                        log.error("Unable to create Output Event Adapter : " + adaptorName, e);
                    }
                }
                HumanTaskServiceComponent.getOutputEventAdapterService().publish(adaptorName, dynamicPropertiesForSms, smsBody);

                //smsAdapter.publish(smsBody, dynamicPropertiesForSms);
            }
        } else {
            log.warn("SMS Rendering type not found for task definition with task id " + task.getId());
        }
    }

    /**
     * Publish Email notifications by extracting the information from the incoming message rendering tags
     * <htd:renderings>
     *  <htd:rendering type="wso2:email" xmlns:wso2="http://wso2.org/ht/schema/renderings/">
     *     <wso2:to name="to" type="xsd:string">wso2bpsemail@wso2.com</wso2:to>
     *     <wso2:subject name="subject" type="xsd:string">email subject to user</wso2:subject>
     *     <wso2:body name="body" type="xsd:string">Hi email notifications</wso2:body>
     *  </htd:rendering>
     *  <htd:rendering type="wso2:sms" xmlns:wso2="http://wso2.org/ht/schema/renderings/">
     *      <wso2:receiver name="receiver" type="xsd:string">94777459299</wso2:receiver>
     *      <wso2:body name="body" type="xsd:string">Hi $firstname$</wso2:body>
     *  </htd:rendering>
     *</htd:renderings>
     *
     * @param  task TaskDAO object for this notification task instance
     * @param taskConfiguration task configuration instance for this notification task definition
     */
    public void publishEmailNotifications(TaskDAO task, HumanTaskBaseConfiguration taskConfiguration)
            throws IOException, SAXException, ParserConfigurationException {

        String rendering = CommonTaskUtil.getRendering(task, taskConfiguration,
                new QName(HumanTaskConstants.RENDERING_NAMESPACE, HumanTaskConstants.RENDERING_TYPE_EMAIL));

        if (rendering != null) {
            Map<String, String> dynamicPropertiesForEmail = new HashMap<String, String>();

            Element root = DOMUtils.stringToDOM(rendering);
            if (root != null) {
                String emailBody = null;
                String mailSubject = null;
                String mailTo = null;
                String contentType = null;
                NodeList mailToList = root.getElementsByTagNameNS(HumanTaskConstants.RENDERING_NAMESPACE,
                                                                  HumanTaskConstants.EMAIL_TO_TAG);
                if(log.isDebugEnabled()) {
                    log.debug("Parsing Email notification rendering element to for notification id " + task.getId());
                }
                if(mailToList != null && mailToList.getLength() > 0) {
                    mailTo = mailToList.item(0).getTextContent();
                } else {
                        log.warn("Email to address not specified for email notification with notification id " +
                                  task.getId());
                }

                NodeList mailSubjectList = root.getElementsByTagNameNS(HumanTaskConstants.RENDERING_NAMESPACE,
                                                                       HumanTaskConstants.EMAIL_SUBJECT_TAG);
                if(log.isDebugEnabled()) {
                    log.debug("Paring Email notification rendering element subject " + task.getId());
                }
                if(mailSubjectList != null && mailSubjectList.getLength() > 0) {
                    mailSubject = mailSubjectList.item(0).getTextContent();
                } else {
                        log.warn("Email subject not specified for email notification with notification id " +
                                  task.getId());
                }

                NodeList mailContentType = root.getElementsByTagNameNS(HumanTaskConstants.RENDERING_NAMESPACE,
                        HumanTaskConstants.EMAIL_CONTENT_TYPE_TAG);
                if (log.isDebugEnabled()) {
                    log.debug("Paring Email notification rendering element contentType " + task.getId());
                }
                if (mailContentType != null && mailContentType.getLength() > 0) {
                    contentType = mailContentType.item(0).getTextContent();
                } else {
                    contentType = HumanTaskConstants.CONTENT_TYPE_TEXT_PLAIN;
                    log.warn("Email contentType not specified for email notification with notification id " +
                            task.getId() + ". Using text/plain.");
                }

                if(log.isDebugEnabled()) {
                    log.debug("Parsing Email notification rendering element body tag for notification id " +
                              task.getId());
                }
                NodeList emailBodyList = root.getElementsByTagNameNS(HumanTaskConstants.RENDERING_NAMESPACE,
                                                                     HumanTaskConstants.EMAIL_OR_SMS_BODY_TAG);

                if(emailBodyList != null && emailBodyList.getLength() > 0) {
                    emailBody = emailBodyList.item(0).getTextContent();
                } else {
                        log.warn("Email notification message body not specified for notification with id " +
                                  task.getId());
                }
                dynamicPropertiesForEmail.put(HumanTaskConstants.ARRAY_EMAIL_ADDRESS, mailTo);
                dynamicPropertiesForEmail.put(HumanTaskConstants.ARRAY_EMAIL_SUBJECT, mailSubject);
                dynamicPropertiesForEmail.put(HumanTaskConstants.ARRAY_EMAIL_TYPE, contentType);

                String adaptorName = getAdaptorName(task.getName(), HumanTaskConstants.RENDERING_TYPE_EMAIL);
                if (!emailAdapterNames.contains(adaptorName)) {
                    OutputEventAdapterConfiguration outputEventAdapterConfiguration =
                            createOutputEventAdapterConfiguration(adaptorName, HumanTaskConstants.RENDERING_TYPE_EMAIL,
                                    HumanTaskConstants.EMAIL_MESSAGE_FORMAT);
                    try {
                        HumanTaskServiceComponent.getOutputEventAdapterService().create(outputEventAdapterConfiguration);
                        emailAdapterNames.add(adaptorName);
                    } catch (OutputEventAdapterException e) {
                        log.error("Unable to create Output Event Adapter : " + adaptorName, e);
                    }
                }
                HumanTaskServiceComponent.getOutputEventAdapterService().publish(adaptorName, dynamicPropertiesForEmail, emailBody);
                //emailAdapter.publish(emailBody, dynamicPropertiesForEmail);
            }
        } else {
            log.warn("Email Rendering type not found for task definition with task id " + task.getId());
        }
    }

    /**
     * Create Output Event Adapter Configuration for given configuration.
     *
     * @param name      Output Event Adapter name
     * @param type      Output Event Adapter type
     * @param msgFormat Output Event Adapter message format
     * @return OutputEventAdapterConfiguration instance for given configuration
     */
    private static OutputEventAdapterConfiguration createOutputEventAdapterConfiguration(String name, String type, String msgFormat) {
        OutputEventAdapterConfiguration outputEventAdapterConfiguration = new OutputEventAdapterConfiguration();
        outputEventAdapterConfiguration.setName(name);
        outputEventAdapterConfiguration.setType(type);
        outputEventAdapterConfiguration.setMessageFormat(msgFormat);

        return outputEventAdapterConfiguration;
    }

    /**
     * Generate unique adaptor identifier for given task name and adaptor type.
     *
     * @param name Name of the adaptor
     * @param type Type of the adaptor
     * @return unique adaptor identifier string
     */
    private static String getAdaptorName(String name, String type) {
        return type + "_" + name.toUpperCase();
    }

}
