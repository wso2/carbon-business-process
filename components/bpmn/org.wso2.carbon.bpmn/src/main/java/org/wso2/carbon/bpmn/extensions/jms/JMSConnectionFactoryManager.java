package org.wso2.carbon.bpmn.extensions.jms;

import java.util.*;

/**
 * Created by dilini on 12/11/15.
 */
public class JMSConnectionFactoryManager {
    private static JMSConnectionFactoryManager connectionFactoryManager = null;

    private final Map<String, JMSConnectionFactory> connectionFactories = new HashMap<>();

    public JMSConnectionFactoryManager(){
    }

    public static synchronized JMSConnectionFactoryManager getInstance(){
        if(connectionFactoryManager == null){
            connectionFactoryManager = new JMSConnectionFactoryManager();
        }
        return connectionFactoryManager;
    }

    public void initializeConnectionFactories(HashMap<String, Hashtable<String, String>> parameterList){
        Set<String> keys = parameterList.keySet();
        Iterator<String> keyIterator = keys.iterator();
        String key;
        while (keyIterator.hasNext()){
            key = keyIterator.next();
            connectionFactories.put(key, new JMSConnectionFactory(parameterList.get(key)));
        }
    }

    public JMSConnectionFactory getConnectionFactory(String jmsProviderID){
        return connectionFactories.get(jmsProviderID);
    }

    public void stop(){
        for(JMSConnectionFactory jcf : connectionFactories.values()){
            jcf.stop();
        }
    }
}
