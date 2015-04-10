/*
* Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.humantask.core.dao;

import org.apache.axis2.databinding.ADBException;
import org.apache.xmlbeans.XmlException;
import org.w3c.dom.Element;
import org.wso2.carbon.humantask.TLeanTask;

/**
 * DAO class for lean task definition
 */
public interface LeanTaskDAO {
    /**
     * set tenant id of the lean task
     *
     * @param tenantId
     */
    void setTenantID(int tenantId);

    /**
     * set laen task definition name
     *
     * @param name
     */
    void setName(String name);

    /**
     * set version
     *
     * @param version
     */
    void setVersion(long version);

<<<<<<< HEAD
    void setleanTaskId(String id);
=======

   void setleanTaskId(String id);

>>>>>>> 8fad7dee1bf193d56667148f2ca320ceb615d5d2

    /**
     * set lean task definition
     *
     * @param leanTask
     */
    void setLeanTask(Element leanTask);

    /**
     * set task status
<<<<<<< HEAD
     *
=======
>>>>>>> 8fad7dee1bf193d56667148f2ca320ceb615d5d2
     * @param status
     */
    void setTaskStatus(TaskPackageStatus status);

    /**
     * set md5sum
<<<<<<< HEAD
     *
=======
>>>>>>> 8fad7dee1bf193d56667148f2ca320ceb615d5d2
     * @param md5sum
     */
    void setmd5sum(String md5sum);

    /**
     * get task status
<<<<<<< HEAD
     *
=======
>>>>>>> 8fad7dee1bf193d56667148f2ca320ceb615d5d2
     * @return
     */
    TaskPackageStatus getStatus();

    /**
     * get tenant id
     *
     * @return
     */
    int getTenantID();

    /**
     * get lean task name
     *
     * @return
     */
    String getName();

    /**
     * get version
     *
     * @return
     */
    long getVersion();

    /**
     * get lean task definition
     *
     * @return
     */
    TLeanTask getLeanTask() throws ADBException, XmlException;

<<<<<<< HEAD
    /**
     *
     * @return
     */
    String getleanTaskId();
=======
   String getleanTaskId();

    String getmd5sum();
>>>>>>> 8fad7dee1bf193d56667148f2ca320ceb615d5d2

    /**
     *
     * @return
     */
    String getmd5sum();
}
