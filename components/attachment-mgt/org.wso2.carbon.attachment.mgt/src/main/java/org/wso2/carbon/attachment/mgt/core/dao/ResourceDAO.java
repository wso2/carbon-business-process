/*
 * Copyright (c) 2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.attachment.mgt.core.dao;

import org.wso2.carbon.attachment.mgt.core.exceptions.AttachmentMgtException;

import javax.activation.DataHandler;
import java.util.Date;

/**
 * DAO interface for the {@link org.wso2.carbon.attachment.mgt.api.Resource}
 */
public interface ResourceDAO {
    /**
     * Returns the id of a resource
     */
    public Long getID();

    /**
     * Sets the id of a resource
     */
    public void setId(Long id);

    /**
     * Returns the created-time of a resource
     */
    public Date getCreatedTime();

    /**
     * Sets the created-time of a resource
     */
    public void setCreatedTime(Date createdTime);

    /**
     * Returns the name of a resource
     */
    public String getName();

    /**
     * Sets the name of a resource
     */
    public void setName(String name);

    /**
     * Returns the owner of a resource
     */
    public String getCreatedBy();

    /**
     * Sets the owner of a resource
     */
    public void setCreatedBy(String createdBy);

    /**
     * Returns the content-type of a resource
     */
    public String getContentType();

    /**
     * Sets the content-type of a resource
     */
    public void setContentType(String contentType);

    /**
     * Returns the url of a resource
     */
    public String getUrl();

    /**
     * Sets the url of a resource
     */
    public void setUrl(String url);

    /**
     * Returns the {@code DataHandler} for the resource
     */
    public DataHandler getContent();

    /**
     * Sets the Content of a resource using {@code DataHandler}
     */
    public void setContent(DataHandler contentHandler) throws AttachmentMgtException;

}
