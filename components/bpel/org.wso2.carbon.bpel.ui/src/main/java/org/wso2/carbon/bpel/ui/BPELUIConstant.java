/*
 * Copyright 2005-2007 WSO2, Inc. (http://wso2.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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

package org.wso2.carbon.bpel.ui;

/**
 * BPEL UI constants.
 */
public final class BPELUIConstant {
    public static final String INSTANCE_STATE_ACTIVE = "ACTIVE";
    public static final String INSTANCE_STATE_COMPLETED = "COMPLETED";
    public static final String INSTANCE_STATE_TERMINATED = "TERMINATED";
    public static final String INSTANCE_STATE_FAILED = "FAILED";
    public static final String INSTANCE_STATE_SUSPENDED = "SUSPENDED";
    public static final String INSTANCE_LIFETIME_TILL_LASTACTIVE = "CREATED_TO_LASTACTIVE";
    public static final String INSTANCE_LIFETIME_FROM_LASTACTIVE_TO_NOW = "LASTACTIVE_TO_NOW";
    public static final String TOTAL_INSTANCES = "TOTAL";

    private BPELUIConstant() {
    }


}
