package org.wso2.carbon.bpmn.core.deployment;

import org.wso2.carbon.bpmn.core.BPMNConstants;
import org.wso2.carbon.bpmn.core.BPMNServerHolder;
import org.wso2.carbon.bpmn.core.BPSException;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class TenantManager {

    private Map<Integer, TenantRepository> tenantRepositories = new HashMap<Integer, TenantRepository>();

    public TenantRepository getTenantRepository(Integer tenantId) {
        return tenantRepositories.get(tenantId);
    }

    public TenantRepository createTenantRepository(Integer tenantId) throws BPSException {
        TenantRepository tenantRepository = new TenantRepository(tenantId);
        tenantRepositories.put(tenantId, tenantRepository);
        return tenantRepository;
    }

}
