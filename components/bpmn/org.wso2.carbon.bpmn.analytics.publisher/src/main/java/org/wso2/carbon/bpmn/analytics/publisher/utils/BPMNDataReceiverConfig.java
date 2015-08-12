/**
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.bpmn.analytics.publisher.utils;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bpmn.analytics.publisher.AnalyticsPublisher;
import org.wso2.carbon.bpmn.analytics.publisher.AnalyticsPublisherConstants;
import org.wso2.carbon.bpmn.analytics.publisher.internal.BPMNAnalyticsHolder;
import org.wso2.carbon.bpmn.core.BPMNConstants;
import org.wso2.carbon.core.util.CryptoException;
import org.wso2.carbon.core.util.CryptoUtil;
import org.wso2.carbon.registry.api.Registry;
import org.wso2.carbon.registry.api.RegistryException;
import org.wso2.carbon.registry.api.Resource;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.utils.CarbonUtils;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;

/**
 * BPMNDataReceiverConfig is used by AnalyticsPublisher to retrieve user name and password
 */
public class BPMNDataReceiverConfig {
    private static final Log log = LogFactory.getLog(BPMNDataReceiverConfig.class);

    /**
     * Get thrift url of data receiver
     *
     * @return thrift url of data receiver
     * @throws RegistryException
     */
    public static String getThriftURL() throws RegistryException {
        String url;
        Registry registry =
                BPMNAnalyticsHolder.getInstance().getRegistryService().getConfigSystemRegistry();

        if (registry.resourceExists(AnalyticsPublisherConstants.DATA_RECEIVER_RESOURCE_PATH)) {
            Resource resource =
                    registry.get(AnalyticsPublisherConstants.DATA_RECEIVER_RESOURCE_PATH);
            url = resource.getProperty(AnalyticsPublisherConstants.THRIFT_URL_PROPERTY);
        } else {
            url = null;
        }

        return url;
    }

    /**
     * Get user name
     *
     * @return user name
     * @throws RegistryException
     * @throws UserStoreException
     */
    public static String getUserName() throws RegistryException, UserStoreException {
        String userName;
        Registry registry =
                BPMNAnalyticsHolder.getInstance().getRegistryService().getConfigSystemRegistry();

        if (registry.resourceExists(AnalyticsPublisherConstants.DATA_RECEIVER_RESOURCE_PATH)) {
            Resource resource =
                    registry.get(AnalyticsPublisherConstants.DATA_RECEIVER_RESOURCE_PATH);
            userName = resource.getProperty(AnalyticsPublisherConstants.USER_NAME_PROPERTY);
        } else {
            userName = AnalyticsPublisherConstants.USER_NAME;
        }
        return userName;
    }

    /**
     * Get password
     *
     * @return password
     * @throws RegistryException
     * @throws UserStoreException
     */
    public static String getPassword() throws RegistryException, UserStoreException {
        String password;
        Registry registry =
                BPMNAnalyticsHolder.getInstance().getRegistryService().getConfigSystemRegistry();

        if (registry.resourceExists(AnalyticsPublisherConstants.DATA_RECEIVER_RESOURCE_PATH)) {
            Resource resource =
                    registry.get(AnalyticsPublisherConstants.DATA_RECEIVER_RESOURCE_PATH);
            try {
                String encryptedPassword = resource.
                        getProperty(AnalyticsPublisherConstants.PASSWORD_PROPERTY);
                byte[] decryptedPassword = CryptoUtil.getDefaultCryptoUtil(BPMNAnalyticsHolder.getInstance().
                        getServerConfigurationService(), BPMNAnalyticsHolder.getInstance().getRegistryService()).
                        base64DecodeAndDecrypt(encryptedPassword);
                password = new String(decryptedPassword);
            } catch (CryptoException e) {
                password = AnalyticsPublisherConstants.PASSWORD;
                String errMsg = "CryptoUtils Error while reading the password from the carbon registry.";
                log.error(errMsg, e);
            }

        } else {
            password = AnalyticsPublisherConstants.PASSWORD;
        }
        return password;
    }

    /**
     * Check BPMN Data Publisher Configuration is activated or not
     *
     * @return true if the BPMN Data Publisher is activated
     * @throws IOException
     * @throws XMLStreamException
     */
    public static boolean isDASPublisherActivated() throws IOException, XMLStreamException {
        String carbonConfigDirPath = CarbonUtils.getCarbonConfigDirPath();
        String activitiConfigPath = carbonConfigDirPath + File.separator + BPMNConstants.ACTIVITI_CONFIGURATION_FILE_NAME;
        File configFile = new File(activitiConfigPath);
        String configContent = FileUtils.readFileToString(configFile);
        OMElement configElement = AXIOMUtil.stringToOM(configContent);
        Iterator beans = configElement.getChildrenWithName(new QName(AnalyticsPublisherConstants.SPRING_NAMESPACE,
                AnalyticsPublisherConstants.BEAN));
        while (beans.hasNext()){
            OMElement bean = (OMElement) beans.next();
            String beanId = bean.getAttributeValue(new QName(null, AnalyticsPublisherConstants.BEAN_ID));
            if(AnalyticsPublisherConstants.BEAN_ID_VALUE.equals(beanId)){
                Iterator beanProps = bean.getChildrenWithName(new QName(AnalyticsPublisherConstants.SPRING_NAMESPACE,
                        AnalyticsPublisherConstants.PROPERTY));
                while (beanProps.hasNext()) {
                    OMElement beanProp = (OMElement) beanProps.next();
                    if (AnalyticsPublisherConstants.ACTIVATE.
                            equals(beanProp.getAttributeValue(new QName(null, AnalyticsPublisherConstants.NAME)))) {
                        String value = beanProp.getAttributeValue(new QName(null, AnalyticsPublisherConstants.VALUE));
                        if(AnalyticsPublisherConstants.TRUE.equals(value)){
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

}
