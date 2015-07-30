<!--
~ Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
~
~ WSO2 Inc. licenses this file to you under the Apache License,
~ Version 2.0 (the "License"); you may not use this file except
~ in compliance with the License.
~ You may obtain a copy of the License at
~
~ http://www.apache.org/licenses/LICENSE-2.0
~
~ Unless required by applicable law or agreed to in writing,
~ software distributed under the License is distributed on an
~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
~ KIND, either express or implied. See the License for the
~ specific language governing permissions and limitations
~ under the License.
-->
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>

<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.bpmn.ui.WorkflowServiceClient" %>
<%@ page import="org.wso2.carbon.context.CarbonContext" %>
<%@ page import="org.wso2.carbon.registry.api.Registry" %>
<%@ page import="org.wso2.carbon.context.RegistryType" %>
<%@ page import="org.wso2.carbon.registry.api.RegistryException" %>
<%@ page import="org.wso2.carbon.registry.core.utils.RegistryUtils" %>
<%@ page import="org.wso2.carbon.registry.api.Resource" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<fmt:bundle basename="org.wso2.carbon.bpmn.ui.i18n.Resources">

    <%
        String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        ConfigurationContext configContext =
                (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);

        WorkflowServiceClient client;
        try {
            client = new WorkflowServiceClient(cookie, serverURL, configContext);
        } catch (Exception e) {
            CarbonUIMessage.sendCarbonUIMessage(e.getMessage(), CarbonUIMessage.ERROR, request, e);
    %>
    <script type="text/javascript">
        location.href = "../admin/error.jsp";
    </script>
    <%
            return;
        }
        RegistryUtils.setTrustStoreSystemProperties();

        String thriftUrl = request.getParameter("thrift_url");
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String isProcessInstanceIdChecked = "", isStartActivityIdChecked = "", isStartUserIdChecked = "",
                isProcessStartTimeChecked = "", isProcessEndTimeChecked = "", isProcessInstanceDurationChecked = "",
                isTaskInstanceIdChecked = "", isAssigneeChecked = "", isTaskProcessInstanceIdChecked = "",
                isTaskInstanceStartTimeChecked = "", isTaskInstanceEndTimeChecked = "", isTaskInstanceDurationChecked = "";

        String[] processInstanceCheckBoxValues = request.getParameterValues("processInstance");
        String[] taskInstanceCheckBoxValues = request.getParameterValues("taskInstance");

        CarbonContext context = CarbonContext.getThreadLocalCarbonContext();
        Registry configRegistry = context.getRegistry(RegistryType.SYSTEM_CONFIGURATION);

        try {
            if (configRegistry.resourceExists("bpmn/data_analytics_publisher/thrift_configuration")) {
                //if resource is available then get properties and set them to the text fields
                Resource resource = configRegistry.get("bpmn/data_analytics_publisher/thrift_configuration");
                if (thriftUrl == null) {
                    //if thrift url is null then set value from the registry
                    thriftUrl = resource.getProperty("data_receiver_thrift_url");
                } else if (!thriftUrl.equals(resource.getProperty("data_receiver_thrift_url"))) {
                    //else if user updates the thrift url then update the registry property
                    resource.setProperty("data_receiver_thrift_url", thriftUrl);
                    configRegistry.put("bpmn/data_analytics_publisher/thrift_configuration", resource);
                }
                if (username == null) {
                    //if username is null then set value from the registry
                    username = resource.getProperty("username");
                } else if (!username.equals(resource.getProperty("username"))) {
                    //else if user updates the username then update the registry property
                    resource.setProperty("username", username);
                    configRegistry.put("bpmn/data_analytics_publisher/thrift_configuration", resource);
                }
                if (password == null) {
                    //if password is null then set value from the registry
                    password = resource.getProperty("password");
                } else if (!password.equals(resource.getProperty("password"))) {
                    //else if user updates the password then update the registry property
                    resource.setProperty("password", password);
                    configRegistry.put("bpmn/data_analytics_publisher/thrift_configuration", resource);
                }
            } else {
                //if resource doesn't exists then create a new resource and add properties to it.
                if ((thriftUrl != null && (thriftUrl.startsWith("tcp://") || thriftUrl.startsWith("ssl://"))) || (username != null) || (password != null)) {
                    Resource resource = configRegistry.newResource();
                    configRegistry.put("bpmn/data_analytics_publisher/thrift_configuration", resource);
                }
            }
            if (configRegistry.resourceExists("bpmn/data_analytics_publisher/instance_properties")) {
                Resource resource = configRegistry.get("bpmn/data_analytics_publisher/instance_properties");

                if ((processInstanceCheckBoxValues != null) && (processInstanceCheckBoxValues.length > 0)) {
                    for (String value : processInstanceCheckBoxValues) {
//                        if(value.equals(resource.getProperty(value))){}
                        resource.setProperty(value, value);
                    }
                    configRegistry.put("bpmn/data_analytics_publisher/instance_properties", resource);
                }
                if ((taskInstanceCheckBoxValues != null) && (taskInstanceCheckBoxValues.length > 0)) {
                    for (String value : taskInstanceCheckBoxValues) {
                        resource.setProperty(value, value);
                    }
                    configRegistry.put("bpmn/data_analytics_publisher/instance_properties", resource);
                }

                if (resource.getProperty("processInstanceId") != null) {
                    //Check if a user remove the selection of a check box, if so after save button clicked remove it from the registry else set checked is true
                    if (isAvailableInResult(processInstanceCheckBoxValues, resource.getProperty("processInstanceId"))) {
                         isProcessInstanceIdChecked = " checked";
                    } else {
                        resource.removeProperty("processInstanceId");
                    }
                }
                if (resource.getProperty("startActivityId") != null) {
                    if(isAvailableInResult(processInstanceCheckBoxValues, resource.getProperty("startActivityId"))){
                        isStartActivityIdChecked = " checked";
                    }else {
                        resource.removeProperty("startActivityId");
                    }
                }
                if (resource.getProperty("startUserId") != null) {
                    isStartUserIdChecked = " checked";
                }
                if (resource.getProperty("processInstanceStartTime") != null) {
                    isProcessStartTimeChecked = " checked";
                }
                if (resource.getProperty("processInstanceEndTime") != null) {
                    isProcessEndTimeChecked = " checked";
                }
                if (resource.getProperty("processInstanceDuration") != null) {
                    isProcessInstanceDurationChecked = " checked";
                }
                if (resource.getProperty("taskInstanceId") != null) {
                    isTaskInstanceIdChecked = " checked";
                }
                if (resource.getProperty("assignee") != null) {
                    isAssigneeChecked = " checked";
                }
                if (resource.getProperty("taskProcessInstanceId") != null) {
                    isTaskProcessInstanceIdChecked = " checked";
                }
                if (resource.getProperty("taskInstanceStartTime") != null) {
                    isTaskInstanceStartTimeChecked = " checked";
                }
                if (resource.getProperty("taskInstanceEndTime") != null) {
                    isTaskInstanceEndTimeChecked = " checked";
                }
                if (resource.getProperty("taskInstanceDuration") != null) {
                    isTaskInstanceDurationChecked = " checked";
                }

            } else {
                Resource resource = configRegistry.newResource();
                configRegistry.put("bpmn/data_analytics_publisher/instance_properties", resource);
            }

        } catch (RegistryException e) {
            CarbonUIMessage.sendCarbonUIMessage(e.getMessage(), CarbonUIMessage.ERROR, request, e);
        }

    %>
    <%!
        private boolean isAvailableInResult(String[] result, String property) {
            for (String checkedBox : result) {
                if (checkedBox.equals(property)) {
                    return true;
                }
            }
            return false;
        }
    %>
    <script type="text/javascript">
        if (typeof String.prototype.startsWith != 'function') {
            String.prototype.startsWith = function (str) {
                return str.length > 0 && this.substring(0, str.length) === str;
            }
        }
        ;

        function validate() {
            var thriftUrl = document.bpmnDataPublisher.thrift_url.value;
            var username = document.bpmnDataPublisher.username.value;
            var password = document.bpmnDataPublisher.password.value;
            var processInstanceCheckBoxes = document.bpmnDataPublisher.processInstance;
            var taskInstanceCheckBoxes = document.bpmnDataPublisher.taskInstance;
            var procCheckLength = 0, taskCheckLength = 0;
            for (var i = 0; i < processInstanceCheckBoxes.length; i++) {
                if (processInstanceCheckBoxes[i].checked)
                    procCheckLength++;
            }
            for (var j = 0; j < taskInstanceCheckBoxes.length; j++) {
                if (taskInstanceCheckBoxes[j].checked)
                    taskCheckLength++;
            }
            if (((procCheckLength == 0) && (taskCheckLength == 0)) || (thriftUrl.length == 0) || (username.length == 0) || (password.length == 0)) {
                CARBON.showWarningDialog('Please provide correct thrift API configuration or select required fields to publish bpmn instances.');
            } else if ((!thriftUrl.startsWith("tcp")) && (!thriftUrl.startsWith("ssl"))) {
                CARBON.showWarningDialog('Incorrect thrift url is provided:' + (thriftUrl.startsWith("tcp")));
            } else {
                document.bpmnDataPublisher.submit();
            }
        }
    </script>

    <carbon:breadcrumb
            label="bpmn.data.publisher.dashboard"
            resourceBundle="org.wso2.carbon.bpmn.ui.i18n.Resources"
            topPage="true"
            request="<%=request%>"/>
    <div id="middle">
        <h2><fmt:message key="bpmn.data.publisher.dashboard"/></h2>

        <div id="workArea">
            <div id="formset">
                <form id="bpmn_data_publish_form" method="post" name="bpmnDataPublisher" action="">

                    <table class="styledLeft" id="thriftUrlTable">
                        <thead>
                        <tr>
                            <th colspan="6"><fmt:message key="bpmn.data.publisher.thrift.configuration"/></th>
                        </tr>
                        </thead>
                        <tbody>
                        <tr class="tableEvenRow">
                            <td>Thrift URL</td>
                            <td>
                                <input id="thriftUrl" type="text" style="width:100%" name="thrift_url"
                                       value="<%=(thriftUrl == null) ? "" : thriftUrl%>"
                                       placeholder="tcp://<ip address>/7611">
                            </td>
                            <td>Username</td>
                            <td>
                                <input id="username" type="text" style="width:100%" name="username"
                                       value="<%=(username == null) ? "" : username%>"
                                       placeholder="">
                            </td>
                            <td>Password</td>
                            <td>
                                <input id="password" type="password" style="width:100%" name="password"
                                       value="<%=(password == null) ? "" : password%>"
                                       placeholder="">
                            </td>
                        </tr>
                        </tbody>
                    </table>
                    <br>

                    <table class="styledLeft" id="instanceTable">
                        <thead>
                        <tr>
                            <th width="50%"><fmt:message key="bpmn.data.publisher.process.instances"/></th>
                            <th width="50%"><fmt:message key="bpmn.data.publisher.task.instances"/></th>
                        </tr>
                        </thead>
                        <tbody>

                        <tr class="tableOddRow">
                            <td>
                                <input value="processInstanceId" id="processInsId" type="checkbox"
                                       name="processInstance" <%= isProcessInstanceIdChecked %>><fmt:message
                                    key="bpmn.process.instance.id"/><br>
                            </td>
                            <td>
                                <input value="taskInstanceId" id="taskInsId" type="checkbox"
                                       name="taskInstance" <%= isTaskInstanceIdChecked %>><fmt:message
                                    key="bpmn.task.instance.id"/><br>
                            </td>
                        </tr>
                        <tr class="tableEvenRow">
                            <td>
                                <input value="startActivityId" id="startActId" type="checkbox"
                                       name="processInstance" <%= isStartActivityIdChecked %>><fmt:message
                                    key="bpmn.start.activity.id"/><br>
                            </td>
                            <td>
                                <input value="assignee" id="assignee" type="checkbox"
                                       name="taskInstance" <%= isAssigneeChecked %>><fmt:message
                                    key="bpmn.task.instance.assignee"/><br>
                            </td>
                        </tr>
                        <tr class="tableOddRow">
                            <td>
                                <input value="startUserId" id="startUserId" type="checkbox"
                                       name="processInstance" <%= isStartUserIdChecked %>><fmt:message
                                    key="bpmn.start.user.id"/><br>
                            </td>
                            <td>
                                <input value="taskProcessInstanceId" id="taskProcessId" type="checkbox"
                                       name="taskInstance" <%= isTaskProcessInstanceIdChecked %>><fmt:message
                                    key="bpmn.process.instance.id"/><br>
                            </td>
                        </tr>
                        <tr class="tableEvenRow">
                            <td>
                                <input value="processInstanceStartTime" id="processInstanceStartTime" type="checkbox"
                                       name="processInstance" <%= isProcessStartTimeChecked %>><fmt:message
                                    key="bpmn.instance.start.time"/><br>
                            </td>
                            <td>
                                <input value="taskInstanceStartTime" id="taskInstanceStartTime" type="checkbox"
                                       name="taskInstance" <%= isTaskInstanceStartTimeChecked %>><fmt:message
                                    key="bpmn.instance.start.time"/><br>
                            </td>
                        </tr>
                        <tr class="tableOddRow">
                            <td>
                                <input value="processInstanceEndTime" id="processInstanceEndTime" type="checkbox"
                                       name="processInstance" <%= isProcessEndTimeChecked %>><fmt:message
                                    key="bpmn.instance.end.time"/><br>
                            </td>
                            <td>
                                <input value="taskInstanceEndTime" id="taskInstanceEndTime" type="checkbox"
                                       name="taskInstance" <%= isTaskInstanceEndTimeChecked %>><fmt:message
                                    key="bpmn.instance.end.time"/><br>
                            </td>
                        </tr>
                        <tr class="tableEvenRow">
                            <td>
                                <input value="processInstanceDuration" id="processInstanceDuration" type="checkbox"
                                       name="processInstance" <%= isProcessInstanceDurationChecked %>><fmt:message
                                    key="bpmn.instance.duration"/><br>
                            </td>
                            <td>
                                <input value="taskInstanceDuration" id="taskInstanceDuration" type="checkbox"
                                       name="taskInstance" <%= isTaskInstanceDurationChecked %>><fmt:message
                                    key="bpmn.instance.duration"/><br>
                            </td>
                        </tr>
                        </tbody>
                    </table>

                    <table class="styledLeft">
                        <tr>
                            <td class="buttonRow">
                                <input name="publish" class="button registryWriteOperation" type="button"
                                       style="float: right;"
                                       value="<fmt:message key="bpmn.publish"/>" onclick="validate()"/>
                            </td>
                        </tr>
                    </table>
                </form>
            </div>
        </div>
    </div>

</fmt:bundle>