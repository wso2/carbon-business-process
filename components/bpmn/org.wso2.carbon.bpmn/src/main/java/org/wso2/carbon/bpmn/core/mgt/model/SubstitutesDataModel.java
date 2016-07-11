/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.bpmn.core.mgt.model;

import java.util.Date;

public class SubstitutesDataModel {

    private String user;
    private String substitute;
    private Date substitutionStart;
    private Date substitutionEnd;
    private boolean enabled;
    private String transitiveSub;
    private Date created;
    private Date updated;
    private int tenantId;
    private String taskList;

    public String getTaskList() {
        return taskList;
    }

    public void setTaskList(String taskList) {
        this.taskList = taskList;
    }

    public int getTenantId() {
        return tenantId;
    }

    public void setTenantId(int tenantId) {
        this.tenantId = tenantId;
    }

    public Date getUpdated() {
        return updated;
    }

    public void setUpdated(Date updated) {
        this.updated = updated;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public String getTransitiveSub() {
        return transitiveSub;
    }

    public void setTransitiveSub(String transitiveSub) {
        this.transitiveSub = transitiveSub;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getSubstitute() {
        return substitute;
    }

    public void setSubstitute(String substitute) {
        this.substitute = substitute;
    }

    public Date getSubstitutionStart() {
        return substitutionStart;
    }

    public void setSubstitutionStart(Date substitutionStart) {
        this.substitutionStart = substitutionStart;
    }

    public Date getSubstitutionEnd() {
        return substitutionEnd;
    }

    public void setSubstitutionEnd(Date substitutionEnd) {
        this.substitutionEnd = substitutionEnd;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String toString() {

        return " User=" + user + " \n" +
                " Substitute= " + substitute +
                " SubstitutionStart= " + substitutionStart +
                "SubstitutionEnd=" + substitutionEnd +
                "TenantID=" + tenantId +
                "Enabled=" + enabled;
    }
}
