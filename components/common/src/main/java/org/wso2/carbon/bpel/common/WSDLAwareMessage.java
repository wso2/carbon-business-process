/*
 * Copyright (c) 2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.bpel.common;

import org.apache.axiom.om.OMElement;
import org.w3c.dom.Element;

import javax.xml.namespace.QName;
import java.util.HashMap;
import java.util.Map;

/**
 * Representation of WSDL input message to the HumanTask
 */
public class WSDLAwareMessage {

    /** The header parts */
    private Map<String, OMElement> headerParts = new HashMap<String, OMElement>();

    /** The body parts */
    private Map<String, OMElement> bodyParts = new HashMap<String, OMElement>();

    /** The wsdl operation name */
    private String operationName;

    /** The port type */
    private QName portTypeName;

    /** The tenant id */
    private int tenantId;

    /**
     * Adds the provided element to the body parts.
     *
     * @param partName : The part name.
     * @param partElement : The element to be added
     */
    public void addBodyPart(String partName, OMElement partElement) {
        bodyParts.put(partName, partElement);
    }

    /**
     * Adds the provided element to the hear parts.
     *
     * @param partName : The part name.
     * @param partElement : The element to be added
     */
    public void addHeaderPart(String partName, OMElement partElement) {
        headerParts.put(partName, partElement);
    }

    /**
     * @return : The operation name.
     */
    public String getOperationName() {
        return operationName;
    }

    /**
     * @param operationName : The operation name to set.
     */
    public void setOperationName(String operationName) {
        this.operationName = operationName;
    }

    /**
     * @return : The port name.
     */
    public QName getPortTypeName() {
        return portTypeName;
    }

    /**
     * @param portTypeName  : The port name to set.
     */
    public void setPortTypeName(QName portTypeName) {
        this.portTypeName = portTypeName;
    }

    /**
     * @return : The tenant id
     */
    public int getTenantId() {
        return tenantId;
    }

    /**
     * @param tenantId : The tenant id to set.
     */
    public void setTenantId(int tenantId) {
        this.tenantId = tenantId;
    }

    /**
     * Gets the body part elements of the message.
     * @return : The body part elements map.
     */
    public Map<String, Element> getBodyPartElements() {
        Map<String, Element> messageBodyParts = new HashMap<String, Element>();

        for (Map.Entry<String, OMElement> part : bodyParts.entrySet()) {
            messageBodyParts.put(part.getKey(), OMUtils.toDOM(part.getValue()));
        }

        return messageBodyParts;
    }

    /**
     * Gets the header part elements of the message.
     * @return : The header part elements map.
     */
    public Map<String, Element> getHeaderPartElements() {
        Map<String, Element> messageHeaderParts = new HashMap<String, Element>();

        for (Map.Entry<String, OMElement> part : headerParts.entrySet()) {
            messageHeaderParts.put(part.getKey(), OMUtils.toDOM(part.getValue()));
        }

        return messageHeaderParts;
    }
}
