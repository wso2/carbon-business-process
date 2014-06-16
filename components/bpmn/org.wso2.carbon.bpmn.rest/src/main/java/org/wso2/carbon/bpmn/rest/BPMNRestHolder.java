package org.wso2.carbon.bpmn.rest;

import org.osgi.service.http.HttpService;
import org.wso2.carbon.registry.core.service.RegistryService;

public class BPMNRestHolder {

    private static BPMNRestHolder instance = null;

    private RegistryService registryService;

    private HttpService httpService;

    private BPMNRestHolder() {}

    public static BPMNRestHolder getInstance() {
        if (instance == null) {
            instance = new BPMNRestHolder();
        }
        return instance;
    }

    public RegistryService getRegistryService() {
        return registryService;
    }

    public void setRegistryService(RegistryService registryService) {
        this.registryService = registryService;
    }

    public HttpService getHttpService() {
        return httpService;
    }

    public void setHttpService(HttpService httpService) {
        this.httpService = httpService;
    }
}
