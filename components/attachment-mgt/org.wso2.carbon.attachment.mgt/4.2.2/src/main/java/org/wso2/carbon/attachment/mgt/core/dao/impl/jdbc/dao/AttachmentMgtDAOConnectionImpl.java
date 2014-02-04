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

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.attachment.mgt.api.attachment.Attachment;
import org.wso2.carbon.attachment.mgt.core.AttachmentImpl;
import org.wso2.carbon.attachment.mgt.core.dao.AttachmentMgtDAOConnection;
import org.wso2.carbon.attachment.mgt.core.dao.AttachmentMgtDAOFactory;
import org.wso2.carbon.attachment.mgt.server.internal.AttachmentServerHolder;
import org.wso2.carbon.attachment.mgt.util.URLGeneratorUtil;

import javax.swing.plaf.basic.BasicDirectoryModel;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * JDBC specific implementation on {@link AttachmentMgtDAOConnection }
 */
public class AttachmentMgtDAOConnectionImpl implements AttachmentMgtDAOConnection {
    /**
     * Class Logger
     */
    private static Log log = LogFactory.getLog(AttachmentDAOImpl.class);

    protected AttachmentMgtDAOFactory daoFactory;

    public AttachmentMgtDAOConnectionImpl() {
        try {
            Connection connection = AttachmentServerHolder.getInstance().getAttachmentServer()
                    .getDataSourceManager()
                    .getDataSource().getConnection();

            this.daoFactory = new AttachmentMgtDAOFactoryImpl(connection);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
        }
    }

    @Override
    public AttachmentMgtDAOFactory getAttachmentMgtDAOFactory() {
        log.warn("Still not implemented.");
        if (this.daoFactory != null) {
            return this.daoFactory;
        } else {
            log.error("AttachmentMgtDAOFactory is not initialized yet.");
            throw new NullPointerException("AttachmentMgtDAOFactory is not initialized yet.");
        }
    }
}
