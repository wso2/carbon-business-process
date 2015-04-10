/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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


package org.wso2.carbon.humantask.core.dao.jpa.openjpa.model;

import org.apache.axis2.databinding.ADBException;
import org.apache.xmlbeans.XmlException;
import org.w3c.dom.Element;
import org.wso2.carbon.humantask.core.dao.LeanTaskDAO;
import org.wso2.carbon.humantask.core.dao.TaskPackageStatus;
<<<<<<< HEAD
=======
import org.wso2.carbon.humantask.core.dao.TaskStatus;
>>>>>>> 8fad7dee1bf193d56667148f2ca320ceb615d5d2
import org.wso2.carbon.humantask.core.utils.DOMUtils;

import javax.persistence.*;

@Entity
@Table(name = "HT_LEANTASK")
public class LeanTask implements LeanTaskDAO {
    @Column(name = "LEANTASK_TENANTID", nullable = false)
    private int tenantId;
    @Column(name = "LEANTASK_NAME", nullable = false)
    private String name;
    @Id
    @Column(name = "LEANTASK_VERSION", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE)
    private long version;
<<<<<<< HEAD
    @Column(name = "LEANTASK_ID")
    private String leanTaskId;
    @Column(name = "LEANTASK_DEF", nullable = false, columnDefinition = "CLOB")
    @Lob
    private String leanTaskDef;
    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", nullable = false)
    private TaskPackageStatus status;
    @Column(name = "md5sum", nullable = false)
    private String md5sum;
=======

    @Column(name="LEANTASK_ID")
    private String leanTaskId;

    @Column(name = "LEANTASK_DEF", nullable = false, columnDefinition = "CLOB")
    @Lob
    private String leanTaskDef;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", nullable = false)
    private TaskPackageStatus status;

    @Column(name = "md5sum", nullable = false)
    private String md5sum;

>>>>>>> 8fad7dee1bf193d56667148f2ca320ceb615d5d2

    public void setTenantID(int tenantId) {
        this.tenantId = tenantId;
    }

    public void setName(String name) {
<<<<<<< HEAD
        this.name = name;
        this.leanTaskId = name;
=======
        this.name=name;
        this.leanTaskId=name;
>>>>>>> 8fad7dee1bf193d56667148f2ca320ceb615d5d2
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public void setleanTaskId(String id) {
        this.leanTaskId = name;
    }

    public void setleanTaskId(String id){
        this.leanTaskId=name;
    }

    public void setLeanTask(Element leanTask) {
        this.leanTaskDef = DOMUtils.domToString(leanTask);
    }

    public void setTaskStatus(TaskPackageStatus status) {
        this.status = status;
    }

    public void setmd5sum(String md5sum) {
        this.md5sum = md5sum;
    }

    public TaskPackageStatus getStatus() {
        return status;
    }

    public void setTaskStatus(TaskPackageStatus status) {
        this.status=status;
    }

    public void setmd5sum(String md5sum) {
        this.md5sum=md5sum;
    }

    public TaskPackageStatus getStatus() {
        return status;
    }

    public int getTenantID() {
        return tenantId;
    }

    public String getName() {
        return name;
    }

    public long getVersion() {
        return version;
    }

    public org.wso2.carbon.humantask.TLeanTask getLeanTask() throws ADBException, XmlException {
        org.wso2.carbon.humantask.TLeanTask xmlbLeantask = org.wso2.carbon.humantask.TLeanTask.Factory.newInstance();
        xmlbLeantask = org.wso2.carbon.humantask.TLeanTask.Factory.parse(leanTaskDef);
        return xmlbLeantask;
    }

    public String getleanTaskId() {
        return leanTaskId;
    }

    public String getmd5sum() {
        return md5sum;
    }

    public String getleanTaskId(){return leanTaskId;}

    public String getmd5sum() {
        return md5sum;
    }
}
