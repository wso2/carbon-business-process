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
import org.wso2.carbon.attachment.mgt.api.attachment.Attachment;
import org.wso2.carbon.attachment.mgt.core.dao.AttachmentDAO;
import org.wso2.carbon.attachment.mgt.core.dao.AttachmentMgtDAOFactory;
import org.wso2.carbon.attachment.mgt.core.exceptions.AttachmentMgtException;
import org.wso2.carbon.attachment.mgt.util.URLGeneratorUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.sql.*;

/**
 * JDBC specific implementation on {@link AttachmentMgtDAOFactory}
 */
public class AttachmentMgtDAOFactoryImpl implements AttachmentMgtDAOFactory {
    /**
     * Class Logger
     */
    private static Log log = LogFactory.getLog(AttachmentMgtDAOFactoryImpl.class);

    private Connection connection;
    private ResultSet resultSet;
    private PreparedStatement statement;

    public AttachmentMgtDAOFactoryImpl(Connection connection) {
        this.connection = connection;
    }

    @Override
    public AttachmentDAO addAttachment(Attachment attachment) throws AttachmentMgtException {
        //Following AttachmentDAO object creation is just to check it's functionality
        String name = "JDBC Sample Name";
        String owner = "JDBC Sample Author";
        String contentType = "JDBC Sample Content Type";
        int instanceID = 789;
        InputStream stream = null;
        try {
            stream = new FileInputStream(new File("/home/denis/Desktop/note.txt"));
        } catch (FileNotFoundException e) {
            log.error(e.getLocalizedMessage(), e);
        }

        String insertAttachment = "INSERT INTO ATTACHMENT (NAME, CREATED_BY, " +
                                  "CONTENT_TYPE, INSTANCE_ID, URL, CONTENT" +
                                  ") VALUES (?,?,?,?,?,?)";

        try {
            log.warn("Having connection at Class level is wrong here.");
            statement = connection.prepareStatement(insertAttachment);

            statement.setString(1, name);
            statement.setString(2, owner);
            statement.setString(3, contentType);
            //statement.setInt(4, attachment.getInstanceId().intValue());
            statement.setInt(4, instanceID);
            statement.setString(5, URLGeneratorUtil.generateURL());
            log.warn("URL generation part should be properly implemented.");

            statement.setBlob(6, stream);
            log.warn("Please check this cast can be avoid with good design patterns.");

            statement.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
        }

        return null;
    }

    @Override
    public AttachmentDAO getAttachmentInfo(String id) throws AttachmentMgtException {
        String errorMsg = "org.wso2.carbon.attachment.mgt.core.dao.impl.jdbc.dao" +
                          ".AttachmentMgtDAOFactoryImpl.getAttachmentInfo is still not implemented";
        log.error(errorMsg);
        throw new UnsupportedOperationException(errorMsg);
    }

    @Override
    public boolean removeAttachment(String id) throws AttachmentMgtException {
        String errorMsg = "org.wso2.carbon.attachment.mgt.core.dao.impl.jdbc.dao" +
                          ".AttachmentMgtDAOFactoryImpl.removeAttachment is still not implemented";
        log.error(errorMsg);
        throw new UnsupportedOperationException(errorMsg);
    }

    @Override
    public AttachmentDAO getAttachmentInfoFromURL(String attachmentURI) throws AttachmentMgtException {
         String errorMsg = "org.wso2.carbon.attachment.mgt.core.dao.impl.jdbc.dao.AttachmentMgtDAOFactoryImpl" +
                           ".getAttachmentInfoFromURL is still not implemented";
        log.error(errorMsg);
        throw new UnsupportedOperationException(errorMsg);
    }
}
