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

package org.wso2.carbon.humantask.core.dao.jpa;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.humantask.core.dao.DatabaseType;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import javax.transaction.TransactionManager;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;

/**
 * A container for vendor specific JPA properties.
 */
public class AbstractJPAVendorAdapter implements JPAVendorAdapter {
    protected final Log log = LogFactory.getLog(AbstractJPAVendorAdapter.class);

    private DataSource dataSource;

    private boolean generateDDL;

    private boolean showSQL;

    @Override
    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    @Override
    public void setGenerateDdl(boolean generateDDl) {
        this.generateDDL = generateDDl;
    }

    public boolean isShowSQL() {
        return showSQL;
    }

    public boolean isGenerateDDL() {
        return generateDDL;
    }

    @Override
    public void setShowSql(boolean showSQL) {
        this.showSQL = showSQL;
    }

    @Override
    public Map<String, ?> getJpaPropertyMap(TransactionManager tnxManager) {
        return Collections.emptyMap();
    }

    @Override
    public Class<? extends EntityManagerFactory> getEntityManagerFactoryInterface() {
        return EntityManagerFactory.class;
    }

    @Override
    public Class<? extends EntityManager> getEntityManagerInterface() {
        return EntityManager.class;
    }

    protected Connection getDBConnection() throws SQLException {
        Connection c = dataSource.getConnection();
        c.setTransactionIsolation(2);
        return c;
    }

    protected void close(Connection con) {
        if (con != null) {
            try {
                con.close();
            } catch (Exception e) {
                log.warn("Exception while closing connection", e);
            }
        }
    }

    protected DatabaseType determineDbType() {
        Connection con = null;
        DatabaseType dbType = null;
        try {
            con = getDBConnection();
            DatabaseMetaData metaData = con.getMetaData();
            if (metaData != null) {
                String dbProductName = metaData.getDatabaseProductName().toLowerCase();
                int dbMajorVer = metaData.getDatabaseMajorVersion();
                if (log.isDebugEnabled()) {
                    log.debug("Using database " + dbProductName + " major version " + dbMajorVer);
                }
                if (dbProductName.contains("db2")) {
                    dbType = DatabaseType.DB2;
                } else if (dbProductName.contains("oracle")) {
                    dbType = DatabaseType.ORACLE;
                } else if (dbProductName.contains("derby")) {
                    dbType = DatabaseType.DERBY;
                } else if (dbProductName.contains("h2")) {
                    dbType = DatabaseType.H2;
                } else if (dbProductName.contains("hsql")) {
                    dbType = DatabaseType.HSQL;
                } else if (dbProductName.contains("microsoft sql")) {
                    dbType = DatabaseType.SQL_SERVER;
                } else if (dbProductName.contains("mysql")) {
                    dbType = DatabaseType.MYSQL;
                } else if (dbProductName.contains("postgresql")) {
                    dbType = DatabaseType.POSTGRESQL;
                } else if (dbProductName.contains("sybase")) {
                    dbType = DatabaseType.SYBASE;
                }
            }
        } catch (SQLException e) {
            log.warn("Unable to determine database dialect.", e);
        } finally {
            close(con);
        }
        return dbType;
    }
}
