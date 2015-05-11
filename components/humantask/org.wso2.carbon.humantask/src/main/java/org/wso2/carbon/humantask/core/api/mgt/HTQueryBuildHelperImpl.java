/*
 *
 *   Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.humantask.client.api.IllegalAccessFault;
import org.wso2.carbon.humantask.client.api.IllegalArgumentFault;
import org.wso2.carbon.humantask.client.api.IllegalOperationFault;
import org.wso2.carbon.humantask.client.api.IllegalStateFault;
import org.wso2.carbon.humantask.core.HumanTaskServer;
import org.wso2.carbon.humantask.core.dao.*;
import org.wso2.carbon.humantask.core.engine.HumanTaskEngine;
import org.wso2.carbon.humantask.core.engine.runtime.api.HumanTaskIllegalAccessException;
import org.wso2.carbon.humantask.core.engine.runtime.api.HumanTaskIllegalArgumentException;
import org.wso2.carbon.humantask.core.engine.runtime.api.HumanTaskIllegalOperationException;
import org.wso2.carbon.humantask.core.engine.runtime.api.HumanTaskIllegalStateException;
import org.wso2.carbon.humantask.core.engine.util.CommonTaskUtil;
import org.wso2.carbon.humantask.core.internal.HumanTaskServerHolder;
import org.wso2.carbon.humantask.core.internal.HumanTaskServiceComponent;
import org.wso2.carbon.humantask.core.store.HumanTaskBaseConfiguration;
import org.wso2.carbon.humantask.core.store.HumanTaskStore;
import org.wso2.carbon.humantask.core.utils.DOMUtils;
import org.wso2.carbon.humantask.core.mgt.services.HumanTaskPackageManagementSkeleton;
import org.wso2.carbon.humantask.skeleton.mgt.services.PackageManagementException;
import org.wso2.carbon.humantask.skeleton.mgt.services.types.DeployedTaskDefinitionsPaginated;
import org.wso2.carbon.humantask.skeleton.mgt.services.types.TaskDefinition_type0;
import org.wso2.carbon.humantask.types.TPredefinedStatus;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.transaction.TransactionManager;
import javax.xml.namespace.QName;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 */
public class HTQueryBuildHelperImpl implements HTQueryBuildHelper {

    private List<TaskDAO> allTasks = new ArrayList<TaskDAO>();
    private int statusCount = 0;
    int statusCount2;
    private static final Log log = LogFactory.getLog(HTQueryBuildHelperImpl.class);
    private int tenantID = -1;
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

    public String[] getTaskInstanceCountsByState(String taskName) throws IllegalAccessFault, IllegalArgumentFault, IllegalStateFault, IllegalOperationFault {
        int statusCount1=0;
        TPredefinedStatus.Enum[] statuses;
        int statusesSize = TPredefinedStatus.Enum.table.lastInt();

        TPredefinedStatus.Enum.table.forInt(1);
        org.apache.xmlbeans.StringEnumAbstractBase.Table table = TPredefinedStatus.Enum.table;
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
                statusCount1 = getTaskCountsForStateAndTaskName(taskName, statuses[i]);
                statusCounts[i] = " " + statuses[i].toString() + " " + statusCount1;

            }
        } else {
            for (int i = 0; i < statuses.length; i++) {
                statusCount1 = getTaskCountsForStateAndTaskDefName(taskName, statuses[i]);
                statusCounts[i] = " " + statuses[i].toString() + " " + statusCount1;

            }
        }


        return statusCounts;
    }

    /**
     * @param taskname
     * @return
     * @throws IllegalAccessFault
     * @throws IllegalArgumentFault
     * @throws IllegalStateFault
     * @throws IllegalOperationFault
     */
    public int getTaskInstanceCountForTaskName(final String taskname) throws IllegalAccessFault, IllegalArgumentFault, IllegalStateFault, IllegalOperationFault {
        final int[] taskCountTemp = {0};
        final List[] tasklist = new List[1];

        try {

            HumanTaskServiceComponent.getHumanTaskServer().getTaskEngine().getScheduler().
                    execTransaction(new Callable<Object>() {
                        public Object call() throws Exception {

                            //log.info("-------------------------------------------------");
                            HumanTaskServerHolder.getInstance().getHtServer().getTaskEngine().getDaoConnectionFactory().getConnection();
                            HumanTaskServer humanTaskServer = HumanTaskServiceComponent.getHumanTaskServer();
                            HumanTaskEngine humanTaskEngine = humanTaskServer.getTaskEngine();
                            HumanTaskDAOConnectionFactory humanTaskDAOConnectionFactory = humanTaskEngine.getDaoConnectionFactory();
                            humanTaskServer.getServerConfig();

                            HTQueryBuilder htQueryBuilder = new HTQueryBuilder();

                            EntityManager em = humanTaskDAOConnectionFactory.getConnection().getEntityManager();
                            Query query = htQueryBuilder.buildQueryToFindTaskInstances(taskname, em);
                            tasklist[0] = query.getResultList();
                            taskCountTemp[0] = tasklist[0].size();


                            return null;
                        }
                    });
        } catch (Exception ex) {
            handleException(ex);
        }


        return taskCountTemp[0];
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
    public int getTaskCountsForStateAndTaskName(final String taskName, final TPredefinedStatus.Enum status) throws IllegalStateFault, IllegalOperationFault, IllegalArgumentFault,
            IllegalAccessFault {

        final List[] tasklist = new List[1];

        try {

            HumanTaskServiceComponent.getHumanTaskServer().getTaskEngine().getScheduler().
                    execTransaction(new Callable<Object>() {
                        public Object call() throws Exception {


                            HumanTaskServerHolder.getInstance().getHtServer().getTaskEngine().getDaoConnectionFactory().getConnection();
                            HumanTaskServer humanTaskServer = HumanTaskServiceComponent.getHumanTaskServer();
                            HumanTaskEngine humanTaskEngine = humanTaskServer.getTaskEngine();
                            HumanTaskDAOConnectionFactory humanTaskDAOConnectionFactory = humanTaskEngine.getDaoConnectionFactory();
                            humanTaskServer.getServerConfig();

                            HTQueryBuilder htQueryBuilder = new HTQueryBuilder();

                            EntityManager em = humanTaskDAOConnectionFactory.getConnection().getEntityManager();
                            Query query = htQueryBuilder.buildQueryToCountTaskInstancesByTaskName(taskName, status, em);
                            tasklist[0] = query.getResultList();
                            if(tasklist[0].listIterator().hasNext()==true)
                                statusCount2 = tasklist[0].size();
                            boolean b=true;
                            return null;
                        }
                    });
        } catch (Exception ex) {
            handleException(ex);
        }

//
        return statusCount2;
    }

    public int getTaskCountsForStateAndTaskDefName(final String taskName, final TPredefinedStatus.Enum status) throws IllegalStateFault, IllegalOperationFault, IllegalArgumentFault,
            IllegalAccessFault {
        final List[] tasklist = new List[1];

        try {

            HumanTaskServiceComponent.getHumanTaskServer().getTaskEngine().getScheduler().
                    execTransaction(new Callable<Object>() {
                        public Object call() throws Exception {

                            //log.info("task def name reached");
                            HumanTaskServerHolder.getInstance().getHtServer().getTaskEngine().getDaoConnectionFactory().getConnection();
                            HumanTaskServer humanTaskServer = HumanTaskServiceComponent.getHumanTaskServer();
                            HumanTaskEngine humanTaskEngine = humanTaskServer.getTaskEngine();
                            HumanTaskDAOConnectionFactory humanTaskDAOConnectionFactory = humanTaskEngine.getDaoConnectionFactory();
                            humanTaskServer.getServerConfig();

                            HTQueryBuilder htQueryBuilder = new HTQueryBuilder();

                            EntityManager em = humanTaskDAOConnectionFactory.getConnection().getEntityManager();
                            Query query = htQueryBuilder.buildQueryToCountTaskInstancesByTaskDefName(taskName, status, em);
                            tasklist[0] = query.getResultList();
                            statusCount2 = Integer.parseInt(tasklist[0].get(0).toString());
                            return null;
                        }
                    });
        } catch (Exception ex) {
            handleException(ex);
        }

//
        return statusCount2;
    }

    /**
     * @return
     * @throws IllegalAccessFault
     * @throws IllegalArgumentFault
     * @throws IllegalStateFault
     * @throws IllegalOperationFault
     */
    public DeployedTaskDetail[][] getAllDeployedTasksDetails() throws IllegalAccessFault, IllegalArgumentFault, IllegalStateFault, IllegalOperationFault {
        DeployedTaskDetail[][] dtt = null;
        long[] tenantIDs = getAllTenantIDs();
        dtt = new DeployedTaskDetail[tenantIDs.length][];
        HumanTaskBaseConfiguration htc;

        int i = 0;
        for (Long tenantID : tenantIDs) {
            List<HumanTaskBaseConfiguration> htcList = getAlldeployedTasks(tenantID);
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

    public DeployedTaskDetail getDeployedTasksDetails(String taskName) throws IllegalAccessFault, IllegalArgumentFault, IllegalStateFault, IllegalOperationFault {
        DeployedTaskDetail dtt;
        dtt = new DeployedTaskDetail();
        HumanTaskBaseConfiguration htc;
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        //String tenantid=CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        HumanTaskServer humanTaskServer = HumanTaskServiceComponent.getHumanTaskServer();
        HumanTaskStore humanTaskStore = humanTaskServer.getTaskStoreManager().getHumanTaskStore(-1234);       //tenantId should be passed
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

    /**
     * @return
     * @throws IllegalAccessFault
     * @throws IllegalArgumentFault
     * @throws IllegalStateFault
     * @throws IllegalOperationFault
     */
    public String[] getAllDeployedTasks() throws IllegalAccessFault, IllegalArgumentFault, IllegalStateFault, IllegalOperationFault, PackageManagementException {
        long[] tenantIDs;
        tenantIDs = getAllTenantIDs();
        String[][] tasklist = new String[tenantIDs.length][];
        String[][] tasknamelist = new String[tenantIDs.length][];
        String[] result;
        //int i=0;
        int j = 0;
        int countTenant = 0;
        /**
         * there should be at least one task available for each tenant/tenantID since the tenantId is get from the deploymentUnit Table
         */

        HumanTaskPackageManagementSkeleton htl = new HumanTaskPackageManagementSkeleton();
        DeployedTaskDefinitionsPaginated res = htl.listDeployedTaskDefinitionsPaginated(0);
        TaskDefinition_type0[] taskDefinition_type0s = res.getTaskDefinition();


        for (Long tenantID : tenantIDs) {

            List<HumanTaskBaseConfiguration> htc = getAlldeployedTasks(tenantID);
            tasklist[countTenant++] = new String[htc.size()];
            tasknamelist[countTenant - 1] = new String[htc.size()];
            j += htc.size();
            for (int i = 0; i < htc.size(); i++) {
                tasklist[countTenant - 1][i] = " " + String.format("%1$-" + 4 + "s", tenantID) + " " + String.format("%1$-" + 55 + "s", htc.get(i).getName()) + "  " + String.format("%1$-" + 55 + "s", htc.get(i).getDefinitionName()) + " " + String.format("%1$-" + 10 + "s", htc.get(i).getOperation()) + " " + String.format("%1$-" + 15 + "s", htc.get(i).getPortName());
                tasknamelist[countTenant - 1][i] = "" + htc.get(i).getName();

            }
        }
        int k = 0;
        result = new String[j + 2];
        result[k++] = String.format("%1$-" + 11 + "s", "InstCount ") + "  " + String.format("%1$-" + 4 + "s", "TID") + "  " + String.format("%1$-" + 55 + "s", "Task Name") + "  " + String.format("%1$-" + 55 + "s", "Task Definition Name") + " " + String.format("%1$-" + 10 + "s", "Operation") + " " + String.format("%1$-" + 15 + "s", "Port name");
        result[k++] = String.format("%1$-" + 11 + "s", "").replace(" ", "-") + "  " + String.format("%1$-" + 4 + "s", "").replace(" ", "-") + "  " + String.format("%1$-" + 55 + "s", "").replace(" ", "-") + "  " + String.format("%1$-" + 55 + "s", "").replace(" ", "-") + " " + String.format("%1$-" + 10 + "s", "").replace(" ", "-") + " " + String.format("%1$-" + 15 + "s", "").replace(" ", "-");

        for (int a = 0; a < tasklist.length; a++) {
            for (int b = 0; b < tasklist[a].length; b++) {
                result[k++] = " [ " + String.format("%1$-" + 6 + "s", getTaskInstanceCountForTaskName(tasknamelist[a][b])) + " ] " + tasklist[a][b];
            }
        }


        return result;
    }


    /**
     * @param ltenantID
     * @return all the deployed tasks for the given tenant
     */

    public List<HumanTaskBaseConfiguration> getAlldeployedTasks(long ltenantID) {
        this.tenantID = (int) ltenantID;
        String[] deployedTasks;
        String[] noTask = {"No deployed task for the specified tenant"};
        String[] noStore = {"No Human Tasks Store found for the given tenantID"};
        HumanTaskServer humanTaskServer = HumanTaskServiceComponent.getHumanTaskServer();
        HumanTaskStore humanTaskStore = humanTaskServer.getTaskStoreManager().getHumanTaskStore(tenantID);


        if (humanTaskStore == null) {
            return null;
        }

        List<HumanTaskBaseConfiguration> humanTaskConfigurations = humanTaskStore.getTaskConfigurations();

        taskCountPerTenant = humanTaskConfigurations.size();
        taskCount.put(tenantID, taskCountPerTenant);

        deployedTasks = new String[humanTaskConfigurations.size()];
        for (int i = 0; i < humanTaskConfigurations.size(); i++) {
            deployedTasks[i] = " " + tenantID + " " + humanTaskConfigurations.get(i).getName() + "\t" + humanTaskConfigurations.get(i).getDefinitionName() + "\t" + humanTaskConfigurations.get(i).getOperation() + " \t" + humanTaskConfigurations.get(i).getPortName();

        }

        if (deployedTasks.length == 0) {
            return null;
        }
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
    public String[] getTaskInstances(final TPredefinedStatus.Enum status) throws IllegalAccessFault, IllegalArgumentFault, IllegalStateFault, IllegalOperationFault {
        String[] instances = {"null"};
        final List[] tasklist = new List[1];

        try {

            HumanTaskServiceComponent.getHumanTaskServer().getTaskEngine().getScheduler().
                    execTransaction(new Callable<Object>() {
                        public Object call() throws Exception {

                            //log.info("-------------------------------------------------");
                            HumanTaskServerHolder.getInstance().getHtServer().getTaskEngine().getDaoConnectionFactory().getConnection();
                            HumanTaskServer humanTaskServer = HumanTaskServiceComponent.getHumanTaskServer();
                            HumanTaskEngine humanTaskEngine = humanTaskServer.getTaskEngine();
                            HumanTaskDAOConnectionFactory humanTaskDAOConnectionFactory = humanTaskEngine.getDaoConnectionFactory();
                            humanTaskServer.getServerConfig();

                            HTQueryBuilder htQueryBuilder = new HTQueryBuilder();
                            TransactionManager transactionManager = humanTaskServer.getDatabase().getTnxManager();
                            EntityManager em = humanTaskDAOConnectionFactory.getConnection().getEntityManager();
                            Query query = htQueryBuilder.buildQueryToFindTaskInstances(status, em);
                            tasklist[0] = query.getResultList();
                            return null;
                        }
                    });

        } catch (Exception ex) {
            handleException(ex);
        }
        int i = 0;
        instances = new String[tasklist[0].size() + 2];

        instances[i++] = String.format("%1$-" + 7 + "s", "ID") + String.format("%1$-" + 60 + "s", "Name") + String.format("%1$-" + 60 + "s", "Definition Name") + String.format("%1$-" + 30 + "s", "Created on");
        instances[i++] = String.format("%1$-" + 6 + "s", "").replace(" ", "-") + " " + String.format("%1$-" + 59 + "s", "").replace(" ", "-") + " " + String.format("%1$-" + 59 + "s", "").replace(" ", "-") + " " + String.format("%1$-" + 30 + "s", "").replace(" ", "-");


        for (Object o : tasklist[0]) {
            TaskDAO taskDAOinstance = (TaskDAO) o;
            instances[i++] = String.format("%1$-" + 7 + "s", taskDAOinstance.getId()) + String.format("%1$-" + 60 + "s", taskDAOinstance.getName()) + String.format("%1$-" + 60 + "s", taskDAOinstance.getDefinitionName()) + String.format("%1$-" + 30 + "s", taskDAOinstance.getCreatedOn());
        }

        return instances;
    }

    /**
     * @param taskid
     * @return all the task details for the given taskID
     * @throws IllegalAccessFault
     * @throws IllegalArgumentFault
     * @throws IllegalStateFault
     * @throws IllegalOperationFault
     * @throws URI.MalformedURIException
     */


    //arraylist
    public String[] getTaskDataById(String taskid) throws IllegalAccessFault, IllegalArgumentFault, IllegalStateFault, IllegalOperationFault, URI.MalformedURIException {

        String[] output = {""};
        List<String> outputList = new ArrayList<String>();
        String temp = null;
        TaskDAO task;

        URI uri = new URI(taskid);

        try {
            final Long taskId = validateTaskId(uri);
            task = HumanTaskServiceComponent.getHumanTaskServer().getTaskEngine().getScheduler().
                    execTransaction(new Callable<TaskDAO>() {
                        public TaskDAO call() throws Exception {
                            HumanTaskEngine engine = HumanTaskServiceComponent.getHumanTaskServer().getTaskEngine();
                            HumanTaskDAOConnection daoConn = engine.getDaoConnectionFactory().getConnection();
                            TaskDAO task = daoConn.getTask(taskId);
                            return task;
                        }

                    });

        } catch (Exception ex) {
            log.error(ex);
            throw new IllegalAccessFault(ex);
        }


        GenericHumanRoleDAO.GenericHumanRoleType[] ghrt = GenericHumanRoleDAO.GenericHumanRoleType.values();
        int ghrtlength = ghrt.length;

        MessageDAO inputMessageDAO = task.getInputMessage();
        MessageDAO outputMessageDAO = task.getOutputMessage();
        String description = task.getTaskDescription("text/plain");

        temp = String.format("%1$-" + 25 + "s", "Task Name") + ":" + task.getName();
        outputList.add(temp);

        for (int i = 0; i < ghrtlength; i++) {
            List<OrganizationalEntityDAO> orgDAO = CommonTaskUtil.getOrgEntitiesForRole(task, ghrt[i]);
            if (orgDAO.size() > 0) {
                temp = String.format("%1$-" + 25 + "s", ghrt[i]) + ":";

                int orgDAOSize = orgDAO.size();
                for (int j = 0; j < orgDAOSize; j++) {
                    temp = temp + orgDAO.get(j).getName() + " [" + orgDAO.get(j).getOrgEntityType() + "]  ";
                }
                outputList.add(temp);
                temp = null;

            }

        }
        if (description != null) {
            temp = String.format("%1$-" + 25 + "s", "Task Description") + ":" + task.getTaskDescription("text/plain");
            outputList.add(temp);


        }

        Element inputBbodyData = inputMessageDAO.getBodyData();
        if (inputBbodyData != null) {
            temp = String.format("%1$-" + 25 + "s", "Task Input") + ":";
            temp = temp + "\n" + DOMUtils.domToString(inputBbodyData);
            outputList.add(temp);
        }


        if (outputMessageDAO != null) {
            Element outputBodyData = outputMessageDAO.getBodyData();
            if (outputBodyData != null) {
                temp = String.format("%1$-" + 25 + "s", "Task Output") + ":";

                temp = temp + "\n" + DOMUtils.domToString(outputBodyData);
                outputList.add(temp);
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
     * @param ex
     * @throws IllegalStateFault
     * @throws IllegalOperationFault
     * @throws IllegalArgumentFault
     * @throws IllegalAccessFault
     */
    private void handleException(Exception ex) throws IllegalStateFault, IllegalOperationFault, IllegalArgumentFault,
            IllegalAccessFault {
        log.error(ex);

        if (ex instanceof HumanTaskIllegalAccessException) {
            throw new IllegalAccessFault(ex.getMessage());
        } else if (ex instanceof HumanTaskIllegalArgumentException) {
            throw new IllegalArgumentFault(ex.getMessage());
        } else if (ex instanceof HumanTaskIllegalOperationException) {
            throw new IllegalOperationFault(ex.getMessage());
        } else if (ex instanceof HumanTaskIllegalStateException) {
            throw new IllegalStateFault(ex.getMessage());
        } else {
            throw new IllegalStateFault(ex.getMessage());
        }
    }


    /**
     * @return
     * @throws IllegalAccessFault
     * @throws IllegalArgumentFault
     * @throws IllegalStateFault
     * @throws IllegalOperationFault
     */
    public long[] getAllTenantIDs() throws IllegalAccessFault, IllegalArgumentFault, IllegalStateFault, IllegalOperationFault {
        long[] result = null;
        final List[] tasklist = new List[1];


        try {

            HumanTaskServiceComponent.getHumanTaskServer().getTaskEngine().getScheduler().
                    execTransaction(new Callable<Object>() {
                        public Object call() throws Exception {


                            HumanTaskServerHolder.getInstance().getHtServer().getTaskEngine().getDaoConnectionFactory().getConnection();
                            HumanTaskServer humanTaskServer = HumanTaskServiceComponent.getHumanTaskServer();
                            HumanTaskEngine humanTaskEngine = humanTaskServer.getTaskEngine();
                            HumanTaskDAOConnectionFactory humanTaskDAOConnectionFactory = humanTaskEngine.getDaoConnectionFactory();
                            humanTaskServer.getServerConfig();

                            HTQueryBuilder htQueryBuilder = new HTQueryBuilder();
                            TransactionManager transactionManager = humanTaskServer.getDatabase().getTnxManager();
                            EntityManager em = humanTaskDAOConnectionFactory.getConnection().getEntityManager();
                            Query query = htQueryBuilder.getTenantIDs(em);
                            tasklist[0] = query.getResultList();


                            return null;
                        }
                    });

        } catch (Exception ex) {
            handleException(ex);
        }


        int i = 0;
        Set<Long> tid = new HashSet<Long>();
        for (Object object : tasklist[0]) {

            DeploymentUnitDAO duDAO = (DeploymentUnitDAO) object;

            tid.add(duDAO.getTenantId());

        }
        result = new long[tid.size()];
        Iterator<Long> iterator = tid.iterator();
        while (iterator.hasNext()) {
            result[i++] = iterator.next().longValue();
        }
        return result;
    }


    /**
     * @return
     * @throws IllegalAccessFault
     * @throws IllegalArgumentFault
     * @throws IllegalStateFault
     * @throws IllegalOperationFault
     */
    private EntityManager getEntityManager() throws IllegalAccessFault, IllegalArgumentFault, IllegalStateFault, IllegalOperationFault {

        final EntityManager[] em = new EntityManager[1];
        try {

            HumanTaskServiceComponent.getHumanTaskServer().getTaskEngine().getScheduler().
                    execTransaction(new Callable<Object>() {
                        public Object call() throws Exception {


                            HumanTaskServerHolder.getInstance().getHtServer().getTaskEngine().getDaoConnectionFactory().getConnection();
                            HumanTaskServer humanTaskServer = HumanTaskServiceComponent.getHumanTaskServer();
                            HumanTaskEngine humanTaskEngine = humanTaskServer.getTaskEngine();
                            HumanTaskDAOConnectionFactory humanTaskDAOConnectionFactory = humanTaskEngine.getDaoConnectionFactory();
                            humanTaskServer.getServerConfig();

                            HTQueryBuilder htQueryBuilder = new HTQueryBuilder();
                            TransactionManager transactionManager = humanTaskServer.getDatabase().getTnxManager();
                            em[0] = humanTaskDAOConnectionFactory.getConnection().getEntityManager();

//
                            return null;
                        }
                    });
        } catch (Exception ex) {
            handleException(ex);
        }


        return em[0];

    }


}
