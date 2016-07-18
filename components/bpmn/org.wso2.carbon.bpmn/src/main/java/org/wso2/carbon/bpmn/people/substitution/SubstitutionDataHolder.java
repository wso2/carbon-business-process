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
package org.wso2.carbon.bpmn.people.substitution;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bpmn.core.BPMNConstants;
import org.wso2.carbon.bpmn.core.mgt.dao.ActivitiDAO;
import org.wso2.carbon.bpmn.core.utils.BPMNActivitiConfiguration;

public final class SubstitutionDataHolder {
    private static final Log log = LogFactory.getLog(SubstitutionDataHolder.class);

    private static SubstitutionDataHolder dataHolder = new SubstitutionDataHolder();
    private ActivitiDAO activitiDAO = new ActivitiDAO();
    private TransitivityResolver resolver = new TransitivityResolver(activitiDAO);
    private Boolean substitutionFeatureEnabled = null;
    public static final String TRUE = "true";
    private Boolean transitivityEnabled = null;

    private SubstitutionDataHolder(){}

    public static SubstitutionDataHolder getInstance() {
        return dataHolder;
    }

    /**
     * Get an Activiti DAO instance
     * @return ActivitiDAO
     */
    public ActivitiDAO getActivitiDAO() {
        return activitiDAO;
    }

    /**
     * Get a transitivity resolver instance
     * @return TransitivityResolver
     */
    public TransitivityResolver getTransitivityResolver() {
        return resolver;
    }

    /**
     * Get the substitution feature enabled config value or default value
     * @return true if substitution enabled
     */
    public boolean isSubstitutionFeatureEnabled() {
        if (substitutionFeatureEnabled == null) {
            substitutionFeatureEnabled = false;
            BPMNActivitiConfiguration activitiConfiguration = BPMNActivitiConfiguration.getInstance();
            if (activitiConfiguration != null) {
                String enabledString = activitiConfiguration
                        .getBPMNPropertyValue(BPMNConstants.SUBSTITUTION_CONFIG, BPMNConstants.SUBSTITUTION_ENABLED);
                if (TRUE.equalsIgnoreCase(enabledString)) {
                    substitutionFeatureEnabled = true;
                }
            }
        }
        return substitutionFeatureEnabled;
    }

    /**
     * Get the transitivity enabled value for substitution from configuration.
     * @return true if transitivity enabled
     */
    public boolean isTransitivityEnabled() {
        if (transitivityEnabled != null) {
            return transitivityEnabled;
        } else {
            transitivityEnabled = BPMNConstants.SUBSTITUTION_TRANSITIVITY_DEFAULT;
            BPMNActivitiConfiguration bpmnActivitiConfiguration = BPMNActivitiConfiguration.getInstance();

            if (bpmnActivitiConfiguration != null) {
                String transitivityEnabledProperty = bpmnActivitiConfiguration
                        .getBPMNPropertyValue(BPMNConstants.SUBSTITUTION_CONFIG,
                                BPMNConstants.SUBSTITUTION_TRANSITIVITY_PROPERTY);

                if (transitivityEnabledProperty != null) {
                    if (transitivityEnabledProperty.trim().equalsIgnoreCase("true") || transitivityEnabledProperty.trim()
                            .equalsIgnoreCase("false")) {
                        transitivityEnabled = Boolean.parseBoolean(transitivityEnabledProperty);
                        if (log.isDebugEnabled()) {
                            log.debug("User substitution transitivity enabled : " + transitivityEnabled);
                        }
                    } else {
                        log.warn("Invalid value for the property: " + BPMNConstants.SUBSTITUTION_TRANSITIVITY_PROPERTY
                                + ". Transitivity is being disabled by default.");
                    }
                }
            }
            return transitivityEnabled;
        }

    }

}
