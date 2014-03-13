/*
 * Copyright (c) 2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.bpel.bam.publisher;

import org.wso2.carbon.databridge.agent.thrift.DataPublisher;
import org.wso2.carbon.databridge.agent.thrift.conf.AgentConfiguration;
import org.wso2.carbon.databridge.agent.thrift.Agent;


import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TenantBamAgentHolder {
    private static TenantBamAgentHolder instance;

    private final Map<Integer, DataPublisher> tenantDataPublisherMap =
            new ConcurrentHashMap<Integer, DataPublisher>();

    private static volatile Agent agent;

    private TenantBamAgentHolder() {
    }

    public static TenantBamAgentHolder getInstance() {
        if (null == instance) {
            synchronized (TenantBamAgentHolder.class) {
                if(instance == null)
                instance = new TenantBamAgentHolder();
            }
        }
        return instance;
    }

    public synchronized Agent createAgent(AgentConfiguration configuration){
         if(agent == null){
            agent = new Agent(configuration);
        }
        return agent;
    }

    public Agent getAgent(Integer tenantId) {
        return agent;
    }

    public  DataPublisher getDataPublisher(Integer tenantId) {
        return tenantDataPublisherMap.get(tenantId);
    }

    public synchronized  void addDataPublisher(Integer tenantId, DataPublisher publisher) {
        tenantDataPublisherMap.put(tenantId, publisher);
    }

    public synchronized void removeDataPublisher(Integer tenantId) {
        tenantDataPublisherMap.remove(tenantId);
    }

    public synchronized void removeAgent() {
        agent.shutdown();
    }

    public synchronized void removeDataPublishers() {
        Collection<DataPublisher> publishers = tenantDataPublisherMap.values();
        Iterator<DataPublisher> iterator = publishers.iterator();
        while(iterator.hasNext()){
           DataPublisher publisher = iterator.next();
            publisher.stop();
        }
        publishers.clear();
    }
}
