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

package org.wso2.carbon.bpel.core.ode.integration.mgt.services;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.TransportInDescription;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.collections.comparators.ComparatorChain;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.common.Filter;
import org.apache.ode.bpel.common.InstanceFilter;
import org.apache.ode.bpel.common.ProcessFilter;
import org.apache.ode.bpel.dao.BpelDAOConnection;
import org.apache.ode.bpel.dao.ProcessDAO;
import org.apache.ode.bpel.dao.ProcessInstanceDAO;
import org.apache.ode.bpel.dd.TDeployment;
import org.apache.ode.bpel.dd.TInvoke;
import org.apache.ode.bpel.dd.TProcessEvents;
import org.apache.ode.bpel.dd.TProvide;
import org.apache.ode.bpel.dd.TService;
import org.apache.ode.bpel.engine.BpelDatabase;
import org.apache.ode.bpel.evt.BpelEvent;
import org.apache.ode.bpel.iapi.ProcessConf;
import org.apache.ode.bpel.iapi.ProcessState;
import org.apache.ode.bpel.pmapi.ProcessInfoCustomizer;
import org.apache.ode.bpel.pmapi.TInstanceStatus;
import org.apache.ode.utils.DOMUtils;
import org.apache.ode.utils.ISO8601DateParser;
import org.apache.ode.utils.stl.CollectionsX;
import org.apache.ode.utils.stl.MemberOfFunction;
import org.w3c.dom.Node;
import org.wso2.carbon.bpel.core.ode.integration.BPELServerImpl;
import org.wso2.carbon.bpel.core.ode.integration.store.ProcessConfigurationImpl;
import org.wso2.carbon.bpel.core.ode.integration.store.TenantProcessStoreImpl;
import org.wso2.carbon.bpel.core.ode.integration.utils.AdminServiceUtils;
import org.wso2.carbon.bpel.skeleton.ode.integration.mgt.services.ProcessManagementException;
import org.wso2.carbon.bpel.skeleton.ode.integration.mgt.services.ProcessManagementServiceSkeletonInterface;
import org.wso2.carbon.bpel.skeleton.ode.integration.mgt.services.types.BpelDefinition;
import org.wso2.carbon.bpel.skeleton.ode.integration.mgt.services.types.CategoryListType;
import org.wso2.carbon.bpel.skeleton.ode.integration.mgt.services.types.Category_type1;
import org.wso2.carbon.bpel.skeleton.ode.integration.mgt.services.types.CleanUpListType;
import org.wso2.carbon.bpel.skeleton.ode.integration.mgt.services.types.CleanUpType;
import org.wso2.carbon.bpel.skeleton.ode.integration.mgt.services.types.DefinitionInfo;
import org.wso2.carbon.bpel.skeleton.ode.integration.mgt.services.types.DeploymentInfo;
import org.wso2.carbon.bpel.skeleton.ode.integration.mgt.services.types.EnableEventListType;
import org.wso2.carbon.bpel.skeleton.ode.integration.mgt.services.types.EndpointRef_type0;
import org.wso2.carbon.bpel.skeleton.ode.integration.mgt.services.types.EndpointReferencesType;
import org.wso2.carbon.bpel.skeleton.ode.integration.mgt.services.types.FailuresInfo;
import org.wso2.carbon.bpel.skeleton.ode.integration.mgt.services.types.Generate_type1;
import org.wso2.carbon.bpel.skeleton.ode.integration.mgt.services.types.InstanceStatus;
import org.wso2.carbon.bpel.skeleton.ode.integration.mgt.services.types.InstanceSummary;
import org.wso2.carbon.bpel.skeleton.ode.integration.mgt.services.types.Instances_type0;
import org.wso2.carbon.bpel.skeleton.ode.integration.mgt.services.types.InvokeServiceListType;
import org.wso2.carbon.bpel.skeleton.ode.integration.mgt.services.types.InvokedServiceType;
import org.wso2.carbon.bpel.skeleton.ode.integration.mgt.services.types.MexInterpreterListType;
import org.wso2.carbon.bpel.skeleton.ode.integration.mgt.services.types.On_type1;
import org.wso2.carbon.bpel.skeleton.ode.integration.mgt.services.types.PaginatedProcessInfoList;
import org.wso2.carbon.bpel.skeleton.ode.integration.mgt.services.types.ProcessDeployDetailsList;
import org.wso2.carbon.bpel.skeleton.ode.integration.mgt.services.types.ProcessDeployDetailsList_type0;
import org.wso2.carbon.bpel.skeleton.ode.integration.mgt.services.types.ProcessEventsListType;
import org.wso2.carbon.bpel.skeleton.ode.integration.mgt.services.types.ProcessInfoType;
import org.wso2.carbon.bpel.skeleton.ode.integration.mgt.services.types.ProcessProperties;
import org.wso2.carbon.bpel.skeleton.ode.integration.mgt.services.types.ProcessProperty_type0;
import org.wso2.carbon.bpel.skeleton.ode.integration.mgt.services.types.ProcessStatus;
import org.wso2.carbon.bpel.skeleton.ode.integration.mgt.services.types.PropertyListType;
import org.wso2.carbon.bpel.skeleton.ode.integration.mgt.services.types.Property_type0;
import org.wso2.carbon.bpel.skeleton.ode.integration.mgt.services.types.ProvideServiceListType;
import org.wso2.carbon.bpel.skeleton.ode.integration.mgt.services.types.ProvidedServiceType;
import org.wso2.carbon.bpel.skeleton.ode.integration.mgt.services.types.ScopeEventListType;
import org.wso2.carbon.bpel.skeleton.ode.integration.mgt.services.types.ScopeEventType;
import org.wso2.carbon.bpel.skeleton.ode.integration.mgt.services.types.ServiceLocation;
import org.wso2.carbon.bpel.skeleton.ode.integration.mgt.services.types.Service_type0;
import org.wso2.carbon.bpel.skeleton.ode.integration.mgt.services.types.Service_type1;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.core.transports.http.HttpTransportListener;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.NetworkUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.SocketException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 * BPEL Process management service implementation.
 */
public class ProcessManagementServiceSkeleton extends AbstractAdmin
        implements ProcessManagementServiceSkeletonInterface {

    private static boolean isServletTransport = false;
    private static boolean isServletTransportSet = false;
    private static Log log = LogFactory.getLog(ProcessManagementServiceSkeleton.class);
    private BPELServerImpl bpelServer = BPELServerImpl.getInstance();
    private BpelDatabase bpelDb = bpelServer.getODEBPELServer().getBpelDb();


    public PaginatedProcessInfoList getPaginatedProcessList(String processListFilter,
                                                            String processListOrderByKey,
                                                            int page)
            throws ProcessManagementException {
        int tPage = page;

        PaginatedProcessInfoList processList = new PaginatedProcessInfoList();
        TenantProcessStoreImpl tenantProcessStore = AdminServiceUtils.getTenantProcessStore();

        if (tPage < 0 || tPage == Integer.MAX_VALUE) {
            tPage = 0;
        }

        Integer itemsPerPage = 10;
        Integer startIndexForCurrentPage = tPage * itemsPerPage;
        Integer endIndexForCurrentPage = (tPage + 1) * itemsPerPage;

        final ProcessFilter processFilter =
                new ProcessFilter(processListFilter, processListOrderByKey);
        Collection<ProcessConf> processListForCurrentPage =
                processQuery(processFilter, tenantProcessStore);
        Integer processListSize = processListForCurrentPage.size();
        Integer pages = (int) Math.ceil((double) processListSize / itemsPerPage);
        processList.setPages(pages);

        ProcessConf[] processConfigurations =
                processListForCurrentPage.toArray(new ProcessConf[processListSize]);

        for (int i = startIndexForCurrentPage;
             (i < endIndexForCurrentPage && i < processListSize); i++) {
            processList.addProcessInfo(AdminServiceUtils.createLimitedProcessInfoObject(processConfigurations[i]));
        }

        return processList;
    }

    public java.lang.String[] getAllProcesses(String getAllProcesses)
            throws ProcessManagementException {
        TenantProcessStoreImpl tenantProcessStore = AdminServiceUtils.getTenantProcessStore();
        Set<QName> processIds = tenantProcessStore.getProcessConfigMap().keySet();
        List<String> pids = new ArrayList<String>();
        for (QName pid : processIds) {
            pids.add(pid.toString());
        }

        return pids.toArray(new String[pids.size()]);
    }

    /* The methods gets data from ProcessConfigurationImpl and display the details
    *  @param  pid
    *  @return processDeployDetailsList
    *
    */
    public ProcessDeployDetailsList_type0 getProcessDeploymentInfo(QName pid) {

        /* Configuring process basic information*/
        ProcessDeployDetailsList processDeployDetailsList = new ProcessDeployDetailsList();
        ProcessDeployDetailsList_type0 processDeployDetailsListType = new ProcessDeployDetailsList_type0();

        TenantProcessStoreImpl tenantProcessStore = AdminServiceUtils.getTenantProcessStore();
        ProcessConf processConf = tenantProcessStore.getProcessConfiguration(pid);
        ProcessConfigurationImpl processConfiguration = (ProcessConfigurationImpl) processConf;

        QName processId = processConfiguration.getProcessId();
        processDeployDetailsListType.setProcessName(processId);
        ProcessStatus processStatus =
                ProcessStatus.Factory.fromValue(processConfiguration.getState().name());
        processDeployDetailsListType.setProcessState(processStatus);
        processDeployDetailsListType.setIsInMemory(processConfiguration.isTransient());

        /* Configuring invoked services by the process*/
        List<TInvoke> invokeList = processConfiguration.getInvokedServices();
        if (invokeList != null) {

            InvokeServiceListType ist = new InvokeServiceListType();

            for (TInvoke invoke : invokeList) {

                InvokedServiceType invokedServiceType = new InvokedServiceType();

                Service_type1 service = new Service_type1();
                service.setName(invoke.getService().getName());
                service.setPort(invoke.getService().getPort());

                invokedServiceType.setService(service);
                invokedServiceType.setPartnerLink(invoke.getPartnerLink());

                ist.addInvokedService(invokedServiceType);
                processDeployDetailsListType.setInvokeServiceList(ist);
            }
        }

        /* Configuring providing services by the process*/
        List<TProvide> provideList = processConfiguration.getProvidedServices();
        if (provideList != null) {

            ProvideServiceListType pst = new ProvideServiceListType();

            for (TProvide provide : provideList) {

                ProvidedServiceType providedServiceType = new ProvidedServiceType();

                Service_type0 service = new Service_type0();
                service.setName(provide.getService().getName());
                service.setPort(provide.getService().getPort());

                providedServiceType.setService(service);

                providedServiceType.setPartnerLink(provide.getPartnerLink());

                pst.addProvidedService(providedServiceType);

            }
            processDeployDetailsListType.setProvideServiceList(pst);
        }

        /* Configuring message exchange interceptors of the process*/
        MexInterpreterListType mxt = new MexInterpreterListType();
        List<String> mexInterceptor = processConfiguration.getMexInterceptors();
        if (mexInterceptor != null) {

            for (String mexInt : mexInterceptor) {

                mxt.addMexinterpreter(mexInt);
            }

        }
        processDeployDetailsListType.setMexInterperterList(mxt);

        /* Configuring process level and scope level enabled events of process*/
        Map<String, Set<BpelEvent.TYPE>> eventsMap = processConfiguration.getEvents();

        ProcessEventsListType processEventsListType = new ProcessEventsListType();
        EnableEventListType enableEventListType = new EnableEventListType();
        ScopeEventListType scopeEventListType = new ScopeEventListType();

        for (Map.Entry<String, Set<BpelEvent.TYPE>> eventEntry : eventsMap.entrySet()) {
            if (eventEntry.getKey() != null) {

                ScopeEventType scopeEvent = new ScopeEventType();
                String scopeName = eventEntry.getKey();
                EnableEventListType enableEventList = new EnableEventListType();
                Set<BpelEvent.TYPE> typeSetforScope = eventEntry.getValue();
                for (BpelEvent.TYPE type : typeSetforScope) {
                    enableEventList.addEnableEvent(type.toString());
                }

                scopeEvent.setScope(scopeName);
                scopeEvent.setEnabledEventList(enableEventList);
                scopeEventListType.addScopeEvent(scopeEvent);
            } else {

                Set<BpelEvent.TYPE> typeSet = eventEntry.getValue();
                for (BpelEvent.TYPE aTypeSet : typeSet) {
                    enableEventListType.addEnableEvent(aTypeSet.toString());
                }
            }
        }

        TProcessEvents.Generate.Enum genEnum = processConfiguration.getGenerateType();
        if (genEnum != null) {
            Generate_type1 generate = Generate_type1.Factory.fromValue(genEnum.toString());
            processEventsListType.setGenerate(generate);
        }
        processEventsListType.setEnableEventsList(enableEventListType);
        processEventsListType.setScopeEventsList(scopeEventListType);

        processDeployDetailsListType.setProcessEventsList(processEventsListType);
        // end of process events

        /* configuring properties defined in the process */
        PropertyListType propertyListType = new PropertyListType();
        Map<QName, Node> propertiesMap = processConfiguration.getProcessProperties();
        Set<Map.Entry<QName, Node>> entries = propertiesMap.entrySet();
        for (Map.Entry entry : entries) {
            ProcessProperty_type0 property = new ProcessProperty_type0();
            property.setName((QName) entry.getKey());
            Node node = (Node) entry.getValue();
            property.setValue(DOMUtils.domToStringLevel2(node));
            propertyListType.addProcessProperty(property);
        }

        processDeployDetailsListType.setPropertyList(propertyListType);

        CleanUpListType cleanUpList = new CleanUpListType();
        Set<ProcessConf.CLEANUP_CATEGORY> sucessTypeCleanups = processConfiguration.getCleanupCategories(true);
        Set<ProcessConf.CLEANUP_CATEGORY> failureTypeCleanups = processConfiguration.getCleanupCategories(false);

        if (sucessTypeCleanups != null) {
            CleanUpType cleanUp = new CleanUpType();
            On_type1 onType = On_type1.success;
            cleanUp.setOn(onType);
            CategoryListType categoryListType = new CategoryListType();
            for (ProcessConf.CLEANUP_CATEGORY sCategory : sucessTypeCleanups) {
                Category_type1 categoryType1 = Category_type1.Factory.fromValue(sCategory.name().toLowerCase());
                categoryListType.addCategory(categoryType1);
            }

            cleanUp.setCategoryList(categoryListType);
            cleanUpList.addCleanUp(cleanUp);
        }

        if (failureTypeCleanups != null) {
            CleanUpType cleanUp = new CleanUpType();
            On_type1 onType = On_type1.failure;
            cleanUp.setOn(onType);
            CategoryListType categoryListType = new CategoryListType();
            for (ProcessConf.CLEANUP_CATEGORY fCategory : failureTypeCleanups) {
                Category_type1 categoryType1 = Category_type1.Factory.fromValue(fCategory.name().toLowerCase());
                categoryListType.addCategory(categoryType1);
            }

            cleanUp.setCategoryList(categoryListType);
            cleanUpList.addCleanUp(cleanUp);
        }
        processDeployDetailsListType.setCleanUpList(cleanUpList);


        processDeployDetailsList.setProcessDeployDetailsList(processDeployDetailsListType);
        return processDeployDetailsListType;
    }

    /*When a user modifies deploy info table they are updated in this method
    *
    * @param processDeployDetailsList
    *
    */

    public void updateDeployInfo(ProcessDeployDetailsList_type0 processDeployDetailsListType)
            throws ProcessManagementException {
        final QName processId = processDeployDetailsListType.getProcessName();
        try {

            TenantProcessStoreImpl tenantProcessStore = AdminServiceUtils.getTenantProcessStore();
            ProcessConfigurationImpl processConf =
                    (ProcessConfigurationImpl) tenantProcessStore.getProcessConfiguration(processId);
            final boolean oldIsInmemory = processConf.isTransient();
            final boolean newIsInmemory = processDeployDetailsListType.getIsInMemory();
            processConf.setState(getProcessState(processDeployDetailsListType));
            processConf.setIsTransient(newIsInmemory);
            processConf.setProcessEventsList(processDeployDetailsListType.getProcessEventsList());
            processConf.setGenerateType(processDeployDetailsListType.getProcessEventsList());
            processConf.setProcessCleanupConfImpl(processDeployDetailsListType.getCleanUpList());
            if (tenantProcessStore.getBPELPackageRepository() != null) {
                tenantProcessStore.getBPELPackageRepository().
                        createPropertiesForUpdatedDeploymentInfo(processConf);
            }
            bpelServer.getODEBPELServer().getContexts().scheduler.execTransaction(new java.util.concurrent
                    .Callable<Boolean>() {
                public Boolean call() throws Exception {

                    ProcessDAO processDAO;
                    ProcessDAO newProcessDAO;

                    if (oldIsInmemory & !newIsInmemory) {
                        processDAO = bpelServer.getODEBPELServer().getContexts().getInMemDao().getConnection()
                                .getProcess(processId);
                        if (bpelServer.getODEBPELServer().getContexts().dao.getConnection().getProcess(processId) ==
                                null) {
                            newProcessDAO = bpelServer.getODEBPELServer().getContexts().dao.getConnection()
                                    .createProcess(processDAO.getProcessId(), processDAO.getType(), processDAO
                                            .getGuid(), processDAO.getVersion());

                            Set<String> correlatorsSet = processDAO.getCorrelatorsSet();
                            for (String correlator : correlatorsSet) {
                                newProcessDAO.addCorrelator(correlator);
                            }
                        }
                    } else if (!oldIsInmemory & newIsInmemory) {
                        QName pId = processId;
                        processDAO = bpelServer.getODEBPELServer().getContexts().dao.getConnection().getProcess(pId);
                        if (bpelServer.getODEBPELServer().getContexts().getInMemDao().getConnection().getProcess(pId)
                                == null) {
                            newProcessDAO = bpelServer.getODEBPELServer().getContexts().getInMemDao().getConnection()
                                    .createProcess(processDAO.getProcessId(), processDAO.getType(), processDAO
                                            .getGuid(), processDAO.getVersion());

                            Set<String> correlatorsSet = processDAO.getCorrelatorsSet();
                            for (String correlator : correlatorsSet) {
                                newProcessDAO.addCorrelator(correlator);
                            }
                        }
                    }

                    return true;
                }
            });


        } catch (Exception e) {
            String errMsg = "Error occurred while updating deployment info for: " + processId;
            log.error(errMsg, e);
            throw new ProcessManagementException(errMsg, e);
        }
    }

    public ProcessState getProcessState(ProcessDeployDetailsList_type0 deployDetailsListType) {
        ProcessStatus processStatus = deployDetailsListType.getProcessState();
        return ProcessState.valueOf(processStatus.getValue());
    }

    public void retireProcess(QName pid) throws ProcessManagementException {
        TenantProcessStoreImpl tenantProcessStore = AdminServiceUtils.getTenantProcessStore();
        try {
            tenantProcessStore.setState(pid, ProcessState.RETIRED);
        } catch (Exception e) {
            String errMsg = "Process: " + pid + " retirement failed.";
            log.error(errMsg, e);
            throw new ProcessManagementException(errMsg, e);
        }
    }


    public void activateProcess(QName pid) throws ProcessManagementException {
        TenantProcessStoreImpl tenantProcessStore = AdminServiceUtils.getTenantProcessStore();
        try {
            tenantProcessStore.setState(pid, ProcessState.ACTIVE);
        } catch (Exception e) {
            String errMsg = "Process: " + pid + " activation failed.";
            log.error(errMsg, e);
            throw new ProcessManagementException(errMsg, e);
        }
    }

    public ProcessInfoType getProcessInfo(QName pid) throws ProcessManagementException {
        ProcessInfoType processInfoType = new ProcessInfoType();
        TenantProcessStoreImpl tenantProcessStore = AdminServiceUtils.getTenantProcessStore();
        ProcessConf processConf = tenantProcessStore.getProcessConfiguration(pid);
        fillProcessInfo(processInfoType, processConf, ProcessInfoCustomizer.ALL, tenantProcessStore);

        return processInfoType;
    }

//    private java.lang.String[] getServiceLocationForProcess(QName processId)
//            throws ProcessManagementException {
//        AxisConfiguration axisConf = getConfigContext().getAxisConfiguration();
//        Map<String, AxisService> services = axisConf.getServices();
//        ArrayList<String> serviceEPRs = new ArrayList<String>();
//
//        for (AxisService service : services.values()) {
//            Parameter pIdParam = service.getParameter(BPELConstants.PROCESS_ID);
//            if (pIdParam != null) {
//                if (pIdParam.getValue().equals(processId)) {
//                    serviceEPRs.addAll(Arrays.asList(service.getEPRs()));
//                }
//            }
//        }
//
//        if (serviceEPRs.size() > 0) {
//            return serviceEPRs.toArray(new String[serviceEPRs.size()]);
//        }
//
//        String errMsg = "Cannot find service for process: " + processId;
//        log.error(errMsg);
//        throw new ProcessManagementException(errMsg);
//    }

    private void fillPartnerLinks(ProcessInfoType pInfo, TDeployment.Process processInfo)
            throws ProcessManagementException {
        if (processInfo.getProvideList() != null) {
            EndpointReferencesType eprsType = new EndpointReferencesType();
            for (TProvide provide : processInfo.getProvideList()) {
                String plinkName = provide.getPartnerLink();
                TService service = provide.getService();
                /* NOTE:Service cannot be null for provider partner link*/
                if (service == null) {
                    String errorMsg = "Error in <provide> element for process " +
                            processInfo.getName() + " partnerlink" + plinkName +
                            " did not identify an endpoint";
                    log.error(errorMsg);
                    throw new ProcessManagementException(errorMsg);
                }
                if (log.isDebugEnabled()) {
                    log.debug("Processing <provide> element for process " + processInfo.getName() +
                            ": partnerlink " + plinkName + " --> " + service.getName() + " : " +
                            service.getPort());
                }

                QName serviceName = service.getName();
                EndpointRef_type0 eprType = new EndpointRef_type0();
                eprType.setPartnerLink(plinkName);
                eprType.setService(serviceName);
                ServiceLocation sLocation = new ServiceLocation();
                try {
                    String url = getTryitURL(serviceName.getLocalPart(), getConfigContext());
                    sLocation.addServiceLocation(url);
                    String[] wsdls = getWsdlInformation(serviceName.getLocalPart(),
                            getConfigContext().getAxisConfiguration());
                    if (wsdls.length == 2) {
                        if (wsdls[0].endsWith("?wsdl")) {
                            sLocation.addServiceLocation(wsdls[0]);
                        } else {
                            sLocation.addServiceLocation(wsdls[1]);
                        }
                    }
                } catch (AxisFault axisFault) {
                    String errMsg = "Error while getting try-it url for the service: " + serviceName;
                    log.error(errMsg, axisFault);
                    throw new ProcessManagementException(errMsg, axisFault);
                }
                eprType.setServiceLocations(sLocation);
                eprsType.addEndpointRef(eprType);
            }
            pInfo.setEndpoints(eprsType);
        }

//        if (processInfo.getInvokeList() != null) {
//            for (TInvoke invoke : processInfo.getInvokeList()) {
//                String plinkName = invoke.getPartnerLink();
//                TService service = invoke.getService();
//                /* NOTE:Service can be null for partner links*/
//                if (service == null) {
//                    continue;
//                }
//                if (log.isDebugEnabled()) {
//                    log.debug("Processing <invoke> element for process " + processInfo.getName() + ": partnerlink" +
//                              plinkName + " -->" + service);
//                }
//
//                QName serviceName = service.getName();
//            }
//        }

    }

    /**
     * Query processes based on a {@link org.apache.ode.bpel.common.ProcessFilter} criteria. This is
     * implemented in memory rather than via database calls since the processes
     * are managed by the {@link org.apache.ode.bpel.iapi.ProcessStore} object and we don't want to make
     * this needlessly complicated.
     *
     * @param filter              process filter
     * @param tenantsProcessStore Current Tenant's process store
     * @return ProcessConf collection
     * @throws ProcessManagementException if an error occurred while processing query
     */
    private Collection<ProcessConf> processQuery(ProcessFilter filter,
                                                 TenantProcessStoreImpl tenantsProcessStore)
            throws ProcessManagementException {

        Map<QName, ProcessConfigurationImpl> processes = tenantsProcessStore.getProcessConfigMap();
        if (log.isDebugEnabled()) {
            for (Map.Entry<QName, ProcessConfigurationImpl> process : processes.entrySet()) {
                log.debug("Process " + process.getKey() + " in state " + process.getValue());
            }
        }

        Set<QName> pids = processes.keySet();

        // Name filter can be implemented using only the PIDs.
        if (filter != null && filter.getNameFilter() != null) {
            // adding escape sequences to [\^$.|?*+(){} characters
            String nameFilter = filter.getNameFilter().replace("\\", "\\\\").replace("]", "\\]").
                    replace("[", "\\[").replace("^", "\\^").replace("$", "\\$").replace("|", "\\|").
                    replace("?", "\\?").replace(".", "\\.").replace("+", "\\+").replace("(", "\\(").
                    replace(")", "\\)").replace("{", "\\{").replace("}", "\\}").replace("*", ".*");
            final Pattern pattern = Pattern.compile(nameFilter + "(-\\d*)?");
            CollectionsX.remove_if(pids, new MemberOfFunction<QName>() {
                @Override
                public boolean isMember(QName o) {
                    return !pattern.matcher(o.getLocalPart()).matches();
                }
            });
        }

        if (filter != null && filter.getNamespaceFilter() != null) {
            // adding escape sequences to [\^$.|?*+(){} characters
            String namespaceFilter = filter.getNamespaceFilter().replace("\\", "\\\\").
                    replace("]", "\\]").replace("[", "\\[").replace("^", "\\^").replace("$", "\\$").
                    replace("|", "\\|").replace("?", "\\?").replace(".", "\\.").replace("+", "\\+").
                    replace("(", "\\(").replace(")", "\\)").replace("{", "\\{").replace("}", "\\}").
                    replace("*", ".*");
            final Pattern pattern = Pattern.compile(namespaceFilter);
            CollectionsX.remove_if(pids, new MemberOfFunction<QName>() {
                @Override
                public boolean isMember(QName o) {
                    String ns = o.getNamespaceURI() == null ? "" : o.getNamespaceURI();
                    return !pattern.matcher(ns).matches();
                }

            });
        }

        // Now we need the process conf objects, we need to be
        // careful since someone could have deleted them by now
        List<ProcessConf> confs = new LinkedList<ProcessConf>();
        for (QName pid : pids) {
            ProcessConf pConf = tenantsProcessStore.getProcessConfiguration(pid);
            if (pConf != null) {
                confs.add(pConf);
            }
        }

        if (filter != null) {
            // TODO Implement process status filtering when status will exist
            // Specific filter for deployment date.
            if (filter.getDeployedDateFilter() != null) {
                for (final String ddf : filter.getDeployedDateFilter()) {
                    final Date dd;
                    try {
                        dd = ISO8601DateParser.parse(Filter.getDateWithoutOp(ddf));
                    } catch (ParseException e) {
                        // Should never happen.
                        String errMsg = "Exception while parsing date";
                        log.error(errMsg, e);
                        throw new ProcessManagementException(errMsg, e);
                    }

                    CollectionsX.remove_if(confs, new MemberOfFunction<ProcessConf>() {
                        @Override
                        public boolean isMember(ProcessConf o) {
                            if (ddf.startsWith("=")) {
                                return !o.getDeployDate().equals(dd);
                            }
                            if (ddf.startsWith("<=")) {
                                return o.getDeployDate().getTime() > dd.getTime();
                            }
                            if (ddf.startsWith(">=")) {
                                return o.getDeployDate().getTime() < dd.getTime();
                            }
                            if (ddf.startsWith("<")) {
                                return o.getDeployDate().getTime() >= dd.getTime();
                            }
                            return ddf.startsWith(">") && (o.getDeployDate().getTime() <= dd.getTime());
                        }
                    });
                }
            }

            // Ordering
            if (filter.getOrders() != null) {
                ComparatorChain cChain = new ComparatorChain();
                for (String key : filter.getOrders()) {
                    boolean ascending = true;
                    String orderKey = key;
                    if (key.startsWith("+") || key.startsWith("-")) {
                        orderKey = key.substring(1, key.length());
                        if (key.startsWith("-")) {
                            ascending = false;
                        }
                    }

                    Comparator c;
                    if ("name".equals(orderKey)) {
                        c = new Comparator<ProcessConf>() {
                            public int compare(ProcessConf o1, ProcessConf o2) {
                                return o1.getProcessId().getLocalPart().compareTo(o2.getProcessId().
                                        getLocalPart());
                            }
                        };
                    } else if ("namespace".equals(orderKey)) {
                        c = new Comparator<ProcessConf>() {
                            public int compare(ProcessConf o1, ProcessConf o2) {
                                String ns1 = o1.getProcessId().getNamespaceURI() == null ? "" :
                                        o1.getProcessId().getNamespaceURI();
                                String ns2 = o2.getProcessId().getNamespaceURI() == null ? "" :
                                        o2.getProcessId().getNamespaceURI();
                                return ns1.compareTo(ns2);
                            }
                        };
                    } else if ("version".equals(orderKey)) {
                        c = new Comparator<ProcessConf>() {
                            public int compare(ProcessConf o1, ProcessConf o2) {
                                return (int) (o1.getVersion() - o2.getVersion());
                            }
                        };
                    } else if ("deployed".equals(orderKey)) {
                        c = new Comparator<ProcessConf>() {
                            public int compare(ProcessConf o1, ProcessConf o2) {
                                return o1.getDeployDate().compareTo(o2.getDeployDate());
                            }
                        };
                    } else if ("status".equals(orderKey)) {
                        c = new Comparator<ProcessConf>() {
                            public int compare(ProcessConf o1, ProcessConf o2) {
                                return o1.getState().compareTo(o2.getState());
                            }
                        };
                    } else {
                        // unrecognized
                        if (log.isDebugEnabled()) {
                            log.debug("unrecognized order key" + orderKey);
                        }
                        continue;
                    }

                    cChain.addComparator(c, !ascending);
                }

                Collections.sort(confs, cChain);
            }
        }

        return confs;
    }

    /**
     * Fill in the <code>ProcessInfo</code> element of the transfer object.
     *
     * @param info               destination XMLBean
     * @param pconf              process configuration object (from store)
     * @param custom             used to customize the quantity of information produced in the
     *                           info
     * @param tenantProcessStore Tenant's Process store
     * @throws ProcessManagementException If an error occurred while filling process information
     */
    private void fillProcessInfo(ProcessInfoType info, ProcessConf pconf,
                                 ProcessInfoCustomizer custom,
                                 TenantProcessStoreImpl tenantProcessStore)
            throws ProcessManagementException {
        if (pconf == null) {
            String errMsg = "Process configuration cannot be null.";
            log.error(errMsg);
            throw new ProcessManagementException(errMsg);
        }

        info.setPid(pconf.getProcessId().toString());
        // TODO: ACTIVE and RETIRED should be used separately.
        // Active process may be retired at the same time
        if (pconf.getState() == ProcessState.RETIRED) {
            info.setStatus(ProcessStatus.RETIRED);
            info.setOlderVersion(AdminServiceUtils.isOlderVersion(pconf, tenantProcessStore));
        } else if (pconf.getState() == ProcessState.DISABLED) {
            info.setStatus(ProcessStatus.DISABLED);
            info.setOlderVersion(0);
        } else {
            info.setStatus(ProcessStatus.ACTIVE);
            info.setOlderVersion(0);
        }
        info.setVersion(pconf.getVersion());

        DefinitionInfo defInfo = new DefinitionInfo();
        defInfo.setProcessName(pconf.getType());
        BpelDefinition bpelDefinition = new BpelDefinition();
        bpelDefinition.setExtraElement(getProcessDefinition(pconf));
        defInfo.setDefinition(bpelDefinition);

        info.setDefinitionInfo(defInfo);

        DeploymentInfo depInfo = new DeploymentInfo();
        depInfo.setPackageName(pconf.getPackage());
        depInfo.setDocument(pconf.getBpelDocument());
        depInfo.setDeployDate(AdminServiceUtils.toCalendar(pconf.getDeployDate()));
        // TODO: Need to fix this by adding info to process conf.
        depInfo.setDeployer(org.wso2.carbon.bpel.core.BPELConstants.BPEL_DEPLOYER_NAME);
        info.setDeploymentInfo(depInfo);

        if (custom.includeInstanceSummary()) {
            InstanceSummary instanceSummary = new InstanceSummary();
            addInstanceSummaryEntry(instanceSummary, pconf, InstanceStatus.ACTIVE);
            addInstanceSummaryEntry(instanceSummary, pconf, InstanceStatus.COMPLETED);
            addInstanceSummaryEntry(instanceSummary, pconf, InstanceStatus.FAILED);
            addInstanceSummaryEntry(instanceSummary, pconf, InstanceStatus.SUSPENDED);
            addInstanceSummaryEntry(instanceSummary, pconf, InstanceStatus.TERMINATED);
            addFailuresToInstanceSummary(instanceSummary, pconf);

            info.setInstanceSummary(instanceSummary);

        }

        if (custom.includeProcessProperties()) {
            ProcessProperties processProps = new ProcessProperties();
            for (Map.Entry<QName, Node> propEntry : pconf.getProcessProperties().entrySet()) {
                QName key = propEntry.getKey();
                if (key != null) {
                    Property_type0 prop = new Property_type0();
                    prop.setName(new QName(key.getNamespaceURI(), key.getLocalPart()));
                    OMFactory omFac = OMAbstractFactory.getOMFactory();
                    OMElement propEle = omFac.createOMElement("PropertyValue", null);
                    propEle.setText(propEntry.getValue().getNodeValue());
                    prop.addExtraElement(propEle);
                    processProps.addProperty(prop);
                }
            }
            info.setProperties(processProps);
        }

        fillPartnerLinks(info, ((ProcessConfigurationImpl) pconf).getProcessDeploymentInfo());
    }

    private void addInstanceSummaryEntry(InstanceSummary instSum, ProcessConf pconf,
                                         InstanceStatus state) throws ProcessManagementException {
        Instances_type0 instances = new Instances_type0();
        instances.setState(state);
        String queryStatus = InstanceFilter.StatusKeys.valueOf(state.toString()).toString().
                toLowerCase();
        final InstanceFilter instanceFilter = new InstanceFilter("status=" + queryStatus
                + " pid=" + pconf.getProcessId());

        int count = dbexec(new BpelDatabase.Callable<Integer>() {

            public Integer run(BpelDAOConnection conn) throws Exception {
                return conn.instanceCount(instanceFilter).intValue();
            }
        });
        instances.setCount(count);
        instSum.addInstances(instances);
    }

    private void addFailuresToInstanceSummary(final InstanceSummary instSum, ProcessConf pconf)
            throws ProcessManagementException {
        final FailuresInfo failureInfo = new FailuresInfo();
        String queryStatus = InstanceFilter.StatusKeys.valueOf(TInstanceStatus.ACTIVE.toString()).
                toString().toLowerCase();
        final InstanceFilter instanceFilter = new InstanceFilter("status=" + queryStatus
                + " pid=" + pconf.getProcessId());
        dbexec(new BpelDatabase.Callable<Void>() {
            public Void run(BpelDAOConnection conn) throws Exception {
                Date lastFailureDt = null;
                int failureInstances = 0;
                for (ProcessInstanceDAO instance : conn.instanceQuery(instanceFilter)) {
                    int count = instance.getActivityFailureCount();
                    if (count > 0) {
                        ++failureInstances;
                        Date failureDt = instance.getActivityFailureDateTime();
                        if (lastFailureDt == null || lastFailureDt.before(failureDt)) {
                            lastFailureDt = failureDt;
                        }
                    }
                }
                if (failureInstances > 0) {
                    failureInfo.setCount(failureInstances);
                    failureInfo.setFailureDate(AdminServiceUtils.toCalendar(lastFailureDt));
                    instSum.setFailures(failureInfo);
                }
                return null;
            }
        });
    }

    /**
     * Execute a database transaction, unwrapping nested
     * {@link org.apache.ode.bpel.pmapi.ManagementException}s.
     *
     * @param callable action to run
     * @return object of type T
     * @throws ProcessManagementException if exception occurred during transaction
     */
    protected <T> T dbexec(BpelDatabase.Callable<T> callable) throws ProcessManagementException {
        try {
            return bpelDb.exec(callable);
        } catch (Exception ex) {
            String errMsg = "Exception during database operation ";
            log.error(errMsg, ex);
            throw new ProcessManagementException(errMsg, ex);
        }
    }

    private OMElement getProcessDefinition(ProcessConf pConf) throws ProcessManagementException {
        if (pConf == null) {
            String errMsg = "Process configuration cannot be null.";
            log.error(errMsg);
            throw new ProcessManagementException(errMsg);
        }

        String bpelDoc = pConf.getBpelDocument();
        List<File> files = pConf.getFiles();

        for (final File file : files) {
            if (file.getPath().endsWith(bpelDoc) ||
                    file.getPath().endsWith(bpelDoc.replaceAll("/", "\\\\"))) {
                XMLStreamReader reader;
                FileInputStream fis = null;
                OMElement bpelDefinition;
                try {
                    fis = new FileInputStream(file);
                    XMLInputFactory xif = XMLInputFactory.newInstance();
                    reader = xif.createXMLStreamReader(fis);
                    StAXOMBuilder builder = new StAXOMBuilder(reader);
                    bpelDefinition = builder.getDocumentElement();
                    bpelDefinition.build();
                } catch (XMLStreamException e) {
                    String errMsg = "XML stream reader exception: " + file.getAbsolutePath();
                    log.error(errMsg, e);
                    throw new ProcessManagementException(errMsg, e);
                } catch (FileNotFoundException e) {
                    String errMsg = "BPEL File reading exception: " + file.getAbsolutePath();
                    log.error(errMsg, e);
                    throw new ProcessManagementException(errMsg, e);
                } finally {
                    if (fis != null) {
                        try {
                            fis.close();
                        } catch (IOException e) {
                            log.warn("Cannot close file input stream.", e);
                        }
                    }
                }

                return bpelDefinition;
            }
        }

        String errMsg = "Process Definition for: " + pConf.getProcessId() + " not found";
        log.error(errMsg);
        throw new ProcessManagementException(errMsg);
    }

    private static String getTryitURL(String serviceName,
                                     ConfigurationContext configurationContext)
            throws AxisFault {
        AxisConfiguration axisConfig = configurationContext.getAxisConfiguration();
        // If axis2 uses the servlet transport then we could use the prefix of its endpoint URL to
        // determine the tryit url
        String wsdlURL = getWsdlInformation(serviceName, axisConfig)[0];
        String tryitPrefix = wsdlURL.substring(0, wsdlURL.length() - serviceName.length() - 5);
        if (!isServletTransport(axisConfig)) {
            int tenantIndex = tryitPrefix.indexOf("/t/");
            if (tenantIndex != -1) {
                String tmpTryitPrefix = tryitPrefix.substring(
                        tryitPrefix.substring(0, tryitPrefix.indexOf("/t/")).lastIndexOf("/"));
                //Check if the  Webapp context root of WSO2 Carbon is set.
                tryitPrefix = tryitPrefix.replaceFirst("//", "");
                if (tryitPrefix.substring(0, tryitPrefix.indexOf("/services/")).lastIndexOf("/") > -1) {
                    tryitPrefix = tryitPrefix.substring(
                            tryitPrefix.substring(0, tryitPrefix.indexOf("/services/")).lastIndexOf("/"));
                } else {
                    tryitPrefix = tmpTryitPrefix;
                }

            } else {
                tryitPrefix = configurationContext.getServiceContextPath() + "/";
            }
        }
        return CarbonUtils.getProxyContextPath(false) + tryitPrefix + serviceName + "?tryit";
    }

    private static String[] getWsdlInformation(String serviceName,
                                               AxisConfiguration axisConfig) throws AxisFault {
        String ip;
        try {
            ip = NetworkUtils.getLocalHostname();
        } catch (SocketException e) {
            throw new AxisFault("Cannot get local host name", e);
        }

        //TODO Ideally, The transport on which wsdls are displayed, should be configurable.
        TransportInDescription transportInDescription = axisConfig.getTransportIn("http");

        if (transportInDescription == null) {
            transportInDescription = axisConfig.getTransportIn("https");
        }

        if (transportInDescription != null) {
            EndpointReference[] epr =
                    transportInDescription.getReceiver().getEPRsForService(serviceName, ip);
            String wsdlUrlPrefix = epr[0].getAddress();
            if (wsdlUrlPrefix.endsWith("/")) {
                wsdlUrlPrefix = wsdlUrlPrefix.substring(0, wsdlUrlPrefix.length() - 1);
            }
            return new String[]{wsdlUrlPrefix + "?wsdl", wsdlUrlPrefix + "?wsdl2"};
        }
        return new String[]{};
    }

    /**
     * A utility method to check whether Axis2 uses the Servlet transport or the NIO transport
     *
     * @param axisConfig
     * @return
     */
    private static boolean isServletTransport(AxisConfiguration axisConfig) {
        if (!isServletTransportSet) {
            TransportInDescription transportInDescription = axisConfig.getTransportIn("http");
            if (transportInDescription == null) {
                transportInDescription = axisConfig.getTransportIn("https");
            }

            if (transportInDescription != null && transportInDescription.getReceiver()
                    instanceof HttpTransportListener) {
                isServletTransport = true;
            }

            isServletTransportSet = true;
        }
        return isServletTransport;
    }
}
