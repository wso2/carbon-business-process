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

import java.util.Arrays;

/**
 * Instance Filter Class.
 */
public class InstanceFilter {

    private static final String CHECKED = "checked";
    private static final String SELECTED = "selected";

    private String pid;

    private String[] status;

    private String startedopt;

    private String starteddate;

    private String ladateopt;

    private String ladate;

    private String asdec;

    private String orderby;

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public String[] getStatus() {
        if (status != null) {
            return Arrays.copyOf(status, status.length);
        }
        return new String[0];
    }

    public void setStatus(String[] status) {
        if (status != null) {
            this.status = Arrays.copyOf(status, status.length);
        }
    }

    public String getStartedopt() {
        return startedopt;
    }

    public void setStartedopt(String startedopt) {
        this.startedopt = startedopt;
    }

    public String getStarteddate() {
        return starteddate;
    }

    public void setStarteddate(String starteddate) {
        this.starteddate = starteddate;
    }

    public String getLadateopt() {
        return ladateopt;
    }

    public void setLadateopt(String ladateopt) {
        this.ladateopt = ladateopt;
    }

    public String getLadate() {
        return ladate;
    }

    public void setLadate(String ladate) {
        this.ladate = ladate;
    }

    public String getAsdec() {
        return asdec;
    }

    public void setAsdec(String asdec) {
        this.asdec = asdec;
    }

    public String getOrderby() {
        return orderby;
    }

    public void setOrderby(String orderby) {
        this.orderby = orderby;
    }

    public String isActiveStatusSelected() {
        if (containsStatus("active")) {
            return CHECKED;
        }

        return "";
    }

    public String isComlpetedSelected() {
        if (containsStatus("completed")) {
            return CHECKED;
        }

        return "";
    }

    public String isSuspendedSelected() {
        if (containsStatus("suspended")) {
            return CHECKED;
        }

        return "";
    }

    public String isTerminatedSelected() {
        if (containsStatus("terminated")) {
            return CHECKED;
        }

        return "";
    }

    public String isErrorSelected() {
        if (containsStatus("error")) {
            return CHECKED;
        }

        return "";
    }

    public String isFailedSelected() {
        if (containsStatus("failed")) {
            return CHECKED;
        }

        return "";
    }

    public String isStartedOnOrBeforeSelected() {
        if (startedopt != null && startedopt.equals("onb")) {
            return CHECKED;
        }

        return "";
    }

    public String isStartedOnOrAfterSelected() {
        if (startedopt != null && startedopt.equals("ona")) {
            return CHECKED;
        }

        return "";
    }

    public String isLastActiveOnOrBeforeSelected() {
        if (ladateopt != null && ladateopt.equals("onb")) {
            return CHECKED;
        }

        return "";
    }

    public String isLastActiveOnOrAfterSelected() {
        if (ladateopt != null && ladateopt.equals("ona")) {
            return CHECKED;
        }

        return "";
    }

    public String getStratedDate() {
        if (starteddate != null) {
            return starteddate;
        }

        return "";
    }

    public String getLastActiveDate() {
        if (ladate != null) {
            return ladate;
        }

        return "";
    }

    public String isOrderByAscendingSelected() {
        if (asdec != null && asdec.equals("Ascending")) {
            return CHECKED;
        }

        return "";
    }

    public String isOrderByDescendingSelected() {
        if (asdec != null && asdec.equals("Descending")) {
            return CHECKED;
        }

        return "";
    }

    public String isOrderByPidSelected() {
        if (orderby != null && orderby.equals("pid")) {
            return SELECTED;
        }

        return "";
    }

    public String isOrderByStatusSelected() {
        if (orderby != null && orderby.equals("status")) {
            return SELECTED;
        }

        return "";
    }

    public String isOrderByStartedDateSelected() {
        if (orderby != null && orderby.equals("started")) {
            return SELECTED;
        }

        return "";
    }

    public String isOrderByLastActiveDateSelected() {
        if (orderby != null && orderby.equals("last-active")) {
            return SELECTED;
        }

        return "";
    }


    private boolean containsStatus(String st) {
        if (status != null) {
            for (String s : status) {
                if (s.equals(st)) {
                    return true;
                }
            }
        }

        return false;
    }
}
