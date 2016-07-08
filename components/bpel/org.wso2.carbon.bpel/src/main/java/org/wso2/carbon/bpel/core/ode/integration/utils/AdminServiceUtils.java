/**
 * Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.bpel.core.ode.integration.utils;

import org.apache.ode.bpel.iapi.ProcessConf;
import org.apache.ode.bpel.iapi.ProcessState;
import org.wso2.carbon.bpel.core.ode.integration.BPELServerImpl;
import org.wso2.carbon.bpel.core.ode.integration.store.TenantProcessStoreImpl;
import org.wso2.carbon.bpel.skeleton.ode.integration.mgt.services.types.LimitedProcessInfoType;
import org.wso2.carbon.bpel.skeleton.ode.integration.mgt.services.types.ProcessStatus;
import org.wso2.carbon.context.CarbonContext;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import javax.xml.namespace.QName;

/**
 * Utility methods to be used for BPEL admin services.
 */
public class AdminServiceUtils {
    public static LimitedProcessInfoType createLimitedProcessInfoObject(ProcessConf processConf) {
        LimitedProcessInfoType processInfoObject = new LimitedProcessInfoType();
        processInfoObject.setPid(processConf.getProcessId().toString());
        processInfoObject.setDeployedDate(toCalendar(processConf.getDeployDate()));
        processInfoObject.setVersion(processConf.getVersion());
        if (processConf.getState() == ProcessState.RETIRED) {
            processInfoObject.setStatus(ProcessStatus.RETIRED);
        } else if (processConf.getState() == ProcessState.DISABLED) {
            processInfoObject.setStatus(ProcessStatus.DISABLED);
        } else {
            processInfoObject.setStatus(ProcessStatus.ACTIVE);
        }

        //check and set the olderVersion of the process
        if (processConf.getState() == ProcessState.RETIRED) {
            processInfoObject.setStatus(ProcessStatus.RETIRED);
            processInfoObject.setOlderVersion(isOlderVersion(processConf, getTenantProcessStore()));
        } else if (processConf.getState() == ProcessState.DISABLED) {
            processInfoObject.setStatus(ProcessStatus.DISABLED);
            processInfoObject.setOlderVersion(0);
        } else {
            processInfoObject.setStatus(ProcessStatus.ACTIVE);
            processInfoObject.setOlderVersion(0);
        }

        processInfoObject.setPackageName(processConf.getPackage());

        return processInfoObject;
    }

    public static Calendar toCalendar(Date dtime) {
        if (dtime == null) {
            return null;
        }

        Calendar c = Calendar.getInstance();
        c.setTime(dtime);
        return c;
    }

    public static int isOlderVersion(ProcessConf pconf, TenantProcessStoreImpl tenantProcessStore) {
        Set<QName> processIDs = tenantProcessStore.getProcessConfigMap().keySet();
        String filter = pconf.getProcessId().getLocalPart().substring(0, pconf.getProcessId().
                getLocalPart().lastIndexOf("-"));

        // Name filter can be implemented using only the PIDs.
        final Pattern pattern = Pattern.compile(filter.replace("*", ".*") + "(-\\d*)?");
        final List<QName> pids = new ArrayList<QName>();
        for (QName pid : processIDs) {
            if (pattern.matcher(pid.getLocalPart()).matches()) {
                pids.add(pid);
            }
        }

        if (pids.size() > 1) {
            long currentVersion = pconf.getVersion();
            for (QName pid : pids) {
                long tempVersion = Long.parseLong(pid.getLocalPart().substring(pid.getLocalPart().
                        lastIndexOf("-") + 1));
                if (tempVersion > currentVersion) {
                    return 1;
                }
            }
        }
        return 0;
    }

    public static TenantProcessStoreImpl getTenantProcessStore() {
        Integer tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();

        return ((TenantProcessStoreImpl) BPELServerImpl.getInstance().
                getMultiTenantProcessStore().getTenantsProcessStore(tenantId));


    }
}
