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

package org.wso2.carbon.bpel.core.ode.integration.store;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.collections.map.MultiKeyMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.activityRecovery.FailureHandlingDocument;
import org.apache.ode.bpel.dd.TCleanup;
import org.apache.ode.bpel.dd.TDeployment;
import org.apache.ode.bpel.dd.TInvoke;
import org.apache.ode.bpel.dd.TMexInterceptor;
import org.apache.ode.bpel.dd.TProcessEvents;
import org.apache.ode.bpel.dd.TProvide;
import org.apache.ode.bpel.dd.TSchedule;
import org.apache.ode.bpel.dd.TScopeEvents;
import org.apache.ode.bpel.dd.TService;
import org.apache.ode.bpel.evt.BpelEvent;
import org.apache.ode.bpel.iapi.ContextException;
import org.apache.ode.bpel.iapi.Endpoint;
import org.apache.ode.bpel.iapi.EndpointReference;
import org.apache.ode.bpel.iapi.EndpointReferenceContext;
import org.apache.ode.bpel.iapi.ProcessConf;
import org.apache.ode.bpel.iapi.ProcessState;
import org.apache.ode.bpel.iapi.Scheduler;
import org.apache.ode.bpel.o.OFailureHandling;
import org.apache.ode.store.DeploymentUnitDir;
import org.apache.ode.store.ProcessCleanupConfImpl;
import org.apache.ode.utils.CronExpression;
import org.apache.ode.utils.DOMUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.wso2.carbon.bpel.common.BusinessProcessConstants;
import org.wso2.carbon.bpel.common.config.EndpointConfiguration;
import org.wso2.carbon.bpel.core.BPELConstants;
import org.wso2.carbon.bpel.core.ode.integration.config.EndpointConfigBuilder;
import org.wso2.carbon.bpel.core.ode.integration.config.PackageConfiguration;
import org.wso2.carbon.bpel.skeleton.ode.integration.mgt.services.types.CategoryListType;
import org.wso2.carbon.bpel.skeleton.ode.integration.mgt.services.types.Category_type1;
import org.wso2.carbon.bpel.skeleton.ode.integration.mgt.services.types.CleanUpListType;
import org.wso2.carbon.bpel.skeleton.ode.integration.mgt.services.types.CleanUpType;
import org.wso2.carbon.bpel.skeleton.ode.integration.mgt.services.types.EnableEventListType;
import org.wso2.carbon.bpel.skeleton.ode.integration.mgt.services.types.ProcessEventsListType;
import org.wso2.carbon.bpel.skeleton.ode.integration.mgt.services.types.ScopeEventListType;
import org.wso2.carbon.bpel.skeleton.ode.integration.mgt.services.types.ScopeEventType;
import org.wso2.carbon.unifiedendpoint.core.UnifiedEndpoint;
import org.wso2.carbon.unifiedendpoint.core.UnifiedEndpointConstants;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.wsdl.Definition;
import javax.wsdl.Service;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

/**
 * The implementation for Process Configuration.
 * ODE engine itself uses this as the implementation of ProcessConf.
 * Multitenancy is introduced through tenant's ConfigurationContext
 */
public class ProcessConfigurationImpl implements ProcessConf, MultiTenantProcessConfiguration {

    public static final String B4P_NAMESPACE = "http://docs.oasis-open.org/ns/bpel4people/bpel4people/200803";

    private static final Log log = LogFactory.getLog(ProcessConfigurationImpl.class);

    // Tenant's configuration context. Used to publish services into correct
    // AxisConfiguration.
    private ConfigurationContext tenatConfigurationContext;

    private final Date deployDate;

    private final Map<QName, Node> properties;

    private final Map<String, Endpoint> partnerRoleInitialValues = new HashMap<String, Endpoint>();

    private final Map<String, PartnerRoleConfig> partnerRoleConfigurations =
            new HashMap<String, PartnerRoleConfig>();

    private final Map<String, Endpoint> myRoleEndpoints = new HashMap<String, Endpoint>();

    private final List<QName> sharedServices = new ArrayList<QName>();

    private final Map<String, Set<BpelEvent.TYPE>> events = new HashMap<String, Set<BpelEvent.TYPE>>(); //process events

    private final List<String> mexInterceptors = new ArrayList<String>();

    private final DeploymentUnitDir du;

    private ProcessState state;                //active / retire/disabled

    private final TDeployment.Process processInfo;

    private long version = 0;

    private QName processId;

    private QName type;

    private List<TInvoke> invokedServices = new ArrayList<TInvoke>();   //invoke services

    private List<TProvide> providedServices = new ArrayList<TProvide>(); // provide services


    // cache the inMemory flag because XMLBeans objects are heavily synchronized (guarded by a coarse-grained lock)
    private volatile boolean inMemory = false;

    private EndpointReferenceContext eprContext;

    private ProcessCleanupConfImpl processCleanupConfImpl;  // NOTE: final tag is removed from cleanuplist  set the
    // category list with  true/false

    private PackageConfiguration bpelPackageConfiguration = new PackageConfiguration();

    private Integer tenantId = -1;

    private boolean undeploying = false;

    private String deployer = "";

    private boolean isB4PTaskIncluded = false;

    //This attribute has been introduced in-order create a BPELDeploymentContext from a ProcessConfigurationImpl
    private String absolutePathForBpelArchive;

    public String getAbsolutePathForBpelArchive() {
        return absolutePathForBpelArchive;
    }

    public void setAbsolutePathForBpelArchive(String absolutePathForBpelArchive) {
        this.absolutePathForBpelArchive = absolutePathForBpelArchive;
    }

    private TProcessEvents.Generate.Enum generateType = null;

    public ProcessConfigurationImpl(Integer tenantId,
                                    TDeployment.Process processDescriptor,
                                    DeploymentUnitDir du,
                                    Date deployDate,
                                    EndpointReferenceContext eprContext,
                                    ConfigurationContext tenantConfigContext) {
        this.deployDate = (Date) deployDate.clone();
        this.type = Utils.getProcessType(processDescriptor);
        this.du = du;
        this.processInfo = processDescriptor;
        this.version = du.getVersion();
        this.processId = Utils.toPid(processDescriptor.getName(), version);
        this.eprContext = eprContext;
        this.state = Utils.calcInitialState(processDescriptor);
        this.properties = Collections.unmodifiableMap(
                Utils.calcInitialProperties(du.getProperties(), processDescriptor));
        this.inMemory = processDescriptor.isSetInMemory() && processDescriptor.getInMemory();
//        this.inMemory = (processDescriptor.isSetInMemory() && processDescriptor.getInMemory()) ||
//                !processDescriptor.isSetInMemory() ;
        this.tenatConfigurationContext = tenantConfigContext;
//        this.tenantId = MultitenantUtils.getTenantId(tenantConfigContext);
        this.tenantId = tenantId;
        this.deployer = tenantId.toString();
        //TODO readPackageConfiguration() and initPartnerLinks() should be merged 
        readPackageConfiguration();
        initPartnerLinks();
        initMexInterceptors();
        initEventList();

        this.processCleanupConfImpl = new ProcessCleanupConfImpl(processDescriptor);

        initSchedules();

    }

    public Integer getTenantId() {
        return tenantId;
    }

    public ConfigurationContext getTenantConfigurationContext() {
        return tenatConfigurationContext;
    }

    public QName getProcessId() {
        return processId;
    }

    public QName getType() {
        return processInfo.getType() == null ? type : processInfo.getType();
    }

    public long getVersion() {
        return version;
    }

    public boolean isTransient() {
        return inMemory;
    }

    public InputStream getCBPInputStream() {
        DeploymentUnitDir.CBPInfo cbpInfo = du.getCBPInfo(getType());
        if (cbpInfo == null) {
            throw new ContextException("CBP record not found for type " + getType());
        }
        try {
            return new FileInputStream(cbpInfo.getCbp());
        } catch (FileNotFoundException e) {
            throw new ContextException("File Not Found: " + cbpInfo.getCbp(), e);
        }
    }

    public long getCBPFileSize() {
        DeploymentUnitDir.CBPInfo cbpInfo = du.getCBPInfo(getType());
        if (cbpInfo == null) {
            throw new ContextException("CBP record not found for type " + getType());
        }
        return cbpInfo.getCbp().length();
    }

    public String getBpelDocument() {
        DeploymentUnitDir.CBPInfo cbpInfo = du.getCBPInfo(getType());
        if (cbpInfo == null) {
            throw new ContextException("CBP record not found for type " + getType());
        }
        try {
            String relative = getRelativePath(du.getDeployDir(), cbpInfo.getCbp()).
                    replaceAll("\\\\", "/");
            if (!relative.endsWith(BPELConstants.BPEL_COMPILED_FILE_EXTENSION)) {
                throw new ContextException("CBP file must end with " +
                        BPELConstants.BPEL_COMPILED_FILE_EXTENSION + " suffix: " + cbpInfo.getCbp());
            }
            relative = relative.replace(BPELConstants.BPEL_COMPILED_FILE_EXTENSION,
                    BPELConstants.BPEL_FILE_EXTENSION);
            File bpelFile = new File(du.getDeployDir(), relative);
            if (!bpelFile.exists()) {
                log.warn("BPEL file does not exist: " + bpelFile);
            }
            return relative;
        } catch (IOException e) {
            throw new ContextException("IOException in getBpelRelativePath: " + cbpInfo.getCbp(), e);
        }
    }

    private String getRelativePath(File base, File path) throws IOException {
        String basePath = base.getCanonicalPath();
        String cbpPath = path.getCanonicalPath();
        if (!cbpPath.startsWith(basePath)) {
            throw new IOException("Invalid relative path: base=" + base + " path=" + path);
        }
        String relative = cbpPath.substring(basePath.length());
        if (relative.startsWith(File.separator)) {
            relative = relative.substring(1);
        }
        return relative;
    }

    public URI getBaseURI() {
        return du.getDeployDir().toURI();
    }

    public Date getDeployDate() {
        return (Date) deployDate.clone();
    }

    public String getDeployer() {
        return deployer;
    }

    public ProcessState getState() {
        return state;
    }

    public List<File> getFiles() {
        return du.allFiles();
    }

    public Map<QName, Node> getProcessProperties() {
        return properties;
    }

    public String getPackage() {
        return du.getName();
    }

    public Definition getDefinitionForService(QName serviceName) {
        return du.getDefinitionForService(serviceName);
    }

    public Definition getDefinitionForPortType(QName portType) {
        return du.getDefinitionForPortType(portType);
    }

    public Map<String, Endpoint> getProvideEndpoints() {
        return Collections.unmodifiableMap(myRoleEndpoints);
    }

    public Map<String, Endpoint> getInvokeEndpoints() {
        return Collections.unmodifiableMap(partnerRoleInitialValues);
    }

    public Map<String, PartnerRoleConfig> getPartnerRoleConfig() {
        return Collections.unmodifiableMap(partnerRoleConfigurations);
    }

    public boolean isSharedService(QName serviceName) {
        return sharedServices.contains(serviceName);
    }

    public boolean isUndeploying() {
        return undeploying;
    }

    public void setUndeploying(boolean undeploying) {
        this.undeploying = undeploying;
    }

    public List<Element> getExtensionElement(QName name) {
        try {
            return DOMUtils.findChildrenByName(DOMUtils.stringToDOM(processInfo.toString()), name);
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    public boolean isEventEnabled(List<String> scopeNames, BpelEvent.TYPE type) {
        if (scopeNames != null) {
            for (String scopeName : scopeNames) {
                Set<BpelEvent.TYPE> evtSet = events.get(scopeName);
                if (evtSet != null && evtSet.contains(type)) {
                    return true;
                }
            }
        }
        Set<BpelEvent.TYPE> evtSet = events.get(null);
        return evtSet != null && evtSet.contains(type);
    }

    public Map<String, String> getEndpointProperties(EndpointReference endpointReference) {
        /**
         * This method is only there to use by ODEProcess#getTimeout method. Because we can't change
         * internals of ODE we have to make our configuration mechanism transparent to ODE.
         * Therefore I only added mex.timeout property to map and returned it here. If there are
         * more properties like this which use by BPEL engine we have to make our configuration
         * mechanism transparent and add that property to this map.
         */
        EndpointConfiguration epConf = null;
        final Map map = eprContext.getConfigLookup(endpointReference);
        final QName service = (QName) map.get("service");
        final String port = (String) map.get("port");
        if (log.isDebugEnabled()) {
            log.debug("Looking Endpoint configuration properties for service: " + service +
                    " and port: " + port);
        }
        if (bpelPackageConfiguration != null) {

            epConf = (EndpointConfiguration) bpelPackageConfiguration.getEndpoints().get(
                    service.getLocalPart(), service.getNamespaceURI(), port);
        }

        HashMap<String, String> props = new HashMap<String, String>();

        if (epConf != null) {
            props.put(BPELConstants.ODE_MEX_TIMEOUT, epConf.getMexTimeout());
        }

        return props;
    }

    public EndpointConfiguration getEndpointConfiguration(EndpointReference endpointReference) {
        /**
         * Previously ode used getEndpointProperties method to access endpoint properties.
         * With new config mechanism, I changed the way that integration layer access endpoint
         * configuration. But BPEL engine is using old method even now.
         */
        final Map map = eprContext.getConfigLookup(endpointReference);
        final QName service = (QName) map.get("service");
        final String port = (String) map.get("port");
        EndpointConfiguration endpointConfig = null;

        if (bpelPackageConfiguration != null) {
            MultiKeyMap endpointConfigs = bpelPackageConfiguration.getEndpoints();
            if (endpointConfigs.size() > 0) {
                endpointConfig = (EndpointConfiguration) endpointConfigs.get(service.getLocalPart(),
                        service.getNamespaceURI(), port);
                if (endpointConfig == null) {
                    endpointConfig = (EndpointConfiguration) endpointConfigs.get(
                            service.getLocalPart(), service.getNamespaceURI(), null);
                }
            }
        }

        if (endpointConfig == null) {
            endpointConfig = new EndpointConfiguration();
            endpointConfig.setServiceName(service.getLocalPart());
            endpointConfig.setServicePort(port);
            endpointConfig.setServiceNS(service.getNamespaceURI());
            UnifiedEndpoint uep = new UnifiedEndpoint();
            uep.setUepId(service.getLocalPart());
            uep.setAddressingEnabled(true);
            uep.setAddressingVersion(UnifiedEndpointConstants.ADDRESSING_VERSION_FINAL);

            bpelPackageConfiguration.addEndpoint(endpointConfig);
        }

        return endpointConfig;
    }

    public boolean isCleanupCategoryEnabled(boolean instanceSucceeded,
                                            CLEANUP_CATEGORY category) {
        return processCleanupConfImpl.isCleanupCategoryEnabled(instanceSucceeded, category);
    }

    public Set<CLEANUP_CATEGORY> getCleanupCategories(boolean instanceSucceeded) {
        return processCleanupConfImpl.getCleanupCategories(instanceSucceeded);
    }

    public List<CronJob> getCronJobs() {
        List<CronJob> jobs = new ArrayList<CronJob>();

        for (TSchedule schedule : processInfo.getScheduleList()) {
            CronJob job = new CronJob();
            try {
                job.setCronExpression(new CronExpression(schedule.getWhen()));
                for (final TCleanup aCleanup : schedule.getCleanupList()) {
                    CleanupInfo cleanupInfo = new CleanupInfo();
                    assert !aCleanup.getFilterList().isEmpty();
                    cleanupInfo.setFilters(aCleanup.getFilterList());
                    ProcessCleanupConfImpl.processACleanup(cleanupInfo.getCategories(),
                            aCleanup.getCategoryList());

                    Scheduler.JobDetails runnableDetails = new Scheduler.JobDetails();
                    runnableDetails.getDetailsExt().
                            put(BPELConstants.ODE_DETAILS_EXT_CLEAN_UP_INFO, cleanupInfo);
                    runnableDetails.setProcessId(processId);
                    runnableDetails.getDetailsExt().
                            put(BPELConstants.ODE_DETAILS_EXT_TRANSACTION_SIZE, 10);
                    job.getRunnableDetailList().add(runnableDetails);
                }
                jobs.add(job);
            } catch (ParseException pe) {
                log.error("Exception during parsing the schedule cron expression: " +
                        schedule.getWhen() + ", skipped the scheduled job.", pe);
            }
        }

        return jobs;
    }

    public QName getCorrelationFilter(String partnerLinkName) {
        for (TProvide tProvide : processInfo.getProvideList()) {
            if (tProvide.getPartnerLink().equals(partnerLinkName)) {
                return tProvide.getCorrelationFilter();
            }
        }
        return null;
    }

    public void setState(ProcessState state) {
        this.state = state;
    }

    public DeploymentUnitDir getDeploymentUnit() {
        return du;
    }

    public List<String> getMexInterceptors() {
        return mexInterceptors; // unmodifiable tag is removed here
    }

    public TDeployment.Process getProcessDeploymentInfo() {
        return processInfo;
    }

    /* the new getters to obtain process details*/

    public TProcessEvents.Generate.Enum getGenerateType() {

        if (processInfo.getProcessEvents() != null && processInfo.getProcessEvents().getGenerate() != null) {
            generateType = processInfo.getProcessEvents().getGenerate();

        }
        return generateType;
    }

    public List<TInvoke> getInvokedServices() {

        if (!invokedServices.isEmpty()) {
            invokedServices.clear();
        }
        for (TInvoke invokedService : processInfo.getInvokeList()) {
            invokedServices.add(invokedService);
        }

        return invokedServices;
    }

    public List<TProvide> getProvidedServices() {

        if (!providedServices.isEmpty()) {
            providedServices.clear();
        }
        for (TProvide providedService : processInfo.getProvideList()) {
            providedServices.add(providedService);
        }
        return providedServices;
    }

    public Map<String, Set<BpelEvent.TYPE>> getEvents() {

        return events;
    }

    public boolean getEventsEnabled() {

        return (events != null) && !events.isEmpty();
    }

    public ProcessCleanupConfImpl getProcessCleanupConfImpl() {
        return processCleanupConfImpl;
    }


    /* end of new getters*/


    //TODO review this with ODE
    private void initSchedules() {
        for (TSchedule schedule : processInfo.getScheduleList()) {
            for (TCleanup cleanup : schedule.getCleanupList()) {
                assert !cleanup.getFilterList().isEmpty();
            }
        }
    }

    private void readPackageConfiguration() {
        File depDir = du.getDeployDir();

        /*
        Read Endpoint Config for invokes
         */
        List<TDeployment.Process> processList = du.getDeploymentDescriptor().getDeploy().
                getProcessList();
        for (TDeployment.Process process : processList) {
            List<TInvoke> tInvokeList = process.getInvokeList();
            for (TInvoke tInvoke : tInvokeList) {
                OMElement serviceEle;
                if (tInvoke.getService() == null) {
                    String errMsg = "Service element missing for the invoke element in deploy.xml";
                    log.error(errMsg);
                    throw new BPELDeploymentException(errMsg);
                }
                try {
                    serviceEle = AXIOMUtil.stringToOM(tInvoke.getService().toString());
                    OMElement endpointEle = serviceEle.getFirstElement();
                    if (endpointEle == null || !endpointEle.getQName().equals(
                            new QName(BusinessProcessConstants.BPEL_PKG_ENDPOINT_CONFIG_NS,
                                    BusinessProcessConstants.ENDPOINT))) {
                        continue;
                    }

                    EndpointConfiguration epConf = EndpointConfigBuilder.
                            buildEndpointConfiguration(endpointEle, depDir.getAbsolutePath());
                    epConf.setServiceName(tInvoke.getService().getName().getLocalPart());
                    epConf.setServiceNS(tInvoke.getService().getName().getNamespaceURI());
                    epConf.setServicePort(tInvoke.getService().getPort());
                    bpelPackageConfiguration.addEndpoint(epConf);
                } catch (XMLStreamException e) {
                    log.warn("Error occurred while reading endpoint configuration. " +
                            "Endpoint config will not be applied to: " + tInvoke.getService());
                }
            }

            List<TProvide> tProvideList = process.getProvideList();
            for (TProvide tProvide : tProvideList) {
                OMElement serviceEle;
                if (tProvide.getService() == null) {
                    String errMsg = "Service element missing for the provide element in deploy.xml";
                    log.error(errMsg);
                    throw new BPELDeploymentException(errMsg);
                }
                try {
                    serviceEle = AXIOMUtil.stringToOM(tProvide.getService().toString());
                    OMElement endpointEle = serviceEle.getFirstElement();
                    if (endpointEle == null || !endpointEle.getQName().equals(
                            new QName(BusinessProcessConstants.BPEL_PKG_ENDPOINT_CONFIG_NS,
                                    BusinessProcessConstants.ENDPOINT))) {
                        continue;
                    }

                    EndpointConfiguration epConf = EndpointConfigBuilder.
                            buildEndpointConfiguration(endpointEle, depDir.getAbsolutePath());
                    epConf.setServiceName(tProvide.getService().getName().getLocalPart());
                    epConf.setServiceNS(tProvide.getService().getName().getNamespaceURI());
                    epConf.setServicePort(tProvide.getService().getPort());
                    bpelPackageConfiguration.addEndpoint(epConf);
                } catch (XMLStreamException e) {
                    log.warn("Error occured while reading endpoint configuration. " +
                            "Endpoint config will not be applied to: " + tProvide.getService());
                }
            }
        }
    }

    /**
     * Initialize partner link details of the BPEL process. Details about partner link's service and
     * port is in the deploy.xml file. This can be used to initialize partner links information in
     * registry. After that we can co-relate this partner links with carbon endpoints. This will
     * help us to dynamically configure endpoint properties like security, RM.
     */
    private void initPartnerLinks() {
        if (processInfo.getInvokeList() != null) {
            for (TInvoke invoke : processInfo.getInvokeList()) {
                String plinkName = invoke.getPartnerLink();
                TService service = invoke.getService();
                /* NOTE:Service can be null for partner links*/
                // TODO Currently in BPS this is a problem, since there is no anyother place to
                // configure this value
                if (service == null) {
                    continue;
                }
                if (log.isDebugEnabled()) {
                    log.debug("Processing <invoke> element for process " + processInfo.getName() +
                            ": partnerlink" + plinkName + " -->" + service);
                }

                QName serviceName = service.getName();
                /* Validating configuration with package content before putting partner role endpoints to map */
                Definition wsdlDef = getDefinitionForService(serviceName);
                if (wsdlDef == null) {
                    String errMsg = "Cannot find WSDL definition for invoke service " + serviceName +
                            ". Required resources not found in the BPEL package " +
                            du.getName() + ".";
                    log.error(errMsg);
                    throw new ContextException(errMsg);
                }

                Service serviceDef = wsdlDef.getService(serviceName);
                if (serviceDef.getPort(service.getPort()) == null) {
                    String errMsg = "Cannot find  port for invoking service for the given name " +
                            serviceName + ". Error in deploy.xml.";
                    log.error(errMsg);
                    throw new ContextException(errMsg);
                }

                partnerRoleInitialValues.put(plinkName, new Endpoint(service.getName(),
                        service.getPort()));
                //TODO add proper variable names
                {
                    OFailureHandling g = null;
                    if (invoke.isSetFailureHandling()) {
                        FailureHandlingDocument.FailureHandling fh = invoke.getFailureHandling();
                        g = new OFailureHandling();

                        if (fh.isSetFaultOnFailure()) {
                            g.faultOnFailure = fh.getFaultOnFailure();
                        }

                        if (fh.isSetRetryDelay()) {
                            g.retryDelay = fh.getRetryDelay();
                        }

                        if (fh.isSetRetryFor()) {
                            g.retryFor = fh.getRetryFor();
                        }
                    }

                    PartnerRoleConfig c = new PartnerRoleConfig(g, invoke.getUsePeer2Peer());
                    if (log.isDebugEnabled()) {
                        log.debug("PartnerRoleConfig for " + plinkName + " " + c.failureHandling +
                                " usePeer2Peer: " + c.usePeer2Peer);
                    }

                    partnerRoleConfigurations.put(plinkName, c);
                }
            }
        }

        if (processInfo.getProvideList() != null) {
            for (TProvide proivde : processInfo.getProvideList()) {
                String plinkName = proivde.getPartnerLink();
                TService service = proivde.getService();

                if (proivde.getCorrelationFilter() != null) {
                    if (B4P_NAMESPACE.equals(proivde.getCorrelationFilter().getNamespaceURI())) {
                        isB4PTaskIncluded = true;
                    }
                }

                /* NOTE:Service cannot be null for provider partner link*/
                if (service == null) {
                    String errorMsg = "Error in <provide> element for process " +
                            processInfo.getName() + ";partnerlink" + plinkName +
                            "did not identify an endpoint";
                    log.error(errorMsg);
                    throw new ContextException(errorMsg);
                }
                if (log.isDebugEnabled()) {
                    log.debug("Processing <provide> element for process " + processInfo.getName() +
                            ": partnerlink " + plinkName + " --> " + service.getName() + " : " +
                            service.getPort());
                }

                QName serviceName = service.getName();

                /* Validating configuration with package content before putting myRole endpoints to map */
                Definition wsdlDef = getDefinitionForService(serviceName);
                if (wsdlDef == null) {
                    String errMsg = "Cannot find WSDL definition for provide service " +
                            serviceName + ". Required resources not found in the BPEL " +
                            "package " + du.getName() + ".";
                    log.error(errMsg);
                    throw new ContextException(errMsg);
                }

                Service serviceDef = wsdlDef.getService(serviceName);
                if (serviceDef.getPort(service.getPort()) == null) {
                    String errMsg = "Cannot find provide port in the given service " + serviceName +
                            ". Error in deploy.xml.";
                    log.error(errMsg);
                    throw new ContextException(errMsg);
                }
                myRoleEndpoints.put(plinkName, new Endpoint(service.getName(), service.getPort()));
                if (proivde.isSetEnableSharing()) {
                    sharedServices.add(service.getName());
                }
            }
        }
    }

    /**
     * This method configure the events generated by process instance according to the configurations in deploy.xml.
     */
    private void initEventList() {
        TProcessEvents processEvents = processInfo.getProcessEvents();
        if (log.isDebugEnabled() && processEvents != null) {
            List<String> enabledEventList = processEvents.getEnableEventList();
            StringBuilder strBuf = new StringBuilder();
            for (String eventType : enabledEventList) {
                strBuf.append(eventType);
                strBuf.append(", ");
            }
            log.debug("Enabled Event List: " + strBuf.toString());
        }
        /* Defaults */
        if (processEvents == null) {
            //disabling events by default
            /* HashSet<BpelEvent.TYPE> all = new HashSet<BpelEvent.TYPE>();
        for (BpelEvent.TYPE t : BpelEvent.TYPE.values()) {
            if (!t.equals(BpelEvent.TYPE.scopeHandling)) {
                all.add(t);
            }
        }

        events.put(null, all);   */
            return;
        }

        /* All events */
        if (processEvents.getGenerate() != null &&
                processEvents.getGenerate().equals(TProcessEvents.Generate.ALL)) {
            HashSet<BpelEvent.TYPE> all = new HashSet<BpelEvent.TYPE>();
            all.addAll(Arrays.asList(BpelEvent.TYPE.values()));
            events.put(null, all);
            return;
        }

        /* Events filtered at the process level*/
        if (processEvents.getEnableEventList() != null) {
            List<String> enabled = processEvents.getEnableEventList();
            HashSet<BpelEvent.TYPE> evtSet = new HashSet<BpelEvent.TYPE>();
            for (String enEvt : enabled) {
                evtSet.add(BpelEvent.TYPE.valueOf(enEvt));
            }
            events.put(null, evtSet);
        }

        /* Events filtered at the scope level */
        if (processEvents.getScopeEventsList() != null) {
            for (TScopeEvents tScopeEvents : processEvents.getScopeEventsList()) {
                HashSet<BpelEvent.TYPE> evtSet = new HashSet<BpelEvent.TYPE>();
                for (String enEvt : tScopeEvents.getEnableEventList()) {
                    evtSet.add(BpelEvent.TYPE.valueOf(enEvt));
                }
                events.put(tScopeEvents.getName(), evtSet);
            }
        }
    }

    private void initMexInterceptors() {
        if (processInfo.getMexInterceptors() != null) {
            for (TMexInterceptor mexInterceptor : processInfo.getMexInterceptors().
                    getMexInterceptorList()) {
                mexInterceptors.add(mexInterceptor.getClassName());
            }
        }
    }


    /* The following methods set the details of deploy information table*/

    public void setProcessEventsList(ProcessEventsListType processEventsList) {

        if (processEventsList != null) {
            events.clear();

            if (processEventsList.getEnableEventsList() != null && processEventsList.getEnableEventsList()
                    .getEnableEvent() != null) {
                EnableEventListType enableEventListType = processEventsList.getEnableEventsList();
                String[] enabledEvents = enableEventListType.getEnableEvent();
                HashSet<BpelEvent.TYPE> enabledEvtSet = new HashSet<BpelEvent.TYPE>();
                for (String event : enabledEvents) {
                    enabledEvtSet.add(BpelEvent.TYPE.valueOf(event));
                }
                events.put(null, enabledEvtSet);
            }
            if (processEventsList.getScopeEventsList() != null && processEventsList.getScopeEventsList()
                    .getScopeEvent() != null) {
                ScopeEventListType scopeEventListType = processEventsList.getScopeEventsList();
                ScopeEventType[] scopeEvents = scopeEventListType.getScopeEvent();

                for (ScopeEventType scopeEvent : scopeEvents) {
                    EnableEventListType enabledEventLst = scopeEvent.getEnabledEventList();
                    HashSet<BpelEvent.TYPE> scopeEnabledEventSet = new HashSet<BpelEvent.TYPE>();
                    if (enabledEventLst != null && enabledEventLst.getEnableEvent() != null) {

                        for (String event : enabledEventLst.getEnableEvent()) {
                            scopeEnabledEventSet.add(BpelEvent.TYPE.valueOf(event));
                        }

                    }
                    events.put(scopeEvent.getScope(), scopeEnabledEventSet);
                }
            }
        }
    }


    public void setIsTransient(boolean inMemory) {

        this.inMemory = inMemory;
    }


    public void setProcessCleanupConfImpl(CleanUpListType cleanUpList) {


        processCleanupConfImpl.getCleanupCategories(true).clear();
        processCleanupConfImpl.getCleanupCategories(false).clear();
        if (cleanUpList != null) {
            List<TCleanup.Category.Enum> sucessCategoryList = new ArrayList<TCleanup.Category.Enum>();
            List<TCleanup.Category.Enum> failCategoryList = new ArrayList<TCleanup.Category.Enum>();
            CleanUpType[] cleanUpType = cleanUpList.getCleanUp();
            if (cleanUpType != null) {
                for (CleanUpType cleanUp : cleanUpType) {
                    if (cleanUp.getOn().getValue().equalsIgnoreCase("success")) {
                        CategoryListType sucCategoryListType = cleanUp.getCategoryList();
                        if (sucCategoryListType != null && sucCategoryListType.getCategory() != null) {
                            for (Category_type1 categoryType1 : sucCategoryListType.getCategory()) {
                                sucessCategoryList.add(TCleanup.Category.Enum.forString(categoryType1.getValue()));
                            }
                            ProcessCleanupConfImpl.processACleanup(processCleanupConfImpl.getCleanupCategories(true),
                                    sucessCategoryList);
                        }
                    }
                    if (cleanUp.getOn().getValue().equalsIgnoreCase("failure")) {
                        CategoryListType failCategoryListType = cleanUp.getCategoryList();
                        if (failCategoryListType != null && failCategoryListType.getCategory() != null) {
                            for (Category_type1 categoryType1 : failCategoryListType.getCategory()) {
                                failCategoryList.add(TCleanup.Category.Enum.forString(categoryType1.getValue()));
                            }
                            ProcessCleanupConfImpl.processACleanup(processCleanupConfImpl.getCleanupCategories(false)
                                    , failCategoryList);
                        }
                    }
                }
            }
        }
    }

    public void setGenerateType(ProcessEventsListType processEventsListType) {

        if (processEventsListType != null && processEventsListType.getGenerate() != null) {

            String value = processEventsListType.getGenerate().getValue();
            generateType = TProcessEvents.Generate.Enum.forString(value);

        }

    }


    public boolean isB4PTaskIncluded() {
        return isB4PTaskIncluded;
    }


}
