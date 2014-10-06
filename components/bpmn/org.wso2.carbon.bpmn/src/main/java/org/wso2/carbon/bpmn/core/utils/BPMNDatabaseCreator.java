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

package org.wso2.carbon.bpmn.core.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.utils.dbcreator.DatabaseCreator;

import javax.sql.DataSource;
import java.io.File;

/**
 *  This class overrides the DatabaseCreator class to provide the db script location for
 *  activiti checksum table BPS_BPMN_DEPLOYMENT_METADATA and tables which might be added for BPMN
 *  internal tasks
 */
public class BPMNDatabaseCreator extends DatabaseCreator {

    private static final Log log = LogFactory.getLog(DatabaseCreator.class);
    public BPMNDatabaseCreator(DataSource dataSource) {
        super(dataSource);
    }

	/**
	 * This points the following path <CARBON_HOME>/dbscripts/bps/bpmn/checksum/create
	 *
	 * @param databaseType  based on the database type, the script to be called will differ ( Ex. mysql.s
	 * @return
	 */

    protected String getDbScriptLocation(String databaseType){
        String scriptName = databaseType + ".sql";
        if (log.isDebugEnabled()) {
            log.debug("Loading database script from :" + scriptName);
        }
        String carbonHome = System.getProperty("carbon.home");
        return carbonHome + File.separator + "dbscripts" + File.separator + "bps" + File.separator
                + "bpmn" + File.separator + "metadata" + File.separator + "create" + File.separator + scriptName;
    }
}
