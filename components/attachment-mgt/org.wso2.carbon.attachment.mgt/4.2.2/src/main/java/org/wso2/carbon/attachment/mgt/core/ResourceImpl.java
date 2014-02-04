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

package org.wso2.carbon.attachment.mgt.core;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.attachment.mgt.api.Resource;

import javax.activation.DataHandler;
import java.io.InputStream;
import java.net.URL;

/**
 * Initial implementation for the org.wso2.carbon.attachment.mgt.api.Resource
 */
public class ResourceImpl implements Resource {
    /**
     * Logger class
     */
    private static Log log = LogFactory.getLog(ResourceImpl.class);

    /**
     * unique id for a resource respected to a given data source
     */
    protected String id;

    /**
     * Name of the resource
     */
    protected String name;

    /**
     * Birth-time of the resource
     */
    protected long createdTime;

    /**
     * Owner of the resource
     */
    protected String author;

    /**
     * eg - text/plain. This is used by the client to render the resource
     */
    protected String contentType;

    /**
     * Actual representation of the resource
     */
    protected DataHandler content;

    /**
     * Unique link to the resource
     */
    protected URL url;

    /**
     * Disabled empty constructor
     */
    private ResourceImpl() {
    }

    public ResourceImpl(String id, String name, String author, String contentType,
                        DataHandler content) {
        this(name, author, contentType, content);
        this.id = id;
    }

    public ResourceImpl(String name, String author, String contentType, DataHandler content) {
        this.name = name;
        this.author = author;
        this.contentType = contentType;
        this.content = content;
    }

    protected ResourceImpl(String id, String name, String author, String contentType, DataHandler content, URL uri) {
        this(name, author, contentType, content);
        this.id = id;
        this.url = uri;
    }

    /**
     * {@inheritDoc}
     */
    public String getId() {
        return this.id;
    }

    /**
     * {@inheritDoc}
     */
    public String getName() {
        return this.name;
    }

    /**
     * {@inheritDoc}
     */
    public long getCreatedTime() {
        return this.createdTime;
    }

    /**
     * {@inheritDoc}
     */
    public String getCreatedBy() {
        return this.author;
    }


    /**
     * {@inheritDoc}
     */
    public String getContentType() {
        return this.contentType;
    }


    /**
     * {@inheritDoc}
     */
    public DataHandler getContent() {
        return this.content;
    }

    @Override
    public URL getURL() {
        return this.url;
    }

}
