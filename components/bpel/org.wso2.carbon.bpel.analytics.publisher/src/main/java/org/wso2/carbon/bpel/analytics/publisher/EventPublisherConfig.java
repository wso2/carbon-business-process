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

package org.wso2.carbon.bpel.analytics.publisher;

import org.wso2.carbon.databridge.agent.thrift.AsyncDataPublisher;
import org.wso2.carbon.databridge.agent.thrift.lb.LoadBalancingDataPublisher;

import java.util.concurrent.ConcurrentHashMap;

/**
 * DataPublisherHolder <code>&lt;EventPublisherConfig&gt;</code>
 */
public class EventPublisherConfig {
    private AsyncDataPublisher dataPublisher;
    private LoadBalancingDataPublisher loadBalancingDataPublisher;

    private ConcurrentHashMap<String, String>  addedStreamDefinitions;

    public EventPublisherConfig(AsyncDataPublisher publisher) {
        this.dataPublisher = publisher;
        addedStreamDefinitions = new ConcurrentHashMap<String, String>();
    }

    public EventPublisherConfig(LoadBalancingDataPublisher loadBalancingDataPublisher) {
        this.loadBalancingDataPublisher = loadBalancingDataPublisher;
        addedStreamDefinitions = new ConcurrentHashMap<String, String>();
    }

    public void addEventStream(String streamName, String version) {
        addedStreamDefinitions.put(streamName + version, "exist");
    }

    public boolean eventStreamAlreadyDefined(String streamName, String version) {
        Object result = null;
        result = addedStreamDefinitions.get(streamName + version);
        if(result != null)
            return true;
        else
            return false;
    }

    public AsyncDataPublisher getDataPublisher() {
        return dataPublisher;
    }

    public LoadBalancingDataPublisher getLoadBalancingDataPublisher() {
        return loadBalancingDataPublisher;
    }
}
