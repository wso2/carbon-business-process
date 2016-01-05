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

/**
 * Created by dilini on 12/10/15.
 */
public class JMSConstants {

    public static final String ACTIVITI_CONFIGURATION_FILE_NAME = "jms.xml";

    public static final String DESTINATION_TYPE_QUEUE = "queue";
    public static final String DESTINATION_TYPE_TOPIC = "topic";

    public static final int QUEUE = 1;
    public static final int TOPIC = 2;

    public static final String PARAM_DESTINATION = "transport.jms.Destination";
    public static final String PARAM_DESTINATION_TYPE = "transport.jsm.DestinationType";
    public static final String PARAM_SESSION_TRANSACTED = "transport.jms.SessionTransacted";
    public static final String PARAM_SESSION_ACK = "transport.jms.SessionAcknowledgement";
    public static final String PARAM_CACHE_USER_TRX = "transport.jms.CacheUserTransaction";
    public static final String PARAM_INIT_RECON_DURATION = "transport.jms.InitialReconnectDuration";
    public static final String PARAM_RECON_PROGRESS_FACT = "transport.jms.ReconnectProgressFactor";
    public static final String PARAM_MAX_RECON_DURATION = "transport.jms.MaxReconnectDuration";
    public static final String PARAM_USERNAME = "transport.jms.UserName";
    public static final String PARAM_PASSWORD = "transport.jms.Password";
    public static final String PARAM_CACHE_LEVEL = "transport.jms.CacheLevel";
    public static final String PARAM_OUTPUT_MAPPINGS = "transport.jms.OutputMappings";
    public static final String PARAM_ON_ERROR = "transport.jms.OnError";
    public static final String PARAM_CONNECTION_FACTORY_JNDI_NAME = "transport.jms.ConnectionFactoryJNDIName";
    public static final String PARAM_CONNECTION_FACTORY_TYPE = "transport.jms.ConnectionFactoryType";
    public static final String PARAM_CONTENT_TYPE = "transport.jms.ContentType";
    public static final int CACHE_NONE = 0;
    public static final int CACHE_CONNECTION = 1;
    public static final int CACHE_SESSION = 2;
    public static final int CACHE_CONSUMER = 3;
    public static final int CACHE_PRODUCER = 4;
    public static final int CACHE_AUTO = 5;

    public static final String JMS_PROVIDER_URL = "java.naming.provider.url";


    public static final String NAMING_FACTORY_INITIAL = "java.naming.factory.initial";
    public static final String CONNECTION_STRING = "connectionfactory.QueueConnectionFactory";
    public static final String TOPIC_PREFIX = "topic.";
    public static final String QUEUE_PREFIX = "queue.";

    public static final String JMS_START_TASK = "org.wso2.carbon.bpmn.extensions.jms.JMSListener";

    public static final String JMS_PROVIDER = "jmsProviderID";
    public static final String JMS_DESTINATION_TYPE = "destinationType";
    public static final String JMS_DESTINATION_NAME = "destinationName";
    public static final String JMS_CONNECTION_USERNAME = "username";
    public static final String JMS_CONNECTION_PASSWORD = "password";
    public static final String JMS_OUTPUT_MAPPINGS = "outputMappings";
    public static final String JMS_ON_ERROR = "onError";
    public static final String JMS_CONTENT_TYPE = "contentType";
    public static final String JMS_QUEUE_CONNECTION_FACTORY = "queueConnectionFactory";
    public static final String JMS_TOPIC_CONNECTION_FACTORY = "topicConnectionFactory";
    public static final String JMS_SESSION_TRANSACTED = "sessionTransacted";
    public static final String JMS_SESSION_ACK = "sessionAcknowledgement";
    public static final String JMS_CACHE_LEVEL = "cacheLevel";
    public static final String JMS_USER_TRANSACTION = "cacheUserTransaction";
    public static final String JMS_INIT_RECONN_DURATION = "initialReconnectDuration";
    public static final String JMS_RECON_PROGRESS_FACTORY = "reconnectProgressFactor";
    public static final String JMS_MAX_RECON_DURATION = "maxReconnectDuration";

    public static final String CONTENT_TYPE_XML = "xml";
    public static final String CONTENT_TYPE_JSON = "json";
    public static final String CONTENT_TYPE_TEXT = "text";
}
