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

package org.wso2.carbon.attachment.mgt.core.dao;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.attachment.mgt.configuration.AttachmentServerConfiguration;
import org.wso2.carbon.attachment.mgt.core.dao.impl.jpa.Constants;
import org.wso2.carbon.attachment.mgt.server.internal.AttachmentServerHolder;
import org.wso2.carbon.attachment.mgt.util.ConfigurationUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Abstract implementation of the DAOManager {@link DAOManager}
 */
public abstract class AbstractDAOManager implements DAOManager {
    /**
     * Class Logger
     */
    private static final Log log = LogFactory.getLog(AbstractDAOManager.class);

    /**
     * DAO Manager maintains a DAOConnectionFactory ({@link AttachmentMgtDAOConnectionFactory}
     * where the connections to the JPA based api is managed)
     */
    protected AttachmentMgtDAOConnectionFactory daoConnectionFactory;

    /**
     * DAO Manager maintains a DAOTransformerFactory {@link AttachmentMgtDAOTransformerFactory}
     * where the DAO to API transformations are maintained
     */
    protected AttachmentMgtDAOTransformerFactory daoTransformerFactory;

    /**
     * Maintains the server configurations, which will be reused to configure the DAO Manager
     */
    protected AttachmentServerConfiguration serverConfiguration;

    /**
     * Returns the AttachmentMgtDAOConnectionFactory reference
     *
     * @return
     */
    @Override
    public AttachmentMgtDAOConnectionFactory getDAOConnectionFactory() {
        if (this.daoConnectionFactory != null) {
            return this.daoConnectionFactory;
        } else {
            log.error("DAO Connection Factory is not initialized.");
            throw new NullPointerException("DAO Connection Factory is not initialized.");
        }
    }

    /**
     * Returns the AttachmentMgtDAOTransformerFactory reference
     *
     * @return
     */
    @Override
    public AttachmentMgtDAOTransformerFactory getDAOTransformerFactory() {
        if (this.daoTransformerFactory != null) {
            return this.daoTransformerFactory;
        } else {
            log.error("DAO Transformer Factory is not initialized.");
            throw new NullPointerException("DAO Transformer Factory is not initialized.");
        }
    }

    @Override
    public void init(AttachmentServerConfiguration serverConfig) {
        serverConfiguration = serverConfig;
        initDAOConnectionFactory(serverConfig.getDaoConnectionFactoryClass());
        initDAOTransformerFactory(serverConfig.getDaoTransformerFactoryClass());
    }

    private void initDAOTransformerFactory(String daoTransformerFactoryClassName) {
        try {
            Class daoTransformerFactoryClass = this.getClass().getClassLoader().loadClass
                    (daoTransformerFactoryClassName);
            Object daoTransformerFactory = daoTransformerFactoryClass.newInstance();

            this.daoTransformerFactory = (AttachmentMgtDAOTransformerFactory) daoTransformerFactory;
        } catch (Exception e) {
            log.error("DAO Transformer Factory creation failed. Reason:" + e.getLocalizedMessage(),
                      e);
        }

    }

    private void initDAOConnectionFactory(String daoConnectionFactoryClassName) {
        try {
            Class daoConnectionFactoryClass = this.getClass().getClassLoader().loadClass(daoConnectionFactoryClassName);
            Object daoConnectionFactory = daoConnectionFactoryClass.newInstance();

            this.daoConnectionFactory = (AttachmentMgtDAOConnectionFactory)
                    daoConnectionFactory;

            this.daoConnectionFactory.setDataSource(AttachmentServerHolder.getInstance()
                                                            .getAttachmentServer()
                                                            .getDataSourceManager().getDataSource());

            this.daoConnectionFactory.setDAOConnectionFactoryProperties(getGenericDAOFactoryProperties());
            this.daoConnectionFactory.init();
        } catch (Exception e) {
            log.fatal("DAO Connection Factory creation failed. Reason:" + e.getLocalizedMessage(),
                      e);
        }
    }

    /**
     * Gets the generic properties for DAO Connection Factory.
     *
     * @return
     */
    private Map<String, Object> getGenericDAOFactoryProperties() {
        Map<String, Object> daoFactoryProperties = new HashMap<String, Object>();
        daoFactoryProperties.put(Constants.DATA_SOURCE_PROP, AttachmentServerHolder.getInstance()
                .getAttachmentServer().getDataSourceManager().getDataSource());
        daoFactoryProperties.put(Constants.PROP_ENABLE_DDL_GENERATION, serverConfiguration.isGenerateDdl());
        daoFactoryProperties.put(Constants.PROP_ENABLE_SQL_TRACING, serverConfiguration.isShowSql());
        daoFactoryProperties.put(Constants.DAO_FACTORY_CLASS_PROP, serverConfiguration.getDaoConnectionFactoryClass());

        return daoFactoryProperties;
    }

    @Override
    public void shutdown() {
        this.daoConnectionFactory.shutdown();
        this.daoConnectionFactory = null;
    }
}
