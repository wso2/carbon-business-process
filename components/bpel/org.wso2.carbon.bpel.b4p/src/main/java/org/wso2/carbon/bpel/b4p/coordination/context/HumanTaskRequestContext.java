/*
 * Copyright (c) 2016 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.bpel.b4p.coordination.context;

/**
 * HumanTask Request Context interface, contains overriding attributes which are used at task creation.
 */
public interface HumanTaskRequestContext {

    /**
     * Defining Getters setters for HumanTask Override attributes defined in HumanTaskSpecification.
     */

    public int getTaskPriority();

    public void setTaskPriority(int priority);

    public boolean isSkippable();

    public void setSkippable(boolean skippable);

    //TODO Implement Actual people assignments

    public long getTaskExpirationTime();

    public void setTaskExpirationTime(long taskExpirationTime);

    public long getTaskActivationDifferedTime();

    public void setTaskActivationDifferedTime(long taskActivationDifferedTime);

}
