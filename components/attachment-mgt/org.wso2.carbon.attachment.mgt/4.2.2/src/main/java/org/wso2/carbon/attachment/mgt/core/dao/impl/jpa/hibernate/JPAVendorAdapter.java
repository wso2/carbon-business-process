/*
 *
 *   Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 * /
 */
package org.wso2.carbon.attachment.mgt.core.dao.impl.jpa.hibernate;

import org.wso2.carbon.attachment.mgt.core.dao.impl.jpa.AbstractJPAVendorAdapter;
import org.wso2.carbon.attachment.mgt.core.dao.impl.jpa.DatabaseType;

import javax.transaction.TransactionManager;
import java.util.HashMap;
import java.util.Map;

public class JPAVendorAdapter extends AbstractJPAVendorAdapter {
    @Override
    public Map<String, ?> getJpaPropertyMap(TransactionManager transactionManager) {
        //TODO: Inorder to support external transaction manager, need to fix these property map
        // properly.
        Map<String, Object> jpaProperties = new HashMap<String, Object>();

        if (getDataSource() != null) {
            jpaProperties.put("hibernate.connection.datasource", getDataSource());
            String dialect = determineDbDialect();
            if (dialect != null) {
                log.info("[Attachment-Mgt Hibernate] DB Dialect: " + dialect);
                jpaProperties.put("hibernate.dialect", dialect);
            }
        }

        if (isGenerateDDL()) {
            log.info("[Attachment-Mgt Hibernate] Generate DDL Enabled.");
            jpaProperties.put("hibernate.hbm2ddl.auto", "update");
        }

        if (isShowSQL()) {
            log.info("[Attachment-Mgt Hibernate] Show SQL enabled.");
            jpaProperties.put("hibernate.show_sql", "true");
        }

        jpaProperties.put("hibernate.cache.provider_class", "org.hibernate.cache.NoCacheProvider");
        jpaProperties.put("hibernate.cache.use_query_cache", "false");
        jpaProperties.put("hibernate.cache.use_second_level_cache", "false");

        return jpaProperties;
    }

    protected String determineDbDialect() {
        DatabaseType dbType = determineDbType();
        switch (dbType) {
            case DB2:
                return "org.hibernate.dialect.DB2Dialect";
            case DERBY:
                return "org.hibernate.dialect.DerbyDialect";
            case H2:
                return "org.hibernate.dialect.H2Dialect";
            case HSQL:
                return "org.hibernate.dialect.HSQLDialect";
            case INFORMIX:
                return "org.hibernate.dialect.InformixDialect";
            case MYSQL:
                return "org.hibernate.dialect.MySQLDialect";
            case ORACLE:
                return "org.hibernate.dialect.OracleDialect";
            case POSTGRESQL:
                return "org.hibernate.dialect.PostgreSQLDialect";
            case SQL_SERVER:
                return "org.hibernate.dialect.SQLServerDialect";
            case SYBASE:
                return "org.hibernate.dialect.SybaseASE15Dialect";
            default:
                return null;
        }
    }
}
