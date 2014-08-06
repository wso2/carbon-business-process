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

package org.wso2.carbon.humantask.core.engine;

import org.wso2.carbon.humantask.core.HumanTaskConstants;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.transport.mail.MailConstants;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.wso2.carbon.bpel.common.WSDLAwareMessage;
import org.wso2.carbon.humantask.core.api.scheduler.Scheduler;
import org.wso2.carbon.humantask.core.dao.HumanTaskDAOConnectionFactory;
import org.wso2.carbon.humantask.core.dao.TaskCreationContext;
import org.wso2.carbon.humantask.core.dao.TaskDAO;
import org.wso2.carbon.humantask.core.engine.event.processor.EventProcessor;
import org.wso2.carbon.humantask.core.engine.runtime.api.EvaluationContext;
import org.wso2.carbon.humantask.core.engine.runtime.api.ExpressionLanguageRuntime;
import org.wso2.carbon.humantask.core.engine.runtime.xpath.XPathExpressionRuntime;
import org.wso2.carbon.humantask.core.engine.util.CommonTaskUtil;
import org.wso2.carbon.humantask.core.internal.HumanTaskServiceComponent;
import org.wso2.carbon.humantask.core.store.HumanTaskBaseConfiguration;
import org.wso2.carbon.humantask.core.store.HumanTaskStore;
import org.xml.sax.InputSource;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Callable;

import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.stream.XMLStreamException;

/**
 * The human task engine. The responsibility of this class is to invoke task
 * creation logic.
 */
public class HumanTaskEngine {
	/**
	 * The DAO connection factory as an abstraction to the underlying
	 * persistence implementation
	 */
	private HumanTaskDAOConnectionFactory daoConnectionFactory;

	/**
	 * The people query evaluator
	 */
	private PeopleQueryEvaluator peopleQueryEvaluator;

	/** The expression */
	private Map<String, ExpressionLanguageRuntime> expressionLanguageRuntimeRegistry;

	/** */
	private EventProcessor eventProcessor;

	/**
	 * Deadline scheduler
	 */
	private Scheduler scheduler;
	private String prefix = "";

	public HumanTaskEngine() {
		initExpressionLanguageRuntimes();
	}

	private void initExpressionLanguageRuntimes() {
		expressionLanguageRuntimeRegistry = new HashMap<String, ExpressionLanguageRuntime>();
		expressionLanguageRuntimeRegistry.put(XPathExpressionRuntime.ns,
				new XPathExpressionRuntime());
	}

	// create task logic.
	private TaskDAO createTask(WSDLAwareMessage message,
			HumanTaskBaseConfiguration taskConfiguration, int tenantId) throws HumanTaskException {

		TaskCreationContext creationContext = new TaskCreationContext();
		creationContext.setTaskConfiguration(taskConfiguration);
		creationContext.setTenantId(tenantId);
		creationContext.setMessageBodyParts(message.getBodyPartElements());
		creationContext.setMessageHeaderParts(message.getHeaderPartElements());
		creationContext.setPeopleQueryEvaluator(peopleQueryEvaluator);

		TaskDAO task = getDaoConnectionFactory().getConnection().createTask(creationContext);
		creationContext.injectExpressionEvaluationContext(task);
		prefix = taskConfiguration.getNamespaceContext()
				.getPrefix(HumanTaskConstants.HTD_NAMESPACE);

		boolean emailNotificationEnabled = HumanTaskServiceComponent.getHumanTaskServer()
				.getServerConfig().getEnableEMailNotificaion();
		boolean smsNotificationEnabled = HumanTaskServiceComponent.getHumanTaskServer()
				.getServerConfig().getEnableSMSNotificaion();
		boolean isNotification = false;
		System.out.println("Task is a :"
				+ taskConfiguration.getConfigurationType().toString()
						.equalsIgnoreCase("notification"));
		if (taskConfiguration.getConfigurationType().toString().equalsIgnoreCase("notification")) {
			isNotification = true;
		}

		if (isNotification) {
			if (emailNotificationEnabled) {
				sendMail(taskConfiguration, creationContext, task);
			}

			if (smsNotificationEnabled) {
				System.out.println("Sms notification enabled:" + smsNotificationEnabled);

				try {
					sendMessageConfigure(message, taskConfiguration, creationContext, task);
				} catch (AxisFault e) {
					e.printStackTrace();
				} catch (XMLStreamException e) {
					// Auto-generated catch block
				}
			}
		}

		return task;
	}

	public void sendMail(HumanTaskBaseConfiguration taskConfiguration,
			TaskCreationContext creationContext, TaskDAO task) {
		ServiceClient serviceClient = null;
		String mail_to = null;
		String ccEmail = null;
		String bccEmail = null;
		String mail_subject = null;
		String emailBody = null;

		String emailBodyTagVal = null;
		try {

			serviceClient = new ServiceClient();

			// getting email details from rendering elements in HumanTaskName.ht
			// email details
			Document docTemp = xmlRead(taskConfiguration.getRenderings().toString());
			// System.out.println("Testing \n 1 : \n" +
			// taskConfiguration.getRenderings().toString());
			NodeList element1 = docTemp.getElementsByTagName(prefix + ":"
					+ HumanTaskConstants.RENDERING_TAG);

			// System.out.println(" 3 : "
			// +
			// element1.item(0).getAttributes().getNamedItem("type").getTextContent());
			for (int i = 0; i < element1.getLength(); i++) {
				if ((element1.item(i).getAttributes().getNamedItem("type").getTextContent())
						.equals(HumanTaskConstants.RENDERING_TYPE_EMAIL)) {
					Document doc = xmlRead(taskConfiguration.getRenderings().getRenderingArray(i)
							.toString());
					// System.out.println("first xml string : "
					// +
					// taskConfiguration.getRenderings().getRenderingArray(i).toString());

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
			System.out.println("EmailBodyTagValue :"+emailBodyTagVal+"\n");
			emailBody = evaluateTextForXPath(emailBodyTagVal, taskConfiguration, creationContext,
					task);
			System.out.println("EmailBody :"+emailBody+"\n");
			if(errorInStringEvaluation(emailBody)){
				return;
			}
		

			// /////////
			

			System.out.println("To is taken as : " + mail_to);
			System.out.println("cc is taken as : " + ccEmail);
			System.out.println("bcc email is taken as : " + bccEmail);
			System.out.println("email body is taken as : " + emailBody);
			System.out.println("prefix is taken as : " + prefix);

			// Get the axis2.xml configuration details for email
			String encodingOptions = "text/html; charset=UTF-8";
			Properties props = new Properties();
			ArrayList<Parameter> mv = serviceClient.getAxisConfiguration()
					.getTransportOut(Constants.TRANSPORT_MAIL).getParameters();
			String username = "", password = "";
			for (int i = 0; i < mv.size(); i++) {
				System.out.println(mv.get(i).getName() + " : " + mv.get(i).getValue());
				if (mv.get(i).getName().equals(MailConstants.MAIL_SMTP_USERNAME)) {
					username = mv.get(i).getValue().toString();
				} else if (mv.get(i).getName().equals(MailConstants.MAIL_SMTP_PASSWORD)) {
					password = mv.get(i).getValue().toString();
				} else {
					props.put(mv.get(i).getName(), mv.get(i).getValue());
				}
			}

			Authenticator auth = new SMTPAuthenticator(username, password);
			System.out.println(props);
			Session session = Session.getDefaultInstance(props, auth);
			MimeMessage messagew = new MimeMessage(session);
			messagew.setContent("Hello", "text/plain");
			messagew.setSubject(mail_subject);
			messagew.setText(emailBody);
			messagew.setHeader("Content-Type", encodingOptions);
			messagew.setSentDate(new Date());
			Address toaddress = new InternetAddress(mail_to);
			messagew.addRecipient(Message.RecipientType.TO, toaddress);
			messagew.addRecipients(Message.RecipientType.CC, processCC(ccEmail));
			messagew.addRecipients(Message.RecipientType.BCC, processCC(bccEmail));
			System.out.println(messagew);

			Transport.send(messagew);
			System.out.println("Send Mail Ok!");

		} catch (AxisFault e) {
			e.printStackTrace();
		} catch (MessagingException e) {
			e.printStackTrace();
		} catch (DOMException e1) {
			e1.printStackTrace();
		}
	}

	public void sendMessageConfigure(WSDLAwareMessage message,
			HumanTaskBaseConfiguration taskConfiguration, TaskCreationContext creationContext,
			TaskDAO task) throws AxisFault, XMLStreamException {

		String smsReciever = null;
		String smsBody = null;
		String smsBodyTagVal = "";

		// Get configurations for sms from relevant humantask's .ht files'
		// rendering elements
		System.out.println("Going to read xml string and get sms configurations.");
		Document docTemp = xmlRead(taskConfiguration.getRenderings().toString());
		// System.out.println("Testing \n 1 : \n" +
		// taskConfiguration.getRenderings().toString());
		NodeList element1 = docTemp.getElementsByTagName(prefix + ":"
				+ HumanTaskConstants.RENDERING_TAG);

		// System.out.println(" 3 : "+
		// element1.item(0).getAttributes().getNamedItem("type").getTextContent());

		for (int i = 0; i < element1.getLength(); i++) {
			if ((element1.item(i).getAttributes().getNamedItem("type").getTextContent())
					.equals(HumanTaskConstants.RENDERING_TYPE_SMS)) {
				Document doc = xmlRead(taskConfiguration.getRenderings().getRenderingArray(i)
						.toString());
				// System.out.println("first xml string : "
				// +
				// taskConfiguration.getRenderings().getRenderingArray(i).toString());

				smsReciever = doc
						.getElementsByTagName(prefix + ":" + HumanTaskConstants.SMS_RECIEVER_TAG)
						.item(0).getTextContent();
				smsBodyTagVal = doc
						.getElementsByTagName(
								prefix + ":" + HumanTaskConstants.EMAIL_OR_SMS_BODY_TAG).item(0)
						.getTextContent();

			}
		}
		smsBody = evaluateTextForXPath(smsBodyTagVal, taskConfiguration, creationContext, task);
		if(errorInStringEvaluation(smsBody)){
			return;
		}
	
		System.out.println("\n \nSMS reciever is taken as : " + smsReciever);
		System.out.println("SMS Body is taken as : " + smsBody);
		sendSMSfromDongle(smsReciever, smsBody);

	}

	private void sendSMSfromDongle(String smsReciever, String smsBody) {
		String gateway = "";
		String port = "";
		int baudRate = 0;
		String dongleManufacturer = "";
		String dongleModel = "";
		String smscNumber = "+947100003";// The SMSC Number is "+947100003" for
											// Mobitel Sri Lanka, "+9471000003"
											// for Dialog Sri Lanka

		ServiceClient serviceClient = null;
		try {
			serviceClient = new ServiceClient();
		} catch (AxisFault e1) {
			e1.printStackTrace();
		}

		// Get the axis2.xml configuration details for gsm sms
		ArrayList<Parameter> mv = serviceClient.getAxisConfiguration()
				.getTransportOut(HumanTaskConstants.TRANSPORT_SMS).getParameters();

		for (int i = 0; i < mv.size(); i++) {
			System.out.println(mv.get(i).getName() + " : " + mv.get(i).getValue());
			if (mv.get(i).getName().equals("systemId")) { // Decide whether to
															// send sms using
															// dongle or the
															// SMPP simulator
				System.out
						.println("Found configurations for SMS transport via a simulator and proceeding to send SMS with it...!");
				sendSMStoSimulator(smsReciever, smsBody);
				return;

			} else if (mv.get(i).getName().equals(HumanTaskConstants.SMS_GATEWAY_ID)) {
				gateway = mv.get(i).getValue().toString();
			} else if (mv.get(i).getName().equals(HumanTaskConstants.SMS_COM_PORT)) {
				port = mv.get(i).getValue().toString();
			} else if (mv.get(i).getName().equals(HumanTaskConstants.SMS_BAUD_RATE)) {
				baudRate = Integer.parseInt(mv.get(i).getValue().toString());
			} else if (mv.get(i).getName().equals(HumanTaskConstants.SMS_DONGLE_MANUFATURER)) {
				dongleManufacturer = mv.get(i).getValue().toString();
			} else if (mv.get(i).getName().equals(HumanTaskConstants.SMS_DONGLE_MODEL)) {
				dongleModel = mv.get(i).getValue().toString();
			}
		}
		System.out
				.println("Found configurations for SMS transport via a GSM Modem and proceeding to send SMS with it...!");
		SendMessage sendMessage = new SendMessage(smsBody, smsReciever, gateway, port, baudRate,
				dongleManufacturer, dongleModel, smscNumber);
		try {
			sendMessage.doIt();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void sendSMStoSimulator(String smsReciever, String smsBody) {
		String smsEPR = "sms://" + smsReciever;
		System.out.println("\n \n SMS reciever is taken as : " + smsEPR);

		ServiceClient serviceClient = null;
		try {
			serviceClient = new ServiceClient();

			Options options = serviceClient.getOptions();
			EndpointReference targetEPR = new EndpointReference(smsEPR);
			options.setTo(targetEPR);
			String sm = "<h1>" + smsBody + "</h1>";
			serviceClient.fireAndForget(AXIOMUtil.stringToOM(sm));
		} catch (AxisFault e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (XMLStreamException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String evaluateTextForXPath(String xmlString,
			HumanTaskBaseConfiguration taskConfiguration, TaskCreationContext creationContext,
			TaskDAO task) {
		String expressionLanguage = taskConfiguration.getExpressionLanguage();
		// System.out.println("Get EL : " + expressionLanguage);
		ExpressionLanguageRuntime expLangRuntime = HumanTaskServiceComponent.getHumanTaskServer()
				.getTaskEngine().getExpressionLanguageRuntime(expressionLanguage);
		EvaluationContext evalCtx = creationContext.getEvalContext();
		String processedString = resolveJAXP(xmlString, task);
		String[] split = processedString.split("\\s+");
		StringBuilder sm = new StringBuilder();
		for (String element : split) {
			if (element.startsWith(prefix + ":") /*
												 * &&
												 * (element.indexOf("htd:")==0)
												 */) {
				sm.append(" " + expLangRuntime.evaluateAsString(element, evalCtx));
			} else {
				sm.append(" " + element);
			}
		}

		return sm.toString();
	}

	public String resolveJAXP(String xmlString, TaskDAO task) {
		String replacedXmlString = CommonTaskUtil.replaceUsingPresentationParams(
				task.getPresentationParameters(), xmlString);
		return replacedXmlString;
	}
	private boolean errorInStringEvaluation(String string) {
		// check for resolvedness

		String[] split2 = (string.toString()).split("\\s+");
		for (String element : split2) {
			//System.out.println("\nsplitted strings..:"+element);
			if (element.startsWith("$") && element.endsWith("$")) {
				System.out.println("ERROR !!! The .ht file is configured with erroneous presentation parameters at following tag value:\n\""+string+"\"");
				System.out.println("So the program is haulted..!!!");
				return true;
			}
		}
		return false;
	}

	public Address[] processCC(String list) throws AddressException {
		String[] split = list.split(",");
		Address[] addresses = new Address[split.length];
		for (int i = 0; i < addresses.length; i++) {
			addresses[i] = new InternetAddress(split[i]);
		}
		return addresses;
	}

	class SMTPAuthenticator extends javax.mail.Authenticator {
		private String userName;
		private String password;

		public SMTPAuthenticator(String username, String password) {
			this.userName = username;
			this.password = password;
		}

		public javax.mail.PasswordAuthentication getPasswordAuthentication() {
			return new javax.mail.PasswordAuthentication(userName, password);
		}
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

	/**
	 * The invoke method called when the
	 * {@link org.wso2.carbon.humantask.core.integration.AxisHumanTaskMessageReceiver}
	 * is called for task creation .
	 * 
	 * @param message
	 *            : The wsdl message containing the task creation logic.
	 * @return : The task ID.
	 * @throws HumanTaskException
	 *             : If the task creation fails.
	 */
	public String invoke(final WSDLAwareMessage message,
			final HumanTaskBaseConfiguration taskConfiguration) throws Exception {
		TaskDAO task = scheduler.execTransaction(new Callable<TaskDAO>() {
			public TaskDAO call() throws Exception {
				HumanTaskStore taskStore = HumanTaskServiceComponent.getHumanTaskServer()
						.getTaskStoreManager().getHumanTaskStore(message.getTenantId());
				// return createTask(message,
				// taskStore.getTaskConfiguration(message.getPortTypeName(),
				// message.getOperationName()),
				// message.getTenantId());
				return createTask(message, taskConfiguration, message.getTenantId());
			}
		});

		return task.getId().toString();
	}

	/**
	 * @return : The {@link HumanTaskDAOConnectionFactory}
	 */
	public HumanTaskDAOConnectionFactory getDaoConnectionFactory() {
		return daoConnectionFactory;
	}

	/**
	 * @param daoConnectionFactory
	 *            : The DAO Connection Factory.
	 */
	public void setDaoConnectionFactory(HumanTaskDAOConnectionFactory daoConnectionFactory) {
		this.daoConnectionFactory = daoConnectionFactory;
	}

	/**
	 * @param pqe
	 *            : The people query evaluator to set.
	 */
	public void setPeopleQueryEvaluator(PeopleQueryEvaluator pqe) {
		this.peopleQueryEvaluator = pqe;
	}

	/**
	 * @return : The people query evaluator for people evaluations.
	 */
	public PeopleQueryEvaluator getPeopleQueryEvaluator() {
		return peopleQueryEvaluator;
	}

	/**
	 * @param language
	 *            : The required type of the expression language.
	 * @return : The ExpressionLanguageRuntime object.
	 */
	public ExpressionLanguageRuntime getExpressionLanguageRuntime(String language) {
		ExpressionLanguageRuntime epxLanguageRuntime = null;
		if (expressionLanguageRuntimeRegistry != null) {
			epxLanguageRuntime = expressionLanguageRuntimeRegistry.get(language);
		}
		return epxLanguageRuntime;
	}

	/**
	 * @return : The scheduler:
	 */
	public Scheduler getScheduler() {
		return scheduler;
	}

	/**
	 * @param scheduler
	 *            : The Scheduler object to set.
	 */
	public void setScheduler(Scheduler scheduler) {
		this.scheduler = scheduler;
	}

	/**
	 * 
	 * @param eventProcessor
	 */
	public void setEventProcessor(EventProcessor eventProcessor) {
		this.eventProcessor = eventProcessor;
	}

	/**
	 * 
	 * @return
	 */
	public EventProcessor getEventProcessor() {
		return this.eventProcessor;
	}

}
