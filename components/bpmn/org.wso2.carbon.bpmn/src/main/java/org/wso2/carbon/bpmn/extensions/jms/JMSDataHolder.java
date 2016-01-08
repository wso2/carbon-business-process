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

import java.util.HashMap;
import java.util.Hashtable;

/**
 * Created by dilini on 12/16/15.
 *
 * At the server start up, the configuration file is read and those data are saved in this data holder class
 * for further references.
 */
public class JMSDataHolder {

    private static JMSDataHolder jmsDataHolder = null;
    private HashMap<String, Hashtable<String, String>> jmsProperties = new HashMap<>();

    private static final Log log = LogFactory.getLog(JMSDataHolder.class);

    /**
     *
     */
    private JMSDataHolder(){
        try {
            jmsProperties = JMSFileReader.readJMSProviderInformation();
        } catch (BPMNJMSException e) {
            log.error(e.getMessage());
        }
    }

    /**
     *
     * @return
     */
    public static JMSDataHolder getInstance(){
        if(jmsDataHolder == null){
            jmsDataHolder = new JMSDataHolder();
        }
        return jmsDataHolder;
    }

    /**
     *
     * @return
     */
    public HashMap<String, Hashtable<String, String>> getJmsProperties() {
        return jmsProperties;
    }
}
