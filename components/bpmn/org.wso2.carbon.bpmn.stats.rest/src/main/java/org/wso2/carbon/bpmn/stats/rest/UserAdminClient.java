package org.wso2.carbon.bpmn.stats.rest;

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
import org.wso2.carbon.context.CarbonContext;
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
public class UserAdminClient {
    private static final Log log = LogFactory.getLog(UserAdminClient.class);

    //Get registry properties from the regPath location
    @GET
    @Path("/allUsers/")
    @Produces(MediaType.APPLICATION_JSON)
    public String[] getUserList() {
        String[] users=null;
        UserAdminStub userAdminStub;
        Properties properties = null;
        try {
            String clientTrustStorePath = "/home/natasha/Documents/GitRepos/product-bps/modules/distribution/target/wso2bps-3.5.1-SNAPSHOT/repository/resources/security/wso2carbon.jks";
            String trustStorePassword = "wso2carbon";
            String trustStoreType = "JKS";
            log.info("1");
            setKeyStore(clientTrustStorePath, trustStorePassword, trustStoreType);
            log.info("2");
            String serviceUrl = "https://localhost:9443/services/UserAdmin";
            log.info("3");
            userAdminStub = new UserAdminStub(serviceUrl);
            log.info("4");
            ServiceClient client = userAdminStub._getServiceClient();
            log.info("5");
            Options option = client.getOptions();
            log.info("6");
            option.setManageSession(true);
            log.info("7");
            option.setProperty(HTTPConstants.COOKIE_STRING, login());
            log.info("8");
            userAdminStub._getServiceClient().getOptions().setTimeOutInMilliSeconds(600000);
            log.info("9");

            users = userAdminStub.listUsers("*", -1);
            for(String user: users){
                log.info("&&&&&&&&&&&&&" + user);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }


        return users;
    }

    //Setup key store according to the config.properties
    private void setKeyStore(String clientTrustStorePath, String trustStorePassword, String trustStoreType) {
        log.info("10");
        System.setProperty("javax.net.ssl.trustStore", clientTrustStorePath);
        log.info("11");
        System.setProperty("javax.net.ssl.trustStorePassword", trustStorePassword);
        log.info("12");
        System.setProperty("javax.net.ssl.trustStoreType", trustStoreType);
    }

    //Creates the login session BPS login
    private String login() throws Exception {

        AuthenticationAdminStub authenticationAdminStub;
        log.info("13");
        String authenticationAdminServiceURL = "https://localhost:9443/services/AuthenticationAdmin";
        log.info("14");
        authenticationAdminStub = new AuthenticationAdminStub(authenticationAdminServiceURL);
        log.info("15");
        ServiceClient client = authenticationAdminStub._getServiceClient();
        log.info("16");
        Options options = client.getOptions();
        log.info("17");
        options.setManageSession(true);
        log.info("18");
        String userName = "admin";
        String password = "admin";
        log.info("19");
        String hostName = NetworkUtils.getLocalHostname();
        log.info("20");
        authenticationAdminStub.login(userName, password, hostName);
        log.info("21");

        ServiceContext serviceContext = authenticationAdminStub.
                _getServiceClient().getLastOperationContext().getServiceContext();
        log.info("22");

        return (String) serviceContext.getProperty(HTTPConstants.COOKIE_STRING);
    }


    public UserAdminClient() {

    }

    /**
     * Get the No.of tasks completed by each user
     */
    @GET
    @Path("/userVsTaskCount/")
    @Produces(MediaType.APPLICATION_JSON)
    public List<UserInfo> getNoOfTasksCompletedByUser() {

        //Map<String, Long> map = new HashMap<String, Long>();

        List<UserInfo> listOfUsers = new ArrayList<>();
        UserInfo userInfo = new UserInfo();
        String [] users = getUserList();

        for(String u : users){
            userInfo = new UserInfo();
            userInfo.setUserName(u);

            long xxx = BPMNOsgiServices.getHistoryService()
                    .createHistoricTaskInstanceQuery().taskAssignee(u).finished().count();
            if(xxx == 0 ){
                //log.info("User ---- "+u+ "No completed tasks"+xxx);
                userInfo.setTaskCount(0);

               // map.put(u, xxx);
            }
            else {
               // log.info("User ---- " + u + "   tasks completed----- " + xxx);
                userInfo.setTaskCount(xxx);
                //map.put(u, xxx);
            }
            listOfUsers.add(userInfo);
        }
        return listOfUsers;
    }

    /**
     * Get the average time duration of each user
     */
    @GET
    @Path("/userVsAvgTimeDuration/")
    @Produces(MediaType.APPLICATION_JSON)
    public List<UserInfo>  getAvgDurationForTasksCompletedByUser() {
        //Map<String, Long> map = new HashMap<String, Long>();

        List<UserInfo> listOfUsers = new ArrayList<>();
        UserInfo userInfo = new UserInfo();

        String [] users = getUserList();
        for(String u : users){

            userInfo = new UserInfo();
            userInfo.setUserName(u);

            long xxx = BPMNOsgiServices.getHistoryService()
                    .createHistoricTaskInstanceQuery().taskAssignee(u).finished().count();
            if(xxx == 0 ){
                log.info("User ---- "+u+ "No completed tasks");
                double avgTime=0;
                userInfo.setAvgTimeDuration(avgTime);
                //map.put(u,avgTime);
            }
            else {
                log.info("User ---- " + u + "   tasks completed----- " + xxx);
                List<HistoricTaskInstance> taskList = BPMNOsgiServices.getHistoryService()
                        .createHistoricTaskInstanceQuery().taskAssignee(u).finished().list();
                double totalTime=0;
                double avgTime=0;
                for (HistoricTaskInstance instance : taskList) {

                    double taskDuration = instance.getDurationInMillis();
                    totalTime = totalTime + taskDuration;


                }
                log.info("Total time --- "+totalTime+ "    Average Time ---"+avgTime);
                avgTime = (totalTime /  xxx)/1000;
                userInfo.setAvgTimeDuration(avgTime);
               // map.put(u,avgTime);
            }
            listOfUsers.add(userInfo);
        }

        return listOfUsers;
    }

    /**
     * Task variation over time i.e. tasks started and completed
     */
    @GET
    @Path("/userTaskVariation/{assignee}")
    @Produces(MediaType.APPLICATION_JSON)
    public InstanceStatPerMonth[] taskVariationOverTime(@PathParam("assignee") String assignee) {
       // String [] users = getUserList();
       // for(String u : users) {

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


     //   }
        return taskStatPerMonths;

    }

}
