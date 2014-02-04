package org.wso2.carbon.bpel.core.ode.integration.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.utils.dbcreator.DatabaseCreator;

import javax.sql.DataSource;
import java.io.File;

/**
 * To handle -Dsetup for BPEL component
 */
public class BPELDatabaseCreator extends DatabaseCreator {
    private static final Log log = LogFactory.getLog(DatabaseCreator.class);

    public BPELDatabaseCreator(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    protected String getDbScriptLocation(String databaseType) {
        String scriptName = databaseType + ".sql";
        if (log.isDebugEnabled()) {
            log.debug("Loading database script from :" + scriptName);
        }
        String carbonHome = System.getProperty("carbon.home");
        return carbonHome + File.separator + "dbscripts" + File.separator + "bps" + File.separator
                + scriptName;
    }
}
