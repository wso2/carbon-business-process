/*
 * Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.bpel.b4p.coordination.dao.jpa.openjpa;

import org.wso2.carbon.bpel.b4p.coordination.dao.Constants;
import org.wso2.carbon.bpel.b4p.coordination.dao.HTCoordinationDAOConnection;
import org.wso2.carbon.bpel.b4p.coordination.dao.HTCoordinationDAOConnectionFactoryJDBC;
import org.wso2.carbon.bpel.b4p.coordination.dao.jpa.JPAVendorAdapter;

import java.util.HashMap;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.sql.DataSource;
import javax.transaction.RollbackException;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;

/**
 * JPA Implementation
 */
public class HTCoordinationDAOConnectionFactoryImpl implements HTCoordinationDAOConnectionFactoryJDBC {

    private static ThreadLocal<HTCoordinationDAOConnectionImpl> connections = new
            ThreadLocal<HTCoordinationDAOConnectionImpl>();
    private EntityManagerFactory entityManagerFactory;
    private DataSource dataSource;
    private TransactionManager tnxManager;
    private Map<String, Object> jpaPropertiesMap;

    public HTCoordinationDAOConnectionFactoryImpl() {

    }

    @Override
    public void setTransactionManager(TransactionManager tnxManager) {
        this.tnxManager = tnxManager;

    }

    @Override
    public void setDAOConnectionFactoryProperties(Map<String, Object> propertiesMap) {
        this.jpaPropertiesMap = propertiesMap;
    }

    @Override
    public HTCoordinationDAOConnection getConnection() {
        //TODO : After we have external tnx manager injection, need to handle the synchronisations at this level.
        // introduce sync and thread local behaviours.

        try {
            tnxManager.getTransaction().registerSynchronization(new Synchronization() {
                // OpenJPA allows cross-transaction entity managers, which we don't want
                public void afterCompletion(int i) {
                    if (connections.get() != null) {
                        connections.get().getEntityManager().close();
                    }
                    connections.set(null);
                }

                public void beforeCompletion() {
                }
            });
        } catch (RollbackException e) {
            throw new RuntimeException("Coulnd't register synchronizer!", e);
        } catch (SystemException e) {
            throw new RuntimeException("Coulnd't register synchronizer!", e);
        }

        if (connections.get() != null) {
            return connections.get();
        } else {
            HashMap propMap = new HashMap();
            propMap.put("openjpa.TransactionMode", "managed");
            EntityManager em = entityManagerFactory.createEntityManager(propMap);
            HTCoordinationDAOConnectionImpl conn = createHTCoordinationDAOConnection(em);
            connections.set(conn);
            return conn;
        }
    }

    protected HTCoordinationDAOConnectionImpl createHTCoordinationDAOConnection(EntityManager entityManager) {
        return new HTCoordinationDAOConnectionImpl(entityManager);
    }

    @Override
    public void init() {

        JPAVendorAdapter vendorAdapter = getJPAVendorAdapter();
        this.entityManagerFactory = Persistence.createEntityManagerFactory("B4P-DAO",
                vendorAdapter.getJpaPropertyMap(tnxManager));

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

        vendorAdapter.setDataSource(dataSource);

        // TODO: Investigate whether this could be moved to upper layer. Directly put bool into prop map.
        Object generateDDL = jpaPropertiesMap.get(Constants.PROP_ENABLE_DDL_GENERATION);
        Object showSQL = jpaPropertiesMap.get(Constants.PROP_ENABLE_SQL_TRACING);

        if (generateDDL == null) {
            generateDDL = Boolean.FALSE.toString();
        }

        if (showSQL == null) {
            showSQL = Boolean.FALSE.toString();
        }

        vendorAdapter.setGenerateDdl((Boolean) generateDDL);
        vendorAdapter.setShowSql((Boolean) showSQL);

        return vendorAdapter;
    }

    @Override
    public DataSource getDataSource() {
        return this.dataSource;
    }

    @Override
    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void shutdown() {
        this.entityManagerFactory.close();
    }
}
