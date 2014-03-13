package org.wso2.carbon.bpel.core.ode.integration.store.clustering;

import org.apache.axis2.clustering.ClusteringFault;
import org.apache.axis2.clustering.state.StateClusteringCommand;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.iapi.ProcessState;
import org.wso2.carbon.bpel.core.BPELConstants;
import org.wso2.carbon.bpel.core.ode.integration.store.ProcessStoreImpl;
import org.wso2.carbon.bpel.core.ode.integration.store.TenantProcessStore;

import javax.xml.namespace.QName;

/**
 * This command is used to change the state of a BPEL process
 * i.e. When activating and retiring processes
 */
public class BPELProcessStateChangedCommand  extends StateClusteringCommand {
    private static final Log log = LogFactory.getLog(BPELConstants.LOGGER_DEPLOYMENT);

    private Integer tenantId;

    private QName pid;

    private ProcessState processState;

    public BPELProcessStateChangedCommand(QName pid, ProcessState processState, Integer tenantId) {
        this.pid = pid;
        this.processState = processState;
        this.tenantId = tenantId;
    }

    @Override
    public void execute(ConfigurationContext configurationContext) throws ClusteringFault {
        if (log.isDebugEnabled()) {
            log.debug("New state changed command received. Process: " + pid + " New state: " +
                    processState + " Tenant: " + tenantId );
        }
        ProcessStoreImpl parentProcessStore =
                (ProcessStoreImpl)configurationContext.getAxisConfiguration()
                        .getParameter(BPELConstants.PARAM_PARENT_PROCESS_STORE).getValue();
        TenantProcessStore tenantProcessStore = parentProcessStore.getTenantsProcessStore(tenantId);
        tenantProcessStore.handleBPELProcessStateChangedNotification(pid, processState);
    }
}
