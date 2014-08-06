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

package org.wso2.carbon.attachment.mgt.core.dao.impl.jpa.openjpa;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.openjpa.persistence.ArgumentException;
import org.wso2.carbon.attachment.mgt.core.dao.AttachmentMgtDAOConnection;
import org.wso2.carbon.attachment.mgt.core.dao.impl.jpa.Constants;
import org.wso2.carbon.attachment.mgt.core.dao.impl.jpa.JPABasedAttachmentMgtDAOConnectionFactory;
import org.wso2.carbon.attachment.mgt.core.dao.impl.jpa.JPAVendorAdapter;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.sql.DataSource;
import javax.transaction.TransactionManager;
import java.util.Map;

/**
 * OpenJPA specific implementation of {@link org.wso2.carbon.attachment.mgt.core.dao.AttachmentMgtDAOConnectionFactory}
 */
public class AttachmentMgtDAOConnectionFactoryImpl implements JPABasedAttachmentMgtDAOConnectionFactory {
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
        //TODO: Couldn;t understand the following code-block. So commented-out for the moment.
        /*try {
            transactionManager.getTransaction().registerSynchronization(new Synchronization() {
                // OpenJPA allows cross-transaction entity managers, which we don't want
                public void afterCompletion(int i) {
                    if (connections.get() != null)
                        connections.get().getEntityManager().close();
                    connections.set(null);
                }
                public void beforeCompletion() { }
            });
        } catch (RollbackException e) {
            throw new RuntimeException("Coulnd't register synchronizer!");
        } catch (SystemException e) {
            throw new RuntimeException("Coulnd't register synchronizer!");
        }*/

        if (connections.get() != null) {
            return connections.get();
        } else {
            try {
                //At the moment a property-map is not passed when constructing the entity-Manager
                EntityManager entityManager = entityManagerFactory.createEntityManager();
                AttachmentMgtDAOConnectionImpl conn = createAttachmentMgtDAOConnection(entityManager);
                connections.set(conn);
                return conn;
            } catch (ArgumentException argEx) {
                log.fatal("Entity-Manager creation failed.", argEx);
                throw argEx;
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

        JPAVendorAdapter vendorAdapter = getJPAVendorAdapter();
        //Here we pass a "null" valued transaction manager,
        // as we enforce the entity-manager-factory to use its local transactions. In future,
        // if required to use external JTA transactions, a transaction reference should be passed
        // as the input parameter.
        this.entityManagerFactory = Persistence.createEntityManagerFactory("Attachment-Mgt-PU"
                , vendorAdapter
                .getJpaPropertyMap(null));
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
    private JPAVendorAdapter getJPAVendorAdapter() {
        JPAVendorAdapter vendorAdapter = new OpenJPAVendorAdapter();

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
