/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.wso2.carbon.bpmn.core.BPMNConstants;
import org.wso2.carbon.bpmn.core.mgt.dao.ActivitiDAO;
import org.wso2.carbon.bpmn.core.mgt.model.SubstitutesDataModel;

import java.util.Map;

public class TransitivityResolver {

    private int tenantId;
    private ActivitiDAO dao;
    Map<String, SubstitutesDataModel> subsMap;

    private TransitivityResolver(){}

    protected TransitivityResolver(ActivitiDAO activitiDAO, int tenantId) {
        this.dao = activitiDAO;
        this.tenantId = tenantId;
    }

    /**
     * Recalculate all the transitive substitutes for the given substitutes
     * @param forcedResolve - if true, Continues to resolve even if transitivity unresolvable
     * @return false if unresolvable state found while forced resolve disabled
     */
    protected synchronized boolean resolveTransitiveSubs(boolean forcedResolve) {
        subsMap = dao.selectActiveSubstitutesByTenant(tenantId);//get only enabled
        for (Map.Entry<String, SubstitutesDataModel> entry : subsMap.entrySet())
        {
            String transitiveSub = entry.getValue().getTransitiveSub();
            if(transitiveSub == null) {
                transitiveSub = calculateTransitiveSubstitute(entry.getValue(), entry.getKey(), entry.getValue().getSubstitute());
            }
            if (!forcedResolve && BPMNConstants.TRANSITIVE_SUB_UNDEFINED.equals(transitiveSub)) { //unresolvable sub found and forced resolve not enabled
                return false;
            }
        }

        //persist the map, cannot do this in above loop since it may run into unresolved state
        for (Map.Entry<String, SubstitutesDataModel> entry : subsMap.entrySet()) {
            dao.updateTransitiveSub(entry.getKey(), tenantId, entry.getValue().getTransitiveSub());
        }


        return true;
    }

    /**
     * Check if the given user unavailability affects the existing transitivity.
     * @param user - user getting unavailable
     */
    public boolean isResolvingRequired(String user) {

        if (dao.countUserAsSubstitute(user, tenantId) > 0) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Recursively look for a available substitute for given data model user. Makes a DB call for each recursive iteration.
     * @param substituteDataModel data model that need the transitive sub
     * @return available addSubstituteInfo name
     */
    private synchronized String calculateTransitiveSubstitute(SubstitutesDataModel substituteDataModel, String originUser, String originSub) {
        String newSub = null;
        SubstitutesDataModel nextDataModel = subsMap.get(substituteDataModel.getSubstitute());

        if (nextDataModel != null) {
            if (nextDataModel.getSubstitute().equals(originUser) || nextDataModel.getSubstitute().equals(originSub)) {//circular dependency, could not resolve
                newSub = BPMNConstants.TRANSITIVE_SUB_UNDEFINED;
            } else if(BPMNConstants.TRANSITIVE_SUB_NOT_APPLICABLE.equals(nextDataModel.getTransitiveSub())) {
                newSub = nextDataModel.getSubstitute();
            } else if (nextDataModel.getTransitiveSub() != null) {
                newSub = nextDataModel.getTransitiveSub();
            } else {
                newSub = calculateTransitiveSubstitute(nextDataModel, originUser, originSub);
            }

        } else { //original substitute is available

            newSub = substituteDataModel.getSubstitute();
        }

        updateMap(substituteDataModel, newSub);
        return newSub;
    }

    private void updateMap(SubstitutesDataModel substituteDataModel, String substitute) {
        SubstitutesDataModel model = substituteDataModel;
        if (substituteDataModel.getSubstitute().equals(substitute)) {
            model.setTransitiveSub(BPMNConstants.TRANSITIVE_SUB_NOT_APPLICABLE);
        } else {
            model.setTransitiveSub(substitute);
        }
        subsMap.put(substituteDataModel.getUser(), model);
    }

    /**
     * Check if an active substitution available for given substitute info
     * @param substitutesDataModel
     * @return true if substitution active
     */
    private boolean isSubstitutionActive(SubstitutesDataModel substitutesDataModel) {
        long startDate = substitutesDataModel.getSubstitutionStart().getTime();
        long endDate = substitutesDataModel.getSubstitutionEnd().getTime();
        long currentTime = System.currentTimeMillis();

        if (substitutesDataModel.isEnabled() && (startDate < currentTime) && (endDate > currentTime)) {
            return true;
        }
        return false;
    }

    protected synchronized boolean  resolveSubstituteForSingleUser(SubstitutesDataModel dataModel) {
        subsMap = dao.selectActiveSubstitutesByTenant(tenantId);
        SubstitutesDataModel subDataModel = dao.selectSubstituteInfo(dataModel.getSubstitute(), tenantId);
        if (subDataModel != null) {
            String newSub = calculateTransitiveSubstitute(dataModel, dataModel.getUser(), dataModel.getSubstitute());
            if (BPMNConstants.TRANSITIVE_SUB_UNDEFINED.equals(newSub)) {
                return false;
            } else {
                dao.updateTransitiveSub(dataModel.getUser(), tenantId, newSub);
                return true;
            }
        } else {
            dao.updateTransitiveSub(dataModel.getUser(), tenantId, BPMNConstants.TRANSITIVE_SUB_NOT_APPLICABLE);
            return true;
        }
    }
}
