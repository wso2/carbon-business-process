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

/**
 * In memory configuration for the data source
 */
class BasicDataSourceConfiguration {
    /**
     * DataSource Name
     */
    private String dataSourceName;

    /**
     * Initial Context Factory class
     */
    private String dataSourceJNDIRepoInitialContextFactory;

    /**
     * JNDI Repo URL
     */
    private String dataSourceJNDIRepoProviderURL;

    /**
     * Constructor to load the configuration
     *
     * @param dataSourceName
     * @param dataSourceJNDIRepoInitialContextFactory
     *
     * @param dataSourceJNDIRepoProviderURL

     */
    public BasicDataSourceConfiguration(String dataSourceName, String dataSourceJNDIRepoInitialContextFactory, String dataSourceJNDIRepoProviderURL) {
        this.dataSourceName = dataSourceName;
        this.dataSourceJNDIRepoInitialContextFactory = dataSourceJNDIRepoInitialContextFactory;
        this.dataSourceJNDIRepoProviderURL = dataSourceJNDIRepoProviderURL;
    }

    /**
     * Default constructor is disabled
     */
    private BasicDataSourceConfiguration() {
    }

    /**
     * returns DataSource Name
     *
     * @return DataSource Name
     */
    public String getDataSourceName() {
        return dataSourceName;
    }

    /**
     * returns Initial Context Factory class
     *
     * @return Initial Context Factory class
     */
    public String getDataSourceJNDIRepoInitialContextFactory() {
        return dataSourceJNDIRepoInitialContextFactory;
    }

    /**
     * returns JNDI Repo URL
     *
     * @return JNDI Repo URL
     */
    public String getDataSourceJNDIRepoProviderURL() {
        return dataSourceJNDIRepoProviderURL;
    }
}
