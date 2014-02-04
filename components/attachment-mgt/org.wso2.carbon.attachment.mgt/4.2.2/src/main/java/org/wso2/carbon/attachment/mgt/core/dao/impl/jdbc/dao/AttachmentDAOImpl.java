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

package org.wso2.carbon.attachment.mgt.core.dao.impl.jdbc.dao;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.attachment.mgt.core.dao.AttachmentDAO;

import javax.activation.DataHandler;
import java.util.Date;

/**
 * JDBC based DAO impl for the Attachment
 */
public class AttachmentDAOImpl implements AttachmentDAO {
    /**
     * Class Logger
     */
    private static Log log = LogFactory.getLog(AttachmentDAOImpl.class);


    @Override
    public Long getID() {
        log.warn(new UnsupportedOperationException("Not impled yet."));
        return null;
    }

    @Override
    public void setId(Long id) {
        log.warn(new UnsupportedOperationException("Not impled yet."));
    }

    @Override
    public Date getCreatedTime() {
        log.warn(new UnsupportedOperationException("Not impled yet."));
        return null;
    }

    @Override
    public void setCreatedTime(Date createdTime) {
        log.warn(new UnsupportedOperationException("Not impled yet."));
    }

    @Override
    public String getName() {
        log.warn(new UnsupportedOperationException("Not impled yet."));
        return null;
    }

    @Override
    public void setName(String name) {
        log.warn(new UnsupportedOperationException("Not impled yet."));

    }

    @Override
    public String getCreatedBy() {
        log.warn(new UnsupportedOperationException("Not impled yet."));
        return null;
    }

    @Override
    public void setCreatedBy(String createdBy) {
        log.warn(new UnsupportedOperationException("Not impled yet."));
    }

    @Override
    public String getContentType() {
        log.warn(new UnsupportedOperationException("Not impled yet."));
        return null;
    }

    @Override
    public void setContentType(String contentType) {
        log.warn(new UnsupportedOperationException("Not impled yet."));
    }

    @Override
    public String getUrl() {
        log.warn(new UnsupportedOperationException("Not impled yet."));
        return null;
    }

    @Override
    public void setUrl(String url) {
        log.warn(new UnsupportedOperationException("Not impled yet."));
    }

    @Override
    public DataHandler getContent() {
        log.warn(new UnsupportedOperationException("Not impled yet."));
        return null;
    }

    @Override
    public void setContent(DataHandler content) {
        log.warn(new UnsupportedOperationException("Not impled yet."));
    }

}
