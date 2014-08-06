package org.wso2.carbon.bpel.cluster.notifier;

import org.apache.axis2.AxisFault;
import org.apache.axis2.clustering.state.Replicator;
import org.apache.axis2.clustering.state.StateClusteringCommand;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bpel.cluster.notifier.internal.BPELClusterNotifierServiceComponent;

/**
 * Send Clustering Commands.
 *
 * We have introduce a separate bundle for this, in order to isolate the ConfigurationContext OSGI
 * service from the BPEL Core bundle. So that we can have
 * <Axis2RequiredServices>org.wso2.carbon.bpel.core.BPELEngineService</Axis2RequiredServices>
 * element in the BPEL deployers pom
 */
public final class BPELClusterNotifier {
    public static final String PARAM_PARENT_PROCESS_STORE = "bpel.process-store";
    private static Log log = LogFactory.getLog(BPELClusterNotifier.class);

    private BPELClusterNotifier() {
    }

    public static void sendClusterNotification(StateClusteringCommand command, Object processStore)
            throws AxisFault {
        if (log.isDebugEnabled()) {
            log.debug("Sending clustering command.");
        }

        AxisConfiguration axisConfig = BPELClusterNotifierServiceComponent.getAxisConfiguration();
        axisConfig.addParameter(PARAM_PARENT_PROCESS_STORE, processStore);
        Replicator.replicateState(command, axisConfig);
    }
}
