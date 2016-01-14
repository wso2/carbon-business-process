package org.wso2.carbon.bpmn.analytics.publisher;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bpmn.analytics.publisher.internal.BPMNAnalyticsHolder;
import org.wso2.carbon.utils.WaitBeforeShutdownObserver;

import java.util.HashMap;
import java.util.Map;

/**
 * AnalyticsSchedulerShutdown class halts the carbon server shutdown until the scheduler is shut.
 */
public class AnalyticsSchedulerShutdown implements WaitBeforeShutdownObserver {
	private static Log log = LogFactory.getLog(AnalyticsSchedulerShutdown.class);
	private boolean status = false;

	//triggered before shutting down server and shutdown the scheduler if exists
	@Override public void startingShutdown() {
		HashMap<Integer, AnalyticsPublisher> publisherList =
				BPMNAnalyticsHolder.getInstance().getAllPublishers();

		if (publisherList != null) {
			for (Map.Entry<Integer, AnalyticsPublisher> entry : publisherList.entrySet()) {
				if (entry != null && entry.getValue() != null) {
					entry.getValue().stopDataPublisher();
				}
			}
		}
		status = true;
	}

	@Override public boolean isTaskComplete() {
		return status;
	}
}
