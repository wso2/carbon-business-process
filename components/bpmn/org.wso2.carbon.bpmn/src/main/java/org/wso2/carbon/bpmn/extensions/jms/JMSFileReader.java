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

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.utils.CarbonUtils;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;;
import java.util.*;

/**
 * Created by dilini on 12/9/15.
 *
 */
public class JMSFileReader {

    private static final Log log = LogFactory.getLog(JMSFileReader.class);

    /**
     * @return
     *
     * reads the configuration file in the server and returns map of parameter tables.
     * each parameter table contains parameters of a connection factory (either queue or topic)
     */
    public static HashMap<String, Hashtable<String, String>> readJMSProviderInformation() throws BPMNJMSException {
        HashMap<String, Hashtable<String, String>> jmsProperties = new HashMap<>();

        try {
            String carbonConfigDirPath = CarbonUtils.getCarbonConfigDirPath();
            String activitiConfigPath = carbonConfigDirPath + File.separator +
                    JMSConstants.ACTIVITI_CONFIGURATION_FILE_NAME;
            File configFile = new File(activitiConfigPath);
            String configContent = null;
            try {
                configContent = FileUtils.readFileToString(configFile);
                OMElement configElement = AXIOMUtil.stringToOM(configContent);
                Iterator jmsTransport = configElement.getChildrenWithName(new QName(null, "jmsTransport"));
                while (jmsTransport.hasNext()) {
                    OMElement transports = (OMElement) jmsTransport.next();
                    Iterator parameters = transports.getChildrenWithName(new QName(null, "parameters"));
                    while (parameters.hasNext()) {
                        OMElement parameter = (OMElement) parameters.next();
                        String paramId = parameter.getAttributeValue(new QName(null, "name"));
                        Iterator params = parameter.getChildrenWithName(new QName(null, "parameter"));
                        Hashtable<String, String> properties = new Hashtable<>();
                        while(params.hasNext()){
                            OMElement param = (OMElement)params.next();
                            String value = param.getAttributeValue(new QName(null, "value"));
                            switch (param.getAttributeValue(new QName(null, "name"))){
                            case JMSConstants.PARAM_CONNECTION_FACTORY_JNDI_NAME:
                                properties.put(JMSConstants.PARAM_CONNECTION_FACTORY_JNDI_NAME, value);
                                break;
                            case JMSConstants.PARAM_CONNECTION_FACTORY_TYPE:
                                properties.put(JMSConstants.PARAM_CONNECTION_FACTORY_TYPE, value);
                                properties.put(JMSConstants.PARAM_DESTINATION_TYPE, value);
                                break;
                            case JMSConstants.NAMING_FACTORY_INITIAL:
                                properties.put(JMSConstants.NAMING_FACTORY_INITIAL, value);
                                break;
                            case JMSConstants.JMS_PROVIDER_URL:
                                properties.put(JMSConstants.JMS_PROVIDER_URL, value);
                                break;
                            case JMSConstants.PARAM_SESSION_TRANSACTED:
                                properties.put(JMSConstants.PARAM_SESSION_TRANSACTED, value);
                                break;
                            case JMSConstants.PARAM_SESSION_ACK:
                                properties.put(JMSConstants.PARAM_SESSION_ACK, value);
                                break;
                            case JMSConstants.PARAM_CACHE_LEVEL:
                                properties.put(JMSConstants.PARAM_CACHE_LEVEL, value);
                                break;
                            case JMSConstants.PARAM_CACHE_USER_TRX:
                                properties.put(JMSConstants.PARAM_CACHE_USER_TRX, value);
                                break;
                            case JMSConstants.PARAM_INIT_RECON_DURATION:
                                properties.put(JMSConstants.PARAM_INIT_RECON_DURATION, value);
                                break;
                            case JMSConstants.PARAM_RECON_PROGRESS_FACT:
                                properties.put(JMSConstants.PARAM_RECON_PROGRESS_FACT, value);
                                break;
                            case JMSConstants.PARAM_MAX_RECON_DURATION:
                                properties.put(JMSConstants.PARAM_MAX_RECON_DURATION, value);
                                break;
                            }
                        }

                        //user should provide the JNDI Name of the InitialContextFactory of the JMSProvider
                        if(properties.get(JMSConstants.NAMING_FACTORY_INITIAL) == null){
                            String contextFactNotProvidedError = "InitialContextFactory is not provided. " +
                                    "java.naming.factory.initial of the JMS provider must be provided.";

                            throw new BPMNJMSException(contextFactNotProvidedError);
                        }

                        //user should provide the Provider URL of the JMSProvider
                        if(properties.get(JMSConstants.JMS_PROVIDER_URL) == null){
                            String provURLNotProvidedError = "ProviderURL is not provided. " +
                                    "java.naming.provider.url of the JMS provider must be provided.";

                            throw new BPMNJMSException(provURLNotProvidedError);
                        }
                        jmsProperties.put(paramId, properties);
                    }
                }
            }catch(FileNotFoundException e){
                log.error("<Server-Home>/repository/conf/jms.xml does not exist");
            }
        }catch (IOException e){
            log.error(e.getMessage(), e);
        } catch (XMLStreamException e) {
            log.error(e.getMessage(), e);
        }
        return jmsProperties;
    }
}
