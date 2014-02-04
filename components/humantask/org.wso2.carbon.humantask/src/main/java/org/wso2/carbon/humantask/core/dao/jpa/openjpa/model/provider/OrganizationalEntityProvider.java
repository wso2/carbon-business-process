/*
 *  Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.humantask.core.dao.jpa.openjpa.model.provider;

import org.wso2.carbon.humantask.TFrom;
import org.wso2.carbon.humantask.core.dao.OrganizationalEntityDAO;
import org.wso2.carbon.humantask.core.engine.HumanTaskException;
import org.wso2.carbon.humantask.core.engine.PeopleQueryEvaluator;
import org.wso2.carbon.humantask.core.engine.runtime.api.EvaluationContext;

import java.util.List;

/**
 * Provides the abstraction needed for creating organizational entity providers.
 * This needs to be implemented separately to provide the ability to extract the organizational
 * entities associated with the generic human roles.
 */
public interface OrganizationalEntityProvider {

    /**
     * Extracts the organizational entities given the people query evaluator, "From" clause of a
     * generic human role and the evaluation context.
     *
     * @param peopleQueryEvaluator  People query evaluator associated with the task engine
     * @param tFrom                 "From" clause of the associated generic human role
     * @param evaluationContext     Evaluation context of a particular task
     * @return                      Organizational entities that satisfy the aforementioned conditions
     * @throws HumanTaskException   Is thrown if the process of extracting organizational entities is
     *                              interrupted
     */
    List<OrganizationalEntityDAO> getOrganizationalEntities(
            PeopleQueryEvaluator peopleQueryEvaluator, TFrom tFrom,
            EvaluationContext evaluationContext) throws HumanTaskException;
    
}
