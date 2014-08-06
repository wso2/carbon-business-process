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

package org.wso2.carbon.attachment.mgt.api;

import javax.activation.DataHandler;
import java.net.URI;
import java.net.URL;
import java.util.Date;

/**
 * Basic interface for any resource to be handled by the Attachment-Mgt
 */
public interface Resource {
    /**
     * The Resource ID, In the default implementation this returns the path.
     *
     * @return the resource id
     */
    public String getId();

    /**
     * While instantiating a Resource, user can specify a name.
     * This method returns the user specified name
     *
     * @return the resource name
     */
    public String getName();

    /**
     * Method to get the created time.
     *
     * @return the created time
     */
    public long getCreatedTime();

    /**
     * Get the user name of the resource author.
     *
     * @return the user name of the resource author.
     */
    public String getCreatedBy();

    /**
     * Get content type.
     *
     * @return the content type.
     */
    public String getContentType();

    /**
     * Method to get the {@code DataHandler} for the content of the resource.
     *
     * @return the {@code DataHandler} for the content.
     */
    public DataHandler getContent();

    /**
     * Returns the URI for the resource
     * @return the URI for the resource
     */
    public URL getURL();
}
