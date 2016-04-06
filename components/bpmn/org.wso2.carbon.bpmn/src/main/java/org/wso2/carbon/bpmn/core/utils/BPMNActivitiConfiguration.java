/**
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.bpmn.core.utils;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.bpmn.core.BPMNConstants;
import org.wso2.carbon.bpmn.core.exception.BPMNException;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

/**
 * This class loads the bean configuration from activiti.xml file
 */
public class BPMNActivitiConfiguration {

    private static final Logger log = LoggerFactory.getLogger(BPMNActivitiConfiguration.class);

    private static volatile BPMNActivitiConfiguration instance = null;
    private Map<String, BPMNBean> bpmnBeanMap = null;

    private BPMNActivitiConfiguration() throws BPMNException {
        bpmnBeanMap = new HashMap<>();
        initializeBPMNConfigBeans();
    }

    public static BPMNActivitiConfiguration getInstance() {

        if (instance == null) {
            synchronized (BPMNActivitiConfiguration.class) {
                if (instance == null) {
                    try {
                        instance = new BPMNActivitiConfiguration();
                    } catch (Exception ex) {
                        log.error(
                                "Initialization of BPMNActivitiConfiguration failed. Default values will be used",
                                ex);
                    }
                }
            }
        }

        return instance;
    }

    /**
     * Returns the BPMNBean object bean defined in activiti.xml file
     *
     * @param beanID bean id to be fetched
     * @return BPMNBean object corresponding to that bean in activiti.xml file
     */
    public BPMNBean getBPMNBean(String beanID) {
        BPMNBean bpmnBean = bpmnBeanMap.get(beanID);
        if (bpmnBean != null) {
            return bpmnBean;
        }
        return null;
    }

    /**
     * Returns the property value of a given bean
     *
     * @param beanID       bean id
     * @param propertyName property name to be fetched
     * @return property value
     */
    public String getBPMNPropertyValue(String beanID, String propertyName) {
        BPMNBean bpmnBean = bpmnBeanMap.get(beanID);
        if (bpmnBean != null) {
            return bpmnBean.getPropertyValue(propertyName);
        }
        return null;
    }

    /**
     * Initializes the activiti.xml config file loading
     *
     * @throws BPMNException
     */

    private void initializeBPMNConfigBeans() throws BPMNException {

        String activitiConfigPath = org.wso2.carbon.kernel.utils.Utils.getCarbonConfigHome()
                .resolve(BPMNConstants.ACTIVITI_CONFIGURATION_FILE_NAME).toString();
        // String activitiConfigPath =
        //   "/Users/himasha/Desktop/351R/wso2bps-3.5.1/repository/conf" + File.separator +
        //  BPMNConstants.ACTIVITI_CONFIGURATION_FILE_NAME;
        File configFile = new File(activitiConfigPath);
        try {
            String configContent = FileUtils.readFileToString(configFile);
            OMElement configElement = AXIOMUtil.stringToOM(configContent);
            Iterator beans = configElement.getChildrenWithName(
                    new QName(BPMNConstants.SPRING_NAMESPACE, BPMNConstants.BEAN));
            while (beans.hasNext()) {
                OMElement bean = (OMElement) beans.next();
                BPMNBean bpmnBean = new BPMNBean(bean);
                bpmnBeanMap.put(bpmnBean.getBeanId(), bpmnBean);
            }
        } catch (IOException e) {
            String errMsg = "Error on reading activiti configuration file ";
            throw new BPMNException(errMsg, e);
        } catch (XMLStreamException e) {
            String errMsg =
                    "Malformed XML Error occured while processing activiti configuration file ";
            throw new BPMNException(errMsg, e);
        }
    }
}
