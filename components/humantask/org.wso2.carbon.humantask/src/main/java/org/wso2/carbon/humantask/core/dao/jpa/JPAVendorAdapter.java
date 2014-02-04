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

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import javax.transaction.TransactionManager;
import java.util.Map;

/**
 * Handle JPA implementation specific EntityManager creation.
 */
public interface JPAVendorAdapter {

    /**
     * Set DataSource to use with JPA layer
     *
     * @param dataSource data source to use with JPA layer
     */
    void setDataSource(DataSource dataSource);

    /**
     * Whether to generate DDL
     *
     * @param generateDdl boolean
     */
    void setGenerateDdl(boolean generateDdl);

    /**
     * Whether to print SQL traces
     *
     * @param showSql boolean
     */
    void setShowSql(boolean showSql);

    /**
     * Returns the current JPA vendor specific property map
     *
     * @return property map
     * @param tnxManager TransactionManager
     */
    Map<String, ?> getJpaPropertyMap(TransactionManager tnxManager);

    /**
     * Return the vendor-specific EntityManagerFactory interface
     * that the EntityManagerFactory proxy is supposed to implement.
     * <p>If the provider does not offer any EntityManagerFactory extensions,
     * the adapter should simply return the standard
     * {@link javax.persistence.EntityManagerFactory} class here.
     *
     * @return
     */
    Class<? extends EntityManagerFactory> getEntityManagerFactoryInterface();

    /**
     * Return the vendor-specific EntityManager interface
     * that this provider's EntityManagers will implement.
     * <p>If the provider does not offer any EntityManager extensions,
     * the adapter should simply return the standard
     * {@link javax.persistence.EntityManager} class here.
     *
     * @return
     */
    Class<? extends EntityManager> getEntityManagerInterface();
}
