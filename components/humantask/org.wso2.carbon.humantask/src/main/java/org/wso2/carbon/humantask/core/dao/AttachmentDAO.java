/*
 * Copyright (c) 2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.humantask.core.dao;

import java.util.Date;

/**
 * DAO representation of the task attachment,
 */
public interface AttachmentDAO {
    /**
     * @return unique id of the attachment
     */
    Long getId();

    /**
     * @param id unique id of the attachment
     */
    void setId(Long id);

    /**
     * @return name of the attachment
     */
    String getName();

    /**
     * @param name name of the attachment
     */
    void setName(String name);

    /**
     * @return media type of the attachment
     */
    String getContentType();

    /**
     * @param contentType media type of the attachment
     */
    void setContentType(String contentType);

    String getAccessType();

    void setAccessType(String accessType);

    /**
     * @return the date, when the attachment is uploaded to the system.
     */
    Date getAttachedAt();

    /**
     * @param attachedAt the date, when the attachment is uploaded to the system.
     */
    void setAttachedAt(Date attachedAt);

    /**
     * @return organizational entity who uploaded the attachment
     */
    OrganizationalEntityDAO getAttachedBy();

    /**
     * @param attachedBy organizational entity who uploaded the attachment
     */
    void setAttachedBy(OrganizationalEntityDAO attachedBy);

    /**
     * @return value representation of the attachment
     */
    String getValue();

    /**
     * @param value representation of the attachment
     */
    void setValue(String value);

    /**
     * @return the associated task for the attachment
     */
    TaskDAO getTask();

    /**
     * @param task Associated task for the attachment
     */
    void setTask(TaskDAO task);
}
