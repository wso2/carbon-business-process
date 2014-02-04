/*
 * Copyright (c) 2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.humantask.core.engine.runtime;

import org.w3c.dom.Node;
import org.wso2.carbon.humantask.core.dao.GenericHumanRoleDAO;
import org.wso2.carbon.humantask.core.dao.MessageDAO;
import org.wso2.carbon.humantask.core.dao.TaskDAO;
import org.wso2.carbon.humantask.core.engine.runtime.api.EvaluationContext;
import org.wso2.carbon.humantask.core.store.HumanTaskBaseConfiguration;

import javax.xml.namespace.NamespaceContext;

/**
 *
 */
public class ExpressionEvaluationContext implements EvaluationContext {
    private HumanTaskBaseConfiguration taskConfig;

    private TaskDAO task;

    /**
     * @param taskDAO    TaskDAO
     * @param taskConfig Task Configuration
     */
    public ExpressionEvaluationContext(TaskDAO taskDAO, HumanTaskBaseConfiguration taskConfig) {
        this.task = taskDAO;
        this.taskConfig = taskConfig;
    }

    /**
     * Return the input message to the task.
     *
     * @return Message
     */
    @Override
    public MessageDAO getInput() {
        return task.getInputMessage();
    }

    /**
     * Return generic human role of give type
     *
     * @param ghrType generic human role type
     * @return generic human role
     */
    @Override
    public GenericHumanRoleDAO getGenericHumanRole(GenericHumanRoleDAO.GenericHumanRoleType ghrType) {
        return null; //TODO - implement me
    }

    /**
     * Return the namespace context for the current task
     *
     * @return NamespaceContext
     * @throws Exception :
     */
    @Override
    public NamespaceContext getNameSpaceContextOfTask() throws Exception {
        return taskConfig.getNamespaceContext();
    }

    /**
     * Get the root node of the payload to task
     *
     * @return Node DOM node
     * @throws Exception :
     */
    @Override
    public Node getRootNode() throws Exception {
        return task.getInputMessage().getBodyData();
    }
}
