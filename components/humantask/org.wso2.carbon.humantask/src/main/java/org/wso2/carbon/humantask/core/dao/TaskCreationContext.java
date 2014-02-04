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

package org.wso2.carbon.humantask.core.dao;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.wso2.carbon.humantask.core.engine.PeopleQueryEvaluator;
import org.wso2.carbon.humantask.core.engine.runtime.ExpressionEvaluationContext;
import org.wso2.carbon.humantask.core.engine.runtime.api.EvaluationContext;
import org.wso2.carbon.humantask.core.store.HumanTaskBaseConfiguration;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.wso2.carbon.bpel.common.constants.Constants;

/**
 * The context data required to create a task object.
 */
public class TaskCreationContext {
    private static Log log = LogFactory.getLog(TaskCreationContext.class);

    private HumanTaskBaseConfiguration taskConfiguration;

    private Integer tenantId;

    /** The request message body */
    private Map<String, Element> messageBodyParts;

    /** The request message header */
    private Map<String, Element> messageHeaderParts;

    /** The request message name. */
    private QName messageName;

    /** The user creating the task. */
    private String createdBy;

    private PeopleQueryEvaluator peopleQueryEvaluator;

    private EvaluationContext evalContext;

    public HumanTaskBaseConfiguration getTaskConfiguration() {
        return taskConfiguration;
    }

    public void setTaskConfiguration(HumanTaskBaseConfiguration taskConfiguration) {
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
        EvaluationContext evaluationContext = new ExpressionEvaluationContext(task, this.getTaskConfiguration());
        this.setEvalContext(evaluationContext);
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

    public void addMessageHeaderPart(String name, Element part) {
        messageHeaderParts.put(name, part);
    }

    public Map<String, Element> getMessageBodyParts() {
        return messageBodyParts;
    }

    public Map<String, Element> getMessageHeaderParts() {
        return messageHeaderParts;
    }

    public void setMessageBodyParts(Map<String, Element> messageBodyParts) {
        this.messageBodyParts = messageBodyParts;
    }

    public void setMessageHeaderParts(Map<String, Element> messageHeaderParts) {
        this.messageHeaderParts = messageHeaderParts;
    }

    /**
     * Extract the header content related to attachment-ids and create a list from of those attachment-ids
     *
     * @return list of attachment-ids
     */
    public List<String> getAttachmentIDs() {
        List<String> attachmentIDs = new ArrayList<String>();

        final String NAMESPACE = Constants.ATTACHMENT_ID_NAMESPACE;
        final String NAMESPACE_PREFIX = Constants.ATTACHMENT_ID_NAMESPACE_PREFIX;
        final String PARENT_ELEMENT_NAME = Constants.ATTACHMENT_ID_PARENT_ELEMENT_NAME;
        final String CHILD_ELEMENT_NAME = Constants.ATTACHMENT_ID_CHILD_ELEMENT_NAME;

        Element attachmentElement = this.messageHeaderParts.get(PARENT_ELEMENT_NAME);

        if (attachmentElement != null && NAMESPACE.equals(attachmentElement.getNamespaceURI())) {
            NodeList childElementList = attachmentElement.getElementsByTagNameNS(NAMESPACE, CHILD_ELEMENT_NAME);
            int size = childElementList.getLength();
            for (int i = 0; i < size; i++) {
                Element child = (Element) childElementList.item(i);
                attachmentIDs.add(child.getTextContent());
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("No header elements found with :" + PARENT_ELEMENT_NAME);
            }
        }

        return attachmentIDs;
    }
}
