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

package org.wso2.carbon.humantask.core.engine.runtime.api;

import org.w3c.dom.Node;
import org.wso2.carbon.humantask.core.dao.GenericHumanRoleDAO;
import org.wso2.carbon.humantask.core.dao.MessageDAO;

import javax.xml.namespace.NamespaceContext;

/**
 * Context for evaluating expressions. Implementation of the
 * {@link ExpressionLanguageRuntime} interface use this interface to access
 * the properties and input messages.
 *
 */
public interface EvaluationContext {

    /**
     * Return the input message to the task.
     * @return Message
     */
    MessageDAO getInput();

    /**
     * Return generic human role of give type
     * @param ghrType generic human role type
     * @return generic human role
     */
    GenericHumanRoleDAO getGenericHumanRole(GenericHumanRoleDAO.GenericHumanRoleType ghrType);

    /**
     * Return the namespace context for the current task
     * @return NamespaceContext
     * @throws Exception :
     */
    NamespaceContext getNameSpaceContextOfTask() throws Exception;

    /**
     * Get the root node of the payload to task
     * @return Node DOM node
     * @throws Exception :
     */
    Node getRootNode() throws Exception;
}
