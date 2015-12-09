/**
 *  Copyright (c) 2015 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.wso2.carbon.bpmn.rest.service.stats;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.task.Task;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bpmn.rest.model.stats.InstanceStatPerMonth;
import org.wso2.carbon.bpmn.rest.model.stats.ResponseHolder;
import org.wso2.carbon.bpmn.rest.model.stats.UserInfo;
import org.wso2.carbon.bpmn.rest.common.utils.BPMNOSGIService;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Service class which includes functionalities related to users
 */

@Path("/userServices/")
public class UserService {
    private static final Log log = LogFactory.getLog(UserService.class);

    /**
     * @return list of users retrieved from the UserStore
     */
    @GET
    @Path("/allUsers/")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public ResponseHolder getUserList() {
        Object[] users = null;

        ResponseHolder response = new ResponseHolder();
        try {
            users = (Object[]) BPMNOSGIService.getUserRealm().getUserStoreManager().listUsers("*", -1);
            response.setData(Arrays.asList(users));
        } catch (Exception e) {
            throw new ActivitiException("Unable to get the user list", e);
        }
        return response;
    }

    public UserService() {
    }

    /**
     * Get the No.of tasks completed by each user
     *
     * @return list with the no.of tasks completed by each user
     */
    @GET
    @Path("/userVsTaskCount/")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public ResponseHolder getNoOfTasksCompletedByUser() {

        List listOfUsers = new ArrayList<>();
        ResponseHolder response = new ResponseHolder();
        String[] users = (String[]) getUserList().getData().toArray();

        for (String u : users) {
            UserInfo userInfo = new UserInfo();
            userInfo.setUserName(u);

            long xxx = BPMNOSGIService.getHistoryService()
                    .createHistoricTaskInstanceQuery().taskAssignee(u).finished().count();
            if (xxx == 0) {
                userInfo.setTaskCount(0);
            } else {
                userInfo.setTaskCount(xxx);
            }
            listOfUsers.add(userInfo);
        }
        response.setData(listOfUsers);
        return response;
    }

    /**
     * Get the average time duration taken by each user to complete tasks
     *
     * @return list with the average time duration taken by each user to complete tasks
     */
    @GET
    @Path("/userVsAvgTimeDuration/")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public ResponseHolder getAvgDurationForTasksCompletedByUser() {

        List listOfUsers = new ArrayList<>();
        ResponseHolder response = new ResponseHolder();
        String[] users = (String[]) getUserList().getData().toArray();
        for (String u : users) {

            UserInfo userInfo = new UserInfo();
            userInfo.setUserName(u);

            long count = BPMNOSGIService.getHistoryService()
                    .createHistoricTaskInstanceQuery().taskAssignee(u).finished().count();
            if (count == 0) {
                double avgTime = 0;
                userInfo.setAvgTimeDuration(avgTime);
            } else {
                List<HistoricTaskInstance> taskList = BPMNOSGIService.getHistoryService()
                        .createHistoricTaskInstanceQuery().taskAssignee(u).finished().list();
                double totalTime = 0;
                double avgTime = 0;
                for (HistoricTaskInstance instance : taskList) {
                    double taskDuration = instance.getDurationInMillis();
                    totalTime = totalTime + taskDuration;
                }
                avgTime = (totalTime / count) / 1000;
                userInfo.setAvgTimeDuration(avgTime);
            }
            listOfUsers.add(userInfo);
        }
        response.setData(listOfUsers);
        return response;
    }

    /**
     * Task variation of user over time i.e. tasks started and completed by the user -- User Performance
     *
     * @param assignee taskAssignee/User selected to view the user performance of task completion over time
     * @return array with the tasks started and completed of the selected user
     */
    @GET
    @Path("/userTaskVariation/{assignee}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public ResponseHolder taskVariationOverTime(@PathParam("assignee") String assignee) {
        ResponseHolder response = new ResponseHolder();
        List list = new ArrayList();
        String[] MONTHS = {"Jan", "Feb", "March", "April", "May", "June", "July", "Aug", "Sep", "Oct", "Nov", "Dec"};
        SimpleDateFormat ft = new SimpleDateFormat("M");

        InstanceStatPerMonth[] taskStatPerMonths = new InstanceStatPerMonth[12];
        for (int i = 0; i < taskStatPerMonths.length; i++) {
            taskStatPerMonths[i] = new InstanceStatPerMonth();
            taskStatPerMonths[i].setMonth(MONTHS[i]);
            taskStatPerMonths[i].setCompletedInstances(0);
            taskStatPerMonths[i].setStartedInstances(0);
        }
        // Get completed tasks
        List<HistoricTaskInstance> taskList = BPMNOSGIService.getHistoryService()
                .createHistoricTaskInstanceQuery().taskAssignee(assignee).finished().list();
        for (HistoricTaskInstance instance : taskList) {
            int startTime = Integer.parseInt(ft.format(instance.getCreateTime()));
            int endTime = Integer.parseInt(ft.format(instance.getEndTime()));
            taskStatPerMonths[startTime - 1].setStartedInstances(taskStatPerMonths[startTime - 1].getStartedInstances() + 1);
            taskStatPerMonths[endTime - 1].setCompletedInstances(taskStatPerMonths[endTime - 1].getCompletedInstances() + 1);

        }
        // Get active/started tasks
        List<Task> taskActive = BPMNOSGIService.getTaskService().createTaskQuery().taskAssignee(assignee).active().list();
        for (Task instance : taskActive) {
            int startTime = Integer.parseInt(ft.format(instance.getCreateTime()));
            taskStatPerMonths[startTime - 1].setStartedInstances(taskStatPerMonths[startTime - 1].getStartedInstances() + 1);
        }

        // Get suspended tasks
        List<Task> taskSuspended = BPMNOSGIService.getTaskService().createTaskQuery().taskAssignee(assignee).suspended().list();
        for (Task instance : taskSuspended) {
            int startTime = Integer.parseInt(ft.format(instance.getCreateTime()));
            taskStatPerMonths[startTime - 1].setStartedInstances(taskStatPerMonths[startTime - 1].getStartedInstances() + 1);
        }

        for (int i = 0; i < taskStatPerMonths.length; i++) {
            list.add(taskStatPerMonths[i]);
        }
        response.setData(list);
        return response;
    }

}
