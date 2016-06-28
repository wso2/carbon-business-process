/*
 * Copyright (c) 2005-2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.bpel.core.ode.integration.config.analytics;

import org.apache.commons.collections.map.MultiKeyMap;

/**
 * Holds the Analytics Server profile information
 */
public class AnalyticsServerProfile {

    // AnalyticsServerProfile name.
    private String name;

    // The Agent name from which the DataPublisher that needs to be created. By default Thrift, and Binary is supported.
    private String type;

    // Authorized username at receiver.
    private String userName;

    // The password of the username provided.
    private String password;

    // The receiving endpoint URL Set. This can be either load balancing URL set or Failover URL set.
    private String receiverURLSet;

    // The authenticating URL Set for the endpoints given in receiverURLSet parameter. This should be in the same format
    // as receiverURL set parameter. If null is passed the authURLs will be offsetted by value of 100.
    private String authURLSet;

    // Stream configuration.
    private MultiKeyMap streamConfigurations = new MultiKeyMap();

    /**
     * Get Analytics Server Profile name
     *
     * @return Analytics Server Profile name.
     */
    public String getName() {
        return name;
    }

    /**
     * Set Analytics Server Profile name
     *
     * @param name Analytics Server Profile name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get Analytics Agent name
     *
     * @return Agent name
     */
    public String getType() {
        return type;
    }

    /**
     * Set Analytics Agent name
     *
     * @param type Analytics Agent name
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Get Authorized username at receiver
     *
     * @return username
     */
    public String getUserName() {
        return userName;
    }

    /**
     * Set Authorized username at receiver
     *
     * @param userName Username as a string value
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     * Get The password of the username provided.
     *
     * @return password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Set The password of the username provided.
     *
     * @param password password as a string
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * @param streamName
     * @param version
     * @return AnalyticsStreamConfiguration
     */
    public AnalyticsStreamConfiguration getAnalyticsStreamConfiguration(String streamName, String version) {
        return (AnalyticsStreamConfiguration) streamConfigurations.get(streamName, version);
    }

    /**
     * Get The receiving endpoint URL Set. This can be either load balancing URL set or Failover URL set.
     * eg : tcp://localhost:7611|tcp://localhost:7612|tcp://localhost:7613
     *
     * @return The receiving endpoint URL Set.
     */
    public String getReceiverURLSet() {
        return receiverURLSet;
    }

    /**
     * Set The receiving endpoint URL Set.
     *
     * @param receiverURLSet The receiving endpoint URL Set
     */
    public void setReceiverURLSet(String receiverURLSet) {
        this.receiverURLSet = receiverURLSet;
    }

    /**
     * Get The authenticating URL Set for the endpoints given in receiverURLSet parameter.
     *
     * @return The authenticating URL Set
     */
    public String getAuthURLSet() {
        return authURLSet;
    }

    /**
     * Set The authenticating URL Set for the endpoints given in receiverURLSet parameter.
     *
     * @param authURLSet The authenticating URL Set
     */
    public void setAuthURLSet(String authURLSet) {
        this.authURLSet = authURLSet;
    }

    public void addAnalyticsStreamConfiguration(String streamName, String version,
                                                AnalyticsStreamConfiguration streamConfiguration) {
        streamConfigurations.put(streamName, version, streamConfiguration);
    }

    @Override
    public String toString() {
        return getName() + ", type of " + getType();
    }
}
