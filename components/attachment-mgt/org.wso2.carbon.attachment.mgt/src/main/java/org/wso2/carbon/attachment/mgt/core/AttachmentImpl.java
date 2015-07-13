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

import org.wso2.carbon.attachment.mgt.api.attachment.Attachment;
import org.wso2.carbon.attachment.mgt.core.dao.AttachmentDAO;
import org.wso2.carbon.attachment.mgt.core.exceptions.AttachmentMgtException;
import org.wso2.carbon.attachment.mgt.util.URLGeneratorUtil;

import javax.activation.DataHandler;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;

/**
 * Initial implementation for org.wso2.carbon.attachment.mgt.api.attachment.Attachment
 */
public class AttachmentImpl extends ResourceImpl implements Attachment {

    /**
     * Disabled the empty constructor.
     */
    private AttachmentImpl() {
        super(null, null, null, null);
    }

    /**
     * Create an attachment with given details
     *
     * @param name
     * @param author
     * @param contentType
     * @param content
     */
    public AttachmentImpl(String name, String author, String contentType, DataHandler content) {
        super(name, author, contentType, content);
    }

    /**
     * Create an attachment with given details
     *
     * @param name
     * @param author
     * @param contentType
     * @param content
     * @param createdTime
     */

    public AttachmentImpl(String name, String author, String contentType, DataHandler content, Date createdTime) {
        super(name, author, contentType, content, createdTime);
    }

    /**
     * Create an attachment with given details
     *
     * @param id
     * @param name
     * @param author
     * @param contentType
     * @param content
     */
    private AttachmentImpl(String id, String name, String author, String contentType, DataHandler content, URL uri) {
        super(id, name, author, contentType, content, uri);
    }

    /**
     * Create an attachment with given details
     *
     * @param id
     * @param name
     * @param author
     * @param contentType
     * @param content
     * @param uri
     * @param createdTime
     */
    private AttachmentImpl(String id, String name, String author, String contentType, DataHandler content, URL uri, Date createdTime) {
        super(id, name, author, contentType, content, uri, createdTime);
    }

    /**
     * Create an attachment with given details
     *
     * @param attachmentDAO
     * @throws URISyntaxException
     * @throws AttachmentMgtException
     */
    public AttachmentImpl(AttachmentDAO attachmentDAO) throws URISyntaxException, AttachmentMgtException {
        this(String.valueOf(attachmentDAO.getID()), attachmentDAO.getName(), attachmentDAO.getCreatedBy(),
             attachmentDAO.getContentType(), attachmentDAO.getContent(), URLGeneratorUtil.getPermanentLink(new URI(attachmentDAO.getUrl())), attachmentDAO.getCreatedTime());
    }
}
