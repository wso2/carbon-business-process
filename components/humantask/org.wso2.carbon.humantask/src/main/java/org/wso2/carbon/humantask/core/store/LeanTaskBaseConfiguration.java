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

<<<<<<< HEAD
import org.wso2.carbon.humantask.*;
=======
import org.wso2.carbon.humantask.TDeadlines;
import org.wso2.carbon.humantask.TRenderings;
import org.wso2.carbon.humantask.TExpression;
import org.wso2.carbon.humantask.TQuery;
import org.wso2.carbon.humantask.TPresentationElements;
import org.wso2.carbon.humantask.TDelegation;
import org.wso2.carbon.humantask.TPeopleAssignments;
import org.wso2.carbon.humantask.TPriorityExpr;
import org.wso2.carbon.humantask.TMessageSchema;
>>>>>>> 8fad7dee1bf193d56667148f2ca320ceb615d5d2
import org.wso2.carbon.humantask.core.HumanTaskConstants;
import org.wso2.carbon.humantask.core.dao.TaskPackageStatus;
import org.wso2.carbon.humantask.core.utils.HumanTaskNamespaceContext;

<<<<<<< HEAD
=======
import javax.xml.namespace.QName;

>>>>>>> 8fad7dee1bf193d56667148f2ca320ceb615d5d2
public abstract class LeanTaskBaseConfiguration {

    public static enum ConfigurationType {
        LEAN_TASK
    }

    private long id;
    private int tenantId;
    private long version;
    private String task_versionName;
    private String defaultExpressionLanguage = HumanTaskConstants.WSHT_EXP_LANG_XPATH20;
    private HumanTaskNamespaceContext namespaceContext = new HumanTaskNamespaceContext();
    private String leanTaskDefName;
<<<<<<< HEAD
    private boolean isLeantask = true;
=======
    private boolean isLeantask;
>>>>>>> 8fad7dee1bf193d56667148f2ca320ceb615d5d2
    private TaskPackageStatus packageStatus = TaskPackageStatus.ACTIVE;
    private boolean isErroneous = false;
    private String deploymentError = "NONE";

    public LeanTaskBaseConfiguration() {
    }

<<<<<<< HEAD
    public LeanTaskBaseConfiguration(int tenatId, String task_versionName, String leanTaskDefName,
                                     long version) {

        this.tenantId = tenatId;
        this.task_versionName = task_versionName;
        this.leanTaskDefName = leanTaskDefName;
=======
    public LeanTaskBaseConfiguration(int tenatId,String task_versionName,String leanTaskDefName,
                                     long version) {

        this.tenantId = tenatId;
        this.leanTaskDefName = leanTaskDefName;
        this.task_versionName = task_versionName;
>>>>>>> 8fad7dee1bf193d56667148f2ca320ceb615d5d2
        this.packageStatus = TaskPackageStatus.ACTIVE;
        this.version = version;
    }


    public String getExpressionLanguage() {
        return defaultExpressionLanguage;
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

    public long getVersion() {
        return this.version;
    }

<<<<<<< HEAD
    //public abstract QName getName();
=======
    public abstract QName getName();
>>>>>>> 8fad7dee1bf193d56667148f2ca320ceb615d5d2


    public abstract TDeadlines getDeadlines();

    public abstract TRenderings getRenderings();

    public abstract TExpression getSearchBy();

    public abstract TQuery getOutcome();

    public abstract TPresentationElements getPresentationElements();

    public abstract TDelegation getDelegation();

    public abstract TPeopleAssignments getPeopleAssignments();

    public abstract TPriorityExpr getPriorityExpression();

    public abstract TMessageSchema getMessageSchema();
}
