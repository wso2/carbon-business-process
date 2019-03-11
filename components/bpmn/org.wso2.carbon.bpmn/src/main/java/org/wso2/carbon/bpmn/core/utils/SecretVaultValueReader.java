/**
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.bpmn.core.utils;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.wso2.carbon.bpmn.core.BPMNConstants;
import org.wso2.carbon.bpmn.core.BPSFault;
import org.wso2.securevault.SecretResolver;
import org.wso2.securevault.SecretResolverFactory;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.util.Iterator;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.ByteArrayInputStream;

/**
 * This class provides the functionality to read the value from SecureVault
 */
public class SecretVaultValueReader {
    private OMElement activitiConfigElement;
    private SecretResolver secretResolver;

    /**
     * Constructor to initialize SecretResolver object
     *
     * @param activitiConfigFile The activit.xml file that contains the aliases
     */
    public SecretVaultValueReader(File activitiConfigFile) throws BPSFault {
        try (FileInputStream fileInputStream = new FileInputStream(activitiConfigFile)) {
            StAXOMBuilder builder = new StAXOMBuilder(fileInputStream);
            activitiConfigElement = builder.getDocumentElement();
            secretResolver = SecretResolverFactory.create(activitiConfigElement, true);
        } catch (XMLStreamException | IOException e) {
            throw new BPSFault("Failed to find activiti.xml", e);
        }
    }

    /**
     * Function that iterates through each property in the file and calls getSecretValue() to check if it has a
     * secretAlias
     *
     * @return InputStream of the activiti.xml file with the resolved secretAliases. Returns null if there are no
     * secret alias attributes in any of the properties
     */
    public InputStream decryptSecretValues() {
        boolean hasSecretValue = false;

        Iterator iteratorBeans = activitiConfigElement.getChildrenWithLocalName("bean");

        while (iteratorBeans.hasNext()) {

            Iterator iteratorProperties = ((OMElement) iteratorBeans.next()).getChildrenWithLocalName("property");
            while (iteratorProperties.hasNext()) {
                OMElement propertyOMElement = (OMElement) iteratorProperties.next();
                String secretValue = getSecureVaultValue(secretResolver, propertyOMElement);

                if (secretValue != null) {
                    hasSecretValue = true;
                    propertyOMElement.addAttribute("value", secretValue, null);
                    propertyOMElement.removeAttribute(
                            propertyOMElement.getAttribute(
                                    new QName(BPMNConstants.SECURE_VAULT_NS, BPMNConstants.SECRET_ALIAS_ATTR_NAME)
                            )
                    );
                }
            } //End of iteration through iteratorProperties
        } //End of iteration through iteratorBeans

        if (hasSecretValue) {
            InputStream activitiConfigStream = new ByteArrayInputStream(activitiConfigElement.toString().getBytes());
            return activitiConfigStream;
        } else {
            return null;
        }
    }

    /**
     * Function to get the SecureVault value as a string.
     *
     * @param secretResolver  SecretResolver that resolves the password in the SecureVault
     * @param propertyElement Parameter that needs to be resolved
     * @return Resolved password as a String. Returns null if secretAlias attribute doesnt exist
     */
    private String getSecureVaultValue(SecretResolver secretResolver, OMElement propertyElement) {
        String value = null;
        OMAttribute attribute;

        if (propertyElement != null) {
            attribute = propertyElement.getAttribute(
                    new QName(BPMNConstants.SECURE_VAULT_NS, "secretAlias")
            );

            if (attribute != null) {
                value = secretResolver.resolve(attribute.getAttributeValue());
            }
        }
        return value;
    }

    public SecretResolver getSecretResolver() {
        return this.secretResolver;
    }
}
