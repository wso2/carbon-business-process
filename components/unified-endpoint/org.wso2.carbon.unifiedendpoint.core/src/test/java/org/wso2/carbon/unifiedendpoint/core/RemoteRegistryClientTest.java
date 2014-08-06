package org.wso2.carbon.unifiedendpoint.core;

import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.app.RemoteRegistryService;
import org.wso2.carbon.registry.core.Resource;

import java.io.File;


public class RemoteRegistryClientTest {
    private static RemoteRegistryService registryService;
    private static Registry registry;
    private static final String CARBON_HOME = "/home/kasun/development/wso2/wso2-distrns/wso2greg-3.6.0-SNAPSHOT" ;


    public void init() {
        System.setProperty("javax.net.ssl.trustStore", CARBON_HOME + File.separator + "resources" + File.separator + "security" + File.separator + "client-truststore.jks");
        System.setProperty("javax.net.ssl.trustStorePassword", "wso2carbon");
        System.setProperty("javax.net.ssl.trustStoreType","JKS");
    }


    public String getResourceContent(String regPath) throws Exception {
        String uepStr = "";
        registryService = new RemoteRegistryService("http://localhost:9763/registry", "admin", "admin");
        registry = registryService.getGovernanceUserRegistry("admin", "admin");
        Resource res = registry.get(regPath);

        if (res != null) {
            uepStr = new String((byte[]) res.getContent());
            //System.out.println("Res :" + new String((byte[]) res.getContent()));
        }

        return uepStr;
    }

}