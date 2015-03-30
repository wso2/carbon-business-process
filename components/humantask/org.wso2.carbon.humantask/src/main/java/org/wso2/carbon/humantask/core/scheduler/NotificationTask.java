/*
 * Copyright (c) 2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.humantask.core.scheduler;

import org.wso2.carbon.event.output.adapter.core.OutputEventAdapter;

import java.util.Map;

public class NotificationTask implements Runnable {
    private OutputEventAdapter outputEventAdapter;
    private Object message;
    private Map<String, String> dynamicProperties;

    public NotificationTask(OutputEventAdapter adapter, Object message, Map<String, String> dynamicProperties) {
        this.outputEventAdapter = adapter;
        this.message = message;
        this.dynamicProperties = dynamicProperties;
    }

    public void run() {
        outputEventAdapter.publish(message, dynamicProperties);

    }
}
