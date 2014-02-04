/*
 * Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.wso2.carbon.humantask.TDeadline;
import org.wso2.carbon.humantask.TDeadlines;
import org.wso2.carbon.humantask.TPresentationElements;
import org.wso2.carbon.humantask.TPriorityExpr;
import org.wso2.carbon.humantask.TTask;
import org.wso2.carbon.humantask.core.CallBackService;
import org.wso2.carbon.humantask.core.dao.TaskPackageStatus;
import org.wso2.carbon.humantask.core.deployment.HumanTaskDeploymentException;
import org.wso2.carbon.humantask.core.deployment.HumanTaskDeploymentUnit;
import org.wso2.carbon.humantask.core.deployment.config.TCallback;
import org.wso2.carbon.humantask.core.deployment.config.THTDeploymentConfig;
import org.wso2.carbon.humantask.core.deployment.config.TPublish;
import org.wso2.carbon.humantask.core.utils.HumanTaskNamespaceContext;
import org.wso2.carbon.humantask.core.utils.HumanTaskStoreUtils;

import javax.wsdl.Definition;
import javax.wsdl.PortType;
import javax.xml.namespace.QName;
import java.io.File;
import java.util.List;

/**
 * Human Task configuration. Contains Task definition, deployment configurations and can use to get all task
 * related properties such as deadlines, presentation parameters, etc.
 */
public class TaskConfiguration extends HumanTaskBaseConfiguration {
    private static Log log = LogFactory.getLog(TaskConfiguration.class);
    // Task definition
    private TTask task;

    // Task Service/Callback and other configuration info
    private THTDeploymentConfig.Task taskDeploymentConfiguration;

    // WSDL definition for the task response(task parent's service)
    private Definition responseWSDL;

    // Whether both interfaces(task interface ans callback interface) in one WSDL
    private boolean useOneWSDL = false;

    private CallBackService callBackService;

    public TaskConfiguration(TTask task,
                             THTDeploymentConfig.Task taskDeploymentConfiguration,
                             HumanInteractionsDocument humanInteractionsDocument,
                             List<Definition> wsdls,
                             String targetNamespace,
                             String humanTaskArtifactName,
                             AxisConfiguration tenatAxisConf,
                             String packageName,
                             long version,
                             File humanTaskDefinitionFile) {
        super(humanInteractionsDocument, targetNamespace, humanTaskArtifactName, tenatAxisConf,
              true, packageName, version,humanTaskDefinitionFile);

        this.task = task;
        this.taskDeploymentConfiguration = taskDeploymentConfiguration;

        try {
            Definition taskWSDL = findWSDLDefinition(wsdls, getPortType(), getOperation());
            if (taskWSDL == null) {
                throw new HumanTaskDeploymentException("Cannot find WSDL definition for task: " + task.getName());
            }
            setWSDL(taskWSDL);

            HumanTaskNamespaceContext nsContext = new HumanTaskNamespaceContext();
            populateNamespace(task.getDomNode().getNodeType() == Node.ELEMENT_NODE ?
                              (Element) task.getDomNode() : null, nsContext);
            setNamespaceContext(nsContext);

            PortType portType = getWSDL().getPortType(getResponsePortType());
            if (portType != null && portType.getOperation(getResponseOperation(), null, null) != null) {
                useOneWSDL = true;
            }

            if (!useOneWSDL) {
                responseWSDL = findWSDLDefinition(wsdls, getResponsePortType(), getResponseOperation());
            }

            initEndpointConfigs();

        } catch (HumanTaskDeploymentException depEx) {
            this.setPackageStatus(TaskPackageStatus.RETIRED);
            this.setErroneous(true);
            this.setDeploymentError(depEx.getMessage());
            log.error(depEx);
        }
    }

    private void initEndpointConfigs() throws HumanTaskDeploymentException {

        if(taskDeploymentConfiguration == null) {
            throw new HumanTaskDeploymentException("Cannot find task deployment configuration.");
        }

        if (taskDeploymentConfiguration.getPublish() == null ||
            taskDeploymentConfiguration.getPublish().getService() == null) {
            throw new HumanTaskDeploymentException("Cannot find publish element in the htconfig.xml file.");
        }

        TPublish.Service service = taskDeploymentConfiguration.getPublish().getService();
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


        if (taskDeploymentConfiguration.getCallback() == null ||
            taskDeploymentConfiguration.getCallback().getService() == null) {
            throw new HumanTaskDeploymentException("Cannot find callback element in the htconfig.xml file.");
        }

        TCallback.Service cbService = taskDeploymentConfiguration.getCallback().getService();
        serviceEle = HumanTaskStoreUtils.getOMElement(cbService.toString());
        endpointConfig = HumanTaskStoreUtils.getEndpointConfig(serviceEle);
        if (endpointConfig != null) {
            endpointConfig.setServiceName(cbService.getName().getLocalPart());
            endpointConfig.setServicePort(cbService.getPort());
            endpointConfig.setServiceNS(cbService.getName().getNamespaceURI());
            endpointConfig.setBasePath(getHumanTaskDefinitionFile().getParentFile().getAbsolutePath());

            addEndpointConfiguration(endpointConfig);
        }
    }

    public TTask getTask() {
        return task;
    }

    public void setTask(TTask task) {
        this.task = task;
    }

    public Definition getResponseWSDL() {
        if (!useOneWSDL) {
            return responseWSDL;
        } else {
            return getWSDL();
        }
    }

    public QName getResponsePortType() {
        return task.getInterface().getResponsePortType();
    }

    public String getResponseOperation() {
        return task.getInterface().getResponseOperation();
    }

    @Override
    public QName getPortType() {
        return task.getInterface().getPortType();
    }

    @Override
    public String getOperation() {
        return task.getInterface().getOperation();
    }


    @Override
    public QName getName() {
        // Returned with task version appended to the end of the task conf name
        return new QName(getTargetNamespace(), task.getName() + "-" + getVersion());
    }

    @Override
    public QName getServiceName() {
        return taskDeploymentConfiguration.getPublish().getService().getName();
    }

    @Override
    public String getPortName() {
        return taskDeploymentConfiguration.getPublish().getService().getPort();
    }

    public QName getCallbackServiceName() {
        return taskDeploymentConfiguration.getCallback().getService().getName();
    }

    public String getCallbackPortName() {
        return taskDeploymentConfiguration.getCallback().getService().getPort();
    }

    @Override
    public TPriorityExpr getPriorityExpression() {
        return task.getPriority();
    }

    @Override
    public QName getDefinitionName() {
        return new QName(getTargetNamespace(), task.getName());
    }

    @Override
    public TPresentationElements getPresentationElements() {
        return task.getPresentationElements();
    }

    /**
     * Deadline configuration of task.
     *
     * @return The task deadlines.
     */
    @Override
    public TDeadlines getDeadlines() {
        return task.getDeadlines();
    }

    /**
     * Specified Deadline configuration of task.
     *
     * @param name Name of the deadline
     * @return The task deadlines.
     */
    public TDeadline getDeadline(String name) {
        TDeadlines deadlines = getDeadlines();
        for (TDeadline deadline : deadlines.getStartDeadlineArray()) {
            if (deadline.getName().equals(name)) {
                return deadline;
            }
        }
        for (TDeadline deadline : deadlines.getCompletionDeadlineArray()) {
            if (deadline.getName().equals(name)) {
                return deadline;
            }
        }
        return null;
    }

    public CallBackService getCallBackService() {
        return callBackService;
    }

    @Override
    public ConfigurationType getConfigurationType() {
        return ConfigurationType.TASK;
    }

    public void setCallBackService(CallBackService callBackService) {
        this.callBackService = callBackService;
    }
}
