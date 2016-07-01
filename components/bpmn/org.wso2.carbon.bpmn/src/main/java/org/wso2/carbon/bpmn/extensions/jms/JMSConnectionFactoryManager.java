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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.*;

/**
 * Created by dilini on 12/11/15.
 * Manages a map of connection factory objects which are created at the server start up using the parameter values
 * in the config file.
 */

public class JMSConnectionFactoryManager {
    private static final Log log = LogFactory.getLog(JMSConnectionFactoryManager.class);
    private static JMSConnectionFactoryManager connectionFactoryManager = null;

    private final Map<String, JMSConnectionFactory> connectionFactories = new HashMap<>();

    /**
     *
     */
    public JMSConnectionFactoryManager(){
    }

    /**
     *
     * @return
     */
    public static synchronized JMSConnectionFactoryManager getInstance(){
        if(connectionFactoryManager == null){
            connectionFactoryManager = new JMSConnectionFactoryManager();
        }
        return connectionFactoryManager;
    }

    /**
     *
     * @param factoryType
     * @param parameterList
     */
    public void initializeConnectionFactories(String factoryType, Hashtable<String, String> parameterList){
        connectionFactories.put(factoryType, new JMSConnectionFactory(parameterList));
    }

    /**
     * given
     * @param factoryType
     * @return the corresponding connectionFactory object.
     */
    public synchronized JMSConnectionFactory getConnectionFactory(String factoryType){
        return connectionFactories.get(factoryType);
    }

    /**
     *
     */
    public void stop(){
        for(JMSConnectionFactory jcf : connectionFactories.values()){
            jcf.stop();
        }
    }
}
