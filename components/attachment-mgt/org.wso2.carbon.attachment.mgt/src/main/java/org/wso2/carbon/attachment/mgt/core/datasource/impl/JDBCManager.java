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

package org.wso2.carbon.attachment.mgt.core.datasource.impl;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.attachment.mgt.configuration.AttachmentServerConfiguration;
import org.wso2.carbon.attachment.mgt.core.datasource.AbstractDataSourceManager;
import org.wso2.carbon.attachment.mgt.core.exceptions.AttachmentMgtException;
import org.wso2.carbon.utils.FileUtil;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.sql.SQLException;

/**
 * Manages a JDBC connection
 */
public class JDBCManager extends AbstractDataSourceManager {
    /**
     * Class Logger
     */
    private static Log log = LogFactory.getLog(JDBCManager.class);

    protected JDBCConfiguration jdbcConfiguration;

    protected volatile BasicDataSource dataSource = null;

    public BasicDataSource getDataSource() {
        return dataSource;
    }

    @Override
    public void init(AttachmentServerConfiguration serverConfig) throws AttachmentMgtException {
        log.warn("Nothing happens at org.wso2.carbon.attachment.mgt.core.datasource.impl.JDBCManager.init");
    }

    @Override
    public void start() throws AttachmentMgtException {
        log.warn("Nothing happens at org.wso2.carbon.attachment.mgt.core.datasource.impl" +
                 ".JDBCManager.start");
    }

    @Override
    public void shutdown() throws AttachmentMgtException {
        try {
            dataSource.close();
            dataSource = null;
        } catch (SQLException e) {
            throw new AttachmentMgtException(e.getLocalizedMessage(), e);
        }
    }

    public void initFromFileConfig(String dbConfigurationPath) throws AttachmentMgtException {
        String jdbcURL;
        String driver;
        String username;
        String password;
        String validationQuery;
        if (dataSource == null) {
            synchronized (JDBCManager.class) {
                if (dataSource == null) {
                    JDBCConfiguration configuration = getDBConfig(dbConfigurationPath);
                    jdbcURL = configuration.getJdbcURL();
                    driver = configuration.getDriver();
                    username = configuration.getUsername();
                    password = configuration.getPassword();
                    validationQuery = configuration.getValidationQuery();
                    if (jdbcURL == null || driver == null || username == null || password == null) {
                        throw new AttachmentMgtException("DB configurations are not properly defined");
                    }
                    dataSource = new BasicDataSource();
                    dataSource.setDriverClassName(driver);
                    dataSource.setUrl(jdbcURL);
                    dataSource.setUsername(username);
                    dataSource.setPassword(password);
                    dataSource.setValidationQuery(validationQuery);
                }
            }
        }
    }

    private static JDBCConfiguration getDBConfig(String configPath) throws AttachmentMgtException {
        String config = null;
        try {
            config = FileUtil.readFileToString(configPath);
            OMElement omElement = AXIOMUtil.stringToOM(config);

            String dbURL = omElement.getFirstChildWithName(new QName("url")).getText();
            String driverName = omElement.getFirstChildWithName(new QName("driverName")).getText();
            String userName = omElement.getFirstChildWithName(new QName("userName")).getText();
            String password = omElement.getFirstChildWithName(new QName("password")).getText();
            String validationQuery = omElement.getFirstChildWithName(new QName("validationQuery")).getText();
            JDBCConfiguration dbConfiguration = new JDBCConfiguration(dbURL, driverName,
                                                                      userName, password, validationQuery);

            return dbConfiguration;
        } catch (IOException e) {
            throw new AttachmentMgtException(e.getLocalizedMessage(), e);
        } catch (XMLStreamException e) {
            throw new AttachmentMgtException(e.getLocalizedMessage(), e);
        }


    }
}
