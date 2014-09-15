package org.wso2.carbon.bpmn.core.mgt.services;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.history.HistoricProcessInstanceQuery;
import org.activiti.engine.runtime.ProcessInstance;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bpmn.core.BPMNServerHolder;
import org.wso2.carbon.bpmn.core.BPSException;
import org.wso2.carbon.bpmn.core.mgt.model.BPMNInstance;
import org.wso2.carbon.bpmn.core.mgt.model.BPMNVariable;
import org.wso2.carbon.context.CarbonContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BPMNInstanceService {

    private static Log log = LogFactory.getLog(BPMNInstanceService.class);

    public void startProcess(String processID) throws BPSException {
        try {
            ProcessEngine engine = BPMNServerHolder.getInstance().getEngine();
            RuntimeService runtimeService = engine.getRuntimeService();
            runtimeService.startProcessInstanceById(processID);
        } catch (Exception e) {
            String msg = "Failed to start the process: " + processID;
            log.error(msg, e);
            throw new BPSException(msg, e);
        }
    }

    public BPMNInstance[] getProcessInstances() throws BPSException {
        Integer tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            ProcessEngine engine = BPMNServerHolder.getInstance().getEngine();
            RuntimeService runtimeService = engine.getRuntimeService();
            List<ProcessInstance> instances = runtimeService.createProcessInstanceQuery().processInstanceTenantId(tenantId.toString()).list();
            return getTenantBPMNInstances(instances);

        } catch (Exception e) {
            String msg = "Failed to get process instances of tenant: " + tenantId;
            log.error(msg, e);
            throw new BPSException(msg, e);
        }
    }

    public int getInstanceCount() throws BPSException {
        Integer tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            ProcessEngine engine = BPMNServerHolder.getInstance().getEngine();
            RuntimeService runtimeService = engine.getRuntimeService();
            List<ProcessInstance> instances = runtimeService.createProcessInstanceQuery().processInstanceTenantId(tenantId.toString()).list();
            return instances.size();

        } catch (Exception e) {
            String msg = "Failed to get the number of process instances.";
            log.error(msg, e);
            throw new BPSException(msg, e);
        }
    }

    public void suspendProcessInstance(String instanceId) throws BPSException {
        Integer tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            RuntimeService runtimeService = BPMNServerHolder.getInstance().getEngine().getRuntimeService();
            List<ProcessInstance> processInstances = runtimeService.createProcessInstanceQuery().processInstanceTenantId(tenantId.toString()).processInstanceId(instanceId).list();
            if (processInstances.isEmpty()) {
                String msg = "No process instances with the ID: " + instanceId;
                log.error(msg);
                throw new BPSException(msg);
            }
            runtimeService.suspendProcessInstanceById(instanceId);
        } catch (Exception e) {
            String msg = "Failed to get the number of process instances.";
            log.error(msg, e);
            throw new BPSException(msg, e);
        }
    }

    public void deleteProcessInstanceSet(String[] instanceIdSet) throws BPSException {
        for (String anInstanceIdSet : instanceIdSet) {
            deleteProcessInstance(anInstanceIdSet);
        }
    }

    public void deleteProcessInstance(String instanceId) throws BPSException {
        Integer tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            RuntimeService runtimeService = BPMNServerHolder.getInstance().getEngine().getRuntimeService();
            List<ProcessInstance> processInstances = runtimeService.createProcessInstanceQuery().processInstanceTenantId(tenantId.toString()).processInstanceId(instanceId).list();
            if (processInstances.isEmpty()) {
                String msg = "No process instances with the ID: " + instanceId;
                log.error(msg);
                throw new BPSException(msg);
            }
            runtimeService.deleteProcessInstance(instanceId, "Deleted by user.");
        } catch (Exception e) {
            String msg = "Failed to get the number of process instances.";
            log.error(msg, e);
            throw new BPSException(msg, e);
        }
    }

    public void activateProcessInstance(String instanceId) throws BPSException {
        Integer tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            RuntimeService runtimeService = BPMNServerHolder.getInstance().getEngine().getRuntimeService();

            List<ProcessInstance> processInstances = runtimeService.createProcessInstanceQuery().processInstanceTenantId(tenantId.toString()).processInstanceId(instanceId).list();
            if (processInstances.isEmpty()) {
                String msg = "No process instances with the ID: " + instanceId;
                log.error(msg);
                throw new BPSException(msg);
            }
            runtimeService.activateProcessInstanceById(instanceId);
        } catch (Exception e) {
            String msg = "Failed to get the number of process instances.";
            log.error(msg, e);
            throw new BPSException(msg, e);
        }
    }


    private BPMNInstance[] getTenantBPMNInstances(List<ProcessInstance> instances) {
        BPMNInstance bpmnInstance;
        Integer tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        List<BPMNInstance> bpmnInstances = new ArrayList<BPMNInstance>();
        HistoricProcessInstanceQuery query = BPMNServerHolder.getInstance().getEngine().getHistoryService().createHistoricProcessInstanceQuery().processInstanceTenantId(tenantId.toString());
        for (ProcessInstance instance : instances) {
            bpmnInstance = new BPMNInstance();
            bpmnInstance.setInstanceId(instance.getId());
            bpmnInstance.setProcessId(instance.getProcessDefinitionId());
            bpmnInstance.setSuspended(instance.isSuspended());
            bpmnInstance.setStartTime(query.processInstanceId(instance.getId()).singleResult().getStartTime());
            bpmnInstance.setVariables(formatVariables(instance.getProcessVariables()));
            bpmnInstances.add(bpmnInstance);
        }
        return bpmnInstances.toArray(new BPMNInstance[bpmnInstances.size()]);
    }

    private BPMNVariable[] formatVariables(Map<String, Object> processVariables) {
        BPMNVariable[] vars = new BPMNVariable[processVariables.size()];
        int currentVar = 0;
        for (Map.Entry entry : processVariables.entrySet()) {
            vars[currentVar] = new BPMNVariable(entry.getKey().toString(), processVariables.get(entry.getKey().toString()).toString());
        }
        return vars;
    }
}
