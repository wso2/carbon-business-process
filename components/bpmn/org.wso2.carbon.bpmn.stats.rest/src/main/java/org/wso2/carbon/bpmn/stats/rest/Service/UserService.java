package org.wso2.carbon.bpmn.stats.rest.Service;

import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.task.Task;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.authenticator.stub.AuthenticationAdminStub;
import org.wso2.carbon.bpmn.stats.rest.Model.InstanceStatPerMonth;
import org.wso2.carbon.bpmn.stats.rest.util.BPMNOsgiServices;
import org.wso2.carbon.bpmn.stats.rest.Model.UserInfo;
import org.wso2.carbon.user.mgt.stub.UserAdminStub;
import org.wso2.carbon.utils.NetworkUtils;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

@Path("/userServices/")
public class UserService {
    private static final Log log = LogFactory.getLog(UserService.class);

    /**
     *
     * @return list of users retrieved from the UserStore
     */
    @GET
    @Path("/allUsers/")
    @Produces(MediaType.APPLICATION_JSON)
    public String[] getUserList() {
        String[] users = null;
        UserAdminStub userAdminStub;
        Properties properties = null;
        try {
            String clientTrustStorePath = "/home/natasha/Documents/GitRepos/product-bps/modules/distribution/target/wso2bps-3.5.1-SNAPSHOT/repository/resources/security/wso2carbon.jks";
            String trustStorePassword = "wso2carbon";
            String trustStoreType = "JKS";
            setKeyStore(clientTrustStorePath, trustStorePassword, trustStoreType);
            String serviceUrl = "https://localhost:9443/services/UserAdmin";
            userAdminStub = new UserAdminStub(serviceUrl);
            ServiceClient client = userAdminStub._getServiceClient();
            Options option = client.getOptions();
            option.setManageSession(true);
            option.setProperty(HTTPConstants.COOKIE_STRING, login());
            userAdminStub._getServiceClient().getOptions().setTimeOutInMilliSeconds(600000);
            users = userAdminStub.listUsers("*", -1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return users;
    }

    /**
     * Setup key store according to the config.properties
     * @param clientTrustStorePath
     * @param trustStorePassword
     * @param trustStoreType
     */
    private void setKeyStore(String clientTrustStorePath, String trustStorePassword, String trustStoreType) {
        System.setProperty("javax.net.ssl.trustStore", clientTrustStorePath);
        System.setProperty("javax.net.ssl.trustStorePassword", trustStorePassword);
        System.setProperty("javax.net.ssl.trustStoreType", trustStoreType);
    }

    /**
     * Creates the login session BPS login
     * @return cookie String
     * @throws Exception
     */
    private String login() throws Exception {

        AuthenticationAdminStub authenticationAdminStub;
        String authenticationAdminServiceURL = "https://localhost:9443/services/AuthenticationAdmin";
        authenticationAdminStub = new AuthenticationAdminStub(authenticationAdminServiceURL);
        ServiceClient client = authenticationAdminStub._getServiceClient();
        Options options = client.getOptions();
        options.setManageSession(true);
        String userName = "admin";
        String password = "admin";
        String hostName = NetworkUtils.getLocalHostname();
        authenticationAdminStub.login(userName, password, hostName);
        ServiceContext serviceContext = authenticationAdminStub.
                _getServiceClient().getLastOperationContext().getServiceContext();
        return (String) serviceContext.getProperty(HTTPConstants.COOKIE_STRING);
    }

    public UserService() {

    }
    /**
     * Get the No.of tasks completed by each user
     * @return list with the no.of tasks completed by each user
     */
    @GET
    @Path("/userVsTaskCount/")
    @Produces(MediaType.APPLICATION_JSON)
    public List<UserInfo> getNoOfTasksCompletedByUser() {

        List<UserInfo> listOfUsers = new ArrayList<>();
        UserInfo userInfo = new UserInfo();
        String[] users = getUserList();

        for (String u : users) {
            userInfo = new UserInfo();
            userInfo.setUserName(u);

            long xxx = BPMNOsgiServices.getHistoryService()
                    .createHistoricTaskInstanceQuery().taskAssignee(u).finished().count();
            if (xxx == 0) {
                userInfo.setTaskCount(0);
            } else {
                userInfo.setTaskCount(xxx);
            }
            listOfUsers.add(userInfo);
        }
        return listOfUsers;
    }

    /**
     * Get the average time duration taken by each user to complete tasks
     * @return list with the average time duration taken by each user to complete tasks
     */
    @GET
    @Path("/userVsAvgTimeDuration/")
    @Produces(MediaType.APPLICATION_JSON)
    public List<UserInfo> getAvgDurationForTasksCompletedByUser() {

        List<UserInfo> listOfUsers = new ArrayList<>();
        UserInfo userInfo = new UserInfo();
        String[] users = getUserList();
        for (String u : users) {

            userInfo = new UserInfo();
            userInfo.setUserName(u);

            long count = BPMNOsgiServices.getHistoryService()
                    .createHistoricTaskInstanceQuery().taskAssignee(u).finished().count();
            if (count == 0) {
                double avgTime = 0;
                userInfo.setAvgTimeDuration(avgTime);
            } else {
                List<HistoricTaskInstance> taskList = BPMNOsgiServices.getHistoryService()
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
        return listOfUsers;
    }

    /**
     * Task variation of user over time i.e. tasks started and completed by the user -- User Performance
     * @param assignee taskAssignee/User selected to view the user performance of task completion over time
     * @return array with the tasks started and completed of the selected user
     */
    @GET
    @Path("/userTaskVariation/{assignee}")
    @Produces(MediaType.APPLICATION_JSON)
    public InstanceStatPerMonth[] taskVariationOverTime(@PathParam("assignee") String assignee) {

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
        List<HistoricTaskInstance> taskList = BPMNOsgiServices.getHistoryService()
                .createHistoricTaskInstanceQuery().taskAssignee(assignee).finished().list();
        for (HistoricTaskInstance instance : taskList) {
            int startTime = Integer.parseInt(ft.format(instance.getCreateTime()));
            int endTime = Integer.parseInt(ft.format(instance.getEndTime()));
            taskStatPerMonths[startTime - 1].setStartedInstances(taskStatPerMonths[startTime - 1].getStartedInstances() + 1);
            taskStatPerMonths[endTime - 1].setCompletedInstances(taskStatPerMonths[endTime - 1].getCompletedInstances() + 1);

        }
        // Get active/started tasks
        List<Task> taskActive = BPMNOsgiServices.getTaskService().createTaskQuery().taskAssignee(assignee).active().list();
        for (Task instance : taskActive) {
            int startTime = Integer.parseInt(ft.format(instance.getCreateTime()));
            taskStatPerMonths[startTime - 1].setStartedInstances(taskStatPerMonths[startTime - 1].getStartedInstances() + 1);
        }

        // Get suspended tasks
        List<Task> taskSuspended = BPMNOsgiServices.getTaskService().createTaskQuery().taskAssignee(assignee).suspended().list();
        for (Task instance : taskSuspended) {
            int startTime = Integer.parseInt(ft.format(instance.getCreateTime()));
            taskStatPerMonths[startTime - 1].setStartedInstances(taskStatPerMonths[startTime - 1].getStartedInstances() + 1);
        }
        return taskStatPerMonths;
    }

}
