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


import org.apache.axis2.engine.AxisConfiguration;
import org.wso2.carbon.bpel.common.config.EndpointConfiguration;
import org.wso2.carbon.humantask.LeanTaskDocument;
import org.wso2.carbon.humantask.TDeadlines;
import org.wso2.carbon.humantask.TPresentationElements;
import org.wso2.carbon.humantask.TPriorityExpr;
import org.wso2.carbon.humantask.core.HumanTaskConstants;
import org.wso2.carbon.humantask.core.dao.TaskPackageStatus;
import org.wso2.carbon.humantask.core.utils.HumanTaskNamespaceContext;

import javax.wsdl.Definition;
import javax.wsdl.PortType;
import javax.xml.namespace.QName;
import java.io.File;
import java.util.ArrayList;
import java.util.List;


public abstract class LeanTaskConfiguration {

    public static enum ConfigurationType {
        LEAN_TASK
    }

    private long id;
    private long version;
    private LeanTaskDocument leanTaskObject;
    private String defaultExpressionLanguage = HumanTaskConstants.WSHT_EXP_LANG_XPATH20;
    private HumanTaskNamespaceContext namespaceContext = new HumanTaskNamespaceContext();
    private String leanTaskArtifactName;
    private boolean isLeantask;
    private AxisConfiguration tenantAxisConf;
    private String packageName;
    private File leanTaskDefinitionFile;
    private List<EndpointConfiguration> endpointConfigs = new ArrayList<EndpointConfiguration>();
    private TaskPackageStatus packageStatus = TaskPackageStatus.ACTIVE;
    private boolean isErroneous = false;
    private String deploymentError = "NONE";

    public LeanTaskConfiguration(){}

    public LeanTaskConfiguration(LeanTaskDocument leanTaskObject,
                                 String leanTaskArtifactName,
                                 AxisConfiguration tenantAxisConf,
                                 boolean isLeantask, String packageName,
                                 long version,
                                 File leanTaskDefinitionFile){

        this.leanTaskObject = leanTaskObject;
        this.leanTaskArtifactName = leanTaskArtifactName;
        this.isLeantask = isLeantask;
        this.tenantAxisConf = tenantAxisConf;
        this.packageName = packageName;
        this.leanTaskDefinitionFile = leanTaskDefinitionFile;
        this.packageStatus = TaskPackageStatus.ACTIVE;
        this.version = version;

        //if (leanTaskDocument.getLeanTask().getExpressionLanguage() != null) {
        //    this.defaultExpressionLanguage = leanTaskDocument.getHumanInteractions().
        //            getExpressionLanguage().trim();
        //}

        //if (humanInteractionsDocument.getHumanInteractions().getExpressionLanguage() != null) {
         //   this.defaultExpressionLanguage = humanInteractionsDocument.getHumanInteractions().
        //            getExpressionLanguage().trim();
        //}
    }


    public String getExpressionLanguage() { return defaultExpressionLanguage;
    }

    public HumanTaskNamespaceContext getNamespaceContext() {
        return namespaceContext;
    }

    public void setNamespaceContext(HumanTaskNamespaceContext namespaceContext) {
        this.namespaceContext = namespaceContext;
    }

    public boolean isLeanTask() {
        return isLeantask;
    }

    public String getPackageName() {
        return packageName;
    }

    public File getLeanTaskDefinitionFile() {
        return leanTaskDefinitionFile;
    }

    public void setLeanTaskDefinitionFile(File leanTaskDefinitionFile) {
        this.leanTaskDefinitionFile = leanTaskDefinitionFile;
    }

    public TaskPackageStatus getPackageStatus() {
        return packageStatus;
    }

    public void setPackageStatus(TaskPackageStatus packageStatus) {
        this.packageStatus = packageStatus;
    }

    public boolean isErroneous() {
        return isErroneous;
    }

    public void setErroneous(boolean erroneous) {
        isErroneous = erroneous;
    }

    public String getDeploymentError() {
        return deploymentError;
    }

    public void setDeploymentError(String deploymentError) {
        this.deploymentError = deploymentError;
    }

    public void addEndpointConfiguration(EndpointConfiguration endpointConfig) {
        endpointConfigs.add(endpointConfig);
    }

    public long getVersion() {
        return this.version;
    }

    public abstract QName getPortType();

    public abstract String getOperation();

    public abstract TPresentationElements getPresentationElements();

    public abstract QName getName();

    public abstract QName getServiceName();

    public abstract String getPortName();

    public abstract TPriorityExpr getPriorityExpression();

    public abstract TDeadlines getDeadlines();

    public abstract ConfigurationType getConfigurationType();

    public abstract QName getDefinitionName();

    /**
     * Returns the qualified name of the lean task.
     * @param wsdls
     * @param portType
     * @param operation
     * @return
     */
    protected Definition findWSDLDefinition(List<Definition> wsdls, QName portType, String operation) {
        for (Definition wsdlDef : wsdls) {
            PortType port = wsdlDef.getPortType(portType);
            if (port != null && port.getOperation(operation, null, null) != null) {
                return wsdlDef;
            }
        }
        return null;
    }


}
