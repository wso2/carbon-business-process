///**
// *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
// *
// *  Licensed under the Apache License, Version 2.0 (the "License");
// *  you may not use this file except in compliance with the License.
// *  You may obtain a copy of the License at
// *
// *        http://www.apache.org/licenses/LICENSE-2.0
// *
// *  Unless required by applicable law or agreed to in writing, software
// *  distributed under the License is distributed on an "AS IS" BASIS,
// *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// *  See the License for the specific language governing permissions and
// *  limitations under the License.
// */
//
//package org.wso2.carbon.bpmn.core.mgt.model;
//
//import org.activiti.engine.history.HistoricProcessInstance;
//
//import java.util.ArrayList;
//import java.util.List;
//
///**
// *
// */
//public class BPMNDeletableInstances {
//
//    private int activeInstanceCount;
//    private int completedInstanceCount;
//    private Integer tenantId;
//
//    private List<HistoricProcessInstance> activeHistoricProcessInstance = null;
//    private List<String> completedProcessDefinitionIds = null;
//
//    public BPMNDeletableInstances() {
//        activeHistoricProcessInstance = new ArrayList<>();
//        completedProcessDefinitionIds = new ArrayList<>();
//    }
//
//    public Integer getTenantId() {
//        return tenantId;
//    }
//
//    public void setTenantId(Integer tenantId) {
//        this.tenantId = tenantId;
//    }
//
//    public int getActiveInstanceCount() {
//        return activeInstanceCount;
//    }
//
//    public void setActiveInstanceCount(int activeInstanceCount) {
//        this.activeInstanceCount += activeInstanceCount;
//    }
//
//    public int getCompletedInstanceCount() {
//        return completedInstanceCount;
//    }
//
//    public void setCompletedInstanceCount(int completedInstanceCount) {
//        this.completedInstanceCount += completedInstanceCount;
//    }
//
//    public List<HistoricProcessInstance> getActiveHistoricProcessInstance() {
//        return activeHistoricProcessInstance;
//    }
//
//    public void setActiveProcessInstance(
//            List<HistoricProcessInstance> activeHistoricProcessInstance) {
//
//        if (activeHistoricProcessInstance != null && activeHistoricProcessInstance.size() > 0) {
//            this.activeHistoricProcessInstance.addAll(activeHistoricProcessInstance);
//        }
//    }
//
//    public List<String> getCompletedProcessDefinitionIds() {
//        return completedProcessDefinitionIds;
//    }
//
//    public void addCompletedProcessDefinitionIds(String completedProcessDefinitionId) {
//        this.completedProcessDefinitionIds.add(completedProcessDefinitionId);
//    }
//}
