package org.wso2.carbon.bpmn.core.deployment;

import com.hazelcast.core.HazelcastInstance;
import org.wso2.carbon.bpmn.core.BPMNServerHolder;
import org.wso2.carbon.bpmn.core.BPSException;
import org.wso2.carbon.bpmn.core.internal.BPMNServiceComponent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class TenantManager {

    private Map<Integer, TenantRepository> tenantRepositories = new HashMap<Integer, TenantRepository>();

    public TenantRepository getTenantRepository(Integer tenantId) {
        return tenantRepositories.get(tenantId);
    }

    public TenantRepository createTenantRepository(Integer tenantId) throws BPSException {
        TenantRepository tenantRepository = new TenantRepository(tenantId);
        tenantRepository.loadExistingDeployments();
        tenantRepositories.put(tenantId, tenantRepository);
        return tenantRepository;
    }

    /**
     * When clustering is enabled we need to add Hazelcast distributed sets for local deployment and process definitions IDs in tenant repository.
     * First need to populate local contents of sets to distributed sets.
     * Then distributed sets will be used in clustering mode.
     */
    public void populateDistributedSets() throws BPSException {

        for (TenantRepository tenantRepository : tenantRepositories.values()) {

            Set<Object> deploymentIds = BPMNServerHolder.getInstance().getHazelcastInstance().getSet("deploymentIds" + tenantRepository.getTenantId());
            Set<Object> processDefinitionIds = BPMNServerHolder.getInstance().getHazelcastInstance().getSet("processDefinitionIds" + tenantRepository.getTenantId());
            for (Object o : tenantRepository.getDeploymentIds()) {

                deploymentIds.add(o);
            }
            for (Object o : tenantRepository.getProcessDefinitionIds()) {

                processDefinitionIds.add(o);
            }
            tenantRepository.setDeploymentIds(deploymentIds);
            tenantRepository.setProcessDefinitionIds(processDefinitionIds);

        }

    }
}
