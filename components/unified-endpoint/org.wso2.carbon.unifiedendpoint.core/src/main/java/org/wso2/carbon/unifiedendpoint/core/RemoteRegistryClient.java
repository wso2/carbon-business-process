package org.wso2.carbon.unifiedendpoint.core;

import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.registry.app.RemoteRegistryService;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.utils.CarbonUtils;

import java.io.File;


public class RemoteRegistryClient {
    private static RemoteRegistryService registryService;
    private static Registry registry;
    private static final String CARBON_HOME = "/home/kasun/development/wso2/wso2-distrns/wso2greg-3.6.0-SNAPSHOT" ;

    
    public RemoteRegistryClient() {
        ServerConfiguration config = ServerConfiguration.getInstance();
        String type = config.getFirstProperty("Security.KeyStore.Type");
        String password = config.getFirstProperty("Security.KeyStore.Password");
        String storeFile = new File(config.getFirstProperty("Security.KeyStore.Location")).getAbsolutePath();

        System.setProperty("javax.net.ssl.trustStore", storeFile);
        System.setProperty("javax.net.ssl.trustStoreType", type);
        System.setProperty("javax.net.ssl.trustStorePassword", password);
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