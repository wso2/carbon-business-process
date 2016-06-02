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
package org.wso2.carbon.bpmn.core.internal.mapper;

import org.apache.ibatis.annotations.Insert;
import org.wso2.carbon.bpmn.core.BPMNConstants;
import org.wso2.carbon.bpmn.core.mgt.model.SubstitutesDataModel;

public interface SubstitutesMapper {

    final String INSERT_SUBSTITUTE = "INSERT INTO " + BPMNConstants.ACT_BPS_SUBSTITUTES_TABLE +
            "  (USER, , SUBSTITUTE, SUBSTITUTION_START, SUBSTITUTION_END, ENABLED, TENANT_ID) VALUES ('user', 'substitute', 0, 0, 1, 1)";

    @Insert(INSERT_SUBSTITUTE)
    int insertSubstitute(SubstitutesDataModel substitutesDataModel);

}
