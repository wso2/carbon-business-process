/*
 * Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.humantask.core.api.event;

/**
 * Human task event listener interface.
 * The onEvent method will be called when a new event is generated.
 */
public interface HumanTaskEventListener {

    /**
     * This method will be called by the human task engine when a new event is generated.
     * The listener logic should be implemented in the method body.
     *
     * @param taskEventInfo : A final object containing the context information related to the
     * task event and the task itself.
     */
    public void onEvent(TaskEventInfo taskEventInfo);

}
