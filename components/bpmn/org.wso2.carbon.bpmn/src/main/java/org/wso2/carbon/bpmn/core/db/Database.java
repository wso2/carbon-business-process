/**
 *  Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */


package org.wso2.carbon.bpmn.core.db;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bpmn.core.BPMNConstants;
import org.wso2.carbon.bpmn.core.exception.BPMNMetaDataTableCreationException;
import org.wso2.carbon.bpmn.core.exception.DatabaseConfigurationException;
import org.wso2.carbon.bpmn.core.utils.BPMNDatabaseCreator;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

/**
 * manages the activiti engine database.
 */

public class Database {

    private static final Log log = LogFactory.getLog(Database.class);

    private String jndiDataSourceName;
    private DataSource dataSource;

    private boolean started;

    /**
     * @return The Human Task DataSource..
     */

    public synchronized DataSource getDataSource() {
        if (this.dataSource == null) {
            throw new RuntimeException("Activiti Engine Database is not properly initialised!");
        }
        return this.dataSource;
    }

    public Database(String jndiDataSourceName) {
        this.jndiDataSourceName = jndiDataSourceName;
    }

    /**
     * Database initialization logic.
     */
    public synchronized void init() throws DatabaseConfigurationException,
                                           BPMNMetaDataTableCreationException {
        this.dataSource = null;
        initDataSource();
        createActivitiMetaDataTable();
        started = true;
    }

	/**
	 * during the start up of the server this method will create BPS_BPMN_DEPLOYMENT_METADATA table
	 * if it doesn't exist in the activiti database.
	 *
	 * @throws org.wso2.carbon.bpmn.core.exception.BPMNMetaDataTableCreationException
	 */
    private void createActivitiMetaDataTable() throws BPMNMetaDataTableCreationException {
            BPMNDatabaseCreator bpmnDatabaseCreator = new BPMNDatabaseCreator(getDataSource());
	        String bpmnDeploymentMetaDataQuery = "SELECT * FROM " + BPMNConstants.BPS_BPMN_DEPLOYMENT_METADATA_TABLE;

            if (!bpmnDatabaseCreator.isDatabaseStructureCreated(bpmnDeploymentMetaDataQuery)) {
                try {
                    bpmnDatabaseCreator.createRegistryDatabase();
                } catch (Exception e) {
                    String errMsg = "Error creating BPS_BPMN_DEPLOYMENT_METADATA table";
                    throw new BPMNMetaDataTableCreationException(errMsg, e);
                }
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("BPS_BPMN_DEPLOYMENT_METADATA table already exists. Using the old table.");
                }
            }
    }


    /**
     * Shut down logic for the Database related resources.
     */
    public synchronized void shutdown() {
        if (!started) {
            return;
        }
        this.dataSource = null;
        this.started = false;
    }

    private void initDataSource() throws DatabaseConfigurationException {
        //Note : We can improve this to consider internal/external databases.
        initExternalDb();
    }

    private void initExternalDb() throws DatabaseConfigurationException {
        try {
            this.dataSource = lookupInJndi(jndiDataSourceName);

            if (log.isDebugEnabled()) {
                log.debug("BPMN Activiti initialization using external DataSource " +
                        jndiDataSourceName);
            }
        } catch (NamingException e) {
            String errorMsg = "Failed to resolved external DataSource at " +
                    jndiDataSourceName;
            throw new DatabaseConfigurationException(errorMsg, e);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T lookupInJndi(String objName) throws NamingException {
        ClassLoader old = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
        InitialContext ctx = null;

        try {

            try {
                ctx = new InitialContext();
                return (T) ctx.lookup(objName);
            } finally {
                if (ctx != null) {
                    try {
                        ctx.close();
                    } catch (NamingException ex1) {
                        log.error("Error closing JNDI connection.", ex1);
                    }
                }

            }
        } finally {
	        Thread.currentThread().setContextClassLoader(old);
        }
    }
}
