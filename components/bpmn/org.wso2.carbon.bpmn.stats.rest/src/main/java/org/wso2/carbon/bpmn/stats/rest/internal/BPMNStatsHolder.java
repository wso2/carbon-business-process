package org.wso2.carbon.bpmn.stats.rest.internal;

import org.activiti.engine.ProcessEngine;
import org.wso2.carbon.user.core.service.RealmService;

/**
 * Created by natasha on 11/20/15.
 */
public class BPMNStatsHolder {
    private static ProcessEngine engine;
    private static RealmService realmService = null;

    public static ProcessEngine getEngine() {
        return engine;
    }

    public static void setEngine(ProcessEngine engine) {
        BPMNStatsHolder.engine = engine;
    }

    public RealmService getRealmService() {
        return realmService;
    }

    public void setRealmService(RealmService realmService) {
        BPMNStatsHolder.realmService = realmService;
    }
}
