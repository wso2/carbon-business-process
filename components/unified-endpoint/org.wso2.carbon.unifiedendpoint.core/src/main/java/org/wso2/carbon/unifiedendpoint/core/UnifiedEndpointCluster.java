/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.unifiedendpoint.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * This represent a group of unified endpoints and this can be used un LB and FO.
 */
public class UnifiedEndpointCluster {

    private String membershipHandler = "default-membership-handler";
    private final List<UnifiedEndpoint> clusteredUnifiedEndpointList = new ArrayList<UnifiedEndpoint>();
    private final List<String> clusteredEndpointUrlList = new ArrayList<String>();
    private Properties clusterProperties;

    /** Load Balancing */
    private boolean isLoadBalancing;
    private String loadBalancingPolicy;
    private String loadBalancingAlgorithm;

    /** FailOver*/
    private boolean isFailOver;


    public String getMembershipHandler() {
        return membershipHandler;
    }

    public void setMembershipHandler(String membershipHandler) {
        this.membershipHandler = membershipHandler;
    }

    public Properties getClusterProperties() {
        return clusterProperties;
    }

    public void setClusterProperties(Properties clusterProperties) {
        this.clusterProperties = clusterProperties;
    }

    public boolean isLoadBalancing() {
        return isLoadBalancing;
    }

    public void setLoadBalancing(boolean loadBalancing) {
        isLoadBalancing = loadBalancing;
    }

    public String getLoadBalancingPolicy() {
        return loadBalancingPolicy;
    }

    public void setLoadBalancingPolicy(String loadBalancingPolicy) {
        this.loadBalancingPolicy = loadBalancingPolicy;
    }

    public String getLoadBalancingAlgorithm() {
        return loadBalancingAlgorithm;
    }

    public void setLoadBalancingAlgorithm(String loadBalancingAlgorithm) {
        this.loadBalancingAlgorithm = loadBalancingAlgorithm;
    }

    public boolean isFailOver() {
        return isFailOver;
    }

    public void setFailOver(boolean failOver) {
        isFailOver = failOver;
    }

     public List<UnifiedEndpoint> getClusteredUnifiedEndpointList() {
        return clusteredUnifiedEndpointList;
    }

    public void addClusteredUnifiedEndpoint(UnifiedEndpoint uEp) {
        clusteredUnifiedEndpointList.add(uEp);
    }

    public List<String> getClusteredEndpointUrlList() {
        return clusteredEndpointUrlList;
    }

    public void addClusteredEndpointUrlList(String uepUrl) {
        clusteredEndpointUrlList.add(uepUrl);
    }
}
