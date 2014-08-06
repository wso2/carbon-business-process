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

package org.wso2.carbon.humantask.core.dao.jpa.openjpa;

import org.wso2.carbon.humantask.core.dao.DatabaseType;
import org.wso2.carbon.humantask.core.dao.jpa.AbstractJPAVendorAdapter;

import javax.transaction.TransactionManager;
import java.util.HashMap;
import java.util.Map;

public class OpenJPAVendorAdapter extends AbstractJPAVendorAdapter {

    @Override
    public Map<String, ?> getJpaPropertyMap(TransactionManager tnxManager) {
        Map<String, Object> jpaProperties = new HashMap<String, Object>();

        if (getDataSource() != null) {
            jpaProperties.put("openjpa.ConnectionFactory", getDataSource());
            String dbDictionary = determineDbDictionary();
            if (dbDictionary != null) {
                log.info("[HT OpenJPA] DB Dictionary: " + dbDictionary);
                jpaProperties.put("openjpa.jdbc.DBDictionary", dbDictionary);
            }
            jpaProperties.put("openjpa.ConnectionFactoryMode", "managed");
//            jpaProperties.put("openjpa.jdbc.TransactionIsolation", "read-committed");
        }

        if (isGenerateDDL()) {
            log.info("[HT OpenJPA] Generate DDL Enabled.");
            jpaProperties.put("openjpa.jdbc.SynchronizeMappings", "buildSchema(ForeignKeys=true)");
        }

        if (isShowSQL()) {
            log.info("[HT OpenJPA] Show SQL enabled.");
            jpaProperties.put("openjpa.Log", "DefaultLevel=WARN, Runtime=INFO, Tool=INFO, SQL=TRACE");
            jpaProperties.put("openjpa.ConnectionFactoryProperties", "PrettyPrint=true, PrettyPrintLineLength=72");
        } else {
            jpaProperties.put("openjpa.Log", "commons");
        }

        if (tnxManager != null) {
            jpaProperties.put("openjpa.ManagedRuntime", new TransactionManagerProvider(tnxManager));
        }

//        jpaProperties.put("openjpa.Id", "HT-PU");
        jpaProperties.put("openjpa.QueryCache", "false");
        jpaProperties.put("openjpa.DataCache", "false");
        jpaProperties.put("openjpa.jdbc.QuerySQLCache", "false");


        return jpaProperties;
    }

    protected String determineDbDictionary() {
        DatabaseType dbType = determineDbType();
        switch (dbType) {
            case DB2:
                return "db2";
            case DERBY:
                return "derby";
            case H2:
                return "h2";
            case HSQL:
                return "hsql(SimulateLocking=true)";
            case INFORMIX:
                return "informix";
            case MYSQL:
                return "mysql";
            case ORACLE:
                return "oracle";
            case POSTGRESQL:
                return "postgres";
            case SQL_SERVER:
                return "sqlserver";
            case SYBASE:
                return "sybase";
            default:
                return null;
        }
    }

}
