/**
 *  Copyright (c) 2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.bpel.core.ode.integration.mgt.services;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.common.Filter;
import org.apache.ode.bpel.common.InstanceFilter;
import org.apache.ode.bpel.common.ProcessState;
import org.apache.ode.bpel.dao.ActivityRecoveryDAO;
import org.apache.ode.bpel.dao.BpelDAOConnection;
import org.apache.ode.bpel.dao.CorrelationSetDAO;
import org.apache.ode.bpel.dao.FaultDAO;
import org.apache.ode.bpel.dao.ProcessInstanceDAO;
import org.apache.ode.bpel.dao.ScopeDAO;
import org.apache.ode.bpel.dao.ScopeStateEnum;
import org.apache.ode.bpel.dao.XmlDataDAO;
import org.apache.ode.bpel.engine.BpelDatabase;
import org.apache.ode.bpel.engine.BpelEngineImpl;
import org.apache.ode.bpel.engine.BpelProcess;
import org.apache.ode.bpel.engine.DebuggerSupport;
import org.apache.ode.bpel.evt.BpelEvent;
import org.apache.ode.bpel.evtproc.ActivityStateDocumentBuilder;
import org.apache.ode.bpel.iapi.ProcessConf;
import org.apache.ode.bpel.pmapi.ActivityInfoDocument;
import org.apache.ode.bpel.pmapi.EventInfoListDocument;
import org.apache.ode.bpel.pmapi.ManagementException;
import org.apache.ode.bpel.pmapi.ProcessNotFoundException;
import org.apache.ode.bpel.pmapi.ProcessingException;
import org.apache.ode.bpel.pmapi.TActivityInfo;
import org.apache.ode.bpel.pmapi.TActivityStatus;
import org.apache.ode.bpel.pmapi.TEventInfo;
import org.apache.ode.bpel.pmapi.TEventInfoList;
import org.apache.ode.bpel.pmapi.TFailureInfo;
import org.apache.ode.bpel.pmapi.TScopeStatus;
import org.apache.ode.dao.jpa.EventDAOImpl;
import org.apache.ode.dao.jpa.ScopeDAOImpl;
import org.apache.ode.il.OMUtils;
import org.w3c.dom.Node;
import org.wso2.carbon.bpel.core.BPELConstants;
import org.wso2.carbon.bpel.core.ode.integration.BPELServerImpl;
import org.wso2.carbon.bpel.core.ode.integration.store.ProcessConfigurationImpl;
import org.wso2.carbon.bpel.core.ode.integration.store.TenantProcessStoreImpl;
import org.wso2.carbon.bpel.core.ode.integration.utils.ActivityInfoWithEventsDocument;
import org.wso2.carbon.bpel.core.ode.integration.utils.ActivityLifeCycleEventsDocumentBuilder;
import org.wso2.carbon.bpel.core.ode.integration.utils.ActivityStateAndEventDocumentBuilder;
import org.wso2.carbon.bpel.skeleton.ode.integration.mgt.services.InstanceManagementException;
import org.wso2.carbon.bpel.skeleton.ode.integration.mgt.services.InstanceManagementServiceSkeletonInterface;
import org.wso2.carbon.bpel.skeleton.ode.integration.mgt.services.types.Action_type1;
import org.wso2.carbon.bpel.skeleton.ode.integration.mgt.services.types.ActivitiesWithEvents_type0;
import org.wso2.carbon.bpel.skeleton.ode.integration.mgt.services.types.Activities_type0;
import org.wso2.carbon.bpel.skeleton.ode.integration.mgt.services.types.ActivityInfoType;
import org.wso2.carbon.bpel.skeleton.ode.integration.mgt.services.types.ActivityInfoWithEventsType;
import org.wso2.carbon.bpel.skeleton.ode.integration.mgt.services.types.ActivityLifeCycleEventsListType;
import org.wso2.carbon.bpel.skeleton.ode.integration.mgt.services.types.ActivityLifeCycleEventsType;
import org.wso2.carbon.bpel.skeleton.ode.integration.mgt.services.types.ActivityStatusType;
import org.wso2.carbon.bpel.skeleton.ode.integration.mgt.services.types.ChildrenWithEvents_type0;
import org.wso2.carbon.bpel.skeleton.ode.integration.mgt.services.types.Children_type0;
import org.wso2.carbon.bpel.skeleton.ode.integration.mgt.services.types.CorrelationPropertyType;
import org.wso2.carbon.bpel.skeleton.ode.integration.mgt.services.types.CorrelationSet_type0;
import org.wso2.carbon.bpel.skeleton.ode.integration.mgt.services.types.CorrelationSets_type0;
import org.wso2.carbon.bpel.skeleton.ode.integration.mgt.services.types.Data_type0;
import org.wso2.carbon.bpel.skeleton.ode.integration.mgt.services.types.EventInfo;
import org.wso2.carbon.bpel.skeleton.ode.integration.mgt.services.types.EventInfoList;
import org.wso2.carbon.bpel.skeleton.ode.integration.mgt.services.types.FailureInfoType;
import org.wso2.carbon.bpel.skeleton.ode.integration.mgt.services.types.FailuresInfoType;
import org.wso2.carbon.bpel.skeleton.ode.integration.mgt.services.types.FaultInfoType;
import org.wso2.carbon.bpel.skeleton.ode.integration.mgt.services.types.InstanceInfoType;
import org.wso2.carbon.bpel.skeleton.ode.integration.mgt.services.types.InstanceInfoWithEventsType;
import org.wso2.carbon.bpel.skeleton.ode.integration.mgt.services.types.InstanceStatus;
import org.wso2.carbon.bpel.skeleton.ode.integration.mgt.services.types.InstanceSummaryE;
import org.wso2.carbon.bpel.skeleton.ode.integration.mgt.services.types.LimitedInstanceInfoType;
import org.wso2.carbon.bpel.skeleton.ode.integration.mgt.services.types.PaginatedInstanceList;
import org.wso2.carbon.bpel.skeleton.ode.integration.mgt.services.types.ScopeInfoType;
import org.wso2.carbon.bpel.skeleton.ode.integration.mgt.services.types.ScopeInfoWithEventsType;
import org.wso2.carbon.bpel.skeleton.ode.integration.mgt.services.types.ScopeStatusType;
import org.wso2.carbon.bpel.skeleton.ode.integration.mgt.services.types.Value_type0;
import org.wso2.carbon.bpel.skeleton.ode.integration.mgt.services.types.VariableInfoType;
import org.wso2.carbon.bpel.skeleton.ode.integration.mgt.services.types.VariableRefType;
import org.wso2.carbon.bpel.skeleton.ode.integration.mgt.services.types.VariablesWithEvents_type0;
import org.wso2.carbon.bpel.skeleton.ode.integration.mgt.services.types.Variables_type0;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.core.AbstractAdmin;

/**
 * Instance Management Service Implementation.
 */
public class InstanceManagementServiceSkeleton extends AbstractAdmin
        implements InstanceManagementServiceSkeletonInterface {
    private static Log log = LogFactory.getLog(InstanceManagementServiceSkeleton.class);
    private BPELServerImpl bpelServer = BPELServerImpl.getInstance();
    private Calendar calendar = Calendar.getInstance();

    private static Map<ScopeStateEnum, ScopeStatusType> scopeStatusMap =
            new HashMap<ScopeStateEnum, ScopeStatusType>();
    private static Map<TScopeStatus.Enum, ScopeStatusType> tscopeStatusMap =
            new HashMap<TScopeStatus.Enum, ScopeStatusType>();
    private static Map<TActivityStatus.Enum, ActivityStatusType> activityStatusMap =
            new HashMap<TActivityStatus.Enum, ActivityStatusType>();

    public static final String INSTANCE_STATUS_ACTIVE = "active";
    public static final String INSTANCE_STATUS_COMPLETED = "completed";
    public static final String INSTANCE_STATUS_FAILED = "failed";
    public static final String INSTANCE_STATUS_SUSPENDED = "suspended";
    public static final String INSTANCE_STATUS_TERMINATED = "terminated";

    static {
        scopeStatusMap.put(ScopeStateEnum.ACTIVE, ScopeStatusType.ACTIVE);
        scopeStatusMap.put(ScopeStateEnum.NEW, ScopeStatusType.NEW);
        scopeStatusMap.put(ScopeStateEnum.COMPLETED, ScopeStatusType.COMPLETED);
        scopeStatusMap.put(ScopeStateEnum.FAULT, ScopeStatusType.FAULTED);

        tscopeStatusMap.put(TScopeStatus.ACTIVE, ScopeStatusType.ACTIVE);
        tscopeStatusMap.put(TScopeStatus.COMPENSATED, ScopeStatusType.COMPENSATED);
        tscopeStatusMap.put(TScopeStatus.COMPENSATING, ScopeStatusType.COMPENSATING);
        tscopeStatusMap.put(TScopeStatus.COMPLETED, ScopeStatusType.COMPLETED);
        tscopeStatusMap.put(TScopeStatus.FAULTED, ScopeStatusType.FAULTED);
        tscopeStatusMap.put(TScopeStatus.FAULTHANDLING, ScopeStatusType.FAULTHANDLING);

        activityStatusMap.put(TActivityStatus.COMPLETED, ActivityStatusType.COMPLETED);
        activityStatusMap.put(TActivityStatus.ENABLED, ActivityStatusType.ENABLED);
        activityStatusMap.put(TActivityStatus.FAILURE, ActivityStatusType.FAILURE);
        activityStatusMap.put(TActivityStatus.STARTED, ActivityStatusType.STARTED);
    }

    public InstanceSummaryE getInstanceSummary() throws InstanceManagementException {
        InstanceSummaryE instanceSummary = new InstanceSummaryE();

        Integer tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();

        TenantProcessStoreImpl tenantProcessStore = (TenantProcessStoreImpl) bpelServer.
                getMultiTenantProcessStore().getTenantsProcessStore(tenantId);

        instanceSummary.setActive(getInstanceCountByState(
                getTenantsProcessList(tenantProcessStore.getProcessConfigMap().keySet()),
                INSTANCE_STATUS_ACTIVE).intValue());
        instanceSummary.setCompleted(getInstanceCountByState(
                getTenantsProcessList(tenantProcessStore.getProcessConfigMap().keySet()),
                INSTANCE_STATUS_COMPLETED).intValue());
        instanceSummary.setFailed(getInstanceCountByState(
                getTenantsProcessList(tenantProcessStore.getProcessConfigMap().keySet()),
                INSTANCE_STATUS_FAILED).intValue());
        instanceSummary.setSuspended(getInstanceCountByState(
                getTenantsProcessList(tenantProcessStore.getProcessConfigMap().keySet()),
                INSTANCE_STATUS_SUSPENDED).intValue());
        instanceSummary.setTerminated(getInstanceCountByState(
                getTenantsProcessList(tenantProcessStore.getProcessConfigMap().keySet()),
                INSTANCE_STATUS_TERMINATED).intValue());

        return instanceSummary;
    }

    /**
     * Verify whether there are proceses exist or not based on the input string
     * This method is introduced inorder to avoid sending erroneous filter strings
     * (e.g - " pid= status=active") to ODE backend.
     * <p/>
     * if input string is similar to " pid= " means there are no processes
     * else input string is similar to " pid = $PID1|$PID2" means some processes exist
     *
     * @param processList process list to be checked
     * @return isProcessListEmpty?
     */
    private boolean isProcessListEmpty(String processList) {
        return processList.trim().equals("pid=");
    }

    private Long getInstanceCountByState(String processList, String instanceState)
            throws InstanceManagementException {
        if (isProcessListEmpty(processList)) {
            return (long) 0;
        }
        final List<Long> instanceCountList = new ArrayList<Long>();
        StringBuilder filter = new StringBuilder();

        if (!isProcessListEmpty(processList)) {
            filter.append(processList);
        }
        filter.append("status=");
        filter.append(instanceState);

        final InstanceFilter instanceFilter = new InstanceFilter(filter.toString(), null,
                Integer.MAX_VALUE);

        try {
            BpelDatabase bpelDb = bpelServer.getODEBPELServer().getBpelDb();
            bpelDb.exec(new BpelDatabase.Callable<Object>() {
                public Object run(BpelDAOConnection conn) throws AxisFault {
                    instanceCountList.add(conn.instanceCount(instanceFilter));

                    return null;
                }
            });
        } catch (Exception e) {
            String errMsg = "Error querying instances from database. Filter: " +
                    instanceFilter.toString();
            log.error(errMsg, e);
            throw new InstanceManagementException(errMsg, e);
        }

        return instanceCountList.get(0);
    }

    /**
     * Get paginated instance list
     *
     * @param filter Instance tFilter
     * @param order  The field on which to be ordered
     * @param limit  The maximum number of instances to be fetched
     * @param page   The page number
     * @return Instances that are filtered through "tFilter", ordered by "order" that fits into
     *         'page'th page
     * @throws InstanceManagementException When an error occurs
     */
    public PaginatedInstanceList getPaginatedInstanceList(String filter, final String order,
                                                          final int limit, final int page)
            throws InstanceManagementException {
        String tFilter = filter;

        final PaginatedInstanceList instanceList = new PaginatedInstanceList();

        Integer tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        TenantProcessStoreImpl tenantProcessStore = (TenantProcessStoreImpl) bpelServer.
                getMultiTenantProcessStore().getTenantsProcessStore(tenantId);

        if (tenantProcessStore.getProcessConfigMap().size() <= 0) {
            instanceList.setPages(0);
            return instanceList;
        }

        if (!tFilter.contains(" pid=")) {
            tFilter = tFilter + getTenantsProcessList(tenantProcessStore.getProcessConfigMap().keySet());
        }
        if (log.isDebugEnabled()) {
            log.debug("Instance Filter:" + tFilter);
        }

        final InstanceFilter instanceFilter = new InstanceFilter(tFilter, order, limit);

        try {
            BpelDatabase bpelDb = bpelServer.getODEBPELServer().getBpelDb();
            bpelDb.exec(new BpelDatabase.Callable<Object>() {
                public Object run(BpelDAOConnection conn) throws InstanceManagementException {
                    Collection<ProcessInstanceDAO> instances = conn.instanceQuery(instanceFilter);
                    int pageNum = page;
                    if (pageNum < 0 || pageNum == Integer.MAX_VALUE) {
                        pageNum = 0;
                    }

                    int startIndexOfCurrentPage = pageNum * BPELConstants.ITEMS_PER_PAGE;
                    int endIndexOfCurrentPage = (pageNum + 1) * BPELConstants.ITEMS_PER_PAGE;
                    int instanceListSize = instances.size();

                    int pages = (int) Math.ceil((double) instanceListSize / BPELConstants.ITEMS_PER_PAGE);
                    instanceList.setPages(pages);

                    ProcessInstanceDAO[] instanceArray =
                            instances.toArray(new ProcessInstanceDAO[instanceListSize]);

                    for (int i = startIndexOfCurrentPage;
                         (i < endIndexOfCurrentPage && i < instanceListSize); i++) {
                        instanceList.addInstance(createLimitedInstanceInfoObject(instanceArray[i]));
                    }
                    return null;
                }
            });
        } catch (Exception e) {
            String errMsg = "Error querying instances from database. Instance Filter:" +
                    instanceFilter.toString();
            log.error(errMsg, e);
            throw new InstanceManagementException(errMsg, e);
        }

        return instanceList;
    }

    /**
     * Returns the maximum size for a variable to be displayed in the instance_view UI,
     * defined in bps.xml.
     * @return Maximum instance variable size
     * @throws InstanceManagementException
     */
    @Override
    public int getBPELInstanceVariableSize() throws InstanceManagementException {
        return bpelServer.getBpelServerConfiguration().getBPELInstanceVariableSize();
    }

    /**
     * Get long running instances with duration
     *
     * @param limit The maximum number of instances to be fetched
     * @return Long running instances
     * @throws InstanceManagementException When an error occurs
     */
    public LimitedInstanceInfoType[] getLongRunningInstances(int limit)
            throws InstanceManagementException {

        final List<LimitedInstanceInfoType> longRunningInstances = new ArrayList<LimitedInstanceInfoType>();
        Integer tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();

        TenantProcessStoreImpl tenantProcessStore = (TenantProcessStoreImpl) bpelServer.
                getMultiTenantProcessStore().getTenantsProcessStore(tenantId);

        if (tenantProcessStore.getProcessConfigMap().size() <= 0) {
            return longRunningInstances.toArray(new LimitedInstanceInfoType[longRunningInstances.size()]);
        }

        String filter = "status=ACTIVE";
        if (!filter.contains(" pid=")) {
            filter = filter + getTenantsProcessList(tenantProcessStore.getProcessConfigMap().keySet());
        }
        if (log.isDebugEnabled()) {
            log.debug("Instance Filter:" + filter);
        }

        String orderBy = "started";

        final InstanceFilter instanceFilter = new InstanceFilter(filter, orderBy, limit);

        try {
            BpelDatabase bpelDb = bpelServer.getODEBPELServer().getBpelDb();
            bpelDb.exec(new BpelDatabase.Callable<Object>() {
                public Object run(BpelDAOConnection conn) throws InstanceManagementException {
                    Collection<ProcessInstanceDAO> instances = conn.instanceQuery(instanceFilter);

                    for (ProcessInstanceDAO piDAO : instances) {
                        longRunningInstances.add(createLimitedInstanceInfoObject(piDAO));
                    }
                    return null;
                }
            });
        } catch (Exception e) {
            String errMsg = "Error querying instances from database. Instance Filter:" +
                    instanceFilter.toString();
            log.error(errMsg, e);
            throw new InstanceManagementException(errMsg, e);
        }

        return longRunningInstances.toArray(new LimitedInstanceInfoType[longRunningInstances.size()]);
    }

    /**
     * Get the instance information
     *
     * @param iid The instance id for which the instance details should be obtained
     * @return Instance information
     */
    public InstanceInfoType getInstanceInfo(long iid) throws InstanceManagementException {
        try {
            isOperationIsValidForTheCurrentTenant(iid);
        } catch (IllegalAccessException ex) {
            return handleError(ex);
        }
        return getInstanceInformation(iid);
    }

    private InstanceInfoType handleError(IllegalAccessException ex) throws InstanceManagementException {
        String errMsg = "You are trying to carry out unauthorized operation!";
        log.error(errMsg);
        throw new InstanceManagementException(errMsg, ex);
    }

    /**
     * Get the instance information with events
     *
     * @param iid The instance id for which the instance details should be obtained
     * @return Instance Info with events
     */
    public InstanceInfoWithEventsType getInstanceInfoWithEvents(long iid)
            throws InstanceManagementException {
        try {
            isOperationIsValidForTheCurrentTenant(iid);
        } catch (IllegalAccessException ex) {
            handleError(ex);
        }
        return getInstanceInformationWithEvents(iid);
    }

    /**
     * Returns a list of activity life cycle events. Used in the management console
     *
     * @param iid Instance ID
     * @return Activity LifeCycles
     */
    public ActivityLifeCycleEventsType getActivityLifeCycleFilter(long iid)
            throws InstanceManagementException {
        try {
            isOperationIsValidForTheCurrentTenant(iid);
        } catch (IllegalAccessException ex) {
            handleError(ex);
        }
        return getActivityLifeCycleEvents(iid);
    }

    /**
     * Retry failed activity of an instance of a process
     *
     * @param iid    Instance ID
     * @param aid    Activity ID
     * @param action Action to perform
     */
    public void recoverActivity(final long iid, final long aid, final Action_type1 action)
            throws InstanceManagementException {
        try {
            dbexec(new BpelDatabase.Callable<QName>() {
                public QName run(BpelDAOConnection conn) throws Exception {
                    ProcessInstanceDAO instance = conn.getInstance(iid);
                    if (instance == null) {
                        return null;
                    }
                    for (ActivityRecoveryDAO recovery : instance.getActivityRecoveries()) {
                        if (recovery.getActivityId() == aid) {
                            BpelProcess process = ((BpelEngineImpl) bpelServer.getODEBPELServer().
                                    getEngine()).
                                    _activeProcesses.get(instance.getProcess().getProcessId());
                            if (process != null) {
                                if (action == Action_type1.cancel) {
                                    process.recoverActivity(instance, recovery.getChannel(), aid,
                                            Action_type1.cancel.getValue(), null);
                                    log.info("Activity retrying is canceled for activity: " + aid +
                                            " of instance: " + iid);
                                } else if (action == Action_type1.retry) {
                                    process.recoverActivity(instance, recovery.getChannel(), aid,
                                            Action_type1.retry.getValue(), null);
                                    log.info("Activity is retried for activity: " + aid +
                                            " of instance: " + iid);
                                } else {
                                    log.warn("Invalid retry action: " + action + " for activity: " +
                                            aid + " of instance: " + iid);
                                    //TODO process fault action
                                }

                                break;
                            }
                        }
                    }
                    return instance.getProcess().getProcessId();
                }
            });
        } catch (Exception e) {
            String errMsg = "Exception occurred while recovering the activity: " + aid +
                    " of ths instance: " + iid + " action: " + action.getValue();
            log.error(errMsg, e);
            throw new InstanceManagementException(errMsg, e);
        }
    }

    private ActivityLifeCycleEventsType getActivityLifeCycleEvents(final long iid)
            throws InstanceManagementException {
        final ActivityLifeCycleEventsType activityLifeCycleEvents = new ActivityLifeCycleEventsType();
        activityLifeCycleEvents.setIid(Long.toString(iid));
        dbexec(new BpelDatabase.Callable<Object>() {
            public Object run(BpelDAOConnection conn) throws InstanceManagementException {
                ProcessInstanceDAO instance = conn.getInstanceEagerly(iid, true);

                if (instance == null) {
                    String errMsg = "Instance " + iid + " not found.";
                    log.error(errMsg);
                    throw new InstanceManagementException(errMsg);
                }

                fillActivityLifeCycleEvents(activityLifeCycleEvents, instance);
                return null;
            }
        });

        return activityLifeCycleEvents;
    }

    private void fillActivityLifeCycleEvents(ActivityLifeCycleEventsType eventInfoArray,
                                             ProcessInstanceDAO processInstance)
            throws InstanceManagementException {
        eventInfoArray.setPid(processInstance.getProcess().getProcessId().toString());

        //fillFaultAndFailure(); //TODO: to be impl

        if (processInstance.getRootScope() != null) {
			eventInfoArray.setEventInfoList(getActivityLifeCycleEventsFromScope(processInstance.getRootScope()));
        }
    }

    private ActivityLifeCycleEventsListType getActivityLifeCycleEventsFromScope(ScopeDAO scope)
            throws InstanceManagementException {
        final ActivityLifeCycleEventsListType activityLifeCycleEventsList =
                new ActivityLifeCycleEventsListType();

        /*dbexec(new BpelDatabase.Callable<Object>() {
            public Object run(BpelDAOConnection conn) throws InstanceManagementException {
                ScopeDAO scope = conn.getScope(siid);
                if (scope == null) {
                    String errMsg = "Scope " + siid + " not found.";
                    log.error(errMsg);
                    throw new InstanceManagementException(errMsg);
                }
                fillActivityLifeCycleEventsFromScope(activityLifeCycleEventsList, scope);
                return null;
            }
        });*/
        fillActivityLifeCycleEventsFromScope(activityLifeCycleEventsList, scope);

        return activityLifeCycleEventsList;
    }

    private void fillActivityLifeCycleEventsFromScope(
            ActivityLifeCycleEventsListType activityLifeCycleEventsList, ScopeDAO scope) {

        //List<BpelEvent> events = scope.listEvents();
    	 Set<EventDAOImpl> eventsEntities = ((ScopeDAOImpl)scope).getEvents();
         List<BpelEvent> events = new ArrayList<BpelEvent>();
         for(EventDAOImpl event : eventsEntities){
         	events.add(event.getEvent());
         }
        ActivityLifeCycleEventsDocumentBuilder docBuilder = new ActivityLifeCycleEventsDocumentBuilder();

        for (BpelEvent e : events) {
            docBuilder.onEvent(e);
        }

        EventInfoListDocument infoList = docBuilder.getActivityLifeCycleEvents();

        fillActivityLifeCycleEventsList(activityLifeCycleEventsList, infoList);

        for (ScopeDAO childScope : scope.getChildScopes()) {
            fillActivityLifeCycleEventsFromScope(activityLifeCycleEventsList, childScope);
        }
    }

    private void fillActivityLifeCycleEventsList(
            ActivityLifeCycleEventsListType activityLifeCycleEventsList,
            EventInfoListDocument infoList) {

        List<TEventInfo> list;
        if (infoList.getEventInfoList() == null) {
            list = infoList.addNewEventInfoList().getEventInfoList();
        } else {
            list = infoList.getEventInfoList().getEventInfoList();
        }

        List<EventInfo> eventInfoList = new ArrayList<EventInfo>();
        Map<Long, Boolean> isFaultMap = new HashMap<Long, Boolean>();
        for (TEventInfo tInfo : list) {
            EventInfo info = new EventInfo();
            info.setType(tInfo.getType());
            info.setName(tInfo.getName());
            info.setLineNumber(tInfo.getLineNumber());
            info.setTimestamp(tInfo.getTimestamp());
            info.setActivityId(tInfo.getActivityId());
            info.setActivityName(tInfo.getActivityName());
            if (tInfo.getName().equals("ActivityFailureEvent")) {
                if (isFaultMap.get(tInfo.getActivityId()) == null) {
                    isFaultMap.put(tInfo.getActivityId(), true);
                }
            } else if (tInfo.getName().equals("ActivityExecEndEvent")) {
                if (isFaultMap.get(tInfo.getActivityId()) != null) {
                    isFaultMap.remove(tInfo.getActivityId());
                }
                isFaultMap.put(tInfo.getActivityId(), false);
            }
            info.setActivityType(tInfo.getActivityType());
            info.setScopeId(tInfo.getScopeId());
            info.setScopeName(tInfo.getScopeName());

            eventInfoList.add(info);
        }

        for (EventInfo info : eventInfoList) {
            boolean isFault = isFaultMap.get(info.getActivityId()) == null ?
                    false : isFaultMap.get(info.getActivityId());
            info.setIsRecoveryRequired(isFault);
            activityLifeCycleEventsList.addEventInfo(info);
        }
    }

    /**
     * Resume a suspended instance
     *
     * @param iid Instance Id
     * @throws InstanceManagementException If the instance cannot be resumed due to the
     *                                     unavailability of Debugger support
     */
    public void resumeInstance(long iid) throws InstanceManagementException {
        try {
            isOperationIsValidForTheCurrentTenant(iid);
        } catch (IllegalAccessException ex) {
            handleError(ex);
        }
        /*
        We need debugger support in order to resume (since we have to force
        a reduction. If one is not available the getDebugger() method should
        throw a ProcessingException
        */
        DebuggerSupport debugSupport = getDebugger(iid);
        if (debugSupport == null) {
            String errMsg = "Cannot resume the instance " + iid + ", Debugger support not available";
            log.error(errMsg);
            throw new InstanceManagementException(errMsg);
        }
        debugSupport.resume(iid);
    }

    /**
     * Suspend an instance
     *
     * @param iid Instance Id
     * @throws InstanceManagementException If the instance cannot be suspended due to the
     *                                     unavailability of Debugger support
     */
    public void suspendInstance(long iid) throws InstanceManagementException {
        try {
            isOperationIsValidForTheCurrentTenant(iid);
        } catch (IllegalAccessException ex) {
            handleError(ex);
        }

        DebuggerSupport debugSupport = getDebugger(iid);
        if (debugSupport == null) {
            String errMsg = "Cannot suspend the instance " + iid + ", Debugger support not available";
            log.error(errMsg);
            throw new InstanceManagementException(errMsg);
        }
        debugSupport.suspend(iid);
    }

    /**
     * Terminate an instance
     *
     * @param iid Instance Id
     * @throws InstanceManagementException If the instance cannot be terminated due to the
     *                                     unavailability of Debugger support
     */

    public void terminateInstance(long iid) throws InstanceManagementException {
        try {
            isOperationIsValidForTheCurrentTenant(iid);
        } catch (IllegalAccessException ex) {
            handleError(ex);
        }
        DebuggerSupport debugSupport = getDebugger(iid);
        if (debugSupport == null) {
            String erMsg = "Cannot terminate the instance " + iid + ", Debugger support not available";
            log.error(erMsg);
            throw new InstanceManagementException(erMsg);
        }
        debugSupport.terminate(iid);
    }

    /**
     * Delete Instances that matches the filter
     *
     * @param filter Instance filter
     * @return Number of instances deleted
     * @throws InstanceManagementException If the filter is invalid or an exception occurred during
     *                                     instance deletion
     */
    public int deleteInstances(String filter, final boolean deleteMessageExchanges)
            throws InstanceManagementException {

        String tFilter = filter;

        Integer tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();

        TenantProcessStoreImpl tenantProcessStore = (TenantProcessStoreImpl) bpelServer.
                getMultiTenantProcessStore().getTenantsProcessStore(tenantId);

        if (isInvalidFilter(tFilter)) {
            String errMsg = "Invalid instance filter: " + tFilter;
            log.error(errMsg);
            throw new InstanceManagementException(errMsg);
        }

        if (!isSecureFilter(new InstanceFilter(tFilter), tenantProcessStore.getProcessConfigMap().keySet())) {
            String errMsg = "Instance deletion operation not permitted due to insecure filter: " +
                    tFilter;
            log.error(errMsg);
            throw new InstanceManagementException(errMsg);
        }

        if (!tFilter.contains(" pid=")) {
            tFilter = tFilter + getTenantsProcessList(tenantProcessStore.getProcessConfigMap().keySet());
        }

        final InstanceFilter instanceFilter = new InstanceFilter(tFilter);

        final List<Long> ret = new LinkedList<Long>();
        try {
            dbexec(new BpelDatabase.Callable<Object>() {
                public Object run(BpelDAOConnection conn) throws IllegalAccessException {
                    Collection<ProcessInstanceDAO> instances = conn.instanceQuery(instanceFilter);

                    // Doing this to avoid any half done operation.
                    // For example if filter returns set of instance which are not owned by this tenant we should
                    // not delete other instances also.
                    for (ProcessInstanceDAO instance : instances) {
                        isOperationIsValidForTheCurrentTenant(instance.getProcess().getProcessId());
                    }

                    for (ProcessInstanceDAO instance : instances) {
                        instance.delete(EnumSet.allOf(ProcessConf.CLEANUP_CATEGORY.class),
                                deleteMessageExchanges);
                        ret.add(instance.getInstanceId());
                    }
                    return null;
                }
            });
        } catch (Exception e) {
            String errMsg = "Exception during instance deletion. Filter: " +
                    instanceFilter.toString();
            log.error(errMsg, e);
            throw new InstanceManagementException(errMsg, e);
        }

        return ret.size();
    }

    private boolean isInvalidFilter(String filter) {
        if (filter == null) {
            return false;
        }
        Matcher expressionMatcher = Filter.__comparatorPattern.matcher(filter);
        return (!filter.trim().equals("") && !expressionMatcher.find());
    }

    /**
     * Get the {@link org.apache.ode.bpel.engine.DebuggerSupport} object for the given instance identifier.
     * Debugger support is required for operations that resume execution in some
     * way or manipulate the breakpoints.
     *
     * @param iid instance identifier
     * @return associated debugger support object
     * @throws ManagementException
     */
    private QName getProcess(final Long iid) {
        QName processId;
        try {
            processId = dbexec(new BpelDatabase.Callable<QName>() {
                public QName run(BpelDAOConnection conn) throws Exception {
                    ProcessInstanceDAO instance = conn.getInstance(iid);
                    return instance == null ? null : instance.getProcess().getProcessId();
                }
            });
        } catch (Exception e) {
            String errMsg = "Exception during instance: " + iid + " retrieval";
            log.error(errMsg, e);
            throw new ProcessingException(errMsg + ": " + e.toString(), e);
        }

        return processId;
    }

    /**
     * Get the {@link DebuggerSupport} object for the given process identifier.
     * Debugger support is required for operations that resume execution in some
     * way or manipulate the breakpoints.
     *
     * @param iid Instance Id
     * @return associated debugger support object
     * @throws InstanceManagementException If an error occurs
     */
    private DebuggerSupport getDebugger(final Long iid) throws InstanceManagementException {
        QName processId;
        try {
            processId = bpelServer.getODEBPELServer().getBpelDb().exec(new BpelDatabase.Callable<QName>() {
                public QName run(BpelDAOConnection conn) throws Exception {
                    ProcessInstanceDAO instance = conn.getInstance(iid);
                    return instance == null ? null : instance.getProcess().getProcessId();
                }
            });
        } catch (Exception e) {
            String errMsg = "Exception during instance " + iid + " retrieval";
            log.error(errMsg, e);
            throw new InstanceManagementException(errMsg, e);
        }

        return getDebugger(processId);
    }

    /**
     * Get the {@link DebuggerSupport} object for the given process identifier.
     * Debugger support is required for operations that resume execution in some
     * way or manipulate the breakpoints.
     *
     * @param processId process identifier
     * @return associated debugger support object
     */
    protected final DebuggerSupport getDebugger(QName processId) {
        BpelProcess process = ((BpelEngineImpl) bpelServer.getODEBPELServer().
                getEngineUnsecured())._activeProcesses.get(processId);
        if (process == null) {
            String errMsg = "The process \"" + processId + "\" does not exist.";
            throw new ProcessNotFoundException(errMsg);
        }

        return process.getDebuggerSupport();
    }


    private InstanceInfoType getInstanceInformation(final long iid)
            throws InstanceManagementException {
        final InstanceInfoType instanceInfo = new InstanceInfoType();
        instanceInfo.setIid(Long.toString(iid));
        dbexec(new BpelDatabase.Callable<Object>() {
            public Object run(BpelDAOConnection conn) throws InstanceManagementException {
                ProcessInstanceDAO instance = conn.getInstanceEagerly(iid, false);

                if (instance == null) {
                    String errMsg = "Instance " + iid + " not found.";
                    log.error(errMsg);
                    throw new InstanceManagementException(errMsg);
                }

                fillInstanceInfo(instanceInfo, instance);
                return null;
            }
        });
        return instanceInfo;
    }

    private InstanceInfoWithEventsType getInstanceInformationWithEvents(final long iid)
            throws InstanceManagementException {
        final InstanceInfoWithEventsType instanceInfoWithEvents = new InstanceInfoWithEventsType();
        instanceInfoWithEvents.setIid(Long.toString(iid));

        dbexec(new BpelDatabase.Callable<Object>() {
            public Object run(BpelDAOConnection conn) throws InstanceManagementException {
                ProcessInstanceDAO instance = conn.getInstanceEagerly(iid, true);

                if (instance == null) {
                    String errMsg = "Instance " + iid + " not found.";
                    log.error(errMsg);
                    throw new InstanceManagementException(errMsg);
                }

                fillInstanceInfoWithEvents(instanceInfoWithEvents, instance);
                return null;
            }
        });

        return instanceInfoWithEvents;
    }

    private void fillInstanceInfo(InstanceInfoType instanceInfo,
                                  ProcessInstanceDAO processInstance)
            throws InstanceManagementException {

        // ((ProcessConfigurationImpl) getTenantProcessForCurrentSession().getProcessConfiguration(QName.valueOf(instanceInfo.getPid()))).getEventsEnabled();

        instanceInfo.setIid(processInstance.getInstanceId().toString());
        instanceInfo.setPid(processInstance.getProcess().getProcessId().toString());
        instanceInfo.setDateStarted(toCalendar(processInstance.getCreateTime()));
        instanceInfo.setDateLastActive(toCalendar(processInstance.getLastActiveTime()));
        instanceInfo.setStatus(odeInstanceStatusToManagementAPIStatus(processInstance.getState()));
        instanceInfo.setIsEventsEnabled(((ProcessConfigurationImpl) getTenantProcessForCurrentSession().getProcessConfiguration(QName.valueOf(instanceInfo.getPid()))).getEventsEnabled());

        fillFaultAndFailure(processInstance, instanceInfo);

        if (processInstance.getRootScope() != null) {
            instanceInfo.setRootScope(
                    getScopeInfo(processInstance.getRootScope()));
        }
    }

    private void fillInstanceInfoWithEvents(InstanceInfoWithEventsType instanceInfoWithEvents,
                                            ProcessInstanceDAO processInstance)
            throws InstanceManagementException {
        instanceInfoWithEvents.setIid(processInstance.getInstanceId().toString());
        instanceInfoWithEvents.setPid(processInstance.getProcess().getProcessId().toString());
        instanceInfoWithEvents.setDateStarted(toCalendar(processInstance.getCreateTime()));
        instanceInfoWithEvents.setDateLastActive(toCalendar(processInstance.getLastActiveTime()));
        instanceInfoWithEvents.setStatus(
                odeInstanceStatusToManagementAPIStatus(processInstance.getState()));

        fillFaultAndFailure(processInstance, instanceInfoWithEvents);

        if (processInstance.getRootScope() != null) {
            instanceInfoWithEvents.setRootScope(
                    getScopeInfoWithEvents(processInstance.getRootScope()));
        }
    }

    /**
     * Use fillFaultAndFailure(ProcessInstanceDAO, InstanceInfoType) to fill the
     * instanceInfoWithEvents
     *
     * @param instance               Process Instance DAO
     * @param instanceInfoWithEvents Instance info with events
     */
    private void fillFaultAndFailure(ProcessInstanceDAO instance,
                                     InstanceInfoWithEventsType instanceInfoWithEvents) {
        InstanceInfoType instanceInfo = new InstanceInfoType();
        fillFaultAndFailure(instance, instanceInfo);

        if (instance.getFault() != null) {
            instanceInfoWithEvents.setFaultInfo(instanceInfo.getFaultInfo());
        }
        if (instance.getActivityFailureCount() > 0) {
            instanceInfoWithEvents.setFailuresInfo(instanceInfo.getFailuresInfo());
        }
    }


    private void fillFaultAndFailure(ProcessInstanceDAO instance, InstanceInfoType instanceInfo) {
        if (instance.getFault() != null) {
            FaultDAO fault = instance.getFault();
            FaultInfoType faultInfo = new FaultInfoType();
            faultInfo.setName(fault.getName());
            faultInfo.setExplanation(fault.getExplanation());
            faultInfo.setLineNumber(fault.getLineNo());
            faultInfo.setAiid(fault.getActivityId());
            Data_type0 data = new Data_type0();
            if (fault.getData() == null) {
                OMFactory omFac = OMAbstractFactory.getOMFactory();
                OMElement faultDataEle = omFac.createOMElement("no-data", null);
                faultDataEle.setText("No data available.");
                data.addExtraElement(faultDataEle);
            } else {
                data.addExtraElement(OMUtils.toOM(fault.getData(), OMAbstractFactory.getOMFactory()));
            }
            faultInfo.setData(data);
            instanceInfo.setFaultInfo(faultInfo);
        }
        if (instance.getActivityFailureCount() > 0) {
            FailuresInfoType failuresInfo = new FailuresInfoType();
            failuresInfo.setCount(instance.getActivityFailureCount());
            failuresInfo.setDateFailure(toCalendar(instance.getActivityFailureDateTime()));
            instanceInfo.setFailuresInfo(failuresInfo);
        }
    }

    /**
     * Execute a database transaction, unwrapping nested
     * {@link org.apache.ode.bpel.pmapi.ManagementException}s.
     *
     * @param callable action to run
     * @return object of type T
     * @throws InstanceManagementException if exception occurred during transaction
     */
    private <T> T dbexec(BpelDatabase.Callable<T> callable) throws InstanceManagementException {
        try {
            BpelDatabase bpelDb = bpelServer.getODEBPELServer().getBpelDb();
            return bpelDb.exec(callable);
        } catch (Exception ex) {
            String errMsg = "Exception during database operation";
            log.error(errMsg, ex);
            throw new InstanceManagementException(errMsg, ex);
        }
    }

    private InstanceStatus odeInstanceStatusToManagementAPIStatus(short status)
            throws InstanceManagementException {
        switch (status) {
            case ProcessState.STATE_NEW:
            case ProcessState.STATE_READY:
            case ProcessState.STATE_ACTIVE:
                return InstanceStatus.ACTIVE;
            case ProcessState.STATE_COMPLETED_OK:
                return InstanceStatus.COMPLETED;
            case ProcessState.STATE_COMPLETED_WITH_FAULT:
                return InstanceStatus.FAILED;
            case ProcessState.STATE_SUSPENDED:
                return InstanceStatus.SUSPENDED;
            case ProcessState.STATE_TERMINATED:
                return InstanceStatus.TERMINATED;
        }

        String errMsg = "Encountered unexpected instance state: " + status;
        log.error(errMsg);
        throw new InstanceManagementException(errMsg);
    }

    private ScopeStatusType odeScopeStatusToManagementAPIStatus(ScopeStateEnum status) {
        return scopeStatusMap.get(status);
    }

    private LimitedInstanceInfoType createLimitedInstanceInfoObject(ProcessInstanceDAO instanceDAO)
            throws InstanceManagementException {
        LimitedInstanceInfoType instanceInfo = new LimitedInstanceInfoType();
        instanceInfo.setIid(Long.toString(instanceDAO.getInstanceId()));
        instanceInfo.setPid(instanceDAO.getProcess().getProcessId().toString());
        instanceInfo.setStatus(odeInstanceStatusToManagementAPIStatus(instanceDAO.getState()));
        instanceInfo.setDateLastActive(toCalendar(instanceDAO.getLastActiveTime()));
        instanceInfo.setDateStarted(toCalendar(instanceDAO.getCreateTime()));
        return instanceInfo;
    }

    private String getTenantsProcessList(Set<QName> processIds) {
        StringBuilder tenantsProcessList = new StringBuilder();
        tenantsProcessList.append(" ");
        tenantsProcessList.append("pid=");
        for (QName pid : processIds) {
            tenantsProcessList.append(pid.toString());
            tenantsProcessList.append("|");
        }
        tenantsProcessList.append(" ");
        return tenantsProcessList.toString();
    }

    private Calendar toCalendar(Date dtime) {
        if (dtime == null) {
            return null;
        }

        Calendar c = (Calendar) calendar.clone();
        c.setTime(dtime);
        return c;
    }

    private ScopeInfoType getScopeInfo(ScopeDAO scope) throws InstanceManagementException {
        final ScopeInfoType scopeInfo = new ScopeInfoType();

        /*dbexec(new BpelDatabase.Callable<Object>() {
            public Object run(BpelDAOConnection conn) throws InstanceManagementException {
                ScopeDAO scope = conn.getScopeEagerly(siid);
                if (scope == null) {
                    String errMsg = "Scope " + siid + " not found.";
                    log.error(errMsg);
                    throw new InstanceManagementException(errMsg);
                }
                fillScopeInfo(scopeInfo, scope);
                return null;
            }
        });*/
        /*ScopeDAO scope = conn.getScopeEagerly(siid);*/
        if (scope == null) {
           //String errMsg = "Scope "  + siid +" not found.";
           String errMsg = "Scope "   +" not found.";
            log.error(errMsg);
            throw new InstanceManagementException(errMsg);
        }
        fillScopeInfo(scopeInfo, scope);
        return scopeInfo;
    }

    private ScopeInfoWithEventsType getScopeInfoWithEvents(ScopeDAO scope)
            throws InstanceManagementException {
        final ScopeInfoWithEventsType scopeInfoWithEvents = new ScopeInfoWithEventsType();

        /*dbexec(new BpelDatabase.Callable<Object>() {
            public Object run(BpelDAOConnection conn) throws InstanceManagementException {
                ScopeDAO scope = conn.getScopeEagerly(siid);
                if (scope == null) {
                    String errMsg = "Scope " + siid + " not found.";
                    log.error(errMsg);
                    throw new InstanceManagementException(errMsg);
                }
                fillScopeInfoWithEvents(scopeInfoWithEvents, scope);
                return null;
            }
        });*/
        
        fillScopeInfoWithEvents(scopeInfoWithEvents, scope);

        return scopeInfoWithEvents;
    }

    private void fillScopeInfo(ScopeInfoType scopeInfo, ScopeDAO scope) {
        scopeInfo.setSiid(scope.getScopeInstanceId().toString());
        scopeInfo.setName(scope.getName());
        scopeInfo.setStatus(odeScopeStatusToManagementAPIStatus(scope.getState()));

        Children_type0 childScopes = new Children_type0();
        if(scope.isChildrenExist()){
        	
        }
        for (ScopeDAO childScope : scope.getChildScopes()) {
            ScopeInfoType childScopeInfo = new ScopeInfoType();
            fillScopeInfo(childScopeInfo, childScope);
            childScopes.addChildRef(childScopeInfo);
        }

        scopeInfo.setChildren(childScopes);
        scopeInfo.setVariables(getVariables(scope));
        if (!scope.getCorrelationDTOs().isEmpty()) {
            scopeInfo.setCorrelationSets(getCorrelationPropertires(scope));
        }
        scopeInfo.setActivities(getActivities(scope));
    }

    private void fillScopeInfoWithEvents(ScopeInfoWithEventsType scopeInfoWithEvents, ScopeDAO scope) {
        scopeInfoWithEvents.setSiid(scope.getScopeInstanceId().toString());
        scopeInfoWithEvents.setName(scope.getName());
        scopeInfoWithEvents.setStatus(odeScopeStatusToManagementAPIStatus(scope.getState()));

        ChildrenWithEvents_type0 childScopesWithEvents = new ChildrenWithEvents_type0();
        for (ScopeDAO childScope : scope.getChildScopes()) {
            ScopeInfoWithEventsType childScopeInfoWithEvents = new ScopeInfoWithEventsType();
            fillScopeInfoWithEvents(childScopeInfoWithEvents, childScope);
            childScopesWithEvents.addChildWithEventsRef(childScopeInfoWithEvents);
        }

        scopeInfoWithEvents.setChildrenWithEvents(childScopesWithEvents);
        //scopeInfoWithEvents.setVariablesWithEvents(getVariablesWithEvents(scope));         //TODO:
        //TODO: Just need to change the schema s.t. avoid CorrelationSets_type1 and remove the comment
//        if (!scope.getCorrelationSets().isEmpty()) {
////            scopeInfoWithEvents.setCorrelationSets(getCorrelationPropertires(scope));
//        }
        scopeInfoWithEvents.setActivitiesWithEvents(getActivitiesWithEvents(scope));

    }

    private VariablesWithEvents_type0 getVariablesWithEvents(ScopeDAO scope) {
//        VariablesWithEvents_type0 variablesWE = new VariablesWithEvents_type0();

        scope.listEvents();

        /*Variables_type0 variables = new Variables_type0();
        for (XmlDataDAO var : scope.getVariables()) {
            VariableRefType varRef = new VariableRefType();
            varRef.setIid(var.getScopeDAO().getProcessInstance().getInstanceId().toString());
            varRef.setSiid(var.getScopeDAO().getScopeInstanceId().toString());
            varRef.setName(var.getName());

            VariableInfoType varInfoType = new VariableInfoType();
            varInfoType.setSelf(varRef);
//            Value_type0 varValue = new Value_type0();
//            varValue.addExtraElement(OMUtils.toOM((Element)var.get(), OMAbstractFactory.getOMFactory()));

            Node val = var.get();
            Value_type0 value = new Value_type0();
            if (val == null) {
                OMFactory omFac = OMAbstractFactory.getOMFactory();
                OMElement emptyEle = omFac.createOMElement("empty-value", null);
                emptyEle.setText("Nil");
                value.addExtraElement(emptyEle);
            } else {
                if (val.getLocalName().equals("temporary-simple-type-wrapper")) {
                    OMFactory omFac = OMAbstractFactory.getOMFactory();
                    if (val.getFirstChild() != null) {
                        OMElement tempSimpleTypeWrapper = omFac.createOMElement("temporary-simple-type-wrapper", null);
                        tempSimpleTypeWrapper.setText(val.getFirstChild().getNodeValue());
                        value.addExtraElement(tempSimpleTypeWrapper);
                    } else {
                        OMElement emptyEle = omFac.createOMElement("empty-value", null);
                        emptyEle.setText("Nil");
                        value.addExtraElement(emptyEle);
                    }
                } else {
                    value.addExtraElement(OMUtils.toOM((org.w3c.dom.Element) val, OMAbstractFactory.getOMFactory()));
                }
            }
            varInfoType.setValue(value);
            variables.addVariableInfo(varInfoType);
        }

        return variables;*/
        return null;
    }

    private Activities_type0 getActivities(ScopeDAO scope) {
        Activities_type0 activities = new Activities_type0();
        Collection<ActivityRecoveryDAO> recoveries = scope.getProcessInstance().getActivityRecoveries();
        /*List<BpelEvent> events = scope.listEvents();*/
        Set<EventDAOImpl> eventsEntities = ((ScopeDAOImpl)scope).getEvents();
        List<BpelEvent> events = new ArrayList<BpelEvent>();
        for(EventDAOImpl event : eventsEntities){
        	events.add(event.getEvent());
        }
        ActivityStateDocumentBuilder b = new ActivityStateDocumentBuilder();

        for (BpelEvent e : events) {
            b.onEvent(e);
        }

        for (ActivityInfoDocument ai : b.getActivities()) {
            for (ActivityRecoveryDAO recovery : recoveries) {
                if (String.valueOf(recovery.getActivityId()).equals(ai.getActivityInfo().getAiid())) {
                    TFailureInfo failure = ai.getActivityInfo().addNewFailure();
                    failure.setReason(recovery.getReason());
                    failure.setDtFailure(toCalendar(recovery.getDateTime()));
                    failure.setActions(recovery.getActions());
                    failure.setRetries(recovery.getRetries());
                    ai.getActivityInfo().setStatus(TActivityStatus.FAILURE);
                }
            }
            ActivityInfoType activity = new ActivityInfoType();
            fillActivityInfo(activity, ai.getActivityInfo());
            activities.addActivityInfo(activity);
        }
        return activities;
    }

    private ActivitiesWithEvents_type0 getActivitiesWithEvents(ScopeDAO scope) {
        ActivitiesWithEvents_type0 activitiesWithEvents = new ActivitiesWithEvents_type0();
        Collection<ActivityRecoveryDAO> recoveries = scope.getProcessInstance().getActivityRecoveries();
        //List<BpelEvent> events = scope.listEvents();
        Set<EventDAOImpl> eventsEntities = ((ScopeDAOImpl)scope).getEvents();
        List<BpelEvent> events = new ArrayList<BpelEvent>();
        for(EventDAOImpl event : eventsEntities){
        	events.add(event.getEvent());
        }
        ActivityStateAndEventDocumentBuilder docBuilder = new ActivityStateAndEventDocumentBuilder();

        for (BpelEvent e : events) {
            docBuilder.onEvent(e);
        }

        for (ActivityInfoWithEventsDocument aweDoc : docBuilder.getActivitiesWithEvents()) {
            for (ActivityRecoveryDAO recovery : recoveries) {
                if (String.valueOf(recovery.getActivityId()).equals(aweDoc.getActivityInfoDoc().
                        getActivityInfo().getAiid())) {
                    TFailureInfo failure = aweDoc.getActivityInfoDoc().getActivityInfo().addNewFailure();
                    failure.setReason(recovery.getReason());
                    failure.setDtFailure(toCalendar(recovery.getDateTime()));
                    failure.setActions(recovery.getActions());
                    failure.setRetries(recovery.getRetries());
                    aweDoc.getActivityInfoDoc().getActivityInfo().setStatus(TActivityStatus.FAILURE);
                }
            }

            ActivityInfoWithEventsType activityWE = new ActivityInfoWithEventsType();
            TActivityInfo actInfoDoc = aweDoc.getActivityInfoDoc().getActivityInfo();
            TEventInfoList evtInfoList = aweDoc.getEventInfoList().getEventInfoList();
            //add activityInfo
            //add event info

            ActivityInfoType activity = fillActivityInfo(new ActivityInfoType(), actInfoDoc);
            /*XmlOptions opt = new XmlOptions();
            opt = opt.setSaveOuter();*/
            EventInfoList eventList = fillEventInfo(new EventInfoList(), evtInfoList);

            activityWE.setActivityInfo(activity);
            activityWE.setActivityEventsList(eventList);
            activitiesWithEvents.addActivityInfoWithEvents(activityWE);
        }
        return activitiesWithEvents;
    }

    private EventInfoList fillEventInfo(EventInfoList eventInfoList, TEventInfoList infoList) {

        EventInfo[] infoArray = new EventInfo[infoList.sizeOfEventInfoArray()];

        List<TEventInfo> list = infoList.getEventInfoList();

        for (int i = 0; i < list.size(); i++) {
            infoArray[i] = new EventInfo();
            EventInfo eventInfo = infoArray[i];
            TEventInfo listElem = list.get(i);

            eventInfo.setName(listElem.getName());
            eventInfo.setType(listElem.getType());
            eventInfo.setLineNumber(listElem.getLineNumber());
            eventInfo.setTimestamp(listElem.getTimestamp());
            //TODO: need to change schema and validate the methods
        }

        /*
        for (int i = 0; i < list.size(); i++) {
            infoArray[i] = new TEventInfo
            TEventInfoListSequence tEventInfo = infoArray[i];
            //tEventInfo = new TEventInfo();
            org.apache.ode.bpel.pmapi.TEventInfo listElem = list.get(i);
            tEventInfo.setActivityDefinitionId(listElem.getActivityDefinitionId());
            //tEventInfo.setActivityFailureReason(listElem.getActivityFailureReason());
            tEventInfo.setActivityId(listElem.getActivityId());
            tEventInfo.setActivityName(listElem.getActivityName());
            //tEventInfo.setActivityRecoveryAction(listElem.getActivityRecoveryAction());
            tEventInfo.setActivityType(listElem.getActivityType());
            //tEventInfo.setCorrelationKey(listElem.getCorrelationKey());
            //tEventInfo.setCorrelationSet(listElem.getCorrelationSet());
            //tEventInfo.setExplanation(listElem.getExplanation());
            //tEventInfo.setExpression(listElem.getExpression());
            //tEventInfo.setFault(listElem.getFault());
            tEventInfo.setProcessId(listElem.getProcessId());
            tEventInfo.setScopeId(listElem.getScopeId());
            tEventInfo.setScopeName(listElem.getScopeName());

        }*/
        eventInfoList.setEventInfo(infoArray);

        return eventInfoList;
    }

    private CorrelationSets_type0 getCorrelationPropertires(ScopeDAO scope) {
        CorrelationSets_type0 correlationSets = new CorrelationSets_type0();
        for (CorrelationSetDAO correlationSetDAO : scope.getCorrelationDTOs()) {
            CorrelationSet_type0 correlationset = new CorrelationSet_type0();
            correlationset.setCsetid(correlationSetDAO.getCorrelationSetId().toString());
            correlationset.setName(correlationSetDAO.getName());
            for (Map.Entry<QName, String> property : correlationSetDAO.getProperties().entrySet()) {
                CorrelationPropertyType prop = new CorrelationPropertyType();
                prop.setCsetid(correlationSetDAO.getCorrelationSetId().toString());
                prop.setPropertyName(property.getKey());
                prop.setString(property.getValue());
                correlationset.addCorrelationProperty(prop);
            }
            correlationSets.addCorrelationSet(correlationset);
        }
        return correlationSets;
    }

    private Variables_type0 getVariables(ScopeDAO scope) {
        Variables_type0 variables = new Variables_type0();
        for (XmlDataDAO var : scope.getVariablesDTOs()) {
            VariableRefType varRef = new VariableRefType();
            /*varRef.setIid(var.getScopeDAO().getProcessInstance().getInstanceId().toString());
            varRef.setSiid(var.getScopeDAO().getScopeInstanceId().toString());*/
            varRef.setIid(scope.getProcessInstance().getInstanceId().toString());
            varRef.setSiid(scope.getScopeInstanceId().toString());
            varRef.setName(var.getName());

            VariableInfoType varInfoType = new VariableInfoType();
            varInfoType.setSelf(varRef);
//            Value_type0 varValue = new Value_type0();
//            varValue.addExtraElement(OMUtils.toOM((Element)var.get(), OMAbstractFactory.getOMFactory()));

            Node val = var.get();
            Value_type0 value = new Value_type0();
            if (val == null) {
                OMFactory omFac = OMAbstractFactory.getOMFactory();
                OMElement emptyEle = omFac.createOMElement("empty-value", null);
                emptyEle.setText("Nil");
                value.addExtraElement(emptyEle);
            } else {
                if (val.getNodeType() == Node.TEXT_NODE) {
                    OMFactory omFac = OMAbstractFactory.getOMFactory();
                    OMElement tempSimpleTypeWrapper = omFac.createOMElement(var.getName(), null);
                    tempSimpleTypeWrapper.setText(val.getTextContent());
                    value.addExtraElement(tempSimpleTypeWrapper);
                } else {
                    value.addExtraElement(OMUtils.toOM((org.w3c.dom.Element) val,
                            OMAbstractFactory.getOMFactory()));
                }
            }
            varInfoType.setValue(value);
            variables.addVariableInfo(varInfoType);
        }

        return variables;
    }

    private ActivityInfoType fillActivityInfo(ActivityInfoType activity, TActivityInfo actInfo) {
        activity.setAiid(actInfo.getAiid());
        activity.setDateCompleted(actInfo.getDtCompleted());
        activity.setDateEnabled(actInfo.getDtEnabled());
        activity.setDateStarted(actInfo.getDtStarted());
        activity.setName(actInfo.getName());
        activity.setType(actInfo.getType());

        TFailureInfo failure = actInfo.getFailure();

        if (failure != null) {
            FailureInfoType failureInfo = new FailureInfoType();
            failureInfo.setActions(failure.getActions());
            failureInfo.setDateFailure(failure.getDtFailure());
            failureInfo.setReason(failure.getReason());
            failureInfo.setRetries(failure.getRetries());

            activity.setFailure(failureInfo);
        }
        activity.setStatus(activityStatusMap.get(actInfo.getStatus()));

        return activity;
    }

    /**
     * Check whether the instance belongs to the current tenant. If not, don't allow any operations.
     *
     * @param iid instance id
     * @throws IllegalAccessException if instance doesn't belong to the current tenant
     * @throws ProcessingException    if there a error getting instance data
     */
    private void isOperationIsValidForTheCurrentTenant(final long iid) throws IllegalAccessException, ProcessingException {
        QName processId = getProcess(iid);
        TenantProcessStoreImpl processStore = getTenantProcessForCurrentSession();
        if (!processStore.containsProcess(processId)) {
            log.error("Trying to invoke a illegal operation. Instance ID:" + iid);
            throw new IllegalAccessException("Operation is not permitted.");
        }
    }

    /**
     * Check whether process belongs to the current tenant. If not, don't allow any operations.
     *
     * @param pid process id
     * @throws IllegalAccessException if process doesn't belong to the current tenant
     */
    private void isOperationIsValidForTheCurrentTenant(final QName pid) throws IllegalAccessException {
        if (!getTenantProcessForCurrentSession().containsProcess(pid)) {
            log.error("Trying to invoke a illegal operation. Process ID:" + pid);
            throw new IllegalAccessException("Operation is not permitted.");
        }
    }

    private TenantProcessStoreImpl getTenantProcessForCurrentSession() {

        Integer tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();

        return (TenantProcessStoreImpl) bpelServer
                .getMultiTenantProcessStore().getTenantsProcessStore(tenantId);
    }

    private boolean isSecureFilter(InstanceFilter filter, Set<QName> processIds) {
        List<String> pids = filter.getPidFilter();
        Set<String> strPids = new HashSet<String>();

        if (pids != null) {

            for (QName pidQName : processIds) {
                strPids.add(pidQName.toString());
            }

            for (String pid : pids) {
                if (!strPids.contains(pid)) {
                    return false;
                }
            }

        }
        return true;
    }
}

