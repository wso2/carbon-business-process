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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public final class InstanceFilterUtil {
    private static Log log = LogFactory.getLog(InstanceFilterUtil.class);

    private InstanceFilterUtil() {
    }

    public static String createInstanceFilterStringFromFormData(final InstanceFilter filterData) {
        if (!isValidFilter(filterData)) {
            if (log.isDebugEnabled()) {
                log.debug("Invalid Instance Filter.");
            }
            /*
            * Here we set space as it is the default filter string.
            * */
            return " ";
        }

        StringBuilder filterBuilder = new StringBuilder();
        // Sending space to back-end. Workaround to handle multi-tenant case.
        // Decide to remove "name}}* namespace=*" filter used earlier to get all instances.

        filterBuilder.append(" ");

        if (filterData.getPid() != null && !filterData.getPid().equals("all") &&
                !filterData.getPid().equals("noprocesses")) {
            filterBuilder.append("pid=");
            filterBuilder.append(filterData.getPid());
            filterBuilder.append(" ");
        }

        if (filterData.getStatus() != null && filterData.getStatus().length > 0) {
            filterBuilder.append("status=");
            for (String status : filterData.getStatus()) {
                filterBuilder.append(status);
                filterBuilder.append("|");
            }

            filterBuilder.deleteCharAt(filterBuilder.lastIndexOf("|"));
            filterBuilder.append(" ");
        }

        if (filterData.getStarteddate() != null && filterData.getStarteddate().trim().length() == 16) {
            filterBuilder.append("started");
            if (filterData.getStartedopt() != null && filterData.getStartedopt().equals("onb")) {
                filterBuilder.append("<=");
            } else if (filterData.getStartedopt() != null) {
                filterBuilder.append(">=");
            } else {
                filterBuilder.append("=");
            }
            filterBuilder.append(filterData.getStarteddate().trim());
            filterBuilder.append(" ");
        }

        if (filterData.getLadate() != null && filterData.getLadate().trim().length() == 16) {
            filterBuilder.append("last-active");
            if (filterData.getLadateopt() != null && filterData.getLadateopt().equals("onb")) {
                filterBuilder.append("<=");
            } else if (filterData.getLadateopt() != null) {
                filterBuilder.append(">=");
            } else {
                filterBuilder.append("=");
            }
            filterBuilder.append(filterData.getLadate().trim());
            filterBuilder.append(" ");
        }

        String finalFilter = filterBuilder.toString();
        if (log.isDebugEnabled()) {
            log.debug("Instance Filter:" + finalFilter);
        }

        return finalFilter;
    }

    public static String getOrderByFromFormData(final InstanceFilter filterData) {
        StringBuilder filterBuilder = new StringBuilder();

        if (filterData.getOrderby() != null && filterData.getAsdec() != null) {
            if (filterData.getAsdec().equals("Ascending")) {
                filterBuilder.append("-");
            } else {
                filterBuilder.append("+");
            }
            filterBuilder.append(filterData.getOrderby());
        } else {
            filterBuilder.append("-last-active");
        }

        if (log.isDebugEnabled()) {
            log.debug("Instance Filter Order-by:" + filterBuilder);
        }

        return filterBuilder.toString();
    }

    private static boolean isValidFilter(final InstanceFilter filterData) {
        return !((filterData.getPid() == null || filterData.getPid().equals("all")) &&
                filterData.getStatus() == null && filterData.getStarteddate() == null &&
                filterData.getStartedopt() == null && filterData.getLadate() == null &&
                filterData.getLadateopt() == null);
    }

}