/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.bpmn.extensions.substitution;

import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bpmn.core.mgt.dao.ActivitiDAO;
import org.wso2.carbon.bpmn.core.mgt.model.SubstitutesDataModel;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

public class UserSubstitutionTaskListener implements TaskListener{

    private static final Log log = LogFactory.getLog(UserSubstitutionTaskListener.class);

    ActivitiDAO dao = new ActivitiDAO();

    @Override
    public void notify(DelegateTask delegateTask) {

        String assignee = delegateTask.getAssignee();

        String substitute = getSubstituteIfEnabled(assignee);
        if (substitute != null) {
            delegateTask.setAssignee(substitute);
            if (log.isDebugEnabled()) {
                log.debug("User: " + assignee + "is substituted by : " + substitute + "for the task" + delegateTask.getName());
            }
        }
    }

    /**
     * Return the Substitute if exist and active or return null
     * @param assignee
     */
    private String getSubstituteIfEnabled(String assignee) {
        //retrieve Substitute info
        SubstitutesDataModel substitutesDataModel = getImmediateSubstitute(MultitenantUtils.getTenantAwareUsername(assignee));
        if (substitutesDataModel != null && UserSubstitutionOperations.isSubstitutionActive(substitutesDataModel)) {
            return UserSubstitutionOperations.getTransitiveSubstitute(substitutesDataModel.getSubstitute());
        } else {
            return null;
        }

    }

    private SubstitutesDataModel getImmediateSubstitute(String assignee){
        return dao.selectSubstituteInfo(assignee, PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId());
    }
}
