/*
 * Copyright (c) 2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.humantask.core.store;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.wso2.carbon.bpel.common.config.EndpointConfiguration;
import org.wso2.carbon.humantask.HumanInteractionsDocument;
import org.wso2.carbon.humantask.TDeadlines;
import org.wso2.carbon.humantask.TNotification;
import org.wso2.carbon.humantask.TPresentationElements;
import org.wso2.carbon.humantask.TPriorityExpr;
import org.wso2.carbon.humantask.core.deployment.HumanTaskDeploymentException;
import org.wso2.carbon.humantask.core.deployment.config.THTDeploymentConfig;
import org.wso2.carbon.humantask.core.deployment.config.TPublish;
import org.wso2.carbon.humantask.core.utils.HumanTaskNamespaceContext;
import org.wso2.carbon.humantask.core.utils.HumanTaskStoreUtils;

import javax.wsdl.Definition;
import javax.xml.namespace.QName;
import java.io.File;
import java.util.List;

public class NotificationConfiguration extends HumanTaskBaseConfiguration {
    private static Log log = LogFactory.getLog(NotificationConfiguration.class);

    // Notification Definition
    private TNotification notificationDefinition;

    // Configuration info of notification
    private THTDeploymentConfig.Notification notificationDeploymentConfiguration;

    public NotificationConfiguration(TNotification notification,
                                     THTDeploymentConfig.Notification notificationDeploymentConfiguration,
                                     HumanInteractionsDocument humanInteractionsDocument,
                                     List<Definition> wsdls,
                                     String targetNamespace,
                                     String humanTaskArtifactName,
                                     AxisConfiguration tenantAxisConfig,
                                     String packageName,
                                     long version,
                                     File humanTaskDefinitionFile)
            throws HumanTaskDeploymentException {
        super(humanInteractionsDocument, targetNamespace, humanTaskArtifactName, tenantAxisConfig,
              false, packageName, version, humanTaskDefinitionFile);

        this.notificationDefinition = notification;
        this.notificationDeploymentConfiguration = notificationDeploymentConfiguration;

        try {
            Definition notificationWSDL = findWSDLDefinition(wsdls, getPortType(), getOperation());
            if (notificationWSDL == null) {
                throw new HumanTaskDeploymentException("Cannot find WSDL definition " +
                                                       "for notification: " + notification.getName());
            }
            setWSDL(notificationWSDL);

            HumanTaskNamespaceContext nsContext = new HumanTaskNamespaceContext();
            populateNamespace(notification.getDomNode().getNodeType() == Node.ELEMENT_NODE ?
                              (Element) notification.getDomNode() : null, nsContext);
            setNamespaceContext(nsContext);

            initEndpointConfigs();
        } catch (HumanTaskDeploymentException depEx) {
            this.setErroneous(true);
            this.setDeploymentError(depEx.getMessage());
            log.error(depEx);
        }
    }

    private void initEndpointConfigs() throws HumanTaskDeploymentException {
        TPublish.Service service = notificationDeploymentConfiguration.getPublish().getService();
        OMElement serviceEle;
        serviceEle = HumanTaskStoreUtils.getOMElement(service.toString());
        EndpointConfiguration endpointConfig = HumanTaskStoreUtils.getEndpointConfig(serviceEle);
        if (endpointConfig != null) {
            endpointConfig.setServiceName(service.getName().getLocalPart());
            endpointConfig.setServicePort(service.getPort());
            endpointConfig.setServiceNS(service.getName().getNamespaceURI());
            endpointConfig.setBasePath(getHumanTaskDefinitionFile().getParentFile().getAbsolutePath());

            addEndpointConfiguration(endpointConfig);
        }
    }

    public TNotification getNotificationDefinition() {
        return notificationDefinition;
    }

    public void setNotificationDefinition(TNotification notificationDefinition) {
        this.notificationDefinition = notificationDefinition;
    }

    public THTDeploymentConfig.Notification getNotificationDeploymentConfiguration() {
        return notificationDeploymentConfiguration;
    }

    public void setNotificatioDeploymentConfiguration(
            THTDeploymentConfig.Notification notificationDeploymentConfiguration) {
        this.notificationDeploymentConfiguration = notificationDeploymentConfiguration;
    }

    @Override
    public QName getPortType() {
        return notificationDefinition.getInterface().getPortType();
    }

    @Override
    public String getOperation() {
        return notificationDefinition.getInterface().getOperation();
    }

    @Override
    public QName getName() {
        return new QName(getTargetNamespace(), notificationDefinition.getName() + "-" + getVersion());
    }

    @Override
    public QName getServiceName() {
        if (notificationDeploymentConfiguration != null &&
            notificationDeploymentConfiguration.getPublish() != null &&
            notificationDeploymentConfiguration.getPublish().getService() != null) {
            return notificationDeploymentConfiguration.getPublish().getService().getName();
        }
        return null;
    }

    @Override
    public String getPortName() {
        if (notificationDeploymentConfiguration != null &&
            notificationDeploymentConfiguration.getPublish() != null &&
            notificationDeploymentConfiguration.getPublish().getService() != null) {
            return notificationDeploymentConfiguration.getPublish().getService().getPort();
        }
        return null;
    }

    @Override
    public TPriorityExpr getPriorityExpression() {
        return notificationDefinition.getPriority();
    }

    @Override
    public QName getDefinitionName() {
        return new QName(getTargetNamespace(), notificationDefinition.getName());
    }

    @Override
    public TPresentationElements getPresentationElements() {
        return notificationDefinition.getPresentationElements();
    }

    @Override
    public ConfigurationType getConfigurationType() {
        return ConfigurationType.NOTIFICATION;
    }

    /**
     * Deadline configuration of task.
     *
     * @return The task deadlines.
     */
    @Override
    public TDeadlines getDeadlines() {
        return null;
    }
}
