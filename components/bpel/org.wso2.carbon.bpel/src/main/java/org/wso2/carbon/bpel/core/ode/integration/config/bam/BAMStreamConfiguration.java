/*
 * Copyright (c) 2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.bpel.core.ode.integration.config.bam;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Holds BAM stream configuration data
 */
public class BAMStreamConfiguration {
    private String name;
    private String nickName;
    private String description;
    private String version;
    private List<BAMKey> payloadBAMKeyList = new ArrayList<BAMKey>();
    private List<BAMKey> correlationBAMKeyList = new ArrayList<BAMKey>();
    private List<BAMKey> metaBAMKeyList = new ArrayList<BAMKey>();

    public BAMStreamConfiguration(String name, String nickName, String description, String version) {
        this.name = name;
        this.nickName = nickName;
        this.description = description;
        this.version = version;
    }

    public String getName() {
        return name;
    }

    public String getNickName() {
        return nickName;
    }

    public String getDescription() {
        return description;
    }

    public String getVersion() {
        return version;
    }

    public List<BAMKey> getMetaBAMKeyList() {
        return metaBAMKeyList;
    }

    public void addMetaBAMKey(BAMKey BAMKey) {
        metaBAMKeyList.add(BAMKey);
    }

    public void addAllMetaBAMKeys(Collection<BAMKey> BAMKeys) {
        metaBAMKeyList.addAll(BAMKeys);
    }

    public List<BAMKey> getCorrelationBAMKeyList() {
        return correlationBAMKeyList;
    }

    public void addCorrelationBAMKey(BAMKey BAMKey) {
        correlationBAMKeyList.add(BAMKey);
    }

    public void addAllCorrelationBAMKeys(Collection<BAMKey> BAMKeys) {
        correlationBAMKeyList.addAll(BAMKeys);
    }

    public List<BAMKey> getPayloadBAMKeyList() {
        return payloadBAMKeyList;
    }

    public void addPayloadBAMKey(BAMKey BAMKey) {
        payloadBAMKeyList.add(BAMKey);
    }

    public void addAllPayloadBAMKeys(Collection<BAMKey> BAMKeys) {
        payloadBAMKeyList.addAll(BAMKeys);
    }
}
