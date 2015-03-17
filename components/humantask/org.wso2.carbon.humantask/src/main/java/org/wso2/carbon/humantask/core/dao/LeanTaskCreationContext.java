/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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


package org.wso2.carbon.humantask.core.dao;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Element;
import org.wso2.carbon.humantask.core.engine.PeopleQueryEvaluator;
import org.wso2.carbon.humantask.core.engine.runtime.api.EvaluationContext;
import org.wso2.carbon.humantask.core.store.LeanTaskConfiguration;

import javax.xml.namespace.QName;
import java.util.Map;


public class LeanTaskCreationContext {

    private static Log log = LogFactory.getLog(LeanTaskCreationContext.class);

    private LeanTaskConfiguration taskConfiguration;
    private Integer tenantId;
    private Map<String, Element> messageBodyParts;
    private QName messageName;
    private String createdBy;
    private PeopleQueryEvaluator peopleQueryEvaluator;
    private EvaluationContext evalContext;

    public LeanTaskConfiguration getTaskConfiguration() {
        return taskConfiguration;
    }

    public void setTaskConfiguration(LeanTaskConfiguration taskConfiguration) {
        this.taskConfiguration = taskConfiguration;
    }

    public Integer getTenantId() {
        return tenantId;
    }

    public void setTenantId(Integer tenantId) {
        this.tenantId = tenantId;
    }

    public PeopleQueryEvaluator getPeopleQueryEvaluator() {
        return peopleQueryEvaluator;
    }

    public void setPeopleQueryEvaluator(PeopleQueryEvaluator peopleQueryEvaluator) {
        this.peopleQueryEvaluator = peopleQueryEvaluator;
    }

    public EvaluationContext getEvalContext() {
        return evalContext;
    }

    public void setEvalContext(EvaluationContext evalContext) {
        this.evalContext = evalContext;
    }

    public void injectExpressionEvaluationContext(TaskDAO task) {
        if (taskConfiguration == null) {
            throw new RuntimeException("The task configuration is empty in the task creation context");
        }
        //Todo
        //EvaluationContext evaluationContext = new ExpressionEvaluationContext(task, this.getTaskConfiguration());
        //this.setEvalContext(evaluationContext);
    }

    public QName getMessageName() {
        return messageName;
    }

    public void setMessageName(QName messageName) {
        this.messageName = messageName;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public void addMessageBodyPart(String name, Element part) {
        messageBodyParts.put(name, part);
    }

    public Map<String, Element> getMessageBodyParts() {
        return messageBodyParts;
    }

    public void setMessageBodyParts(Map<String, Element> messageBodyParts) {
        this.messageBodyParts = messageBodyParts;
    }

    //getAttachmentIDs


}
