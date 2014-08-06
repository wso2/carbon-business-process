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

import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.humantask.TArgument;
import org.wso2.carbon.humantask.TFrom;
import org.wso2.carbon.humantask.core.HumanTaskConstants;
import org.wso2.carbon.humantask.core.dao.OrganizationalEntityDAO;
import org.wso2.carbon.humantask.core.engine.HumanTaskException;
import org.wso2.carbon.humantask.core.engine.PeopleQueryEvaluator;
import org.wso2.carbon.humantask.core.engine.runtime.api.EvaluationContext;
import org.wso2.carbon.humantask.core.engine.runtime.api.HumanTaskRuntimeException;
import org.wso2.carbon.humantask.core.engine.util.CommonTaskUtil;

import java.util.ArrayList;
import java.util.List;

public class LogicalPeopleGroupBasedOrgEntityProvider implements OrganizationalEntityProvider {

    public List<OrganizationalEntityDAO> getOrganizationalEntities(
            PeopleQueryEvaluator peopleQueryEvaluator, TFrom tFrom,
            EvaluationContext evaluationContext) throws HumanTaskException {
        String roleName = null;

        for (TArgument tArgument : tFrom.getArgumentArray()) {
            String expressionLanguage = (tArgument.getExpressionLanguage() == null) ?
                    tFrom.getExpressionLanguage() : tArgument.getExpressionLanguage();
            if (expressionLanguage == null) {
                expressionLanguage = HumanTaskConstants.WSHT_EXP_LANG_XPATH20;
            }
            //TODO what about expression language
            if ("role".equals(tArgument.getName())) {
                roleName = tArgument.newCursor().getTextValue();
                if(roleName != null && roleName.contains("htd:getInput")) {
                roleName =
                        CommonTaskUtil.calculateRole(evaluationContext, roleName,
                                expressionLanguage);
                }
                break;
            }
        }

        if (roleName == null || StringUtils.isEmpty(roleName)) {
            throw new HumanTaskRuntimeException("The role name cannot be empty: " +
                    tFrom.toString());
        } else {
            roleName = roleName.trim();
        }

        List<OrganizationalEntityDAO> orgEnties = new ArrayList<OrganizationalEntityDAO>();
        orgEnties.add(peopleQueryEvaluator.createGroupOrgEntityForRole(roleName));

        return orgEnties;
    }

}
