/*
 * Copyright (c) 2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.bpel.core.ode.integration;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.deployment.DeploymentEngine;
import org.apache.axis2.deployment.DeploymentException;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.AxisServiceGroup;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.collections.map.MultiKeyMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.agents.memory.SizingAgent;
import org.apache.ode.bpel.epr.WSDL11Endpoint;
import org.apache.ode.bpel.iapi.*;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.bpel.common.BusinessProcessConstants;
import org.wso2.carbon.bpel.common.ServiceConfigurationUtil;
import org.wso2.carbon.bpel.common.config.EndpointConfiguration;
import org.wso2.carbon.bpel.core.ode.integration.store.ProcessConfigurationImpl;
import org.wso2.carbon.bpel.core.ode.integration.store.TenantProcessStore;
import org.wso2.carbon.bpel.core.ode.integration.utils.AxisServiceUtils;

import javax.wsdl.Definition;
import javax.wsdl.PortType;
import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Axis2 implementation of the {@link org.apache.ode.bpel.iapi.BindingContext}
 * interface. Deals with the activation of endpoints.
 */

public class BPELBindingContextImpl implements BindingContext {
    private static final Log log = LogFactory.getLog(BPELBindingContextImpl.class);
    private BPELServerImpl bpelServer;
    private MultiKeyMap services = new MultiKeyMap();
    private Map<BPELProcessProxy, EndpointReference> serviceEprMap =
            new HashMap<BPELProcessProxy, EndpointReference>();

    public BPELBindingContextImpl(BPELServerImpl bpelServer) {
        this.bpelServer = bpelServer;
    }

    public EndpointReference activateMyRoleEndpoint(QName processId, Endpoint myRoleEndpoint) {
        try {
            if (log.isDebugEnabled()) {
                log.debug("Activating MyRole endpoint for process: " + processId + " endpoint: " +
                        myRoleEndpoint);
            }
            ProcessConf processConfiguration = ((ProcessStore) bpelServer.
                    getMultiTenantProcessStore()).getProcessConfiguration(processId);

            BPELProcessProxy processProxy = publishAxisService(processConfiguration,
                    myRoleEndpoint.serviceName,
                    myRoleEndpoint.portName);
            serviceEprMap.put(processProxy, processProxy.getServiceReference());
            updateServiceList(getTenantId(processId), myRoleEndpoint, STATE.ADD);
            return processProxy.getServiceReference();
        } catch (AxisFault af) {
            final String errMsg = "Could not activate endpoint for service " +
                    myRoleEndpoint.serviceName + " and port " +
                    myRoleEndpoint.portName;
            log.error(errMsg, af);
            throw new ContextException(errMsg, af);
        }
    }

    public void deactivateMyRoleEndpoint(QName processID, Endpoint endpoint) {
        if (log.isDebugEnabled()) {
            log.debug("Deactivating my role endpoint for process: " + processID + " service: " +
                    endpoint.serviceName + " and port: " + endpoint.portName);
        }

        Integer tenantId = bpelServer.getMultiTenantProcessStore().getTenantId(processID);

        BPELProcessProxy processProxy = getBPELProcessProxy(tenantId.toString(), endpoint.serviceName, endpoint.portName);
        if (processProxy != null) {
            ProcessConfigurationImpl processConf =
                    (ProcessConfigurationImpl) processProxy.getProcessConfiguration();
            if (processConf.isUndeploying()) {
                AxisService service = processProxy.getAxisService();
                Parameter param = service.getParameter(CarbonConstants.PRESERVE_SERVICE_HISTORY_PARAM);
                param.setValue("false");
            }

            removeBPELProcessProxyAndAxisService(tenantId.toString(), endpoint.serviceName, endpoint.portName);

            updateServiceList(
                    ((ProcessConfigurationImpl) processProxy.getProcessConfiguration()).getTenantId(),
                    endpoint,
                    STATE.REMOVE);
            serviceEprMap.remove(processProxy);
        }  // else this method also get called for the retired processes where there could be an
        // active version of the same process type. Since there is only one service for a
        // particular process type, processProxy will be null for all the endpoints except for 1.

    }

    public PartnerRoleChannel createPartnerRoleChannel(QName processId, PortType portType,
                                                       Endpoint initialPartnerEndpoint) {
        ProcessConf processConfiguration = ((ProcessStore) bpelServer.getMultiTenantProcessStore())
                .getProcessConfiguration(processId);
        Definition wsdl = processConfiguration.getDefinitionForService(
                initialPartnerEndpoint.serviceName);
        if (wsdl == null) {
            throw new ContextException("Cannot find definition for service " +
                    initialPartnerEndpoint.serviceName
                    + " in the context of process " + processId);
        }
        return createPartnerService(processConfiguration,
                initialPartnerEndpoint.serviceName,
                initialPartnerEndpoint.portName);
    }

    public long calculateSizeofService(EndpointReference endpointReference) {
        if (bpelServer.getOdeConfigurationProperties().isProcessSizeThrottled()) {
            for (Map.Entry<BPELProcessProxy, EndpointReference> entry : serviceEprMap.entrySet()) {
                if (endpointReference.equals(entry.getValue())) {
                    return SizingAgent.deepSizeOf(entry.getKey());
                }
            }
        }
        return 0;
    }

    private Integer getTenantId(QName processId) {
        ProcessConf processConfiguration = ((ProcessStore) bpelServer.
                getMultiTenantProcessStore()).getProcessConfiguration(processId);

        return  ((ProcessConfigurationImpl)processConfiguration).getTenantId();

//        return MultitenantUtils
//                .getTenantId(((MultiTenantProcessConfiguration) processConfiguration).getTenantConfigurationContext());
    }

    private PartnerService createPartnerService(ProcessConf pConf, QName serviceName,
                                                String portName)
            throws ContextException {
        PartnerService partnerService;

        Definition def = pConf.getDefinitionForService(serviceName);
        try {
            if (log.isDebugEnabled()) {
                log.debug("Creating external service " + serviceName);
            }
            partnerService = new PartnerService(def, serviceName, portName,
                    getConfigurationContextFromProcessConfiguration(pConf),
                    pConf, bpelServer.getHttpConnectionManager());

        } catch (Exception ex) {
            throw new ContextException("Error creating external service! name:" + serviceName +
                    ", port:" + portName, ex);
        }

        // if not SOAP nor HTTP binding
        if (partnerService == null) {
            throw new ContextException("Only SOAP and HTTP binding supported!");
        }

        log.debug("Created external service " + serviceName);
        return partnerService;
    }

    private BPELProcessProxy publishAxisService(ProcessConf processConfiguration, QName serviceName,
                                                String portName)
            throws AxisFault {
        // TODO: Need to fix this to suite multi-tenant environment
        // TODO: There is a problem in this, in this manner we can't have two axis services with
        // same QName
        BPELProcessProxy processProxy = new BPELProcessProxy(processConfiguration, bpelServer,
                serviceName, portName);
        ConfigurationContext tenantConfigCtx =
                getConfigurationContextFromProcessConfiguration(processConfiguration);

        AxisService axisService;
        try {
            axisService = AxisServiceUtils.createAxisService(tenantConfigCtx.getAxisConfiguration(),
                    processProxy);

            EndpointConfiguration endpointConfig =
                ((ProcessConfigurationImpl) processConfiguration).getEndpointConfiguration(
                        new WSDL11Endpoint(serviceName, portName));

            ServiceConfigurationUtil.configureService(axisService, endpointConfig, tenantConfigCtx);

        } catch (AxisFault e) {
            log.error("Error occurred creating the axis service " + serviceName.toString());
            throw new DeploymentException("BPEL Package deployment failed.", e);
        }

        processProxy.setAxisService(axisService);
        removeBPELProcessProxyAndAxisService(processConfiguration.getDeployer(), serviceName, portName);
        services.put(processConfiguration.getDeployer(), serviceName, portName, processProxy);

        ArrayList<AxisService> serviceList = new ArrayList<AxisService>();
        serviceList.add(axisService);
        DeploymentEngine.addServiceGroup(createServiceGroupForService(axisService), serviceList,
                null, null, tenantConfigCtx.getAxisConfiguration());
//
        if (log.isDebugEnabled()) {
            log.debug("BPELProcessProxy created for process " + processConfiguration.getProcessId());
            log.debug("AxisService " + serviceName + " created for BPEL process " +
                    processConfiguration.getProcessId());
        }

        return processProxy;
    }

    private AxisServiceGroup createServiceGroupForService(AxisService svc) throws AxisFault {
        AxisServiceGroup svcGroup = new AxisServiceGroup();
        svcGroup.setServiceGroupName(svc.getName());
        svcGroup.addService(svc);
        // Checking configured using files param is not a good solution. We must figure out a way to handle this
        // at Carbon persistence manager layer.
        if (svc.getParameter(CarbonConstants.PRESERVE_SERVICE_HISTORY_PARAM) != null &&
                svc.getParameter(BusinessProcessConstants.CONFIGURED_USING_BPEL_PKG_CONFIG_FILES) == null) {
            svcGroup.addParameter(new Parameter(CarbonConstants.PRESERVE_SERVICE_HISTORY_PARAM, "true"));
        }

        return svcGroup;
    }

    private BPELProcessProxy getBPELProcessProxy(String processDeployer, QName serviceName, String portName) {
        return (BPELProcessProxy) services.get(processDeployer, serviceName, portName);
    }

    private void removeBPELProcessProxyAndAxisService(
            String processDeployer,
            QName serviceName,
            String portName) {

        if (log.isDebugEnabled()) {
            log.debug("Removing service " + serviceName.toString() + " port " + portName);
        }
        BPELProcessProxy processProxy = (BPELProcessProxy) services.remove(processDeployer, serviceName, portName);
        if (processProxy != null) {
            try {
                String axisServiceName = processProxy.getAxisService().getName();
                AxisConfiguration axisConfig = processProxy.getAxisService().getAxisConfiguration();
                ///////////////////////////////////////////////////////////////////////////////////////////////////////
                // There is a issue in this code due to tenant unloading and loading. When we unload a tenant we don't
                // undeploy the process. So BPELBindingContextImpl#deactivateMyRoleEndpoint will not get invoked and
                // BPELProcessProxy instance will be there in the BPELBindingContextImpl#services map. So at the
                // time we load the tenant again, axisService returned from axisConfig will be null even though we
                // have a BPELProcessProxy instance in service map. This is because teant unloading logic cleans the
                // axis configuration. So if the axisService is null, we ignore the other steps after this.
                ///////////////////////////////////////////////////////////////////////////////////////////////////////
                AxisService axisService = axisConfig.getService(axisServiceName);
                if (axisService != null) {
                    // first, de-allocate its schemas
                    axisService.releaseSchemaList();
                    // then, de-allocate its parameters
                    // the service's wsdl object model is stored as one of its parameters!
                    // can't stress strongly enough how important it is to clean this up.
//                ArrayList<Parameter> parameters = (ArrayList<Parameter>) axisService.getParameters();
//                for (Parameter parameter : parameters) {
//                    if (!parameter.getName().equals(CarbonConstants.PRESERVE_SERVICE_HISTORY_PARAM) && !BPELProcessStoreImpl.isUndeploying) {
//                        axisService.removeParameter(parameter);
//                    }
//                }
//
//                if (RegistryBasedProcessStoreImpl.isUndeploying) {
//                    BPSServerImpl.getAxisConfig().getServiceGroup(axisServiceName).removeParameter(
//                            new Parameter(CarbonConstants.PRESERVE_SERVICE_HISTORY_PARAM, "true"));
//                }
//
//                if (RegistryBasedProcessStoreImpl.isUpdatingBPELPackage) {
//                    axisService.addParameter(new Parameter(CarbonConstants.PRESERVE_SERVICE_HISTORY_PARAM, "true"));
//                    axisService.getAxisServiceGroup().addParameter(new Parameter(CarbonConstants.PRESERVE_SERVICE_HISTORY_PARAM, "true"));
//                }
                    // now, stop the service
                    axisConfig.stopService(axisServiceName);
                    // calling removeServiceGroup() is workaround to AXIS2-4314.
                    //  It happens that Axis2 creates one group per service you add with AxisConfiguration.addService().
                    // See this.createService()
                    // Once this issue is fixed (hopully in Axis2-1.5), we can use removeService() again.
                    axisConfig.removeServiceGroup(axisServiceName);
                    // This must not done. This will cause axis configuration to cleanup deployment schedular
                    // and it'll throw exception when it tries to reschedule deployer after undeplying.
                    //_server._axisConfig.cleanup();
                }
            } catch (AxisFault axisFault) {
                log.error("Couldn't remove service " + serviceName);
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Couldn't find service " + serviceName + " port " + portName + " to remove.");
            }
        }
    }

    private ConfigurationContext getConfigurationContextFromProcessConfiguration(ProcessConf processConf) {
        if (processConf instanceof ProcessConfigurationImpl) {
            return ((ProcessConfigurationImpl) processConf)
                    .getTenantConfigurationContext();
        }
        throw new RuntimeException("ProcessConf implementatoin type mismatch. " +
                "ProcessConf implentation should be a instance of" +
                " org.wso2.carbon.bpel.core.ode.integration.store.ProcessConfigurationImpl.");
    }

    private void updateServiceList(int tenantId, Endpoint myRoleEndpoint, STATE state) {
        TenantProcessStore tenantProcessStore = bpelServer.getMultiTenantProcessStore().
                getTenantsProcessStore(tenantId);
        if (tenantProcessStore == null) {
            throw new RuntimeException("TenantProcessStore null for tenant " + tenantId + ".");
        }
        switch (state) {
            case ADD:
                if (log.isDebugEnabled()) {
                    log.debug("Adding published service information for service: " + myRoleEndpoint.serviceName);
                }
                tenantProcessStore.getDeployedServices().put(myRoleEndpoint.serviceName, new Object());
                break;
            case REMOVE:
                if (log.isDebugEnabled()) {
                    log.debug("Removing published service information for service: " + myRoleEndpoint.serviceName);
                }
                tenantProcessStore.getDeployedServices().remove(myRoleEndpoint.serviceName);
                break;
        }

    }

    private static enum STATE {
        ADD,
        REMOVE
    }
}
