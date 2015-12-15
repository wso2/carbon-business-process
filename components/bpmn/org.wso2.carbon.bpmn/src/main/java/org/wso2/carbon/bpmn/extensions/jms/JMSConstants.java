package org.wso2.carbon.bpmn.extensions.jms;

/**
 * Created by dilini on 12/10/15.
 */
public class JMSConstants {

    public static final String DESTINATION_TYPE_QUEUE = "queue";
    public static final String DESTINATION_TYPE_TOPIC = "topic";

    public static final int QUEUE = 1;
    public static final int TOPIC = 2;

    public static final String PARAM_DESTINATION = "transport.jms.Destination";
    public static final String PARAM_DESTINATION_TYPE = "transport.jsm.DestinationType";
    public static final String PARAM_JMS_CONNFAC = "transport.jms.ConnectionFactory";
    public static final String PARAM_JMS_CONNFAC_TYPE = "transport.jms.ConnectionFactoryType";
    public static final String PARAM_SESSION_TRANSACTED = "transport.jms.SessionTransacted";
    public static final String PARAM_SESSION_ACK = "transport.jms.SessionAcknowledgement";
    public static final String PARAM_USERNAME = "transport.jms.UserName";
    public static final String PARAM_PASSWORD = "transport.jms.Password";
    public static final String PARAM_CACHE_LEVEL = "transport.jms.CacheLevel";
    public static final String PARAM_OUTPUT_MAPPINGS = "transport.jms.OutputMappings";
    public static final String PARAM_ON_ERROR = "transport.jms.OnError";

    public static final int CACHE_NONE = 0;
    public static final int CACHE_CONNECTION = 1;
    public static final int CACHE_SESSION = 2;
    public static final int CACHE_CONSUMER = 3;
    public static final int CACHE_PRODUCER = 4;
    public static final int CACHE_AUTO = 5;

    public static final String JMS_PROVIDER_URL = "java.naming.provider.url";
    public static final String JMS_CONNECTION_FACTORY_JNDI_NAME = "transport.jms.ConnectionFactoryJNDIName";

    public static final String NAMING_FACTORY_INITIAL = "java.naming.factory.initial";
    public static final String CONNECTION_STRING = "connectionfactory.QueueConnectionFactory";
    public static final String TOPIC_PREFIX = "topic.";
    public static final String QUEUE_PREFIX = "queue.";

    public static final String JMS_START_TASK = "org.wso2.carbon.bpmn.extensions.jms.JMSListener";
    public static final String JMS_SENDER = "org.wso2.carbon.bpmn.extensions.jms.JMSSender";

    public static final String JMS_PROVIDER = "jmsProviderID";
    public static final String JMS_DESTINATION_TYPE = "destinationType";
    public static final String JMS_DESTINATION_NAME = "destinationName";
    public static final String JMS_CONNECTION_USERNAME = "username";
    public static final String JMS_CONNECTION_PASSWORD = "password";
    public static final String JMS_CACHE_LEVEL = "cacheLevel";
    public static final String JMS_OUTPUT_MAPPINGS = "outputMappings";
    public static final String JMS_ON_ERROR = "onError";
}
