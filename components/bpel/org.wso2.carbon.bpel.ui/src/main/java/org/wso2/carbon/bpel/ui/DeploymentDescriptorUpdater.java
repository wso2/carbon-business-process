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

package org.wso2.carbon.bpel.ui;

import org.wso2.carbon.bpel.stub.mgt.types.InvokeServiceListType;
import org.wso2.carbon.bpel.stub.mgt.types.MexInterpreterListType;
import org.wso2.carbon.bpel.stub.mgt.types.PropertyListType;
import org.wso2.carbon.bpel.stub.mgt.types.ProvideServiceListType;
import org.wso2.carbon.bpel.stub.mgt.types.ScopeEventType;

import java.util.Arrays;

/**
 * This is the jsp bean class used to keep the form data of the deployment descriptor editor when
 * the form is updated at runtime
 */
@Deprecated
public class DeploymentDescriptorUpdater {

    private static final String CHECKED = "checked";


    private String[] events;

    private String gentype;   // this is the 'generate' type for radio buttons in process level events table

    private String[] successtypecleanups;

    private String[] failuretypecleanups;

    private String processstate;

    private String inmemorystatus;

    private InvokeServiceListType invokedServiceList;

    private ProvideServiceListType provideServiceList;

    private MexInterpreterListType mexInterceptors;

    private PropertyListType propertyList;

    private ScopeEventType[] scopeEvents;

    public DeploymentDescriptorUpdater() {
        this.events = new String[0];
        this.gentype = null;
        this.successtypecleanups = new String[0];
        this.failuretypecleanups = new String[0];
        this.processstate = null;
        this.inmemorystatus = null;
        this.invokedServiceList = null;
        this.provideServiceList = null;
        this.mexInterceptors = null;
        this.propertyList = null;
        this.scopeEvents = null;
    }

    public String getInmemorystatus() {
        return inmemorystatus;
    }

    public void setInmemorystatus(String inmemorystatus) {
        this.inmemorystatus = inmemorystatus;
    }

    public String[] getEvents() {
        return Arrays.copyOf(events, events.length);
    }

    public void setEvents(String[] events) {
        if (events != null) {
            this.events = Arrays.copyOf(events, events.length);
        }
    }

    public String getGentype() {
        return gentype;
    }

    public void setGentype(String gentype) {
        this.gentype = gentype;
    }

    public String[] getSuccesstypecleanups() {
        return Arrays.copyOf(successtypecleanups, successtypecleanups.length);
    }

    public void setSuccesstypecleanups(String[] successtypecleanups) {
        if (successtypecleanups != null) {
            this.successtypecleanups = Arrays.copyOf(successtypecleanups, successtypecleanups.length);
        }
    }

    public String[] getFailuretypecleanups() {
        return Arrays.copyOf(failuretypecleanups, failuretypecleanups.length);
    }

    public void setFailuretypecleanups(String[] failuretypecleanups) {
        if (failuretypecleanups != null) {
            this.failuretypecleanups = Arrays.copyOf(failuretypecleanups, failuretypecleanups.length);
        }
    }

    public InvokeServiceListType getInvokedServiceList() {
        return invokedServiceList;
    }

    public void setInvokedServiceList(InvokeServiceListType invokedServiceList) {

        this.invokedServiceList = invokedServiceList;
    }

    public ProvideServiceListType getProvideServiceList() {
        return provideServiceList;
    }

    public void setProvideServiceList(ProvideServiceListType provideServiceList) {
        this.provideServiceList = provideServiceList;
    }

    public MexInterpreterListType getMexInterceptors() {
        return mexInterceptors;
    }

    public void setMexInterceptors(MexInterpreterListType mexInterceptors) {
        this.mexInterceptors = mexInterceptors;
    }

    public PropertyListType getPropertyList() {
        return propertyList;
    }

    public void setPropertyList(PropertyListType propertyList) {
        this.propertyList = propertyList;
    }

    public ScopeEventType[] getScopeEvents() {
        if(scopeEvents !=null) {
            return Arrays.copyOf(scopeEvents, scopeEvents.length);
        }
        return null;
    }

    public void setScopeEvents(ScopeEventType[] scopeEvents) {
        if (scopeEvents != null) {
            this.scopeEvents = Arrays.copyOf(scopeEvents, scopeEvents.length);;
        }
    }

    public String getProcessstate() {
        return processstate;
    }

    public void setProcessstate(String processstate) {
        this.processstate = processstate;
    }

    private boolean containsEvent(String st) {
        if (events != null) {
            for (String s : events) {
                if (s.equals(st)) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean containsSuccessCleanupEvent(String st) {
        if (successtypecleanups != null) {
            for (String s : successtypecleanups) {
                if (s.equals(st)) {
                    return true;
                }
            }
        }

        return false;
    }


    private boolean containsfailureCleanupEvent(String st) {
        if (failuretypecleanups != null) {
            for (String s : failuretypecleanups) {
                if (s.equals(st)) {
                    return true;
                }
            }
        }

        return false;
    }

    /*
     * The following methods are used to keep track of state of each element in the form(radio button,checkbox etc.)
     *
     */
    public String isTypeNoneSelected() {

        if (gentype != null && gentype.equals("none")) {
            return CHECKED;
        }

        return "";
    }

    public String isTypeAllSelected() {
        if (gentype != null && gentype.equals("all")) {
            return CHECKED;
        }
        return "";
    }

    public String isTypeSelectedSelected() {

        if (gentype != null && gentype.equals("selected")) {
            return CHECKED;
        }
        return "";
    }

    public String isInsLifSelected() {

        if (containsEvent("instanceLifecycle")) {
            return CHECKED;
        }
        return "";
    }

    public String isActLifSelected() {

        if (containsEvent("activityLifecycle")) {
            return CHECKED;
        }
        return "";
    }

    public String isDataHandSelected() {

        if (containsEvent("dataHandling")) {
            return CHECKED;
        }
        return "";
    }

    public String isScopeHandSelected() {

        if (containsEvent("scopeHandling")) {
            return CHECKED;
        }
        return "";
    }

    public String isCorrelatnSelected() {

        if (containsEvent("correlation")) {
            return CHECKED;
        }
        return "";
    }

    public String isFailInsCreated() {
        if (containsfailureCleanupEvent("instance")) {
            return CHECKED;
        }
        return "";
    }

    public String isFailVarCreated() {
        if (containsfailureCleanupEvent("variables")) {
            return CHECKED;
        }
        return "";
    }

    public String isFailMesCreated() {
        if (containsfailureCleanupEvent("messages")) {
            return CHECKED;
        }
        return "";
    }

    public String isFailCorCreated() {
        if (containsfailureCleanupEvent("correlations")) {
            return CHECKED;
        }
        return "";
    }

    public String isFailEveCreated() {
        if (containsfailureCleanupEvent("events")) {
            return CHECKED;
        }
        return "";
    }

    public String isSucInsCreated() {
        if (containsSuccessCleanupEvent("instance")) {
            return CHECKED;
        }
        return "";
    }

    public String isSucVarCreated() {
        if (containsSuccessCleanupEvent("variables")) {
            return CHECKED;
        }
        return "";
    }

    public String isSucMesCreated() {
        if (containsSuccessCleanupEvent("messages")) {
            return CHECKED;
        }
        return "";
    }

    public String isSucCorCreated() {
        if (containsSuccessCleanupEvent("correlations")) {
            return CHECKED;
        }
        return "";
    }

    public String isSucEveCreated() {
        if (containsSuccessCleanupEvent("events")) {
            return CHECKED;
        }
        return "";
    }

    public String isActiveStateSelected() {
        if (processstate != null && processstate.equalsIgnoreCase("active")) {
            return CHECKED;
        }

        return "";

    }

    public String isRetiredStateSelected() {
        if (processstate != null && processstate.equalsIgnoreCase("retired")) {
            return CHECKED;
        }

        return "";

    }

    public String isDisabledStateSelected() {
        if (processstate != null && processstate.equalsIgnoreCase("disabled")) {
            return CHECKED;
        }

        return "";

    }

    public String isInMemoryTrueSelected() {
        if (inmemorystatus != null && inmemorystatus.equalsIgnoreCase("true")) {
            return CHECKED;
        } else {
            return "";
        }


    }

    public String isInMemoryFalseSelected() {
        if (inmemorystatus != null && inmemorystatus.equalsIgnoreCase("false")) {
            return CHECKED;
        } else {
            return "";
        }


    }
}
