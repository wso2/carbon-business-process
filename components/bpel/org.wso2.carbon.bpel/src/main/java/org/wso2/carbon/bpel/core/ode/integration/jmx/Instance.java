/*
 * Copyright (c) 2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.bpel.core.ode.integration.jmx;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.common.InstanceFilter;
import org.apache.ode.bpel.common.ProcessState;
import org.apache.ode.bpel.dao.BpelDAOConnection;
import org.apache.ode.bpel.dao.ProcessInstanceDAO;
import org.apache.ode.bpel.engine.BpelDatabase;
import org.wso2.carbon.bpel.core.BPELConstants;
import org.wso2.carbon.bpel.core.ode.integration.BPELServerImpl;
import org.wso2.carbon.bpel.core.ode.integration.store.TenantProcessStoreImpl;
import org.wso2.carbon.bpel.skeleton.ode.integration.mgt.services.InstanceManagementException;
import org.wso2.carbon.bpel.skeleton.ode.integration.mgt.services.types.InstanceStatus;
import org.wso2.carbon.bpel.skeleton.ode.integration.mgt.services.types.LimitedInstanceInfoType;
import org.wso2.carbon.bpel.skeleton.ode.integration.mgt.services.types.PaginatedInstanceList;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import javax.xml.namespace.QName;
import java.lang.String;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Set;

public class Instance extends AbstractAdmin implements InstanceMXBean {
    private BPELServerImpl bpelServer = BPELServerImpl.getInstance();
    private static Log log = LogFactory.getLog(Instance.class);
    private Calendar calendar = Calendar.getInstance();

    @Override
    public String[] getInstanceInfoFromInstanceId() {
        StringBuffer buffer = new StringBuffer();
        PaginatedInstanceList paginatedInstanceList;
        String instanceInfoArray[]=null;
        int arrayCount=0;
        try {
            paginatedInstanceList = getPaginatedInstanceList(" ", "-last-active", 200, 0);
            LimitedInstanceInfoType[] instanceArray = paginatedInstanceList.getInstance();
            instanceInfoArray= new String[instanceArray.length];
            for (LimitedInstanceInfoType instance : instanceArray) {
                buffer.append("Instance id="+instance.getIid());
                buffer.append("  ");
                buffer.append("Process id="+instance.getPid());
                buffer.append(" ");
                buffer.append("Status ="+instance.getStatus());
                buffer.append(" ");
                buffer.append("Started Date="+instance.getDateStarted().get(5));
                buffer.append("-"+instance.getDateStarted().get(2));
                buffer.append("-"+instance.getDateStarted().get(1));
                buffer.append("  ");
                buffer.append(instance.getDateStarted().get(11));
                buffer.append(":"+instance.getDateStarted().get(12));
                buffer.append(":"+instance.getDateStarted().get(13));
                buffer.append("  ");

                buffer.append("Date Last Activate="+instance.getDateLastActive().get(5));
                buffer.append("-"+instance.getDateLastActive().get(2));
                buffer.append("-"+instance.getDateLastActive().get(1));
                buffer.append("  ");
                buffer.append(instance.getDateLastActive().get(11));
                buffer.append(":"+instance.getDateLastActive().get(12));
                buffer.append(":"+instance.getDateLastActive().get(13));
                buffer.append("  ");
                instanceInfoArray[arrayCount]=buffer.toString();
                arrayCount++;
                buffer.delete(0,buffer.length());
            }
        } catch (InstanceManagementException e) {
            String errMsg="failed to get instance information from instance id";
            log.error(errMsg, e);
        }
       return instanceInfoArray;
    }

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

    private Calendar toCalendar(Date dtime) {
        if (dtime == null) {
            return null;
        }

        Calendar c = (Calendar) calendar.clone();
        c.setTime(dtime);
        return c;
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


}
