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
package org.wso2.carbon.bpel.core.ode.integration.store.repository;

import org.apache.ode.bpel.dd.TProcessEvents;
import org.apache.ode.bpel.evt.BpelEvent;
import org.apache.ode.bpel.iapi.ProcessConf;
import org.apache.ode.bpel.iapi.ProcessState;
import org.wso2.carbon.bpel.core.BPELConstants;
import org.wso2.carbon.bpel.core.ode.integration.store.BPELDeploymentContext;
import org.wso2.carbon.bpel.skeleton.ode.integration.mgt.services.ProcessManagementException;
import org.wso2.carbon.bpel.skeleton.ode.integration.mgt.services.types.CategoryListType;
import org.wso2.carbon.bpel.skeleton.ode.integration.mgt.services.types.Category_type1;
import org.wso2.carbon.bpel.skeleton.ode.integration.mgt.services.types.CleanUpType;
import org.wso2.carbon.bpel.skeleton.ode.integration.mgt.services.types.EnableEventListType;
import org.wso2.carbon.bpel.skeleton.ode.integration.mgt.services.types.Generate_type1;
import org.wso2.carbon.bpel.skeleton.ode.integration.mgt.services.types.On_type1;
import org.wso2.carbon.bpel.skeleton.ode.integration.mgt.services.types.ScopeEventType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Utility methods to use in BPEL package repository implementation.
 */
public final class BPELPackageRepositoryUtils {

    private BPELPackageRepositoryUtils() {
    }

    /* The following method returns the packagelocation of an deploy descriptor info updated bpel package
    *  @param packageName - the bpel package
    *  @param versionlessPackageName - the package name after removing the version tag
    *  @return packagelocation - location to the bpel package
    *
    */
    public static String getResourcePathForDeployInfoUpdatedBPELPackage(String packageName,
                                                                        String versionlessPackageName) {
        return BPELConstants.REG_PATH_OF_BPEL_PACKAGES + versionlessPackageName +
                BPELConstants.BPEL_PACKAGE_VERSIONS + packageName;
    }

    public static String getResourcePathForBPELPackage(BPELDeploymentContext deploymentContext) {
        return BPELConstants.REG_PATH_OF_BPEL_PACKAGES + deploymentContext.getBpelPackageName();
    }

    public static String getResourcePathForBPELPackage(String packageName) {
        return BPELConstants.REG_PATH_OF_BPEL_PACKAGES + packageName;
    }

    public static String getResourcePathForBPELPackageContent(
            BPELDeploymentContext deploymentContext) {
        return BPELConstants.REG_PATH_OF_BPEL_PACKAGES + deploymentContext.getBpelPackageName() +
                BPELConstants.BPEL_PACKAGE_VERSIONS +
                deploymentContext.getBpelPackageNameWithVersion();
    }

    public static String getResourcePathForBPELPackageVersions(
            BPELDeploymentContext deploymentContext) {
        return BPELConstants.REG_PATH_OF_BPEL_PACKAGES + deploymentContext.getBpelPackageName() +
                BPELConstants.BPEL_PACKAGE_VERSIONS;
    }

    public static String getBPELPackageArchiveResourcePath(String bpelPackageName) {
        return BPELConstants.REG_PATH_OF_BPEL_PACKAGES.concat(bpelPackageName).
                concat(BPELConstants.PATH_SEPARATOR).concat(bpelPackageName).concat(".zip");
    }

    /*The following methods are used to write the updated fields of a bpel package into registry as properties*/
    protected static String getBPELPackageProcessEventsInList(Map<String, Set<BpelEvent.TYPE>> events) {
        String enabledEventsList = "";
        if (events != null) {

            Set<Map.Entry<String, Set<BpelEvent.TYPE>>> eventEntries = events.entrySet();
            for (Map.Entry entry : eventEntries) {
                if (entry.getKey() == null) {
                    HashSet<BpelEvent.TYPE> evtSet = (HashSet<BpelEvent.TYPE>) entry.getValue();
                    for (BpelEvent.TYPE type : evtSet) {
                        enabledEventsList = enabledEventsList.concat(type.name() + ",");
                    }
                }
            }
        }
        return enabledEventsList;
    }

    protected static List<String> getBPELPackageScopeEventsInList(Map<String, Set<BpelEvent.TYPE>> events) {
        List<String> scopeEvents = new ArrayList<String>();

        if (events != null) {
            Set<Map.Entry<String, Set<BpelEvent.TYPE>>> eventEntries = events.entrySet();
            for (Map.Entry entry : eventEntries) {
                if (entry.getKey() != null) {
                    String scopeEventEntry = "";
                    scopeEventEntry = scopeEventEntry.concat(entry.getKey() + ",");
                    HashSet<BpelEvent.TYPE> evtSet = (HashSet<BpelEvent.TYPE>) entry.getValue();
                    for (BpelEvent.TYPE type : evtSet) {
                        scopeEventEntry = scopeEventEntry.concat(type.name() + ",");
                    }
                    scopeEvents.add(scopeEventEntry);
                }
            }
        }
        return scopeEvents;
    }

    public static String getBPELPackageSuccessCleanUpsInList(Set<ProcessConf.CLEANUP_CATEGORY> cleanupCategories) {
        String successCleanUpsList = "";
        if (cleanupCategories != null) {
            for (ProcessConf.CLEANUP_CATEGORY category : cleanupCategories) {
                successCleanUpsList = successCleanUpsList.concat(category.name() + ",");
            }
            if (!successCleanUpsList.equalsIgnoreCase("")) {
                successCleanUpsList = successCleanUpsList.substring(0, successCleanUpsList.lastIndexOf(','));
            }
        }
        return successCleanUpsList;
    }

    public static String getBPELPackageFailureCleanUpsAsString(Set<ProcessConf.CLEANUP_CATEGORY> cleanupCategories) {
        String failureCleanUpsList = "";
        if (cleanupCategories != null) {
            for (ProcessConf.CLEANUP_CATEGORY category : cleanupCategories) {
                failureCleanUpsList = failureCleanUpsList.concat(category.name() + ",");
            }
            if (!failureCleanUpsList.equalsIgnoreCase("")) {
                failureCleanUpsList = failureCleanUpsList.substring(0, failureCleanUpsList.lastIndexOf(','));
            }
        }
        return failureCleanUpsList;
    }

    public static String getBPELPackageProcessGenerateType(TProcessEvents.Generate.Enum generateType) {

        if (generateType != null) {
            return generateType.toString();
        } else {
            return "";
        }
    }

    public static ProcessState getProcessState(String stateInString) {
        if (stateInString.equalsIgnoreCase("ACTIVE")) {
            return ProcessState.ACTIVE;
        } else if (stateInString.equalsIgnoreCase("RETIRED")) {
            return ProcessState.RETIRED;
        } else {
            return ProcessState.DISABLED;
        }
    }

    /* This method splits a bpel package name to remove its deployed version
    *  @param packageName
    *
    */
    public static String getVersionlessPackageName(String packageName) throws ProcessManagementException {
        String tPackageName = packageName;
        if (tPackageName != null) {
            tPackageName = tPackageName.substring(0, tPackageName.lastIndexOf('-'));
        }
        return tPackageName;
    }

    public static EnableEventListType getEnabledEventsListFromString(String processEventsInString) {

        EnableEventListType enableEventListType = new EnableEventListType();
        String[] enabledEvents = getStringsFromArray(processEventsInString);
        for (String event : enabledEvents) {
            enableEventListType.addEnableEvent(event);
        }
        return enableEventListType;
    }

    public static Generate_type1 getProcessGenerateTypeFromString(String generateTypeString) {
        return Generate_type1.Factory.fromValue(generateTypeString);
    }

    public static ScopeEventType getScopeEventFromString(String scopeEventInString) {

        ScopeEventType scopeEvent = new ScopeEventType();
        EnableEventListType enableEventListType = new EnableEventListType();
        String[] scopeEventEntries = getStringsFromArray(scopeEventInString);
        for (int i = 0; i < scopeEventEntries.length - 1; i++) {
            enableEventListType.addEnableEvent(scopeEventEntries[i + 1]);
        }
        scopeEvent.setScope(scopeEventEntries[0]);
        scopeEvent.setEnabledEventList(enableEventListType);
        return scopeEvent;
    }

    public static CleanUpType getSuccessCleanUpType(String successCleanupsInString) {
        CleanUpType successCleanUp = new CleanUpType();
        String[] successCategories = getStringsFromArray(successCleanupsInString);
        CategoryListType categoryList = new CategoryListType();

        for (String category : successCategories) {
            Category_type1 categoryType1 = Category_type1.Factory.fromValue(category.toLowerCase());
            categoryList.addCategory(categoryType1);
        }
        successCleanUp.setOn(On_type1.success);
        successCleanUp.setCategoryList(categoryList);
        return successCleanUp;
    }

    public static CleanUpType getFailureCleanUpType(String failureCleanupsInString) {
        CleanUpType failureCleanUp = new CleanUpType();
        String[] failureCategories = getStringsFromArray(failureCleanupsInString);
        CategoryListType categoryList = new CategoryListType();

        for (String category : failureCategories) {
            Category_type1 categoryType1 = Category_type1.Factory.fromValue(category.toLowerCase());
            categoryList.addCategory(categoryType1);
        }
        failureCleanUp.setOn(On_type1.failure);
        failureCleanUp.setCategoryList(categoryList);
        return failureCleanUp;
    }


    /*  This method splits a string to remove its ',' characters
    *  @param eventsInString  -  the string which contains the events each separated by commas
    *  @return new String[] - the string array which consists of events strings after splitting
    *
    */
    public static String[] getStringsFromArray(String eventsInString) {
        if (!eventsInString.isEmpty()) {
            return eventsInString.split(",");
        } else {
            return new String[0];
        }

    }


}
