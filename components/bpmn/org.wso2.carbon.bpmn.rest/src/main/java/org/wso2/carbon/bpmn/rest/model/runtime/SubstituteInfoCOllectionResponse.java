/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
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
package org.wso2.carbon.bpmn.rest.model.runtime;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;


@XmlRootElement(name = "Substitutes")
@XmlAccessorType(XmlAccessType.FIELD)
public class SubstituteInfoCollectionResponse {

    @XmlElement(name = "SubstituteInfo", type = SubstituteInfoResponse.class)
    private List<SubstituteInfoResponse> substituteInfoList;
    private int start;
    private int size;
    private String sort;
    private String order;

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public String getSort() {
        return sort;
    }

    public void setSort(String sort) {
        this.sort = sort;
    }

    public String getOrder() {
        return order;
    }

    public void setOrder(String order) {
        this.order = order;
    }

    public SubstituteInfoCollectionResponse(){}

    public List<SubstituteInfoResponse> getSubstituteInfoList() {
        return substituteInfoList;
    }

    public void setSubstituteInfoList(List<SubstituteInfoResponse> substituteInfoList) {
        this.substituteInfoList = substituteInfoList;
    }

}
