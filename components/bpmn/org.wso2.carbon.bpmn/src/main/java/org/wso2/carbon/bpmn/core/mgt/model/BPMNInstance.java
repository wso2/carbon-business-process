///**
// *  Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
//import java.util.Date;
//
///**
// *
// */
//public class BPMNInstance {
//
//    private String instanceId;
//    private String processId;
//    private String processName;
//    private BPMNVariable[] variables;
//    private boolean suspended;
//    private Date startTime;
//    private Date endTime;
//
//    public String getInstanceId() {
//        return instanceId;
//    }
//
//    public void setInstanceId(String instanceId) {
//        this.instanceId = instanceId;
//    }
//
//    public String getProcessId() {
//        return processId;
//    }
//
//    public void setProcessId(String processId) {
//        this.processId = processId;
//    }
//
//    public String getProcessName() {
//        return processName;
//    }
//
//    public void setProcessName(String processName) {
//        this.processName = processName;
//    }
//
//    public BPMNVariable[] getVariables() {
//        return variables == null ? null : (BPMNVariable[]) this.variables.clone();
//    }
//
//    public void setVariables(BPMNVariable[] variables) {
//        this.variables = variables == null ? null : (BPMNVariable[]) variables.clone();
//    }
//
//    public boolean isSuspended() {
//        return suspended;
//    }
//
//    public void setSuspended(boolean suspended) {
//        this.suspended = suspended;
//    }
//
//    public Date getStartTime() {
//        return startTime == null ? null : (Date) this.startTime.clone();
//    }
//
//    public void setStartTime(Date startTime) {
//        this.startTime = startTime == null ? null : (Date) startTime.clone();
//    }
//
//    public Date getEndTime() {
//        return endTime == null ? null : (Date) this.endTime.clone();
//    }
//
//    public void setEndTime(Date endTime) {
//        this.endTime = endTime == null ? null : (Date) endTime.clone();
//    }
//}
