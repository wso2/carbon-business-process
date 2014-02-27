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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.ejb.Ejb3Configuration;
import org.wso2.carbon.attachment.mgt.core.dao.AttachmentMgtDAOConnection;
import org.wso2.carbon.attachment.mgt.core.dao.AttachmentMgtDAOConnectionFactory;
import org.wso2.carbon.attachment.mgt.core.dao.impl.jpa.Constants;
import org.wso2.carbon.attachment.mgt.core.dao.impl.jpa.openjpa.entity.AttachmentDAOImpl;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import javax.transaction.TransactionManager;
import java.util.Map;

public class AttachmentMgtDAOConnectionFactoryImpl implements AttachmentMgtDAOConnectionFactory {

    /**
     * Class Logger
     */
    private static Log log = LogFactory.getLog(AttachmentMgtDAOConnectionFactoryImpl.class);

    protected EntityManagerFactory entityManagerFactory;

    private DataSource dataSource;

    private TransactionManager transactionManager;

    private Map<String, Object> jpaPropertiesMap;

    private static ThreadLocal<AttachmentMgtDAOConnectionImpl> connections = new
            ThreadLocal<AttachmentMgtDAOConnectionImpl>();

    //TODO: Throw a proper exception
    @Override
    public AttachmentMgtDAOConnection getDAOConnection() {

        if (connections.get() != null) {
            return connections.get();
        } else {
            try {
                //At the moment a property-map is not passed when constructing the entity-Manager
                EntityManager entityManager = entityManagerFactory.createEntityManager();
                AttachmentMgtDAOConnectionImpl conn = createAttachmentMgtDAOConnection(entityManager);
                connections.set(conn);
                return conn;
            } catch (Exception argEx) {
                log.fatal("Entity-Manager creation failed.", argEx);
                throw new RuntimeException(argEx);
            }
        }
    }

    protected AttachmentMgtDAOConnectionImpl createAttachmentMgtDAOConnection(EntityManager
                                                                                      entityManager) {
        return new AttachmentMgtDAOConnectionImpl(entityManager);
    }

    @Override
    public void init() {
        if (transactionManager == null) {
            log.debug("Transaction-Manager is not initialized before initializing entityManager. So internal " +
                    "transaction-manager in entity manager will be used.");
        }

        org.wso2.carbon.attachment.mgt.core.dao.impl.jpa.JPAVendorAdapter vendorAdapter = getJPAVendorAdapter();
        // Note: It is recommended to use javax.persistence.Persistence.createEntityManagerFactory() to create
        // EntityManagerFactory. But it is failing in OSGI environment. So we used Ejb3Configuration, but it is deprecated.
        Ejb3Configuration cfg = new Ejb3Configuration();
        cfg.addAnnotatedClass(AttachmentDAOImpl.class);
        this.entityManagerFactory = cfg.createEntityManagerFactory(vendorAdapter.getJpaPropertyMap(null));
    }

    @Override
    public void shutdown() {
        this.entityManagerFactory.close();
    }

    @Override
    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void setDAOConnectionFactoryProperties(Map<String, Object> propertiesMap) {
        this.jpaPropertiesMap = propertiesMap;
    }

    @Override
    public void setTransactionManager(TransactionManager tnxManager) {
        this.transactionManager = tnxManager;
    }

    /**
     * Returns the JPA Vendor adapter based on user preference
     * <p/>
     * Note: Currently we only support one JPA vendor(OpenJPA), so I have omitted vendor selection
     * logic.
     *
     * @return JPAVendorAdapter implementation
     */
    private org.wso2.carbon.attachment.mgt.core.dao.impl.jpa.JPAVendorAdapter getJPAVendorAdapter() {
        org.wso2.carbon.attachment.mgt.core.dao.impl.jpa.JPAVendorAdapter vendorAdapter = new JPAVendorAdapter();

        if (dataSource != null) {
            vendorAdapter.setDataSource(dataSource);
        } else {
            log.error("DataSource is not initialized prior to initializing JPAVendorAdapter.");
        }

        // TODO: Investigate whether this could be moved to upper layer. Directly put bool into prop map.
        Object generateDDL = jpaPropertiesMap.get(Constants.PROP_ENABLE_DDL_GENERATION);
        Object showSQL = jpaPropertiesMap.get(Constants.PROP_ENABLE_SQL_TRACING);

        if (generateDDL == null) {
            generateDDL = Boolean.FALSE.toString();
        }

        if (showSQL == null) {
            showSQL = Boolean.FALSE.toString();
        }

        vendorAdapter.setGenerateDdl(Boolean.parseBoolean(generateDDL.toString()));
        vendorAdapter.setShowSql(Boolean.parseBoolean(showSQL.toString()));

        return vendorAdapter;
    }
}
