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
import org.wso2.carbon.bpmn.core.BPMNConstants;

import javax.xml.namespace.QName;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * This class holds the activiti's bean configuration in to separate bean objects
 */
public class BPMNBean {

    private String beanId;
    private String beanClass;

    private Map<String, String> propertyMap = null;


    public BPMNBean(OMElement bean){

        propertyMap = new HashMap<>();
        initializeBean(bean);
    }

    public String getBeanId() {
        return beanId;
    }

    public String getBeanClass() {
        return beanClass;
    }

    public String getPropertyValue(String propertyName){

        String propertyValue = propertyMap.get(propertyName);
        if(propertyValue != null){
            return propertyValue;
        }

        return BPMNConstants.NOT_DEFINED_VAR;
    }

    public Map<String, String> getPropertyMap() {
        return propertyMap;
    }

    private void initializeBean(OMElement bean){

        this.beanId = bean.getAttributeValue(new QName(null, BPMNConstants.BEAN_ID));
        this.beanClass = bean.getAttributeValue(new QName(null, BPMNConstants.BEAN_CLASS));

        Iterator beanProps = bean.getChildrenWithName(new QName(BPMNConstants.SPRING_NAMESPACE,
                BPMNConstants.PROPERTY));
        while (beanProps.hasNext()) {
            OMElement beanProp = (OMElement) beanProps.next();

            if(beanProp != null){
                String propertyName = beanProp.getAttributeValue(new QName(null, BPMNConstants.NAME));
                String propertyValue = beanProp.getAttributeValue(new QName(null, BPMNConstants.VALUE));

                propertyMap.put(propertyName, propertyValue);
            }
        }
    }
}
