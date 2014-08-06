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

package org.wso2.carbon.humantask.core.engine.runtime.xpath;

/**
 *
 */
public final class XPath2Constants {
    private XPath2Constants() {
    }

    public static final String FUNCTION_GET_POTENTIAL_OWNERS = "getPotentialOwners";

    public static final String FUNCTION_GET_ACTUAL_OWNER = "getActualOwner";

    public static final String FUNCTION_GET_TASK_INITIATOR = "getTaskInitiator";

    public static final String FUNCTION_GET_TASK_STAKEHOLDERS = "getTaskStakeholders";

    public static final String FUNCTION_GET_BUSINESS_ADMINISTRATORS = "getBusinessAdministrators";

    public static final String FUNCTION_GET_EXCLUDED_OWNERS = "getExcludedOwners";

    public static final String FUNCTION_GET_TASK_PRIORITY = "getTaskPriority";

    public static final String FUNCTION_GET_INPUT = "getInput";

    public static final String FUNCTION_GET_LOGICAL_PEOPLE_GROUP = "getLogicalPeopleGroup";

    public static final String WSHT_EXP_LANG_XPATH20 = "urn:wsht:sublang:xpath2.0";

    public static final String FUNCTION_UNION = "union";

    public static final String FUNCTION_INTERSECT = "intersect";

    public static final String FUNCTION_EXCEPT = "except";
}
