/*
 *
 *   Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 * /
 */

package org.wso2.carbon.humantask.core.api.mgt;

import org.apache.axis2.databinding.types.URI;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Element;
import org.wso2.carbon.humantask.client.api.IllegalAccessFault;
import org.wso2.carbon.humantask.client.api.IllegalArgumentFault;
import org.wso2.carbon.humantask.client.api.IllegalOperationFault;
import org.wso2.carbon.humantask.client.api.IllegalStateFault;
import org.wso2.carbon.humantask.core.dao.*;
import org.wso2.carbon.humantask.core.engine.HumanTaskEngine;
import org.wso2.carbon.humantask.core.engine.util.CommonTaskUtil;
import org.wso2.carbon.humantask.core.internal.HumanTaskServerHolder;
import org.wso2.carbon.humantask.core.internal.HumanTaskServiceComponent;
import org.wso2.carbon.humantask.core.store.HumanTaskBaseConfiguration;
import org.wso2.carbon.humantask.core.store.HumanTaskStore;
import org.wso2.carbon.humantask.core.utils.DOMUtils;
import org.wso2.carbon.humantask.types.TPredefinedStatus;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The queries necessary to retrieve information is built here according to the users input
 */
public class HTQueryBuildHelperImpl implements HTQueryBuildHelper {

    private static final Log log = LogFactory.getLog(HTQueryBuildHelperImpl.class);
//    private int tenantID = -1;
    private int taskCountPerTenant = 0;
    HashMap<Integer, Integer> taskCount = new HashMap<Integer, Integer>();


    /**
     * @param taskName
     * @return
     * @throws IllegalAccessFault
     * @throws IllegalArgumentFault
     * @throws IllegalStateFault
     * @throws IllegalOperationFault
     */

    public String[] getTaskInstanceCountsByState(String taskName)
            throws Exception {
        int statusCount = 0;
        TPredefinedStatus.Enum[] statuses;
        int statusesSize = TPredefinedStatus.Enum.table.lastInt();
        statuses = new TPredefinedStatus.Enum[statusesSize];
        String[] statusCounts = new String[statusesSize];
        for (int i = 0; i < statusesSize; i++) {
            statuses[i] = TPredefinedStatus.Enum.forInt(i + 1);
        }
        Boolean isTaskName = false;
        Pattern p = Pattern.compile("-[0-9]+$");
        Matcher m = p.matcher(taskName);
        isTaskName = m.find();
        if (isTaskName) {
            for (int i = 0; i < statuses.length; i++) {
                statusCount = getTaskCountsForStateAndTaskName(taskName, statuses[i]);
                statusCounts[i] = " " + statuses[i].toString() + " " + statusCount;
            }
        } else {
            for (int i = 0; i < statuses.length; i++) {
                statusCount = getTaskCountsForStateAndTaskDefName(taskName, statuses[i]);
                statusCounts[i] = " " + statuses[i].toString() + " " + statusCount;
            }
        }
        return statusCounts;
    }

    /**
     * @param taskName
     * @return
     * @throws IllegalAccessFault
     * @throws IllegalArgumentFault
     * @throws IllegalStateFault
     * @throws IllegalOperationFault
     */
    private int getTaskInstanceCountForTaskName(final String taskName)
            throws Exception {

        final List[] taskList = new List[1];

        return HumanTaskServiceComponent.getHumanTaskServer().getTaskEngine().getScheduler().
                execTransaction(new Callable<Integer>() {
                    public Integer call() throws Exception {

                        HumanTaskDAOConnection connection = HumanTaskServerHolder.getInstance().getHtServer().
                                getTaskEngine().getDaoConnectionFactory().getConnection();
                        HTQueryBuilder htQueryBuilder = new HTQueryBuilder();
                        EntityManager em = connection.getEntityManager();
                        Query query = htQueryBuilder.buildQueryToFindTaskInstances(taskName, em);
                        List resultList = query.getResultList();
                        if(resultList != null)
                        return taskList[0].size();
                    }
                });

    }

    /**
     * @param taskName
     * @param status
     * @return
     * @throws IllegalStateFault
     * @throws IllegalOperationFault
     * @throws IllegalArgumentFault
     * @throws IllegalAccessFault
     */
    private int getTaskCountsForStateAndTaskName(final String taskName,
                                                final TPredefinedStatus.Enum status)
            throws Exception {

        return HumanTaskServiceComponent.getHumanTaskServer().getTaskEngine().getScheduler().
                execTransaction(new Callable<Integer>() {
                    public Integer call() throws Exception {

                        HumanTaskDAOConnection connection = HumanTaskServerHolder.getInstance().getHtServer().
                                getTaskEngine().getDaoConnectionFactory().getConnection();
                        HTQueryBuilder htQueryBuilder = new HTQueryBuilder();
                        EntityManager em = connection.getEntityManager();
                        Query query = htQueryBuilder.buildQueryToCountTaskInstancesByTaskName(taskName, status, em);
                        List resultList = query.getResultList();
                        if (resultList != null) {
                            return resultList.size();
                        }
                        return 0;
                    }
                });
    }

    private int getTaskCountsForStateAndTaskDefName(
            final String taskName,
            final TPredefinedStatus.Enum status)
            throws Exception {
        return HumanTaskServiceComponent.getHumanTaskServer().getTaskEngine().getScheduler().
                execTransaction(new Callable<Integer>() {
                    public Integer call() throws Exception {
                        HumanTaskDAOConnection connection = HumanTaskServerHolder.getInstance().getHtServer().
                                getTaskEngine().getDaoConnectionFactory().getConnection();
                        HTQueryBuilder htQueryBuilder = new HTQueryBuilder();
                        EntityManager em = connection.getEntityManager();
                        Query query = htQueryBuilder.buildQueryToCountTaskInstancesByTaskDefName(taskName, status, em);
                        List resultList = query.getResultList();
                        if (resultList != null) {
                            return resultList.size();
                        }
                        return 0;
                    }
                });
    }

    /**
     * @return
     * @throws IllegalAccessFault
     * @throws IllegalArgumentFault
     * @throws IllegalStateFault
     * @throws IllegalOperationFault
     */
    public DeployedTaskDetail[][] getAllDeployedTasksDetails()
            throws Exception {
        DeployedTaskDetail[][] dtt = null;
        long[] tenantIDs = getAllTenantIDs();
        dtt = new DeployedTaskDetail[tenantIDs.length][];
        HumanTaskBaseConfiguration htc;
        int i = 0;
        for (Long tenantID : tenantIDs) {
            List<HumanTaskBaseConfiguration> htcList = getAllDeployedTasks(tenantID);
            int size = htcList.size();
            dtt[i] = new DeployedTaskDetail[size];

            for (int j = 0; j < size; j++) {
                htc = htcList.get(j);
                dtt[i][j] = new DeployedTaskDetail();
                dtt[i][j].setTenantID(tenantID.intValue());
                dtt[i][j].setTaskDefName(htc.getDefinitionName());
                dtt[i][j].setTaskName(htc.getName());
                dtt[i][j].setOperation(htc.getOperation());
                dtt[i][j].setPortName(htc.getPortName());
                dtt[i][j].setTaskCount(getTaskInstanceCountForTaskName(htc.getName().toString()));
                dtt[i][j].setConfigType(htc.getConfigurationType());
                dtt[i][j].setPackageName(htc.getPackageName());

            }
            i++;
        }

        return dtt;
    }

    /**
     * This method is not used, hence commenting out

    public DeployedTaskDetail getDeployedTasksDetails(String taskName)
            throws Exception {
        DeployedTaskDetail dtt;
        dtt = new DeployedTaskDetail();
        HumanTaskBaseConfiguration htc;
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        HumanTaskServer humanTaskServer = HumanTaskServiceComponent.getHumanTaskServer();
        HumanTaskStore humanTaskStore = humanTaskServer.getTaskStoreManager().getHumanTaskStore(-1234);
        QName taskName1 = QName.valueOf(taskName);
        htc = humanTaskStore.getTaskConfiguration(taskName1);

        dtt = new DeployedTaskDetail();
        dtt.setTenantID(tenantID);
        dtt.setTaskDefName(htc.getDefinitionName());
        dtt.setTaskName(htc.getName());
        dtt.setOperation(htc.getOperation());
        dtt.setPortName(htc.getPortName());
        dtt.setTaskCount(getTaskInstanceCountForTaskName(htc.getName().toString()));
        dtt.setConfigType(htc.getConfigurationType());
        dtt.setPackageName(htc.getPackageName());
        dtt.setStatus(htc.getPackageStatus().toString());
        dtt.setReservedCount(getTaskCountsForStateAndTaskName(taskName.toString(), TPredefinedStatus.RESERVED));
        dtt.setCompletedCount(getTaskCountsForStateAndTaskName(taskName.toString(), TPredefinedStatus.COMPLETED));
        dtt.setReadyCount(getTaskCountsForStateAndTaskName(taskName.toString(), TPredefinedStatus.READY));
        dtt.setFailedCount(getTaskCountsForStateAndTaskName(taskName.toString(), TPredefinedStatus.FAILED));
        dtt.setInProgressCount(getTaskCountsForStateAndTaskName(taskName.toString(), TPredefinedStatus.IN_PROGRESS));
        return dtt;
    }

     */

    /**
     * @return
     * @throws IllegalAccessFault
     * @throws IllegalArgumentFault
     * @throws IllegalStateFault
     * @throws IllegalOperationFault
     */
    public String[] getAllDeployedTasks()
            throws Exception {
        long[] tenantIDs;
        tenantIDs = getAllTenantIDs();
        String[][] taskList = new String[tenantIDs.length][];
        String[][] taskNameList = new String[tenantIDs.length][];
        String[] result;
        int j = 0;
        int countTenant = 0;

        for (Long tenantID : tenantIDs) {
            List<HumanTaskBaseConfiguration> allDeployedTasks = getAllDeployedTasks(tenantID);
            taskList[countTenant++] = new String[allDeployedTasks.size()];
            taskNameList[countTenant - 1] = new String[allDeployedTasks.size()];
            j += allDeployedTasks.size();
            for (int i = 0; i < allDeployedTasks.size(); i++) {
                taskList[countTenant - 1][i] =
                        " " + String.format("%1$-" + 4 + "s", tenantID) + " " +
                        String.format("%1$-" + 55 + "s", allDeployedTasks.get(i).getName()) + "  " +
                        String.format("%1$-" + 55 + "s", allDeployedTasks.get(i).getDefinitionName()) + "" +
                        " " + String.format("%1$-" + 10 + "s", allDeployedTasks.get(i).getOperation()) + " " +
                        String.format("%1$-" + 15 + "s", allDeployedTasks.get(i).getPortName());
                taskNameList[countTenant - 1][i] = "" + allDeployedTasks.get(i).getName();
            }
        }

        int k = 0;
        result = new String[j + 2];
        result[k++] = String.format("%1$-" + 11 + "s", "InstCount ") + "  " +
                      String.format("%1$-" + 4 + "s", "TID") + "  " +
                      String.format("%1$-" + 55 + "s", "Task Name") + "  " +
                      String.format("%1$-" + 55 + "s", "Task Definition Name") + " " +
                      String.format("%1$-" + 10 + "s", "Operation") + " " +
                      String.format("%1$-" + 15 + "s", "Port name");

        result[k++] = String.format("%1$-" + 11 + "s", "").replace(" ", "-") + "  " +
                      String.format("%1$-" + 4 + "s", "").replace(" ", "-")  + "  " +
                      String.format("%1$-" + 55 + "s", "").replace(" ", "-") + "  " +
                      String.format("%1$-" + 55 + "s", "").replace(" ", "-") + " " +
                      String.format("%1$-" + 10 + "s", "").replace(" ", "-") + " " +
                      String.format("%1$-" + 15 + "s", "").replace(" ", "-");

        for (int a = 0; a < taskList.length; a++) {
            for (int b = 0; b < taskList[a].length; b++) {
                result[k++] = " [ " + String.format("%1$-" + 6 + "s",
                        getTaskInstanceCountForTaskName (taskNameList[a][b])) + " ] " + taskList[a][b];
            }
        }
        return result;
    }


    /**
     * @param ltenantID
     * @return all the deployed tasks for the given tenant
     */

    public List<HumanTaskBaseConfiguration> getAllDeployedTasks(long ltenantID) {
        int tenantID = (int) ltenantID;
        HumanTaskStore humanTaskStore = HumanTaskServiceComponent.getHumanTaskServer().
                getTaskStoreManager().getHumanTaskStore(tenantID);
        List<HumanTaskBaseConfiguration> humanTaskConfigurations = humanTaskStore.getTaskConfigurations();
        return humanTaskConfigurations;
    }

    /**
     * @param status
     * @return all task instances in the given status
     * @throws IllegalAccessFault
     * @throws IllegalArgumentFault
     * @throws IllegalStateFault
     * @throws IllegalOperationFault
     */
    public String[] getTaskInstances(final TPredefinedStatus.Enum status)
            throws Exception {
        String[] instances = {"null"};
        List taskList = HumanTaskServiceComponent.getHumanTaskServer().getTaskEngine().getScheduler().
                execTransaction(new Callable<List>() {
                    public List call() throws Exception {
                        HumanTaskDAOConnection connection = HumanTaskServerHolder.getInstance().getHtServer().
                                getTaskEngine().getDaoConnectionFactory().getConnection();
                        HTQueryBuilder htQueryBuilder = new HTQueryBuilder();
                        Query query = htQueryBuilder.buildQueryToFindTaskInstances(status, connection.getEntityManager());
                        return query.getResultList();
                    }
                });


        int i = 0;
        instances = new String[taskList.size() + 2];
        instances[i++] = String.format("%1$-" + 7 + "s", "ID") +
                         String.format("%1$-" + 60 + "s", "Name") +
                         String.format("%1$-" + 60 + "s", "Definition " + "Name") +
                         String.format("%1$-" + 30 + "s", "Created on");

        instances[i++] = String.format("%1$-" + 6 + "s", "").replace(" ", "-") + " " +
                         String.format("%1$-" + 59 + "s", "").replace( " ", "-") + " " +
                         String.format("%1$-" + 59 + "s", "").replace(" ", "-") + " " +
                         String.format("%1$-" + 30 + "s", "").replace(" ", "-");

        for (Object o : taskList) {
            TaskDAO taskDaoInstance = (TaskDAO) o;
            instances[i++] = String.format("%1$-" + 7 + "s", taskDaoInstance.getId()) +
                             String.format("%1$-" + 60 + "s", taskDaoInstance.getName()) +
                             String.format("%1$-" + 60 + "s", taskDaoInstance.getDefinitionName()) +
                             String.format("%1$-" + 30 + "s", taskDaoInstance.getCreatedOn());
        }
        return instances;
    }

    /**
     * @param taskId
     * @return all the task details for the given taskID
     * @throws IllegalAccessFault
     * @throws IllegalArgumentFault
     * @throws IllegalStateFault
     * @throws IllegalOperationFault
     * @throws URI.MalformedURIException
     */
    public String[] getTaskDataById(String taskId)
            throws IllegalAccessFault, IllegalArgumentFault, IllegalStateFault,
                   IllegalOperationFault, URI.MalformedURIException {

        String[] output = {""};
        List<String> outputList = new ArrayList<>();
        TaskDAO task;
        URI uri = new URI(taskId);
        try {
            final Long validatedTaskId = validateTaskId(uri);
            task = HumanTaskServiceComponent.getHumanTaskServer().getTaskEngine().getScheduler().
                    execTransaction(new Callable<TaskDAO>() {
                        public TaskDAO call() throws Exception {
                            HumanTaskEngine engine = HumanTaskServiceComponent.getHumanTaskServer().getTaskEngine();
                            HumanTaskDAOConnection daoConn = engine.getDaoConnectionFactory().getConnection();
                            TaskDAO task = daoConn.getTask(validatedTaskId);
                            return task;
                        }
                    });
        } catch (Exception ex) {
            throw new IllegalAccessFault(ex);
        }

        GenericHumanRoleDAO.GenericHumanRoleType[] genericHumanRoleTypes = GenericHumanRoleDAO.GenericHumanRoleType.values();

        MessageDAO inputMessageDAO = task.getInputMessage();
        MessageDAO outputMessageDAO = task.getOutputMessage();
        String description = task.getTaskDescription("text/plain");

        String titleString = String.format("%1$-" + 25 + "s" , "Task Name") + ":" + task.getName();
        outputList.add(titleString);

        for (int i = 0; i < genericHumanRoleTypes.length; i++) {
            List<OrganizationalEntityDAO> organizationalEntityDAOs =
                    CommonTaskUtil.getOrgEntitiesForRole(task, genericHumanRoleTypes[i]);
            if (organizationalEntityDAOs.size() > 0) {
                String taskDataString = String.format("%1$-" + 25 + "s", genericHumanRoleTypes[i]) + ":";

                for (int j = 0; j < organizationalEntityDAOs.size() ; j++) {
                    taskDataString = taskDataString + organizationalEntityDAOs.get(j).getName() + " [" +
                                     organizationalEntityDAOs.get(j).getOrgEntityType() + "]  ";
                }
                outputList.add(taskDataString);
            }
        }
        if (description != null) {
            String taskDescriptionString = String.format("%1$-" + 25 + "s", "Task Description") + ":" +
                                           task.getTaskDescription("text/plain");
            outputList.add(taskDescriptionString);
        }
        Element inputBodyData = inputMessageDAO.getBodyData();
        if (inputBodyData != null) {
            String inputMsgStr = String.format("%1$-" + 25 + "s", "Task Input")
                                 + ":" + "\n" + DOMUtils.domToString(inputBodyData);
            outputList.add(inputMsgStr);
        }
        if (outputMessageDAO != null) {
            Element outputBodyData = outputMessageDAO.getBodyData();
            if (outputBodyData != null) {
                String outputMessageStr  = String.format("%1$-" + 25 + "s", "Task Output") + ":" + "\n" +
                                           DOMUtils.domToString(outputBodyData);
                outputList.add(outputMessageStr);
            }
        }

        output = new String[outputList.size()];
        int i = 0;
        for (Object o : outputList) {
            output[i++] = o.toString();
        }
        return output;
    }


    /**
     * @param taskId
     * @return
     */
    private Long validateTaskId(URI taskId) {
        if (taskId == null || StringUtils.isEmpty(taskId.toString())) {
            throw new IllegalArgumentException("The task id cannot be null or empty");
        }
        try {
            return Long.valueOf(taskId.toString());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("The task id must be a number", e);
        }
    }


    /**
     * @return
     * @throws IllegalAccessFault
     * @throws IllegalArgumentFault
     * @throws IllegalStateFault
     * @throws IllegalOperationFault
     */
    public long[] getAllTenantIDs()
            throws Exception {
        List DeploymentUnitList = HumanTaskServiceComponent.getHumanTaskServer().getTaskEngine().getScheduler().
                execTransaction(new Callable<List>() {
                    public List call() throws Exception {
                        HumanTaskDAOConnection connection = HumanTaskServerHolder.getInstance().getHtServer().
                                getTaskEngine().getDaoConnectionFactory().getConnection();
                        HTQueryBuilder htQueryBuilder = new HTQueryBuilder();
                        Query query = htQueryBuilder.getDeploymentUnits(connection.getEntityManager());
                        return query.getResultList();
                    }
                });
        Set<Long> tenantIdSet = new HashSet<>();
        for (Object object : DeploymentUnitList) {
            DeploymentUnitDAO duDAO = (DeploymentUnitDAO) object;
            tenantIdSet.add(duDAO.getTenantId());
        }
        long[] results = {};

        Iterator<Long> iterator = tenantIdSet.iterator();
        for(int i = 0; iterator.hasNext(); i++) {
            results[i] = ((Long)iterator.next()).longValue();
        }
        return results;
    }
}
